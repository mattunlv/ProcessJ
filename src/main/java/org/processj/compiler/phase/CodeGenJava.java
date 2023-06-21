package org.processj.compiler.phase;

import java.util.*;
import java.util.stream.Collectors;

import org.processj.compiler.ast.expression.access.ArrayAccessExpression;
import org.processj.compiler.ast.expression.access.RecordAccessExpression;
import org.processj.compiler.ast.expression.binary.AssignmentExpression;
import org.processj.compiler.ast.expression.binary.BinaryExpression;
import org.processj.compiler.ast.expression.constructing.NewArrayExpression;
import org.processj.compiler.ast.expression.literal.*;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.expression.result.*;
import org.processj.compiler.ast.expression.unary.*;
import org.processj.compiler.ast.expression.yielding.ChannelEndExpression;
import org.processj.compiler.ast.expression.yielding.ChannelReadExpression;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.declarative.LocalDeclaration;
import org.processj.compiler.ast.statement.declarative.ProtocolCase;
import org.processj.compiler.ast.statement.declarative.RecordMemberDeclaration;
import org.processj.compiler.ast.statement.conditional.SwitchGroupStatement;
import org.processj.compiler.ast.expression.result.SwitchLabel;
import org.processj.compiler.ast.statement.conditional.SwitchStatement;
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.ast.statement.yielding.ParBlock;
import org.processj.compiler.ast.type.*;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.yielding.AltCase;
import org.processj.compiler.ast.statement.yielding.AltStatement;
import org.processj.compiler.ast.expression.*;
import org.processj.runtime.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

/**
 * A tree walker which collects data from an AST object and then pushes this
 * data into a template to translate a ProcessJ source file to Java code.
 * 
 * @author ben
 * @version 06/10/2018
 * @since 1.2
 */
// TODO: Generate Initialization code for Arrays of Channels (ends)
public class CodeGenJava extends Phase {

    /** String template file locator */
    private final String STGRAMMAR_FILE = "src/main/resources/stringtemplates/java/grammarTemplatesJava.stg";

    /** Current Java version -- only needed for debugging */
    private final String JVM_RUNTIME = System.getProperty("java.version");
    /**
     * Collection of templates, imported templates, and/or groups that contain
     * formal template definitions
     */
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

    /**
     * This is used to store variable the tables below -- 'localsForAnon' and
     * 'paramsForAnon'
     */
    private boolean inParFor = false;

    /**
     * These two fields are responsible for holding variables that are passed to
     * anonymous processes in the generated code for a par-for
     */
    private HashMap<String, String> localsForAnonymousProcess = new LinkedHashMap<>();
    private HashMap<String, String> paramsForAnonymousProcess = new LinkedHashMap<>();

    /** Currently executing protocol */
    private String currentProtocol = null;

    /** All imports are kept in this table */
    private HashSet<String> importFiles = new LinkedHashSet<>();

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

    /** Identifier for local variable declaration */
    private int localDecID = 0;

    /** Jump label used when procedures org.processj.yield */
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
     * definitions. Additionally, the constructor initializes a symbol table with
     * top-level declarations.
     *
     */
    public CodeGenJava() {
        super(null);
        stGroup = new STGroupFile(STGRAMMAR_FILE);
    }

    /**
     * Changes the name of a procedure, method, protocol, record, channel
     * or local variable so that the JVM can separate common names which
     * belong to the same compiled class. The ProcessJ naming convention
     * is as follows:
     * 1.) For a procedure, the procedure is encoded as '_proc$name' where
     *     name is the procedure's unique identifier.
     * 2.) For a Java method, the method is encoded as '_method$name' where
     *     name is the method's unique identifier.
     * 3.) For parameters, the parameter is encoded as '_pd$nameX' where
     *     name is the name of the argument and 'X' represents the position of
     *     the parameter in the argument list; e.g.,
     *              foo(_pd$name0, _pd$name1, ..., _pd@nameN)
     * 4.) For locals, the local is encoded as '_ld$nameX' where name is
     *     the name of the local variable and 'X' is the local's unique
     *     identifier.
     * 5.) For protocols, the protocol is encoded as '_prot$name' where
     *     name is a protocol tag and 'X' is the protocol's unique identifier.
     *
     * @param name
     *            The name or tag of a procedure, method, protocol, record,
     *            parameter, or local variable.
     * @param X
     *            A unique identifier or position in a procedure's argument
     *            list.
     * @param type
     *            A tag to encode in a procedure, method, parameter, local
     *            variable, protocol, record, or channel.
     * @return A symbolic encoded name that represents an identifier/variable.
     */
    public static String makeVariableName(final String name, int X, Tag type) {
        String varName = "";

        switch (type) {
        case MAIN_NAME:
            break; // Do nothing for now
        case PROCEDURE_NAME:
            varName = Tag.PROCEDURE_NAME + name;
            break;
        case METHOD_NAME:
            varName = Tag.METHOD_NAME + name;
            break;
        case PARAM_NAME:
            varName = Tag.PARAM_NAME + name + X;
            break;
        case LOCAL_NAME:
            varName = Tag.LOCAL_NAME + name + X;
            break;
        case PROTOCOL_NAME:
            varName = Tag.PROTOCOL_NAME + name;
            break;
        default:
            break;
        }

        return varName;
    }

    /**
     * Returns the wrapper class for the given class type.
     *
     * @param type
     *          A wrapper class type or the class itself.
     * @return The type instances represented by a class.
     */
    public static Class<?> getWrapperClass(Type type) {
        assert type != null;
        Class<?> typeName = null;

        if (type.isIntegerType())
            typeName = Integer.class;
        else if (type.isByteType())
            typeName = Byte.class;
        else if (type.isLongType())
            typeName = Long.class;
        else if (type.isDoubleType())
            typeName = Double.class;
        else if (type.isFloatType())
            typeName = Float.class;
        else if (type.isBooleanType())
            typeName = Boolean.class;
        else if (type.isCharType())
            typeName = Character.class;
        else if (type.isShortType())
            typeName = Short.class;
        else if (type instanceof RecordTypeDeclaration)
            typeName = PJRecord.class;
        else if (type instanceof ChannelType || type instanceof ChannelEndType)
            typeName = PJChannel.class;
        else if (type.isTimerType())
            typeName = PJTimer.class;
        else if (type.isBarrierType())
            typeName = PJBarrier.class;

        return typeName;
    }

    /**
     * Returns a string representing a primitive wrapper class or
     * the class itself.
     *
     * @param type
     *          A primitive class type or the class itself.
     * @return A String representation of class type.
     */
    public static String getWrapperType(Type type) {
        return getWrapperClass(type).getSimpleName();
    }

    /**
     * Visit a single compilation unit which starts with an optional package
     * declaration, followed by zero or more import declarations, followed by zero
     * or more type declarations.
     *
     * @param compilation An AST that represents the entire compilation unit.
     * @return A text generated after evaluating this compilation unit.
     */
    @Override
    public final void visitCompilation(Compilation compilation) {
        org.processj.compiler.Compiler.Info(compilation + "Visiting a Compilation");

        currentCompilation = compilation;
        // Code generated by the ST template
        String codeGen = null;
        // Template to fill in
        ST stCompilation = stGroup.getInstanceOf("Compilation");
        // Reference to all remaining types
        ArrayList<String> body = new ArrayList<>();
        // Holds all top-level declarations
        Sequence<Type> typeDecls = compilation.getTypeDeclarations();
        // Package name for this source file
        String packagename = compilation.getPackageName();

        for (Import im : compilation.getImports()) {
            //if (im != null)
                    //importFiles.add((String) im.visit(this));
        }

        for (AST decl : typeDecls) {
            if (decl instanceof Type) {
                // Collect procedures, records, protocols, external types, etc.
                String t = ""; //(String) ((Type) decl).visit(this);

                if (t != null)
                    body.add(t);
            } else if (decl instanceof ConstantDeclaration) {
                // Iterate over remaining declarations, which is anything that
                // comes after top-level declarations
                //((ConstantDeclaration) decl).visit(this);

                //if (cd != null)
                    //body.add(cd);
            }
        }

        stCompilation.add("pathName", packagename);
        stCompilation.add("fileName", compilation.fileName);
        stCompilation.add("name", sourceFile);
        stCompilation.add("body", body);
        stCompilation.add("version", JVM_RUNTIME);

        // Add all import statements to the file (if any)
        if (importFiles.size() > 0)
            stCompilation.add("imports", importFiles);

        // This will render the code for debugging
        codeGen = stCompilation.render();

    }

    String newChanArrayName = null;

    final StringBuilder sourceBuilder = new StringBuilder();

    int indent = 0;

    private JavaSourceContext sourceContext = new ArrayLiteralSourceContext();
    private Deque<JavaSourceContext> sourceContexts = new ArrayDeque<>();

    private static String ClassMemberDeclarationOf(final int indent, final String modifiers, final String type, final String name) {

        return " ".repeat(indent) + modifiers + " " + type + " " + name + ";";

    }

    private static String ClassFieldInitializationExpression(final int indent, final String name, final String expression) {

        return " ".repeat(indent) + "this." + name + " = " + expression + ";\n";

    }

    private static String ParameterDeclarationOf(final String modifiers, final String type, final String name) {

        return modifiers + " " + type + " " + name;

    }

    @Override
    public final void visitImport(final Import importName) {

        sourceBuilder.append("import " + importName.getPackageName() + ";\n");

    }

    @Override
    public final void visitConstantDeclaration(final ConstantDeclaration constantDeclaration) throws Phase.Error {

        sourceBuilder
                .append(" ".repeat(indent) + "public final static")
                .append(" " + constantDeclaration.getType())
                .append(" " + constantDeclaration);

        // Assert the Constant Declaration is initialized
        if(constantDeclaration.isInitialized())
            constantDeclaration.getInitializationExpression().accept(this);

        sourceBuilder.append(";\n");

    }

