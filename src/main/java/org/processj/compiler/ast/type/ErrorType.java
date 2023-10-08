package org.processj.compiler.ast.type;

import org.processj.compiler.ast.Name;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class ErrorType implements Type {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ErrorType} to its' default state.</p>
     * @since 0.1.0
     */
    public ErrorType() {
        super();
    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ErrorType} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ErrorType} & both
     *         represent the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof ErrorType);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link ErrorType}.</p>
     * @return Literal {@link String} representation of the {@link ErrorType}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return "<Error>";

    }

    /// --------------------
    /// org.processj.ast.AST

    @Override
    public Name getName() {
        return null;
    }

    @Override
    public String getSignature() {
        return null;
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

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ChannelEndType}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitErrorType(ErrorType)} method.</p>
     *
     * @param visitor The {@link Visitor} to dispatch.
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        visitor.visitErrorType(this);

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

    // *************************************************************************
    // ** Type Related Methods

    // Error types should not be checked like this...
    public final boolean typeEqual(Type t) {
        return false;
    }

    // Error types should not be checked like this...
    public final boolean typeEquivalent(Type t) {
        return false;
    }

    // Error types should not be checked like this...
    public final boolean typeAssignmentCompatible(Type t) {
        return false;
    }

}