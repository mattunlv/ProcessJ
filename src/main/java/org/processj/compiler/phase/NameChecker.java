package org.processj.compiler.phase;

import org.processj.compiler.ast.expression.constructing.NewArrayExpression;
import org.processj.compiler.ast.expression.constructing.NewMobileExpression;
import org.processj.compiler.ast.expression.literal.ProtocolLiteralExpression;
import org.processj.compiler.ast.expression.literal.RecordLiteralExpression;
import org.processj.compiler.ast.expression.result.CastExpression;
import org.processj.compiler.ast.expression.result.InvocationExpression;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.statement.control.BreakStatement;
import org.processj.compiler.ast.statement.control.ContinueStatement;
import org.processj.compiler.ast.statement.declarative.LocalDeclaration;
import org.processj.compiler.ast.type.*;

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
     * <p>Asserts that the {@link ConstantDeclaration}'s {@link Type} is bound to the {@link ConstantDeclaration}.</p>
     *
     * @param constantDeclaration The {@link ConstantDeclaration} to bind.
     * @throws Phase.Error If the {@link ConstantDeclaration}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitConstantDeclaration(final ConstantDeclaration constantDeclaration) throws Phase.Error {

        // Assert the Constant Declaration Type's Name has been resolved
        NameAssert.Resolved(this, constantDeclaration);

        // Resolve the children; Don't iterate twice
        if(constantDeclaration.isInitialized())
            constantDeclaration.getInitializationExpression().accept(this);

    }

    /**
     * <p>Asserts that the {@link ProcedureTypeDeclaration}'s declared {@link Type}s are bound to the {@link ProcedureTypeDeclaration}.</p>
     * @param procedureTypeDeclaration The {@link ProcedureTypeDeclaration} to bind.
     * @throws Phase.Error If the {@link ProcedureTypeDeclaration}'s declared {@link Type}s are not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitProcedureTypeDeclaration(final ProcedureTypeDeclaration procedureTypeDeclaration) throws Phase.Error {

        // Assert the Procedure Type Declaration's Implements & Return Type are resolved
        NameAssert.Resolved(this, procedureTypeDeclaration);

        // Resolve the children; Don't iterate twice
        procedureTypeDeclaration.getBody().accept(this);

    }

    /**
     * <p>Asserts that the {@link ProtocolTypeDeclaration}'s declared {@link Type}s are bound to the {@link ProtocolTypeDeclaration}.</p>
     *
     * @param protocolTypeDeclaration The {@link ProtocolTypeDeclaration} to bind.
     * @throws Phase.Error If the {@link ProtocolTypeDeclaration}'s declared {@link Type}s are not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitProtocolTypeDeclaration(final ProtocolTypeDeclaration protocolTypeDeclaration) throws Phase.Error {

        // Assert the Protocol Type Declaration's Implements & Return Type are resolved
        NameAssert.Resolved(this, protocolTypeDeclaration);

        // Resolve the children; Don't iterate twice
        protocolTypeDeclaration.getBody().accept(this);

    }

    /**
     * <p>Asserts that the {@link RecordTypeDeclaration}'s declared {@link Type}s are bound to the {@link RecordTypeDeclaration}.</p>
     *
     * @param recordTypeDeclaration The {@link RecordTypeDeclaration} to bind.
     * @throws Phase.Error If the {@link RecordTypeDeclaration}'s declared {@link Type}s are not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitRecordTypeDeclaration(final RecordTypeDeclaration recordTypeDeclaration) throws Phase.Error {

        // Assert the Record Type Declaration's Implements & Return Type are resolved
        NameAssert.Resolved(this, recordTypeDeclaration);

        // Resolve the children
        recordTypeDeclaration.visitChildren(this);

    }

    /**
     * <p>Asserts that the {@link LocalDeclaration}'s {@link Type} is bound to the {@link LocalDeclaration}.</p>
     *
     * @param localDeclaration The {@link LocalDeclaration} to bind.
     * @throws Phase.Error If the {@link LocalDeclaration}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitLocalDeclaration(final LocalDeclaration localDeclaration) throws Phase.Error {

        // Assert the Local Declaration Type's Names have been resolved
        NameAssert.Resolved(this, localDeclaration);

        // Resolve the Children
        localDeclaration.visitChildren(this);

    }

    /**
     * <p>Asserts that the {@link ArrayType}'s component {@link Type} is bound to the
     * {@link ArrayType}.</p>
     *
     * @param arrayType The {@link ArrayType} to bind.
     * @throws Phase.Error If the {@link ArrayType}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitArrayType(final ArrayType arrayType) throws Phase.Error {

        // Assert the Array Type Component Type's Name has been resolved
        NameAssert.Resolved(this, arrayType);

        // Resolve the Children
        arrayType.visitChildren(this);

    }

    /**
     * <p>Asserts that the {@link ChannelType}'s component {@link Type} is bound to the
     * {@link ChannelType}.</p>
     *
     * @param channelType The {@link ChannelType} to bind.
     * @throws Phase.Error If the {@link ChannelType}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitChannelType(final ChannelType channelType) throws Phase.Error {

        // Assert the Channel Type Component Type's Name has been resolved
        NameAssert.Resolved(this, channelType);

        // Resolve the Children
        channelType.visitChildren(this);

    }

    /**
     * <p>Asserts that the {@link ChannelEndType}'s component {@link Type} is bound to the
     * {@link ChannelEndType}.</p>
     *
     * @param channelEndType The {@link ChannelEndType} to bind.
     * @throws Phase.Error If the {@link ChannelEndType}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitChannelEndType(final ChannelEndType channelEndType) throws Phase.Error {

        // Assert the Channel End Type Component Type's Name has been resolved
        NameAssert.Resolved(this, channelEndType);

        // Resolve the Children
        channelEndType.visitChildren(this);

    }

    /**
     * <p>Asserts that the {@link CastExpression}'s cast {@link Type} is bound to the {@link CastExpression}.</p>
     *
     * @param castExpression The {@link CastExpression} to bind.
     * @throws Phase.Error If the {@link CastExpression}'s cast {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitCastExpression(final CastExpression castExpression) throws Phase.Error {

        // Assert the Cast Expression Cast Type's Name has been resolved
        NameAssert.Resolved(this, castExpression);

        // Resolve the Children
        castExpression.visitChildren(this);

    }

    /**
     * <p>Asserts that the {@link InvocationExpression}'s {@link Type} is bound to the {@link InvocationExpression}.</p>
     *
     * @param invocationExpression The {@link InvocationExpression} to bind.
     * @throws Phase.Error If the {@link InvocationExpression}'s {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitInvocationExpression(final InvocationExpression invocationExpression) throws Phase.Error {

        // Assert the Invocation's Name has been resolved
        NameAssert.Resolved(this, invocationExpression);

        // Resolve the Children
        invocationExpression.visitChildren(this);

    }

    /**
     * <p>Asserts that the {@link NameExpression}'s {@link Type} is bound to the {@link NameExpression}.</p>
     * @param nameExpression The {@link NameExpression} to bind.
     * @throws Phase.Error If the {@link NameExpression}'s {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitNameExpression(final NameExpression nameExpression) throws Phase.Error {

        // Simply update the Type
        NameAssert.Resolved(this, nameExpression);

        // Resolve the Children
        nameExpression.visitChildren(this);

    }

    /**
     * <p>Asserts that the {@link NewArrayExpression}'s component {@link Type} is bound to the {@link NewArrayExpression}.</p>
     * @param newArrayExpression The {@link NewArrayExpression} to bind.
     * @throws Phase.Error If the {@link NewArrayExpression}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitNewArrayExpression(final NewArrayExpression newArrayExpression) throws Phase.Error {

        // Assert the Component Type has been resolved
        NameAssert.Resolved(this, newArrayExpression);

        // Resolve the Children
        newArrayExpression.visitChildren(this);

    }

    /**
     * <p>Asserts that the {@link NewMobileExpression}'s {@link Type} is bound to the {@link NewMobileExpression}.</p>
     *
     * @param newMobileExpression The {@link NewMobileExpression} to bind.
     * @throws Phase.Error If the {@link NewMobileExpression}'s {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitNewMobileExpression(final NewMobileExpression newMobileExpression) throws Phase.Error {

        // Assert the Component Type has been resolved
        NameAssert.Resolved(this, newMobileExpression);

        // Resolve the Children
        newMobileExpression.visitChildren(this);

    }

    /**
     * <p>Asserts that the {@link ProtocolLiteralExpression}'s {@link Type} is bound to the {@link ProtocolLiteralExpression} as
     * well as its tag case.</p>
     * @param protocolLiteralExpression The {@link ProtocolLiteralExpression} to bind.
     * @throws Phase.Error If the {@link ProtocolLiteralExpression}'s {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitProtocolLiteralExpression(final ProtocolLiteralExpression protocolLiteralExpression) throws Phase.Error {

        // Assert the Type & Tag Case has been resolved
        NameAssert.Resolved(this, protocolLiteralExpression);

        // Resolve the Children
        protocolLiteralExpression.visitChildren(this);



    }

    /**
     * <p>Asserts that the {@link RecordLiteralExpression}'s {@link Type} is bound to the {@link RecordLiteralExpression}.</p>
     *
     * @param recordLiteralExpression The {@link RecordLiteralExpression} to bind.
     * @throws Phase.Error If the {@link RecordLiteralExpression}'s {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitRecordLiteralExpression(final RecordLiteralExpression recordLiteralExpression) throws Phase.Error {

        // Assert the Type has been resolved
        NameAssert.Resolved(this, recordLiteralExpression);

        // Resolve the Children
        recordLiteralExpression.visitChildren(this);



    }

    /**
     * <p>Asserts that the {@link BreakStatement}'s label is visible in the current scope.</p>
     *
     * @param breakStatement The {@link BreakStatement} to assert.
     * @throws Phase.Error If the {@link BreakStatement}'s label is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitBreakStatement(final BreakStatement breakStatement) throws Phase.Error {

        // Assert the Continue Statement's Label is visible
        NameAssert.Resolved(this, breakStatement);

        // Resolve the Children
        breakStatement.visitChildren(this);

    }

    /**
     * <p>Asserts that the {@link ContinueStatement}'s label is visible in the current scope.</p>
     *
     * @param continueStatement The {@link ContinueStatement} to assert.
     * @throws Phase.Error If the {@link ContinueStatement}'s label is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final void visitContinueStatement(final ContinueStatement continueStatement) throws Phase.Error {

        // Assert the Continue Statement's Label is visible
        NameAssert.Resolved(this, continueStatement);

        // Resolve the Children
        continueStatement.visitChildren(this);

    }

}
