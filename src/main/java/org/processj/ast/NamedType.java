package org.processj.ast;

import org.processj.utilities.Visitor;

public class NamedType extends Type implements DefineTopLevelDecl {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Name} instance corresponding with the {@link NamedType}.</p>
     */
    private final Name      name            ;

    /**
     * <p>{@link String} value of the {@link NamedType}'s name.</p>
     */
    private final String    nameLiteral     ;

    /**
     * <p>The {@link Name}'s resolved {@link Type}; either through the {@link Compilation}'s scope or a resolved
     * package access.</p>
     */
    private Type            resolvedType    ;

    private DefineTopLevelDecl resolvedTopLevelDecl = null; // could be a SymbolTable
    // This field is set to the actual type this named type resolved to.
    // This is done in the resolve method in org.processj.typechecker/TypeChecker.java

    /// ------------
    /// Constructors

    public NamedType(final Name name) {
        super(new AST[] { name });
        this.nameLiteral    = (name != null) ? name.toString() : "";
        this.name           = name;
        this.resolvedType   = null;
    }

    public NamedType(final Name name, final Type type) {
        super(new AST[] { name });
        this.nameLiteral    = (name != null) ? name.toString() : "";
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

        return (this.resolvedType == null) ? this.nameLiteral : this.resolvedType.toString();

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
    public final <S> S visit(final Visitor<S> visitor) {

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

        return "L" + this.nameLiteral + ";";

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link NamedType} has a bound, resolved {@link Type}.</p>
     * @return Flag indicating if the {@link NamedType} has a bound, resolved {@link Type}.
     * @since 0.1.0
     */
    public final boolean hasResolvedType() {

        return this.resolvedType != null;

    }

    /**
     * <p>Returns a flag indicating if the {@link Name} is prefixed with a fully-qualified package name.</p>
     * @return Flag indicating if the {@link Name} is prefixed with a fully-qualified package name.
     * @since 0.1.0
     */
    public final boolean specifiesPackage() {

        return this.name.specifiesPackage();

    }

    public Name name() {

        return this.name;

    }

    public final Type getType() {

        return this.resolvedType;

    }

    public final void setType(Type type) {

        this.resolvedType = type;

    }

    public void setResolvedTopLevelDecl(DefineTopLevelDecl td) {
        this.resolvedTopLevelDecl = td;
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
