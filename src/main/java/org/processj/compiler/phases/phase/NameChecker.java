package org.processj.compiler.phases.phase;

import org.processj.compiler.ast.*;

public class NameChecker extends Phase {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link Phase.Listener}.</p>
     * @param listener The {@link Phase.Listener} that receives any {@link Phase.Message}, {@link Phase.Warning},
     *                 or {@link Phase.Error} messages.
     * @since 0.1.0
     */
    public NameChecker(final Phase.Listener listener) {
        super(listener);
    }

    /// -------------------------
    /// org.processj.ast.Visitor

    /**
     * <p>Asserts that the {@link ConstantDecl}'s {@link Type} is bound to the {@link ConstantDecl}.</p>
     * @param constantDeclaration The {@link ConstantDecl} to bind.
     * @throws Phase.Error If the {@link ConstantDecl}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitConstantDecl(final ConstantDecl constantDeclaration) throws Phase.Error {

        // Assert the Constant Declaration Type's Name has been resolved
        NameAssert.Resolved(this, constantDeclaration);

        // Resolve the Constant Declaration
        if(constantDeclaration.isInitialized())
            constantDeclaration.getInitializationExpression().visit(this);

        return null;

    }

    /**
     * <p>Asserts that the {@link ProcTypeDecl}'s declared {@link Type}s are bound to the {@link ProcTypeDecl}.</p>
     * @param procedureTypeDeclaration The {@link ProcTypeDecl} to bind.
     * @throws Phase.Error If the {@link ProcTypeDecl}'s declared {@link Type}s are not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitProcTypeDecl(final ProcTypeDecl procedureTypeDeclaration) throws Phase.Error {

        // Assert the Procedure Type Declaration's Implements & Return Type are resolved
        NameAssert.Resolved(this, procedureTypeDeclaration);

        // Resolve the Statements
        procedureTypeDeclaration.getBody().visit(this);

        return null;

    }

    /**
     * <p>Asserts that the {@link ProtocolTypeDecl}'s declared {@link Type}s are bound to the {@link ProtocolTypeDecl}.</p>
     * @param protocolTypeDeclaration The {@link ProtocolTypeDecl} to bind.
     * @throws Phase.Error If the {@link ProtocolTypeDecl}'s declared {@link Type}s are not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitProtocolTypeDecl(final ProtocolTypeDecl protocolTypeDeclaration) throws Phase.Error {

        // Assert the Protocol Type Declaration's Implements & Return Type are resolved
        NameAssert.Resolved(this, protocolTypeDeclaration);

        // Resolve the children
        protocolTypeDeclaration.getBody().visit(this);

        return null;

    }

    /**
     * <p>Asserts that the {@link RecordTypeDecl}'s declared {@link Type}s are bound to the {@link RecordTypeDecl}.</p>
     * @param recordTypeDeclaration The {@link RecordTypeDecl} to bind.
     * @throws Phase.Error If the {@link RecordTypeDecl}'s declared {@link Type}s are not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitRecordTypeDecl(final RecordTypeDecl recordTypeDeclaration) throws Phase.Error {

        // Assert the Record Type Declaration's Implements & Return Type are resolved
        NameAssert.Resolved(this, recordTypeDeclaration);

        // Resolve the children
        recordTypeDeclaration.getBody().visit(this);

        return null;

    }

    /**
     * <p>Asserts that the {@link LocalDecl}'s {@link Type} is bound to the {@link LocalDecl}.</p>
     * @param localDeclaration The {@link LocalDecl} to bind.
     * @throws Phase.Error If the {@link LocalDecl}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitLocalDecl(final LocalDecl localDeclaration) throws Phase.Error {

        // Assert the Local Declaration Type's Names have been resolved
        NameAssert.Resolved(this, localDeclaration);

        // Assert the Local Declaration has an initialization expression
        if(localDeclaration.isInitialized())
            localDeclaration.getInitializationExpression().visit(this);

        return null;

    }

    /**
     * <p>Asserts that the {@link ArrayType}'s component {@link Type} is bound to the
     * {@link ArrayType}.</p>
     * @param arrayType The {@link ArrayType} to bind.
     * @throws Phase.Error If the {@link ArrayType}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitArrayType(final ArrayType arrayType) throws Phase.Error {

        // Assert the Array Type Component Type's Name has been resolved
        NameAssert.Resolved(this, arrayType);

        return null;

    }

    /**
     * <p>Asserts that the {@link ChannelType}'s component {@link Type} is bound to the
     * {@link ChannelType}.</p>
     * @param channelType The {@link ChannelType} to bind.
     * @throws Phase.Error If the {@link ChannelType}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitChannelType(final ChannelType channelType) throws Phase.Error {

        // Assert the Channel Type Component Type's Name has been resolved
        NameAssert.Resolved(this, channelType);

        return null;

    }

    /**
     * <p>Asserts that the {@link ChannelEndType}'s component {@link Type} is bound to the
     * {@link ChannelEndType}.</p>
     * @param channelEndType The {@link ChannelEndType} to bind.
     * @throws Phase.Error If the {@link ChannelEndType}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitChannelEndType(final ChannelEndType channelEndType) throws Phase.Error {

        // Assert the Channel End Type Component Type's Name has been resolved
        NameAssert.Resolved(this, channelEndType);

        return null;

    }

    /**
     * <p>Asserts that the {@link CastExpr}'s cast {@link Type} is bound to the {@link CastExpr}.</p>
     * @param castExpression The {@link CastExpr} to bind.
     * @throws Phase.Error If the {@link CastExpr}'s cast {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitCastExpr(final CastExpr castExpression) throws Phase.Error {

        // Assert the Cast Expression Cast Type's Name has been resolved
        NameAssert.Resolved(this, castExpression);

        // Resolve the expression
        castExpression.getExpression().visit(this);

        return null;

    }

    /**
     * <p>Asserts that the {@link Invocation}'s {@link Type} is bound to the {@link Invocation}.</p>
     * @param invocation The {@link Invocation} to bind.
     * @throws Phase.Error If the {@link Invocation}'s {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitInvocation(final Invocation invocation) throws Phase.Error {

        // Assert the Invocation's Name has been resolved
        NameAssert.Resolved(this, invocation);

        // Resolve the target
        invocation.getTarget().visit(this);

        // Resolve the parameters
        invocation.getParameters().visit(this);

        return null;

    }

    /**
     * <p>Asserts that the {@link NameExpr}'s {@link Type} is bound to the {@link NameExpr}.</p>
     * @param nameExpression The {@link NameExpr} to bind.
     * @throws Phase.Error If the {@link NameExpr}'s {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitNameExpr(final NameExpr nameExpression) throws Phase.Error {

        // Simply update the Type
        NameAssert.Resolved(this, nameExpression);

        return null;

    }

    /**
     * <p>Asserts that the {@link NewArray}'s component {@link Type} is bound to the {@link NewArray}.</p>
     * @param newArray The {@link NewArray} to bind.
     * @throws Phase.Error If the {@link NewArray}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitNewArray(final NewArray newArray) throws Phase.Error {

        // Assert the Component Type has been resolved
        NameAssert.Resolved(this, newArray);

        // Resolve the New Array Expression
        if(newArray.definesBracketExpressions())
            newArray.getBracketExpressions().visit(this);

        // Resolve the literal
        if(newArray.definesLiteralExpression())
            newArray.getInitializationExpression().visit(this);

        return null;

    }

    /**
     * <p>Asserts that the {@link NewMobile}'s {@link Type} is bound to the {@link NewMobile}.</p>
     * @param newMobile The {@link NewMobile} to bind.
     * @throws Phase.Error If the {@link NewMobile}'s {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitNewMobile(final NewMobile newMobile) throws Phase.Error {

        // Assert the Component Type has been resolved
        NameAssert.Resolved(this, newMobile);

        return null;

    }

    /**
     * <p>Asserts that the {@link ProtocolLiteral}'s {@link Type} is bound to the {@link ProtocolLiteral} as
     * well as its tag case.</p>
     * @param protocolLiteral The {@link ProtocolLiteral} to bind.
     * @throws Phase.Error If the {@link ProtocolLiteral}'s {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitProtocolLiteral(final ProtocolLiteral protocolLiteral) throws Phase.Error {

        // Assert the Type & Tag Case has been resolved
        NameAssert.Resolved(this, protocolLiteral);

        // Resolve the children
        protocolLiteral.getExpressions().visit(this);

        return null;

    }

    /**
     * <p>Asserts that the {@link RecordLiteral}'s {@link Type} is bound to the {@link RecordLiteral}.</p>
     * @param recordLiteral The {@link RecordLiteral} to bind.
     * @throws Phase.Error If the {@link RecordLiteral}'s {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitRecordLiteral(final RecordLiteral recordLiteral) throws Phase.Error {

        // Assert the Type has been resolved
        NameAssert.Resolved(this, recordLiteral);

        // Resolve the Record Literal Expression
        recordLiteral.getRecordMemberLiterals().visit(this);

        return null;

    }

    /**
     * <p>Asserts that the {@link BreakStat}'s label is visible in the current scope.</p>
     * @param breakStat The {@link BreakStat} to assert.
     * @throws Phase.Error If the {@link BreakStat}'s label is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitBreakStat(final BreakStat breakStat) throws Phase.Error {

        // Assert the Continue Statement's Label is visible
        NameAssert.Resolved(this, breakStat);

        return null;

    }

    /**
     * <p>Asserts that the {@link ContinueStat}'s label is visible in the current scope.</p>
     * @param continueStat The {@link ContinueStat} to assert.
     * @throws Phase.Error If the {@link ContinueStat}'s label is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitContinueStat(final ContinueStat continueStat) throws Phase.Error {

        // Assert the Continue Statement's Label is visible
        NameAssert.Resolved(this, continueStat);

        return null;

    }

    /**
     * <p>Asserts that the {@link SwitchLabel} {@link org.processj.compiler.ast.expression.Expression}'s {@link Type}
     * is declared constant or is a {@link ProcTypeDecl}'s tag.</p>
     * @param switchLabel The {@link SwitchLabel} to assert.
     * @throws Phase.Error If the {@link SwitchLabel}'s is not constant or a {@link ProcTypeDecl}'s tag.
     * @since 0.1.0
     */
    @Override
    public final Void visitSwitchLabel(final SwitchLabel switchLabel) throws Phase.Error {

        // Assert the SwitchLabel Expression is constant or a Protocol Tag
        TypeAssert.SwitchLabelConstantOrProtocolType(this, switchLabel);

        // Resolve the children
        switchLabel.visitChildren(this);

        return null;

    }

}
