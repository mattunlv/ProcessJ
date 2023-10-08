package org.processj.compiler.ast.statement.yielding;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a declaration of a channel write {@link Statement} that may cause the enclosing
 * {@link Context} to yield.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see Context
 * @see YieldingContext
 * @see Statement
 */
public class ChannelWriteStatement extends Statement implements YieldingContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Expression} whose evaluated result with be written to the specified channel.</p>
     * @since 1.0.0
     * @see Expression
     */
    private Expression writeExpression      ;

    /**
     * <p>The {@link Expression} whose evaluation determines the channel that will write the specified
     * {@link Expression}.</p>
     * @since 1.0.0
     * @see Expression
     */
    private final Expression channel        ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ChannelWriteStatement} to its' default state with the specified channel
     * {@link Expression} & write {@link Expression}.</p>
     * @param channel The {@link Expression} that evaluates to a channel.
     * @param writeExpression The {@link Expression} whose result will be written to the evaluated channel.
     * @since 1.0.0
     * @see Expression
     */
    public ChannelWriteStatement(final Expression channel, final Expression writeExpression) {
        super(channel, writeExpression);

        this.writeExpression = writeExpression  ;
        this.channel         = channel          ;

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link Context}, and dispatches the {@link Visitor} to the channel
     * {@link Expression} via {@link Expression#accept(Visitor)}, write {@link Expression} via
     * {@link Expression#accept(Visitor)}, & {@link ChannelWriteStatement} via
     * {@link Visitor#visitChannelWriteStatement(ChannelWriteStatement)} before updating the {@link Context} to the
     * enclosing {@link Context}.
     * @since 1.0.0
     * @see Visitor
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Dispatch the Type
        this.channel.accept(visitor);

        // Dispatch the Name
        this.writeExpression.accept(visitor);

        // Dispatch the Sync Statement
        visitor.visitChannelWriteStatement(this);

        // Close the Context
        visitor.setContext(this.closeContext());

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the {@link ChannelWriteStatement}'s target, channel {@link Expression}.</p>
     * @return The {@link ChannelWriteStatement}'s target, channel {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public Expression getTargetExpression() {

        return this.channel;

    }

    /**
     * <p>Returns the {@link ChannelWriteStatement}'s write {@link Expression}.</p>
     * @return The {@link ChannelWriteStatement}'s write {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public Expression getWriteExpression() {

        return this.writeExpression;

    }

    /**
     * <p>Mutates the {@link ChannelWriteStatement}'s write {@link Expression}.</p>
     * @param writeExpression The desired {@link Expression} to bind to the {@link ChannelWriteStatement}.
     * @since 1.0.0
     * @see Expression
     */
    public final void setWriteExpression(final Expression writeExpression) {

        this.writeExpression = writeExpression;

    }

}