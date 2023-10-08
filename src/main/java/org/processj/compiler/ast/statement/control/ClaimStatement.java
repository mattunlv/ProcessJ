package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a {@link Statement} that specifies a set of channels the contained {@link Statement}
 * will execute with.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see Statement
 */
public class ClaimStatement extends Statement {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Sequence} of channels the {@link Statement} has exclusive access to.</p>
     * @since 1.0.0
     * @see Sequence
     * @see Statement
     */
    private final Sequence<AST> channels    ;

    /**
     * <p>The {@link Statement} that executes with the specified {@link Sequence} of channels.</p>
     * @since 1.0.0
     * @see Sequence
     * @see Statement
     */
    private final Statement     statement   ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ClaimStatement} with the specified {@link Sequence} of channels.</p>
     * @param channels The {@link Sequence} of channels the specified {@link Statement} executes with.
     * @param statement The {@link Statement} that executes with the specified {@link Sequence} of channels.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link ClaimStatement}.
     * @since 1.0.0
     * @see Name
     * @see Token
     */
    public ClaimStatement(final Sequence<AST> channels, final Statement statement, final Token... tokens) {
        super(tokens);

        this.channels   = channels  ;
        this.statement  = statement ;

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context}, and dispatches the
     * {@link Visitor} to each channel specified in the {@link ClaimStatement}, &  {@link ClaimStatement} via
     * {@link Visitor#visitClaimStatement(ClaimStatement)} before updating the {@link org.processj.compiler.ast.Context}
     * to the enclosing {@link Context}.
     * @since 1.0.0
     * @see Visitor
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Dispatch the children
        for(final AST ast: this.channels) ast.accept(visitor);

        // Dispatch the Claim Statement
        visitor.visitClaimStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the {@link Sequence} of channels the {@link ClaimStatement}'s {@link Statement} will execute with.</p>
     * @return The {@link Sequence} of channels the {@link ClaimStatement}'s {@link Statement} will execute with.
     * @since 1.0.0
     * @see Sequence
     */
    public final Sequence<AST> getChannels() {

        return this.channels;

    }

    /**
     * <p>Returns the {@link Statement} that executes with the specified {@link Sequence} of channels.</p>
     * @return The {@link Statement} that executes with the specified {@link Sequence} of channels.
     * @since 1.0.0
     * @see Sequence
     */
    public final Statement getStatement() {

        return this.statement;

    }

}