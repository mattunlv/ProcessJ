package org.processj.compiler.utilities.printers;

import org.processj.compiler.ast.expression.access.ArrayAccessExpression;
import org.processj.compiler.ast.expression.access.RecordAccessExpression;
import org.processj.compiler.ast.expression.binary.AssignmentExpression;
import org.processj.compiler.ast.expression.binary.BinaryExpression;
import org.processj.compiler.ast.expression.constructing.NewArrayExpression;
import org.processj.compiler.ast.expression.constructing.NewMobileExpression;
import org.processj.compiler.ast.expression.literal.ArrayLiteralExpression;
import org.processj.compiler.ast.expression.literal.PrimitiveLiteralExpression;
import org.processj.compiler.ast.expression.literal.ProtocolLiteralExpression;
import org.processj.compiler.ast.expression.literal.RecordLiteralExpression;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.expression.result.*;
import org.processj.compiler.ast.expression.unary.*;
import org.processj.compiler.ast.expression.yielding.ChannelEndExpression;
import org.processj.compiler.ast.expression.yielding.ChannelReadExpression;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.declarative.LocalDeclaration;
import org.processj.compiler.ast.statement.declarative.ProtocolCase;
import org.processj.compiler.ast.statement.declarative.RecordMemberDeclaration;
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.ast.statement.yielding.ParBlock;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.statement.alt.AltCase;
import org.processj.compiler.ast.statement.alt.AltStatement;
import org.processj.compiler.ast.type.ArrayType;
import org.processj.compiler.ast.type.ChannelEndType;
import org.processj.compiler.ast.type.ChannelType;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.type.ConstantDeclaration;
import org.processj.compiler.ast.statement.alt.GuardStatement;
import org.processj.compiler.ast.Import;
import org.processj.compiler.ast.Modifier;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.type.NamedType;
import org.processj.compiler.ast.type.ParameterDeclaration;
import org.processj.compiler.ast.Pragma;
import org.processj.compiler.ast.type.PrimitiveType;
import org.processj.compiler.ast.type.ProcedureTypeDeclaration;
import org.processj.compiler.ast.type.ProtocolTypeDeclaration;
import org.processj.compiler.ast.type.RecordTypeDeclaration;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.statement.switched.SwitchGroupStatement;
import org.processj.compiler.ast.expression.result.SwitchLabel;
import org.processj.compiler.ast.statement.switched.SwitchStatement;
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
    public final Void visitAltCase(AltCase altCase) {
		System.out.print(tab());
		if (altCase.getPreconditionExpression() != null) {
			System.out.print("(");
            try {
                altCase.getPreconditionExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            System.out.print(") && ");
		}
        try {
            altCase.getGuard().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" : ");
		indent += 2;
        try {
            altCase.getBody().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitAltStatement(AltStatement altStatement) throws Phase.Error {
		p("alt {");
		indent += 2;
		altStatement.visitChildren(this);
		indent -= 2;
		p("}");
		return null;
	}

	@Override
    public final Void visitArrayAccessExpression(ArrayAccessExpression arrayAccessExpression) {
        try {
            arrayAccessExpression.getTargetExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print("[");
        try {
            arrayAccessExpression.getIndexExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print("]");
		return null;
	}

	@Override
    public final Void visitArrayLiteralExpression(ArrayLiteralExpression arrayLiteralExpression) throws Phase.Error{
		// TODO
		return arrayLiteralExpression.visitChildren(this);
	}

	@Override
    public final Void visitArrayType(ArrayType arrayType) {

		System.out.print(arrayType.toString());

		return null;

	}

	@Override
    public final Void visitAssignmentExpression(AssignmentExpression assignmentExpression) {
        try {
            assignmentExpression.getLeftExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" " + assignmentExpression.opString() + " ");
        try {
            assignmentExpression.getRightExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitBinaryExpression(BinaryExpression binaryExpression) {
        try {
            binaryExpression.getLeft().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" " + binaryExpression.opString() + " ");
        try {
            binaryExpression.getRight().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitBlockStatement(BlockStatement blockStatement) {
		System.out.println(tab() + "{");
		indent += 2;
		for (Statement st : blockStatement.getStatements()) {
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
    public final Void visitBreakStatement(BreakStatement breakStatement) {
		System.out.print(tab() + "break");
		if (breakStatement.getTarget() != null) {
			System.out.print(" ");
            try {
                breakStatement.getTarget().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        }
		System.out.println(";");
		return null;
	}

	@Override
    public final Void visitCastExpression(CastExpression castExpression) throws Phase.Error {
		// TODO
		return castExpression.visitChildren(this);
	}

	@Override
    public final Void visitChannelType(ChannelType channelType) {
		String modString = channelType.modString();
		System.out.print(modString);
		if (!modString.equals(""))
			System.out.print(" ");
		System.out.print("chan<");
        try {
            channelType.getComponentType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(">");
		return null;
	}

	@Override
    public final Void visitChannelEndExpression(ChannelEndExpression channelEndExpression) {
        try {
            channelEndExpression.getChannelType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print("." + (channelEndExpression.isRead() ? "read" : "write"));
		return null;
	}

	@Override
    public final Void visitChannelEndType(ChannelEndType channelEndType) {
		if (channelEndType.isSharedEnd())
			System.out.print("shared ");
		System.out.print("chan<");
        try {
            channelEndType.getComponentType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(">." + (channelEndType.isReadEnd() ? "read" : "write"));
		return null;
	}

	@Override
    public final Void visitChannelReadExpression(ChannelReadExpression channelReadExpression) {
        try {
            channelReadExpression.getChannelExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(".read(");
		if (channelReadExpression.getExtendedRendezvous() != null) {
			System.out.println("{");
			indent += 2;
            try {
                channelReadExpression.getExtendedRendezvous().getStatements().visit(this);
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
    public final Void visitChannelWriteStatement(ChannelWriteStatement channelWriteStatement) {
		System.out.print(tab());

        try {
            channelWriteStatement.getTargetExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(".write(");
        try {
            channelWriteStatement.getWriteExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(");");
		return null;
	}

	@Override
    public final Void visitClaimStatement(ClaimStatement claimStatement) throws Phase.Error {
		// TODO
		return claimStatement.visitChildren(this);
	}

	@Override
    public final Void visitCompilation(Compilation compilation) throws Phase.Error  {
		System.out.println("Compilation");
		return compilation.visitChildren(this);
	}

	@Override
    public final Void visitConstantDeclaration(ConstantDeclaration constantDeclaration) {
		System.out.print(tab());
		printModifierSequence(constantDeclaration.modifiers());
		if (constantDeclaration.modifiers().size() > 0)
			System.out.print(" ");
        try {
            constantDeclaration.getType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        try {
            constantDeclaration.getName().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        try {
            constantDeclaration.getInitializationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");
		return null;
	}

	@Override
    public final Void visitContinueStatement(ContinueStatement continueStatement) {
		System.out.print("continue");
		if (continueStatement.getTarget() != null) {
			System.out.print(" ");
            try {
                continueStatement.getTarget().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        }
		return null;
	}

	@Override
    public final Void visitDoStatement(DoStatement doStatement) throws Phase.Error {
		System.out.print(tab() + "do ");

			System.out.println("{");
			indent += 2;

            doStatement.getBody().visit(this);
            indent -= 2;
			System.out.print(tab() + "} while (");

            doStatement.getEvaluationExpression().visit(this);

            System.out.print(")");

		System.out.println("");
		return null;
	}

	@Override
    public final Void visitExpressionStatement(ExpressionStatement expressionStatement) {
		System.out.print(tab());
        try {
            expressionStatement.getExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");
		return null;
	}

	@Override
    public final Void visitForStatement(ForStatement forStatement) throws Phase.Error {
		System.out.print(tab());
		System.out.print("for (");
		if (forStatement.getInitializationStatements() != null) {
			if (forStatement.getInitializationStatements().size() > 0) {
				// there are some children - if the first is a localDecl so are the rest!
				if (forStatement.getInitializationStatements().child(0) instanceof LocalDeclaration) {
					LocalDeclaration ld = (LocalDeclaration) forStatement.getInitializationStatements().child(0);
					System.out.print(ld.getType().toString() + " ");
					for (int i = 0; i < forStatement.getInitializationStatements().size(); i++) {
						ld = (LocalDeclaration) forStatement.getInitializationStatements().child(i);
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
                        if (i < forStatement.getInitializationStatements().size() - 1)
							System.out.print(",");
					}
				} else {
					for (Statement es : forStatement.getInitializationStatements())
                        try {
                            es.visit(this);
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                }
			}
		}
		System.out.print(";");
		if (forStatement.getEvaluationExpression() != null)
            try {
                forStatement.getEvaluationExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        System.out.print(";");
		if (forStatement.getIncrementStatements() != null) {
			for (int i = 0; i < forStatement.getIncrementStatements().size(); i++) {
				if (forStatement.getIncrementStatements().child(i) instanceof ExpressionStatement) {
					ExpressionStatement es = (ExpressionStatement) forStatement.getIncrementStatements().child(i);
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
        forStatement.getBody().visit(this);
        indent -= 2;
        System.out.println(tab() + "}");
		return null;
	}

	@Override
    public final Void visitGuardStatement(GuardStatement guardStatement) {
		if (guardStatement.getStatement() instanceof ExpressionStatement)
            try {
                ((ExpressionStatement) guardStatement.getStatement()).getExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        else if (guardStatement.getStatement() instanceof SkipStatement)
			System.out.print("skip");
		else if (guardStatement.getStatement() instanceof TimeoutStatement) {
			TimeoutStatement ts = (TimeoutStatement) guardStatement.getStatement();
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
    public final Void visitIfStatement(IfStatement ifStatement) throws Phase.Error {
		System.out.print(tab());
		System.out.print("if (");
        try {
            ifStatement.getEvaluationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(")");
		System.out.println(" {");
		indent += 2;

        ifStatement.getThenBody().visit(this);

        indent -= 2;

        System.out.print(tab() + "}");

		System.out.print(" else");

		System.out.println(" {");
        indent += 2;
        ifStatement.getElseBody().visit(this);

        indent -= 2;
		System.out.println(tab() + "}");

		return null;
	}

	@Override
    public final Void visitImport(Import importName) {
		// System.out.print(tab() + "import " + im.packageName() + ".");
		// if (im.all())
		// System.out.println("*;");
		// else
		// System.out.println(im.file() + ";");
		return null;
	}

	@Override
    public final Void visitInvocationExpression(InvocationExpression invocationExpression) {
		if (invocationExpression.getTarget() != null)
            try {
                invocationExpression.getTarget().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        System.out.print(invocationExpression.getProcedureName() + "(");
		for (int i = 0; i < invocationExpression.getParameters().size(); i++) {
            try {
                invocationExpression.getParameters().child(i).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            if (i < invocationExpression.getParameters().size() - 1)
				System.out.print(",");
		}
		System.out.print(")");
		return null;
	}

	@Override
    public final Void visitLocalDeclaration(LocalDeclaration localDeclaration) {
		System.out.print(tab());
		if (localDeclaration.isConstant())
			System.out.print("const ");

        try {
            localDeclaration.getType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        try {
            localDeclaration.getName().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        try {
            localDeclaration.getInitializationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");
		return null;
	}

	@Override
    public final Void visitModifier(Modifier modifier) {
		System.out.print(modifier.toString());
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
    public final Void visitName(Name name) {
		System.out.print(name);
		return null;
	}

	@Override
    public final Void visitNamedType(NamedType namedType) {
        try {
            namedType.getName().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitNameExpression(NameExpression nameExpression) {
        try {
            nameExpression.getName().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitNewArrayExpression(NewArrayExpression newArrayExpression)  throws Phase.Error {
		// TODO
		return newArrayExpression.visitChildren(this);
	}

	@Override
    public final Void visitNewMobileExpression(NewMobileExpression newMobileExpression) {
		System.out.print(tab() + "new mobile ");
        try {
            newMobileExpression.name().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitParameterDeclaration(ParameterDeclaration parameterDeclaration) {
		if (parameterDeclaration.isConstant())
			System.out.print("const ");
        try {
            parameterDeclaration.getType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        System.out.print(parameterDeclaration.getName());

        return null;
	}

	@Override
    public final Void visitParBlockStatement(ParBlock parBlock) {

		// TODO - don't forget that there are barriers to enroll on.
		System.out.println(tab() + "par {");
		indent += 2;
        try {
            parBlock.getBody().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;
		System.out.println(tab() + "}");
		return null;
	}

	@Override
    public final Void visitPragma(Pragma pragma) {
		System.out.println(tab() + "#pragma " + pragma + " " + (pragma.getValue() == null ? "" : pragma.getValue()));
		return null;
	}

	@Override
    public final Void visitPrimitiveLiteralExpression(PrimitiveLiteralExpression primitiveLiteralExpression) {
		System.out.print(primitiveLiteralExpression.getText());
		return null;
	}

	@Override
    public final Void visitPrimitiveType(PrimitiveType primitiveType) {
		System.out.print(primitiveType.toString());
		return null;
	}

	@Override
    public final Void visitProcedureTypeDeclaration(ProcedureTypeDeclaration procedureTypeDeclaration) {
		System.out.print(tab());
		printModifierSequence(procedureTypeDeclaration.modifiers());
		if (procedureTypeDeclaration.modifiers().size() > 0)
			System.out.print(" ");
        try {
            procedureTypeDeclaration.getReturnType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
		System.out.print(procedureTypeDeclaration);
		System.out.print("(");
		for (int i = 0; i < procedureTypeDeclaration.getParameters().size(); i++) {
            try {
                procedureTypeDeclaration.getParameters().child(i).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            if (i < procedureTypeDeclaration.getParameters().size() - 1)
				System.out.print(", ");
		}
		System.out.print(")");
		if (procedureTypeDeclaration.getImplements().size() > 0) {
			System.out.print(" implements ");
			for (int i = 0; i < procedureTypeDeclaration.getImplements().size(); i++) {
                try {
                    procedureTypeDeclaration.getImplements().child(i).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (i < procedureTypeDeclaration.getImplements().size() - 1)
					System.out.print(", ");
			}
		}

		if (procedureTypeDeclaration.getBody() != null) {
			System.out.println(" {");
			indent += 2;
            try {
                procedureTypeDeclaration.getBody().getStatements().visit(this);
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
    public final Void visitProtocolLiteralExpression(ProtocolLiteralExpression protocolLiteralExpression)  throws Phase.Error {
		// TODO
		return protocolLiteralExpression.visitChildren(this);
	}

	@Override
    public final Void visitProtocolCase(ProtocolCase protocolCase)  throws Phase.Error {
		// TODO
		return protocolCase.visitChildren(this);
	}

	@Override
    public final Void visitProtocolTypeDeclaration(ProtocolTypeDeclaration protocolTypeDeclaration) throws Phase.Error  {
		// TODO
		return protocolTypeDeclaration.visitChildren(this);
	}

	@Override
    public final Void visitRecordAccessExpression(RecordAccessExpression recordAccessExpression) {
        try {
            recordAccessExpression.getTarget().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(".)");
        try {
            recordAccessExpression.field().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitRecordLiteralExpression(RecordLiteralExpression recordLiteralExpression)  throws Phase.Error {
		// TODO
		return recordLiteralExpression.visitChildren(this);
	}

	@Override
    public final Void visitRecordMemberDeclaration(RecordMemberDeclaration recordMemberDeclaration) {
		System.out.print(tab());
        try {
            recordMemberDeclaration.getType().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        try {
            recordMemberDeclaration.getName().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");
		return null;
	}

	@Override
    public final Void visitRecordTypeDeclaration(RecordTypeDeclaration recordTypeDeclaration) {
		System.out.print(tab());
		printModifierSequence(recordTypeDeclaration.modifiers());
		if (recordTypeDeclaration.modifiers().size() > 0)
			System.out.print(" ");
		System.out.print("record ");
		System.out.print(recordTypeDeclaration);
		if (recordTypeDeclaration.getExtends().size() > 0) {
			System.out.print(" extends ");
			for (int i = 0; i < recordTypeDeclaration.getExtends().size(); i++) {
                try {
                    recordTypeDeclaration.getExtends().child(i).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (i < recordTypeDeclaration.getExtends().size() - 1)
					System.out.print(", ");
			}
		}
		System.out.println(" {");
		indent += 2;
        try {
            recordTypeDeclaration.getBody().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;
		System.out.println(tab() + "}");
		return null;
	}

	@Override
    public final Void visitReturnStatement(ReturnStatement returnStatement) {
		System.out.print(tab() + "return");
		if (returnStatement.getExpression() != null) {
			System.out.print(" ");
            try {
                returnStatement.getExpression().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        }
		return null;
	}

	@Override
    public final Void visitSequence(Sequence sequence) {
		// se.visitChildren(this);
		for (int i = 0; i < sequence.size(); i++) {
			if (sequence.child(i) != null)
                try {
                    sequence.child(i).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
        }
		return null;
	}

	@Override
    public final Void visitSkipStatement(SkipStatement skipStatement) {
		System.out.println("skip;");
		return null;
	}

	@Override
    public final Void visitStopStatement(StopStatement stopStatement) {
		System.out.println("stop;");
		return null;
	}

	@Override
    public final Void visitSuspendStatement(SuspendStatement suspendStatement) {
		System.out.print("suspend resume with (");
        try {
            suspendStatement.getParameters().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(")");
		return null;
	}

	@Override
    public final Void visitSwitchGroupStatement(SwitchGroupStatement switchGroupStatement) {
        try {
            switchGroupStatement.getLabels().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent += 2;
        try {
            switchGroupStatement.getStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;
		return null;
	}

	@Override
    public final Void visitSwitchLabelExpression(SwitchLabel switchLabel) {
		if (switchLabel.isDefault())
			System.out.println(tab() + "default:");
		else
			System.out.println(tab() + "case " + switchLabel.getExpression() + ":");
		return null;
	}

	@Override
    public final Void visitSwitchStatement(SwitchStatement switchStatement) {
		System.out.print(tab() + "switch (");
        try {
            switchStatement.getEvaluationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(") {");
		indent += 2;
        try {
            switchStatement.getBody().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;
		System.out.println(tab() + "}");
		return null;
	}

	@Override
    public final Void visitSyncStatement(SyncStatement syncStatement) {
		System.out.print(tab());
        try {
            syncStatement.getBarrierExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(".sync();");
		return null;
	}

	@Override
    public final Void visitTernaryExpression(TernaryExpression ternaryExpression) {
        try {
            ternaryExpression.getEvaluationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ? ");
        try {
            ternaryExpression.thenPart().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" : ");
        try {
            ternaryExpression.elsePart().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitTimeoutStatement(TimeoutStatement timeoutStatement) {
		System.out.print(tab());
        try {
            timeoutStatement.getTimerExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(".timeout(");
        try {
            timeoutStatement.getDelayExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(");");
		return null;
	}

	@Override
    public final Void visitUnaryPostExpression(UnaryPostExpression unaryPostExpression) {
        try {
            unaryPostExpression.getExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(unaryPostExpression.opString());
		return null;
	}

	@Override
    public final Void visitUnaryPreExpression(UnaryPreExpression unaryPreExpression) {
		System.out.print(unaryPreExpression.opString());
        try {
            unaryPreExpression.getExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
	}

	@Override
    public final Void visitWhileStatement(WhileStatement whileStatement) throws Phase.Error {
		System.out.print(tab() + "while (");
        try {
            whileStatement.getEvaluationExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(")");

        System.out.println(" {");
        indent += 2;
        whileStatement.getBody().visit(this);
        indent -= 2;
        System.out.println(tab() + "}");

        return null;
	}
}