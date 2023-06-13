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
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.alt.GuardStatement;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.declarative.LocalDeclaration;
import org.processj.compiler.ast.statement.declarative.ProtocolCase;
import org.processj.compiler.ast.statement.declarative.RecordMemberDeclaration;
import org.processj.compiler.ast.statement.switched.SwitchGroupStatement;
import org.processj.compiler.ast.expression.result.SwitchLabel;
import org.processj.compiler.ast.statement.switched.SwitchStatement;
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.ast.statement.yielding.ParBlock;
import org.processj.compiler.ast.type.*;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.alt.AltCase;
import org.processj.compiler.ast.statement.alt.AltStatement;
import org.processj.compiler.phases.phase.Visitor;

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
    public AST visitAltCase(AltCase altCase) throws Phase.Error {
        System.out.println(indent(altCase.line) + "AltCase:");
        indent += 2;
        Visitor.super.visitAltCase(altCase);
        indent -= 2;
        return null;
    }
    // AltStat
    public AST visitAltStatement(AltStatement altStatement) throws Phase.Error {
        System.out.println(indent(altStatement.line) + "AltStat:");
        indent += 2;
        Visitor.super.visitAltStatement(altStatement);
        indent -= 2;
        return null;
    }
    // ArrayAccessExpr
    public AST visitArrayAccessExpression(ArrayAccessExpression arrayAccessExpression) throws Phase.Error {
        System.out.println(indent(arrayAccessExpression.line) + "ArrayAccessExpr:");
        indent += 2;
        Visitor.super.visitArrayAccessExpression(arrayAccessExpression);
        indent -=2;
        return null;
    }
    // ArrayLiteral
    public AST visitArrayLiteralExpression(ArrayLiteralExpression arrayLiteralExpression) throws Phase.Error {
        System.out.println(indent(arrayLiteralExpression.line) + "ArrayLiteral:");
        indent += 2;
        Visitor.super.visitArrayLiteralExpression(arrayLiteralExpression);
        indent -= 2;
        return null;
    }
    // ArrayType
    public AST visitArrayType(ArrayType arrayType) throws Phase.Error {
        System.out.print(indent(arrayType.line) + "ArrayType:");
        for (int i = 0; i< arrayType.getDepth(); i++)
            System.out.print("[]");
        System.out.println();
        indent += 2;
        Visitor.super.visitArrayType(arrayType);
        indent -= 2;
        return null;
    }
    // Assignment
    public AST visitAssignmentExpression(AssignmentExpression assignmentExpression) throws Phase.Error {
        System.out.println(indent(assignmentExpression.line) + "Assignment:");
        indent += 2;
        Visitor.super.visitAssignmentExpression(assignmentExpression);
        System.out.println(indent(assignmentExpression.line) + "Operator: " + assignmentExpression.opString());
        indent -= 2;
        return null;
    }
    // BinaryExpr
    public AST visitBinaryExpression(BinaryExpression binaryExpression) throws Phase.Error {
        System.out.println(indent(binaryExpression.line) + "BinaryExpr:");
        indent += 2;
        Visitor.super.visitBinaryExpression(binaryExpression);
        System.out.println(indent(binaryExpression.line) + "Operator: " + binaryExpression.opString());
        indent -= 2;
        return null;
    }
    // Block
    public AST visitBlockStatement(BlockStatement blockStatement) throws Phase.Error {
        System.out.println(indent(blockStatement.line) + "Block:");
        indent += 2;
        Visitor.super.visitBlockStatement(blockStatement);
        indent -= 2;
        return null;
    }
    // BreakStat
    /** BREAK STATEMENT */
    public AST visitBreakStatement(BreakStatement breakStatement) throws Phase.Error {
        System.out.println(indent(breakStatement.line) + "BreakStat");
        indent += 2;
        Visitor.super.visitBreakStatement(breakStatement);
        indent -= 2;
        return null;
    }
    // CastExpr
    public AST visitCastExpression(CastExpression castExpression) throws Phase.Error {
        System.out.println(indent(castExpression.line) + "CastExpr:");
        indent += 2;
        Visitor.super.visitCastExpression(castExpression);
        indent -= 2;
        return null;
    }
    // ChannelType
    public AST visitChannelType(ChannelType channelType) throws Phase.Error {
        System.out.println(indent(channelType.line) + "ChannelType:");
        indent += 2;
        Visitor.super.visitChannelType(channelType);
        indent -= 2;
        return null;
    }
    // ChannelEndExpr
    public AST visitChannelEndExpression(ChannelEndExpression channelEndExpression) throws Phase.Error {
        System.out.println(indent(channelEndExpression.line) + "ChannelEndExpr:");
        indent += 2;
        Visitor.super.visitChannelEndExpression(channelEndExpression);
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
    public AST visitChannelReadExpression(ChannelReadExpression channelReadExpression) throws Phase.Error {
        System.out.println(indent(channelReadExpression.line) + "ChannelReadExpr:");
        indent += 2;
        Visitor.super.visitChannelReadExpression(channelReadExpression);
        indent -= 2;
        return null;
    }
    // ChannelWriteStat
    public AST visitChannelWriteStatement(ChannelWriteStatement channelWriteStatement) throws Phase.Error {
        System.out.println(indent(channelWriteStatement.line) + "ChannelWriteStat:");
        indent += 2;
        Visitor.super.visitChannelWriteStatement(channelWriteStatement);
        indent -= 2;
        return null;
    }
    // ClaimStat
    public AST visitClaimStatement(ClaimStatement claimStatement) throws Phase.Error {
        System.out.println(indent(claimStatement.line) + "ClaimStat:");
        indent += 2;
        Visitor.super.visitClaimStatement(claimStatement);
        indent -= 2;
        return null;
    }
    // Compilation
    public AST visitCompilation(Compilation compilation) throws Phase.Error {
        System.out.println(indent(compilation.line) + "Compilation:");
        indent += 2;
        Visitor.super.visitCompilation(compilation);
        indent -= 2;
        return null;
    }
    // ConstantDecl
    public AST visitConstantDeclaration(ConstantDeclaration constantDeclaration) throws Phase.Error {
        System.out.println(indent(constantDeclaration.line) + "ConstantDecl:");
        indent += 2;
        Visitor.super.visitConstantDeclaration(constantDeclaration);
        indent -= 2;
        return null;
    }
    // ContinueStat
    public AST visitContinueStatement(ContinueStatement continueStatement) throws Phase.Error {
        System.out.println(indent(continueStatement.line) + "Continue Statement");
        indent += 2;
        Visitor.super.visitContinueStatement(continueStatement);
        indent -= 2;
        return null;
    }
    // DoStat
    public AST visitDoStatement(DoStatement doStatement) throws Phase.Error {
        System.out.println(indent(doStatement.line) + "DoStat:");
        indent += 2;
        Visitor.super.visitDoStatement(doStatement);
        indent -= 2;
        return null;
    }
    // ExprStat
    public AST visitExpressionStatement(ExpressionStatement expressionStatement) throws Phase.Error {
        System.out.println(indent(expressionStatement.line) + "ExprStat:");
        indent += 2;
        Visitor.super.visitExpressionStatement(expressionStatement);
        indent -= 2;
        return null;
    }
    // ExternType
    public AST visitExternType(ExternType externType) throws Phase.Error {
        System.out.println(indent(externType.line) + "ExternType:");
        indent += 2;
        Visitor.super.visitExternType(externType);
        indent -= 2;
        return null;
    }
    // ForStat
    public AST visitForStatement(ForStatement forStatement) throws Phase.Error {
        System.out.println(indent(forStatement.line) + "ForStat:");
        indent += 2;
        Visitor.super.visitForStatement(forStatement);
        indent -= 2;
        return null;
    }
    // Guard
    public AST visitGuardStatement(GuardStatement g) throws Phase.Error {
        System.out.println(indent(g.line) + "Guard:");
        indent += 2;
        Visitor.super.visitGuardStatement(g);
        indent -= 2;
        return null;
    }
    // IfStat
    public AST visitIfStatement(IfStatement ifStatement) throws Phase.Error {
        System.out.println(indent(ifStatement.line) + "IfStat:");
        indent += 2;
        Visitor.super.visitIfStatement(ifStatement);
        indent -= 2;
        return null;
    }
    // ImplicitImport
    public AST visitImplicitImportExpression(ImplicitImportExpression im) throws Phase.Error {
        System.out.println(indent(im.line) + "ImplicitImport:");
        indent += 2;
        Visitor.super.visitImplicitImportExpression(im);
        indent -= 2;
        return null;
    }
    // Import
    public AST visitImport(Import importName) throws Phase.Error {
        System.out.println(indent(importName.line) + "Import:");
        indent += 2;
        Visitor.super.visitImport(importName);
        indent -= 2;
        return null;
    }
    // Invocation
    public AST visitInvocationExpression(InvocationExpression invocationExpression) throws Phase.Error {
        System.out.println(indent(invocationExpression.line) + "Invocation:");
        indent += 2;
        Visitor.super.visitInvocationExpression(invocationExpression);
        indent -= 2;
        return null;
    }
    // LocalDecl
    public AST visitLocalDeclaration(LocalDeclaration localDeclaration) throws Phase.Error {
        System.out.println(indent(localDeclaration.line) + "LocalDecl:");
        indent += 2;
        Visitor.super.visitLocalDeclaration(localDeclaration);
        indent -= 2;
        return null;
    }
    // Modifier
    public AST visitModifier(Modifier modifier) throws Phase.Error {
        System.out.println(indent(modifier.line) + "Modifier (" + modifier + ")");
        return null;
    }
    // Name
    public AST visitName(Name name) throws Phase.Error {
        System.out.println(indent(name.line) + "Name = " + name);
        return null;
    }
    // NamedType
    public AST visitNamedType(NamedType namedType) throws Phase.Error {
        System.out.println(indent(namedType.line)+ "NamedType:");
        indent += 2;
        Visitor.super.visitNamedType(namedType);
        indent -= 2;
        return null;
    }
    // NameExpr
    public AST visitNameExpression(NameExpression nameExpression) throws Phase.Error {
        System.out.println(indent(nameExpression.line) + "NameExpr:");
        indent += 2;
        Visitor.super.visitNameExpression(nameExpression);
        indent -= 2;
        return null;
    }
    // NewArray
    public AST visitNewArrayExpression(NewArrayExpression newArrayExpression) throws Phase.Error {
        System.out.println(indent(newArrayExpression.line) + "New Array");
        indent += 2;
        Visitor.super.visitNewArrayExpression(newArrayExpression);
        indent -= 2;
        return null;
    }
    // NewMobile
    public AST visitNMewMobile(NewMobileExpression nm) throws Phase.Error {
        System.out.println(indent(nm.line) + "NewMobile:");
        indent += 2;
        Visitor.super.visitNewMobileExpression(nm);
        indent -= 2;
        return null;
    }
    // ParamDecl
    public AST visitParameterDeclaration(ParameterDeclaration parameterDeclaration) throws Phase.Error {
        System.out.println(indent(parameterDeclaration.line) + "ParamDecl: ");
        indent += 2;
        Visitor.super.visitParameterDeclaration(parameterDeclaration);
        indent -= 2;
        return null;
    }
    // ParBlock
    public AST visitParBlockStatement(ParBlock parBlock) throws Phase.Error {
        System.out.println(indent(parBlock.line) + "ParBlock:");
        indent += 2;
        Visitor.super.visitParBlockStatement(parBlock);
        indent -= 2;
        return null;
    }
    // Pragma
    public AST visitPragma(Pragma pragma) throws Phase.Error {
        System.out.println(indent(pragma.line) + "Pragma:");
        indent += 2;
        Visitor.super.visitPragma(pragma);
        indent -= 2;
        return null;
    }
    // PrimitiveLiteral
    public AST visitPrimitiveLiteralExpression(PrimitiveLiteralExpression primitiveLiteralExpression) throws Phase.Error {
        System.out.println(indent(primitiveLiteralExpression.line) + "PrimtiveLiteral = " + primitiveLiteralExpression);
        indent += 2;
        Visitor.super.visitPrimitiveLiteralExpression(primitiveLiteralExpression);
        indent -= 2;
        return null;
    }
    // PrimitiveType
    public AST visitPrimitiveType(PrimitiveType primitiveType) throws Phase.Error {
        System.out.println(indent(primitiveType.line) + "PrimitiveType = " + primitiveType);
        return null;
    }
    // ProcTypeDecl
    public AST visitProcedureTypeDeclaration(ProcedureTypeDeclaration procedureTypeDeclaration) throws Phase.Error {
        System.out.println(indent(procedureTypeDeclaration.line) + "ProcTypeDecl:");
        indent += 2;

        System.out.println(indent(procedureTypeDeclaration.line) + "Annotations: " + procedureTypeDeclaration.getAnnotations().toString());
        Visitor.super.visitProcedureTypeDeclaration(procedureTypeDeclaration);
        indent -= 2;
        return null;
    }
    // ProtocolLiteral
    public AST visitProtocolLiteralExpression(ProtocolLiteralExpression protocolLiteralExpression) throws Phase.Error {
        System.out.println(indent(protocolLiteralExpression.line) + "ProtocolLiteral:");
        indent += 2;
        Visitor.super.visitProtocolLiteralExpression(protocolLiteralExpression);
        indent -=2;
        return null;
    }
    // ProtocolCase
    public AST visitProtocolCase(ProtocolCase protocolCase) throws Phase.Error {
        System.out.println(indent(protocolCase.line) + "ProtocolCase:");
        indent += 2;
        Visitor.super.visitProtocolCase(protocolCase);
        indent -=2;
        return null;
    }
    // ProtocolTypeDecl
    public AST visitProtocolTypeDeclaration(ProtocolTypeDeclaration protocolTypeDeclaration) throws Phase.Error {
        System.out.println(indent(protocolTypeDeclaration.line) + "ProtocolTypeDecl:");
        indent += 2;
        Visitor.super.visitProtocolTypeDeclaration(protocolTypeDeclaration);
        indent -= 2;
        return null;
    }
    // RecordAccess
    public AST visitRecordAccessExpression(RecordAccessExpression recordAccessExpression) throws Phase.Error {
        System.out.println(indent(recordAccessExpression.line) + "RecordAccess:");
        indent += 2;
        Visitor.super.visitRecordAccessExpression(recordAccessExpression);
        indent -= 2;
        return null;
    }
    // RecordLiteral
    public AST visitRecordLiteralExpression(RecordLiteralExpression recordLiteralExpression) throws Phase.Error {
        System.out.println(indent(recordLiteralExpression.line) + "RecordLiteral:");
        indent += 2;
        Visitor.super.visitRecordLiteralExpression(recordLiteralExpression);
        indent -=2;
        return null;
    }
    // RecordMember
    public AST visitRecordMemberDeclaration(RecordMemberDeclaration recordMemberDeclaration) throws Phase.Error {
        System.out.println(indent(recordMemberDeclaration.line) + "RecordMember:");
        indent +=2;
        Visitor.super.visitRecordMemberDeclaration(recordMemberDeclaration);
        indent -=2;
        return null;
    }
    // RecordTypeDecl
    public AST visitRecordTypeDeclaration(RecordTypeDeclaration recordTypeDeclaration) throws Phase.Error {
        System.out.println(indent(recordTypeDeclaration.line)+ "RecordTypeDecl:");
        indent += 2;
        Visitor.super.visitRecordTypeDeclaration(recordTypeDeclaration);
        indent -= 2;
        return null;
    }
    // ReturnStat
    public AST visitReturnStatement(ReturnStatement returnStatement) throws Phase.Error {
        if (returnStatement.getExpression() == null)
            System.out.println(indent(returnStatement.line) + "Return");
        else
            System.out.println(indent(returnStatement.line) + "Return:");
        indent += 2;
        Visitor.super.visitReturnStatement(returnStatement);
        indent -= 2;
        return null;
    }
    // Sequence
    public AST visitSequence(Sequence sequence) throws Phase.Error {
        System.out.println(indent(sequence.line) + "Sequence:[" + sequence.size() + " nodes]");
        int i = 0;
        for (Object a : sequence) {
            AST b = (AST)a;
            if (b != null) {
                System.out.println(indent(b.line) + "Sequence[" + i++ + "]:");
                indent += 2;
                try {
                    b.visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                indent -= 2;
            } else
                System.out.println(indent(sequence.line) + "Sequence[" + i++ + "]: = null");
        }
        return null;
    }
    // SkipStat
    public AST visitSkipStatement(SkipStatement skipStatement) throws Phase.Error {
        System.out.println(indent(skipStatement.line) + "SkipStat");
        return null;
    }
    // StopStat
    public AST visitStopStat(SkipStatement ss) throws Phase.Error {
        System.out.println(indent(ss.line) + "StopStat");
        return null;
    }
    // SuspendStat
    public AST visitSuspendStatement(SuspendStatement suspendStatement) throws Phase.Error {
        System.out.println(indent(suspendStatement.line) + "SuspendStat:");
        indent += 2;
        Visitor.super.visitSuspendStatement(suspendStatement);
        indent -= 2;
        return null;
    }
    // SwitchGroup
    public AST visitSwitchGroupStatement(SwitchGroupStatement switchGroupStatement) throws Phase.Error {
        System.out.println(indent(switchGroupStatement.line) + "SwitchGroup:");
        indent += 2;
        Visitor.super.visitSwitchGroupStatement(switchGroupStatement);
        indent -= 2;
        return null;
    }
    // SwitchLabel
    public AST visitSwitchLabelExpression(SwitchLabel switchLabel) throws Phase.Error {
        System.out.println(indent(switchLabel.line) + "SwitchLabel:");
        indent += 2;
        Visitor.super.visitSwitchLabelExpression(switchLabel);
        indent -= 2;
        return null;
    }
    // SwitchStat
    public AST visitSwitchStatement(SwitchStatement switchStatement) throws Phase.Error {
        System.out.println(indent(switchStatement.line) + "SwitchStat:");
        indent += 2;
        Visitor.super.visitSwitchStatement(switchStatement);
        indent -= 2;
        return null;
    }
    // SyncStat
    public AST visitSyncStatement(SyncStatement ss) throws Phase.Error {
        System.out.println(indent(ss.line) + "SyncStat");
        indent += 2;
        Visitor.super.visitSyncStatement(ss);
        indent -= 2;
        return null;
    }
    // Ternary
    public AST visitTernaryExpression(TernaryExpression ternaryExpression) throws Phase.Error {
        System.out.println(indent(ternaryExpression.line) + "Ternary:");
        indent += 2;
        Visitor.super.visitTernaryExpression(ternaryExpression);
        indent -= 2;
        return null;
    }
    // TimeoutStat
    public AST visitTimeoutStatement(TimeoutStatement timeoutStatement) throws Phase.Error {
        System.out.println(indent(timeoutStatement.line) + "TimeoutStat:");
        indent += 2;
        Visitor.super.visitTimeoutStatement(timeoutStatement);
        indent -= 2;
        return null;
    }
    // UnaryPostExpr
    public AST visitUnaryPostExpression(UnaryPostExpression unaryPostExpression) throws Phase.Error {
        System.out.println(indent(unaryPostExpression.line) + "UnaryPostExpr:");
        indent += 2;
        Visitor.super.visitUnaryPostExpression(unaryPostExpression);
	System.out.println(indent(unaryPostExpression.line) + "Operator = " + unaryPostExpression.opString());
        indent -= 2;
        return null;
    }
    // UnaryPreExpr
    public AST visitUnaryPreExpression(UnaryPreExpression unaryPreExpression) throws Phase.Error {
        System.out.println(indent(unaryPreExpression.line) + "UnaryPreExpr:");
        indent += 2;
        Visitor.super.visitUnaryPreExpression(unaryPreExpression);
        System.out.println(indent(unaryPreExpression.line) + "Operator = " + unaryPreExpression.opString());
        indent -= 2;
        return null;
    }

    // WhileStat
    public AST visitWhileStatement(WhileStatement whileStatement) throws Phase.Error {
        System.out.println(indent(whileStatement.line) + "WhileStat:");
        indent += 2;
        Visitor.super.visitWhileStatement(whileStatement);
        indent -= 2;
        return null;
    }
}