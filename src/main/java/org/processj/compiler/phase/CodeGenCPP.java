package org.processj.compiler.phase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Stack;

import com.sun.jdi.LongType;
import org.processj.compiler.ast.expression.access.ArrayAccessExpression;
import org.processj.compiler.ast.expression.access.RecordAccessExpression;
import org.processj.compiler.ast.expression.binary.AssignmentExpression;
import org.processj.compiler.ast.expression.binary.BinaryExpression;
import org.processj.compiler.ast.expression.constructing.NewArrayExpression;
import org.processj.compiler.ast.expression.literal.*;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.expression.result.CastExpression;
import org.processj.compiler.ast.expression.yielding.ChannelEndExpression;
import org.processj.compiler.ast.expression.yielding.ChannelReadExpression;
import org.processj.compiler.ast.expression.result.InvocationExpression;
import org.processj.compiler.ast.expression.unary.*;
import org.processj.compiler.ast.modifier.Modifier;
import org.processj.compiler.ast.packages.Import;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.declarative.*;
import org.processj.compiler.ast.statement.conditional.SwitchStatement;
import org.processj.compiler.ast.type.ProcedureType;
import org.processj.compiler.ast.type.ProtocolType;
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.ast.statement.conditional.ParBlock;
import org.processj.compiler.ast.type.*;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.conditional.AltStatement;
import org.processj.compiler.ast.expression.*;
import org.processj.compiler.ast.type.primitive.*;
import org.processj.compiler.ast.type.primitive.numeric.DoubleType;
import org.processj.compiler.ast.type.primitive.numeric.FloatType;
import org.processj.compiler.ast.type.primitive.numeric.NumericType;
import org.processj.compiler.ast.type.primitive.numeric.integral.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

// required for finding imports
import java.io.File;

/**
 * A tree walker that collects data from an AST object and then
 * pushes this data into a template to translate a ProcessJ source
 * file to Java code.
 *
 *          A visitor interface used to perform operations across a
 *          collection of different objects.
 *
 * @author Ben
 * @version 06/10/2018
 * @since 1.2
 */
public class CodeGenCPP extends Phase {

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

    // Jump label used when procedures org.processj.yield.
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
     */
    public CodeGenCPP() {
        super(null);
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
        // workingDir;
        return workingDir;
    }

    /**
     * Visit a single compilation unit which starts with an optional package
     * declaration, followed by zero or more import declarations, followed
     * by zero or more type declarations.
     *
     * @param compilation An AST that represents the entire compilation unit.
     * @// A text generated after evaluating this compilation unit.
     */
    @Override
    public final void visitCompilation(Compilation compilation) throws Phase.Error {
        org.processj.compiler.Compiler.Info(compilation + "Visiting a Compilation");

        currentCompilation = compilation;
        // Code generated by the template.
        String code = null;
        // Instance of Compilation template to fill in.
        ST stCompilation = stGroup.getInstanceOf("Compilation");
        // Reference to all remaining types.
        ArrayList<String> body = new ArrayList<String>();
        // Holds all top level types declarations.
        //Sequence<Type> typeDecls = compilation.getTypeDeclarations();
        // Package name for this source file.
        String packagename = compilation.getPackageName();

        for (Import im : compilation.getImports()) {
            //if (im != null)
                //importFiles.add((String) im.visit(this));

        }

        //for (AST decl : typeDecls) {
            //if (decl instanceof Type) {
                // Collect procedures, records, protocols, and external types (if any).
                //String t = ""; //(String) ((Type) decl).visit(this);

                //if (t != null)
                    //body.add(t);
            //} else if (decl instanceof ConstantDeclaration) {
                // Iterate over remaining declarations, which is anything that
                // comes after top-level declarations.
                //String cd = ""; //(String) ((ConstantDeclaration) decl).visit(this);

                //if (cd != null)
                    //body.add(cd);
            //}
        //}

        stCompilation.add("pathName", packagename);
        stCompilation.add("fileName", compilation.fileName);
        stCompilation.add("name", compilation.fileNoExtension());
        stCompilation.add("body", body);
        stCompilation.add("version", currentJVM);

        // Add the import statements to the file.
        if (importFiles.size() > 0)
            stCompilation.add("imports", importFiles);

        // Rendered code for debbuging.
        code = stCompilation.render();

        // Code generated by the ProcessJ-CPP compiler.

        // code;
    }

