package org.processj.compiler.ast.packages;

import org.processj.compiler.ast.AST;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

import java.util.*;

/**
 * <p>Class that encapsulates an ordered {@link List} of {@link Pragma}s.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see List
 * @see Pragma
 */
public class Pragmas extends AST implements Iterable<Pragma> {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link List} of {@link Pragma}s managed by the {@link Pragmas} instance.</p>
     * @since 1.0.0
     * @see List
     * @see Pragma
     */
    private final List<Pragma> pragmas;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Pragmas} instance to its' default state as an empty sequence.</p>
     * @since 1.0.0
     * @see Pragma
     */
    public Pragmas() {
        super();

        this.pragmas = new ArrayList<>();

    }

    /**
     * <p>Initializes the {@link Pragmas} instance to its' default state with the specified {@link Pragma} as
     * the initial {@link Pragma}.</p>
     * @since 1.0.0
     * @see Pragma
     */
    public Pragmas(final Pragma pragma) {
        super(pragma);

        this.pragmas = new ArrayList<>();

        if(pragma != null)
            this.pragmas.add(pragma);

    }

    /// --------
    /// Iterable

    /**
     * <p>Returns the {@link Iterator} instance that allows the {@link Pragmas} instance to be iterated using an
     * enhanced for-loop.</p>
     * @return The {@link Iterator} instance to iterate the {@link Pragmas} instance {@link Pragma} children.
     * @since 1.0.0
     * @see Iterable
     * @see Iterator
     * @see Pragma
     */
    @Override
    public final Iterator<Pragma> iterator() {

        return pragmas.iterator();

    }

    /// ---
    /// AST

    /**
     * <p>Iterates through the contained {@link Pragma}s & dispatches the specified {@link Visitor} via
     * {@link Pragma#accept(Visitor)}.</p>
     * @param visitor The {@link Visitor} to dispatch to each contained {@link Pragma}.
     * @throws Phase.Error If {@link Pragma#accept(Visitor)} failed.
     * @since 1.0.0
     * @see Visitor
     * @see Pragma
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        for(final Pragma pragma: this)
            pragma.accept(visitor);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Clears the managed {@link List} of all handles corresponding to the {@link Pragmas}, {@link Pragma}
     * children.</p>
     * @since 1.0.0
     * @see List
     * @see Pragma
     */
    public final void clear() {

        this.pragmas.clear();

    }

    /**
     * <p>Inserts the {@link Pragma} at the specified index.</p>
     * @param index The integer value corresponding to the position of the {@link Pragmas} to insert the specified
     *              {@link Pragma}.
     * @param pragma The {@link Pragma} to insert.
     * @since 1.0.0
     * @see List
     * @see Pragma
     */
    public final void insert(int index, final Pragma pragma) {

        this.pragmas.set(index, pragma);

    }

    /**
     * <p>Returns a flag indicating if the {@link Pragmas} instance contains any {@link Pragma} children.</p>
     * @return A flag indicating if the {@link Pragmas} instance contains any {@link Pragma} children.
     * @since 1.0.0
     * @see List
     * @see Pragma
     */
    public final boolean isEmpty() {

        return this.pragmas.isEmpty();

    }

    /**
     * <p>Returns an integer value corresponding to the amount of {@link Pragma} children contained in the
     * {@link Pragmas} instance.</p>
     * @return an integer value corresponding to the amount of {@link Pragma} children contained in the
     * {@link Pragmas} instance.
     * @since 1.0.0
     * @see Pragma
     * @see List
     */
    public final int size() {

        return pragmas.size();

    }

    /**
     * <p>Returns the first {@link Pragma} instance contained in the {@link Pragmas} instance, if any.</p>
     * @return The first {@link Pragma} instance contained in the {@link Pragmas} instance, if any.
     * @since 1.0.0
     * @see Pragma
     */
    public final Pragma getFirst() {

        return this.pragmas.get(0);

    }

    /**
     * <p>Returns the last {@link Pragma} instance contained in the {@link Pragmas} instance, if any.</p>
     * @return The last {@link Pragma} instance contained in the {@link Pragmas} instance, if any.
     * @since 1.0.0
     * @see Pragma
     */
    public final Pragma getLast() {

        return this.pragmas.get(this.pragmas.size() - 1);

    }

    /**
     * <p>Retrieves the {@link Pragma} at the specified integral index.</p>
     * @param index The index corresponding to the desired child {@link Pragma} to retrieve
     * @return {@link Pragma} instance.
     * @since 1.0.0
     * @see Pragma
     */
    public final Pragma child(final int index) {

        return this.pragmas.get(index);

    }

    /**
     * <p>Appends the {@link Pragma} as the next element into the managed {@link List} of {@link Pragma}s
     * & returns a reference to the target {@link Pragmas} instance.</p>
     * @param pragma The {@link Pragma} to append to the end of the {@link List}.
     * @return Reference to the target {@link Pragmas} instance.
     * @since 1.0.0
     * @see Pragma
     * @see List
     */
    public final Pragmas append(final Pragma pragma) {

        this.pragmas.add(pragma);

        return this;

    }

    /**
     * <p>Appends the entire collection of {@link Pragma}s from the specified {@link Pragma} to the end of the
     * {@link Pragmas} instance & returns a reference to the mutated {@link Pragmas} instance.</p>
     * @param pragmas The {@link Pragmas} instance to append the {@link Pragma} children from.
     * @return Reference to the mutated {@link Pragmas} instance.
     * @since 1.0.0
     * @see Pragma
     * @see List
     */
    public final Pragmas appendAllFrom(final Pragmas pragmas) {

        this.pragmas.addAll(pragmas.pragmas);

        return this;

    }

    /**
     * <p>Removes & returns the last {@link Pragma} contained in the {@link Pragmas} instance.</p>
     * @return the last {@link Pragma} contained in the {@link Pragmas} instance
     * @since 1.0.0
     * @see Pragma
     */
    public final Pragma removeLast() {

        return this.pragmas.remove(this.pragmas.size() - 1);

    }

}