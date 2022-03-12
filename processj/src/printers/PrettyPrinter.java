package printers;

import ast.AST;
import ast.AltCase;
import ast.AltStat;
import ast.ArrayAccessExpr;
import ast.ArrayLiteral;
import ast.ArrayType;
import ast.Assignment;
import ast.BinaryExpr;
import ast.Block;
import ast.BreakStat;
import ast.CastExpr;
import ast.ChannelEndExpr;
import ast.ChannelEndType;
import ast.ChannelReadExpr;
import ast.ChannelType;
import ast.ChannelWriteStat;
import ast.ClaimStat;
import ast.Compilation;
import ast.ConstantDecl;
import ast.ContinueStat;
import ast.DoStat;
import ast.ExprStat;
import ast.ForStat;
import ast.Guard;
import ast.IfStat;
import ast.Import;
import ast.Invocation;
import ast.LocalDecl;
import ast.Modifier;
import ast.Name;
import ast.NameExpr;
import ast.NamedType;
import ast.NewArray;
import ast.NewMobile;
import ast.ParBlock;
import ast.ParamDecl;
import ast.Pragma;
import ast.PrimitiveLiteral;
import ast.PrimitiveType;
import ast.ProcTypeDecl;
import ast.ProtocolCase;
import ast.ProtocolLiteral;
import ast.ProtocolTypeDecl;
import ast.RecordAccess;
import ast.RecordLiteral;
import ast.RecordMember;
import ast.RecordTypeDecl;
import ast.ReturnStat;
import ast.Sequence;
import ast.SkipStat;
import ast.Statement;
import ast.StopStat;
import ast.SuspendStat;
import ast.SwitchGroup;
import ast.SwitchLabel;
import ast.SwitchStat;
import ast.SyncStat;
import ast.Ternary;
import ast.TimeoutStat;
import ast.UnaryPostExpr;
import ast.UnaryPreExpr;
import ast.Var;
import ast.WhileStat;
import utilities.Log;
import utilities.Visitor;

public class PrettyPrinter<T extends AST> extends Visitor<T> {
	public static int indent = 0;

	int lineno = 1;

	private String tab() {

		String s = "";
		if (lineno < 10)
			s = "00" + lineno;
		else if (lineno < 100)
			s = "0" + lineno;
		else
			s = "" + lineno;
		s = s + ":  ";
		lineno++;
		for (int i = 0; i < indent; i++)
			s += " ";
		return s;
	}

	private void p(String s) {
		System.out.println(tab() + s);
	}

	public PrettyPrinter() {
		System.out.println("ProcessJ Pretty Print");
		debug = true;
	}

	public T visitAltCase(AltCase ac) {
		System.out.print(tab());
		if (ac.precondition() != null) {
			System.out.print("(");
			ac.precondition().visit(this);
			System.out.print(") && ");
		}
		ac.guard().visit(this);
		System.out.print(" : ");
		indent += 2;
		ac.stat().visit(this);
		return null;
	}

	public T visitAltStat(AltStat as) {
		p("alt {");
		indent += 2;
		as.visitChildren(this);
		indent -= 2;
		p("}");
		return null;
	}

	public T visitArrayAccessExpr(ArrayAccessExpr ae) {
		ae.target().visit(this);
		System.out.print("[");
		ae.index().visit(this);
		System.out.print("]");
		return null;
	}

	public T visitArrayLiteral(ArrayLiteral al) {
		// TODO
		return al.visitChildren(this);
	}

	public T visitArrayType(ArrayType at) {
		at.baseType().visit(this);
		for (int i = 0; i < at.getDepth(); i++)
			System.out.print("[]");
		return null;
	}

	public T visitAssignment(Assignment as) {
		as.left().visit(this);
		System.out.print(" " + as.opString() + " ");
		as.right().visit(this);
		return null;
	}

	public T visitBinaryExpr(BinaryExpr be) {
		be.left().visit(this);
		System.out.print(" " + be.opString() + " ");
		be.right().visit(this);
		return null;
	}

	public T visitBlock(Block bl) {
		System.out.println(tab() + "{");
		indent += 2;
		for (Statement st : bl.stats()) {
			indent += 2;
			if (st == null) {
				System.out.println(tab() + ";");
			} else {
				st.visit(this);
				// System.out.println(";");
			}
			indent -= 2;
		}
		indent -= 2;
		System.out.println(tab() + "}");
		return null;
	}

	public T visitBreakStat(BreakStat bs) {
		System.out.print(tab() + "break");
		if (bs.target() != null) {
			System.out.print(" ");
			bs.target().visit(this);
		}
		System.out.println(";");
		return null;
	}

	public T visitCastExpr(CastExpr ce) {
		// TODO
		return ce.visitChildren(this);
	}

