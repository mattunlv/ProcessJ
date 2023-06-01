package org.processj.ast;

import org.processj.utilities.Visitor;

public class ExternType extends Type {

    /// --------------
    /// Private Fields

    /**
     * <p>{@link String} value of the {@link ExternType}'s name.</p>
     */
    private final String name;

    /// ------------
    /// Constructors

    public ExternType(final Name name) {
        super(new AST[] { name });
        this.name = (name != null) ? name.toString() : "";
    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ExternType} & both represent
     * the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ExternType} & both represent
     *         the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof ExternType);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link Type}.</p>
     * @return Literal {@link String} representation of the {@link Type}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.name;

    }

    /// --------------------
    /// org.processj.ast.AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ExternType}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitExternType(ExternType)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) {

        return visitor.visitExternType(this);

    }

    /// ---------------------
    /// org.processj.ast.Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ExternType}.</p>
     * @return The internal {@link String} signature representing the {@link ExternType}.
     * @since 0.1.0
     */
    @Override
    public final String getSignature() {

        return "E" + this.name + ";";

    }

    // *************************************************************************
    // ** Type Related Methods

    // α = ExternType(n1) and β = ExternType(n2)
    // α =T β ⇔ n1 == n2
    @Override
    public boolean typeEqual(final Type that) {

        return this.equals(that);

    }

    // α ~T β ⇔ α =T β
    @Override
    public boolean typeEquivalent(final Type that) {

        return this.equals(that);

    }

    // α :=T β ⇔ α =T β
    @Override
    public boolean typeAssignmentCompatible(final Type that) {

        return this.equals(that);

    }

}