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
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.statement.yielding.AltCase;
import org.processj.compiler.ast.statement.yielding.AltStatement;
import org.processj.compiler.ast.type.ArrayType;
import org.processj.compiler.ast.type.ChannelEndType;
import org.processj.compiler.ast.type.ChannelType;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.type.ConstantDeclaration;
import org.processj.compiler.ast.statement.yielding.GuardStatement;
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
import org.processj.compiler.ast.statement.conditional.SwitchGroupStatement;
import org.processj.compiler.ast.expression.result.SwitchLabel;
import org.processj.compiler.ast.statement.conditional.SwitchStatement;
import org.processj.compiler.phase.Visitor;

public class PrettyPrinter implements Visitor {
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
    public final void visitAltCase(AltCase altCase) {
		System.out.print(tab());
		if (altCase.getPreconditionExpression() != null) {
			System.out.print("(");
            try {
                altCase.getPreconditionExpression().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            System.out.print(") && ");
		}
        try {
            altCase.getGuard().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" : ");
		indent += 2;
        try {
            altCase.getBody().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

	}

	@Override
    public final void visitAltStatement(AltStatement altStatement) throws Phase.Error {
		p("alt {");
		indent += 2;
		altStatement.visitChildren(this);
		indent -= 2;
		p("}");

	}

	@Override
    public final void visitArrayAccessExpression(ArrayAccessExpression arrayAccessExpression) {
        try {
            arrayAccessExpression.getTargetExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print("[");
        try {
            arrayAccessExpression.getIndexExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print("]");

	}

	@Override
    public final void visitArrayLiteralExpression(ArrayLiteralExpression arrayLiteralExpression) throws Phase.Error{
		// TODO
        arrayLiteralExpression.visitChildren(this);
	}

	@Override
    public final void visitArrayType(ArrayType arrayType) {

		System.out.print(arrayType.toString());



	}

	@Override
    public final void visitAssignmentExpression(AssignmentExpression assignmentExpression) {
        try {
            assignmentExpression.getLeftExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" " + assignmentExpression.opString() + " ");
        try {
            assignmentExpression.getRightExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

	}

	@Override
    public final void visitBinaryExpression(BinaryExpression binaryExpression) {
        try {
            binaryExpression.getLeftExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" " + binaryExpression.opString() + " ");
        try {
            binaryExpression.getRightExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

	}

	@Override
    public final void visitBlockStatement(BlockStatement blockStatement) {
		System.out.println(tab() + "{");
		indent += 2;
		for (Statement st : blockStatement.getStatements()) {
			indent += 2;
			if (st == null) {
				System.out.println(tab() + ";");
			} else {
                try {
                    st.accept(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                // System.out.println(";");
			}
			indent -= 2;
		}
		indent -= 2;
		System.out.println(tab() + "}");

	}

	@Override
    public final void visitBreakStatement(BreakStatement breakStatement) {
		System.out.print(tab() + "break");
		if (breakStatement.getTarget() != null) {
			System.out.print(" ");
            try {
                breakStatement.getTarget().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        }
		System.out.println(";");

	}

	@Override
    public final void visitCastExpression(CastExpression castExpression) throws Phase.Error {
		// TODO
        castExpression.visitChildren(this);
	}

	@Override
    public final void visitChannelType(ChannelType channelType) {
		String modString = channelType.modString();
		System.out.print(modString);
		if (!modString.equals(""))
			System.out.print(" ");
		System.out.print("chan<");
        try {
            channelType.getComponentType().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(">");

	}

	@Override
    public final void visitChannelEndExpression(ChannelEndExpression channelEndExpression) {
        try {
            channelEndExpression.getChannelType().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print("." + (channelEndExpression.isRead() ? "read" : "write"));

	}

	@Override
    public final void visitChannelEndType(ChannelEndType channelEndType) {
		if (channelEndType.isSharedEnd())
			System.out.print("shared ");
		System.out.print("chan<");
        try {
            channelEndType.getComponentType().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(">." + (channelEndType.isReadEnd() ? "read" : "write"));

	}

	@Override
    public final void visitChannelReadExpression(ChannelReadExpression channelReadExpression) {
        try {
            channelReadExpression.getTargetExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(".read(");
		if (channelReadExpression.getExtendedRendezvous() != null) {
			System.out.println("{");
			indent += 2;
            try {
                channelReadExpression.getExtendedRendezvous().getStatements().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            indent -= 2;
			System.out.print("}");
		}
		System.out.print(")");

	}

	@Override
    public final void visitChannelWriteStatement(ChannelWriteStatement channelWriteStatement) {
		System.out.print(tab());

        try {
            channelWriteStatement.getTargetExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(".write(");
        try {
            channelWriteStatement.getWriteExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(");");

	}

	@Override
    public final void visitClaimStatement(ClaimStatement claimStatement) throws Phase.Error {
		// TODO
        claimStatement.visitChildren(this);
	}

	@Override
    public final void visitCompilation(Compilation compilation) throws Phase.Error  {
		System.out.println("Compilation");
        compilation.visitChildren(this);
	}

	@Override
    public final void visitConstantDeclaration(ConstantDeclaration constantDeclaration) {
		System.out.print(tab());
		//printModifierSequence(constantDeclaration.getModifiers());
		//if (constantDeclaration.getModifiers().size() > 0)
			//System.out.print(" ");
        try {
            constantDeclaration.getType().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        try {
            constantDeclaration.getName().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        try {
            constantDeclaration.getInitializationExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");

	}

	@Override
    public final void visitContinueStatement(ContinueStatement continueStatement) {
		System.out.print("continue");
		if (continueStatement.getTarget() != null) {
			System.out.print(" ");
            try {
                continueStatement.getTarget().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        }

	}

	@Override
    public final void visitDoStatement(DoStatement doStatement) throws Phase.Error {
		System.out.print(tab() + "do ");

			System.out.println("{");
			indent += 2;

            doStatement.getBody().accept(this);
            indent -= 2;
			System.out.print(tab() + "} while (");

            doStatement.getEvaluationExpression().accept(this);

            System.out.print(")");

		System.out.println("");

	}

	@Override
    public final void visitExpressionStatement(ExpressionStatement expressionStatement) {
		System.out.print(tab());
        try {
            expressionStatement.getExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");

	}

	@Override
    public final void visitForStatement(ForStatement forStatement) throws Phase.Error {
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
                            ld.getName().accept(this);
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                        try {
                            ld.getInitializationExpression().accept(this);
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                        if (i < forStatement.getInitializationStatements().size() - 1)
							System.out.print(",");
					}
				} else {
					for (Statement es : forStatement.getInitializationStatements())
                        try {
                            es.accept(this);
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                }
			}
		}
		System.out.print(";");
		if (forStatement.getEvaluationExpression() != null)
            try {
                forStatement.getEvaluationExpression().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        System.out.print(";");
		if (forStatement.getIncrementStatements() != null) {
			for (int i = 0; i < forStatement.getIncrementStatements().size(); i++) {
				if (forStatement.getIncrementStatements().child(i) instanceof ExpressionStatement) {
					ExpressionStatement es = (ExpressionStatement) forStatement.getIncrementStatements().child(i);
                    try {
                        es.getExpression().accept(this);
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
                }
			}

		}
		System.out.print(")");

        System.out.println(" {");
        indent += 2;
        forStatement.getBody().accept(this);
        indent -= 2;
        System.out.println(tab() + "}");

	}

	@Override
    public final void visitGuardStatement(GuardStatement guardStatement) {
		if (guardStatement.getStatement() instanceof ExpressionStatement)
            try {
                ((ExpressionStatement) guardStatement.getStatement()).getExpression().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        else if (guardStatement.getStatement() instanceof SkipStatement)
			System.out.print("skip");
		else if (guardStatement.getStatement() instanceof TimeoutStatement) {
			TimeoutStatement ts = (TimeoutStatement) guardStatement.getStatement();
            try {
                ts.getTimerExpression().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            System.out.print(".timeout(");
            try {
                ts.getDelayExpression().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            System.out.print(")");
		}

	}

	@Override
    public final void visitIfStatement(IfStatement ifStatement) throws Phase.Error {
		System.out.print(tab());
		System.out.print("if (");
        try {
            ifStatement.getEvaluationExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(")");
		System.out.println(" {");
		indent += 2;

        ifStatement.getThenBody().accept(this);

        indent -= 2;

        System.out.print(tab() + "}");

		System.out.print(" else");

		System.out.println(" {");
        indent += 2;
        ifStatement.getElseBody().accept(this);

        indent -= 2;
		System.out.println(tab() + "}");


	}

	@Override
    public final void visitImport(Import importName) {
		// System.out.print(tab() + "import " + im.packageName() + ".");
		// if (im.all())
		// System.out.println("*;");
		// else
		// System.out.println(im.file() + ";");

	}

	@Override
    public final void visitInvocationExpression(InvocationExpression invocationExpression) {
		if (invocationExpression.getTarget() != null)
            try {
                invocationExpression.getTarget().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        System.out.print(invocationExpression.getProcedureName() + "(");
		for (int i = 0; i < invocationExpression.getParameterExpressions().size(); i++) {
            try {
                invocationExpression.getParameterExpressions().child(i).accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            if (i < invocationExpression.getParameterExpressions().size() - 1)
				System.out.print(",");
		}
		System.out.print(")");

	}

	@Override
    public final void visitLocalDeclaration(LocalDeclaration localDeclaration) {
		System.out.print(tab());
		if (localDeclaration.isConstant())
			System.out.print("const ");

        try {
            localDeclaration.getType().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        try {
            localDeclaration.getName().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        try {
            localDeclaration.getInitializationExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");

	}

	@Override
    public final void visitModifier(Modifier modifier) {
		System.out.print(modifier.toString());

	}

	public void printModifierSequence(Sequence<Modifier> mods) {
		int i = 0;
		for (Modifier m : mods) {
            try {
                m.accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            if (i < mods.size() - 1)
				System.out.print(" ");
			i++;
		}
	}

	@Override
    public final void visitName(Name name) {
		System.out.print(name);

	}

	@Override
    public final void visitNamedType(NamedType namedType) {
        try {
            namedType.getName().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

	}

	@Override
    public final void visitNameExpression(NameExpression nameExpression) {
        try {
            nameExpression.getName().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

	}

	@Override
    public final void visitNewArrayExpression(NewArrayExpression newArrayExpression)  throws Phase.Error {
		// TODO
        newArrayExpression.visitChildren(this);
	}

	@Override
    public final void visitNewMobileExpression(NewMobileExpression newMobileExpression) {
		System.out.print(tab() + "new mobile ");
        try {
            newMobileExpression.name().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

	}

	@Override
    public final void visitParameterDeclaration(ParameterDeclaration parameterDeclaration) {
		if (parameterDeclaration.isDeclaredConstant())
			System.out.print("const ");
        try {
            parameterDeclaration.getType().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        System.out.print(parameterDeclaration.getName());

	}

	@Override
    public final void visitParBlockStatement(ParBlock parBlock) {

		// TODO - don't forget that there are barriers to enroll on.
		System.out.println(tab() + "par {");
		indent += 2;
        try {
            parBlock.getBody().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;
		System.out.println(tab() + "}");

	}

	@Override
    public final void visitPragma(Pragma pragma) {
		System.out.println(tab() + "#pragma " + pragma + " " + (pragma.getValue() == null ? "" : pragma.getValue()));

	}

	@Override
    public final void visitPrimitiveLiteralExpression(PrimitiveLiteralExpression primitiveLiteralExpression) {
		System.out.print(primitiveLiteralExpression.getText());

	}

	@Override
    public final void visitPrimitiveType(PrimitiveType primitiveType) {
		System.out.print(primitiveType.toString());

	}

	@Override
    public final void visitProcedureTypeDeclaration(ProcedureTypeDeclaration procedureTypeDeclaration) {
		System.out.print(tab());
		printModifierSequence(procedureTypeDeclaration.getModifiers());
		if (procedureTypeDeclaration.getModifiers().size() > 0)
			System.out.print(" ");
        try {
            procedureTypeDeclaration.getReturnType().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
		System.out.print(procedureTypeDeclaration);
		System.out.print("(");
		for (int i = 0; i < procedureTypeDeclaration.getParameters().size(); i++) {
            try {
                procedureTypeDeclaration.getParameters().child(i).accept(this);
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
                    procedureTypeDeclaration.getImplements().child(i).accept(this);
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
                procedureTypeDeclaration.getBody().getStatements().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            indent -= 2;
			System.out.println(tab() + "}");
		} else
			System.out.println(" ;");


	}

	@Override
    public final void visitProtocolLiteralExpression(ProtocolLiteralExpression protocolLiteralExpression)  throws Phase.Error {
		// TODO
        protocolLiteralExpression.visitChildren(this);
	}

	@Override
    public final void visitProtocolCase(ProtocolCase protocolCase)  throws Phase.Error {
		// TODO
        protocolCase.visitChildren(this);
	}

	@Override
    public final void visitProtocolTypeDeclaration(ProtocolTypeDeclaration protocolTypeDeclaration) throws Phase.Error  {
		// TODO
		protocolTypeDeclaration.visitChildren(this);
	}

	@Override
    public final void visitRecordAccessExpression(RecordAccessExpression recordAccessExpression) {
        try {
            recordAccessExpression.getTarget().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(".)");
        try {
            recordAccessExpression.field().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

	}

	@Override
    public final void visitRecordLiteralExpression(RecordLiteralExpression recordLiteralExpression)  throws Phase.Error {
		// TODO
        recordLiteralExpression.visitChildren(this);
	}

	@Override
    public final void visitRecordMemberDeclaration(RecordMemberDeclaration recordMemberDeclaration) {
		System.out.print(tab());
        try {
            recordMemberDeclaration.getType().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        try {
            recordMemberDeclaration.getName().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");

	}

	@Override
    public final void visitRecordTypeDeclaration(RecordTypeDeclaration recordTypeDeclaration) {
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
                    recordTypeDeclaration.getExtends().child(i).accept(this);
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
            recordTypeDeclaration.getBody().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;
		System.out.println(tab() + "}");

	}

	@Override
    public final void visitReturnStatement(ReturnStatement returnStatement) {
		System.out.print(tab() + "return");
		if (returnStatement.getExpression() != null) {
			System.out.print(" ");
            try {
                returnStatement.getExpression().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        }

	}

	@Override
    public final void visitSequence(Sequence sequence) {
		// se.visitChildren(this);
		for (int i = 0; i < sequence.size(); i++) {
			if (sequence.child(i) != null)
                try {
                    sequence.child(i).accept(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
        }

	}

	@Override
    public final void visitSkipStatement(SkipStatement skipStatement) {
		System.out.println("skip;");

	}

	@Override
    public final void visitStopStatement(StopStatement stopStatement) {
		System.out.println("stop;");

	}

	@Override
    public final void visitSuspendStatement(SuspendStatement suspendStatement) {
		System.out.print("suspend resume with (");
        try {
            suspendStatement.getParameters().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(")");

	}

	@Override
    public final void visitSwitchGroupStatement(SwitchGroupStatement switchGroupStatement) {
        try {
            switchGroupStatement.getLabels().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent += 2;
        try {
            switchGroupStatement.getStatements().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;

	}

	@Override
    public final void visitSwitchLabelExpression(SwitchLabel switchLabel) {
		if (switchLabel.isDefault())
			System.out.println(tab() + "default:");
		else
			System.out.println(tab() + "case " + switchLabel.getExpression() + ":");

	}

	@Override
    public final void visitSwitchStatement(SwitchStatement switchStatement) {
		System.out.print(tab() + "switch (");
        try {
            switchStatement.getEvaluationExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(") {");
		indent += 2;
        try {
            switchStatement.getBody().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;
		System.out.println(tab() + "}");

	}

	@Override
    public final void visitSyncStatement(SyncStatement syncStatement) {
		System.out.print(tab());
        try {
            syncStatement.getBarrierExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(".sync();");

	}

	@Override
    public final void visitTernaryExpression(TernaryExpression ternaryExpression) {
        try {
            ternaryExpression.getEvaluationExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ? ");
        try {
            ternaryExpression.getThenExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" : ");
        try {
            ternaryExpression.getElseExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

	}

	@Override
    public final void visitTimeoutStatement(TimeoutStatement timeoutStatement) {
		System.out.print(tab());
        try {
            timeoutStatement.getTimerExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(".timeout(");
        try {
            timeoutStatement.getDelayExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(");");

	}

	@Override
    public final void visitUnaryPostExpression(UnaryPostExpression unaryPostExpression) {
        try {
            unaryPostExpression.getExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(unaryPostExpression.opString());

	}

	@Override
    public final void visitUnaryPreExpression(UnaryPreExpression unaryPreExpression) {
		System.out.print(unaryPreExpression.opString());
        try {
            unaryPreExpression.getExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

	}

	@Override
    public final void visitWhileStatement(WhileStatement whileStatement) throws Phase.Error {
		System.out.print(tab() + "while (");
        try {
            whileStatement.getEvaluationExpression().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(")");

        System.out.println(" {");
        indent += 2;
        whileStatement.getBody().accept(this);
        indent -= 2;
        System.out.println(tab() + "}");

	}
}