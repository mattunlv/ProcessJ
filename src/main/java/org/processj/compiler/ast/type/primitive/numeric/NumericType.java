package org.processj.compiler.ast.type.primitive.numeric;

import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.ast.type.primitive.BarrierType;
import org.processj.compiler.ast.type.primitive.PrimitiveType;

/**
 * <p>Encapsulates a {@link Type} that's binary-encoded.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see NumericType
 */
public abstract class NumericType extends PrimitiveType {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link NumericType} with the specified {@link Name}.</p>
     * @param name The {@link Name} corresponding with the {@link NumericType}.
     * @since 1.0.0
     * @see Name
     */
    public NumericType(final Name name) {
        super(name);
    }

    /**
     * <p>Initializes the {@link NumericType} to its' default state.</p>
     * @since 1.0.0
     */
    public NumericType(final Token token) {
        super(token);
    }

    /// --------------------------
    /// Protected Abstract Methods

    /**
     * <p>Returns the minimum value that can be represented by the {@link NumericType} as a double.</p>
     * @return the minimum value that can be represented by the {@link NumericType} as a double.
     * @since 1.0.0
     */
    public abstract double getDoubleRangeMinimum();

    /**
     * <p>Returns the maximum value that can be represented by the {@link NumericType} as a double.</p>
     * @return the maximum value that can be represented by the {@link NumericType} as a double.
     * @since 1.0.0
     */
    public abstract double getDoubleRangeMaximum();

    /// ------
    /// Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link NumericType} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link NumericType} & both
     *         represent the same {@link Type} via name.
     * @since 1.0.0
     * @see Object
     * @see Type
     */
    @Override
    public boolean equals(final Object that) {

        return super.equals(that) && (that instanceof NumericType);

    }

    /// -------------
    /// PrimitiveType

    /**
     * <p>Returns a flag indicating if the {@link NumericType} instance is {@link Type} equal to the specified
     * {@link Object}. A {@link NumericType} is {@link Type} equal to some {@link Type} if and only if the {@link Type}
     * is an {@link NumericType}; {@link Type} equality is asserted by name equality.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type equal to the {@link NumericType}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public final boolean isTypeEqualTo(final Object that) {

        return this.equals(that);

    }

    /**
     * <p>Returns a flag indicating if the {@link NumericType} instance is {@link Type} equivalent to the specified
     * {@link Object}. An {@link NumericType} is {@link Type} equivalent to some {@link Type} if and only if the
     * {@link Type} is a {@link NumericType}; {@link Type} equivalence is asserted by name equivalence.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type equivalent to the {@link NumericType}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public final boolean isTypeEquivalentTo(final Object that) {

        return this.equals(that);

    }

    /**
     * <p>Returns a flag indicating if the {@link NumericType} instance is {@link Type} less than the specified
     * {@link Object}. An {@link NumericType} is {@link Type} less than some {@link Type} if and only if the
     * {@link Type} is a {@link NumericType} and the range of the {@link NumericType} is a proper subset of the
     * specified {@link Type}; The {@link Type} less than condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type greater than the {@link NumericType}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public boolean isTypeLessThan(final Object that) {

        // Return the result to the subset check; for Type supremacy, the specified Type's range
        // must cover more representations, i.e. a wider range on both extrema
        return (that instanceof NumericType)
                && (this.getDoubleRangeMinimum() > ((NumericType) that).getDoubleRangeMinimum())
                && (this.getDoubleRangeMaximum() < ((NumericType) that).getDoubleRangeMaximum());

    }

    /**
     * <p>Returns a flag indicating if the {@link NumericType} instance is {@link Type} greater than the specified
     * {@link Object}. An {@link NumericType} is {@link Type} greater than some {@link Type} if and only if the
     * {@link Type} is a {@link NumericType} and the range of the specified {@link Type} is a proper subset of the
     * {@link NumericType}; The {@link Type} greater than condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type less than to the {@link NumericType}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public final boolean isTypeGreaterThan(final Object that) {

        // Simply invert the check
        return (that instanceof NumericType) && ((NumericType) that).isTypeLessThan(this);

    }

    /**
     * <p>Returns a flag indicating if the {@link NumericType} instance is {@link Type} less than or equal to the
     * specified {@link Object}. An {@link NumericType} is {@link Type} less than or equal to some {@link Type} if
     * and only if the the range of the {@link NumericType} is a proper subset or equal set of the specified
     * {@link Type}'s range; The {@link Type} less than or equal to to condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type greater than or equal to the
     * {@link NumericType}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public final boolean isTypeLessThanOrEqualTo(final Object that) {

        // Check for both; trivial check first
        return this.equals(that) || this.isTypeLessThan(that);

    }

    /**
     * <p>Returns a flag indicating if the {@link NumericType} instance is {@link Type} greater than or equal to the
     * specified {@link Object}. An {@link NumericType} is {@link Type} less than or equal to some {@link Type} if
     * and only if the the range of the {@link NumericType} is a proper superset or equal set of the specified
     * {@link Type}'s range; The {@link Type} greater than or equal to to condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type less than or equal to the
     * {@link NumericType}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public final boolean isTypeGreaterThanOrEqualTo(final Object that) {

        // Check for both; trivial check first
        return this.equals(that) || this.isTypeGreaterThan(that);

    }

    /**
     * <p>Returns a flag indicating if the {@link NumericType} instance is the {@link Type} ceiling of both the
     * specified {@link Type} & itself. {@link Type} ceiling is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the {@link NumericType} instance is the {@link Type} ceiling of both the
     * specified {@link Type} & itself.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public final boolean isTypeCeilingOf(final Object that) {

        return this.isTypeGreaterThanOrEqualTo(that);

    }

    /**
     * <p>Returns a flag indicating if the {@link NumericType} instance is a sub{@link Type} of the
     * specified {@link Type}. Sub{@link Type} is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the {@link NumericType} instance is the sub{@link Type} of the
     * specified {@link Type} & itself.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public final boolean isSubTypeOf(final Object that) {

        return this.isTypeLessThan(that);

    }

    /**
     * <p>Returns a flag indicating if the {@link NumericType} instance is assignment compatible to the
     * specified {@link Type}. Assignment compatability is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the {@link NumericType} instance is assignment compatible to the
     * specified {@link Type}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public final boolean isAssignmentCompatibleTo(final Object that) {

        return this.isTypeLessThanOrEqualTo(that);

    }

}
