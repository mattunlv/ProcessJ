package org.processj.compiler.ast.packages;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;

/**
 * <p>Class that represents a {@link Pragma}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca.
 * @version 1.0.0
 * @since 0.1.0
 * @see AST
 */
public final class Pragma extends AST {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Name} instance corresponding to the {@link Pragma}.</p>
     * @since 0.1.0
     * @see Name
     */
    private final Name name;

    /**
     * <p>The {@link String} literal value specified with the {@link Pragma}.</p>
     * @since 0.1.0
     * @see String
     */
    private final String value;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Pragma} to its' default state with the specified {@link Name} &amp; {@link String}.</p>
     * @param name The {@link Name} corresponding to the {@link Pragma}.
     * @param stringLiteral The {@link String} literal value specified with the {@link Pragma}.
     * @since 0.1.0
     * @see Name
     * @see String
     */
    public Pragma(final Name name, final String stringLiteral) {
        super(name);

        this.name = name;
        this.value = stringLiteral;

    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} representation of the {@link Pragma}.</p>
     * @return {@link String} representation of the {@link Pragma}.
     * @since 0.1.0
     * @see String
     */
    @Override
    public String toString() {

        return this.name.toString() + ':' + this.value;

    }

    /**
     * <p>Returns a calculated integer hashcode value corresponding to the state of the {@link Pragma}'s fields.</p>
     * @return A calculated integer hashcode value.
     * @since 0.1.0
     */
    @Override
    public int hashCode() {

        return (this.name + this.value).hashCode();

    }

    /**
     * <p>Returns a boolean flag indicating if the specified {@link Pragma} is identical to the
     * given {@link Object}. This method will check the super class invocation of {@link Object#equals(Object)},
     * the instance of the specified {@link Object}, the {@link Name}, & the {@link String} literal value.</p>
     * @param that The {@link Object} instance to check against the given instance.
     * @return boolean flag indicating if the specified instance is identical to this.
     * @since 0.1.0
     * @see Object
     * @see String
     */
    @Override
    public boolean equals(final Object that) {

        return super.equals(that) && (that instanceof Pragma)
                && this.toString().equals(that.toString())
                && this.value.equals(((Pragma) that).value);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the {@link String} literal value specified with the {@link Pragma}.</p>
     * @return {@link String} literal value specified with the {@link Pragma}.
     * @since 0.1.0
     * @see String
     */
    public String getValue() {

        return this.value;

    }

}