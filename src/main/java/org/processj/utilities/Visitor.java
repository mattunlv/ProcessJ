package org.processj.utilities;

import org.processj.Phase;
import org.processj.ast.*;
import org.processj.ast.alt.AltCase;
import org.processj.ast.alt.AltStat;
import org.processj.ast.alt.Guard;
import org.processj.ast.expression.ArrayAccessExpr;
import org.processj.ast.expression.ArrayLiteral;
import org.processj.ast.expression.Assignment;
import org.processj.ast.expression.BinaryExpr;

/**
 * Abstract class for the visitor pattern. This abstract class
 * must be re-implemented for each traversal through the tree.
 *
 * @author Matt Pedersen
 * @since 1.1
 *
 */
public interface Visitor<T extends Object> {

    default SymbolMap getScope() {
        return null;
    }

    default void setScope(final SymbolMap symbolMap) { /* Do Nothing */ }

    default T visitAnnotation(Annotation at) throws Phase.Error {
        return null;
    }

    default T visitAnnotations(Annotations as) throws Phase.Error {
        return null;
    }

    default T visitAltCase(AltCase ac) throws Phase.Error { return null; }

    default T visitAltStat(AltStat as) throws Phase.Error {
        return null;
    }

    default T visitArrayAccessExpr(ArrayAccessExpr ae) throws Phase.Error {
        return null;
    }

    default T visitArrayLiteral(ArrayLiteral al) throws Phase.Error {
        return null;
    }

    default T visitArrayType(ArrayType at) throws Phase.Error {
        return null;
    }

    default T visitAssignment(Assignment as) throws Phase.Error {
        return null;
    }

    default T visitBinaryExpr(BinaryExpr be) throws Phase.Error {
        return null;
    }

    default T visitBlock(Block bl) throws Phase.Error {
        return null;
    }

    default T visitBreakStat(BreakStat bs) throws Phase.Error {
        return null;
    }

    default T visitCastExpr(CastExpr ce) throws Phase.Error {
        return null;
    }

    default T visitChannelType(ChannelType ct) throws Phase.Error {
        return null;
    }

    default T visitChannelEndExpr(ChannelEndExpr ce) throws Phase.Error {
        return null;
    }

    default T visitChannelEndType(ChannelEndType ct) throws Phase.Error {
        return null;
    }

    default T visitChannelReadExpr(ChannelReadExpr cr) throws Phase.Error {
        return null;
    }

    default T visitChannelWriteStat(ChannelWriteStat cw) throws Phase.Error {
        return null;
    }

    default T visitClaimStat(ClaimStat cs) throws Phase.Error {
        return null;
    }

    default T visitCompilation(Compilation co) throws Phase.Error {
        return co.visitChildren(this);
    }

    default T visitConstantDecl(ConstantDecl cd) throws Phase.Error {
        return null;
    }

    default T visitContinueStat(ContinueStat cs) throws Phase.Error {
        return null;
    }

    default T visitDoStat(DoStat ds) throws Phase.Error {
        return null;
    }

    default T visitErrorType(ErrorType et) throws Phase.Error {
        return null;
    }

    default T visitExprStat(ExprStat es) throws Phase.Error {
        return null;
    }

    default T visitExternType(ExternType et) throws Phase.Error {
        return null;
    }

    default T visitForStat(ForStat fs) throws Phase.Error {
        return null;
    }

    default T visitGuard(Guard gu) throws Phase.Error {
        return null;
    }

    default T visitIfStat(IfStat is) throws Phase.Error {
        return null;
    }

    default T visitImplicitImport(ImplicitImport ii) throws Phase.Error {
        return null;
    }

    default T visitImport(Import im) throws Phase.Error {
        return im.visitChildren(this);
    }

    default T visitInvocation(Invocation in) throws Phase.Error {
        return null;
    }

    default T visitLocalDecl(LocalDecl ld) throws Phase.Error {
        return null;
    }

    default T visitModifier(Modifier mo) throws Phase.Error {
        return null;
    }

    default T visitName(Name na) throws Phase.Error {
        return null;
    }

    default T visitNamedType(NamedType nt) throws Phase.Error {
        return null;
    }

    default T visitNameExpr(NameExpr ne) throws Phase.Error {
        return null;
    }

    default T visitNewArray(NewArray ne) throws Phase.Error {
        return null;
    }

    default T visitNewMobile(NewMobile nm) throws Phase.Error {
        return null;
    }

    default T visitParamDecl(ParamDecl pd) throws Phase.Error {
        return null;
    }

    default T visitParBlock(ParBlock pb) throws Phase.Error {
        return null;
    }

    default T visitPragma(Pragma pr) throws Phase.Error {
        return null;
    }

    default T visitPrimitiveLiteral(PrimitiveLiteral li) throws Phase.Error {
        return null;
    }

    default T visitPrimitiveType(PrimitiveType py) throws Phase.Error {
        return null;
    }

    default T visitProcTypeDecl(ProcTypeDecl pd) throws Phase.Error {

        return null;


    }

    default T visitProtocolLiteral(ProtocolLiteral pl) throws Phase.Error {
        return null;
    }

    default T visitProtocolCase(ProtocolCase pc) throws Phase.Error {
        return null;
    }

    default T visitProtocolTypeDecl(ProtocolTypeDecl pd) throws Phase.Error {
        return null;
    }

    default T visitQualifiedName(QualifiedName qn) throws Phase.Error {
        return null;
    }

    default T visitRecordAccess(RecordAccess ra) throws Phase.Error {
        return null;
    }

    default T visitRecordMemberLiteral(RecordMemberLiteral rm) throws Phase.Error {
        return null;
    }

    default T visitRecordLiteral(RecordLiteral rl) throws Phase.Error {
        return null;
    }

    default T visitRecordMember(RecordMember rm) throws Phase.Error {
        return null;
    }

    default T visitRecordTypeDecl(RecordTypeDecl rt) throws Phase.Error {
        return null;
    }

    default T visitReturnStat(ReturnStat rs) throws Phase.Error {
        return null;
    }

    default T visitSequence(Sequence se) throws Phase.Error {

        for(int index = 0; index < se.size(); index++)
            if(se.child(index) != null) se.child(index).visit(this);

        return null;

    }

    default T visitSkipStat(SkipStat ss) throws Phase.Error {
        return null;
    }

    default T visitStopStat(StopStat ss) throws Phase.Error {
        return null;
    }

    default T visitSuspendStat(SuspendStat ss) throws Phase.Error {
        return null;
    }

    default T visitSwitchGroup(SwitchGroup sg) throws Phase.Error {
        return null;
    }

    default T visitSwitchLabel(SwitchLabel sl) throws Phase.Error {
        return null;
    }

    default T visitSwitchStat(SwitchStat st) throws Phase.Error {
        return null;
    }

    default T visitSyncStat(SyncStat st) throws Phase.Error {
        return null;
    }

    default T visitTernary(Ternary te) throws Phase.Error {
        return null;
    }

    default T visitTimeoutStat(TimeoutStat ts) throws Phase.Error {
        return null;
    }

    default T visitUnaryPostExpr(UnaryPostExpr up) throws Phase.Error {
        return null;
    }

    default T visitUnaryPreExpr(UnaryPreExpr up) throws Phase.Error {
        return null;
    }

    default T visitVar(Var va) throws Phase.Error {
        return null;
    }

    default T visitWhileStat(WhileStat ws) throws Phase.Error {
        return null;
    }

}
