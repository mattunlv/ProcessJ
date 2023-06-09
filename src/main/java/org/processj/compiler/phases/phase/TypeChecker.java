
package org.processj.compiler.phases.phase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.alt.AltCase;
import org.processj.compiler.ast.alt.Guard;
import org.processj.compiler.ast.expression.*;
import org.processj.compiler.utilities.Log;
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
    Hashtable<String, ProtocolCase> protocolTagsSwitchedOn = new Hashtable<String, ProtocolCase>();

    // Set of nested protocols in nested switch statements
    HashSet<String> protocolsSwitchedOn = new HashSet<String>();

    public TypeChecker(final Phase.Listener listener) {
        super(listener);
    }

    // ContinueStat - nothing to do here, but further checks are needed. TODO
    // ParBlock - nothing to do
    // SkipStat - nothing to do
    // StopStat - nothing to do
    // ProtocolCase - nothing to do.
    // SwitchGroup -- nothing to do - handled in SwitchStat
    // SwitchLabel -- nothing to do - handled in SwitchStat

    /// ----------------------------------------------------------------------------------------------------------- ///
    /// Top Level Declarations                                                                                      ///
    /// ----------------------------------------------------------------------------------------------------------- ///

    /**
     * <p>Verifies that the {@link ConstantDecl}'s {@link Type} is assignment compatible with the {@link ConstantDecl}'s
     * initialization {@link Expression} if it has one. Otherwise, this passes through.</p>
     * @param constantDeclaration The {@link ConstantDecl} to check.
     * @see ConstantDecl
     * @see Expression
     * @see Type
     * @since 0.1.0
     */
    @Override
    public final Void visitConstantDecl(final ConstantDecl constantDeclaration) throws Phase.Error {

        Log.log(constantDeclaration.line + ": Visiting a ConstantDeclaration (" + constantDeclaration + ").");

        // Retrieve the declared Type
        final Type declaredType = constantDeclaration.getType();

        // Assert the Type is not a Procedure Type
        if(declaredType instanceof ProcTypeDecl)
            throw new ConstantDeclaredAsProcedureException(this, constantDeclaration).commit();

        // If we have something to check
        if(constantDeclaration.isInitialized()) {

            // Resolve the initialization Expression
            constantDeclaration.getInitializationExpression().visit(this);

            // Initialize a handle to the initialization Expression's Type
            final Type initializedType = constantDeclaration.getInitializationExpression().getType();

            // Assert the right hand side Type & left hand side Type are assignment compatible
            if(!declaredType.typeAssignmentCompatible(initializedType))
                throw new DeclaredTypeNotAssignmentCompatibleException(this, initializedType, declaredType).commit();

        }

        return null;

    }

    @Override
    public final Void visitProcTypeDecl(final ProcTypeDecl procedureTypeDeclaration) throws Phase.Error {

        Log.log(procedureTypeDeclaration.line + ": Visiting a Procedure Type Declaration '"
                + procedureTypeDeclaration + "'.");

        // ReturnType should already be bound
        // ParamDeclarations should already be bound
        // Implements list should already be bound

        // TODO: Maybe check implements list?
        procedureTypeDeclaration.getBody().visit(this);

        return null;

    }

    @Override
    public final Void visitProtocolTypeDecl(final ProtocolTypeDecl protocolTypeDeclaration) throws Phase.Error {

        // TODO: Maybe check extend Types?
        protocolTypeDeclaration.getBody().visit(this);

        return null;

    }

    @Override
    public final Void visitRecordTypeDecl(final RecordTypeDecl recordTypeDeclaration) throws Phase.Error {

        // TODO: Maybe check extends Types?
        recordTypeDeclaration.getBody().visit(this);

        return null;

    }

    /**
     * <p>Verifies that the {@link LocalDecl}'s {@link Type} is assignment compatible with the {@link LocalDecl}'s
     * initialization {@link Expression} if it has one. Otherwise, this passes through.</p>
     * @param localDecl The {@link LocalDecl} to check.
     * @see LocalDecl
     * @see Expression
     * @see Type
     * @since 0.1.0
     */
    @Override
    public final Void visitLocalDecl(final LocalDecl localDecl) throws Phase.Error {

        Log.log(localDecl.line + ": Visiting a LocalDecl (" + localDecl + ").");

        // If we have something to check
        if(localDecl.isInitialized()) {

            // Visit the initialization expression first
            localDecl.getInitializationExpression().visit(this);

            // Initialize a handle to each Type
            final Type declaredType     = localDecl.getType();
            final Type initializedType  = localDecl.getInitializationExpression().getType();

            if(!declaredType.typeAssignmentCompatible(initializedType))
                throw new DeclaredTypeNotAssignmentCompatibleException(this, initializedType, declaredType).commit();

        }

        return null;

    }

    /**
     * <p>Verifies that the {@link AltCase}'s precondition resolves to a boolean {@link Type}, if the
     * {@link Guard} is an input {@link Guard} that it resolves correctly
     * for both sides of the {@link Assignment} {@link Expression} or if it's a {@link TimeoutStat} that the timer
     * {@link Expression} is a Timer type and the delay {@link Expression} resolves to an integral {@link Type};
     * lastly it recurs down on the contained {@link Statement}.</p>
     * @param altCase The {@link AltCase} to check.
     * @see AltCase
     * @see Guard
     * @see Expression
     * @see Type
     * @since 0.1.0
     */
    @Override
    public final Void visitAltCase(final AltCase altCase) throws Phase.Error {

        // Alt Case -> Syntax: (Expr) && Guard : Statement => Boolean?(T(expr))
        // Three possibilities for this: Assignment, TimeoutStatement, & Skip Statement
        // Expr must be Boolean and Guard and Statement must be visited

        // If the AltCase defines a Boolean Precondition
        if(altCase.definesPrecondition()) {

            // Retrieve a handle to the Precondition
            final Expression precondition = altCase.getPreconditionExpression();

            // Resolve the precondition
            precondition.visit(this);

            // If the precondition's Type is not Boolean
            if(!(precondition.getType()).isBooleanType())
                throw new AltCasePreconditionNotBoundToBooleanType(this, altCase);

        }

        // It's possible we may have a null guard;
        // TODO: Verify no null guards reach this point
        if(altCase.definesGuard()) altCase.getGuard().visit(this);

        // Lastly, recur on the Statement
        altCase.getStatements().visit(this);

        return null;

    }

    @Override
    public final Void visitChannelWriteStat(final ChannelWriteStat channelWriteStat) throws Phase.Error {

        // TODO: A Channel Write Statement looks like an Invocation. Shouldn't
        // TODO: Resolve the ChannelEndType here instead of the Parse Phase?

        // Some sort of Name Expression should resolve to a Channel End Type either from
        // a primary expression or direct expression
        // Initialize a handle to the target & write Expressions
        final Expression targetExpression   = channelWriteStat.getTargetExpression()  ;
        final Expression writeExpression    = channelWriteStat.getWriteExpression()   ;

        // Bind the Target Expression first
        targetExpression.visit(this);

        // Assert that the Type bound to the Expression is a Channel End Type
        if(!(targetExpression.getType() instanceof ChannelEndType))
            throw new WriteToNonChannelEndTypeException(this, channelWriteStat).commit();

        // Visit the expression being written.
        channelWriteStat.getWriteExpression().visit(this);

        // Assert the target Expression is Type Equal
        TypeAssert.TargetExpressionTypeEqual(channelWriteStat);

        return null;

    }

    @Override
    public final Void visitDoStat(final DoStat doStatement) throws Phase.Error {

        Log.log(doStatement.line + ": Visiting a do statement");

        // Resolve the evaluation Expression
        doStatement.getEvaluationExpression().visit(this);

        // Initialize a handle to the do statement's evaluation Expression's Type
        final Type type = doStatement.getEvaluationExpression().getType();

        // Assert the evaluation Expression is bound to a boolean Type
        if(!type.isBooleanType())
            throw new ControlEvaluationExpressionNonBooleanTypeException(this, type);

        // Recur on the body
        doStatement.getStatements().visit(this);

        return null;

    }

    @Override
    public final Void visitIfStat(final IfStat ifStatement) throws Phase.Error {

        Log.log(ifStatement.line + ": Visiting a if statement");

        // Resolve the evaluation Expression
        ifStatement.getEvaluationExpression().visit(this);

        // Initialize a handle to the do statement's evaluation Expression's Type
        final Type type = ifStatement.getEvaluationExpression().getType();

        // Assert that it's a boolean Type
        if(!ifStatement.getEvaluationExpression().getType().isBooleanType())
            throw new ControlEvaluationExpressionNonBooleanTypeException(this, type).commit();

        // Resolve the then block
        ifStatement.getThenStatements().visit(this);

        // Resolve the else block
        ifStatement.getElseBody().visit(this);

        return null;

    }

    @Override
    public final Void visitWhileStat(final WhileStat whileStatement) throws Phase.Error {

        Log.log(whileStatement.line + ": Visiting a while statement");

        // Resolve the evaluation Expression
        whileStatement.getEvaluationExpression().visit(this);

        // Initialize a handle to the evaluation expression's Type
        final Type type = whileStatement.getEvaluationExpression().getType();

        // Assert the evaluation Expression is bound to a boolean Type
        if(!type.isBooleanType())
            throw new ControlEvaluationExpressionNonBooleanTypeException(this, type).commit();

        // Recur on the body
        whileStatement.getStatements().visit(this);

        return null;

    }

    @Override
    public final Void visitForStat(final ForStat forStatement) throws Phase.Error {

        Log.log(forStatement.line + ": Visiting a for statement");

        // A non-par for loop cannot enroll on anything.
        if(forStatement.getBarrierExpressions().size() > 0 || forStatement.isPar())
            throw new ProcessEnrolledOnBarriersException(this, forStatement).commit();

        // Check that all the barrier expressions are of barrier type.
        for(final Expression barrierExpression: forStatement.getBarrierExpressions()) {

            // Resolve the Barrier Expression
            barrierExpression.visit(this);

            // Retrieve the Type
            final Type type = barrierExpression.getType();

            // Assert it's a barrier Type
            if(!barrierExpression.getType().isBarrierType())
                throw new ExpectedBarrierTypeException(this, type).commit();

        }

        // Resolve the initialization Expression, if any.
        if(forStatement.definesInitializationExpression())
            forStatement.getInitializationExpression().visit(this);

        if(forStatement.definesIncrement())
            forStatement.getIncrementExpression().visit(this);

        if(forStatement.definesEvaluationExpression()) {

            // Resolve the evaluation Expression
            forStatement.getEvaluationExpression().visit(this);

            // Retrieve the Type
            final Type type = forStatement.getEvaluationExpression().getType();

            // Assert the Expression is bound to a boolean Type
            if(!type.isBooleanType())
                throw new ControlEvaluationExpressionNonBooleanTypeException(this, type).commit();

        }

        // Recur on the statement
        forStatement.getStatements().visit(this);

        return null;

    }

    @Override
    public final Void visitReturnStat(final ReturnStat returnStatement) throws Phase.Error {

        Log.log(returnStatement.line + ": visiting a return statement");

        // Initialize a handle to the current Context
        final SymbolMap.Context context = this.getScope().getContext();

        // Assert the Context is a Procedure Type
        if(!(context instanceof ProcTypeDecl))
            throw new InvalidReturnStatementContextException(this, context).commit();

        // Otherwise, initialize a handle to its return Type
        final Type returnType = ((ProcTypeDecl) context).getReturnType();

        // Assert that the return Type is void and the return statement doesn't define an Expression
        if(returnType.isVoidType() && (returnStatement.getExpression() != null))
            throw new VoidProcedureReturnTypeException(this, context);

        // Assert that the return Type is not void and the return statement does define an Expression
        if(!returnType.isVoidType() && returnStatement.getExpression() == null)
            throw new ProcedureReturnsVoidException(this, returnType).commit();

        // Resolve the Return Type's Expression
        returnStatement.getExpression().visit(this);

        // Initialize a handle to the Expression's Type
        final Type type = returnStatement.getExpression().getType();

        // Assert the Return Type is assignment Compatible with the Return Statement's
        if(!returnType.typeAssignmentCompatible(type))
            throw new IncompatibleReturnTypeException(this, returnType, type).commit();

        return null;

    }

    @Override
    public final Void visitBreakStat(final BreakStat breakStat) throws Phase.Error {

        // Assert the Break Statement is in a Switch Group
        this.getScope().forEachContextUntil(context -> {

            // Initialize the result
            final boolean inSwitchGroup = (context instanceof SwitchGroup);

            // Mark the Switch group
            if(inSwitchGroup) ((SwitchGroup) context).setContainsBreakStatement(true);

            // Return the result
            return inSwitchGroup;

        });

        return null;

    }

    @Override
    public final Void visitSwitchGroup(final SwitchGroup switchGroup) throws Phase.Error {

        Log.log(switchGroup.line + ": Visiting SwitchGroup (" + switchGroup + ").");

        // Resolve the labels first to mark ourselves
        switchGroup.getLabels().visit(this);

        // Now the statements
        switchGroup.getStatements().visit(this);

        // Initialize a handle to the Context
        final SymbolMap.Context context = this.getScope().getContext();

        // Assert the Context is a SwitchStatement
        if(!(context instanceof SwitchStat))
            throw new InvalidSwitchLabelContextException(this, context);

        // Initialize a handle to the evaluation Expression's Type
        final Type type = ((SwitchStat) context).getEvaluationExpression().getType();

        // If the evaluation Expression's Type is a Protocol Type so we can remove ourselves
        if(type instanceof ProtocolTypeDecl) {

            // Initialize a handle to the evaluation Expression's name
            final String name = ((SwitchStat) context).getEvaluationExpression().toString();

            // Assert the SwitchGroup doesn't contain more than one label
            if(switchGroup.getLabels().size() > 1)
                PJBugManager.INSTANCE.reportMessage(
                        new PJMessage.Builder()
                                .addAST(switchGroup)
                                .addError(VisitorMessageNumber.REWRITE_1004)
                                .addArguments(name)
                                .build());

            // Assert the SwitchGroup contains a break statement
            // TODO: Assert this with a nested forEachContext within visitBreakSTat
            else if(!switchGroup.containsBreakStatement())
                PJBugManager.INSTANCE.reportMessage(
                        new PJMessage.Builder()
                                .addAST(switchGroup)
                                .addError(VisitorMessageNumber.REWRITE_1005)
                                .addArguments(name)
                                .build());

            this.protocolTagsSwitchedOn.remove(((ProtocolTypeDecl) type).toString());

        }

        return null;

    }

    @Override
    public final Void visitSwitchLabel(final SwitchLabel switchLabel) throws Phase.Error {

        // TODO: We migrated NameChecker Error 421 here, but we're already checking for that in SwitchStat
        Log.log(switchLabel.line + ": Visiting SwitchLabel (" + switchLabel.getExpression() + ").");

        // Initialize a handle to the Context
        final SymbolMap.Context context = this.getScope().getContext();

        // Assert the Context is a SwitchStatement
        if(!(context instanceof SwitchStat))
            throw new InvalidSwitchLabelContextException(this, context);

        // We only check on non-default labels
        if(!switchLabel.isDefault()) {

            // Initialize a handle to the Evaluation Expression & its Type
            final Expression evaluationExpression   = ((SwitchStat) context).getEvaluationExpression();
            final Type       type                   = evaluationExpression.getType();

            // If the Evaluation Expression is bound to an integral or String Type
            if(type.isIntegralType() || type.isStringType()) {

                // Resolve the Switch Label's Expression
                switchLabel.getExpression().visit(this);

                // Initialize a handle to the Type
                final Type switchLabelType = switchLabel.getExpression().getType();

                // Assert the constant Expression's Type is assignable to the Evaluation Expression's Type
                if(!(type.typeAssignmentCompatible(switchLabelType)))
                    throw new InvalidSwitchLabelExpressionTypeException(
                            this, evaluationExpression, switchLabelType, type).commit();

            // Otherwise, the Evaluation Expression should be a Protocol Type
            } else if(switchLabel.getExpression() instanceof NameExpr) {

                // Initialize a handle to the Tag
                // TODO: We should probably use properly qualified names here - what if there are two different protocols named the same ?
                final String            tag                     = switchLabel.getExpression().toString();
                final ProtocolTypeDecl  protocolTypeDeclaration = (ProtocolTypeDecl) type;
                final ProtocolCase      protocolCase            = protocolTypeDeclaration.getCase(tag);

                // Assert a valid Protocol Case
                if(protocolCase == null)
                    throw new UndefinedTagException(this, tag, protocolTypeDeclaration).commit();

                // Aggregate to the switched protocol tags
                this.protocolTagsSwitchedOn.put(protocolTypeDeclaration.toString(), protocolCase);

            // TODO: This is most likely dead code since Protocol Types for evaluation Expressions are checked above.
            // TODO: Test for this to make sure we don't or do arrive here
            } else throw
                    new SwitchLabelExpressionNotProtocolTagException(this, switchLabel.getExpression()).commit();

        }

        return null;

    }

    @Override
    public final Void visitSwitchStat(final SwitchStat switchStatement) throws Phase.Error {

        // Resolve the evaluation Expression
        switchStatement.getEvaluationExpression().visit(this);

        // Initialize a handle to the evaluation Expression's Type
        final Type type = switchStatement.getEvaluationExpression().getType();

        // Assert the evaluation Expression's bound Type is an integral, string, or Protocol Type (Switch on Tag)
        // TODO: This checks for Error 421
        if(!(type instanceof ProtocolTypeDecl || type.isIntegralType() || type.isStringType()))
            throw new InvalidSwitchStatementExpressionTypeException(this, type).commit();

        // Protocol Type; Check for illegally nested Switch Statements
        // TODO: Should we do this in with SymbolMap.Context instead?
        if(type instanceof ProtocolTypeDecl) {

            // Assert we haven't already switched on the current Protocol
            if(this.protocolsSwitchedOn.contains(type.toString()))
                throw new IllegalNestedSwitchInProtocolException(this, (ProtocolTypeDecl) type).commit();

            // Aggregate the Protocol Name
            this.protocolsSwitchedOn.add(type.toString());

        }

        // Resolve the children
        switchStatement.switchBlocks().visit(this);

        // Remove the Protocol Type, if any
        if(this.protocolsSwitchedOn.contains(type.toString())) this.protocolsSwitchedOn.remove(type.toString());

        return null;

    }

    @Override
    public final Void visitSuspendStat(final SuspendStat suspendStatement) throws Phase.Error {

        Log.log(suspendStatement.line + ": Visiting a suspend statement.");

        // Initialize a handle to the current Context
        final SymbolMap.Context context = this.getScope().getContext();

        // Assert the Context is a Procedure
        if(!(context instanceof ProcTypeDecl))
            throw new InvalidSuspendStatementContextException(this, context).commit();

        if(!((ProcTypeDecl) context).isMobile())
            throw new SuspendInNonMobileProcedureException(this, context).commit();

        return null;

    }

    @Override
    public final Void visitSyncStat(final SyncStat syncStatement) throws Phase.Error {

        // Resolve the barrier expression
        syncStatement.barrier().visit(this);

        // Initialize a handle to the barrier Expression's Type
        final Type type = syncStatement.barrier().getType();

        // Assert the Expression is bound to a Barrier Type
        if(!type.isBarrierType())
            throw new InvalidSynchronizationExpressionTypeException(this, type).commit();

        return null;

    }

    /**
     * <p>Verifies that the {@link TimeoutStat}'s timer {@link Expression} is a primitive timer {@link Type} & that
     * the delay {@link Expression} is an integral {@link Type}.</p>
     * @param timeoutStatement The {@link TimeoutStat} to verify.
     * @since 0.1.0
     */
    @Override
    public final Void visitTimeoutStat(final TimeoutStat timeoutStatement) throws Phase.Error {

        Log.log(timeoutStatement.line + ": visiting a timeout statement.");

        // Retrieve a handle to the timer & delay expressions
        final Expression timerExpression = timeoutStatement.getTimerExpression();
        final Expression delayExpression = timeoutStatement.getDelayExpression();

        // Resolve the timer Expression first
        timerExpression.visit(this);

        // Initialize a handle to the timer & delay Types
        final Type timerType = timerExpression.getType();
        final Type delayType = timerExpression.getType();

        // Assert that the timer Expression is a Timer Type
        if(!timerType.isTimerType())
            throw new InvalidTimeoutTargetTypeException(this, timerType).commit();

        // Resolve the delay Expression
        delayExpression.visit(this);

        // Assert that the delay Expression is an integral type
        if(!delayType.isIntegralType())
            throw new InvalidTimeoutDelayTypeException(this, delayType).commit();

        return null;

    }

    @Override
    public final Void visitInvocation(final Invocation invocation) throws Phase.Error {

        Log.log(invocation.line + ": visiting invocation (" + invocation+ ")");

        // Invocation Parameter Types should be bound already, attempt to retrieve the aggregated results
        final List<Object>          candidates = this.getScope().get(invocation.getProcedureName());
        final List<ProcTypeDecl>    compatible = new ArrayList<>();

        // Aggregate all valid results
        candidates.forEach(result -> { if(result instanceof SymbolMap)
            { aggregateAssignmentCompatible((SymbolMap) result, invocation, compatible); }});

        FancyPrint(invocation, compatible, 1, 1);

        // Assert we have at least one candidate
        if(compatible.size() == 0)
            throw new NoCandidateForInvocationFoundException(this, invocation).commit();

        // Attempt to resolve a Candidate
        final ProcTypeDecl candidate = Candidate(compatible);

        // Assert the candidate was resolved
        if(candidate == null)
            throw new AmbiguousInvocationException(this, invocation).commit();

        // Bind the Type
        invocation.setType(candidate.getReturnType());

        Log.log(invocation.line + ": invocation has type: " + invocation.getType());

        return null;

    }

    @Override
    public final Void visitNewArray(final NewArray newArray) throws Phase.Error {

        Log.log(newArray.line + ": Visiting a NewArray " + newArray.getBracketExpressions().size() + " " + newArray.dims().size());

        // Resolve the bracket Expressions
        newArray.getBracketExpressions().visit(this);

        // Assert each bracket Expression is an integral Type
        newArray.forEachBracketExpression(expression -> {

            // Assert the Expression is bound to an integral Type
            if(!expression.getType().isIntegralType())
                throw new InvalidArrayDimensionTypeException(this, expression);

        });

        // Initialize a handle to the depth & the synthesized ArrayType
        final int       depth     = newArray.getDepth();
        final ArrayType arrayType = new ArrayType(newArray.getComponentType(), depth);

        // If the New Array Expression is defined with an initializer with a depth greater
        // than 0; no error otherwise since any array can hold an empty array.
        if(newArray.definesLiteralExpression() && (newArray.getInitializationExpression().getDepth() > 0)) {

            // Initialize a handle to the ArrayLiteral Expression & the Synthesized Array Type
            final ArrayLiteral arrayLiteral = newArray.getInitializationExpression();

            // Iterate through each Expression contained in the ArrayLiteral Expression
            for(final Expression expression: arrayLiteral.getExpressions()) {

                // Resolve the Expression
                expression.visit(this);

                // Initialize a handle to the Expression's Type
                final Type type = expression.getType();

                // Assert an ArrayType is bound to the Expression
                if(!(type instanceof ArrayType))
                    throw new TypeNotAssignmentCompatibleException(this, expression, arrayType);

                // Assert assignment compatibility
                if(!arrayType.typeAssignmentCompatible(type))
                    throw new TypeNotAssignmentCompatibleException(this, expression, arrayType);

            }

            // Bind the ArrayLiteral's Type
            arrayLiteral.setType(arrayType);

        }

        // Bind the ArrayType
        newArray.setType(arrayType);

        Log.log(newArray.line + ": NewArray type is " + newArray.getType());

        return null;

    }

    @Override
    public final Void visitTernary(Ternary ternaryExpression) throws Phase.Error {
        // e ? t : f
        // Primitive?(Type(t)) & Primitive?(Type(f)) & (Type(t) :=T Type(f) || Type(f)
        // :=T Type(t)) =>
        // Type(e ? t : f) = ceiling(Type(t), Type(f))
        Log.log(ternaryExpression.line + ": Visiting a ternary expression");

        // Resolve the Expression's Type
        ternaryExpression.getEvaluationExpression().visit(this);

        // Initialize a handle to the Expression's Type
        final Type expressionType = ternaryExpression.getEvaluationExpression().getType();

        // Assert that the Expression is bound to a boolean Type
        // Error 3070
        if(!expressionType.isBooleanType())
            throw new ControlEvaluationExpressionNonBooleanTypeException(this, expressionType).commit();

        // Resolve the then & else branches
        ternaryExpression.thenPart().visit(this);
        ternaryExpression.elsePart().visit(this);

        // Initialize a handle to the then & else part
        final Type thenType  = ternaryExpression.thenPart().getType();
        final Type elseType  = ternaryExpression.elsePart().getType();

        // TODO: Watch out for ArrayType, ProtocolType, & RecordTypes
        // TODO: Error 3071 & 3072
        // Assert the then part is assignment compatible with the else part
        if(!thenType.typeAssignmentCompatible(elseType)) {

            // Assert the else part is assignment compatible with the then part
            if(!elseType.typeAssignmentCompatible(thenType))
                throw new TypeNotAssignmentCompatibleException(this, ternaryExpression.thenPart(), elseType).commit();

            // Bind the Type
            ternaryExpression.setType(elseType);

        // Assert the else part is assignment compatible with the then part
        } else {

            // Assert the then part is assignment compatible with the else part
            if(!thenType.typeAssignmentCompatible(elseType))
                throw new TypeNotAssignmentCompatibleException(this, ternaryExpression.elsePart(), thenType).commit();

            // Bind the Type
            ternaryExpression.setType(thenType);

        }

        Log.log(ternaryExpression.line + ": Ternary has type: " + ternaryExpression.type);

        return null;

    }

    @Override
    public final Void visitAssignment(final Assignment assignment) throws Phase.Error {
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
        Log.log(assignment.line + ": Visiting an assignment");

        // Resolve both sides
        assignment.left().visit(this);
        assignment.getRight().visit(this);

        // Initialize a handle to each side's Type
        final Type leftType     = assignment.left().getType();
        final Type rightType    = assignment.getRight().getType();

        // Assert the left hand side is assignable
        // TODO: Error 630
        if(!leftType.assignable())
            throw new TypeNotAssignmentCompatibleException(this, assignment.left(), rightType).commit();

        // Resolve the Operators
        switch(assignment.getOperator()) {

            case Assignment.EQ: {

                if(!leftType.typeAssignmentCompatible(rightType))
                    throw new TypeNotAssignmentCompatibleException(this, assignment.left(), rightType);

            } break;

            case Assignment.PLUSEQ: {

                if(leftType.isStringType()
                        && !rightType.isNumericType()
                        && !rightType.isBooleanType()
                        && !rightType.isCharType()
                        && !rightType.isStringType())
                    throw new TypeNotAssignmentCompatibleException(this, assignment.left(), rightType).commit();

                break;

            }
            case Assignment.MULTEQ:
            case Assignment.DIVEQ:
            case Assignment.MODEQ:
            case Assignment.MINUSEQ: {

                if(!leftType.typeAssignmentCompatible(rightType))
                    throw new TypeNotAssignmentCompatibleException(this, assignment.left(), rightType).commit();

            } break;

            case Assignment.LSHIFTEQ:
            case Assignment.RSHIFTEQ:
            case Assignment.RRSHIFTEQ: {

                if(!leftType.isIntegralType())
                    throw new LeftSideOfShiftNotIntegralOrBoolean(this, leftType);

                if(!rightType.isIntegralType())
                    throw new RightSideOfShiftNotIntegralOrBoolean(this, rightType);

            } break;

            case Assignment.ANDEQ:
            case Assignment.OREQ:
            case Assignment.XOREQ: {

                // TODO: Error 3009
                if(!(leftType.isIntegralType() && rightType.isIntegralType())
                        || (leftType.isBooleanType() && rightType.isBooleanType()))
                    throw new CompoundBitwiseTypesNotIntegralOrBoolean(this, leftType, rightType).commit();

            } break;

        }

        // Bind the Assignment Expression's Type to the Left hand side's Type
        assignment.setType(leftType);

        Log.log(assignment.line + ": Assignment has type: " + assignment.getType());

        return null;

    }

    @Override
    public final Void visitBinaryExpr(final BinaryExpr binaryExpression) throws Phase.Error {
        Log.log(binaryExpression.line + ": Visiting a Binary Expression");

        // Resolve the left & right hand sides
        binaryExpression.left().visit(this);
        binaryExpression.right().visit(this);

        final Type leftType   = binaryExpression.left().getType();
        final Type rightType  = binaryExpression.right().getType();

        final Type resultType;

        switch (binaryExpression.op()) {

            case BinaryExpr.LT:
            case BinaryExpr.GT:
            case BinaryExpr.LTEQ:
            case BinaryExpr.GTEQ: {

                if(!leftType.isNumericType() && !rightType.isNumericType())
                    throw new RelationalOperatorRequiresNumericTypeException(this, leftType, rightType).commit();

                // Update the result type
                resultType = new PrimitiveType(PrimitiveType.BooleanKind);

            } break;

            case BinaryExpr.EQEQ:
            case BinaryExpr.NOTEQ: {

                // TODO: barriers, timers, procs, records and protocols
                // Assert the Types are equal or any combination of numeric Types
                if(!((leftType.typeEqual(rightType)) || (leftType.isNumericType() && rightType.isNumericType())))
                    throw (leftType.isVoidType() || rightType.isVoidType())
                            ? new VoidTypeUsedInLogicalComparisonException(this, leftType, rightType).commit()
                            : new LogicalComparisonTypeMismatchException(this, leftType, rightType).commit();

                // Update the result Type
                resultType = new PrimitiveType(PrimitiveType.BooleanKind);

            } break;

            case BinaryExpr.ANDAND:
            case BinaryExpr.OROR: {

                if(!leftType.isBooleanType() || rightType.isBooleanType())
                    throw new LogicalComparisonNotBooleanTypeException(this, leftType, rightType).commit();

                // Update the result Type
                resultType = new PrimitiveType(PrimitiveType.BooleanKind);

            } break;

            case BinaryExpr.AND:
            case BinaryExpr.OR:
            case BinaryExpr.XOR: {

                if(leftType.isBooleanType() && rightType.isBooleanType())
                    resultType = new PrimitiveType(PrimitiveType.BooleanKind);

                else if(leftType.isIntegralType() && rightType.isIntegralType())
                    resultType = leftType;

                else
                    throw new BinaryBitwiseTypesNotIntegralOrBoolean(this, leftType, rightType).commit();

            } break;

            // + - * / % : Type must be numeric
            case BinaryExpr.PLUS: {

                if(leftType.isStringType()
                    && !rightType.isNumericType()
                    && !rightType.isBooleanType()
                    && !rightType.isStringType()
                    && !rightType.isCharType())
                    throw new TypeNotAssignableException(this, binaryExpression.left(), rightType).commit();

                else if(rightType.isStringType()
                    && !leftType.isNumericType()
                    && !leftType.isBooleanType()
                    && !leftType.isStringType()
                    && !leftType.isCharType())
                    throw new TypeNotAssignableException(this, binaryExpression.right(), leftType).commit();

            }
            case BinaryExpr.MINUS:
            case BinaryExpr.MULT:
            case BinaryExpr.DIV:
            case BinaryExpr.MOD: {

                if(!leftType.isNumericType() || !rightType.isNumericType())
                    throw new ArithmeticOperatorRequiresNumericTypeException(this, leftType, rightType).commit();

                // Update the result Type
                resultType = leftType;

            } break;

            case BinaryExpr.LSHIFT:
            case BinaryExpr.RSHIFT:
            case BinaryExpr.RRSHIFT: {

                if(!leftType.isIntegralType())
                    throw new LeftSideOfShiftNotIntegralOrBoolean(this, leftType);

                if(!rightType.isIntegralType())
                    throw new RightSideOfShiftNotIntegralOrBoolean(this, rightType);

                // Update the result type
                resultType = leftType;

            } break;

            default: resultType = new ErrorType();

        }

        if(!resultType.isBooleanType()) {

            // Retrieve the Type ceiling
            Type ceiling = ((PrimitiveType) leftType).typeCeiling((PrimitiveType) rightType);

            // Promote if necessary
            if (ceiling.isByteType() || ceiling.isShortType() || ceiling.isCharType())
                ceiling = new PrimitiveType(PrimitiveType.IntKind);

            // Bind the Type
            binaryExpression.setType(ceiling);

        } else binaryExpression.setType(resultType);

        Log.log(binaryExpression.line + ": Binary Expression has type: " + binaryExpression.getType());

        return null;

    }

    @Override
    public final Void visitUnaryPostExpr(final UnaryPostExpr unaryPostExpression) throws Phase.Error {

        Log.log(unaryPostExpression.line + ": Visiting a unary post expression");

        // Resolve the Expression
        unaryPostExpression.getExpression().visit(this);

        // Retrieve a handle to the type
        final Type expressionType = unaryPostExpression.getExpression().getType();

        // Assert the expression is bound to a numeric Type
        if(!expressionType.isNumericType())
            throw new InvalidUnaryOperandException(this, unaryPostExpression, unaryPostExpression.opString()).commit();

        // TODO: what about protocol ?? Must be inside the appropriate case.protocol access
        if(!(unaryPostExpression.getExpression() instanceof NameExpr)
                && !(unaryPostExpression.getExpression() instanceof RecordAccess)
                && !(unaryPostExpression.getExpression() instanceof ArrayAccessExpr))
            throw new InvalidLiteralUnaryOperandException(this, unaryPostExpression).commit();

        // Bind the Type
        unaryPostExpression.setType(expressionType);

        Log.log(unaryPostExpression.line + ": Unary Post Expression has type: " + expressionType);

        return null;

    }

    @Override
    public final Void visitUnaryPreExpr(final UnaryPreExpr unaryPreExpression) throws Phase.Error {

        Log.log(unaryPreExpression.line + ": Visiting a unary pre expression");

        // Resolve the Expression
        unaryPreExpression.getExpression().visit(this);

        // Initialize a handle to the Expression's Type
        final Type expressionType = unaryPreExpression.getType();

        switch(unaryPreExpression.getOperator()) {

            case UnaryPreExpr.PLUS:
            case UnaryPreExpr.MINUS:

                // TODO: Error 3052
                if(!expressionType.isNumericType())
                    throw new InvalidUnaryOperandException(
                            this, unaryPreExpression, unaryPreExpression.opString()).commit();

                break;

            case UnaryPreExpr.NOT:

                // TODO: Error 3053
                if(!expressionType.isBooleanType())
                    throw new InvalidUnaryOperandException(
                            this, unaryPreExpression, unaryPreExpression.opString()).commit();

                break;

            case UnaryPreExpr.COMP:

                // TODO: Error 3054
                if(!expressionType.isIntegralType())
                    throw new InvalidUnaryOperandException(
                            this, unaryPreExpression, unaryPreExpression.opString()).commit();

                break;

            case UnaryPreExpr.PLUSPLUS:
            case UnaryPreExpr.MINUSMINUS:

                // TODO: protocol access Error 3057
                if(!(unaryPreExpression.getExpression() instanceof NameExpr)
                        && !(unaryPreExpression.getExpression() instanceof RecordAccess)
                        && !(unaryPreExpression.getExpression() instanceof ArrayAccessExpr))
                    throw new InvalidLiteralUnaryOperandException(this, unaryPreExpression).commit();

                // TODO: Error 3057
                if (!expressionType.isNumericType())
                    throw new InvalidUnaryOperandException(this, unaryPreExpression, unaryPreExpression.opString()).commit();

                break;

        }

        // Bind the Type
        unaryPreExpression.setType(expressionType);

        Log.log(unaryPreExpression.line + ": Unary Pre Expression has type: " + expressionType);

        return null;

    }

    @Override
    public final Void visitChannelEndExpr(final ChannelEndExpr channelEndExpression) throws Phase.Error {

        Log.log(channelEndExpression.line + ": Visiting a channel end expression.");

        // Resolve the Channel Type
        channelEndExpression.getChannelType().visit(this);

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
            synthesized = new ChannelEndType(ChannelEndType.NOT_SHARED, channelType.getComponentType(), end);

        // Channel has both ends shared, create a shared ChannelEndType.
        else if (channelType.isShared() == ChannelType.SHARED_READ_WRITE)
            synthesized = new ChannelEndType(ChannelEndType.SHARED, channelType.getComponentType(), end);

        // Channel has read end shared; if .read then create a shared channel end, otherwise create a non-shared one.
        else if (channelType.isShared() == ChannelType.SHARED_READ) {

            final int share = (channelEndExpression.isRead() && (channelType.isShared() == ChannelType.SHARED_READ))
                    ? ChannelEndType.SHARED : ChannelType.NOT_SHARED;

            // Set the Synthesized Type
            synthesized = new ChannelEndType(share, channelType.getComponentType(), end);

        } else if(channelType.isShared() == ChannelType.SHARED_WRITE) {

            final int share = (channelEndExpression.isWrite() && (channelType.isShared() == ChannelType.SHARED_WRITE))
                    ? ChannelEndType.SHARED : ChannelType.NOT_SHARED;

            // Set the Synthesized Type
            synthesized = new ChannelEndType(share, channelType.getComponentType(), end);

        }

        // Bind the Type
        channelEndExpression.setType(synthesized);

        Log.log(channelEndExpression.line + ": Channel End Expr has type: " + channelEndExpression.getType());

        return null;

    }

    @Override
    public final Void visitChannelReadExpr(final ChannelReadExpr channelReadExpression) throws Phase.Error {

        Log.log(channelReadExpression.line + ": Visiting a channel read expression.");

        // Resolve the Channel Type
        channelReadExpression.getExpression().visit(this);

        // Initialize a handle to the Type
        final Type type = channelReadExpression.getType();

        // Assert the ChannelReadExpression is bound to the appropriate Type
        if(!(type.isTimerType() || type instanceof ChannelEndType || type instanceof ChannelType))
            throw new InvalidChannelReadExpressionTypeException(this, channelReadExpression).commit();

        if(type instanceof ChannelEndType)
            channelReadExpression.setType(((ChannelEndType) type).getComponentType());

        else if(type instanceof ChannelType)
            channelReadExpression.setType(((ChannelType) type).getComponentType());

        else channelReadExpression.setType(new PrimitiveType(PrimitiveType.LongKind));

        // Assert the ChannelReadExpression defines an extended rendezvous
        if(channelReadExpression.definesExtendedRendezvous()) {

            // Assert that timer reads do not have an extended rendezvous.
            if(type.isTimerType())
                throw new TimerReadWithExtendedRendezvous(this, channelReadExpression).commit();

            // Otherwise, resolve the extended rendezvous
            channelReadExpression.getExtendedRendezvous().visit(this);

        }

        Log.log(channelReadExpression.line + ": Channel read expression has type: " + channelReadExpression.getType());

        return null;

    }

    @Override
    public final Void visitArrayAccessExpr(final ArrayAccessExpr arrayAccessExpression) throws Phase.Error {
        // ArrayAccessExpr
        // Syntax: Expr1[Expr2]
        // Expr1 must be array type and Expr2 my be integer.
        // Array?(T(Expr1)) /\ Integer?(T(Expr2))
        // If T(Expr1) = Array(BaseType) => T(Expr1[Expr2]) := BaseType
        Log.log(arrayAccessExpression.line + ": Visiting ArrayAccessExpr");

        // Resolve the Target Expression
        arrayAccessExpression.targetExpression().visit(this);

        // Initialize a handle to the target Expression's Type
        final Type targetType = arrayAccessExpression.targetExpression().getType();

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

        Log.log(arrayAccessExpression.line + ": ArrayAccessExpr has type " + arrayAccessExpression.type);

        // Resolve the index Type
        arrayAccessExpression.indexExpression().visit(this);

        // Initialize a handle to the Index Type
        Type indexType = arrayAccessExpression.indexExpression().getType();

        // This error does not create an error type cause the baseType() is still the array expression's type.
        // TODO: Error 655
        if(!indexType.isIntegerType())
            throw new InvalidArrayDimensionTypeException(this, arrayAccessExpression).commit();

        Log.log(arrayAccessExpression.line + ": Array Expression has type: " + arrayAccessExpression.type);

        return null;

    }

    @Override
    public final Void visitRecordAccess(final RecordAccess recordAccessExpression) throws Phase.Error {

        Log.log(recordAccessExpression.line + ": visiting a record access expression (" + recordAccessExpression.field() + ")");

        // Resolve the Record
        recordAccessExpression.getTarget().visit(this);

        // Initialize a handle to the Type
        final Type type = recordAccessExpression.getType();

        // TODO: size of strings.... size()? size? or length? for now: size()

        if((type instanceof ArrayType) && recordAccessExpression.field().toString().equals("size")) {

            recordAccessExpression.setType(new PrimitiveType(PrimitiveType.IntKind));
            recordAccessExpression.isArraySize = true;

        } else if(type.isStringType() && recordAccessExpression.field().toString().equals("length")) {

            recordAccessExpression.setType(new PrimitiveType(PrimitiveType.LongKind));
            recordAccessExpression.isStringLength = true;

        } else {

            // Assert the Type is a Record Type Declaration
            if(type instanceof RecordTypeDecl) {

                // Find the field and make the type of the record access equal to the field. TODO: test inheritence here
                final RecordMember recordMember =
                        ((RecordTypeDecl) type).getMember(recordAccessExpression.field().toString());

                // TODO: Error 3062
                if(recordMember == null)
                    throw new UndefinedSymbolException(this, recordAccessExpression).commit();

                // Bind the Type
                recordAccessExpression.setType(recordMember.getType());

            // Otherwise, it's Protocol Type
            } else {

                final ProtocolTypeDecl protocolTypeDeclaration = (ProtocolTypeDecl) type;

                // TODO: Error Format: "Illegal access to non-switched protocol type '" + protocolTypeDeclaration + "'."
                if(!protocolsSwitchedOn.contains(protocolTypeDeclaration.toString()))
                    throw new IllegalNestedSwitchInProtocolException(this, protocolTypeDeclaration).commit();

                final ProtocolCase protocolCase = protocolTagsSwitchedOn.get(protocolTypeDeclaration.toString());
                final String fieldName = recordAccessExpression.field().toString();

                boolean found = false;

                if(protocolCase != null)
                    for(RecordMember recordMember: protocolCase.getBody())
                        if(recordMember.getName().toString().equals(fieldName)) {

                            recordAccessExpression.setType(recordMember.getType());
                            found = true;

                        }

                // TODO: Error 3073
                // TODO: Format: "Unknown field reference '" + fieldName + "' in protocol tag '"
                //                            + protocolTypeDeclaration + "' in protocol '" + protocolTypeDeclaration + "'."
                if(!found)
                    throw new UndefinedSymbolException(this, recordAccessExpression).commit();

            }

        }

        Log.log(recordAccessExpression.line + ": record access expression has type: " + recordAccessExpression.type);

        return null;

    }

    @Override
    public final Void visitCastExpr(CastExpr ce) throws Phase.Error {

        Log.log(ce.line + ": Visiting a cast expression");

        // Resolve the expression
        ce.getExpression().visit(this);

        // Initialize a handle to the expression & cast Type
        final Type expressionType   = ce.getExpression().getType();
        final Type castType         = ce.getCastType();

        if(expressionType.isNumericType() && castType.isNumericType()) {

            ce.type = castType;

        } else if (expressionType instanceof ProtocolTypeDecl && castType instanceof ProtocolTypeDecl) {

            // TODO: finish this

        } else if (expressionType instanceof RecordTypeDecl && castType instanceof RecordTypeDecl) {

            // TODO: finish this

        } else {

            // Turns out that casts like this are illegal:
            // int a[][];
            // double b[][];
            // a = (int[][])b;
            // b = (double[][])a;
            ce.type = new ErrorType();
            // !!Error: Illegal cast of value of type exprType to castType.

        }

        Log.log(ce.line + ": Cast Expression has type: " + ce.type);

        return null;

    }

    @Override
    public final Void visitNameExpr(final NameExpr nameExpression) throws Phase.Error {

        Log.log(nameExpression.line + ": Visiting a Name Expression (" + nameExpression + ").");

        // One of the few terminal points, attempt to retrieve the type from our current scope
        final Object resolved = this.getScope().get(nameExpression.getName().getName(), nameExpression.getPackageName());

        // Assert that our result is a Type
        if(!(resolved instanceof Type))
            throw new UndefinedSymbolException(this, nameExpression).commit();

        // Otherwise, bind the Type
        nameExpression.setType((Type) resolved);

        Log.log(nameExpression.line + ": Name Expression (" + nameExpression + ") has type: " + nameExpression.type);

        return null;

    }

    @Override
    public final Void visitArrayLiteral(ArrayLiteral arrayLiteral) throws Phase.Error {

        Log.log(arrayLiteral.line + ": visiting an array literal.");

        return null;

    }

    @Override
    public final Void visitProtocolLiteral(ProtocolLiteral pl) throws Phase.Error {

        Log.log(pl.line + ": Visiting a protocol literal");

        // TODO: be careful here if a protocol type extends another protocol type, then the
        // record literal must contains expressions for that part too!!!

        return null;

    }

    @Override
    public final Void visitRecordLiteral(RecordLiteral rl) throws Phase.Error {

        Log.log(rl.line + ": visiting a record literal (" + rl + ").");
        //RecordTypeDecl rt = rl.myTypeDecl;

        // TODO: be careful here if a record type extends another record type, then the
        // record literal must contains expressions for that part too!!!

        return null;

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
        private final ConstantDecl constantDeclaration;

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
                                                       final ConstantDecl constantDeclaration) {
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
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link AltCase}'s precondition
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
         * <p>The {@link AltCase} with a non-boolean {@link Type} precondition.</p>
         */
        private final AltCase altCase;

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
                                                           final AltCase altCase) {
            super(culpritInstance);
            this.altCase = altCase;
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
         * <p>The {@link ChannelWriteStat} bound to a  a non-{@link ChannelEndType}.</p>
         */
        private final ChannelWriteStat channelWriteStat;

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
                                                           final ChannelWriteStat channelWriteStat) {
            super(culpritInstance);
            this.channelWriteStat = channelWriteStat;
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
         * <p>The {@link ChannelWriteStat} bound to a  a non-{@link ChannelEndType}.</p>
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
         * <p>The {@link ForStat} enrolled on addition barriers.</p>
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
         * <p>The {@link SymbolMap.Context} the {@link ReturnStat} appreared in.</p>
         */
        private final SymbolMap.Context returnStatementContext;

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
                                                         final SymbolMap.Context context) {
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
         * <p>The {@link SymbolMap.Context} the {@link SwitchLabel} appeared in.</p>
         */
        private final SymbolMap.Context switchLabelContext;

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
                                                     final SymbolMap.Context context) {
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
         * <p>The {@link SymbolMap.Context} the {@link SuspendStat} appeared in.</p>
         */
        private final SymbolMap.Context suspendStatementContext;

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
                                                          final SymbolMap.Context context) {
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
         * <p>The {@link SymbolMap.Context} the {@link ReturnStat} appreared in.</p>
         */
        private final SymbolMap.Context returnStatementContext;

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
                                                   final SymbolMap.Context context) {
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
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link SwitchStat}'s evaluation
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
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link SwitchLabel}'s
     * {@link Expression}'s {@link Type} not assignable to the {@link SwitchStat}'s Evaluation {@link Expression}'s
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
         * <p>The {@link SwitchLabel}'s {@link Expression}.</p>
         */
        private final Expression switchLabelExpression;

        /**
         * <p>The {@link SwitchLabel}'s {@link Type}.</p>
         */
        private final Type labelType;

        /**
         * <p>The {@link SwitchStat}'s evaluation {@link Expression}'s {@link Type}.</p>
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
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link SwitchLabel}'s
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
         * <p>The {@link SwitchLabel}'s {@link Expression}.</p>
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
     * {@link ProtocolTypeDecl}'s Tag.</p>
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
         * <p>The {@link String} value of the undefined {@link ProtocolTypeDecl}'s Tag.</p>
         */
        private final String tag;

        /**
         * <p>The {@link ProtocolTypeDecl} searched.</p>
         */
        private final ProtocolTypeDecl protocolTypeDeclaration;

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
                                        final ProtocolTypeDecl protocolTypeDeclaration) {
            super(culpritInstance);

            this.tag                        = tag                       ;
            this.protocolTypeDeclaration    = protocolTypeDeclaration   ;

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
            return String.format(Message, this.tag, this.protocolTypeDeclaration);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an illegally nested
     * {@link SwitchStat} within a {@link ProtocolTypeDecl}.</p>
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
         * <p>The {@link ProtocolTypeDecl} with an illegally nested {@link SwitchStat}.</p>
         */
        private final ProtocolTypeDecl protocolTypeDeclaration;

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
                                                         final ProtocolTypeDecl protocolTypeDeclaration) {
            super(culpritInstance);

            this.protocolTypeDeclaration = protocolTypeDeclaration   ;

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
            return String.format(Message, this.protocolTypeDeclaration);

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
         * <p>The {@link SymbolMap.Context} the {@link SuspendStat} appeared in.</p>
         */
        private final SymbolMap.Context suspendStatementContext;

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
                                                          final SymbolMap.Context context) {
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
     * {@link Type}-bounded {@link SyncStat}.</p>
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
         * <p>The {@link Type} bound to the {@link SyncStat}.</p>
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
     * {@link Type}-bounded {@link TimeoutStat} target {@link Type}.</p>
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
         * <p>The {@link Type} bound to the {@link TimeoutStat}.</p>
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
     * {@link Type}-bounded {@link TimeoutStat} delay {@link Type}.</p>
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
         * <p>The {@link Type} bound to the {@link TimeoutStat} delay {@link Expression}.</p>
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
     * {@link Type}-bounded {@link Invocation}.</p>
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
         * <p>The {@link Invocation} that attempted to resolve.</p>
         */
        private final Invocation invocation;

        /**
         * <p>The {@link Type} resolved from an {@link Invocation}'s name.</p>
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
                                                   final Invocation invocation,
                                                   final Type resolvedType) {
            super(culpritInstance);
            this.invocation   = invocation          ;
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
            return String.format(Message, this.invocation, this.resolvedType);

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
         * <p>The {@link Invocation} that attempted to resolve.</p>
         */
        private final Invocation invocation;

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
                                                        final Invocation invocation) {
            super(culpritInstance);
            this.invocation   = invocation          ;
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
            return String.format(Message, this.invocation);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an invocation that
     * couldn't resolve to a {@link ProcTypeDecl}.</p>
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
         * <p>The {@link Invocation} that attempted to resolve.</p>
         */
        private final Invocation invocation;

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
                                               final Invocation invocation) {
            super(culpritInstance);
            this.invocation   = invocation          ;
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
            return String.format(Message, this.invocation);

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
     * {@link BinaryExpr} where either side does not have a numeric {@link Type} bound.</p>
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
     * {@link BinaryExpr} where either side has a void {@link Type} bound.</p>
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
     * {@link BinaryExpr} where the left and right hand side {@link Type}s do not match.</p>
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
     * {@link BinaryExpr} where the left and right hand side {@link Type}s are not bound to a boolean
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
     * {@link BinaryExpr} where either side does not have a numeric {@link Type} bound.</p>
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
     * {@link UnaryPostExpr} with an invalid operand.</p>
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
     * {@link UnaryPostExpr} with a Literal Operand.</p>
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
         * <p>The mis-typed {@link ChannelEndExpr}.</p>
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
                                                                    final ChannelEndExpr channelEndExpression) {
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
         * <p>The mis-typed {@link ChannelReadExpr}.</p>
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
         * <p>The mis-typed {@link ChannelReadExpr}.</p>
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

    private static boolean assertAssignmentCompatible(final ProcTypeDecl procedureTypeDeclaration,
                                                      final Invocation invocation) {

        // Assert they each have the same amount of parameters
        boolean areAssignmentCompatible =
                invocation.getParameterCount() == procedureTypeDeclaration.getParameterCount();

        // Iterate while the invariant is true
        for(int index = 0; areAssignmentCompatible && (index < invocation.getParameterCount()); index++) {

            // Initialize a handle to the Procedure Type's Parameter Type
            final Type parameterType    = procedureTypeDeclaration.getTypeForParameter(index);
            final Type invocationType   = invocation.getTypeForParameter(index);

            // Update the invariant
            areAssignmentCompatible = ((parameterType != null) && (invocationType != null))
                    && parameterType.typeAssignmentCompatible(invocationType);

        }

        // Return the result
        return areAssignmentCompatible;

    }

    private static boolean assertTypeAssignmentCompatible(final ProcTypeDecl leftProcedure,
                                                          final ProcTypeDecl rightProcedure) {

        // Assert they each have the same amount of parameters
        boolean areAssignmentCompatible =
                rightProcedure.getParameterCount() == leftProcedure.getParameterCount();

        // Iterate while the invariant is true
        for(int index = 0; areAssignmentCompatible && (index < rightProcedure.getParameterCount()); index++) {

            // Initialize a handle to the Procedure Type's Parameter Type
            final Type leftType    = leftProcedure.getTypeForParameter(index);
            final Type rightType   = rightProcedure.getTypeForParameter(index);

            // Update the invariant
            areAssignmentCompatible = ((leftType != null) && (rightType != null))
                    && leftType.typeAssignmentCompatible(rightType);

        }

        // Return the result
        return areAssignmentCompatible;

    }

    private static List<ProcTypeDecl> aggregateAssignmentCompatible(final SymbolMap symbolMap,
                                                                    final Invocation invocation,
                                                                    final List<ProcTypeDecl> result) {

        // Iterate through the SymbolMap
        symbolMap.forEachSymbol(symbol -> {

            // Assert the Symbol is a Procedure Type Declaration & its assignment compatible
            // with the specified Invocation
            if((symbol instanceof ProcTypeDecl)
                    && assertAssignmentCompatible((ProcTypeDecl) symbol, invocation))
                result.add((ProcTypeDecl) symbol);

        });

        // Return the result
        return result;

    }
    private static ProcTypeDecl Candidate(final List<ProcTypeDecl> candidates) {

        int remaining = candidates.size();

        for(int leftIndex = 0; leftIndex < candidates.size(); leftIndex++) {

            ProcTypeDecl leftProcedure = candidates.get(leftIndex);

            // If we should continue
            if(leftProcedure != null) {

                // Hide the current procedure
                candidates.set(leftIndex, null);

                // Iterate for the right hand side
                for(int rightIndex = 0; rightIndex < candidates.size(); rightIndex++) {

                    // Retrieve the right hand side
                    ProcTypeDecl rightProcedure = candidates.get(rightIndex);

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

        ProcTypeDecl result = null;

        if(remaining == 1) for(final ProcTypeDecl procedure: candidates)
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

    private static List<String> Signatures(final List<ProcTypeDecl> candidates) {

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

    private static void FancyPrint(final Invocation invocation, final List<ProcTypeDecl> candidates,
                                   final int leftPadding, final int rightPadding) {

        final String title = "Candidates for '" + invocation.getProcedureName() + "': ";

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
