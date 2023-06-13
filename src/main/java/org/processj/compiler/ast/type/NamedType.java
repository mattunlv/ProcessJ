package org.processj.compiler.ast.type;

import org.processj.compiler.ast.*;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class NamedType extends Type {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Name} instance corresponding with the {@link NamedType}.</p>
     */
    private final Name      name            ;

    /**
     * <p>The {@link Name}'s resolved {@link Type}; either through the {@link Compilation}'s scope or a resolved
     * package access.</p>
     */
    private Type            resolvedType    ;

    /// ------------
    /// Constructors

    public NamedType(final Name name) {
        this(name, null);
    }

    public NamedType(final Name name, final Type type) {
        super(new AST[] { name });
        this.name           = name;
        this.resolvedType   = type;
    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of the {@link NamedType}'s
     * resolved {@link Type} & both represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of the {@link NamedType}'s
     *          resolved {@link Type} & both represent the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (this.resolvedType != null) && this.resolvedType.equals(that);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link NamedType}.</p>
     * @return Literal {@link String} representation of the {@link NamedType}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.name.toString();

    }

    /// --------------------
    /// org.processj.ast.AST
    
    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link NamedType}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitNamedType(NamedType)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitNamedType(this);

    }

    /// ---------------------
    /// org.processj.ast.Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link NamedType}.</p>
     * @return The internal {@link String} signature representing the {@link NamedType}.
     * @since 0.1.0
     */
    @Override
    public final String getSignature() {

        return "L" + this.name + ";";

    }

    @Override
    public final String getPackageName() {

        return this.name.getPackageName();

    }

    /// --------------
    /// Public Methods

    public final Name getName() {

        return this.name;

    }

    public final Type getType() {

        return this.resolvedType;

    }

    public final void setType(Type type) {

        this.resolvedType = type;

    }

    // TODO
    @Override
    public boolean typeEqual(final Type that) {

        return this.equals(that);

    }

    // TODO
    @Override
    public boolean typeEquivalent(final Type that) {

        return this.equals(that);

    }

    // TODO
    @Override
    public boolean typeAssignmentCompatible(final Type that) {

        return this.equals(that);

    }

}
