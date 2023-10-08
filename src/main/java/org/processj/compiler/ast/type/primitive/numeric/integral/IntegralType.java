package org.processj.compiler.ast.type.primitive.numeric.integral;

import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.ast.type.primitive.numeric.NumericType;

/**
 * <p>Encapsulates an n-bit integral {@link Type} represented as a two's complement binary integer.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see NumericType
 */
public abstract class IntegralType extends NumericType {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link IntegralType} with the specified {@link Name}.</p>
     * @param name The {@link Name} corresponding with the {@link IntegralType}.
     * @since 1.0.0
     * @see Name
     */
    public IntegralType(final Name name) {
        super(name);
    }
    /**
     * <p>Initializes the {@link IntegralType} to its' default state.</p>
     * @since 1.0.0
     */
    public IntegralType(final Token token) {
        super(token);
    }
    /// --------------------------
    /// Protected Abstract Methods

    /**
     * <p>Returns the minimum value that can be represented by the {@link IntegralType} as a long.</p>
     * @return the minimum value that can be represented by the {@link IntegralType} as a long.
     * @since 1.0.0
     */
    protected abstract long getRangeMinimum();

    /**
     * <p>Returns the maximum value that can be represented by the {@link IntegralType} as a long.</p>
     * @return the maximum value that can be represented by the {@link IntegralType} as a long.
     * @since 1.0.0
     */
    protected abstract long getRangeMaximum();

    /// ------
    /// Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link IntegralType} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link IntegralType} & both
     *         represent the same {@link Type} via name.
     * @since 1.0.0
     * @see Object
     * @see Type
     */
    @Override
    public boolean equals(final Object that) {

        return super.equals(that) && (that instanceof IntegralType);

    }

    /// -----------
    /// NumericType

    /**
     * <p>Returns the minimum value that can be represented by the {@link IntegralType} as a double.</p>
     * @return the minimum value that can be represented by the {@link IntegralType} as a double.
     * @since 1.0.0
     */
    @Override
    public double getDoubleRangeMinimum() {

        return (double) this.getRangeMinimum();

    }

    /**
     * <p>Returns the maximum value that can be represented by the {@link IntegralType} as a double.</p>
     * @return the maximum value that can be represented by the {@link IntegralType} as a double.
     * @since 1.0.0
     */
    @Override
    public double getDoubleRangeMaximum() {

        return (double) this.getRangeMaximum();

    }

    /**
     * <p>Returns a flag indicating if the {@link IntegralType} instance is {@link Type} less than the specified
     * {@link Object}. An {@link IntegralType} is {@link Type} less than some {@link Type} if and only if the
     * {@link Type} is a {@link NumericType} and the range of the {@link IntegralType} is a proper subset of the
     * specified {@link Type}; The {@link Type} less than condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type greater than the {@link IntegralType}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public boolean isTypeLessThan(final Object that) {

        // Return the result to the subset check; for Type supremacy, the specified Type's range
        // must cover more representations, i.e. a wider range on both extrema
        return ((that instanceof IntegralType)
                && (this.getRangeMinimum() > ((IntegralType) that).getRangeMinimum())
                && (this.getRangeMaximum() < ((IntegralType) that).getRangeMaximum()))
                // Otherwise, check for Numeric Type
                || super.isTypeLessThan(that);

    }

}
