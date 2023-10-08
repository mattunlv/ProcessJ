package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.BarrierSet;
import org.processj.compiler.ast.statement.Statements;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

import java.util.*;

/**
 * <p>Class that encapsulates an iterative {@link Statement} that specifies initialization {@link Statement}s,
 * an evaluation {@link Expression}, & increment {@link Statement}s that executes as long as the evaluation
 * {@link Expression} holds true.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see BlockStatement
 * @see IterativeContext
 */
public class ForStatement extends BlockStatement implements IterativeContext {

    /// --------------
    /// Private Fields

    /**
     * <p>Flag indicating if the {@link ForStatement} is parallel.</p>
     * @since 1.0.0
     */
    public final  boolean           isParallel                  ;

    /**
     * <p>The set of specified initialization {@link Statements}.</p>
     * @since 1.0.0
     * @see Statements
     */
    private final Statements        initializationStatements    ;

    /**
     * <p>The evaluation {@link Expression} dictating execution of the {@link ForStatement}.</p>
     * @since 1.0.0
     * @see Expression
     */
    private final Expression        evaluationExpression        ;

    /**
     * <p>The set of specified increment {@link Statements}.</p>
     * @since 1.0.0
     * @see Statements
     */
    private final Statements        incrementStatements         ;

    /**
     * <p>{@link List} that is populated with {@link Expression}s contained in the parallel {@link ForStatement}
     * that alter states; e.g. assignment, pre & post increment/decrements.</p>
     * @since 1.0.0
     * @see List
     */
    public final List<Expression>   mutatedExpressions          ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ForStatement} to its' default state with the specified set of initialization
     * {@link Statements}, evaluation {@link Expression}, increment {@link Statements}, set of barrier
     * {@link Expression}s, {@link Statement} body & a flag indicating if the {@link ForStatement} is parallel.</p>
     * @param initializationStatements The set of {@link Statements} defining local variables for use in the
     *                                 {@link ForStatement}.
     * @param evaluationExpression The {@link Expression} dictating execution of the {@link ForStatement}.
     * @param incrementStatements The set of {@link Statements} that are executed after each evaluation of the
     *                            {@link ForStatement}.
     * @param barrierSet The set of barrier {@link Expression}s to enroll on the {@link ForStatement}.
     * @param body The {@link ForStatement}'s body;.
     * @param isParallel Flag indicating if the {@link ForStatement} is parallel.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link WhileStatement}.
     * @since 1.0.0
     * @see Statements
     * @see Statement
     * @see Expression
     * @see BarrierSet
     */
    public ForStatement(final Statements initializationStatements,
                        final Expression evaluationExpression,
                        final Statements incrementStatements ,
                        final BarrierSet barrierSet,
                        final Statement body,
                        final boolean isParallel,
                        final Token... tokens) {
        super(barrierSet, body, tokens);

        this.initializationStatements   = (initializationStatements != null) ? initializationStatements : new Statements()  ;
        this.incrementStatements        = (incrementStatements != null) ? incrementStatements : new Statements()  ;
        this.evaluationExpression       = evaluationExpression          ;
        this.isParallel                 = isParallel                    ;
        this.mutatedExpressions         = new ArrayList<>()             ;

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context} & scope, and dispatches the
     * {@link Visitor} to the initialization {@link Statements} via {@link Statements#accept(Visitor)}, evaluation
     * {@link Expression} via {@link Expression#accept(Visitor)}, increment {@link Statements} via
     * {@link Statements#accept(Visitor)}, the {@link ForStatement} via {@link Visitor#visitForStatement(ForStatement)},
     * & {@link Statement} children via {@link Statement#accept(Visitor)} before updating the
     * {@link org.processj.compiler.ast.Context} to the enclosing {@link Context}.
     * @since 1.0.0
     * @see Visitor
     * @see Statement
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope
        this.openScope();

        // Dispatch the initialization statements
        this.initializationStatements.accept(visitor);

        // Dispatch the evaluation Expression, if any
        if(this.evaluationExpression != null) this.evaluationExpression.accept(visitor);

        // Dispatch the increment statements
        this.incrementStatements.accept(visitor);

        // Dispatch the For Statement
        visitor.visitForStatement(this);

        // Dispatch the children
        this.getBody().accept(visitor);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// ----------------
    /// IterativeContext

    /**
     * <p>Returns the evaluation {@link Expression} dictating execution of the {@link ForStatement}.</p>
     * @return The evaluation {@link Expression} dictating execution of the {@link ForStatement}.
     * @since 1.0.0
     * @see Expression
     */
    @Override
    public final Expression getEvaluationExpression() {

        return this.evaluationExpression;

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link ForStatement} was specified as parallel.</p>
     * @return Flag indicating if the {@link ForStatement} was specified as parallel.
     * @since 1.0.0
     */
    public final boolean isParallel() {

        return this.isParallel;

    }

    /**
     * <p>Returns a flag indicating if the {@link ForStatement} was specified with initialization {@link Statements}.
     * </p>
     * @return A flag indicating if the {@link ForStatement} was specified with initialization {@link Statements}.
     * @since 1.0.0
     * @see Statements
     */
    public final boolean definesInitializationStatements() {

        return this.initializationStatements != null;

    }

    /**
     * <p>Returns a flag indicating if the {@link ForStatement} was specified with an evaluation {@link Expression}
     * .</p>
     * @return A flag indicating if the {@link ForStatement} was specified with an evaluation {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final boolean definesEvaluationExpression() {

        return this.evaluationExpression != null;

    }

    /**
     * <p>Returns a flag indicating if the {@link ForStatement} was specified with increment {@link Statements}.
     * </p>
     * @return A flag indicating if the {@link ForStatement} was specified with increment {@link Statements}.
     * @since 1.0.0
     * @see Statements
     */
    public final boolean definesIncrementStatements() {

        return this.incrementStatements != null;

    }

    /**
     * <p>Returns the set of initialization {@link Statements} specified with the {@link ForStatement}.</p>
     * @return The set of initialization {@link Statements} specified with the {@link ForStatement}.
     * @since 1.0.0
     * @see Statements
     */
    public final Statements getInitializationStatements() {

        return this.initializationStatements;

    }

    /**
     * <p>Returns the set of increment {@link Statements} specified with the {@link ForStatement}.</p>
     * @return The set of increment {@link Statements} specified with the {@link ForStatement}.
     * @since 1.0.0
     * @see Statements
     */
    public final Statements getIncrementStatements() {

        return this.incrementStatements;

    }

}