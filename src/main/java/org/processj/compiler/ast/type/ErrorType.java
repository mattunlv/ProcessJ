package org.processj.compiler.ast.type;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class ErrorType extends Type {

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

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ChannelEndType}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitErrorType(ErrorType)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitErrorType(this);

    }

    // *************************************************************************
    // ** Type Related Methods

    // Error types should not be checked like this...
    @Override 
    public final boolean typeEqual(Type t) {
        return false;
    }

    // Error types should not be checked like this...
    @Override 
    public final boolean typeEquivalent(Type t) {
        return false;
    }

    // Error types should not be checked like this...
    @Override 
    public final boolean typeAssignmentCompatible(Type t) {
        return false;
    }

}