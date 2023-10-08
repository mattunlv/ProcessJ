package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a control {@link Statement} that blocks the path of execution to the next immediate
 * {@link Statement} once the specified time has elapsed.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see ControlContext
 * @see Statement
 */
public class TimeoutStatement extends Statement implements ControlContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The timer {@link Expression} specifying the timer.</p>
     * @since 1.0.0
     * @see Expression
     */
    private Expression timerExpression    ;

    /**
     * <p>The {@link Expression} specifying an additional time delay.</p>
     * @since 1.0.0
     * @see Expression
     */
    private Expression delayExpression    ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link TimeoutStatement} with the specified timer {@link Expression} & delay
     * {@link Expression}.</p>
     * @param timerExpression The {@link Expression} specifying the timer.
     * @param delayExpression The {@link Expression} specifying an additional time delay.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link SyncStatement}.
     * @since 1.0.0
     * @see Expression
     * @see Token
     */
    public TimeoutStatement(final Expression timerExpression,
                            final Expression delayExpression,
                            final Token... tokens) {
        super(tokens);

        this.timerExpression    = timerExpression;
        this.delayExpression    = delayExpression;

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link Context}, and dispatches the {@link Visitor} to the contained timer &
     * delay {@link Expression}s via {@link Expression#accept(Visitor)} & {@link TimeoutStatement} via
     * {@link Visitor#visitSyncStatement(SyncStatement)} before updating the {@link Context} to the enclosing
     * {@link Context}.
     * @since 1.0.0
     * @see Visitor
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Dispatch the Timer Expression, if any
        if(this.timerExpression != null) this.timerExpression.accept(visitor);

        // Dispatch the delay Expression, if any
        if(this.delayExpression != null) this.delayExpression.accept(visitor);

        // Dispatch the Timeout Statement
        visitor.visitTimeoutStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the timer {@link Expression}.</p>
     * @return The timer {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final Expression getTimerExpression() {

        return this.timerExpression;

    }

    /**
     * <p>Returns the delay {@link Expression}.</p>
     * @return The delay {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final Expression getDelayExpression() {

        return this.delayExpression;

    }

    /**
     * <p>Mutates the {@link TimeoutStatement}'s timer {@link Expression}.</p>
     * @param timerExpression The desired timer {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final void setTimerExpression(final Expression timerExpression) {

        this.timerExpression = timerExpression;

    }

    /**
     * <p>Mutates the {@link TimeoutStatement}'s delay {@link Expression}.</p>
     * @param delayExpression The desired delay {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final void setDelayExpression(final Expression delayExpression) {

        this.delayExpression = delayExpression;

    }

}