    @Override
    public final void visitProcedureTypeDeclaration(ProcedureType procedureType) throws Phase.Error {
        org.processj.compiler.Compiler.Info(procedureType + "Visiting a ProcTypeDecl (" + procedureType + ")");

        // Generated template after evaluating this visitor.
        ST stProcTypeDecl = null;
        // Save previous procedure.
        prevProcName = currentProcName;
        // Save previous jump labels.
        ArrayList<String> prevLabels = switchLabelList;
        if (!switchLabelList.isEmpty())
            switchLabelList = new ArrayList<String>();
        // Name of invoked procedure.
        currentProcName = paramDeclNames.getOrDefault(procedureType.toString(), procedureType.toString());
        // Procedures are static classes which belong to the same package. To avoid
        // having classes with the same name, we generate a new name for this procedure.
        String procName = null;
        // For non-invocations, that is, for anything other than a procedure
        // that yields, we need to extends the PJProcess class anonymously.
        if ("Anonymous".equals(currentProcName)) {
            org.processj.compiler.Compiler.Info(procedureType + "Creating anonymous process within " + prevProcName);
            // String thisAnonProcParent = generatedProcNames.get(prevProcName);
            String thisAnonProcParent = !anonProcStack.empty() ? anonProcStack.peek() : generatedProcNames.get(prevProcName);
            String savePrevProcName = prevProcName;

            // Grab the instance for an anonymous procedure.
            stProcTypeDecl = stGroup.getInstanceOf("AnonymousProcess");
            nestedAnonymousProcesses++;
            stProcTypeDecl.add("parent", getParentString());
            insideAnonymousProcess = true;
            // Generate a name
            procName = CodeGenJava.makeVariableName(currentProcName + Integer.toString(procCount++) + signature(procedureType), 0, CodeGenJava.Tag.PROCEDURE_NAME);
            org.processj.compiler.Compiler.Info(procedureType + "Generated Proc Name " + procName);
            org.processj.compiler.Compiler.Info(procedureType + "My previous Proc is " + prevProcName);
            anonProcStack.push(procName);
            // Store generated name for labels of a possible ChannelReadExpr
            generatedProcNames.put(currentProcName, procName);
            // Preserve current jump label.
            int prevJumLabel = jumpLabel;
            jumpLabel = 0;
            // Statements that appear in the procedure.

            org.processj.compiler.Compiler.Info(procedureType + "visiting body of " + procName);
            String thisAnonProcName = procName;
            org.processj.compiler.Compiler.Info(procedureType + "saved previous anon proc name " + thisAnonProcParent);
            org.processj.compiler.Compiler.Info(procedureType + "saved current anon proc name " + thisAnonProcName);
            String[] body = new String[0]; //procedureTypeDeclaration.getBody().visit(this);

            org.processj.compiler.Compiler.Info(procedureType + "body of " + procName + " visited");
            prevProcName = savePrevProcName;

            stProcTypeDecl.add("parBlock", currentParBlock);
            stProcTypeDecl.add("syncBody", body);
            // Add the barrier this procedure should resign from.
            if (procedureType.doesYield() && !barrierList.isEmpty()) {
                for (int i = 0; i < barrierList.size(); ++i) {
                    org.processj.compiler.Compiler.Info(procedureType + "barrier added to anon proc: " + barrierList.get(i));
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
            Parameters formals = procedureType.getParameters();

            if (formals != null && formals.size() > 0) {
                // Iterate through and visit every parameter declaration.
                for (int i = 0; i < formals.size(); ++i) {
                    org.processj.compiler.Compiler.Info(procedureType + "visiting parameters");
                    ParameterDeclaration actualParam = formals.getParameterAt(i);
                    // Retrieve the name and type of a parameter in the parameter list.
                    // Note that we ignored the value //ed by this visitor.
                    try {
                        actualParam.accept(this);
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
                }
            } else
                ; // The procedure does not take any parameters.

            // Retrieve modifier(s) attached to invoked procedure such as private,
            // public, protected, etc.
            String[] modifiers = new String[0]; //(String[]) procedureTypeDeclaration.modifiers().visit(this);

            // Grab the // type of the invoked procedure.
            String procType = ""; //(String) procedureTypeDeclaration.getReturnType().visit(this);

            // The procedure's annotation determines if we have a yielding procedure
            // or a Java method (a non-yielding procedure).
            boolean doYield = procedureType.doesYield();
            // Set the template to the correct instance value and then initialize
            // its attributes.
            if (doYield) {
                // This procedure yields! Grab the instance of a yielding procedure
                // from the string template in order to define a new class.
                procName = CodeGenJava.makeVariableName(currentProcName + Integer.toString(procCount) + signature(procedureType), 0, CodeGenJava.Tag.PROCEDURE_NAME);
                generatedProcNames.put(currentProcName, procName);
                procCount++;
                org.processj.compiler.Compiler.Info("Stored " + currentProcName + "'s helper name as " + generatedProcNames.get(currentProcName) + ".");
                stProcTypeDecl = stGroup.getInstanceOf("ProcClass");
                stProcTypeDecl.add("name", procName);
                // Visit all declarations that appear in the procedure.
                String[] body = new String[0];// (String[]) procedureTypeDeclaration.getBody().visit(this);

                // The statements that appear in the body of the procedure.
                stProcTypeDecl.add("syncBody", body);
            } else {
                // Otherwise, grab the instance of a non-yielding procedure instead
                // to define a new static Java method.
                procName = CodeGenJava.makeVariableName(currentProcName + Integer.toString(procCount) + signature(procedureType), 0, CodeGenJava.Tag.METHOD_NAME);
                generatedProcNames.put(currentProcName, procName);
                procCount++;
                org.processj.compiler.Compiler.Info("Stored " + currentProcName + "'s helper name as " + generatedProcNames.get(currentProcName) + ".");
                stProcTypeDecl = stGroup.getInstanceOf("Method");
                stProcTypeDecl.add("name", procName);
                stProcTypeDecl.add("type", procType);
                // Do we have any modifier?
                if (modifiers != null && modifiers.length > 0)
                    stProcTypeDecl.add("modifier", modifiers);
                // Visit all declarations that appear in the procedure.
                String[] body = new String[0];//(String[]) procedureTypeDeclaration.getBody().visit(this);

                stProcTypeDecl.add("body", body);
            }
            // Create an entry point for the ProcessJ program, which is just
            // a Java main method that is called by the JVM.
            org.processj.compiler.Compiler.Info(procedureType + "CHECKING FOR MAIN");
            if ("main".equals(currentProcName) && procedureType.getSignature().equals(CodeGenJava.Tag.MAIN_NAME.toString())) {
                org.processj.compiler.Compiler.Info(procedureType + "FOUND MAIN");
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

            //     org.processj.compiler.Compiler.Info(pd, "sizes are " + localParams.values().size() + ", " + localParams.keySet().size() + ", "
            //                 + localInits.values().size() + ", " + localDeletes.values().size());

            //     for(int i = 0; i < localParams.values().size(); i++) {
            //         org.processj.compiler.Compiler.Info(pd, ((String)localParams.values().toArray()[i]));
            //     }
            //     for(int i = 0; i < localParams.keySet().size(); i++) {
            //         org.processj.compiler.Compiler.Info(pd, ((String)localParams.keySet().toArray()[i]));
            //     }
            //     for(int i = 0; i < localInits.values().size(); i++) {
            //         org.processj.compiler.Compiler.Info(pd, ((String)localInits.values().toArray()[i]));
            //     }
            //     for(int i = 0; i < localDeletes.values().size(); i++) {
            //         org.processj.compiler.Compiler.Info(pd, ((String)localDeletes.values().toArray()[i]));
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

        // stProcTypeDecl.render();
    }

    @Override
    public final void visitBinaryExpression(BinaryExpression binaryExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(binaryExpression + "Visiting a BinaryExpr");

        // Generated template after evaluating this visitor.
        ST stBinaryExpr = stGroup.getInstanceOf("BinaryExpr");
        String op = binaryExpression.opString();
        String lhs = ""; //(String) binaryExpression.getLeft().visit(this);

        lhs = lhs.replace(DELIMITER, "");
        org.processj.compiler.Compiler.Info(binaryExpression + "lhs is " + lhs);
        if (binaryExpression.getLeftExpression() instanceof NameExpression) {
            if (insideAnonymousProcess) {
                // lhs = "parent->" + lhs;
            }
        }
        lhs = binaryExpression.getLeftExpression().hasParens ? "(" + lhs + ")" : lhs;
        String rhs = ""; //(String) binaryExpression.getRight().visit(this);

        rhs = binaryExpression.getRightExpression().hasParens ? "(" + rhs + ")" : rhs;
        rhs = rhs.replace(DELIMITER, "");

        if (binaryExpression.getRightExpression() instanceof NameExpression) {
            if (insideAnonymousProcess) {
                // rhs = "parent->" + rhs;
            }
        }

        stBinaryExpr.add("lhs", lhs);
        stBinaryExpr.add("rhs", rhs);
        stBinaryExpr.add("op", op);

        // stBinaryExpr.render();
    }

    @Override
    public final void visitWhileStatement(WhileStatement whileStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(whileStatement + "Visiting a WhileStat");

        // Generated template after evaluating this visitor.
        ST stWhileStat = stGroup.getInstanceOf("WhileStat");
        // Sequence of statements enclosed in a block statement.
        String[] stats = null;
        String condExpr = null;

        //if (whileStatement.getEvaluationExpression() != null)
            //condExpr = ((String) whileStatement.getEvaluationExpression().visit(this)).replace(DELIMITER, "");

        if (whileStatement.getBody() != null) {
            Object o = null;

                whileStatement.getBody().accept(this);

            if (o instanceof String)
                stats = new String[] { (String) o };
            else
                stats = (String[]) o;
        } else // The body of a while-loop could be empty.
            stats = new String[] { DELIMITER };

        stWhileStat.add("expr", condExpr);
        stWhileStat.add("body", stats);

    }

    @Override
    public final void visitDoStatement(DoStatement doStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(doStatement + "Visiting a DoStat");

        // Generated template after evaluating this visitor.
        ST stDoStat = stGroup.getInstanceOf("DoStat");
        // Sequence of statements enclosed in a block statement.
        String[] stats = null;
        String condExpr = null;

        //if(doStatement.getEvaluationExpression() != null)
            //condExpr = ((String) doStatement.getEvaluationExpression().visit(this)).replace(DELIMITER, "");

        if (doStatement.getBody() != null) {
            Object o = null; //doStatement.getBody().visit(this);

            if (o instanceof String)
                stats = new String[] { (String) o };
            else
                stats = (String[]) o;
        }

        stDoStat.add("expr", condExpr);
        stDoStat.add("body", stats);

        // stDoStat.render();
    }

    @Override
    public final void visitForStatement(ForStatement forStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(forStatement + "Visiting a ForStat");

        // Generated template after evaluating this visitor.
        ST stForStat = stGroup.getInstanceOf("ForStat");
        ArrayList<String> init = new ArrayList<String>();  // Initialization part.
        ArrayList<String> incr = new ArrayList<String>();  // Increment part.

        String initStr = null;
        if (!forStatement.isParallel()) { // Is it a regular for loop?
            if (forStatement.getInitializationStatements() != null) {
                for (Statement st : forStatement.getInitializationStatements()) {
                    //if (st != null)
                        // TODO: why does this break?
                        //initStr = (String)st.visit(this);

                    //if(initStr != null) {
                            //init.add(initStr.replace(";", ""));  // Remove the ';' added in LocalDecl.
                        //}
                        // init.add((String)st.visit(this));
                }
            }

            if(forStatement.getIncrementStatements() != null) {
                //for (ExpressionStatement expr : forStatement.getIncrementStatements())
                    //incr.add(((String) expr.visit(this)).replace(";", ""));
            }

            if (forStatement.getEvaluationExpression() != null) {
                String expr = ""; //((String) forStatement.getEvaluationExpression().visit(this)).replace(";", "");

                stForStat.add("expr", expr);
            }

            // Sequence of statements enclosed in a block statement.
            if (forStatement.getBody() != null) {
                Object o = null; //forStatement.getBody().visit(this);

                stForStat.add("stats", o);
            }
        } else // No! then this is a par-for.
            ;

        if (!init.isEmpty())
            stForStat.add("init", init);
        if (!incr.isEmpty())
            stForStat.add("incr", incr);

        // stForStat.render();
    }

    @Override
    public final void visitContinueStatement(ContinueStatement continueStatement) {
        org.processj.compiler.Compiler.Info(continueStatement + "Visiting a ContinueStat");

        // Generated template after evaluating this visitor.
        ST stContinueStat = stGroup.getInstanceOf("ContinueStat");
        String name = null;
        // If target is not 'null' then we have a label to jump to.
        if (continueStatement.getTarget() != null) {
            //name = (String) continueStatement.getTarget().visit(this);

            stContinueStat.add("name", name);
        }

        // stContinueStat.render();
    }

    @Override
    public final void visitIfStatement(IfStatement ifStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(ifStatement + "Visiting a IfStat");

        // Generated template after evaluating this visitor.
        ST stIfStat = stGroup.getInstanceOf("IfStat");
        // Sequence of statements enclosed in a block statement.
        String[] thenStats = null;
        String[] thenParts = null;
        String condExpr = null;

        //if (ifStatement.getEvaluationExpression() != null)
            //condExpr = (String) ifStatement.getEvaluationExpression().visit(this);

        //if (ifStatement.getThenBody() != null) {
            //thenStats = (String[]) ifStatement.getThenBody().visit(this);

        //}
        //if (ifStatement.getElseBody() != null) {

            //thenParts = (String[]) ifStatement.getElseBody().visit(this);

       //}

        stIfStat.add("expr", condExpr);
        stIfStat.add("thenPart", thenStats);
        stIfStat.add("elsePart", thenParts);

        // stIfStat.render();
    }

    @Override
    public final void visitAssignmentExpression(AssignmentExpression assignmentExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(assignmentExpression + "Visiting an Assignment");

        // Generated template after evaluating this visitor.
        ST stVar = stGroup.getInstanceOf("Var");

        String op = (String) assignmentExpression.opString();
        String lhs = null;
        String rhs = null;

        //if (assignmentExpression.getLeftExpression() != null)
            //lhs =  (String) assignmentExpression.getLeftExpression().visit(this);


        if (assignmentExpression.getRightExpression() instanceof NewArrayExpression) {
            // createNewArray(lhs, ((NewArrayExpression) assignmentExpression.getRightExpression()));
        }
        else if (assignmentExpression.getRightExpression() instanceof ChannelReadExpression) {
            org.processj.compiler.Compiler.Info("visitAssignment: //ing createChannelReadExpr");
            // createChannelReadExpr(lhs, op, ((ChannelReadExpression) assignmentExpression.getRightExpression()));
        } else {
            if (assignmentExpression.getRightExpression() != null) {
                org.processj.compiler.Compiler.Info("visitAssignment: as.right() != null");
                //rhs = (String) assignmentExpression.getRightExpression().visit(this);

                rhs = rhs.replace(DELIMITER, "");
            }
        }

        // if (insideAnonymousProcess == true) {
        //     lhs = "parent->"+lhs;
        // }
        stVar.add("name", lhs);

        stVar.add("val", rhs);
        stVar.add("op", op);

        // stVar.render();
    }

    @Override
    public final void visitParameterDeclaration(ParameterDeclaration parameterDeclaration) throws Phase.Error {
        org.processj.compiler.Compiler.Info(parameterDeclaration + "Visiting a ParamDecl (" + parameterDeclaration.getType() + " " + parameterDeclaration + ")");

        // Grab the type and name of a declared variable.
        String name = parameterDeclaration.toString();

        String type = ""; //(String) parameterDeclaration.getType().visit(this);

        // Temporary _silly_ fixed for channel types.
        if (parameterDeclaration.getType() instanceof ChannelType || parameterDeclaration.getType() instanceof ChannelEndType) {
            // type = PJChannel.class.getSimpleName() + type.substring(type.indexOf("<"), type.length());
            // TODO: make this build the appropriate type a la C++
            org.processj.compiler.Compiler.Info(parameterDeclaration + "in visitParamDecl() type is " + type);
        }

        // If it needs to be a pointer, make it so
        if(!(parameterDeclaration.getType() instanceof NamedType && ((NamedType) parameterDeclaration.getType()).getType() instanceof ProtocolType) && (parameterDeclaration.getType() instanceof BarrierType || !(parameterDeclaration.getType() instanceof PrimitiveType || (parameterDeclaration.getType() instanceof ArrayType)))) {
            org.processj.compiler.Compiler.Info(parameterDeclaration + "appending a pointer specifier to type of " + name);
            type += "*";
        }

        // Create a tag for this parameter and then add it to the collection
        // of parameters for reference.
        String newName = CodeGenJava.makeVariableName(name, ++varDecId, CodeGenJava.Tag.PARAM_NAME);
        formalParams.put(newName, type);
        paramDeclNames.put(name, newName);

        // Ignored the value //ed by this visitor. The types and variables
        // are _always_ resolved elsewhere.
        // null;
    }

    @Override
    public final void visitLocalDeclaration(LocalDeclaration localDeclaration) throws Phase.Error {
        org.processj.compiler.Compiler.Info(localDeclaration + "Visting a LocalDecl (" + localDeclaration.getType() + " " + localDeclaration + ")");

        // We could have the following targets:
        //   1.) T x;                                         // A declaration
        //   2.) T x = 4;                                     // A simple declaration
        //   3.) T x = in.read();                             // A single channel read
        //   4.) T x = a.read() + b.read() + ... + z.read();  // Multiple channel reads
        //   5.) T x = read();                                // A Java method that //s a value
        //   6.) T x = a + b;                                 // A binary expression
        //   7.) T x = a = b ...;                             // A complex assignment statement
        String name = localDeclaration.toString();
        String type = ""; //(String) localDeclaration.getType().visit(this);

        String val = null;
        String chanType = type;

        // Create a tag for this local declaration
        String newName = CodeGenJava.makeVariableName(name, ++localDecId, CodeGenJava.Tag.LOCAL_NAME);

        // If it needs to be a pointer, make it so
        if(!(localDeclaration.getType() instanceof NamedType && ((NamedType) localDeclaration.getType()).getType() instanceof ProtocolType) && (localDeclaration.getType() instanceof BarrierType /*|| ld.type() instanceof TimerType */|| !(localDeclaration.getType() instanceof PrimitiveType || (localDeclaration.getType() instanceof ArrayType)))) {
            org.processj.compiler.Compiler.Info(localDeclaration + "appending a pointer specifier to type of " + name);
            type += "*";
        }

        localParams.put(newName, type);
        paramDeclNames.put(name, newName);

        if (localDeclaration.getType() instanceof BarrierType) {
            org.processj.compiler.Compiler.Info("SHOULD NOT HAVE PARENT APPENDED");
        }

        if (nestedAnonymousProcesses > 0 &&
            !(localDeclaration.getType() instanceof BarrierType)) {
            newName = getParentString() + newName;
        }

        // This variable could be initialized, e.g. through an assignment operator
        Expression expr = localDeclaration.getInitializationExpression();


        if (/*expr == null && */ /* ld.type().isTimerType() ||*/
            localDeclaration.getType() instanceof BarrierType ||
            localDeclaration.getType() instanceof ChannelType ||
            localDeclaration.getType() instanceof NamedType && ((NamedType) localDeclaration.getType()).getType() instanceof RecordType ||
            (localDeclaration.getType() instanceof ArrayType) ||
            localDeclaration.getType() instanceof NamedType && ((NamedType) localDeclaration.getType()).getType() instanceof ProtocolType) {
        // if(expr != null && !(ld.type() instanceof NamedType && ((NamedType)ld.type()).type().isProtocolType()) && (ld.type().isBarrierType() /*|| ld.type().isTimerType() */|| !(ld.type().isPrimitiveType() || ld.type() instanceof ArrayType))) {
            org.processj.compiler.Compiler.Info(localDeclaration + "creating delete statement for " + name);
            String deleteStmt = "";
            if (expr != null && expr instanceof ProtocolLiteralExpression) {
                org.processj.compiler.Compiler.Info("Should create delete stmt for protocol literal");
                String protoType = ((ProtocolLiteralExpression)expr).toString();
                String protoTag = ((ProtocolLiteralExpression)expr).getTag().toString();
                // deleteStmt = "if (" + newName + ") { delete reinterpret_cast<" +
                //     protoType + "::" + protoTag + "*>(" + newName + "); }";
            } else if (expr == null) {
                deleteStmt = "if (" + newName + ") { delete " + newName + "; }";
            }
            localDeletes.put(name, deleteStmt);
        }

        if (localDeclaration.getType() instanceof ArrayType) {
            currentArrayTypeString = type;
            currentArrayDepth = ((ArrayType) localDeclaration.getType()).getDepth() - 1;
        }

        // Visit the expressions associated with this variable
        if (expr != null) {
            if (localDeclaration.getType() instanceof PrimitiveType) {
                val = "";//(String) expr.visit(this);

            }
            else if (localDeclaration.getType() instanceof NamedType) {// Must be a record or protocol
                currentProtocolName = newName;
                val = ""; //(String) expr.visit(this);
            }
            else if (localDeclaration.getType() instanceof ArrayType) {
                val = ""; //(String) expr.visit(this);
            }
        }

        // Is it a barrier declaration? If so, we must generate code that
        // creates a barrier object
        if (localDeclaration.getType() instanceof BarrierType && expr == null) {
            ST stBarrierDecl = stGroup.getInstanceOf("BarrierDecl");
            val = stBarrierDecl.render();
        }
        // Is it a simple declaration for a channel type? If so, and since
        // channels cannot be created using the operator 'new', we generate
        // code to create a channel object
        if (localDeclaration.getType() instanceof ChannelType && expr == null) {
            ST stChannelDecl = stGroup.getInstanceOf("ChannelDecl");
            stChannelDecl.add("type", chanType);
            val = stChannelDecl.render();
        }
        // After making this local declaration a field of the procedure
        // in which it was declared, we // iff this local variable
        // is not initialized
        if (expr == null) {
            org.processj.compiler.Compiler.Info(localDeclaration + "LocalDecl " + name + " is not initialized.");
            if (!(localDeclaration.getType() instanceof BarrierType) && (localDeclaration.getType() instanceof PrimitiveType ||
                localDeclaration.getType() instanceof ArrayType ||  // Could be an uninitialized array declaration
                localDeclaration.getType() instanceof NamedType))   // Could be a record or protocol declaration
                //// null;                // The 'null' value is used to removed empty
                                            // sequences in the generated code

                // if it's a protocol, it can't have a null initializer
                if (localDeclaration.getType() instanceof NamedType && ((NamedType) localDeclaration.getType()).getType() instanceof ProtocolType) {
                    // null;
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

        org.processj.compiler.Compiler.Info(localDeclaration + "stVarStr is " + stVar.render());

        // stVar.render();
    }

    // @Override
    public final void visitLocalDeclOld(LocalDeclaration ld) throws Phase.Error {
        org.processj.compiler.Compiler.Info(ld + "Visting a LocalDecl (" + ld.getType() + " " + ld + ")");

        // We could have the following targets:
        //      T x;                              -> a declaration
        //      T x = 4;                          -> a simple declaration
        //      T x = in.read();                  -> a single channel read
        //      T x = b.read() + c.read() + ...;  -> multiple channel reads
        //      T x = read();                     -> a Java method that //s a value
        //      T x = a + b;                      -> a binary expression
        //      T x = a = b ...;                  -> a complex assignment
        String name = ld.toString();
        String type = ""; //(String) ld.getType().visit(this);

        String val = null;

        String chantype = type;
        // The channel type, e.g., one-2-one, one-2-many, many-2-one, many-to-many.
        org.processj.compiler.Compiler.Info(ld + "in visitLocalDecl(): type is " + type + ".");
        // if (ld.type().isChannelType() || ld.type().isChannelEndType())
        //     type = PJChannel.class.getSimpleName() + type.substring(type.indexOf("<"), type.length());

        // Create a tag for this local channel expression parameter.
        String newName = CodeGenJava.makeVariableName(name, ++localDecId, CodeGenJava.Tag.LOCAL_NAME);

        // This variable could be initialized, e.g., through an assignment operator.
        Expression expr = ld.getInitializationExpression();

        // If it needs to be a pointer, make it so
        if(!(ld.getType() instanceof NamedType && ((NamedType)ld.getType()).getType() instanceof ProtocolType) && (ld.getType() instanceof BarrierType /*|| ld.type().isTimerType() */|| !(ld.getType() instanceof PrimitiveType || ld.getType() instanceof ArrayType))) {
            org.processj.compiler.Compiler.Info(ld + "appending a pointer specifier to type of " + name);
            type += "*";
        }
        if (expr == null &&/* ld.type().isTimerType() ||*/ ld.getType() instanceof BarrierType/* || !(ld.type() instanceof PrimitiveType)*/) {
            org.processj.compiler.Compiler.Info(ld + "creating delete statement for " + name);
            String deleteStmt = "delete " + newName + ";";
            localDeletes.put(name, deleteStmt);
        }
        localParams.put(newName, type);
        paramDeclNames.put(name, newName);

        if (ld.getType() instanceof ArrayType) {
            currentArrayTypeString = type;
            currentArrayDepth = ((ArrayType)ld.getType()).getDepth() - 1;
        }

        // Visit the expressions associated with this variable.
        if (expr != null) {
            if (expr instanceof ChannelReadExpression) {
                // Save the name of the variable if it's a channel read so we can read to it
                currentChannelReadName = newName;

                // This is always assignment in the case of a read
                // but it doesn't hurt to make it a variable in
                // case we decide to add more functionality
                currentChannelReadOp = "=";
            }
            if (ld.getType() instanceof PrimitiveType)
                val = ""; //(String) expr.visit(this);

            else if (ld.getType() instanceof NamedType) // Must be a record or protocol.
                val = ""; //(String) expr.visit(this);

            else if (ld.getType() instanceof ArrayType)
                val = ""; //(String) expr.visit(this);

        }

        // Is it a barrier declaration? If so, we must generate code that
        // creates a barrier object.
        if (ld.getType() instanceof BarrierType && expr == null) {
            ST stBarrierDecl = stGroup.getInstanceOf("BarrierDecl");
            val = stBarrierDecl.render();
        }
        // Is it a simple declaration for a channel type? If so, and since
        // channels cannot be created using the operator 'new', we generate
        // code to create a channel object.
        if (ld.getType() instanceof ChannelType && expr == null) {
            ST stChannelDecl = stGroup.getInstanceOf("ChannelDecl");
            stChannelDecl.add("type", chantype);
            val = stChannelDecl.render();
        }

        // After making this local declaration a field of the procedure
        // in which it was declared, we // if and only if this local
        // variable is not initialized.
        if (expr == null) {
            org.processj.compiler.Compiler.Info(ld + name + "'s expr is null.");
            // if (!ld.type().isBarrierType() &&
            //     !ld.type().isChannelType() && (ld.type().isPrimitiveType() ||
            //     ld.type() instanceof ArrayType ||    // Could be an uninitialized array declaration.
            //     ld.type().isNamedType())) {   // Could be records or protocols.
                // org.processj.compiler.Compiler.Info(ld, name + " has a 0 initializer.");
                // TODO: static cast this to the type of the variable
                // localInits.put(name, "0");
                // TODO: do we need this as an init? probably not...

            localInits.put(name, "static_cast<" + type + ">(0)");

            if (ld.getType() instanceof TimerType) {
                org.processj.compiler.Compiler.Info(ld + "Timer needs null initializer (for now)");
                // TODO: this may have to call some function that gets the
                // timer's length, or we need to figure out how to redirect
                // and rewrite the timerRead ST definition to be what we
                // place here.
                // val = "static_cast<" + type + ">(0)";
                val = "new ProcessJRuntime::pj_timer(this, 0)";
            }

            if (ld.getType() instanceof PrimitiveType && ld.getType() instanceof NumericType) {
                val = "static_cast<" + type + ">(0)";
            }
            // if (!ld.type().isChannelType()) {

            //     localInits.put(name, "static_cast<" + type + ">(0)");
            //         // null;              // The 'null' is used to removed empty sequences.
            // } else {
            //     localInits.put(name, val);
            // }
            // }
        } else {
            if (ld.getType() instanceof PrimitiveType && ld.getType() instanceof StringType) {
                localInits.put(name, "\"\"");
                // TODO: Double check this line
            } else if (ld.getType() instanceof RecordType) {
                val = "static_cast<" + type + ">(0)";
            } else if (ld.getType() instanceof ProtocolType) {
                // TODO: find out how to fix this problem, variants cannot
                // have null initializers. need to rework this so that we
                // don't have two inits. ugh.
            } else {
                localInits.put(name, "0");
            }
        }

        // If we reach this section of code, then we have a variable
        // declaration with some initial value(s).
        if ((val != null) && !(expr instanceof ChannelReadExpression)) {
            val = val.replace(DELIMITER, "");
        }

        if (expr instanceof ChannelReadExpression) {
            if (ld.getType() instanceof PrimitiveType) {
                // TODO: do we need this as an init? probably not...
                // localInits.put(newName, (((PrimitiveType)ld.type()).getKind() == PrimitiveType.StringKind) ? "\"\"" : "0");
            } else if (ld.getType() instanceof NamedType ||
                       ld.getType() instanceof ArrayType) {
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
        org.processj.compiler.Compiler.Info(ld + "In visitLocalDecl(): stVarStr is " + stVarStr + ".");

        // reset channel read name
        currentChannelReadName = null;
        currentChannelReadOp = null;

        if (expr instanceof ChannelReadExpression) {
            // val;
        }
        // stVarStr;
        // // null;
    }

    @Override
    public final void visitExpressionStatement(ExpressionStatement expressionStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(expressionStatement + "Visiting an ExprStat");

        // expressionStatement.getExpression().visit(this);
    }

    @Override
    public final void visitName(Name name) {

        org.processj.compiler.Compiler.Info(name + "Visiting a Name (" + name + ")");

        // paramDeclNames.getOrDefault(name.getName(), name.getName());

    }

    @Override
    public final void visitNameExpression(NameExpression nameExpression) {
        org.processj.compiler.Compiler.Info(nameExpression + "Visiting a NameExpr (" + nameExpression.getName() + ")");

        // NameExpr always points to 'myDecl'.
        if (nestedAnonymousProcesses > 0 &&
            !(nameExpression.getType() instanceof ConstantDeclaration)) {
            // getParentString() + nameExpression.getName().visit(this);
        }

            // nameExpression.getName().visit(this);

    }

    @Override
    public final void visitNamedType(NamedType namedType) {
        org.processj.compiler.Compiler.Info(namedType + "Visiting a NamedType (" + namedType + ")");

        String type = namedType.toString();

        // This is for protocol inheritance.
        if (namedType.getType() != null && (namedType.getType() instanceof ProtocolType)) {
            // type = PJProtocolCase.class.getSimpleName();
            type = "ProcessJRuntime::pj_protocol_case*";
        }

        // type;
    }

    @Override
    public final void visitNewArrayExpression(NewArrayExpression newArrayExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(newArrayExpression + "Visiting a NewArray");
        Object o = createNewArray(null, newArrayExpression);

        if(o instanceof String) {
            org.processj.compiler.Compiler.Info(newArrayExpression + "array template is " + o);
        }
        // // createNewArray(null, ne);
        // o;
    }

    @Override
    public final void visitPrimitiveType(PrimitiveType primitiveType) {
        org.processj.compiler.Compiler.Info(primitiveType + "Visiting a Primitive Type (" + primitiveType + ")");

        // ProcessJ primitive types that do not translate directly
        // to Java primitive types.
        String typeStr = primitiveType.toString();

        if(primitiveType instanceof StringType) {
            // typeStr = "char*";
            typeStr = "std::string";
        } else if (primitiveType instanceof BooleanType) {
            typeStr = "bool";
        } else if (primitiveType instanceof TimerType) {
            // typeStr = PJTimer.class.getSimpleName();
            typeStr = "ProcessJRuntime::pj_timer*";
        } else if (primitiveType instanceof BarrierType) {
            // typeStr = PJBarrier.class.getSimpleName();
            typeStr = "ProcessJRuntime::pj_barrier";
        }

        // typeStr;
    }

    @Override
    public final void visitPrimitiveLiteralExpression(PrimitiveLiteralExpression primitiveLiteralExpression) {
        org.processj.compiler.Compiler.Info(primitiveLiteralExpression + "Visiting a Primitive Literal (" + primitiveLiteralExpression.getText() + ")");

        ST stPrimitiveLiteral = stGroup.getInstanceOf("PrimitiveLiteral");
        if (primitiveLiteralExpression.isSuffixed()) {
            stPrimitiveLiteral.add("type", primitiveLiteralExpression.suffix());
        }
        stPrimitiveLiteral.add("value", primitiveLiteralExpression.getText());

        // stPrimitiveLiteral.render();
    }

    @Override
    public final void visitChannelType(ChannelType channelType) throws Phase.Error {
        org.processj.compiler.Compiler.Info(channelType + "Visiting a ChannelType (" + channelType + ")");

        // Channel class type.
        String chantype = "";
        switch (channelType.isShared()) {
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
        String type = getChannelType(channelType.getComponentType());
        // String type = getCPPChannelType(ct.baseType());

        // If it needs to be a pointer, make it so
        if(!(channelType.getComponentType() instanceof NamedType && ((NamedType) channelType.getComponentType()).getType() instanceof ProtocolType) && (channelType.getComponentType() instanceof BarrierType/*|| ct.baseType().isTimerType() */|| !(channelType.getComponentType() instanceof PrimitiveType || (channelType.getComponentType() instanceof ArrayType)))) {
            org.processj.compiler.Compiler.Info(channelType + "appending a pointer specifier to type of " + channelType);
            type += "*";
        }

        // chantype + "<" + type + ">";
    }

    @Override
    public final void visitChannelEndExpression(ChannelEndExpression channelEndExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(channelEndExpression + "Visiting a ChannelEndExpr (" + (channelEndExpression.isRead() ? "read" : "write") + ")");

        String channel = ""; //(String) channelEndExpression.getChannelType().visit(this);

        // channel;
    }

    @Override
    public final void visitChannelEndType(ChannelEndType channelEndType) throws Phase.Error {
        org.processj.compiler.Compiler.Info(channelEndType + "Visiting a ChannelEndType (" + channelEndType + ")");

        // Channel class type.
        // String chanType = PJOne2OneChannel.class.getSimpleName();
        String chanType = "ProcessJRuntime::pj_one2one_channel";
        if (channelEndType.isSharedEnd()) {  // Is it a shared channel?
            if (channelEndType.isReadEnd())  // One-2-many channel.
                // chanType = PJOne2ManyChannel.class.getSimpleName();
                // chanType = "ProcessJRuntime::pj_one2many_channel";
                chanType = "ProcessJRuntime::pj_many2many_channel";
            else if (channelEndType.isWriteEnd()) // Many-2-one channel.
                // chanType = PJMany2OneChannel.class.getSimpleName();
                // chanType = "ProcessJRuntime::pj_many2one_channel";
                chanType = "ProcessJRuntime::pj_many2many_channel";
            else // Many-2-many channel.
                // chanType = PJMany2ManyChannel.class.getSimpleName();
                chanType = "ProcessJRuntime::pj_many2many_channel";
        }
        // Resolve parameterized type for channels, e.g., chan<T>
        // where 'T' is the type to be resolved.
        String type = getChannelType(channelEndType.getComponentType());
        // STring type = getCPPChannelType(ct.baseType());

        // If it needs to be a pointer, make it so
        if(!(channelEndType.getComponentType() instanceof NamedType && ((NamedType) channelEndType.getComponentType()).getType() instanceof ProtocolType) && (channelEndType.getComponentType() instanceof BarrierType /*|| ct.baseType().isTimerType() */|| !(channelEndType.getComponentType() instanceof PrimitiveType || (channelEndType.getComponentType() instanceof ArrayType)))) {
            org.processj.compiler.Compiler.Info(channelEndType + "appending a pointer specifier to type of " + channelEndType);
            type += "*";
        }

        // chanType + "<" + type + ">";
    }

    @Override
    public final void visitChannelWriteStatement(ChannelWriteStatement channelWriteStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(channelWriteStatement + "Visiting a ChannelWriteStat");

        // Generated template after evaluating this visitor.
        ST stChanWriteStat = stGroup.getInstanceOf("ChanWriteStat");
        // 'c.write(x)' is a channel-end expression, where 'c'
        // is the writing end of a channel.
        Expression chanExpr = channelWriteStatement.getTargetExpression();
        // 'c' is the name of the channel.
        String chanWriteName = ""; //(String) chanExpr.visit(this);

        // Expression sent through channel.
        String expr = ""; //(String) channelWriteStatement.getWriteExpression().visit(this);

        expr = expr.replace(DELIMITER, "");

        int countLabel = 1; // One for the 'runLabel'.
        // Is the writing end of this channel shared?
        if (chanExpr.type instanceof ChannelEndType && ((ChannelEndType) chanExpr.type).isSharedEnd()) {
            stChanWriteStat = stGroup.getInstanceOf("ChannelMany2One");
            ++countLabel;
        }

        if (channelWriteStatement.getWriteExpression() instanceof CastExpression &&
            ((CastExpression) channelWriteStatement.getWriteExpression()).getExpression() instanceof NameExpression &&
            ((NameExpression)((CastExpression) channelWriteStatement.getWriteExpression()).getExpression()).myDecl instanceof LocalDeclaration &&
            ((LocalDeclaration)((NameExpression)((CastExpression) channelWriteStatement.getWriteExpression()).getExpression()).myDecl).getType() instanceof NamedType &&
            ((NamedType)((LocalDeclaration)((NameExpression)((CastExpression) channelWriteStatement.getWriteExpression()).getExpression()).myDecl).getType()).getType() instanceof RecordType) {
            org.processj.compiler.Compiler.Info(channelWriteStatement + "adding null for sent pointer");
            localNulls.add(paramDeclNames.get(((NameExpression)((CastExpression) channelWriteStatement.getWriteExpression()).getExpression()).toString()) + "= nullptr;");
        }

        stChanWriteStat.add("chanName", chanWriteName);
        stChanWriteStat.add("writeExpr", expr);
        org.processj.compiler.Compiler.Info(channelWriteStatement + "proc name is " + currentProcName + ", = " + generatedProcNames.get(currentProcName));
        stChanWriteStat.add("procName", generatedProcNames.get(currentProcName));

        // Add the switch block for resumption.
        for (int label = 0; label < countLabel; ++label) {
            // Increment the jump label.
            stChanWriteStat.add("resume" + label, ++jumpLabel);
            // Add the jump label to the switch list.
            switchLabelList.add(renderSwitchLabel(jumpLabel));
        }

        // stChanWriteStat.render();
    }

    @Override
    public final void visitChannelReadExpression(ChannelReadExpression channelReadExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(channelReadExpression + "Visiting a ChannelReadExpr");

        // createChannelReadExpr(currentChannelReadName, currentChannelReadOp, channelReadExpression);

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

        // // stChannelReadExpr.render();
    }

    @Override
    public final void visitArrayAccessExpression(ArrayAccessExpression arrayAccessExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(arrayAccessExpression + "Visiting an ArrayAccessExpr");

        // Generated template after evaluating this visitor.
        ST stArrayAccessExpr = stGroup.getInstanceOf("ArrayAccessExpr");
        String name = ""; //(String) arrayAccessExpression.getTargetExpression().visit(this);

        String index = ""; //(String) arrayAccessExpression.getIndexExpression().visit(this);

        stArrayAccessExpr.add("name", name);
        stArrayAccessExpr.add("index", index);

        // stArrayAccessExpr.render();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void visitArrayLiteralExpression(ArrayLiteralExpression arrayLiteralExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(arrayLiteralExpression + "Visiting an ArrayLiteral");

        // Is the array initialize at compile time? If so, create a
        // list of values separated by commas and enclosed between
        // braces (note, the org.processj.syntax for array literals in ProcessJ
        // is different to Java's).
        if (arrayLiteralExpression.elements().size() > 1 || isArrayLiteral) {
            // The following extends naturally to two-dimensional, and
            // even higher-dimensional arrays -- but they are not used
            // very often in practice.
            String previousArrayTypeString = currentArrayTypeString;
            String type = getNestedArrayType();
            currentArrayTypeString = type;
            String[] vals = new String[0];// arrayLiteralExpression.elements().visit(this);

            currentArrayTypeString = previousArrayTypeString;
            // "new " +
                   //type +
                   //Arrays.asList(vals)
                    //.toString()
                    //.replace("[", " { ")
                    //.replace("]", " } ");
        }

        // arrayLiteralExpression.elements().visit(this);
    }

    @Override
    public final void visitArrayType(ArrayType arrayType) throws Phase.Error {

        org.processj.compiler.Compiler.Info(arrayType + "Visiting an ArrayType (" + arrayType + ")");

        String stArrayType = ""; //(String) arrayType.getComponentType().visit(this);

        org.processj.compiler.Compiler.Info(arrayType + "stArrayType is " + stArrayType);

        if(!(arrayType.getComponentType() instanceof PrimitiveType)
                || !(arrayType.getComponentType() instanceof NamedType)) {

            if(arrayType.getDepth() > 1) {
                org.processj.compiler.Compiler.Info(arrayType + "depth > 1, pj_md_array used");
                // "ProcessJRuntime::pj_md_array<" + stArrayType + ">*";
            }

            // // "ProcessJRuntime::pj_array<" + stArrayType + ">*";

        }

        org.processj.compiler.Compiler.Info(arrayType + "base of array, pj_array used");

        // "ProcessJRuntime::pj_array<" + stArrayType + ">*";

    }

    @Override
    public final void visitModifier(Modifier modifier) throws Phase.Error {
        org.processj.compiler.Compiler.Info(modifier + "Visiting a Modifier (" + modifier + ")");

        // Type of modifiers: public, protected, private, etc.
        // modifier.toString();
    }

    @Override
    public final void visitBlockStatement(BlockStatement blockStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(blockStatement + "Visiting a Block");

        // The scope in which declarations appear, starting with their
        // own initializers and including any further declarations such
        // invocations or sequence of statements.
        String[] stats = new String[0]; //(String[]) blockStatement.getStatements().visit(this);

        // stats;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final void visitSequence(Sequence sequence)  throws Phase.Error {
        org.processj.compiler.Compiler.Info(sequence + "Visiting a Sequence");

        // Sequence of statements enclosed in a block statement.
        ArrayList<String> seqs = new ArrayList<String>();
        // Iterate through every statement.
        for (int i = 0; i < sequence.size(); ++i) {
            if (sequence.child(i) != null) {
                Object stats = null; //sequence.child(i).visit(this);

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

        // seqs.toArray(new String[0]);
    }

    @Override
    public final void visitBreakStatement(BreakStatement breakStatement)  throws Phase.Error {
        org.processj.compiler.Compiler.Info(breakStatement + "Visiting a BreakStat");

        // Generated template after evaluating this visitor.
        ST stBreakStat = stGroup.getInstanceOf("BreakStat");

        //if (breakStatement.getTarget() != null) // No parse tree for 'break'.
                //stBreakStat.add("name", breakStatement.getTarget().visit(this));


        // stBreakStat.render();

    }

    @Override
    public final void visitSwitchLabelExpression(SwitchStatement.Group.Case aCase) throws Phase.Error {
        org.processj.compiler.Compiler.Info(aCase + "Visiting a SwitchLabel");

        // Generated template after evaluating this visitor.
        ST stSwitchLabel = stGroup.getInstanceOf("SwitchLabel");

        // This could be a default label, in which case, expr()
        // would be 'null'.
        String label = null;
        if (!aCase.isDefault())
            label = ""; //(String) switchLabel.getExpression().visit(this);

        if (isProtocolCase) {
            // Silly way to keep track of a protocol tag, however, this
            // should (in theory) _always_ work.
            // label = "\"" + label + "\"";
            label = "case " + Integer.toString(protocolCaseNameIndices.get(aCase.getExpression().toString()));
            currentProtocolTag = aCase.getExpression().toString();
            // label;
        }
        stSwitchLabel.add("label", label);

        // stSwitchLabel.render();
    }

    @Override
    public final void visitSwitchStatementGroup(SwitchStatement.Group group) throws Phase.Error {
        org.processj.compiler.Compiler.Info(group + "Visit a SwitchGroup");

        // Generated template after evaluating this visitor.
        ST stSwitchGroup = stGroup.getInstanceOf("SwitchGroup");

        ArrayList<String> labels = new ArrayList<String>();
        //for (SwitchLabel sl : switchGroupStatement.getLabels())
            //labels.add((String) sl.visit(this));

        ArrayList<String> stats = new ArrayList<String>();
        for (Statement st : group.getBody()) {
            if (st == null)
                continue;
            //stats.add((String) st.visit(this));

        }

        stSwitchGroup.add("labels", labels);
        stSwitchGroup.add("stats", stats);

        // stSwitchGroup.render();
    }

    @Override
    public final void visitSwitchStatement(SwitchStatement switchStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(switchStatement + "Visiting a SwitchStat");

        // Is this a protocol tag?
        if (switchStatement.getEvaluationExpression().type instanceof ProtocolType) {
            isProtocolCase = true;
            // org.processj.compiler.Compiler.Info(st, "Generating protocol choice for type " + st.expr().type);
            // // generateProtocolChoice(st);
        }

        // Generated template after evaluating this visitor.
        ST stSwitchStat = stGroup.getInstanceOf("SwitchStat");

        String expr = ""; //(String) switchStatement.getEvaluationExpression().visit(this);

        ArrayList<String> switchGroup = new ArrayList<String>();

        //for (SwitchGroupStatement sg : (Sequence<SwitchGroupStatement>) switchStatement.getBody().getStatements())
            //switchGroup.add((String) sg.visit(this));

        stSwitchStat.add("tag", isProtocolCase);
        stSwitchStat.add("expr", expr);
        stSwitchStat.add("block", switchGroup);

        // Reset the value for this protocol tag.
        isProtocolCase = false;

        // stSwitchStat.render();
    }

    @Override
    public final void visitCastExpression(CastExpression castExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(castExpression + "Visiting a CastExpr");

        // Generated template after evaluating this invocation.
        ST stCastExpr = stGroup.getInstanceOf("CastExpr");
        // This result in:
        //      ((<type>) (<expr>))
        String type = ""; //(String) castExpression.getCastType().visit(this);

        String expr = ""; //(String) castExpression.getExpression().visit(this);

        // If it needs to be a pointer, make it so
        if(!(castExpression.getCastType() instanceof NamedType && ((NamedType) castExpression.getCastType()).getType() instanceof ProtocolType) && (castExpression.getCastType() instanceof BarrierType /*|| ce.type().isTimerType() */|| !(castExpression.getCastType() instanceof PrimitiveType || (castExpression.getCastType() instanceof ArrayType)))) {
            org.processj.compiler.Compiler.Info(castExpression + "appending a pointer specifier to type of " + expr);
            type += "*";
        }

        stCastExpr.add("type", type);
        stCastExpr.add("expr", expr);

        // stCastExpr.render();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final void visitInvocationExpression(InvocationExpression invocationExpression) throws Phase.Error {
        // Ignore anything that is not a procedure invocation.
        if (invocationExpression.ignore) {
            org.processj.compiler.Compiler.Info(invocationExpression + "Visiting a label/goto point.");

            ST stIgnore = stGroup.getInstanceOf("InvocationIgnore");

            stIgnore.add("name", paramDeclNames.getOrDefault(invocationExpression.getProcedureName(), invocationExpression.getProcedureName()));

                //stIgnore.add("var", invocationExpression.getParameters().visit(this));


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

            // stIgnore.render();
        }

        org.processj.compiler.Compiler.Info(invocationExpression + "Visiting Invocation (" + invocationExpression.targetProc + ")");

        // Generated template after evaluating this invocation.
        ST stInvocation = null;
        // Target procedure.
        ProcedureType pd = invocationExpression.targetProc;
        // Name of invoked procedure.
        String pdName = pd.toString();
        String pdGenName = generatedProcNames.get(pdName);
        org.processj.compiler.Compiler.Info(invocationExpression + "NOTE: " + pdName + " gets us " + generatedProcNames.get(pdName));
        // Check local procedures, if none is found then the procedure must come
        // from a different file and maybe package.
        final String filename = "";// pd.myCompilation.fileName;
        if(currentCompilation.fileName.equals(filename)) {
            String name = pdName + signature(pd);
            if (pd.doesYield()) {
                name = CodeGenJava.makeVariableName(name, 0, CodeGenJava.Tag.PROCEDURE_NAME);
            } else if (generatedProcNames.get(pdName) == null) {
                name = CodeGenJava.makeVariableName(name, 0, CodeGenJava.Tag.METHOD_NAME);
                // pdName = pd.myCompilation.fileNoExtension() + "." + name;
            }
            if(pdGenName == null) {
                pdName = name;
            } else {
                pdName = pdGenName;
            }
        } else if (pd.isNative()) {
            // Make the package visible on import by using the qualified name of
            // the class the procedure belongs to and the name of the directory
            // the procedure's class belongs to, e.g., std.io.println(), where
            //      1.) 'std' is the name of the package,
            //      2.) 'io' is the name of the class/file,
            //      3.) 'println' is the method declared in the class
            pdName = pd.getName().getPackageName() + "::" + pd;
        } else
            ;

        // These are the formal parameters of a procedure/method which are specified
        // by a list of comma-separated arguments.
        Sequence<Expression> parameters = invocationExpression.getParameterExpressions();
        String[] paramsList = new String[0]; //(String[]) parameters.visit(this);

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
        Parameters formalParams = invocationExpression.targetProc.getParameters();
        String[] typesList = new String[formalParams.size()];
        String[] varsList = new String[formalParams.size()];
        for (int i = 0; i < formalParams.size(); ++i) {
            org.processj.compiler.Compiler.Info(invocationExpression + "visiting formal parameter " + formalParams.getParameterAt(i).toString());
            varsList[i] = formalParams.getParameterAt(i).toString();
            Type t = formalParams.getParameterAt(i).getType();

            typesList[i] = ""; //(String)t.visit(this);

            if(!(t instanceof NamedType && ((NamedType)t).getType() instanceof ProtocolType) && (t instanceof BarrierType || !(t instanceof PrimitiveType || (t instanceof ArrayType)))) {
                org.processj.compiler.Compiler.Info(pd + "appending a pointer specifier to type of " + formalParams.getParameterAt(i));
                typesList[i] += "*";
            }
        }

        // if (formalParams != null && formalParams.size() > 0) {
        //     String[] typesList = new String[formalParams.size()];
        //     org.processj.compiler.Compiler.Info("FORMAL PARAMS NOT NULL (size is " + formalParams.size() + ").");
        //     for (int i = 0; i < formalParams.size(); ++i) {
        //         org.processj.compiler.Compiler.Info(in, "invocation's original class formal param found: " + formalParams.child(i).type());
        //         typesList[i] = formalParams.child(i).type().visit(this);
        //         org.processj.compiler.Compiler.Info(in, "visiting //ed " + typesList[i]);
        //     }
        // }

        // For an invocation of a procedure that yields and one which
        // is not inside par-block, we wrap the procedure in a par-block.
        if(pd.doesYield() && currentParBlock == null) {
            // (new ParBlock(
                        //new Sequence(new ExpressionStatement(invocationExpression)), // Statements.
                        //new Sequence()))                // Barriers.
                        //.visit(this);                   // Return a procedure wrapped in a par-block.
        }

        // Does this procedure org.processj.yield?
        if (pd.doesYield()) {
            stInvocation = stGroup.getInstanceOf("InvocationProcType");
            stInvocation.add("parBlock", currentParBlock);
            // Add the barrier this procedure should resign from.
            if (!barrierList.isEmpty()) {
                for (int i = 0; i < barrierList.size(); ++i) {
                    org.processj.compiler.Compiler.Info(pd + "barrier added to invocation: " + barrierList.get(i));
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

        org.processj.compiler.Compiler.Info(invocationExpression + "Leaving visitInvocation()");

        // stInvocation.render();
    }

    @Override
    public final void visitImport(Import importName) throws Phase.Error {
        org.processj.compiler.Compiler.Info(importName + "Visiting an import statement (" + importName + ")");
        org.processj.compiler.Compiler.Info(importName + "IMPORTANT: this visitor is not finished yet");
        org.processj.compiler.Compiler.Info(importName + "import statement is: " + importName.toString());

        // Generated template after evaluating this visitor.
        ST stImport = stGroup.getInstanceOf("Import");

        org.processj.compiler.Compiler.Info(importName + "import stringtemplate instance is " + stImport.render());
        stImport = stGroup.getInstanceOf("Import");
        // TODO: this doesn't actually use the stImport grabbed above,
        // need to fix that
        // stImport.add("package", im.toString());

        // replace dots with slashes to build the path to the file we
        // want to include
        String importPath = importName.toString().replace('.', '/');

        // debug logging -- delete later
        org.processj.compiler.Compiler.Info(importName + "import path is " + importPath);

        // declare our include line -- this is our // value
        String includeLine = "";

        // if the import line has a wild card
        if (importPath.endsWith("*")) {

            // we need to build a string with _all_ of the headers
            // we want to include
            org.processj.compiler.Compiler.Info(importName + "wildcard detected, building import list");

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
                org.processj.compiler.Compiler.Info(importName + "building import statement(s)");
                for (int i = 0; i < foundImports.length; i++) {
                    org.processj.compiler.Compiler.Info(importName + "found list import " + foundImports[i]);
                    // Take off the .pj suffix and add the .hpp suffix
                    String removeSuffix = foundImports[i].replace(".pj", ".hpp");
                    org.processj.compiler.Compiler.Info(importName + "removed .pj and replaced with .hpp: " + removeSuffix);
                    includeLine += "#include <" + importPath.replace("*", "") + removeSuffix + ">\n";
                }
            }
            // includeLine;
        }

        // otherwise just build the include for that file
        includeLine = "#include <" + importPath + ".hpp>";

        // debug logging -- delete later and just // the raw string
        // ---
        // maybe add some error checking later if necessary (i.e. the
        // file we want to include may not exist)
        org.processj.compiler.Compiler.Info(importName + "includeLine is " + includeLine);
        // includeLine;

        // NOTE: leaving the old // here just in case...?
        // // stImport.render();
    }

    @Override
    public final void visitProtocolTypeDeclaration(ProtocolType protocolType) throws Phase.Error {
        org.processj.compiler.Compiler.Info(protocolType + "Visiting a ProtocolTypeDecl (" + protocolType + ")");

        // Generated template after evaluating this visitor.
        ST stProtocolClass = stGroup.getInstanceOf("ProtocolClass");

        String name = paramDeclNames.getOrDefault(protocolType.toString(), protocolType.toString());

        ArrayList<String> modifiers = new ArrayList<String>();
        ArrayList<String> body = new ArrayList<String>();

        //for (Modifier m : protocolTypeDeclaration.modifiers())
            //modifiers.add((String) m.visit(this));

        // Add extended protocols (if any).
        if (protocolType.getExtends().size() > 0) {
            for (Name n : protocolType.getExtends()) {
                //ProtocolType ptd = (ProtocolType) getScope().get(n.getName());
                //for (ProtocolType.Case pc : ptd.getBody())
                    //protocolTagsSwitchedOn.put(protocolType + "->" + pc, ptd.toString());
            }
        }

        // The scope in which all members appear in a protocol.
        if (protocolType.getBody() != null) {
            //for (ProtocolCase pc : protocolTypeDeclaration.getBody())
                //body.add((String) pc.visit(this));

        }

        stProtocolClass.add("name", name);
        // stProtocolClass.add("modifiers", modifiers);
        stProtocolClass.add("body", body);

        // stProtocolClass.render();
    }

    // @Override
    // public final void visitProtocolTypeDecl(ProtocolTypeDecl pd) {
    //     org.processj.compiler.Compiler.Info(pd, "Visiting a ProtocolTypeDecl (" + pd.name().getname() + ")");

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

    //     // stProtocolClass.render();
    // }

    @Override
    public final void visitProtocolCase(ProtocolType.Case aCase) throws Phase.Error {
        org.processj.compiler.Compiler.Info(aCase + "Visiting a ProtocolCase (" + aCase + ")");

        // Generated template after evaluating this visitor.
        ST stProtocolCase = stGroup.getInstanceOf("ProtocolCase");

        // Since we are keeping the name of a tag as is, this (in theory)
        // shouldn't cause any name collision.
        String protocName = ""; //(String) protocolCase.getName().visit(this);

        // This shouldn't create name collision problems even if we
        // use the same visitor for protocols and records.
        recordFields.clear();

        // The scope in which all members of this tag appeared.
        //for (RecordType.Member rm : aCase.getBody())
            //try {
                //rm.accept(this);
            //} catch (Phase.Error error) {
                //throw new RuntimeException(error);
            //}

        // The list of fields that should be passed to the constructor
        // of the static class that the record belongs to.
        if (!recordFields.isEmpty()) {
            stProtocolCase.add("types", recordFields.values());
            stProtocolCase.add("vars", recordFields.keySet());
        }

        stProtocolCase.add("name", protocName);
        stProtocolCase.add("index", protocolCaseIndex);
        protocolCaseNameIndices.put(protocName, protocolCaseIndex++);

        // stProtocolCase.render();
    }

    @Override
    public final void visitProtocolLiteralExpression(ProtocolLiteralExpression protocolLiteralExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(protocolLiteralExpression + "Visiting a ProtocolLiteral (" + protocolLiteralExpression + ")");

        // Generated template after evaluating this visitor.
        ST stProtocolLiteral = stGroup.getInstanceOf("ProtocolLiteral");
        // TODO: Originally pl.name().visit(this);
        String type = protocolLiteralExpression.toString();

        String tag = ""; //(String) protocolLiteralExpression.getTag().visit(this);

        // This map is used to determine the order in which values are
        // used with the constructor of the class associated with this
        // kind of protocol.
        HashMap<String, String> members = new LinkedHashMap<String, String>();
        // We need the members of the tag currently being used.
        ProtocolType.Case target = null;
        //ProtocolType pt = (ProtocolType) getScope().get(type);
        //if (pt != null) { // This should never be 'null'.
            //target = pt.getCase(tag);
            // Now that we have the target tag, iterate over all of its members.
            //for (RecordType.Member rm : target.getBody()) {
                //String name = (String) rm.getName().visit(this);

                //members.put(name, null);
            //}
        //}

        // A visit to a RecordLiteral would // a string, e.g., 'z = 3',
        // where 'z' is the protocol member and '3' is the literal value
        // used to initialized 'z' with.
        for (RecordMemberLiteralExpression rm : protocolLiteralExpression.getExpressions()) {
            String lhs = rm.toString();
            String expr = ""; //(String) rm.getExpression().visit(this);

            if (members.put(lhs, expr) == null)
                org.processj.compiler.Compiler.Info(protocolLiteralExpression + "> Initializing '" + lhs + "' with '" + expr + "'");
            else
                ; // We should never get here.
        }

        stProtocolLiteral.add("type", type);
        stProtocolLiteral.add("protocolType", protocolLiteralExpression.getType().toString());
        stProtocolLiteral.add("tag", tag);
        stProtocolLiteral.add("vals", members.values());

        // stProtocolLiteral.render();
    }

    @Override
    public final void visitRecordTypeDeclaration(RecordType recordType) throws Phase.Error {
        org.processj.compiler.Compiler.Info(recordType + "Visiting a RecordTypeDecl (" + recordType + ")");

        // Generated template after evaluating this visitor.
        ST stRecordStruct = stGroup.getInstanceOf("RecordStruct");
        String recName = paramDeclNames.getOrDefault(recordType.toString(), recordType.toString());
        ArrayList<String> modifiers = new ArrayList<String>();
        //for (Modifier m : recordType.getModifiers()) {
            //modifiers.add((String) m.visit(this));

        //}

        // Check for any extended records
        //if (recordType.getExtends() != null && recordType.getExtends().size() > 0) {
            //org.processj.compiler.Compiler.Info(recordType + "extend not empty.");
            //Sequence<Name> extend = recordType.getExtends();
            //for(int i = 0; i < extend.size(); ++i) {
                //Name n = extend.child(i);
                //org.processj.compiler.Compiler.Info("Found name " + n);
                //stRecordStruct.add("extend", n);
            //}
        //}

        // Remove fields from record.
        recordFields.clear();

        // The scope in which all members appeared in a record.
        //for (RecordType.Member rm : recordType.getBody())
            //try {
                //rm.accept(this);
            //} catch (Phase.Error error) {
                //throw new RuntimeException(error);
            //}

        // The list of fields that should be passed to the constructor
        // of the static class that the record belongs to.
        if (!recordFields.isEmpty()) {
            Object[] types = recordFields.values().toArray();
            Object[] names = recordFields.keySet().toArray();
            for (int i = 0; i < types.length; ++i) {
                if (((String)types[i]).contains("*")) {
                    org.processj.compiler.Compiler.Info("adding destructor for member " + names[i]);
                    stRecordStruct.add("deletes", names[i]);
                }
            }
            stRecordStruct.add("types", recordFields.values());
            stRecordStruct.add("vars", recordFields.keySet());
        }

        stRecordStruct.add("name", recName);
        stRecordStruct.add("modifiers", modifiers);
        // stRecordStruct.render();
    }

    @Override
    public final void visitRecordTypeDeclarationMember(RecordType.Member member) throws Phase.Error {
        org.processj.compiler.Compiler.Info(member + "Visiting a RecordMember (" + member.getType() + " " + member + ")");

        // Grab the type and name of a declared variable.
        String name = "";//(String) recordMemberDeclaration.getName().visit(this);

        String type = ""; //(String) recordMemberDeclaration.getType().visit(this);

        org.processj.compiler.Compiler.Info(member + "type is " + type);

        // If it needs to be a pointer, make it so
        if(!(member.getType() instanceof NamedType && ((NamedType) member.getType()).getType() instanceof ProtocolType) && (member.getType() instanceof BarrierType /*|| rm.type().isTimerType() */|| !(member.getType() instanceof PrimitiveType || (member.getType() instanceof ArrayType)))) {
            org.processj.compiler.Compiler.Info(member + "appending a pointer specifier to type of " + name);
            type += "*";
        }

        // Add this field to the collection of record members for reference.
        recordFields.put(name, type);

        // Ignored the value //ed by this visitor. The types and variables
        // are _always_ resolved elsewhere.
        // null;
    }

    @Override
    public final void visitRecordLiteralExpression(RecordLiteralExpression recordLiteralExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(recordLiteralExpression + "Visiting a RecordLiteral (" + recordLiteralExpression + ")");

        // Generated template after evaluating this visitor.
        ST stRecordLiteral = stGroup.getInstanceOf("RecordLiteral");
        String type = ""; //(String) recordLiteralExpression.getName().visit(this);

        // This map is used to determine the order in which values
        // are passed to the constructor of the class associated with
        // this record.
        HashMap<String, String> members = new LinkedHashMap<String, String>();
        //RecordType rt = (RecordType) getScope().get(type);

        //if (rt != null) { // This should never be 'null'.
            //for (RecordType.Member rm : rt.getBody()) {
                //String name = rm.toString();
                //org.processj.compiler.Compiler.Info(recordLiteralExpression + "> got RecordMember " + name);
                //members.put(name, null);
           //}
        //}

        // A visit to a RecordMemberLiteral would // a string, e.g., 'z = 3',
        // where 'z' is the record member and '3' is the literal value used to
        // initialized 'z' with. This is something we don't want to do. Instead,
        // we need to // the literal value assigned to 'z'.
        for (RecordMemberLiteralExpression rm : recordLiteralExpression.getRecordMemberLiterals()) {
            String lhs = rm.toString();
            String expr = ""; //(String) rm.getExpression().visit(this);

            if (members.put(lhs, expr) == null) {
                org.processj.compiler.Compiler.Info(recordLiteralExpression + "> Initializing '" + lhs + "' with '" + expr + "'");
            } else {
                ; // We should never get here.
            }
        }

        stRecordLiteral.add("type", type);
        stRecordLiteral.add("vals", members.values());
        stRecordLiteral.add("names", members.keySet());

        // stRecordLiteral.render();
    }

    @Override
    public final void visitRecordAccessExpression(RecordAccessExpression recordAccessExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(recordAccessExpression + "Visiting a RecordAccess (" + recordAccessExpression + ")");

        // Generated template after evaluating this visitor.
        ST stRecordAccess = stGroup.getInstanceOf("RecordAccess");

        if (recordAccessExpression.getTarget().getType() instanceof RecordType) {
            String name = ""; //(String) recordAccessExpression.getTarget().visit(this);

            String field = recordAccessExpression.field().toString();

            stRecordAccess.add("name", name);
            stRecordAccess.add("member", field);
            stRecordAccess.add("op", "->");
        } else if (recordAccessExpression.getTarget().type instanceof ProtocolType) {
            stRecordAccess = stGroup.getInstanceOf("ProtocolAccess");
            ProtocolType pt = (ProtocolType) recordAccessExpression.getTarget().type;
            String protocName = paramDeclNames.getOrDefault(pt.toString(), pt.toString());
            String name = ""; //(String) recordAccessExpression.getTarget().visit(this);

            String field = recordAccessExpression.field().toString();       // Field in inner class.

            // Cast a protocol to a supertype if needed.
            if (protocolTagsSwitchedOn.containsKey(protocName + "->" + currentProtocolTag))
                protocName = protocolTagsSwitchedOn.get(protocName + "->" + currentProtocolTag);

            stRecordAccess.add("protocName", protocName);
            stRecordAccess.add("tag", currentProtocolTag);
            stRecordAccess.add("var", name);
            stRecordAccess.add("member", field);
        } else { // This is for arrays and strings.
            String name = ""; //(String) recordAccessExpression.getTarget().visit(this);

            stRecordAccess.add("name", name);
            // Call the appropriate method to retrieve the number of characters
            // in a string or the number of elements in an N-dimensional array.
            if (recordAccessExpression.isArraySize) // '...'.size for N-dimensional array.
                stRecordAccess.add("member", "length");
            else if (recordAccessExpression.isStringLength) // '...'.length for number of characters in a string.
                stRecordAccess.add("member", "length()");

            if (recordAccessExpression.getTarget().getType() instanceof StringType) {
                stRecordAccess.add("op", ".");
                org.processj.compiler.Compiler.Info(recordAccessExpression + "adding dot operator");
            } else {
                org.processj.compiler.Compiler.Info(recordAccessExpression + "adding arrow operator");
                stRecordAccess.add("op", "->");
            }
        }

        org.processj.compiler.Compiler.Info(recordAccessExpression + "Record Access is " + stRecordAccess.render());

        // stRecordAccess.render();
    }

    @Override
    public final void visitParBlockStatement(ParBlock parBlock) throws Phase.Error {
        org.processj.compiler.Compiler.Info(parBlock + "Visiting a ParBlock with " + parBlock.getBody().size() + " statements.");

        // Report a warning message for having an empty par-block?
        // TODO: should this be done in 'org.processj.reachability'?
        //if (parBlock.getBody().getStatements().size() == 0)
            // null;
        // Generated template after evaluating this visitor.
        ST stParBlock = stGroup.getInstanceOf("ParBlock");
        // Save previous par-block.
        String prevParBlock = currentParBlock;
        // Save previous barrier expressions.
        ArrayList<String> prevBarrier = barrierList;
        if (!barrierList.isEmpty()) {
            org.processj.compiler.Compiler.Info(parBlock + "barrierList not empty");
            barrierList = new ArrayList<String>();
        } else {
            org.processj.compiler.Compiler.Info(parBlock + "barrierList empty");
        }
        // Create a name for this new par-block.
        currentParBlock = CodeGenJava.makeVariableName(CodeGenJava.Tag.PAR_BLOCK_NAME.toString(), ++parDecId, CodeGenJava.Tag.LOCAL_NAME);
        // Since this is a new par-block, we need to create a variable inside
        // the process in which this par-block was declared.
        stParBlock.add("name", currentParBlock);
        stParBlock.add("count", parBlock.getBody().size());
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
        final Sequence<Expression> barriersSequence = new Sequence<>();
        //parBlock.getBarrierSet().forEach(barriersSequence::append);

        Sequence<Expression> barriers = barriersSequence;
        if (barriers.size() > 0) {
            for (Expression ex : barriersSequence) {
                //barrierList.add((String) ex.visit(this));

            }
        }

        for (int i = 0; i < barrierList.size(); ++i) {
            org.processj.compiler.Compiler.Info(parBlock + "barrierList[" + i + "]: " + barrierList.get(i));
        }

        // Visit the sequence of statements in the par-block.
        Statements statements = parBlock.getBody();
        if (!statements.isEmpty()) {
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
                // that org.processj.yield a result. An statement is a line of code that performs
                // some action, e.g., print statements, an assignment statement, etc.
                if (st instanceof ExpressionStatement && ((ExpressionStatement) st).getExpression() instanceof InvocationExpression) {
                    ExpressionStatement es = (ExpressionStatement) st;
                    InvocationExpression in = (InvocationExpression) es.getExpression();
                    // If this invocation is made on a process, then visit the
                    // invocation and // a string representing the wrapper
                    // class for this procedure; e.g.,
                    //      (new <classType>(...) {
                    //          @Override public synchronized void run() { ... }
                    //          @Override public finalize() { ... }
                    //      }.schedule();
                    if (in.targetProc.doesYield()) {
                        //stmts.add((String) in.visit(this));

                    }
                    else {// Otherwise, the invocation is made through a static Java method.
                        //stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));

                    }
                } else {
                    //stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));

                }
            }
            stParBlock.add("body", stmts);
        }
        // Add the barrier to the par-block.
        if (!barrierList.isEmpty() && !parBlock.getBarrierSet().isEmpty()) {
            HashMap<String, Integer> parBarriers = new HashMap();
            //for (Expression e : parBlock.getBarrierSet()) {
                //String name = ""; //(String) e.visit(this);

                //parBarriers.put(name, parBlock.getBarrierSet().get(e.toString()));
            //}
            //stParBlock.add("barrier", parBarriers.keySet());
            //stParBlock.add("enrollees", parBarriers.values());
        }

        // Restore the par-block.
        currentParBlock = prevParBlock;
        // Restore barrier expressions.
        barrierList = prevBarrier;

        // org.processj.compiler.Compiler.Info(pb, "ParBlock is " + stParBlock.render());

        // stParBlock.render();
    }

    @Override
    public final void visitTimeoutStatement(TimeoutStatement timeoutStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(timeoutStatement + "Visiting a TimeoutStat");

        // Generated template after evaluating this visitor.
        ST stTimeoutStat = stGroup.getInstanceOf("TimeoutStat");
        String timer = ""; //(String) timeoutStatement.getTimerExpression().visit(this);

        String delay = ""; //(String) timeoutStatement.getDelayExpression().visit(this);

        stTimeoutStat.add("name", timer);
        stTimeoutStat.add("delay", delay);

        // Increment the jump label.
        stTimeoutStat.add("resume0", ++jumpLabel);
        // Add the jump label to the switch list.
        switchLabelList.add(renderSwitchLabel(jumpLabel));
        // Add the current generated proc name
        stTimeoutStat.add("procName", generatedProcNames.get(currentProcName));

        // return stTimeoutStat.render();
    }

    @Override
    public final void visitSyncStatement(SyncStatement syncStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(syncStatement + "Visiting a SyncStat");

        // Generated template after evaluating this visitor.
        ST stSyncStat = stGroup.getInstanceOf("SyncStat");
        String barrier = ""; //(String) syncStatement.getBarrierExpression().visit(this);

        stSyncStat.add("barrier", barrier);

        // Increment the jump label.
        stSyncStat.add("resume0", ++jumpLabel);
        stSyncStat.add("procName", generatedProcNames.get(currentProcName));
        // Add the jump label to the switch list.
        switchLabelList.add(renderSwitchLabel(jumpLabel));

        // return stSyncStat.render();
    }

    @Override
    public final void visitUnaryPostExpression(UnaryPostExpression ue) throws Phase.Error {
        org.processj.compiler.Compiler.Info(ue + "Visiting a UnaryPostExpr (" + ue.opString() + ")");

        // Generated template after evaluating this visitor.
        ST stUnaryPostExpr = stGroup.getInstanceOf("UnaryPostExpr");
        String operand = ""; //ue.getExpression().visit(this);

        String op = ue.opString();

        stUnaryPostExpr.add("operand", operand);
        stUnaryPostExpr.add("op", op);

        // stUnaryPostExpr.render();
    }

    @Override
    public final void visitUnaryPreExpression(UnaryPreExpression ue) throws Phase.Error {
        org.processj.compiler.Compiler.Info(ue + "Visiting a UnaryPreExpr (" + ue.opString() + ")");

        // Generated template after evaluating this visitor.
        ST stUnaryPreExpr = stGroup.getInstanceOf("UnaryPreExpr");
        String operand = ""; //ue.getExpression().visit(this);

        String op = ue.opString();

        stUnaryPreExpr.add("operand", operand);
        stUnaryPreExpr.add("op", op);

        // stUnaryPreExpr.render();
    }

    @Override
    public final void visitAltStatementCase(AltStatement.Case aCase) throws Phase.Error {
        org.processj.compiler.Compiler.Info(aCase + "Visiting an AltCase");

        // Generated template after evaluating this visitor.
        ST stAltCase = stGroup.getInstanceOf("AltCase");
        Statement stat = aCase.getGuardStatement().getStatement();
        String guard = null;
        String[] stats = new String[0]; //altCase.getBody().visit(this);

        if (stat instanceof ExpressionStatement)
            guard = ""; //(String) stat.visit(this);

        final int caseNumber = aCase.getCaseNumber();

        if(caseNumber == -1)
            org.processj.compiler.utilities.Error.error("AltCase Error: The caseNumber for this AltCase was never set!");

        stAltCase.add("number", aCase.getCaseNumber());
        stAltCase.add("guardExpr", guard);
        stAltCase.add("stats", stats);

        // stAltCase.render();
    }

    @Override
    public final void visitConstantDeclaration(ConstantDeclaration constantDeclaration) throws Phase.Error {
        org.processj.compiler.Compiler.Info(constantDeclaration + "Visiting ConstantDecl (" + constantDeclaration.getType() + " " + constantDeclaration + ")");

        // Generated template after evaluating this visitor.
        ST stConstantDecl = stGroup.getInstanceOf("ConstantDecl");

        //stConstantDecl.add("type", constantDeclaration.getType().visit(this));


        org.processj.compiler.Compiler.Info(constantDeclaration + "ConstantDecl ST is " + stConstantDecl.render());

        // Generated template after evaluating this visitor.
        ST stVar = stGroup.getInstanceOf("Var")
                // Returned values for name and expression (if any).
                .add("name", paramDeclNames.getOrDefault(constantDeclaration.toString(), constantDeclaration.toString()));

        // This variable could be initialized, e.g., through an assignment
        // operator.
        // Visit the expressions associated with this variable.
        // This is safe for when our target is not too complicated, e.g.,
        // initializing variables with primitives or string literals.
        if (constantDeclaration.isInitialized())

                //stVar.add("val", constantDeclaration.getInitializationExpression().visit(this));


        stConstantDecl.add("var", stVar.render());

        org.processj.compiler.Compiler.Info(constantDeclaration + "Second ConstantDecl ST is " + stConstantDecl.render());

        // stConstantDecl.render();

    }

    @Override
    public final void visitAltStatement(AltStatement altStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(altStatement + "Visiting an AltStat");

        // Generated template after evaluating this visitor.
        ST stAltStat = stGroup.getInstanceOf("AltStat");
        ST stBooleanGuards = stGroup.getInstanceOf("BooleanGuards");
        ST stObjectGuards = stGroup.getInstanceOf("ObjectGuards");

        Statements cases = altStatement.getBody();
        ArrayList<String> blocals = new ArrayList<String>();
        ArrayList<String> bguards = new ArrayList<String>();
        ArrayList<String> guards = new ArrayList<String>();
        ArrayList<String> altCases = new ArrayList<String>();

        // Set boolean guards.
        //for (int i = 0; i < cases.size(); ++i) {
            //AltStatement.Case ac = cases.child(i);
            //if (ac.getPreconditionExpression() == null)
                //bguards.add(String.valueOf(true));
            //else {
                //if (ac.getPreconditionExpression() instanceof LiteralExpression)
                    //bguards.add((String) ac.getPreconditionExpression().visit(this));

                //else { // This is an expression.
                    //Name n = new Name("btemp");
                    //LocalDeclaration ld = new LocalDeclaration(
                            //new BooleanType(),
                            //n, ac.getPreconditionExpression(),
                            //false);

                    //blocals.add((String) ld.visit(this));

                    //bguards.add((String) n.visit(this));

                //}
            //}
        //}

        stBooleanGuards.add("constants", bguards);
        stBooleanGuards.add("locals", blocals);

        // Set case number for all AltCases.
        //for (int i = 0; i < cases.size(); ++i)
            //cases.child(i).setCaseNumber(i);
        // Visit all guards.
        //for (int i = 0; i < cases.size(); ++i) {
            //AltStatement.Case ac = cases.child(i);
            //Statement stat = ac.getGuardStatement().getStatement();
            // Channel read expression?
            //if (stat instanceof ExpressionStatement) {
                //Expression e = ((ExpressionStatement) stat).getExpression();
                //ChannelReadExpression cr = null;
                //if (e instanceof AssignmentExpression)
                    //cr = (ChannelReadExpression) ((AssignmentExpression) e).getRightExpression();
                //guards.add((String) cr.getChannelExpression().visit(this));

            //} else if (stat instanceof SkipStatement) {
                // guards.add(PJAlt.class.getSimpleName() + ".SKIP");
                //guards.add("ProcessJRuntime::Alternation::SKIP");
            //} else if (stat instanceof TimeoutStatement) {
                //TimeoutStatement ts = (TimeoutStatement)stat;
                //ST stTimeout = stGroup.getInstanceOf("TimeoutStatCase");
                //stTimeout.add("name", ts.getTimerExpression().visit(this));
                //stTimeout.add("delay", ts.getDelayExpression().visit(this));

                // add to timer locals of alt
                //stAltStat.add("timerLocals", stTimeout.render());
                // add the timer to the guards
                //guards.add((String)ts.getTimerExpression().visit(this));

            //}

            //altCases.add((String) ac.visit(this));

        //}

        //stObjectGuards.add("guards", guards);

        // <--
        // This is needed because of the 'StackMapTable' for the generated Java bytecode.
        //Name n = new Name("index");
        //try {
            //new LocalDeclaration(
                    //new IntegralType(),
                    ///n, null,
                    //false /* not constant */).accept(this);
        //} catch (Phase.Error error) {
            //throw new RuntimeException(error);
        //}

        // Create a tag for this local alt declaration.
        //String newName = CodeGenJava.makeVariableName("alt", ++localDecId, CodeGenJava.Tag.LOCAL_NAME);
        //localParams.put(newName, "ProcessJRuntime::Alternation*");
        // localInits.put(newName, "new ProcessJRuntime::Alternation(" + cases.size() + ", this)");
        // localDeletes.put(newName, "delete " + newName + ";");
        //paramDeclNames.put(newName, newName);
        // -->

        //stAltStat.add("alt", newName);
        //stAltStat.add("count", cases.size());
        //stAltStat.add("initBooleanGuards", stBooleanGuards.render());
        //stAltStat.add("initGuards", stObjectGuards.render());
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
        //stAltStat.add("procName", generatedProcNames.get(currentProcName));
        //stAltStat.add("jump", ++jumpLabel);
        //stAltStat.add("cases", altCases);
        //stAltStat.add("index", n.visit(this));


        // localParams.put(newName, "ProcessJRuntime::Alternation");

        // Add the jump label to the switch list.
        //switchLabelList.add(renderSwitchLabel(jumpLabel));

        //needsAltLocals = true;

        // stAltStat.render();
    }

    @Override
    public final void visitReturnStatement(ReturnStatement returnStatement) throws Phase.Error {
        //org.processj.compiler.Compiler.Info(//Statement, "Visiting a ReturnStat");

        ST stReturnStat = stGroup.getInstanceOf("ReturnStat");
        String expr = null;

        if (returnStatement.getExpression() != null)
            expr = "";//(String) //Statement.getExpression().visit(this);

        stReturnStat.add("expr", expr);

        // stReturnStat.render();
    }

    //
    // HELPER METHODS
    //

    /**
     * Returns the parameterized type of a Channel object.
     *
     * @param t
     *          The specified primitive type or user-defined type.
     * @//
     *          The type parameter delimited by angle brackets.
     */
    private String getChannelType(Type t) throws Phase.Error {
        String baseType = null;
        if (t instanceof NamedType) {
            NamedType nt = (NamedType) t;
            //baseType = (String) nt.visit(this);

        } else if (t instanceof PrimitiveType) { // This is needed because we can only have wrapper class.
            // baseType = Helper.getWrapperType(t);
            baseType = getCPPChannelType(t);
        }

        // baseType;
        return null;
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
        // stSwitchCase.render();
        return null;
    }

    /**
     * Creates and //s an anonymous procedure for non-invocations.
     *
     * @param st
     *          The statement inside the body of a procedure.
     * @//
     *          An 'anonymous' procedure.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ProcedureType createAnonymousProcTypeDecl(Statement st) {
         //return new ProcedureTypeDeclaration(
                //new Modifiers(),               // Modifiers.
                //null,                         // Return type.
                //new Name("Anonymous"),        // Procedure name.
                //new Sequence(),               // Formal parameters.
                //new Sequence(),               // Implement.
                //null,                         // Annotations.
                //new BlockStatement(new Sequence(st))); // Body.
        return null;
    }

    @SuppressWarnings("unchecked")
    private Object createNewArray(String lhs, NewArrayExpression na) throws Phase.Error {
        org.processj.compiler.Compiler.Info(na + ": Creating a New Array");

        // Generated template after evaluating this visitor.
        ST stNewArray = stGroup.getInstanceOf("NewArray");
        String[] dims = new String[0];//(String[]) na.getBracketExpressions().visit(this);

        // String type = (String) na.baseType().visit(this);
        String type = currentArrayTypeString;
        type = type.substring(0, type.length() - 1);

        ST stNewArrayLiteral = stGroup.getInstanceOf("NewArrayLiteral");
        if (na.getInitializationExpression() != null) {
            ArrayList<String> inits = new ArrayList<String>();
            Sequence<Expression> seq = na.getInitializationExpression().elements();
            for (Expression e : seq) {
                isArrayLiteral = e instanceof ArrayLiteralExpression ? true : false;
                //inits.add((String) e.visit(this));

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

        // stNewArray.render();
        return null;
    }

    private String getNestedArrayType() {
        String str = currentArrayTypeString;
        int firstIndex = 0;
        int lastIndex = 0;
        firstIndex = str.indexOf("<") + 1;
        lastIndex = str.lastIndexOf(">") - 1;
        str = str.substring(firstIndex, lastIndex);
        // str;
        return null;
    }

    private Object createChannelReadExpr(String lhs, String op, ChannelReadExpression cr) throws Phase.Error {
        org.processj.compiler.Compiler.Info(cr + "Creating Channel Read Expression");

        // Generated template after evaluating this visitor.
        ST stChannelReadExpr = stGroup.getInstanceOf("ChannelReadExpr");
        // 'c.read()' is a channel-end expression, where 'c'
        // is the reading end of a channel.
        Expression chanExpr = cr.getTargetExpression();
        // 'c' is the name of the channel.
        String chanEndName = ""; //(String) chanExpr.visit(this);

        // Is it a timer read expression?
        if (chanExpr.type instanceof PrimitiveType && chanExpr.getType() instanceof TimerType) {
            org.processj.compiler.Compiler.Info(cr + "THIS IS A TIMER READ INSTEAD");
            ST stTimerRedExpr = stGroup.getInstanceOf("TimerRedExpr");

            // TODO: this might not be needed if timers are NameExprs
            // if (insideAnonymousProcess) {
            //     lhs = getParentString() + lhs;
            // }

            stTimerRedExpr.add("name", lhs);
            // stTimerRedExpr.render();
        }

        int countLabel = 2;  // One for the 'runLabel' and one for the 'read' operation.
        // Is the reading end of this channel shared?
        if (chanExpr.type instanceof ChannelEndType && ((ChannelEndType) chanExpr.type).isSharedEnd()) {
            stChannelReadExpr = stGroup.getInstanceOf("ChannelOne2Many");
            ++countLabel;
        }

        // Do we have an extended rendezvous?
        if (cr.getExtendedRendezvous() != null) {
            Object o = null; //cr.getExtendedRendezvous().visit(this);

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

        org.processj.compiler.Compiler.Info("channelReadExpr: st is " + stChannelReadExpr.render());

        // stChannelReadExpr.render();
        return null;
    }

    /**
     * Returns a string representing the signature of the wrapper
     * class or Java method that encapsulates a PJProcess.
     */
    private String signature(ProcedureType pd) {
        String s = "";
        for (ParameterDeclaration param : pd.getParameters()) {
            s = s + "$" + param.getType().getSignature();
            // Array [t; where 't' is the baste type.
            if (param.getType() instanceof ArrayType)
                s = s.replace("[", "ar").replace(DELIMITER, "");
            // <Rn; 'n' is the name.
            else if (param.getType() instanceof RecordType)
                s = s.replace("<", "rc").replace(DELIMITER, "");
            // <Pn; 'n' is the name.
            else if (param.getType() instanceof ProtocolType)
                s = s.replace("<", "pt").replace(DELIMITER, "");
            // {t;
            else if (param.getType() instanceof ChannelType)
                s = s.replace("{", "ct").replace(DELIMITER, "");
            // channel end type.
            else if (param.getType() instanceof ChannelEndType) {
                if (((ChannelEndType) param.getType()).isReadEnd()) // {t;? channel read.
                    s = s.replace("{", "cr").replace(DELIMITER, "").replace("?", "");
                else // {t;! channel write.
                    s = s.replace("{", "cw").replace(DELIMITER, "").replace("!", "");
            } else
                s = s.replace(";", "");
        }
        // String.valueOf(s.hashCode()).replace("-", "$");
        return null;
    }

    /**
     * Returns an array of filenames to import in the
     * event of a wildcard import
     */
    private String[] findImports(String importPath) {
        org.processj.compiler.Compiler.Info("in findImports");
        org.processj.compiler.Compiler.Info("NOTE: current directory is " + System.getProperty("user.dir") + ".");
        // build fully qualified path to our include directory
        String dir = "include/C++/" + importPath;
        org.processj.compiler.Compiler.Info("dir length is " + dir.length() + ".");
        // chop off the last /*
        dir = dir.substring(0, dir.length() - 2);
        org.processj.compiler.Compiler.Info("Finding files in directory " + dir + "...");

        File f = new File(dir);


        String[] imports = f.list();

        if (imports == null) {
            org.processj.compiler.Compiler.Info("Found no imports.");
        } else {
            for(String str : imports) {
                org.processj.compiler.Compiler.Info("Found file " + str + ".");
            }
        }
        return null;
        // imports;
    }

    private String getCPPChannelType(Type type) {
        org.processj.compiler.Compiler.Info("in getCPPChannelType()");
        assert type != null;
        String typeName = "";

        if (type instanceof IntegerType)
            typeName = "int32_t";
        else if (type instanceof ByteType)
            typeName = "int8_t";
        else if (type instanceof LongType)
            typeName = "int64_t";
        else if (type instanceof DoubleType)
            typeName = "double";
        else if (type instanceof FloatType)
            typeName = "float";
        else if (type instanceof BooleanType)
            typeName = "bool";
        else if (type instanceof CharType)
            typeName = "char";
        else if (type instanceof ShortType)
            typeName = "int16_t";

        // typeName;
        return null;
    }

    private String getParentString() {
        String parents = "";
        for (int i = 0; i < nestedAnonymousProcesses; ++i) {
            parents += "parent->";
        }

        // // "";
        return parents;
    }
}