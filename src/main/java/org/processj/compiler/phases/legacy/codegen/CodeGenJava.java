package org.processj.compiler.phases.legacy.codegen;

import java.util.*;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.alt.AltCase;
import org.processj.compiler.ast.alt.AltStat;
import org.processj.compiler.ast.expression.*;
import org.processj.compiler.utilities.Assert;
import org.processj.runtime.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import org.processj.compiler.utilities.Log;
import org.processj.compiler.utilities.SymbolTable;
import org.processj.compiler.utilities.Tuple;
import org.processj.compiler.phases.phase.Visitor;

/**
 * A tree walker which collects data from an AST object and then pushes this
 * data into a template to translate a ProcessJ source file to Java code.
 * 
 * @author ben
 * @version 06/10/2018
 * @since 1.2
 */
// TODO: Generate Initialization code for Arrays of Channels (ends)
public class CodeGenJava implements Visitor<Object> {

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
     * @param s The top-level declarations which can be procedures, records,
     *          protocols, constants, and/or external types.
     */
    public CodeGenJava(SymbolTable s) {
        topLvlDecls = s;
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
        type = Assert.nonNull(type, "The parameter type cannot be null.");
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
        else if (type instanceof RecordTypeDecl)
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
     * @param co An AST that represents the entire compilation unit.
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
        Sequence<Type> typeDecls = co.getTypeDeclarations();
        // Package name for this source file
        String packagename = co.getPackageName();

        for (Import im : co.getImports()) {
            if (im != null)
                try {
                    importFiles.add((String) im.visit(this));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
        }

        for (AST decl : typeDecls) {
            if (decl instanceof Type) {
                // Collect procedures, records, protocols, external types, etc.
                String t = null;
                try {
                    t = (String) ((Type) decl).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (t != null)
                    body.add(t);
            } else if (decl instanceof ConstantDecl) {
                // Iterate over remaining declarations, which is anything that
                // comes after top-level declarations
                String cd = null;
                try {
                    cd = (String) ((ConstantDecl) decl).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (cd != null)
                    body.add(cd);
            }
        }

        stCompilation.add("pathName", packagename);
        stCompilation.add("fileName", co.fileName);
        stCompilation.add("name", sourceFile);
        stCompilation.add("body", body);
        stCompilation.add("version", JVM_RUNTIME);

        // Add all import statements to the file (if any)
        if (importFiles.size() > 0)
            stCompilation.add("imports", importFiles);

        // This will render the code for debugging
        codeGen = stCompilation.render();

        return codeGen;
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
    public final Void visitImport(final Import importStatement) {

        sourceBuilder.append("import " + importStatement.getPackageName() + ";\n");

        return null;

    }

    @Override
    public final Void visitConstantDecl(final ConstantDecl constantDeclaration) throws Phase.Error {

        sourceBuilder
                .append(" ".repeat(indent) + "public final static")
                .append(" " + constantDeclaration.getType())
                .append(" " + constantDeclaration);

        // Assert the Constant Declaration is initialized
        if(constantDeclaration.isInitialized())
            constantDeclaration.getInitializationExpression().visit(this);

        sourceBuilder.append(";\n");

        return null;

    }

    @Override
    public final Void visitProcTypeDecl(final ProcTypeDecl procedureTypeDeclaration) throws Phase.Error  {

        Log.log(procedureTypeDeclaration, "Visiting a ProcTypeDecl (" + procedureTypeDeclaration + ")");

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
            String[] body = (String[]) procedureTypeDeclaration.getBody().visit(this);

            stProcTypeDecl.add("parBlock", currentParBlock);
            stProcTypeDecl.add("syncBody", body);
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
            String procType = (String) procedureTypeDeclaration.getReturnType().visit(this);
            // Retrieve the modifier(s) attached to the invoked procedure such
            // as private, public, protected, etc.
            String[] modifiers = (String[]) procedureTypeDeclaration.modifiers().visit(this);

            // Formal parameters that must be passed to the procedure
            Sequence<ParamDecl> formals = procedureTypeDeclaration.getParameters();

            // Do we have any parameters?
            if((formals != null) && formals.size() > 0) {
                // Iterate through and visit every parameter declaration.
                // Retrieve the name and type of each parameter specified in
                // a list of comma-separated arguments. Note that we ignored
                // the value returned by this visitor
                for(int i = 0; i < formals.size(); ++i)
                    formals.child(i).visit(this);

            }

            // Visit all declarations that appear in the procedure
            String[] body = null;

            if(procedureTypeDeclaration.getBody() != null)
                body = (String[]) procedureTypeDeclaration.getBody().visit(this);

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
                stProcTypeDecl.add("type", procType);

                // Do we have any access modifier? If so, add them
                if(modifiers != null && modifiers.length > 0)
                    stProcTypeDecl.add("modifier", modifiers);
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

        return null;

    }

    @Override
    public final Void visitProtocolTypeDecl(final ProtocolTypeDecl pd) {

        ST stProtocolClass = stGroup.getInstanceOf("ProtocolClass");
        // TODO: Used to be paramForAnon
        String name = paramToVarNames.getOrDefault(pd.toString(), pd.toString());

        ArrayList<String> modifiers = new ArrayList<>();
        ArrayList<String> body      = new ArrayList<>();

        for (Modifier m : pd.modifiers())
            try {
                modifiers.add((String) m.visit(this));
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }

        currentProtocol = name;
        // We use tags to associate parent and child protocols
        if (pd.extend().size() > 0) {
            for (Name n : pd.extend()) {
                ProtocolTypeDecl ptd = (ProtocolTypeDecl) topLvlDecls.get(n.toString());
                for (ProtocolCase pc : ptd.getBody())
                    protocolNameToProtocolTag.put(String.format("%s.%s", pd, pc),
                            ptd.toString());
            }
        }

        // The scope in which all protocol members appear
        if (pd.getBody() != null)
            for (ProtocolCase pc : pd.getBody())
                try {
                    body.add((String) pc.visit(this));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }

        stProtocolClass.add("name", name);
        stProtocolClass.add("modifiers", modifiers);
        stProtocolClass.add("body", body);

        return null;
    }

    @Override
    public final Void visitRecordTypeDecl(final RecordTypeDecl recordTypeDeclaration) {

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
        for(final RecordMember recordMember: recordTypeDeclaration.getBody()) {

            // Initialize a handle to the type name
            final String typeName = (recordMember.getType() instanceof ProtocolTypeDecl)
                    ? PJProtocolCase.class.getSimpleName() : recordMember.getType().toString();
            final String name = recordMember.getName().toString();

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

        return null;

    }

    @Override
    public Object visitLocalDecl(LocalDecl ld)  {
        Log.log(ld, "Visting a LocalDecl (" + ld.getType() + " " + ld + ")");

        // We could have the following targets:
        // 1.) T x; // A declaration
        // 2.) T x = 4; // A simple declaration
        // 3.) T x = in.read(); // A single channel read
        // 4.) T x = a.read() + b.read() + ... + z.read(); // Multiple channel reads
        // 5.) T x = read(); // A Java method that returns a value
        // 6.) T x = a + b; // A binary expression
        // 7.) T x = a = b ...; // A complex assignment statement
        String name = ld.toString();
        String type = null;
        try {
            type = (String) ld.getType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        String val = null;
        boolean isConstant = ld.isConstant();

        // Is it a protocol or a record type?
        if (ld.getType() instanceof RecordTypeDecl)
            type = ld.getType().toString();
        if (ld.getType() instanceof ProtocolTypeDecl)
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
        Expression expr = ld.getInitializationExpression();
        // Visit the expressions associated with this variable
        if (expr != null) {
            if (ld.getType() instanceof PrimitiveType)
                try {
                    val = (String) expr.visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
            else if (ld.getType() instanceof RecordTypeDecl || ld.getType() instanceof ProtocolTypeDecl)
                try {
                    val = (String) expr.visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
            else if (ld.getType() instanceof ArrayType) {
                newChanArrayName = newName;
                try {
                    val = (String) expr.visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                newChanArrayName = null;
            }
        }

        // Is it a barrier declaration? If so, we must generate code
        // that creates a barrier object
        if (ld.getType().isBarrierType() && expr == null) {
            ST stBarrierDecl = stGroup.getInstanceOf("BarrierDecl");
            val = stBarrierDecl.render();
        }
        // Is it a simple declaration for a channel type? If so, and since
        // channels cannot be created using the operator 'new', we generate
        // code to create a channel object
        if (ld.getType() instanceof ChannelType && expr == null) {
            ST stChannelDecl = stGroup.getInstanceOf("ChannelDecl");
            stChannelDecl.add("type", chanType);
            val = stChannelDecl.render();
        }
        // After making this local declaration a field of the procedure
        // in which it was declared, we return iff this local variable
        // is not initialized
        if (expr == null) {
            if (!ld.getType().isBarrierType() && (ld.getType() instanceof PrimitiveType || (ld.getType() instanceof ArrayType) || // Could be an
                    // uninitialized
                    // array
                    // declaration
                    ld.getType() instanceof RecordTypeDecl || // Could be a record or protocol declaration
                    ld.getType() instanceof ProtocolTypeDecl)) // The 'null' value is used to removed empty
                return null; // sequences in the generated code
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
            return stVar.render();
        } else {
            if (inParFor) {
                localsForAnonymousProcess.remove(newName);
                localsForAnonymousProcess.put(newName + " = " + val, type);
            }
            localToFields.remove(newName);
            localToFields.put(newName + " = " + val, "final " + type);
        }

//        return stVar.render();
        return null;
    }

    @Override
    public Object visitParamDecl(ParamDecl pd)  {
        Log.log(pd, "Visiting a ParamDecl (" + pd.getType() + " " + pd + ")");

        // Grab the type and name of a variable declaration
        // TODO: Originally pd.getName().visit(this)
        String name = pd.getName().toString();

        String type = null;
        try {
            type = (String) pd.getType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

        // Silly fix for channel types
        if (pd.getType() instanceof ChannelType || pd.getType() instanceof ChannelEndType)
            type = PJChannel.class.getSimpleName() + type.substring(type.indexOf("<"), type.length());
        else if (pd.getType() instanceof RecordTypeDecl)
            type = pd.getType().toString();
        else if (pd.getType() instanceof ProtocolTypeDecl)
            type = pd.getType().toString();

        // Create a tag for this parameter and then add it to the collection
        // of parameters for reference
        String newName = makeVariableName(name, ++varDecID, Tag.PARAM_NAME);
        paramToFields.put(newName, type);
        paramToVarNames.put(name, newName);

        // Ignored the value returned by this visitor as the types and
        // variables are _always_ resolved elsewhere
        return null;
    }

    @Override
    public Object visitAltCase(AltCase ac)  {
        Log.log(ac, "Visiting an AltCase");

        ST stAltCase = stGroup.getInstanceOf("AltCase");
        Statement stat = ac.getGuard().getStatement();
//        String guard = (String) stat.visit(this);
        String guard = null;
        try {
            guard = stat instanceof TimeoutStat ? null : (String) stat.visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        String[] stats = new String[0];
        try {
            stats = (String[]) ac.getStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        // <--
        if (!indexSetOfAltCase.isEmpty()) {
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

    @Override
    public Object visitBlock(Block bl) throws Phase.Error {

        // The scope in which declarations appear, starting with their
        // own initializers and including any further declarations like
        // invocations or sequence of statements
        String[] stats = (String[]) bl.getStatements().visit(this);

        return stats;

    }

    @Override
    public Object visitBreakStat(BreakStat bs)  {
        Log.log(bs, "Visiting a BreakStat");

        ST stBreakStat = stGroup.getInstanceOf("BreakStat");
        // No parse-tree for 'break'
        if (bs.getTarget() != null)
            try {
                stBreakStat.add("name", bs.getTarget().visit(this));
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }

        return stBreakStat.render();
    }

    @Override
    public Object visitChannelWriteStat(ChannelWriteStat cw)  {
        Log.log(cw, "Visiting a ChannelWriteStat");

        ST stChanWriteStat = stGroup.getInstanceOf("ChanWriteStat");
        // 'c.write(x)' is a channel-end expression, where 'c' is the
        // writing end of the channel
        Expression chanExpr = cw.getTargetExpression();
        // 'c' is the name of the channel
        String chanWriteName = null;
        try {
            chanWriteName = (String) chanExpr.visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        // Expression sent through channel
        String expr = null;
        try {
            expr = (String) cw.getWriteExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
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

        return stChanWriteStat.render();
    }

    @Override
    public final Void visitContinueStat(ContinueStat cs) throws Phase.Error {

        Log.log(cs, "Visiting a ContinueStat");

        ST      stContinueStat  = stGroup.getInstanceOf("ContinueStat");
        String  name            = null;

        // If target isn't null, we have a label to jump to
        if (cs.getTarget() != null) {

            name = (String) cs.getTarget().visit(this);
            stContinueStat.add("name", name);

        }

        return null;

    }

    @Override
    public Object visitDoStat(DoStat ds)  {
        Log.log(ds, "Visiting a DoStat");

        ST stDoStat = stGroup.getInstanceOf("DoStat");
        String[] stats = null;
        String expr = null;

        if (ds.getEvaluationExpression() != null)
            try {
                expr = ((String) ds.getEvaluationExpression().visit(this));
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        if (ds.getStatements() != null) {
            Object o = null;
            try {
                o = ds.getStatements().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            if (o instanceof String) {
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
    public Object visitExprStat(ExprStat es)  {
        Log.log(es, "Visiting an ExprStat");

        try {
            return es.getExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public Object visitForStat(ForStat fs) throws Phase.Error {
        Log.log(fs, "Visiting a ForStat");

        ST stForStat = stGroup.getInstanceOf("ParForStat");
        String expr = null;
        ArrayList<String> init = null;
        ArrayList<String> incr = null;
        String[] stats = null;

        boolean preParFor = inParFor;
        inParFor = fs.isPar() || preParFor;

        if (fs.getInitializationExpression() != null) {
            init = new ArrayList<>();
            for (Statement st : fs.getInitializationExpression())
                try {
                    init.add(((String) st.visit(this)).replace(DELIMITER, ""));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
        }
        if (fs.getEvaluationExpression() != null)
            try {
                expr = (String) fs.getEvaluationExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        if (fs.getIncrementExpression() != null) {
            incr = new ArrayList<>();
            for (Statement st : fs.getIncrementExpression())
                try {
                    incr.add(((String) st.visit(this)).replace(DELIMITER, ""));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
        }

        if (!fs.isPar()) {
            if (fs.getStatements() != null) {
                Object o = null;
                try {
                    o = fs.getStatements().visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (o instanceof String) {
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
        currentParBlock = makeVariableName(Tag.PAR_BLOCK_NAME.toString(), ++parDecID, Tag.LOCAL_NAME);

        // Increment the jump label and add it to the switch-stmt list
        stForStat.add("jump", ++jumpLabel);
        switchCases.add(renderSwitchCase(jumpLabel));

        // Rendered the value of each statement
        ArrayList<String> stmts = new ArrayList<String>();


        barriers = new ArrayList<>();

        for(final Expression expression: fs.getBarriers())
            barriers.add((String) expression.visit(this));

        Sequence<Expression> se = fs.getBarrierExpressions();
        for (Statement st : fs.getStatements()) {
            // An expression is any valid unit of code that resolves to a value,
            // that is, it can be a combination of variables, operations and values
            // that org.processj.yield a result. An statement is a line of code that performs
            // some action, e.g. print statements, an assignment statement, etc.
            if (st instanceof ExprStat && ((ExprStat) st).getExpression() instanceof Invocation) {
                ExprStat es = (ExprStat) st;
                Invocation in = (Invocation) es.getExpression();
                // If this invocation is made on a process, then visit the
                // invocation and return a string representing the wrapper
                // class for this procedure; e.g.
                // (new <classType>(...) {
                // @Override public synchronized void run() { ... }
                // @Override public finalize() { ... }
                // }.schedule();
                if (in.targetProc.doesYield())
                    try {
                        stmts.add((String) in.visit(this));
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
                else // Otherwise, the invocation is made through a static Java method
                    try {
                        stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
            } else
                try {
                    stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
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
    public Object visitIfStat(IfStat is) throws Phase.Error {
        Log.log(is, "Visiting a IfStat");

        ST stIfStat = stGroup.getInstanceOf("IfStat");
        // Sequence of statements enclosed in a block-stmt
        String[] thenStats = null;
        String[] thenParts = null;
        String condExpr = null;
        // We either have an if-statement _or_ a loop construct that
        // has been re-written as an if-statement
        if(is.getEvaluationExpression() != null)
            try {
                condExpr = (String) is.getEvaluationExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }

        thenStats = (String[]) is.getThenStatements().visit(this);

        if(is.getElseBody() != null) {

            // TODO: Else Body is always a Sequence; Make sure we can
            // TODO: Generate s String from the statements
            thenParts = (String[]) is.getElseBody().visit(this);

        }

        stIfStat.add("expr", condExpr);
        stIfStat.add("thenPart", thenStats);
        stIfStat.add("elsePart", thenParts);

        return stIfStat.render();
    }

    @Override
    public Object visitParBlock(ParBlock pb) throws Phase.Error {

        Log.log(pb, "Visiting a ParBlock with " + pb.getStatements().size() + " statements.");

        // Don't generate code for an empty par statement
        if(pb.getStatements().size() == 0)
            return null;

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
        stParBlock.add("count", pb.getStatements().size());
        stParBlock.add("process", "this");

        // Increment the jump label and add it to the switch-stmt list
        stParBlock.add("jump", ++jumpLabel);
        switchCases.add(renderSwitchCase(jumpLabel));

        // Add the barrier this par-block enrolls in
        if(pb.getBarrierSet().size() > 0) {

            HashMap<String, Integer> parBlockBarrierSet = new HashMap<>();

            for(Expression e: pb.getBarrierSet()) {

                String name = (String) e.visit(this);
                parBlockBarrierSet.put(name, pb.getEnrolls().get(((NameExpr) e).toString()));

            }

            stParBlock.add("barrier", parBlockBarrierSet.keySet());
            stParBlock.add("enrollees", parBlockBarrierSet.values());

        }

        // Visit the sequence of statements in the par-block
        Sequence<Statement> statements = pb.getStatements();

        // Rendered the value of each statement
        ArrayList<String> stmts = new ArrayList<String>();

        for(Statement st : statements) {
            if(st == null)
                continue;

            Sequence<Expression> se = new Sequence<>();
            st.getBarrierSet().forEach(se::append);

            if(se != null) {
                barriers = new ArrayList<>();

                for(Expression e: se)
                    barriers.add((String) e.visit(this));

            }

            if(st instanceof ExprStat && ((ExprStat) st).getExpression() instanceof Invocation) {

                ExprStat    es = (ExprStat)     st;
                Invocation  in = (Invocation)   es.getExpression();
                // If this invocation is made inside a process, then visit the
                // invocation and return a string representing the wrapper
                // class for this procedure; e.g.
                // (new <classType>(...) {
                // @Override public synchronized void run() { ... }
                // @Override public finalize() { ... }
                // }.schedule();
                if(in.targetProc.doesYield()) stmts.add((String) in.visit(this));

                // Otherwise, the invocation is made through a static Java method
                else stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));

            } else stmts.add((String) createAnonymousProcTypeDecl(st).visit(this));

        }

        stParBlock.add("body", stmts);

        // Restore the par-block
        currentParBlock = prevParBlock;
        // Restore barrier expressions
        barriers = prevBarrier;

        return stParBlock.render();

    }

    @Override
    public Object visitProtocolCase(ProtocolCase pc)  {
        Log.log(pc, "Visiting a ProtocolCase (" + pc + ")");

        ST stProtocolType = stGroup.getInstanceOf("ProtocolType");
        // Since we are keeping the name of a tag as is, this (in theory)
        // shouldn't cause any name collision
        String protocName = null;
        try {
            protocName = (String) pc.getName().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        // This shouldn't create name collision problems even if we
        // use the same visitor for protocols and records
        recordMemberToField.clear();

        // The scope in which all members of this tag appeared
        for (RecordMember rm : pc.getBody())
            try {
                rm.visit(this);
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

        return stProtocolType.render();
    }

    @Override
    public Object visitReturnStat(ReturnStat rs)  {
        Log.log(rs, "Visiting a ReturnStat");

        ST stReturnStat = stGroup.getInstanceOf("ReturnStat");
        String expr = "";

        if (rs.getExpression() != null)
            try {
                expr = (String) rs.getExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }

        // This removes the extra ";" for invocation calls
        expr = expr.replace(DELIMITER, "");
        stReturnStat.add("expr", expr);

        return stReturnStat.render();
    }

    @Override
    public Object visitSwitchStat(SwitchStat st)  {
        Log.log(st, "Visiting a SwitchStat");

        ST stSwitchStat = stGroup.getInstanceOf("SwitchStat");
        // Is this a protocol tag?
        if (st.getEvaluationExpression().type instanceof ProtocolTypeDecl)
            isProtocolCase = true;

        String expr = null;
        try {
            expr = (String) st.getEvaluationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        ArrayList<String> switchGroup = new ArrayList<>();

        for (SwitchGroup sg : st.switchBlocks())
            try {
                switchGroup.add((String) sg.visit(this));
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }

        stSwitchStat.add("tag", isProtocolCase);
        stSwitchStat.add("expr", expr);
        stSwitchStat.add("block", switchGroup);

        // Reset the value for this protocol tag
        isProtocolCase = false;

        return stSwitchStat.render();
    }

    @Override
    public Object visitSwitchLabel(SwitchLabel sl)  {
        Log.log(sl, "Visiting a SwitchLabel");

        ST stSwitchLabel = stGroup.getInstanceOf("SwitchLabel");

        // This could be a default label, in which case, expr() would be null
        String label = null;
        if (!sl.isDefault())
            try {
                label = (String) sl.getExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        if (isProtocolCase) {
            // The protocol tag currently being used
            currentProtocolTag = label;
            label = String.format("\"%s\"", label);
        }

        stSwitchLabel.add("label", label);

        return stSwitchLabel.render();
    }

    @Override
    public Object visitSwitchGroup(SwitchGroup sg)  {
        Log.log(sg, "Visit a SwitchGroup");

        ST stSwitchGroup = stGroup.getInstanceOf("SwitchGroup");

        ArrayList<String> labels = new ArrayList<>();
        for (SwitchLabel sl : sg.getLabels())
            try {
                labels.add((String) sl.visit(this));
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }

        ArrayList<String> stats = new ArrayList<>();
        for (Statement st : sg.getStatements()) {
            if (st != null) {
                Object stmt = null;
                try {
                    stmt = st.visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (stmt instanceof String[])
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
    public Object visitSyncStat(SyncStat st)  {
        Log.log(st, "Visiting a SyncStat");

        ST stSyncStat = stGroup.getInstanceOf("SyncStat");
        String barrier = null;
        try {
            barrier = (String) st.barrier().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        stSyncStat.add("barrier", barrier);

        // Increment the jump label and add it to the switch-stmt list
        stSyncStat.add("resume0", ++jumpLabel);
        switchCases.add(renderSwitchCase(jumpLabel));

        return stSyncStat.render();
    }

    @Override
    public Object visitTimeoutStat(TimeoutStat ts)  {
        Log.log(ts, "Visiting a TimeoutStat");

        ST stTimeoutStat = stGroup.getInstanceOf("TimeoutStat");
        String timer = null;
        try {
            timer = (String) ts.getTimerExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        String delay = null;
        try {
            delay = (String) ts.getDelayExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

        stTimeoutStat.add("name", timer);
        stTimeoutStat.add("delay", delay);

        // Increment the jump label and add it to the switch-stmt list
        stTimeoutStat.add("resume0", ++jumpLabel);
        switchCases.add(renderSwitchCase(jumpLabel));

        return stTimeoutStat.render();
    }

    @Override
    public Object visitWhileStat(WhileStat ws) throws Phase.Error  {
        Log.log(ws, "Visiting a WhileStat");

        ST stWhileStat = stGroup.getInstanceOf("WhileStat");
        String[] stats = null;
        String expr = null;



        if(ws.getEvaluationExpression() != null)
            expr = ((String) ws.getEvaluationExpression().visit(this));

        if(ws.getStatements() != null) {
            Object o = ws.getStatements().visit(this);

            if(o instanceof String) {
                stats = new String[] { (String) o };
            } else {
                stats = (String[]) o;
            }
        }

        stWhileStat.add("expr", expr);
        stWhileStat.add("body", stats);

        return stWhileStat.render();
    }









    @Override
    public Object visitArrayAccessExpr(ArrayAccessExpr ae)  {
        Log.log(ae, "Visiting an ArrayAccessExpr");

        ST stArrayAccessExpr = stGroup.getInstanceOf("ArrayAccessExpr");
        String name = null;
        try {
            name = (String) ae.targetExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        String index = null;
        try {
            index = (String) ae.indexExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

        stArrayAccessExpr.add("name", name);
        stArrayAccessExpr.add("index", index);

        return stArrayAccessExpr.render();
    }

    @Override
    public final Void visitAssignment(Assignment as) throws Phase.Error {
        Log.log(as, "Visiting an Assignment");

        ST stVar = stGroup.getInstanceOf("Var");

        String op = as.opString();
        String lhs = null;
        String rhs = null;
        String type = null;

        if (as.left() != null) { // Not a protocol or record
            try {
                lhs = (String) as.left().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            // Unfortunately, a declaration of for an array of channel reads must be
            // of the form 'PJOne2OneChannel<?>[]...' due to the way inheritance is
            // done in Java. Thus we need need to cast - unnecessarily - the returned
            // value of a channel read expression
            if (as.left().type != null) {
                if (as.left().type instanceof RecordTypeDecl)
                    type = as.left().type.toString();
                else if (as.left().type instanceof ProtocolTypeDecl)
                    type = PJProtocolCase.class.getSimpleName();
                else
                    try {
                        type = (String) as.left().type.visit(this);
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
            }
        }

        if (as.getRight() instanceof NewArray)
            createNewArray(lhs, ((NewArray) as.getRight()));

        else if (as.getRight() instanceof ChannelReadExpr)
            createChannelReadExpr(lhs, type, op, ((ChannelReadExpr) as.getRight()));
        else if (as.getRight() != null) {
            try {
                rhs = (String) as.getRight().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            rhs = rhs.replace(DELIMITER, "");
        }

        stVar.add("name", lhs);
        stVar.add("val", rhs);
        stVar.add("op", op);

        return null;
    }

    @Override
    public final Void visitBinaryExpr(BinaryExpr be)  {
        Log.log(be, "Visiting a BinaryExpr");

        ST stBinaryExpr = stGroup.getInstanceOf("BinaryExpr");
        String op = be.opString();
        String lhs = null;
        try {
            lhs = (String) be.left().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        lhs = lhs.replace(DELIMITER, "");
        lhs = be.left().hasParens ? "(" + lhs + ")" : lhs;
        String rhs = null;
        try {
            rhs = (String) be.right().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        rhs = be.right().hasParens ? "(" + rhs + ")" : rhs;
        rhs = rhs.replace(DELIMITER, "");

        // <--
        // Silly rewrite for comparing two strings in ProcessJ using the
        // equals(Xxx) method from Java
        if ("==".equals(op) && (be.left() instanceof NameExpr && be.right() instanceof NameExpr)
                && ((((NameExpr) be.left()).myDecl instanceof LocalDecl)
                && ((NameExpr) be.right()).myDecl instanceof LocalDecl)) {
            LocalDecl ld1 = (LocalDecl) ((NameExpr) be.left()).myDecl;
            LocalDecl ld2 = (LocalDecl) ((NameExpr) be.right()).myDecl;
            if (ld1.getType().isStringType() && ld2.getType().isStringType()) {
                stBinaryExpr = stGroup.getInstanceOf("StringCompare");
                stBinaryExpr.add("str1", lhs);
                stBinaryExpr.add("str2", rhs);
                return null;
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
            Object o = topLvlDecls.get(namedType);
            if (o instanceof RecordTypeDecl) {
                stBinaryExpr = stGroup.getInstanceOf("RecordExtend");
                stBinaryExpr.add("name", lhs);
                stBinaryExpr.add("type", String.format("I_%s", rhs));
                return null;
            }

            if(namedType.equals(PJProtocolCase.class.getSimpleName())) {
                stBinaryExpr = stGroup.getInstanceOf("RecordExtend");
                stBinaryExpr.add("name", lhs);
                stBinaryExpr.add("type", currentProtocol);
                return null;
            }

        }
        // -->

        stBinaryExpr.add("lhs", lhs);
        stBinaryExpr.add("rhs", rhs);
        stBinaryExpr.add("op", op);

        return null;
    }

    @Override
    public final Void visitCastExpr(CastExpr ce) throws Phase.Error  {
        Log.log(ce, "Visiting a CastExpr");

        ST stCastExpr = stGroup.getInstanceOf("CastExpr");
        // This result in (TYPE)(EXPR)
        String type = (String) ce.getCastType().visit(this);
        String expr = (String) ce.getExpression().visit(this);

        stCastExpr.add("type", type);
        stCastExpr.add("expr", expr);

        return null;
    }

    @Override
    public final Void visitChannelEndExpr(ChannelEndExpr ce) throws Phase.Error {

        String channel = (String) ce.getChannelType().visit(this);

        return null;

    }

    @Override
    public final Void visitChannelReadExpr(ChannelReadExpr channelReadExpression) throws Phase.Error  {

        ST stChannelReadExpr = stGroup.getInstanceOf("ChannelReadExpr");

        // 'c.read()' is a channel-end expression, where 'c' is the reading
        // end of the channel
        Expression chanExpr = channelReadExpression.getExpression();
        // 'c' is the name of the channel
        String chanEndName = (String) chanExpr.visit(this);

        stChannelReadExpr.add("chanName", chanEndName);

        // One for the 'label' and one for the 'read' operation
        int countLabel = 2;

        // Add the switch block for resumption
        for(int label = 0; label < countLabel; ++label) {

            // Increment jump label and it to the switch-stmt list
            stChannelReadExpr.add("resume" + label, ++jumpLabel);
            switchCases.add(renderSwitchCase(jumpLabel));

        }

        return null;

    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final Void visitInvocation(Invocation in) throws Phase.Error  {

        // We ignore any GOTO or LABEL invocation since they are only needed
        // for the __asm__ bytecode rewrite
        if(in.ignore) {

            Log.log(in, "Visiting a " + in);

            ST stIgnore = stGroup.getInstanceOf("InvocationIgnore");

            stIgnore.add("name", paramToVarNames.getOrDefault(in.getProcedureName(), in.getProcedureName()));
            stIgnore.add("var", in.getParameters().visit(this));

            return  null;

        }

        // Check if the invocation is either a 'sync' or a 'fork'
        if(in.getProcedureName().equals("sync") || in.getProcedureName().equals("fork")) {
            Log.log(in, "Visiting Invocation (" + in + ")");
            // TODO:
        }

        ST stInvocation = null;
        // Target procedure
        ProcTypeDecl pd = in.targetProc;
        // Name of invoked procedure
        String pdName = pd.toString();

        // Check local procedures, if none is found then the procedure must
        // come from a different file and maybe package
        if(currentCompilation.fileName.equals(pd.myCompilation.fileName)) {

            String name = pdName + hashSignature(pd);

            if(pd.doesYield()) name = makeVariableName(name, 0, Tag.PROCEDURE_NAME);
            else name = makeVariableName(name, 0, Tag.METHOD_NAME);

            pdName = pd.myCompilation.fileNoExtension() + "." + name;

        } else if(pd.isNative()) {

            // Make the package visible on import by using the qualified
            // name of the class the procedure belongs to and the name of
            // the directory the procedure's class belongs to, e.g.
            // std.io.println(), where
            // 1.) 'std' is the name of the package,
            // 2.) 'io' is the name of the class/file,
            // 3.) 'println' is the method declared in the class
            pdName = pd.getPackageName() + "." + pdName;

        } else ; // TODO: This procedure is called from another package

        // These are the formal parameters of the procedure/method being invoked
        // which are specified by a list of comma-separated arguments
        Sequence<Expression> parameters = in.getParameters();
        String[] paramsList = (String[]) parameters.visit(this);

        if(paramsList != null)
            for(int i = 0; i < paramsList.length; ++i)
                paramsList[i] = paramsList[i].replace(DELIMITER, "");

        // For an invocation of a procedure that yields and one which
        // is not inside par-block, we wrap the procedure in a par-block
        if(pd.doesYield() && currentParBlock == null) {
             (new ParBlock(new Sequence(new ExprStat(in)), // Statements
                        new Sequence())) // Barriers
                        .visit(this); // Return a procedure wrapped in a par-block

            return null;

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

        return null;

    }

    @Override
    public final Void visitNameExpr(final NameExpr nameExpression) {

        this.sourceContext.addChild(new NameExpressionSourceContext(nameExpression.getName()));

        return null;

    }

    @Override
    public final Void visitNewArray(NewArray ne) throws Phase.Error {
        Log.log(ne, "Visiting a NewArray");
        createNewArray(null, ne);
        return null;
    }

    @Override
    public final Void visitRecordAccess(final RecordAccess recordAccess) throws Phase.Error {

        ST stAccessor;

        // Initialize a handle to the Target Expression's Type
        final Type      targetType = recordAccess.getTarget().getType();
        final String    name        = (String) recordAccess.getTarget().visit(this); // Reference to inner class type

        if(targetType instanceof RecordTypeDecl) {

            stAccessor = stGroup.getInstanceOf("RecordAccessor");

            String field = recordAccess.toString();

            stAccessor.add("name", name);
            stAccessor.add("member", field);

        } else if(targetType instanceof ProtocolTypeDecl) {

            stAccessor = stGroup.getInstanceOf("ProtocolAccess");

            String field = recordAccess.toString(); // Field in inner class

            ProtocolTypeDecl pt = (ProtocolTypeDecl) targetType;
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
            if(recordAccess.isArraySize) // 'Xxx.size' for N-dimensional array
                stAccessor.add("member", "length");

            else if (recordAccess.isStringLength) // 'Xxx.length' for number of characters in a string
                stAccessor.add("member", "length()");

        }

        return null;

    }





    @Override
    public final Void visitTernary(final Ternary ternary) throws Phase.Error {

        final ExpressionSourceContext evaluationExpressionSourceContext =
                new ExpressionSourceContext();

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = evaluationExpressionSourceContext;

        // Visit the Evaluation Expression
        ternary.getEvaluationExpression().visit(this);

        this.sourceContexts.peek().addChild(evaluationExpressionSourceContext);

        final ExpressionSourceContext thenSourceContext =
                new ExpressionSourceContext();

        this.sourceContext = thenSourceContext;

        // Visit the Then Expression
        ternary.thenPart().visit(this);

        this.sourceContexts.peek().addChild(thenSourceContext);

        final ExpressionSourceContext elseSourceContext =
                new ExpressionSourceContext();

        this.sourceContext = elseSourceContext;

        // Visit the else Expression
        ternary.elsePart().visit(this);

        // Restore
        this.sourceContexts.peek().addChild(elseSourceContext);
        this.sourceContext = this.sourceContexts.pop();

        this.sourceContext.addChild(new TernaryExpressionSourceContext(
                evaluationExpressionSourceContext, thenSourceContext, elseSourceContext));

        return null;

    }

    @Override
    public final Void visitUnaryPostExpr(final UnaryPostExpr unaryPostExpression) throws Phase.Error  {

        final UnaryPostExpressionSourceContext unaryPostExpressionSourceContext =
                new UnaryPostExpressionSourceContext(unaryPostExpression.opString());

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = unaryPostExpressionSourceContext;

        // Visit the Component Type
        unaryPostExpression.getExpression().visit(this);

        this.sourceContext = this.sourceContexts.pop();
        this.sourceContext.addChild(unaryPostExpressionSourceContext);

        return null;

    }

    @Override
    public final Void visitUnaryPreExpr(final UnaryPreExpr unaryPreExpression) throws Phase.Error {

        final UnaryPreExpressionSourceContext unaryPreExpressionSourceContext =
                new UnaryPreExpressionSourceContext(unaryPreExpression.opString());

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = unaryPreExpressionSourceContext;

        // Visit the Component Type
        unaryPreExpression.getExpression().visit(this);

        this.sourceContext = this.sourceContexts.pop();
        this.sourceContext.addChild(unaryPreExpressionSourceContext);

        return null;

    }

    // TODO: Right-hand side Expression
    @Override
    public final Void visitArrayLiteral(ArrayLiteral al) throws Phase.Error  {

        // Initialize a handle to the ArrayLiteralSourceContext
        final ArrayLiteralSourceContext arrayLiteralSourceContext = new ArrayLiteralSourceContext();

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = arrayLiteralSourceContext;

        // Visit the Type
        al.getType().visit(this);

        // Visit the Expressions
        al.getExpressions().visit(this);

        // Pop the previous SourceContext
        this.sourceContext = this.sourceContexts.pop();

        // Aggregate the ArrayLiteral SourceContext
        this.sourceContext.addChild(arrayLiteralSourceContext);

        return null;

    }

    // TODO: Right-hand side Expression
    @Override
    public final Void visitPrimitiveLiteral(final PrimitiveLiteral primitiveLiteral)  {

        this.sourceContext.addChild(new PrimitiveLiteralSourceContext(primitiveLiteral));

        return null;

    }

    // TODO: Right-hand side Expression
    @Override
    public final Void visitProtocolLiteral(final ProtocolLiteral protocolLiteral) throws Phase.Error {

        // Initialize a handle to the ProtocolLiteral Expression's Name & target Tag
        final Name name = protocolLiteral.getName();
        final Name tag  = protocolLiteral.getTag();

        // Initialize a handle to the ProtocolLiteralSourceContext
        final ProtocolLiteralSourceContext sourceContext = new ProtocolLiteralSourceContext(name, tag);

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = sourceContext;

        // Visit the Expressions
        protocolLiteral.getExpressions().visit(this);

        // Initialize a handle to the ProtocolType Declaration & the Protocol Case
        // TODO: Make sure that the ProtocolTypeDeclaration outputs its' defined members in-order
        // TODO: Maybe Visit Children?
        final ProtocolTypeDecl  protocolTypeDeclaration = (ProtocolTypeDecl) this.getScope().get(name.toString());
        final ProtocolCase      target                  = protocolTypeDeclaration.getCase(tag.toString());

        // Aggregate the Expressions to match RecordTypeDeclaration's order
        target.getBody().forEach(sourceContext::resolveOrder);

        // Pop the previous SourceContext
        this.sourceContext = this.sourceContexts.pop();

        // Aggregate the SourceContext to the parent
        this.sourceContext.addChild(sourceContext);

        return null;
    }

    // TODO: Right-hand side Expression
    @Override
    public final Void visitRecordLiteral(final RecordLiteral recordLiteral) throws Phase.Error  {

        // Initialize a handle to the ProtocolLiteral Expression's Name & target Tag
        final Name name = recordLiteral.getName();

        // Initialize a handle to the ProtocolLiteralSourceContext
        final RecordLiteralSourceContext recordLiteralSourceContext = new RecordLiteralSourceContext(name);

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = recordLiteralSourceContext;

        // Visit the Expressions
        recordLiteral.getRecordMemberLiterals().visit(this);

        // Initialize a handle to the ProtocolType Declaration & the Protocol Case
        // TODO: Make sure that the ProtocolTypeDeclaration outputs its' defined members in-order
        // TODO: Maybe Visit Children?
        final RecordTypeDecl recordTypeDeclaration = (RecordTypeDecl) this.getScope().get(name.toString());

        // Aggregate the Expressions to match RecordTypeDeclaration's order
        recordTypeDeclaration.getBody().forEach(recordLiteralSourceContext::resolveOrder);

        // Pop the previous SourceContext
        this.sourceContext = this.sourceContexts.pop();

        // Aggregate the SourceContext to the parent
        this.sourceContext.addChild(recordLiteralSourceContext);

        return null;

    }

    @Override
    public final Void visitRecordMemberLiteral(final RecordMemberLiteral recordMemberLiteral) throws Phase.Error {

        final RecordMemberLiteralSourceContext recordMemberLiteralSourceContext =
                new RecordMemberLiteralSourceContext(recordMemberLiteral.getName());

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = recordMemberLiteralSourceContext;

        // Visit the Expression
        recordMemberLiteral.getExpression().visit(this);

        this.sourceContext = this.sourceContexts.pop();
        this.sourceContext.addChild(recordMemberLiteralSourceContext);

        return null;

    }

    @Override
    public final Void visitArrayType(final ArrayType arrayType) throws Phase.Error {

        final ArrayTypeSourceContext arrayTypeSourceContext = new ArrayTypeSourceContext(arrayType);

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = arrayTypeSourceContext;

        // Visit the Component Type
        arrayType.getComponentType().visit(this);

        this.sourceContext = this.sourceContexts.pop();
        this.sourceContext.addChild(arrayTypeSourceContext);

        return null;

    }

    @Override
    public final Void visitChannelType(final ChannelType channelType) throws Phase.Error  {

        final ChannelTypeSourceContext channelTypeSourceContext = new ChannelTypeSourceContext(channelType);

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = channelTypeSourceContext;

        // Visit the Component Type
        channelType.getComponentType().visit(this);

        this.sourceContext = this.sourceContexts.pop();
        this.sourceContext.addChild(channelTypeSourceContext);

        return null;

    }

    @Override
    public final Void visitChannelEndType(final ChannelEndType channelEndType) throws Phase.Error {

        final ChannelEndTypeSourceContext channelEndTypeSourceContext = new ChannelEndTypeSourceContext(channelEndType);

        // Push the current Context & update the current context
        this.sourceContexts.push(this.sourceContext);
        this.sourceContext = channelEndTypeSourceContext;

        // Visit the Component Type
        channelEndType.getComponentType().visit(this);

        this.sourceContext = this.sourceContexts.pop();
        this.sourceContext.addChild(channelEndTypeSourceContext);

        return null;

    }

    @Override
    public final Void visitPrimitiveType(final PrimitiveType primitiveType) {

        this.sourceContext.addChild(new PrimitiveTypeSourceContext(primitiveType));

        return null;

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

        private PrimitiveLiteral primitiveLiteral   ;

        public PrimitiveLiteralSourceContext(final PrimitiveLiteral primitiveLiteral) {

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

        public final void resolveOrder(final RecordMember recordMember) {

            if(recordMember != null) {

                // Initialize a handle to the Expression
                final JavaSourceContext javaSourceContext =
                        this.recordMemberLiterals.get(recordMember.toString());

                // TODO: Assert the Expression was found
                this.recordMemberLiterals.remove(recordMember.toString());

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

        public final void resolveOrder(final RecordMember recordMember) {

            if(recordMember != null) {

                // Initialize a handle to the Expression
                final JavaSourceContext javaSourceContext =
                        this.recordMemberLiterals.get(recordMember.toString());

                // TODO: Assert the Expression was found
                this.recordMemberLiterals.remove(recordMember.toString());

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

            else if (arrayType.getComponentType() instanceof RecordTypeDecl)
                type = arrayType.getComponentType().toString();

            else if (arrayType.getComponentType() instanceof ProtocolTypeDecl)
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
    public Object visitSequence(Sequence se)  {
        Log.log(se, "Visiting a Sequence");

        // Sequence of statements enclosed in a block-stmt
        ArrayList<String> seqs = new ArrayList<>();
        // Iterate through every statement
        for (int i = 0; i < se.size(); ++i) {
            if (se.child(i) != null) {
                Object stats = null;
                try {
                    stats = se.child(i).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
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

        return seqs.toArray(new String[0]);
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
    public Object visitAltStat(AltStat as)  {
        Log.log(as, "Visiting an AltStat");
        
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
        if (as.isReplicated()) {
            Log.log(as, "Visiting a Dynamic or Replicated Alt");
            // This queue contains the number of cases found in the alt stmt
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            Sequence<AltCase> reAltCase = as.getStatements();
            // This section belongs to the generated code for the ForStat
            // The ST for the ForStat pertaining to an AltCase should be
            // created only for replicated Alts
            if (as.isReplicated())
                arrayOfReplicatedAltLoop.add((ST) createAltForStat(as));
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
                    ST stForStat = (ST) createAltForStat((AltStat) ((Block) ac.getStatements()).getStatements().child(0));
                    arrayOfReplicatedAltLoop.add(stForStat);
                    AltStat as2 = (AltStat) ((Block) ac.getStatements()).getStatements().child(0);
                    reAltCase = as2.getStatements();
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
                    else if (ac.getPreconditionExpression() instanceof Literal)
                        try {
                            bguard = (String) ac.getPreconditionExpression().visit(this);
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                    else {
                        // This is for expressions that evaluate to a boolean value.
                        // Such expressions become local variables (or, in our case, fields)
                        Name n = new Name("btemp");
                        LocalDecl ld = new LocalDecl(new PrimitiveType(PrimitiveType.BooleanKind),
                                new Var(n, ac.getPreconditionExpression()), false /* not constant */);
                        try {
                            listOfReplicatedAltLocals.add((String) ld.visit(this));
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                        try {
                            bguard = (String) n.visit(this);
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                    }

                    Statement stat = ac.getGuard().getStatement();
                    // TODO: What if the expression is not an array??
                    // Is it just a channel read expression?
                    if (stat instanceof ExprStat) {
                        Expression e = ((ExprStat) stat).getExpression();
                        ChannelReadExpr cr = null;
                        if (e instanceof Assignment) {
                            cr = (ChannelReadExpr) ((Assignment) e).getRight();
                            // Finally, we are in the last replicated alt, so we need to
                            // store each object guard with its correct index (or position)
                            ST stRepAltObjectGuard = stGroup.getInstanceOf("RepAltObjectGuards");
                            stRepAltObjectGuard.add("objectGuards", "repAltObjectGuards");
                            try {
                                stRepAltObjectGuard.add("objectValue", (String) cr.getExpression().visit(this));
                            } catch (Phase.Error error) {
                                throw new RuntimeException(error);
                            }
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
                    else if (stat instanceof SkipStat)
                        listOfReplicatedObjectGuards.add(String.format("%s.SKIP", PJAlt.class.getSimpleName()));
                    // Timeout statement?
                    else if (stat instanceof TimeoutStat) {
                        // Initialize the timeout statement
                        TimeoutStat ts = (TimeoutStat) stat;
//                        ST stTimeout = stGroup.getInstanceOf("TimeoutStatCase");
//                        stTimeout.add("name", ts.timer().visit(this));
//                        stTimeout.add("delay", ts.delay().visit(this));
//                        listOfReplicatedAltLocals.add(stTimeout.render());
                        try {
                            listOfReplicatedAltLocals.add((String) ts.visit(this));
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                        try {
                            listOfReplicatedObjectGuards.add((String) ts.getTimerExpression().visit(this));
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                    }
                    try {
                        listOfReplicatedAltCases.add((String) ac.visit(this));
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
                    // *************************************
                    // *************************************
                    if (!queue.isEmpty()) {
                        reAltCase = as.getStatements();
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
                new LocalDecl(new PrimitiveType(PrimitiveType.IntKind), new Var(n, null), false /* not constant */)
                        .visit(this);
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
            try {
                stReplicatedAltStat.add("index", n.visit(this));
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }

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

        Sequence<AltCase> cases = as.getStatements();
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
            else if (ac.getPreconditionExpression() instanceof Literal)
                try {
                    bguards.add((String) ac.getPreconditionExpression().visit(this));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
            else {
                // This is for expressions that evaluate to a boolean value.
                // Such expressions become local variables (or, in our case, fields)
                Name n = new Name("btemp");
                LocalDecl ld = new LocalDecl(new PrimitiveType(PrimitiveType.BooleanKind),
                        new Var(n, ac.getPreconditionExpression()), false /* not constant */);
                try {
                    blocals.add((String) ld.visit(this));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                try {
                    bguards.add((String) n.visit(this));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
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
            if (stat instanceof ExprStat) {
                Expression e = ((ExprStat) stat).getExpression();
                ChannelReadExpr cr = null;
                if (e instanceof Assignment)
                    cr = (ChannelReadExpr) ((Assignment) e).getRight();
                try {
                    guards.add((String) cr.getExpression().visit(this));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
            }
            // Skip statement?
            else if (stat instanceof SkipStat)
                guards.add(String.format("%s.SKIP", PJAlt.class.getSimpleName()));
            // Timeout statement?
            else if (stat instanceof TimeoutStat) {
                // Initialize the timeout statement
                TimeoutStat ts = (TimeoutStat) stat;
//                ST stTimeout = stGroup.getInstanceOf("TimeoutStatCase");
//                stTimeout.add("name", ts.timer().visit(this));
//                stTimeout.add("delay", ts.delay().visit(this));
//                tlocals.add(stTimeout.render());
                try {
                    tlocals.add((String) ts.visit(this));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                try {
                    guards.add((String) ts.getTimerExpression().visit(this));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
            }
            try {
                altCases.add((String) ac.visit(this));
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
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
            new LocalDecl(new PrimitiveType(PrimitiveType.IntKind), new Var(n, null), false /* not constant */).visit(this);
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
        try {
            stAltStat.add("index", n.visit(this));
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

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

    private Object createAltForStat(AltStat as)  {
        Log.log(as.line + ": Creating a ForStat for a replicated Alt");

        ST stForStat = stGroup.getInstanceOf("ForStat");
        ArrayList<String> init = null; // Initialization part
        ArrayList<String> incr = null; // Step counter
        String expr = null; // Conditional expression
        if (as.initializationStatements() != null) {
            init = new ArrayList<>();
            for (Statement st : as.initializationStatements())
                try {
                    init.add(((String) st.visit(this)).replace(DELIMITER, ""));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
            // Collect all local variables that pertain to the replicated alt
            for (String str : init)
                indexSetOfAltCase.add(str.substring(0, str.indexOf("=")).trim());
        }
        if (as.evaluationExpression() != null)
            try {
                expr = (String) as.evaluationExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        if (as.getIncrementStatements() != null) {
            incr = new ArrayList<>();
            for (Statement st : as.getIncrementStatements())
                try {
                    incr.add(((String) st.visit(this)).replace(DELIMITER, ""));
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
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
        if (t instanceof RecordTypeDecl) {
            baseType = t.toString();
        } else if (t instanceof ProtocolTypeDecl) {
            baseType = PJProtocolCase.class.getSimpleName();
        } else if (t instanceof PrimitiveType) {
            // This is needed because we can only have wrapper class
            baseType = getWrapperType(t);
        } else if (t instanceof ArrayType) {
            try {
                baseType = (String) t.visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
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
    private ProcTypeDecl createAnonymousProcTypeDecl(Statement st)  {
        return new ProcTypeDecl(new Sequence(), // Modifiers
                null, // Return type
                new Name("Anonymous"), // Procedure name
                new Sequence(), // Formal parameters
                new Sequence(), // Implement
                null, // Annotations
                new Block(new Sequence(st))); // Body
    }

    int index = 0;

    private Tuple<?> createLocalDeclForLoop(String dims)  {
        final String localDeclName = makeVariableName("loop", index++, Tag.LOCAL_NAME).replace("_ld$", "");
        Name n = new Name(localDeclName);
        NameExpr ne = new NameExpr(n);
        PrimitiveLiteral pl = new PrimitiveLiteral(new Token(0, "0", 0, 0, 0), 4 /* kind */);
        LocalDecl ld = new LocalDecl(new PrimitiveType(PrimitiveType.IntKind), new Var(n, null),
                false /* not constant */);
        try {
            ld.visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        BinaryExpr be = new BinaryExpr(ne, new NameExpr(new Name(dims)), BinaryExpr.LT);
        ExprStat es = new ExprStat(new UnaryPreExpr(ne, UnaryPreExpr.PLUSPLUS));
        Sequence<Statement> init = new Sequence<>();
        init.append(new ExprStat((Expression) new Assignment(ne, pl, Assignment.EQ)));
        Sequence<ExprStat> incr = new Sequence<>();
        incr.append(es);
        return new Tuple(init, be, incr);
    }

    @SuppressWarnings("unchecked")
    private Object createNewArray(String lhs, NewArray na) throws Phase.Error {

        ST          stNewArray  = stGroup.getInstanceOf("NewArray");
        String[]    dims        = (String[]) na.getBracketExpressions().visit(this);
        String      type        = (String) na.getComponentType().visit(this);

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

                if(e instanceof ArrayLiteral)
                    isArrayLiteral = true;

                inits.add((String) e.visit(this));

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
            stCreateArray.add("chanType", na.getComponentType().visit(this)); // this may be removed????
            stCreateArray.add("brackets", String.join("", Collections.nCopies(((ArrayType) na.type).getDepth(), "[]")));
            stCreateArray.add("dims", dims.length);

            return stCreateArray.render();

        }

        return stNewArray.render();

    }

    private Object createChannelReadExpr(String lhs, String type, String op, ChannelReadExpr cr) {
        Log.log(cr, "Creating Channel Read Expression");

        ST stChannelReadExpr = stGroup.getInstanceOf("ChannelReadExpr");
        // 'c.read()' is a channel-end expression, where 'c' is the
        // reading end of a channel
        Expression chanExpr = cr.getExpression();
        // 'c' is the name of the channel
        String chanEndName = null;
        try {
            chanEndName = (String) chanExpr.visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

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
            Object o = null;
            try {
                o = cr.getExtendedRendezvous().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            ST stBlockRV = stGroup.getInstanceOf("BlockRV");
            stBlockRV.add("block", o);
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
        NameExpr ne = new NameExpr(n);
        PrimitiveLiteral pl = new PrimitiveLiteral(new Token(0, "0", 0, 0, 0), 4 /* kind */);
        LocalDecl ld = new LocalDecl(new PrimitiveType(PrimitiveType.IntKind), new Var(n, null),
                false /* not constant */);
        try {
            ld.visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
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
        as.setReplicated(true);
        ac = new AltCase(null, null, new Block(new Sequence<>(as)));
        ac.setNestedAltStatement();
        return ac;
    }

    // Returns a string representing the signature of the wrapper
    // class or Java method that encapsulates a PJProcess
    public String hashSignature(ProcTypeDecl pd) {
        String signature = pd.getSignature();
        return String.valueOf(signature.hashCode()).replace('-', '$');
    }

}
