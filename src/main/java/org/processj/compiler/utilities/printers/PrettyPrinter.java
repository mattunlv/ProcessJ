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
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.ast.statement.conditional.ParBlock;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.statement.conditional.AltStatement;
import org.processj.compiler.ast.type.ArrayType;
import org.processj.compiler.ast.type.ChannelEndType;
import org.processj.compiler.ast.type.ChannelType;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.statement.declarative.ConstantDeclaration;
import org.processj.compiler.ast.packages.Import;
import org.processj.compiler.ast.modifier.Modifier;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.type.NamedType;
import org.processj.compiler.ast.statement.declarative.ParameterDeclaration;
import org.processj.compiler.ast.packages.Pragma;
import org.processj.compiler.ast.type.primitive.PrimitiveType;
import org.processj.compiler.ast.type.ProcedureType;
import org.processj.compiler.ast.type.ProtocolType;
import org.processj.compiler.ast.type.RecordType;
import org.processj.compiler.ast.Sequence;
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
    public final void visitAltStatementCase(AltStatement.Case aCase) {
		System.out.print(tab());
		if (aCase.getPreconditionExpression() != null) {
			System.out.print("(");
            try {
                aCase.getPreconditionExpression().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            System.out.print(") && ");
		}
        try {
            aCase.getGuardStatement().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" : ");
		indent += 2;
        try {
            aCase.accept(this);
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
		for (Statement st : blockStatement) {
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
            channelEndExpression.getChannelExpression().accept(this);
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
                channelReadExpression.getExtendedRendezvous().accept(this);
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

            doStatement.accept(this);
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
        forStatement.accept(this);
        indent -= 2;
        System.out.println(tab() + "}");

	}

	@Override
    public final void visitGuardStatement(AltStatement.Case.Guard guard) {
		if (guard.getStatement() instanceof ExpressionStatement)
            try {
                ((ExpressionStatement) guard.getStatement()).getExpression().accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        else if (guard.getStatement() instanceof SkipStatement)
			System.out.print("skip");
		else if (guard.getStatement() instanceof TimeoutStatement) {
			TimeoutStatement ts = (TimeoutStatement) guard.getStatement();
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

        ifStatement.accept(this);

        indent -= 2;

        System.out.print(tab() + "}");

		System.out.print(" else");

		System.out.println(" {");
        indent += 2;
        ifStatement.getElseStatement().accept(this);

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
		if (parameterDeclaration.isConstant())
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
            parBlock.accept(this);
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
    public final void visitProcedureTypeDeclaration(ProcedureType procedureType)throws Phase.Error {
		System.out.print(tab());
		procedureType.getModifiers().accept(this);
		if (procedureType.getModifiers().isEmpty())
			System.out.print(" ");
        try {
            procedureType.getReturnType().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
		System.out.print(procedureType);
		System.out.print("(");
		for (int i = 0; i < procedureType.getParameters().size(); i++) {
            try {
                procedureType.getParameters().getParameterAt(i).accept(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            if (i < procedureType.getParameters().size() - 1)
				System.out.print(", ");
		}
		System.out.print(")");
		if (procedureType.getImplements().size() > 0) {
			System.out.print(" implements ");
			for (int i = 0; i < procedureType.getImplements().size(); i++) {
                try {
                    procedureType.getImplements().getNameAt(i).accept(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (i < procedureType.getImplements().size() - 1)
					System.out.print(", ");
			}
		}

		if (!procedureType.isEmpty()) {
			System.out.println(" {");
			indent += 2;

            for(final Statement statement: procedureType)
                statement.accept(this);

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
    public final void visitProtocolCase(ProtocolType.Case aCase)  throws Phase.Error {
		// TODO
        aCase.visitChildren(this);
	}

	@Override
    public final void visitProtocolTypeDeclaration(ProtocolType protocolType) throws Phase.Error  {
		// TODO
		protocolType.visitChildren(this);
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
    public final void visitRecordTypeDeclarationMember(RecordType.Member member) {
		System.out.print(tab());
        try {
            member.getType().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.print(" ");
        try {
            member.getName().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println(";");

	}

	@Override
    public final void visitRecordTypeDeclaration(RecordType recordType) {
		System.out.print(tab());
		//printModifierSequence(recordType.getModifiers());
		if (!recordType.getModifiers().isEmpty())
			System.out.print(" ");
		System.out.print("record ");
		System.out.print(recordType);
		//if (recordType.getExtends().size() > 0) {
			//System.out.print(" extends ");
			//for (int i = 0; i < recordType.getExtends().size(); i++) {
                //try {
                    //recordType.getExtends().child(i).accept(this);
                //} catch (Phase.Error error) {
                    //throw new RuntimeException(error);
                //}
                //if (i < recordType.getExtends().size() - 1)
					//System.out.print(", ");
			//}
		//}
		System.out.println(" {");
		indent += 2;
        try {
            recordType.getBody().accept(this);
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
    public final void visitSwitchStatementGroup(SwitchStatement.Group group) {
        try {
            group.getCases().accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent += 2;
        try {
            group.accept(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        indent -= 2;

	}

	@Override
    public final void visitSwitchLabelExpression(SwitchStatement.Group.Case aCase) {
		if (aCase.isDefault())
			System.out.println(tab() + "default:");
		else
			System.out.println(tab() + "case " + aCase.getExpression() + ":");

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
            switchStatement.accept(this);
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
        whileStatement.accept(this);
        indent -= 2;
        System.out.println(tab() + "}");

	}
}