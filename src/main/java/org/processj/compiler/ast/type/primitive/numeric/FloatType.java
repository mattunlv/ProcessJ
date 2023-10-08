package org.processj.compiler.ast.type.primitive.numeric;

import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.type.Type;

/**
 * <p>Encapsulates a 32-bit IEEE 754 floating-point {@link Type}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see NumericType
 */
public class FloatType extends NumericType {

    /// -----------------
    /// Private Constants

    /**
     * <p>The minimum value a {@link FloatType} can represent in Two's complement.</p>
     * @since 1.0.0
     */
    private final static double Minimum = -0x1.fffffeP+127f;

    /**
     * <p>The maximum value a {@link FloatType} can represent in Two's complement.</p>
     * @since 1.0.0
     */
    private final static double Maximum = 0x1.fffffeP+127f;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link FloatType} to its' default state.</p>
     * @since 1.0.0
     */
    public FloatType() {
        super(new Name("float"));
    }

    /**
     * <p>Initializes the {@link NumericType} to its' default state.</p>
     * @since 1.0.0
     */
    public FloatType(final Token token) {
        super(token);
    }

    /// ------
    /// Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link FloatType} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link FloatType} & both
     *         represent the same {@link Type} via name.
     * @since 1.0.0
     * @see Object
     * @see Type
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof FloatType);

    }

    /// ----
    /// Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link FloatType}.</p>
     * @return The internal {@link String} signature representing the {@link FloatType}.
     * @since 1.0.0
     * @see String
     */
    @Override
    public final String getSignature() {

        return "F";

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

    /// ------------
    /// NumericType

    /**
     * <p>Returns the minimum value that can be represented by the {@link NumericType} as a double.</p>
     * @return the minimum value that can be represented by the {@link NumericType} as a double.
     * @since 1.0.0
     */
    @Override
    public double getDoubleRangeMinimum() {

        return FloatType.Minimum;

    }

    /**
     * <p>Returns the maximum value that can be represented by the {@link NumericType} as a double.</p>
     * @return the maximum value that can be represented by the {@link NumericType} as a double.
     * @since 1.0.0
     */
    @Override
    public double getDoubleRangeMaximum() {

        return FloatType.Maximum;

    }

}
