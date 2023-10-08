// Generated from src/main/java/org/processj/compiler/phase/generated/ProcessJ.g4 by ANTLR 4.13.0
package org.processj.compiler.phase.generated;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ProcessJParser}.
 */
public interface ProcessJListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(ProcessJParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(ProcessJParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(ProcessJParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(ProcessJParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#names}.
	 * @param ctx the parse tree
	 */
	void enterNames(ProcessJParser.NamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#names}.
	 * @param ctx the parse tree
	 */
	void exitNames(ProcessJParser.NamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(ProcessJParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(ProcessJParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#pragma}.
	 * @param ctx the parse tree
	 */
	void enterPragma(ProcessJParser.PragmaContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#pragma}.
	 * @param ctx the parse tree
	 */
	void exitPragma(ProcessJParser.PragmaContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#packageDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterPackageDeclaration(ProcessJParser.PackageDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#packageDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitPackageDeclaration(ProcessJParser.PackageDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterImportDeclaration(ProcessJParser.ImportDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitImportDeclaration(ProcessJParser.ImportDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#typeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterTypeDeclaration(ProcessJParser.TypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#typeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitTypeDeclaration(ProcessJParser.TypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#procedureTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterProcedureTypeDeclaration(ProcessJParser.ProcedureTypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#procedureTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitProcedureTypeDeclaration(ProcessJParser.ProcedureTypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameters(ProcessJParser.FormalParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameters(ProcessJParser.FormalParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#protocolTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterProtocolTypeDeclaration(ProcessJParser.ProtocolTypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#protocolTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitProtocolTypeDeclaration(ProcessJParser.ProtocolTypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#protocolBody}.
	 * @param ctx the parse tree
	 */
	void enterProtocolBody(ProcessJParser.ProtocolBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#protocolBody}.
	 * @param ctx the parse tree
	 */
	void exitProtocolBody(ProcessJParser.ProtocolBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#protocolCase}.
	 * @param ctx the parse tree
	 */
	void enterProtocolCase(ProcessJParser.ProtocolCaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#protocolCase}.
	 * @param ctx the parse tree
	 */
	void exitProtocolCase(ProcessJParser.ProtocolCaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#recordTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterRecordTypeDeclaration(ProcessJParser.RecordTypeDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#recordTypeDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitRecordTypeDeclaration(ProcessJParser.RecordTypeDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#extends}.
	 * @param ctx the parse tree
	 */
	void enterExtends(ProcessJParser.ExtendsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#extends}.
	 * @param ctx the parse tree
	 */
	void exitExtends(ProcessJParser.ExtendsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#annotations}.
	 * @param ctx the parse tree
	 */
	void enterAnnotations(ProcessJParser.AnnotationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#annotations}.
	 * @param ctx the parse tree
	 */
	void exitAnnotations(ProcessJParser.AnnotationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation(ProcessJParser.AnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation(ProcessJParser.AnnotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#annotation_value}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation_value(ProcessJParser.Annotation_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#annotation_value}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation_value(ProcessJParser.Annotation_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#recordBody}.
	 * @param ctx the parse tree
	 */
	void enterRecordBody(ProcessJParser.RecordBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#recordBody}.
	 * @param ctx the parse tree
	 */
	void exitRecordBody(ProcessJParser.RecordBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#recordMember}.
	 * @param ctx the parse tree
	 */
	void enterRecordMember(ProcessJParser.RecordMemberContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#recordMember}.
	 * @param ctx the parse tree
	 */
	void exitRecordMember(ProcessJParser.RecordMemberContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(ProcessJParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(ProcessJParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#typeWithoutDims}.
	 * @param ctx the parse tree
	 */
	void enterTypeWithoutDims(ProcessJParser.TypeWithoutDimsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#typeWithoutDims}.
	 * @param ctx the parse tree
	 */
	void exitTypeWithoutDims(ProcessJParser.TypeWithoutDimsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#typeWithDims}.
	 * @param ctx the parse tree
	 */
	void enterTypeWithDims(ProcessJParser.TypeWithDimsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#typeWithDims}.
	 * @param ctx the parse tree
	 */
	void exitTypeWithDims(ProcessJParser.TypeWithDimsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveType(ProcessJParser.PrimitiveTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveType(ProcessJParser.PrimitiveTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#channelType}.
	 * @param ctx the parse tree
	 */
	void enterChannelType(ProcessJParser.ChannelTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#channelType}.
	 * @param ctx the parse tree
	 */
	void exitChannelType(ProcessJParser.ChannelTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#channelEndType}.
	 * @param ctx the parse tree
	 */
	void enterChannelEndType(ProcessJParser.ChannelEndTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#channelEndType}.
	 * @param ctx the parse tree
	 */
	void exitChannelEndType(ProcessJParser.ChannelEndTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#modifier}.
	 * @param ctx the parse tree
	 */
	void enterModifier(ProcessJParser.ModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#modifier}.
	 * @param ctx the parse tree
	 */
	void exitModifier(ProcessJParser.ModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclaration(ProcessJParser.VariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclaration(ProcessJParser.VariableDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#variableDeclarators}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclarators(ProcessJParser.VariableDeclaratorsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#variableDeclarators}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclarators(ProcessJParser.VariableDeclaratorsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#variableDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclarator(ProcessJParser.VariableDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#variableDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclarator(ProcessJParser.VariableDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#dimension}.
	 * @param ctx the parse tree
	 */
	void enterDimension(ProcessJParser.DimensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#dimension}.
	 * @param ctx the parse tree
	 */
	void exitDimension(ProcessJParser.DimensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#arrayInitializer}.
	 * @param ctx the parse tree
	 */
	void enterArrayInitializer(ProcessJParser.ArrayInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#arrayInitializer}.
	 * @param ctx the parse tree
	 */
	void exitArrayInitializer(ProcessJParser.ArrayInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#variableInitializers}.
	 * @param ctx the parse tree
	 */
	void enterVariableInitializers(ProcessJParser.VariableInitializersContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#variableInitializers}.
	 * @param ctx the parse tree
	 */
	void exitVariableInitializers(ProcessJParser.VariableInitializersContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(ProcessJParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(ProcessJParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(ProcessJParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(ProcessJParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#statementWithoutTrailingSubstatement}.
	 * @param ctx the parse tree
	 */
	void enterStatementWithoutTrailingSubstatement(ProcessJParser.StatementWithoutTrailingSubstatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#statementWithoutTrailingSubstatement}.
	 * @param ctx the parse tree
	 */
	void exitStatementWithoutTrailingSubstatement(ProcessJParser.StatementWithoutTrailingSubstatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#barriers}.
	 * @param ctx the parse tree
	 */
	void enterBarriers(ProcessJParser.BarriersContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#barriers}.
	 * @param ctx the parse tree
	 */
	void exitBarriers(ProcessJParser.BarriersContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#statementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void enterStatementNoShortIf(ProcessJParser.StatementNoShortIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#statementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void exitStatementNoShortIf(ProcessJParser.StatementNoShortIfContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#ifThenStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfThenStatement(ProcessJParser.IfThenStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#ifThenStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfThenStatement(ProcessJParser.IfThenStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#ifThenElseStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfThenElseStatement(ProcessJParser.IfThenElseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#ifThenElseStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfThenElseStatement(ProcessJParser.IfThenElseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#ifThenElseStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void enterIfThenElseStatementNoShortIf(ProcessJParser.IfThenElseStatementNoShortIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#ifThenElseStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void exitIfThenElseStatementNoShortIf(ProcessJParser.IfThenElseStatementNoShortIfContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(ProcessJParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(ProcessJParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#whileStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatementNoShortIf(ProcessJParser.WhileStatementNoShortIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#whileStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatementNoShortIf(ProcessJParser.WhileStatementNoShortIfContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void enterForStatement(ProcessJParser.ForStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void exitForStatement(ProcessJParser.ForStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#forStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void enterForStatementNoShortIf(ProcessJParser.ForStatementNoShortIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#forStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void exitForStatementNoShortIf(ProcessJParser.ForStatementNoShortIfContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#forInit}.
	 * @param ctx the parse tree
	 */
	void enterForInit(ProcessJParser.ForInitContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#forInit}.
	 * @param ctx the parse tree
	 */
	void exitForInit(ProcessJParser.ForInitContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#forUpdate}.
	 * @param ctx the parse tree
	 */
	void enterForUpdate(ProcessJParser.ForUpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#forUpdate}.
	 * @param ctx the parse tree
	 */
	void exitForUpdate(ProcessJParser.ForUpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#doStatement}.
	 * @param ctx the parse tree
	 */
	void enterDoStatement(ProcessJParser.DoStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#doStatement}.
	 * @param ctx the parse tree
	 */
	void exitDoStatement(ProcessJParser.DoStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#claimStatement}.
	 * @param ctx the parse tree
	 */
	void enterClaimStatement(ProcessJParser.ClaimStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#claimStatement}.
	 * @param ctx the parse tree
	 */
	void exitClaimStatement(ProcessJParser.ClaimStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#claimStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void enterClaimStatementNoShortIf(ProcessJParser.ClaimStatementNoShortIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#claimStatementNoShortIf}.
	 * @param ctx the parse tree
	 */
	void exitClaimStatementNoShortIf(ProcessJParser.ClaimStatementNoShortIfContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#channels_}.
	 * @param ctx the parse tree
	 */
	void enterChannels_(ProcessJParser.Channels_Context ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#channels_}.
	 * @param ctx the parse tree
	 */
	void exitChannels_(ProcessJParser.Channels_Context ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#channel_}.
	 * @param ctx the parse tree
	 */
	void enterChannel_(ProcessJParser.Channel_Context ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#channel_}.
	 * @param ctx the parse tree
	 */
	void exitChannel_(ProcessJParser.Channel_Context ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#barrierSyncStatement}.
	 * @param ctx the parse tree
	 */
	void enterBarrierSyncStatement(ProcessJParser.BarrierSyncStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#barrierSyncStatement}.
	 * @param ctx the parse tree
	 */
	void exitBarrierSyncStatement(ProcessJParser.BarrierSyncStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#timeoutStatement}.
	 * @param ctx the parse tree
	 */
	void enterTimeoutStatement(ProcessJParser.TimeoutStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#timeoutStatement}.
	 * @param ctx the parse tree
	 */
	void exitTimeoutStatement(ProcessJParser.TimeoutStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#statementExpression}.
	 * @param ctx the parse tree
	 */
	void enterStatementExpression(ProcessJParser.StatementExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#statementExpression}.
	 * @param ctx the parse tree
	 */
	void exitStatementExpression(ProcessJParser.StatementExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#labelledStatement}.
	 * @param ctx the parse tree
	 */
	void enterLabelledStatement(ProcessJParser.LabelledStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#labelledStatement}.
	 * @param ctx the parse tree
	 */
	void exitLabelledStatement(ProcessJParser.LabelledStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#switchStatement}.
	 * @param ctx the parse tree
	 */
	void enterSwitchStatement(ProcessJParser.SwitchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#switchStatement}.
	 * @param ctx the parse tree
	 */
	void exitSwitchStatement(ProcessJParser.SwitchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#switchBlock}.
	 * @param ctx the parse tree
	 */
	void enterSwitchBlock(ProcessJParser.SwitchBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#switchBlock}.
	 * @param ctx the parse tree
	 */
	void exitSwitchBlock(ProcessJParser.SwitchBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#switchBlockStatementGroup}.
	 * @param ctx the parse tree
	 */
	void enterSwitchBlockStatementGroup(ProcessJParser.SwitchBlockStatementGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#switchBlockStatementGroup}.
	 * @param ctx the parse tree
	 */
	void exitSwitchBlockStatementGroup(ProcessJParser.SwitchBlockStatementGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#altBlock}.
	 * @param ctx the parse tree
	 */
	void enterAltBlock(ProcessJParser.AltBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#altBlock}.
	 * @param ctx the parse tree
	 */
	void exitAltBlock(ProcessJParser.AltBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#altCase}.
	 * @param ctx the parse tree
	 */
	void enterAltCase(ProcessJParser.AltCaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#altCase}.
	 * @param ctx the parse tree
	 */
	void exitAltCase(ProcessJParser.AltCaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#guard}.
	 * @param ctx the parse tree
	 */
	void enterGuard(ProcessJParser.GuardContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#guard}.
	 * @param ctx the parse tree
	 */
	void exitGuard(ProcessJParser.GuardContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(ProcessJParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(ProcessJParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentExpression(ProcessJParser.AssignmentExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentExpression(ProcessJParser.AssignmentExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalExpression(ProcessJParser.ConditionalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalExpression(ProcessJParser.ConditionalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#conditionalOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalOrExpression(ProcessJParser.ConditionalOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#conditionalOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalOrExpression(ProcessJParser.ConditionalOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#conditionalAndExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalAndExpression(ProcessJParser.ConditionalAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#conditionalAndExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalAndExpression(ProcessJParser.ConditionalAndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterInclusiveOrExpression(ProcessJParser.InclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitInclusiveOrExpression(ProcessJParser.InclusiveOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterExclusiveOrExpression(ProcessJParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitExclusiveOrExpression(ProcessJParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(ProcessJParser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(ProcessJParser.AndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpression(ProcessJParser.EqualityExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpression(ProcessJParser.EqualityExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpression(ProcessJParser.RelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpression(ProcessJParser.RelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void enterShiftExpression(ProcessJParser.ShiftExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void exitShiftExpression(ProcessJParser.ShiftExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpression(ProcessJParser.AdditiveExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpression(ProcessJParser.AdditiveExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpression(ProcessJParser.MultiplicativeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpression(ProcessJParser.MultiplicativeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpression(ProcessJParser.UnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpression(ProcessJParser.UnaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#preIncrementExpression}.
	 * @param ctx the parse tree
	 */
	void enterPreIncrementExpression(ProcessJParser.PreIncrementExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#preIncrementExpression}.
	 * @param ctx the parse tree
	 */
	void exitPreIncrementExpression(ProcessJParser.PreIncrementExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#preDecrementExpression}.
	 * @param ctx the parse tree
	 */
	void enterPreDecrementExpression(ProcessJParser.PreDecrementExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#preDecrementExpression}.
	 * @param ctx the parse tree
	 */
	void exitPreDecrementExpression(ProcessJParser.PreDecrementExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#unaryExpressionNotPlusMinus}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpressionNotPlusMinus(ProcessJParser.UnaryExpressionNotPlusMinusContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#unaryExpressionNotPlusMinus}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpressionNotPlusMinus(ProcessJParser.UnaryExpressionNotPlusMinusContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#castExpression}.
	 * @param ctx the parse tree
	 */
	void enterCastExpression(ProcessJParser.CastExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#castExpression}.
	 * @param ctx the parse tree
	 */
	void exitCastExpression(ProcessJParser.CastExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void enterPostfixExpression(ProcessJParser.PostfixExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void exitPostfixExpression(ProcessJParser.PostfixExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpression(ProcessJParser.PrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpression(ProcessJParser.PrimaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#primaryExpressionNoCreation}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpressionNoCreation(ProcessJParser.PrimaryExpressionNoCreationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#primaryExpressionNoCreation}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpressionNoCreation(ProcessJParser.PrimaryExpressionNoCreationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#leftHandSideExpression}.
	 * @param ctx the parse tree
	 */
	void enterLeftHandSideExpression(ProcessJParser.LeftHandSideExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#leftHandSideExpression}.
	 * @param ctx the parse tree
	 */
	void exitLeftHandSideExpression(ProcessJParser.LeftHandSideExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#suffix}.
	 * @param ctx the parse tree
	 */
	void enterSuffix(ProcessJParser.SuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#suffix}.
	 * @param ctx the parse tree
	 */
	void exitSuffix(ProcessJParser.SuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#arrayAccessSuffix}.
	 * @param ctx the parse tree
	 */
	void enterArrayAccessSuffix(ProcessJParser.ArrayAccessSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#arrayAccessSuffix}.
	 * @param ctx the parse tree
	 */
	void exitArrayAccessSuffix(ProcessJParser.ArrayAccessSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#recordAccessSuffix}.
	 * @param ctx the parse tree
	 */
	void enterRecordAccessSuffix(ProcessJParser.RecordAccessSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#recordAccessSuffix}.
	 * @param ctx the parse tree
	 */
	void exitRecordAccessSuffix(ProcessJParser.RecordAccessSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#channelReadSuffix}.
	 * @param ctx the parse tree
	 */
	void enterChannelReadSuffix(ProcessJParser.ChannelReadSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#channelReadSuffix}.
	 * @param ctx the parse tree
	 */
	void exitChannelReadSuffix(ProcessJParser.ChannelReadSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#channelWriteSuffix}.
	 * @param ctx the parse tree
	 */
	void enterChannelWriteSuffix(ProcessJParser.ChannelWriteSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#channelWriteSuffix}.
	 * @param ctx the parse tree
	 */
	void exitChannelWriteSuffix(ProcessJParser.ChannelWriteSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#invocationSuffix}.
	 * @param ctx the parse tree
	 */
	void enterInvocationSuffix(ProcessJParser.InvocationSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#invocationSuffix}.
	 * @param ctx the parse tree
	 */
	void exitInvocationSuffix(ProcessJParser.InvocationSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#arguments}.
	 * @param ctx the parse tree
	 */
	void enterArguments(ProcessJParser.ArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#arguments}.
	 * @param ctx the parse tree
	 */
	void exitArguments(ProcessJParser.ArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#newArrayExpression}.
	 * @param ctx the parse tree
	 */
	void enterNewArrayExpression(ProcessJParser.NewArrayExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#newArrayExpression}.
	 * @param ctx the parse tree
	 */
	void exitNewArrayExpression(ProcessJParser.NewArrayExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#dimExpression}.
	 * @param ctx the parse tree
	 */
	void enterDimExpression(ProcessJParser.DimExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#dimExpression}.
	 * @param ctx the parse tree
	 */
	void exitDimExpression(ProcessJParser.DimExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#dims}.
	 * @param ctx the parse tree
	 */
	void enterDims(ProcessJParser.DimsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#dims}.
	 * @param ctx the parse tree
	 */
	void exitDims(ProcessJParser.DimsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#newRecordExpression}.
	 * @param ctx the parse tree
	 */
	void enterNewRecordExpression(ProcessJParser.NewRecordExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#newRecordExpression}.
	 * @param ctx the parse tree
	 */
	void exitNewRecordExpression(ProcessJParser.NewRecordExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#newRecordExpressionArguments}.
	 * @param ctx the parse tree
	 */
	void enterNewRecordExpressionArguments(ProcessJParser.NewRecordExpressionArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#newRecordExpressionArguments}.
	 * @param ctx the parse tree
	 */
	void exitNewRecordExpressionArguments(ProcessJParser.NewRecordExpressionArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#newProtocolExpression}.
	 * @param ctx the parse tree
	 */
	void enterNewProtocolExpression(ProcessJParser.NewProtocolExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#newProtocolExpression}.
	 * @param ctx the parse tree
	 */
	void exitNewProtocolExpression(ProcessJParser.NewProtocolExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#newMobileExpression}.
	 * @param ctx the parse tree
	 */
	void enterNewMobileExpression(ProcessJParser.NewMobileExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#newMobileExpression}.
	 * @param ctx the parse tree
	 */
	void exitNewMobileExpression(ProcessJParser.NewMobileExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(ProcessJParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(ProcessJParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcessJParser#assignmentOperator}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentOperator(ProcessJParser.AssignmentOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcessJParser#assignmentOperator}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentOperator(ProcessJParser.AssignmentOperatorContext ctx);
}