    @Override
    public final void visitProcedureTypeDeclaration(final ProcedureTypeDeclaration procedureTypeDeclaration) throws Phase.Error  {

        org.processj.compiler.Compiler.Info(procedureTypeDeclaration + "Visiting a ProcTypeDecl (" + procedureTypeDeclaration + ")");

        ST stProcTypeDecl = null;

        // Save previous procedure state & jump labels
        ArrayList<String>   prevLabels      = switchCases       ;
        String              prevProcName    = currentProcName   ;

        if(!switchCases.isEmpty()) switchCases = new ArrayList<>();

        // Name of the invoked procedure
        currentProcName = paramToVarNames.getOrDefault(procedureTypeDeclaration.toString(), procedureTypeDeclaration.toString());

        // Procedures are static classes which belong to the same package and
        // class. To avoid having classes with the same name, we generate a
        // new name for the currently executing procedure
        String procName = null;

        // For non-invocations, that is, for anything other than a procedure
        // that yields, we need to extend the PJProcess class anonymously
        if(currentProcName.equals("Anonymous")) {

            // Preserve current jump label for resumption
            int prevJumpLabel = jumpLabel;
            jumpLabel = 0;

            // Create an instance for such anonymous procedure
            stProcTypeDecl = stGroup.getInstanceOf("AnonymousProcess2");

            // Statements that appear in the procedure being executed
            //String[] body = (String[]) procedureTypeDeclaration.getBody().visit(this);

            stProcTypeDecl.add("parBlock", currentParBlock);
            stProcTypeDecl.add("syncBody", procedureTypeDeclaration.getBody());
            stProcTypeDecl.add("isPar", inParFor);

            // Add the barrier this procedure should resign from
            if(!barriers.isEmpty())
                stProcTypeDecl.add("barrier", barriers);

            // Add the switch block for yield and resumption
            if(!switchCases.isEmpty()) {
                ST stSwitchBlock = stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", switchCases);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }

            // The list of local variables defined in the body of a procedure
            // becomes the instance fields of the class
            if(!localsForAnonymousProcess.isEmpty()) {
                stProcTypeDecl.add("ltypes", localsForAnonymousProcess.values());
                stProcTypeDecl.add("lvars", localsForAnonymousProcess.keySet());
            }

            // Restore jump label so it knows where to resume from
            jumpLabel = prevJumpLabel;

        } else {

            // Restore global variables for a new PJProcess class
            resetGlobals();

            // Grab the return type of the invoked procedure
            //String procType = (String) procedureTypeDeclaration.getReturnType().visit(this);
            // Retrieve the modifier(s) attached to the invoked procedure such
            // as private, public, protected, etc.
            //String[] modifiers = (String[]) procedureTypeDeclaration.modifiers().visit(this);

            // Formal parameters that must be passed to the procedure
            Sequence<ParameterDeclaration> formals = procedureTypeDeclaration.getParameters();

            // Do we have any parameters?
            if((formals != null) && formals.size() > 0) {
                // Iterate through and visit every parameter declaration.
                // Retrieve the name and type of each parameter specified in
                // a list of comma-separated arguments. Note that we ignored
                // the value returned by this visitor
                for(int i = 0; i < formals.size(); ++i)
                    formals.child(i).accept(this);

            }

            // Visit all declarations that appear in the procedure
            String[] body = null;

            //if(procedureTypeDeclaration.getBody() != null)
                //body = (String[]) procedureTypeDeclaration.getBody().visit(this);

            // The procedure's annotation determines if we have a yielding procedure
            // or a Java method (a non-yielding procedure)
            boolean doesProcYield = procedureTypeDeclaration.doesYield();

            // Set the template to the correct instance value and then initialize
            // its attributes
            if(doesProcYield) {

                // This procedure yields! Grab the instance of a yielding procedure
                // from the string template in order to define a new class
                procName = makeVariableName(currentProcName + hashSignature(procedureTypeDeclaration), 0, Tag.PROCEDURE_NAME);
                stProcTypeDecl = stGroup.getInstanceOf("ProcClass");
                stProcTypeDecl.add("name", procName);
                stProcTypeDecl.add("syncBody", body);

            } else {

                // Otherwise, grab the instance of a non-yielding procedure to define a new static Java method
                procName = makeVariableName(currentProcName + hashSignature(procedureTypeDeclaration), 0, Tag.METHOD_NAME);
                stProcTypeDecl = stGroup.getInstanceOf("Method");
                stProcTypeDecl.add("name", procName);
                //stProcTypeDecl.add("type", procType);

                // Do we have any access modifier? If so, add them
                //if(modifiers != null && modifiers.length > 0)
                    //stProcTypeDecl.add("modifier", modifiers);
                stProcTypeDecl.add("body", body);

            }

            // Create an entry point for the ProcessJ program, which is just
            // a Java main method that is called by the JVM
            if(currentProcName.equals("main") && procedureTypeDeclaration.getSignature().equals(Tag.MAIN_NAME.toString())) {
                // Create an instance of a Java main method template
                ST stMain = stGroup.getInstanceOf("Main");
                stMain.add("class", currentCompilation.fileNoExtension());
                stMain.add("name", procName);

                // Pass the list of command line arguments to this main method
                if(!paramToFields.isEmpty()) {
                    stMain.add("types", paramToFields.values());
                    stMain.add("vars", paramToFields.keySet());
                }

                // Add the entry point of the program
                stProcTypeDecl.add("main", stMain.render());

            }

            // The list of command-line arguments should be passed to the constructor
            // of the static class that the main method belongs or be passed to the
            // static method
            if(!paramToFields.isEmpty()) {
                stProcTypeDecl.add("types", paramToFields.values());
                stProcTypeDecl.add("vars", paramToFields.keySet());
            }

            // The list of local variables defined in the body of a procedure
            // becomes the instance fields of the class
            if(!localToFields.isEmpty()) {
                stProcTypeDecl.add("ltypes", localToFields.values());
                stProcTypeDecl.add("lvars", localToFields.keySet());
            }

            // Add the switch block for resumption (if any)
            if(!switchCases.isEmpty()) {
                ST stSwitchBlock = stGroup.getInstanceOf("SwitchBlock");
                stSwitchBlock.add("jumps", switchCases);
                stProcTypeDecl.add("switchBlock", stSwitchBlock.render());
            }

        }

        // Restore and reset previous values
        currentProcName = prevProcName;
        // Restore previous jump labels
        switchCases = prevLabels;

    }

    @Override
    public final void visitProtocolTypeDeclaration(final ProtocolTypeDeclaration protocolTypeDeclaration) {

        ST stProtocolClass = stGroup.getInstanceOf("ProtocolClass");
        // TODO: Used to be paramForAnon
        String name = paramToVarNames.getOrDefault(protocolTypeDeclaration.toString(), protocolTypeDeclaration.toString());

        ArrayList<String> modifiers = new ArrayList<>();
        ArrayList<String> body      = new ArrayList<>();

        //for (Modifier m : protocolTypeDeclaration.modifiers())

                //modifiers.add((String) m.visit(this));

        currentProtocol = name;
        // We use tags to associate parent and child protocols
        if (protocolTypeDeclaration.extend().size() > 0) {
            for (Name n : protocolTypeDeclaration.extend()) {
                ProtocolTypeDeclaration ptd = (ProtocolTypeDeclaration) getScope().get(n.toString());
                for (ProtocolCase pc : ptd.getBody())
                    protocolNameToProtocolTag.put(String.format("%s.%s", protocolTypeDeclaration, pc),
                            ptd.toString());
            }
        }

        // The scope in which all protocol members appear
        //if (protocolTypeDeclaration.getBody() != null)
            //for (ProtocolCase pc : protocolTypeDeclaration.getBody())
                //body.add((String) pc.visit(this));

        stProtocolClass.add("name", name);
        stProtocolClass.add("modifiers", modifiers);
        stProtocolClass.add("body", body);

    }

    @Override
    public final void visitRecordTypeDeclaration(final RecordTypeDeclaration recordTypeDeclaration) {

        // TODO: Used to be paramsForAnon
        String recName = paramToVarNames.getOrDefault(recordTypeDeclaration.toString(),
                recordTypeDeclaration.toString());

        // Initialize a handle to the interface name and modifier & extends StringBuilders
        final String        interfaceName           = "interface I_" + recordTypeDeclaration    ;
        final StringBuilder modifierStringBuilder   = new StringBuilder();
        final StringBuilder extendsStringBuilder    = new StringBuilder();

        // Synthesize the modifiers & the extends
        // TODO: Make sure we don't specify 'protected' by default or always
        recordTypeDeclaration.modifiers().forEach(
                modifier -> modifierStringBuilder.append(modifier).append(" "));
        recordTypeDeclaration.getExtends().forEach(
                extend -> extendsStringBuilder.append(extend).append(", "));

        // Initialize a handle to the interface name & modifier string
        final String modifierSet    = modifierStringBuilder.toString();
        final String implementSet   = extendsStringBuilder.toString();

        this.sourceBuilder
                .append(modifierSet)
                .append(interfaceName)
                .append(" { /* Empty */ }\n\n");

        this.sourceBuilder
                .append(modifierSet)
                .append("static class ")
                .append(recordTypeDeclaration)
                .append(" extends PJRecord implements ")
                .append(implementSet)
                // TODO: Check that if the RecordTypeDecl extends anything, this isn't used.
                .append(interfaceName)
                .append(" {\n\n");

        this.indent += 4;

        // Initialize the parameter & member initializer String Builders
        final StringBuilder parameterSet    = new StringBuilder();
        final StringBuilder initializerSet  = new StringBuilder();

        // Iterate through the RecordMembers
        for(final RecordMemberDeclaration recordMemberDeclaration : recordTypeDeclaration.getBody()) {

            // Initialize a handle to the type name
            final String typeName = (recordMemberDeclaration.getType() instanceof ProtocolTypeDeclaration)
                    ? PJProtocolCase.class.getSimpleName() : recordMemberDeclaration.getType().toString();
            final String name = recordMemberDeclaration.getName().toString();

            this.sourceBuilder
                    .append(ClassMemberDeclarationOf(indent, "public", typeName, name))
                    .append("\n");

            initializerSet
                    .append(ClassFieldInitializationExpression(indent + 4, name, name))
                    .append("\n");

            parameterSet
                    .append(ParameterDeclarationOf("final", typeName, name))
                    .append(", ");

        }

        // TODO: Append Constructor & toString declarations

    }

    @Override
    public final void visitLocalDeclaration(LocalDeclaration localDeclaration)  {
        org.processj.compiler.Compiler.Info(localDeclaration + "Visting a LocalDecl (" + localDeclaration.getType() + " " + localDeclaration + ")");

        // We could have the following targets:
        // 1.) T x; // A declaration
        // 2.) T x = 4; // A simple declaration
        // 3.) T x = in.read(); // A single channel read
        // 4.) T x = a.read() + b.read() + ... + z.read(); // Multiple channel reads
        // 5.) T x = read(); // A Java method that returns a value
        // 6.) T x = a + b; // A binary expression
        // 7.) T x = a = b ...; // A complex assignment statement
        String name = localDeclaration.toString();
        String type = null;

            //type = (String) localDeclaration.getType().visit(this);

        String val = null;
        boolean isConstant = localDeclaration.isConstant();

        // Is it a protocol or a record type?
        if (localDeclaration.getType() instanceof RecordTypeDeclaration)
            type = localDeclaration.getType().toString();
        if (localDeclaration.getType() instanceof ProtocolTypeDeclaration)
            type = PJProtocolCase.class.getSimpleName();
        // Update the type for record and protocol types
        String chanType = type; // TODO: is this needed?

//        if ( ld.type().isArrayType() ) {
//            Type baseType = ((ArrayType) ld.type()).getActualBaseType();
//            if ( baseType.isChannelType() || baseType.isChannelEndType() )
//                type = PJChannel.class.getSimpleName() + "[][]";
//        }

        // Create a tag for this local declaration
        String newName = makeVariableName(name, ++localDecID, Tag.LOCAL_NAME);
        if (inParFor) {
            localsForAnonymousProcess.put(newName, type);
            paramsForAnonymousProcess.put(name, newName);
        }
        localToFields.put(newName, type);
        paramToVarNames.put(name, newName);

        // This variable could be initialized, e.g. through an assignment operator
        Expression expr = localDeclaration.getInitializationExpression();
        // Visit the expressions associated with this variable
        if (expr != null) {
            if (localDeclaration.getType() instanceof PrimitiveType)
                val = "";//(String) expr.visit(this);

            else if (localDeclaration.getType() instanceof RecordTypeDeclaration || localDeclaration.getType() instanceof ProtocolTypeDeclaration)
                val = ""; //(String) expr.visit(this);

            else if (localDeclaration.getType() instanceof ArrayType) {
                newChanArrayName = newName;
                //val = (String) expr.visit(this);

                newChanArrayName = null;
            }
        }

        // Is it a barrier declaration? If so, we must generate code
        // that creates a barrier object
        if (localDeclaration.getType().isBarrierType() && expr == null) {
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
        // in which it was declared, we return iff this local variable
        // is not initialized
        if (expr == null) {
            if (!localDeclaration.getType().isBarrierType() && (localDeclaration.getType() instanceof PrimitiveType || (localDeclaration.getType() instanceof ArrayType) || // Could be an
                    // uninitialized
                    // array
                    // declaration
                    localDeclaration.getType() instanceof RecordTypeDeclaration || // Could be a record or protocol declaration
                    localDeclaration.getType() instanceof ProtocolTypeDeclaration)); // The 'null' value is used to removed empty
                // return sequences in the generated code
        }

        // If we reach this section of code, then we have a variable
        // declaration with some initial value
        if (val != null)
            val = val.replace(DELIMITER, "");

        ST stVar = stGroup.getInstanceOf("Var");
        stVar.add("name", newName);
        // Lame fixed for variables that are constants.
        if (!isConstant) {
            stVar.add("val", val);
            //return null;
        } else {
            if (inParFor) {
                localsForAnonymousProcess.remove(newName);
                localsForAnonymousProcess.put(newName + " = " + val, type);
            }
            localToFields.remove(newName);
            localToFields.put(newName + " = " + val, "final " + type);
        }

//        return stVar.render();

    }

    @Override
    public final void visitParameterDeclaration(ParameterDeclaration parameterDeclaration)  {
        org.processj.compiler.Compiler.Info(parameterDeclaration + "Visiting a ParamDecl (" + parameterDeclaration.getType() + " " + parameterDeclaration + ")");

        // Grab the type and name of a variable declaration
        // TODO: Originally pd.getName().visit(this)
        String name = parameterDeclaration.getName().toString();

        String type = "";//(String) parameterDeclaration.getType().visit(this);

        // Silly fix for channel types
        if (parameterDeclaration.getType() instanceof ChannelType || parameterDeclaration.getType() instanceof ChannelEndType)
            type = PJChannel.class.getSimpleName() + type.substring(type.indexOf("<"), type.length());
        else if (parameterDeclaration.getType() instanceof RecordTypeDeclaration)
            type = parameterDeclaration.getType().toString();
        else if (parameterDeclaration.getType() instanceof ProtocolTypeDeclaration)
            type = parameterDeclaration.getType().toString();

        // Create a tag for this parameter and then add it to the collection
        // of parameters for reference
        String newName = makeVariableName(name, ++varDecID, Tag.PARAM_NAME);
        paramToFields.put(newName, type);
        paramToVarNames.put(name, newName);

        // Ignored the value returned by this visitor as the types and
        // variables are _always_ resolved elsewhere

    }

    @Override
    public final void visitAltCase(AltCase altCase)  {
        org.processj.compiler.Compiler.Info(altCase + "Visiting an AltCase");

        ST stAltCase = stGroup.getInstanceOf("AltCase");
        Statement stat = altCase.getGuard().getStatement();
//        String guard = (String) stat.visit(this);
        String guard = ""; //stat instanceof TimeoutStatement ? null : (String) stat.visit(this);

        String[] stats = new String[0]; //(String[]) altCase.getBody().visit(this);

        // <--
        if (!indexSetOfAltCase.isEmpty()) {
            ST stRepLocalVars = stGroup.getInstanceOf("RepLocalVars");
            stRepLocalVars.add("indexSet", indexSetOfAltCase);
            stAltCase.add("dynamicAlt", stRepLocalVars.render());
        }
        // -->
        stAltCase.add("number", altCase.getCaseNumber());
        stAltCase.add("guardExpr", guard);
        stAltCase.add("stats", stats);

    }

    @Override
    public final void visitBlockStatement(BlockStatement blockStatement) throws Phase.Error {

        // The scope in which declarations appear, starting with their
        // own initializers and including any further declarations like
        // invocations or sequence of statements
        String[] stats = new String[0]; //(String[]) blockStatement.getStatements().visit(this);

    }

    @Override
    public final void visitBreakStatement(BreakStatement breakStatement)  {
        org.processj.compiler.Compiler.Info(breakStatement + "Visiting a BreakStat");

        ST stBreakStat = stGroup.getInstanceOf("BreakStat");
        // No parse-tree for 'break'
        //if (breakStatement.getTarget() != null)
            //stBreakStat.add("name", breakStatement.getTarget().visit(this));

    }

    @Override
    public final void visitChannelWriteStatement(ChannelWriteStatement channelWriteStatement)  {
        org.processj.compiler.Compiler.Info(channelWriteStatement + "Visiting a ChannelWriteStat");

        ST stChanWriteStat = stGroup.getInstanceOf("ChanWriteStat");
        // 'c.write(x)' is a channel-end expression, where 'c' is the
        // writing end of the channel
        Expression chanExpr = channelWriteStatement.getTargetExpression();
        // 'c' is the name of the channel
        String chanWriteName = ""; //(String) chanExpr.visit(this);

        // Expression sent through channel
        String expr = ""; //(String) channelWriteStatement.getWriteExpression().visit(this);

        expr = expr.replace(DELIMITER, "");
        // The value one is for the 'runLabel'
        int countLabel = 1;
        // Is the writing end of this channel shared?
        if (chanExpr.type instanceof ChannelEndType && ((ChannelEndType) chanExpr.type).isSharedEnd()) {
            stChanWriteStat = stGroup.getInstanceOf("ChannelMany2One");
            ++countLabel;
        }

        stChanWriteStat.add("chanName", chanWriteName);
        stChanWriteStat.add("writeExpr", expr);

        // Add the switch block for resumption
        for (int label = 0; label < countLabel; ++label) {
            // Increment jump label and it to the switch-stmt list
            stChanWriteStat.add("resume" + label, ++jumpLabel);
            switchCases.add(renderSwitchCase(jumpLabel));
        }

    }

    @Override
    public final void visitContinueStatement(ContinueStatement continueStatement) throws Phase.Error {

        org.processj.compiler.Compiler.Info(continueStatement + "Visiting a ContinueStat");

        ST      stContinueStat  = stGroup.getInstanceOf("ContinueStat");
        String  name            = null;

        // If target isn't null, we have a label to jump to
        if (continueStatement.getTarget() != null) {

            name = ""; //(String) continueStatement.getTarget().visit(this);
            stContinueStat.add("name", name);

        }

    }

    @Override
    public final void visitDoStatement(DoStatement doStatement)  {
        org.processj.compiler.Compiler.Info(doStatement + "Visiting a DoStat");

        ST stDoStat = stGroup.getInstanceOf("DoStat");
        String[] stats = null;
        String expr = null;

        if(doStatement.getEvaluationExpression() != null)
            expr = ""; //((String) doStatement.getEvaluationExpression().visit(this));

        if (doStatement.getBody() != null) {
            //Object o = doStatement.getBody().visit(this);

            //if (o instanceof String) {
                //stats = new String[] { (String) o };
            //} else {
                //stats = (String[]) o;
            //}
        }

        stDoStat.add("expr", expr);
        stDoStat.add("body", stats);

    }

    @Override
    public final void visitExpressionStatement(ExpressionStatement expressionStatement)  {
        org.processj.compiler.Compiler.Info(expressionStatement + "Visiting an ExprStat");

        try {
            expressionStatement.getExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

    }

    @Override
    public final void visitForStatement(ForStatement forStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(forStatement + "Visiting a ForStat");

        ST stForStat = stGroup.getInstanceOf("ParForStat");
        String expr = null;
        ArrayList<String> init = null;
        ArrayList<String> incr = null;
        String[] stats = null;

        boolean preParFor = inParFor;
        inParFor = forStatement.isPar() || preParFor;

        if (forStatement.getInitializationStatements() != null) {
            init = new ArrayList<>();
            //for(Statement st : forStatement.getInitializationStatements())
                //init.add(((String) st.visit(this)).replace(DELIMITER, ""));

        }
        if (forStatement.getEvaluationExpression() != null)

                expr = ""; //(String) forStatement.getEvaluationExpression().visit(this);

        if (forStatement.getIncrementStatements() != null) {
            incr = new ArrayList<>();
            //for (Statement st : forStatement.getIncrementStatements())
                //incr.add(((String) st.visit(this)).replace(DELIMITER, ""));

        }

        if (!forStatement.isPar()) {
            if (forStatement.getBody() != null) {
                //Object o = forStatement.getBody().visit(this);

                //if (o instanceof String) {
                    //stats = new String[] { (String) o };
                //} else {
                    //stats = (String[]) o;
                //}
            }

            stForStat = stGroup.getInstanceOf("ForStat");
            stForStat.add("init", init);
            stForStat.add("expr", expr);
            stForStat.add("incr", incr);
            stForStat.add("stats", stats);

        }

        // Save previous barrier expressions
        ArrayList<String> prevBarrier = barriers;

        // Save the previous par-block
        String prevParBlock = currentParBlock;
        currentParBlock = makeVariableName(Tag.PAR_BLOCK_NAME.toString(), ++parDecID, Tag.LOCAL_NAME);

        // Increment the jump label and add it to the switch-stmt list
        stForStat.add("jump", ++jumpLabel);
        switchCases.add(renderSwitchCase(jumpLabel));

        // Rendered the value of each statement
        ArrayList<String> stmts = new ArrayList<String>();


        barriers = new ArrayList<>();

       // for(final Expression expression: forStatement.getBarriers())
            //barriers.add((String) expression.visit(this));

        Sequence<Expression> se = forStatement.getBarrierExpressions();
        for (Statement st : forStatement.getBody().getStatements()) {
            // An expression is any valid unit of code that resolves to a value,
            // that is, it can be a combination of variables, operations and values
            // that org.processj.yield a result. An statement is a line of code that performs
            // some action, e.g. print statements, an assignment statement, etc.
            if (st instanceof ExpressionStatement && ((ExpressionStatement) st).getExpression() instanceof InvocationExpression) {
                ExpressionStatement es = (ExpressionStatement) st;
                InvocationExpression in = (InvocationExpression) es.getExpression();
                // If this invocation is made on a process, then visit the
                // invocation and return a string representing the wrapper
                // class for this procedure; e.g.
                // (new <classType>(...) {
                // @Override public synchronized void run() { ... }
                // @Override public finalize() { ... }
                // }.schedule();
                if(in.targetProc.doesYield()) {
                    //stmts.add((String) in.visit(this));
                } //else // Otherwise, the invocation is made through a static Java method
                    //stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
            } //else stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));

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

    }

    @Override
    public final void visitIfStatement(IfStatement ifStatement) throws Phase.Error {
        org.processj.compiler.Compiler.Info(ifStatement + "Visiting a IfStat");

        ST stIfStat = stGroup.getInstanceOf("IfStat");
        // Sequence of statements enclosed in a block-stmt
        String[] thenStats = null;
        String[] thenParts = null;
        String condExpr = null;
        // We either have an if-statement _or_ a loop construct that
        // has been re-written as an if-statement
        //if(ifStatement.getEvaluationExpression() != null)
            //condExpr = (String) ifStatement.getEvaluationExpression().visit(this);

        thenStats = new String[0]; //(String[]) ifStatement.getThenBody().visit(this);

        if(ifStatement.getElseBody() != null) {

            // TODO: Else Body is always a Sequence; Make sure we can
            // TODO: Generate s String from the statements
            thenParts = new String[0]; //(String[]) ifStatement.getElseBody().visit(this);

        }

        stIfStat.add("expr", condExpr);
        stIfStat.add("thenPart", thenStats);
        stIfStat.add("elsePart", thenParts);

    }

    @Override
    public final void visitParBlockStatement(ParBlock parBlock) throws Phase.Error {

        org.processj.compiler.Compiler.Info(parBlock + "Visiting a ParBlock with " + parBlock.getBody().getStatements().size() + " statements.");

        // Don't generate code for an empty par statement
        //if(parBlock.getBody().getStatements().size() == 0)
            //return null;

        ST stParBlock = stGroup.getInstanceOf("ParBlock");
        // Save the previous par-block
        String prevParBlock = currentParBlock;
        // Save previous barrier expressions
        ArrayList<String> prevBarrier = barriers;
        // Create a name for this new par-block
        currentParBlock = makeVariableName(Tag.PAR_BLOCK_NAME.toString(), ++parDecID, Tag.LOCAL_NAME);

        // Since this is a new par-block, we need to create a variable
        // inside the process in which this par-block was declared
        stParBlock.add("name", currentParBlock);
        stParBlock.add("count", parBlock.getBody().getStatements().size());
        stParBlock.add("process", "this");

        // Increment the jump label and add it to the switch-stmt list
        stParBlock.add("jump", ++jumpLabel);
        switchCases.add(renderSwitchCase(jumpLabel));

        // Add the barrier this par-block enrolls in
        if(parBlock.getBarrierSet().size() > 0) {

            HashMap<String, Integer> parBlockBarrierSet = new HashMap<>();

            for(Expression e: parBlock.getBarrierSet()) {

                String name = ""; //(String) e.visit(this);
                parBlockBarrierSet.put(name, parBlock.getEnrolls().get(((NameExpression) e).toString()));

            }

            stParBlock.add("barrier", parBlockBarrierSet.keySet());
            stParBlock.add("enrollees", parBlockBarrierSet.values());

        }

        // Visit the sequence of statements in the par-block
        Sequence<Statement> statements = (Sequence<Statement>) parBlock.getBody().getStatements();

        // Rendered the value of each statement
        ArrayList<String> stmts = new ArrayList<String>();

        for(Statement st : statements) {
            if(st == null)
                continue;

            Sequence<Expression> se = new Sequence<>();
            st.getBarrierSet().forEach(se::append);

            if(se != null) {
                barriers = new ArrayList<>();

                //for(Expression e: se)
                    //barriers.add((String) e.visit(this));

            }

            if(st instanceof ExpressionStatement && ((ExpressionStatement) st).getExpression() instanceof InvocationExpression) {

                ExpressionStatement es = (ExpressionStatement)     st;
                InvocationExpression in = (InvocationExpression)   es.getExpression();
                // If this invocation is made inside a process, then visit the
                // invocation and return a string representing the wrapper
                // class for this procedure; e.g.
                // (new <classType>(...) {
                // @Override public synchronized void run() { ... }
                // @Override public finalize() { ... }
                // }.schedule();
                //if(in.targetProc.doesYield()) stmts.add((String) in.visit(this));

                // Otherwise, the invocation is made through a static Java method
                //else stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));

            } //else stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));

        }

        stParBlock.add("body", stmts);

        // Restore the par-block
        currentParBlock = prevParBlock;
        // Restore barrier expressions
        barriers = prevBarrier;

    }

    @Override
    public final void visitProtocolCase(ProtocolCase protocolCase)  {
        org.processj.compiler.Compiler.Info(protocolCase + "Visiting a ProtocolCase (" + protocolCase + ")");

        ST stProtocolType = stGroup.getInstanceOf("ProtocolType");
        // Since we are keeping the name of a tag as is, this (in theory)
        // shouldn't cause any name collision
        String protocName = ""; //(String) protocolCase.getName().visit(this);
        // This shouldn't create name collision problems even if we
        // use the same visitor for protocols and records
        recordMemberToField.clear();

        // The scope in which all members of this tag appeared
        for (RecordMemberDeclaration rm : protocolCase.getBody())
            try {
                rm.accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }

        // The list of fields passed to the constructor of the static
        // class that the record belongs to
        if (!recordMemberToField.isEmpty()) {
            stProtocolType.add("types", recordMemberToField.values());
            stProtocolType.add("vars", recordMemberToField.keySet());
        }
        stProtocolType.add("name", protocName);

    }

    @Override
    public final void visitReturnStatement(ReturnStatement returnStatement)  {
        org.processj.compiler.Compiler.Info(returnStatement + "Visiting a ReturnStat");

        ST stReturnStat = stGroup.getInstanceOf("ReturnStat");
        String expr = "";

        //if (returnStatement.getExpression() != null)
            //expr = (String) returnStatement.getExpression().visit(this);

        // This removes the extra ";" for invocation calls
        expr = expr.replace(DELIMITER, "");
        stReturnStat.add("expr", expr);

    }

    @Override
    public final void visitSwitchStatement(SwitchStatement switchStatement)  {
        org.processj.compiler.Compiler.Info(switchStatement + "Visiting a SwitchStat");

        ST stSwitchStat = stGroup.getInstanceOf("SwitchStat");
        // Is this a protocol tag?
        if (switchStatement.getEvaluationExpression().type instanceof ProtocolTypeDeclaration)
            isProtocolCase = true;

        String expr = ""; //(String) switchStatement.getEvaluationExpression().visit(this);

        ArrayList<String> switchGroup = new ArrayList<>();

        //for (SwitchGroupStatement sg : (Sequence<SwitchGroupStatement>) switchStatement.getBody().getStatements())
            //switchGroup.add((String) sg.visit(this));

        stSwitchStat.add("tag", isProtocolCase);
        stSwitchStat.add("expr", expr);
        stSwitchStat.add("block", switchGroup);

        // Reset the value for this protocol tag
        isProtocolCase = false;

    }

    @Override
    public final void visitSwitchLabelExpression(SwitchLabel switchLabel)  {
        org.processj.compiler.Compiler.Info(switchLabel + "Visiting a SwitchLabel");

        ST stSwitchLabel = stGroup.getInstanceOf("SwitchLabel");

        // This could be a default label, in which case, expr() would be null
        String label = null;
        //if (!switchLabel.isDefault())
            //label = (String) switchLabel.getExpression().visit(this);

        if (isProtocolCase) {
            // The protocol tag currently being used
            currentProtocolTag = label;
            label = String.format("\"%s\"", label);
        }

        stSwitchLabel.add("label", label);

    }

    @Override
    public final void visitSwitchGroupStatement(SwitchGroupStatement switchGroupStatement)  {
        org.processj.compiler.Compiler.Info(switchGroupStatement + "Visit a SwitchGroup");

        ST stSwitchGroup = stGroup.getInstanceOf("SwitchGroup");

        ArrayList<String> labels = new ArrayList<>();
        //for (SwitchLabel sl : switchGroupStatement.getLabels())
            //labels.add((String) sl.visit(this));

        ArrayList<String> stats = new ArrayList<>();
        for (Statement st : switchGroupStatement.getStatements()) {
            if (st != null) {
                //Object stmt = st.visit(this);

                //if (stmt instanceof String[])
                    //stats.addAll(Arrays.asList((String[]) stmt));
                //else
                    //stats.add((String) stmt);
            }
        }

        stSwitchGroup.add("labels", labels);
        stSwitchGroup.add("stats", stats);

    }

    @Override
    public final void visitSyncStatement(SyncStatement syncStatement)  {
        org.processj.compiler.Compiler.Info(syncStatement + "Visiting a SyncStat");

        ST stSyncStat = stGroup.getInstanceOf("SyncStat");
        String barrier = ""; //(String) syncStatement.getBarrierExpression().visit(this);

        stSyncStat.add("barrier", barrier);

        // Increment the jump label and add it to the switch-stmt list
        stSyncStat.add("resume0", ++jumpLabel);
        switchCases.add(renderSwitchCase(jumpLabel));

    }

    @Override
    public final void visitTimeoutStatement(TimeoutStatement timeoutStatement)  {
        org.processj.compiler.Compiler.Info(timeoutStatement + "Visiting a TimeoutStat");

        ST stTimeoutStat = stGroup.getInstanceOf("TimeoutStat");
        //String timer = (String) timeoutStatement.getTimerExpression().visit(this);

        String delay = ""; //(String) timeoutStatement.getDelayExpression().visit(this);

        //stTimeoutStat.add("name", timer);
        stTimeoutStat.add("delay", delay);

        // Increment the jump label and add it to the switch-stmt list
        stTimeoutStat.add("resume0", ++jumpLabel);
        switchCases.add(renderSwitchCase(jumpLabel));

    }

    @Override
    public final void visitWhileStatement(WhileStatement whileStatement) throws Phase.Error  {
        org.processj.compiler.Compiler.Info(whileStatement + "Visiting a WhileStat");

        ST stWhileStat = stGroup.getInstanceOf("WhileStat");
        String[] stats = null;
        String expr = null;



        //if(whileStatement.getEvaluationExpression() != null)
            //expr = ((String) whileStatement.getEvaluationExpression().visit(this));

        if(whileStatement.getBody() != null) {
            //Object o = whileStatement.getBody().visit(this);

            //if(o instanceof String) {
                //stats = new String[] { (String) o };
            //} else {
                //stats = (String[]) o;
            //}
        }

        stWhileStat.add("expr", expr);
        stWhileStat.add("body", stats);

    }









    @Override
    public final void visitArrayAccessExpression(ArrayAccessExpression arrayAccessExpression)  {
        org.processj.compiler.Compiler.Info(arrayAccessExpression + "Visiting an ArrayAccessExpr");

        ST stArrayAccessExpr = stGroup.getInstanceOf("ArrayAccessExpr");
        //String name = (String) arrayAccessExpression.getTargetExpression().visit(this);

        //String index = (String) arrayAccessExpression.getIndexExpression().visit(this);

        //stArrayAccessExpr.add("name", name);
        stArrayAccessExpr.add("index", index);

    }

    @Override
    public final void visitAssignmentExpression(AssignmentExpression assignmentExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(assignmentExpression + "Visiting an Assignment");

        ST stVar = stGroup.getInstanceOf("Var");

        String op = assignmentExpression.opString();
        String lhs = null;
        String rhs = null;
        String type = null;

        if (assignmentExpression.getLeftExpression() != null) { // Not a protocol or record
            lhs = ""; //(String) assignmentExpression.getLeftExpression().visit(this);

            // Unfortunately, a declaration of for an array of channel reads must be
            // of the form 'PJOne2OneChannel<?>[]...' due to the way inheritance is
            // done in Java. Thus we need need to cast - unnecessarily - the returned
            // value of a channel read expression
            if (assignmentExpression.getLeftExpression().type != null) {
                if (assignmentExpression.getLeftExpression().type instanceof RecordTypeDeclaration)
                    type = assignmentExpression.getLeftExpression().type.toString();
                else if (assignmentExpression.getLeftExpression().type instanceof ProtocolTypeDeclaration)
                    type = PJProtocolCase.class.getSimpleName();
                //else type = (String) assignmentExpression.getLeftExpression().type.visit(this);

            }
        }

        if (assignmentExpression.getRightExpression() instanceof NewArrayExpression)
            createNewArray(lhs, ((NewArrayExpression) assignmentExpression.getRightExpression()));

        else if (assignmentExpression.getRightExpression() instanceof ChannelReadExpression)
            createChannelReadExpr(lhs, type, op, ((ChannelReadExpression) assignmentExpression.getRightExpression()));
        else if (assignmentExpression.getRightExpression() != null) {
            rhs = ""; //(String) assignmentExpression.getRightExpression().visit(this);
            rhs = rhs.replace(DELIMITER, "");
        }

        stVar.add("name", lhs);
        stVar.add("val", rhs);
        stVar.add("op", op);

    }

    @Override
    public final void visitBinaryExpression(BinaryExpression binaryExpression)  {
        org.processj.compiler.Compiler.Info(binaryExpression + "Visiting a BinaryExpr");

        ST stBinaryExpr = stGroup.getInstanceOf("BinaryExpr");
        String op = binaryExpression.opString();
        String lhs = ""; //(String) binaryExpression.getLeft().visit(this);

        lhs = lhs.replace(DELIMITER, "");
        lhs = binaryExpression.getLeftExpression().hasParens ? "(" + lhs + ")" : lhs;
        String rhs = ""; //(String) binaryExpression.getRight().visit(this);

        rhs = binaryExpression.getRightExpression().hasParens ? "(" + rhs + ")" : rhs;
        rhs = rhs.replace(DELIMITER, "");

        // <--
        // Silly rewrite for comparing two strings in ProcessJ using the
        // equals(Xxx) method from Java
        if ("==".equals(op) && (binaryExpression.getLeftExpression() instanceof NameExpression && binaryExpression.getRightExpression() instanceof NameExpression)
                && ((((NameExpression) binaryExpression.getLeftExpression()).myDecl instanceof LocalDeclaration)
                && ((NameExpression) binaryExpression.getRightExpression()).myDecl instanceof LocalDeclaration)) {
            LocalDeclaration ld1 = (LocalDeclaration) ((NameExpression) binaryExpression.getLeftExpression()).myDecl;
            LocalDeclaration ld2 = (LocalDeclaration) ((NameExpression) binaryExpression.getRightExpression()).myDecl;
            if (ld1.getType().isStringType() && ld2.getType().isStringType()) {
                stBinaryExpr = stGroup.getInstanceOf("StringCompare");
                stBinaryExpr.add("str1", lhs);
                stBinaryExpr.add("str2", rhs);
            }
        }
        // A rewrite for the 'instanceof' operator in Java happens when the token OP
        // in a binary expression represents the token 'is'. Thus, to render the correct
        // code, we look for the name of the left-hand side operand, which is a record
        // or protocol variable, and then use the NameType of the right-hand side
        // operand as the type to check if the left-hand side operand is indeed an
        // instanceof the right hand side operand
        // TODO: there should be a check for this??
        if ("instanceof".equals(op) && localToFields.containsKey(lhs)) {
            String namedType = localToFields.get(lhs);
            Object o = getScope().get(namedType);
            if (o instanceof RecordTypeDeclaration) {
                stBinaryExpr = stGroup.getInstanceOf("RecordExtend");
                stBinaryExpr.add("name", lhs);
                stBinaryExpr.add("type", String.format("I_%s", rhs));
            }

            if(namedType.equals(PJProtocolCase.class.getSimpleName())) {
                stBinaryExpr = stGroup.getInstanceOf("RecordExtend");
                stBinaryExpr.add("name", lhs);
                stBinaryExpr.add("type", currentProtocol);
            }

        }
        // -->

        stBinaryExpr.add("lhs", lhs);
        stBinaryExpr.add("rhs", rhs);
        stBinaryExpr.add("op", op);

    }

    @Override
    public final void visitCastExpression(CastExpression castExpression) throws Phase.Error  {
        org.processj.compiler.Compiler.Info(castExpression + "Visiting a CastExpr");

        ST stCastExpr = stGroup.getInstanceOf("CastExpr");
        // This result in (TYPE)(EXPR)
        String type = ""; //(String) castExpression.getCastType().visit(this);
        String expr = ""; //(String) castExpression.getExpression().visit(this);

        stCastExpr.add("type", type);
        stCastExpr.add("expr", expr);

    }

    @Override
    public final void visitChannelEndExpression(ChannelEndExpression channelEndExpression) throws Phase.Error {

        String channel = ""; //(String) channelEndExpression.getChannelType().visit(this);

    }

    @Override
    public final void visitChannelReadExpression(ChannelReadExpression channelReadExpression) throws Phase.Error  {

        ST stChannelReadExpr = stGroup.getInstanceOf("ChannelReadExpr");

        // 'c.read()' is a channel-end expression, where 'c' is the reading
        // end of the channel
        Expression chanExpr = channelReadExpression.getTargetExpression();
        // 'c' is the name of the channel
        String chanEndName = ""; //(String) chanExpr.visit(this);

        stChannelReadExpr.add("chanName", chanEndName);

        // Add the switch block for resumption
        for(int label = 0; label < 2; ++label) {

            // Increment jump label and it to the switch-stmt list
            stChannelReadExpr.add("resume" + label, ++jumpLabel);
            switchCases.add(renderSwitchCase(jumpLabel));

        }


    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final void visitInvocationExpression(InvocationExpression invocationExpression) throws Phase.Error  {

        // We ignore any GOTO or LABEL invocation since they are only needed
        // for the __asm__ bytecode rewrite
        if(invocationExpression.ignore) {

            org.processj.compiler.Compiler.Info(invocationExpression + "Visiting a " + invocationExpression);

            ST stIgnore = stGroup.getInstanceOf("InvocationIgnore");

            //stIgnore.add("name", paramToVarNames.getOrDefault(invocationExpression.getProcedureName(), invocationExpression.getProcedureName()));
            //stIgnore.add("var", invocationExpression.getParameters().visit(this));

        }

        // Check if the invocation is either a 'sync' or a 'fork'
        if(invocationExpression.getProcedureName().equals("sync") || invocationExpression.getProcedureName().equals("fork")) {
            org.processj.compiler.Compiler.Info(invocationExpression + "Visiting Invocation (" + invocationExpression + ")");
            // TODO:
        }

        ST stInvocation = null;
        // Target procedure
        ProcedureTypeDeclaration pd = invocationExpression.targetProc;
        // Name of invoked procedure
        String pdName = pd.toString();

        // Check local procedures, if none is found then the procedure must
        // come from a different file and maybe package
        final String filename = ""; //pd.myCompilation.fileName;
        if(currentCompilation.fileName.equals(filename)) {

            String name = pdName + hashSignature(pd);

            if(pd.doesYield()) name = makeVariableName(name, 0, Tag.PROCEDURE_NAME);
            else name = makeVariableName(name, 0, Tag.METHOD_NAME);


            pdName = filename; //pd.myCompilation.fileNoExtension() + "." + name;

        } else if(pd.isNative()) {

            // Make the package visible on import by using the qualified
            // name of the class the procedure belongs to and the name of
            // the directory the procedure's class belongs to, e.g.
            // std.io.println(), where
            // 1.) 'std' is the name of the package,
            // 2.) 'io' is the name of the class/file,
            // 3.) 'println' is the method declared in the class
            pdName = pd.getName().getPackageName() + "." + pdName;

        } else ; // TODO: This procedure is called from another package

        // These are the formal parameters of the procedure/method being invoked
        // which are specified by a list of comma-separated arguments
        Sequence<Expression> parameters = invocationExpression.getParameterExpressions();
        String[] paramsList = new String[0]; //(String[]) parameters.visit(this);

        if(paramsList != null)
            for(int i = 0; i < paramsList.length; ++i)
                paramsList[i] = paramsList[i].replace(DELIMITER, "");

        // For an invocation of a procedure that yields and one which
        // is not inside par-block, we wrap the procedure in a par-block
        if(pd.doesYield() && currentParBlock == null) {
             (new ParBlock(new Sequence(new ExpressionStatement(invocationExpression)), // Statements
                        new Sequence())) // Barriers
                        .accept(this); // Return a procedure wrapped in a par-block

        }

        // Does this procedure yield?
        if(pd.doesYield()) {

            stInvocation = stGroup.getInstanceOf("InvocationProcType");
            stInvocation.add("parBlock", currentParBlock);
            stInvocation.add("isPar", inParFor);

            // Add the barrier this procedure should resign from
            if(!barriers.isEmpty())
                stInvocation.add("barrier", barriers);

        } else
            // Must be an invocation made through a static Java method
            stInvocation = stGroup.getInstanceOf("Invocation");

        stInvocation.add("name", pdName);
        stInvocation.add("vars", paramsList);

    }

    @Override
    public final void visitNameExpression(final NameExpression nameExpression) {

        this.sourceContext.addChild(new NameExpressionSourceContext(nameExpression.getName()));

    }

    @Override
    public final void visitNewArrayExpression(NewArrayExpression newArrayExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(newArrayExpression + "Visiting a NewArray");
        createNewArray(null, newArrayExpression);

    }

    @Override
    public final void visitRecordAccessExpression(final RecordAccessExpression recordAccessExpression) throws Phase.Error {

        ST stAccessor;

        // Initialize a handle to the Target Expression's Type
        final Type      targetType = recordAccessExpression.getTarget().getType();
        final String    name        = ""; //(String) recordAccessExpression.getTarget().visit(this); // Reference to inner class type

        if(targetType instanceof RecordTypeDeclaration) {

            stAccessor = stGroup.getInstanceOf("RecordAccessor");

            String field = recordAccessExpression.toString();

            stAccessor.add("name", name);
            stAccessor.add("member", field);

        } else if(targetType instanceof ProtocolTypeDeclaration) {

            stAccessor = stGroup.getInstanceOf("ProtocolAccess");

            String field = recordAccessExpression.toString(); // Field in inner class

            ProtocolTypeDeclaration pt = (ProtocolTypeDeclaration) targetType;
            String protocName = paramToVarNames.getOrDefault(pt.toString(), pt.toString());

            // Cast a protocol to a super-type if needed
            String key = String.format("%s.%s", protocName, currentProtocolTag);

            if(protocolNameToProtocolTag.containsKey(key))
                protocName = protocolNameToProtocolTag.get(key);

            stAccessor.add("protocName", protocName);
            stAccessor.add("tag", currentProtocolTag);
            stAccessor.add("var", name);
            stAccessor.add("member", field);

        // This is for arrays and strings -- ProcessJ has no notion of classes,
        // i.e. it has no concept of objects either. Arrays and strings are
        // therefore treated as primitive data types
        } else {

            stAccessor = stGroup.getInstanceOf("RecordAccessor");

            stAccessor.add("name", name);

            // Call the appropriate method to retrieve the number of characters
            // in a string or the number of elements in an N-dimensional array
            if(recordAccessExpression.isArraySize) // 'Xxx.size' for N-dimensional array
                stAccessor.add("member", "length");

            else if (recordAccessExpression.isStringLength) // 'Xxx.length' for number of characters in a string
                stAccessor.add("member", "length()");

        }

    }





    @Override
    public final void visitTernaryExpression(final TernaryExpression ternaryExpression) throws Phase.Error {

        final ExpressionSourceContext evaluationExpressionSourceContext =
                new ExpressionSourceContext();

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = evaluationExpressionSourceContext;

        // Visit the Evaluation Expression
        ternaryExpression.getEvaluationExpression().accept(this);

        this.sourceContexts.peek().addChild(evaluationExpressionSourceContext);

        final ExpressionSourceContext thenSourceContext =
                new ExpressionSourceContext();

        this.sourceContext = thenSourceContext;

        // Visit the Then Expression
        ternaryExpression.getThenExpression().accept(this);

        this.sourceContexts.peek().addChild(thenSourceContext);

        final ExpressionSourceContext elseSourceContext =
                new ExpressionSourceContext();

        this.sourceContext = elseSourceContext;

        // Visit the else Expression
        ternaryExpression.getElseExpression().accept(this);

        // Restore
        this.sourceContexts.peek().addChild(elseSourceContext);
        this.sourceContext = this.sourceContexts.pop();

        this.sourceContext.addChild(new TernaryExpressionSourceContext(
                evaluationExpressionSourceContext, thenSourceContext, elseSourceContext));

    }

    @Override
    public final void visitUnaryPostExpression(final UnaryPostExpression unaryPostExpression) throws Phase.Error  {

        final UnaryPostExpressionSourceContext unaryPostExpressionSourceContext =
                new UnaryPostExpressionSourceContext(unaryPostExpression.opString());

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = unaryPostExpressionSourceContext;

        // Visit the Component Type
        unaryPostExpression.getExpression().accept(this);

        this.sourceContext = this.sourceContexts.pop();
        this.sourceContext.addChild(unaryPostExpressionSourceContext);

    }

    @Override
    public final void visitUnaryPreExpression(final UnaryPreExpression unaryPreExpression) throws Phase.Error {

        final UnaryPreExpressionSourceContext unaryPreExpressionSourceContext =
                new UnaryPreExpressionSourceContext(unaryPreExpression.opString());

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = unaryPreExpressionSourceContext;

        // Visit the Component Type
        unaryPreExpression.getExpression().accept(this);

        this.sourceContext = this.sourceContexts.pop();
        this.sourceContext.addChild(unaryPreExpressionSourceContext);

    }

    // TODO: Right-hand side Expression
    @Override
    public final void visitArrayLiteralExpression(ArrayLiteralExpression arrayLiteralExpression) throws Phase.Error  {

        // Initialize a handle to the ArrayLiteralSourceContext
        final ArrayLiteralSourceContext arrayLiteralSourceContext = new ArrayLiteralSourceContext();

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = arrayLiteralSourceContext;

        // Visit the Type
        arrayLiteralExpression.getType().accept(this);

        // Visit the Expressions
        arrayLiteralExpression.getExpressions().accept(this);

        // Pop the previous SourceContext
        this.sourceContext = this.sourceContexts.pop();

        // Aggregate the ArrayLiteral SourceContext
        this.sourceContext.addChild(arrayLiteralSourceContext);

    }

    // TODO: Right-hand side Expression
    @Override
    public final void visitPrimitiveLiteralExpression(final PrimitiveLiteralExpression primitiveLiteralExpression)  {

        this.sourceContext.addChild(new PrimitiveLiteralSourceContext(primitiveLiteralExpression));

    }

    // TODO: Right-hand side Expression
    @Override
    public final void visitProtocolLiteralExpression(final ProtocolLiteralExpression protocolLiteralExpression) throws Phase.Error {

        // Initialize a handle to the ProtocolLiteral Expression's Name & target Tag
        final Name name = protocolLiteralExpression.getName();
        final Name tag  = protocolLiteralExpression.getTag();

        // Initialize a handle to the ProtocolLiteralSourceContext
        final ProtocolLiteralSourceContext sourceContext = new ProtocolLiteralSourceContext(name, tag);

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = sourceContext;

        // Visit the Expressions
        protocolLiteralExpression.getExpressions().accept(this);

        // Initialize a handle to the ProtocolType Declaration & the Protocol Case
        // TODO: Make sure that the ProtocolTypeDeclaration outputs its' defined members in-order
        // TODO: Maybe Visit Children?
        final ProtocolTypeDeclaration protocolTypeDeclaration = (ProtocolTypeDeclaration) this.getScope().get(name.toString());
        final ProtocolCase      target                  = protocolTypeDeclaration.getCase(tag.toString());

        // Aggregate the Expressions to match RecordTypeDeclaration's order
        target.getBody().forEach(sourceContext::resolveOrder);

        // Pop the previous SourceContext
        this.sourceContext = this.sourceContexts.pop();

        // Aggregate the SourceContext to the parent
        this.sourceContext.addChild(sourceContext);

    }

    // TODO: Right-hand side Expression
    @Override
    public final void visitRecordLiteralExpression(final RecordLiteralExpression recordLiteralExpression) throws Phase.Error  {

        // Initialize a handle to the ProtocolLiteral Expression's Name & target Tag
        final Name name = recordLiteralExpression.getName();

        // Initialize a handle to the ProtocolLiteralSourceContext
        final RecordLiteralSourceContext recordLiteralSourceContext = new RecordLiteralSourceContext(name);

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = recordLiteralSourceContext;

        // Visit the Expressions
        recordLiteralExpression.getRecordMemberLiterals().accept(this);

        // Initialize a handle to the ProtocolType Declaration & the Protocol Case
        // TODO: Make sure that the ProtocolTypeDeclaration outputs its' defined members in-order
        // TODO: Maybe Visit Children?
        final RecordTypeDeclaration recordTypeDeclaration = (RecordTypeDeclaration) this.getScope().get(name.toString());

        // Aggregate the Expressions to match RecordTypeDeclaration's order
        recordTypeDeclaration.getBody().forEach(recordLiteralSourceContext::resolveOrder);

        // Pop the previous SourceContext
        this.sourceContext = this.sourceContexts.pop();

        // Aggregate the SourceContext to the parent
        this.sourceContext.addChild(recordLiteralSourceContext);

    }

    @Override
    public final void visitRecordMemberLiteral(final RecordMemberLiteralExpression recordMemberLiteralExpression) throws Phase.Error {

        final RecordMemberLiteralSourceContext recordMemberLiteralSourceContext =
                new RecordMemberLiteralSourceContext(recordMemberLiteralExpression.getName());

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = recordMemberLiteralSourceContext;

        // Visit the Expression
        recordMemberLiteralExpression.getExpression().accept(this);

        this.sourceContext = this.sourceContexts.pop();
        this.sourceContext.addChild(recordMemberLiteralSourceContext);

    }

    @Override
    public final void visitArrayType(final ArrayType arrayType) throws Phase.Error {

        final ArrayTypeSourceContext arrayTypeSourceContext = new ArrayTypeSourceContext(arrayType);

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = arrayTypeSourceContext;

        // Visit the Component Type
        arrayType.getComponentType().accept(this);

        this.sourceContext = this.sourceContexts.pop();
        this.sourceContext.addChild(arrayTypeSourceContext);

    }

    @Override
    public final void visitChannelType(final ChannelType channelType) throws Phase.Error  {

        final ChannelTypeSourceContext channelTypeSourceContext = new ChannelTypeSourceContext(channelType);

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = channelTypeSourceContext;

        // Visit the Component Type
        channelType.getComponentType().accept(this);

        this.sourceContext = this.sourceContexts.pop();
        this.sourceContext.addChild(channelTypeSourceContext);

    }

    @Override
    public final void visitChannelEndType(final ChannelEndType channelEndType) throws Phase.Error {

        final ChannelEndTypeSourceContext channelEndTypeSourceContext = new ChannelEndTypeSourceContext(channelEndType);

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = channelEndTypeSourceContext;

        // Visit the Component Type
        channelEndType.getComponentType().accept(this);

        this.sourceContext = this.sourceContexts.pop();
        this.sourceContext.addChild(channelEndTypeSourceContext);

    }

    @Override
    public final void visitPrimitiveType(final PrimitiveType primitiveType) {

        this.sourceContext.addChild(new PrimitiveTypeSourceContext(primitiveType));

    }


    private static abstract class JavaSourceContext {


        public void addChild(final JavaSourceContext javaSourceContext) { }

        public List<JavaSourceContext> getFlattened() { return List.of(); }

    }

    private static class ExpressionSourceContext extends JavaSourceContext {

        private List<JavaSourceContext> children;

        public ExpressionSourceContext() {

            this.children = new ArrayList<>();

        }

        @Override
        public final String toString() {
            return "";
        }

        @Override
        public final void addChild(final JavaSourceContext javaSourceContext) {

            // TODO: Assert we don't overwrite
            // TODO: Resolve for BinaryExpr
            if(javaSourceContext != null)
                this.children.add(javaSourceContext);

        }

        @Override
        public List<JavaSourceContext> getFlattened() {

            return this.children;

        }

    }

    private static class NameExpressionSourceContext extends JavaSourceContext {

        private Name name;

        public NameExpressionSourceContext(final Name name) {

            this.name = name;

        }

        @Override
        public final String toString() {
            return "";
        }

        @Override
        public List<JavaSourceContext> getFlattened() {

            return List.of(this);

        }

    }

    private static class TernaryExpressionSourceContext extends JavaSourceContext {

        private JavaSourceContext evaluationSourceContext   ;
        private JavaSourceContext thenSourceContext         ;
        private JavaSourceContext elseSourceContext         ;

        public TernaryExpressionSourceContext(final JavaSourceContext evaluationSourceContext,
                                              final JavaSourceContext thenSourceContext,
                                              final JavaSourceContext elseSourceContext) {

            this.evaluationSourceContext    = evaluationSourceContext;
            this.thenSourceContext          = thenSourceContext;
            this.elseSourceContext          = elseSourceContext;

        }

        @Override
        public final String toString() {
            return "";
        }

        @Override
        public List<JavaSourceContext> getFlattened() {

            return List.of(this.evaluationSourceContext, this.thenSourceContext, this.elseSourceContext);

        }

    }

    private static class UnaryPostExpressionSourceContext extends JavaSourceContext {

        private final String            operator                ;
        private List<JavaSourceContext> children                ;

        public UnaryPostExpressionSourceContext(final String operator) {

            this.operator                   = operator              ;
            this.children                   = new ArrayList<>()     ;

        }

        @Override
        public final String toString() {

            // <expression><operator>
            return "" + operator;

        }

        @Override
        public final void addChild(final JavaSourceContext javaSourceContext) {

            // TODO: Assert we don't overwrite
            if(javaSourceContext != null)
                this.children.addAll(javaSourceContext.getFlattened());

        }

        @Override
        public List<JavaSourceContext> getFlattened() {

            return this.children;

        }

    }

    private static class UnaryPreExpressionSourceContext extends JavaSourceContext {

        private final String            operator                ;
        private List<JavaSourceContext> children                ;

        public UnaryPreExpressionSourceContext(final String operator) {

            this.operator                   = operator              ;
            this.children                   = new ArrayList<>()     ;

        }

        @Override
        public final String toString() {

            // <operator><expression>
            return operator + "";

        }

        @Override
        public final void addChild(final JavaSourceContext javaSourceContext) {

            // TODO: Assert we don't overwrite
            if(javaSourceContext != null)
                this.children.addAll(javaSourceContext.getFlattened());

        }

        @Override
        public List<JavaSourceContext> getFlattened() {

            return this.children;

        }

    }

    // TODO: Generate Java new Array
    private static class ArrayLiteralSourceContext extends JavaSourceContext {

        private JavaSourceContext       typeSourceContext   ;
        private List<JavaSourceContext> children            ;

        public ArrayLiteralSourceContext() {

            this.typeSourceContext  = null                  ;
            this.children           = new ArrayList<>()     ;

        }

        /**
         * Generates a new <type>[] {..., ..., ..., ... }; Expression i.e. new Array Literal Creation
         */
        @Override
        public final String toString() {

            final StringBuilder stringBuilder = new StringBuilder("new ");

            stringBuilder
                    //.append(this.type)
                    .append("[] { ")
                    // TODO: Visit Children
                    .append(" };");

            // TODO: Expressions Visit
            //this.expressions.forEach(expression -> expression.visit(this));

            return stringBuilder.toString();

        }

        @Override
        public final void addChild(final JavaSourceContext javaSourceContext) {

            if(javaSourceContext instanceof TypeSourceContext)
                this.typeSourceContext = javaSourceContext;

            else if(javaSourceContext != null)
                this.children.add(javaSourceContext);

        }

        @Override
        public List<JavaSourceContext> getFlattened() {

            final List<JavaSourceContext> flattened = new ArrayList<>();

            flattened.add(typeSourceContext);

            this.children.forEach(javaSourceContext -> flattened.addAll(javaSourceContext.getFlattened()));

            return flattened;

        }

    }

    // TODO: TBD
    private static class PrimitiveLiteralSourceContext extends JavaSourceContext {

        private PrimitiveLiteralExpression primitiveLiteral   ;

        public PrimitiveLiteralSourceContext(final PrimitiveLiteralExpression primitiveLiteral) {

            this.primitiveLiteral = primitiveLiteral ;

        }

        @Override
        public final String toString() {

            return primitiveLiteral.toString();

        }

        @Override
        public List<JavaSourceContext> getFlattened() {

            return List.of(this);

        }

    }

    // TODO: Generate Java new Object
    private static class ProtocolLiteralSourceContext extends JavaSourceContext {

        private Name                            tag                     ;
        private Name                            type                    ;
        private Map<String, JavaSourceContext>  recordMemberLiterals    ;
        private List<JavaSourceContext>         children                ;

        public ProtocolLiteralSourceContext(final Name type, final Name tag) {

            this.tag                    = type              ;
            this.type                   = type              ;
            this.children               = new ArrayList<>() ;
            this.recordMemberLiterals   = new HashMap()     ;

        }

        /**
         * Generates a new type.tag(); Expression i.e. instance creation
         * @return
         */
        @Override
        public final String toString() {

            final StringBuilder stringBuilder = new StringBuilder("new ");

            stringBuilder
                    .append(this.type)
                    .append(".")
                    .append(this.tag)
                    .append("(");

            // TODO: Expressions Visit
            //this.expressions.forEach(expression -> expression.visit(this));

            return stringBuilder.toString();

        }

        public final void resolveOrder(final RecordMemberDeclaration recordMemberDeclaration) {

            if(recordMemberDeclaration != null) {

                // Initialize a handle to the Expression
                final JavaSourceContext javaSourceContext =
                        this.recordMemberLiterals.get(recordMemberDeclaration.toString());

                // TODO: Assert the Expression was found
                this.recordMemberLiterals.remove(recordMemberDeclaration.toString());

                // Aggregate the Expression
                this.children.add(javaSourceContext);

            }

        }

        @Override
        public void addChild(final JavaSourceContext javaSourceContext) {

            // TODO: Assert JavaSourceContext is a RecordLiteralSourceContext
            if(javaSourceContext instanceof RecordMemberLiteralSourceContext) {

                final RecordMemberLiteralSourceContext recordMemberLiteralSourceContext =
                        (RecordMemberLiteralSourceContext) javaSourceContext;

                this.recordMemberLiterals.put(
                        recordMemberLiteralSourceContext.getName().toString(),
                        recordMemberLiteralSourceContext.getExpressionSourceContext());

            }

        }

        @Override
        public List<JavaSourceContext> getFlattened() {

            final List<JavaSourceContext> flattened = new ArrayList<>();

            flattened.add(this);

            this.children.forEach(javaSourceContext -> flattened.addAll(javaSourceContext.getFlattened()));

            return flattened;

        }

    }

    // TODO: Generate Java new Object
    private static class RecordLiteralSourceContext extends JavaSourceContext {

        private Name                            type                    ;
        private Map<String, JavaSourceContext>  recordMemberLiterals    ;
        private List<JavaSourceContext>         children                ;

        public RecordLiteralSourceContext(final Name type) {

            this.type                   = type              ;
            this.children               = new ArrayList<>() ;
            this.recordMemberLiterals   = new HashMap()     ;

        }

        /**
         * Generates a new type.tag(); Expression i.e. instance creation
         */
        @Override
        public final String toString() {

            final StringBuilder stringBuilder = new StringBuilder("new ");

            stringBuilder
                    .append(this.type)
                    .append("(");

            return stringBuilder.toString();

        }

        public final void resolveOrder(final RecordMemberDeclaration recordMemberDeclaration) {

            if(recordMemberDeclaration != null) {

                // Initialize a handle to the Expression
                final JavaSourceContext javaSourceContext =
                        this.recordMemberLiterals.get(recordMemberDeclaration.toString());

                // TODO: Assert the Expression was found
                this.recordMemberLiterals.remove(recordMemberDeclaration.toString());

                // Aggregate the Expression
                this.children.add(javaSourceContext);

            }

        }

        @Override
        public void addChild(final JavaSourceContext javaSourceContext) {

            // TODO: Assert JavaSourceContext is a RecordLiteralSourceContext
            if(javaSourceContext instanceof RecordMemberLiteralSourceContext) {

                final RecordMemberLiteralSourceContext recordMemberLiteralSourceContext =
                        (RecordMemberLiteralSourceContext) javaSourceContext;

                this.recordMemberLiterals.put(
                        recordMemberLiteralSourceContext.getName().toString(),
                        recordMemberLiteralSourceContext.getExpressionSourceContext());

            }

        }

        @Override
        public List<JavaSourceContext> getFlattened() {

            final List<JavaSourceContext> flattened = new ArrayList<>();

            flattened.add(this);

            this.children.forEach(javaSourceContext -> flattened.addAll(javaSourceContext.getFlattened()));

            return flattened;

        }

    }

    // TODO: TBD
    private static class RecordMemberLiteralSourceContext extends JavaSourceContext {

        private Name                        name                    ;
        private JavaSourceContext           expressionSourceContext ;

        public RecordMemberLiteralSourceContext(final Name name) {

            this.name                       = name ;
            this.expressionSourceContext    = null ;

        }

        @Override
        public void addChild(final JavaSourceContext javaSourceContext) {

            // TODO: Assert expressionTypeContext is not being overwritten
            this.expressionSourceContext = javaSourceContext;

        }

        public final Name getName() {

            return this.name;

        }

        public final JavaSourceContext getExpressionSourceContext() {

            return this.expressionSourceContext;

        }

    }

    interface TypeSourceContext {}

    // TODO: Print Translation i.e. type[]...[]
    private static class ArrayTypeSourceContext extends JavaSourceContext implements TypeSourceContext {

        private ArrayType           arrayType               ;
        private JavaSourceContext   componentTypeContext    ;

        public ArrayTypeSourceContext(final ArrayType arrayType) {

            this.arrayType              = arrayType    ;
            this.componentTypeContext   = null         ;

        }

        @Override
        public String toString() {

            String type = "";

            if ((arrayType.getComponentType() instanceof ChannelType)
                    || (arrayType.getComponentType() instanceof ChannelEndType))
                // Truncate the <
                type = type.substring(0, type.indexOf("<"));// + "<?>";

            else if (arrayType.getComponentType() instanceof RecordTypeDeclaration)
                type = arrayType.getComponentType().toString();

            else if (arrayType.getComponentType() instanceof ProtocolTypeDeclaration)
                type = PJProtocolCase.class.getSimpleName();
            //else if (componentType instanceof PrimitiveType)
            //    ;

            return type + "[]".repeat(arrayType.getDepth());

        }

        @Override
        public void addChild(final JavaSourceContext javaSourceContext) {

            // TODO: Assert ComponentTypeContext is not being overwritten
            this.componentTypeContext = javaSourceContext;

        }

        @Override
        public List<JavaSourceContext> getFlattened() {

            final List<JavaSourceContext> flattened = new ArrayList<>();

            flattened.add(this);
            flattened.addAll(this.componentTypeContext.getFlattened());

            return flattened;

        }

    }

    // TODO: Print Translation i.e. type<componentType>
    private static class ChannelTypeSourceContext extends JavaSourceContext implements TypeSourceContext {

        private ChannelType         channelType             ;
        private JavaSourceContext   componentTypeContext    ;

        public ChannelTypeSourceContext(final ChannelType channelType) {

            this.channelType            = channelType    ;
            this.componentTypeContext   = null           ;

        }

        @Override
        public String toString() {

            // Channel class type
            String chantype = "";
            switch (channelType.isShared()) {
                case ChannelType.NOT_SHARED:
                    chantype = PJOne2OneChannel.class.getSimpleName();
                    break;
                case ChannelType.SHARED_READ:
                    chantype = PJOne2ManyChannel.class.getSimpleName();
                    break;
                case ChannelType.SHARED_WRITE:
                    chantype = PJMany2OneChannel.class.getSimpleName();
                    break;
                case ChannelType.SHARED_READ_WRITE:
                    chantype = PJMany2ManyChannel.class.getSimpleName();
                    break;
            }
            // Resolve parameterized type for channel, e.g. chan<T> where
            // 'T' is the type to be resolved
            //String type = getChannelType(channelType.getComponentType());

            return "";//String.format("%s<%s>", chantype, type);

        }

        @Override
        public void addChild(final JavaSourceContext javaSourceContext) {

            // TODO: Assert ComponentTypeContext is not being overwritten
            this.componentTypeContext = javaSourceContext;

        }

        @Override
        public List<JavaSourceContext> getFlattened() {

            final List<JavaSourceContext> flattened = new ArrayList<>();

            flattened.add(this);
            flattened.addAll(this.componentTypeContext.getFlattened());

            return flattened;

        }

    }

    // TODO: Print Translation i.e. type<componentType>
    private static class ChannelEndTypeSourceContext extends JavaSourceContext implements TypeSourceContext {

        private ChannelEndType      channelEndType          ;
        private JavaSourceContext   componentTypeContext    ;

        public ChannelEndTypeSourceContext(final ChannelEndType channelEndType) {

            this.channelEndType         = channelEndType    ;
            this.componentTypeContext   = null              ;

        }

        @Override
        public String toString() {

            // Channel class type
            String chanType = PJOne2OneChannel.class.getSimpleName();
            // Is it a shared channel?
            if (channelEndType.isSharedEnd()) {
                if (channelEndType.isReadEnd()) // One-2-Many channel
                    chanType = PJOne2ManyChannel.class.getSimpleName();
                else if (channelEndType.isWriteEnd()) // Many-2-One channel
                    chanType = PJMany2OneChannel.class.getSimpleName();
                else // Many-2-Many channel
                    chanType = PJMany2ManyChannel.class.getSimpleName();
            }
            // Resolve parameterized type for channels, e.g. chan<T> where
            // 'T' is the type to be resolved
            //String type = getChannelType(channelEndType.getComponentType());

            return ""; //String.format("%s<%s>", chanType, type);

        }

        @Override
        public void addChild(final JavaSourceContext javaSourceContext) {

            // TODO: Assert ComponentTypeContext is not being overwritten
            this.componentTypeContext = javaSourceContext;

        }

        @Override
        public List<JavaSourceContext> getFlattened() {

            final List<JavaSourceContext> flattened = new ArrayList<>();

            flattened.add(this);
            flattened.addAll(this.componentTypeContext.getFlattened());

            return flattened;

        }

    }

    // TODO: Print Translation
    private static class PrimitiveTypeSourceContext extends JavaSourceContext implements TypeSourceContext  {

        private final PrimitiveType primitiveType;

        public PrimitiveTypeSourceContext(final PrimitiveType primitiveType) {

            this.primitiveType = primitiveType;

        }

        @Override
        public String toString() {

            String string = this.primitiveType.toString();

            if(this.primitiveType.isStringType())
                string = "String";

            else if (this.primitiveType.isTimerType())
                string = PJTimer.class.getSimpleName();

            else if (this.primitiveType.isBarrierType())
                string = PJBarrier.class.getSimpleName();

            return string;

        }

        @Override
        public List<JavaSourceContext> getFlattened() { return List.of(this); }

    }




    @Override
    @SuppressWarnings("rawtypes")
    public final void visitSequence(Sequence sequence)  {
        org.processj.compiler.Compiler.Info(sequence + "Visiting a Sequence");

        // Sequence of statements enclosed in a block-stmt
        ArrayList<String> seqs = new ArrayList<>();
        // Iterate through every statement
        for (int i = 0; i < sequence.size(); ++i) {
            if (sequence.child(i) != null) {
                Object stats = null; //sequence.child(i).visit(this);

                if (stats == null)
                    continue;
                // These are either
                // 1.) a sequence of statements, or
                // 2.) a single statement
                // found in a block statement, e.g. local declarations,
                // variable declarations, invocations, etc.
                if (stats instanceof String[])
                    seqs.addAll(Arrays.asList((String[]) stats));
                else
                    seqs.add((String) stats);
            }
        }

        //seqs.toArray(new String[0]);
    }

    /** List of replicated alt loops */
    ArrayList<ST> arrayOfReplicatedAltLoop;
    ArrayList<String> indexSetOfAltCase;
    ArrayList<String> listOfReplicatedAltLocals;
    ArrayList<String> listOfReplicatedAltCases;
    ArrayList<String> listOfReplicatedObjectGuards;
    ArrayList<String> listOfReplicatedAltLoops;

    int indexForAltStat;
    int readyID;
    int booleanGuardID;
    int objectGuardID;

    @Override
    public final void visitAltStatement(AltStatement altStatement)  {
        org.processj.compiler.Compiler.Info(altStatement + "Visiting an AltStat");
        
        arrayOfReplicatedAltLoop = new ArrayList<>();
        indexSetOfAltCase = new ArrayList<>();
        listOfReplicatedAltLocals = new ArrayList<>();
        listOfReplicatedAltCases = new ArrayList<>();
        listOfReplicatedObjectGuards = new ArrayList<>();
        listOfReplicatedAltLoops = new ArrayList<>();
        
        indexForAltStat = 1;
        readyID = 0;
        booleanGuardID = 0;
        objectGuardID = 0;

        // For dynamic or replicated alts we use 'AltGuard' objects, which contain
        // the indices of an n-array, where 'n' represents the number of loops to
        // wrap this guard, and a case number
        // *******************************************************************
        // *******************************************************************
        // <--
        // Added: 06/02/2022 -- Rewrite for replicated atls
        // This is where we handle the generated code for RepAlts; note that
        // a dynamic alt is one that contains a replicated Alt
        if (altStatement.isReplicated()) {
            org.processj.compiler.Compiler.Info(altStatement + "Visiting a Dynamic or Replicated Alt");
            // This queue contains the number of cases found in the alt stmt
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            Sequence<AltCase> reAltCase = altStatement.getBody();
            // This section belongs to the generated code for the ForStat
            // The ST for the ForStat pertaining to an AltCase should be
            // created only for replicated Alts
            if (altStatement.isReplicated())
                arrayOfReplicatedAltLoop.add((ST) createAltForStat(altStatement));
            // Count the number of cases of this dynamic alt
            for (int i = 0; i < reAltCase.size(); ++i)
                queue.add(i);
            // Rewrite guards that are not part of an altStat
            for (int i = 0; i < reAltCase.size(); ++i) {
                AltCase ac = reAltCase.child(i);
                if (!ac.isNestedAltStatement())
                    reAltCase.insert(i, createDynamicAltStat(ac));
            }
            int childIndex = 0;
            for (int altCaseIndex = queue.remove();;) {
//                System.err.println("[" + altCaseIndex + "]");
                AltCase ac = reAltCase.child(childIndex);
                ac.setCaseNumber(altCaseIndex);
                if (ac.isNestedAltStatement()) {
                    ST stForStat = (ST) createAltForStat((AltStatement) ((BlockStatement) ac.getBody()).getStatements().child(0));
                    arrayOfReplicatedAltLoop.add(stForStat);
                    AltStatement as2 = (AltStatement) ((BlockStatement) ac.getBody()).getStatements().child(0);
                    reAltCase = as2.getBody();
//                    altCaseIndex = 0;
                    childIndex = 0;
                } else {
                    ST forStat = null;
                    if (!arrayOfReplicatedAltLoop.isEmpty()) {
                        forStat = arrayOfReplicatedAltLoop.get(0);
                        for (int j = 1; j < arrayOfReplicatedAltLoop.size(); ++j) {
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
                    if (ac.getPreconditionExpression() == null)
                        bguard = String.valueOf(true);
                    else if (ac.getPreconditionExpression() instanceof LiteralExpression)
                        bguard = ""; //(String) ac.getPreconditionExpression().visit(this);
                    else {
                        // This is for expressions that evaluate to a boolean value.
                        // Such expressions become local variables (or, in our case, fields)
                        Name n = new Name("btemp");
                        LocalDeclaration ld = new LocalDeclaration(new PrimitiveType(PrimitiveType.BooleanKind),
                                n, ac.getPreconditionExpression(), false /* not constant */);
                        //listOfReplicatedAltLocals.add((String) ld.visit(this));

                        bguard = ""; //(String) n.visit(this);

                    }

                    Statement stat = ac.getGuard().getStatement();
                    // TODO: What if the expression is not an array??
                    // Is it just a channel read expression?
                    if (stat instanceof ExpressionStatement) {
                        Expression e = ((ExpressionStatement) stat).getExpression();
                        ChannelReadExpression cr = null;
                        if (e instanceof AssignmentExpression) {
                            cr = (ChannelReadExpression) ((AssignmentExpression) e).getRightExpression();
                            // Finally, we are in the last replicated alt, so we need to
                            // store each object guard with its correct index (or position)
                            ST stRepAltObjectGuard = stGroup.getInstanceOf("RepAltObjectGuards");
                            stRepAltObjectGuard.add("objectGuards", "repAltObjectGuards");
                            //stRepAltObjectGuard.add("objectValue", (String) cr.getChannelExpression().visit(this));

                            stRepAltObjectGuard.add("booleanGuards", "repAltBooleanGuards");
                            stRepAltObjectGuard.add("booleanValue", bguard);
                            // Set the number of loops, the index set, and the case number
                            // pertaining to this altCase
                            ST stRepAltIndexSet = stGroup.getInstanceOf("RepAltIndexSet");
                            stRepAltIndexSet.add("size", arrayOfReplicatedAltLoop.size());
                            stRepAltIndexSet.add("indexSet", indexSetOfAltCase);
                            stRepAltIndexSet.add("altCase", altCaseIndex);
                            stRepAltIndexSet.add("varID", indexForAltStat);

                            forStat.add("stats", stRepAltIndexSet.render());
                            forStat.add("stats", stRepAltObjectGuard.render());
                            listOfReplicatedAltLoops.add(arrayOfReplicatedAltLoop.get(0).render());
                        }
                    }
                    // Skip statement?
                    else if (stat instanceof SkipStatement)
                        listOfReplicatedObjectGuards.add(String.format("%s.SKIP", PJAlt.class.getSimpleName()));
                    // Timeout statement?
                    else if (stat instanceof TimeoutStatement) {
                        // Initialize the timeout statement
                        TimeoutStatement ts = (TimeoutStatement) stat;
//                        ST stTimeout = stGroup.getInstanceOf("TimeoutStatCase");
//                        stTimeout.add("name", ts.timer().visit(this));
//                        stTimeout.add("delay", ts.delay().visit(this));
//                        listOfReplicatedAltLocals.add(stTimeout.render());
                        //listOfReplicatedAltLocals.add((String) ts.visit(this));

                        //listOfReplicatedObjectGuards.add((String) ts.getTimerExpression().visit(this));

                    }
                    //listOfReplicatedAltCases.add((String) ac.visit(this));

                    // *************************************
                    // *************************************
                    if (!queue.isEmpty()) {
                        reAltCase = altStatement.getBody();
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
            // I don't think this is needed anymore, but we will leave it here for legacy
            // code
            Name n = new Name("index" + (++indexForAltStat));
            try {
                new LocalDeclaration(new PrimitiveType(PrimitiveType.IntKind), n, null, false /* not constant */)
                        .accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            // Create a tag for this local alt declaration
            String newName = makeVariableName("alt", ++localDecID, Tag.LOCAL_NAME);
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
            //stReplicatedAltStat.add("index", n.visit(this));

            // Add the jump label to the switch-stmt list
            switchCases.add(renderSwitchCase(jumpLabel));

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

        Sequence<AltCase> cases = altStatement.getBody();
        ArrayList<String> blocals = new ArrayList<>(); // Variables for pre-guard expressions
        ArrayList<String> bguards = new ArrayList<>(); // Default boolean guards
        ArrayList<String> guards = new ArrayList<>(); // Guard statements
        ArrayList<String> altCases = new ArrayList<>();// Generated code for each alt-cases
        ArrayList<String> tlocals = new ArrayList<>(); // Timeouts

        // Set boolean guards
        for (int i = 0; i < cases.size(); ++i) {
            AltCase ac = cases.child(i);
            if (ac.getPreconditionExpression() == null)
                bguards.add(String.valueOf(true));
            else if (ac.getPreconditionExpression() instanceof LiteralExpression){
            //bguards.add((String) ac.getPreconditionExpression().visit(this));

            } else {
                // This is for expressions that evaluate to a boolean value.
                // Such expressions become local variables (or, in our case, fields)
                Name n = new Name("btemp");
                LocalDeclaration ld = new LocalDeclaration(new PrimitiveType(PrimitiveType.BooleanKind),
                        n, ac.getPreconditionExpression(), false /* not constant */);
                // blocals.add((String) ld.visit(this));

                //bguards.add((String) n.visit(this));

            }
        }
        stBooleanGuards.add("constants", bguards);
        stBooleanGuards.add("locals", blocals);

        //
        stBooleanGuards.add("readyID", booleanGuardID);
        //

        // Set case number for all AltCases
        for (int i = 0; i < cases.size(); ++i)
            cases.child(i).setCaseNumber(i);
        // Visit all guards
        for (int i = 0; i < cases.size(); ++i) {
            AltCase ac = cases.child(i);
            Statement stat = ac.getGuard().getStatement();
            // Channel read expression?
            if (stat instanceof ExpressionStatement) {
                Expression e = ((ExpressionStatement) stat).getExpression();
                ChannelReadExpression cr = null;
                if (e instanceof AssignmentExpression)
                    cr = (ChannelReadExpression) ((AssignmentExpression) e).getRightExpression();
                //guards.add((String) cr.getChannelExpression().visit(this));

            }
            // Skip statement?
            else if (stat instanceof SkipStatement)
                guards.add(String.format("%s.SKIP", PJAlt.class.getSimpleName()));
            // Timeout statement?
            else if (stat instanceof TimeoutStatement) {
                // Initialize the timeout statement
                TimeoutStatement ts = (TimeoutStatement) stat;
//                ST stTimeout = stGroup.getInstanceOf("TimeoutStatCase");
//                stTimeout.add("name", ts.timer().visit(this));
//                stTimeout.add("delay", ts.delay().visit(this));
//                tlocals.add(stTimeout.render());

                    //tlocals.add((String) ts.visit(this));


                    //guards.add((String) ts.getTimerExpression().visit(this));

            }
            //altCases.add((String) ac.visit(this));

        }
        stTimerLocals.add("timers", tlocals);
        stObjectGuards.add("guards", guards);

        //
        stObjectGuards.add("readyID", objectGuardID);
        //

        // <--
        // This is needed because of the StackMapTable for the generated Java bytecode
        Name n = new Name("index");
        try {
            new LocalDeclaration(new PrimitiveType(PrimitiveType.IntKind), n, null, false /* not constant */).accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        // Create a tag for this local alt declaration
        String newName = makeVariableName("alt", ++localDecID, Tag.LOCAL_NAME);
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
        //stAltStat.add("index", n.visit(this));

        //
        stAltStat.add("readyID", readyID);
        //

        // Add the jump label to the switch-stmt list
        switchCases.add(renderSwitchCase(jumpLabel));
        readyID++;
        booleanGuardID = currBooleanGuard;
        objectGuardID = currObjectGuard;

    }

    private Object createAltForStat(AltStatement as)  {
        org.processj.compiler.Compiler.Info(as + ": Creating a ForStat for a replicated Alt");

        ST stForStat = stGroup.getInstanceOf("ForStat");
        ArrayList<String> init = null; // Initialization part
        ArrayList<String> incr = null; // Step counter
        String expr = null; // Conditional expression
        if (as.initializationStatements() != null) {
            init = new ArrayList<>();
            //for (Statement st : as.initializationStatements())
                //init.add(((String) st.visit(this)).replace(DELIMITER, ""));

            // Collect all local variables that pertain to the replicated alt
            for (String str : init)
                indexSetOfAltCase.add(str.substring(0, str.indexOf("=")).trim());
        }
        if(as.evaluationExpression() != null)
            expr = ""; //(String) as.evaluationExpression().visit(this);

        if (as.getIncrementStatements() != null) {
            incr = new ArrayList<>();
            //for(Statement st : as.getIncrementStatements())
                //incr.add(((String) st.visit(this)).replace(DELIMITER, ""));

        }
        stForStat.add("init", init);
        stForStat.add("expr", expr);
        stForStat.add("incr", incr);
        return stForStat;
    }

    // *************************************************************************
    // ** HELPER METHODS

    /**
     * Returns the parameterized type of a Channel object.
     * 
     * @param t The specified primitive type or user-defined type.
     * @return The type parameter delimited by angle brackets.
     */
    private String getChannelType(Type t) throws Phase.Error  {
        String baseType = null;
        if (t instanceof RecordTypeDeclaration) {
            baseType = t.toString();
        } else if (t instanceof ProtocolTypeDeclaration) {
            baseType = PJProtocolCase.class.getSimpleName();
        } else if (t instanceof PrimitiveType) {
            // This is needed because we can only have wrapper class
            baseType = getWrapperType(t);
        } else if (t instanceof ArrayType) {
            //baseType = (String) t.visit(this);
        }

        return baseType;
    }

    // This is used for newly-created processes
    private void resetGlobals()  {
        parDecID = 0;
        varDecID = 0;
        localDecID = 0;
        jumpLabel = 0;

        localToFields.clear();
        switchCases.clear();
        barriers.clear();

        paramToFields.clear();
        paramToVarNames.clear();

        localsForAnonymousProcess.clear();
        paramsForAnonymousProcess.clear();
    }

    // Returns a string representation of a jump label
    private String renderSwitchCase(int jump)  {
        ST stSwitchCase = stGroup.getInstanceOf("SwitchCase");

        stSwitchCase.add("jump", jump);

        return stSwitchCase.render();
    }

    /**
     * Creates and returns an anonymous procedure for non-invocations.
     * 
     * @param st The statement inside the body of a procedure.
     * @return An 'anonymous' procedure.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ProcedureTypeDeclaration createAnonymousProcTypeDecl(Statement st)  {
        return new ProcedureTypeDeclaration(null,
                new Modifiers(), // Modifiers
                null, // Return type
                new Name("Anonymous"), // Procedure name
                new Sequence(), // Formal parameters
                new Sequence(), // Implement
                new BlockStatement(new Sequence(st))); // Body
    }

    int index = 0;

    private List<?> createLocalDeclForLoop(String dims)  {
        final String localDeclName = makeVariableName("loop", index++, Tag.LOCAL_NAME).replace("_ld$", "");
        Name n = new Name(localDeclName);
        NameExpression ne = new NameExpression(n);
        PrimitiveLiteralExpression pl = new PrimitiveLiteralExpression(new Token(0, "0", 0, 0, 0), 4 /* kind */);
        LocalDeclaration ld = new LocalDeclaration(new PrimitiveType(PrimitiveType.IntKind), n, null,
                false /* not constant */);
        try {
            ld.accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        BinaryExpression be = new BinaryExpression(ne, new NameExpression(new Name(dims)), BinaryExpression.LT);
        ExpressionStatement es = new ExpressionStatement(new UnaryPreExpression(ne, UnaryPreExpression.PLUSPLUS));
        Sequence<Statement> init = new Sequence<>();
        init.append(new ExpressionStatement((Expression) new AssignmentExpression(ne, pl, AssignmentExpression.EQ)));
        Sequence<ExpressionStatement> incr = new Sequence<>();
        incr.append(es);
        return List.of(init, be, incr);
    }

    @SuppressWarnings("unchecked")
    private Object createNewArray(String lhs, NewArrayExpression na) throws Phase.Error {

        ST          stNewArray  = stGroup.getInstanceOf("NewArray");
        String[]    dims        = new String[0];//(String[]) na.getBracketExpressions().visit(this);
        String      type        = "";//(String) na.getComponentType().visit(this);

        boolean isChannelType = false;

        // This is done so that we can instantiate arrays of channel types whose types are generic
        if(na.getComponentType() instanceof ChannelType
                || na.getComponentType() instanceof ChannelEndType) {
            type = type.substring(0, type.indexOf("<"));// + "<?>";
            isChannelType = true;
        }

        ST stNewArrayLiteral = stGroup.getInstanceOf("NewArrayLiteral");

        if(na.getInitializationExpression() != null) {

            ArrayList<String>       inits   = new ArrayList<>();
            Sequence<Expression>    seq     = (Sequence<Expression>) na.getInitializationExpression().elements();

            for(Expression e : seq) {

                if(e instanceof ArrayLiteralExpression)
                    isArrayLiteral = true;

                //inits.add((String) e.visit(this));

            }

            stNewArrayLiteral.add("dim", String.join("", Collections.nCopies(((ArrayType) na.type).getDepth(), "[]")));
            stNewArrayLiteral.add("vals", inits);

        } else stNewArrayLiteral.add("dims", dims);

        stNewArray.add("name", lhs);
        stNewArray.add("type", type);
        stNewArray.add("init", stNewArrayLiteral.render());

        // Reset value for array literal expression
        isArrayLiteral = false;

        if(isChannelType && newChanArrayName != null) {

            ST stCreateArray = stGroup.getInstanceOf("createArray");
            stCreateArray.add("name", newChanArrayName);
            stCreateArray.add("newArray", stNewArray.render());
            stCreateArray.add("type", type);
            //stCreateArray.add("chanType", na.getComponentType().visit(this)); // this may be removed????
            stCreateArray.add("brackets", String.join("", Collections.nCopies(((ArrayType) na.type).getDepth(), "[]")));
            stCreateArray.add("dims", dims.length);

            return stCreateArray.render();

        }

        return stNewArray.render();

    }

    private Object createChannelReadExpr(String lhs, String type, String op, ChannelReadExpression cr) {
        org.processj.compiler.Compiler.Info(cr + "Creating Channel Read Expression");

        ST stChannelReadExpr = stGroup.getInstanceOf("ChannelReadExpr");
        // 'c.read()' is a channel-end expression, where 'c' is the
        // reading end of a channel
        Expression chanExpr = cr.getTargetExpression();
        // 'c' is the name of the channel
        String chanEndName = null;
        //chanEndName = (String) chanExpr.visit(this);

        // Is it a timer read expression?
        if (chanExpr.type.isTimerType()) {
            ST stTimerRedExpr = stGroup.getInstanceOf("TimerRedExpr");
            stTimerRedExpr.add("name", lhs);
            return stTimerRedExpr.render();
        }

        // One for the 'label' and one for the 'read' operation
        int countLabel = 2;
        // Is the reading end of this channel shared?
        if (chanExpr.type instanceof ChannelEndType && ((ChannelEndType) chanExpr.type).isSharedEnd()) {
            stChannelReadExpr = stGroup.getInstanceOf("ChannelOne2Many");
            ++countLabel;
        }

        // Do we have an extended rendezvous?
        if (cr.getExtendedRendezvous() != null) {
            //Object o = cr.getExtendedRendezvous().visit(this);

            ST stBlockRV = stGroup.getInstanceOf("BlockRV");
            //stBlockRV.add("block", o);
            stChannelReadExpr.add("extendRv", stBlockRV.render());
        }

        stChannelReadExpr.add("chanName", chanEndName);
        // Add the switch block for resumption
        for (int label = 0; label < countLabel; ++label) {
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
        NameExpression ne = new NameExpression(n);
        PrimitiveLiteralExpression pl = new PrimitiveLiteralExpression(new Token(0, "0", 0, 0, 0), 4 /* kind */);
        LocalDeclaration ld = new LocalDeclaration(new PrimitiveType(PrimitiveType.IntKind), n, null,
                false /* not constant */);
        try {
            ld.accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        PrimitiveLiteralExpression pl2 = new PrimitiveLiteralExpression(new Token(0, "1", 0, 0, 0), 4 /* kind */);
        BinaryExpression be = new BinaryExpression(ne, pl2, BinaryExpression.LT);
        ExpressionStatement es = new ExpressionStatement(new UnaryPreExpression(ne, UnaryPreExpression.PLUSPLUS));
        Sequence<Statement> init = new Sequence<>();
        init.append(new ExpressionStatement((Expression) new AssignmentExpression(ne, pl, AssignmentExpression.EQ)));
        Sequence<ExpressionStatement> incr = new Sequence<>();
        incr.append(es);
        Sequence<AltCase> body = new Sequence<>();
        body.append(ac);
        AltStatement as = new AltStatement(init, be, incr, body, false);
        as.setReplicated(true);
        ac = new AltCase(null, null, new BlockStatement(new Sequence<>(as)));
        ac.setNestedAltStatement();
        return ac;
    }

    // Returns a string representing the signature of the wrapper
    // class or Java method that encapsulates a PJProcess
    public String hashSignature(ProcedureTypeDeclaration pd) {
        String signature = pd.getSignature();
        return String.valueOf(signature.hashCode()).replace('-', '$');
    }

    protected static void GenerateNativeLibraryCode(final Phase phase, final Context.SymbolMap scope, final Compilation compilation) throws Phase.Error {

        // If the pragma map contains a LIBRARY key
        if(scope.definesLibraryPragma()) {

            // Initialize a handle to the file & package name derived from the decoded Pragmas
            final String fileName       = scope.getNativeFilename();
            final String packageName    = compilation.getPackageName();

            // Initialize the source file name
            final String sourceFileName = packageName.replace('.', '/') + "_" + fileName;

            // TODO: Generate ProcessJFiles for LIBRARY & PROCESSJ specifications
            Phase.PragmaAssert.GenerateNativeLibraryCodeFrom(phase, scope.getNativeSignatures(),
                    packageName, scope.getNativeLibrary(), fileName, sourceFileName);

        }

    }

    /**
     * This enum represents and identifies various types such as
     * yielding procedures, non-yielding procedures, parameters,
     * local variables, protocols, records, channels, etc.
     *
     * @author ben
     * @version 06/15/2018
     * @since 1.2
     */
    public enum Tag {

        // Signatures and types. These labels can be used for debugging.
        MAIN_NAME ("([T;)V", "mainProcedureType"),
        PROCEDURE_NAME ("_proc$", "procedureType"),
        METHOD_NAME ("_method$", "methodType"),
        PARAM_NAME ("_pd$", "parameterType"),
        LOCAL_NAME ("_ld$", "localVariableType"),
        PAR_BLOCK_NAME ("par$", "parBlockType"),
        PROTOCOL_NAME ("_prot$", "protocolType");

        private final String tag;
        private final String label;

        Tag(String tag, String label) {
            this.tag = tag;
            this.label = label;
        }

        public static Tag get(String tag) {
            return findValueOf(tag);
        }

        public static boolean has(String tag) {
            try {
                return findValueOf(tag) != null;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        public static Tag findValueOf(String name) {
            try {
                return Tag.valueOf(name);
            } catch (IllegalArgumentException e) {
                Tag result = null;
                for (Tag t : values()) {
                    if ( t.tag.startsWith(name) ) {
                        if ( result==null )
                            result = t;
                        else
                            throw new IllegalArgumentException("Unable to find ambiguous tag '" +
                                    name + "' in " + tags());
                    }
                }
                if ( result==null )
                    throw new IllegalArgumentException("Unable to find ambiguous tag '" +
                                name + "' in " + tags());
                return result;
            }
        }

        public static List<String> tags() {
            return (List<String>) Arrays.stream(values())
                         .map(tag -> tag.toString())
                         .collect(Collectors.toList());
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return tag;
        }
    }

    public static class IOCallsRewrite extends Phase {

        public IOCallsRewrite() {
            super(null);
        }

        @Override
        public final void visitInvocationExpression(InvocationExpression invocationExpression) {
            org.processj.compiler.Compiler.Info(invocationExpression + "Attempting to rewrite invocation of " + invocationExpression);

            if (invocationExpression.targetProc != null) {

                org.processj.compiler.Compiler.Info(invocationExpression + "targetProc is " + invocationExpression.targetProc);

                if (invocationExpression.targetProc.getName().getPackageName().equals("io") &&
                    (invocationExpression.targetProc.toString().equals("println") ||
                     invocationExpression.targetProc.toString().equals("print"))) {
                    org.processj.compiler.Compiler.Info("This is the function we're looking for");
                } else {
                    //return invocationExpression;
                }
            }

            if (invocationExpression.getParameterExpressions() != null) {
                if(invocationExpression.getParameterExpressions().size() == 0) {
                    org.processj.compiler.Compiler.Info(invocationExpression + "Params are empty");
                    //return invocationExpression;
                }

                boolean rewritten = false;
                Sequence<Expression> params = invocationExpression.getParameterExpressions();
                Sequence<Expression> newParams = new Sequence<Expression>();
                org.processj.compiler.Compiler.Info(invocationExpression + "Invocation of " + invocationExpression + " has argument(s):");
                for (int i = 0; i < params.size(); ++i) {
                    if (params.child(i) instanceof BinaryExpression) {
                        org.processj.compiler.Compiler.Info(invocationExpression + params.child(i).toString());
                        if (checkForString((BinaryExpression)params.child(i)) == true) {
                            rewritten = true;
                            org.processj.compiler.Compiler.Info(invocationExpression + "string concatenation found, rewriting.");
                            newParams.merge(rebuildParams(extractParams((BinaryExpression)params.child(i))));
                        }
                    }
                }

                org.processj.compiler.Compiler.Info(invocationExpression + "Received params from extractParams():");
                for (int i = 0; i < newParams.size(); ++i) {
                    org.processj.compiler.Compiler.Info(invocationExpression + newParams.child(i).toString());
                }

                // TODO: is this appropriate? is there another way?
                if(rewritten == true) {
                    invocationExpression.children[2] = newParams;
                }
            }

            //return invocationExpression;
        }

        public Sequence<Expression> extractParams(BinaryExpression be) {
            org.processj.compiler.Compiler.Info(be + "Extracting Parameters from " + be);

            org.processj.compiler.Compiler.Info(be + "Left is " + be.getLeftExpression().toString() + ", right is " + be.getRightExpression().toString());

            Sequence<Expression> newParams = new Sequence<Expression>();

            if (be.getLeftExpression() instanceof BinaryExpression) {
                newParams.merge(extractParams((BinaryExpression)be.getLeftExpression()));
            } else if (be.getLeftExpression() instanceof PrimitiveLiteralExpression || be.getLeftExpression() instanceof ArrayAccessExpression) {
                newParams.append(be.getLeftExpression());
            }

            newParams.append(be.getRightExpression());

            return newParams;
        }

        public Sequence<Expression> rebuildParams(Sequence<Expression> params) {
            org.processj.compiler.Compiler.Info("rebuilding params from sequence:");
            for (int i = 0; i < params.size(); ++i) {
                org.processj.compiler.Compiler.Info(params.child(i).toString());
            }

            Sequence<Expression> rebuiltParams = new Sequence<Expression>();
            Expression left = null;
            BinaryExpression be = null;

            // parse the params and rebuilt our arguments the correct way
            for (int i = 0; i < params.size(); ++i) {
                org.processj.compiler.Compiler.Info("Checking " + params.child(i));
                if (params.child(i) instanceof PrimitiveLiteralExpression &&
                    ((PrimitiveLiteralExpression)params.child(i)).getKind() == PrimitiveLiteralExpression.StringKind) {
                    org.processj.compiler.Compiler.Info(params.child(i).toString() + " is string PrimitiveLiteral, appending as argument");
                    if (be != null) {
                        org.processj.compiler.Compiler.Info("Appending new BinaryExpr " + be.toString() + " to rebuiltParams");
                        rebuiltParams.append(be);
                        be = null;
                    } else if (left != null) {
                        org.processj.compiler.Compiler.Info("Appending unfinished lhs of BinaryExpr " + left.toString() + " to rebuiltParams");
                        rebuiltParams.append(left);
                        left = null;
                    }
                    org.processj.compiler.Compiler.Info("Appending " + params.child(i).toString() + " to rebuiltParams");
                    rebuiltParams.append(params.child(i));
                } else if (params.child(i) instanceof ArrayAccessExpression) {
                    Type t = ((ArrayAccessExpression) params.child(i)).getTargetExpression().getType();
                    if (t instanceof NamedType) {
                            org.processj.compiler.Compiler.Info("array of NamedTypes");
                        } else if (t instanceof PrimitiveType) {
                            org.processj.compiler.Compiler.Info("array of PrimitiveTypes");
                            if (((PrimitiveType)t).getKind() == PrimitiveType.StringKind) {
                                org.processj.compiler.Compiler.Info(params.child(i).toString() + " is string PrimitiveLiteral");
                                if (be != null) {
                                    org.processj.compiler.Compiler.Info("Appending to new BinaryExpr " + be.toString() + " to rebuiltParams");
                                    rebuiltParams.append(be);
                                    be = null;
                                } else if (left != null) {
                                    org.processj.compiler.Compiler.Info("Appending unfinished lhs of BinaryExpr " + left.toString() + " to rebuiltParams");
                                    rebuiltParams.append(left);
                                    left = null;
                                }
                                org.processj.compiler.Compiler.Info("Appending " + params.child(i).toString() + " to rebuiltParams");
                                rebuiltParams.append(params.child(i));
                            } else {
                                org.processj.compiler.Compiler.Info(params.child(i).toString() + " is not string PrimitiveLiteral");
                                if (be != null) {
                                    org.processj.compiler.Compiler.Info("Appending " + params.child(i).toString() + " to existing binaryExpr");
                                    be = new BinaryExpression(be, params.child(i), BinaryExpression.PLUS);
                                } else if (left != null) {
                                    org.processj.compiler.Compiler.Info("Appending " + params.child(i).toString() + " as rhs of new binaryExpr");
                                    be = new BinaryExpression(left, params.child(i), BinaryExpression.PLUS);
                                } else if (i == params.size() - 1) {
                                    org.processj.compiler.Compiler.Info("Last item in params, adding as next argument");
                                    rebuiltParams.append(params.child(i));
                                } else {
                                    org.processj.compiler.Compiler.Info("Adding " + params.child(i).toString() + " as lhs of potential BinaryExpr");
                                    left = params.child(i);
                                }
                            }
                        }
                } else {
                    org.processj.compiler.Compiler.Info(params.child(i).toString() + " is not string PrimitiveLiteral");
                    if (be != null) {
                        org.processj.compiler.Compiler.Info("Appending " + params.child(i).toString() + " to existing binaryExpr");
                        be = new BinaryExpression(be, params.child(i), BinaryExpression.PLUS);
                    } else if (left != null) {
                        org.processj.compiler.Compiler.Info("Appending " + params.child(i).toString() + " as rhs of new binaryExpr");
                        be = new BinaryExpression(left, params.child(i), BinaryExpression.PLUS);
                    } else if (i == params.size() - 1) {
                        org.processj.compiler.Compiler.Info("Last item in params, adding as next argument");
                        rebuiltParams.append(params.child(i));
                    } else {
                        org.processj.compiler.Compiler.Info("Adding " + params.child(i).toString() + " as lhs of potential BinaryExpr");
                        left = params.child(i);
                    }
                }
            }

            return rebuiltParams;
        }

        public boolean checkForString(BinaryExpression be) {
            org.processj.compiler.Compiler.Info(be + "Checking for string concatenation versus addition");

            // Check if the lhs is a string
            if (be.getLeftExpression() instanceof PrimitiveLiteralExpression) {
                if(((PrimitiveLiteralExpression)be.getLeftExpression()).getKind() == PrimitiveLiteralExpression.StringKind) {
                    return true;
                }
            }
            // Now the rhs
            if (be.getRightExpression() instanceof PrimitiveLiteralExpression) {
                if(((PrimitiveLiteralExpression)be.getRightExpression()).getKind() == PrimitiveLiteralExpression.StringKind) {
                    return true;
                }
            }

            // recursively check on left and right
            boolean left = false;
            boolean right = false;

            if (be.getLeftExpression() instanceof BinaryExpression) {
                left = checkForString((BinaryExpression)be.getLeftExpression());
            }

            if(be.getRightExpression() instanceof BinaryExpression) {
                right = checkForString((BinaryExpression)be.getRightExpression());
            }

            return false | left | right;
        }

    }
}
