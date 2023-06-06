package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

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
public class Pragma extends AST {

    /// ---------------
    /// Private Members

    /**
     * <p>The {@link Name} corresponding to the {@link Pragma}.</p>
     */
    private final Name   name    ;

    /**
     * <p>The {@link String} literal value specified with the {@link Pragma}.</p>
     */
    private final String value   ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Pragma} to its' default state.</p>
     * @param name The {@link Name} corresponding to the {@link Pragma}.
     * @param stringLiteral The {@link String} literal value specified with the {@link Pragma}.
     * @since 0.1.0
     */
    public Pragma(final Name name, final String stringLiteral) {
        super(new AST[] { name });

        this.name   = name          ;
        this.value  = stringLiteral ;

    }

    /// ----------------
    /// java.lang.Object

    // TODO: Hashcode

    /**
     * <p>Returns a boolean flag indicating if the specified {@link Pragma} is identical to the
     * given {@link Pragma}. This method will check the super class invocation of {@link Object#equals(Object)},
     * the instance of the specified {@link Object}, the {@link Name}, & the {@link String} literal value.</p>
     * @param that The {@link Object} instance to check against the given instance.
     * @return boolean flag indicating if the specified instance is identical to this.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof Pragma)
                && this.toString().equals(that.toString())
                && this.value.equals(((Pragma) that).value);

    }

    /**
     * <p>Returns the {@link String} representation of the {@link Pragma}.</p>
     * @return {@link String} representation of the {@link Pragma}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.name.getName().toUpperCase();

    }

    /// --------------------
    /// org.processj.ast.AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link Pragma}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitPragma} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitPragma(this);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the {@link String} literal value specified with the {@link Pragma}.</p>
     * @return {{@link String} literal value specified with the {@link Pragma}.
     * @since 0.1.0
     */
    public String getValue() {

        return this.value;

    }

}