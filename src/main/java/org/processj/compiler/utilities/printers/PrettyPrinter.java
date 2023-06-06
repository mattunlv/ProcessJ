package org.processj.compiler.utilities.printers;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.alt.AltCase;
import org.processj.compiler.ast.alt.AltStat;
import org.processj.compiler.ast.expression.ArrayAccessExpr;
import org.processj.compiler.ast.expression.ArrayLiteral;
import org.processj.compiler.ast.ArrayType;
import org.processj.compiler.ast.expression.Assignment;
import org.processj.compiler.ast.expression.BinaryExpr;
import org.processj.compiler.ast.Block;
import org.processj.compiler.ast.BreakStat;
import org.processj.compiler.ast.CastExpr;
import org.processj.compiler.ast.ChannelEndExpr;
import org.processj.compiler.ast.ChannelEndType;
import org.processj.compiler.ast.ChannelReadExpr;
import org.processj.compiler.ast.ChannelType;
import org.processj.compiler.ast.ChannelWriteStat;
import org.processj.compiler.ast.ClaimStat;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.ConstantDecl;
import org.processj.compiler.ast.ContinueStat;
import org.processj.compiler.ast.DoStat;
import org.processj.compiler.ast.ExprStat;
import org.processj.compiler.ast.ForStat;
import org.processj.compiler.ast.alt.Guard;
import org.processj.compiler.ast.IfStat;
import org.processj.compiler.ast.Import;
import org.processj.compiler.ast.Invocation;
import org.processj.compiler.ast.LocalDecl;
import org.processj.compiler.ast.Modifier;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.NameExpr;
import org.processj.compiler.ast.NamedType;
import org.processj.compiler.ast.NewArray;
import org.processj.compiler.ast.NewMobile;
import org.processj.compiler.ast.ParBlock;
import org.processj.compiler.ast.ParamDecl;
import org.processj.compiler.ast.Pragma;
import org.processj.compiler.ast.PrimitiveLiteral;
import org.processj.compiler.ast.PrimitiveType;
import org.processj.compiler.ast.ProcTypeDecl;
import org.processj.compiler.ast.ProtocolCase;
import org.processj.compiler.ast.ProtocolLiteral;
import org.processj.compiler.ast.ProtocolTypeDecl;
import org.processj.compiler.ast.RecordAccess;
import org.processj.compiler.ast.RecordLiteral;
import org.processj.compiler.ast.RecordMember;
import org.processj.compiler.ast.RecordTypeDecl;
import org.processj.compiler.ast.ReturnStat;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.SkipStat;
import org.processj.compiler.ast.Statement;
import org.processj.compiler.ast.StopStat;
import org.processj.compiler.ast.SuspendStat;
import org.processj.compiler.ast.SwitchGroup;
import org.processj.compiler.ast.SwitchLabel;
import org.processj.compiler.ast.SwitchStat;
import org.processj.compiler.ast.SyncStat;
import org.processj.compiler.ast.Ternary;
import org.processj.compiler.ast.TimeoutStat;
import org.processj.compiler.ast.UnaryPostExpr;
import org.processj.compiler.ast.UnaryPreExpr;
import org.processj.compiler.ast.WhileStat;
import org.processj.compiler.phases.phase.Visitor;

