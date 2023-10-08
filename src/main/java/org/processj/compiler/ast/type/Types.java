package org.processj.compiler.ast.type;

import org.processj.compiler.ast.AST;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

import java.util.*;

/**
 * <p>Class that encapsulates an ordered {@link List} of {@link Type}s.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see List
 * @see Type
 */
public class Types extends AST implements Iterable<Type> {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link List} of {@link Type}s managed by the {@link Types} instance.</p>
     * @since 1.0.0
     * @see List
     * @see Type
     */
    private final List<Type> types;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Types} instance to its' default state as an empty sequence.</p>
     * @since 1.0.0
     * @see Type
     */
    public Types() {
        super();

        this.types = new ArrayList<>();

    }

    /**
     * <p>Initializes the {@link Types} instance to its' default state with the specified {@link Type} as
     * the initial {@link Type}.</p>
     * @since 1.0.0
     * @see Type
     */
    public Types(final Type type) {
        super();

        this.types = new ArrayList<>();

        if(type != null)
            this.types.add(type);

    }

    /// --------
    /// Iterable

    /**
     * <p>Returns the {@link Iterator} instance that allows the {@link Types} instance to be iterated using an
     * enhanced for-loop.</p>
     * @return The {@link Iterator} instance to iterate the {@link Types} instance {@link Type} children.
     * @since 1.0.0
     * @see Iterable
     * @see Iterator
     * @see Type
     */
    @Override
    public final Iterator<Type> iterator() {

        return types.iterator();

    }

    /// ---
    /// AST

    /**
     * <p>Iterates through the contained {@link Type}s & dispatches the specified {@link Visitor} via
     * {@link Type#accept(Visitor)}.</p>
     * @param visitor The {@link Visitor} to dispatch to each contained {@link Type}.
     * @throws Phase.Error If {@link Type#accept(Visitor)} failed.
     * @since 1.0.0
     * @see Visitor
     * @see Type
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        for(final Type type: this)
            type.accept(visitor);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Clears the managed {@link List} of all handles corresponding to the {@link Types}, {@link Type}
     * children.</p>
     * @since 1.0.0
     * @see List
     * @see Type
     */
    public final void clear() {

        this.types.clear();

    }

    /**
     * <p>Inserts the {@link Type} at the specified index.</p>
     * @param index The integer value corresponding to the position of the {@link Types} to insert the specified
     *              {@link Type}.
     * @param type The {@link Type} to insert.
     * @since 1.0.0
     * @see List
     * @see Type
     */
    public final void insert(int index, final Type type) {

        this.types.set(index, type);

    }

    /**
     * <p>Returns a flag indicating if the {@link Types} instance contains any {@link Type} children.</p>
     * @return A flag indicating if the {@link Types} instance contains any {@link Type} children.
     * @since 1.0.0
     * @see List
     * @see Type
     */
    public final boolean isEmpty() {

        return this.types.isEmpty();

    }

    /**
     * <p>Returns an integer value corresponding to the amount of {@link Type} children contained in the
     * {@link Types} instance.</p>
     * @return an integer value corresponding to the amount of {@link Type} children contained in the
     * {@link Types} instance.
     * @since 1.0.0
     * @see Type
     * @see List
     */
    public final int size() {

        return types.size();

    }

    /**
     * <p>Returns the first {@link Type} instance contained in the {@link Types} instance, if any.</p>
     * @return The first {@link Type} instance contained in the {@link Types} instance, if any.
     * @since 1.0.0
     * @see Type
     */
    public final Type getFirst() {

        return this.types.get(0);

    }

    /**
     * <p>Returns the last {@link Type} instance contained in the {@link Types} instance, if any.</p>
     * @return The last {@link Type} instance contained in the {@link Types} instance, if any.
     * @since 1.0.0
     * @see Type
     */
    public final Type getLast() {

        return this.types.get(this.types.size() - 1);

    }

    /**
     * <p>Retrieves the {@link Type} at the specified integral index.</p>
     * @param index The index corresponding to the desired child {@link Type} to retrieve
     * @return {@link Type} instance.
     * @since 1.0.0
     * @see Type
     */
    public final Type child(final int index) {

        return this.types.get(index);

    }

    /**
     * <p>Appends the {@link Type} as the next element into the managed {@link List} of {@link Type}s
     * & returns a reference to the target {@link Types} instance.</p>
     * @param type The {@link Type} to append to the end of the {@link List}.
     * @return Reference to the target {@link Types} instance.
     * @since 1.0.0
     * @see Type
     * @see List
     */
    public final Types append(final Type type) {

        this.types.add(type);

        return this;

    }

    /**
     * <p>Appends the entire collection of {@link Type}s from the specified {@link Type} to the end of the
     * {@link Types} instance & returns a reference to the mutated {@link Types} instance.</p>
     * @param types The {@link Types} instance to append the {@link Type} children from.
     * @return Reference to the mutated {@link Types} instance.
     * @since 1.0.0
     * @see Type
     * @see List
     */
    public final Types appendAllFrom(final Types types) {

        this.types.addAll(types.types);

        return this;

    }

    /**
     * <p>Removes & returns the last {@link Type} contained in the {@link Types} instance.</p>
     * @return the last {@link Type} contained in the {@link Types} instance
     * @since 1.0.0
     * @see Type
     */
    public final Type removeLast() {

        return this.types.remove(this.types.size() - 1);

    }

}