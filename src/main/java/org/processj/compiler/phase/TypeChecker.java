
package org.processj.compiler.phase;

import java.util.*;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.access.ArrayAccessExpression;
import org.processj.compiler.ast.expression.access.RecordAccessExpression;
import org.processj.compiler.ast.expression.binary.AssignmentExpression;
import org.processj.compiler.ast.expression.binary.BinaryExpression;
import org.processj.compiler.ast.expression.constructing.NewArrayExpression;
import org.processj.compiler.ast.expression.literal.ArrayLiteralExpression;
import org.processj.compiler.ast.expression.literal.ProtocolLiteralExpression;
import org.processj.compiler.ast.expression.literal.RecordLiteralExpression;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.expression.result.*;
import org.processj.compiler.ast.expression.unary.*;
import org.processj.compiler.ast.expression.yielding.ChannelEndExpression;
import org.processj.compiler.ast.expression.yielding.ChannelReadExpression;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.expression.*;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.declarative.*;
import org.processj.compiler.ast.type.ProcedureType;
import org.processj.compiler.ast.type.ProtocolType;
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.ast.type.*;
import org.processj.compiler.ast.type.primitive.*;
import org.processj.compiler.ast.type.primitive.numeric.NumericType;
import org.processj.compiler.ast.type.primitive.numeric.integral.*;
import org.processj.compiler.utilities.PJBugManager;
import org.processj.compiler.utilities.PJMessage;
import org.processj.compiler.utilities.VisitorMessageNumber;

