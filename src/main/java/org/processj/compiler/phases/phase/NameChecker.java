package org.processj.compiler.phases.phase;

import org.processj.compiler.ast.*;

// TODO: Error 405 (Not found
// TODO: Errors 416 & 417 (Record Literal)
// TODO: Errors 409, 412, & 413 (Protocol Literal)
// TODO: Errors 418 & 419 (Record Type Declaration
// TODO: Errors 414 & 415 (Protocol Type Declaration)
// TODO: Errors 408 & 410 (Procedure Type Declaration)
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
        super.visitConstantDecl(constantDeclaration);

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

        // Initialize a handle to the scope & return Type
        final Type returnType = procedureTypeDeclaration.getReturnType()  ;

        // Visit the Type if it's not a NamedType
        if(!(returnType instanceof NamedType)) returnType.visit(this);

        // Update the return type
        procedureTypeDeclaration.setReturnType((Type) NameAssert.Resolved(this, returnType.getName()));

        // Bind the implement type list
        procedureTypeDeclaration.setTypeForEachImplement((name) -> (Type) NameAssert.Resolved(this, name));

        // Resolve the parameter list
        procedureTypeDeclaration.getParameters().visit(this);

        // Recur on the body
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

        // This should bind the declared & extends Types
        super.visitProtocolTypeDecl(protocolTypeDeclaration);

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

        // This should bind the declared & extends Types
        super.visitRecordTypeDecl(recordTypeDeclaration);

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

        // Resolve the Parameter Declaration
        super.visitLocalDecl(localDeclaration);

        return null;

    }

    /**
     * <p>Asserts that the {@link ParamDecl}'s {@link Type} is bound to the {@link ParamDecl}.</p>
     * @param parameterDeclaration The {@link ParamDecl} to bind.
     * @throws Phase.Error If the {@link ParamDecl}'s component {@link Type} is not visible in the current scope.
     * @since 0.1.0
     */
    @Override
    public final Void visitParamDecl(final ParamDecl parameterDeclaration) throws Phase.Error {

        // Assert the Parameter Declaration's Type Component Type's Name has been resolved
        NameAssert.Resolved(this, parameterDeclaration);

        // Resolve the Parameter Declaration
        super.visitParamDecl(parameterDeclaration);

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

        // Resolve the Channel Type
        super.visitArrayType(arrayType);

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

        // Resolve the Channel Type
        super.visitChannelType(channelType);

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

        // Resolve the Channel End Type
        super.visitChannelEndType(channelEndType);

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
        NameAssert.Resolved(this, breakStat.getTarget());

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
        NameAssert.Resolved(this, continueStat.getTarget());

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

        // Resolve the Cast Expression
        super.visitCastExpr(castExpression);

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

        // Resolve the Invocation
        super.visitInvocation(invocation);

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
        super.visitNewArray(newArray);

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

        // Resolve the Protocol Literal
        super.visitProtocolLiteral(protocolLiteral);

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
        super.visitRecordLiteral(recordLiteral);

        return null;

    }

}
