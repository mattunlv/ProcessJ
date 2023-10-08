package org.processj.compiler.ast.type;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.declarative.ConstantDeclaration;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Encapsulates an {@link ArrayType}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see Type
 */
public class ArrayType extends AST implements Type {

    /// --------------
    /// Private Fields

    /**
     * <p>The integer value corresponding to the {@link ArrayType}'s depth as specified with '[]' in the source file.
     * </p>
     * @since 1.0.0
     */
    private final int   depth           ;

    /**
     * <p>The {@link ArrayType}'s component (base) {@link Type}. Must not be an instance of {@link ArrayType}.</p>
     * @since 1.0.0
     * @see Type
     */
    private Type        componentType   ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ArrayType} to its' default state with the specified {@link Type} & integer depth.
     * If the specified {@link Type} is an {@link ArrayType}, this will aggregate its' depth & set the
     * {@link ArrayType}'s component {@link Type} to the same component {@link Type} of the specified
     * {@link ArrayType}.</p>
     * @param componentType The {@link Type} to bind to the {@link ConstantDeclaration}.
     * @param depth The integer value of the {@link ArrayType}'s depth as specified by '[]'.
     * @since 1.0.0
     * @see AST
     * @see Type
     */
    public ArrayType(final Type componentType, final int depth) {

        this.componentType = componentType;
        this.depth         = ((componentType instanceof ArrayType)
                ? ((ArrayType) componentType).getDepth() : 0) + depth  ;

    }

    /// ------
    /// Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ArrayType} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ArrayType} & both
     *         represent the same {@link Type} via name.
     * @since 1.0.0
     * @see Object
     * @see Type
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that)
                && (that instanceof ArrayType)
                && this.componentType.equals(((ArrayType) that).componentType);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link ArrayType}.</p>
     * @return Literal {@link String} representation of the {@link ArrayType}.
     * @since 1.0.0
     * @see String
     */
    @Override
    public final String toString() {

        return this.componentType + "[]".repeat(this.depth);

    }

    /// ---
    /// AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ArrayType}; Updates the
     * {@link Visitor}'s {@link Context} & dispatches the {@link Visitor}'s
     * {@link Visitor#visitArrayType(ArrayType)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @since 1.0.0
     * @see Visitor
     * @see Phase.Error
     * @see Context
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        //visitor.setContext(this.openContext(visitor.getContext()));

        visitor.visitArrayType(this);

        //visitor.setContext(this.closeContext());

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

    /// ----
    /// Type

    @Override
    public Name getName() {
        return null;
    }

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ArrayType}.</p>
     * @return The internal {@link String} signature representing the {@link ArrayType}.
     * @since 1.0.0
     * @see Type
     * @see String
     */
    @Override
    public final String getSignature() {

        // Initialize the StringBuilder
        StringBuilder stringBuilder = new StringBuilder(this.componentType.getSignature());

        // Iterate through the depth & sandwich the current string
        for(int currentDepth = 0; currentDepth < this.depth; currentDepth++)
            stringBuilder = new StringBuilder("[" + stringBuilder + ";");

        // Return the result
        return stringBuilder.toString();

    }

    @Override
    public Type addDimension() {
        return null;
    }

    @Override
    public Type clearDepth() {
        return null;
    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the component (base) {@link Type} composing the {@link ArrayType}.</p>
     * @return The component (base) {@link Type} composing the {@link ArrayType}.
     * @since 1.0.0
     * @see Type
     */
    public final Type getComponentType() {

        return this.componentType;

    }

    /**
     * <p>Returns the integer value corresponding with the {@link ArrayType}'s depth..</p>
     * @return The integer value corresponding with the {@link ArrayType}'s depth.
     * @since 1.0.0
     */
    public final int getDepth() {

        return this.depth;

    }

    /**
     * <p>Mutates the {@link ArrayType}'s component {@link Type}.</p>
     * @param componentType The {@link Type} to update.
     * @since 1.0.0
     * @see Type
     */
    public final void setComponentType(final Type componentType) {

        this.componentType = componentType;

    }

    // *************************************************************************
    // ** Type Related Methods

    // if α=Array(t1,I1) ∧ β=Array(t2,I2)
    // α =T β ⇔ Array?(α) ∧ Array?(β) ∧ (t1 =T t2) ∧ ((I1 =I2) ∨ (I1 =⊥) ∨ (I2 =⊥))
    public boolean typeEqual(final Type type) {

        return this.equals(type);

    }

    public boolean typeEquivalent(Type t) {

        return this.equals(t);

    }

    public boolean typeAssignmentCompatible(Type t) {

        return this.equals(t);

    }

}