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
import org.processj.compiler.ast.expression.resolve.ImplicitImportExpression;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.expression.result.*;
import org.processj.compiler.ast.expression.unary.*;
import org.processj.compiler.ast.expression.yielding.ChannelEndExpression;
import org.processj.compiler.ast.expression.yielding.ChannelReadExpression;
import org.processj.compiler.ast.modifier.Modifier;
import org.processj.compiler.ast.packages.Import;
import org.processj.compiler.ast.packages.Pragma;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.declarative.*;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.conditional.SwitchStatement;
import org.processj.compiler.ast.type.ProcedureType;
import org.processj.compiler.ast.type.ProtocolType;
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.ast.statement.conditional.ParBlock;
import org.processj.compiler.ast.type.*;
import org.processj.compiler.ast.type.primitive.PrimitiveType;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.conditional.AltStatement;
import org.processj.compiler.phase.Visitor;

public class ParseTreePrinter implements Visitor {
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
    public void visitAltStatementCase(AltStatement.Case aCase) throws Phase.Error {
        System.out.println(aCase + "AltCase:");
        indent += 2;
        Visitor.super.visitAltStatementCase(aCase);
        indent -= 2;

    }
    // AltStat
    public void visitAltStatement(AltStatement altStatement) throws Phase.Error {
        System.out.println( "AltStat:");
        indent += 2;
        Visitor.super.visitAltStatement(altStatement);
        indent -= 2;
    }
    // ArrayAccessExpr
    public void visitArrayAccessExpression(ArrayAccessExpression arrayAccessExpression) throws Phase.Error {
        System.out.println("ArrayAccessExpr:");
        indent += 2;
        Visitor.super.visitArrayAccessExpression(arrayAccessExpression);
        indent -=2;

    }
    // ArrayLiteral
    public void visitArrayLiteralExpression(ArrayLiteralExpression arrayLiteralExpression) throws Phase.Error {
        System.out.println("ArrayLiteral:");
        indent += 2;
        Visitor.super.visitArrayLiteralExpression(arrayLiteralExpression);
        indent -= 2;
    }
    // ArrayType
    public void visitArrayType(ArrayType arrayType) throws Phase.Error {
        System.out.print("ArrayType:");
        for (int i = 0; i< arrayType.getDepth(); i++)
            System.out.print("[]");
        System.out.println();
        indent += 2;
        Visitor.super.visitArrayType(arrayType);
        indent -= 2;
    }
    // Assignment
    public void visitAssignmentExpression(AssignmentExpression assignmentExpression) throws Phase.Error {
        System.out.println("Assignment:");
        indent += 2;
        Visitor.super.visitAssignmentExpression(assignmentExpression);
        System.out.println("Operator: " + assignmentExpression.opString());
        indent -= 2;

    }
    // BinaryExpr
    public void visitBinaryExpression(BinaryExpression binaryExpression) throws Phase.Error {
        System.out.println("BinaryExpr:");
        indent += 2;
        Visitor.super.visitBinaryExpression(binaryExpression);
        System.out.println("Operator: " + binaryExpression.opString());
        indent -= 2;

    }
    // Block
    public void visitBlockStatement(BlockStatement blockStatement) throws Phase.Error {
        System.out.println("Block:");
        indent += 2;
        Visitor.super.visitBlockStatement(blockStatement);
        indent -= 2;

    }
    // BreakStat
    /** BREAK STATEMENT */
    public void visitBreakStatement(BreakStatement breakStatement) throws Phase.Error {
        System.out.println("BreakStat");
        indent += 2;
        Visitor.super.visitBreakStatement(breakStatement);
        indent -= 2;

    }
    // CastExpr
    public void visitCastExpression(CastExpression castExpression) throws Phase.Error {
        System.out.println("CastExpr:");
        indent += 2;
        Visitor.super.visitCastExpression(castExpression);
        indent -= 2;

    }
    // ChannelType
    public void visitChannelType(ChannelType channelType) throws Phase.Error {
        System.out.println("ChannelType:");
        indent += 2;
        Visitor.super.visitChannelType(channelType);
        indent -= 2;

    }
    // ChannelEndExpr
    public void visitChannelEndExpression(ChannelEndExpression channelEndExpression) throws Phase.Error {
        System.out.println("ChannelEndExpr:");
        indent += 2;
        Visitor.super.visitChannelEndExpression(channelEndExpression);
        indent -= 2;

    }
    // ChannelEndType
    public void visitChannelEndType(ChannelEndType ce) throws Phase.Error {
        System.out.println("ChannelEndType:");
        indent += 2;
        Visitor.super.visitChannelEndType(ce);
        indent -= 2;

    }
    // ChannelReadExpr
    public void visitChannelReadExpression(ChannelReadExpression channelReadExpression) throws Phase.Error {
        System.out.println("ChannelReadExpr:");
        indent += 2;
        Visitor.super.visitChannelReadExpression(channelReadExpression);
        indent -= 2;

    }
    // ChannelWriteStat
    public void visitChannelWriteStatement(ChannelWriteStatement channelWriteStatement) throws Phase.Error {
        System.out.println("ChannelWriteStat:");
        indent += 2;
        Visitor.super.visitChannelWriteStatement(channelWriteStatement);
        indent -= 2;

    }
    // ClaimStat
    public void visitClaimStatement(ClaimStatement claimStatement) throws Phase.Error {
        System.out.println("ClaimStat:");
        indent += 2;
        Visitor.super.visitClaimStatement(claimStatement);
        indent -= 2;

    }
    // Compilation
    public void visitCompilation(Compilation compilation) throws Phase.Error {
        System.out.println("Compilation:");
        indent += 2;
        Visitor.super.visitCompilation(compilation);
        // TODO: Print Pragma
        indent -= 2;

    }
    // ConstantDecl
    public void visitConstantDeclaration(ConstantDeclaration constantDeclaration) throws Phase.Error {
        System.out.println("ConstantDecl:");
        indent += 2;
        Visitor.super.visitConstantDeclaration(constantDeclaration);
        indent -= 2;

    }
    // ContinueStat
    public void visitContinueStatement(ContinueStatement continueStatement) throws Phase.Error {
        System.out.println("Continue Statement");
        indent += 2;
        Visitor.super.visitContinueStatement(continueStatement);
        indent -= 2;

    }
    // DoStat
    public void visitDoStatement(DoStatement doStatement) throws Phase.Error {
        System.out.println("DoStat:");
        indent += 2;
        Visitor.super.visitDoStatement(doStatement);
        indent -= 2;

    }
    // ExprStat
    public void visitExpressionStatement(ExpressionStatement expressionStatement) throws Phase.Error {
        System.out.println("ExprStat:");
        indent += 2;
        Visitor.super.visitExpressionStatement(expressionStatement);
        indent -= 2;

    }
    // ExternType
    public void visitExternType(ExternType externType) throws Phase.Error {
        System.out.println("ExternType:");
        indent += 2;
        Visitor.super.visitExternType(externType);
        indent -= 2;

    }
    // ForStat
    public void visitForStatement(ForStatement forStatement) throws Phase.Error {
        System.out.println("ForStat:");
        indent += 2;
        Visitor.super.visitForStatement(forStatement);
        indent -= 2;

    }
    // Guard
    public void visitGuardStatement(AltStatement.Case.Guard g) throws Phase.Error {
        System.out.println("Guard:");
        indent += 2;
        Visitor.super.visitGuardStatement(g);
        indent -= 2;

    }
    // IfStat
    public void visitIfStatement(IfStatement ifStatement) throws Phase.Error {
        System.out.println("IfStat:");
        indent += 2;
        Visitor.super.visitIfStatement(ifStatement);
        indent -= 2;

    }
    // ImplicitImport
    public void visitImplicitImportExpression(ImplicitImportExpression im) throws Phase.Error {
        System.out.println("ImplicitImport:");
        indent += 2;
        Visitor.super.visitImplicitImportExpression(im);
        indent -= 2;

    }
    // Import
    public void visitImport(Import importName) throws Phase.Error {
        System.out.println("Import:");
        indent += 2;
        Visitor.super.visitImport(importName);
        indent -= 2;

    }
    // Invocation
    public void visitInvocationExpression(InvocationExpression invocationExpression) throws Phase.Error {
        System.out.println("Invocation:");
        indent += 2;
        Visitor.super.visitInvocationExpression(invocationExpression);
        indent -= 2;

    }
    // LocalDecl
    public void visitLocalDeclaration(LocalDeclaration localDeclaration) throws Phase.Error {
        System.out.println("LocalDecl:");
        indent += 2;
        Visitor.super.visitLocalDeclaration(localDeclaration);
        indent -= 2;

    }
    // Modifier
    public void visitModifier(Modifier modifier) throws Phase.Error {
        System.out.println("Modifier (" + modifier + ")");

    }
    // Name
    public void visitName(Name name) throws Phase.Error {
        System.out.println("Name = " + name);

    }
    // NamedType
    public void visitNamedType(NamedType namedType) throws Phase.Error {
        System.out.println("NamedType:");
        indent += 2;
        Visitor.super.visitNamedType(namedType);
        indent -= 2;

    }
    // NameExpr
    public void visitNameExpression(NameExpression nameExpression) throws Phase.Error {
        System.out.println("NameExpr:");
        indent += 2;
        Visitor.super.visitNameExpression(nameExpression);
        indent -= 2;

    }
    // NewArray
    public void visitNewArrayExpression(NewArrayExpression newArrayExpression) throws Phase.Error {
        System.out.println("New Array");
        indent += 2;
        Visitor.super.visitNewArrayExpression(newArrayExpression);
        indent -= 2;

    }
    // NewMobile
    public void visitNMewMobile(NewMobileExpression nm) throws Phase.Error {
        System.out.println("NewMobile:");
        indent += 2;
        Visitor.super.visitNewMobileExpression(nm);
        indent -= 2;

    }
    // ParamDecl
    public void visitParameterDeclaration(ParameterDeclaration parameterDeclaration) throws Phase.Error {
        System.out.println("ParamDecl: ");
        indent += 2;
        Visitor.super.visitParameterDeclaration(parameterDeclaration);
        indent -= 2;

    }
    // ParBlock
    public void visitParBlockStatement(ParBlock parBlock) throws Phase.Error {
        System.out.println("ParBlock:");
        indent += 2;
        Visitor.super.visitParBlockStatement(parBlock);
        indent -= 2;

    }
    // PrimitiveLiteral
    public void visitPrimitiveLiteralExpression(PrimitiveLiteralExpression primitiveLiteralExpression) throws Phase.Error {
        System.out.println("PrimtiveLiteral = " + primitiveLiteralExpression);
        indent += 2;
        Visitor.super.visitPrimitiveLiteralExpression(primitiveLiteralExpression);
        indent -= 2;

    }
    // PrimitiveType
    public void visitPrimitiveType(PrimitiveType primitiveType) throws Phase.Error {
        System.out.println("PrimitiveType = " + primitiveType);

    }
    // ProcTypeDecl
    public void visitProcedureTypeDeclaration(ProcedureType procedureType) throws Phase.Error {
        System.out.println("ProcTypeDecl:");
        indent += 2;

        System.out.println("Annotations: " + procedureType.getAnnotations().toString());
        Visitor.super.visitProcedureTypeDeclaration(procedureType);
        indent -= 2;

    }
    // ProtocolLiteral
    public void visitProtocolLiteralExpression(ProtocolLiteralExpression protocolLiteralExpression) throws Phase.Error {
        System.out.println("ProtocolLiteral:");
        indent += 2;
        Visitor.super.visitProtocolLiteralExpression(protocolLiteralExpression);
        indent -=2;

    }
    // ProtocolCase
    public void visitProtocolCase(ProtocolType.Case aCase) throws Phase.Error {
        System.out.println("ProtocolCase:");
        indent += 2;
        Visitor.super.visitProtocolCase(aCase);
        indent -=2;

    }
    // ProtocolTypeDecl
    public void visitProtocolTypeDeclaration(ProtocolType protocolType) throws Phase.Error {
        System.out.println("ProtocolTypeDecl:");
        indent += 2;
        Visitor.super.visitProtocolTypeDeclaration(protocolType);
        indent -= 2;

    }
    // RecordAccess
    public void visitRecordAccessExpression(RecordAccessExpression recordAccessExpression) throws Phase.Error {
        System.out.println("RecordAccess:");
        indent += 2;
        Visitor.super.visitRecordAccessExpression(recordAccessExpression);
        indent -= 2;

    }
    // RecordLiteral
    public void visitRecordLiteralExpression(RecordLiteralExpression recordLiteralExpression) throws Phase.Error {
        System.out.println("RecordLiteral:");
        indent += 2;
        Visitor.super.visitRecordLiteralExpression(recordLiteralExpression);
        indent -=2;

    }
    // RecordMember
    public void visitRecordTypeDeclarationMember(RecordType.Member member) throws Phase.Error {
        System.out.println("RecordMember:");
        indent +=2;
        Visitor.super.visitRecordTypeDeclarationMember(member);
        indent -=2;

    }
    // RecordTypeDecl
    public void visitRecordTypeDeclaration(RecordType recordType) throws Phase.Error {
        System.out.println("RecordTypeDecl:");
        indent += 2;
        Visitor.super.visitRecordTypeDeclaration(recordType);
        indent -= 2;

    }
    // ReturnStat
    public void visitReturnStatement(ReturnStatement returnStatement) throws Phase.Error {
        if (returnStatement.getExpression() == null)
            System.out.println("Return");
        else
            System.out.println("Return:");
        indent += 2;
        Visitor.super.visitReturnStatement(returnStatement);
        indent -= 2;

    }
    // Sequence
    public void visitSequence(Sequence sequence) throws Phase.Error {
        System.out.println("Sequence:[" + sequence.size() + " nodes]");
        int i = 0;
        for (Object a : sequence) {
            AST b = (AST)a;
            if (b != null) {
                System.out.println("Sequence[" + i++ + "]:");
                indent += 2;
                try {
                    b.accept(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                indent -= 2;
            } else
                System.out.println("Sequence[" + i++ + "]: = null");
        }

    }
    // SkipStat
    public void visitSkipStatement(SkipStatement skipStatement) throws Phase.Error {
        System.out.println("SkipStat");

    }
    // StopStat
    public void visitStopStat(SkipStatement ss) throws Phase.Error {
        System.out.println("StopStat");

    }
    // SuspendStat
    public void visitSuspendStatement(SuspendStatement suspendStatement) throws Phase.Error {
        System.out.println("SuspendStat:");
        indent += 2;
        Visitor.super.visitSuspendStatement(suspendStatement);
        indent -= 2;

    }
    // SwitchGroup
    public void visitSwitchStatementGroup(SwitchStatement.Group group) throws Phase.Error {
        System.out.println("SwitchGroup:");
        indent += 2;
        Visitor.super.visitSwitchStatementGroup(group);
        indent -= 2;

    }
    // SwitchLabel
    public void visitSwitchLabelExpression(SwitchStatement.Group.Case aCase) throws Phase.Error {
        System.out.println("SwitchLabel:");
        indent += 2;
        Visitor.super.visitSwitchLabelExpression(aCase);
        indent -= 2;

    }
    // SwitchStat
    public void visitSwitchStatement(SwitchStatement switchStatement) throws Phase.Error {
        System.out.println("SwitchStat:");
        indent += 2;
        Visitor.super.visitSwitchStatement(switchStatement);
        indent -= 2;

    }
    // SyncStat
    public void visitSyncStatement(SyncStatement ss) throws Phase.Error {
        System.out.println("SyncStat");
        indent += 2;
        Visitor.super.visitSyncStatement(ss);
        indent -= 2;

    }
    // Ternary
    public void visitTernaryExpression(TernaryExpression ternaryExpression) throws Phase.Error {
        System.out.println("Ternary:");
        indent += 2;
        Visitor.super.visitTernaryExpression(ternaryExpression);
        indent -= 2;

    }
    // TimeoutStat
    public void visitTimeoutStatement(TimeoutStatement timeoutStatement) throws Phase.Error {
        System.out.println("TimeoutStat:");
        indent += 2;
        Visitor.super.visitTimeoutStatement(timeoutStatement);
        indent -= 2;

    }
    // UnaryPostExpr
    public void visitUnaryPostExpression(UnaryPostExpression unaryPostExpression) throws Phase.Error {
        System.out.println("UnaryPostExpr:");
        indent += 2;
        Visitor.super.visitUnaryPostExpression(unaryPostExpression);
	System.out.println("Operator = " + unaryPostExpression.opString());
        indent -= 2;

    }
    // UnaryPreExpr
    public void visitUnaryPreExpression(UnaryPreExpression unaryPreExpression) throws Phase.Error {
        System.out.println("UnaryPreExpr:");
        indent += 2;
        Visitor.super.visitUnaryPreExpression(unaryPreExpression);
        System.out.println("Operator = " + unaryPreExpression.opString());
        indent -= 2;

    }

    // WhileStat
    public void visitWhileStatement(WhileStatement whileStatement) throws Phase.Error {
        System.out.println("WhileStat:");
        indent += 2;
        Visitor.super.visitWhileStatement(whileStatement);
        indent -= 2;
    }
}