package org.processj.compiler.phase;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.access.ArrayAccessExpression;
import org.processj.compiler.ast.expression.access.RecordAccessExpression;
import org.processj.compiler.ast.expression.binary.AssignmentExpression;
import org.processj.compiler.ast.expression.binary.BinaryExpression;
import org.processj.compiler.ast.expression.constructing.NewArrayExpression;
import org.processj.compiler.ast.expression.constructing.NewMobileExpression;
import org.processj.compiler.ast.expression.literal.*;
import org.processj.compiler.ast.expression.resolve.ImplicitImportExpression;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.expression.result.*;
import org.processj.compiler.ast.expression.unary.*;
import org.processj.compiler.ast.expression.yielding.ChannelEndExpression;
import org.processj.compiler.ast.expression.yielding.ChannelReadExpression;
import org.processj.compiler.ast.modifier.Modifier;
import org.processj.compiler.ast.packages.Import;
import org.processj.compiler.ast.packages.Pragma;
import org.processj.compiler.ast.statement.conditional.AltStatement;
import org.processj.compiler.ast.statement.declarative.*;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.conditional.SwitchStatement;
import org.processj.compiler.ast.type.ProcedureType;
import org.processj.compiler.ast.type.ProtocolType;
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.ast.statement.conditional.ParBlock;
import org.processj.compiler.ast.type.*;
import org.processj.compiler.ast.type.primitive.PrimitiveType;

