package codegen.cpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Stack;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import ast.*;
import codegen.Helper;
import codegen.Tag;
import processj.runtime.*;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Visitor;
import utilities.Assert;

// required for finding imports
import java.io.File;

/**
 * A tree walker that collects data from an AST object and then
 * pushes this data into a template to translate a ProcessJ source
 * file to Java code.
 *
 * @param <T>
 *          A visitor interface used to perform operations across a
 *          collection of different objects.
 *
 * @author Ben
 * @version 06/10/2018
 * @since 1.2
 */
public class CodeGenCPP extends Visitor<Object> {

    // String template file locator.
    private final String stGrammarFile = "resources/stringtemplates/cpp/grammarTemplatesCPP.stg";

    // Current java version.
    private final String currentJVM = System.getProperty("java.version");

    // Collection of templates, imported templates, and/or groups that
    // contain formal template definitions.
    private STGroup stGroup;

    // Current compilation unit.
    private Compilation currentCompilation = null;

    // The user working directory.
    private String workingDir = null;

    // Current procedure call.
    private String currentProcName = null;

    // Previous procedure call.
    private String prevProcName = null;

    // Stack of anon proc names
    private Stack<String> anonProcStack = new Stack<String>();

    // Current par-block.
    private String currentParBlock = null;

    // Current array depth reached
    private int currentArrayDepth = 0;

    // Current array type string
    private String currentArrayTypeString = null;

    // Current channel read variable name
    private String currentChannelReadName = null;

    // Current channel read operator
    private String currentChannelReadOp = null;

    // Counter for anonymous processes generated in a par
    private int procCount = 0;

    private int anonProcCount = 0;

    private int protocolCaseIndex = 0;

    // All imports are kept in this table.
    private HashSet<String> importFiles = new LinkedHashSet<String>();

    // Top level declarations.
    private SymbolTable topLevelDecls = null;

    // Contains formal parameters transformed to fields.
    private HashMap<String, String> formalParams = new LinkedHashMap<String, String>();

    // Contains formal parameter names transformed to name tags.
    private HashMap<String, String> paramDeclNames = new LinkedHashMap<String, String>();

    // Contains local parameters transformed to fields.
    private HashMap<String, String> localParams = new LinkedHashMap<String, String>();

    // Contains local param init statements
    private HashMap<String, String> localInits = new LinkedHashMap<String, String>();

    // Contains local param delete statements
    private HashMap<String, String> localDeletes = new LinkedHashMap<String, String>();

    // Contains local param names to be deleted
    private ArrayList<String> localNulls = new ArrayList<String>();

    // Contains record members transformed to fields.
    private HashMap<String, String> recordFields = new LinkedHashMap<String, String>();

    // Contains protocol names and the corresponding tags currently switched on.
    private Hashtable<String, String> protocolTagsSwitchedOn = new Hashtable<String, String>();

    // Maps protocol case names to indexes
    private HashMap<String, Integer> protocolCaseNameIndices = new LinkedHashMap<String, Integer>();

    // List of switch labels.
    private ArrayList<String> switchLabelList = new ArrayList<String>();

    // List of barrier expressions.
    private ArrayList<String> barrierList = new ArrayList<String>();

    // Contains names of classes/procs mapped to their generated names
    private HashMap<String, String> generatedProcNames = new LinkedHashMap<String, String>();

    // boolean flag for including the proper alt fields to a process
    private boolean needsAltLocals = false;

    // Identifier for parameter declaration.
    private int varDecId = 0;

    // Identifier for par-block declaration.
    private int parDecId = 0;

    // Identifier for local variable declaration.
    private int localDecId = 0;

    // Jump label used when procedures yield.
    private int jumpLabel = 0;

    // Access to protocol case.
    private boolean isProtocolCase = false;

    private boolean insideAnonymousProcess = false;

    private int nestedAnonymousProcesses = 0;

    // Access to protocol tag.
    private String currentProtocolTag = null;

    // access to protocol name
    private String currentProtocolName = null;

    // This is used for arrays of N-dimensions.
    private boolean isArrayLiteral = false;

    private final static String DELIMITER = ";";

    /**
     * Internal constructor that loads a group file containing a collection of
     * templates, imported templates, and/or groups containing formal template
     * definitions. Additionally, the constructor initializes a symbol table
     * with top level types declarations.
     *
     * @param s
     *          The top level types which can be procedures, records, protocols,
     *          constants, and/or external types.
     */
    public CodeGenCPP(SymbolTable s) {
        Log.logHeader("*****************************************");
        Log.logHeader("* C O D E - G E N E R A T O R   C P P   *");
        Log.logHeader("*****************************************");
        topLevelDecls = s;
        stGroup = new STGroupFile(stGrammarFile);
    }

    /**
     * Sets the system properties to a current working directory.
     *
     * @param workingDir
     *          A working directory.
     */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * Return a string representing the current working directory.
     */
    public String getWorkingDir() {
        return workingDir;
    }

    /**
     * Visit a single compilation unit which starts with an optional package
     * declaration, followed by zero or more import declarations, followed
     * by zero or more type declarations.
     *
     * @param co
     *          An AST that represents the entire compilation unit.
     * @return A text generated after evaluating this compilation unit.
     */
    @Override
    public Object visitCompilation(Compilation co) {
        Log.log(co, "Visiting a Compilation");

        currentCompilation = co;
        // Code generated by the template.
        String code = null;
        // Instance of Compilation template to fill in.
        ST stCompilation = stGroup.getInstanceOf("Compilation");
        // Reference to all remaining types.
        ArrayList<String> body = new ArrayList<String>();
        // Holds all top level types declarations.
        Sequence<Type> typeDecls = co.typeDecls();
        // Package name for this source file.
        String packagename = co.packageNoName();

        for (Import im : co.imports()) {
            if (im != null)
                importFiles.add((String) im.visit(this));
        }

        for (AST decl : typeDecls) {
            if (decl instanceof Type) {
                // Collect procedures, records, protocols, and external types (if any).
                String t = (String) ((Type) decl).visit(this);
                if (t != null)
                    body.add(t);
            } else if (decl instanceof ConstantDecl) {
                // Iterate over remaining declarations, which is anything that
                // comes after top-level declarations.
                String cd = (String) ((ConstantDecl) decl).visit(this);
                if (cd != null)
                    body.add(cd);
            }
        }

        stCompilation.add("pathName", packagename);
        stCompilation.add("fileName", co.fileName);
        stCompilation.add("name", co.fileNoExtension());
        stCompilation.add("body", body);
        stCompilation.add("version", currentJVM);

        // Add the import statements to the file.
        if (importFiles.size() > 0)
            stCompilation.add("imports", importFiles);

        // Rendered code for debbuging.
        code = stCompilation.render();

        // Code generated by the ProcessJ-CPP compiler.
        Log.logHeader("****************************************");
        Log.logHeader("*      G E N E R A T E D   C O D E     *");
        Log.logHeader("****************************************");
        Log.logHeader(code);

        return code;
    }

    @Override
    public Object visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a ProcTypeDecl (" + pd.name().getname() + ")");

        // Generated template after evaluating this visitor.
        ST stProcTypeDecl = null;
        // Save previous procedure.
        prevProcName = currentProcName;
        // Save previous jump labels.
        ArrayList<String> prevLabels = switchLabelList;
        if (!switchLabelList.isEmpty())
            switchLabelList = new ArrayList<String>();
        // Name of invoked procedure.
        currentProcName = (String) pd.name().visit(this);
        // Procedures are static classes which belong to the same package. To avoid
        // having classes with the same name, we generate a new name for this procedure.
        String procName = null;
        // For non-invocations, that is, for anything other than a procedure
        // that yields, we need to extends the PJProcess class anonymously.
        if ("Anonymous".equals(currentProcName)) {
            Log.log(pd, "Creating anonymous process within " + prevProcName);
            // String thisAnonProcParent = generatedProcNames.get(prevProcName);
            String thisAnonProcParent = !anonProcStack.empty() ? anonProcStack.peek() : generatedProcNames.get(prevProcName);
            String savePrevProcName = prevProcName;

            // Grab the instance for an anonymous procedure.
            stProcTypeDecl = stGroup.getInstanceOf("AnonymousProcess");
            nestedAnonymousProcesses++;
            stProcTypeDecl.add("parent", getParentString());
            insideAnonymousProcess = true;
            // Generate a name
            procName = Helper.makeVariableName(currentProcName + Integer.toString(procCount++) + signature(pd), 0, Tag.PROCEDURE_NAME);
            Log.log(pd, "Generated Proc Name " + procName);
            Log.log(pd, "My previous Proc is " + prevProcName);
            anonProcStack.push(procName);
            // Store generated name for labels of a possible ChannelReadExpr
            generatedProcNames.put(currentProcName, procName);
            // Preserve current jump label.
            int prevJumLabel = jumpLabel;
            jumpLabel = 0;
            // Statements that appear in the procedure.

            Log.log(pd, "visiting body of " + procName);
            String thisAnonProcName = procName;
            Log.log(pd, "saved previous anon proc name " + thisAnonProcParent);
            Log.log(pd, "saved current anon proc name " + thisAnonProcName);
            String[] body = (String[]) pd.body().visit(this);
            Log.log(pd, "body of " + procName + " visited");
            prevProcName = savePrevProcName;

            stProcTypeDecl.add("parBlock", currentParBlock);
            stProcTypeDecl.add("syncBody", body);
            // Add the barrier this procedure should resign from.
            if (Helper.doesProcYield(pd) && !barrierList.isEmpty()) {
                for (int i = 0; i < barrierList.size(); ++i) {
                    Log.log(pd, "barrier added to anon proc: " + barrierList.get(i));
                }
                stProcTypeDecl.add("barrier", barrierList);
            }
            // Add the switch block.
            if (!switchLabelList.isEmpty()) {
                ST stSwitchBlock = stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", switchLabelList);
                // stSwitchBlock.add("name", generatedProcNames.get(currentProcName));
                stSwitchBlock.add("name", procName);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }
            // add a generated name of the process
            // stProcTypeDecl.add("name", Helper.makeVariableName("Anonymous" + Integer.toString(procCount) + signature(pd), 0, Tag.PROCEDURE_NAME));
            stProcTypeDecl.add("name", procName);
            stProcTypeDecl.add("parentClass", thisAnonProcParent);
            stProcTypeDecl.add("anonCounter", procCount);

            // any anonymous process needs access to the arguments of its enclosing class
            if (!formalParams.isEmpty()) {
                stProcTypeDecl.add("types", formalParams.values());
                stProcTypeDecl.add("vars", formalParams.keySet());
            }

            // Restore jump label.
            jumpLabel = prevJumLabel;
            insideAnonymousProcess = false;
            nestedAnonymousProcesses--;
            anonProcStack.pop();
        } else {
            // Restore global variables for a new PJProcess class.
            resetGlobals();
            // Formal parameters that must be passed to the procedure.
            Sequence<ParamDecl> formals = pd.formalParams();

            if (formals != null && formals.size() > 0) {
                // Iterate through and visit every parameter declaration.
                for (int i = 0; i < formals.size(); ++i) {
                    Log.log(pd, "visiting parameters");
                    ParamDecl actualParam = formals.child(i);
                    // Retrieve the name and type of a parameter in the parameter list.
                    // Note that we ignored the value returned by this visitor.
                    actualParam.visit(this);
                }
            } else
                ; // The procedure does not take any parameters.

            // Retrieve modifier(s) attached to invoked procedure such as private,
            // public, protected, etc.
            String[] modifiers = (String[]) pd.modifiers().visit(this);
            // Grab the return type of the invoked procedure.
            String procType = (String) pd.returnType().visit(this);
            // The procedure's annotation determines if we have a yielding procedure
            // or a Java method (a non-yielding procedure).
            boolean doYield = Helper.doesProcYield(pd);
            // Set the template to the correct instance value and then initialize
            // its attributes.
            if (doYield) {
                // This procedure yields! Grab the instance of a yielding procedure
                // from the string template in order to define a new class.
                procName = Helper.makeVariableName(currentProcName + Integer.toString(procCount) + signature(pd), 0, Tag.PROCEDURE_NAME);
                generatedProcNames.put(currentProcName, procName);
                procCount++;
                Log.log("Stored " + currentProcName + "'s helper name as " + generatedProcNames.get(currentProcName) + ".");
                stProcTypeDecl = stGroup.getInstanceOf("ProcClass");
                stProcTypeDecl.add("name", procName);
                // Visit all declarations that appear in the procedure.
                String[] body = (String[]) pd.body().visit(this);

                // The statements that appear in the body of the procedure.
                stProcTypeDecl.add("syncBody", body);
            } else {
                // Otherwise, grab the instance of a non-yielding procedure instead
                // to define a new static Java method.
                procName = Helper.makeVariableName(currentProcName + Integer.toString(procCount) + signature(pd), 0, Tag.METHOD_NAME);
                generatedProcNames.put(currentProcName, procName);
                procCount++;
                Log.log("Stored " + currentProcName + "'s helper name as " + generatedProcNames.get(currentProcName) + ".");
                stProcTypeDecl = stGroup.getInstanceOf("Method");
                stProcTypeDecl.add("name", procName);
                stProcTypeDecl.add("type", procType);
                // Do we have any modifier?
                if (modifiers != null && modifiers.length > 0)
                    stProcTypeDecl.add("modifier", modifiers);
                // Visit all declarations that appear in the procedure.
                String[] body = (String[]) pd.body().visit(this);
                stProcTypeDecl.add("body", body);
            }
            // Create an entry point for the ProcessJ program, which is just
            // a Java main method that is called by the JVM.
            Log.log(pd, "CHECKING FOR MAIN");
            if ("main".equals(currentProcName) && pd.signature().equals(Tag.MAIN_NAME.toString())) {
                Log.log(pd, "FOUND MAIN");
                // Create an instance of a Java main method template.
                ST stMain = stGroup.getInstanceOf("Main");
                stMain.add("class", currentCompilation.fileNoExtension());
                stMain.add("name", procName);
                // Pass the list of command line arguments to this main method.
                if (!formalParams.isEmpty()) {
                    stMain.add("types", formalParams.values());
                    stMain.add("vars", formalParams.keySet());
                    // Here we add argc and argv to the main, so that we can
                    // construct a vector of strings instead of an array
                    // of char*'s, which is much closer to a String array
                    // in java than a bare array in C++ in terms of
                    // functionality
                    // String[] types = {"int32_t", "char*"};
                    // String[] vars  = {"argc", "argv"};
                    // stMain.add("types", types);
                    // stMain.add("vars", vars);
                    // stMain.add("argc", vars[0]);
                    // stMain.add("argv", vars[1]);
                }
                // Add entry point of the program.
                stProcTypeDecl.add("main", stMain.render());
            }

            // The list of command line arguments should be passed to the constructor
            // of the static class that the main method belongs to (some procedure class)
            // or should be passed to the Java method (some static method).
            if (!formalParams.isEmpty()) {
                // Here we match what we did with main above by making space
                // for our arg vector (literally a vector)
                // if ("main".equals(currentProcName) && pd.signature().equals(Tag.MAIN_NAME.toString())) {
                    // String[] types = {"std::vector<std::string>"};
                    // String[] vars  = {"args"};
                //     stProcTypeDecl.add("types", formalParams.values());
                //     stProcTypeDecl.add("vars", formalParams.keySet());
                // }
                // if (!("main".equals(currentProcName) && pd.signature().equals(Tag.MAIN_NAME.toString()))) {
                //     stProcTypeDecl.add("types", formalParams.values());
                //     stProcTypeDecl.add("vars", formalParams.keySet());
                // }

                    stProcTypeDecl.add("types", formalParams.values());
                    stProcTypeDecl.add("vars", formalParams.keySet());
            }
            // The list of local variables defined in the body of a procedure becomes
            // the member variables of the procedure class.
            if (!localParams.isEmpty()) {

            //     Log.log(pd, "sizes are " + localParams.values().size() + ", " + localParams.keySet().size() + ", "
            //                 + localInits.values().size() + ", " + localDeletes.values().size());

            //     for(int i = 0; i < localParams.values().size(); i++) {
            //         Log.log(pd, ((String)localParams.values().toArray()[i]));
            //     }
            //     for(int i = 0; i < localParams.keySet().size(); i++) {
            //         Log.log(pd, ((String)localParams.keySet().toArray()[i]));
            //     }
            //     for(int i = 0; i < localInits.values().size(); i++) {
            //         Log.log(pd, ((String)localInits.values().toArray()[i]));
            //     }
            //     for(int i = 0; i < localDeletes.values().size(); i++) {
            //         Log.log(pd, ((String)localDeletes.values().toArray()[i]));
            //     }

                stProcTypeDecl.add("ltypes", localParams.values());
                stProcTypeDecl.add("lvars", localParams.keySet());
            //     if (!localInits.isEmpty()) {
            //         stProcTypeDecl.add("linits", localInits.values());
            //     }

                if(!localDeletes.isEmpty()) {
                    stProcTypeDecl.add("ldeletes", localDeletes.values());
                }

                if(!localNulls.isEmpty()) {
                    stProcTypeDecl.add("lnulls", localNulls);
                }
            }
            // Add the switch block for resumption.
            if (!switchLabelList.isEmpty()) {
                ST stSwitchBlock = stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", switchLabelList);
                stSwitchBlock.add("name", procName);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }
        }

