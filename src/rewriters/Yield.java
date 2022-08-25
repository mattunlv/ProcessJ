package rewriters;

import ast.*;
import utilities.Visitor;

public class Yield extends Visitor<Boolean> {

    public Boolean visitChildren(AST[] children) {
        boolean b = false;
        for (AST c : children) {
            if (c != null) {
                boolean bb = c.visit(this);
                b = b || bb;
            }
        }
        return b;
    }

    public Boolean visitAnnotations(Annotations as) {
        return false;
    }

    public Boolean visitAltCase(AltCase ac) {
        // This seems silly cause any Alt yields!
        return visitChildren(ac.children);
    }

    public Boolean visitAltStat(AltStat as) {
        visitChildren(as.children);
        as.setYield();
        return true;
    }

    public Boolean visitArrayAccessExpr(ArrayAccessExpr ae) {
        boolean b = visitChildren(ae.children);
        if (b) 
            ae.setYield();
        return b;
    }

    public Boolean visitArrayLiteral(ArrayLiteral al) {
        boolean b = visitChildren(al.children); 
        if (b)
            al.setYield();
        return b;
    }

    public Boolean visitArrayType(ArrayType at) {
        return false;
    }

    public Boolean visitAssignment(Assignment as) {
        boolean b = visitChildren(as.children);
        if (b)
            as.setYield();
        return b;
    }

    public Boolean visitBinaryExpr(BinaryExpr be) {
        boolean b = visitChildren(be.children);
        if (b) 
            be.setYield();
        return b;
    }

    public Boolean visitBlock(Block bl) {
        boolean b = visitChildren(bl.children);
        if (b)
            bl.setYield();
        //System.out.println("[Yield]: Block yields: " + b);
        return b;
    }   

    public Boolean visitBreakStat(BreakStat bs) {
        return false;
    }

    public Boolean visitCastExpr(CastExpr ce) {
        boolean b = visitChildren(ce.children);
        if (b) 
            ce.setYield();
        return b;
    }

    public Boolean visitChannelType(ChannelType ct) {
        // This one is kind of silly too.
        return false;
    }

    public Boolean visitChannelEndExpr(ChannelEndExpr ce) {
        boolean b = visitChildren(ce.children);
        if (b)
            ce.setYield();
        return b;
    }

    public Boolean visitChannelEndType(ChannelEndType ct) {
        return false;
    }

    public Boolean visitChannelReadExpr(ChannelReadExpr cr) {
        visitChildren(cr.children);
        cr.setYield();
        return true;
    }

    public Boolean visitChannelWriteStat(ChannelWriteStat cw) {
        visitChildren(cw.children);
        cw.setYield();
        return true;
    }

    public Boolean visitCompilation(Compilation co) {
        // At the compilation level we don't really care.
        //System.out.println("[Yield]: Visiting a Compilation");
        visitChildren(co.children);
        return false;
    }

    public Boolean visitConstantDecl(ConstantDecl cd) {
        return false;
    }

    public Boolean visitContinueStat(ContinueStat cs) {
        return false;
    }

    public Boolean visitDoStat(DoStat ds) {
        boolean b = visitChildren(ds.children);
        if (b) 
            ds.setYield();
        return b;
    }

    public Boolean visitErrorType(ErrorType et) {
        return false;
    }

    public Boolean visitExprStat(ExprStat es) {
        boolean b = visitChildren(es.children);
        if (b)
            es.setYield();
        return b;
    }

    public Boolean visitExternType(ExternType et) {
        return false;
    }

    public Boolean visitForStat(ForStat fs) {
        boolean b = visitChildren(fs.children);
        if (b)
            fs.setYield();
        return b;
    }

    public Boolean visitGuard(Guard gu) {
        visitChildren(gu.children);
        return true;
    }

    public Boolean visitIfStat(IfStat is) {
        boolean b = visitChildren(is.children);
        if (b)
            is.setYield();
        return b;
    }

    public Boolean visitImplicitImport(ImplicitImport ii) {
        return false;
    }

    public Boolean visitImport(Import im) {
        return false;
    }

    public Boolean visitInvocation(Invocation in) {
        boolean b = visitChildren(in.children);
        if (b) 
            in.setYield();
        return b;
    }

    public Boolean visitLocalDecl(LocalDecl ld) {
        boolean b = visitChildren(ld.children);
        if (b)
            ld.setYield();
        return b;
    }

    public Boolean visitModifier(Modifier mo) {
        return false;
    }

