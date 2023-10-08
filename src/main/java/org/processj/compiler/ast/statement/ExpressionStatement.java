package org.processj.compiler.ast.statement;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a {@link Statement} that contains an {@link Expression}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see Expression
 * @see Statement
 */
public class ExpressionStatement extends Statement {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Expression} bound to the {@link Statement}.</p>
     * @since 1.0.0
     * @see Expression
     */
    private final Expression expression;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ExpressionStatement} to its' default state with the specified {@link Expression}.</p>
     * @param expression The {@link Expression} to bind to the {@link ExpressionStatement}.
     * @since 1.0.0
     * @see Expression
     */
    public ExpressionStatement(final Expression expression) {
        super(expression);

        this.expression = expression;

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link Context}, and dispatches the {@link Visitor} to the {@link Expression}
     * via {@link Expression#accept(Visitor)}, & {@link ExpressionStatement} via
     * {@link Visitor#visitExpressionStatement(ExpressionStatement)} before updating the {@link Context} to the
     * enclosing {@link Context}.
     * @since 1.0.0
     * @see Visitor
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Dispatch the Expression
        this.expression.accept(visitor);

        // Dispatch the Sync Statement
        visitor.visitExpressionStatement(this);

        // Close the Context
        visitor.setContext(this.closeContext());

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the {@link ExpressionStatement}'s bound {@link Expression}.</p>
     * @return The {@link ExpressionStatement}'s bound {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final Expression getExpression() {

        return this.expression;

    }

    /**
     * <p>Returns a flag indicating if the {@link ExpressionStatement} has an {@link Expression} bound.</p>
     * @return A flag indicating if the {@link ExpressionStatement} has an {@link Expression} bound.
     * @since 1.0.0
     * @see Expression
     */
    public final boolean definesExpression() {

        return this.expression != null;

    }

}