        // Restore and reset previous values.
        currentProcName = prevProcName;

        // Restore previous jump labels.
        switchLabelList = prevLabels;

        if (needsAltLocals) {
            stProcTypeDecl.add("altLocals", needsAltLocals);
        }

        return stProcTypeDecl.render();
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr be) {
        Log.log(be, "Visiting a BinaryExpr");

        // Generated template after evaluating this visitor.
        ST stBinaryExpr = stGroup.getInstanceOf("BinaryExpr");
        String op = be.opString();
        String lhs = (String) be.left().visit(this);
        lhs = lhs.replace(DELIMITER, "");
        Log.log(be, "lhs is " + lhs);
        if (be.left() instanceof NameExpr) {
            if (insideAnonymousProcess) {
                // lhs = "parent->" + lhs;
            }
        }
        lhs = be.left().hasParens ? "(" + lhs + ")" : lhs;
        String rhs = (String) be.right().visit(this);
        rhs = be.right().hasParens ? "(" + rhs + ")" : rhs;
        rhs = rhs.replace(DELIMITER, "");

        if (be.right() instanceof NameExpr) {
            if (insideAnonymousProcess) {
                // rhs = "parent->" + rhs;
            }
        }

        stBinaryExpr.add("lhs", lhs);
        stBinaryExpr.add("rhs", rhs);
        stBinaryExpr.add("op", op);

        return stBinaryExpr.render();
    }

    @Override
    public Object visitWhileStat(WhileStat ws) {
        Log.log(ws, "Visiting a WhileStat");

        // Generated template after evaluating this visitor.
        ST stWhileStat = stGroup.getInstanceOf("WhileStat");
        // Sequence of statements enclosed in a block statement.
        String[] stats = null;
        String condExpr = null;

        if (ws.expr() != null)
            condExpr = ((String) ws.expr().visit(this)).replace(DELIMITER, "");

        if (ws.stat() != null) {
            Object o = ws.stat().visit(this);
            if (o instanceof String)
                stats = new String[] { (String) o };
            else
                stats = (String[]) o;
        } else // The body of a while-loop could be empty.
            stats = new String[] { DELIMITER };

        stWhileStat.add("expr", condExpr);
        stWhileStat.add("body", stats);

        return stWhileStat.render();
    }

    @Override
    public Object visitDoStat(DoStat ds) {
        Log.log(ds, "Visiting a DoStat");

        // Generated template after evaluating this visitor.
        ST stDoStat = stGroup.getInstanceOf("DoStat");
        // Sequence of statements enclosed in a block statement.
        String[] stats = null;
        String condExpr = null;

        if (ds.expr() != null)
            condExpr = ((String) ds.expr().visit(this)).replace(DELIMITER, "");

        if (ds.stat() != null) {
            Object o = ds.stat().visit(this);
            if (o instanceof String)
                stats = new String[] { (String) o };
            else
                stats = (String[]) o;
        }

        stDoStat.add("expr", condExpr);
        stDoStat.add("body", stats);

        return stDoStat.render();
    }

    @Override
    public Object visitForStat(ForStat fs) {
        Log.log(fs, "Visiting a ForStat");

        // Generated template after evaluating this visitor.
        ST stForStat = stGroup.getInstanceOf("ForStat");
        ArrayList<String> init = new ArrayList<String>();  // Initialization part.
        ArrayList<String> incr = new ArrayList<String>();  // Increment part.

        String initStr = null;
        if (!fs.isPar()) { // Is it a regular for loop?
            if (fs.init() != null) {
                for (Statement st : fs.init()) {
                    if (st != null)
                        // TODO: why does this break?
                        initStr = (String)st.visit(this);
                        if(initStr != null) {
                            init.add(initStr.replace(";", ""));  // Remove the ';' added in LocalDecl.
                        }
                        // init.add((String)st.visit(this));
                }
            }

            if (fs.incr() != null) {
                for (ExprStat expr : fs.incr())
                    incr.add(((String) expr.visit(this)).replace(";", ""));
            }

            if (fs.expr() != null) {
                String expr = ((String) fs.expr().visit(this)).replace(";", "");
                stForStat.add("expr", expr);
            }

            // Sequence of statements enclosed in a block statement.
            if (fs.stats() != null) {
                Object o = fs.stats().visit(this);
                stForStat.add("stats", o);
            }
        } else // No! then this is a par-for.
            ;

        if (!init.isEmpty())
            stForStat.add("init", init);
        if (!incr.isEmpty())
            stForStat.add("incr", incr);

        return stForStat.render();
    }

    @Override
    public Object visitContinueStat(ContinueStat cs) {
        Log.log(cs, "Visiting a ContinueStat");

        // Generated template after evaluating this visitor.
        ST stContinueStat = stGroup.getInstanceOf("ContinueStat");
        String name = null;
        // If target is not 'null' then we have a label to jump to.
        if (cs.target() != null) {
            name = (String) cs.target().visit(this);
            stContinueStat.add("name", name);
        }

        return stContinueStat.render();
    }

    @Override
    public Object visitIfStat(IfStat is) {
        Log.log(is, "Visiting a IfStat");

        // Generated template after evaluating this visitor.
        ST stIfStat = stGroup.getInstanceOf("IfStat");
        // Sequence of statements enclosed in a block statement.
        String[] thenStats = null;
        String[] thenParts = null;
        String condExpr = null;

        if (is.expr() != null)
            condExpr = (String) is.expr().visit(this);
        if (is.thenpart() != null) {
            if (is.thenpart() instanceof Block)
                thenStats = (String[]) is.thenpart().visit(this);
            else {
                String stat = (String) is.thenpart().visit(this);
                thenStats = new String[] { stat };
            }
        }
        if (is.elsepart() != null) {
            if (is.elsepart() instanceof Block)
                thenParts = (String[]) is.elsepart().visit(this);
            else {
                String stat = (String) is.elsepart().visit(this);
                thenParts = new String[] { stat };
            }
        }

        stIfStat.add("expr", condExpr);
        stIfStat.add("thenPart", thenStats);
        stIfStat.add("elsePart", thenParts);

        return stIfStat.render();
    }

    @Override
    public Object visitAssignment(Assignment as) {
        Log.log(as, "Visiting an Assignment");

        // Generated template after evaluating this visitor.
        ST stVar = stGroup.getInstanceOf("Var");

        String op = (String) as.opString();
        String lhs = null;
        String rhs = null;

        if (as.left() != null)
            lhs =  (String) as.left().visit(this);

        if (as.right() instanceof NewArray) {
            return createNewArray(lhs, ((NewArray) as.right()));
        }
        else if (as.right() instanceof ChannelReadExpr) {
            Log.log("visitAssignment: returning createChannelReadExpr");
            return createChannelReadExpr(lhs, op, ((ChannelReadExpr) as.right()));
        } else {
            if (as.right() != null) {
                Log.log("visitAssignment: as.right() != null");
                rhs = (String) as.right().visit(this);
                rhs = rhs.replace(DELIMITER, "");
            }
        }

        // if (insideAnonymousProcess == true) {
        //     lhs = "parent->"+lhs;
        // }
        stVar.add("name", lhs);

        stVar.add("val", rhs);
        stVar.add("op", op);

        return stVar.render();
    }

    @Override
    public Object visitParamDecl(ParamDecl pd) {
        Log.log(pd, "Visiting a ParamDecl (" + pd.type().typeName() + " " + pd.paramName().getname() + ")");

        // Grab the type and name of a declared variable.
        String name = (String) pd.paramName().visit(this);
        String type = (String) pd.type().visit(this);

        // Temporary _silly_ fixed for channel types.
        if (pd.type().isChannelType() || pd.type().isChannelEndType()) {
            // type = PJChannel.class.getSimpleName() + type.substring(type.indexOf("<"), type.length());
            // TODO: make this build the appropriate type a la C++
            Log.log(pd, "in visitParamDecl() type is " + type);
        }

        // If it needs to be a pointer, make it so
        if(!(pd.type() instanceof NamedType && ((NamedType)pd.type()).type().isProtocolType()) &&  (pd.type().isBarrierType() || !(pd.type().isPrimitiveType() || pd.type().isArrayType()))) {
            Log.log(pd, "appending a pointer specifier to type of " + name);
            type += "*";
        }

        // Create a tag for this parameter and then add it to the collection
        // of parameters for reference.
        String newName = Helper.makeVariableName(name, ++varDecId, Tag.PARAM_NAME);
        formalParams.put(newName, type);
        paramDeclNames.put(name, newName);

        // Ignored the value returned by this visitor. The types and variables
        // are _always_ resolved elsewhere.
        return null;
    }

    @Override
    public Object visitLocalDecl(LocalDecl ld) {
        Log.log(ld, "Visting a LocalDecl (" + ld.type().typeName() + " " + ld.var().name().getname() + ")");

        // We could have the following targets:
        //   1.) T x;                                         // A declaration
        //   2.) T x = 4;                                     // A simple declaration
        //   3.) T x = in.read();                             // A single channel read
        //   4.) T x = a.read() + b.read() + ... + z.read();  // Multiple channel reads
        //   5.) T x = read();                                // A Java method that returns a value
        //   6.) T x = a + b;                                 // A binary expression
        //   7.) T x = a = b ...;                             // A complex assignment statement
        String name = ld.var().name().getname();
        String type = (String) ld.type().visit(this);
        String val = null;
        String chanType = type;

        // Create a tag for this local declaration
        String newName = Helper.makeVariableName(name, ++localDecId, Tag.LOCAL_NAME);

        // If it needs to be a pointer, make it so
        if(!(ld.type() instanceof NamedType && ((NamedType)ld.type()).type().isProtocolType()) && (ld.type().isBarrierType() /*|| ld.type().isTimerType() */|| !(ld.type().isPrimitiveType() || ld.type().isArrayType()))) {
            Log.log(ld, "appending a pointer specifier to type of " + name);
            type += "*";
        }

        localParams.put(newName, type);
        paramDeclNames.put(name, newName);

        if (ld.type().isBarrierType()) {
            Log.log("SHOULD NOT HAVE PARENT APPENDED");
        }

        if (nestedAnonymousProcesses > 0 &&
            !ld.type().isBarrierType()) {
            newName = getParentString() + newName;
        }

        // This variable could be initialized, e.g. through an assignment operator
        Expression expr = ld.var().init();


        if (/*expr == null && */ /* ld.type().isTimerType() ||*/
            ld.type().isBarrierType() ||
            ld.type().isChannelType() ||
            ld.type() instanceof NamedType && ((NamedType)ld.type()).type().isRecordType() ||
            ld.type().isArrayType() ||
            ld.type() instanceof NamedType && ((NamedType)ld.type()).type().isProtocolType()) {
        // if(expr != null && !(ld.type() instanceof NamedType && ((NamedType)ld.type()).type().isProtocolType()) && (ld.type().isBarrierType() /*|| ld.type().isTimerType() */|| !(ld.type().isPrimitiveType() || ld.type().isArrayType()))) {
            Log.log(ld, "creating delete statement for " + name);
            String deleteStmt = "";
            if (expr != null && expr instanceof ProtocolLiteral) {
                Log.log("Should create delete stmt for protocol literal");
                String protoType = ((ProtocolLiteral)expr).name().getname();
                String protoTag = ((ProtocolLiteral)expr).tag().getname();
                // deleteStmt = "if (" + newName + ") { delete reinterpret_cast<" +
                //     protoType + "::" + protoTag + "*>(" + newName + "); }";
            } else if (expr == null) {
                deleteStmt = "if (" + newName + ") { delete " + newName + "; }";
            }
            localDeletes.put(name, deleteStmt);
        }

        if (ld.type().isArrayType()) {
            currentArrayTypeString = type;
            currentArrayDepth = ((ArrayType)ld.type()).getActualDepth() - 1;
        }

        // Visit the expressions associated with this variable
        if (expr != null) {
            if (ld.type() instanceof PrimitiveType) {
                val = (String) expr.visit(this);
            }
            else if (ld.type() instanceof NamedType) {// Must be a record or protocol
                currentProtocolName = newName;
                val = (String) expr.visit(this);
            }
            else if (ld.type() instanceof ArrayType) {
                val = (String) expr.visit(this);
            }
        }

        // Is it a barrier declaration? If so, we must generate code that
        // creates a barrier object
        if (ld.type().isBarrierType() && expr == null) {
            ST stBarrierDecl = stGroup.getInstanceOf("BarrierDecl");
            val = stBarrierDecl.render();
        }
        // Is it a simple declaration for a channel type? If so, and since
        // channels cannot be created using the operator 'new', we generate
        // code to create a channel object
        if (ld.type().isChannelType() && expr == null) {
            ST stChannelDecl = stGroup.getInstanceOf("ChannelDecl");
            stChannelDecl.add("type", chanType);
            val = stChannelDecl.render();
        }
        // After making this local declaration a field of the procedure
        // in which it was declared, we return iff this local variable
        // is not initialized
        if (expr == null) {
            Log.log(ld, "LocalDecl " + name + " is not initialized.");
            if (!ld.type().isBarrierType() && (ld.type().isPrimitiveType() ||
                ld.type().isArrayType() ||  // Could be an uninitialized array declaration
                ld.type().isNamedType()))   // Could be a record or protocol declaration
                //return null;                // The 'null' value is used to removed empty
                                            // sequences in the generated code

                // if it's a protocol, it can't have a null initializer
                if (ld.type() instanceof NamedType && ((NamedType)ld.type()).type().isProtocolType()) {
                    return null;
                } else {
                    val = "static_cast<" + type + ">(0)";
                }
        }

        // If we reach this section of code, then we have a variable
        // declaration with some initial value
        if (val != null)
            val = val.replace(DELIMITER, "");

        ST stVar = stGroup.getInstanceOf("Var");
        stVar.add("type", type);
        stVar.add("name", newName);
        stVar.add("val", val);

        Log.log(ld, "stVarStr is " + stVar.render());

        return stVar.render();
    }

    // @Override
    public Object visitLocalDeclOld(LocalDecl ld) {
        Log.log(ld, "Visting a LocalDecl (" + ld.type().typeName() + " " + ld.var().name().getname() + ")");

        // We could have the following targets:
        //      T x;                              -> a declaration
        //      T x = 4;                          -> a simple declaration
        //      T x = in.read();                  -> a single channel read
        //      T x = b.read() + c.read() + ...;  -> multiple channel reads
        //      T x = read();                     -> a Java method that returns a value
        //      T x = a + b;                      -> a binary expression
        //      T x = a = b ...;                  -> a complex assignment
        String name = (String) ld.var().name().getname();
        String type = (String) ld.type().visit(this);
        String val = null;

        String chantype = type;
        // The channel type, e.g., one-2-one, one-2-many, many-2-one, many-to-many.
        Log.log(ld, "in visitLocalDecl(): type is " + type + ".");
        // if (ld.type().isChannelType() || ld.type().isChannelEndType())
        //     type = PJChannel.class.getSimpleName() + type.substring(type.indexOf("<"), type.length());

        // Create a tag for this local channel expression parameter.
        String newName = Helper.makeVariableName(name, ++localDecId, Tag.LOCAL_NAME);

        // This variable could be initialized, e.g., through an assignment operator.
        Expression expr = ld.var().init();

        // If it needs to be a pointer, make it so
        if(!(ld.type() instanceof NamedType && ((NamedType)ld.type()).type().isProtocolType()) && (ld.type().isBarrierType() /*|| ld.type().isTimerType() */|| !(ld.type().isPrimitiveType() || ld.type().isArrayType()))) {
            Log.log(ld, "appending a pointer specifier to type of " + name);
            type += "*";
        }
        if (expr == null &&/* ld.type().isTimerType() ||*/ ld.type().isBarrierType()/* || !(ld.type().isPrimitiveType())*/) {
            Log.log(ld, "creating delete statement for " + name);
            String deleteStmt = "delete " + newName + ";";
            localDeletes.put(name, deleteStmt);
        }
        localParams.put(newName, type);
        paramDeclNames.put(name, newName);

        if (ld.type().isArrayType()) {
            currentArrayTypeString = type;
            currentArrayDepth = ((ArrayType)ld.type()).getActualDepth() - 1;
        }

        // Visit the expressions associated with this variable.
        if (expr != null) {
            if (expr instanceof ChannelReadExpr) {
                // Save the name of the variable if it's a channel read so we can read to it
                currentChannelReadName = newName;

                // This is always assignment in the case of a read
                // but it doesn't hurt to make it a variable in
                // case we decide to add more functionality
                currentChannelReadOp = "=";
            }
            if (ld.type() instanceof PrimitiveType)
                val = (String) expr.visit(this);
            else if (ld.type() instanceof NamedType) // Must be a record or protocol.
                val = (String) expr.visit(this);
            else if (ld.type() instanceof ArrayType)
                val = (String) expr.visit(this);
        }

        // Is it a barrier declaration? If so, we must generate code that
        // creates a barrier object.
        if (ld.type().isBarrierType() && expr == null) {
            ST stBarrierDecl = stGroup.getInstanceOf("BarrierDecl");
            val = stBarrierDecl.render();
        }
        // Is it a simple declaration for a channel type? If so, and since
        // channels cannot be created using the operator 'new', we generate
        // code to create a channel object.
        if (ld.type().isChannelType() && expr == null) {
            ST stChannelDecl = stGroup.getInstanceOf("ChannelDecl");
            stChannelDecl.add("type", chantype);
            val = stChannelDecl.render();
        }

        // After making this local declaration a field of the procedure
        // in which it was declared, we return if and only if this local
        // variable is not initialized.
        if (expr == null) {
            Log.log(ld, name + "'s expr is null.");
            // if (!ld.type().isBarrierType() &&
            //     !ld.type().isChannelType() && (ld.type().isPrimitiveType() ||
            //     ld.type().isArrayType() ||    // Could be an uninitialized array declaration.
            //     ld.type().isNamedType())) {   // Could be records or protocols.
                // Log.log(ld, name + " has a 0 initializer.");
                // TODO: static cast this to the type of the variable
                // localInits.put(name, "0");
                // TODO: do we need this as an init? probably not...

            localInits.put(name, "static_cast<" + type + ">(0)");

            if (ld.type().isTimerType()) {
                Log.log(ld, "Timer needs null initializer (for now)");
                // TODO: this may have to call some function that gets the
                // timer's length, or we need to figure out how to redirect
                // and rewrite the timerRead ST definition to be what we
                // place here.
                // val = "static_cast<" + type + ">(0)";
                val = "new ProcessJRuntime::pj_timer(this, 0)";
            }

            if (ld.type() instanceof PrimitiveType && ((PrimitiveType)ld.type()).isNumericType()) {
                val = "static_cast<" + type + ">(0)";
            }
            // if (!ld.type().isChannelType()) {

            //     localInits.put(name, "static_cast<" + type + ">(0)");
            //         return null;              // The 'null' is used to removed empty sequences.
            // } else {
            //     localInits.put(name, val);
            // }
            // }
        } else {
            if (ld.type() instanceof PrimitiveType && ((PrimitiveType)ld.type()).isStringType()) {
                localInits.put(name, "\"\"");
            } else if (ld.type() instanceof RecordTypeDecl && ((RecordTypeDecl)ld.type()).isRecordType()) {
                val = "static_cast<" + type + ">(0)";
            } else if (ld.type().isProtocolType()) {
                // TODO: find out how to fix this problem, variants cannot
                // have null initializers. need to rework this so that we
                // don't have two inits. ugh.
            } else {
                localInits.put(name, "0");
            }
        }

        // If we reach this section of code, then we have a variable
        // declaration with some initial value(s).
        if ((val != null) && !(expr instanceof ChannelReadExpr)) {
            val = val.replace(DELIMITER, "");
        }

        if (expr instanceof ChannelReadExpr) {
            if (ld.type().isPrimitiveType()) {
                // TODO: do we need this as an init? probably not...
                // localInits.put(newName, (((PrimitiveType)ld.type()).getKind() == PrimitiveType.StringKind) ? "\"\"" : "0");
            } else if (ld.type().isNamedType() ||
                       ld.type().isArrayType()) {
                // TODO: do we need this as an init? probably not...
                // localInits.put(name, "nullptr");
            }
        } else {
            // We need to store the initializer so we can build the locals later
            // localInits.put(newName, val);
        }

        ST stVar = stGroup.getInstanceOf("Var");
        // stVar.add("type", type);
        stVar.add("name", newName);
        stVar.add("val", val);

        String stVarStr = stVar.render();
        Log.log(ld, "In visitLocalDecl(): stVarStr is " + stVarStr + ".");

        // reset channel read name
        currentChannelReadName = null;
        currentChannelReadOp = null;

        if (expr instanceof ChannelReadExpr) {
            return val;
        }
        return stVarStr;
        // return null;
    }

    @Override
    public Object visitExprStat(ExprStat es) {
        Log.log(es, "Visiting an ExprStat");

        return es.expr().visit(this);
    }

    @Override
    public Object visitName(Name na) {
        Log.log(na, "Visiting a Name (" + na.getname() + ")");

        String name = null;

        if (!paramDeclNames.isEmpty() && paramDeclNames.containsKey(na.getname()))
            name = paramDeclNames.get(na.getname());

        if (name == null)
            name = na.getname();

        return name;
    }

    @Override
    public Object visitNameExpr(NameExpr ne) {
        Log.log(ne, "Visiting a NameExpr (" + ne.name().getname() + ")");

        // NameExpr always points to 'myDecl'.
        if (nestedAnonymousProcesses > 0 &&
            !(ne.myDecl instanceof ConstantDecl)) {
            return getParentString() + ne.name().visit(this);
        }
        return ne.name().visit(this);
    }

    @Override
    public Object visitNamedType(NamedType nt) {
        Log.log(nt, "Visiting a NamedType (" + nt.name().getname() + ")");

        String type = (String) nt.name().getname();
        // This is for protocol inheritance.
        if (nt.type() != null && nt.type().isProtocolType()) {
            // type = PJProtocolCase.class.getSimpleName();
            type = "ProcessJRuntime::pj_protocol_case*";
        }

        return type;
    }

    @Override
    public Object visitNewArray(NewArray ne) {
        Log.log(ne, "Visiting a NewArray");
        Object o = createNewArray(null, ne);

        if(o instanceof String) {
            Log.log(ne, "array template is " + o);
        }
        // return createNewArray(null, ne);
        return o;
    }

    @Override
    public Object visitPrimitiveType(PrimitiveType py) {
        Log.log(py, "Visiting a Primitive Type (" + py.typeName() + ")");

        // ProcessJ primitive types that do not translate directly
        // to Java primitive types.
        String typeStr = py.typeName();
        if (py.isStringType()) {
            // typeStr = "char*";
            typeStr = "std::string";
        } else if (py.isBooleanType()) {
            typeStr = "bool";
        } else if (py.isTimerType()) {
            // typeStr = PJTimer.class.getSimpleName();
            typeStr = "ProcessJRuntime::pj_timer*";
        } else if (py.isBarrierType()) {
            // typeStr = PJBarrier.class.getSimpleName();
            typeStr = "ProcessJRuntime::pj_barrier";
        }

        return typeStr;
    }

    @Override
    public Object visitPrimitiveLiteral(PrimitiveLiteral li) {
        Log.log(li, "Visiting a Primitive Literal (" + li.getText() + ")");

        ST stPrimitiveLiteral = stGroup.getInstanceOf("PrimitiveLiteral");
        if (li.isSuffixed()) {
            stPrimitiveLiteral.add("type", li.suffix());
        }
        stPrimitiveLiteral.add("value", li.getText());

        return stPrimitiveLiteral.render();
    }

    @Override
    public Object visitChannelType(ChannelType ct) {
        Log.log(ct, "Visiting a ChannelType (" + ct + ")");

        // Channel class type.
        String chantype = "";
        switch (ct.shared()) {
        case ChannelType.NOT_SHARED:
            chantype = "ProcessJRuntime::pj_one2one_channel";
            break;
        case ChannelType.SHARED_READ:
            // chantype = "ProcessJRuntime::pj_one2many_channel";
            chantype = "ProcessJRuntime::pj_many2many_channel";
            break;
        case ChannelType.SHARED_WRITE:
            // chantype = "ProcessJRuntime::pj_many2one_channel";
            chantype = "ProcessJRuntime::pj_many2many_channel";
            break;
        case ChannelType.SHARED_READ_WRITE:
            chantype = "ProcessJRuntime::pj_many2many_channel";
            break;
        }
        // Resolve parameterized type for channel, e.g., chan<T>
        // where 'T' is the type to be resolved.
        String type = getChannelType(ct.baseType());
        // String type = getCPPChannelType(ct.baseType());

        // If it needs to be a pointer, make it so
        if(!(ct.baseType() instanceof NamedType && ((NamedType)ct.baseType()).type().isProtocolType()) && (ct.baseType().isBarrierType() /*|| ct.baseType().isTimerType() */|| !(ct.baseType().isPrimitiveType() || ct.baseType().isArrayType()))) {
            Log.log(ct, "appending a pointer specifier to type of " + ct);
            type += "*";
        }

        return chantype + "<" + type + ">";
    }

    @Override
    public Object visitChannelEndExpr(ChannelEndExpr ce) {
        Log.log(ce, "Visiting a ChannelEndExpr (" + (ce.isRead() ? "read" : "write") + ")");

        String channel = (String) ce.channel().visit(this);

        return channel;
    }

    @Override
    public Object visitChannelEndType(ChannelEndType ct) {
        Log.log(ct, "Visiting a ChannelEndType (" + ct.typeName() + ")");

        // Channel class type.
        // String chanType = PJOne2OneChannel.class.getSimpleName();
        String chanType = "ProcessJRuntime::pj_one2one_channel";
        if (ct.isShared()) {  // Is it a shared channel?
            if (ct.isRead())  // One-2-many channel.
                // chanType = PJOne2ManyChannel.class.getSimpleName();
                // chanType = "ProcessJRuntime::pj_one2many_channel";
                chanType = "ProcessJRuntime::pj_many2many_channel";
            else if (ct.isWrite()) // Many-2-one channel.
                // chanType = PJMany2OneChannel.class.getSimpleName();
                // chanType = "ProcessJRuntime::pj_many2one_channel";
                chanType = "ProcessJRuntime::pj_many2many_channel";
            else // Many-2-many channel.
                // chanType = PJMany2ManyChannel.class.getSimpleName();
                chanType = "ProcessJRuntime::pj_many2many_channel";
        }
        // Resolve parameterized type for channels, e.g., chan<T>
        // where 'T' is the type to be resolved.
        String type = getChannelType(ct.baseType());
        // STring type = getCPPChannelType(ct.baseType());

        // If it needs to be a pointer, make it so
        if(!(ct.baseType() instanceof NamedType && ((NamedType)ct.baseType()).type().isProtocolType()) && (ct.baseType().isBarrierType() /*|| ct.baseType().isTimerType() */|| !(ct.baseType().isPrimitiveType() || ct.baseType().isArrayType()))) {
            Log.log(ct, "appending a pointer specifier to type of " + ct);
            type += "*";
        }

        return chanType + "<" + type + ">";
    }

    @Override
    public Object visitChannelWriteStat(ChannelWriteStat cw) {
        Log.log(cw, "Visiting a ChannelWriteStat");

        // Generated template after evaluating this visitor.
        ST stChanWriteStat = stGroup.getInstanceOf("ChanWriteStat");
        // 'c.write(x)' is a channel-end expression, where 'c'
        // is the writing end of a channel.
        Expression chanExpr = cw.channel();
        // 'c' is the name of the channel.
        String chanWriteName = (String) chanExpr.visit(this);
        // Expression sent through channel.
        String expr = (String) cw.expr().visit(this);
        expr = expr.replace(DELIMITER, "");

        int countLabel = 1; // One for the 'runLabel'.
        // Is the writing end of this channel shared?
        if (chanExpr.type.isChannelEndType() && ((ChannelEndType) chanExpr.type).isShared()) {
            stChanWriteStat = stGroup.getInstanceOf("ChannelMany2One");
            ++countLabel;
        }

        if (cw.expr() instanceof CastExpr &&
            ((CastExpr)cw.expr()).expr() instanceof NameExpr &&
            ((NameExpr)((CastExpr)cw.expr()).expr()).myDecl instanceof LocalDecl &&
            ((LocalDecl)((NameExpr)((CastExpr)cw.expr()).expr()).myDecl).type() instanceof NamedType &&
            ((NamedType)((LocalDecl)((NameExpr)((CastExpr)cw.expr()).expr()).myDecl).type()).type().isRecordType()) {
            Log.log(cw, "adding null for sent pointer");
            localNulls.add(paramDeclNames.get(((NameExpr)((CastExpr)cw.expr()).expr()).name().getname()) + "= nullptr;");
        }

        stChanWriteStat.add("chanName", chanWriteName);
        stChanWriteStat.add("writeExpr", expr);
        Log.log(cw, "proc name is " + currentProcName + ", = " + generatedProcNames.get(currentProcName));
        stChanWriteStat.add("procName", generatedProcNames.get(currentProcName));

        // Add the switch block for resumption.
        for (int label = 0; label < countLabel; ++label) {
            // Increment the jump label.
            stChanWriteStat.add("resume" + label, ++jumpLabel);
            // Add the jump label to the switch list.
            switchLabelList.add(renderSwitchLabel(jumpLabel));
        }

        return stChanWriteStat.render();
    }

    @Override
    public Object visitChannelReadExpr(ChannelReadExpr cr) {
        Log.log(cr, "Visiting a ChannelReadExpr");

        return createChannelReadExpr(currentChannelReadName, currentChannelReadOp, cr);

        // TODO: ensure that this did not break anything via tests

        // // Generated template after evaluating this visitor.
        // ST stChannelReadExpr = stGroup.getInstanceOf("ChannelReadExpr");
        // // 'c.read()' is a channel-end expression, where 'c'
        // // is the reading end of a channel.
        // Expression chanExpr = cr.channel();
        // // 'c' is the name of the channel.
        // String chanEndName = (String) chanExpr.visit(this);
        // stChannelReadExpr.add("chanName", chanEndName);
        // // Add the 'switch' block for resumption.
        // for (int label = 0; label < 2; ++label) {
        //     // Increment jump label.
        //     stChannelReadExpr.add("resume" + label, ++jumpLabel);
        //     // Add jump label to the switch list.
        //     switchLabelList.add(renderSwitchLabel(jumpLabel));
        // }
        // stChannelReadExpr.add("procName", generatedProcNames.get(currentProcName));

        // if (currentChannelReadName != null) {
        //     stChannelReadExpr.add("lhs", currentChannelReadName);
        //     stChannelReadExpr.add("op", currentChannelReadOp);
        // }

        // return stChannelReadExpr.render();
    }

    @Override
    public Object visitVar(Var va) {
        Log.log(va, "Visiting a Var (" + va.name().getname() + ")");

        // Generated template after evaluating this visitor.
        ST stVar = stGroup.getInstanceOf("Var");
        // Returned values for name and expression (if any).
        String name = (String) va.name().visit(this);

        String exprStr = null;
        // This variable could be initialized, e.g., through an assignment
        // operator.
        Expression expr = va.init();
        // Visit the expressions associated with this variable.
        if (expr != null) {
            // This is safe for when our target is not too complicated, e.g.,
            // initializing variables with primitives or string literals.
            exprStr = (String) expr.visit(this);
            stVar.add("val", exprStr);
        }

        stVar.add("name", name);

        return stVar.render();
    }

    @Override
    public Object visitArrayAccessExpr(ArrayAccessExpr ae) {
        Log.log(ae, "Visiting an ArrayAccessExpr");

        // Generated template after evaluating this visitor.
        ST stArrayAccessExpr = stGroup.getInstanceOf("ArrayAccessExpr");
        String name = (String) ae.target().visit(this);
        String index = (String) ae.index().visit(this);

        stArrayAccessExpr.add("name", name);
        stArrayAccessExpr.add("index", index);

        return stArrayAccessExpr.render();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object visitArrayLiteral(ArrayLiteral al) {
        Log.log(al, "Visiting an ArrayLiteral");

        // Is the array initialize at compile time? If so, create a
        // list of values separated by commas and enclosed between
        // braces (note, the syntax for array literals in ProcessJ
        // is different to Java's).
        if (al.elements().size() > 1 || isArrayLiteral) {
            // The following extends naturally to two-dimensional, and
            // even higher-dimensional arrays -- but they are not used
            // very often in practice.
            String previousArrayTypeString = currentArrayTypeString;
            String type = getNestedArrayType();
            currentArrayTypeString = type;
            String[] vals = (String[]) al.elements().visit(this);
            currentArrayTypeString = previousArrayTypeString;
            return "new " +
                   type +
                   Arrays.asList(vals)
                    .toString()
                    .replace("[", " { ")
                    .replace("]", " } ");
        }

        return al.elements().visit(this);
    }

    @Override
    public Object visitArrayType(ArrayType at) {
        Log.log(at, "Visiting an ArrayType (" + at.typeName() + ")");

        String stArrayType = (String) at.baseType().visit(this);

        Log.log(at, "stArrayType is " + stArrayType);

        if (!at.baseType().isPrimitiveType() || !at.baseType().isNamedType()) {
            if (at.getActualDepth() > 1) {
                Log.log(at, "depth > 1, pj_md_array used");
                return "ProcessJRuntime::pj_md_array<" + stArrayType + ">*";
            }
            // return "ProcessJRuntime::pj_array<" + stArrayType + ">*";
        }
        Log.log(at, "base of array, pj_array used");
        return "ProcessJRuntime::pj_array<" + stArrayType + ">*";
    }

    @Override
    public Object visitModifier(Modifier mo) {
        Log.log(mo, "Visiting a Modifier (" + mo + ")");

        // Type of modifiers: public, protected, private, etc.
        return mo.toString();
    }

    @Override
    public Object visitBlock(Block bl) {
        Log.log(bl, "Visiting a Block");

        // The scope in which declarations appear, starting with their
        // own initializers and including any further declarations such
        // invocations or sequence of statements.
        String[] stats = (String[]) bl.stats().visit(this);

        return stats;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object visitSequence(Sequence se) {
        Log.log(se, "Visiting a Sequence");

        // Sequence of statements enclosed in a block statement.
        ArrayList<String> seqs = new ArrayList<String>();
        // Iterate through every statement.
        for (int i = 0; i < se.size(); ++i) {
            if (se.child(i) != null) {
                Object stats = se.child(i).visit(this);
                if (stats == null)
                    continue;
                // These are either
                //      1.) a sequence of statements, or
                //      2.) a single statement
                // found in a block statement, e.g. local declarations,
                // variable declarations, invocations, etc.
                if (stats instanceof String[]) {
                    String[] statsStr = (String[]) stats;
                    seqs.addAll(Arrays.asList(statsStr));
                } else {
                    seqs.add((String) stats);
                }
            }
        }

        return seqs.toArray(new String[0]);
    }

    @Override
    public Object visitBreakStat(BreakStat bs) {
        Log.log(bs, "Visiting a BreakStat");

        // Generated template after evaluating this visitor.
        ST stBreakStat = stGroup.getInstanceOf("BreakStat");

        if (bs.target() != null) // No parse tree for 'break'.
            stBreakStat.add("name", bs.target().visit(this));

        return stBreakStat.render();
    }

    @Override
    public Object visitSwitchLabel(SwitchLabel sl) {
        Log.log(sl, "Visiting a SwitchLabel");

        // Generated template after evaluating this visitor.
        ST stSwitchLabel = stGroup.getInstanceOf("SwitchLabel");

        // This could be a default label, in which case, expr()
        // would be 'null'.
        String label = null;
        if (!sl.isDefault())
            label = (String) sl.expr().visit(this);
        if (isProtocolCase) {
            // Silly way to keep track of a protocol tag, however, this
            // should (in theory) _always_ work.
            // label = "\"" + label + "\"";
            label = "case " + Integer.toString(protocolCaseNameIndices.get(sl.expr().toString()));
            currentProtocolTag = sl.expr().toString();
            return label;
        }
        stSwitchLabel.add("label", label);

        return stSwitchLabel.render();
    }

    @Override
    public Object visitSwitchGroup(SwitchGroup sg) {
        Log.log(sg, "Visit a SwitchGroup");

        // Generated template after evaluating this visitor.
        ST stSwitchGroup = stGroup.getInstanceOf("SwitchGroup");

        ArrayList<String> labels = new ArrayList<String>();
        for (SwitchLabel sl : sg.labels())
            labels.add((String) sl.visit(this));

        ArrayList<String> stats = new ArrayList<String>();
        for (Statement st : sg.statements()) {
            if (st == null)
                continue;
            stats.add((String) st.visit(this));
        }

        stSwitchGroup.add("labels", labels);
        stSwitchGroup.add("stats", stats);

        return stSwitchGroup.render();
    }

    @Override
    public Object visitSwitchStat(SwitchStat st) {
        Log.log(st, "Visiting a SwitchStat");

        // Is this a protocol tag?
        if (st.expr().type.isProtocolType()) {
            isProtocolCase = true;
            // Log.log(st, "Generating protocol choice for type " + st.expr().type);
            // return generateProtocolChoice(st);
        }

        // Generated template after evaluating this visitor.
        ST stSwitchStat = stGroup.getInstanceOf("SwitchStat");

        String expr = (String) st.expr().visit(this);
        ArrayList<String> switchGroup = new ArrayList<String>();

        for (SwitchGroup sg : st.switchBlocks())
            switchGroup.add((String) sg.visit(this));

        stSwitchStat.add("tag", isProtocolCase);
        stSwitchStat.add("expr", expr);
        stSwitchStat.add("block", switchGroup);

        // Reset the value for this protocol tag.
        isProtocolCase = false;

        return stSwitchStat.render();
    }

    @Override
    public Object visitCastExpr(CastExpr ce) {
        Log.log(ce, "Visiting a CastExpr");

        // Generated template after evaluating this invocation.
        ST stCastExpr = stGroup.getInstanceOf("CastExpr");
        // This result in:
        //      ((<type>) (<expr>))
        String type = (String) ce.type().visit(this);
        String expr = (String) ce.expr().visit(this);

        // If it needs to be a pointer, make it so
        if(!(ce.type() instanceof NamedType && ((NamedType)ce.type()).type().isProtocolType()) && (ce.type().isBarrierType() /*|| ce.type().isTimerType() */|| !(ce.type().isPrimitiveType() || ce.type().isArrayType()))) {
            Log.log(ce, "appending a pointer specifier to type of " + expr);
            type += "*";
        }

        stCastExpr.add("type", type);
        stCastExpr.add("expr", expr);

        return stCastExpr.render();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object visitInvocation(Invocation in) {
        // Ignore anything that is not a procedure invocation.
        if (in.ignore) {
            Log.log(in, "Visiting a label/goto point.");

            ST stIgnore = stGroup.getInstanceOf("InvocationIgnore");
            stIgnore.add("name", in.procedureName().visit(this));
            stIgnore.add("var", in.params().visit(this));

            // TODO: might need this, but might not. check!!!
            // String[] params = (String[]) in.params().visit(this);

            // if(params != null) {
            //     if (insideAnonymousProcess == true) {
            //         for (int i = 0; i < params.length; i++) {
            //             params[i] = "parent->" + params[i];
            //         }
            //     }
            // }

            // stIgnore.add("var", params);

            return stIgnore.render();
        }

        Log.log(in, "Visiting Invocation (" + in.targetProc.name().getname() + ")");

        // Generated template after evaluating this invocation.
        ST stInvocation = null;
        // Target procedure.
        ProcTypeDecl pd = in.targetProc;
        // Name of invoked procedure.
        String pdName = pd.name().getname();
        String pdGenName = generatedProcNames.get(pdName);
        Log.log(in, "NOTE: " + pdName + " gets us " + generatedProcNames.get(pdName));
        // Check local procedures, if none is found then the procedure must come
        // from a different file and maybe package.
        if (currentCompilation.fileName.equals(pd.myCompilation.fileName)) {
            String name = pdName + signature(pd);
            if (Helper.doesProcYield(pd)) {
                name = Helper.makeVariableName(name, 0, Tag.PROCEDURE_NAME);
            } else if (generatedProcNames.get(pdName) == null) {
                name = Helper.makeVariableName(name, 0, Tag.METHOD_NAME);
                // pdName = pd.myCompilation.fileNoExtension() + "." + name;
            }
            if(pdGenName == null) {
                pdName = name;
            } else {
                pdName = pdGenName;
            }
        } else if (pd.isNative) {
            // Make the package visible on import by using the qualified name of
            // the class the procedure belongs to and the name of the directory
            // the procedure's class belongs to, e.g., std.io.println(), where
            //      1.) 'std' is the name of the package,
            //      2.) 'io' is the name of the class/file,
            //      3.) 'println' is the method declared in the class
            pdName = pd.filename + "::" + pdName;
        } else
            ;

        // These are the formal parameters of a procedure/method which are specified
        // by a list of comma-separated arguments.
        Sequence<Expression> parameters = in.params();
        String[] paramsList = (String[]) parameters.visit(this);
        if (paramsList != null) {
            for (int i = 0; i < paramsList.length; ++i) {
                paramsList[i] = paramsList[i].replace(DELIMITER, "");
            }
        }

        // We need to extract the types of the original proc we're
        // extending in-line to construct the correct overloaded constructor
        // (and to call the base constructor with the correct arguments)
        // Invocation -> ProcTypeDecl -> Sequence<ParamDecl>
        // in         .  targetProc   .  formalParams()
        Sequence<ParamDecl> formalParams = in.targetProc.formalParams();
        String[] typesList = new String[formalParams.size()];
        String[] varsList = new String[formalParams.size()];
        for (int i = 0; i < formalParams.size(); ++i) {
            Log.log(in, "visiting formal parameter " + ((ParamDecl)formalParams.child(i)).name().toString());
            varsList[i] = ((ParamDecl)formalParams.child(i)).name().toString();
            Type t = (Type) ((ParamDecl)formalParams.child(i)).type();
            typesList[i] = (String)t.visit(this);

            if(!(t instanceof NamedType && ((NamedType)t).type().isProtocolType()) && (t.isBarrierType() || !(t.isPrimitiveType() || t.isArrayType()))) {
                Log.log(pd, "appending a pointer specifier to type of " + ((ParamDecl)formalParams.child(i)).name());
                typesList[i] += "*";
            }
        }

        // if (formalParams != null && formalParams.size() > 0) {
        //     String[] typesList = new String[formalParams.size()];
        //     Log.log("FORMAL PARAMS NOT NULL (size is " + formalParams.size() + ").");
        //     for (int i = 0; i < formalParams.size(); ++i) {
        //         Log.log(in, "invocation's original class formal param found: " + formalParams.child(i).type());
        //         typesList[i] = formalParams.child(i).type().visit(this);
        //         Log.log(in, "visiting returned " + typesList[i]);
        //     }
        // }

        // For an invocation of a procedure that yields and one which
        // is not inside par-block, we wrap the procedure in a par-block.
        if (Helper.doesProcYield(pd) && currentParBlock == null) {
            return (new ParBlock(
                    new Sequence(new ExprStat(in)), // Statements.
                    new Sequence()))                // Barriers.
                    .visit(this);                   // Return a procedure wrapped in a par-block.
        }

        // Does this procedure yield?
        if (Helper.doesProcYield(pd)) {
            stInvocation = stGroup.getInstanceOf("InvocationProcType");
            stInvocation.add("parBlock", currentParBlock);
            // Add the barrier this procedure should resign from.
            if (!barrierList.isEmpty()) {
                for (int i = 0; i < barrierList.size(); ++i) {
                    Log.log(pd, "barrier added to invocation: " + barrierList.get(i));
                }
                stInvocation.add("barrier", barrierList);
                // stInvocation.add("vars", barrierList);
                // stInvocation.add("types", "ProcessJRuntime::pj_barrier*");
            }

            // Add the types for our vars
            if (typesList.length != 0) {
                stInvocation.add("types", typesList);
            }
            if (varsList.length != 0) {
                stInvocation.add("vars", varsList);
            }
            if (paramsList.length != 0) {
                stInvocation.add("argvars", paramsList);
            }
            // Add the proc count that we'll need for id generation
            nestedAnonymousProcesses++;
            stInvocation.add("anonCounter", anonProcCount++);
            stInvocation.add("parent", getParentString());
            stInvocation.add("parentClass", generatedProcNames.get(currentProcName));
            nestedAnonymousProcesses--;
        } else {
            // Must be an invocation made through a static Java method.
            stInvocation = stGroup.getInstanceOf("Invocation");
            stInvocation.add("vars", paramsList);
        }

        stInvocation.add("name", pdName);

        Log.log(in, "Leaving visitInvocation()");

        return stInvocation.render();
    }

    @Override
    public Object visitImport(Import im) {
        Log.log(im, "Visiting an import statement (" + im + ")");
        Log.log(im, "IMPORTANT: this visitor is not finished yet");
        Log.log(im, "import statement is: " + im.toString());

        // Generated template after evaluating this visitor.
        ST stImport = stGroup.getInstanceOf("Import");

        Log.log(im, "import stringtemplate instance is " + stImport.render());
        stImport = stGroup.getInstanceOf("Import");
        // TODO: this doesn't actually use the stImport grabbed above,
        // need to fix that
        // stImport.add("package", im.toString());

        // replace dots with slashes to build the path to the file we
        // want to include
        String importPath = im.toString().replace('.', '/');

        // debug logging -- delete later
        Log.log(im, "import path is " + importPath);

        // declare our include line -- this is our return value
        String includeLine = "";

        // if the import line has a wild card
        if (importPath.endsWith("*")) {

            // we need to build a string with _all_ of the headers
            // we want to include
            Log.log(im, "wildcard detected, building import list");

            // look through the directory/ies
            // ---
            // these come back as blah.pj, but might need to be
            // modified in findImports() to blah.hpp later.
            // depending on what ben says in the email reply
            // i get back from him, these could be .pj and then
            // i could add a pragma to include native c++,
            // or some other solution. check back later...
            String[] foundImports = findImports(importPath);
            if (foundImports != null) {
                // build the full import
                Log.log(im, "building import statement(s)");
                for (int i = 0; i < foundImports.length; i++) {
                    Log.log(im, "found list import " + foundImports[i]);
                    // Take off the .pj suffix and add the .hpp suffix
                    String removeSuffix = foundImports[i].replace(".pj", ".hpp");
                    Log.log(im, "removed .pj and replaced with .hpp: " + removeSuffix);
                    includeLine += "#include <" + importPath.replace("*", "") + removeSuffix + ">\n";
                }
            }
            return includeLine;
        }

        // otherwise just build the include for that file
        includeLine = "#include <" + importPath + ".hpp>";

        // debug logging -- delete later and just return the raw string
        // ---
        // maybe add some error checking later if necessary (i.e. the
        // file we want to include may not exist)
        Log.log(im, "includeLine is " + includeLine);
        return includeLine;

        // NOTE: leaving the old return here just in case...?
        // return stImport.render();
    }

    @Override
    public Object visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        Log.log(pd, "Visiting a ProtocolTypeDecl (" + pd.name().getname() + ")");

        // Generated template after evaluating this visitor.
        ST stProtocolClass = stGroup.getInstanceOf("ProtocolClass");
        String name = (String) pd.name().visit(this);
        ArrayList<String> modifiers = new ArrayList<String>();
        ArrayList<String> body = new ArrayList<String>();

        for (Modifier m : pd.modifiers())
            modifiers.add((String) m.visit(this));

        // Add extended protocols (if any).
        if (pd.extend().size() > 0) {
            for (Name n : pd.extend()) {
                ProtocolTypeDecl ptd = (ProtocolTypeDecl) topLevelDecls.get(n.getname());
                for (ProtocolCase pc : ptd.body())
                    protocolTagsSwitchedOn.put(pd.name().getname() + "->" + pc.name().getname(),
                            ptd.name().getname());
            }
        }

        // The scope in which all members appear in a protocol.
        if (pd.body() != null) {
            for (ProtocolCase pc : pd.body())
                body.add((String) pc.visit(this));
        }

        stProtocolClass.add("name", name);
        // stProtocolClass.add("modifiers", modifiers);
        stProtocolClass.add("body", body);

        return stProtocolClass.render();
    }

    // @Override
    // public Object visitProtocolTypeDecl(ProtocolTypeDecl pd) {
    //     Log.log(pd, "Visiting a ProtocolTypeDecl (" + pd.name().getname() + ")");

    //     ST stProtocolClass = stGroup.getInstanceOf("ProtocolClass");
    //     String name = (String) pd.name().visit(this);
    //     ArrayList<String> body = new ArrayList<String>();
    //     ArrayList<String> cases = new ArrayList<String>();

    //     // TODO: need to add cases from extended protocols

    //     if (pd.body() != null) {
    //         for (ProtocolCase pc: pd.body()) {
    //             cases.add(pc.name().getname());
    //             body.add((String) pc.visit(this));
    //         }
    //     }

    //     if (pd.extend().size() > 0) {
    //         for (Name n : pd.extend()) {
    //             ProtocolTypeDecl ptd = (ProtocolTypeDecl) topLevelDecls.get(n.getname());
    //             for (ProtocolCase pc : ptd.body()) {
    //                 cases.add(pc.name().getname());
    //             }
    //         }
    //     }

    //     stProtocolClass.add("name", name);
    //     stProtocolClass.add("body", body);
    //     stProtocolClass.add("cases", cases);

    //     return stProtocolClass.render();
    // }

    @Override
    public Object visitProtocolCase(ProtocolCase pc) {
        Log.log(pc, "Visiting a ProtocolCase (" + pc.name().getname() + ")");

        // Generated template after evaluating this visitor.
        ST stProtocolCase = stGroup.getInstanceOf("ProtocolCase");

        // Since we are keeping the name of a tag as is, this (in theory)
        // shouldn't cause any name collision.
        String protocName = (String) pc.name().visit(this);
        // This shouldn't create name collision problems even if we
        // use the same visitor for protocols and records.
        recordFields.clear();

        // The scope in which all members of this tag appeared.
        for (RecordMember rm : pc.body())
            rm.visit(this);

        // The list of fields that should be passed to the constructor
        // of the static class that the record belongs to.
        if (!recordFields.isEmpty()) {
            stProtocolCase.add("types", recordFields.values());
            stProtocolCase.add("vars", recordFields.keySet());
        }

        stProtocolCase.add("name", protocName);
        stProtocolCase.add("index", protocolCaseIndex);
        protocolCaseNameIndices.put(protocName, protocolCaseIndex++);

        return stProtocolCase.render();
    }

    @Override
    public Object visitProtocolLiteral(ProtocolLiteral pl) {
        Log.log(pl, "Visiting a ProtocolLiteral (" + pl.name().getname() + ")");

        // Generated template after evaluating this visitor.
        ST stProtocolLiteral = stGroup.getInstanceOf("ProtocolLiteral");
        String type = (String) pl.name().visit(this);
        String tag = (String) pl.tag().visit(this);

        // This map is used to determine the order in which values are
        // used with the constructor of the class associated with this
        // kind of protocol.
        HashMap<String, String> members = new LinkedHashMap<String, String>();
        // We need the members of the tag currently being used.
        ProtocolCase target = null;
        ProtocolTypeDecl pt = (ProtocolTypeDecl) topLevelDecls.get(type);
        if (pt != null) { // This should never be 'null'.
            target = pt.getCase(tag);
            // Now that we have the target tag, iterate over all of its members.
            for (RecordMember rm : target.body()) {
                String name = (String) rm.name().visit(this);
                members.put(name, null);
            }
        }

        // A visit to a RecordLiteral would return a string, e.g., 'z = 3',
        // where 'z' is the protocol member and '3' is the literal value
        // used to initialized 'z' with.
        for (RecordMemberLiteral rm : pl.expressions()) {
            String lhs = (String) rm.name().getname();
            String expr = (String) rm.expr().visit(this);
            if (members.put(lhs, expr) == null)
                Log.log(pl, "> Initializing '" + lhs + "' with '" + expr + "'");
            else
                ; // We should never get here.
        }

        stProtocolLiteral.add("type", type);
        stProtocolLiteral.add("protocolType", pl.myTypeDecl.name().getname());
        stProtocolLiteral.add("tag", tag);
        stProtocolLiteral.add("vals", members.values());

        return stProtocolLiteral.render();
    }

    @Override
    public Object visitRecordTypeDecl(RecordTypeDecl rt) {
        Log.log(rt, "Visiting a RecordTypeDecl (" + rt.name().getname() + ")");

        // Generated template after evaluating this visitor.
        ST stRecordStruct = stGroup.getInstanceOf("RecordStruct");
        String recName = (String) rt.name().visit(this);
        ArrayList<String> modifiers = new ArrayList<String>();
        for (Modifier m : rt.modifiers()) {
            modifiers.add((String) m.visit(this));
        }

        // Check for any extended records
        if (rt.extend() != null && rt.extend().size() > 0) {
            Log.log(rt, "extend not empty.");
            Sequence<Name> extend = rt.extend();
            for(int i = 0; i < extend.size(); ++i) {
                Name n = extend.child(i);
                Log.log("Found name " + n.simplename());
                stRecordStruct.add("extend", n.simplename());
            }
        }

        // Remove fields from record.
        recordFields.clear();

        // The scope in which all members appeared in a record.
        for (RecordMember rm : rt.body())
            rm.visit(this);

        // The list of fields that should be passed to the constructor
        // of the static class that the record belongs to.
        if (!recordFields.isEmpty()) {
            Object[] types = recordFields.values().toArray();
            Object[] names = recordFields.keySet().toArray();
            for (int i = 0; i < types.length; ++i) {
                if (((String)types[i]).contains("*")) {
                    Log.log("adding destructor for member " + names[i]);
                    stRecordStruct.add("deletes", names[i]);
                }
            }
            stRecordStruct.add("types", recordFields.values());
            stRecordStruct.add("vars", recordFields.keySet());
        }

        stRecordStruct.add("name", recName);
        stRecordStruct.add("modifiers", modifiers);
        return stRecordStruct.render();
    }

    @Override
    public Object visitRecordMember(RecordMember rm) {
        Log.log(rm, "Visiting a RecordMember (" + rm.type() + " " + rm.name().getname() + ")");

        // Grab the type and name of a declared variable.
        String name = (String) rm.name().visit(this);
        String type = (String) rm.type().visit(this);

        Log.log(rm, "type is " + type);

        // If it needs to be a pointer, make it so
        if(!(rm.type() instanceof NamedType && ((NamedType)rm.type()).type().isProtocolType()) && (rm.type().isBarrierType() /*|| rm.type().isTimerType() */|| !(rm.type().isPrimitiveType() || rm.type().isArrayType()))) {
            Log.log(rm, "appending a pointer specifier to type of " + name);
            type += "*";
        }

        // Add this field to the collection of record members for reference.
        recordFields.put(name, type);

        // Ignored the value returned by this visitor. The types and variables
        // are _always_ resolved elsewhere.
        return null;
    }

    @Override
    public Object visitRecordLiteral(RecordLiteral rl) {
        Log.log(rl, "Visiting a RecordLiteral (" + rl.name().getname() + ")");

        // Generated template after evaluating this visitor.
        ST stRecordLiteral = stGroup.getInstanceOf("RecordLiteral");
        String type = (String) rl.name().visit(this);

        // This map is used to determine the order in which values
        // are passed to the constructor of the class associated with
        // this record.
        HashMap<String, String> members = new LinkedHashMap<String, String>();
        RecordTypeDecl rt = (RecordTypeDecl) topLevelDecls.get(type);

        if (rt != null) { // This should never be 'null'.
            for (RecordMember rm : rt.body()) {
                String name = (String) rm.name().getname();
                Log.log(rl, "> got RecordMember " + name);
                members.put(name, null);
            }
        }

        // A visit to a RecordMemberLiteral would return a string, e.g., 'z = 3',
        // where 'z' is the record member and '3' is the literal value used to
        // initialized 'z' with. This is something we don't want to do. Instead,
        // we need to return the literal value assigned to 'z'.
        for (RecordMemberLiteral rm : rl.members()) {
            String lhs = (String) rm.name().getname();
            String expr = (String) rm.expr().visit(this);
            if (members.put(lhs, expr) == null) {
                Log.log(rl, "> Initializing '" + lhs + "' with '" + expr + "'");
            } else {
                ; // We should never get here.
            }
        }

        stRecordLiteral.add("type", type);
        stRecordLiteral.add("vals", members.values());
        stRecordLiteral.add("names", members.keySet());

        return stRecordLiteral.render();
    }

    @Override
    public Object visitRecordAccess(RecordAccess ra) {
        Log.log(ra, "Visiting a RecordAccess (" + ra + ")");

        // Generated template after evaluating this visitor.
        ST stRecordAccess = stGroup.getInstanceOf("RecordAccess");

        if (ra.record().type.isRecordType()) {
            String name = (String) ra.record().visit(this);
            String field = (String) ra.field().getname();

            stRecordAccess.add("name", name);
            stRecordAccess.add("member", field);
            stRecordAccess.add("op", "->");
        } else if (ra.record().type.isProtocolType()) {
            stRecordAccess = stGroup.getInstanceOf("ProtocolAccess");
            ProtocolTypeDecl pt = (ProtocolTypeDecl) ra.record().type;
            String protocName = (String) pt.name().visit(this); // Wrapper class.
            String name = (String) ra.record().visit(this);     // Reference to inner class type.
            String field = (String) ra.field().getname();       // Field in inner class.

            // Cast a protocol to a supertype if needed.
            if (protocolTagsSwitchedOn.containsKey(protocName + "->" + currentProtocolTag))
                protocName = protocolTagsSwitchedOn.get(protocName + "->" + currentProtocolTag);

            stRecordAccess.add("protocName", protocName);
            stRecordAccess.add("tag", currentProtocolTag);
            stRecordAccess.add("var", name);
            stRecordAccess.add("member", field);
        } else { // This is for arrays and strings.
            String name = (String) ra.record().visit(this);
            stRecordAccess.add("name", name);
            // Call the appropriate method to retrieve the number of characters
            // in a string or the number of elements in an N-dimensional array.
            if (ra.isArraySize) // '...'.size for N-dimensional array.
                stRecordAccess.add("member", "length");
            else if (ra.isStringLength) // '...'.length for number of characters in a string.
                stRecordAccess.add("member", "length()");

            if (ra.record().type.isStringType()) {
                stRecordAccess.add("op", ".");
                Log.log(ra, "adding dot operator");
            } else {
                Log.log(ra, "adding arrow operator");
                stRecordAccess.add("op", "->");
            }
        }

        Log.log(ra, "Record Access is " + stRecordAccess.render());

        return stRecordAccess.render();
    }

    @Override
    public Object visitParBlock(ParBlock pb) {
        Log.log(pb, "Visiting a ParBlock with " + pb.stats().size() + " statements.");

        // Report a warning message for having an empty par-block?
        // TODO: should this be done in 'reachability'?
        if (pb.stats().size() == 0)
            return null;
        // Generated template after evaluating this visitor.
        ST stParBlock = stGroup.getInstanceOf("ParBlock");
        // Save previous par-block.
        String prevParBlock = currentParBlock;
        // Save previous barrier expressions.
        ArrayList<String> prevBarrier = barrierList;
        if (!barrierList.isEmpty()) {
            Log.log(pb, "barrierList not empty");
            barrierList = new ArrayList<String>();
        } else {
            Log.log(pb, "barrierList empty");
        }
        // Create a name for this new par-block.
        currentParBlock = Helper.makeVariableName(Tag.PAR_BLOCK_NAME.toString(), ++parDecId, Tag.LOCAL_NAME);
        // Since this is a new par-block, we need to create a variable inside
        // the process in which this par-block was declared.
        stParBlock.add("name", currentParBlock);
        stParBlock.add("count", pb.stats().size());
        stParBlock.add("process", "this");
        stParBlock.add("procName", generatedProcNames.get(currentProcName));

        if (insideAnonymousProcess) {
            stParBlock.add("parent", getParentString());
        }

        // we should also add a ProcessJRuntime::pj_par to the locals of whatever
        // process we're in
        localParams.put(currentParBlock, "ProcessJRuntime::pj_par*");
        // TODO: do we need this as an init? probably not...
        localInits.put(currentParBlock, "static_cast<ProcessJRuntime::pj_par*>(0)");
        // localDeletes.put(currentParBlock, "delete " + currentParBlock + ";");

        // Increment the jump label.
        stParBlock.add("jump", ++jumpLabel);
        // Add the jump label to the switch list.
        switchLabelList.add(renderSwitchLabel(jumpLabel));
        // Add the barrier this par block enrolls in.
        Sequence<Expression> barriers = pb.barriers();
        if (barriers.size() > 0) {
            for (Expression ex : barriers) {
                barrierList.add((String) ex.visit(this));
            }
        }

        for (int i = 0; i < barrierList.size(); ++i) {
            Log.log(pb, "barrierList[" + i + "]: " + barrierList.get(i));
        }

        // Visit the sequence of statements in the par-block.
        Sequence<Statement> statements = pb.stats();
        if (statements.size() > 0) {
            // if statement is not a ParBlock, then we don't care about it
            // Rendered value of each statement.
            ArrayList<String> stmts = new ArrayList<String>();
            for (Statement st : statements) {
                if (st == null) {
                    continue;
                }
                // if (st instanceof ParBlock) {
                //     continue;
                // }
                // An expression is any valid unit of code that resolves to a value,
                // that is, it can be a combination of variables, operations and values
                // that yield a result. An statement is a line of code that performs
                // some action, e.g., print statements, an assignment statement, etc.
                if (st instanceof ExprStat && ((ExprStat) st).expr() instanceof Invocation) {
                    ExprStat es = (ExprStat) st;
                    Invocation in = (Invocation) es.expr();
                    // If this invocation is made on a process, then visit the
                    // invocation and return a string representing the wrapper
                    // class for this procedure; e.g.,
                    //      (new <classType>(...) {
                    //          @Override public synchronized void run() { ... }
                    //          @Override public finalize() { ... }
                    //      }.schedule();
                    if (Helper.doesProcYield(in.targetProc)) {
                        stmts.add((String) in.visit(this));
                    }
                    else {// Otherwise, the invocation is made through a static Java method.
                        stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
                    }
                } else {
                    stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
                }
            }
            stParBlock.add("body", stmts);
        }
        // Add the barrier to the par-block.
        if (!barrierList.isEmpty() && pb.barriers().size() > 0) {
            HashMap<String, Integer> parBarriers = new HashMap();
            for (Expression e : pb.barriers()) {
                String name = (String) e.visit(this);
                parBarriers.put(name, pb.enrolls.get(((NameExpr)e).name().getname()));
            }
            stParBlock.add("barrier", parBarriers.keySet());
            stParBlock.add("enrollees", parBarriers.values());
        }

        // Restore the par-block.
        currentParBlock = prevParBlock;
        // Restore barrier expressions.
        barrierList = prevBarrier;

        // Log.log(pb, "ParBlock is " + stParBlock.render());

        return stParBlock.render();
    }

    @Override
    public Object visitTimeoutStat(TimeoutStat ts) {
        Log.log(ts, "Visiting a TimeoutStat");

        // Generated template after evaluating this visitor.
        ST stTimeoutStat = stGroup.getInstanceOf("TimeoutStat");
        String timer = (String) ts.timer().visit(this);
        String delay = (String) ts.delay().visit(this);

        stTimeoutStat.add("name", timer);
        stTimeoutStat.add("delay", delay);

        // Increment the jump label.
        stTimeoutStat.add("resume0", ++jumpLabel);
        // Add the jump label to the switch list.
        switchLabelList.add(renderSwitchLabel(jumpLabel));
        // Add the current generated proc name
        stTimeoutStat.add("procName", generatedProcNames.get(currentProcName));

        return stTimeoutStat.render();
    }

    @Override
    public Object visitSyncStat(SyncStat st) {
        Log.log(st, "Visiting a SyncStat");

        // Generated template after evaluating this visitor.
        ST stSyncStat = stGroup.getInstanceOf("SyncStat");
        String barrier = (String) st.barrier().visit(this);
        stSyncStat.add("barrier", barrier);

        // Increment the jump label.
        stSyncStat.add("resume0", ++jumpLabel);
        stSyncStat.add("procName", generatedProcNames.get(currentProcName));
        // Add the jump label to the switch list.
        switchLabelList.add(renderSwitchLabel(jumpLabel));

        return stSyncStat.render();
    }

    @Override
    public Object visitUnaryPostExpr(UnaryPostExpr ue) {
        Log.log(ue, "Visiting a UnaryPostExpr (" + ue.opString() + ")");

        // Generated template after evaluating this visitor.
        ST stUnaryPostExpr = stGroup.getInstanceOf("UnaryPostExpr");
        String operand = (String) ue.expr().visit(this);
        String op = ue.opString();

        stUnaryPostExpr.add("operand", operand);
        stUnaryPostExpr.add("op", op);

        return stUnaryPostExpr.render();
    }

    @Override
    public Object visitUnaryPreExpr(UnaryPreExpr ue) {
        Log.log(ue, "Visiting a UnaryPreExpr (" + ue.opString() + ")");

        // Generated template after evaluating this visitor.
        ST stUnaryPreExpr = stGroup.getInstanceOf("UnaryPreExpr");
        String operand = (String) ue.expr().visit(this);
        String op = ue.opString();

        stUnaryPreExpr.add("operand", operand);
        stUnaryPreExpr.add("op", op);

        return stUnaryPreExpr.render();
    }

    @Override
    public Object visitAltCase(AltCase ac) {
        Log.log(ac, "Visiting an AltCase");

        // Generated template after evaluating this visitor.
        ST stAltCase = stGroup.getInstanceOf("AltCase");
        Statement stat = ac.guard().guard();
        String guard = null;
        String[] stats = (String[]) ac.stat().visit(this);

        if (stat instanceof ExprStat)
            guard = (String) stat.visit(this);

        stAltCase.add("number", ac.getCaseNumber());
        stAltCase.add("guardExpr", guard);
        stAltCase.add("stats", stats);

        return stAltCase.render();
    }

    @Override
    public Object visitConstantDecl(ConstantDecl cd) {
        Log.log(cd, "Visting ConstantDecl (" + cd.type().typeName() + " " + cd.var().name().getname() + ")");

        // Generated template after evaluating this visitor.
        ST stConstantDecl = stGroup.getInstanceOf("ConstantDecl");
        stConstantDecl.add("type", cd.type().visit(this));

        Log.log(cd, "ConstantDecl ST is " + stConstantDecl.render());
        stConstantDecl.add("var", cd.var().visit(this));

        Log.log(cd, "Second ConstantDecl ST is " + stConstantDecl.render());

        return stConstantDecl.render();
    }

    @Override
    public Object visitAltStat(AltStat as) {
        Log.log(as, "Visiting an AltStat");

        // Generated template after evaluating this visitor.
        ST stAltStat = stGroup.getInstanceOf("AltStat");
        ST stBooleanGuards = stGroup.getInstanceOf("BooleanGuards");
        ST stObjectGuards = stGroup.getInstanceOf("ObjectGuards");

        Sequence<AltCase> cases = as.body();
        ArrayList<String> blocals = new ArrayList<String>();
        ArrayList<String> bguards = new ArrayList<String>();
        ArrayList<String> guards = new ArrayList<String>();
        ArrayList<String> altCases = new ArrayList<String>();

        // Set boolean guards.
        for (int i = 0; i < cases.size(); ++i) {
            AltCase ac = cases.child(i);
            if (ac.precondition() == null)
                bguards.add(String.valueOf(true));
            else {
                if (ac.precondition() instanceof Literal)
                    bguards.add((String) ac.precondition().visit(this));
                else { // This is an expression.
                    Name n = new Name("btemp");
                    LocalDecl ld = new LocalDecl(
                            new PrimitiveType(PrimitiveType.BooleanKind),
                            new Var(n, ac.precondition()),
                            false);
                    blocals.add((String) ld.visit(this));
                    bguards.add((String) n.visit(this));
                }
            }
        }

        stBooleanGuards.add("constants", bguards);
        stBooleanGuards.add("locals", blocals);

        // Set case number for all AltCases.
        for (int i = 0; i < cases.size(); ++i)
            cases.child(i).setCaseNumber(i);
        // Visit all guards.
        for (int i = 0; i < cases.size(); ++i) {
            AltCase ac = cases.child(i);
            Statement stat = ac.guard().guard();
            // Channel read expression?
            if (stat instanceof ExprStat) {
                Expression e = ((ExprStat) stat).expr();
                ChannelReadExpr cr = null;
                if (e instanceof Assignment)
                    cr = (ChannelReadExpr) ((Assignment) e).right();
                guards.add((String) cr.channel().visit(this));
            } else if (stat instanceof SkipStat) {
                // guards.add(PJAlt.class.getSimpleName() + ".SKIP");
                guards.add("ProcessJRuntime::Alternation::SKIP");
            } else if (stat instanceof TimeoutStat) {
                TimeoutStat ts = (TimeoutStat)stat;
                ST stTimeout = stGroup.getInstanceOf("TimeoutStatCase");
                stTimeout.add("name", ts.timer().visit(this));
                stTimeout.add("delay", ts.delay().visit(this));
                // add to timer locals of alt
                stAltStat.add("timerLocals", stTimeout.render());
                // add the timer to the guards
                guards.add((String)ts.timer().visit(this));
            }

            altCases.add((String) ac.visit(this));
        }

        stObjectGuards.add("guards", guards);

        // <--
        // This is needed because of the 'StackMapTable' for the generated Java bytecode.
        Name n = new Name("index");
        new LocalDecl(
                new PrimitiveType(PrimitiveType.IntKind),
                new Var(n, null),
                false /* not constant */).visit(this);

        // Create a tag for this local alt declaration.
        String newName = Helper.makeVariableName("alt", ++localDecId, Tag.LOCAL_NAME);
        localParams.put(newName, "ProcessJRuntime::Alternation*");
        // localInits.put(newName, "new ProcessJRuntime::Alternation(" + cases.size() + ", this)");
        // localDeletes.put(newName, "delete " + newName + ";");
        paramDeclNames.put(newName, newName);
        // -->

        stAltStat.add("alt", newName);
        stAltStat.add("count", cases.size());
        stAltStat.add("initBooleanGuards", stBooleanGuards.render());
        stAltStat.add("initGuards", stObjectGuards.render());
        // stAltStat.add("bguards", "booleanGuards");
        // stAltStat.add("guards", "objectGuards");
        // need to reroute these to be declared/initialized before the switch-case
        // localParams.put("boolean_guards", "std::vector<bool>");
        // localInits.put("boolean_guards", stBooleanGuards.render());
        // localParams.put("object_guards", "std::vector<ProcessJRuntime::Alternation_guard_type>");
        // localInits.put("object_guards", stObjectGuards.render());
        // localParams.put("alt_ready", "bool");
        // localInits.put("alt_ready", "false");
        // localParams.put("selected", "int");
        // localInits.put("selected", "-1");
        stAltStat.add("procName", generatedProcNames.get(currentProcName));
        stAltStat.add("jump", ++jumpLabel);
        stAltStat.add("cases", altCases);
        stAltStat.add("index", n.visit(this));

        // localParams.put(newName, "ProcessJRuntime::Alternation");

        // Add the jump label to the switch list.
        switchLabelList.add(renderSwitchLabel(jumpLabel));

        needsAltLocals = true;

        return stAltStat.render();
    }

    @Override
    public Object visitReturnStat(ReturnStat rs) {
        Log.log(rs, "Visiting a ReturnStat");

        ST stReturnStat = stGroup.getInstanceOf("ReturnStat");
        String expr = null;

        if (rs.expr() != null)
            expr = (String) rs.expr().visit(this);

        stReturnStat.add("expr", expr);

        return stReturnStat.render();
    }

    //
    // HELPER METHODS
    //

    /**
     * Returns the parameterized type of a Channel object.
     *
     * @param t
     *          The specified primitive type or user-defined type.
     * @return
     *          The type parameter delimited by angle brackets.
     */
    private String getChannelType(Type t) {
        String baseType = null;
        if (t.isNamedType()) {
            NamedType nt = (NamedType) t;
            baseType = (String) nt.visit(this);
        } else if (t.isPrimitiveType()) { // This is needed because we can only have wrapper class.
            // baseType = Helper.getWrapperType(t);
            baseType = getCPPChannelType(t);
        }

        return baseType;
    }

    /**
     * This is used for newly-created processes.
     */
    private void resetGlobals() {
        // parDecId = 0;
        // varDecId = 0;
        // localDecId = 0;
        jumpLabel = 0;
        needsAltLocals = false;

        localParams.clear();
        localInits.clear();
        localDeletes.clear();
        localNulls.clear();
        switchLabelList.clear();
        barrierList.clear();

        formalParams.clear();
        paramDeclNames.clear();
    }

    /**
     * Returns a string representation of a jump label.
     */
    private String renderSwitchLabel(int jump) {
        ST stSwitchCase = stGroup.getInstanceOf("SwitchCase");
        stSwitchCase.add("jump", jump);
        stSwitchCase.add("name", generatedProcNames.get(currentProcName));
        return stSwitchCase.render();
    }

    /**
     * Creates and returns an anonymous procedure for non-invocations.
     *
     * @param st
     *          The statement inside the body of a procedure.
     * @return
     *          An 'anonymous' procedure.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ProcTypeDecl createAnonymousProcTypeDecl(Statement st) {
        return new ProcTypeDecl(
                new Sequence(),               // Modifiers.
                null,                         // Return type.
                new Name("Anonymous"),        // Procedure name.
                new Sequence(),               // Formal parameters.
                new Sequence(),               // Implement.
                null,                         // Annotations.
                new Block(new Sequence(st))); // Body.
    }

    @SuppressWarnings("unchecked")
    private Object createNewArray(String lhs, NewArray na) {
        Log.log(na.line + ": Creating a New Array");

        // Generated template after evaluating this visitor.
        ST stNewArray = stGroup.getInstanceOf("NewArray");
        String[] dims = (String[]) na.dimsExpr().visit(this);
        // String type = (String) na.baseType().visit(this);
        String type = currentArrayTypeString;
        type = type.substring(0, type.length() - 1);

        ST stNewArrayLiteral = stGroup.getInstanceOf("NewArrayLiteral");
        if (na.init() != null) {
            ArrayList<String> inits = new ArrayList<String>();
            Sequence<Expression> seq = na.init().elements();
            for (Expression e : seq) {
                isArrayLiteral = e instanceof ArrayLiteral ? true : false;
                inits.add((String) e.visit(this));
            }

            stNewArrayLiteral.add("dim", String.join("", Collections.nCopies(((ArrayType) na.type).getDepth(), "[]")));
            stNewArrayLiteral.add("vals", inits);
        }
        else
            stNewArrayLiteral.add("dims", dims);

        stNewArray.add("name", lhs);
        stNewArray.add("type", type);
        stNewArray.add("init", stNewArrayLiteral.render());

        // Reset value for array literal expressions.
        isArrayLiteral = false;
        currentArrayTypeString = null;
        currentArrayDepth = 0;

        return stNewArray.render();
    }

    private String getNestedArrayType() {
        String str = currentArrayTypeString;
        int firstIndex = 0;
        int lastIndex = 0;
        firstIndex = str.indexOf("<") + 1;
        lastIndex = str.lastIndexOf(">") - 1;
        str = str.substring(firstIndex, lastIndex);
        return str;
    }

    private Object createChannelReadExpr(String lhs, String op, ChannelReadExpr cr) {
        Log.log(cr, "Creating Channel Read Expression");

        // Generated template after evaluating this visitor.
        ST stChannelReadExpr = stGroup.getInstanceOf("ChannelReadExpr");
        // 'c.read()' is a channel-end expression, where 'c'
        // is the reading end of a channel.
        Expression chanExpr = cr.channel();
        // 'c' is the name of the channel.
        String chanEndName = (String) chanExpr.visit(this);

        // Is it a timer read expression?
        if (chanExpr.type instanceof PrimitiveType && chanExpr.type.isTimerType()) {
            Log.log(cr, "THIS IS A TIMER READ INSTEAD");
            ST stTimerRedExpr = stGroup.getInstanceOf("TimerRedExpr");

            // TODO: this might not be needed if timers are NameExprs
            // if (insideAnonymousProcess) {
            //     lhs = getParentString() + lhs;
            // }

            stTimerRedExpr.add("name", lhs);
            return stTimerRedExpr.render();
        }

        int countLabel = 2;  // One for the 'runLabel' and one for the 'read' operation.
        // Is the reading end of this channel shared?
        if (chanExpr.type.isChannelEndType() && ((ChannelEndType) chanExpr.type).isShared()) {
            stChannelReadExpr = stGroup.getInstanceOf("ChannelOne2Many");
            ++countLabel;
        }

        // Do we have an extended rendezvous?
        if (cr.extRV() != null) {
            Object o = cr.extRV().visit(this);
            stChannelReadExpr.add("extendRv", o);
        }

        stChannelReadExpr.add("chanName", chanEndName);
        // Add the switch block for resumption.
        for (int label = 0; label < countLabel; ++label) {
            // Increment jump label.
            stChannelReadExpr.add("resume" + label, ++jumpLabel);
            // Add jump label to the switch list.
            switchLabelList.add(renderSwitchLabel(jumpLabel));
        }

        // if (insideAnonymousProcess == true) {
        //     lhs = "parent->" + lhs;
        // }

        stChannelReadExpr.add("lhs", lhs);
        stChannelReadExpr.add("op", op);
        stChannelReadExpr.add("procName", generatedProcNames.get(currentProcName));

        Log.log("channelReadExpr: st is " + stChannelReadExpr.render());

        return stChannelReadExpr.render();
    }

    /**
     * Returns a string representing the signature of the wrapper
     * class or Java method that encapsulates a PJProcess.
     */
    private String signature(ProcTypeDecl pd) {
        String s = "";
        for (ParamDecl param : pd.formalParams()) {
            s = s + "$" + param.type().signature();
            // Array [t; where 't' is the baste type.
            if (param.type().isArrayType())
                s = s.replace("[", "ar").replace(DELIMITER, "");
            // <Rn; 'n' is the name.
            else if (param.type().isRecordType())
                s = s.replace("<", "rc").replace(DELIMITER, "");
            // <Pn; 'n' is the name.
            else if (param.type().isProtocolType())
                s = s.replace("<", "pt").replace(DELIMITER, "");
            // {t;
            else if (param.type().isChannelType())
                s = s.replace("{", "ct").replace(DELIMITER, "");
            // channel end type.
            else if (param.type().isChannelEndType()) {
                if (((ChannelEndType) param.type()).isRead()) // {t;? channel read.
                    s = s.replace("{", "cr").replace(DELIMITER, "").replace("?", "");
                else // {t;! channel write.
                    s = s.replace("{", "cw").replace(DELIMITER, "").replace("!", "");
            } else
                s = s.replace(";", "");
        }
        return String.valueOf(s.hashCode()).replace("-", "$");
    }

    /**
     * Returns an array of filenames to import in the
     * event of a wildcard import
     */
    private String[] findImports(String importPath) {
        Log.log("in findImports");
        Log.log("NOTE: current directory is " + System.getProperty("user.dir") + ".");
        // build fully qualified path to our include directory
        String dir = "include/C++/" + importPath;
        Log.log("dir length is " + dir.length() + ".");
        // chop off the last /*
        dir = dir.substring(0, dir.length() - 2);
        Log.log("Finding files in directory " + dir + "...");

        File f = new File(dir);


        String[] imports = f.list();

        if (imports == null) {
            Log.log("Found no imports.");
        } else {
            for(String str : imports) {
                Log.log("Found file " + str + ".");
            }
        }

        return imports;
    }

    private String getCPPChannelType(Type type) {
        Log.log("in getCPPChannelType()");
        type = Assert.nonNull(type, "The parameter type cannot be null.");
        String typeName = "";

        if (type.isIntegerType())
            typeName = "int32_t";
        else if (type.isByteType())
            typeName = "int8_t";
        else if (type.isLongType())
            typeName = "int64_t";
        else if (type.isDoubleType())
            typeName = "double";
        else if (type.isFloatType())
            typeName = "float";
        else if (type.isBooleanType())
            typeName = "bool";
        else if (type.isCharType())
            typeName = "char";
        else if (type.isShortType())
            typeName = "int16_t";

        return typeName;
    }

    private String getParentString() {
        String parents = "";
        for (int i = 0; i < nestedAnonymousProcesses; ++i) {
            parents += "parent->";
        }
        return parents;
        // return "";
    }
}
