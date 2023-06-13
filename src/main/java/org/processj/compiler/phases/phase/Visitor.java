package org.processj.compiler.phases.phase;

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
import org.processj.compiler.ast.statement.alt.AltCase;
import org.processj.compiler.ast.statement.alt.AltStatement;
import org.processj.compiler.ast.statement.alt.GuardStatement;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.declarative.LocalDeclaration;
import org.processj.compiler.ast.statement.declarative.ProtocolCase;
import org.processj.compiler.ast.statement.declarative.RecordMemberDeclaration;
import org.processj.compiler.ast.statement.switched.SwitchGroupStatement;
import org.processj.compiler.ast.expression.result.SwitchLabel;
import org.processj.compiler.ast.statement.switched.SwitchStatement;
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.ast.statement.yielding.ParBlock;
import org.processj.compiler.ast.type.*;

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
public interface Visitor<T> {

    default SymbolMap getScope() {

        return null;

    }

    default void setScope(final SymbolMap symbolMap) { /* Do Nothing */ }

    default T visitAnnotation(final Annotation annotation) throws Phase.Error {

        return annotation.visitChildren(this);

    }

    default T visitAnnotations(final Annotations annotations) throws Phase.Error {

        return annotations.visitChildren(this);

    }

    default T visitCompilation(final Compilation compilation) throws Phase.Error {

        return compilation.visitChildren(this);

    }

    default T visitImport(final Import importName) throws Phase.Error {

        return importName.visitChildren(this);

    }

    default T visitModifier(final Modifier modifier) throws Phase.Error {

        return modifier.visitChildren(this);

    }

    default T visitName(final Name name) throws Phase.Error {

        return name.visitChildren(this);

    }

    default T visitPragma(final Pragma pragma) throws Phase.Error {

        return pragma.visitChildren(this);

    }

    default T visitSequence(final Sequence sequence) throws Phase.Error {

        for(int index = 0; index < sequence.size(); index++)
            if(sequence.child(index) != null) sequence.child(index).visit(this);

        return null;

    }

    default T visitArrayType(final ArrayType arrayType) throws Phase.Error {

        return arrayType.visitChildren(this);

    }

    default T visitChannelType(final ChannelType channelType) throws Phase.Error {

        return channelType.visitChildren(this);

    }

    default T visitChannelEndType(final ChannelEndType channelEndType) throws Phase.Error {

        return channelEndType.visitChildren(this);

    }

    default T visitErrorType(final ErrorType errorType) throws Phase.Error {

        return errorType.visitChildren(this);

    }

    default T visitExternType(final ExternType externType) throws Phase.Error {

        return externType.visitChildren(this);

    }

    default T visitNamedType(final NamedType namedType) throws Phase.Error {

        return namedType.visitChildren(this);

    }

    default T visitPrimitiveType(final PrimitiveType primitiveType) throws Phase.Error {

        return primitiveType.visitChildren(this);

    }

    default T visitConstantDeclaration(final ConstantDeclaration constantDeclaration) throws Phase.Error {

        return constantDeclaration.visitChildren(this);

    }

    default T visitProcedureTypeDeclaration(final ProcedureTypeDeclaration procedureTypeDeclaration) throws Phase.Error {

        return procedureTypeDeclaration.visitChildren(this);

    }

    default T visitProtocolTypeDeclaration(final ProtocolTypeDeclaration protocolTypeDeclaration) throws Phase.Error {

        return protocolTypeDeclaration.visitChildren(this);

    }

    default T visitRecordTypeDeclaration(final RecordTypeDeclaration recordTypeDeclaration) throws Phase.Error {

        return recordTypeDeclaration.visitChildren(this);

    }

    default T visitArrayAccessExpression(final ArrayAccessExpression arrayAccessExpression) throws Phase.Error {

        return arrayAccessExpression.visitChildren(this);

    }

    default T visitAssignmentExpression(final AssignmentExpression assignmentExpression) throws Phase.Error {

        return assignmentExpression.visitChildren(this);

    }

    default T visitBinaryExpression(final BinaryExpression binaryExpression) throws Phase.Error {

        return binaryExpression.visitChildren(this);

    }

    default T visitCastExpression(final CastExpression castExpression) throws Phase.Error {

        return castExpression.visitChildren(this);

    }

    default T visitChannelEndExpression(final ChannelEndExpression channelEndExpression) throws Phase.Error {

        return channelEndExpression.visitChildren(this);

    }

    default T visitChannelReadExpression(final ChannelReadExpression channelReadExpression) throws Phase.Error {

        return channelReadExpression.visitChildren(this);

    }

    default T visitImplicitImportExpression(final ImplicitImportExpression implicitImportExpression) throws Phase.Error {

        return implicitImportExpression.visitChildren(this);

    }

    default T visitInvocationExpression(final InvocationExpression invocationExpression) throws Phase.Error {

        return invocationExpression.visitChildren(this);

    }

    default T visitNameExpression(final NameExpression nameExpression) throws Phase.Error {

        return nameExpression.visitChildren(this);

    }

    default T visitNewArrayExpression(final NewArrayExpression newArrayExpression) throws Phase.Error {

        return newArrayExpression.visitChildren(this);

    }

    default T visitNewMobileExpression(final NewMobileExpression newMobileExpression) throws Phase.Error {

        return newMobileExpression.visitChildren(this);

    }

