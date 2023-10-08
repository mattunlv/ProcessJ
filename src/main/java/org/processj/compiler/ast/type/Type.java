package org.processj.compiler.ast.type;

import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.type.primitive.numeric.NumericType;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public interface Type {

    Name getName();

    /**
     * <p>Returns the internal {@link String} signature representing the {@link Type}.</p>
     * @return The internal {@link String} signature representing the {@link Type}.
     * @since 0.1.0
     */
    String getSignature();

    Type addDimension();

    Type clearDepth();

    int getDepth();

    void accept(final Visitor visitor) throws Phase.Error;

    /**
     * <p>Returns a flag indicating if the {@link Type} instance is {@link Type} equal to the specified
     * {@link Object}. A {@link Type} is {@link Type} equal to some {@link Type} if and only if the {@link Type}
     * is an {@link Type}; {@link Type} equality is asserted by name equality.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type equal to the {@link Type}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    boolean isTypeEqualTo(final Object that);

    /**
     * <p>Returns a flag indicating if the {@link Type} instance is {@link Type} equivalent to the specified
     * {@link Object}. An {@link Type} is {@link Type} equivalent to some {@link Type} if and only if the
     * {@link Type} is a {@link Type}; {@link Type} equivalence is asserted by name equivalence.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type equivalent to the {@link Type}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    boolean isTypeEquivalentTo(final Object that);

    /**
     * <p>Returns a flag indicating if the {@link Type} instance is {@link Type} less than the specified
     * {@link Object}. An {@link Type} is {@link Type} less than some {@link Type} if and only if the
     * {@link Type} is a {@link NumericType} and the range of the {@link Type} is a proper subset of the
     * specified {@link Type}; The {@link Type} less than condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type greater than the {@link Type}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    boolean isTypeLessThan(final Object that);

    /**
     * <p>Returns a flag indicating if the {@link Type} instance is {@link Type} greater than the specified
     * {@link Object}. An {@link Type} is {@link Type} greater than some {@link Type} if and only if the
     * {@link Type} is a {@link NumericType} and the range of the specified {@link Type} is a proper subset of the
     * {@link Type}; The {@link Type} greater than condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type less than to the {@link Type}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    boolean isTypeGreaterThan(final Object that);

    /**
     * <p>Returns a flag indicating if the {@link Type} instance is {@link Type} less than or equal to the
     * specified {@link Object}. An {@link Type} is {@link Type} less than or equal to some {@link Type} if
     * and only if the the range of the {@link Type} is a proper subset or equal set of the specified
     * {@link Type}'s range; The {@link Type} less than or equal to to condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type greater than or equal to the
     * {@link Type}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    boolean isTypeLessThanOrEqualTo(final Object that);

    /**
     * <p>Returns a flag indicating if the {@link Type} instance is {@link Type} greater than or equal to the
     * specified {@link Object}. An {@link Type} is {@link Type} less than or equal to some {@link Type} if
     * and only if the the range of the {@link Type} is a proper superset or equal set of the specified
     * {@link Type}'s range; The {@link Type} greater than or equal to to condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type less than or equal to the
     * {@link Type}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    boolean isTypeGreaterThanOrEqualTo(final Object that);

    /**
     * <p>Returns a flag indicating if the {@link Type} instance is the {@link Type} ceiling of both the
     * specified {@link Type} & itself. {@link Type} ceiling is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the {@link Type} instance is the {@link Type} ceiling of both the
     * specified {@link Type} & itself.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    boolean isTypeCeilingOf(final Object that);

    /**
     * <p>Returns a flag indicating if the {@link Type} instance is a sub{@link Type} of the
     * specified {@link Type}. Sub{@link Type} is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the {@link Type} instance is the sub{@link Type} of the
     * specified {@link Type} & itself.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    boolean isSubTypeOf(final Object that);

    /**
     * <p>Returns a flag indicating if the {@link Type} instance is assignment compatible to the
     * specified {@link Type}. Assignment compatability is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the {@link Type} instance is assignment compatible to the
     * specified {@link Type}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    boolean isAssignmentCompatibleTo(final Object that);


}
