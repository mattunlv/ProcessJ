package org.processj.compiler.phases.phase;

import org.processj.compiler.ProcessJSourceFile;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.alt.AltCase;
import org.processj.compiler.ast.alt.AltStat;
import org.processj.compiler.ast.alt.Guard;
import org.processj.compiler.ast.expression.*;

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

            // TODO: Maybe create the Lexer within the parser & pass the file instead
            // Initialize the Lexer & Parser
            parser = new Parser(new Lexer(processJSourceFile.getCorrespondingFileReader()));

            // Retrieve the Compilation
            compilation = (Compilation) parser.parse().value;

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

    /**
     * <p>Defines the {@link ConstantDecl} in the current scope, consolidates any {@link Name} & {@link ArrayType}
     * depth(s), & rewrites the {@link ConstantDecl}'s initialization {@link Expression} if it's specified as an
     * {@link ArrayLiteral} to a {@link NewArray}.</p>
     * @param constantDeclaration The {@link ConstantDecl} to define, consolidate & rewrite.
     * @throws Phase.Error If the {@link ConstantDecl} already exists.
     * @since 0.1.0
     */
    @Override
    public final Void visitConstantDecl(final ConstantDecl constantDeclaration) throws Phase.Error {

        // Assert the Constant Declaration's Name is undefined
        DeclarationAssert.Declares(this, constantDeclaration);

        // Assert the Constant Declaration's Type, Name, & initialization Expression are rewritten
        // if they're specified as ArrayType
        SemanticAssert.RewriteArrayType(constantDeclaration);

        // Resolve the Type
        constantDeclaration.getType().visit(this);

        // Assert the Constant Declaration is initialized
        if(constantDeclaration.isInitialized())
            constantDeclaration.getInitializationExpression().visit(this);

        return null;

    }

    /**
     * <p>Inserts a {@link String}-{@link SymbolMap} pair into the {@link Compilation}'s symbol table where
     * the {@link SymbolMap} contains the different overloads of the {@link ProcTypeDecl} as long as it is not
     * qualified as mobile.</p>
     * @param procedureTypeDeclaration The {@link ProcTypeDecl} to map.
     * @throws Phase.Error If the {@link ProcTypeDecl}'s name is already defined in the {@link Compilation}'s symbol
     * table, if it overloads a mobile {@link ProcTypeDecl}, or if it is qualified as mobile and attempts to overload
     * a non-mobile {@link ProcTypeDecl}.
     * @since 0.1.0
     */
    @Override
    public final Void visitProcTypeDecl(final ProcTypeDecl procedureTypeDeclaration) throws Phase.Error {

        // Assert that the Procedure's Name or Overload is not defined
        DeclarationAssert.Declares(this, procedureTypeDeclaration);

        // Resolve the Procedure Type
        super.visitProcTypeDecl(procedureTypeDeclaration);

        return null;

    }

    /**
     * <p>Defines the {@link ProtocolTypeDecl} in the current scope if it doesn't already exist.</p>
     * @param protocolTypeDeclaration The {@link ProtocolTypeDecl} to define.
     * @throws Phase.Error If the {@link ProcTypeDecl} already exists.
     * @since 0.1.0
     */
    @Override
    public final Void visitProtocolTypeDecl(final ProtocolTypeDecl protocolTypeDeclaration) throws Phase.Error {

        // Assert that the Protocol Type's Name is not defined
        DeclarationAssert.Declares(this, protocolTypeDeclaration);

        // Resolve the Protocol Type
        super.visitProtocolTypeDecl(protocolTypeDeclaration);

        return null;

    }

    /**
     * <p>Defines the {@link RecordTypeDecl} in the current scope if it doesn't already exist.</p>
     * @param recordTypeDeclaration The {@link RecordTypeDecl} to define.
     * @throws Phase.Error If the {@link RecordTypeDecl} already exists.
     * @since 0.1.0
     */
    @Override
    public final Void visitRecordTypeDecl(final RecordTypeDecl recordTypeDeclaration) throws Phase.Error {

        // Assert that the Record's Name is not defined
        DeclarationAssert.Declares(this, recordTypeDeclaration);

        // Resolve the Record Type
        super.visitRecordTypeDecl(recordTypeDeclaration);

        return null;

    }

    /**
     * <p>Resets the {@link Type} bound to the {@link ParamDecl}. If the {@link ParamDecl}'s {@link Type} &
     * {@link Name} depth combined are greater than the {@link ArrayType}'s depth, an {@link ArrayType} will be
     * constructed and the {@link ParamDecl}'s {@link Type} subsequently mutated.</p>
     * @param parameterDeclaration The {@link ParamDecl} to mutate.
     * @since 0.1.0
     */
    @Override
    public final Void visitParamDecl(final ParamDecl parameterDeclaration) throws Phase.Error {

        // Assert the Parameter Declaration's Name is undefined
        DeclarationAssert.Defines(this, parameterDeclaration);

        // Assert the Parameter Declaration's Type is rewritten if it's specified as an ArrayType
        SemanticAssert.RewriteArrayType(parameterDeclaration);

        // Resolve the Parameter Declaration
        super.visitParamDecl(parameterDeclaration);

        return null;

    }

    /**
     * <p>Resets the {@link Type} bound to the {@link ConstantDecl}. If the {@link ConstantDecl}'s {@link Type} &
     * {@link Name} depth combined are greater than the {@link ArrayType}'s depth, an {@link ArrayType} will be
     * constructed and the {@link ConstantDecl}'s {@link Type} subsequently mutated.</p>
     * @param localDeclaration The {@link ConstantDecl} to mutate.
     * @since 0.1.0
     */
    @Override
    public final Void visitLocalDecl(final LocalDecl localDeclaration) throws Phase.Error {

        // Assert that the Local Declaration's Name is defined
        DeclarationAssert.Defines(this, localDeclaration);

        // Assert that the Local Declaration's Label is undefined
        DeclarationAssert.DefinesLabel(this, localDeclaration.getLabel(), localDeclaration);

        // Assert the Constant Declaration's Type, Name, & initialization Expression are rewritten
        // if they're specified as ArrayType
        SemanticAssert.RewriteArrayType(localDeclaration);

        // Resolve
        super.visitLocalDecl(localDeclaration);

        return null;

    }

    /**
     * <p>Asserts the {@link ArrayAccessExpr} is in any nearest enclosing {@link ParBlock}'s write set.</p>
     * @param arrayAccessExpr The {@link ArrayAccessExpr} to assert.
     * @throws Phase.Error If the {@link ParBlock} write set composition assertion failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitArrayAccessExpr(final ArrayAccessExpr arrayAccessExpr) throws Phase.Error {

        // Assert the Array Access Expression is in any nearest enclosing Par Block's write set
        SemanticAssert.InParBlockWriteSet(this, arrayAccessExpr);

        // Resolve the ArrayAccess Expression
        super.visitArrayAccessExpr(arrayAccessExpr);

        return null;

    }

    /**
     * <p>Asserts that the {@link BinaryExpr} is in the nearest enclosing {@link ParBlock}'s read set &
     * it is not composing a replicated {@link AltStat}'s input guard write expression.</p>
     * @param binaryExpression The {@link BinaryExpr} to assert.
     * @throws Phase.Error If the visibility & composition assertions failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitBinaryExpr(final BinaryExpr binaryExpression) throws Phase.Error {

        // Assert the Assignment Expression is in any enclosing Par Block's Read Set
        SemanticAssert.InParBlockReadSet(this, binaryExpression);

        // Assert the Assignment Expression is not a write expression for a replicated Alt Input Guard
        SemanticAssert.NotReplicatedAltInputGuardWriteExpression(this, binaryExpression);

        // Resolve the Binary Expression
        super.visitBinaryExpr(binaryExpression);

        return null;

    }

    /**
     * <p>Asserts the {@link Assignment} is in the nearest enclosing Par Block's Read set, is not composing a replicated
     * {@link AltStat}'s input guard write expression, its' left-hand side is visible to the nearest enclosing par
     * for & its' left-hand side is in the nearest {@link ParBlock}'s write set.</p>
     * @param assignmentExpression The {@link Assignment} to assert.
     * @throws Phase.Error If any of the assertions failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitAssignment(final Assignment assignmentExpression) throws Phase.Error {

        // Assert the Assignment Expression is in any enclosing Par Block's Read Set
        SemanticAssert.InParBlockReadSet(this, assignmentExpression);

        // Assert the Assignment Expression is not a write expression for a replicated Alt Input Guard
        SemanticAssert.NotReplicatedAltInputGuardWriteExpression(this, assignmentExpression);

        // Resolve the Assignment Expression
        super.visitAssignment(assignmentExpression);

        // Initialize a handle to the left hand side
        final Expression expression = assignmentExpression.left();

        // Assert the left-hand side of the Assignment Expression is visible to the Enclosing Par For
        SemanticAssert.VisibleToEnclosingParFor(this, expression);

        // Assert that the left-hand side Expression is not in the ParBlock's write set
        if(expression instanceof NameExpr || expression instanceof RecordAccess)
            SemanticAssert.InParBlockWriteSet(this, expression);

        return null;

    }

    /**
     * <p>Asserts the {@link ChannelReadExpr} is not composing a replicated {@link AltStat}'s input guard write
     * expression, is not composing an {@link AltCase}'s precondition, is not enclosed in a {@link Literal}
     * {@link Expression} & marks any enclosing contexts as yielding..</p>
     * @param channelReadExpression The {@link ChannelReadExpr} to assert
     * @throws Phase.Error If the assertion failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitChannelReadExpr(final ChannelReadExpr channelReadExpression) throws Phase.Error {

        // Assert the Channel Read Expression is not a write expression for a replicated Alt Input Guard
        SemanticAssert.NotReplicatedAltInputGuardWriteExpression(this, channelReadExpression);

        // Assert the Channel Read Expression is not a yielding precondition
        SemanticAssert.NonYieldingPrecondition(this, channelReadExpression);

        // Assert the Channel Read Expression is not in a Literal Expression
        SemanticAssert.NotInLiteralExpression(this, channelReadExpression);

        // Assert the Channel Read Expression's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Channel Read Expression
        super.visitChannelReadExpr(channelReadExpression);

        return null;

    }

    /**
     * <p>Asserts the {@link Invocation} is not composing an {@link AltCase}'s precondition.</p>
     * @param invocation The {@link Invocation} to check
     * @throws Phase.Error If the {@link Invocation} is composing alt precondition.
     * @since 0.1.0
     */
    @Override
    public final Void visitInvocation(final Invocation invocation) throws Phase.Error  {

        // Assert the Invocation isn't composing an Alt Case's Precondition
        SemanticAssert.NotPreconditionExpression(this, invocation);

        // Resolve the Invocation
        super.visitInvocation(invocation);

        return null;

    }

    /**
     * <p>Asserts that the {@link NameExpr} is in the nearest enclosing {@link ParBlock}'s read set &
     * it is not composing a replicated {@link AltStat}'s input guard write expression.</p>
     * @param nameExpression The {@link NameExpr} to assert.
     * @throws Phase.Error If the visibility & composition assertions failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitNameExpr(final NameExpr nameExpression) throws Phase.Error {

        // Assert the Name Expression is in any enclosing Par Block's Read Set
        SemanticAssert.InParBlockReadSet(this, nameExpression);

        // Assert the Name Expression is not a write expression for a replicated Alt Input Guard
        SemanticAssert.NotReplicatedAltInputGuardWriteExpression(this, nameExpression);

        // Resolve the Name Expression
        super.visitNameExpr(nameExpression);

        return null;

    }

    /**
     * <p>Asserts the {@link RecordAccess} is in the nearest enclosing {@link ParBlock}'s read set.</p>
     * @param recordAccessExpression The {@link RecordAccess} to assert
     * @throws Phase.Error If the {@link RecordAccess} is not in the nearest enclosing {@link ParBlock}'s
     * read set.
     * @since 0.1.0
     */
    @Override
    public final Void visitRecordAccess(final RecordAccess recordAccessExpression) throws Phase.Error {

        // Assert the Record Access Expression is in the nearest enclosing Par Block's read set.
        SemanticAssert.InParBlockReadSet(this, recordAccessExpression);

        // Resolve the Record Access
        super.visitRecordAccess(recordAccessExpression);

        return null;

    }

    /**
     * <p>Asserts that the {@link UnaryPreExpr} is not composing an {@link AltCase}'s precondition & if it
     * is composed of some name-bound {@link Expression}, that the name-bound {@link Expression} is visible
     * to any nearest enclosing par-for & is in any nearest enclosing {@link ParBlock}'s write set.</p>
     * @param unaryPreExpression The {@link UnaryPreExpr} to assert.
     * @throws Phase.Error If the visibility & composition assertion failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitUnaryPreExpr(final UnaryPreExpr unaryPreExpression) throws Phase.Error {

        // Assert that the Unary Pre Expression is not an Alt Statement Pre Condition Expression
        SemanticAssert.NotPreconditionExpression(this, unaryPreExpression);

        // Initialize a handle to the Unary Pre Expression's Operator & Expression
        final int        operator   = unaryPreExpression.getOperator();
        final Expression expression = unaryPreExpression.getExpression();

        // TODO: Errors 712, 713, 714, 715; Test Symbols are complete
        // Assert that if the Unary Pre Expression is defined with an arithmetic increment or decrement operator
        // and a Name Expression, that it's visible to any immediate enclosing Par For & that the Expression is
        // not in a Par Block's Write Set
        if((operator == UnaryPreExpr.PLUSPLUS || operator == UnaryPreExpr.MINUSMINUS)
                && ((expression instanceof NameExpr)
                || (expression instanceof RecordAccess)
                || (expression instanceof ArrayAccessExpr))) {

            // Assert the name-bound Expression is visible to ant nearest enclosing Par For
            SemanticAssert.VisibleToEnclosingParFor(this, expression);

            // Assert the name-bound Expression is in any nearest enclosing Par Block's Write Set
            SemanticAssert.InParBlockWriteSet(this, expression);

        }

        // Resolve the Unary Pre Expression
        super.visitUnaryPreExpr(unaryPreExpression);

        return null;

    }

    /**
     * <p>Asserts that the {@link UnaryPostExpr} is not composing an {@link AltCase}'s precondition & if it
     * is composed of some name-bound {@link Expression}, that the name-bound {@link Expression} is visible
     * to any nearest enclosing par-for & is in any nearest enclosing {@link ParBlock}'s write set.</p>
     * @param unaryPostExpression The {@link UnaryPostExpr} to assert.
     * @throws Phase.Error If the visibility & composition assertion failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitUnaryPostExpr(final UnaryPostExpr unaryPostExpression) throws Phase.Error {

        // Assert that the Unary Post Expression is not an Alt Statement Pre Condition Expression
        SemanticAssert.NotPreconditionExpression(this, unaryPostExpression);

        // Initialize a handle to the Unary Pre Expression's Operator & Expression
        final Expression expression = unaryPostExpression.getExpression();
        final int        operator   = unaryPostExpression.getOperator();

        // TODO: Errors 708, 709, 710, 711; Test Symbols are complete
        // Assert that if the Unary Post Expression is defined with an arithmetic increment or decrement operator
        // and a Name Expression, that it's visible to any immediate enclosing Par For & that the Expression is
        // not in a Par Block's Write Set
        if((operator == UnaryPreExpr.PLUSPLUS || operator == UnaryPreExpr.MINUSMINUS)
                && ((expression instanceof NameExpr)
                || (expression instanceof RecordAccess)
                || (expression instanceof ArrayAccessExpr))) {

            // Assert the name-bound Expression is visible to ant nearest enclosing Par For
            SemanticAssert.VisibleToEnclosingParFor(this, expression);

            // Assert the name-bound Expression is in any nearest enclosing Par Block's Write Set
            SemanticAssert.InParBlockWriteSet(this, expression);

        }

        // Resolve the Unary Pre Expression first
        super.visitUnaryPostExpr(unaryPostExpression);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link AltStat} in the current scope, flattens any non-replicated
     * {@link AltCase}s contained by the {@link AltStat}, checks for a single initialization {@link Expression}
     * if the {@link AltStat} is replicated, checks for any enclosing {@link AltStat}s if the {@link AltStat} is
     * specified as pri, & marks any enclosing contexts as yielding.</p>
     * @param altStatement The {@link AltStat} to mutate.
     * @throws Phase.Error If it was thrown by one of the {@link AltStat}'s children.
     * @since 0.1.0
     */
    @Override
    public final Void visitAltStat(final AltStat altStatement) throws Phase.Error {
        /* alt {
             x = c.read() : { x = 1; }
               }
           causes issues!
         */
        //Log.log(as, "AltStat ignore in parallel usage checking.");

        // Assert that the Alt Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, altStatement.getLabel(), altStatement);

        // Assert the Alt Statement has been flattened
        SemanticAssert.FlattenedAltStatement(altStatement);

        // Assert that if the Alt Statement is replicated, it specifies only one initialization expression
        SemanticAssert.SingleInitializationForReplicatedAlt(altStatement);

        // Assert that if the Alt Statement is prioritized, it is not enclosed by another Alt Statement
        SemanticAssert.PriAltNotEnclosedByAltStatement(this, altStatement);

        // Assert the Alt Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Alt Statement
        super.visitAltStat(altStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link AltCase} in the current scope & marks any enclosing contexts as
     * yielding.</p>
     * @param altCase The {@link AltCase} to mutate.
     * @throws Phase.Error If the {@link AltCase}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitAltCase(final AltCase altCase) throws Phase.Error {

        // Assert that the Alt Case's Label is undefined
        DeclarationAssert.DefinesLabel(this, altCase.getLabel(), altCase);

        // Assert the Alt Case's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Alt Case
        super.visitAltCase(altCase);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link Block} in the current scope & asserts that the {@link Block}
     * does not contain any halting {@link Statement}s anywhere in its' body with the exception of the last
     * {@link Statement}.</p>
     * @param block The {@link Block} to define its' labels & verify absence of halting {@link Statement}s.
     * @throws Phase.Error If the {@link Block}'s label is defined in the current scope or if it contains any
     *          intermediate halting {@link Statement}s.
     * @since 0.1.0
     */
    @Override
    public final Void visitBlock(final Block block) throws Phase.Error {

        // Assert the Break Statement does not contain halting procedures except for the last
        // statement
        ReachabilityAssert.DoesNotContainHaltingProcedures(this, block);

        // Assert that the Block's Label doesn't clash with any visible names
        DeclarationAssert.DefinesLabel(this, block.getLabel(), block);

        // Resolve the Block
        super.visitBlock(block);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link BreakStat} in the current scope, asserts that the
     * {@link BreakStat} is not enclosed in a parallel {@link SymbolMap.Context} & is enclosed in a breakable
     * {@link SymbolMap.Context}.</p>
     * @param breakStatement The {@link BreakStat} to validate.
     * @throws Phase.Error If the {@link BreakStat}'s label is defined in the current scope or if the {@link BreakStat}
     * is enclosed in a parallel or non-breakable {@link SymbolMap.Context}.
     * @since 0.1.0
     */
    @Override
    public final Void visitBreakStat(final BreakStat breakStatement) throws Phase.Error {

        // TODO: Check that if the Break Statement is in a loop, it's not in a Switch Statement
        // Assert the Break Statement is enclosed in a Breakable Context
        ReachabilityAssert.EnclosingIterativeContextBreaksAndReachable(this, breakStatement);

        // Assert that the Break Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, breakStatement.getLabel(), breakStatement);

        // Resolve the Break Statement
        super.visitBreakStat(breakStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link ChannelWriteStat} in the current scope.</p>
     * @param channelWriteStatement The {@link BreakStat} to define its' labels.
     * @throws Phase.Error If the {@link BreakStat}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitChannelWriteStat(final ChannelWriteStat channelWriteStatement) throws Phase.Error {

        // Assert that the Channel Write Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, channelWriteStatement.getLabel(), channelWriteStatement);

        // Resolve the Channel Write Statement
        super.visitChannelWriteStat(channelWriteStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link ClaimStat} in the current scope & marks any enclosing
     * Contexts as yielding.</p>
     * @param claimStatement The {@link ClaimStat} to define its' labels.
     * @throws Phase.Error If the {@link ClaimStat}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitClaimStat(final ClaimStat claimStatement) throws Phase.Error {

        // Assert that the Claim Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, claimStatement.getLabel(), claimStatement);

        // Assert the Claim Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Claim Statement
        super.visitClaimStat(claimStatement);

        return null;

    }

    /**
     * <p>Asserts that the {@link ContinueStat} is not enclosed in a parallel {@link SymbolMap.Context}
     * & is enclosed in an iterative {@link SymbolMap.Context}.</p>
     * @param continueStatement The {@link ContinueStat} to validate.
     * @throws Error If the {@link ContinueStat} is enclosed in a parallel or non-iterative {@link SymbolMap.Context}.
     */
    @Override
    public final Void visitContinueStat(final ContinueStat continueStatement) throws Error {

        // Assert the Continue Statement is enclosed in a Breakable Context
        ReachabilityAssert.EnclosingIterativeContextBreaksAndReachable(this, continueStatement);

        // Assert that the Continue Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, continueStatement.getLabel(), continueStatement);

        // TODO: Mark Loop as having ContinueStat?

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link DoStat} in the current scope.</p>
     * @param doStatement The {@link DoStat} to define its' labels.
     * @throws Phase.Error If the {@link DoStat}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitDoStat(final DoStat doStatement) throws Phase.Error {

        // Assert that the Do Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, doStatement.getLabel(), doStatement);

        // Resolve the Do Statement
        super.visitDoStat(doStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link ExprStat} in the current scope.</p>
     * @param expressionStatement The {@link ExprStat} to define its' labels.
     * @throws Phase.Error If the {@link ExprStat}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitExprStat(final ExprStat expressionStatement) throws Phase.Error {

        // Assert that the Expression Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, expressionStatement.getLabel(), expressionStatement);

        // Resolve the Expression Statement
        super.visitExprStat(expressionStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link ForStat} in the current scope.</p>
     * @param forStatement The {@link ForStat} to define its' labels.
     * @throws Phase.Error If the {@link ForStat}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitForStat(final ForStat forStatement) throws Phase.Error {

        // Assert that the Expression Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, forStatement.getLabel(), forStatement);

        // Resolve the For Statement
        super.visitForStat(forStatement);

        return null;

    }

    /**
     * <p>Marks any enclosing contexts as yielding.</p>
     * @param guardStatement The {@link Guard} to finalize
     * @throws Phase.Error If the yielding denotation failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitGuard(final Guard guardStatement) throws Phase.Error {

        // Assert the Guard Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Guard Statement
        super.visitGuard(guardStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link IfStat} in the current scope & asserts that both branches of the
     * {@link IfStat} are reachable.</p>
     * @param ifStatement The {@link IfStat} to validate.
     * @throws Phase.Error If the {@link IfStat}'s label is defined in the current scope or if the one of the
     * {@link IfStat}'s branches are unreachable.
     * @since 0.1.0
     */
    @Override
    public final Void visitIfStat(final IfStat ifStatement) throws Phase.Error {

        // Assert the If Statement's branches are reachable
        ReachabilityAssert.ConditionalContextReachable(this, ifStatement);

        // Assert that the Expression Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, ifStatement.getLabel(), ifStatement);

        // Proceed as normal
        super.visitIfStat(ifStatement);

        return null;

    }

    /**
     * <p>Asserts that the {@link ParBlock} is not empty & flattens any immediate child {@link ParBlock}s contained by
     * the {@link ParBlock}, defines any labels specified in the {@link ParBlock} in the current scope, & marks any
     * enclosing contexts as yielding.</p>
     * @param parBlock The {@link ParBlock} to validate.
     * @throws Phase.Error If it was thrown by one of the {@link ParBlock}'s children.
     * @since 0.1.0
     */
    @Override
    public final Void visitParBlock(final ParBlock parBlock) throws Phase.Error {

        // Assert that the Par Block's Label is undefined
        DeclarationAssert.DefinesLabel(this, parBlock.getLabel(), parBlock);

        // Assert the Par Block is not empty
        SemanticAssert.NotEmptyParallelContext(this);

        // Assert the Par Bloc has been flattened
        SemanticAssert.FlattenedParBlock(parBlock);

        // Assert the Par Block's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the ParBlock
        super.visitParBlock(parBlock);

        return null;

    }

    /**
     * <p>Asserts that the {@link ReturnStat} is not enclosed in a parallel or choice {@link SymbolMap.Context} &
     * defines any labels specified in the {@link ForStat} in the current scope & asserts the {@link ReturnStat} is not
     * enclosed by an {@link AltStat}.</p>
     * @param returnStatement The {@link ReturnStat} to finalize
     * @throws Phase.Error If the {@link ReturnStat}'s label is defined in the current scope or
     * the {@link ReturnStat} is contained in an {@link AltStat}.
     * @since 0.1.0
     */
    @Override
    public final Void visitReturnStat(final ReturnStat returnStatement) throws Phase.Error  {

        // Assert that the Return Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, returnStatement.getLabel(), returnStatement);
        // TODO: Mark loop as having a Return Statement?

        // TODO: These were repeated originally
        // Assert the Return Statement is not enclosed by an Alt Statement
        SemanticAssert.NotInAltStatement(this, returnStatement);

        // Assert the Return Statement is not enclosed in a parallel or choice Context
        ReachabilityAssert.NotEnclosedInParallelOrChoiceContext(this, returnStatement);

        // Resolve the Return Statement
        super.visitReturnStat(returnStatement);

        return null;
    }

    /**
     * <p>Defines any labels specified in the {@link SkipStat} in the current scope.</p>
     * @param skipStatement The {@link SkipStat} to define its' labels.
     * @throws Phase.Error If the {@link SkipStat}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitSkipStat(final SkipStat skipStatement) throws Phase.Error {

        // Assert that the Skip Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, skipStatement.getLabel(), skipStatement);

        // Resolve the Skip Statement
        super.visitSkipStat(skipStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link StopStat} in the current scope.</p>
     * @param stopStatement The {@link StopStat} to define its' labels.
     * @throws Phase.Error If the {@link StopStat}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitStopStat(final StopStat stopStatement) throws Phase.Error {

        // Assert that the Stop Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, stopStatement.getLabel(), stopStatement);

        // Resolve the Stop Statement
        super.visitStopStat(stopStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link SuspendStat} in the current scope & marks any enclosing
     * contexts as yielding.</p>
     * @param suspendStatement The {@link SuspendStat} to finalize
     * @throws Phase.Error If the yielding denotation failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitSuspendStat(final SuspendStat suspendStatement) throws Phase.Error {

        // Assert that the Suspend Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, suspendStatement.getLabel(), suspendStatement);

        // Assert the Suspend Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Suspend Statement
        super.visitSuspendStat(suspendStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link SwitchStat} in the current scope.</p>
     * @param switchStatement The {@link SwitchStat} to define its' labels.
     * @throws Phase.Error If the {@link SwitchStat}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitSwitchStat(final SwitchStat switchStatement) throws Phase.Error {

        // Assert that the Switch Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, switchStatement.getLabel(), switchStatement);

        // Resolve the Switch Statement
        super.visitSwitchStat(switchStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link SyncStat} in the current scope & marks any enclosing
     * contexts as yielding.</p>
     * @param syncStatement The {@link SyncStat} to finalize
     * @throws Phase.Error If the {@link SyncStat}'s label was already defined in the scope or the
     * yielding denotation failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitSyncStat(final SyncStat syncStatement) throws Phase.Error {

        // Assert that the Sync Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, syncStatement.getLabel(), syncStatement);

        // Assert the Sync Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Sync Statement
        super.visitSyncStat(syncStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link TimeoutStat} in the current scope & marks any enclosing
     * contexts as yielding.</p>
     * @param timeoutStatement The {@link TimeoutStat} to finalize
     * @throws Phase.Error If the {@link TimeoutStat}'s label was already defined in the scope or the
     * yielding denotation failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitTimeoutStat(final TimeoutStat timeoutStatement) throws Phase.Error {

        // Assert that the Timeout Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, timeoutStatement.getLabel(), timeoutStatement);

        // Assert the Timeout Statement's enclosing Contexts are marked as yielding
        SemanticAssert.SetEnclosingContextYields(this);

        // Resolve the Timeout Statement
        super.visitTimeoutStat(timeoutStatement);

        return null;

    }

    /**
     * <p>Defines any labels specified in the {@link WhileStat} in the current scope.</p>
     * @param whileStatement The {@link WhileStat} to define its' labels.
     * @throws Phase.Error If the {@link WhileStat}'s label is defined in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitWhileStat(final WhileStat whileStatement) throws Phase.Error {

        // Assert that the While Statement's Label is undefined
        DeclarationAssert.DefinesLabel(this, whileStatement.getLabel(), whileStatement);

        // Resolve the While Statement
        super.visitWhileStat(whileStatement);

        return null;

    }

}
