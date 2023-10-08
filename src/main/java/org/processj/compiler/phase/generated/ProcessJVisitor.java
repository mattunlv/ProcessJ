// Generated from src/main/java/org/processj/compiler/phase/generated/ProcessJ.g4 by ANTLR 4.13.0
package org.processj.compiler.phase.generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ProcessJParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ProcessJVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(ProcessJParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(ProcessJParser.QualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#names}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNames(ProcessJParser.NamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(ProcessJParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#pragma}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPragma(ProcessJParser.PragmaContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#packageDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageDeclaration(ProcessJParser.PackageDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#importDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportDeclaration(ProcessJParser.ImportDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#typeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDeclaration(ProcessJParser.TypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#procedureTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureTypeDeclaration(ProcessJParser.ProcedureTypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#formalParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameters(ProcessJParser.FormalParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#protocolTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProtocolTypeDeclaration(ProcessJParser.ProtocolTypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#protocolBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProtocolBody(ProcessJParser.ProtocolBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#protocolCase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProtocolCase(ProcessJParser.ProtocolCaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#recordTypeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordTypeDeclaration(ProcessJParser.RecordTypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#extends}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtends(ProcessJParser.ExtendsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#annotations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotations(ProcessJParser.AnnotationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotation(ProcessJParser.AnnotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#annotation_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotation_value(ProcessJParser.Annotation_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#recordBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordBody(ProcessJParser.RecordBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#recordMember}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordMember(ProcessJParser.RecordMemberContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(ProcessJParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#typeWithoutDims}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeWithoutDims(ProcessJParser.TypeWithoutDimsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#typeWithDims}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeWithDims(ProcessJParser.TypeWithDimsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#primitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveType(ProcessJParser.PrimitiveTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#channelType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChannelType(ProcessJParser.ChannelTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#channelEndType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChannelEndType(ProcessJParser.ChannelEndTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#modifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModifier(ProcessJParser.ModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaration(ProcessJParser.VariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#variableDeclarators}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclarators(ProcessJParser.VariableDeclaratorsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#variableDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclarator(ProcessJParser.VariableDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#dimension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimension(ProcessJParser.DimensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#arrayInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayInitializer(ProcessJParser.ArrayInitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#variableInitializers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableInitializers(ProcessJParser.VariableInitializersContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(ProcessJParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(ProcessJParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#statementWithoutTrailingSubstatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementWithoutTrailingSubstatement(ProcessJParser.StatementWithoutTrailingSubstatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#barriers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBarriers(ProcessJParser.BarriersContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#statementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementNoShortIf(ProcessJParser.StatementNoShortIfContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#ifThenStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfThenStatement(ProcessJParser.IfThenStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#ifThenElseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfThenElseStatement(ProcessJParser.IfThenElseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#ifThenElseStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfThenElseStatementNoShortIf(ProcessJParser.IfThenElseStatementNoShortIfContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(ProcessJParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#whileStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatementNoShortIf(ProcessJParser.WhileStatementNoShortIfContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#forStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(ProcessJParser.ForStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#forStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatementNoShortIf(ProcessJParser.ForStatementNoShortIfContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#forInit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForInit(ProcessJParser.ForInitContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#forUpdate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForUpdate(ProcessJParser.ForUpdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#doStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoStatement(ProcessJParser.DoStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#claimStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClaimStatement(ProcessJParser.ClaimStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#claimStatementNoShortIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClaimStatementNoShortIf(ProcessJParser.ClaimStatementNoShortIfContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#channels_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChannels_(ProcessJParser.Channels_Context ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#channel_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChannel_(ProcessJParser.Channel_Context ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#barrierSyncStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBarrierSyncStatement(ProcessJParser.BarrierSyncStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#timeoutStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeoutStatement(ProcessJParser.TimeoutStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#statementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementExpression(ProcessJParser.StatementExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#labelledStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabelledStatement(ProcessJParser.LabelledStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#switchStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchStatement(ProcessJParser.SwitchStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#switchBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchBlock(ProcessJParser.SwitchBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#switchBlockStatementGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchBlockStatementGroup(ProcessJParser.SwitchBlockStatementGroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#altBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltBlock(ProcessJParser.AltBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#altCase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltCase(ProcessJParser.AltCaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#guard}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGuard(ProcessJParser.GuardContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(ProcessJParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentExpression(ProcessJParser.AssignmentExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#conditionalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalExpression(ProcessJParser.ConditionalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#conditionalOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalOrExpression(ProcessJParser.ConditionalOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#conditionalAndExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalAndExpression(ProcessJParser.ConditionalAndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInclusiveOrExpression(ProcessJParser.InclusiveOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusiveOrExpression(ProcessJParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#andExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(ProcessJParser.AndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#equalityExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpression(ProcessJParser.EqualityExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#relationalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpression(ProcessJParser.RelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#shiftExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftExpression(ProcessJParser.ShiftExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#additiveExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpression(ProcessJParser.AdditiveExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpression(ProcessJParser.MultiplicativeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpression(ProcessJParser.UnaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#preIncrementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreIncrementExpression(ProcessJParser.PreIncrementExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#preDecrementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreDecrementExpression(ProcessJParser.PreDecrementExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#unaryExpressionNotPlusMinus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpressionNotPlusMinus(ProcessJParser.UnaryExpressionNotPlusMinusContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#castExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastExpression(ProcessJParser.CastExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#postfixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixExpression(ProcessJParser.PostfixExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpression(ProcessJParser.PrimaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#primaryExpressionNoCreation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpressionNoCreation(ProcessJParser.PrimaryExpressionNoCreationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#leftHandSideExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeftHandSideExpression(ProcessJParser.LeftHandSideExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#suffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuffix(ProcessJParser.SuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#arrayAccessSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayAccessSuffix(ProcessJParser.ArrayAccessSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#recordAccessSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordAccessSuffix(ProcessJParser.RecordAccessSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#channelReadSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChannelReadSuffix(ProcessJParser.ChannelReadSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#channelWriteSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChannelWriteSuffix(ProcessJParser.ChannelWriteSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#invocationSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInvocationSuffix(ProcessJParser.InvocationSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#arguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArguments(ProcessJParser.ArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#newArrayExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewArrayExpression(ProcessJParser.NewArrayExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#dimExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimExpression(ProcessJParser.DimExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#dims}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDims(ProcessJParser.DimsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#newRecordExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewRecordExpression(ProcessJParser.NewRecordExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#newRecordExpressionArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewRecordExpressionArguments(ProcessJParser.NewRecordExpressionArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#newProtocolExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewProtocolExpression(ProcessJParser.NewProtocolExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#newMobileExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewMobileExpression(ProcessJParser.NewMobileExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(ProcessJParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcessJParser#assignmentOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentOperator(ProcessJParser.AssignmentOperatorContext ctx);
}