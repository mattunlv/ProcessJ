package org.processj.compiler.ast.type.primitive.numeric;

import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.type.Type;

/**
 * <p>Encapsulates a 64-bit IEEE 754 floating-point {@link Type}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see NumericType
 */
public class DoubleType extends NumericType {

    /// -----------------
    /// Private Constants

    /**
     * <p>The minimum value a {@link DoubleType} can represent in Two's complement.</p>
     * @since 1.0.0
     */
    private final static double Minimum = -0x1.fffffffffffffP+1023;

    /**
     * <p>The maximum value a {@link DoubleType} can represent in Two's complement.</p>
     * @since 1.0.0
     */
    private final static double Maximum = 0x1.fffffffffffffP+1023;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link DoubleType} to its' default state.</p>
     * @since 1.0.0
     */
    public DoubleType() {
        super(new Name("double"));
    }

    /**
     * <p>Initializes the {@link DoubleType} to its' default state.</p>
     * @since 1.0.0
     */
    public DoubleType(final Token token) {
        super(token);
    }

    /// ------
    /// Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link DoubleType} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link DoubleType} & both
     *         represent the same {@link Type} via name.
     * @since 1.0.0
     * @see Object
     * @see Type
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof DoubleType);

    }

    /// ----
    /// Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link DoubleType}.</p>
     * @return The internal {@link String} signature representing the {@link DoubleType}.
     * @since 1.0.0
     * @see String
     */
    @Override
    public final String getSignature() {

        return "D";

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

        return DoubleType.Minimum;

    }

    /**
     * <p>Returns the maximum value that can be represented by the {@link NumericType} as a double.</p>
     * @return the maximum value that can be represented by the {@link NumericType} as a double.
     * @since 1.0.0
     */
    @Override
    public double getDoubleRangeMaximum() {

        return DoubleType.Maximum;

    }

}
