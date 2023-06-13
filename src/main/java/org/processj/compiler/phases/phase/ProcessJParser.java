package org.processj.compiler.phases.phase;

import org.processj.compiler.ast.expression.result.CastExpression;
import org.processj.compiler.ast.expression.result.TernaryExpression;
import org.processj.compiler.ast.statement.switched.SwitchGroupStatement;
import org.processj.compiler.phases.phase.generated.*;
import org.processj.compiler.ProcessJSourceFile;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.*;
import org.processj.compiler.ast.expression.access.ArrayAccessExpression;
import org.processj.compiler.ast.expression.access.RecordAccessExpression;
import org.processj.compiler.ast.expression.binary.AssignmentExpression;
import org.processj.compiler.ast.expression.binary.BinaryExpression;
import org.processj.compiler.ast.expression.constructing.NewArrayExpression;
import org.processj.compiler.ast.expression.literal.ArrayLiteralExpression;
import org.processj.compiler.ast.expression.literal.LiteralExpression;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.expression.yielding.ChannelReadExpression;
import org.processj.compiler.ast.expression.result.InvocationExpression;
import org.processj.compiler.ast.expression.unary.*;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.alt.AltCase;
import org.processj.compiler.ast.statement.alt.AltStatement;
import org.processj.compiler.ast.statement.alt.GuardStatement;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.declarative.LocalDeclaration;
import org.processj.compiler.ast.statement.switched.SwitchStatement;
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
     * <p>Parses the specified {@link ProcessJSourceFile} to produce a corresponding {@link Compilation}. This
     * method mutates the {@link Compilation} contained by the {@link ProcessJSourceFile}.</p>
     * @throws Phase.Error If an error occurred during parsing.
     * @since 0.1.0
     */
    @Override
    protected final void executePhase() throws Phase.Error{

        // Initialize a handle to the Compilation & Parser
        Parser parser;

        // Retrieve the ProcessJSource file
        final ProcessJSourceFile processJSourceFile = this.getProcessJSourceFile();

        // Declare a handle to the result
        Compilation compilation = null;

        // Attempt to
        try {

            // Initialize the Lexer & Parser
            parser = new Parser(processJSourceFile.getCorrespondingFileReader(), this);

            // Retrieve the Compilation
            compilation = parser.getParsedCompilation();

            // Otherwise
        } catch(final Exception exception) {

            // Assert the Parser is valid
            ParserAssert.FileOpenFailure.Assert(this, processJSourceFile);

            // Initialize & throw the error
            ParserAssert.ParserFailure.Assert(this, processJSourceFile);

        }

        // Assert a valid Compilation
        if(compilation != null) {

            // Perform the preliminary transformation
            compilation.visit(this);

            // Update the ProcessJSource File
            processJSourceFile.setCompilation(compilation);

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

    /// -------
    /// Visitor

    @Override
    public final Void visitCompilation(final Compilation compilation) throws Phase.Error {

        // Assert a specified package name if the Compilation contains Pragmas
        if(compilation.definesPragmas() && !compilation.definesPackageName())
            throw new PragmaAssert.MissingPackageNameException(this).commit();

        // Initialize a handle to the scope
        final SymbolMap scope = this.getScope();

        // Assert the decoded pragmas are defined in this scope
        scope.setPragmasTo(PragmaAssert.DecodedPragmas(this, compilation.getPragmas()));

        // Assert the Scope defines a library Pragma
        if(scope.definesLibraryPragma()) {

            // Send an informative message
            new PragmaAssert.LibraryPragmaDetected(this).commit();

            // Validate the pragma map
            PragmaAssert.ValidatePragmaMap(this, scope.getPragmaMap());

        }

        // Traverse the parse tree
        compilation.visit(this);

        // This smells. It might not be invalid
        if(scope.isNative() && !scope.definesNativeSignatures())
            throw new PragmaAssert.InvalidNativeSignaturesException(this).commit();

        return null;

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
    public final Void visitConstantDeclaration(final ConstantDeclaration constantDeclaration)
            throws Phase.Error {

        // Assert the Constant Declaration's Name is undefined
        DeclarationAssert.Declares(this, constantDeclaration);

        // Assert the Constant Declaration's Type, Name, & initialization Expression are rewritten
        // if they're specified as ArrayType
        RewriteAssert.RewriteArrayType(constantDeclaration);

        // Initialize a handle to the Scope
        final SymbolMap scope = this.getScope();

        if(scope.isNativeLibrary() || scope.isNative())
            PragmaAssert.ValidateNativeConstantDeclaration(this, constantDeclaration, scope.isNative());

        else PragmaAssert.ValidateNonNativeConstantDeclaration(this, constantDeclaration);

        // Resolve the Type
        constantDeclaration.getType().visit(this);

        // Assert the Constant Declaration is initialized
        if(constantDeclaration.isInitialized())
            constantDeclaration.getInitializationExpression().visit(this);

        return null;

    }

    /**
     * <p>Imports the package corresponding with the {@link Import}'s package name if it's defined.</p>
     * @param importName The {@link Import} specifying a package to process.
     * @throws Phase.Error If the {@link Import} handling failed.
     * @see Import
     * @see Phase.Error
     * @since 0.1.0
     */
    @Override
    public final Void visitImport(final Import importName)
            throws Phase.Error {

        if(!importName.isEmpty())
            ImportAssert.ImportSpecified(this, importName.toString());

        return null;

    }

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
    public final Void visitName(final Name name)
            throws Phase.Error {

        if(name.specifiesPackage())
            ImportAssert.ImportSpecified(this, name.getPackageName());

        return null;

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
    public final Void visitProcedureTypeDeclaration(final ProcedureTypeDeclaration procedureTypeDeclaration)
            throws Phase.Error {

        // Assert that the Procedure's Name or Overload is not defined
        DeclarationAssert.Declares(this, procedureTypeDeclaration);

        // Initialize a handle to the Scope
        final SymbolMap scope = this.getScope();

        if(scope.isNativeLibrary() || scope.isNative()) {

            // Validate the declaration
            PragmaAssert.ValidateNativeProcedureDeclaration(this, procedureTypeDeclaration);

            // Validate the return type
            PragmaAssert.ValidateNativeProcedureReturnType(this, procedureTypeDeclaration);

            // Validate the parameter types
            PragmaAssert.ValidateNativeProcedureParameterTypes(this, procedureTypeDeclaration);

            // Finally, aggregate the Procedure's native signature
            if(scope.isNative()) {

                scope.aggregateNativeSignature(PragmaAssert.NativeTypeStringFor(procedureTypeDeclaration.getReturnType())
                        + " " + scope.getPackageName() + "_" + procedureTypeDeclaration
                        + "_" + PragmaAssert.NativeTypeListStringFor(procedureTypeDeclaration));

                // TODO: Should this cover NATIVELIB?
                procedureTypeDeclaration.setNative();
                procedureTypeDeclaration.setPackageName(scope.getPackageName());

            }

        } else {

            PragmaAssert.ValidateNonNativeProcedureDeclaration(this, procedureTypeDeclaration);

        }

        // Assert Flattened Statements; this will resolve the ProcedureTypeDeclaration's Children
        RewriteAssert.Flattened(this, procedureTypeDeclaration.getBody());

        // Update the Body with the flattened statements
        procedureTypeDeclaration.getBody().aggregate(procedureTypeDeclaration.getMergeBody());

        return null;

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
    public final Void visitProtocolTypeDeclaration(final ProtocolTypeDeclaration protocolTypeDeclaration)
            throws Phase.Error {

        // Assert that the Protocol Type's Name is not defined
        DeclarationAssert.Declares(this, protocolTypeDeclaration);

        // Initialize a handle to the Scope
        final SymbolMap scope = this.getScope();

        if(scope.isNativeLibrary() || scope.isNative())
            throw new PragmaAssert.LibraryContainsProtocolDeclarationException(this, protocolTypeDeclaration).commit();


        // Resolve the Protocol Type
        protocolTypeDeclaration.getBody().visit(this);

        return null;

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
    public final Void visitRecordTypeDeclaration(final RecordTypeDeclaration recordTypeDeclaration)
            throws Phase.Error {

        // Assert that the Record's Name is not defined
        DeclarationAssert.Declares(this, recordTypeDeclaration);

        // Initialize a handle to the Scope
        final SymbolMap scope = this.getScope();

        if(scope.isNativeLibrary() || scope.isNative())
            throw new PragmaAssert.LibraryContainsRecordDeclarationException(this, recordTypeDeclaration).commit();

        // Resolve the Record Type
        recordTypeDeclaration.getBody().visit(this);

        return null;

    }

    /**
     * <p>Asserts the {@link ArrayAccessExpression} is in any nearest enclosing {@link ParBlock}'s write set.</p>
     *
     * @param arrayAccessExpression The {@link ArrayAccessExpression} to assert.
     * @throws Phase.Error If the {@link ParBlock} write set composition assertion failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitArrayAccessExpression(final ArrayAccessExpression arrayAccessExpression)
            throws Phase.Error {

        // Assert the Array Access Expression is in any nearest enclosing Par Block's write set
        SemanticAssert.InParBlockWriteSet(this, arrayAccessExpression);

        // Initialize a handle to the target & indexing Expressions
        final Expression targetExpression = arrayAccessExpression.getTargetExpression();
        final Expression indexExpression  = arrayAccessExpression.getIndexExpression();

        // Resolve the target Expression
        targetExpression.visit(this);

        // Assert the target expression yields
        if(targetExpression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the ArrayAccessExpression

        }

        // Resolve the Index Expression
        indexExpression.visit(this);

        // Assert the index expression yields
        if(indexExpression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the ArrayAccessExpression

        }

        // Update the ArrayAccess Expression's yield flag
        if(targetExpression.doesYield() || indexExpression.doesYield())
            arrayAccessExpression.setYield();

        return null;

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
    public final Void visitAssignmentExpression(final AssignmentExpression assignmentExpression)
            throws Phase.Error {

        // Assert the Assignment Expression is in any enclosing Par Block's Read Set
        SemanticAssert.InParBlockReadSet(this, assignmentExpression);

        // Assert the Assignment Expression is not a write expression for a replicated Alt Input Guard
        SemanticAssert.NotReplicatedAltInputGuardWriteExpression(this, assignmentExpression);

        // Initialize a handle to the left hand side
        final Expression leftExpression = assignmentExpression.getLeftExpression();

        // Resolve the left hand Expression
        leftExpression.visit(this);

        // Resolve the right hand Expression
        assignmentExpression.getRightExpression().visit(this);

        // Assert the left-hand side of the Assignment Expression is visible to the Enclosing Par For
        SemanticAssert.VisibleToEnclosingParFor(this, leftExpression);

        // Assert that the left-hand side Expression is not in the ParBlock's write set
        if(leftExpression instanceof NameExpression || leftExpression instanceof RecordAccessExpression)
            SemanticAssert.InParBlockWriteSet(this, leftExpression);

        return null;

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
    public final Void visitBinaryExpression(final BinaryExpression binaryExpression)
            throws Phase.Error {

        // Assert the Assignment Expression is in any enclosing Par Block's Read Set
        SemanticAssert.InParBlockReadSet(this, binaryExpression);

        // Assert the Assignment Expression is not a write expression for a replicated Alt Input Guard
        SemanticAssert.NotReplicatedAltInputGuardWriteExpression(this, binaryExpression);

        // Initialize a handle to the Left & Right Expressions
        Expression leftExpression  = binaryExpression.getLeft()    ;
        Expression rightExpression = binaryExpression.getRight()   ;

        // Resolve the Left Expression
        leftExpression.visit(this);

        // Assert the Left Expression Yields
        if(leftExpression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the BinaryExpression

        }

        // Resolve the Right Expression
        rightExpression.visit(this);

        // Assert the Right Expression Yields
        if(rightExpression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the BinaryExpression

        }

        return null;

    }

    @Override
    public final Void visitCastExpression(final CastExpression castExpression)
            throws Phase.Error {

        // Initialize a handle to the Cast Expression's expression
        Expression expression = castExpression.getExpression();

        // Resolve the Expression
        expression.visit(this);

        // Assert the Expression Yields
        if(expression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the CastExpression

        }

        return null;

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
    public final Void visitChannelReadExpression(final ChannelReadExpression channelReadExpression)
            throws Phase.Error {

        // Assert the Channel Read Expression is not a write expression for a replicated Alt Input Guard
        SemanticAssert.NotReplicatedAltInputGuardWriteExpression(this, channelReadExpression);

        // Assert the Channel Read Expression is not a yielding precondition
        SemanticAssert.NonYieldingPrecondition(this, channelReadExpression);

        // Assert the Channel Read Expression is not in a Literal Expression
        SemanticAssert.NotInLiteralExpression(this, channelReadExpression);

        // Assert the Channel Read Expression's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Channel Expression
        channelReadExpression.getChannelExpression().visit(this);

        // Initialize a handle to the enclosing merge body
        final BlockStatement mergeBody = this.getContext().getMergeBody();

        // Assert unrolled
        RewriteAssert.YieldedUnrolledInto(mergeBody, channelReadExpression);

        // Assert the ChannelReadExpression defines an Extended Rendezvous & resolve it
        // TODO: Check if we need to unroll extended rendezvous
        if(channelReadExpression.definesExtendedRendezvous())
            channelReadExpression.getExtendedRendezvous().visit(this);

        return null;

    }

    /**
     * <p>Asserts the {@link InvocationExpression} is not composing an {@link AltCase}'s precondition.</p>
     *
     * @param invocationExpression The {@link InvocationExpression} to check
     * @throws Phase.Error If the {@link InvocationExpression} is composing alt precondition.
     * @since 0.1.0
     */
    @Override
    public final Void visitInvocationExpression(final InvocationExpression invocationExpression)
            throws Phase.Error  {
        // TODO: Mobiles
        // Assert the Invocation isn't composing an Alt Case's Precondition
        SemanticAssert.NotPreconditionExpression(this, invocationExpression);

        // Resolve the return Type
        invocationExpression.getReturnType().visit(this);

        // Initialize a handle to the enclosing Context's Merge Body & the Parameter Expressions
        final Sequence<Expression> parameters = invocationExpression.getParameters();

        // Resolve the Parameters
        for(int index = 0; index < parameters.size(); index++) {

            // Initialize a handle to the parameter Expression
            final Expression parameterExpression = parameters.child(index);

            // Resolve the Parameter Expression
            parameterExpression.visit(this);

            // Assert the Expression Yields
            if(parameterExpression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the InvocationExpression's
                // TODO: Parameter Expressions

            }

        }

        // Resolve the target Expression, if any
        invocationExpression.getTarget().visit(this);

        return null;

    }

    /**
     * <p>Asserts that the {@link NameExpression} is in the nearest enclosing {@link ParBlock}'s read set &
     * it is not composing a replicated {@link AltStatement}'s input guard write expression.</p>
     * @param nameExpression The {@link NameExpression} to assert.
     * @throws Phase.Error If the visibility & composition assertions failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitNameExpression(final NameExpression nameExpression)
            throws Phase.Error {

        // Assert the Name Expression is in any enclosing Par Block's Read Set
        SemanticAssert.InParBlockReadSet(this, nameExpression);

        // Assert the Name Expression is not a write expression for a replicated Alt Input Guard
        SemanticAssert.NotReplicatedAltInputGuardWriteExpression(this, nameExpression);

        return null;

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
    public final Void visitRecordAccessExpression(final RecordAccessExpression recordAccessExpression)
            throws Phase.Error {

        // Assert the Record Access Expression is in the nearest enclosing Par Block's read set.
        SemanticAssert.InParBlockReadSet(this, recordAccessExpression);

        // Resolve the Record
        recordAccessExpression.getTarget().visit(this);

        return null;

    }

    @Override
    public final Void visitTernaryExpression(final TernaryExpression ternaryExpression)
            throws Phase.Error {

        // Initialize a handle to the enclosing Context's Merge Body
        final BlockStatement mergeBody = this.getContext().getMergeBody();

        // Initialize a handle to the Evaluation Expression, then Expression, & Else Expression
        final Expression evaluationExpression = RewriteAssert.YieldedUnrolledInto(mergeBody, ternaryExpression.getEvaluationExpression());
        final Expression thenExpression       = RewriteAssert.YieldedUnrolledInto(mergeBody, ternaryExpression.thenPart());
        final Expression elseExpression       = RewriteAssert.YieldedUnrolledInto(mergeBody, ternaryExpression.elsePart());

        // Resolve the Evaluation Expression
        evaluationExpression.visit(this);

        // Assert the Evaluation Expression Yields
        if(evaluationExpression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the TernaryExpression

        }

        // Resolve the Then Expression
        thenExpression.visit(this);

        // Assert the Evaluation Expression Yields
        if(thenExpression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the TernaryExpression

        }

        // Resolve the Else Expression
        thenExpression.visit(this);

        // Assert the Evaluation Expression Yields
        if(elseExpression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the TernaryExpression

        }

        return null;

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
    public final Void visitUnaryPreExpression(final UnaryPreExpression unaryPreExpression)
            throws Phase.Error {

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
        expression.visit(this);

        // Assert the Evaluation Expression Yields
        if(expression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the UnaryPreExpression

        }

        return null;

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
    public final Void visitUnaryPostExpression(final UnaryPostExpression unaryPostExpression)
            throws Phase.Error {

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
        expression.visit(this);

        // Assert the Evaluation Expression Yields
        if(expression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the UnaryPreExpression

        }

        return null;

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
    public final Void visitAltStatement(final AltStatement altStatement)
            throws Phase.Error {

        //Log.log(as, "AltStat ignore in parallel usage checking.");

        // Assert that the Alt Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, altStatement.getLabel(), altStatement);

        // Assert the Alt Statement has been flattened
        RewriteAssert.FlattenedAltStatement(altStatement);

        // Assert that if the Alt Statement is replicated, it specifies only one initialization expression
        SemanticAssert.SingleInitializationForReplicatedAlt(altStatement);

        // Assert that if the Alt Statement is prioritized, it is not enclosed by another Alt Statement
        SemanticAssert.PriAltNotEnclosedByAltStatement(this, altStatement);

        // Assert the Alt Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the initialization Expression, if any
        if(altStatement.definesInitializationStatements())
            altStatement.initializationStatements().visit(this);

        // Resolve the evaluation Expression, if any
        if(altStatement.definesEvaluationExpression())
            altStatement.evaluationExpression().visit(this);

        // Resolve the Increment Expression, if any
        if(altStatement.definesIncrementExpression())
            altStatement.evaluationExpression().visit(this);

        // TODO: handle Replicated Alts & synthesize labels like the other unrolled contexts
        // Resolve the body
        altStatement.getBody().visit(this);

        // Aggregate the children to the enclosing context
        this.getContext().getMergeBody().aggregate(altStatement.getMergeBody());

        return null;

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
    public final Void visitAltCase(final AltCase altCase)
            throws Phase.Error {

        // Assert that the Alt Case's Label is undefined
        DeclarationAssert.DefinesLabel(this, altCase.getLabel(), altCase);

        // Assert the Alt Case's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // TODO: Synthesize Labels like the other unrolled Contexts
        // Resolve the Precondition Expression
        if(altCase.definesPrecondition())
            altCase.getPreconditionExpression().visit(this);

        // Resolve the Guard, if any
        if(altCase.definesGuard())
            altCase.getGuard().visit(this);

        // Resolve the Body, if any
        altCase.getBody().visit(this);

        // Aggregate the children to the enclosing context
        this.getContext().getMergeBody().aggregate(altCase.getMergeBody());

        return null;

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
    public final Void visitBlockStatement(final BlockStatement blockStatement)
            throws Phase.Error {

        // Assert the Break Statement does not contain halting procedures except for the last
        // statement
        ReachabilityAssert.DoesNotContainHaltingProcedures(this, blockStatement);

        // Assert that the Block's Label doesn't clash with any visible names
        DeclarationAssert.DefinesLabel(this, blockStatement.getLabel(), blockStatement);

        // Initialize a handle to the Merge Body
        final BlockStatement mergeBody = this.getEnclosingContext().getMergeBody();

        // Assert Flattened, Resolved Children
        RewriteAssert.Flattened(this, blockStatement);

        // Aggregate the children to the enclosing context
        mergeBody.aggregate(blockStatement.getMergeBody());

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link BreakStatement} in the current scope, asserts that the
     * {@link BreakStatement} is not enclosed in a parallel {@link SymbolMap.Context} & is enclosed in a breakable
     * {@link SymbolMap.Context}.</p>
     *
     * @param breakStatement The {@link BreakStatement} to validate.
     * @throws Phase.Error If the {@link BreakStatement}'s label is defined in the current scope or if the {@link BreakStatement}
     *                     is enclosed in a parallel or non-breakable {@link SymbolMap.Context}.
     * @since 0.1.0
     */
    @Override
    public final Void visitBreakStatement(final BreakStatement breakStatement)
            throws Phase.Error {

        // TODO: Check that if the Break Statement is in a loop, it's not in a Switch Statement
        // Assert the Break Statement is enclosed in a Breakable Context
        ReachabilityAssert.EnclosingIterativeContextBreaksAndReachable(this, breakStatement);

        // Assert that the Break Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, breakStatement.getLabel(), breakStatement);

        // Aggregate the synthesized goto TODO: Maybe Rewrite in CodeGen?
        this.getContext().getMergeBody().append(RewriteAssert.GotoStatementFor(breakStatement.getTarget().toString()));

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link ChannelWriteStatement} in the current scope.</p>
     *
     * @param channelWriteStatement The {@link BreakStatement} to define its' labels.
     * @throws Phase.Error If the {@link BreakStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitChannelWriteStatement(final ChannelWriteStatement channelWriteStatement)
            throws Phase.Error {

        // Assert that the Channel Write Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, channelWriteStatement.getLabel(), channelWriteStatement);

        // Resolve the write Expression
        channelWriteStatement.getWriteExpression().visit(this);

        // Initialize a handle to the enclosing Context's Merge Body
        final BlockStatement mergeBody = this.getContext().getMergeBody();

        // Initialize a handle to the target & write Expressions
        Expression targetExpression = channelWriteStatement.getTargetExpression();
        Expression writeExpression  = channelWriteStatement.getWriteExpression();

        // Resolve the target Expression
        targetExpression.visit(this);

        // Assert the Target Expression Yields
        if(targetExpression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the ChannelWriteStatement

        }

        // Resolve the write Expression
        writeExpression.visit(this);

        // Assert the Write Expression Yields
        if(writeExpression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the ChannelWriteStatement

        }

        // Aggregate the synthetic Write Statement
        mergeBody.append(channelWriteStatement);

        return null;

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
    public final Void visitClaimStatement(final ClaimStatement claimStatement)
            throws Phase.Error {

        // Assert that the Claim Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, claimStatement.getLabel(), claimStatement);

        // Assert the Claim Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Channels List
        claimStatement.getChannels().visit(this);

        // Resolve the Statement
        claimStatement.getStatement().visit(this);

        // Merge the Claim Statement
        this.getContext().getMergeBody().append(claimStatement);

        return null;

    }

    /**
     * <p>Asserts that the {@link ContinueStatement} is not enclosed in a parallel {@link SymbolMap.Context}
     * & is enclosed in an iterative {@link SymbolMap.Context}.</p>
     *
     * @param continueStatement The {@link ContinueStatement} to validate.
     * @throws Error If the {@link ContinueStatement} is enclosed in a parallel or non-iterative {@link SymbolMap.Context}.
     */
    @Override
    public final Void visitContinueStatement(final ContinueStatement continueStatement)
            throws Error {

        // Assert the Continue Statement is enclosed in a Breakable Context
        ReachabilityAssert.EnclosingIterativeContextBreaksAndReachable(this, continueStatement);

        // Assert that the Continue Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, continueStatement.getLabel(), continueStatement);

        // TODO: Mark Loop as having ContinueStat?

        // Aggregate the synthesized goto TODO: Maybe rewrite in CodeGen?
        this.getContext().getMergeBody().append(RewriteAssert.GotoStatementFor(continueStatement.getTarget().toString()));

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link DoStatement} in the current scope.</p>
     *
     * @param doStatement The {@link DoStatement} to define its' labels.
     * @throws Phase.Error If the {@link DoStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitDoStatement(final DoStatement doStatement)
            throws Phase.Error {

        // Assert that the Do Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, doStatement.getLabel(), doStatement);

        // Initialize a handle to the synthetic labels
        // TODO: When synthesizing labels, make sure they do not clash with another name
        // TODO: Check BarrierSet & to-be Jump labels

        // Initialize a handle to the enclosing Context's & While Statement's evaluation Expression
        final BlockStatement enclosingMergeBody = this.getEnclosingContext().getMergeBody();

        // Assert the WhileStatement's Merge Body is cleared
        doStatement.clearMergeBody();

        // Assert the Do Statement defines a body
        if(doStatement.definesBody())
            doStatement.getBody().getStatements().child(0).setLabel(doStatement.getLabel());

        // Assert Flattened Statements
        RewriteAssert.Flattened(this, doStatement.getBody());

        // Merge the children
        enclosingMergeBody.aggregate(doStatement.getMergeBody());

        // TODO: aggregate yield

        final Expression evaluationExpression =
                RewriteAssert.YieldedUnrolledInto(enclosingMergeBody, doStatement.getEvaluationExpression());

        // Append the synthesized break condition statement
        enclosingMergeBody.append(RewriteAssert.BreakConditionStatementFrom("", doStatement.getLabel(), evaluationExpression));

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link ExpressionStatement} in the current scope.</p>
     *
     * @param expressionStatement The {@link ExpressionStatement} to define its' labels.
     * @throws Phase.Error If the {@link ExpressionStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitExpressionStatement(final ExpressionStatement expressionStatement)
            throws Phase.Error {

        // Assert that the Expression Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, expressionStatement.getLabel(), expressionStatement);

        // Initialize a handle to the Expression
        Expression expression = expressionStatement.getExpression();

        // Resolve the Expression
        expression.visit(this);

        // Assert the Target Expression Yields
        if(expression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the ExpressionStatement

        }

        this.getContext().getMergeBody().append(expressionStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link ForStatement} in the current scope.</p>
     *
     * @param forStatement The {@link ForStatement} to define its' labels.
     * @throws Phase.Error If the {@link ForStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitForStatement(final ForStatement forStatement)
            throws Phase.Error {

        // Assert that the Expression Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, forStatement.getLabel(), forStatement);

        // Initialize a handle to the enclosing Context's Merge Body
        final BlockStatement enclosingMergeBody = this.getEnclosingContext().getMergeBody();

        // Initialize a handle to the label
        String startLabel = forStatement.getLabel();

        // Clear the For Statement's Merge Body
        forStatement.getBody().clearMergeBody();

        // Assert the For Statement defines initialization Statements
        if(forStatement.definesInitializationStatements()) {

            // Initialize a handle to the first Statement
            final Statement initializationStatement = forStatement.getInitializationStatements().child(0);

            // Assert the Initialization Statement does not define a label
            if(!initializationStatement.definesLabel()) {

                initializationStatement.setLabel(startLabel);
                startLabel += "_EVALUATION";

            }

            // Resolve the initialization Statements
            for(final Statement statement: forStatement.getInitializationStatements())
                statement.visit(this);

        }

        // Aggregate the synthesized break condition statement
        // TODO: Resolve Evaluation Expression & check for yielding
        enclosingMergeBody.append(RewriteAssert.InvertedBreakConditionStatementFrom(startLabel, forStatement.getEndLabel(),
                forStatement.getEvaluationExpression()));

        // Assert the statements have been flattened
        RewriteAssert.Flattened(this, forStatement.getBody());

        // Assert the For Statement defines increment statements
        // TODO: Check for yielding Expressions
        if(forStatement.definesIncrementStatements())
            forStatement.getIncrementStatements().visit(this);

        // Merge the children
        for(final Statement statement: forStatement.getMergeBody().getStatements())
            enclosingMergeBody.append(statement);

        // TODO: aggregate yield & jump label

        // Merge the epilogue
        enclosingMergeBody.append(RewriteAssert.LabelledGotoStatementFrom("", startLabel));

        return null;

    }

    /**
     * <p>Marks any enclosing contexts as yielding.</p>
     *
     * @param guardStatement The {@link GuardStatement} to finalize
     * @throws Phase.Error If the yielding denotation failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitGuardStatement(final GuardStatement guardStatement)
            throws Phase.Error {

        // Assert the Guard Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Initialize a handle to the GuardStatement's Expression
        Expression expression = guardStatement.getExpression();

        // Resolve the Expression
        expression.visit(this);

        // Assert the Expression Yields
        if(expression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the GuardStatement
            // TODO: Aggregate to merge body

        }

        // Resolve the Statement, if any
        guardStatement.getStatement().visit(this);

        return null;

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
    public final Void visitIfStatement(final IfStatement ifStatement)
            throws Phase.Error {

        // Assert the If Statement's branches are reachable
        ReachabilityAssert.ConditionalContextReachable(this, ifStatement);

        // Assert that the Expression Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, ifStatement.getLabel(), ifStatement);

        // Initialize a handle to the synthetic labels
        // TODO: When synthesizing labels, make sure they do not clash with another name
        // TODO: Check BarrierSet & to-be Jump labels
        // Initialize a handle to the enclosing Context's & While Statement's evaluation Expression
        final BlockStatement enclosingMergeBody = this.getEnclosingContext().getMergeBody();

        String label     = ifStatement.getLabel();
        String elseLabel = ifStatement.getEndLabel();

        // Assert the If Statement defines an else statement
        if(ifStatement.definesElseStatement()) {

            // Initialize a handle to the Else Statement
            final BlockStatement elseBody = ifStatement.getElseBody();

            // Initialize the Else Statement's end label
            elseBody.setEndLabel(elseLabel);

            // Initialize a handle to the Else Statement's Label
            elseLabel = (elseBody.definesLabel()) ? elseBody.getLabel() : label + "_ELSE";

            // Update the Else Statement's Label
            elseBody.setLabel(elseLabel);

        }

        // Resolve the Evaluation Expression
        // TODO: Visit Evaluation Expression, check for yielding Expressions, & Merge
        final Expression evaluationExpression = RewriteAssert.YieldedUnrolledInto(enclosingMergeBody,
                ifStatement.getEvaluationExpression());

        // Append the synthesized break condition statement
        enclosingMergeBody.append(RewriteAssert.InvertedBreakConditionStatementFrom(
                ifStatement.getLabel(), elseLabel, evaluationExpression));

        // Assert the If Statement's Then Merge Body is cleared
        ifStatement.getThenBody().clearMergeBody();

        // Assert Flattened Statements
        RewriteAssert.Flattened(this, ifStatement.getThenBody());

        // Merge the children
        enclosingMergeBody.aggregate(ifStatement.getThenBody().getMergeBody());

        // Assert the IfStatement defines an Else Statement
        if(ifStatement.definesElseStatement()) {

            // Aggregate the End Goto
            enclosingMergeBody.append(RewriteAssert.LabelledGotoStatementFrom("", ifStatement.getEndLabel()));

            // Resolve the Else Statement
            ifStatement.getElseBody().visit(this);

            // Merge the Children
            enclosingMergeBody.aggregate(ifStatement.getElseBody().getMergeBody());

        }

        return null;

    }

    /**
     * <p>Resets the {@link Type} bound to the {@link ParameterDeclaration}. If the {@link ParameterDeclaration}'s {@link Type} &
     * {@link Name} depth combined are greater than the {@link ArrayType}'s depth, an {@link ArrayType} will be
     * constructed and the {@link ParameterDeclaration}'s {@link Type} subsequently mutated.</p>
     * @param parameterDeclaration The {@link ParameterDeclaration} to mutate.
     * @since 0.1.0
     */
    @Override
    public final Void visitParameterDeclaration(final ParameterDeclaration parameterDeclaration)
            throws Phase.Error {

        // Assert the Parameter Declaration's Name is undefined
        DeclarationAssert.Defines(this, parameterDeclaration);

        // Assert the Parameter Declaration's Type is rewritten if it's specified as an ArrayType
        RewriteAssert.RewriteArrayType(parameterDeclaration);

        // Resolve the Parameter Declaration's Type
        parameterDeclaration.getType().visit(this);

        return null;

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
    public final Void visitLocalDeclaration(final LocalDeclaration localDeclaration)
            throws Phase.Error {

        // Assert that the Local Declaration's Name is defined
        DeclarationAssert.Defines(this, localDeclaration);

        // Assert that the Local Declaration's Label is undefined
        DeclarationAssert.DefinesLabel(this, localDeclaration.getLabel(), localDeclaration);

        // Assert the Constant Declaration's Type, Name, & initialization Expression are rewritten
        // if they're specified as ArrayType
        RewriteAssert.RewriteArrayType(localDeclaration);

        // Initialize a handle to the enclosing Context's merge body
        final BlockStatement mergeBody = this.getContext().getMergeBody();

        // Resolve the Type
        localDeclaration.getType().visit(this);

        // Assert the Local Declaration is initialized
        if(localDeclaration.isInitialized()) {

            // Initialize a handle to the initialization Expression
            final Expression initializationExpression = localDeclaration.getInitializationExpression();

            // Resolve the initialization Expression
            initializationExpression.visit(this);

            // Assert the Target Expression Yields
            if(initializationExpression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the LocalDeclaration's

            }

        }

        // Append the Local Declaration as-is
        mergeBody.append(localDeclaration);

        return null;

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
    public final Void visitParBlockStatement(final ParBlock parBlock)
            throws Phase.Error {

        // Assert that the Par Block's Label is undefined
        DeclarationAssert.DefinesLabel(this, parBlock.getLabel(), parBlock);

        // Assert the Par Block is not empty
        SemanticAssert.NotEmptyParallelContext(this);

        // Assert the Par Bloc has been flattened
        RewriteAssert.FlattenedParBlock(parBlock);

        // Assert the Par Block's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Initialize a handle to the synthetic labels
        // TODO: When synthesizing labels, make sure they do not clash with another name
        // TODO: Check BarrierSet & to-be Jump labels
        final String jumpLabel  = ""; // TODO

        // Initialize a handle to the enclosing Context's Body
        final BlockStatement enclosingMergeBody = this.getEnclosingContext().getMergeBody();

        // Assert the WhileStatement's Merge Body is cleared
        parBlock.clearMergeBody();

        // Assert Flattened Statements
        RewriteAssert.Flattened(this, parBlock.getBody());

        // TODO: Aggregate statement enrolls to ParBlock for each visit

        // TODO: Aggregate b.enroll(enrollees) statements first

        // Merge the children
        enclosingMergeBody.aggregate(parBlock.getMergeBody());

        // TODO: aggregate yield

        // Merge the epilogue
        enclosingMergeBody.append(RewriteAssert.LabelledGotoStatementFrom(jumpLabel, parBlock.getLabel()));

        return null;

    }

    /**
     * <p>Asserts that the {@link ReturnStatement} is not enclosed in a parallel or choice {@link SymbolMap.Context} &
     * defines any labels specified in the {@link ForStatement} in the current scope & asserts the {@link ReturnStatement} is not
     * enclosed by an {@link AltStatement}.</p>
     *
     * @param returnStatement The {@link ReturnStatement} to finalize
     * @throws Phase.Error If the {@link ReturnStatement}'s label is defined in the current scope or
     *                     the {@link ReturnStatement} is contained in an {@link AltStatement}.
     * @since 0.1.0
     */
    @Override
    public final Void visitReturnStatement(final ReturnStatement returnStatement)
            throws Phase.Error  {

        // Assert that the Return Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, returnStatement.getLabel(), returnStatement);
        // TODO: Mark loop as having a Return Statement?

        // Assert the Return Statement is not enclosed by an Alt Statement
        SemanticAssert.NotInAltStatement(this, returnStatement);

        // Assert the Return Statement is not enclosed in a parallel or choice Context
        ReachabilityAssert.NotEnclosedInParallelOrChoiceContext(this, returnStatement);

        // Resolve the Return Statement
        if(returnStatement.definesExpression()) {

            // Initialize a handle to the Expression
            final Expression expression = returnStatement.getExpression();

            // Resolve the Expression
            expression.visit(this);

            // Assert the Expression Yields
            if(expression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the ReturnStatement
                // TODO: Over the original Expression

            }

        }

        return null;
    }

    /**
     * <p>Defines any labels specified in the {@link SkipStatement} in the current scope.</p>
     *
     * @param skipStatement The {@link SkipStatement} to define its' labels.
     * @throws Phase.Error If the {@link SkipStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitSkipStatement(final SkipStatement skipStatement)
            throws Phase.Error {

        // Assert that the Skip Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, skipStatement.getLabel(), skipStatement);

        // Aggregate the Skip Statement
        this.getContext().getMergeBody().append(skipStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link StopStatement} in the current scope.</p>
     *
     * @param stopStatement The {@link StopStatement} to define its' labels.
     * @throws Phase.Error If the {@link StopStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitStopStatement(final StopStatement stopStatement)
            throws Phase.Error {

        // Assert that the Stop Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, stopStatement.getLabel(), stopStatement);

        // Aggregate the Stop Statement
        this.getContext().getMergeBody().append(stopStatement);

        return null;

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
    public final Void visitSuspendStatement(final SuspendStatement suspendStatement)
            throws Phase.Error {

        // Assert that the Suspend Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, suspendStatement.getLabel(), suspendStatement);

        // Assert the Suspend Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Parameters
        // TODO: Resolve for parameter, an unroll and check if the current needs replacing
        suspendStatement.getParameters().visit(this);

        // Aggregate the suspend statement
        this.getContext().getMergeBody().append(suspendStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link SwitchStatement} in the current scope.</p>
     *
     * @param switchStatement The {@link SwitchStatement} to define its' labels.
     * @throws Phase.Error If the {@link SwitchStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitSwitchStatement(final SwitchStatement switchStatement)
            throws Phase.Error {

        // Assert that the Switch Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, switchStatement.getLabel(), switchStatement);

        // Resolve the Evaluation Expression
        switchStatement.getEvaluationExpression().visit(this);

        // Resolve the SwitchGroups
        switchStatement.getBody().visit(this);

        // TODO: Insert Yield & Jump Label
        final BlockStatement enclosingMergeBody = this.getScope().getContext().getMergeBody();

        if(switchStatement.getEvaluationExpression().doesYield()) {

            // Initialize a handle to the Enclosing Context's merge body & the Switch Statement's
            // evaluation expression
            final String            evaluationExpressionName    = RewriteAssert.nextTemp();
            final Expression        evaluationExpression        = switchStatement.getEvaluationExpression();
            final LocalDeclaration  localDeclaration            =
                    RewriteAssert.LocalDeclarationFrom(evaluationExpression.getType(), evaluationExpressionName);

            enclosingMergeBody.append(localDeclaration);
            enclosingMergeBody.append(RewriteAssert.AssignmentStatementFrom(evaluationExpressionName, evaluationExpression));

        }

        // Initialize a handle to the Switch Groups
        final Sequence<SwitchGroupStatement> switchGroups =
                (Sequence<SwitchGroupStatement>) switchStatement.getBody().getStatements();

        for(final SwitchGroupStatement switchGroup: switchGroups) {

            // TODO: Create if statements for each label

            // TODO: Aggregate the child statements

        }

        return null;

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
    public final Void visitSyncStatement(final SyncStatement syncStatement)
            throws Phase.Error {

        // Assert that the Sync Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, syncStatement.getLabel(), syncStatement);

        // Assert the Sync Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Sync Statement
        // TODO: Resolve for barrier Expression, unroll and check if the current needs replacing
        syncStatement.getBarrierExpression().visit(this);

        this.getContext().getMergeBody().append(syncStatement);

        return null;

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
    public final Void visitTimeoutStatement(final TimeoutStatement timeoutStatement)
            throws Phase.Error {

        // Assert that the Timeout Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, timeoutStatement.getLabel(), timeoutStatement);

        // Assert the Timeout Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Initialize a handle to the enclosing Context's Merge Body
        final BlockStatement mergeBody = this.getContext().getMergeBody();

        // Initialize a handle to the target & write Expressions
        Expression timerExpression = timeoutStatement.getTimerExpression();
        Expression delayExpression = timeoutStatement.getDelayExpression();

        // Resolve the Timer Statement
        timerExpression.visit(this);

        // Assert the Expression Yields
        if(timerExpression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the TimeoutStatement
            // TODO: Over the original Expression

        }

        // Resolve the Delay Expression
        delayExpression.visit(this);

        // Assert the Expression Yields
        if(delayExpression.doesYield()) {

            // TODO: Collect the NameExpressions from the temp statement to emplace into the TimeoutStatement
            // TODO: Over the original Expression

        }

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link WhileStatement} in the current scope.</p>
     *
     * @param whileStatement The {@link WhileStatement} to define its' labels.
     * @throws Phase.Error If the {@link WhileStatement}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitWhileStatement(final WhileStatement whileStatement)
            throws Phase.Error {

        // Assert that the While Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, whileStatement.getLabel(), whileStatement);

        // Resolve the Evaluation Expression
        whileStatement.getEvaluationExpression();

        // Initialize a handle to the synthetic labels
        // TODO: When synthesizing labels, make sure they do not clash with another name
        // TODO: Check BarrierSet & to-be Jump labels
        final String jumpLabel  = ""; // TODO

        // Initialize a handle to the enclosing Context's & While Statement's evaluation Expression
        final BlockStatement    enclosingMergeBody   = this.getEnclosingContext().getMergeBody();

        // TODO: Resolve the Evaluation Expression & check for yielding Expressions
        final Expression        evaluationExpression =
                RewriteAssert.YieldedUnrolledInto(enclosingMergeBody, whileStatement.getEvaluationExpression());

        // Append the synthesized break condition statement
        enclosingMergeBody.append(RewriteAssert.InvertedBreakConditionStatementFrom(
                whileStatement.getLabel(), whileStatement.getEndLabel(), evaluationExpression));

        // Assert the WhileStatement's Merge Body is cleared
        whileStatement.getMergeBody().clear();

        // Assert Flattened Statements
        RewriteAssert.Flattened(this, whileStatement.getBody());

        // Merge the children
        enclosingMergeBody.aggregate(whileStatement.getMergeBody());

        // TODO: aggregate yield

        // Merge the epilogue
        enclosingMergeBody.append(RewriteAssert.LabelledGotoStatementFrom(jumpLabel, whileStatement.getLabel()));


        return null;

    }

}
