package org.processj.compiler.ast.packages;

import org.processj.compiler.ast.AST;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

import java.util.*;

/**
 * <p>Class that encapsulates an ordered {@link List} of {@link Import}s.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see List
 * @see Import
 */
public class Imports extends AST implements Iterable<Import> {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link List} of {@link Import}s managed by the {@link Imports} instance.</p>
     * @since 1.0.0
     * @see List
     * @see Import
     */
    private final List<Import> imports;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Imports} instance to its' default state as an empty sequence.</p>
     * @since 1.0.0
     * @see Import
     */
    public Imports() {
        super();

        this.imports = new ArrayList<>();

    }

    /**
     * <p>Initializes the {@link Imports} instance to its' default state with the specified {@link Import} as
     * the initial {@link Import}.</p>
     * @since 1.0.0
     * @see Import
     */
    public Imports(final Import pragma) {
        super(pragma);

        this.imports = new ArrayList<>();

        if(pragma != null)
            this.imports.add(pragma);

    }

    /// --------
    /// Iterable

    /**
     * <p>Returns the {@link Iterator} instance that allows the {@link Imports} instance to be iterated using an
     * enhanced for-loop.</p>
     * @return The {@link Iterator} instance to iterate the {@link Imports} instance {@link Import} children.
     * @since 1.0.0
     * @see Iterable
     * @see Iterator
     * @see Import
     */
    @Override
    public final Iterator<Import> iterator() {

        return imports.iterator();

    }

    /// ---
    /// AST

    /**
     * <p>Iterates through the contained {@link Import}s & dispatches the specified {@link Visitor} via
     * {@link Import#accept(Visitor)}.</p>
     * @param visitor The {@link Visitor} to dispatch to each contained {@link Import}.
     * @throws Phase.Error If {@link Import#accept(Visitor)} failed.
     * @since 1.0.0
     * @see Visitor
     * @see Import
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        for(final Import pragma: this)
            pragma.accept(visitor);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Clears the managed {@link List} of all handles corresponding to the {@link Imports}, {@link Import}
     * children.</p>
     * @since 1.0.0
     * @see List
     * @see Import
     */
    public final void clear() {

        this.imports.clear();

    }

    /**
     * <p>Inserts the {@link Import} at the specified index.</p>
     * @param index The integer value corresponding to the position of the {@link Imports} to insert the specified
     *              {@link Import}.
     * @param pragma The {@link Import} to insert.
     * @since 1.0.0
     * @see List
     * @see Import
     */
    public final void insert(int index, final Import pragma) {

        this.imports.set(index, pragma);

    }

    /**
     * <p>Returns a flag indicating if the {@link Imports} instance contains any {@link Import} children.</p>
     * @return A flag indicating if the {@link Imports} instance contains any {@link Import} children.
     * @since 1.0.0
     * @see List
     * @see Import
     */
    public final boolean isEmpty() {

        return this.imports.isEmpty();

    }

    /**
     * <p>Returns an integer value corresponding to the amount of {@link Import} children contained in the
     * {@link Imports} instance.</p>
     * @return an integer value corresponding to the amount of {@link Import} children contained in the
     * {@link Imports} instance.
     * @since 1.0.0
     * @see Import
     * @see List
     */
    public final int size() {

        return imports.size();

    }

    /**
     * <p>Returns the first {@link Import} instance contained in the {@link Imports} instance, if any.</p>
     * @return The first {@link Import} instance contained in the {@link Imports} instance, if any.
     * @since 1.0.0
     * @see Import
     */
    public final Import getFirst() {

        return this.imports.get(0);

    }

    /**
     * <p>Returns the last {@link Import} instance contained in the {@link Imports} instance, if any.</p>
     * @return The last {@link Import} instance contained in the {@link Imports} instance, if any.
     * @since 1.0.0
     * @see Import
     */
    public final Import getLast() {

        return this.imports.get(this.imports.size() - 1);

    }

    /**
     * <p>Retrieves the {@link Import} at the specified integral index.</p>
     * @param index The index corresponding to the desired child {@link Import} to retrieve
     * @return {@link Import} instance.
     * @since 1.0.0
     * @see Import
     */
    public final Import child(final int index) {

        return this.imports.get(index);

    }

    /**
     * <p>Appends the {@link Import} as the next element into the managed {@link List} of {@link Import}s
     * & returns a reference to the target {@link Imports} instance.</p>
     * @param pragma The {@link Import} to append to the end of the {@link List}.
     * @return Reference to the target {@link Imports} instance.
     * @since 1.0.0
     * @see Import
     * @see List
     */
    public final Imports append(final Import pragma) {

        this.imports.add(pragma);

        return this;

    }

    /**
     * <p>Appends the entire collection of {@link Import}s from the specified {@link Import} to the end of the
     * {@link Imports} instance & returns a reference to the mutated {@link Imports} instance.</p>
     * @param imports The {@link Imports} instance to append the {@link Import} children from.
     * @return Reference to the mutated {@link Imports} instance.
     * @since 1.0.0
     * @see Import
     * @see List
     */
    public final Imports appendAllFrom(final Imports imports) {

        this.imports.addAll(imports.imports);

        return this;

    }

    /**
     * <p>Removes & returns the last {@link Import} contained in the {@link Imports} instance.</p>
     * @return the last {@link Import} contained in the {@link Imports} instance
     * @since 1.0.0
     * @see Import
     */
    public final Import removeLast() {

        return this.imports.remove(this.imports.size() - 1);

    }

}