/**
 * <p>Abstract class for the visitor pattern. This abstract class must be re-implemented for each traversal through
 * the parse tree.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public interface Visitor {

    default Context.SymbolMap getScope() {

        return null;

    }

    default Context getContext() {

        return null;

    }

    default void setScope(final Context.SymbolMap symbolMap) { /* Do Nothing */ }

    default void setContext(final Context context) { /* Do Nothing */ }


    default void visitAnnotation(final Annotation annotation) throws Phase.Error {

        annotation.accept(this);

    }

    default void visitAnnotations(final Annotations annotations) throws Phase.Error {

        annotations.accept(this);

    }

    default void visitCompilation(final Compilation compilation) throws Phase.Error {

        compilation.accept(this);

    }

    default void visitImport(final Import importName) throws Phase.Error {

        importName.accept(this);

    }

    default void visitModifier(final Modifier modifier) throws Phase.Error {

        modifier.accept(this);

    }

    default void visitName(final Name name) throws Phase.Error {

        name.accept(this);

    }

    default void visitSequence(final Sequence sequence) throws Phase.Error {

        for(int index = 0; index < sequence.size(); index++)
            if(sequence.child(index) != null) sequence.child(index).accept(this);

    }

    default void visitArrayType(final ArrayType arrayType) throws Phase.Error {

        arrayType.accept(this);

    }

    default void visitChannelType(final ChannelType channelType) throws Phase.Error {

        channelType.accept(this);

    }

    default void visitChannelEndType(final ChannelEndType channelEndType) throws Phase.Error {

        channelEndType.accept(this);

    }

    default void visitErrorType(final ErrorType errorType) throws Phase.Error {

        errorType.accept(this);

    }

    default void visitExternType(final ExternType externType) throws Phase.Error {

        externType.accept(this);

    }

    default void visitNamedType(final NamedType namedType) throws Phase.Error {

        namedType.accept(this);

    }

    default void visitPrimitiveType(final PrimitiveType primitiveType) throws Phase.Error {

        primitiveType.accept(this);

    }

    default void visitConstantDeclaration(final ConstantDeclaration constantDeclaration) throws Phase.Error {

        constantDeclaration.accept(this);

    }

    default void visitProcedureTypeDeclaration(final ProcedureType procedureType) throws Phase.Error {

        procedureType.accept(this);

    }

    default void visitProtocolTypeDeclaration(final ProtocolType protocolType) throws Phase.Error {

        protocolType.accept(this);

    }

    default void visitRecordTypeDeclaration(final RecordType recordType) throws Phase.Error {

        recordType.accept(this);

    }

    default void visitArrayAccessExpression(final ArrayAccessExpression arrayAccessExpression) throws Phase.Error {

        arrayAccessExpression.accept(this);

    }

    default void visitAssignmentExpression(final AssignmentExpression assignmentExpression) throws Phase.Error {

        assignmentExpression.accept(this);

    }

    default void visitBinaryExpression(final BinaryExpression binaryExpression) throws Phase.Error {

        binaryExpression.accept(this);

    }

    default void visitCastExpression(final CastExpression castExpression) throws Phase.Error {

        castExpression.accept(this);

    }

    default void visitChannelEndExpression(final ChannelEndExpression channelEndExpression) throws Phase.Error {

        channelEndExpression.accept(this);

    }

    default void visitChannelReadExpression(final ChannelReadExpression channelReadExpression) throws Phase.Error {

        channelReadExpression.accept(this);

    }

    default void visitImplicitImportExpression(final ImplicitImportExpression implicitImportExpression) throws Phase.Error {

        implicitImportExpression.accept(this);

    }

    default void visitInvocationExpression(final InvocationExpression invocationExpression) throws Phase.Error {

        invocationExpression.accept(this);

    }

    default void visitNameExpression(final NameExpression nameExpression) throws Phase.Error {

        nameExpression.accept(this);

    }

    default void visitNewArrayExpression(final NewArrayExpression newArrayExpression) throws Phase.Error {

        newArrayExpression.accept(this);

    }

    default void visitNewMobileExpression(final NewMobileExpression newMobileExpression) throws Phase.Error {

        newMobileExpression.accept(this);

    }

    default void visitRecordAccessExpression(final RecordAccessExpression recordAccessExpression) throws Phase.Error {

        recordAccessExpression.accept(this);

    }

    default void visitSwitchLabelExpression(final SwitchStatement.Group.Case aCase) throws Phase.Error {

        aCase.accept(this);

    }

    default void visitTernaryExpression(final TernaryExpression ternaryExpression) throws Phase.Error {

        ternaryExpression.accept(this);

    }

    default void visitUnaryPostExpression(final UnaryPostExpression unaryPostExpression) throws Phase.Error {

        unaryPostExpression.accept(this);

    }

    default void visitUnaryPreExpression(final UnaryPreExpression unaryPreExpression) throws Phase.Error {

        unaryPreExpression.accept(this);

    }

    default void visitArrayLiteralExpression(final ArrayLiteralExpression arrayLiteralExpression) throws Phase.Error {

        arrayLiteralExpression.accept(this);

    }

    default void visitProtocolLiteralExpression(final ProtocolLiteralExpression protocolLiteralExpression) throws Phase.Error {

        protocolLiteralExpression.accept(this);

    }

    default void visitPrimitiveLiteralExpression(final PrimitiveLiteralExpression primitiveLiteralExpression) throws Phase.Error {

        primitiveLiteralExpression.accept(this);

    }

    default void visitRecordMemberLiteral(final RecordMemberLiteralExpression recordMemberLiteralExpression) throws Phase.Error {

        recordMemberLiteralExpression.accept(this);

    }

    default void visitRecordLiteralExpression(final RecordLiteralExpression recordLiteralExpression) throws Phase.Error {

        recordLiteralExpression.accept(this);

    }

    default void visitAltStatement(final AltStatement altStatement) throws Phase.Error {

        altStatement.accept(this);

    }

    default void visitAltStatementCase(final AltStatement.Case aCase) throws Phase.Error {

        aCase.accept(this);

    }

    default void visitBlockStatement(final BlockStatement blockStatement) throws Phase.Error {

        blockStatement.accept(this);

    }

    default void visitBreakStatement(final BreakStatement breakStatement) throws Phase.Error {

        breakStatement.accept(this);

    }

    default void visitChannelWriteStatement(final ChannelWriteStatement channelWriteStatement) throws Phase.Error {

        channelWriteStatement.accept(this);

    }

    default void visitClaimStatement(final ClaimStatement claimStatement) throws Phase.Error {

        claimStatement.accept(this);

    }

    default void visitContinueStatement(final ContinueStatement continueStatement) throws Phase.Error {

        continueStatement.accept(this);

    }

    default void visitDoStatement(final DoStatement doStatement) throws Phase.Error {

        doStatement.accept(this);

    }

    default void visitExpressionStatement(final ExpressionStatement expressionStatement) throws Phase.Error {

        expressionStatement.accept(this);

    }

    default void visitForStatement(final ForStatement forStatement) throws Phase.Error {

        forStatement.accept(this);

    }

    default void visitGuardStatement(final AltStatement.Case.Guard guard) throws Phase.Error {

        guard.accept(this);

    }

    default void visitIfStatement(final IfStatement ifStatement) throws Phase.Error {

        ifStatement.accept(this);

    }

    default void visitLocalDeclaration(final LocalDeclaration localDeclaration) throws Phase.Error {

        localDeclaration.accept(this);

    }

    default void visitParameterDeclaration(final ParameterDeclaration parameterDeclaration) throws Phase.Error {

        parameterDeclaration.accept(this);

    }

    default void visitParBlockStatement(final ParBlock parBlock) throws Phase.Error {

        parBlock.accept(this);

    }

    default void visitProtocolCase(final ProtocolType.Case aCase) throws Phase.Error {

        aCase.accept(this);

    }

    default void visitRecordTypeDeclarationMember(final RecordType.Member member) throws Phase.Error {

        member.accept(this);

    }

    default void visitReturnStatement(final ReturnStatement tatement) throws Phase.Error {

        tatement.accept(this);

    }

    default void visitSkipStatement(final SkipStatement skipStatement) throws Phase.Error {

        skipStatement.accept(this);

    }

    default void visitStopStatement(final StopStatement stopStatement) throws Phase.Error {

        stopStatement.accept(this);

    }

    default void visitSuspendStatement(final SuspendStatement suspendStatement) throws Phase.Error {

        suspendStatement.accept(this);

    }

    default void visitSwitchStatementGroup(final SwitchStatement.Group group) throws Phase.Error {

        group.accept(this);

    }

    default void visitSwitchStatement(final SwitchStatement switchStatement) throws Phase.Error {

        switchStatement.accept(this);

    }

    default void visitSyncStatement(final SyncStatement syncStatement) throws Phase.Error {

        syncStatement.accept(this);

    }

    default void visitTimeoutStatement(final TimeoutStatement timeoutStatement) throws Phase.Error {

        timeoutStatement.accept(this);

    }

    default void visitVariableDeclaration(final VariableDeclaration variableDeclaration) throws Phase.Error {

        variableDeclaration.accept(this);

    }

    default void visitWhileStatement(final WhileStatement whileStatement) throws Phase.Error {

        whileStatement.accept(this);

    }

}
