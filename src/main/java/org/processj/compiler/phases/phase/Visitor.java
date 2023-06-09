package org.processj.compiler.phases.phase;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.alt.AltCase;
import org.processj.compiler.ast.alt.AltStat;
import org.processj.compiler.ast.alt.Guard;
import org.processj.compiler.ast.expression.ArrayAccessExpr;
import org.processj.compiler.ast.expression.ArrayLiteral;
import org.processj.compiler.ast.expression.Assignment;
import org.processj.compiler.ast.expression.BinaryExpr;

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

    default T visitAnnotation(Annotation at) throws Phase.Error {
        return null;
    }

    default T visitAnnotations(Annotations as) throws Phase.Error {
        return null;
    }

    default T visitAltCase(AltCase ac) throws Phase.Error {

        return ac.visitChildren(this);

    }

    default T visitAltStat(AltStat as) throws Phase.Error {
        return as.visitChildren(this);
    }

    default T visitArrayAccessExpr(ArrayAccessExpr ae) throws Phase.Error {
        return ae.visitChildren(this);
    }

    default T visitArrayLiteral(ArrayLiteral al) throws Phase.Error {
        return al.visitChildren(this);
    }

    default T visitArrayType(ArrayType at) throws Phase.Error {
        return at.visitChildren(this);
    }

    default T visitAssignment(Assignment as) throws Phase.Error {
        return as.visitChildren(this);
    }

    default T visitBinaryExpr(BinaryExpr be) throws Phase.Error {
        return be.visitChildren(this);
    }

    default T visitBlock(Block bl) throws Phase.Error {
        return bl.visitChildren(this);
    }

    default T visitBreakStat(BreakStat bs) throws Phase.Error {
        return null;
    }

    default T visitCastExpr(CastExpr ce) throws Phase.Error {
        return ce.visitChildren(this);
    }

    default T visitChannelType(ChannelType ct) throws Phase.Error {
        return ct.visitChildren(this);
    }

    default T visitChannelEndExpr(ChannelEndExpr ce) throws Phase.Error {
        return ce.visitChildren(this);
    }

    default T visitChannelEndType(ChannelEndType ct) throws Phase.Error {
        return ct.visitChildren(this);
    }

    default T visitChannelReadExpr(ChannelReadExpr cr) throws Phase.Error {
        return cr.visitChildren(this);
    }

    default T visitChannelWriteStat(ChannelWriteStat cw) throws Phase.Error {
        return cw.visitChildren(this);
    }

    default T visitClaimStat(ClaimStat cs) throws Phase.Error {
        return cs.visitChildren(this);
    }

    default T visitCompilation(Compilation co) throws Phase.Error {
        return co.visitChildren(this);
    }

    default T visitConstantDecl(ConstantDecl cd) throws Phase.Error {
        return cd.visitChildren(this);
    }

    default T visitContinueStat(ContinueStat cs) throws Phase.Error {
        return null;
    }

    default T visitDoStat(DoStat ds) throws Phase.Error {
        return ds.visitChildren(this);
    }

    default T visitErrorType(ErrorType et) throws Phase.Error {
        return et.visitChildren(this);
    }

    default T visitExprStat(ExprStat es) throws Phase.Error {
        return es.visitChildren(this);
    }

    default T visitExternType(ExternType et) throws Phase.Error {
        return null;
    }

    default T visitForStat(ForStat fs) throws Phase.Error {
        return fs.visitChildren(this);
    }

    default T visitGuard(Guard gu) throws Phase.Error {
        return gu.visitChildren(this);
    }

    default T visitIfStat(IfStat is) throws Phase.Error {
        return is.visitChildren(this);
    }

    default T visitImplicitImport(ImplicitImport ii) throws Phase.Error {
        return ii.visitChildren(this);
    }

    default T visitImport(Import im) throws Phase.Error {
        return im.visitChildren(this);
    }

    default T visitInvocation(Invocation in) throws Phase.Error {
        return in.visitChildren(this);
    }

    default T visitLocalDecl(LocalDecl ld) throws Phase.Error {
        return ld.visitChildren(this);
    }

    default T visitModifier(Modifier mo) throws Phase.Error {
        return null;
    }

    default T visitName(Name na) throws Phase.Error {
        return null;
    }

    default T visitNamedType(NamedType nt) throws Phase.Error {
        return nt.visitChildren(this);
    }

    default T visitNameExpr(NameExpr ne) throws Phase.Error {
        return ne.visitChildren(this);
    }

    default T visitNewArray(NewArray ne) throws Phase.Error {
        return ne.visitChildren(this);
    }

    default T visitNewMobile(NewMobile nm) throws Phase.Error {
        return nm.visitChildren(this);
    }

    default T visitParamDecl(ParamDecl pd) throws Phase.Error {
        return pd.visitChildren(this);
    }

    default T visitParBlock(ParBlock pb) throws Phase.Error {
        return pb.visitChildren(this);
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

        return pd.visitChildren(this);


    }

    default T visitProtocolLiteral(ProtocolLiteral pl) throws Phase.Error {
        return pl.visitChildren(this);
    }

    default T visitProtocolCase(ProtocolCase pc) throws Phase.Error {
        return pc.visitChildren(this);
    }

    default T visitProtocolTypeDecl(ProtocolTypeDecl pd) throws Phase.Error {
        return pd.visitChildren(this);
    }

    default T visitQualifiedName(QualifiedName qn) throws Phase.Error {
        return qn.visitChildren(this);
    }

    default T visitRecordAccess(RecordAccess ra) throws Phase.Error {
        return ra.visitChildren(this);
    }

    default T visitRecordMemberLiteral(RecordMemberLiteral rm) throws Phase.Error {
        return rm.visitChildren(this);
    }

    default T visitRecordLiteral(RecordLiteral rl) throws Phase.Error {
        return rl.visitChildren(this);
    }

    default T visitRecordMember(RecordMember rm) throws Phase.Error {
        return rm.visitChildren(this);
    }

    default T visitRecordTypeDecl(RecordTypeDecl rt) throws Phase.Error {
        return rt.visitChildren(this);
    }

    default T visitReturnStat(ReturnStat rs) throws Phase.Error {
        return rs.visitChildren(this);
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
        return ss.visitChildren(this);
    }

    default T visitSwitchGroup(SwitchGroup sg) throws Phase.Error {
        return sg.visitChildren(this);
    }

    default T visitSwitchLabel(SwitchLabel sl) throws Phase.Error {
        return sl.visitChildren(this);
    }

    default T visitSwitchStat(SwitchStat st) throws Phase.Error {
        return st.visitChildren(this);
    }

    default T visitSyncStat(SyncStat st) throws Phase.Error {
        return st.visitChildren(this);
    }

    default T visitTernary(Ternary te) throws Phase.Error {
        return te.visitChildren(this);
    }

    default T visitTimeoutStat(TimeoutStat ts) throws Phase.Error {
        return ts.visitChildren(this);
    }

    default T visitUnaryPostExpr(UnaryPostExpr up) throws Phase.Error {
        return up.visitChildren(this);
    }

    default T visitUnaryPreExpr(UnaryPreExpr up) throws Phase.Error {
        return up.visitChildren(this);
    }

    default T visitVar(Var va) throws Phase.Error {
        return va.visitChildren(this);
    }

    default T visitWhileStat(WhileStat ws) throws Phase.Error {
        return ws.visitChildren(this);
    }

}
