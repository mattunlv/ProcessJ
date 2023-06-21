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
import org.processj.compiler.ast.statement.yielding.AltCase;
import org.processj.compiler.ast.statement.yielding.AltStatement;
import org.processj.compiler.ast.statement.yielding.GuardStatement;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.declarative.LocalDeclaration;
import org.processj.compiler.ast.statement.declarative.ProtocolCase;
import org.processj.compiler.ast.statement.declarative.RecordMemberDeclaration;
import org.processj.compiler.ast.statement.conditional.SwitchGroupStatement;
import org.processj.compiler.ast.expression.result.SwitchLabel;
import org.processj.compiler.ast.statement.conditional.SwitchStatement;
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

        annotation.visitChildren(this);

    }

    default void visitAnnotations(final Annotations annotations) throws Phase.Error {

        annotations.visitChildren(this);

    }

    default void visitCompilation(final Compilation compilation) throws Phase.Error {

        compilation.visitChildren(this);

    }

    default void visitImport(final Import importName) throws Phase.Error {

        importName.visitChildren(this);

    }

    default void visitModifier(final Modifier modifier) throws Phase.Error {

        modifier.visitChildren(this);

    }

    default void visitName(final Name name) throws Phase.Error {

        name.visitChildren(this);

    }

    default void visitPragma(final Pragma pragma) throws Phase.Error {

        pragma.visitChildren(this);

    }

    default void visitSequence(final Sequence sequence) throws Phase.Error {

        for(int index = 0; index < sequence.size(); index++)
            if(sequence.child(index) != null) sequence.child(index).accept(this);

    }

    default void visitArrayType(final ArrayType arrayType) throws Phase.Error {

        arrayType.visitChildren(this);

    }

    default void visitChannelType(final ChannelType channelType) throws Phase.Error {

        channelType.visitChildren(this);

    }

    default void visitChannelEndType(final ChannelEndType channelEndType) throws Phase.Error {

        channelEndType.visitChildren(this);

    }

    default void visitErrorType(final ErrorType errorType) throws Phase.Error {

        errorType.visitChildren(this);

    }

    default void visitExternType(final ExternType externType) throws Phase.Error {

        externType.visitChildren(this);

    }

    default void visitNamedType(final NamedType namedType) throws Phase.Error {

        namedType.visitChildren(this);

    }

    default void visitPrimitiveType(final PrimitiveType primitiveType) throws Phase.Error {

        primitiveType.visitChildren(this);

    }

    default void visitConstantDeclaration(final ConstantDeclaration constantDeclaration) throws Phase.Error {

        constantDeclaration.visitChildren(this);

    }

    default void visitProcedureTypeDeclaration(final ProcedureTypeDeclaration procedureTypeDeclaration) throws Phase.Error {

        procedureTypeDeclaration.visitChildren(this);

    }

    default void visitProtocolTypeDeclaration(final ProtocolTypeDeclaration protocolTypeDeclaration) throws Phase.Error {

        protocolTypeDeclaration.visitChildren(this);

    }

    default void visitRecordTypeDeclaration(final RecordTypeDeclaration recordTypeDeclaration) throws Phase.Error {

        recordTypeDeclaration.visitChildren(this);

    }

    default void visitArrayAccessExpression(final ArrayAccessExpression arrayAccessExpression) throws Phase.Error {

        arrayAccessExpression.visitChildren(this);

    }

    default void visitAssignmentExpression(final AssignmentExpression assignmentExpression) throws Phase.Error {

        assignmentExpression.visitChildren(this);

    }

    default void visitBinaryExpression(final BinaryExpression binaryExpression) throws Phase.Error {

        binaryExpression.visitChildren(this);

    }

    default void visitCastExpression(final CastExpression castExpression) throws Phase.Error {

        castExpression.visitChildren(this);

    }

    default void visitChannelEndExpression(final ChannelEndExpression channelEndExpression) throws Phase.Error {

        channelEndExpression.visitChildren(this);

    }

    default void visitChannelReadExpression(final ChannelReadExpression channelReadExpression) throws Phase.Error {

        channelReadExpression.visitChildren(this);

    }

    default void visitImplicitImportExpression(final ImplicitImportExpression implicitImportExpression) throws Phase.Error {

        implicitImportExpression.visitChildren(this);

    }

    default void visitInvocationExpression(final InvocationExpression invocationExpression) throws Phase.Error {

        invocationExpression.visitChildren(this);

    }

    default void visitNameExpression(final NameExpression nameExpression) throws Phase.Error {

        nameExpression.visitChildren(this);

    }

    default void visitNewArrayExpression(final NewArrayExpression newArrayExpression) throws Phase.Error {

        newArrayExpression.visitChildren(this);

    }

    default void visitNewMobileExpression(final NewMobileExpression newMobileExpression) throws Phase.Error {

        newMobileExpression.visitChildren(this);

    }

    default void visitRecordAccessExpression(final RecordAccessExpression recordAccessExpression) throws Phase.Error {

        recordAccessExpression.visitChildren(this);

    }

    default void visitSwitchLabelExpression(final SwitchLabel switchLabel) throws Phase.Error {

        switchLabel.visitChildren(this);

    }

    default void visitTernaryExpression(final TernaryExpression ternaryExpression) throws Phase.Error {

        ternaryExpression.visitChildren(this);

    }

    default void visitUnaryPostExpression(final UnaryPostExpression unaryPostExpression) throws Phase.Error {

        unaryPostExpression.visitChildren(this);

    }

    default void visitUnaryPreExpression(final UnaryPreExpression unaryPreExpression) throws Phase.Error {

        unaryPreExpression.visitChildren(this);

    }

    default void visitArrayLiteralExpression(final ArrayLiteralExpression arrayLiteralExpression) throws Phase.Error {

        arrayLiteralExpression.visitChildren(this);

    }

    default void visitProtocolLiteralExpression(final ProtocolLiteralExpression protocolLiteralExpression) throws Phase.Error {

        protocolLiteralExpression.visitChildren(this);

    }

    default void visitPrimitiveLiteralExpression(final PrimitiveLiteralExpression primitiveLiteralExpression) throws Phase.Error {

        primitiveLiteralExpression.visitChildren(this);

    }

    default void visitRecordMemberLiteral(final RecordMemberLiteralExpression recordMemberLiteralExpression) throws Phase.Error {

        recordMemberLiteralExpression.visitChildren(this);

    }

    default void visitRecordLiteralExpression(final RecordLiteralExpression recordLiteralExpression) throws Phase.Error {

        recordLiteralExpression.visitChildren(this);

    }

    default void visitAltStatement(final AltStatement altStatement) throws Phase.Error {

        altStatement.visitChildren(this);

    }

    default void visitAltCase(final AltCase altCase) throws Phase.Error {

        altCase.visitChildren(this);

    }

    default void visitBlockStatement(final BlockStatement blockStatement) throws Phase.Error {

        blockStatement.visitChildren(this);

    }

    default void visitBreakStatement(final BreakStatement breakStatement) throws Phase.Error {

        breakStatement.visitChildren(this);

    }

    default void visitChannelWriteStatement(final ChannelWriteStatement channelWriteStatement) throws Phase.Error {

        channelWriteStatement.visitChildren(this);

    }

    default void visitClaimStatement(final ClaimStatement claimStatement) throws Phase.Error {

        claimStatement.visitChildren(this);

    }

    default void visitContinueStatement(final ContinueStatement continueStatement) throws Phase.Error {

        continueStatement.visitChildren(this);

    }

    default void visitDoStatement(final DoStatement doStatement) throws Phase.Error {

        doStatement.visitChildren(this);

    }

    default void visitExpressionStatement(final ExpressionStatement expressionStatement) throws Phase.Error {

        expressionStatement.visitChildren(this);

    }

    default void visitForStatement(final ForStatement forStatement) throws Phase.Error {

        forStatement.visitChildren(this);

    }

    default void visitGuardStatement(final GuardStatement guardStatement) throws Phase.Error {

        guardStatement.visitChildren(this);

    }

    default void visitIfStatement(final IfStatement ifStatement) throws Phase.Error {

        ifStatement.visitChildren(this);

    }

    default void visitLocalDeclaration(final LocalDeclaration localDeclaration) throws Phase.Error {

        localDeclaration.visitChildren(this);

    }

    default void visitParameterDeclaration(final ParameterDeclaration parameterDeclaration) throws Phase.Error {

        parameterDeclaration.visitChildren(this);

    }

    default void visitParBlockStatement(final ParBlock parBlock) throws Phase.Error {

        parBlock.visitChildren(this);

    }

    default void visitProtocolCase(final ProtocolCase protocolCase) throws Phase.Error {

        protocolCase.visitChildren(this);

    }

    default void visitRecordMemberDeclaration(final RecordMemberDeclaration recordMemberDeclaration) throws Phase.Error {

        recordMemberDeclaration.visitChildren(this);

    }

    default void visitReturnStatement(final ReturnStatement tatement) throws Phase.Error {

        tatement.visitChildren(this);

    }

    default void visitSkipStatement(final SkipStatement skipStatement) throws Phase.Error {

        skipStatement.visitChildren(this);

    }

    default void visitStopStatement(final StopStatement stopStatement) throws Phase.Error {

        stopStatement.visitChildren(this);

    }

    default void visitSuspendStatement(final SuspendStatement suspendStatement) throws Phase.Error {

        suspendStatement.visitChildren(this);

    }

    default void visitSwitchGroupStatement(final SwitchGroupStatement switchGroupStatement) throws Phase.Error {

        switchGroupStatement.visitChildren(this);

    }

    default void visitSwitchStatement(final SwitchStatement switchStatement) throws Phase.Error {

        switchStatement.visitChildren(this);

    }

    default void visitSyncStatement(final SyncStatement syncStatement) throws Phase.Error {

        syncStatement.visitChildren(this);

    }

    default void visitTimeoutStatement(final TimeoutStatement timeoutStatement) throws Phase.Error {

        timeoutStatement.visitChildren(this);

    }

    default void visitWhileStatement(final WhileStatement whileStatement) throws Phase.Error {

        whileStatement.visitChildren(this);

    }

}
