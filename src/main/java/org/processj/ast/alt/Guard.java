package org.processj.ast.alt;

import org.processj.Phase;
import org.processj.ast.AST;
import org.processj.ast.Statement;
import org.processj.utilities.Visitor;

/**
 * <p>Class that represents a {@link Guard} {@link Statement} within an {@link AltCase}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca.
 * @version 1.0.0
 * @since 0.1.0
 * @see AltCase
 * @see Statement
 */
public class Guard extends AST {

    /// --------------
    /// Private Fields

    /**
     * <p>The literal {@link Guard} {@link Statement}.</p>
     */
    private final Statement statement;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Guard} to its' default state with the specified {@link Statement}.</p>
     * @param statement The literal {@link Statement} corresponding to the {@link Guard}.
     * @since 0.1.0
     */
    public Guard(final Statement statement) {
        super(new AST[] { statement });

        this.statement = statement;

    }

    /// ----------------
    /// java.lang.Object

    // TODO: Hashcode

    /**
     * <p>Returns a boolean flag indicating if the specified {@link Guard} is identical to the
     * given {@link Guard}. This method will check the super class invocation of {@link Object#equals(Object)},
     * & the instance of the specified {@link Object}.</p>
     * @param that The {@link Object} instance to check against the given instance.
     * @return boolean flag indicating if the specified instance is identical to this.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof Guard)
                && this.statement.equals(((Guard) that).statement);

    }

    /**
     * <p>Returns the {@link String} representation of the {@link Guard}.</p>
     * @return {@link String} representation of the {@link Guard}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.statement.toString();

    }

    /// --------------------
    /// org.processj.ast.AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link Guard}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitGuard(Guard)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitGuard(this);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the {@link Statement} instance corresponding with the {@link Guard}.</p>
     * @return {@link Statement} instance corresponding with the {@link Guard}.
     * @since 0.1.0
     */
    public final Statement getStatement() {

        return this.statement;

    }

}