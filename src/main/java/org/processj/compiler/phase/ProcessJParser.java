package org.processj.compiler.phase;

import org.processj.compiler.ast.expression.result.CastExpression;
import org.processj.compiler.ast.expression.result.TernaryExpression;
import org.processj.compiler.phase.generated.Parser;
import org.processj.compiler.SourceFile;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.*;
import org.processj.compiler.ast.expression.access.ArrayAccessExpression;
import org.processj.compiler.ast.expression.access.RecordAccessExpression;
import org.processj.compiler.ast.expression.binary.*;
import org.processj.compiler.ast.expression.constructing.NewArrayExpression;
import org.processj.compiler.ast.expression.literal.ArrayLiteralExpression;
import org.processj.compiler.ast.expression.literal.LiteralExpression;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.expression.yielding.ChannelReadExpression;
import org.processj.compiler.ast.expression.result.InvocationExpression;
import org.processj.compiler.ast.expression.unary.*;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.yielding.AltCase;
import org.processj.compiler.ast.statement.yielding.AltStatement;
import org.processj.compiler.ast.statement.yielding.GuardStatement;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.declarative.LocalDeclaration;
import org.processj.compiler.ast.statement.conditional.SwitchStatement;
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.ast.statement.yielding.ParBlock;
import org.processj.compiler.ast.type.*;

