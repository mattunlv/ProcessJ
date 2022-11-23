package codegen.java;

import java.util.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import ast.AST;
import ast.AltCase;
import ast.AltStat;
import ast.ArrayAccessExpr;
import ast.ArrayLiteral;
import ast.ArrayType;
import ast.Assignment;
import ast.BinaryExpr;
import ast.Block;
import ast.BreakStat;
import ast.CastExpr;
import ast.ChannelEndExpr;
import ast.ChannelEndType;
import ast.ChannelReadExpr;
import ast.ChannelType;
import ast.ChannelWriteStat;
import ast.Compilation;
import ast.ConstantDecl;
import ast.ContinueStat;
import ast.DoStat;
import ast.ExprStat;
import ast.Expression;
import ast.ForStat;
import ast.IfStat;
import ast.Import;
import ast.Invocation;
import ast.Literal;
import ast.LocalDecl;
import ast.Modifier;
import ast.Name;
import ast.NameExpr;
import ast.NamedType;
import ast.NewArray;
import ast.ParBlock;
import ast.ParamDecl;
import ast.PrimitiveLiteral;
import ast.PrimitiveType;
import ast.ProcTypeDecl;
import ast.ProtocolCase;
import ast.ProtocolLiteral;
import ast.ProtocolTypeDecl;
import ast.RecordAccess;
import ast.RecordLiteral;
import ast.RecordMember;
import ast.RecordMemberLiteral;
import ast.RecordTypeDecl;
import ast.ReturnStat;
import ast.Sequence;
import ast.SkipStat;
import ast.Statement;
import ast.SwitchGroup;
import ast.SwitchLabel;
import ast.SwitchStat;
import ast.SyncStat;
import ast.Ternary;
import ast.TimeoutStat;
import ast.Token;
import ast.Type;
import ast.UnaryPostExpr;
import ast.UnaryPreExpr;
import ast.Var;
import ast.WhileStat;
import ast.ExternType;
import ast.ErrorType;
import codegen.Helper;
import codegen.Tag;
import processj.runtime.*;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Tuple;
import utilities.Visitor;

/**
 * A tree walker which collects data from an AST object and then
 * pushes this data into a template to translate a ProcessJ source
 * file to Java code.
 *
 * @author ben
 * @version 06/10/2018
 * @since 1.2
 */
public class CodeGenJava extends Visitor<Object> {

    /** String template file locator */
    private final String STGRAMMAR_FILE = "resources/stringtemplates/java/grammarTemplatesJava.stg";

    /** Current Java version -- only needed for debugging */
    private final String JVM_RUNTIME = System.getProperty("java.version");

    /** Collection of templates, imported templates, and/or groups that
     * contain formal template definitions */
    private STGroup stGroup;

    /** Current compilation unit */
    private Compilation currentCompilation = null;

    /** The user working directory */
    private String directory = null;

    /** The source program */
    private String sourceFile = null;

    /** Currently executing procedure */
    private String currentProcName = null;

    /** Currently executing par-block */
    private String currentParBlock = null;

    /** This is used to store variable the tables below --
     * 'localsForAnon' and 'paramsForAnon'*/
    private boolean inParFor = false;

    /** These two fields are responsible for holding variables
     * that are passed to anonymous processes in the generated
     * code for a par-for */
    private HashMap<String, String> localsForAnonymousProcess = new LinkedHashMap<>();
    private HashMap<String, String> paramsForAnonymousProcess = new LinkedHashMap<>();

    /** Currently executing protocol */
    private String currentProtocol = null;

    /** All imports are kept in this table */
    private HashSet<String> importFiles = new LinkedHashSet<>();

    /** Top level declarations */
    private SymbolTable topLvlDecls = null;

    /** Formal parameters transformed into fields */
    private HashMap<String, String> paramToFields = new LinkedHashMap<>();

    /** Formal parameter names transformed into variable names */
    private HashMap<String, String> paramToVarNames = new LinkedHashMap<>();

    /** Local parameters transformed into fields */
    private HashMap<String, String> localToFields = new LinkedHashMap<>();

    /** Record members transformed into fields */
    private HashMap<String, String> recordMemberToField = new LinkedHashMap<>();

    /** Protocol names and tags currently switched on */
    private HashMap<String, String> protocolNameToProtocolTag = new HashMap<>();

    /** List of switch labels */
    private ArrayList<String> switchCases = new ArrayList<>();

    /** List of barrier expressions */
    private ArrayList<String> barriers = new ArrayList<>();

    /** Identifier for parameter declaration */
    private int varDecID = 0;

    /** Identifier for par-block declaration */
    private int parDecID = 0;

    /** Jump label used when procedures yield */
    private int jumpLabel = 0;

    /** Access to protocol case */
    private boolean isProtocolCase = false;

    /** Access to protocol tag */
    private String currentProtocolTag = null;

    /** This is used for arrays of N-dimensions */
    private boolean isArrayLiteral = false;

    /** This is used to remove the punctuator from a statement */
    private final static String DELIMITER = ";";

    /**
     * Internal constructor that loads a group file containing a collection of
     * templates, imported templates, and/or groups containing formal template
     * definitions. Additionally, the constructor initializes a symbol table
     * with top-level declarations.
     *
     * @param s
     *          The top-level declarations which can be procedures, records,
     *          protocols, constants, and/or external types.
     */
    public CodeGenJava(SymbolTable s) {
        Log.logHeader("*******************************************");
        Log.logHeader("*  C O D E   G E N E R A T O R   J A V A  *");
        Log.logHeader("*******************************************");

        topLvlDecls = s;
        stGroup = new STGroupFile(STGRAMMAR_FILE);
    }

    /**
     * Sets the system properties to a current working directory.
     *
     * @param directory
     *          A working directory.
     */
    public void workingDir(String directory) {
        this.directory = directory;
    }

