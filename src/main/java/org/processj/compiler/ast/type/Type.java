package org.processj.compiler.ast.type;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;

public abstract class Type extends AST {

    private final Name name;

    public Type() {
        // must only be called from ErrorType
        super();
        this.name = new Name("");
    }

    public Type(AST a) {
        super(a);
        this.name = new Name("");
    }

    public Type(final AST... children) {
        super(children);
        this.name = new Name("");
    }

    public Type(Token t) {
        super(t);
        this.name = new Name(t.lexeme);
    }

    public Type(final Name name) {

        this.name = name;

    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link Type} & both represent
     * the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link Type} & both represent
     *         the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public boolean equals(final Object that) {

        // Trivial check
        return (that instanceof Type)
                // And if the specified Instance is a NamedType
                && ((that instanceof NamedType)
                // Join the result with the recurred invocation
                ? this.equals(((NamedType) that).getType())
                // Otherwise, with something acceptable
                : this.toString().equals(that.toString()));

    }



    /**
     * <p>Returns a literal {@link String} representation of the {@link Type}.</p>
     * @return Literal {@link String} representation of the {@link Type}.
     * @since 0.1.0
     */
    @Override
    public String toString() {

        return "null";

    }

    public String getPackageName() {

        return "";

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the internal {@link String} signature representing the {@link Type}.</p>
     * @return The internal {@link String} signature representing the {@link Type}.
     * @since 0.1.0
     */
    public String getSignature() {

        return "";

    }

    public boolean assignable() {

        return (!this.toString().equals("null") && !this.toString().equals("void"));

    }

    /**
     * <p>Returns the {@link Type} that composes the instance, or qualifies parameteric {@link Type}s.
     * In most cases this method returns the {@link Type} itself.</p>
     * @return {@link Type} that composes the instance, or qualifies parameteric {@link Type}s
     * @since 0.1.0
     */
    public Type getComponentType() {

        return this;

    }

    public Name getName() {

        return this.name;

    }

    public boolean typeEqual(Type other) { return false; }

    public boolean typeEquivalent(Type other) { return false; }

    public boolean typeAssignmentCompatible(Type other) { return false;}
    
    /*
     * NOTE: the general implementation of all these should be to return 'false'.
     * Each type should in turn implement which ever one applies to it. 
     */

    // Reimplemented in PrimitiveType
    public boolean isIntegerType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isBooleanType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isByteType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isShortType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isCharType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isLongType() {
        return false;
    }

    public boolean isTimerType() {
        return false;
    }

    public boolean isBarrierType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isVoidType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isStringType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isFloatType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isDoubleType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isNumericType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isIntegralType() {
        return false;
    }

}