    default T visitRecordAccessExpression(final RecordAccessExpression recordAccessExpression) throws Phase.Error {

        return recordAccessExpression.visitChildren(this);

    }

    default T visitSwitchLabelExpression(final SwitchLabel switchLabel) throws Phase.Error {

        return switchLabel.visitChildren(this);

    }

    default T visitTernaryExpression(final TernaryExpression ternaryExpression) throws Phase.Error {

        return ternaryExpression.visitChildren(this);

    }

    default T visitUnaryPostExpression(final UnaryPostExpression unaryPostExpression) throws Phase.Error {

        return unaryPostExpression.visitChildren(this);

    }

    default T visitUnaryPreExpression(final UnaryPreExpression unaryPreExpression) throws Phase.Error {

        return unaryPreExpression.visitChildren(this);

    }

    default T visitArrayLiteralExpression(final ArrayLiteralExpression arrayLiteralExpression) throws Phase.Error {

        return arrayLiteralExpression.visitChildren(this);

    }

    default T visitProtocolLiteralExpression(final ProtocolLiteralExpression protocolLiteralExpression) throws Phase.Error {

        return protocolLiteralExpression.visitChildren(this);

    }

    default T visitPrimitiveLiteralExpression(final PrimitiveLiteralExpression primitiveLiteralExpression) throws Phase.Error {

        return primitiveLiteralExpression.visitChildren(this);

    }

    default T visitRecordMemberLiteral(final RecordMemberLiteralExpression recordMemberLiteralExpression) throws Phase.Error {

        return recordMemberLiteralExpression.visitChildren(this);

    }

    default T visitRecordLiteralExpression(final RecordLiteralExpression recordLiteralExpression) throws Phase.Error {

        return recordLiteralExpression.visitChildren(this);

    }

    default T visitAltStatement(final AltStatement altStatement) throws Phase.Error {

        return altStatement.visitChildren(this);

    }

    default T visitAltCase(final AltCase altCase) throws Phase.Error {

        return altCase.visitChildren(this);

    }

    default T visitBlockStatement(final BlockStatement blockStatement) throws Phase.Error {

        return blockStatement.visitChildren(this);

    }

    default T visitBreakStatement(final BreakStatement breakStatement) throws Phase.Error {

        return breakStatement.visitChildren(this);

    }

    default T visitChannelWriteStatement(final ChannelWriteStatement channelWriteStatement) throws Phase.Error {

        return channelWriteStatement.visitChildren(this);

    }

    default T visitClaimStatement(final ClaimStatement claimStatement) throws Phase.Error {

        return claimStatement.visitChildren(this);

    }

    default T visitContinueStatement(final ContinueStatement continueStatement) throws Phase.Error {

        return continueStatement.visitChildren(this);

    }

    default T visitDoStatement(final DoStatement doStatement) throws Phase.Error {

        return doStatement.visitChildren(this);

    }

    default T visitExpressionStatement(final ExpressionStatement expressionStatement) throws Phase.Error {

        return expressionStatement.visitChildren(this);

    }

    default T visitForStatement(final ForStatement forStatement) throws Phase.Error {

        return forStatement.visitChildren(this);

    }

    default T visitGuardStatement(final GuardStatement guardStatement) throws Phase.Error {

        return guardStatement.visitChildren(this);

    }

    default T visitIfStatement(final IfStatement ifStatement) throws Phase.Error {

        return ifStatement.visitChildren(this);

    }

    default T visitLocalDeclaration(final LocalDeclaration localDeclaration) throws Phase.Error {

        return localDeclaration.visitChildren(this);

    }

    default T visitParameterDeclaration(final ParameterDeclaration parameterDeclaration) throws Phase.Error {

        return parameterDeclaration.visitChildren(this);

    }

    default T visitParBlockStatement(final ParBlock parBlock) throws Phase.Error {

        return parBlock.visitChildren(this);

    }

    default T visitProtocolCase(final ProtocolCase protocolCase) throws Phase.Error {

        return protocolCase.visitChildren(this);

    }

    default T visitRecordMemberDeclaration(final RecordMemberDeclaration recordMemberDeclaration) throws Phase.Error {

        return recordMemberDeclaration.visitChildren(this);

    }

    default T visitReturnStatement(final ReturnStatement returnStatement) throws Phase.Error {

        return returnStatement.visitChildren(this);

    }

    default T visitSkipStatement(final SkipStatement skipStatement) throws Phase.Error {

        return skipStatement.visitChildren(this);

    }

    default T visitStopStatement(final StopStatement stopStatement) throws Phase.Error {

        return stopStatement.visitChildren(this);

    }

    default T visitSuspendStatement(final SuspendStatement suspendStatement) throws Phase.Error {

        return suspendStatement.visitChildren(this);

    }

    default T visitSwitchGroupStatement(final SwitchGroupStatement switchGroupStatement) throws Phase.Error {

        return switchGroupStatement.visitChildren(this);

    }

    default T visitSwitchStatement(final SwitchStatement switchStatement) throws Phase.Error {

        return switchStatement.visitChildren(this);

    }

    default T visitSyncStatement(final SyncStatement syncStatement) throws Phase.Error {

        return syncStatement.visitChildren(this);

    }

    default T visitTimeoutStatement(final TimeoutStatement timeoutStatement) throws Phase.Error {

        return timeoutStatement.visitChildren(this);

    }

    default T visitWhileStatement(final WhileStatement whileStatement) throws Phase.Error {

        return whileStatement.visitChildren(this);

    }

}
