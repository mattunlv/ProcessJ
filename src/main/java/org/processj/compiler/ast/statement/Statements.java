package org.processj.compiler.ast.statement;

import org.processj.compiler.ast.AST;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

import java.util.*;

/**
 * <p>Class that encapsulates an ordered {@link List} of {@link Statement}s.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see List
 * @see Statement
 */
public class Statements extends AST implements Iterable<Statement> {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link List} of {@link Statement}s managed by the {@link Statements} instance.</p>
     * @since 1.0.0
     * @see List
     * @see Statement
     */
    private final List<Statement> statements;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Statements} instance to its' default state as an empty sequence.</p>
     * @since 1.0.0
     * @see Statement
     */
    public Statements() {
        super();

        this.statements = new ArrayList<>();

    }

    /**
     * <p>Initializes the {@link Statements} instance to its' default state with the specified {@link Statement} as
     * the initial {@link Statement}.</p>
     * @since 1.0.0
     * @see Statement
     */
    public Statements(final Statement statement) {
        super(statement);

        this.statements = new ArrayList<>();

        if(statement != null)
            this.statements.add(statement);

    }

    /**
     * <p>Initializes the {@link Statements} instance to its' default state with the specified {@link Statements} as
     * the initial set of {@link Statements}.</p>
     * @since 1.0.0
     * @see Statements
     */
    public Statements(final Statements statements) {
        super(statements);

        this.statements = new ArrayList<>();

        if(statements != null) for(final Statement statement: statements)
            if(statement != null) this.statements.add(statement);

    }

    /// --------
    /// Iterable

    /**
     * <p>Returns the {@link Iterator} instance that allows the {@link Statements} instance to be iterated using an
     * enhanced for-loop.</p>
     * @return The {@link Iterator} instance to iterate the {@link Statements} instance {@link Statement} children.
     * @since 1.0.0
     * @see Iterable
     * @see Iterator
     * @see Statement
     */
    @Override
    public final Iterator<Statement> iterator() {

        return statements.iterator();

    }

    /// ---
    /// AST

    /**
     * <p>Iterates through the contained {@link Statement}s & dispatches the specified {@link Visitor} via
     * {@link Statement#accept(Visitor)}.</p>
     * @param visitor The {@link Visitor} to dispatch to each contained {@link Statement}.
     * @throws Phase.Error If {@link Statement#accept(Visitor)} failed.
     * @since 1.0.0
     * @see Visitor
     * @see Statement
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        for(final Statement statement: this)
            statement.accept(visitor);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Clears the managed {@link List} of all handles corresponding to the {@link Statements}, {@link Statement}
     * children.</p>
     * @since 1.0.0
     * @see List
     * @see Statement
     */
    public final void clear() {

        this.statements.clear();

    }

    /**
     * <p>Inserts the {@link Statement} at the specified index.</p>
     * @param index The integer value corresponding to the position of the {@link Statements} to insert the specified
     *              {@link Statement}.
     * @param statement The {@link Statement} to insert.
     * @since 1.0.0
     * @see List
     * @see Statement
     */
    public final void insert(int index, final Statement statement) {

        this.statements.set(index, statement);

    }

    /**
     * <p>Returns a flag indicating if the {@link Statements} instance contains any {@link Statement} children.</p>
     * @return A flag indicating if the {@link Statements} instance contains any {@link Statement} children.
     * @since 1.0.0
     * @see List
     * @see Statement
     */
    public final boolean isEmpty() {

        return this.statements.isEmpty();

    }

    /**
     * <p>Returns an integer value corresponding to the amount of {@link Statement} children contained in the
     * {@link Statements} instance.</p>
     * @return an integer value corresponding to the amount of {@link Statement} children contained in the
     * {@link Statements} instance.
     * @since 1.0.0
     * @see Statement
     * @see List
     */
    public final int size() {

        return statements.size();

    }

    /**
     * <p>Returns the first {@link Statement} instance contained in the {@link Statements} instance, if any.</p>
     * @return The first {@link Statement} instance contained in the {@link Statements} instance, if any.
     * @since 1.0.0
     * @see Statement
     */
    public final Statement getFirst() {

        return this.statements.get(0);

    }

    /**
     * <p>Returns the last {@link Statement} instance contained in the {@link Statements} instance, if any.</p>
     * @return The last {@link Statement} instance contained in the {@link Statements} instance, if any.
     * @since 1.0.0
     * @see Statement
     */
    public final Statement getLast() {

        return this.statements.get(this.statements.size() - 1);

    }

    /**
     * <p>Retrieves the {@link Statement} at the specified integral index.</p>
     * @param index The index corresponding to the desired child {@link Statement} to retrieve
     * @return {@link Statement} instance.
     * @since 1.0.0
     * @see Statement
     */
    public final Statement child(final int index) {

        return this.statements.get(index);

    }

    /**
     * <p>Appends the {@link Statement} as the next element into the managed {@link List} of {@link Statement}s
     * & returns a reference to the target {@link Statements} instance.</p>
     * @param statement The {@link Statement} to append to the end of the {@link List}.
     * @return Reference to the target {@link Statements} instance.
     * @since 1.0.0
     * @see Statement
     * @see List
     */
    public final Statements append(final Statement statement) {

        this.statements.add(statement);

        return this;

    }

    /**
     * <p>Appends the entire collection of {@link Statement}s from the specified {@link Statement} to the end of the
     * {@link Statements} instance & returns a reference to the mutated {@link Statements} instance.</p>
     * @param statements The {@link Statements} instance to append the {@link Statement} children from.
     * @return Reference to the mutated {@link Statements} instance.
     * @since 1.0.0
     * @see Statement
     * @see List
     */
    public final Statements appendAllFrom(final Statements statements) {

        this.statements.addAll(statements.statements);

        return this;

    }

    /**
     * <p>Removes & returns the last {@link Statement} contained in the {@link Statements} instance.</p>
     * @return the last {@link Statement} contained in the {@link Statements} instance
     * @since 1.0.0
     * @see Statement
     */
    public final Statement removeLast() {

        return this.statements.remove(this.statements.size() - 1);

    }

}