package org.processj.compiler.ast.statement.yielding;

import org.processj.compiler.ast.expression.binary.AssignmentExpression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.statement.ExpressionStatement;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that represents a {@link GuardStatement} {@link Statement} within an {@link AltCase}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca.
 * @version 1.0.0
 * @since 0.1.0
 * @see AltCase
 * @see Statement
 */
public class GuardStatement extends AST {

    /// --------------
    /// Private Fields

    /**
     * <p>The literal {@link GuardStatement} {@link Statement}.</p>
     */
    private final Statement statement;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link GuardStatement} to its' default state with the specified {@link Statement}.</p>
     * @param statement The literal {@link Statement} corresponding to the {@link GuardStatement}.
     * @since 0.1.0
     */
    public GuardStatement(final Statement statement) {
        super(new AST[] { statement });

        this.statement = statement;

    }

    /// ----------------
    /// java.lang.Object

    // TODO: Hashcode

    /**
     * <p>Returns a boolean flag indicating if the specified {@link GuardStatement} is identical to the
     * given {@link GuardStatement}. This method will check the super class invocation of {@link Object#equals(Object)},
     * & the instance of the specified {@link Object}.</p>
     * @param that The {@link Object} instance to check against the given instance.
     * @return boolean flag indicating if the specified instance is identical to this.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof GuardStatement)
                && this.statement.equals(((GuardStatement) that).statement);

    }

    /**
     * <p>Returns the {@link String} representation of the {@link GuardStatement}.</p>
     * @return {@link String} representation of the {@link GuardStatement}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.statement.toString();

    }

    /// --------------------
    /// org.processj.ast.AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link GuardStatement}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitGuardStatement(GuardStatement)} method.</p>
     *
     * @param visitor The {@link Visitor} to dispatch.
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        visitor.visitGuardStatement(this);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the {@link Statement} instance corresponding with the {@link GuardStatement}.</p>
     * @return {@link Statement} instance corresponding with the {@link GuardStatement}.
     * @since 0.1.0
     */
    public final Statement getStatement() {

        return this.statement;

    }

    public final Expression getExpression() {

        return (this.statement instanceof ExpressionStatement) ? ((ExpressionStatement) this.statement).getExpression() : null;

    }

    public final AssignmentExpression getInputExpression() {

        final Expression expression = (this.statement instanceof ExpressionStatement)
                ? ((ExpressionStatement) this.statement).getExpression() : null;

        return (expression instanceof AssignmentExpression) ? (AssignmentExpression) expression : null;

    }

    public final boolean isInputGuard() {

        return this.getInputExpression() != null;

    }

}