package org.processj.compiler.ast.statement.declarative;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.type.RecordType;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

import java.util.*;

/**
 * <p>Class that encapsulates an ordered {@link List} of protocol extend {@link Name}s.</p>
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
public class RecordExtends extends AST implements Iterable<Name> {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link List} of {@link Name}s managed by the {@link RecordExtends} instance.</p>
     * @since 1.0.0
     * @see List
     * @see Name
     */
    private final List<Name>                            names           ;

    /**
     * <p>Maps each contained {@link Name}'s {@link String} value of their {@link Name} to its'
     * corresponding {@link RecordType}.</p>
     * @since 1.0.0
     * @see Name
     * @see String
     * @see RecordType
     */
    private final Map<String, RecordType>    candidates      ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link RecordExtends} instance to its' default state as an empty sequence.</p>
     * @since 1.0.0
     * @see Name
     */
    public RecordExtends() {
        super();

        this.names      = new ArrayList<>() ;
        this.candidates = new HashMap<>()   ;

    }

    /**
     * <p>Initializes the {@link RecordExtends} instance to its' default state with the specified {@link Name} as
     * the initial {@link Name}.</p>
     * @since 1.0.0
     * @see Name
     */
    public RecordExtends(final Name name) {
        super(name);

        this.names      = new ArrayList<>() ;
        this.candidates = new HashMap<>()   ;

        if(name != null)
            this.names.add(name);

    }

    /// ------
    /// Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link RecordExtends} & it contains
     * the same amount of {@link RecordExtends} as this instance. If so, this will iterate through the set of
     * {@link Name}s & assert that each {@link Name}'s exists in the other instance.</p>
     * @param that The {@link Object} instance to check for existence.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link RecordExtends}, it contains the
     * same amount of {@link Name}s, & every {@link RecordExtends}'s {@link Name} exists in the other instance.
     * @since 1.0.0
     * @see Object
     * @see Name
     */
    @Override
    public final boolean equals(final Object that) {

        boolean result = (that instanceof RecordExtends)
                && ((RecordExtends) that).size() == this.size();

        if(result) for(final Map.Entry<String, RecordType> entry: this.candidates.entrySet()) {

            result = ((RecordExtends) that).candidates.containsKey(entry.getKey());

            if(!result) break;

        }

        return result;

    }

    /// --------
    /// Iterable

    /**
     * <p>Returns the {@link Iterator} instance that allows the {@link RecordExtends} instance to be iterated using an
     * enhanced for-loop.</p>
     * @return The {@link Iterator} instance to iterate the {@link RecordExtends} instance {@link Name} children.
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
     * <p>Clears the managed {@link List} of all handles corresponding to the {@link RecordExtends}, {@link Name}
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
     * @param index The integer value corresponding to the position of the {@link RecordExtends} to insert the specified
     *              {@link Name}.
     * @param name The {@link Name} to insert.
     * @since 1.0.0
     * @see List
     * @see Name
     */
    public final void insert(int index, final Name name, final RecordType protocolTypeDeclaration) {

        final Name previous = this.names.get(index);

        this.candidates.remove(previous.toString());

        this.names.set(index, name);

        this.candidates.put(name.toString(), protocolTypeDeclaration);

    }

    /**
     * <p>Returns a flag indicating if the {@link RecordExtends} instance contains any {@link Name} children.</p>
     * @return A flag indicating if the {@link RecordExtends} instance contains any {@link Name} children.
     * @since 1.0.0
     * @see List
     * @see Name
     */
    public final boolean isEmpty() {

        return this.names.isEmpty();

    }

    /**
     * <p>Returns an integer value corresponding to the amount of {@link Name} children contained in the
     * {@link RecordExtends} instance.</p>
     * @return an integer value corresponding to the amount of {@link Name} children contained in the
     * {@link RecordExtends} instance.
     * @since 1.0.0
     * @see Name
     * @see List
     */
    public final int size() {

        return names.size();

    }

    /**
     * <p>Returns the first {@link Name} instance contained in the {@link RecordExtends} instance, if any.</p>
     * @return The first {@link Name} instance contained in the {@link RecordExtends} instance, if any.
     * @since 1.0.0
     * @see Name
     */
    public final Name getFirst() {

        return this.names.get(0);

    }

    /**
     * <p>Returns the last {@link Name} instance contained in the {@link RecordExtends} instance, if any.</p>
     * @return The last {@link Name} instance contained in the {@link RecordExtends} instance, if any.
     * @since 1.0.0
     * @see Name
     */
    public final Name getLast() {

        return this.names.get(this.names.size() - 1);

    }

    /**
     * <p>Appends the {@link Name} as the next element into the managed {@link List} of {@link Name}s
     * & returns a reference to the target {@link RecordExtends} instance.</p>
     * @param name The {@link Name} to append to the end of the {@link List}.
     * @return Reference to the target {@link RecordExtends} instance.
     * @since 1.0.0
     * @see Name
     * @see List
     */
    public final RecordExtends append(final Name name, final RecordType protocolTypeDeclaration) {

        this.names.add(name);

        this.candidates.put(name.toString(), protocolTypeDeclaration);

        return this;

    }

}