	public T visitChannelType(ChannelType ct) {
		String modString = ct.modString();
		System.out.print(modString);
		if (!modString.equals(""))
			System.out.print(" ");
		System.out.print("chan<");
		ct.baseType().visit(this);
		System.out.print(">");
		return null;
	}

	public T visitChannelEndExpr(ChannelEndExpr ce) {
		ce.channel().visit(this);
		System.out.print("." + (ce.isRead() ? "read" : "write"));
		return null;
	}

	public T visitChannelEndType(ChannelEndType ct) {
		if (ct.isShared())
			System.out.print("shared ");
		System.out.print("chan<");
		ct.baseType().visit(this);
		System.out.print(">." + (ct.isRead() ? "read" : "write"));
		return null;
	}

	public T visitChannelReadExpr(ChannelReadExpr cr) {
		cr.channel().visit(this);
		System.out.print(".read(");
		if (cr.extRV() != null) {
			System.out.println("{");
			indent += 2;
			cr.extRV().stats().visit(this);
			indent -= 2;
			System.out.print("}");
		}
		System.out.print(")");
		return null;
	}

	public T visitChannelWriteStat(ChannelWriteStat cw) {
		System.out.print(tab());

		cw.channel().visit(this);
		System.out.print(".write(");
		cw.expr().visit(this);
		System.out.println(");");
		return null;
	}

	public T visitClaimStat(ClaimStat cs) {
		// TODO
		return cs.visitChildren(this);
	}

	public T visitCompilation(Compilation co) {
		System.out.println("Compilation");
		return co.visitChildren(this);
	}

	public T visitConstantDecl(ConstantDecl cd) {
		System.out.print(tab());
		printModifierSequence(cd.modifiers());
		if (cd.modifiers().size() > 0)
			System.out.print(" ");
		cd.type().visit(this);
		System.out.print(" ");
		cd.var().visit(this);
		System.out.println(";");
		return null;
	}

	public T visitContinueStat(ContinueStat cs) {
		System.out.print("continue");
		if (cs.target() != null) {
			System.out.print(" ");
			cs.target().visit(this);
		}
		return null;
	}

	public T visitDoStat(DoStat ds) {
		System.out.print(tab() + "do ");
		if (ds.stat() instanceof Block) {
			System.out.println("{");
			indent += 2;
			((Block) ds.stat()).stats().visit(this);
			indent -= 2;
			System.out.print(tab() + "} while (");
			ds.expr().visit(this);
			System.out.print(")");
		} else {
			System.out.println("");
			indent += 2;
			ds.stat().visit(this);
			indent -= 2;
			System.out.print(tab() + "while (");
			ds.expr().visit(this);
			System.out.print(");");
		}
		System.out.println("");
		return null;
	}

	public T visitExprStat(ExprStat es) {
		System.out.print(tab());
		es.expr().visit(this);
		System.out.println(";");
		return null;
	}

	public T visitForStat(ForStat fs) {
		System.out.print(tab());
		System.out.print("for (");
		if (fs.init() != null) {
			if (fs.init().size() > 0) {
				// there are some children - if the first is a localDecl so are the rest!
				if (fs.init().child(0) instanceof LocalDecl) {
					LocalDecl ld = (LocalDecl) fs.init().child(0);
					System.out.print(ld.type().typeName() + " ");
					for (int i = 0; i < fs.init().size(); i++) {
						ld = (LocalDecl) fs.init().child(i);
						ld.var().visit(this);
						if (i < fs.init().size() - 1)
							System.out.print(",");
					}
				} else {
					for (Statement es : fs.init())
						es.visit(this);
				}
			}
		}
		System.out.print(";");
		if (fs.expr() != null)
			fs.expr().visit(this);
		System.out.print(";");
		if (fs.incr() != null) {
			for (int i = 0; i < fs.incr().size(); i++) {
				if (fs.incr().child(i) instanceof ExprStat) {
					ExprStat es = (ExprStat) fs.incr().child(i);
					es.expr().visit(this);
				}
			}

		}
		System.out.print(")");
		if (fs.stats() instanceof Block) {
			System.out.println(" {");
			indent += 2;
			((Block) fs.stats()).stats().visit(this);
			indent -= 2;
			System.out.println(tab() + "}");
		} else {
			System.out.println("");
			fs.stats().visit(this);
		}
		return null;
	}

	public T visitGuard(Guard gu) {
		if (gu.guard() instanceof ExprStat)
			((ExprStat) gu.guard()).expr().visit(this);
		else if (gu.guard() instanceof SkipStat)
			System.out.print("skip");
		else if (gu.guard() instanceof TimeoutStat) {
			TimeoutStat ts = (TimeoutStat) gu.guard();
			ts.timer().visit(this);
			System.out.print(".timeout(");
			ts.delay().visit(this);
			System.out.print(")");
		}
		return null;
	}