/**
 * <p>Encapsulates a {@link Parser} instance in order to provide proper error handling during the parsing phase.
 * Allows for loosely-coupled dependencies between the {@link Parser} & the rest of the compiler.</p>
 * @see Parser
 * @see Phase
 * @see Parser.Handler
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public class ProcessJParser extends Phase implements Parser.Handler {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ProcessJParser} to its' default state with the specified
     * {@link Phase.Listener}.
     * @param listener The {@link Phase.Listener} that receives any {@link Phase.Message},
     * {@link Phase.Warning}, or {@link Phase.Error} messages from the {@link ProcessJParser}.
     * @since 0.1.0
     */
    public ProcessJParser(final Phase.Listener listener) {
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

        // Initialize a handle to the Compilation & Parser
        Parser parser;

        // Retrieve the ProcessJSource file
        final SourceFile sourceFile = this.getSourceFile();

        // Declare a handle to the result
        Compilation compilation = null;

        // Attempt to
        try {

            // Initialize the Lexer & Parser
            parser = new Parser(sourceFile.getCorrespondingFileReader(), this);

            // Retrieve the Compilation
            compilation = parser.getParsedCompilation();

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
            compilation.accept(this);

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
    @Override
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
    @Override
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
    @Override
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

        if(name.specifiesPackage())
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
     * <p>Inserts a {@link String}-{@link SymbolMap} pair into the {@link Compilation}'s symbol table where
     * the {@link SymbolMap} contains the different overloads of the {@link ProcedureTypeDeclaration} as long as it is not
     * qualified as mobile.</p>
     * <p>Validates the {@link ProcedureTypeDeclaration}. Verifies that:
     *      1. If the {@link Compilation} is specified as a native library, the {@link ProcedureTypeDeclaration} is declared native
     *      and does not define a body.
     *      otherwise
     *      2. If the {@link Compilation} is not specified as a native library, the {@link ProcedureTypeDeclaration} is not declared
     *      native and defines a body.</p>
     * @param procedureTypeDeclaration The {@link ProcedureTypeDeclaration} to map.
     * @throws Phase.Error If the {@link ProcedureTypeDeclaration}'s name is already defined in the {@link Compilation}'s symbol
     * table, if it overloads a mobile {@link ProcedureTypeDeclaration}, or if it is qualified as mobile and attempts to overload
     * a non-mobile {@link ProcedureTypeDeclaration}.
     * @since 0.1.0
     */
    @Override
    public final void visitProcedureTypeDeclaration(final ProcedureTypeDeclaration procedureTypeDeclaration) throws Phase.Error {

        // Assert that the Procedure's Name or Overload is not defined
        DeclarationAssert.Declares(this, procedureTypeDeclaration);

        // Assert the native ProcedureTypeDeclaration if applicable
        PragmaAssert.ValidateNativeProcedureDeclaration(this, procedureTypeDeclaration);



    }

    /**
     * <p>Defines the {@link ProtocolTypeDeclaration} in the current scope if it doesn't already exist.</p>
     * <p>Validates the {@link ProtocolTypeDeclaration}. Verifies that If the {@link Compilation} is specified as a native
     *     library, it does not declare any {@link ProtocolTypeDeclaration}.
     *
     * @param protocolTypeDeclaration The {@link ProtocolTypeDeclaration} to define.
     * @throws Phase.Error If the {@link ProcedureTypeDeclaration} already exists.
     * @since 0.1.0
     */
    @Override
    public final void visitProtocolTypeDeclaration(final ProtocolTypeDeclaration protocolTypeDeclaration) throws Phase.Error {

        // Assert that the Protocol Type's Name is not defined
        DeclarationAssert.Declares(this, protocolTypeDeclaration);

        // Assert a native library does not contain a Protocol Type Declaration
        PragmaAssert.ValidateNativeLibraryDoesNotContainProtocolDeclaration(this, protocolTypeDeclaration);

        // Resolve the Protocol Type
        protocolTypeDeclaration.getBody().accept(this);



    }

    /**
     * <p>Defines the {@link RecordTypeDeclaration} in the current scope if it doesn't already exist.</p>
     * <p>Validates the {@link RecordTypeDeclaration}. Verifies that If the {@link Compilation} is specified as a native
     *    library, it does not declare any {@link RecordTypeDeclaration}.
     *
     * @param recordTypeDeclaration The {@link RecordTypeDeclaration} to define.
     * @throws Phase.Error If the {@link RecordTypeDeclaration} already exists.
     * @since 0.1.0
     */
    @Override
    public final void visitRecordTypeDeclaration(final RecordTypeDeclaration recordTypeDeclaration) throws Phase.Error {

        // Assert that the Record's Name is not defined
        DeclarationAssert.Declares(this, recordTypeDeclaration);

        // Assert a native library does not contain a RecordTypeDeclaration
        PragmaAssert.ValidateNativeLibraryDoesNotContainRecordDeclaration(this, recordTypeDeclaration);

        // Resolve the Record Type
        recordTypeDeclaration.getBody().accept(this);



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
     * expression, is not composing an {@link AltCase}'s precondition, is not enclosed in a {@link LiteralExpression}
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
     * <p>Asserts the {@link InvocationExpression} is not composing an {@link AltCase}'s precondition.</p>
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
     * <p>Asserts that the {@link UnaryPreExpression} is not composing an {@link AltCase}'s precondition & if it
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
     * <p>Asserts that the {@link UnaryPostExpression} is not composing an {@link AltCase}'s precondition & if it
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
     * {@link AltCase}s contained by the {@link AltStatement}, checks for a single initialization {@link Expression}
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
            altStatement.evaluationExpression().accept(this);

        // Resolve the Increment Expression, if any
        if(altStatement.definesIncrementExpression())
            altStatement.evaluationExpression().accept(this);

        // Resolve the body
        altStatement.getBody().accept(this);



    }

    /**
     * <p>Defines any labels specified in the {@link AltCase} in the current scope & marks any enclosing contexts as
     * yielding.</p>
     *
     * @param altCase The {@link AltCase} to mutate.
     * @throws Phase.Error If the {@link AltCase}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitAltCase(final AltCase altCase) throws Phase.Error {

        // Assert that the Alt Case's Label is undefined
        DeclarationAssert.DefinesLabel(this, altCase.getLabel(), altCase);

        // Assert the Alt Case's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Precondition Expression
        if(altCase.definesPrecondition())
            altCase.getPreconditionExpression().accept(this);

        // Resolve the Guard, if any
        if(altCase.definesGuard())
            altCase.getGuard().accept(this);

        // Resolve the Body, if any
        altCase.getBody().accept(this);



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
     * @param guardStatement The {@link GuardStatement} to finalize
     * @throws Phase.Error If the yielding denotation failed.
     * @since 0.1.0
     */
    @Override
    public final void visitGuardStatement(final GuardStatement guardStatement) throws Phase.Error {

        // Assert the Guard Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the GuardExpression
        guardStatement.getExpression().accept(this);

        // Resolve the statement
        guardStatement.getStatement().accept(this);



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

        // Resolve the then body
        ifStatement.getThenBody().accept(this);

        // Resolve the else body
        if(ifStatement.definesElseStatement())
            ifStatement.getElseBody().accept(this);



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

}
