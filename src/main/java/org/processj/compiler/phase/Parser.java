package org.processj.compiler.phase;

import org.processj.compiler.phase.generated.ProcessJParser.CompilationUnitContext;
import org.processj.compiler.phase.generated.ProcessJParser.PragmaContext;
import org.processj.compiler.phase.generated.ProcessJParser.PackageDeclarationContext;
import org.processj.compiler.phase.generated.ProcessJParser.ImportDeclarationContext;
import org.processj.compiler.phase.generated.ProcessJParser.QualifiedNameContext;
import org.processj.compiler.phase.generated.ProcessJParser.NamesContext;
import org.processj.compiler.phase.generated.ProcessJParser.NameContext;
import org.processj.compiler.phase.generated.ProcessJParser.ModifiersContext;
import org.processj.compiler.phase.generated.ProcessJParser.AnnotationsContext;
import org.processj.compiler.phase.generated.ProcessJParser.AnnotationContext;
import org.processj.compiler.phase.generated.ProcessJParser.Annotation_valueContext;
import org.processj.compiler.phase.generated.ProcessJParser.ExtendsContext;
import org.processj.compiler.phase.generated.ProcessJParser.TypeDeclarationContext;
import org.processj.compiler.phase.generated.ProcessJParser.ProcedureTypeDeclarationContext;
import org.processj.compiler.phase.generated.ProcessJParser.ProtocolTypeDeclarationContext;
import org.processj.compiler.phase.generated.ProcessJParser.RecordTypeDeclarationContext;
import org.processj.compiler.phase.generated.ProcessJParser.RecordBodyContext;
import org.processj.compiler.phase.generated.ProcessJParser.RecordMemberContext;
import org.processj.compiler.phase.generated.ProcessJParser.ProtocolBodyContext;
import org.processj.compiler.phase.generated.ProcessJParser.ProtocolCaseContext;

import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.packages.Pragmas;
import org.processj.compiler.ast.packages.Pragma;
import org.processj.compiler.ast.packages.Import;
import org.processj.compiler.ast.packages.Imports;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Annotations;
import org.processj.compiler.ast.Annotation;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.ast.type.ProcedureType;
import org.processj.compiler.ast.type.ProtocolType;
import org.processj.compiler.ast.type.RecordType;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.*;
import org.processj.compiler.ast.expression.literal.*;
import org.processj.compiler.ast.expression.result.CastExpression;
import org.processj.compiler.ast.expression.result.TernaryExpression;
import org.processj.compiler.ast.modifier.*;
import org.processj.compiler.ast.statement.declarative.*;
import org.processj.compiler.SourceFile;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.*;
import org.processj.compiler.ast.expression.access.ArrayAccessExpression;
import org.processj.compiler.ast.expression.access.RecordAccessExpression;
import org.processj.compiler.ast.expression.binary.*;
import org.processj.compiler.ast.expression.constructing.NewArrayExpression;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.expression.yielding.ChannelReadExpression;
import org.processj.compiler.ast.expression.result.InvocationExpression;
import org.processj.compiler.ast.expression.unary.*;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.conditional.AltStatement;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.conditional.SwitchStatement;
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.ast.statement.conditional.ParBlock;
import org.processj.compiler.ast.type.*;
import org.processj.compiler.ast.type.primitive.*;
import org.processj.compiler.ast.type.primitive.numeric.DoubleType;
import org.processj.compiler.ast.type.primitive.numeric.FloatType;
import org.processj.compiler.ast.type.primitive.numeric.integral.*;
import org.processj.compiler.phase.generated.ProcessJLexer;
import org.processj.compiler.phase.generated.ProcessJParser;
import org.processj.compiler.phase.generated.ProcessJVisitor;
import org.processj.compiler.utilities.Strings;

import java.util.ArrayList;
import java.util.List;

import static org.antlr.v4.runtime.CharStreams.fromFileName;

