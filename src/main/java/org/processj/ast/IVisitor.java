package org.processj.ast;

import org.processj.Phase;

/**
 * Abstract class for the visitor pattern. This abstract class
 * must be re-implemented for each traversal through the tree.
 *
 * @author Matt Pedersen
 * @since 1.1
 */
public interface IVisitor<T> {

    default T visitAnnotation(Annotation at) {
        return null;
    }

    default T visitAnnotations(Annotations as) {
        return null;
    }

    default T visitAltCase(AltCase ac) { return null; }

    default T visitAltStat(AltStat as) {
        return null;
    }

    default T visitArrayAccessExpr(ArrayAccessExpr ae) {
        return null;
    }

    default T visitArrayLiteral(ArrayLiteral al) {
        return null;
    }

    default T visitArrayType(ArrayType at) {
        return null;
    }

    default T visitAssignment(Assignment as) {
        return null;
    }

    default T visitBinaryExpr(BinaryExpr be) {
        return null;
    }

    default T visitBlock(Block bl) {
        return null;
    }

    default T visitBreakStat(BreakStat bs) {
        return null;
    }

    default T visitCastExpr(CastExpr ce) {
        return null;
    }

    default T visitChannelType(ChannelType ct) {
        return null;
    }

    default T visitChannelEndExpr(ChannelEndExpr ce) {
        return null;
    }

    default T visitChannelEndType(ChannelEndType ct) {
        return null;
    }

    default T visitChannelReadExpr(ChannelReadExpr cr) {
        return null;
    }

    default T visitChannelWriteStat(ChannelWriteStat cw) {
        return null;
    }

    default T visitClaimStat(ClaimStat cs) {
        return null;
    }

    default T visitCompilation(Compilation co) {
        return null;
    }

    default T visitConstantDecl(ConstantDecl cd) throws Phase.Error {
        return null;
    }

    default T visitContinueStat(ContinueStat cs) {
        return null;
    }

    default T visitDoStat(DoStat ds) {
        return null;
    }

    default T visitErrorType(ErrorType et) {
        return null;
    }

    default T visitExprStat(ExprStat es) {
        return null;
    }

    default T visitExternType(ExternType et) {
        return null;
    }

    default T visitForStat(ForStat fs) {
        return null;
    }

    default T visitGuard(Guard gu) {
        return null;
    }

    default T visitIfStat(IfStat is) {
        return null;
    }

    default T visitImplicitImport(ImplicitImport ii) {
        return null;
    }

    default T visitImport(Import im) throws Phase.Error {
        return null;
    }

    default T visitInvocation(Invocation in) {
        return null;
    }

    default T visitLocalDecl(LocalDecl ld) {
        return null;
    }

    default T visitModifier(Modifier mo) {
        return null;
    }

    default T visitName(Name na) {
        return null;
    }

    default T visitNamedType(NamedType nt) throws Phase.Error {
        return null;
    }

    default T visitNameExpr(NameExpr ne) {
        return null;
    }

    default T visitNewArray(NewArray ne) {
        return null;
    }

    default T visitNewMobile(NewMobile nm) {
        return null;
    }

    default T visitParamDecl(ParamDecl pd) {
        return null;
    }

    default T visitParBlock(ParBlock pb) {
        return null;
    }

    default T visitPragma(Pragma pr) {
        return null;
    }

    default T visitPrimitiveLiteral(PrimitiveLiteral li) {
        return null;
    }

    default T visitPrimitiveType(PrimitiveType py) {
        return null;
    }

    default T visitProcTypeDecl(ProcTypeDecl pd) throws Phase.Error {
        return null;
    }

    default T visitProtocolLiteral(ProtocolLiteral pl) throws Phase.Error {
        return null;
    }

    default T visitProtocolCase(ProtocolCase pc) {
        return null;
    }

    default T visitProtocolTypeDecl(ProtocolTypeDecl pd) throws Phase.Error {
        return null;
    }

    default T visitQualifiedName(QualifiedName qn) {
        return null;
    }

    default T visitRecordAccess(RecordAccess ra) {
        return null;
    }

    default T visitRecordMemberLiteral(RecordMemberLiteral rm) {
        return null;
    }

    default T visitRecordLiteral(RecordLiteral rl) {
        return null;
    }

    default T visitRecordMember(RecordMember rm) {
        return null;
    }

    default T visitRecordTypeDecl(RecordTypeDecl rt) throws Phase.Error {
        return null;
    }

    default T visitReturnStat(ReturnStat rs) {
        return null;
    }

    default T visitSequence(Sequence se) { return null; }

    default T visitSkipStat(SkipStat ss) {
        return null;
    }

    default T visitStopStat(StopStat ss) {
        return null;
    }

    default T visitSuspendStat(SuspendStat ss) {
        return null;
    }

    default T visitSwitchGroup(SwitchGroup sg) {
        return null;
    }

    default T visitSwitchLabel(SwitchLabel sl) {
        return null;
    }

    default T visitSwitchStat(SwitchStat st) {
        return null;
    }

    default T visitSyncStat(SyncStat st) {
        return null;
    }

    default T visitTernary(Ternary te) {
        return null;
    }

    default T visitTimeoutStat(TimeoutStat ts) {
        return null;
    }

    default T visitUnaryPostExpr(UnaryPostExpr up) {
        return null;
    }

    default T visitUnaryPreExpr(UnaryPreExpr up) {
        return null;
    }

    default T visitVar(Var va) {
        return null;
    }

    default T visitWhileStat(WhileStat ws) {
        return null;
    }

}
