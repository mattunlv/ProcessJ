package org.processj.compiler.ast.type;

import org.processj.compiler.ast.*;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class NamedType extends AST implements Type {

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

        // Trivial check
        //return (that instanceof Type)
                // And if the specified Instance is a NamedType
                //&& ((that instanceof NamedType)
                // Join the result with the recurred invocation
                //? this.equals(((NamedType) that).getType())
                // Otherwise, with something acceptable
                //: this.toString().equals(that.toString()));


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
     *
     * @param visitor The {@link Visitor} to dispatch.
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        visitor.visitNamedType(this);

    }

    @Override
    public boolean isTypeEqualTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeEquivalentTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeLessThan(Object that) {
        return false;
    }

    @Override
    public boolean isTypeGreaterThan(Object that) {
        return false;
    }

    @Override
    public boolean isTypeLessThanOrEqualTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeGreaterThanOrEqualTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeCeilingOf(Object that) {
        return false;
    }

    @Override
    public boolean isSubTypeOf(Object that) {
        return false;
    }

    @Override
    public boolean isAssignmentCompatibleTo(Object that) {
        return false;
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
    public Type addDimension() {
        return null;
    }

    @Override
    public Type clearDepth() {
        return null;
    }

    @Override
    public int getDepth() {
        return 0;
    }

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
    public boolean typeEqual(final Type that) {

        return this.equals(that);

    }

    // TODO
    public boolean typeEquivalent(final Type that) {

        return this.equals(that);

    }

    // TODO
    public boolean typeAssignmentCompatible(final Type that) {

        return this.equals(that);

    }

}
