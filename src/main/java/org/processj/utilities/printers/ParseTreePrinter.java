package org.processj.utilities.printers;

import org.processj.Phase;
import org.processj.ast.alt.AltCase;
import org.processj.ast.alt.AltStat;
import org.processj.ast.alt.Guard;
import org.processj.ast.expression.ArrayAccessExpr;
import org.processj.ast.expression.ArrayLiteral;
import org.processj.ast.expression.Assignment;
import org.processj.ast.expression.BinaryExpr;
import org.processj.utilities.Visitor;
import org.processj.ast.*;

public class ParseTreePrinter implements Visitor<AST> {
    public int indent=0;

    private String indent(int line) throws Phase.Error {
        String s = "" + line + ": ";
        int l = 4-s.length();
        for (int i=0; i<indent+l; i++) {
            s = s + " ";
        }
        return s;
    }

    public ParseTreePrinter() throws Phase.Error {
    }

    // AltCase
    public AST visitAltCase(AltCase ac) throws Phase.Error {
        System.out.println(indent(ac.line) + "AltCase:");
        indent += 2;
        Visitor.super.visitAltCase(ac);
        indent -= 2;
        return null;
    }
    // AltStat
    public AST visitAltStat(AltStat as) throws Phase.Error {
        System.out.println(indent(as.line) + "AltStat:");
        indent += 2;
        Visitor.super.visitAltStat(as);
        indent -= 2;
        return null;
    }
    // ArrayAccessExpr
    public AST visitArrayAccessExpr(ArrayAccessExpr ae) throws Phase.Error {
        System.out.println(indent(ae.line) + "ArrayAccessExpr:");
        indent += 2;
        Visitor.super.visitArrayAccessExpr(ae);
        indent -=2;
        return null;
    }
    // ArrayLiteral
    public AST visitArrayLiteral(ArrayLiteral al) throws Phase.Error {
        System.out.println(indent(al.line) + "ArrayLiteral:");
        indent += 2;
        Visitor.super.visitArrayLiteral(al);
        indent -= 2;
        return null;
    }
    // ArrayType
    public AST visitArrayType(ArrayType at) throws Phase.Error {
        System.out.print(indent(at.line) + "ArrayType:");
        for (int i=0;i<at.getDepth();i++)
            System.out.print("[]");
        System.out.println();
        indent += 2;
        Visitor.super.visitArrayType(at);
        indent -= 2;
        return null;
    }
    // Assignment
    public AST visitAssignment(Assignment as) throws Phase.Error {
        System.out.println(indent(as.line) + "Assignment:");
        indent += 2;
        Visitor.super.visitAssignment(as);
        System.out.println(indent(as.line) + "Operator: " + as.opString());
        indent -= 2;
        return null;
    }
    // BinaryExpr
    public AST visitBinaryExpr(BinaryExpr be) throws Phase.Error {
        System.out.println(indent(be.line) + "BinaryExpr:");
        indent += 2;
        Visitor.super.visitBinaryExpr(be);
        System.out.println(indent(be.line) + "Operator: " + be.opString());
        indent -= 2;
        return null;
    }
    // Block
    public AST visitBlock(Block bl) throws Phase.Error {
        System.out.println(indent(bl.line) + "Block:");
        indent += 2;
        Visitor.super.visitBlock(bl);
        indent -= 2;
        return null;
    }
    // BreakStat
    /** BREAK STATEMENT */
    public AST visitBreakStat(BreakStat bs) throws Phase.Error {
        System.out.println(indent(bs.line) + "BreakStat");
        indent += 2;
        Visitor.super.visitBreakStat(bs);
        indent -= 2;
        return null;
    }
    // CastExpr
    public AST visitCastExpr(CastExpr ce) throws Phase.Error {
        System.out.println(indent(ce.line) + "CastExpr:");
        indent += 2;
        Visitor.super.visitCastExpr(ce);
        indent -= 2;
        return null;
    }
    // ChannelType
    public AST visitChannelType(ChannelType ct) throws Phase.Error {
        System.out.println(indent(ct.line) + "ChannelType:");
        indent += 2;
        Visitor.super.visitChannelType(ct);
        indent -= 2;
        return null;
    }
    // ChannelEndExpr
    public AST visitChannelEndExpr(ChannelEndExpr ce) throws Phase.Error {
        System.out.println(indent(ce.line) + "ChannelEndExpr:");
        indent += 2;
        Visitor.super.visitChannelEndExpr(ce);
        indent -= 2;
        return null;
    }
    // ChannelEndType
    public AST visitChannelEndType(ChannelEndType ce) throws Phase.Error {
        System.out.println(indent(ce.line) + "ChannelEndType:");
        indent += 2;
        Visitor.super.visitChannelEndType(ce);
        indent -= 2;
        return null;
    }
    // ChannelReadExpr
    public AST visitChannelReadExpr(ChannelReadExpr cr) throws Phase.Error {
        System.out.println(indent(cr.line) + "ChannelReadExpr:");
        indent += 2;
        Visitor.super.visitChannelReadExpr(cr);
        indent -= 2;
        return null;
    }
    // ChannelWriteStat
    public AST visitChannelWriteStat(ChannelWriteStat cw) throws Phase.Error {
        System.out.println(indent(cw.line) + "ChannelWriteStat:");
        indent += 2;
        Visitor.super.visitChannelWriteStat(cw);
        indent -= 2;
        return null;
    }
    // ClaimStat
    public AST visitClaimStat(ClaimStat cs) throws Phase.Error {
        System.out.println(indent(cs.line) + "ClaimStat:");
        indent += 2;
        Visitor.super.visitClaimStat(cs);
        indent -= 2;
        return null;
    }
    // Compilation
    public AST visitCompilation(Compilation co) throws Phase.Error {
        System.out.println(indent(co.line) + "Compilation:");
        indent += 2;
        Visitor.super.visitCompilation(co);
        indent -= 2;
        return null;
    }
    // ConstantDecl
    public AST visitConstantDecl(ConstantDecl cd) throws Phase.Error {
        System.out.println(indent(cd.line) + "ConstantDecl:");
        indent += 2;
        Visitor.super.visitConstantDecl(cd);
        indent -= 2;
        return null;
    }
    // ContinueStat
    public AST visitContinueStat(ContinueStat cs) throws Phase.Error {
        System.out.println(indent(cs.line) + "Continue Statement");
        indent += 2;
        Visitor.super.visitContinueStat(cs);
        indent -= 2;
        return null;
    }
    // DoStat
    public AST visitDoStat(DoStat ds) throws Phase.Error {
        System.out.println(indent(ds.line) + "DoStat:");
        indent += 2;
        Visitor.super.visitDoStat(ds);
        indent -= 2;
        return null;
    }
    // ExprStat
    public AST visitExprStat(ExprStat es) throws Phase.Error {
        System.out.println(indent(es.line) + "ExprStat:");
        indent += 2;
        Visitor.super.visitExprStat(es);
        indent -= 2;
        return null;
    }
    // ExternType
    public AST visitExternType(ExternType et) throws Phase.Error {
        System.out.println(indent(et.line) + "ExternType:");
        indent += 2;
        Visitor.super.visitExternType(et);
        indent -= 2;
        return null;
    }
    // ForStat
    public AST visitForStat(ForStat fs) throws Phase.Error {
        System.out.println(indent(fs.line) + "ForStat:");
        indent += 2;
        Visitor.super.visitForStat(fs);
        indent -= 2;
        return null;
    }
    // Guard
    public AST visitGuard(Guard g) throws Phase.Error {
        System.out.println(indent(g.line) + "Guard:");
        indent += 2;
        Visitor.super.visitGuard(g);
        indent -= 2;
        return null;
    }
    // IfStat
    public AST visitIfStat(IfStat is) throws Phase.Error {
        System.out.println(indent(is.line) + "IfStat:");
        indent += 2;
        Visitor.super.visitIfStat(is);
        indent -= 2;
        return null;
    }
    // ImplicitImport
    public AST visitImplicitImport(ImplicitImport im) throws Phase.Error {
        System.out.println(indent(im.line) + "ImplicitImport:");
        indent += 2;
        Visitor.super.visitImplicitImport(im);
        indent -= 2;
        return null;
    }
    // Import
    public AST visitImport(Import im) throws Phase.Error {
        System.out.println(indent(im.line) + "Import:");
        indent += 2;
        Visitor.super.visitImport(im);
        indent -= 2;
        return null;
    }
    // Invocation
    public AST visitInvocation(Invocation in) throws Phase.Error {
        System.out.println(indent(in.line) + "Invocation:");
        indent += 2;
        Visitor.super.visitInvocation(in);
        indent -= 2;
        return null;
    }
    // LocalDecl
    public AST visitLocalDecl(LocalDecl ld) throws Phase.Error {
        System.out.println(indent(ld.line) + "LocalDecl:");
        indent += 2;
        Visitor.super.visitLocalDecl(ld);
        indent -= 2;
        return null;
    }
    // Modifier
    public AST visitModifier(Modifier mo) throws Phase.Error {
        System.out.println(indent(mo.line) + "Modifier (" + mo + ")");
        return null;
    }
    // Name
    public AST visitName(Name na) throws Phase.Error {
        System.out.println(indent(na.line) + "Name = " + na);
        return null;
    }
    // NamedType
    public AST visitNamedType(NamedType nt) throws Phase.Error {
        System.out.println(indent(nt.line)+ "NamedType:");
        indent += 2;
        Visitor.super.visitNamedType(nt);
        indent -= 2;
        return null;
    }
    // NameExpr
    public AST visitNameExpr(NameExpr ne) throws Phase.Error {
        System.out.println(indent(ne.line) + "NameExpr:");
        indent += 2;
        Visitor.super.visitNameExpr(ne);
        indent -= 2;
        return null;
    }
    // NewArray
    public AST visitNewArray(NewArray ne) throws Phase.Error {
        System.out.println(indent(ne.line) + "New Array");
        indent += 2;
        Visitor.super.visitNewArray(ne);
        indent -= 2;
        return null;
    }
    // NewMobile
    public AST visitNMewMobile(NewMobile nm) throws Phase.Error {
        System.out.println(indent(nm.line) + "NewMobile:");
        indent += 2;
        Visitor.super.visitNewMobile(nm);
        indent -= 2;
        return null;
    }
    // ParamDecl
    public AST visitParamDecl(ParamDecl pd) throws Phase.Error {
        System.out.println(indent(pd.line) + "ParamDecl: ");
        indent += 2;
        Visitor.super.visitParamDecl(pd);
        indent -= 2;
        return null;
    }
    // ParBlock
    public AST visitParBlock(ParBlock pb) throws Phase.Error {
        System.out.println(indent(pb.line) + "ParBlock:");
        indent += 2;
        Visitor.super.visitParBlock(pb);
        indent -= 2;
        return null;
    }
    // Pragma
    public AST visitPragma(Pragma p) throws Phase.Error {
        System.out.println(indent(p.line) + "Pragma:");
        indent += 2;
        Visitor.super.visitPragma(p);
        indent -= 2;
        return null;
    }
    // PrimitiveLiteral
    public AST visitPrimitiveLiteral(PrimitiveLiteral li) throws Phase.Error {
        System.out.println(indent(li.line) + "PrimtiveLiteral = " + li);
        indent += 2;
        Visitor.super.visitPrimitiveLiteral(li);
        indent -= 2;
        return null;
    }
    // PrimitiveType
    public AST visitPrimitiveType(PrimitiveType pt) throws Phase.Error {
        System.out.println(indent(pt.line) + "PrimitiveType = " + pt);
        return null;
    }
    // ProcTypeDecl
    public AST visitProcTypeDecl(ProcTypeDecl pd) throws Phase.Error {
        System.out.println(indent(pd.line) + "ProcTypeDecl:");
        indent += 2;

        System.out.println(indent(pd.line) + "Annotations: " + pd.getAnnotations().toString());
        Visitor.super.visitProcTypeDecl(pd);
        indent -= 2;
        return null;
    }
    // ProtocolLiteral
    public AST visitProtocolLiteral(ProtocolLiteral pl) throws Phase.Error {
        System.out.println(indent(pl.line) + "ProtocolLiteral:");
        indent += 2;
        Visitor.super.visitProtocolLiteral(pl);
        indent -=2;
        return null;
    }
    // ProtocolCase
    public AST visitProtocolCase(ProtocolCase pc) throws Phase.Error {
        System.out.println(indent(pc.line) + "ProtocolCase:");
        indent += 2;
        Visitor.super.visitProtocolCase(pc);
        indent -=2;
        return null;
    }
    // ProtocolTypeDecl
    public AST visitProtocolTypeDecl(ProtocolTypeDecl pd) throws Phase.Error {
        System.out.println(indent(pd.line) + "ProtocolTypeDecl:");
        indent += 2;
        Visitor.super.visitProtocolTypeDecl(pd);
        indent -= 2;
        return null;
    }
    // RecordAccess
    public AST visitRecordAccess(RecordAccess ra) throws Phase.Error {
        System.out.println(indent(ra.line) + "RecordAccess:");
        indent += 2;
        Visitor.super.visitRecordAccess(ra);
        indent -= 2;
        return null;
    }
    // RecordLiteral
    public AST visitRecordLiteral(RecordLiteral rl) throws Phase.Error {
        System.out.println(indent(rl.line) + "RecordLiteral:");
        indent += 2;
        Visitor.super.visitRecordLiteral(rl);
        indent -=2;
        return null;
    }
    // RecordMember
    public AST visitRecordMember(RecordMember rm) throws Phase.Error {
        System.out.println(indent(rm.line) + "RecordMember:");
        indent +=2;
        Visitor.super.visitRecordMember(rm);
        indent -=2;
        return null;
    }
    // RecordTypeDecl
    public AST visitRecordTypeDecl(RecordTypeDecl rt) throws Phase.Error {
        System.out.println(indent(rt.line)+ "RecordTypeDecl:");
        indent += 2;
        Visitor.super.visitRecordTypeDecl(rt);
        indent -= 2;
        return null;
    }
    // ReturnStat
    public AST visitReturnStat(ReturnStat rs) throws Phase.Error {
        if (rs.getExpression() == null)
            System.out.println(indent(rs.line) + "Return");
        else
            System.out.println(indent(rs.line) + "Return:");
        indent += 2;
        Visitor.super.visitReturnStat(rs);
        indent -= 2;
        return null;
    }
    // Sequence
    public AST visitSequence(Sequence se) throws Phase.Error {
        System.out.println(indent(se.line) + "Sequence:[" + se.size() + " nodes]");
        int i = 0;
        for (Object a : se) {
            AST b = (AST)a;
            if (b != null) {
                System.out.println(indent(b.line) + "Sequence[" + i++ + "]:");
                indent += 2;
                try {
                    b.visit(this);
                } catch (org.processj.Phase.Error error) {
                    throw new RuntimeException(error);
                }
                indent -= 2;
            } else
                System.out.println(indent(se.line) + "Sequence[" + i++ + "]: = null");
        }
        return null;
    }
    // SkipStat
    public AST visitSkipStat(SkipStat ss) throws Phase.Error {
        System.out.println(indent(ss.line) + "SkipStat");
        return null;
    }
    // StopStat
    public AST visitStopStat(SkipStat ss) throws Phase.Error {
        System.out.println(indent(ss.line) + "StopStat");
        return null;
    }
    // SuspendStat
    public AST visitSuspendStat(SuspendStat ss) throws Phase.Error {
        System.out.println(indent(ss.line) + "SuspendStat:");
        indent += 2;
        Visitor.super.visitSuspendStat(ss);
        indent -= 2;
        return null;
    }
    // SwitchGroup
    public AST visitSwitchGroup(SwitchGroup sg) throws Phase.Error {
        System.out.println(indent(sg.line) + "SwitchGroup:");
        indent += 2;
        Visitor.super.visitSwitchGroup(sg);
        indent -= 2;
        return null;
    }
    // SwitchLabel
    public AST visitSwitchLabel(SwitchLabel sl) throws Phase.Error {
        System.out.println(indent(sl.line) + "SwitchLabel:");
        indent += 2;
        Visitor.super.visitSwitchLabel(sl);
        indent -= 2;
        return null;
    }
    // SwitchStat
    public AST visitSwitchStat(SwitchStat st) throws Phase.Error {
        System.out.println(indent(st.line) + "SwitchStat:");
        indent += 2;
        Visitor.super.visitSwitchStat(st);
        indent -= 2;
        return null;
    }
    // SyncStat
    public AST visitSyncStat(SyncStat ss) throws Phase.Error {
        System.out.println(indent(ss.line) + "SyncStat");
        indent += 2;
        Visitor.super.visitSyncStat(ss);
        indent -= 2;
        return null;
    }
    // Ternary
    public AST visitTernary(Ternary te) throws Phase.Error {
        System.out.println(indent(te.line) + "Ternary:");
        indent += 2;
        Visitor.super.visitTernary(te);
        indent -= 2;
        return null;
    }
    // TimeoutStat
    public AST visitTimeoutStat(TimeoutStat ts) throws Phase.Error {
        System.out.println(indent(ts.line) + "TimeoutStat:");
        indent += 2;
        Visitor.super.visitTimeoutStat(ts);
        indent -= 2;
        return null;
    }
    // UnaryPostExpr
    public AST visitUnaryPostExpr(UnaryPostExpr up) throws Phase.Error {
        System.out.println(indent(up.line) + "UnaryPostExpr:");
        indent += 2;
        Visitor.super.visitUnaryPostExpr(up);
	System.out.println(indent(up.line) + "Operator = " + up.opString());
        indent -= 2;
        return null;
    }
    // UnaryPreExpr
    public AST visitUnaryPreExpr(UnaryPreExpr up) throws Phase.Error {
        System.out.println(indent(up.line) + "UnaryPreExpr:");
        indent += 2;
        Visitor.super.visitUnaryPreExpr(up);
        System.out.println(indent(up.line) + "Operator = " + up.opString());
        indent -= 2;
        return null;
    }

    // WhileStat
    public AST visitWhileStat(WhileStat ws) throws Phase.Error {
        System.out.println(indent(ws.line) + "WhileStat:");
        indent += 2;
        Visitor.super.visitWhileStat(ws);
        indent -= 2;
        return null;
    }
}