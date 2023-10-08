package org.processj.compiler.ast.statement.declarative;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

import java.util.*;

/**
 * <p>Class that encapsulates an ordered {@link List} of {@link Name}s.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see List
 * @see Name
 */
public class Names extends AST implements Iterable<Name> {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link List} of {@link Name}s managed by the {@link Names} instance.</p>
     * @since 1.0.0
     * @see List
     * @see Name
     */
    private final List<Name> names ;

    /**
     * <p>Maps each contained {@link Name}'s {@link String} value of their {@link Name} to its'
     * corresponding {@link SymbolMap}.</p>
     * @since 1.0.0
     * @see Name
     * @see String
     * @see SymbolMap
     */
    private final Map<String, SymbolMap> candidates;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Names} instance to its' default state as an empty sequence.</p>
     * @since 1.0.0
     * @see Name
     */
    public Names() {
        super();

        this.names      = new ArrayList<>() ;
        this.candidates = new HashMap<>()   ;

    }

    /**
     * <p>Initializes the {@link Names} instance to its' default state with the specified {@link Name} as
     * the initial {@link Name}.</p>
     * @since 1.0.0
     * @see Name
     */
    public Names(final Name name) {
        super(name);

        this.names      = new ArrayList<>() ;
        this.candidates = new HashMap<>()   ;

        if(name != null)
            this.names.add(name);

    }

    /// ------
    /// Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link Names} & it contains
     * the same amount of {@link Names} as this instance. If so, this will iterate through the set of
     * {@link Name}s & assert that each {@link Name}'s exists in the other instance.</p>
     * @param that The {@link Object} instance to check for existence.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link Names}, it contains the
     * same amount of {@link Name}s, & every {@link Names}'s {@link Name} exists in the other instance.
     * @since 1.0.0
     * @see Object
     * @see Name
     */
    @Override
    public final boolean equals(final Object that) {

        boolean result = (that instanceof Names)
                && ((Names) that).size() == this.size();

        if(result) for(final Map.Entry<String, SymbolMap> entry: this.candidates.entrySet()) {

            result = ((Names) that).candidates.containsKey(entry.getKey());

            if(!result) break;

        }

        return result;

    }

    /// --------
    /// Iterable

    /**
     * <p>Returns the {@link Iterator} instance that allows the {@link Names} instance to be iterated using an
     * enhanced for-loop.</p>
     * @return The {@link Iterator} instance to iterate the {@link Names} instance {@link Name} children.
     * @since 1.0.0
     * @see Iterable
     * @see Iterator
     * @see Name
     */
    @Override
    public final Iterator<Name> iterator() {

        return names.iterator();

    }

    /// ---
    /// AST

    /**
     * <p>Iterates through the contained {@link Name}s & dispatches the specified {@link Visitor} via
     * {@link Name#accept(Visitor)}.</p>
     * @param visitor The {@link Visitor} to dispatch to each contained {@link Name}.
     * @throws Phase.Error If {@link Name#accept(Visitor)} failed.
     * @since 1.0.0
     * @see Visitor
     * @see Name
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        for(final Name name: this)
            name.accept(visitor);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Clears the managed {@link List} of all handles corresponding to the {@link Names}, {@link Name}
     * children.</p>
     * @since 1.0.0
     * @see List
     * @see Name
     */
    public final void clear() {

        this.names.clear();

    }

    /**
     * <p>Inserts the {@link Name} at the specified index.</p>
     * @param index The integer value corresponding to the position of the {@link Names} to insert the specified
     *              {@link Name}.
     * @param name The {@link Name} to insert.
     * @since 1.0.0
     * @see List
     * @see Name
     */
    public final void insert(int index, final Name name, final SymbolMap symbolMap) {

        final Name previous = this.names.get(index);

        this.candidates.remove(previous.toString());

        this.names.set(index, name);

        this.candidates.put(name.toString(), symbolMap);

    }

    /**
     * <p>Returns a flag indicating if the {@link Names} instance contains any {@link Name} children.</p>
     * @return A flag indicating if the {@link Names} instance contains any {@link Name} children.
     * @since 1.0.0
     * @see List
     * @see Name
     */
    public final boolean isEmpty() {

        return this.names.isEmpty();

    }

    /**
     * <p>Returns an integer value corresponding to the amount of {@link Name} children contained in the
     * {@link Names} instance.</p>
     * @return an integer value corresponding to the amount of {@link Name} children contained in the
     * {@link Names} instance.
     * @since 1.0.0
     * @see Name
     * @see List
     */
    public final int size() {

        return names.size();

    }

    /**
     * <p>Returns the first {@link Name} instance contained in the {@link Names} instance, if any.</p>
     * @return The first {@link Name} instance contained in the {@link Names} instance, if any.
     * @since 1.0.0
     * @see Name
     */
    public final Name getFirst() {

        return this.names.get(0);

    }

    /**
     * <p>Returns the last {@link Name} instance contained in the {@link Names} instance, if any.</p>
     * @return The last {@link Name} instance contained in the {@link Names} instance, if any.
     * @since 1.0.0
     * @see Name
     */
    public final Name getLast() {

        return this.names.get(this.names.size() - 1);

    }

    /**
     * <p>Returns the candidates {@link Map} bound to the {@link Names}.</p>
     * @return The candidates {@link Map} bound to the {@link Names}.
     * @since 1.0.0
     * @see Name
     */
    public final Map<String, SymbolMap> getCandidates() {

        return this.candidates;

    }

    /**
     * <p>Retrieves the {@link Name} at the specified integral index.</p>
     * @param index The index corresponding to the desired child {@link Name} to retrieve
     * @return {@link Name} instance.
     * @since 1.0.0
     * @see Name
     */
    public final Name getNameAt(final int index) {

        return this.names.get(index);

    }

    /**
     * <p>Appends the {@link Name} as the next element into the managed {@link List} of {@link Name}s
     * & returns a reference to the target {@link Names} instance.</p>
     * @param name The {@link Name} to append to the end of the {@link List}.
     * @return Reference to the target {@link Names} instance.
     * @since 1.0.0
     * @see Name
     * @see List
     */
    public final Names append(final Name name, final SymbolMap symbolMap) {

        this.names.add(name);

        this.candidates.put(name.toString(), symbolMap);

        return this;

    }

    /**
     * <p>Appends the {@link Name} as the next element into the managed {@link List} of {@link Name}s
     * & returns a reference to the target {@link Names} instance.</p>
     * @param name The {@link Name} to append to the end of the {@link List}.
     * @return Reference to the target {@link Names} instance.
     * @since 1.0.0
     * @see Name
     * @see List
     */
    public final Names append(final Name name) {

        this.names.add(name);

        return this;

    }

    /**
     * <p>Appends the entire collection of {@link Name}s from the specified {@link Name} to the end of the
     * {@link Names} instance & returns a reference to the mutated {@link Names} instance.</p>
     * @param names The {@link Names} instance to append the {@link Name} children from.
     * @return Reference to the mutated {@link Names} instance.
     * @since 1.0.0
     * @see Name
     * @see List
     */
    public final Names appendAllFrom(final Names names) {

        this.names.addAll(names.names);

        return this;

    }

    /**
     * <p>Removes & returns the last {@link Name} contained in the {@link Names} instance.</p>
     * @return the last {@link Name} contained in the {@link Names} instance
     * @since 1.0.0
     * @see Name
     */
    public final Name removeLast() {

        return this.names.remove(this.names.size() - 1);

    }

}