	public T visitIfStat(IfStat is) {
		System.out.print(tab());
		System.out.print("if (");
		is.expr().visit(this);
		System.out.print(")");
		if (is.thenpart() instanceof Block)
			System.out.println(" {");
		else
			System.out.println("");
		indent += 2;
		if (is.thenpart() instanceof Block)
			((Block) is.thenpart()).stats().visit(this);
		else
			is.thenpart().visit(this);
		indent -= 2;
		if (is.thenpart() instanceof Block)
			System.out.print(tab() + "}");

		if (is.thenpart() instanceof Block && is.elsepart() != null)
			System.out.print(" else");
		if (!(is.thenpart() instanceof Block) && is.elsepart() != null)
			System.out.print(tab() + "else");
		if (is.thenpart() instanceof Block && is.elsepart() == null)
			System.out.println("");

		if (is.elsepart() != null) {
			if (is.elsepart() instanceof Block)
				System.out.println(" {");
			else
				System.out.println("");
			indent += 2;
			if (is.elsepart() instanceof Block)
				((Block) is.elsepart()).stats().visit(this);
			else
				is.elsepart().visit(this);
			indent -= 2;
			if (is.elsepart() instanceof Block)
				System.out.println(tab() + "}");
		}
		return null;
	}

	public T visitImport(Import im) {
		// System.out.print(tab() + "import " + im.packageName() + ".");
		// if (im.all())
		// System.out.println("*;");
		// else
		// System.out.println(im.file() + ";");
		return null;
	}

	public T visitInvocation(Invocation in) {
		if (in.target() != null)
			in.target().visit(this);
		System.out.print(in.procedureName() + "(");
		for (int i = 0; i < in.params().size(); i++) {
			in.params().child(i).visit(this);
			if (i < in.params().size() - 1)
				System.out.print(",");
		}
		System.out.print(")");
		return null;
	}

	public T visitLocalDecl(LocalDecl ld) {
		System.out.print(tab());
		if (ld.isConst())
			System.out.print("const ");

		ld.type().visit(this);
		System.out.print(" ");
		ld.var().visit(this);
		System.out.println(";");
		return null;
	}

	public T visitModifier(Modifier mo) {
		System.out.print(mo.toString());
		return null;
	}

	public void printModifierSequence(Sequence<Modifier> mods) {
		int i = 0;
		for (Modifier m : mods) {
			m.visit(this);
			if (i < mods.size() - 1)
				System.out.print(" ");
			i++;
		}
	}

	public T visitName(Name na) {
		System.out.print(na.getname());
		return null;
	}

	public T visitNamedType(NamedType nt) {
		nt.name().visit(this);
		return null;
	}

	public T visitNameExpr(NameExpr ne) {
		ne.name().visit(this);
		return null;
	}

	public T visitNewArray(NewArray ne) {
		// TODO
		return ne.visitChildren(this);
	}

	public T visitNewMobile(NewMobile nm) {
		System.out.print(tab() + "new mobile ");
		nm.name().visit(this);
		return null;
	}

	public T visitParamDecl(ParamDecl pd) {
		if (pd.isConstant())
			System.out.print("const ");
		pd.type().visit(this);
		System.out.print(" ");
		pd.paramName().visit(this);
		return null;
	}

	public T visitParBlock(ParBlock pb) {

		// TODO - don't forget that there are barriers to enroll on.
		System.out.println(tab() + "par {");
		indent += 2;
		pb.stats().visit(this);
		indent -= 2;
		System.out.println(tab() + "}");
		return null;
	}

	public T visitPragma(Pragma pr) {
		System.out.println(tab() + "#pragma " + pr.pname() + " " + (pr.value() == null ? "" : pr.value()));
		return null;
	}

	public T visitPrimitiveLiteral(PrimitiveLiteral li) {
		System.out.print(li.getText());
		return null;
	}

	public T visitPrimitiveType(PrimitiveType pt) {
		System.out.print(pt.typeName());
		return null;
	}

	public T visitProcTypeDecl(ProcTypeDecl pd) {
		System.out.print(tab());
		printModifierSequence(pd.modifiers());
		if (pd.modifiers().size() > 0)
			System.out.print(" ");
		pd.returnType().visit(this);
		System.out.print(" ");
		pd.name().visit(this);
		System.out.print("(");
		for (int i = 0; i < pd.formalParams().size(); i++) {
			pd.formalParams().child(i).visit(this);
			if (i < pd.formalParams().size() - 1)
				System.out.print(", ");
		}
		System.out.print(")");
		if (pd.implement().size() > 0) {
			System.out.print(" implements ");
			for (int i = 0; i < pd.implement().size(); i++) {
				pd.implement().child(i).visit(this);
				if (i < pd.implement().size() - 1)
					System.out.print(", ");
			}
		}

		if (pd.body() != null) {
			System.out.println(" {");
			indent += 2;
			pd.body().stats().visit(this);
			indent -= 2;
			System.out.println(tab() + "}");
		} else
			System.out.println(" ;");

		return null;
	}