/**
 * <p>Encapsulates a {@link Parser} instance in order to provide proper error handling during the parsing phase.
 * Allows for loosely-coupled dependencies between the {@link Parser} & the rest of the compiler.</p>
 * @see Parser
 * @see Phase
 * @see ProcessJVisitor
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public class Parser extends Phase implements ProcessJVisitor<AST> {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Parser} to its' default state with the specified
     * {@link Phase.Listener}.
     * @param listener The {@link Phase.Listener} that receives any {@link Phase.Message},
     * {@link Phase.Warning}, or {@link Phase.Error} messages from the {@link Parser}.
     * @since 0.1.0
     */
    public Parser(final Phase.Listener listener) {
        super(listener);
    }

    /// -----
    /// Phase

    /**
     * <p>Parses the specified {@link SourceFile} to produce a corresponding {@link Compilation}. This
     * method mutates the {@link Compilation} contained by the {@link SourceFile}.</p>
     * @throws Phase.Error If an error occurred during parsing.
     * @since 0.1.0
     */
    @Override
    protected final void executePhase() throws Phase.Error{

        // Retrieve the ProcessJSource file
        final SourceFile sourceFile = this.getSourceFile();

        // Declare a handle to the result
        Compilation compilation = null;

        // Attempt to
        try {

            final ProcessJLexer processJLexer = new ProcessJLexer(fromFileName(sourceFile.getPath()));
            final ProcessJParser processJParser = new ProcessJParser(new CommonTokenStream(processJLexer));
            final ProcessJParser.CompilationUnitContext compilationUnitContext = processJParser.compilationUnit();

            compilation = (Compilation) compilationUnitContext.accept(this);

            // Otherwise
        } catch(final Exception exception) {

            // Assert the Parser is valid
            ParserAssert.FileOpenFailure.Assert(this, sourceFile);

            // Initialize & throw the error
            ParserAssert.ParserFailure.Assert(this, sourceFile);

        }

        // Assert a valid Compilation
        if(compilation != null) {

            // Perform the preliminary transformation
            //compilation.accept(this);

            // Update the ProcessJSource File
            sourceFile.setCompilation(compilation);

        }

    }

    /// --------------
    /// Parser.Handler

    /**
     * <p>Callback that's invoked when the {@link Parser} encounters a syntax error.</p>
     * @param token The {@link Token} that was last received by the {@link Parser}
     * @param line The integer value of the line where the error occurred.
     * @param lineLength The integer value corresponding to the length of the current line.
     * @param lineCount The integer value of the amount of lines in the source file.
     * @since 0.1.0
     */
    public void onSyntaxError(final Token token,
                              final int line, final int lineLength, final int lineCount) {

        if(token == null) ParserAssert.UnexpectedEndOfFile.Assert(this, line);

        else ParserAssert.SyntaxErrorException.Assert(this, line, lineCount, lineLength - token.lexeme.length());

    }

    /**
     * <p>Callback that's invoked when the {@link Parser} encounters an illegal cast expression.</p>
     * @param expression The illegal {@link Expression}.
     * @param lineLength The integer value corresponding to the length of the current line.
     * @param lineCount The integer value of the amount of lines in the source file.
     * @since 0.1.0
     */
    public void onIllegalCastExpression(final Expression expression,
                                        final int lineLength, final int lineCount) {

        // Create the exception & send it off
        ParserAssert.IllegalCastExpression.Assert(this, expression.getLine(),
                lineLength - expression.getColumn(), lineCount);

    }

    /**
     * <p>Callback that's invoked when the {@link Parser} encounters a malformed package access expression.</p>
     * @param expression The illegal {@link Expression}.
     * @param lineLength The integer value corresponding to the length of the current line.
     * @param lineCount The integer value of the amount of lines in the source file.
     * @since 0.1.0
     */
    public void onMalformedPackageAccessExpression(final Expression expression,
                                                   final int lineLength, final int lineCount) {

        // Create the exception & send it off
        ParserAssert.MalformedPackageAccess.Assert(this, expression.getLine(),
                lineLength - expression.getColumn(), lineCount);

    }


    // TODO: Maybe don't do this here
    /**
     * <p>Imports the package corresponding with the {@link Import}'s package name if it's defined.</p>
     * @param importName The {@link Import} specifying a package to process.
     * @throws Phase.Error If the {@link Import} handling failed.
     * @see Import
     * @see Phase.Error
     * @since 0.1.0
     */
    @Override
    public final void visitImport(final Import importName) throws Phase.Error {

        if(!importName.isEmpty())
            ImportAssert.ImportSpecified(this, importName.toString());



    }

    // TODO: Maybe don't do this here
    /**
     * <p>Imports the package corresponding with the {@link Name}'s package name if it's defined.</p>
     *
     * @param name The {@link Name} specifying a package to process.
     * @throws Phase.Error If the {@link Import} handling failed.
     * @see Import
     * @see Phase.Error
     * @since 0.1.0
     */
    @Override
    public final void visitName(final Name name) throws Phase.Error {

        ImportAssert.ImportSpecified(this, name.getPackageName());

    }

    /// -------
    /// Visitor

    @Override
    public final void visitCompilation(final Compilation compilation) throws Phase.Error {

        // Assert the Compilation is a valid native library
        PragmaAssert.ValidateNativeLibrary(this, compilation);

        // Traverse the parse tree
        compilation.accept(this);

        // Assert the Compilation is valid after the traversal
        PragmaAssert.ValidateNativeCompilation(this, compilation);

    }

    /**
     * <p>Defines the {@link ConstantDeclaration} in the current scope, consolidates any {@link Name} & {@link ArrayType}
     * depth(s), & rewrites the {@link ConstantDeclaration}'s initialization {@link Expression} if it's specified as an
     * {@link ArrayLiteralExpression} to a {@link NewArrayExpression}.</p>
     * <p>Validates the {@link ConstantDeclaration}. Verifies that:
     *       1. If the {@link Compilation} is specified as a native library, the {@link ConstantDeclaration} is declared native
     *       and is not initialized.
     *       otherwise
     *       2. If the {@link Compilation} is not specified as a native library, the {@link ConstantDeclaration} is not declared
     *       native and is initialized.</p>
     * @param constantDeclaration The {@link ConstantDeclaration} to define, consolidate & rewrite.
     * @throws Phase.Error If the {@link ConstantDeclaration} already exists.
     * @since 0.1.0
     */
    @Override
    public final void visitConstantDeclaration(final ConstantDeclaration constantDeclaration) throws Phase.Error {

        // Assert the Constant Declaration's Name is undefined
        DeclarationAssert.Declares(this, constantDeclaration);

        // Assert the Constant Declaration's Type, Name, & initialization Expression are rewritten
        // if they're specified as ArrayType
        RewriteAssert.RewriteArrayType(constantDeclaration);

        // Validate the native Constant Declaration if applicable
        PragmaAssert.ValidateNativeConstantDeclaration(this, constantDeclaration);

        // Resolve the Type
        constantDeclaration.getType().accept(this);

        // Assert the Constant Declaration is initialized
        if(constantDeclaration.isInitialized())
            constantDeclaration.getInitializationExpression().accept(this);



    }

    /**
     * <p>Inserts a {@link String}-{@link org.processj.compiler.ast.Context.SymbolMap} pair into the
     * {@link Compilation}'s symbol table where the {@link org.processj.compiler.ast.Context.SymbolMap} contains the
     * different overloads of the {@link ProcedureType} as long as it is not qualified as mobile.</p>
     * <p>Validates the {@link ProcedureType}. Verifies that:
     *      1. If the {@link Compilation} is specified as a native library, the {@link ProcedureType} is declared native
     *      and does not define a body.
     *      otherwise
     *      2. If the {@link Compilation} is not specified as a native library, the {@link ProcedureType} is not declared
     *      native and defines a body.</p>
     * @param procedureType The {@link ProcedureType} to map.
     * @throws Phase.Error If the {@link ProcedureType}'s name is already defined in the {@link Compilation}'s symbol
     * table, if it overloads a mobile {@link ProcedureType}, or if it is qualified as mobile and attempts to overload
     * a non-mobile {@link ProcedureType}.
     * @since 0.1.0
     */
    @Override
    public final void visitProcedureTypeDeclaration(final ProcedureType procedureType) throws Phase.Error {

        // Assert that the Procedure's Name or Overload is not defined
        DeclarationAssert.Declares(this, procedureType);

        // Assert the native ProcedureTypeDeclaration if applicable
        PragmaAssert.ValidateNativeProcedureDeclaration(this, procedureType);



    }

    /**
     * <p>Defines the {@link ProtocolType} in the current scope if it doesn't already exist.</p>
     * <p>Validates the {@link ProtocolType}. Verifies that If the {@link Compilation} is specified as a native
     *     library, it does not declare any {@link ProtocolType}.
     *
     * @param protocolType The {@link ProtocolType} to define.
     * @throws Phase.Error If the {@link ProcedureType} already exists.
     * @since 0.1.0
     */
    @Override
    public final void visitProtocolTypeDeclaration(final ProtocolType protocolType) throws Phase.Error {

        // Assert that the Protocol Type's Name is not defined
        DeclarationAssert.Declares(this, protocolType);

        // Assert a native library does not contain a Protocol Type Declaration
        PragmaAssert.ValidateNativeLibraryDoesNotContainProtocolDeclaration(this, protocolType);

        // Resolve the Protocol Type
        protocolType.getBody().accept(this);



    }

    /**
     * <p>Defines the {@link RecordType} in the current scope if it doesn't already exist.</p>
     * <p>Validates the {@link RecordType}. Verifies that If the {@link Compilation} is specified as a native
     *    library, it does not declare any {@link RecordType}.
     *
     * @param recordType The {@link RecordType} to define.
     * @throws Phase.Error If the {@link RecordType} already exists.
     * @since 0.1.0
     */
    @Override
    public final void visitRecordTypeDeclaration(final RecordType recordType) throws Phase.Error {

        // Assert that the Record's Name is not defined
        DeclarationAssert.Declares(this, recordType);

        // Assert a native library does not contain a RecordTypeDeclaration
        PragmaAssert.ValidateNativeLibraryDoesNotContainRecordDeclaration(this, recordType);

        // Resolve the Record Type
        recordType.getBody().accept(this);



    }

    /**
     * <p>Asserts the {@link ArrayAccessExpression} is in any nearest enclosing {@link ParBlock}'s write set.</p>
     *
     * @param arrayAccessExpression The {@link ArrayAccessExpression} to assert.
     * @throws Phase.Error If the {@link ParBlock} write set composition assertion failed.
     * @since 0.1.0
     */
    @Override
    public final void visitArrayAccessExpression(final ArrayAccessExpression arrayAccessExpression) throws Phase.Error {

        // Assert the Array Access Expression is in any nearest enclosing Par Block's write set
        SemanticAssert.InParBlockWriteSet(this, arrayAccessExpression);

        // Resolve the target Expression
        arrayAccessExpression.getTargetExpression().accept(this);
        arrayAccessExpression.getIndexExpression().accept(this);

        // Update the ArrayAccess Expression's yield flag
        // TODO: Probably remove me/mark from the yielding expression
        if(arrayAccessExpression.getTargetExpression().doesYield()
                || arrayAccessExpression.getIndexExpression().doesYield())
            arrayAccessExpression.setYield();



    }

    /**
     * <p>Asserts the {@link AssignmentExpression} is in the nearest enclosing Par Block's Read set, is not composing a replicated
     * {@link AltStatement}'s input guard write expression, its' left-hand side is visible to the nearest enclosing par
     * for & its' left-hand side is in the nearest {@link ParBlock}'s write set.</p>
     *
     * @param assignmentExpression The {@link AssignmentExpression} to assert.
     * @throws Phase.Error If any of the assertions failed.
     * @since 0.1.0
     */
    @Override
    public final void visitAssignmentExpression(final AssignmentExpression assignmentExpression) throws Phase.Error {

        // Assert the Assignment Expression is in any enclosing Par Block's Read Set
        SemanticAssert.InParBlockReadSet(this, assignmentExpression);

        // Assert the Assignment Expression is not a write expression for a replicated Alt Input Guard
        SemanticAssert.NotReplicatedAltInputGuardWriteExpression(this, assignmentExpression);

        // Initialize a handle to the left hand side
        final Expression leftExpression = assignmentExpression.getLeftExpression();

        // Resolve the left hand Expression
        assignmentExpression.getLeftExpression().accept(this);

        // Resolve the right hand Expression
        assignmentExpression.getRightExpression().accept(this);

        // Assert the left-hand side of the Assignment Expression is visible to the Enclosing Par For
        SemanticAssert.VisibleToEnclosingParFor(this, leftExpression);

        // Assert that the left-hand side Expression is not in the ParBlock's write set
        // TODO: Probably set this from NameExpression and/or RecordAccess Expression
        if(leftExpression instanceof NameExpression || leftExpression instanceof RecordAccessExpression)
            SemanticAssert.InParBlockWriteSet(this, leftExpression);



    }

    /**
     * <p>Asserts that the {@link BinaryExpression} is in the nearest enclosing {@link ParBlock}'s read set &
     * it is not composing a replicated {@link AltStatement}'s input guard write expression.</p>
     *
     * @param binaryExpression The {@link BinaryExpression} to assert.
     * @throws Phase.Error If the visibility & composition assertions failed.
     * @since 0.1.0
     */
    @Override
    public final void visitBinaryExpression(final BinaryExpression binaryExpression) throws Phase.Error {

        // Assert the Assignment Expression is in any enclosing Par Block's Read Set
        SemanticAssert.InParBlockReadSet(this, binaryExpression);

        // Assert the Assignment Expression is not a write expression for a replicated Alt Input Guard
        SemanticAssert.NotReplicatedAltInputGuardWriteExpression(this, binaryExpression);

        // Resolve the Left Expression
        binaryExpression.getLeftExpression().accept(this);

        // Resolve the Right Expression
        binaryExpression.getRightExpression().accept(this);



    }

    @Override
    public final void visitCastExpression(final CastExpression castExpression) throws Phase.Error {

        // Initialize a handle to the Cast Expression's expression
        Expression expression = castExpression.getExpression();

        // Resolve the Expression
        expression.accept(this);



    }

    /**
     * <p>Asserts the {@link ChannelReadExpression} is not composing a replicated {@link AltStatement}'s input guard write
     * expression, is not composing an {@link AltStatement.Case}'s precondition, is not enclosed in a {@link LiteralExpression}
     * {@link Expression} & marks any enclosing contexts as yielding..</p>
     * @param channelReadExpression The {@link ChannelReadExpression} to assert
     * @throws Phase.Error If the assertion failed.
     * @since 0.1.0
     */
    @Override
    public final void visitChannelReadExpression(final ChannelReadExpression channelReadExpression) throws Phase.Error {

        // Assert the Channel Read Expression is not in a Literal Expression
        SemanticAssert.NotInLiteralExpression(this, channelReadExpression);

        // Assert the Channel Read Expression is not a write expression for a replicated Alt Input Guard
        SemanticAssert.NotReplicatedAltInputGuardWriteExpression(this, channelReadExpression);

        // Assert the Channel Read Expression is not a yielding precondition
        SemanticAssert.NonYieldingPrecondition(this, channelReadExpression);

        // Assert the Channel Read Expression's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Channel Expression
        channelReadExpression.getTargetExpression().accept(this);

        // Assert the ChannelReadExpression defines an Extended Rendezvous & resolve it
        if(channelReadExpression.definesExtendedRendezvous())
            channelReadExpression.getExtendedRendezvous().accept(this);



    }

    /**
     * <p>Asserts the {@link InvocationExpression} is not composing an {@link AltStatement.Case}'s precondition.</p>
     *
     * @param invocationExpression The {@link InvocationExpression} to check
     * @throws Phase.Error If the {@link InvocationExpression} is composing alt precondition.
     * @since 0.1.0
     */
    @Override
    public final void visitInvocationExpression(final InvocationExpression invocationExpression) throws Phase.Error  {
        // TODO: Mobiles
        // Assert the Invocation isn't composing an Alt Case's Precondition
        SemanticAssert.NotPreconditionExpression(this, invocationExpression);

        // Resolve the target Expression, if any
        invocationExpression.getTarget().accept(this);

        // Resolve the parameter expressions
        invocationExpression.getParameterExpressions().accept(this);

        // Resolve the return Type
        invocationExpression.getReturnType().accept(this);



    }

    /**
     * <p>Asserts that the {@link NameExpression} is in the nearest enclosing {@link ParBlock}'s read set &
     * it is not composing a replicated {@link AltStatement}'s input guard write expression.</p>
     * @param nameExpression The {@link NameExpression} to assert.
     * @throws Phase.Error If the visibility & composition assertions failed.
     * @since 0.1.0
     */
    @Override
    public final void visitNameExpression(final NameExpression nameExpression) throws Phase.Error {

        // Assert the Name Expression is in any enclosing Par Block's Read Set
        SemanticAssert.InParBlockReadSet(this, nameExpression);

        // Assert the Name Expression is not a write expression for a replicated Alt Input Guard
        SemanticAssert.NotReplicatedAltInputGuardWriteExpression(this, nameExpression);



    }

    /**
     * <p>Asserts the {@link RecordAccessExpression} is in the nearest enclosing {@link ParBlock}'s read set.</p>
     *
     * @param recordAccessExpression The {@link RecordAccessExpression} to assert
     * @throws Phase.Error If the {@link RecordAccessExpression} is not in the nearest enclosing {@link ParBlock}'s
     *                     read set.
     * @since 0.1.0
     */
    @Override
    public final void visitRecordAccessExpression(final RecordAccessExpression recordAccessExpression) throws Phase.Error {

        // Assert the Record Access Expression is in the nearest enclosing Par Block's read set.
        SemanticAssert.InParBlockReadSet(this, recordAccessExpression);

        // Resolve the Record
        recordAccessExpression.getTarget().accept(this);



    }

    @Override
    public final void visitTernaryExpression(final TernaryExpression ternaryExpression) throws Phase.Error {

        // Resolve the evaluation Expression
        ternaryExpression.getEvaluationExpression().accept(this);

        // Resolve the then Expression
        ternaryExpression.getThenExpression().accept(this);

        // Resolve the else Expression
        ternaryExpression.getElseExpression().accept(this);



    }

    /**
     * <p>Asserts that the {@link UnaryPreExpression} is not composing an {@link AltStatement.Case}'s precondition & if it
     * is composed of some name-bound {@link Expression}, that the name-bound {@link Expression} is visible
     * to any nearest enclosing par-for & is in any nearest enclosing {@link ParBlock}'s write set.</p>
     *
     * @param unaryPreExpression The {@link UnaryPreExpression} to assert.
     * @throws Phase.Error If the visibility & composition assertion failed.
     * @since 0.1.0
     */
    @Override
    public final void visitUnaryPreExpression(final UnaryPreExpression unaryPreExpression) throws Phase.Error {

        // Assert that the Unary Pre Expression is not an Alt Statement Pre Condition Expression
        SemanticAssert.NotPreconditionExpression(this, unaryPreExpression);

        // Initialize a handle to the Unary Pre Expression's Operator & Expression
        final int        operator   = unaryPreExpression.getOperator();
        final Expression expression = unaryPreExpression.getExpression();

        // TODO: Errors 712, 713, 714, 715; Test Symbols are complete
        // Assert that if the Unary Pre Expression is defined with an arithmetic increment or decrement operator
        // and a Name Expression, that it's visible to any immediate enclosing Par For & that the Expression is
        // not in a Par Block's Write Set
        if((operator == UnaryPreExpression.PLUSPLUS || operator == UnaryPreExpression.MINUSMINUS)
                && ((expression instanceof NameExpression)
                || (expression instanceof RecordAccessExpression)
                || (expression instanceof ArrayAccessExpression))) {

            // Assert the name-bound Expression is visible to ant nearest enclosing Par For
            SemanticAssert.VisibleToEnclosingParFor(this, expression);

            // Assert the name-bound Expression is in any nearest enclosing Par Block's Write Set
            SemanticAssert.InParBlockWriteSet(this, expression);

        }

        // Resolve the Expression
        expression.accept(this);



    }

    /**
     * <p>Asserts that the {@link UnaryPostExpression} is not composing an {@link AltStatement.Case}'s precondition & if it
     * is composed of some name-bound {@link Expression}, that the name-bound {@link Expression} is visible
     * to any nearest enclosing par-for & is in any nearest enclosing {@link ParBlock}'s write set.</p>
     *
     * @param unaryPostExpression The {@link UnaryPostExpression} to assert.
     * @throws Phase.Error If the visibility & composition assertion failed.
     * @since 0.1.0
     */
    @Override
    public final void visitUnaryPostExpression(final UnaryPostExpression unaryPostExpression) throws Phase.Error {

        // Assert that the Unary Post Expression is not an Alt Statement Pre Condition Expression
        SemanticAssert.NotPreconditionExpression(this, unaryPostExpression);

        // Initialize a handle to the Unary Pre Expression's Operator & Expression
        final Expression expression = unaryPostExpression.getExpression();
        final int        operator   = unaryPostExpression.getOperator();

        // TODO: Errors 708, 709, 710, 711; Test Symbols are complete
        // Assert that if the Unary Post Expression is defined with an arithmetic increment or decrement operator
        // and a Name Expression, that it's visible to any immediate enclosing Par For & that the Expression is
        // not in a Par Block's Write Set
        if((operator == UnaryPreExpression.PLUSPLUS || operator == UnaryPreExpression.MINUSMINUS)
                && ((expression instanceof NameExpression)
                || (expression instanceof RecordAccessExpression)
                || (expression instanceof ArrayAccessExpression))) {

            // Assert the name-bound Expression is visible to ant nearest enclosing Par For
            SemanticAssert.VisibleToEnclosingParFor(this, expression);

            // Assert the name-bound Expression is in any nearest enclosing Par Block's Write Set
            SemanticAssert.InParBlockWriteSet(this, expression);

        }

        // Resolve the Expression
        expression.accept(this);



    }

    /**
     * <p>Defines any labels specified in the {@link AltStatement} in the current scope, flattens any non-replicated
     * {@link AltStatement.Case}s contained by the {@link AltStatement}, checks for a single initialization {@link Expression}
     * if the {@link AltStatement} is replicated, checks for any enclosing {@link AltStatement}s if the {@link AltStatement} is
     * specified as pri, & marks any enclosing contexts as yielding.</p>
     * alt {
     *     x = c.read() : { x = 1; }
     * } causes issues!
     *
     * @param altStatement The {@link AltStatement} to mutate.
     * @throws Phase.Error If it was thrown by one of the {@link AltStatement}'s children.
     * @since 0.1.0
     */
    @Override
    public final void visitAltStatement(final AltStatement altStatement) throws Phase.Error {

        // Assert that the Alt Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, altStatement.getLabel(), altStatement);

        // Assert that if the Alt Statement is replicated, it specifies only one initialization expression
        SemanticAssert.SingleInitializationForReplicatedAlt(altStatement);

        // Assert that if the Alt Statement is prioritized, it is not enclosed by another Alt Statement
        SemanticAssert.PriAltNotEnclosedByAltStatement(this, altStatement);

        // Assert the Alt Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the initialization Expression, if any
        if(altStatement.definesInitializationStatements())
            altStatement.initializationStatements().accept(this);

        // Resolve the evaluation Expression, if any
        if(altStatement.definesEvaluationExpression())
            altStatement.getEvaluationExpression().accept(this);

        // Resolve the Increment Expression, if any
        if(altStatement.definesIncrementExpression())
            altStatement.getEvaluationExpression().accept(this);

        // Resolve the body
        altStatement.getBody().accept(this);

    }

    /**
     * <p>Defines any labels specified in the {@link AltStatement.Case} in the current scope & marks any enclosing contexts as
     * yielding.</p>
     *
     * @param aCase The {@link AltStatement.Case} to mutate.
     * @throws Phase.Error If the {@link AltStatement.Case}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitAltStatementCase(final AltStatement.Case aCase) throws Phase.Error {

        // Assert that the Alt Case's Label is undefined
        DeclarationAssert.DefinesLabel(this, aCase.getLabel(), aCase);

        // Assert the Alt Case's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Precondition Expression
        if(aCase.definesPrecondition())
            aCase.getPreconditionExpression().accept(this);

        // Resolve the Guard, if any
        if(aCase.definesGuard())
            aCase.getGuardStatement().accept(this);

        // Resolve the Body, if any
        aCase.getBody().accept(this);



    }

    /**
     * <p>Defines any labels specified in the {@link BlockStatement} in the current scope & asserts that the {@link BlockStatement}
     * does not contain any halting {@link Statement}s anywhere in its' body with the exception of the last
     * {@link Statement}.</p>
     *
     * @param blockStatement The {@link BlockStatement} to define its' labels & verify absence of halting {@link Statement}s.
     * @throws Phase.Error If the {@link BlockStatement}'s label is defined in the current scope or if it contains any
     *                     intermediate halting {@link Statement}s.
     * @since 0.1.0
     */
    @Override
    public final void visitBlockStatement(final BlockStatement blockStatement) throws Phase.Error {

        // Assert the Break Statement does not contain halting procedures except for the last statement
        ReachabilityAssert.DoesNotContainHaltingProcedures(this, blockStatement);

        // Assert that the Block's Label doesn't clash with any visible names
        DeclarationAssert.DefinesLabel(this, blockStatement.getLabel(), blockStatement);



    }

    /**
     * <p>Defines any labels specified in the {@link BreakStatement} in the current scope, asserts that the
     * {@link BreakStatement} is not enclosed in a parallel {@link Context} & is enclosed in a breakable
     * {@link Context}.</p>
     *
     * @param breakStatement The {@link BreakStatement} to validate.
     * @throws Phase.Error If the {@link BreakStatement}'s label is defined in the current scope or if the {@link BreakStatement}
     *                     is enclosed in a parallel or non-breakable {@link Context}.
     * @since 0.1.0
     */
    @Override
    public final void visitBreakStatement(final BreakStatement breakStatement) throws Phase.Error {

        // TODO: Check that if the Break Statement is in a loop, it's not in a Switch Statement
        // Assert the Break Statement is enclosed in a Breakable Context
        ReachabilityAssert.EnclosingIterativeContextBreaksAndReachable(this, breakStatement);

        // Assert that the Break Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, breakStatement.getLabel(), breakStatement);



    }

    /**
     * <p>Defines any labels specified in the {@link ChannelWriteStatement} in the current scope.</p>
     *
     * @param channelWriteStatement The {@link BreakStatement} to define its' labels.
     * @throws Phase.Error If the {@link BreakStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitChannelWriteStatement(final ChannelWriteStatement channelWriteStatement) throws Phase.Error {

        // Assert that the Channel Write Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, channelWriteStatement.getLabel(), channelWriteStatement);

        // Resolve the target Expression
        channelWriteStatement.getTargetExpression().accept(this);

        // Resolve the write Expression
        channelWriteStatement.getWriteExpression().accept(this);



    }

    /**
     * <p>Defines any labels specified in the {@link ClaimStatement} in the current scope & marks any enclosing
     * Contexts as yielding.</p>
     *
     * @param claimStatement The {@link ClaimStatement} to define its' labels.
     * @throws Phase.Error If the {@link ClaimStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitClaimStatement(final ClaimStatement claimStatement) throws Phase.Error {

        // Assert that the Claim Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, claimStatement.getLabel(), claimStatement);

        // Assert the Claim Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Channels List
        claimStatement.getChannels().accept(this);

        // Resolve the Statement
        claimStatement.getStatement().accept(this);



    }

    /**
     * <p>Asserts that the {@link ContinueStatement} is not enclosed in a parallel {@link Context}
     * & is enclosed in an iterative {@link Context}.</p>
     *
     * @param continueStatement The {@link ContinueStatement} to validate.
     * @throws java.lang.Error If the {@link ContinueStatement} is enclosed in a parallel or non-iterative {@link Context}.
     */
    @Override
    public final void visitContinueStatement(final ContinueStatement continueStatement) throws Phase.Error {

        // Assert the Continue Statement is enclosed in a Breakable Context
        ReachabilityAssert.EnclosingIterativeContextBreaksAndReachable(this, continueStatement);

        // Assert that the Continue Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, continueStatement.getLabel(), continueStatement);

        // TODO: Mark Loop as having ContinueStat?



    }

    /**
     * <p>Defines any labels specified in the {@link DoStatement} in the current scope.</p>
     *
     * @param doStatement The {@link DoStatement} to define its' labels.
     * @throws Phase.Error If the {@link DoStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitDoStatement(final DoStatement doStatement) throws Phase.Error {

        // Assert that the Do Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, doStatement.getLabel(), doStatement);

        // Resolve the Evaluation Expression
        doStatement.getEvaluationExpression().accept(this);

        // Resolve the body
        doStatement.getBody().accept(this);



    }

    /**
     * <p>Defines any labels specified in the {@link ExpressionStatement} in the current scope.</p>
     *
     * @param expressionStatement The {@link ExpressionStatement} to define its' labels.
     * @throws Phase.Error If the {@link ExpressionStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitExpressionStatement(final ExpressionStatement expressionStatement) throws Phase.Error {

        // Assert that the Expression Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, expressionStatement.getLabel(), expressionStatement);

        // Resolve the Expression
        expressionStatement.getExpression().accept(this);



    }

    /**
     * <p>Defines any labels specified in the {@link ForStatement} in the current scope.</p>
     *
     * @param forStatement The {@link ForStatement} to define its' labels.
     * @throws Phase.Error If the {@link ForStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitForStatement(final ForStatement forStatement) throws Phase.Error {

        // Assert that the Expression Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, forStatement.getLabel(), forStatement);

        // Resolve the evaluation expression
        forStatement.getEvaluationExpression().accept(this);

        // Resolve the initialization statements
        if(forStatement.definesInitializationStatements())
            forStatement.getInitializationStatements().accept(this);

        // Resolve the increment statements
        if(forStatement.definesIncrementStatements())
            forStatement.getIncrementStatements().accept(this);

        // Resolve the body
        forStatement.getBody().accept(this);



    }

    /**
     * <p>Marks any enclosing contexts as yielding.</p>
     *
     * @param guard The {@link AltStatement.Case.Guard} to finalize
     * @throws Phase.Error If the yielding denotation failed.
     * @since 0.1.0
     */
    @Override
    public final void visitGuardStatement(final AltStatement.Case.Guard guard) throws Phase.Error {

        // Assert the Guard Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the GuardExpression
        guard.getExpression().accept(this);

        // Resolve the statement
        guard.getStatement().accept(this);



    }

    /**
     * <p>Defines any labels specified in the {@link IfStatement} in the current scope & asserts that both branches of the
     * {@link IfStatement} are reachable.</p>
     *
     * @param ifStatement The {@link IfStatement} to validate.
     * @throws Phase.Error If the {@link IfStatement}'s label is defined in the current scope or if the one of the
     *                     {@link IfStatement}'s branches are unreachable.
     * @since 0.1.0
     */
    @Override
    public final void visitIfStatement(final IfStatement ifStatement) throws Phase.Error {

        // Assert the If Statement's branches are reachable
        ReachabilityAssert.ConditionalContextReachable(this, ifStatement);

        // Assert that the Expression Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, ifStatement.getLabel(), ifStatement);

        // Resolve the Evaluation Expression
        ifStatement.getEvaluationExpression().accept(this);

        // Resolve the body
        ifStatement.getBody().accept(this);

        // Resolve the else body
        ifStatement.getElseStatement().accept(this);

    }

    /**
     * <p>Resets the {@link Type} bound to the {@link ParameterDeclaration}. If the {@link ParameterDeclaration}'s {@link Type} &
     * {@link Name} depth combined are greater than the {@link ArrayType}'s depth, an {@link ArrayType} will be
     * constructed and the {@link ParameterDeclaration}'s {@link Type} subsequently mutated.</p>
     * @param parameterDeclaration The {@link ParameterDeclaration} to mutate.
     * @since 0.1.0
     */
    @Override
    public final void visitParameterDeclaration(final ParameterDeclaration parameterDeclaration) throws Phase.Error {

        // Assert the Parameter Declaration's Name is undefined
        DeclarationAssert.Defines(this, parameterDeclaration);

        // Assert the Parameter Declaration's Type is rewritten if it's specified as an ArrayType
        RewriteAssert.RewriteArrayType(parameterDeclaration);

        // Resolve the Parameter Declaration's Type
        parameterDeclaration.getType().accept(this);



    }

    /**
     * <p>Resets the {@link Type} bound to the {@link ConstantDeclaration}. If the {@link ConstantDeclaration}'s {@link Type} &
     * {@link Name} depth combined are greater than the {@link ArrayType}'s depth, an {@link ArrayType} will be
     * constructed and the {@link ConstantDeclaration}'s {@link Type} subsequently mutated.</p>
     *
     * @param localDeclaration The {@link ConstantDeclaration} to mutate.
     * @since 0.1.0
     */
    @Override
    public final void visitLocalDeclaration(final LocalDeclaration localDeclaration) throws Phase.Error {

        // Assert that the Local Declaration's Name is defined
        DeclarationAssert.Defines(this, localDeclaration);

        // Assert that the Local Declaration's Label is undefined
        DeclarationAssert.DefinesLabel(this, localDeclaration.getLabel(), localDeclaration);

        // Assert the Constant Declaration's Type, Name, & initialization Expression are rewritten
        // if they're specified as ArrayType
        RewriteAssert.RewriteArrayType(localDeclaration);

        // Resolve the initialization Expression
        if(localDeclaration.isInitialized())
            localDeclaration.getInitializationExpression().accept(this);



    }

    /**
     * <p>Asserts that the {@link ParBlock} is not empty & flattens any immediate child {@link ParBlock}s contained by
     * the {@link ParBlock}, defines any labels specified in the {@link ParBlock} in the current scope, & marks any
     * enclosing contexts as yielding.</p>
     *
     * @param parBlock The {@link ParBlock} to validate.
     * @throws Phase.Error If it was thrown by one of the {@link ParBlock}'s children.
     * @since 0.1.0
     */
    @Override
    public final void visitParBlockStatement(final ParBlock parBlock) throws Phase.Error {

        // Assert that the Par Block's Label is undefined
        DeclarationAssert.DefinesLabel(this, parBlock.getLabel(), parBlock);

        // Assert the Par Block is not empty
        SemanticAssert.NotEmptyParallelContext(this);

        // Assert the Par Block's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

    }

    /**
     * <p>Asserts that the {@link ReturnStatement} is not enclosed in a parallel or choice {@link Context} &
     * defines any labels specified in the {@link ForStatement} in the current scope & asserts the {@link ReturnStatement} is not
     * enclosed by an {@link AltStatement}.</p>
     *
     * @param returnStatement The {@link ReturnStatement} to finalize
     * @throws Phase.Error If the {@link ReturnStatement}'s label is defined in the current scope or
     *                     the {@link ReturnStatement} is contained in an {@link AltStatement}.
     * @since 0.1.0
     */
    @Override
    public final void visitReturnStatement(final ReturnStatement returnStatement) throws Phase.Error  {

        // Assert that the Return Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, returnStatement.getLabel(), returnStatement);
        // TODO: Mark loop as having a Return Statement?

        // Assert the Return Statement is not enclosed by an Alt Statement
        SemanticAssert.NotInAltStatement(this, returnStatement);

        // Assert the Return Statement is not enclosed in a parallel or choice Context
        ReachabilityAssert.NotEnclosedInParallelOrChoiceContext(this, returnStatement);



    }

    /**
     * <p>Defines any labels specified in the {@link SkipStatement} in the current scope.</p>
     *
     * @param skipStatement The {@link SkipStatement} to define its' labels.
     * @throws Phase.Error If the {@link SkipStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitSkipStatement(final SkipStatement skipStatement) throws Phase.Error {

        // Assert that the Skip Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, skipStatement.getLabel(), skipStatement);



    }

    /**
     * <p>Defines any labels specified in the {@link StopStatement} in the current scope.</p>
     *
     * @param stopStatement The {@link StopStatement} to define its' labels.
     * @throws Phase.Error If the {@link StopStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitStopStatement(final StopStatement stopStatement) throws Phase.Error {

        // Assert that the Stop Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, stopStatement.getLabel(), stopStatement);



    }

    /**
     * <p>Defines any labels specified in the {@link SuspendStatement} in the current scope & marks any enclosing
     * contexts as yielding.</p>
     *
     * @param suspendStatement The {@link SuspendStatement} to finalize
     * @throws Phase.Error If the yielding denotation failed.
     * @since 0.1.0
     */
    @Override
    public final void visitSuspendStatement(final SuspendStatement suspendStatement) throws Phase.Error {

        // Assert that the Suspend Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, suspendStatement.getLabel(), suspendStatement);

        // Assert the Suspend Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Parameters
        suspendStatement.getParameters().accept(this);

    }

    /**
     * <p>Defines any labels specified in the {@link SwitchStatement} in the current scope.</p>
     *
     * @param switchStatement The {@link SwitchStatement} to define its' labels.
     * @throws Phase.Error If the {@link SwitchStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitSwitchStatement(final SwitchStatement switchStatement) throws Phase.Error {

        // Assert that the Switch Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, switchStatement.getLabel(), switchStatement);

        // Resolve the Evaluation Expression
        switchStatement.getEvaluationExpression().accept(this);

        // Resolve the SwitchGroups
        switchStatement.getBody().accept(this);

    }

    /**
     * <p>Defines any labels specified in the {@link SyncStatement} in the current scope & marks any enclosing
     * contexts as yielding.</p>
     *
     * @param syncStatement The {@link SyncStatement} to finalize
     * @throws Phase.Error If the {@link SyncStatement}'s label was already defined in the scope or the
     *                     yielding denotation failed.
     * @since 0.1.0
     */
    @Override
    public final void visitSyncStatement(final SyncStatement syncStatement) throws Phase.Error {

        // Assert that the Sync Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, syncStatement.getLabel(), syncStatement);

        // Assert the Sync Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

    }

    /**
     * <p>Defines any labels specified in the {@link TimeoutStatement} in the current scope & marks any enclosing
     * contexts as yielding.</p>
     *
     * @param timeoutStatement The {@link TimeoutStatement} to finalize
     * @throws Phase.Error If the {@link TimeoutStatement}'s label was already defined in the scope or the
     *                     yielding denotation failed.
     * @since 0.1.0
     */
    @Override
    public final void visitTimeoutStatement(final TimeoutStatement timeoutStatement) throws Phase.Error {

        // Assert that the Timeout Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, timeoutStatement.getLabel(), timeoutStatement);

        // Assert the Timeout Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

    }

    /**
     * <p>Defines any labels specified in the {@link WhileStatement} in the current scope.</p>
     *
     * @param whileStatement The {@link WhileStatement} to define its' labels.
     * @throws Phase.Error If the {@link WhileStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitWhileStatement(final WhileStatement whileStatement) throws Phase.Error {

        // Assert that the While Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, whileStatement.getLabel(), whileStatement);

    }

    /// ----------------------------------------------------------------------------------------------------------- ///
    ///                                                                                                             ///
    /// ----------------------------------------------------------------------------------------------------------- ///

    /**
     * <p>Constructs a {@link Compilation} by extracting the data contained in the specified in the
     * {@link CompilationUnitContext} &amp; recurring on the {@link Context}s provided by the
     * {@link CompilationUnitContext}.</p>
     * @param compilationUnitContext The {@link CompilationUnitContext} to construct the {@link Compilation} with.
     * @return Newly constructed {@link Compilation} instance.
     * @see Compilation
     * @see CompilationUnitContext
     */
    @Override
    public final AST visitCompilationUnit(final CompilationUnitContext compilationUnitContext) {

        // Initialize the Pragmas, Imports, & Types
        final Pragmas pragmas = new Pragmas();
        final Imports imports = new Imports();
        final Types types = new Types();

        // Construct the pragmas
        compilationUnitContext.pragma().forEach(pragmaContext -> pragmas.append((Pragma) pragmaContext.accept(this)));

        // Initialize the package Name
        final Name packageName = (compilationUnitContext.packageDeclaration() != null)
                ? (Name) compilationUnitContext.packageDeclaration().accept(this)
                : new Name("");

        // Append the Imports
        compilationUnitContext.importDeclaration().forEach(importDeclarationContext ->
                imports.append((Import) importDeclarationContext.accept(this)));

        // Append the Types
        compilationUnitContext.typeDeclaration().forEach(typeDeclarationContext ->
                types.append((Type) typeDeclarationContext.accept(this)));

        // Return the result
        return new Compilation(pragmas, packageName, imports, types);

    }

    /**
     * <p>Constructs a {@link Pragma} by extracting the data contained in the specified in the
     * {@link PragmaContext}.</p>
     * @param pragmaContext The {@link PragmaContext} used to construct the {@link Pragma} with.
     * @return A Newly-constructed {@link Pragma} instance.
     * @see Compilation
     * @see CompilationUnitContext
     */
    @Override
    public final AST visitPragma(final PragmaContext pragmaContext) {

        // Return the pragma
        return new Pragma((Name) pragmaContext.name().accept(this), pragmaContext.StringLiteral() != null
                ? pragmaContext.StringLiteral().getText()
                : "");

    }

    /**
     * <p>Constructs a {@link Name} instance by recurring on the {@link Context} provided by the
     * {@link PackageDeclarationContext}.</p>
     * @param packageDeclarationContext The {@link CompilationUnitContext} to construct the {@link Compilation} with.
     * @return Newly constructed {@link Compilation} instance.
     * @see Name
     * @see PackageDeclarationContext
     */
    @Override
    public final AST visitPackageDeclaration(final PackageDeclarationContext packageDeclarationContext) {

        return packageDeclarationContext.qualifiedName().accept(this);

    }

    /**
     * <p>Constructs an {@link Import} instance by recurring on the {@link Context} provided by the
     * {@link ImportDeclarationContext}.</p>
     * @param importDeclarationContext The {@link ImportDeclarationContext} to construct the {@link Import} instance with.
     * @return Newly constructed {@link Import} instance.
     * @see Import
     * @see ImportDeclarationContext
     * @see Name
     */
    @Override
    public final AST visitImportDeclaration(final ImportDeclarationContext importDeclarationContext) {

        return new Import((Name) importDeclarationContext.qualifiedName().accept(this),
                importDeclarationContext.getText().contains(".*"));

    }

    /**
     * <p>Constructs a {@link Name} instance by extracting the data contained in the specified in the
     * {@link QualifiedNameContext}.</p>
     * @param qualifiedNameContext The {@link QualifiedNameContext} used to construct the {@link Name} instance with.
     * @return A Newly-constructed {@link Name} instance.
     * @see Name
     * @see QualifiedNameContext
     */
    @Override
    public final AST visitQualifiedName(final QualifiedNameContext qualifiedNameContext) {

        return new Name(qualifiedNameContext.Identifier().toString(), (qualifiedNameContext.qualifiedName() != null)
                ? (Name) qualifiedNameContext.qualifiedName().accept(this)
                : null);

    }

    /**
     * <p>Constructs a {@link Names} instance by extracting the data contained in the specified in the
     * {@link NamesContext}.</p>
     * @param namesContext The {@link NamesContext} used to construct the {@link Name}s instance with.
     * @return A Newly-constructed {@link Names} instance.
     * @see Names
     * @see NamesContext
     */
    @Override
    public final AST visitNames(final NamesContext namesContext) {

        return (namesContext.names() != null) ? ((Names) namesContext.names()
                .accept(this))
                .append(new Name(namesContext.Identifier().getText()))
                : new Names(new Name(namesContext.Identifier().getText()));

    }

    /**
     * <p>Constructs a {@link Name} instance by extracting the data contained in the specified in the
     * {@link NameContext}.</p>
     * @param nameContext The {@link NameContext} used to construct the {@link Name} instance with.
     * @return A Newly-constructed {@link Name} instance.
     * @see Name
     * @see NameContext
     */
    @Override
    public final AST visitName(final NameContext nameContext) {

        return new Name(nameContext.Identifier().getText());

    }

    /**
     * <p>Constructs a {@link Modifiers} instance by extracting the data contained in the specified in the
     * {@link ModifiersContext}.</p>
     * @param modifiersContext The {@link ModifiersContext} used to construct the {@link Modifiers} instance with.
     * @return A Newly-constructed {@link Modifiers} instance.
     * @see Modifiers
     * @see ModifiersContext
     */
    @Override
    public final AST visitModifiers(final ProcessJParser.ModifiersContext modifiersContext) {

        // Initialize the Modifiers
        final Modifiers modifiers = new Modifiers();

        // Aggregate the Modifiers
        modifiersContext.modifier().forEach(modifierContext -> modifiers.add((Modifier) modifierContext.accept(this)));

        // Return the result
        return modifiers;

    }

    /**
     * <p>Constructs an {@link Annotations} instance by extracting the data contained in the specified in the
     * {@link AnnotationsContext}.</p>
     * @param annotationsContext The {@link AnnotationsContext} used to construct the {@link Annotations} instance with.
     * @return A Newly-constructed {@link Annotations} instance.
     * @see Annotations
     * @see AnnotationsContext
     */
    @Override
    public final AST visitAnnotations(final AnnotationsContext annotationsContext) {

        return (annotationsContext.annotations() != null)
                ? ((Annotations) annotationsContext.annotations().accept(this))
                    .add((Annotation) annotationsContext.annotation().accept(this))
                : new Annotations((Annotation) annotationsContext.annotation().accept(this));

    }

    /**
     * <p>Constructs an {@link Annotation} instance by extracting the data contained in the specified in the
     * {@link AnnotationContext} &amp; recurring on the {@link Context} provided.</p>
     * @param annotationContext The {@link AnnotationContext} used to construct the {@link Annotation} instance with.
     * @return A Newly-constructed {@link Annotation} instance.
     * @see Annotation
     * @see AnnotationContext
     * @since 0.1.0
     */
    @Override
    public final AST visitAnnotation(final AnnotationContext annotationContext) {

        return new Annotation(annotationContext.Identifier().getText(),
                (Expression) annotationContext.annotation_value().accept(this));

    }

    /**
     * <p>Constructs a {@link LiteralExpression} instance by extracting the data contained in the specified
     * {@link Annotation_valueContext} instance.</p>
     * @param annotationValueContext The {@link AnnotationContext} used to construct the {@link Annotation} instance
     *                               with.
     * @return A Newly-constructed {@link LiteralExpression} instance.
     * @see LiteralExpression
     * @see Annotation_valueContext
     * @since 0.1.0
     */
    @Override
    public final AST visitAnnotation_value(final Annotation_valueContext annotationValueContext) {

        // Initialize the result
        LiteralExpression literalExpression = null;

        // Branch off to one of these four
        if(annotationValueContext.BooleanLiteral() != null)
            literalExpression = new BooleanLiteral(annotationValueContext.toString());
        else if(annotationValueContext.IntegerLiteral() != null)
            literalExpression = new IntegerLiteral(annotationValueContext.toString());
        else if(annotationValueContext.FloatingPointLiteral() != null)
            literalExpression = new FloatingPointLiteral(annotationValueContext.toString());
        else if(annotationValueContext.Identifier() != null)
            literalExpression = new StringLiteral(annotationValueContext.toString());

        // Return the result
        return literalExpression;

    }

    /**
     * <p>Constructs a {@link Names} instance by extracting the data contained in the specified in the
     * {@link ExtendsContext}.</p>
     * @param extendsContext The {@link ExtendsContext} used to construct the {@link Names} instance with.
     * @return A Newly-constructed {@link Names} instance.
     * @see Names
     * @see ExtendsContext
     * @since 0.1.0
     */
    @Override
    public final AST visitExtends(final ProcessJParser.ExtendsContext extendsContext) {

        return extendsContext.names().accept(this);

    }

    /**
     * <p>Constructs a {@link Type} instance by recurring on the {@link Context} provided by the
     * {@link TypeDeclarationContext}.</p>
     * @param typeDeclarationContext The {@link TypeDeclarationContext} to construct the {@link Type} instance with.
     * @return Newly constructed {@link Type} instance.
     * @see Type
     * @see TypeDeclarationContext
     */
    @Override
    public final AST visitTypeDeclaration(final TypeDeclarationContext typeDeclarationContext) {

        // Initialize the result
        AST type = null;

        // Accept one of three
        if(typeDeclarationContext.recordTypeDeclaration() != null)
            type = typeDeclarationContext.recordTypeDeclaration().accept(this);
        else if(typeDeclarationContext.procedureTypeDeclaration() != null)
            type = typeDeclarationContext.procedureTypeDeclaration().accept(this);
        else if(typeDeclarationContext.protocolTypeDeclaration() != null)
            type = typeDeclarationContext.protocolTypeDeclaration().accept(this);

        // Return the result
        return type;

    }

    /**
     * <p>Constructs a {@link ProtocolType} instance by recurring on the {@link Context} provided by the
     * {@link ProtocolTypeDeclarationContext}.</p>
     * @param protocolTypeDeclarationContext The {@link ProtocolTypeDeclarationContext} to construct the
     * {@link ProtocolType} instance with.
     * @return Newly constructed {@link ProtocolType} instance.
     * @see ProtocolType
     * @see ProtocolTypeDeclarationContext
     */
    @Override
    public final AST visitProtocolTypeDeclaration(final ProtocolTypeDeclarationContext protocolTypeDeclarationContext) {

        // Initialize the Modifiers & Protocol Name
        final Name protocolName = new Name(protocolTypeDeclarationContext.Identifier().getText());
        final Modifiers modifiers = (protocolTypeDeclarationContext.modifiers() != null)
                ? (Modifiers) protocolTypeDeclarationContext.modifiers().accept(this)
                : new Modifiers();

        // Initialize the extends Names
        final Names names = (protocolTypeDeclarationContext.extends_() != null)
                ? (Names) protocolTypeDeclarationContext.extends_().accept(this)
                : new Names();

        // Initialize the Annotations
        final Annotations annotations = (protocolTypeDeclarationContext.annotations() != null)
                ? (Annotations) protocolTypeDeclarationContext.annotations().accept(this)
                : new Annotations();

        // Initialize the body
        final BlockStatement protocolBody = (protocolTypeDeclarationContext.protocolBody() != null)
                ? (BlockStatement) protocolTypeDeclarationContext.protocolBody().accept(this)
                : new BlockStatement();

        // Return the result
        return new ProtocolType(modifiers, protocolName, names, annotations, protocolBody);

    }

    /**
     * <p>Constructs a {@link ProtocolType} instance by recurring on the {@link Context} provided by the
     * {@link ProtocolTypeDeclarationContext}.</p>
     * @param protocolBodyContext The {@link ProtocolTypeDeclarationContext} to construct the
     * {@link ProtocolType} instance with.
     * @return Newly constructed {@link ProtocolType} instance.
     * @see ProtocolType
     * @see ProtocolTypeDeclarationContext
     */
    @Override
    public final AST visitProtocolBody(final ProtocolBodyContext protocolBodyContext) {

        // Initialize the BlockStatement
        final BlockStatement blockStatement = new BlockStatement();

        // Aggregate the Protocol Cases
        protocolBodyContext.protocolCase().forEach(protocolCaseContext ->
                blockStatement.append((ProtocolType.Case) protocolCaseContext.accept(this)));

        // Return the result
        return blockStatement;
    }

    /**
     * <p>Constructs a {@link BlockStatement} containing {@link ProtocolType.Case} instances by recurring on the
     * {@link Context}s provided by the {@link ProtocolCaseContext}.</p>
     * @param protocolCaseContext The {@link ProtocolBodyContext} to construct the
     * {@link BlockStatement} containing the {@link ProtocolType.Case} instances with.
     * @return Newly constructed {@link BlockStatement} instance containing {@link ProtocolType.Case} instances.
     * @see BlockStatement
     * @see ProtocolType.Case
     * @see ProtocolCaseContext
     */
    @Override
    public final AST visitProtocolCase(final ProtocolCaseContext protocolCaseContext) {

        // Initialize the Case body
        final BlockStatement caseBody = new BlockStatement();

        // Aggregate any RecordType.Members
        if(protocolCaseContext.recordMember() != null) protocolCaseContext.recordMember()
                .forEach(recordMemberContext -> caseBody.append((RecordType.Member) recordMemberContext.accept(this)));

        // Return the result
        return new ProtocolType.Case(new Name(protocolCaseContext.Identifier().getText()), caseBody);

    }

    /**
     * <p>Constructs a {@link RecordType} instance by recurring on the {@link Context} provided by the
     * {@link RecordTypeDeclarationContext}.</p>
     * @param recordTypeDeclarationContext The {@link RecordTypeDeclarationContext} to construct the {@link RecordType}
     *                                     instance with.
     * @return Newly constructed {@link RecordType} instance.
     * @see RecordType
     * @see RecordTypeDeclarationContext
     */
    @Override
    public final AST visitRecordTypeDeclaration(final RecordTypeDeclarationContext recordTypeDeclarationContext) {

        final Name name = new Name(recordTypeDeclarationContext.Identifier().getText());

        final Modifiers modifiers = (recordTypeDeclarationContext.modifiers() != null)
                ? (Modifiers) recordTypeDeclarationContext.modifiers().accept(this)
                : new Modifiers();

        final Names extends_ = (recordTypeDeclarationContext.extends_() != null)
                ? (Names) recordTypeDeclarationContext.extends_().accept(this)
                : new Names();

        final Annotations annotations = (recordTypeDeclarationContext.annotations() != null)
                ? (Annotations) recordTypeDeclarationContext.annotations().accept(this)
                : new Annotations();

        final BlockStatement blockStatement =
                (BlockStatement) recordTypeDeclarationContext.recordBody().accept(this);

        return new RecordType(modifiers, name, extends_, annotations, blockStatement);

    }

    /**
     * <p>Constructs a {@link BlockStatement} instance by recurring on the {@link Context} provided by the
     * {@link RecordBodyContext} &amp; consolidating all of the {@link RecordType.Member} instances into a single
     * {@link BlockStatement}.</p>
     * @param recordBodyContext The {@link RecordBodyContext} to construct the {@link ProtocolType} instance with.
     * @return Newly constructed {@link BlockStatement} instance.
     * @see RecordType
     * @see RecordTypeDeclarationContext
     * @since 0.1.0
     */
    @Override
    public final AST visitRecordBody(final RecordBodyContext recordBodyContext) {

        // Initialize the BlockStatement
        final BlockStatement recordBody = new BlockStatement();

        // Aggregate the members
        recordBodyContext.recordMember()
                .forEach(memberDeclarationContext ->
                        recordBody.appendAllFrom((BlockStatement) memberDeclarationContext.accept(this)));

        // Return the result
        return recordBody;

    }

    /**
     * <p>Constructs a {@link BlockStatement} instance by constructing {@link VariableDeclaration} instances for each
     * {@link Name} specified in the declaration &amp; aggregating them into a single {@link BlockStatement}.</p>
     * @param recordMemberContext The {@link RecordBodyContext} to construct the {@link BlockStatement} instance with.
     * @return Newly constructed {@link BlockStatement} instance.
     * @see RecordType.Member
     * @see BlockStatement
     * @see RecordMemberContext
     * @since 0.1.0
     */
    @Override
    public final AST visitRecordMember(final RecordMemberContext recordMemberContext) {

        // Initialize the Type, Names, block statement, & list
        final Type type = (Type) recordMemberContext.accept(this);
        final Names names = (Names) recordMemberContext.names().accept(this);
        final BlockStatement recordMembers = new BlockStatement();
        final List<VariableDeclaration> variableDeclarations = new ArrayList<>();

        // Aggregate each name
        names.forEach(name -> recordMembers.append(
                new VariableDeclaration(new Modifiers(), type, name, null)));

        // Return the result
        return recordMembers;

    }


    @Override
    public AST visitProcedureTypeDeclaration(ProcessJParser.ProcedureTypeDeclarationContext context) {

        final Modifiers modifiers = (context.modifiers() != null)
                ? (Modifiers) context.modifiers().accept(this)
                : new Modifiers();

        final Type returnType = (Type) context.type().accept(this);

        final Name name = (Name) context.name().accept(this);

        final Parameters parameters = (context.formalParameters() != null)
                ? (Parameters) context.formalParameters().accept(this)
                : new Parameters();

        final Annotations annotations = (context.annotations() != null)
                ? (Annotations) context.annotations().accept(this)
                : new Annotations();

        final Names names = (context.names() != null)
                ? (Names) context.names().accept(this)
                : new Names();

        final BlockStatement blockStatement = (context.block() != null)
                ? (BlockStatement) context.block().accept(this)
                : new BlockStatement();

        return new ProcedureType(annotations, modifiers, returnType, name, parameters, names, blockStatement);

    }

    @Override
    public AST visitFormalParameters(ProcessJParser.FormalParametersContext context) {

        ProcessJParser.FormalParametersContext formalParametersContext = context;

        final Parameters parameters = new Parameters();

        while(formalParametersContext != null) {

            final Modifiers modifiers = (formalParametersContext.modifiers() != null)
                ? (Modifiers) formalParametersContext.modifiers().accept(this)
                : new Modifiers();

            final ProcessJParser.VariableDeclaratorContext variableDeclaratorContext
                    = formalParametersContext.variableDeclarator();

            final Type type = (Type) formalParametersContext.type().accept(this);

            final Name name = new Name(variableDeclaratorContext.Identifier().getText());

            Expression initializationExpression = null;

            if(variableDeclaratorContext.expression() != null)
                initializationExpression = (Expression) variableDeclaratorContext.expression().accept(this);

            else if(variableDeclaratorContext.arrayInitializer() != null)
                initializationExpression = (Expression) variableDeclaratorContext.expression().accept(this);

            parameters.append(new ParameterDeclaration(modifiers, type, name, initializationExpression));

            formalParametersContext = formalParametersContext.formalParameters();

        }

        return parameters;

    }

    @Override
    public AST visitType(ProcessJParser.TypeContext context) {
        return (context.typeWithoutDims() != null)
                ? context.typeWithoutDims().accept(this)
                : context.typeWithDims().accept(this);
    }

    @Override
    public AST visitTypeWithoutDims(ProcessJParser.TypeWithoutDimsContext context) {

        AST result = null;

        if(context.primitiveType() != null) result = context.primitiveType().accept(this);
        else if(context.channelType() != null) result = context.channelType().accept(this);
        else if(context.channelEndType() != null) result = context.channelEndType().accept(this);

        return result;

    }

    @Override
    public AST visitTypeWithDims(ProcessJParser.TypeWithDimsContext typeWithDimsContext) {

        return new ArrayType((Type) typeWithDimsContext.typeWithoutDims().accept(this),
                Strings.OccurrencesOf('[', typeWithDimsContext.getText()));
    }

    @Override
    public AST visitPrimitiveType(ProcessJParser.PrimitiveTypeContext context) {

        PrimitiveType result = null;

        switch (context.getText()) {
            case "boolean": result = new BooleanType(); break;
            case "char": result = new CharType(); break;
            case "byte": result = new ByteType(); break;
            case "short": result = new ShortType(); break;
            case "int": result = new IntegerType(); break;
            case "long": result = new LongType(); break;
            case "float": result = new FloatType(); break;
            case "double": result = new DoubleType(); break;
            case "string": result = new StringType(); break;
            case "barrier": result = new BarrierType(); break;
            case "timer": result = new TimerType(); break;
            case "void": result = new VoidType(); break;
        };

        return result;

    }

    @Override
    public AST visitChannelType(ProcessJParser.ChannelTypeContext context) {

        return new ChannelType((Type) context.type(),
                context.getText().contains("shared"),
                context.getText().contains("read"));

    }

    @Override
    public AST visitChannelEndType(ProcessJParser.ChannelEndTypeContext channelEndTypeContext) {

        return new ChannelEndType((Type) channelEndTypeContext.type(),
                channelEndTypeContext.getText().contains("shared"),
                channelEndTypeContext.getText().contains("read"),
                !channelEndTypeContext.getText().contains("write"));

    }

    @Override
    public AST visitModifier(final ProcessJParser.ModifierContext modifierContext) {

        final Modifier modifier = switch (modifierContext.getText()) {
            case "public" -> new Public(new Token("public"));
            case "private" -> new Private(new Token("private"));
            case "protected" -> new Protected(new Token("protected"));
            case "native" -> new Native(new Token("native"));
            case "const" -> new Constant(new Token("const"));
            case "mobile" -> new Mobile(new Token("mobile"));
            default -> null;
        };

        return modifier;

    }

    @Override
    public AST visitVariableDeclaration(ProcessJParser.VariableDeclarationContext context) {
        return null;
    }

    @Override
    public AST visitVariableDeclarators(ProcessJParser.VariableDeclaratorsContext context) {
        return null;
    }

    @Override
    public AST visitVariableDeclarator(ProcessJParser.VariableDeclaratorContext context) {

        final Name name = new Name(context.Identifier().getText());

        int depth = 0;

        for(final ProcessJParser.DimensionContext dimensionContext : context.dimension())
            depth++;

        return null;

    }

    @Override
    public AST visitDimension(ProcessJParser.DimensionContext dimensionContext) {
        return null;
    }

    @Override
    public AST visitArrayInitializer(ProcessJParser.ArrayInitializerContext context) {
        return null;
    }

    @Override
    public AST visitVariableInitializers(ProcessJParser.VariableInitializersContext context) {
        return null;
    }

    @Override
    public AST visitBlock(ProcessJParser.BlockContext context) {
        return null;
    }

    @Override
    public AST visitStatement(ProcessJParser.StatementContext context) {
        return null;
    }

    @Override
    public AST visitStatementWithoutTrailingSubstatement(ProcessJParser.StatementWithoutTrailingSubstatementContext context) {
        return null;
    }

    @Override
    public AST visitBarriers(ProcessJParser.BarriersContext context) {
        return null;
    }

    @Override
    public AST visitStatementNoShortIf(ProcessJParser.StatementNoShortIfContext context) {
        return null;
    }

    @Override
    public AST visitIfThenStatement(ProcessJParser.IfThenStatementContext context) {
        return null;
    }

    @Override
    public AST visitIfThenElseStatement(ProcessJParser.IfThenElseStatementContext context) {
        return null;
    }

    @Override
    public AST visitIfThenElseStatementNoShortIf(ProcessJParser.IfThenElseStatementNoShortIfContext context) {
        return null;
    }

    @Override
    public AST visitWhileStatement(ProcessJParser.WhileStatementContext context) {
        return null;
    }

    @Override
    public AST visitWhileStatementNoShortIf(ProcessJParser.WhileStatementNoShortIfContext context) {
        return null;
    }

    @Override
    public AST visitForStatement(ProcessJParser.ForStatementContext context) {
        return null;
    }

    @Override
    public AST visitForStatementNoShortIf(ProcessJParser.ForStatementNoShortIfContext context) {
        return null;
    }

    @Override
    public AST visitForInit(ProcessJParser.ForInitContext context) {
        return null;
    }

    @Override
    public AST visitForUpdate(ProcessJParser.ForUpdateContext context) {
        return null;
    }

    @Override
    public AST visitDoStatement(ProcessJParser.DoStatementContext context) {
        return null;
    }

    @Override
    public AST visitClaimStatement(ProcessJParser.ClaimStatementContext context) {
        return null;
    }

    @Override
    public AST visitClaimStatementNoShortIf(ProcessJParser.ClaimStatementNoShortIfContext context) {
        return null;
    }

    @Override
    public AST visitChannels_(ProcessJParser.Channels_Context context) {
        return null;
    }

    @Override
    public AST visitChannel_(ProcessJParser.Channel_Context context) {
        return null;
    }

    @Override
    public AST visitBarrierSyncStatement(ProcessJParser.BarrierSyncStatementContext context) {
        return null;
    }

    @Override
    public AST visitTimeoutStatement(ProcessJParser.TimeoutStatementContext context) {
        return null;
    }

    @Override
    public AST visitStatementExpression(ProcessJParser.StatementExpressionContext context) {
        return null;
    }

    @Override
    public AST visitLabelledStatement(ProcessJParser.LabelledStatementContext context) {
        return null;
    }

    @Override
    public AST visitSwitchStatement(ProcessJParser.SwitchStatementContext context) {
        return null;
    }

    @Override
    public AST visitSwitchBlock(ProcessJParser.SwitchBlockContext context) {
        return null;
    }

    @Override
    public AST visitSwitchBlockStatementGroup(ProcessJParser.SwitchBlockStatementGroupContext context) {
        return null;
    }

    @Override
    public AST visitAltBlock(ProcessJParser.AltBlockContext context) {
        return null;
    }

    @Override
    public AST visitAltCase(ProcessJParser.AltCaseContext context) {
        return null;
    }

    @Override
    public AST visitGuard(ProcessJParser.GuardContext context) {
        return null;
    }

    @Override
    public AST visitExpression(ProcessJParser.ExpressionContext context) {
        return null;
    }

    @Override
    public AST visitAssignmentExpression(ProcessJParser.AssignmentExpressionContext context) {
        return null;
    }

    @Override
    public AST visitConditionalExpression(ProcessJParser.ConditionalExpressionContext context) {
        return null;
    }

    @Override
    public AST visitConditionalOrExpression(ProcessJParser.ConditionalOrExpressionContext context) {
        return null;
    }

    @Override
    public AST visitConditionalAndExpression(ProcessJParser.ConditionalAndExpressionContext context) {
        return null;
    }

    @Override
    public AST visitInclusiveOrExpression(ProcessJParser.InclusiveOrExpressionContext context) {
        return null;
    }

    @Override
    public AST visitExclusiveOrExpression(ProcessJParser.ExclusiveOrExpressionContext context) {
        return null;
    }

    @Override
    public AST visitAndExpression(ProcessJParser.AndExpressionContext context) {
        return null;
    }

    @Override
    public AST visitEqualityExpression(ProcessJParser.EqualityExpressionContext context) {
        return null;
    }

    @Override
    public AST visitRelationalExpression(ProcessJParser.RelationalExpressionContext context) {
        return null;
    }

    @Override
    public AST visitShiftExpression(ProcessJParser.ShiftExpressionContext context) {
        return null;
    }

    @Override
    public AST visitAdditiveExpression(ProcessJParser.AdditiveExpressionContext context) {
        return null;
    }

    @Override
    public AST visitMultiplicativeExpression(ProcessJParser.MultiplicativeExpressionContext context) {
        return null;
    }

    @Override
    public AST visitUnaryExpression(ProcessJParser.UnaryExpressionContext context) {
        return null;
    }

    @Override
    public AST visitPreIncrementExpression(ProcessJParser.PreIncrementExpressionContext context) {
        return null;
    }

    @Override
    public AST visitPreDecrementExpression(ProcessJParser.PreDecrementExpressionContext context) {
        return null;
    }

    @Override
    public AST visitUnaryExpressionNotPlusMinus(ProcessJParser.UnaryExpressionNotPlusMinusContext context) {
        return null;
    }

    @Override
    public AST visitCastExpression(ProcessJParser.CastExpressionContext context) {
        return null;
    }

    @Override
    public AST visitPostfixExpression(ProcessJParser.PostfixExpressionContext context) {
        return null;
    }

    @Override
    public AST visitPrimaryExpression(ProcessJParser.PrimaryExpressionContext context) {
        return null;
    }

    @Override
    public AST visitPrimaryExpressionNoCreation(ProcessJParser.PrimaryExpressionNoCreationContext context) {
        return null;
    }

    @Override
    public AST visitLeftHandSideExpression(ProcessJParser.LeftHandSideExpressionContext context) {
        return null;
    }

    @Override
    public AST visitSuffix(ProcessJParser.SuffixContext context) {
        return null;
    }

    @Override
    public AST visitArrayAccessSuffix(ProcessJParser.ArrayAccessSuffixContext context) {
        return null;
    }

    @Override
    public AST visitRecordAccessSuffix(ProcessJParser.RecordAccessSuffixContext context) {
        return null;
    }

    @Override
    public AST visitChannelReadSuffix(ProcessJParser.ChannelReadSuffixContext context) {
        return null;
    }

    @Override
    public AST visitChannelWriteSuffix(ProcessJParser.ChannelWriteSuffixContext context) {
        return null;
    }

    @Override
    public AST visitInvocationSuffix(ProcessJParser.InvocationSuffixContext context) {
        return null;
    }

    @Override
    public AST visitArguments(ProcessJParser.ArgumentsContext context) {
        return null;
    }

    @Override
    public AST visitNewArrayExpression(ProcessJParser.NewArrayExpressionContext context) {
        return null;
    }

    @Override
    public AST visitDimExpression(ProcessJParser.DimExpressionContext context) {
        return null;
    }

    @Override
    public AST visitDims(ProcessJParser.DimsContext context) {
        return null;
    }

    @Override
    public AST visitNewRecordExpression(ProcessJParser.NewRecordExpressionContext context) {
        return null;
    }

    @Override
    public AST visitNewRecordExpressionArguments(ProcessJParser.NewRecordExpressionArgumentsContext context) {
        return null;
    }

    @Override
    public AST visitNewProtocolExpression(ProcessJParser.NewProtocolExpressionContext context) {
        return null;
    }

    @Override
    public AST visitNewMobileExpression(ProcessJParser.NewMobileExpressionContext context) {
        return null;
    }

    @Override
    public AST visitLiteral(ProcessJParser.LiteralContext context) {
        return null;
    }

    @Override
    public AST visitAssignmentOperator(ProcessJParser.AssignmentOperatorContext context) {
        return null;
    }

    @Override
    public AST visit(ParseTree parseTree) {
        return null;
    }

    @Override
    public AST visitChildren(RuleNode ruleNode) {
        return null;
    }

    @Override
    public AST visitTerminal(TerminalNode terminalNode) {
        return null;
    }

    @Override
    public AST visitErrorNode(ErrorNode errorNode) {
        return null;
    }

}
