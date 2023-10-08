package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a control {@link Statement} that blocks the path of execution to the next immediate
 * {@link Statement} once the barrier {@link Expression} has synchronized.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see ControlContext
 * @see Expression
 * @see Statement
 */
public class SyncStatement extends Statement implements ControlContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The barrier {@link Expression} that dictates the enclosing {@link Context}'s execution.</p>
     * @since 1.0.0
     * @see Context
     * @see Expression
     */
    private Expression barrierExpression;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link SyncStatement} with the specified barrier {@link Expression}.</p>
     * @param barrierExpression The {@link Expression} that dictates the enclosing {@link Context}'s execution.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link SyncStatement}.
     * @since 1.0.0
     * @see Expression
     * @see Token
     */
    public SyncStatement(final Expression barrierExpression, final Token... tokens) {
        super(tokens);

        this.barrierExpression = barrierExpression;

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link Context}, and dispatches the {@link Visitor} to the contained barrier
     * {@link Expression} via {@link Expression#accept(Visitor)} & {@link SyncStatement} via
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

        // Dispatch the Barrier Expression, if any
        if(this.barrierExpression != null) this.barrierExpression.accept(visitor);

        // Dispatch the Sync Statement
        visitor.visitSyncStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the {@link Expression} that dictates the enclosing {@link Context}'s execution.</p>
     * @return The {@link Expression} that dictates the enclosing {@link Context}'s execution.
     * @since 1.0.0
     * @see Expression
     */
    public final Expression getBarrierExpression() {

        return this.barrierExpression;

    }

    /**
     * <p>Mutates the {@link SyncStatement}'s barrier {@link Expression}.</p>
     * @param barrierExpression The desired barrier {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final void setBarrierExpression(final Expression barrierExpression) {

        this.barrierExpression  = barrierExpression;

    }

}