	public T visitProtocolLiteral(ProtocolLiteral pl) {
		// TODO
		return pl.visitChildren(this);
	}

	public T visitProtocolCase(ProtocolCase pc) {
		// TODO
		return pc.visitChildren(this);
	}

	public T visitProtocolTypeDecl(ProtocolTypeDecl pd) {
		// TODO
		return pd.visitChildren(this);
	}

	public T visitRecordAccess(RecordAccess ra) {
		ra.record().visit(this);
		System.out.print(".)");
		ra.field().visit(this);
		return null;
	}

	public T visitRecordLiteral(RecordLiteral rl) {
		// TODO
		return rl.visitChildren(this);
	}

	public T visitRecordMember(RecordMember rm) {
		System.out.print(tab());
		rm.type().visit(this);
		System.out.print(" ");
		rm.name().visit(this);
		System.out.println(";");
		return null;
	}

	public T visitRecordTypeDecl(RecordTypeDecl rt) {
		System.out.print(tab());
		printModifierSequence(rt.modifiers());
		if (rt.modifiers().size() > 0)
			System.out.print(" ");
		System.out.print("record ");
		rt.name().visit(this);
		if (rt.extend().size() > 0) {
			System.out.print(" extends ");
			for (int i = 0; i < rt.extend().size(); i++) {
				rt.extend().child(i).visit(this);
				if (i < rt.extend().size() - 1)
					System.out.print(", ");
			}
		}
		System.out.println(" {");
		indent += 2;
		rt.body().visit(this);
		indent -= 2;
		System.out.println(tab() + "}");
		return null;
	}

	public T visitReturnStat(ReturnStat rs) {
		System.out.print(tab() + "return");
		if (rs.expr() != null) {
			System.out.print(" ");
			rs.expr().visit(this);
		}
		return null;
	}

	public T visitSequence(Sequence se) {
		// se.visitChildren(this);
		for (int i = 0; i < se.size(); i++) {
			if (se.child(i) != null)
				se.child(i).visit(this);
		}
		return null;
	}

	public T visitSkipStat(SkipStat ss) {
		System.out.println("skip;");
		return null;
	}

	public T visitStopStat(StopStat ss) {
		System.out.println("stop;");
		return null;
	}

	public T visitSuspendStat(SuspendStat ss) {
		System.out.print("suspend resume with (");
		ss.params().visit(this);
		System.out.print(")");
		return null;
	}

	public T visitSwitchGroup(SwitchGroup sg) {
		sg.labels().visit(this);
		indent += 2;
		sg.statements().visit(this);
		indent -= 2;
		return null;
	}

	public T visitSwitchLabel(SwitchLabel sl) {
		if (sl.isDefault())
			System.out.println(tab() + "default:");
		else
			System.out.println(tab() + "case " + sl.expr() + ":");
		return null;
	}

	public T visitSwitchStat(SwitchStat st) {
		System.out.print(tab() + "switch (");
		st.expr().visit(this);
		System.out.println(") {");
		indent += 2;
		st.switchBlocks().visit(this);
		indent -= 2;
		System.out.println(tab() + "}");
		return null;
	}

	public T visitSyncStat(SyncStat st) {
		System.out.print(tab());
		st.barrier().visit(this);
		System.out.println(".sync();");
		return null;
	}

	public T visitTernary(Ternary te) {
		te.expr().visit(this);
		System.out.print(" ? ");
		te.trueBranch().visit(this);
		System.out.print(" : ");
		te.falseBranch().visit(this);
		return null;
	}

	public T visitTimeoutStat(TimeoutStat ts) {
		System.out.print(tab());
		ts.timer().visit(this);
		System.out.print(".timeout(");
		ts.delay().visit(this);
		System.out.println(");");
		return null;
	}

	public T visitUnaryPostExpr(UnaryPostExpr up) {
		up.expr().visit(this);
		System.out.print(up.opString());
		return null;
	}

	public T visitUnaryPreExpr(UnaryPreExpr up) {
		System.out.print(up.opString());
		up.expr().visit(this);
		return null;
	}

	public T visitVar(Var va) {
		System.out.print(va.name().getname());
		if (va.init() != null) {
			System.out.print(" = ");
			va.init().visit(this);
		}
		return null;
	}

	public T visitWhileStat(WhileStat ws) {
		System.out.print(tab() + "while (");
		ws.expr().visit(this);
		System.out.print(")");
		if (ws.stat() instanceof Block) {
			System.out.println(" {");
			indent += 2;
			((Block) ws.stat()).stats().visit(this);
			indent -= 2;
			System.out.println(tab() + "}");
		} else
			ws.stat().visit(this);
		return null;
	}
}