public class PrettyPrinter implements Visitor<Void> {
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
	}

	@Override
    public final Void visitAltCase(AltCase ac) {
		System.out.print(tab());
		if (ac.getPreconditionExpression() != null) {
			System.out.print("(");
            try {
                ac.getPreconditionExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            System.out.print(") && ");
		}
        try {
            ac.getGuard().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" : ");
		indent += 2;
        try {
            ac.getStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitAltStat(AltStat as) throws Phase.Error {
		p("alt {");
		indent += 2;
		as.visitChildren(this);
		indent -= 2;
		p("}");
		return null;
	}

	@Override
    public final Void visitArrayAccessExpr(ArrayAccessExpr ae) {
        try {
            ae.targetExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print("[");
        try {
            ae.indexExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print("]");
		return null;
	}

	@Override
    public final Void visitArrayLiteral(ArrayLiteral al) throws Phase.Error{
		// TODO
		return al.visitChildren(this);
	}

	@Override
    public final Void visitArrayType(ArrayType at) {

		System.out.print(at.toString());

		return null;

	}

	@Override
    public final Void visitAssignment(Assignment as) {
        try {
            as.left().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" " + as.opString() + " ");
        try {
            as.getRight().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitBinaryExpr(BinaryExpr be) {
        try {
            be.left().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" " + be.opString() + " ");
        try {
            be.right().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitBlock(Block bl) {
		System.out.println(tab() + "{");
		indent += 2;
		for (Statement st : bl.getStatements()) {
			indent += 2;
			if (st == null) {
				System.out.println(tab() + ";");
			} else {
                try {
                    st.visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                // System.out.println(";");
			}
			indent -= 2;
		}
		indent -= 2;
		System.out.println(tab() + "}");
		return null;
	}

	@Override
    public final Void visitBreakStat(BreakStat bs) {
		System.out.print(tab() + "break");
		if (bs.getTarget() != null) {
			System.out.print(" ");
            try {
                bs.getTarget().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        }
		System.out.println(";");
		return null;
	}

	@Override
    public final Void visitCastExpr(CastExpr ce) throws Phase.Error {
		// TODO
		return ce.visitChildren(this);
	}

	@Override
    public final Void visitChannelType(ChannelType ct) {
		String modString = ct.modString();
		System.out.print(modString);
		if (!modString.equals(""))
			System.out.print(" ");
		System.out.print("chan<");
        try {
            ct.getComponentType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(">");
		return null;
	}

	@Override
    public final Void visitChannelEndExpr(ChannelEndExpr ce) {
        try {
            ce.getChannelType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print("." + (ce.isRead() ? "read" : "write"));
		return null;
	}

	@Override
    public final Void visitChannelEndType(ChannelEndType ct) {
		if (ct.isSharedEnd())
			System.out.print("shared ");
		System.out.print("chan<");
        try {
            ct.getComponentType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(">." + (ct.isReadEnd() ? "read" : "write"));
		return null;
	}

	@Override
    public final Void visitChannelReadExpr(ChannelReadExpr cr) {
        try {
            cr.getExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(".read(");
		if (cr.getExtendedRendezvous() != null) {
			System.out.println("{");
			indent += 2;
            try {
                cr.getExtendedRendezvous().getStatements().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            indent -= 2;
			System.out.print("}");
		}
		System.out.print(")");
		return null;
	}

	@Override
    public final Void visitChannelWriteStat(ChannelWriteStat cw) {
		System.out.print(tab());

        try {
            cw.getTargetExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(".write(");
        try {
            cw.getWriteExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(");");
		return null;
	}

	@Override
    public final Void visitClaimStat(ClaimStat cs) throws Phase.Error {
		// TODO
		return cs.visitChildren(this);
	}

	@Override
    public final Void visitCompilation(Compilation co) throws Phase.Error  {
		System.out.println("Compilation");
		return co.visitChildren(this);
	}

	@Override
    public final Void visitConstantDecl(ConstantDecl cd) {
		System.out.print(tab());
		printModifierSequence(cd.modifiers());
		if (cd.modifiers().size() > 0)
			System.out.print(" ");
        try {
            cd.getType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        try {
            cd.getName().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        try {
            cd.getInitializationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");
		return null;
	}

	@Override
    public final Void visitContinueStat(ContinueStat cs) {
		System.out.print("continue");
		if (cs.getTarget() != null) {
			System.out.print(" ");
            try {
                cs.getTarget().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        }
		return null;
	}

	@Override
    public final Void visitDoStat(DoStat ds) throws Phase.Error {
		System.out.print(tab() + "do ");

			System.out.println("{");
			indent += 2;

            ds.getStatements().visit(this);
            indent -= 2;
			System.out.print(tab() + "} while (");

            ds.getEvaluationExpression().visit(this);

            System.out.print(")");

		System.out.println("");
		return null;
	}

	@Override
    public final Void visitExprStat(ExprStat es) {
		System.out.print(tab());
        try {
            es.getExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");
		return null;
	}

	@Override
    public final Void visitForStat(ForStat fs) throws Phase.Error {
		System.out.print(tab());
		System.out.print("for (");
		if (fs.getInitializationExpression() != null) {
			if (fs.getInitializationExpression().size() > 0) {
				// there are some children - if the first is a localDecl so are the rest!
				if (fs.getInitializationExpression().child(0) instanceof LocalDecl) {
					LocalDecl ld = (LocalDecl) fs.getInitializationExpression().child(0);
					System.out.print(ld.getType().toString() + " ");
					for (int i = 0; i < fs.getInitializationExpression().size(); i++) {
						ld = (LocalDecl) fs.getInitializationExpression().child(i);
                        try {
                            ld.getName().visit(this);
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                        try {
                            ld.getInitializationExpression().visit(this);
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                        if (i < fs.getInitializationExpression().size() - 1)
							System.out.print(",");
					}
				} else {
					for (Statement es : fs.getInitializationExpression())
                        try {
                            es.visit(this);
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                }
			}
		}
		System.out.print(";");
		if (fs.getEvaluationExpression() != null)
            try {
                fs.getEvaluationExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        System.out.print(";");
		if (fs.getIncrementExpression() != null) {
			for (int i = 0; i < fs.getIncrementExpression().size(); i++) {
				if (fs.getIncrementExpression().child(i) instanceof ExprStat) {
					ExprStat es = (ExprStat) fs.getIncrementExpression().child(i);
                    try {
                        es.getExpression().visit(this);
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
                }
			}

		}
		System.out.print(")");

        System.out.println(" {");
        indent += 2;
        fs.getStatements().visit(this);
        indent -= 2;
        System.out.println(tab() + "}");
		return null;
	}

	@Override
    public final Void visitGuard(Guard gu) {
		if (gu.getStatement() instanceof ExprStat)
            try {
                ((ExprStat) gu.getStatement()).getExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        else if (gu.getStatement() instanceof SkipStat)
			System.out.print("skip");
		else if (gu.getStatement() instanceof TimeoutStat) {
			TimeoutStat ts = (TimeoutStat) gu.getStatement();
            try {
                ts.getTimerExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            System.out.print(".timeout(");
            try {
                ts.getDelayExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            System.out.print(")");
		}
		return null;
	}

	@Override
    public final Void visitIfStat(IfStat is) throws Phase.Error {
		System.out.print(tab());
		System.out.print("if (");
        try {
            is.evaluationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(")");
		System.out.println(" {");
		indent += 2;

        is.getThenStatements().visit(this);

        indent -= 2;

        System.out.print(tab() + "}");

		System.out.print(" else");

		System.out.println(" {");
        indent += 2;
        is.getElseBody().visit(this);

        indent -= 2;
		System.out.println(tab() + "}");

		return null;
	}

	@Override
    public final Void visitImport(Import im) {
		// System.out.print(tab() + "import " + im.packageName() + ".");
		// if (im.all())
		// System.out.println("*;");
		// else
		// System.out.println(im.file() + ";");
		return null;
	}

	@Override
    public final Void visitInvocation(Invocation in) {
		if (in.getTarget() != null)
            try {
                in.getTarget().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        System.out.print(in.getProcedureName() + "(");
		for (int i = 0; i < in.getParameters().size(); i++) {
            try {
                in.getParameters().child(i).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            if (i < in.getParameters().size() - 1)
				System.out.print(",");
		}
		System.out.print(")");
		return null;
	}

	@Override
    public final Void visitLocalDecl(LocalDecl ld) {
		System.out.print(tab());
		if (ld.isConstant())
			System.out.print("const ");

        try {
            ld.getType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        try {
            ld.getName().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        try {
            ld.getInitializationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");
		return null;
	}

	@Override
    public final Void visitModifier(Modifier mo) {
		System.out.print(mo.toString());
		return null;
	}

	public void printModifierSequence(Sequence<Modifier> mods) {
		int i = 0;
		for (Modifier m : mods) {
            try {
                m.visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            if (i < mods.size() - 1)
				System.out.print(" ");
			i++;
		}
	}

	@Override
    public final Void visitName(Name na) {
		System.out.print(na);
		return null;
	}

	@Override
    public final Void visitNamedType(NamedType nt) {
        try {
            nt.getName().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitNameExpr(NameExpr ne) {
        try {
            ne.getName().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitNewArray(NewArray ne)  throws Phase.Error {
		// TODO
		return ne.visitChildren(this);
	}

	@Override
    public final Void visitNewMobile(NewMobile nm) {
		System.out.print(tab() + "new mobile ");
        try {
            nm.name().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitParamDecl(ParamDecl pd) {
		if (pd.isConstant())
			System.out.print("const ");
        try {
            pd.getType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        System.out.print(pd.getName());

        return null;
	}

	@Override
    public final Void visitParBlock(ParBlock pb) {

		// TODO - don't forget that there are barriers to enroll on.
		System.out.println(tab() + "par {");
		indent += 2;
        try {
            pb.getStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;
		System.out.println(tab() + "}");
		return null;
	}

	@Override
    public final Void visitPragma(Pragma pr) {
		System.out.println(tab() + "#pragma " + pr + " " + (pr.getValue() == null ? "" : pr.getValue()));
		return null;
	}

	@Override
    public final Void visitPrimitiveLiteral(PrimitiveLiteral li) {
		System.out.print(li.getText());
		return null;
	}

	@Override
    public final Void visitPrimitiveType(PrimitiveType pt) {
		System.out.print(pt.toString());
		return null;
	}

	@Override
    public final Void visitProcTypeDecl(ProcTypeDecl pd) {
		System.out.print(tab());
		printModifierSequence(pd.modifiers());
		if (pd.modifiers().size() > 0)
			System.out.print(" ");
        try {
            pd.getReturnType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
		System.out.print(pd);
		System.out.print("(");
		for (int i = 0; i < pd.getParameters().size(); i++) {
            try {
                pd.getParameters().child(i).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            if (i < pd.getParameters().size() - 1)
				System.out.print(", ");
		}
		System.out.print(")");
		if (pd.implement().size() > 0) {
			System.out.print(" implements ");
			for (int i = 0; i < pd.implement().size(); i++) {
                try {
                    pd.implement().child(i).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (i < pd.implement().size() - 1)
					System.out.print(", ");
			}
		}

		if (pd.getBody() != null) {
			System.out.println(" {");
			indent += 2;
            try {
                pd.getBody().getStatements().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            indent -= 2;
			System.out.println(tab() + "}");
		} else
			System.out.println(" ;");

		return null;
	}

	@Override
    public final Void visitProtocolLiteral(ProtocolLiteral pl)  throws Phase.Error {
		// TODO
		return pl.visitChildren(this);
	}

	@Override
    public final Void visitProtocolCase(ProtocolCase pc)  throws Phase.Error {
		// TODO
		return pc.visitChildren(this);
	}

	@Override
    public final Void visitProtocolTypeDecl(ProtocolTypeDecl pd) throws Phase.Error  {
		// TODO
		return pd.visitChildren(this);
	}

	@Override
    public final Void visitRecordAccess(RecordAccess ra) {
        try {
            ra.record().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(".)");
        try {
            ra.field().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitRecordLiteral(RecordLiteral rl)  throws Phase.Error {
		// TODO
		return rl.visitChildren(this);
	}

	@Override
    public final Void visitRecordMember(RecordMember rm) {
		System.out.print(tab());
        try {
            rm.getType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        try {
            rm.getName().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");
		return null;
	}

	@Override
    public final Void visitRecordTypeDecl(RecordTypeDecl rt) {
		System.out.print(tab());
		printModifierSequence(rt.modifiers());
		if (rt.modifiers().size() > 0)
			System.out.print(" ");
		System.out.print("record ");
		System.out.print(rt);
		if (rt.getExtends().size() > 0) {
			System.out.print(" extends ");
			for (int i = 0; i < rt.getExtends().size(); i++) {
                try {
                    rt.getExtends().child(i).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (i < rt.getExtends().size() - 1)
					System.out.print(", ");
			}
		}
		System.out.println(" {");
		indent += 2;
        try {
            rt.getBody().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;
		System.out.println(tab() + "}");
		return null;
	}

	@Override
    public final Void visitReturnStat(ReturnStat rs) {
		System.out.print(tab() + "return");
		if (rs.getExpression() != null) {
			System.out.print(" ");
            try {
                rs.getExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        }
		return null;
	}

	@Override
    public final Void visitSequence(Sequence se) {
		// se.visitChildren(this);
		for (int i = 0; i < se.size(); i++) {
			if (se.child(i) != null)
                try {
                    se.child(i).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
        }
		return null;
	}

	@Override
    public final Void visitSkipStat(SkipStat ss) {
		System.out.println("skip;");
		return null;
	}

	@Override
    public final Void visitStopStat(StopStat ss) {
		System.out.println("stop;");
		return null;
	}

	@Override
    public final Void visitSuspendStat(SuspendStat ss) {
		System.out.print("suspend resume with (");
        try {
            ss.params().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(")");
		return null;
	}

	@Override
    public final Void visitSwitchGroup(SwitchGroup sg) {
        try {
            sg.getLabels().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent += 2;
        try {
            sg.getStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;
		return null;
	}

	@Override
    public final Void visitSwitchLabel(SwitchLabel sl) {
		if (sl.isDefault())
			System.out.println(tab() + "default:");
		else
			System.out.println(tab() + "case " + sl.getExpression() + ":");
		return null;
	}

	@Override
    public final Void visitSwitchStat(SwitchStat st) {
		System.out.print(tab() + "switch (");
        try {
            st.getEvaluationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(") {");
		indent += 2;
        try {
            st.switchBlocks().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;
		System.out.println(tab() + "}");
		return null;
	}

	@Override
    public final Void visitSyncStat(SyncStat st) {
		System.out.print(tab());
        try {
            st.barrier().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(".sync();");
		return null;
	}

	@Override
    public final Void visitTernary(Ternary te) {
        try {
            te.getEvaluationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ? ");
        try {
            te.thenPart().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" : ");
        try {
            te.elsePart().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitTimeoutStat(TimeoutStat ts) {
		System.out.print(tab());
        try {
            ts.getTimerExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(".timeout(");
        try {
            ts.getDelayExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(");");
		return null;
	}

	@Override
    public final Void visitUnaryPostExpr(UnaryPostExpr up) {
        try {
            up.getExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(up.opString());
		return null;
	}

	@Override
    public final Void visitUnaryPreExpr(UnaryPreExpr up) {
		System.out.print(up.opString());
        try {
            up.getExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitWhileStat(WhileStat ws) throws Phase.Error {
		System.out.print(tab() + "while (");
        try {
            ws.getEvaluationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(")");

        System.out.println(" {");
        indent += 2;
        ws.getStatements().visit(this);
        indent -= 2;
        System.out.println(tab() + "}");

        return null;
	}
}