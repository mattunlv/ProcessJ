package org.processj.compiler.ast.statement.declarative;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.ast.Name;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

import java.util.*;

/**
 * <p>Class that encapsulates an ordered {@link List} of {@link ParameterDeclaration}s.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see List
 * @see ParameterDeclaration
 */
public class Parameters extends AST implements Iterable<ParameterDeclaration> {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link List} of {@link ParameterDeclaration}s managed by the {@link Parameters} instance.</p>
     * @since 1.0.0
     * @see List
     * @see ParameterDeclaration
     */
    private final List<ParameterDeclaration>    parameterDeclarations   ;

    /**
     * <p>Maps each contained {@link ParameterDeclaration}'s {@link String} value of their {@link Name} to its'
     * declared {@link Type}.</p>
     * @since 1.0.0
     * @see Name
     * @see String
     * @see Type
     */
    private final Map<String, Type>             parameterTypes          ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Parameters} instance to its' default state as an empty sequence.</p>
     * @since 1.0.0
     * @see ParameterDeclaration
     */
    public Parameters() {
        super();

        this.parameterDeclarations  = new ArrayList<>() ;
        this.parameterTypes         = new HashMap<>()   ;

    }

    /**
     * <p>Initializes the {@link Parameters} instance to its' default state with the specified {@link ParameterDeclaration} as
     * the initial {@link ParameterDeclaration}.</p>
     * @since 1.0.0
     * @see ParameterDeclaration
     */
    public Parameters(final ParameterDeclaration parameterDeclaration) {
        super(parameterDeclaration);

        this.parameterDeclarations  = new ArrayList<>() ;
        this.parameterTypes         = new HashMap<>()   ;

        if(parameterDeclaration != null)
            this.parameterDeclarations.add(parameterDeclaration);

    }

    /// ------
    /// Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link Parameters} & it contains
     * the same amount of {@link Parameters} as this instance. If so, this will iterate through the set of
     * {@link ParameterDeclaration}s & assert that each {@link ParameterDeclaration}'s {@link Type} is Type Equal to
     * the others.</p>
     * @param that The {@link Object} instance to check for type equality.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link Parameters}, it contains the
     * same amount of {@link ParameterDeclaration}s, & every {@link ParameterDeclaration}'s {@link Type} is type equal
     * to the others.
     * @since 1.0.0
     * @see Object
     * @see ParameterDeclaration
     * @see Type
     */
    @Override
    public final boolean equals(final Object that) {

        boolean result = (that instanceof Parameters)
                && ((Parameters) that).size() == this.size();

        for(int index = 0; result && (index < this.size()); index++)
            result = this.parameterDeclarations.get(index).getType()
                    .equals(((Parameters) that).getParameterAt(index).getType());

        return result;

    }

    /// --------
    /// Iterable

    /**
     * <p>Returns the {@link Iterator} instance that allows the {@link Parameters} instance to be iterated using an
     * enhanced for-loop.</p>
     * @return The {@link Iterator} instance to iterate the {@link Parameters} instance {@link ParameterDeclaration} children.
     * @since 1.0.0
     * @see Iterable
     * @see Iterator
     * @see ParameterDeclaration
     */
    @Override
    public final Iterator<ParameterDeclaration> iterator() {

        return parameterDeclarations.iterator();

    }

    /// ---
    /// AST

    /**
     * <p>Iterates through the contained {@link ParameterDeclaration}s & dispatches the specified {@link Visitor} via
     * {@link ParameterDeclaration#accept(Visitor)}.</p>
     * @param visitor The {@link Visitor} to dispatch to each contained {@link ParameterDeclaration}.
     * @throws Phase.Error If {@link ParameterDeclaration#accept(Visitor)} failed.
     * @since 1.0.0
     * @see Visitor
     * @see ParameterDeclaration
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        for(final ParameterDeclaration parameterDeclaration: this)
            parameterDeclaration.accept(visitor);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Clears the managed {@link List} of all handles corresponding to the {@link Parameters}, {@link ParameterDeclaration}
     * children.</p>
     * @since 1.0.0
     * @see List
     * @see ParameterDeclaration
     */
    public final void clear() {

        this.parameterDeclarations.clear();

    }

    /**
     * <p>Inserts the {@link ParameterDeclaration} at the specified index.</p>
     * @param index The integer value corresponding to the position of the {@link Parameters} to insert the specified
     *              {@link ParameterDeclaration}.
     * @param parameterDeclaration The {@link ParameterDeclaration} to insert.
     * @since 1.0.0
     * @see List
     * @see ParameterDeclaration
     */
    public final void insert(int index, final ParameterDeclaration parameterDeclaration) {

        final ParameterDeclaration previous = this.parameterDeclarations.get(index);

        this.parameterTypes.remove(previous.getName().toString());

        this.parameterDeclarations.set(index, parameterDeclaration);

        this.parameterTypes.put(parameterDeclaration.getName().toString(), parameterDeclaration.getType());

    }

    /**
     * <p>Returns a flag indicating if the {@link Parameters} instance contains any {@link ParameterDeclaration} children.</p>
     * @return A flag indicating if the {@link Parameters} instance contains any {@link ParameterDeclaration} children.
     * @since 1.0.0
     * @see List
     * @see ParameterDeclaration
     */
    public final boolean isEmpty() {

        return this.parameterDeclarations.isEmpty();

    }

    /**
     * <p>Returns an integer value corresponding to the amount of {@link ParameterDeclaration} children contained in the
     * {@link Parameters} instance.</p>
     * @return an integer value corresponding to the amount of {@link ParameterDeclaration} children contained in the
     * {@link Parameters} instance.
     * @since 1.0.0
     * @see ParameterDeclaration
     * @see List
     */
    public final int size() {

        return parameterDeclarations.size();

    }

    /**
     * <p>Returns the first {@link ParameterDeclaration} instance contained in the {@link Parameters} instance, if any.</p>
     * @return The first {@link ParameterDeclaration} instance contained in the {@link Parameters} instance, if any.
     * @since 1.0.0
     * @see ParameterDeclaration
     */
    public final ParameterDeclaration getFirst() {

        return this.parameterDeclarations.get(0);

    }

    /**
     * <p>Returns the last {@link ParameterDeclaration} instance contained in the {@link Parameters} instance, if any.</p>
     * @return The last {@link ParameterDeclaration} instance contained in the {@link Parameters} instance, if any.
     * @since 1.0.0
     * @see ParameterDeclaration
     */
    public final ParameterDeclaration getLast() {

        return this.parameterDeclarations.get(this.parameterDeclarations.size() - 1);

    }

    /**
     * <p>Retrieves the {@link ParameterDeclaration} at the specified integral index.</p>
     * @param index The index corresponding to the desired child {@link ParameterDeclaration} to retrieve
     * @return {@link ParameterDeclaration} instance.
     * @since 1.0.0
     * @see ParameterDeclaration
     */
    public final ParameterDeclaration getParameterAt(final int index) {

        return this.parameterDeclarations.get(index);

    }

    /**
     * <p>Appends the {@link ParameterDeclaration} as the next element into the managed {@link List} of {@link ParameterDeclaration}s
     * & returns a reference to the target {@link Parameters} instance.</p>
     * @param parameterDeclaration The {@link ParameterDeclaration} to append to the end of the {@link List}.
     * @return Reference to the target {@link Parameters} instance.
     * @since 1.0.0
     * @see ParameterDeclaration
     * @see List
     */
    public final Parameters append(final ParameterDeclaration parameterDeclaration) {

        this.parameterDeclarations.add(parameterDeclaration);

        this.parameterTypes.put(parameterDeclaration.getName().toString(), parameterDeclaration.getType());

        return this;

    }

}