    /**
     * Sets the current source program.
     *
     * @param sourceFile
     *          A source program being processed.
     */
    public void sourceProgam(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * Return a string representing the current working directory.
     */
    public String workingDir() {
        return directory;
    }

    /**
     * Return the current source program.
     */
    public String sourceProgram() {
        return sourceFile;
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
        // Code generated by the ST template
        String codeGen = null;
        // Template to fill in
        ST stCompilation = stGroup.getInstanceOf("Compilation");
        // Reference to all remaining types
        ArrayList<String> body = new ArrayList<>();
        // Holds all top-level declarations
        Sequence<Type> typeDecls = co.typeDecls();
        // Package name for this source file
        String packagename = co.packageNoName();

        for (Import im : co.imports()) {
            if ( im!=null )
                importFiles.add((String) im.visit(this));
        }

        for (AST decl : typeDecls) {
            if ( decl instanceof Type ) {
                // Collect procedures, records, protocols, external types, etc.
                String t = (String) ((Type) decl).visit(this);
                if ( t!=null )
                    body.add(t);
            } else if ( decl instanceof ConstantDecl ) {
                // Iterate over remaining declarations, which is anything that
                // comes after top-level declarations
                String cd = (String) ((ConstantDecl) decl).visit(this);
                if ( cd!=null )
                    body.add(cd);
            }
        }

        stCompilation.add("pathName", packagename);
        stCompilation.add("fileName", co.fileName);
        stCompilation.add("name", sourceFile);
        stCompilation.add("body", body);
        stCompilation.add("version", JVM_RUNTIME);

        // Add all import statements to the file (if any)
        if ( importFiles.size()>0 )
            stCompilation.add("imports", importFiles);

        // This will render the code for debugging
        codeGen = stCompilation.render();

        Log.logHeader("========================================");
        Log.logHeader("*           J A V A   C O D E          *");
        Log.logHeader("========================================");
        Log.logHeader("\n" + codeGen);

        return codeGen;
    }

    @Override
    public Object visitDoStat(DoStat ds) {

        Log.log(ds, "Visiting a DoStat");

        ST stDoStat = stGroup.getInstanceOf("DoStat");
        String[] stats = null;
        String expr = null;

        if ( ds.expr()!=null )
            expr = ((String) ds.expr().visit(this));
        if ( ds.stat()!=null ) {
            Object o = ds.stat().visit(this);
            if ( o instanceof String ) {
                stats = new String[] { (String) o };
            } else {
                stats = (String[]) o;
            }
        }

        stDoStat.add("expr", expr);
        stDoStat.add("body", stats);

        return stDoStat.render();

    }

    @Override
    public Object visitForStat(ForStat fs) {

        Log.log(fs, "Visiting a ForStat");

        ST stForStat = stGroup.getInstanceOf("ParForStat");
        String expr = null;
        ArrayList<String> init = null;
        ArrayList<String> incr = null;
        String[] stats = null;

        boolean preParFor = inParFor;
        inParFor = fs.isPar() || preParFor;

        if ( fs.init()!=null ) {
            init = new ArrayList<>();
            for (Statement st : fs.init())
                init.add(((String) st.visit(this)).replace(DELIMITER, ""));
        }
        if ( fs.expr()!=null )
            expr = (String) fs.expr().visit(this);
        if ( fs.incr()!=null ) {
            incr = new ArrayList<>();
            for (Statement st: fs.incr())
                incr.add(((String) st.visit(this)).replace(DELIMITER, ""));
        }

        if ( !fs.isPar() ) {
            if ( fs.stats()!=null ) {
                Object o = fs.stats().visit(this);
                if ( o instanceof String ) {
                    stats = new String[] { (String) o };
                } else {
                    stats = (String[]) o;
                }
            }

            stForStat = stGroup.getInstanceOf("ForStat");
            stForStat.add("init", init);
            stForStat.add("expr", expr);
            stForStat.add("incr", incr);
            stForStat.add("stats", stats);

            return stForStat.render();
        }

        // Save previous barrier expressions
        ArrayList<String> prevBarrier = barriers;

        // Save the previous par-block
        String prevParBlock = currentParBlock;
        currentParBlock = Helper.makeVariableName(Tag.PAR_BLOCK_NAME.toString(), Tag.LOCAL_NAME);

        // Increment the jump label and add it to the switch-stmt list
        stForStat.add("jump", ++jumpLabel);
        switchCases.add(renderSwitchCase(jumpLabel));

        // Rendered the value of each statement
        ArrayList<String> stmts = new ArrayList<String>();
        if ( fs.stats()!=null ) {
            if ( !(fs.stats() instanceof ForStat) ) {
                Sequence<Expression> se = fs.barriers();
                if ( se!=null ) {
                    barriers = new ArrayList<>();
                    for (Expression e : se)
                        barriers.add((String) e.visit(this));
                }
                if ( fs.stats() instanceof Block ) {
                    Block bl = (Block) fs.stats();
                    for (Statement st : bl.stats()) {
                        // An expression is any valid unit of code that resolves to a value,
                        // that is, it can be a combination of variables, operations and values
                        // that yield a result. An statement is a line of code that performs
                        // some action, e.g. print statements, an assignment statement, etc.
                        if ( st instanceof ExprStat && ((ExprStat) st).expr() instanceof Invocation ) {
                            ExprStat es = (ExprStat) st;
                            Invocation in = (Invocation) es.expr();
                            // If this invocation is made on a process, then visit the
                            // invocation and return a string representing the wrapper
                            // class for this procedure; e.g.
                            //    (new <classType>(...) {
                            //        @Override public synchronized void run() { ... }
                            //        @Override public finalize() { ... }
                            //    }.schedule();
                            if ( Helper.doesProcYield(in.targetProc) )
                                stmts.add((String) in.visit(this));
                            else // Otherwise, the invocation is made through a static Java method
                                stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
                        } else
                            stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
                    }
                }
            } else
                stmts.add((String) fs.stats().visit(this));
        }

        stForStat.add("init", init);
        stForStat.add("expr", expr);
        stForStat.add("incr", incr);
        stForStat.add("stats", stmts);
        stForStat.add("name", currentParBlock);
        stForStat.add("barrier", barriers);

        inParFor = preParFor;
        barriers = prevBarrier;
        // Restore the par-block
        currentParBlock = prevParBlock;

        return stForStat.render();

    }

    @Override
    public Object visitIfStat(IfStat is) {
        Log.log(is, "Visiting a IfStat");

        ST stIfStat = stGroup.getInstanceOf("IfStat");
        // Sequence of statements enclosed in a block-stmt
        String[] thenStats = null;
        String[] thenParts = null;
        String condExpr = null;
        // We either have an if-statement _or_ a loop construct that
        // has been re-written as an if-statement
        if ( is.expr()!=null )
            condExpr = (String) is.expr().visit(this);
        if ( is.thenpart()!=null ) {
            if ( is.thenpart() instanceof Block )
                thenStats = (String[]) is.thenpart().visit(this);
            else {
                String stat = (String) is.thenpart().visit(this);
                thenStats = new String[] { stat };
            }
        }
        if ( is.elsepart()!=null ) {
            if ( is.elsepart() instanceof Block )
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
    public Object visitBinaryExpr(BinaryExpr be) {
        Log.log(be, "Visiting a BinaryExpr");

        ST stBinaryExpr = stGroup.getInstanceOf("BinaryExpr");
        String op = be.opString();
        String lhs = (String) be.left().visit(this);
        lhs = lhs.replace(DELIMITER, "");
        lhs = be.left().hasParens ? "(" + lhs + ")" : lhs;
        String rhs = (String) be.right().visit(this);
        rhs = be.right().hasParens ? "(" + rhs + ")" : rhs;
        rhs = rhs.replace(DELIMITER, "");

        // <--
        // Silly rewrite for comparing two strings in ProcessJ using the
        // equals(Xxx) method from Java
        if ( "==".equals(op) && (be.left() instanceof NameExpr && be.right() instanceof NameExpr) &&
            ((((NameExpr) be.left()).myDecl instanceof LocalDecl) &&
            ((NameExpr) be.right()).myDecl instanceof LocalDecl) ) {
            LocalDecl ld1 = (LocalDecl) ((NameExpr) be.left()).myDecl;
            LocalDecl ld2 = (LocalDecl) ((NameExpr) be.right()).myDecl;
            if ( ld1.type().isStringType() && ld2.type().isStringType() ) {
               stBinaryExpr = stGroup.getInstanceOf("StringCompare");
               stBinaryExpr.add("str1", lhs);
               stBinaryExpr.add("str2", rhs);
               return stBinaryExpr.render();
            }
        }
        // A rewrite for the 'instanceof' operator in Java happens when the token OP
        // in a binary expression represents the token 'is'. Thus, to render the correct
        // code, we look for the name of the left-hand side operand, which is a record
        // or protocol variable, and then use  the NameType of the right-hand side operand
        // as the type to check  if the left-hand side operand is indeed an instanceof
        // the right hand side operand
        if ( "instanceof".equals(op) && localToFields.containsKey(lhs) ) {
            String namedType = localToFields.get(lhs);
            Object o = topLvlDecls.get(namedType);
            if ( o instanceof RecordTypeDecl ) {
                stBinaryExpr = stGroup.getInstanceOf("RecordExtend");
                stBinaryExpr.add("name", lhs);
                stBinaryExpr.add("type", String.format("I_%s", rhs));
                return stBinaryExpr.render();
            }
            if ( namedType.equals(PJProtocolCase.class.getSimpleName()) ) {
                stBinaryExpr = stGroup.getInstanceOf("RecordExtend");
                stBinaryExpr.add("name", lhs);
                stBinaryExpr.add("type", currentProtocol);
                return stBinaryExpr.render();
            }
        }
        // -->

        stBinaryExpr.add("lhs", lhs);
        stBinaryExpr.add("rhs", rhs);
        stBinaryExpr.add("op", op);

        return stBinaryExpr.render();
    }


    @Override
    public Object visitChannelReadExpr(ChannelReadExpr cr) {
        Log.log(cr, "Visiting a ChannelReadExpr");

        ST stChannelReadExpr = stGroup.getInstanceOf("ChannelReadExpr");
        // 'c.read()' is a channel-end expression, where 'c' is the reading
        // end of the channel
        Expression chanExpr = cr.channel();
        // 'c' is the name of the channel
        String chanEndName = (String) chanExpr.visit(this);
        stChannelReadExpr.add("chanName", chanEndName);
        // One for the 'label' and one for the 'read' operation
        int countLabel = 2;
        // Add the switch block for resumption
        for (int label = 0; label<countLabel; ++label) {
            // Increment jump label and it to the switch-stmt list
            stChannelReadExpr.add("resume" + label, ++jumpLabel);
            switchCases.add(renderSwitchCase(jumpLabel));
        }

        return stChannelReadExpr.render();
    }

    /// ------------------------------------------------------------------------------------------------
    /// Refactored

    @Override
    public Object visitModifier(final Modifier modifier) {

        Log.log(modifier, "Visiting a Modifier (" + modifier + ")");

        return modifier.toString();

    }

    @Override
    public Object visitBlock(final Block block) {

        Log.log(block, "Visiting a Block");

        final StringBuilder builder = new StringBuilder();

        for(final Statement statement: block.stats())
            builder.append((String) block.stats().visit(this) + "\n");

        return builder.toString();

    }

    @Override
    public Object visitWhileStat(final WhileStat whileStatement) {

        Log.log(whileStatement, "Visiting a WhileStat");

        final String  expression  = (whileStatement.expr() != null) ?
            (String) whileStatement.expr().visit(this) : "";
        final String  statements  = (whileStatement.stat() != null) ?
            (String) whileStatement.stat().visit(this) : "";

        return "while(" + expression + ") {\n\n" + statements + "\n}\n\n";

    }

    @Override
    public Object visitAssignment(final Assignment assignment) {

        Log.log(assignment, "Visiting an Assignment");

        String op   = assignment.opString();
        String type = "";
        String lhs  = "";
        String rhs  = "";

        if(assignment.right() instanceof NewArray)
            return createNewArray(lhs, ((NewArray) assignment.right()));

        else if(assignment.right() instanceof ChannelReadExpr)
            return createChannelReadExpr(lhs, type, op, ((ChannelReadExpr) assignment.right()));

        if(assignment.left() != null)
            lhs = assignment.left().type.getJavaWrapper();

        else if(assignment.right() != null)
            rhs = (String) assignment.right().visit(this);

        return lhs + (((op != null) && (!op.isEmpty()) ? " " + op + " " + rhs : "")) + ";";

    }

    @Override
    public Object visitExprStat(final ExprStat expressionStatement) {

        Log.log(expressionStatement, "Visiting an ExprStat");

        return expressionStatement.expr().visit(this);

    }

    @Override
    public Object visitBreakStat(final BreakStat breakStatement) {

        Log.log(breakStatement, "Visiting a BreakStat");

        return ((breakStatement.target() != null) ?
            (String) breakStatement.target().visit(this) + " " : "") + "break;";

    }

    @Override
    public Object visitContinueStat(final ContinueStat continueStatement) {

        Log.log(continueStatement, "Visiting a ContinueStat");

        return "continue" + ((continueStatement.target() != null) ?
            " " + continueStatement.target().visit(this) : "") + ";";

    }

    @Override
    public Object visitParamDecl(final ParamDecl parameterDeclaration) {

        Log.log(parameterDeclaration, "Visiting a ParamDecl (" + parameterDeclaration.type().typeName() + " " + parameterDeclaration.paramName().getname() + ")");

        final String name     = parameterDeclaration.paramName().getname()                  ;
        final String newName  = parameterDeclaration.paramName().getAlias(Tag.PARAM_NAME)   ;
        final String type     = parameterDeclaration.type().getJavaWrapper()                ;

        paramToFields.put(newName, type);
        paramToVarNames.put(name, newName);

        return null;

    }

    @Override
    public Object visitConstantDecl(final ConstantDecl constantDeclaration) {

        Log.log(constantDeclaration, "Visting ConstantDecl (" + constantDeclaration.type().typeName() + " " + constantDeclaration.var().name().getname() + ")");

        final String type           = constantDeclaration.getType().getJavaWrapper()                        ;
        final String name           = constantDeclaration.getName().getname()                               ;
        final String initializer    = (String) constantDeclaration.getInitializerExpression().visit(this)   ;

        return type + " " + name + ((initializer != null) ? " = " + initializer : "") + ";";

    }

    @Override
    public Object visitLocalDecl(final LocalDecl localDeclaration) {

        Log.log(localDeclaration, "Visting a LocalDecl (" + localDeclaration.type().typeName() + " " + localDeclaration.var().name().getname() + ")");

        final String        name           = localDeclaration.getName().toString()                 ;
        final String        type           = localDeclaration.type().getJavaWrapper()              ;
        final Expression    initializer    = localDeclaration.var().init()                         ;
        final String        newName        = localDeclaration.getName().getAlias(Tag.LOCAL_NAME)   ;

        if(inParFor) {

            localsForAnonymousProcess.put(newName, type);
            paramsForAnonymousProcess.put(name, newName);

        }

        localToFields.put(newName, type);
        paramToVarNames.put(name, newName);

        String val = "";

        if(initializer != null)
            val = (String) initializer.visit(this);

        else if(localDeclaration.type().isBarrierType())
            val = "new PJBarrier()";

        else if(localDeclaration.type().isChannelType())
            val = "new " + type + "()";

        return type + " " + newName + ((!val.isEmpty()) ? " = " + val : "") + ";";

    }

    @Override
    public Object visitTernary(final Ternary ternary) {

        Log.log(ternary, "Visiting a Ternary");

        final String expression   = (String) ternary.expr().visit(this);
        final String trueBranch   = (String) ternary.trueBranch().visit(this);
        final String falseBranch  = (String) ternary.falseBranch().visit(this);

        return "(" + expression + ") ? " + trueBranch + " : " + falseBranch;

    }

    @Override
    public Object visitNewArray(final NewArray newArray) {

        Log.log(newArray, "Visiting a NewArray");

        return createNewArray(null, newArray);

    }
    
    @Override
    public Object visitCastExpr(final CastExpr castExpression) {

        Log.log(castExpression, "Visiting a CastExpr");

        final String type         = (String) castExpression.type().getJavaWrapper();
        final String expression   = (String) castExpression.expr().visit(this);

        return "(" + ((!type.isEmpty()) ? "(" + type + ") " : "") + "(" + expression + "))";
        
    }

    @Override
    public Object visitNameExpr(final NameExpr nameExpression) {

        Log.log(nameExpression, "Visiting a NameExpr (" + nameExpression.name().getname() + ")");

        return nameExpression.name().getname();

    }

    @Override
    public Object visitChannelEndExpr(final ChannelEndExpr channelEndExpression) {
        
        Log.log(channelEndExpression, "Visiting a ChannelEndExpr (" + (channelEndExpression.isRead() ? "read" : "write") + ")");

        return channelEndExpression.channel().visit(this);

    }

    @Override
    public Object visitPrimitiveType(final PrimitiveType primitiveType) {

        return primitiveType.getJavaWrapper();

    }

    @Override
    public Object visitChannelType(final ChannelType channelType) {

        return channelType.getJavaWrapper();

    }

    @Override
    public Object visitChannelEndType(final ChannelEndType channelEndType) {

        return channelEndType.getJavaWrapper();
        
    }

    @Override
    public Object visitNamedType(final NamedType namedType) {

        return namedType.getJavaWrapper();

    }

    @Override
    public Object visitArrayType(final ArrayType arrayType) {

        return arrayType.getJavaWrapper();

    }

    @Override
    public Object visitPrimitiveLiteral(final PrimitiveLiteral primitiveLiteral) {

        Log.log(primitiveLiteral, "Visiting a Primitive Literal (" + primitiveLiteral.getText() + ")");

        return primitiveLiteral.getText() + ((primitiveLiteral.isSuffixed()) ? primitiveLiteral.suffix() : "");

    }

    @Override
    public Object visitArrayLiteral(final ArrayLiteral arrayLiteral) {

        Log.log(arrayLiteral, "Visiting an ArrayLiteral");

        final StringBuilder builder = new StringBuilder("{ ");

        int current = 0;

        for(final Object expression: arrayLiteral.elements()) {

            builder.append(((Expression) expression).visit(this));

            if(current < (arrayLiteral.elements().size() - 1))
                builder.append(", ");

        }

        builder.append(" }");

        return builder.toString();

    }

    /// --------------------------------------------------------------------------------------------
    /// End Refactored

    @Override
    public Object visitRecordTypeDecl(RecordTypeDecl rt) {
        Log.log(rt, "Visiting a RecordTypeDecl (" + rt.name().getname() + ")");

        ST stRecordType = stGroup.getInstanceOf("RecordType");
        String recName = (String) rt.name().getname();
        ArrayList<String> modifiers = new ArrayList<>();

        for (Modifier m : rt.modifiers())
            modifiers.add((String) m.visit(this));

        // Remove fields from previous record
        recordMemberToField.clear();

        // The scope in which all members appeared in a record
        for (RecordMember rm : rt.body())
            rm.visit(this);

        // The list of fields which should be passed to the constructor
        // of the static class that the record belongs to
        if ( !recordMemberToField.isEmpty() ) {
            stRecordType.add("types", recordMemberToField.values());
            stRecordType.add("vars", recordMemberToField.keySet());
        }

        ArrayList<String> extend = new ArrayList<>();
        extend.add(recName);
        if ( rt.extend().size()>0 )
            for (Name n : rt.extend())
                extend.add(n.getname());

        stRecordType.add("extend", extend);
        stRecordType.add("name", recName);
        stRecordType.add("modifiers", modifiers);

        return stRecordType.render();
    }

    @Override
    public Object visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        Log.log(pd, "Visiting a ProtocolTypeDecl (" + pd.name().getname() + ")");

        ST stProtocolClass = stGroup.getInstanceOf("ProtocolClass");
        String name = (String) pd.name().getAlias(Tag.PROTOCOL_NAME);
        ArrayList<String> modifiers = new ArrayList<>();
        ArrayList<String> body = new ArrayList<>();

        for (Modifier m : pd.modifiers())
            modifiers.add((String) m.visit(this));

        currentProtocol = name;
        // We use tags to associate parent and child protocols
        if ( pd.extend().size()>0 ) {
            for (Name n : pd.extend()) {
                ProtocolTypeDecl ptd = (ProtocolTypeDecl) topLvlDecls.get(n.getname());
                for (ProtocolCase pc : ptd.body())
                    protocolNameToProtocolTag.put(String.format("%s.%s", pd.name().getname(),
                            pc.name().getname()), ptd.name().getname());
            }
        }

        // The scope in which all protocol members appear
        if ( pd.body()!=null )
            for (ProtocolCase pc : pd.body())
                body.add((String) pc.visit(this));

        stProtocolClass.add("name", name);
        stProtocolClass.add("modifiers", modifiers);
        stProtocolClass.add("body", body);

        return stProtocolClass.render();
    }

    @Override
    public Object visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a ProcTypeDecl (" + pd.name().getname() + ")");

        ST stProcTypeDecl = null;
        // Save previous procedure state
        String prevProcName = currentProcName;
        // Save previous jump labels
        ArrayList<String> prevLabels = switchCases;
        if ( !switchCases.isEmpty() )
            switchCases = new ArrayList<>();
        // Name of the invoked procedure
        currentProcName = (String) pd.name().getAlias(Tag.PROCEDURE_NAME);
        // Procedures are static classes which belong to the same package and
        // class. To avoid having classes with the same name, we generate a
        // new name for the currently executing procedure
        String procName = null;
        // For non-invocations, that is, for anything other than a procedure
        // that yields, we need to extends the PJProcess class anonymously
        if ( "Anonymous".equals(currentProcName) ) {
            // Preserve current jump label for resumption
            int prevJumpLabel = jumpLabel;
            jumpLabel = 0;
            // Create an instance for such anonymous procedure
            stProcTypeDecl = stGroup.getInstanceOf("AnonymousProcess2");
            // Statements that appear in the procedure being executed
            String[] body = (String[]) pd.body().visit(this);
            stProcTypeDecl.add("parBlock", currentParBlock);
            stProcTypeDecl.add("syncBody", body);
            stProcTypeDecl.add("isPar", inParFor);
            // Add the barrier this procedure should resign from
            if ( !barriers.isEmpty() )
                stProcTypeDecl.add("barrier", barriers);
            // Add the switch block for yield and resumption
            if ( !switchCases.isEmpty() ) {
                ST stSwitchBlock = stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", switchCases);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }
            // The list of local variables defined in the body of a procedure
            // becomes the instance fields of the class
            if ( !localsForAnonymousProcess.isEmpty() ) {
                stProcTypeDecl.add("ltypes", localsForAnonymousProcess.values());
                stProcTypeDecl.add("lvars", localsForAnonymousProcess.keySet());
            }
            // Restore jump label so it knows where to resume from
            jumpLabel = prevJumpLabel;
        } else {
            // Restore global variables for a new PJProcess class
            resetGlobals();
            // Formal parameters that must be passed to the procedure
            Sequence<ParamDecl> formals = pd.formalParams();
            // Do we have any parameters?
            if ( formals!=null && formals.size()>0 ) {
                // Iterate through and visit every parameter declaration.
                // Retrieve the name and type of each parameter specified in
                // a list of comma-separated arguments. Note that we ignored
                // the value returned by this visitor
                for (int i=0; i<formals.size(); ++i)
                    formals.child(i).visit(this);
            }
            // Visit all declarations that appear in the procedure
            String[] body = null;
            if ( pd.body()!=null ) body = (String[]) pd.body().visit(this);
            // Retrieve the modifier(s) attached to the invoked procedure such
            // as private, public, protected, etc.
            String[] modifiers = (String[]) pd.modifiers().visit(this);
            // Grab the return type of the invoked procedure
            String procType = (String) pd.returnType().getJavaWrapper();
            // The procedure's annotation determines if we have a yielding procedure
            // or a Java method (a non-yielding procedure)
            boolean doesProcYield = Helper.doesProcYield(pd);
            // Set the template to the correct instance value and then initialize
            // its attributes
            if ( doesProcYield ) {
                // This procedure yields! Grab the instance of a yielding procedure
                // from the string template in order to define a new class
                procName = Helper.makeVariableName(currentProcName + hashSignature(pd), Tag.PROCEDURE_NAME);
                stProcTypeDecl = stGroup.getInstanceOf("ProcClass");
                stProcTypeDecl.add("name", procName);
                // Add the statements that appear in the body of the procedure
                stProcTypeDecl.add("syncBody", body);
            } else {
                // Otherwise, grab the instance of a non-yielding procedure to
                // define a new static Java method
                procName = Helper.makeVariableName(currentProcName + hashSignature(pd), Tag.METHOD_NAME);
                stProcTypeDecl = stGroup.getInstanceOf("Method");
                stProcTypeDecl.add("name", procName);
                stProcTypeDecl.add("type", procType);
                // Do we have any access modifier? If so, add them
                if ( modifiers!=null && modifiers.length>0 )
                    stProcTypeDecl.add("modifier", modifiers);
                stProcTypeDecl.add("body", body);
            }

            // Create an entry point for the ProcessJ program, which is just
            // a Java main method that is called by the JVM
            if ( "main".equals(currentProcName) && pd.signature().equals(Tag.MAIN_NAME.toString()) ) {
                // Create an instance of a Java main method template
                ST stMain = stGroup.getInstanceOf("Main");
                stMain.add("class", currentCompilation.fileNoExtension());
                stMain.add("name", procName);
                // Pass the list of command line arguments to this main method
                if ( !paramToFields.isEmpty() ) {
                    stMain.add("types", paramToFields.values());
                    stMain.add("vars", paramToFields.keySet());
                }
                // Add the entry point of the program
                stProcTypeDecl.add("main", stMain.render());
            }
            // The list of command-line arguments should be passed to the constructor
            // of the static class that the main method belongs or be passed to the
            // static method
            if ( !paramToFields.isEmpty() ) {
                stProcTypeDecl.add("types", paramToFields.values());
                stProcTypeDecl.add("vars", paramToFields.keySet());
            }
            // The list of local variables defined in the body of a procedure
            // becomes the instance fields of the class
            if ( !localToFields.isEmpty() ) {
                stProcTypeDecl.add("ltypes", localToFields.values());
                stProcTypeDecl.add("lvars", localToFields.keySet());
            }
            // Add the switch block for resumption (if any)
            if ( !switchCases.isEmpty() ) {
                ST stSwitchBlock = stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", switchCases);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }
        }

        // Restore and reset previous values
        currentProcName = prevProcName;
        // Restore previous jump labels
        switchCases = prevLabels;

        return stProcTypeDecl.render();
    }

    @Override
    public Object visitChannelWriteStat(ChannelWriteStat cw) {
        Log.log(cw, "Visiting a ChannelWriteStat");

        ST stChanWriteStat = stGroup.getInstanceOf("ChanWriteStat");
        // 'c.write(x)' is a channel-end expression, where 'c' is the
        // writing end of the channel
        Expression chanExpr = cw.channel();
        // 'c' is the name of the channel
        String chanWriteName = (String) chanExpr.visit(this);
        // Expression sent through channel
        String expr = (String) cw.expr().visit(this);
        expr = expr.replace(DELIMITER, "");
        // The value one is for the 'runLabel'
        int countLabel = 1;
        // Is the writing end of this channel shared?
        if ( chanExpr.type.isChannelEndType() && ((ChannelEndType) chanExpr.type).isShared() ) {
            stChanWriteStat = stGroup.getInstanceOf("ChannelMany2One");
            ++countLabel;
        }

        stChanWriteStat.add("chanName", chanWriteName);
        stChanWriteStat.add("writeExpr", expr);

        // Add the switch block for resumption
        for (int label = 0; label<countLabel; ++label) {
            // Increment jump label and it to the switch-stmt list
            stChanWriteStat.add("resume" + label, ++jumpLabel);
            switchCases.add(renderSwitchCase(jumpLabel));
        }

        return stChanWriteStat.render();
    }

    @Override
    public Object visitArrayAccessExpr(ArrayAccessExpr ae) {
        Log.log( "Visiting an ArrayAccessExpr");

        ST stArrayAccessExpr = stGroup.getInstanceOf("ArrayAccessExpr");

        final String name  = (String) ae.target().visit(this);
        final String index = (String) ae.index().visit(this);

        stArrayAccessExpr.add("name", name);
        stArrayAccessExpr.add("index", index);

        return String.format("%s[%s]", name, index);

    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object visitSequence(Sequence se) {
        Log.log(se, "Visiting a Sequence");

        // Sequence of statements enclosed in a block-stmt
        ArrayList<String> seqs = new ArrayList<>();
        // Iterate through every statement
        for (int i = 0; i < se.size(); ++i) {
            if ( se.child(i)!=null ) {
                Object stats = se.child(i).visit(this);
                if ( stats==null )
                    continue;
                // These are either
                //   1.) a sequence of statements, or
                //   2.) a single statement
                // found in a block statement, e.g. local declarations,
                // variable declarations, invocations, etc.
                if ( stats instanceof String[] )
                    seqs.addAll(Arrays.asList((String[]) stats));
                else
                    seqs.add((String) stats);
            }
        }

        return seqs.toArray(new String[0]);
    }



    @Override
    public Object visitSwitchLabel(SwitchLabel sl) {
        Log.log(sl, "Visiting a SwitchLabel");

        ST stSwitchLabel = stGroup.getInstanceOf("SwitchLabel");

        // This could be a default label, in which case, expr() would be null
        String label = null;
        if ( !sl.isDefault() )
            label = (String) sl.expr().visit(this);
        if ( isProtocolCase ) {
            // The protocol tag currently being used
            currentProtocolTag = label;
            label = String.format("\"%s\"", label);
        }

        stSwitchLabel.add("label", label);

        return stSwitchLabel.render();
    }

    @Override
    public Object visitSwitchGroup(SwitchGroup sg) {
        Log.log(sg, "Visit a SwitchGroup");

        ST stSwitchGroup = stGroup.getInstanceOf("SwitchGroup");

        ArrayList<String> labels = new ArrayList<>();
        for (SwitchLabel sl : sg.labels())
            labels.add((String) sl.visit(this));

        ArrayList<String> stats = new ArrayList<>();
        for (Statement st : sg.statements()) {
            if ( st!=null ) {
                Object stmt = st.visit(this);
                if ( stmt instanceof String[] )
                    stats.addAll(Arrays.asList((String[]) stmt));
                else
                    stats.add((String) stmt);
            }
        }

        stSwitchGroup.add("labels", labels);
        stSwitchGroup.add("stats", stats);

        return stSwitchGroup.render();
    }

    @Override
    public Object visitSwitchStat(SwitchStat st) {
        Log.log(st, "Visiting a SwitchStat");

        ST stSwitchStat = stGroup.getInstanceOf("SwitchStat");
        // Is this a protocol tag?
        if ( st.expr().type.isProtocolType() )
            isProtocolCase = true;

        String expr = (String) st.expr().visit(this);
        ArrayList<String> switchGroup = new ArrayList<>();

        for (SwitchGroup sg : st.switchBlocks())
            switchGroup.add((String) sg.visit(this));

        stSwitchStat.add("tag", isProtocolCase);
        stSwitchStat.add("expr", expr);
        stSwitchStat.add("block", switchGroup);

        // Reset the value for this protocol tag
        isProtocolCase = false;

        return stSwitchStat.render();
    }


    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object visitInvocation(Invocation in) {

        // We ignore any GOTO or LABEL invocation since they are only needed
        // for the __asm__ bytecode rewrite
        if ( in.ignore ) {
            Log.log(in, "Visiting a " + in.procedureName().getname());
            ST stIgnore = stGroup.getInstanceOf("InvocationIgnore");
            stIgnore.add("name", in.procedureName().getAlias(Tag.PROCEDURE_NAME));
            stIgnore.add("var", in.params().visit(this));
            return stIgnore.render();
        }
        // <--
        // Check if the invocation is either a 'sync' or a 'fork'
        if ( in.procedureName().simplename().equals("sync") ||
             in.procedureName().simplename().equals("fork")) {
            Log.log(in, "Visiting Invocation (" + in.procedureName().simplename() + ")");
            // TODO:
        }
        // -->

        ST stInvocation = null;
        // Target procedure
        ProcTypeDecl pd = in.targetProc;



        // Name of invoked procedure
        String pdName = pd.name().getname();
        // Check local procedures, if none is found then the procedure must? 
        // come from a different file and maybe package
        if ( currentCompilation.fileName.equals(pd.myCompilation.fileName) ) {
            String name = pdName + hashSignature(pd);
            if ( Helper.doesProcYield(pd) )
                name = Helper.makeVariableName(name, Tag.PROCEDURE_NAME);
            else
                name = Helper.makeVariableName(name, Tag.METHOD_NAME);
            pdName = pd.myCompilation.fileNoExtension() + "." + name;
        } else if ( pd.isNative ) {
            // Make the package visible on import by using the qualified
            // name of the class the procedure belongs to and the name of
            // the directory the procedure's class belongs to, e.g.
            // std.io.println(), where
            //   1.) 'std' is the name of the package,
            //   2.) 'io' is the name of the class/file,
            //   3.) 'println' is the method declared in the class
            pdName = pd.filename + "." + pdName;
        } else
            ; // TODO: This procedure is called from another package

        // These are the formal parameters of the procedure/method being invoked
        // which are specified by a list of comma-separated arguments
        Sequence<Expression> parameters = in.params();
        String[] paramsList = (String[]) parameters.visit(this);
        if ( paramsList!=null )
            for (int i = 0; i<paramsList.length; ++i)
                paramsList[i] = paramsList[i].replace(DELIMITER, "");

        // For an invocation of a procedure that yields and one which
        // is not inside par-block, we wrap the procedure in a par-block
        if ( Helper.doesProcYield(pd) && currentParBlock==null ) {
            return (new ParBlock(
                    new Sequence(new ExprStat(in)), // Statements
                    new Sequence()))                // Barriers
                    .visit(this);                   // Return a procedure wrapped in a par-block
        }

        // Does this procedure yield?
        if ( Helper.doesProcYield(pd) ) {
            stInvocation = stGroup.getInstanceOf("InvocationProcType");
            stInvocation.add("parBlock", currentParBlock);
            // <--
            stInvocation.add("isPar", inParFor);
            // -->
            // Add the barrier this procedure should resign from
            if ( !barriers.isEmpty() )
                stInvocation.add("barrier", barriers);
        } else
            // Must be an invocation made through a static Java method
            stInvocation = stGroup.getInstanceOf("Invocation");

        stInvocation.add("name", pdName);
        stInvocation.add("vars", paramsList);

        return stInvocation.render();
    }

    @Override
    public Object visitImport(Import im) {
        Log.log(im, "Visiting an import statement (" + im + ")");

        ST stImport = stGroup.getInstanceOf("Import");
        stImport = stGroup.getInstanceOf("Import");
        stImport.add("package", im.toString());

        return stImport.render();
    }



    @Override
    public Object visitProtocolCase(ProtocolCase pc) {
        Log.log(pc, "Visiting a ProtocolCase (" + pc.name().getname() + ")");

        ST stProtocolType = stGroup.getInstanceOf("ProtocolType");
        // Since we are keeping the name of a tag as is, this (in theory)
        // shouldn't cause any name collision
        String protocName = (String) pc.name().getname();
        // This shouldn't create name collision problems even if we
        // use the same visitor for protocols and records
        recordMemberToField.clear();

        // The scope in which all members of this tag appeared
        for (RecordMember rm : pc.body())
            rm.visit(this);

        // The list of fields passed to the constructor of the static
        // class that the record belongs to
        if ( !recordMemberToField.isEmpty() ) {
            stProtocolType.add("types", recordMemberToField.values());
            stProtocolType.add("vars", recordMemberToField.keySet());
        }
        stProtocolType.add("name", protocName);

        return stProtocolType.render();
    }

    @Override
    public Object visitProtocolLiteral(ProtocolLiteral pl) {
        Log.log(pl, "Visiting a ProtocolLiteral (" + pl.name().getname() + ")");

        ST stProtocolLiteral = stGroup.getInstanceOf("ProtocolLiteral");
        String type = (String) pl.name().getname();
        String tag = (String) pl.tag().visit(this);

        // This map is used to determine the order in which values are
        // used with the constructor of the class associated with this
        // kind of protocol
        HashMap<String, String> members = new LinkedHashMap<>();
        // We need the members of the tag currently being used
        ProtocolTypeDecl pt = (ProtocolTypeDecl) topLvlDecls.get(type);

        if ( pt!=null ) {
            ProtocolCase target = pt.getCase(tag);
            // Now that we have the target tag, iterate over all of its members
            for (RecordMember rm : target.body()) {
                String name = rm.name().getname();
                members.put(name, null);
            }
        }

        // A visit to a RecordLiteral returns a string of the form:
        // VAR = VAL, where VAR is a record member, and VAR is the
        // value assigned to VAR. Instead of parsing the string, we
        // are going to grab the values assigned to each protocol
        // member, one by one, while traversing the AST
        for (RecordMemberLiteral rm : pl.expressions()) {
            String lhs = rm.name().getname();
            String expr = (String) rm.expr().visit(this);
            if ( members.put(lhs, expr)==null )
                Log.log(pl, "> Initializing '" + lhs + "' with '" + expr + "'");
        }

        stProtocolLiteral.add("type", type);
        stProtocolLiteral.add("tag", tag);
        stProtocolLiteral.add("vals", members.values());

        return stProtocolLiteral.render();
    }

    @Override
    public Object visitRecordMember(RecordMember rm) {
        Log.log(rm, "Visiting a RecordMember (" + rm.type() + " " + rm.name().getname() + ")");

        String name = rm.name().getname();
        String type = (String) rm.type().getJavaWrapper();

        // Check if the type is a record or protocol type
        if ( rm.type().isRecordType() )
            type = ((RecordTypeDecl) rm.type()).name().getname();
        else if ( rm.type().isProtocolType() )
            type = PJProtocolCase.class.getSimpleName();

        // Add this field to the collection of record members for reference
        recordMemberToField.put(name, type);

        // Ignored the value returned by this visitor as the types and
        // variables are _always_ resolved elsewhere
        return null;
    }

    @Override
    public Object visitRecordLiteral(RecordLiteral rl) {
        Log.log(rl, "Visiting a RecordLiteral (" + rl.name().getname() + ")");

        ST stRecordListeral = stGroup.getInstanceOf("RecordLiteral");
        String type = (String) rl.name().getname();

        // This map is used to determine the order in which values
        // are passed to the constructor of the class associated
        // with this record
        HashMap<String, String> members = new LinkedHashMap<>();
        RecordTypeDecl rt = (RecordTypeDecl) topLvlDecls.get(type);

        if ( rt!=null )
            for (RecordMember rm : rt.body()) {
                String name = rm.name().getname();
                members.put(name, null);
            }

        // A visit to a RecordMemberLiteral returns a string of the form:
        // VAR = VAL, where VAR is a record member, and VAR is the value
        // assigned to VAR. Instead of parsing the string, we are going
        // to grab the values assigned to each record member, one by one,
        // while traversing the AST
        for (RecordMemberLiteral rm : rl.members()) {
            String lhs = rm.name().getname();
            String expr = (String) rm.expr().visit(this);
            if ( members.put(lhs, expr)==null )
                Log.log(rl, "> Initializing '" + lhs + "' with '" + expr + "'");
        }

        stRecordListeral.add("type", type);
        stRecordListeral.add("vals", members.values());

        return stRecordListeral.render();
    }

    @Override
    public Object visitRecordAccess(RecordAccess ra) {
        Log.log(ra, "Visiting a RecordAccess (" + ra + ")");

        ST stAccessor = stGroup.getInstanceOf("RecordAccessor");

        if ( ra.record().type.isRecordType() ) {
            String name = (String) ra.record().visit(this);
            String field = ra.field().getname();
            stAccessor.add("name", name);
            stAccessor.add("member", field);
        } else if ( ra.record().type.isProtocolType() ) {
            stAccessor = stGroup.getInstanceOf("ProtocolAccess");
            ProtocolTypeDecl pt = (ProtocolTypeDecl) ra.record().type;
            String protocName = (String) pt.name().getname(); // Wrapper class
            String name = (String) ra.record().visit(this); // Reference to inner class type
            String field = ra.field().getname(); // Field in inner class

            // Cast a protocol to a super-type if needed
            String key = String.format("%s.%s", protocName, currentProtocolTag);
            if ( protocolNameToProtocolTag.get(key)!=null )
                protocName = protocolNameToProtocolTag.get(key);

            stAccessor.add("protocName", protocName);
            stAccessor.add("tag", currentProtocolTag);
            stAccessor.add("var", name);
            stAccessor.add("member", field);
        }
        // This is for arrays and strings -- ProcessJ has no notion of classes,
        // i.e. it has no concept of objects either. Arrays and strings are
        // therefore treated as primitive data types
        else {
            String name = (String) ra.record().visit(this);
            stAccessor.add("name", name);
            // Call the appropriate method to retrieve the number of characters
            // in a string or the number of elements in an N-dimensional array
            if ( ra.isArraySize ) // 'Xxx.size' for N-dimensional array
                stAccessor.add("member", "length");
            else if ( ra.isStringLength ) // 'Xxx.length' for number of characters in a string
                stAccessor.add("member", "length()");
        }

        return stAccessor.render();
    }

    @Override
    public Object visitParBlock(ParBlock pb) {
        Log.log(pb, "Visiting a ParBlock with " + pb.stats().size() + " statements.");

        // Don't generate code for an empty par statement
        if ( pb.stats().size()==0 )
            return null;
        ST stParBlock = stGroup.getInstanceOf("ParBlock");
        // Save the previous par-block
        String prevParBlock = currentParBlock;
        // Save previous barrier expressions
        ArrayList<String> prevBarrier = barriers;
        // Create a name for this new par-block
        currentParBlock = Helper.makeVariableName(Tag.PAR_BLOCK_NAME.toString(), Tag.LOCAL_NAME);
        // Since this is a new par-block, we need to create a variable
        // inside the process in which this par-block was declared
        stParBlock.add("name", currentParBlock);
        stParBlock.add("count", pb.stats().size());
        stParBlock.add("process", "this");

        // Increment the jump label and add it to the switch-stmt list
        stParBlock.add("jump", ++jumpLabel);
        switchCases.add(renderSwitchCase(jumpLabel));
        // Add the barrier this par-block enrolls in
        if ( pb.barriers().size()>0 ) {
            HashMap<String, Integer> parBarries = new HashMap<>();
            for (Expression e : pb.barriers()) {
                String name = (String) e.visit(this);
                parBarries.put(name, pb.enrolls.get(((NameExpr) e).name().getname()));
            }
            stParBlock.add("barrier", parBarries.keySet());
            stParBlock.add("enrollees", parBarries.values());
        }
        // Visit the sequence of statements in the par-block
        Sequence<Statement> statements = pb.stats();
        // Rendered the value of each statement
        ArrayList<String> stmts = new ArrayList<String>();
        for (Statement st : statements) {
            if ( st==null )
                continue;
            // <--
            Sequence<Expression> se = st.barrierNames;
            if ( se!=null ) {
                barriers = new ArrayList<>();
                for (Expression e : se)
                    barriers.add((String) e.visit(this));
            }
            // -->
            // An expression is any valid unit of code that resolves to a value,
            // that is, it can be a combination of variables, operations and values
            // that yield a result. An statement is a line of code that performs
            // some action, e.g. print statements, an assignment statement, etc.
            if ( st instanceof ExprStat && ((ExprStat) st).expr() instanceof Invocation ) {
                ExprStat es = (ExprStat) st;
                Invocation in = (Invocation) es.expr();
                // If this invocation is made on a process, then visit the
                // invocation and return a string representing the wrapper
                // class for this procedure; e.g.
                //    (new <classType>(...) {
                //        @Override public synchronized void run() { ... }
                //        @Override public finalize() { ... }
                //    }.schedule();
                if ( Helper.doesProcYield(in.targetProc) )
                    stmts.add((String) in.visit(this));
                else // Otherwise, the invocation is made through a static Java method
                    stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
            } else
                stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
        }
        stParBlock.add("body", stmts);
        // Restore the par-block
        currentParBlock = prevParBlock;
        // Restore barrier expressions
        barriers = prevBarrier;

        return stParBlock.render();
    }

    @Override
    public Object visitTimeoutStat(TimeoutStat ts) {
        Log.log(ts, "Visiting a TimeoutStat");

        ST stTimeoutStat = stGroup.getInstanceOf("TimeoutStat");
        String timer = (String) ts.timer().visit(this);
        String delay = (String) ts.delay().visit(this);

        stTimeoutStat.add("name", timer);
        stTimeoutStat.add("delay", delay);

        // Increment the jump label and add it to the switch-stmt list
        stTimeoutStat.add("resume0", ++jumpLabel);
        switchCases.add(renderSwitchCase(jumpLabel));

        return stTimeoutStat.render();
    }

    @Override
    public Object visitSyncStat(SyncStat st) {
        Log.log(st, "Visiting a SyncStat");

        ST stSyncStat = stGroup.getInstanceOf("SyncStat");
        String barrier = (String) st.barrier().visit(this);
        stSyncStat.add("barrier", barrier);

        // Increment the jump label and add it to the switch-stmt list
        stSyncStat.add("resume0", ++jumpLabel);
        switchCases.add(renderSwitchCase(jumpLabel));

        return stSyncStat.render();
    }

    @Override
    public Object visitUnaryPostExpr(UnaryPostExpr ue) {
        Log.log(ue, "Visiting a UnaryPostExpr (" + ue.opString() + ")");

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

        ST stAltCase = stGroup.getInstanceOf("AltCase");
        Statement stat = ac.guard().guard();
//        String guard = (String) stat.visit(this);
        String guard = stat instanceof TimeoutStat? null : (String) stat.visit(this);
        String[] stats = (String[]) ac.stat().visit(this);
        // <--
        if ( !indexSetOfAltCase.isEmpty() ) {
            ST stRepLocalVars = stGroup.getInstanceOf("RepLocalVars");
            stRepLocalVars.add("indexSet", indexSetOfAltCase);
            stAltCase.add("dynamicAlt", stRepLocalVars.render());
        }
        // -->
        stAltCase.add("number", ac.getCaseNumber());
        stAltCase.add("guardExpr", guard);
        stAltCase.add("stats", stats);

        return stAltCase.render();
    }

    /** List of replicated alt loops */
    ArrayList<ST> arrayOfReplicatedAltLoop = new ArrayList<>();
    ArrayList<String> indexSetOfAltCase = new ArrayList<>();
    ArrayList<String> listOfReplicatedAltLocals = new ArrayList<>();
    ArrayList<String> listOfReplicatedAltCases = new ArrayList<>();
    ArrayList<String> listOfReplicatedObjectGuards = new ArrayList<>();
    ArrayList<String> listOfReplicatedAltLoops = new ArrayList<>();

    int indexForAltStat = 1;
    int readyID = 0;
    int booleanGuardID = 0;
    int objectGuardID = 0;

    @Override
    public Object visitAltStat(AltStat as) {
        Log.log(as, "Visiting an AltStat");

        // For dynamic or replicated alts we use 'AltGuard' objects, which contain
        // the indices of an n-array, where 'n' represents the number of loops that
        // wrap this guard, and a case number
        // *******************************************************************
        // *******************************************************************
        // <--
        // Added: 06/02/2022 -- Rewrite for replicated atls
        // This is where we handle the generated code for RepAlts; noote that
        // a dynamic alt is one that contains a replicated Alt
        if ( as.isReplicated() || as.isDynamic() ) {
            Log.log(as, "Visiting a Dynamic or Replicated Alt");
            // This queue contains the number of cases found in the alt stmt
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            Sequence<AltCase> reAltCase = as.body();
            // This section belongs to the generated code for the ForStat
            // The ST for the ForStat pertaining to an AltCase should be
            // created only for replicated Alts
            if ( as.isReplicated() )
                arrayOfReplicatedAltLoop.add((ST) createAltForStat(as));
            // Count the number of cases of this dynamic alt
            for (int i=0; i<reAltCase.size(); ++i)
                queue.add(i);
            // Rewrite guards that are not part of an altStat
            for (int i=0; i<reAltCase.size(); ++i) {
                AltCase ac = reAltCase.child(i);
                if ( !ac.isAltStat() )
                    reAltCase.set(i, createDynamicAltStat(ac));
            }
            int childIndex = 0;
            for (int altCaseIndex = queue.remove();;) {
//                System.err.println("[" + altCaseIndex + "]");
                AltCase ac = reAltCase.child(childIndex);
                ac.setCaseNumber(altCaseIndex);
                if ( ac.isAltStat() ) {
                    ST stForStat = (ST) createAltForStat((AltStat) ((Block) ac.stat()).stats().child(0));
                    arrayOfReplicatedAltLoop.add(stForStat);
                    AltStat as2 = (AltStat) ((Block) ac.stat()).stats().child(0);
                    reAltCase = as2.body();
//                    altCaseIndex = 0;
                    childIndex = 0;
                } else  {
                    ST forStat = null;
                    if ( !arrayOfReplicatedAltLoop.isEmpty() ) {
                        forStat = arrayOfReplicatedAltLoop.get(0);
                        for (int j=1; j<arrayOfReplicatedAltLoop.size(); ++j) {
                            forStat.add("stats", arrayOfReplicatedAltLoop.get(j));
                            forStat = arrayOfReplicatedAltLoop.get(j);
                        }
                    }
                    // The guard must be here!
                    // *************************************
                    // *************************************
                    // Try to generated code for the alt guards or bail out and terminate
                    // if the code generator fails to return a value
//                    System.err.println("---- visiting a Guard");
                    // Set the boolean guards
                    String bguard = "";
                    if ( ac.precondition()==null )
                        bguard = String.valueOf(true);
                    else if ( ac.precondition() instanceof Literal )
                        bguard = (String) ac.precondition().visit(this);
                    else {
                        // This is for expressions that evaluate to a boolean value.
                        // Such expressions become local variables (or, in our case, fields)
                        Name n = new Name("btemp");
                        LocalDecl ld = new LocalDecl(
                                new PrimitiveType(PrimitiveType.BooleanKind),
                                new Var(n, ac.precondition()),
                                false /* not constant */);
                        listOfReplicatedAltLocals.add((String) ld.visit(this));
                        bguard = (String) n.visit(this);
                    }

                    Statement stat = ac.guard().guard();
                    // TODO: What if the expression is not an array??
                    // Is it just a channel read expression?
                    if ( stat instanceof ExprStat ) {
                        Expression e = ((ExprStat) stat).expr();
                        ChannelReadExpr cr = null;
                        if ( e instanceof Assignment ) {
                            cr = (ChannelReadExpr) ((Assignment) e).right();
                            // Finally, we are in the last replicated alt, so we need to
                            // store each object guard with its correct index (or position)
                            ST stRepAltObjectGuard = stGroup.getInstanceOf("RepAltObjectGuards");
                            stRepAltObjectGuard.add("objectGuards", "repAltObjectGuards");
                            stRepAltObjectGuard.add("objectValue", (String) cr.channel().visit(this));
                            stRepAltObjectGuard.add("booleanGuards", "repAltBooleanGuards");
                            stRepAltObjectGuard.add("booleanValue", bguard);
                            // Set the number of loops, the index set, and the case number
                            // pertaining to this altCase
                            ST stRepAltIndexSet = stGroup.getInstanceOf("RepAltIndexSet");
                            stRepAltIndexSet.add("size", arrayOfReplicatedAltLoop.size());
                            stRepAltIndexSet.add("indexSet", indexSetOfAltCase);
                            stRepAltIndexSet.add("altCase", altCaseIndex);

                            forStat.add("stats", stRepAltIndexSet.render());
                            forStat.add("stats", stRepAltObjectGuard.render());
                            listOfReplicatedAltLoops.add(arrayOfReplicatedAltLoop.get(0).render());
                        }
                    }
                    // Skip statement?
                    else if ( stat instanceof SkipStat )
                        listOfReplicatedObjectGuards.add(String.format("%s.SKIP", PJAlt.class.getSimpleName()));
                    // Timeout statement?
                    else if ( stat instanceof TimeoutStat ) {
                        // Initialize the timeout statement
                        TimeoutStat ts = (TimeoutStat) stat;
                        ST stTimeout = stGroup.getInstanceOf("TimeoutStatCase");
                        stTimeout.add("name", ts.timer().visit(this));
                        stTimeout.add("delay", ts.delay().visit(this));
                        listOfReplicatedAltLocals.add(stTimeout.render());
                        listOfReplicatedObjectGuards.add((String) ts.timer().visit(this));
                    }
                    listOfReplicatedAltCases.add((String) ac.visit(this));
                    // *************************************
                    // *************************************
                    if ( !queue.isEmpty() ) {
                        reAltCase = as.body();
                        arrayOfReplicatedAltLoop.clear();
                        indexSetOfAltCase.clear();
                        altCaseIndex = queue.remove();
                        childIndex = altCaseIndex;
                        continue;
                    } else
                        break;
                }
            }

            // <--
            // This is needed because of the StackMapTable for the generated Java bytecode
            // I don't think this is needed anymore, but we will leave it here for legacy code
            Name n = new Name("index" + (++indexForAltStat));
            new LocalDecl(new PrimitiveType(PrimitiveType.IntKind),
                    new Var(n, null),
                    false /* not constant */).visit(this);
            // Create a tag for this local alt declaration
            String newName = Helper.makeVariableName("alt", Tag.LOCAL_NAME);
            localToFields.put(newName, "PJAlt");
            paramToVarNames.put(newName, newName);
            // -->

            ST stRepAltGuards = stGroup.getInstanceOf("RepAltGuards");
            stRepAltGuards.add("locals", listOfReplicatedAltLocals);

            ST stReplicatedAltStat = stGroup.getInstanceOf("ReplicatedAltStat");
            stReplicatedAltStat.add("alt", newName);
            stReplicatedAltStat.add("initGuards", stRepAltGuards.render());
            stReplicatedAltStat.add("objectGuards", listOfReplicatedObjectGuards);
            stReplicatedAltStat.add("bguards", "repAltBooleanGuards");
            stReplicatedAltStat.add("guards", "repAltObjectGuards");
            stReplicatedAltStat.add("initForStmt", listOfReplicatedAltLoops);
            stReplicatedAltStat.add("jump", ++jumpLabel);
            stReplicatedAltStat.add("cases", listOfReplicatedAltCases);
            stReplicatedAltStat.add("index", n.visit(this));

            // Add the jump label to the switch-stmt list
            switchCases.add(renderSwitchCase(jumpLabel));
            return stReplicatedAltStat.render();
        }
        // -->
        // *******************************************************************
        // *******************************************************************

        int currBooleanGuard = booleanGuardID;
        booleanGuardID++;
        int currObjectGuard = objectGuardID;
        objectGuardID++;

        ST stAltStat = stGroup.getInstanceOf("AltStat");
        ST stTimerLocals = stGroup.getInstanceOf("TimerLocals");
        ST stBooleanGuards = stGroup.getInstanceOf("BooleanGuards");
        ST stObjectGuards = stGroup.getInstanceOf("ObjectGuards");

        Sequence<AltCase> cases = as.body();
        ArrayList<String> blocals = new ArrayList<>(); // Variables for pre-guard expressions
        ArrayList<String> bguards = new ArrayList<>(); // Default boolean guards
        ArrayList<String> guards = new ArrayList<>();  // Guard statements
        ArrayList<String> altCases = new ArrayList<>();// Generated code for each alt-cases
        ArrayList<String> tlocals = new ArrayList<>(); // Timeouts

        // Set boolean guards
        for (int i = 0; i<cases.size(); ++i) {
            AltCase ac = cases.child(i);
            if ( ac.precondition()==null )
                bguards.add(String.valueOf(true));
            else if ( ac.precondition() instanceof Literal )
                bguards.add((String) ac.precondition().visit(this));
            else {
                // This is for expressions that evaluate to a boolean value.
                // Such expressions become local variables (or, in our case, fields)
                Name n = new Name("btemp");
                LocalDecl ld = new LocalDecl(
                        new PrimitiveType(PrimitiveType.BooleanKind),
                        new Var(n, ac.precondition()),
                        false /* not constant */);
                blocals.add((String) ld.visit(this));
                bguards.add((String) n.visit(this));
            }
        }
        stBooleanGuards.add("constants", bguards);
        stBooleanGuards.add("locals", blocals);

        //
        stBooleanGuards.add("readyID", booleanGuardID);
        //

        // Set case number for all AltCases
        for (int i = 0; i<cases.size(); ++i)
            cases.child(i).setCaseNumber(i);
        // Visit all guards
        for (int i = 0; i<cases.size(); ++i) {
            AltCase ac = cases.child(i);
            Statement stat = ac.guard().guard();
            // Channel read expression?
            if ( stat instanceof ExprStat ) {
                Expression e = ((ExprStat) stat).expr();
                ChannelReadExpr cr = null;
                if ( e instanceof Assignment )
                    cr = (ChannelReadExpr) ((Assignment) e).right();
                guards.add((String) cr.channel().visit(this));
            }
            // Skip statement?
            else if ( stat instanceof SkipStat )
                guards.add(String.format("%s.SKIP", PJAlt.class.getSimpleName()));
            // Timeout statement?
            else if ( stat instanceof TimeoutStat ) {
                // Initialize the timeout statement
                TimeoutStat ts = (TimeoutStat) stat;
                ST stTimeout = stGroup.getInstanceOf("TimeoutStatCase");
                stTimeout.add("name", ts.timer().visit(this));
                stTimeout.add("delay", ts.delay().visit(this));
                tlocals.add(stTimeout.render());
                guards.add((String) ts.timer().visit(this));
            }
            altCases.add((String) ac.visit(this));
        }
        stTimerLocals.add("timers", tlocals);
        stObjectGuards.add("guards", guards);

        //
        stObjectGuards.add("readyID", objectGuardID);
        //

        // <--
        // This is needed because of the StackMapTable for the generated Java bytecode
        Name n = new Name("index");
        new LocalDecl(new PrimitiveType(PrimitiveType.IntKind),
                new Var(n, null),
                false /* not constant */).visit(this);
        // Create a tag for this local alt declaration
        String newName = Helper.makeVariableName("alt", Tag.LOCAL_NAME);
        localToFields.put(newName, "PJAlt");
        paramToVarNames.put(newName, newName);
        // -->

        stAltStat.add("alt", newName);
        stAltStat.add("count", cases.size());
        stAltStat.add("timerLocals", stTimerLocals.render());
        stAltStat.add("initBooleanGuards", stBooleanGuards.render());
        stAltStat.add("initGuards", stObjectGuards.render());
        stAltStat.add("bguards", "booleanGuards" + booleanGuardID);
        stAltStat.add("guards", "objectGuards" + objectGuardID);
        stAltStat.add("jump", ++jumpLabel);
        stAltStat.add("cases", altCases);
        stAltStat.add("index", n.visit(this));

        //
        stAltStat.add("readyID", readyID);
        //

        // Add the jump label to the switch-stmt list
        switchCases.add(renderSwitchCase(jumpLabel));
        readyID++;
        booleanGuardID = currBooleanGuard;
        objectGuardID = currObjectGuard;

        return stAltStat.render();
    }

    private Object createAltForStat(AltStat as) {
        Log.log(as.line + ": Creating a ForStat for a replicated Alt");

        ST stForStat = stGroup.getInstanceOf("ForStat");
        ArrayList<String> init = null; // Initialization part
        ArrayList<String> incr = null; // Step counter
        String expr = null; // Conditional expression
        if ( as.init()!=null ) {
            init = new ArrayList<>();
            for (Statement st : as.init())
                init.add(((String) st.visit(this)).replace(DELIMITER, ""));
            // Collect all local variables that pertain to the replicated alt
            for (String str : init)
                indexSetOfAltCase.add(str.substring(0, str.indexOf("=")).trim());
        }
        if ( as.expr()!=null )
            expr = (String) as.expr().visit(this);
        if ( as.incr()!=null ) {
            incr = new ArrayList<>();
            for (Statement st : as.incr())
                incr.add(((String) st.visit(this)).replace(DELIMITER, ""));
        }
        stForStat.add("init", init);
        stForStat.add("expr", expr);
        stForStat.add("incr", incr);
        return stForStat;
    }

    @Override
    public Object visitReturnStat(ReturnStat rs) {
        Log.log(rs, "Visiting a ReturnStat");

        ST stReturnStat = stGroup.getInstanceOf("ReturnStat");
        String expr = null;

        if(rs.expr() != null) {

            expr = (String) rs.expr().visit(this);

            // This removes the extra ";" for invocation calls
            expr = expr.replace(DELIMITER, "");
            stReturnStat.add("expr", expr);

        }

        return stReturnStat.render();
    }

    // *************************************************************************
    // ** HELPER METHODS

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

        if (t instanceof RecordTypeDecl) {
            baseType = ((RecordTypeDecl) t).name().getname();
        } else if ( t.isProtocolType() ) {
            baseType = PJProtocolCase.class.getSimpleName();
        } else if ( t.isPrimitiveType() ) {
            baseType = Helper.getWrapperType(t);
        } else if ( t.isArrayType() ) {
            baseType = (String) t.visit(this);
        } else if(t instanceof NamedType) {
            baseType = ((NamedType) t).name().toString();
        }

        return baseType;
    }

    // This is used for newly-created processes
    private void resetGlobals() {
        parDecID = 0;
        varDecID = 0;
        jumpLabel = 0;

        Helper.ResetGlobals();

        localToFields.clear();
        switchCases.clear();
        barriers.clear();

        paramToFields.clear();
        paramToVarNames.clear();

        localsForAnonymousProcess.clear();
        paramsForAnonymousProcess.clear();
    }

    // Returns a string representation of a jump label
    private String renderSwitchCase(int jump) {
        ST stSwitchCase = stGroup.getInstanceOf("SwitchCase");

        stSwitchCase.add("jump", jump);

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
                new Sequence(),               // Modifiers
                null,                         // Return type
                new Name("Anonymous"),        // Procedure name
                new Sequence(),               // Formal parameters
                new Sequence(),               // Implement
                null,                         // Annotations
                new Block(new Sequence(st))); // Body
    }

    private Object createChannelArrayInitializer(String lhs, String[] dims, String type) {
        Log.log("Creating a New Channel Array");

//        final int size = ((ArrayType) na.type).getDepth();
        ArrayList<ST> forStmts = new ArrayList<>();
        for (int i = 0; i < dims.length; ++i) {
            Tuple<?> tuple = createLocalDeclForLoop(dims[i]);
            ForStat as = new ForStat(new Token(0, "0", 0, 0, 0), (Sequence<Statement>) tuple.get(0),
                    (BinaryExpr) tuple.get(1), (Sequence<ExprStat>) tuple.get(2), null, null, false);
            //
            ST stForStat = stGroup.getInstanceOf("ForStat");
            ArrayList<String> init = null; // Initialization part
            ArrayList<String> incr = null; // Step counter
            String expr = null; // Conditional expression
            if ( as.init()!=null ) {
                init = new ArrayList<>();
                for (Statement st : as.init())
                    init.add(((String) st.visit(this)).replace(DELIMITER, ""));
                // Collect all local variables that pertain to the replicated alt
                for (String str : init)
                    indexSetOfAltCase.add(str.substring(0, str.indexOf("=")).trim());
            }
            if ( as.expr()!=null )
                expr = (String) as.expr().visit(this);
            if ( as.incr()!=null ) {
                incr = new ArrayList<>();
                for (Statement st : as.incr())
                    incr.add(((String) st.visit(this)).replace(DELIMITER, ""));
            }
            stForStat.add("init", init);
            stForStat.add("expr", expr);
            stForStat.add("incr", incr);
            forStmts.add(stForStat);
        }
//        ForStat forStmt = forStmts.get(0);
//        for (int i = 1; i < forStmts.size(); ++i) {
//            forStmt.children[4] = forStmts.get(i);
//            forStmt = forStmts.get(i);
//        }
//        String str = (String) forStmts.get(0).visit(this);
        ST stChannelDecl = stGroup.getInstanceOf("ChannelDecl");
        stChannelDecl.add("type", type);
        ST stForStmt = forStmts.get(forStmts.size() - 1);
        stForStmt.add("stats", lhs + " = " + stChannelDecl.render());
        stForStmt = forStmts.get(0);
        for (int i = 1; i < forStmts.size(); ++i) {
            stForStmt.add("stats", forStmts.get(i));
            stForStmt = forStmts.get(i);
        }

        return null;
    }


    int index = 0;

    private Tuple<?> createLocalDeclForLoop(String dims) {
        final String localDeclName = Helper.makeVariableName("tmp__", Tag.LOCAL_NAME);
        Name n = new Name(localDeclName);
        NameExpr ne = new NameExpr(n);
        PrimitiveLiteral pl = new PrimitiveLiteral(new Token(0, "0", 0, 0, 0), 4 /* kind */);
        LocalDecl ld = new LocalDecl(
                new PrimitiveType(PrimitiveType.IntKind),
                new Var(n, null),
                false /* not constant */);
        ld.visit(this);
        BinaryExpr be = new BinaryExpr(ne, new NameExpr(new Name(dims)), BinaryExpr.LT);
        ExprStat es = new ExprStat(new UnaryPreExpr(ne, UnaryPreExpr.PLUSPLUS));
        Sequence<Statement> init = new Sequence<>();
        init.append(new ExprStat((Expression) new Assignment(ne, pl, Assignment.EQ)));
        Sequence<ExprStat> incr = new Sequence<>();
        incr.append(es);
        return new Tuple(init, be, incr);
    }

    private String ranchSauce(final String name, final String type, final String[] dims, final String access, int depth, int index, char init, final String linit) {

        if(depth == 0) {

            String result = (name != null) ? name + " = " : "";

            result += "new " + type;

            if(type.equals("Integer"))
                result += "(0);";
            else result += "();";

            return result ;

        }

        final ST        newArray    = stGroup.getInstanceOf("NewArray2");
        final String    newName     = (access != null) ? name + "[" + access + "]" : name;

        final StringBuilder builder = new StringBuilder(type);

        for(int tindex = index; tindex < dims.length; tindex++)
            builder.append("[" + dims[tindex] + "]");
        
        newArray.add("name", newName);
        newArray.add("type", builder.toString());
        newArray.add("expression", dims[index]);
        newArray.add("iterator", String.valueOf(init));
        newArray.add("statement", ranchSauce(newName, type, dims, String.valueOf(init), --depth, ++index, ++init, null));

        return newArray.render();

    }

    @SuppressWarnings("unchecked")
    private Object createNewArray(String lhs, NewArray na) {
        Log.log(na.line + ": Creating a New Array");

        final ST          stNewArray  = stGroup.getInstanceOf("NewArray2");

        final String[]    dims        = (String[])  na.dimsExpr().visit(this);
        final String      type        = (String)    na.baseType().getJavaWrapper();

        ST stNewArrayLiteral = stGroup.getInstanceOf("NewArrayLiteral");

        if(na.init() != null) {

            final ArrayList<String>       inits   = new ArrayList<>()                             ;
            final Sequence<Expression>    seq     = (Sequence<Expression>) na.init().elements()   ;
            
            for (Expression e : seq) {
                if ( e instanceof ArrayLiteral )
                    isArrayLiteral = true;
                inits.add((String) e.visit(this));
            }
            
            stNewArrayLiteral.add("dim", String.join("", Collections.nCopies(((ArrayType) na.type).getDepth(), "[]")));
            stNewArrayLiteral.add("vals", inits);

        } else stNewArrayLiteral.add("dims", dims);

        if(lhs != null)
            stNewArray.add("name", lhs);

        stNewArray.add("type", type);

        // Reset value for array literal expression
        isArrayLiteral = false;

        return ranchSauce(lhs, type, dims, null, dims.length, 0, 'a', stNewArrayLiteral.render());
    }

    private String createArrayList(String type, int dims) {
        StringBuilder builder = new StringBuilder();
        final String str = "ArrayList<%s>";
        for (int i = 0; i < dims - 1; ++i ) {
            builder.append(String.format(str, str));
        }
        return String.format(builder.toString(), type);
    }

    private Object createChannelReadExpr(String lhs, String type, String op, ChannelReadExpr cr) {
        Log.log(cr, "Creating Channel Read Expression");

        ST stChannelReadExpr = stGroup.getInstanceOf("ChannelReadExpr");
        // 'c.read()' is a channel-end expression, where 'c' is the
        // reading end of a channel
        Expression chanExpr = cr.channel();
        // 'c' is the name of the channel
        String chanEndName = (String) chanExpr.visit(this);

        // Is it a timer read expression?
        if ( chanExpr.type.isTimerType() ) {
            ST stTimerRedExpr = stGroup.getInstanceOf("TimerRedExpr");
            stTimerRedExpr.add("name", lhs);
            return stTimerRedExpr.render();
        }

        // One for the 'label' and one for the 'read' operation
        int countLabel = 2;
        // Is the reading end of this channel shared?
        if ( chanExpr.type.isChannelEndType() && ((ChannelEndType) chanExpr.type).isShared() ) {
            stChannelReadExpr = stGroup.getInstanceOf("ChannelOne2Many");
            ++countLabel;
        }

        // Do we have an extended rendezvous?
        if ( cr.extRV()!=null ) {
            Object o = cr.extRV().visit(this);
            ST stBlockRV = stGroup.getInstanceOf("BlockRV");
            stBlockRV.add("block", o);
            stChannelReadExpr.add("extendRv", stBlockRV.render());
        }

        stChannelReadExpr.add("chanName", chanEndName);
        // Add the switch block for resumption
        for (int label = 0; label<countLabel; ++label) {
            // Increment jump label and add it to the switch-stmt list
            stChannelReadExpr.add("resume" + label, ++jumpLabel);
            switchCases.add(renderSwitchCase(jumpLabel));
        }

        stChannelReadExpr.add("lhs", lhs);
        stChannelReadExpr.add("type", type);
        stChannelReadExpr.add("op", op);

        return stChannelReadExpr.render();
    }

    public AltCase createDynamicAltStat(AltCase ac) {
        Name n = new Name("tmp_i");
        NameExpr ne = new NameExpr(n);
        PrimitiveLiteral pl = new PrimitiveLiteral(new Token(0, "0", 0, 0, 0), 4 /* kind */);
        LocalDecl ld = new LocalDecl(
                new PrimitiveType(PrimitiveType.IntKind),
                new Var(n, null),
                false /* not constant */);
        ld.visit(this);
        PrimitiveLiteral pl2 = new PrimitiveLiteral(new Token(0, "1", 0, 0, 0), 4 /* kind */);
        BinaryExpr be = new BinaryExpr(ne, pl2, BinaryExpr.LT);
        ExprStat es = new ExprStat(new UnaryPreExpr(ne, UnaryPreExpr.PLUSPLUS));
        Sequence<Statement> init = new Sequence<>();
        init.append(new ExprStat((Expression) new Assignment(ne, pl, Assignment.EQ)));
        Sequence<ExprStat> incr = new Sequence<>();
        incr.append(es);
        Sequence<AltCase> body = new Sequence<>();
        body.append(ac);
        AltStat as = new AltStat(init, be, incr, body, false);
        as.dynamic = true;
        ac = new AltCase(null, null, new Block(new Sequence<Statement>(as)));
        ac.isAltStat = true;
        return ac;
    }

    // Returns a string representing the signature of the wrapper
    // class or Java method that encapsulates a PJProcess
    public String hashSignature(ProcTypeDecl pd) {
        String signature = pd.signature();
        return String.valueOf(signature.hashCode()).replace('-', '$');
    }
}