    public Boolean visitName(Name na) {
        return false;
    }

    public Boolean visitNamedType(NamedType nt) {
        return false;
    }

    public Boolean visitNameExpr(NameExpr ne) {
        return false;
    }

    public Boolean visitNewArray(NewArray ne) {
        boolean b = visitChildren(ne.children);
        if (b)
            ne.setYield();
        return b;
    }

    public Boolean visitNewMobile(NewMobile nm) {
        nm.setYield();
        return true;
        // TODO: This may not be the correct value to return here....
    }

    public Boolean visitParamDecl(ParamDecl pd) {
        return false;
    }

    public Boolean visitParBlock(ParBlock pb) {
        
        visitChildren(pb.children);
        pb.setYield();
        return true;
    }

    public Boolean visitPragma(Pragma pr) {
        return false;
    }

    public Boolean visitPrimitiveLiteral(PrimitiveLiteral li) {
        return false;
    }

    public Boolean visitPrimitiveType(PrimitiveType py) {
        return false;
    }

    public Boolean visitProcTypeDecl(ProcTypeDecl pd) {
        //System.out.println("[Yield]: Visiting " + pd.name());
        boolean b = visitChildren(pd.children);
        pd.yields = b;
        //System.out.println("[Yield]: Procedure " + pd.name() + " " + (b?"yields":"does not yield"));
        return b;
    }

    public Boolean visitProtocolLiteral(ProtocolLiteral pl) {
        boolean b = visitChildren(pl.children);
        if (b)
            pl.setYield();
        return b;
    }

    public Boolean visitProtocolCase(ProtocolCase pc) {
        return false;
    }

    public Boolean visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        return false;
    }

    public Boolean visitQualifiedName(QualifiedName qn) {
        return false;
    }

    public Boolean visitRecordAccess(RecordAccess ra) {
        boolean b = visitChildren(ra.children);
        if (b)
            ra.setYield();
        return b;
    }

    public Boolean visitRecordLiteral(RecordLiteral rl) {
        return visitChildren(rl.children);
    }

    public Boolean visitRecordMember(RecordMember rm) {
        return visitChildren(rm.children);
    }

    public Boolean visitRecordTypeDecl(RecordTypeDecl rt) {
        return visitChildren(rt.children);
    }

    public Boolean visitReturnStat(ReturnStat rs) {
        return visitChildren(rs.children);
    }

    public Boolean visitSequence(Sequence se) {
        boolean b = false;
        for (int i = 0; i < se.size(); i++)
            if (se.child(i) != null) {
		//System.out.println(i + " " + (se==null) + " " + (se.child(i) == null) + " " + se.child(i));	       
                boolean bb = se.child(i).visit(this);
                b = b || bb;
            }
        return b;
    }

    public Boolean visitSkipStat(SkipStat ss) {
        return false;
    }

    public Boolean visitStopStat(StopStat ss) {
        return false;
    }

    public Boolean visitSuspendStat(SuspendStat ss) {
        ss.setYield();
        return true;
    }

    public Boolean visitSwitchGroup(SwitchGroup sg) {
        boolean b = visitChildren(sg.children);
        return b;
    }   

    public Boolean visitSwitchLabel(SwitchLabel sl) {
        return false;
    }

    public Boolean visitSwitchStat(SwitchStat st) {
        boolean b = visitChildren(st.children);
        if (b)
            st.setYield();
        return b;
    }

    public Boolean visitRecordMemberLiteral(RecordMemberLiteral rm) {
	return rm.expr().visit(this);
    }


    public Boolean visitSyncStat(SyncStat st) {
        st.setYield();
        return true;
    }

    public Boolean visitTernary(Ternary te) {
        boolean b = visitChildren(te.children);
        if (b)
            te.setYield();
        return b;
    }

    public Boolean visitTimeoutStat(TimeoutStat ts) {
        visitChildren(ts.children);
        return true;
    }

    public Boolean visitUnaryPostExpr(UnaryPostExpr up) {
        boolean b = visitChildren(up.children);
        if (b)
            up.setYield();
        return b;
    }

    public Boolean visitUnaryPreExpr(UnaryPreExpr up) {
        boolean b = visitChildren(up.children);
        if (b)
            up.setYield();
        return b;
    }

    public Boolean visitVar(Var va) {
        boolean b = visitChildren(va.children);
        return b;
    }

    public Boolean visitWhileStat(WhileStat ws) {
        boolean b = visitChildren(ws.children);
        if (b)
            ws.setYield();
        return b;
    }
}