/**
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public class TypeChecker extends Phase {

    // Contains the protocol name and the corresponding tags currently switched on.
    Hashtable<String, ProtocolType.Case> protocolTagsSwitchedOn = new Hashtable<String, ProtocolType.Case>();

    // Set of nested protocols in nested switch statements
    HashSet<String> protocolsSwitchedOn = new HashSet<>();

    public TypeChecker(final Phase.Listener listener) {
        super(listener);
    }

    // ContinueStat - TODO

    /**
     * <p>Verifies that the {@link ConstantDeclaration}'s {@link Type} is assignment compatible with the {@link ConstantDeclaration}'s
     * initialization {@link Expression} if it has one. Otherwise, this passes through.</p>
     *
     * @param constantDeclaration The {@link ConstantDeclaration} to check.
     * @see ConstantDeclaration
     * @see Expression
     * @see Type
     * @since 0.1.0
     */
    @Override
    public final void visitConstantDeclaration(final ConstantDeclaration constantDeclaration) throws Phase.Error {

        org.processj.compiler.Compiler.Info(constantDeclaration + ": Visiting a ConstantDeclaration (" + constantDeclaration + ").");

        // Retrieve the declared Type
        final Type declaredType = constantDeclaration.getType();

        // Assert the Type is not a Procedure Type
        if(declaredType instanceof ProcedureType)
            throw new ConstantDeclaredAsProcedureException(this, constantDeclaration).commit();

        // If we have something to check
        if(constantDeclaration.isInitialized()) {

            // Resolve the initialization Expression
            constantDeclaration.getInitializationExpression().accept(this);

            // Initialize a handle to the initialization Expression's Type
            final Type initializedType = constantDeclaration.getInitializationExpression().getType();

            // Assert the right hand side Type & left hand side Type are assignment compatible
            if(!declaredType.isAssignmentCompatibleTo(initializedType))
                throw new DeclaredTypeNotAssignmentCompatibleException(this, initializedType, declaredType).commit();

        }



    }

    @Override
    public final void visitProcedureTypeDeclaration(final ProcedureType procedureType) throws Phase.Error {

        org.processj.compiler.Compiler.Info(procedureType + ": Visiting a Procedure Type Declaration '"
                + procedureType + "'.");

        // ReturnType should already be bound
        // ParamDeclarations should already be bound
        // Implements list should already be bound

        // TODO: Maybe check implements list?
        procedureType.getBody().accept(this);



    }

    @Override
    public final void visitProtocolTypeDeclaration(final ProtocolType protocolType) throws Phase.Error {

        // TODO: Maybe check extend Types?
        protocolType.getBody().accept(this);



    }

    @Override
    public final void visitRecordTypeDeclaration(final RecordType recordType) throws Phase.Error {

        // TODO: Maybe check extends Types?
        recordType.getBody().accept(this);



    }

    /**
     * <p>Verifies that the {@link LocalDeclaration}'s {@link Type} is assignment compatible with the {@link LocalDeclaration}'s
     * initialization {@link Expression} if it has one. Otherwise, this passes through.</p>
     *
     * @param localDeclaration The {@link LocalDeclaration} to check.
     * @see LocalDeclaration
     * @see Expression
     * @see Type
     * @since 0.1.0
     */
    @Override
    public final void visitLocalDeclaration(final LocalDeclaration localDeclaration) throws Phase.Error {

        org.processj.compiler.Compiler.Info(localDeclaration + ": Visiting a LocalDecl (" + localDeclaration + ").");

        // If we have something to check
        if(localDeclaration.isInitialized()) {

            // Visit the initialization expression first
            localDeclaration.getInitializationExpression().accept(this);

            // Initialize a handle to each Type
            final Type declaredType     = localDeclaration.getType();
            final Type initializedType  = localDeclaration.getInitializationExpression().getType();

            if(!declaredType.isAssignmentCompatibleTo(initializedType))
                throw new DeclaredTypeNotAssignmentCompatibleException(this, initializedType, declaredType).commit();

        }



    }

    /**
     * <p>Verifies that the {@link AltStatement.Case}'s precondition resolves to a boolean {@link Type}, if the
     * {@link AltStatement.Case.Guard} is an input {@link AltStatement.Case.Guard} that it resolves correctly
     * for both sides of the {@link AssignmentExpression} {@link Expression} or if it's a {@link TimeoutStatement} that the timer
     * {@link Expression} is a Timer type and the delay {@link Expression} resolves to an integral {@link Type};
     * lastly it recurs down on the contained {@link Statement}.</p>
     *
     * @param aCase The {@link AltStatement.Case} to check.
     * @see AltStatement.Case
     * @see AltStatement.Case.Guard
     * @see Expression
     * @see Type
     * @since 0.1.0
     */
    @Override
    public final void visitAltStatementCase(final AltStatement.Case aCase) throws Phase.Error {

        // Alt Case -> Syntax: (Expr) && Guard : Statement => Boolean?(T(expr))
        // Three possibilities for this: Assignment, TimeoutStatement, & Skip Statement
        // Expr must be Boolean and Guard and Statement must be visited

        // If the AltCase defines a Boolean Precondition
        if(aCase.definesPrecondition()) {

            // Retrieve a handle to the Precondition
            final Expression precondition = aCase.getPreconditionExpression();

            // Resolve the precondition
            precondition.accept(this);

            // If the precondition's Type is not Boolean
            if(!(precondition.getType() instanceof BooleanType))
                throw new AltCasePreconditionNotBoundToBooleanType(this, aCase);

        }

        // It's possible we may have a null guard;
        // TODO: Verify no null guards reach this point
        if(aCase.definesGuard()) aCase.getGuardStatement().accept(this);

        // Lastly, recur on the Statement
        aCase.accept(this);



    }

    @Override
    public final void visitChannelWriteStatement(final ChannelWriteStatement channelWriteStatement) throws Phase.Error {

        // TODO: A Channel Write Statement looks like an Invocation. Shouldn't
        // TODO: Resolve the ChannelEndType here instead of the Parse Phase?

        // Some sort of Name Expression should resolve to a Channel End Type either from
        // a primary expression or direct expression
        // Initialize a handle to the target & write Expressions
        final Expression targetExpression   = channelWriteStatement.getTargetExpression()  ;
        final Expression writeExpression    = channelWriteStatement.getWriteExpression()   ;

        // Bind the Target Expression first
        targetExpression.accept(this);

        // Assert that the Type bound to the Expression is a Channel End Type
        if(!(targetExpression.getType() instanceof ChannelEndType))
            throw new WriteToNonChannelEndTypeException(this, channelWriteStatement).commit();

        // Visit the expression being written.
        channelWriteStatement.getWriteExpression().accept(this);

        // Assert the target Expression is Type Equal
        TypeAssert.TargetExpressionTypeEqual(channelWriteStatement);

    }

    @Override
    public final void visitDoStatement(final DoStatement doStatement) throws Phase.Error {

        org.processj.compiler.Compiler.Info(doStatement + ": Visiting a do statement");

        // Resolve the evaluation Expression
        doStatement.getEvaluationExpression().accept(this);

        // Initialize a handle to the do statement's evaluation Expression's Type
        final Type type = doStatement.getEvaluationExpression().getType();

        // Assert the evaluation Expression is bound to a boolean Type
        if(!(type instanceof BooleanType))
            throw new ControlEvaluationExpressionNonBooleanTypeException(this, type);

        // Recur on the body
        doStatement.getBody().accept(this);



    }

    @Override
    public final void visitIfStatement(final IfStatement ifStatement) throws Phase.Error {

        org.processj.compiler.Compiler.Info(ifStatement + ": Visiting a if statement");

        // Resolve the evaluation Expression
        ifStatement.getEvaluationExpression().accept(this);

        // Initialize a handle to the do statement's evaluation Expression's Type
        final Type type = ifStatement.getEvaluationExpression().getType();

        // Assert that it's a boolean Type
        if(!(ifStatement.getEvaluationExpression().getType() instanceof BooleanType))
            throw new ControlEvaluationExpressionNonBooleanTypeException(this, type).commit();

        // Resolve the then block
        //ifStatement.getThenBody().accept(this);

        // Resolve the else block
        ifStatement.getElseStatement().accept(this);



    }

    @Override
    public final void visitWhileStatement(final WhileStatement whileStatement) throws Phase.Error {

        // Resolve the evaluation Expression
        whileStatement.getEvaluationExpression().accept(this);

        // Initialize a handle to the evaluation expression's Type
        final Type type = whileStatement.getEvaluationExpression().getType();

        // Assert the evaluation Expression is bound to a boolean Type
        if(!(type instanceof BooleanType))
            throw new ControlEvaluationExpressionNonBooleanTypeException(this, type).commit();

        // Recur on the body
        whileStatement.getBody().accept(this);

    }

    @Override
    public final void visitForStatement(final ForStatement forStatement) throws Phase.Error {

        org.processj.compiler.Compiler.Info(forStatement + ": Visiting a for statement");

        // A non-par for loop cannot enroll on anything.
        if(!forStatement.getBarrierSet().isEmpty() || forStatement.isParallel())
            throw new ProcessEnrolledOnBarriersException(this, forStatement).commit();

        // Check that all the barrier expressions are of barrier type.
        //for(final Expression barrierExpression: forStatement.getBarrierSet()) {

            // Resolve the Barrier Expression
            //barrierExpression.accept(this);

            // Retrieve the Type
            //final Type type = barrierExpression.getType();

            // Assert it's a barrier Type
            //if(!(barrierExpression.getType() instanceof BarrierType))
                //throw new ExpectedBarrierTypeException(this, type).commit();

        //}

        // Resolve the initialization Expression, if any.
        if(forStatement.definesInitializationStatements())
            forStatement.getInitializationStatements().accept(this);

        if(forStatement.definesIncrementStatements())
            forStatement.getIncrementStatements().accept(this);

        if(forStatement.definesEvaluationExpression()) {

            // Resolve the evaluation Expression
            forStatement.getEvaluationExpression().accept(this);

            // Retrieve the Type
            final Type type = forStatement.getEvaluationExpression().getType();

            // Assert the Expression is bound to a boolean Type
            if(!(type instanceof BooleanType))
                throw new ControlEvaluationExpressionNonBooleanTypeException(this, type).commit();

        }

        // Recur on the statement
        forStatement.getBody().accept(this);



    }

    @Override
    public final void visitReturnStatement(final ReturnStatement returnStatement) throws Phase.Error {

        org.processj.compiler.Compiler.Info(returnStatement + ": visiting a return statement");

        // Initialize a handle to the current Context
        final Context context = this.getContext();

        // Assert the Context is a Procedure Type
        if(!(context instanceof ProcedureType))
            throw new InvalidReturnStatementContextException(this, context).commit();

        // Otherwise, initialize a handle to its return Type
        final Type returnType = ((ProcedureType) context).getReturnType();

        // Assert that the return Type is void and the return statement doesn't define an Expression
        if(returnType instanceof VoidType && (returnStatement.getExpression() != null))
            throw new VoidProcedureReturnTypeException(this, context);

        // Assert that the return Type is not void and the return statement does define an Expression
        if(!(returnType instanceof VoidType) && returnStatement.getExpression() == null)
            throw new ProcedureReturnsVoidException(this, returnType).commit();

        // Resolve the Return Type's Expression
        returnStatement.getExpression().accept(this);

        // Initialize a handle to the Expression's Type
        final Type type = returnStatement.getExpression().getType();

        // Assert the Return Type is assignment Compatible with the Return Statement's
        if(!returnType.isAssignmentCompatibleTo(type))
            throw new IncompatibleReturnTypeException(this, returnType, type).commit();



    }

    @Override
    public final void visitBreakStatement(final BreakStatement breakStatement) throws Phase.Error {

        // Assert the Break Statement is in a Switch Group
        this.getContext().forEachContextUntil(context -> {

            // Initialize the result
            final boolean inSwitchGroup = (context instanceof SwitchStatement.Group);

            // Mark the Switch group
            //if(inSwitchGroup) ((SwitchStatement.Group) context).setContainsBreakStatement(true);

            // Return the result
            return inSwitchGroup;

        });



    }

    @Override
    public final void visitSwitchStatementGroup(final SwitchStatement.Group switchGroup) throws Phase.Error {

        org.processj.compiler.Compiler.Info(switchGroup + ": Visiting SwitchGroup (" + switchGroup + ").");

        // Resolve the labels first to mark ourselves
        switchGroup.getCases().accept(this);

        // Now the statements
        switchGroup.getBody().accept(this);

        // Initialize a handle to the Context
        final Context context = this.getContext();

        // Assert the Context is a SwitchStatement
        if(!(context instanceof SwitchStatement))
            throw new InvalidSwitchLabelContextException(this, context);

        // Initialize a handle to the evaluation Expression's Type
        final Type type = ((SwitchStatement) context).getEvaluationExpression().getType();

        // If the evaluation Expression's Type is a Protocol Type so we can remove ourselves
        if(type instanceof ProtocolType) {

            // Initialize a handle to the evaluation Expression's name
            final String name = ((SwitchStatement) context).getEvaluationExpression().toString();

            // Assert the SwitchGroup doesn't contain more than one label
            if(switchGroup.getCases().size() > 1)
                PJBugManager.INSTANCE.reportMessage(
                        new PJMessage.Builder()
                                .addAST(switchGroup)
                                .addError(VisitorMessageNumber.REWRITE_1004)
                                .addArguments(name)
                                .build());

            // Assert the SwitchGroup contains a break statement
            // TODO: Assert this with a nested Context within visitBreakSTat
            //else if(!switchGroup.containsBreakStatement())
            //    PJBugManager.INSTANCE.reportMessage(
            //            new PJMessage.Builder()
            //                    .addAST(switchGroup)
            //                    .addError(VisitorMessageNumber.REWRITE_1005)
            //                    .addArguments(name)
            //                    .build());

            this.protocolTagsSwitchedOn.remove(((ProtocolType) type).toString());

        }



    }

    /**
     * <p>Asserts that the {@link SwitchStatement.Group.Case} {@link org.processj.compiler.ast.expression.Expression}'s {@link Type}
     * is declared constant or is a {@link ProcedureType}'s tag.</p>
     *
     * @param aCase The {@link SwitchStatement.Group.Case} to assert.
     * @throws Phase.Error If the {@link SwitchStatement.Group.Case}'s is not constant or a {@link ProcedureType}'s tag.
     * @since 0.1.0
     */
    @Override
    public final void visitSwitchLabelExpression(final SwitchStatement.Group.Case aCase) throws Phase.Error {

        // TODO: We migrated NameChecker Error 421 here, but we're already checking for that in SwitchStat
        org.processj.compiler.Compiler.Info(aCase + ": Visiting SwitchLabel (" + aCase.getExpression() + ").");

        // Initialize a handle to the Context
        final Context context = this.getContext();

        // Assert the Context is a SwitchStatement
        if(!(context instanceof SwitchStatement))
            throw new InvalidSwitchLabelContextException(this, context);

        // We only check on non-default labels
        if(!aCase.isDefault()) {

            // Initialize a handle to the Evaluation Expression & its Type
            final Expression evaluationExpression   = ((SwitchStatement) context).getEvaluationExpression();
            final Type       type                   = evaluationExpression.getType();

            // If the Evaluation Expression is bound to an integral or String Type
            if(type instanceof IntegralType || type instanceof StringType) {

                // Resolve the Switch Label's Expression
                aCase.getExpression().accept(this);

                // Initialize a handle to the Type
                final Type switchLabelType = aCase.getExpression().getType();

                // Assert the constant Expression's Type is assignable to the Evaluation Expression's Type
                if(!(type.isAssignmentCompatibleTo(switchLabelType)))
                    throw new InvalidSwitchLabelExpressionTypeException(
                            this, evaluationExpression, switchLabelType, type).commit();

            // Otherwise, the Evaluation Expression should be a Protocol Type
            } else if(aCase.getExpression() instanceof NameExpression) {

                // Initialize a handle to the Tag
                // TODO: We should probably use properly qualified names here - what if there are two different protocols named the same ?
                final String            tag                     = aCase.getExpression().toString();
                final ProtocolType protocolType = (ProtocolType) type;
                final ProtocolType.Case protocolCase            = protocolType.getCase(tag);

                // Assert a valid Protocol Case
                if(protocolCase == null)
                    throw new UndefinedTagException(this, tag, protocolType).commit();

                // Aggregate to the switched protocol tags
                this.protocolTagsSwitchedOn.put(protocolType.toString(), protocolCase);

            // TODO: This is most likely dead code since Protocol Types for evaluation Expressions are checked above.
            // TODO: Test for this to make sure we don't or do arrive here
            } else throw
                    new SwitchLabelExpressionNotProtocolTagException(this, aCase.getExpression()).commit();

        }

        // Assert the SwitchLabel Expression is constant or a Protocol Tag
        TypeAssert.SwitchLabelConstantOrProtocolType(this, aCase);



    }

    @Override
    public final void visitSwitchStatement(final SwitchStatement switchStatement) throws Phase.Error {

        // Resolve the evaluation Expression
        switchStatement.getEvaluationExpression().accept(this);

        // Initialize a handle to the evaluation Expression's Type
        final Type type = switchStatement.getEvaluationExpression().getType();

        // Assert the evaluation Expression's bound Type is an integral, string, or Protocol Type (Switch on Tag)
        // TODO: This checks for Error 421
        if(!(type instanceof ProtocolType || type instanceof IntegralType || type instanceof StringType))
            throw new InvalidSwitchStatementExpressionTypeException(this, type).commit();

        // Protocol Type; Check for illegally nested Switch Statements
        // TODO: Should we do this in with SymbolMap.Context instead?
        if(type instanceof ProtocolType) {

            // Assert we haven't already switched on the current Protocol
            if(this.protocolsSwitchedOn.contains(type.toString()))
                throw new IllegalNestedSwitchInProtocolException(this, (ProtocolType) type).commit();

            // Aggregate the Protocol Name
            this.protocolsSwitchedOn.add(type.toString());

        }

        // Resolve the children
        switchStatement.getBody().accept(this);

        // Remove the Protocol Type, if any
        if(this.protocolsSwitchedOn.contains(type.toString())) this.protocolsSwitchedOn.remove(type.toString());



    }

    @Override
    public final void visitSuspendStatement(final SuspendStatement suspendStatement) throws Phase.Error {

        org.processj.compiler.Compiler.Info(suspendStatement + ": Visiting a suspend statement.");

        // Initialize a handle to the current Context
        final Context context = this.getContext();

        // Assert the Context is a Procedure
        if(!(context instanceof ProcedureType))
            throw new InvalidSuspendStatementContextException(this, context).commit();

        if(!((ProcedureType) context).isMobile())
            throw new SuspendInNonMobileProcedureException(this, context).commit();



    }

    @Override
    public final void visitSyncStatement(final SyncStatement syncStatement) throws Phase.Error {

        // Resolve the barrier expression
        syncStatement.getBarrierExpression().accept(this);

        // Initialize a handle to the barrier Expression's Type
        final Type type = syncStatement.getBarrierExpression().getType();

        // Assert the Expression is bound to a Barrier Type
        if(!(type instanceof BarrierType))
            throw new InvalidSynchronizationExpressionTypeException(this, type).commit();

    }

    /**
     * <p>Verifies that the {@link TimeoutStatement}'s timer {@link Expression} is a primitive timer {@link Type} & that
     * the delay {@link Expression} is an integral {@link Type}.</p>
     *
     * @param timeoutStatement The {@link TimeoutStatement} to verify.
     * @since 0.1.0
     */
    @Override
    public final void visitTimeoutStatement(final TimeoutStatement timeoutStatement) throws Phase.Error {

        org.processj.compiler.Compiler.Info(timeoutStatement + ": visiting a timeout statement.");

        // Retrieve a handle to the timer & delay expressions
        final Expression timerExpression = timeoutStatement.getTimerExpression();
        final Expression delayExpression = timeoutStatement.getDelayExpression();

        // Resolve the timer Expression first
        timerExpression.accept(this);

        // Initialize a handle to the timer & delay Types
        final Type timerType = timerExpression.getType();
        final Type delayType = timerExpression.getType();

        // Assert that the timer Expression is a Timer Type
        if(!(timerType instanceof TimerType))
            throw new InvalidTimeoutTargetTypeException(this, timerType).commit();

        // Resolve the delay Expression
        delayExpression.accept(this);

        // Assert that the delay Expression is an integral type
        if(!(delayType instanceof IntegralType))
            throw new InvalidTimeoutDelayTypeException(this, delayType).commit();

    }

    @Override
    public final void visitInvocationExpression(final InvocationExpression invocationExpression) throws Phase.Error {

        org.processj.compiler.Compiler.Info(invocationExpression + ": visiting invocation (" + invocationExpression + ")");

        // Invocation Parameter Types should be bound already, attempt to retrieve the aggregated results
        final List<Object>          candidates = this.getScope().get(invocationExpression.getProcedureName());
        final List<ProcedureType>    compatible = new ArrayList<>();

        // Aggregate all valid results
        candidates.forEach(result -> { if(result instanceof Context.SymbolMap)
            { aggregateAssignmentCompatible((Context.SymbolMap) result, invocationExpression, compatible); }});

        FancyPrint(invocationExpression, compatible, 1, 1);

        // Assert we have at least one candidate
        if(compatible.size() == 0)
            throw new NoCandidateForInvocationFoundException(this, invocationExpression).commit();

        // Attempt to resolve a Candidate
        final ProcedureType candidate = Candidate(compatible);

        // Assert the candidate was resolved
        if(candidate == null)
            throw new AmbiguousInvocationException(this, invocationExpression).commit();

        // Bind the Type
        invocationExpression.setType(candidate.getReturnType());

        org.processj.compiler.Compiler.Info(invocationExpression + ": invocation has type: " + invocationExpression.getType());



    }

    @Override
    public final void visitNewArrayExpression(final NewArrayExpression newArrayExpression) throws Phase.Error {

        org.processj.compiler.Compiler.Info(newArrayExpression + ": Visiting a NewArray " + newArrayExpression.getBracketExpressions().size() + " " + newArrayExpression.dims().size());

        // Resolve the bracket Expressions
        newArrayExpression.getBracketExpressions().accept(this);

        // Assert each bracket Expression is an integral Type
        newArrayExpression.forEachBracketExpression(expression -> {

            // Assert the Expression is bound to an integral Type
            if(!(expression.getType() instanceof IntegralType))
                throw new InvalidArrayDimensionTypeException(this, expression);

        });

        // Initialize a handle to the depth & the synthesized ArrayType
        final int       depth     = newArrayExpression.getDepth();
        final ArrayType arrayType = new ArrayType(newArrayExpression.getComponentType(), depth);

        // If the New Array Expression is defined with an initializer with a depth greater
        // than 0; no error otherwise since any array can hold an empty array.
        if(newArrayExpression.definesLiteralExpression() && (newArrayExpression.getInitializationExpression().getDepth() > 0)) {

            // Initialize a handle to the ArrayLiteral Expression & the Synthesized Array Type
            final ArrayLiteralExpression arrayLiteralExpression = newArrayExpression.getInitializationExpression();

            // Iterate through each Expression contained in the ArrayLiteral Expression
            for(final Expression expression: arrayLiteralExpression.getExpressions()) {

                // Resolve the Expression
                expression.accept(this);

                // Initialize a handle to the Expression's Type
                final Type type = expression.getType();

                // Assert an ArrayType is bound to the Expression
                if(!(type instanceof ArrayType))
                    throw new TypeNotAssignmentCompatibleException(this, expression, arrayType);

                // Assert assignment compatibility
                if(!arrayType.isAssignmentCompatibleTo(type))
                    throw new TypeNotAssignmentCompatibleException(this, expression, arrayType);

            }

            // Bind the ArrayLiteral's Type
            arrayLiteralExpression.setType(arrayType);

        }

        // Bind the ArrayType
        newArrayExpression.setType(arrayType);

        org.processj.compiler.Compiler.Info(newArrayExpression + ": NewArray type is " + newArrayExpression.getType());



    }

    @Override
    public final void visitTernaryExpression(TernaryExpression ternaryExpression) throws Phase.Error {
        // e ? t : f
        // Primitive?(Type(t)) & Primitive?(Type(f)) & (Type(t) :=T Type(f) || Type(f)
        // :=T Type(t)) =>
        // Type(e ? t : f) = ceiling(Type(t), Type(f))
        org.processj.compiler.Compiler.Info(ternaryExpression + ": Visiting a ternary expression");

        // Resolve the Expression's Type
        ternaryExpression.getEvaluationExpression().accept(this);

        // Initialize a handle to the Expression's Type
        final Type expressionType = ternaryExpression.getEvaluationExpression().getType();

        // Assert that the Expression is bound to a boolean Type
        // Error 3070
        if(!(expressionType instanceof BooleanType))
            throw new ControlEvaluationExpressionNonBooleanTypeException(this, expressionType).commit();

        // Resolve the then & else branches
        ternaryExpression.getThenExpression().accept(this);
        ternaryExpression.getElseExpression().accept(this);

        // Initialize a handle to the then & else part
        final Type thenType  = ternaryExpression.getThenExpression().getType();
        final Type elseType  = ternaryExpression.getElseExpression().getType();

        // TODO: Watch out for ArrayType, ProtocolType, & RecordTypes
        // TODO: Error 3071 & 3072
        // Assert the then part is assignment compatible with the else part
        if(!thenType.isAssignmentCompatibleTo(elseType)) {

            // Assert the else part is assignment compatible with the then part
            if(!elseType.isAssignmentCompatibleTo(thenType))
                throw new TypeNotAssignmentCompatibleException(this, ternaryExpression.getThenExpression(), elseType).commit();

            // Bind the Type
            ternaryExpression.setType(elseType);

        // Assert the else part is assignment compatible with the then part
        } else {

            // Assert the then part is assignment compatible with the else part
            if(!thenType.isAssignmentCompatibleTo(elseType))
                throw new TypeNotAssignmentCompatibleException(this, ternaryExpression.getElseExpression(), thenType).commit();

            // Bind the Type
            ternaryExpression.setType(thenType);

        }

        org.processj.compiler.Compiler.Info(ternaryExpression + ": Ternary has type: " + ternaryExpression.type);



    }

    @Override
    public final void visitAssignmentExpression(final AssignmentExpression assignmentExpression) throws Phase.Error {
        // Assignment
        //
        // Syntax: Name <op> Expr [ shoreted to v <op> e below ]
        // where op is one of =, +=, -=, *=, /=, %=, <<=, >>=, &=, |=, ^=
        //
        // = : (v := e)
        //
        // T(v) :=T T(e)
        // T(v = e) := T(v)
        // (Some variables are not assignable: channels, barriers, more ??)
        // TODO: RECORDS and PROTOCOLS
        //
        // +=, -=, *=, /=, %= : ( v ?= e) [ ? is one of {+,-,*,/,%} ]
        //
        // (op = + /\ String?(T(v)) /\
        // (Numeric?(T(e)) \/ Boolean?(T(e)) \/ String?(T(e) \/
        // Char?(T(e)))) \/
        // (op != + /\ (T(v) :=T T(e)))
        //
        //
        org.processj.compiler.Compiler.Info(assignmentExpression + ": Visiting an assignment");

        // Resolve both sides
        assignmentExpression.getLeftExpression().accept(this);
        assignmentExpression.getRightExpression().accept(this);

        // Initialize a handle to each side's Type
        final Type leftType     = assignmentExpression.getLeftExpression().getType();
        final Type rightType    = assignmentExpression.getRightExpression().getType();

        // Assert the left hand side is assignable
        // TODO: Error 630
        if(leftType.toString().equals("null") || leftType.toString().equals("void"))
            throw new TypeNotAssignmentCompatibleException(this, assignmentExpression.getLeftExpression(), rightType).commit();

        // Resolve the Operators
        switch(assignmentExpression.getOperator()) {

            case AssignmentExpression.EQ: {

                if(!leftType.isAssignmentCompatibleTo(rightType))
                    throw new TypeNotAssignmentCompatibleException(this, assignmentExpression.getLeftExpression(), rightType);

            } break;

            case AssignmentExpression.PLUSEQ: {

                if(leftType instanceof StringType
                        && !(rightType instanceof NumericType)
                        && !(rightType instanceof BooleanType)
                        && !(rightType instanceof CharType)
                        && !(rightType instanceof StringType))
                    throw new TypeNotAssignmentCompatibleException(this, assignmentExpression.getLeftExpression(), rightType).commit();

                break;

            }
            case AssignmentExpression.MULTEQ:
            case AssignmentExpression.DIVEQ:
            case AssignmentExpression.MODEQ:
            case AssignmentExpression.MINUSEQ: {

                if(!leftType.isAssignmentCompatibleTo(rightType))
                    throw new TypeNotAssignmentCompatibleException(this, assignmentExpression.getLeftExpression(), rightType).commit();

            } break;

            case AssignmentExpression.LSHIFTEQ:
            case AssignmentExpression.RSHIFTEQ:
            case AssignmentExpression.RRSHIFTEQ: {

                if(!(leftType instanceof IntegralType))
                    throw new LeftSideOfShiftNotIntegralOrBoolean(this, leftType);

                if(!(rightType instanceof IntegralType))
                    throw new RightSideOfShiftNotIntegralOrBoolean(this, rightType);

            } break;

            case AssignmentExpression.ANDEQ:
            case AssignmentExpression.OREQ:
            case AssignmentExpression.XOREQ: {

                // TODO: Error 3009
                if(!(leftType instanceof IntegralType && rightType instanceof IntegralType)
                        || (leftType instanceof BooleanType && rightType instanceof BooleanType))
                    throw new CompoundBitwiseTypesNotIntegralOrBoolean(this, leftType, rightType).commit();

            } break;

        }

        // Bind the Assignment Expression's Type to the Left hand side's Type
        assignmentExpression.setType(leftType);

        org.processj.compiler.Compiler.Info(assignmentExpression + ": Assignment has type: " + assignmentExpression.getType());



    }

    @Override
    public final void visitBinaryExpression(final BinaryExpression binaryExpression) throws Phase.Error {
        org.processj.compiler.Compiler.Info(binaryExpression + ": Visiting a Binary Expression");

        // Resolve the left & right hand sides
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);

        final Type leftType   = binaryExpression.getLeftExpression().getType();
        final Type rightType  = binaryExpression.getRightExpression().getType();

        final Type resultType;

        switch (binaryExpression.op()) {

            case BinaryExpression.LT:
            case BinaryExpression.GT:
            case BinaryExpression.LTEQ:
            case BinaryExpression.GTEQ: {

                if(!(leftType instanceof NumericType) && !(rightType instanceof NumericType))
                    throw new RelationalOperatorRequiresNumericTypeException(this, leftType, rightType).commit();

                // Update the result type
                resultType = new BooleanType();

            } break;

            case BinaryExpression.EQEQ:
            case BinaryExpression.NOTEQ: {

                // TODO: barriers, timers, procs, records and protocols
                // Assert the Types are equal or any combination of numeric Types
                if(!((leftType.isTypeEqualTo(rightType)) || (leftType instanceof NumericType && rightType instanceof NumericType)))
                    throw (leftType instanceof VoidType || rightType instanceof VoidType)
                            ? new VoidTypeUsedInLogicalComparisonException(this, leftType, rightType).commit()
                            : new LogicalComparisonTypeMismatchException(this, leftType, rightType).commit();

                // Update the result Type
                resultType = new BooleanType();

            } break;

            case BinaryExpression.ANDAND:
            case BinaryExpression.OROR: {

                if(!(leftType instanceof BooleanType) || rightType instanceof BooleanType)
                    throw new LogicalComparisonNotBooleanTypeException(this, leftType, rightType).commit();

                // Update the result Type
                resultType = new BooleanType();

            } break;

            case BinaryExpression.AND:
            case BinaryExpression.OR:
            case BinaryExpression.XOR: {

                if(leftType instanceof BooleanType && rightType instanceof BooleanType)
                    resultType = new BooleanType();

                else if(leftType instanceof IntegralType && rightType instanceof IntegralType)
                    resultType = leftType;

                else
                    throw new BinaryBitwiseTypesNotIntegralOrBoolean(this, leftType, rightType).commit();

            } break;

            // + - * / % : Type must be numeric
            case BinaryExpression.PLUS: {

                if(leftType instanceof StringType
                    && !(rightType instanceof NumericType)
                    && !(rightType instanceof BooleanType)
                    && !(rightType instanceof StringType)
                    && !(rightType instanceof CharType))
                    throw new TypeNotAssignableException(this, binaryExpression.getLeftExpression(), rightType).commit();

                else if(rightType instanceof NumericType
                    && !(leftType instanceof BooleanType)
                    && !(leftType instanceof StringType)
                    && !(leftType instanceof CharType))
                    throw new TypeNotAssignableException(this, binaryExpression.getRightExpression(), leftType).commit();

            }
            case BinaryExpression.MINUS:
            case BinaryExpression.MULT:
            case BinaryExpression.DIV:
            case BinaryExpression.MOD: {

                if(!(leftType instanceof NumericType) || !(rightType instanceof NumericType))
                    throw new ArithmeticOperatorRequiresNumericTypeException(this, leftType, rightType).commit();

                // Update the result Type
                resultType = leftType;

            } break;

            case BinaryExpression.LSHIFT:
            case BinaryExpression.RSHIFT:
            case BinaryExpression.RRSHIFT: {

                if(!(leftType instanceof IntegralType))
                    throw new LeftSideOfShiftNotIntegralOrBoolean(this, leftType);

                if(!(rightType instanceof IntegralType))
                    throw new RightSideOfShiftNotIntegralOrBoolean(this, rightType);

                // Update the result type
                resultType = leftType;

            } break;

            default: resultType = new ErrorType();

        }

        if(!(resultType instanceof BooleanType)) {

            // Retrieve the Type ceiling
            Type ceiling = rightType;

            // Promote if necessary
            if(((PrimitiveType) leftType).isTypeCeilingOf(rightType))
                ceiling = (leftType instanceof LongType) ? new LongType() : new IntegerType();

            // Bind the Type
            binaryExpression.setType(ceiling);

        } else binaryExpression.setType(resultType);

        org.processj.compiler.Compiler.Info(binaryExpression + ": Binary Expression has type: " + binaryExpression.getType());



    }

    @Override
    public final void visitUnaryPostExpression(final UnaryPostExpression unaryPostExpression) throws Phase.Error {

        org.processj.compiler.Compiler.Info(unaryPostExpression + ": Visiting a unary post expression");

        // Resolve the Expression
        unaryPostExpression.getExpression().accept(this);

        // Retrieve a handle to the type
        final Type expressionType = unaryPostExpression.getExpression().getType();

        // Assert the expression is bound to a numeric Type
        if(!(expressionType instanceof NumericType))
            throw new InvalidUnaryOperandException(this, unaryPostExpression, unaryPostExpression.opString()).commit();

        // TODO: what about protocol ?? Must be inside the appropriate case.protocol access
        if(!(unaryPostExpression.getExpression() instanceof NameExpression)
                && !(unaryPostExpression.getExpression() instanceof RecordAccessExpression)
                && !(unaryPostExpression.getExpression() instanceof ArrayAccessExpression))
            throw new InvalidLiteralUnaryOperandException(this, unaryPostExpression).commit();

        // Bind the Type
        unaryPostExpression.setType(expressionType);

        org.processj.compiler.Compiler.Info(unaryPostExpression + ": Unary Post Expression has type: " + expressionType);



    }

    @Override
    public final void visitUnaryPreExpression(final UnaryPreExpression unaryPreExpression) throws Phase.Error {

        org.processj.compiler.Compiler.Info(unaryPreExpression + ": Visiting a unary pre expression");

        // Resolve the Expression
        unaryPreExpression.getExpression().accept(this);

        // Initialize a handle to the Expression's Type
        final Type expressionType = unaryPreExpression.getType();

        switch(unaryPreExpression.getOperator()) {

            case UnaryPreExpression.PLUS:
            case UnaryPreExpression.MINUS:

                // TODO: Error 3052
                if(!(expressionType instanceof NumericType))
                    throw new InvalidUnaryOperandException(
                            this, unaryPreExpression, unaryPreExpression.opString()).commit();

                break;

            case UnaryPreExpression.NOT:

                // TODO: Error 3053
                if(!(expressionType instanceof BooleanType))
                    throw new InvalidUnaryOperandException(
                            this, unaryPreExpression, unaryPreExpression.opString()).commit();

                break;

            case UnaryPreExpression.COMP:

                // TODO: Error 3054
                if(!(expressionType instanceof IntegralType))
                    throw new InvalidUnaryOperandException(
                            this, unaryPreExpression, unaryPreExpression.opString()).commit();

                break;

            case UnaryPreExpression.PLUSPLUS:
            case UnaryPreExpression.MINUSMINUS:

                // TODO: protocol access Error 3057
                if(!(unaryPreExpression.getExpression() instanceof NameExpression)
                        && !(unaryPreExpression.getExpression() instanceof RecordAccessExpression)
                        && !(unaryPreExpression.getExpression() instanceof ArrayAccessExpression))
                    throw new InvalidLiteralUnaryOperandException(this, unaryPreExpression).commit();

                // TODO: Error 3057
                if (!(expressionType instanceof NumericType))
                    throw new InvalidUnaryOperandException(this, unaryPreExpression, unaryPreExpression.opString()).commit();

                break;

        }

        // Bind the Type
        unaryPreExpression.setType(expressionType);

        org.processj.compiler.Compiler.Info(unaryPreExpression + ": Unary Pre Expression has type: " + expressionType);



    }

    @Override
    public final void visitChannelEndExpression(final ChannelEndExpression channelEndExpression) throws Phase.Error {

        // Resolve the Channel Type
        channelEndExpression.getChannelExpression().accept(this);

        // Initialize a handle to the Channel Type
        final Type type = channelEndExpression.getType();

        // Expression must be of ChannelType type.
        if(!(type instanceof ChannelType))
            throw new ChannelEndExpressionBoundToNonChannelTypeException(this, channelEndExpression).commit();

        // Retrieve a handle to the Channel Type & its' end
        final ChannelType   channelType = (ChannelType) type;
        final int           end         = (channelEndExpression.isRead()
                ? ChannelEndType.READ_END : ChannelEndType.WRITE_END);

        Type synthesized = new ErrorType();

        // TODO: Redo; make it a little more accessible
        // Channel has no shared ends.
        if(channelType.isShared() == ChannelType.NOT_SHARED)
            synthesized = new ChannelEndType(ChannelEndType.NOT_SHARED, channelType.getComponentType(), end, false, false, false);

        // Channel has both ends shared, create a shared ChannelEndType.
        else if (channelType.isShared() == ChannelType.SHARED_READ_WRITE)
            synthesized = new ChannelEndType(ChannelEndType.SHARED, channelType.getComponentType(), end, false, false, false);

        // Channel has read end shared; if .read then create a shared channel end, otherwise create a non-shared one.
        else if (channelType.isShared() == ChannelType.SHARED_READ) {

            final int share = (channelEndExpression.isRead() && (channelType.isShared() == ChannelType.SHARED_READ))
                    ? ChannelEndType.SHARED : ChannelType.NOT_SHARED;

            // Set the Synthesized Type
            synthesized = new ChannelEndType(share, channelType.getComponentType(), end, false, false, false);

        } else if(channelType.isShared() == ChannelType.SHARED_WRITE) {

            final int share = (channelEndExpression.isWrite() && (channelType.isShared() == ChannelType.SHARED_WRITE))
                    ? ChannelEndType.SHARED : ChannelType.NOT_SHARED;

            // Set the Synthesized Type
            synthesized = new ChannelEndType(share, channelType.getComponentType(), end, false, false, false);

        }

        // Bind the Type
        channelEndExpression.setType(synthesized);

        org.processj.compiler.Compiler.Info(channelEndExpression + ": Channel End Expr has type: " + channelEndExpression.getType());



    }

    @Override
    public final void visitChannelReadExpression(final ChannelReadExpression channelReadExpression) throws Phase.Error {

        org.processj.compiler.Compiler.Info(channelReadExpression + ": Visiting a channel read expression.");

        // Resolve the Channel Type
        channelReadExpression.getTargetExpression().accept(this);

        // Initialize a handle to the Type
        final Type type = channelReadExpression.getType();

        // Assert the ChannelReadExpression is bound to the appropriate Type
        if(!(type instanceof TimerType || type instanceof ChannelEndType || type instanceof ChannelType))
            throw new InvalidChannelReadExpressionTypeException(this, channelReadExpression).commit();

        //if(type instanceof ChannelEndType)
        //    channelReadExpression.setType(type.getComponentType());

        else if(type instanceof ChannelType)
            channelReadExpression.setType(((ChannelType) type).getComponentType());

        else channelReadExpression.setType(new LongType());

        // Assert the ChannelReadExpression defines an extended rendezvous
        if(channelReadExpression.definesExtendedRendezvous()) {

            // Assert that timer reads do not have an extended rendezvous.
            if(type instanceof TimerType)
                throw new TimerReadWithExtendedRendezvous(this, channelReadExpression).commit();

            // Otherwise, resolve the extended rendezvous
            channelReadExpression.getExtendedRendezvous().accept(this);

        }

        org.processj.compiler.Compiler.Info(channelReadExpression + ": Channel read expression has type: " + channelReadExpression.getType());



    }

    @Override
    public final void visitArrayAccessExpression(final ArrayAccessExpression arrayAccessExpression) throws Phase.Error {
        // ArrayAccessExpr
        // Syntax: Expr1[Expr2]
        // Expr1 must be array type and Expr2 my be integer.
        // Array?(T(Expr1)) /\ Integer?(T(Expr2))
        // If T(Expr1) = Array(BaseType) => T(Expr1[Expr2]) := BaseType
        org.processj.compiler.Compiler.Info(arrayAccessExpression + ": Visiting ArrayAccessExpr");

        // Resolve the Target Expression
        arrayAccessExpression.getTargetExpression().accept(this);

        // Initialize a handle to the target Expression's Type
        final Type targetType = arrayAccessExpression.getTargetExpression().getType();

        // Assert the target type is an ArrayType
        if(!(targetType instanceof ArrayType))
            throw new InvalidArrayAccessTypeException(this, targetType).commit();

        // Initialize a handle to the ArrayType
        final ArrayType arrayType = (ArrayType) targetType;

        // Initialize a handle to the component Type
        Type resultantType = arrayType.getComponentType();

        // Update the resultant Type if the depth is greater than 1
        if(arrayType.getDepth() > 1)
            resultantType = new ArrayType(arrayType.getComponentType(), arrayType.getDepth() - 1);

        // Set the Type
        arrayAccessExpression.setType(resultantType);

        org.processj.compiler.Compiler.Info(arrayAccessExpression + ": ArrayAccessExpr has type " + arrayAccessExpression.type);

        // Resolve the index Type
        arrayAccessExpression.getIndexExpression().accept(this);

        // Initialize a handle to the Index Type
        Type indexType = arrayAccessExpression.getIndexExpression().getType();

        // This error does not create an error type cause the baseType() is still the array expression's type.
        // TODO: Error 655
        if(!(indexType instanceof IntegerType))
            throw new InvalidArrayDimensionTypeException(this, arrayAccessExpression).commit();

        org.processj.compiler.Compiler.Info(arrayAccessExpression + ": Array Expression has type: " + arrayAccessExpression.type);



    }

    @Override
    public final void visitRecordAccessExpression(final RecordAccessExpression recordAccessExpression) throws Phase.Error {

        org.processj.compiler.Compiler.Info(recordAccessExpression + ": visiting a record access expression (" + recordAccessExpression.field() + ")");

        // Resolve the Record
        recordAccessExpression.getTarget().accept(this);

        // Initialize a handle to the Type
        final Type type = recordAccessExpression.getType();

        // TODO: size of strings.... size()? size? or length? for now: size()

        if((type instanceof ArrayType) && recordAccessExpression.field().toString().equals("size")) {

            recordAccessExpression.setType(new IntegerType());
            recordAccessExpression.isArraySize = true;

        } else if(type instanceof StringType && recordAccessExpression.field().toString().equals("length")) {

            recordAccessExpression.setType(new LongType());
            recordAccessExpression.isStringLength = true;

        } else {

            // Assert the Type is a Record Type Declaration
            if(type instanceof RecordType) {

                // Find the field and make the type of the record access equal to the field. TODO: test inheritence here
                final RecordType.Member member =
                        ((RecordType) type).getMember(recordAccessExpression.field().toString());

                // TODO: Error 3062
                if(member == null)
                    throw new UndefinedSymbolException(this, recordAccessExpression).commit();

                // Bind the Type
                recordAccessExpression.setType(member.getType());

            // Otherwise, it's Protocol Type
            } else {

                final ProtocolType protocolType = (ProtocolType) type;

                // TODO: Error Format: "Illegal access to non-switched protocol type '" + protocolTypeDeclaration + "'."
                if(!protocolsSwitchedOn.contains(protocolType.toString()))
                    throw new IllegalNestedSwitchInProtocolException(this, protocolType).commit();

                final ProtocolType.Case aCase = protocolTagsSwitchedOn.get(protocolType.toString());
                final String fieldName = recordAccessExpression.field().toString();

                boolean found = false;

                //if(aCase != null)
                    //for(RecordType.Member member : aCase.getBody())
                        //if(member.getName().toString().equals(fieldName)) {

                            //recordAccessExpression.setType(member.getType());
                            //found = true;

                        //}

                // TODO: Error 3073
                // TODO: Format: "Unknown field reference '" + fieldName + "' in protocol tag '"
                //                            + protocolTypeDeclaration + "' in protocol '" + protocolTypeDeclaration + "'."
                if(!found)
                    throw new UndefinedSymbolException(this, recordAccessExpression).commit();

            }

        }

        org.processj.compiler.Compiler.Info(recordAccessExpression + ": record access expression has type: " + recordAccessExpression.type);



    }

    @Override
    public final void visitCastExpression(CastExpression castExpression) throws Phase.Error {

        org.processj.compiler.Compiler.Info(castExpression + ": Visiting a cast expression");

        // Resolve the expression
        castExpression.getExpression().accept(this);

        // Initialize a handle to the expression & cast Type
        final Type expressionType   = castExpression.getExpression().getType();
        final Type castType         = castExpression.getCastType();

        if(expressionType instanceof NumericType && castType instanceof NumericType) {

            castExpression.type = castType;

        } else if (expressionType instanceof ProtocolType && castType instanceof ProtocolType) {

            // TODO: finish this

        } else if (expressionType instanceof RecordType && castType instanceof RecordType) {

            // TODO: finish this

        } else {

            // Turns out that casts like this are illegal:
            // int a[][];
            // double b[][];
            // a = (int[][])b;
            // b = (double[][])a;
            castExpression.type = new ErrorType();
            // !!Error: Illegal cast of value of type exprType to castType.

        }

        org.processj.compiler.Compiler.Info(castExpression + ": Cast Expression has type: " + castExpression.type);



    }

    @Override
    public final void visitNameExpression(final NameExpression nameExpression) throws Phase.Error {

        org.processj.compiler.Compiler.Info(nameExpression + ": Visiting a Name Expression (" + nameExpression + ").");

        // One of the few terminal points, attempt to retrieve the type from our current scope
        final Object resolved = this.getScope().get(nameExpression.getName().toString(), nameExpression.getPackageName());

        // Assert that our result is a Type
        if(!(resolved instanceof Type))
            throw new UndefinedSymbolException(this, nameExpression).commit();

        // Otherwise, bind the Type
        nameExpression.setType((Type) resolved);

        org.processj.compiler.Compiler.Info(nameExpression + ": Name Expression (" + nameExpression + ") has type: " + nameExpression.type);

    }

    @Override
    public final void visitProtocolLiteralExpression(ProtocolLiteralExpression protocolLiteralExpression) throws Phase.Error {

        // TODO: be careful here if a protocol type extends another protocol type, then the
        // record literal must contains expressions for that part too!!!



    }

    @Override
    public final void visitRecordLiteralExpression(RecordLiteralExpression recordLiteralExpression) throws Phase.Error {

        // TODO: be careful here if a record type extends another record type, then the
        // record literal must contains expressions for that part too!!!



    }

    /// ------
    /// Errors

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an undefined Symbol.
     * // TODO: Error 3029
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class UndefinedSymbolException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Constant '%s' declared as procedure type.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the undefined symbol.</p>
         */
        private final Expression expression;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.UndefinedSymbolException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected UndefinedSymbolException(final TypeChecker culpritInstance,
                                           final Expression expression) {
            super(culpritInstance);
            this.expression = expression;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.expression);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a Constant Declaration specified
     * as a procedure type declaration.
     * // TODO: Error 3040
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ConstantDeclaredAsProcedureException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Constant '%s' declared as procedure type.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the undefined symbol.</p>
         */
        private final ConstantDeclaration constantDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.ConstantDeclaredAsProcedureException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ConstantDeclaredAsProcedureException(final TypeChecker culpritInstance,
                                                       final ConstantDeclaration constantDeclaration) {
            super(culpritInstance);
            this.constantDeclaration = constantDeclaration;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.constantDeclaration);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a Constant Declaration specified
     * an assignment literal that is not assignment compatible with its declared Type.
     * // TODO: Error 3058
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class DeclaredTypeNotAssignmentCompatibleException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Cannot assign value of type '%s' to variable of Type '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Type} value of the declared type.</p>
         */
        private final Type declaredType;

        /**
         * <p>The {@link Type} that was attempt to assign.</p>
         */
        private final Type assignmentType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link DeclaredTypeNotAssignmentCompatibleException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected DeclaredTypeNotAssignmentCompatibleException(final TypeChecker culpritInstance,
                                                               final Type declaredType,
                                                               final Type assignmentType) {
            super(culpritInstance);
            this.declaredType   = declaredType      ;
            this.assignmentType = assignmentType    ;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.assignmentType, this.declaredType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link AltStatement.Case}'s precondition
     * not bound to boolean {@link Type}.
     * // TODO: Error 660
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class AltCasePreconditionNotBoundToBooleanType extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "AltCase Precondition must be a boolean Type.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link AltStatement.Case} with a non-boolean {@link Type} precondition.</p>
         */
        private final AltStatement.Case aCase;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.ConstantDeclaredAsProcedureException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected AltCasePreconditionNotBoundToBooleanType(final TypeChecker culpritInstance,
                                                           final AltStatement.Case aCase) {
            super(culpritInstance);
            this.aCase = aCase;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return Message;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a Channel Write Statement bound
     * to a non-channel end type..
     * // TODO: Error 3023
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class WriteToNonChannelEndTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Cannot write to a non-channel end Type.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ChannelWriteStatement} bound to a  a non-{@link ChannelEndType}.</p>
         */
        private final ChannelWriteStatement channelWriteStatement;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.ConstantDeclaredAsProcedureException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected WriteToNonChannelEndTypeException(final TypeChecker culpritInstance,
                                                           final ChannelWriteStatement channelWriteStatement) {
            super(culpritInstance);
            this.channelWriteStatement = channelWriteStatement;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return Message;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a Control {@link Statement}'s
     * evaluation {@link Expression} bound to a non-boolean {@link Type}.
     * // TODO: Error 3024
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ControlEvaluationExpressionNonBooleanTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Control statement expression's Type is not bound to a boolean type.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ChannelWriteStatement} bound to a  a non-{@link ChannelEndType}.</p>
         */
        private final Type type;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.ControlEvaluationExpressionNonBooleanTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ControlEvaluationExpressionNonBooleanTypeException(final TypeChecker culpritInstance,
                                                                    final Type type) {
            super(culpritInstance);
            this.type = type;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return Message;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a non-par for loop enrolling
     * on Barriers.</p>
     * // TODO: Error 3026
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ProcessEnrolledOnBarriersException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message
                = "Process already enrolled on barriers (a non-par for loop cannot enroll on barriers).";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ForStatement} enrolled on addition barriers.</p>
         */
        private final Statement statement;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.ProcessEnrolledOnBarriersException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ProcessEnrolledOnBarriersException(final TypeChecker culpritInstance, final Statement statement) {
            super(culpritInstance);
            this.statement = statement;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return Message;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a par for loop expecting a barrier
     * type.</p>
     * // TODO: Error 3025
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ExpectedBarrierTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Barrier Type expected, found '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Type} bound to the barrier {@link Expression}.</p>
         */
        private final Type boundType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.ProcessEnrolledOnBarriersException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ExpectedBarrierTypeException(final TypeChecker culpritInstance, final Type boundType) {
            super(culpritInstance);
            this.boundType = boundType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.boundType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an improperly-placed return
     * statement.</p>
     * // TODO: Error 3027
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidReturnStatementContextException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Invalid return statement context '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Context} the {@link ReturnStatement} appreared in.</p>
         */
        private final Context returnStatementContext;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.ProcessEnrolledOnBarriersException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidReturnStatementContextException(final TypeChecker culpritInstance,
                                                         final Context context) {
            super(culpritInstance);
            this.returnStatementContext = context;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.returnStatementContext);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an improperly-placed Switch
     * label.</p>
     * // TODO: Error 0000
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidSwitchLabelContextException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Invalid switch label context '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Context} the {@link SwitchStatement.Group.Case} appeared in.</p>
         */
        private final Context switchLabelContext;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.InvalidSwitchLabelContextException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidSwitchLabelContextException(final TypeChecker culpritInstance,
                                                     final Context context) {
            super(culpritInstance);
            this.switchLabelContext = context;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.switchLabelContext);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an improperly-placed Suspend
     * {@link Statement}.</p>
     * // TODO: Error 3042
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidSuspendStatementContextException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message =
                "Invalid suspend statement context '%s'; suspend statement in non-procedure type.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Context} the {@link SuspendStatement} appeared in.</p>
         */
        private final Context suspendStatementContext;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link InvalidSuspendStatementContextException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidSuspendStatementContextException(final TypeChecker culpritInstance,
                                                          final Context context) {
            super(culpritInstance);
            this.suspendStatementContext = context;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.suspendStatementContext);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a Procedure Type with a
     * specified void return {@link Type} containing a return statement.</p>
     * // TODO: Error 3040
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class VoidProcedureReturnTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Procedure return type is void; return statement cannot return a value.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Context} the {@link ReturnStatement} appreared in.</p>
         */
        private final Context returnStatementContext;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.ProcessEnrolledOnBarriersException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected VoidProcedureReturnTypeException(final TypeChecker culpritInstance,
                                                   final Context context) {
            super(culpritInstance);
            this.returnStatementContext = context;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.returnStatementContext);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a Procedure Type not
     * returning a value.</p>
     * // TODO: Error 3040
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ProcedureReturnsVoidException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Procedure return type is '%s' but procedure return type is void.";

        /// --------------
        /// Private Fields

        /**
         * <p>The Procedure's return {@link Type}.</p>
         */
        private final Type returnType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.ProcedureReturnsVoidException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ProcedureReturnsVoidException(final TypeChecker culpritInstance,
                                                final Type type) {
            super(culpritInstance);
            this.returnType = type;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.returnType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a Procedure Type with a mismatched
     * return {@link Type}.</p>
     * // TODO: Error 3042
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class IncompatibleReturnTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message =
                "Incompatible type in return statement. Procedure marked to return '%s' but returns '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The Procedure's return {@link Type}.</p>
         */
        private final Type returnType;

        /**
         * <p>The actual bound {@link Type}.</p>
         */
        private final Type boundType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.IncompatibleReturnTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected IncompatibleReturnTypeException(final TypeChecker culpritInstance,
                                                  final Type returnType,
                                                  final Type type) {
            super(culpritInstance);
            this.returnType = returnType;
            this.boundType  = type;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.returnType, this.boundType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link SwitchStatement}'s evaluation
     * {@link Expression} bound to an improper {@link Type}.</p>
     * // TODO: Error 0000
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidSwitchStatementExpressionTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Illegal type '%s' in expression in switch statement.";

        /// --------------
        /// Private Fields

        /**
         * <p>The bound {@link Type}.</p>
         */
        private final Type boundType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.IncompatibleReturnTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidSwitchStatementExpressionTypeException(final TypeChecker culpritInstance,
                                                                final Type type) {
            super(culpritInstance);
            this.boundType  = type;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.boundType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link SwitchStatement.Group.Case}'s
     * {@link Expression}'s {@link Type} not assignable to the {@link SwitchStatement}'s Evaluation {@link Expression}'s
     * {@link Type}.</p>
     * // TODO: Error 0000
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidSwitchLabelExpressionTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message =
                "Switch label '%s' of type '%s' not compatible with switch expression's type '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link SwitchStatement.Group.Case}'s {@link Expression}.</p>
         */
        private final Expression switchLabelExpression;

        /**
         * <p>The {@link SwitchStatement.Group.Case}'s {@link Type}.</p>
         */
        private final Type labelType;

        /**
         * <p>The {@link SwitchStatement}'s evaluation {@link Expression}'s {@link Type}.</p>
         */
        private final Type evaluationExpressionType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.InvalidSwitchLabelExpressionTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidSwitchLabelExpressionTypeException(final TypeChecker culpritInstance,
                                                            final Expression switchLabelExpression,
                                                            final Type labelType,
                                                            final Type evaluationExpressionType) {
            super(culpritInstance);
            this.switchLabelExpression      = switchLabelExpression     ;
            this.labelType                  = labelType                 ;
            this.evaluationExpressionType   = evaluationExpressionType  ;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.switchLabelExpression, this.labelType, this.evaluationExpressionType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link SwitchStatement.Group.Case}'s
     * {@link Expression}'s {@link Type} not a Protocol Tag.</p>
     * // TODO: Error 0000
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class SwitchLabelExpressionNotProtocolTagException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Switch label '%s' is not a protocol tag.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link SwitchStatement.Group.Case}'s {@link Expression}.</p>
         */
        private final Expression labelExpression;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.InvalidSwitchLabelExpressionTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected SwitchLabelExpressionNotProtocolTagException(final TypeChecker culpritInstance,
                                                               final Expression labelExpression) {
            super(culpritInstance);

            this.labelExpression = labelExpression;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.labelExpression);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an attempt to access an undefined
     * {@link ProtocolType}'s Tag.</p>
     * // TODO: Error 0000
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class UndefinedTagException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Tag '%s' is not found in protocol '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the undefined {@link ProtocolType}'s Tag.</p>
         */
        private final String tag;

        /**
         * <p>The {@link ProtocolType} searched.</p>
         */
        private final ProtocolType protocolType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.UndefinedTagException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected UndefinedTagException(final TypeChecker culpritInstance,
                                        final String tag,
                                        final ProtocolType protocolType) {
            super(culpritInstance);

            this.tag                        = tag                       ;
            this.protocolType = protocolType;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.tag, this.protocolType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an illegally nested
     * {@link SwitchStatement} within a {@link ProtocolType}.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class IllegalNestedSwitchInProtocolException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Illegally nested switch statement in Protocol '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ProtocolType} with an illegally nested {@link SwitchStatement}.</p>
         */
        private final ProtocolType protocolType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.UndefinedTagException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected IllegalNestedSwitchInProtocolException(final TypeChecker culpritInstance,
                                                         final ProtocolType protocolType) {
            super(culpritInstance);

            this.protocolType = protocolType;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.protocolType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an improperly-placed Suspend
     * {@link Statement}, particularly within a non mobile procedure.</p>
     * // TODO: Error 3043
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class SuspendInNonMobileProcedureException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message =
                "Invalid suspend statement context '%s'; suspend statement in non-mobile procedure type.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Context} the {@link SuspendStatement} appeared in.</p>
         */
        private final Context suspendStatementContext;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link SuspendInNonMobileProcedureException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected SuspendInNonMobileProcedureException(final TypeChecker culpritInstance,
                                                          final Context context) {
            super(culpritInstance);
            this.suspendStatementContext = context;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.suspendStatementContext);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an improperly
     * {@link Type}-bounded {@link SyncStatement}.</p>
     * // TODO: Error 0000
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidSynchronizationExpressionTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message =
                "Synchronization statement can only bind to barrier types; found: '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Type} bound to the {@link SyncStatement}.</p>
         */
        private final Type synchronizationExpressionType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link InvalidSynchronizationExpressionTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidSynchronizationExpressionTypeException(final TypeChecker culpritInstance,
                                                                final Type synchronizationExpressionType) {
            super(culpritInstance);
            this.synchronizationExpressionType = synchronizationExpressionType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.synchronizationExpressionType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an improperly
     * {@link Type}-bounded {@link TimeoutStatement} target {@link Type}.</p>
     * // TODO: Error 0000
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidTimeoutTargetTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Timeout statement can only bind to a timer; found: '%s'.";
        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Type} bound to the {@link TimeoutStatement}.</p>
         */
        private final Type timeoutTargetType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link InvalidTimeoutTargetTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidTimeoutTargetTypeException(final TypeChecker culpritInstance,
                                                    final Type timeoutTargetType) {
            super(culpritInstance);
            this.timeoutTargetType = timeoutTargetType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.timeoutTargetType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an improperly
     * {@link Type}-bounded {@link TimeoutStatement} delay {@link Type}.</p>
     * // TODO: Error 3049
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidTimeoutDelayTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message =
                "Invalid delay type '%s' in timeout statement; timeout statement must specify an integral Type.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Type} bound to the {@link TimeoutStatement} delay {@link Expression}.</p>
         */
        private final Type timeoutDelayType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link InvalidTimeoutDelayTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidTimeoutDelayTypeException(final TypeChecker culpritInstance,
                                                    final Type timeoutDelayType) {
            super(culpritInstance);
            this.timeoutDelayType = timeoutDelayType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.timeoutDelayType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an improperly
     * {@link Type}-bounded {@link InvocationExpression}.</p>
     * // TODO: Error 3052
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidInvocationTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Invalid invocation type for '%s'; found: '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link InvocationExpression} that attempted to resolve.</p>
         */
        private final InvocationExpression invocationExpression;

        /**
         * <p>The {@link Type} resolved from an {@link InvocationExpression}'s name.</p>
         */
        private final Type resolvedType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link InvalidInvocationTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidInvocationTypeException(final TypeChecker culpritInstance,
                                                   final InvocationExpression invocationExpression,
                                                   final Type resolvedType) {
            super(culpritInstance);
            this.invocationExpression = invocationExpression;
            this.resolvedType = resolvedType    ;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.invocationExpression, this.resolvedType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an invocation without
     * any suitable candidates.</p>
     * // TODO: Error 3037
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class NoCandidateForInvocationFoundException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "No suitable procedure found for '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link InvocationExpression} that attempted to resolve.</p>
         */
        private final InvocationExpression invocationExpression;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link NoCandidateForInvocationFoundException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected NoCandidateForInvocationFoundException(final TypeChecker culpritInstance,
                                                        final InvocationExpression invocationExpression) {
            super(culpritInstance);
            this.invocationExpression = invocationExpression;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.invocationExpression);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an invocation that
     * couldn't resolve to a {@link ProcedureType}.</p>
     * // TODO: Error 3038
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class AmbiguousInvocationException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Ambiguous invocation '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link InvocationExpression} that attempted to resolve.</p>
         */
        private final InvocationExpression invocationExpression;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link AmbiguousInvocationException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected AmbiguousInvocationException(final TypeChecker culpritInstance,
                                               final InvocationExpression invocationExpression) {
            super(culpritInstance);
            this.invocationExpression = invocationExpression;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.invocationExpression);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an array dimension
     * {@link Expression} binding to a non-integral {@link Type}.</p>
     * // TODO: Error 3031
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidArrayDimensionTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Invalid type for array dimension expression '%s'; found: '%s'";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Expression} that did not bind to an Integral Type.</p>
         */
        private final Expression arrayDimensionExpression;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link InvalidArrayDimensionTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidArrayDimensionTypeException(final TypeChecker culpritInstance,
                                                    final Expression arrayDimensionExpression) {
            super(culpritInstance);
            this.arrayDimensionExpression = arrayDimensionExpression;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.arrayDimensionExpression, this.arrayDimensionExpression.getType());

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link Type} not
     * being assignment compatible with another.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class TypeNotAssignmentCompatibleException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Invalid type for expression '%s'; expected: '%s', but found '%s";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Expression} that did not bind to the expected {@link Type}.</p>
         */
        private final Expression expression;

        /**
         * <p>The {@link Type} that was expected</p>
         */
        private final Type       expectedType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeNotAssignmentCompatibleException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected TypeNotAssignmentCompatibleException(final TypeChecker culpritInstance,
                                                       final Expression expression,
                                                       final Type expectedType) {
            super(culpritInstance);
            this.expression     = expression;
            this.expectedType = expectedType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.expression, this.expectedType, this.expression.getType());

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link Type} not
     * being assignable.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class TypeNotAssignableException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Invalid assignment; '%s' is not assignable to '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Expression} that cannot be assigned.</p>
         */
        private final Expression expression     ;

        /**
         * <p>The {@link Type} that was expected</p>
         */
        private final Type       expectedType   ;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeNotAssignmentCompatibleException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected TypeNotAssignableException(final TypeChecker culpritInstance,
                                             final Expression expression,
                                             final Type expectedType) {
            super(culpritInstance);
            this.expression     = expression;
            this.expectedType = expectedType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.expression, this.expectedType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link Type} used
     * on the left hand side of a bit shift operator not bound to an integral {@link Type}.</p>
     * // TODO: Error 604
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class LeftSideOfShiftNotIntegralOrBoolean extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Left hand side of shift operator must be an integral type; found '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Type} that was found.</p>
         */
        private final Type foundType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link LeftSideOfShiftNotIntegralOrBoolean}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected LeftSideOfShiftNotIntegralOrBoolean(final TypeChecker culpritInstance,
                                                      final Type expectedType) {
            super(culpritInstance);
            this.foundType = expectedType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.foundType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link Type} used
     * on the right hand side of a bit shift operator not bound to an integral {@link Type}.</p>
     * // TODO: Error 605
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class RightSideOfShiftNotIntegralOrBoolean extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Right hand side of shift operator must be an integral type; found '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Type} that was found.</p>
         */
        private final Type foundType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link RightSideOfShiftNotIntegralOrBoolean}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected RightSideOfShiftNotIntegralOrBoolean(final TypeChecker culpritInstance,
                                                       final Type foundType) {
            super(culpritInstance);
            this.foundType = foundType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.foundType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link Type} used
     * on the right & left hand side of a bit shift operator not bound to an integral or boolean {@link Type}.</p>
     * // TODO: Error 606
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class CompoundBitwiseTypesNotIntegralOrBoolean extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message =
                "Right & left hand side of compound bitwise operator must be an integral or boolean type; found '%s' '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The left hand side {@link Type} that was found.</p>
         */
        private final Type leftType;

        /**
         * <p>The Right hand side {@link Type} that was found.</p>
         */
        private final Type rightType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link CompoundBitwiseTypesNotIntegralOrBoolean}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected CompoundBitwiseTypesNotIntegralOrBoolean(final TypeChecker culpritInstance,
                                                           final Type leftType,
                                                           final Type rightType) {
            super(culpritInstance);
            this.leftType = leftType;
            this.rightType = rightType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.leftType, this.rightType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a relational
     * {@link BinaryExpression} where either side does not have a numeric {@link Type} bound.</p>
     * // TODO: Error 3010
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class RelationalOperatorRequiresNumericTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Relational Expression requires numeric types; found: '%s' and '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The left hand side {@link Type} that was found.</p>
         */
        private final Type leftType;

        /**
         * <p>The Right hand side {@link Type} that was found.</p>
         */
        private final Type rightType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link RelationalOperatorRequiresNumericTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected RelationalOperatorRequiresNumericTypeException(final TypeChecker culpritInstance,
                                                                 final Type leftType,
                                                                 final Type rightType) {
            super(culpritInstance);
            this.leftType = leftType;
            this.rightType = rightType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.leftType, this.rightType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a logical
     * {@link BinaryExpression} where either side has a void {@link Type} bound.</p>
     * // TODO: Error 3011
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class VoidTypeUsedInLogicalComparisonException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Void used in logical binary comparison; found: '%s' and '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The left hand side {@link Type} that was found.</p>
         */
        private final Type leftType;

        /**
         * <p>The Right hand side {@link Type} that was found.</p>
         */
        private final Type rightType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link VoidTypeUsedInLogicalComparisonException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected VoidTypeUsedInLogicalComparisonException(final TypeChecker culpritInstance,
                                                                 final Type leftType,
                                                                 final Type rightType) {
            super(culpritInstance);
            this.leftType = leftType;
            this.rightType = rightType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.leftType, this.rightType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a logical
     * {@link BinaryExpression} where the left and right hand side {@link Type}s do not match.</p>
     * // TODO: Error 3012
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class LogicalComparisonTypeMismatchException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Type mismatch in logical binary comparison; found: '%s' and '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The left hand side {@link Type} that was found.</p>
         */
        private final Type leftType;

        /**
         * <p>The Right hand side {@link Type} that was found.</p>
         */
        private final Type rightType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link LogicalComparisonTypeMismatchException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected LogicalComparisonTypeMismatchException(final TypeChecker culpritInstance,
                                                         final Type leftType,
                                                         final Type rightType) {
            super(culpritInstance);
            this.leftType = leftType;
            this.rightType = rightType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.leftType, this.rightType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a logical
     * {@link BinaryExpression} where the left and right hand side {@link Type}s are not bound to a boolean
     * {@link Type}.</p>
     * // TODO: Error 3013
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class LogicalComparisonNotBooleanTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Logical binary comparison requires boolean types; found: '%s' and '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The left hand side {@link Type} that was found.</p>
         */
        private final Type leftType;

        /**
         * <p>The Right hand side {@link Type} that was found.</p>
         */
        private final Type rightType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link LogicalComparisonNotBooleanTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected LogicalComparisonNotBooleanTypeException(final TypeChecker culpritInstance,
                                                         final Type leftType,
                                                         final Type rightType) {
            super(culpritInstance);
            this.leftType = leftType;
            this.rightType = rightType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.leftType, this.rightType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link Type} used
     * on the right & left hand side of a binary bitwise operator not bound to an integral or boolean {@link Type}.</p>
     * // TODO: Error 606
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class BinaryBitwiseTypesNotIntegralOrBoolean extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message =
                "Right & left hand side of binary bitwise operator must be an integral or boolean type; found '%s' '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The left hand side {@link Type} that was found.</p>
         */
        private final Type leftType;

        /**
         * <p>The Right hand side {@link Type} that was found.</p>
         */
        private final Type rightType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link BinaryBitwiseTypesNotIntegralOrBoolean}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected BinaryBitwiseTypesNotIntegralOrBoolean(final TypeChecker culpritInstance,
                                                           final Type leftType,
                                                           final Type rightType) {
            super(culpritInstance);
            this.leftType = leftType;
            this.rightType = rightType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.leftType, this.rightType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a arithmetic
     * {@link BinaryExpression} where either side does not have a numeric {@link Type} bound.</p>
     * // TODO: Error 3015
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ArithmeticOperatorRequiresNumericTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Arithmetic Expression requires numeric types; found: '%s' and '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The left hand side {@link Type} that was found.</p>
         */
        private final Type leftType;

        /**
         * <p>The Right hand side {@link Type} that was found.</p>
         */
        private final Type rightType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ArithmeticOperatorRequiresNumericTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ArithmeticOperatorRequiresNumericTypeException(final TypeChecker culpritInstance,
                                                                 final Type leftType,
                                                                 final Type rightType) {
            super(culpritInstance);
            this.leftType = leftType;
            this.rightType = rightType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.leftType, this.rightType);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a arithmetic
     * {@link UnaryPostExpression} with an invalid operand.</p>
     * // TODO: Error 3051
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidUnaryOperandException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Cannot apply operator '%s' to something of type '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The invalid unary operand.</p>
         */
        private final Expression operand;

        /**
         * <p>{@link String} value of the Unary operator.</p>
         */
        private final String operator;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link InvalidUnaryOperandException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidUnaryOperandException(final TypeChecker culpritInstance,
                                               final Expression operand,
                                               final String operator) {
            super(culpritInstance);
            this.operand = operand;
            this.operator = operator;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.operator, this.operand);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a arithmetic
     * {@link UnaryPostExpression} with a Literal Operand.</p>
     * // TODO: Error 3057
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidLiteralUnaryOperandException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Non-literal expression expected; found: '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The invalid unary operand.</p>
         */
        private final Expression operand;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link InvalidLiteralUnaryOperandException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidLiteralUnaryOperandException(final TypeChecker culpritInstance,
                                                      final Expression operand) {
            super(culpritInstance);
            this.operand = operand;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.operand);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of ChannelEndExpression
     * bound to a non-{@link ChannelType}.</p>
     * // TODO: Error 3019
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ChannelEndExpressionBoundToNonChannelTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message =
                "Channel End expression '%s' bound to a non-channel type; found: '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The mis-typed {@link ChannelEndExpression}.</p>
         */
        private final Expression channelEndExpression;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ChannelEndExpressionBoundToNonChannelTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ChannelEndExpressionBoundToNonChannelTypeException(final TypeChecker culpritInstance,
                                                                    final ChannelEndExpression channelEndExpression) {
            super(culpritInstance);
            this.channelEndExpression = channelEndExpression;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.channelEndExpression, this.channelEndExpression.getType());

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a ChannelReadExpression
     * bound to an invalid {@link Type}.</p>
     * // TODO: Error 3019
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidChannelReadExpressionTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message =
                "Invalid type for Channel Read Expression '%s'; found '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The mis-typed {@link ChannelReadExpression}.</p>
         */
        private final Expression channelReadExpression;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ChannelEndExpressionBoundToNonChannelTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidChannelReadExpressionTypeException(final TypeChecker culpritInstance,
                                                            final Expression channelReadExpression) {
            super(culpritInstance);
            this.channelReadExpression = channelReadExpression;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.channelReadExpression, this.channelReadExpression.getType());

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a ChannelReadExpression
     * reading from a timer with a specified extended rendezvous.</p>
     * TODO: Error 3022
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class TimerReadWithExtendedRendezvous extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message =
                "Timer read expression '%s' cannot have extended rendez-vous block.";

        /// --------------
        /// Private Fields

        /**
         * <p>The mis-typed {@link ChannelReadExpression}.</p>
         */
        private final Expression channelReadExpression;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TimerReadWithExtendedRendezvous}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected TimerReadWithExtendedRendezvous(final TypeChecker culpritInstance,
                                                  final Expression channelReadExpression) {
            super(culpritInstance);
            this.channelReadExpression = channelReadExpression;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.channelReadExpression);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an invalid ArrayAccessType.</p>
     * TODO: Error 3022
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidArrayAccessTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message =
                "Invalid array access type; found: '%s'.";

        /// --------------
        /// Private Fields

        /**
         * <p>The resolved {@link Type}.</p>
         */
        private final Type arrayAccessType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link InvalidArrayAccessTypeException}.</p>
         * @param culpritInstance The {@link TypeChecker} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidArrayAccessTypeException(final TypeChecker culpritInstance,
                                                  final Type arrayAccessType) {
            super(culpritInstance);
            this.arrayAccessType = arrayAccessType;
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return String.format(Message, this.arrayAccessType);

        }

    }

    // TODO: Remove These

    private static boolean assertAssignmentCompatible(final ProcedureType procedureType,
                                                      final InvocationExpression invocationExpression) {

        // Assert they each have the same amount of parameters
        boolean areAssignmentCompatible =
                invocationExpression.getParameterCount() == procedureType.getParameterCount();

        // Iterate while the invariant is true
        for(int index = 0; areAssignmentCompatible && (index < invocationExpression.getParameterCount()); index++) {

            // Initialize a handle to the Procedure Type's Parameter Type
            final Type parameterType    = procedureType.getTypeForParameterAt(index);
            final Type invocationType   = invocationExpression.getTypeForParameter(index);

            // Update the invariant
            areAssignmentCompatible = ((parameterType != null) && (invocationType != null))
                    && parameterType.isAssignmentCompatibleTo(invocationType);

        }

        // Return the result
        return areAssignmentCompatible;

    }

    private static boolean assertTypeAssignmentCompatible(final ProcedureType leftProcedure,
                                                          final ProcedureType rightProcedure) {

        // Assert they each have the same amount of parameters
        boolean areAssignmentCompatible =
                rightProcedure.getParameterCount() == leftProcedure.getParameterCount();

        // Iterate while the invariant is true
        for(int index = 0; areAssignmentCompatible && (index < rightProcedure.getParameterCount()); index++) {

            // Initialize a handle to the Procedure Type's Parameter Type
            final Type leftType    = leftProcedure.getTypeForParameterAt(index);
            final Type rightType   = rightProcedure.getTypeForParameterAt(index);

            // Update the invariant
            areAssignmentCompatible = ((leftType != null) && (rightType != null))
                    && leftType.isAssignmentCompatibleTo(rightType);

        }

        // Return the result
        return areAssignmentCompatible;

    }

    private static List<ProcedureType> aggregateAssignmentCompatible(final Context.SymbolMap symbolMap,
                                                                     final InvocationExpression invocationExpression,
                                                                     final List<ProcedureType> result) {

        // Iterate through the SymbolMap
        symbolMap.forEachSymbol(symbol -> {

            // Assert the Symbol is a Procedure Type Declaration & its assignment compatible
            // with the specified Invocation
            if((symbol instanceof ProcedureType)
                    && assertAssignmentCompatible((ProcedureType) symbol, invocationExpression))
                result.add((ProcedureType) symbol);

        });

        // Return the result
        return result;

    }
    private static ProcedureType Candidate(final List<ProcedureType> candidates) {

        int remaining = candidates.size();

        for(int leftIndex = 0; leftIndex < candidates.size(); leftIndex++) {

            ProcedureType leftProcedure = candidates.get(leftIndex);

            // If we should continue
            if(leftProcedure != null) {

                // Hide the current procedure
                candidates.set(leftIndex, null);

                // Iterate for the right hand side
                for(int rightIndex = 0; rightIndex < candidates.size(); rightIndex++) {

                    // Retrieve the right hand side
                    ProcedureType rightProcedure = candidates.get(rightIndex);

                    // Assert if for all k, right[k] :> left[k]; Remove the right hand side
                    if((rightProcedure != null) && (assertTypeAssignmentCompatible(leftProcedure, rightProcedure))) {

                        candidates.set(rightIndex, null);

                        remaining--;

                    }

                }

                // Reset the left hand side
                candidates.set(leftIndex, leftProcedure);

            }

        }

        ProcedureType result = null;

        if(remaining == 1) for(final ProcedureType procedure: candidates)
            if(procedure != null) {

                result = procedure;
                break;

            }

        return result;

    }

    private static String Line(final char side, final char fill, final int length, final int leftPadding, final int rightPadding) {

        return side + String.valueOf(fill).repeat(length + leftPadding + rightPadding) + side;

    }

    private static String Border(final int length, final int leftPadding, final int rightPadding) {

        return Line('+', '-', length, leftPadding, rightPadding);

    }

    private static String Title(final String title, final char fill, final int leftPadding, final int rightPadding) {

        final int trail = (title.length() + leftPadding + rightPadding) % 2;

        return '+' + String.valueOf(fill).repeat(leftPadding) + title
                + String.valueOf(fill).repeat( rightPadding + trail) + '+';

    }

    private static String Content(final String content, final char fill, final int leftPadding, final int rightPadding) {

        final int trail = (content.length() + leftPadding + rightPadding) % 2;

        return '|' + String.valueOf(fill).repeat(leftPadding) + content
                + String.valueOf(fill).repeat( rightPadding + trail) + '|';

    }

    private static List<String> Signatures(final List<ProcedureType> candidates) {

        // Initialize the result
        final List<String> signatures = new ArrayList<>();

        // Collect the candidate signatures
        candidates.forEach(candidate -> signatures.add(candidate.getSignature()));

        // Return the result
        return signatures;

    }

    private static int Longest(final List<String> list) {

        int result = 0;

        for(final String string: list)
            result = Math.max(string.length(), result);

        // Return the result
        return result;

    }

    private static void FancyPrint(final InvocationExpression invocationExpression, final List<ProcedureType> candidates,
                                   final int leftPadding, final int rightPadding) {

        final String title = "Candidates for '" + invocationExpression.getProcedureName() + "': ";

        // Retrieve the signatures & the longest string out of them and the title
        final List<String>  signatures  = Signatures(candidates);
        final int           longest     = Math.max(title.length(), Longest(signatures));
        final int           trail       = (longest + leftPadding + rightPadding) % 2;

        System.out.println(Border(longest + trail, leftPadding, rightPadding));
        System.out.println(Line('+', ' ', longest + trail, leftPadding, rightPadding));
        System.out.println(Title(title, ' ', leftPadding, rightPadding));
        System.out.println(Line('+', ' ', longest + trail, leftPadding, rightPadding));
        System.out.println(Border(longest + trail, leftPadding, rightPadding));
        System.out.println(Line('|', ' ', longest + trail, leftPadding, rightPadding));

        for(final String signature: signatures)
            System.out.println(Content(signature, ' ', leftPadding, rightPadding));


        System.out.println(Line('|', ' ', longest + trail, leftPadding, rightPadding));
        System.out.println(Border(longest + trail, leftPadding, rightPadding));

    }


}
