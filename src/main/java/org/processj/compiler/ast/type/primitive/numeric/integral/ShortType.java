package org.processj.compiler.ast.type.primitive.numeric.integral;

import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.type.Type;

/**
 * <p>Encapsulates an 16-bit integral {@link Type} represented as a two's complement binary integer.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see IntegralType
 */
public class ShortType extends IntegralType {

    /// -----------------
    /// Private Constants

    /**
     * <p>The minimum value a {@link ShortType} can represent in Two's complement.</p>
     * @since 1.0.0
     */
    private final static long Minimum = 0xFFFFFFFFFFFF8000L;

    /**
     * <p>The maximum value a {@link ShortType} can represent in Two's complement.</p>
     * @since 1.0.0
     */
    private final static long Maximum = 0x0000000000007FFFL;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ShortType} to its' default state.</p>
     * @since 1.0.0
     */
    public ShortType() {
        super(new Name("short"));
    }
    /**
     * <p>Initializes the {@link ShortType} to its' default state.</p>
     * @since 1.0.0
     */
    public ShortType(final Token token) {
        super(token);
    }
    /// ------
    /// Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ShortType} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ShortType} & both
     *         represent the same {@link Type} via name.
     * @since 1.0.0
     * @see Object
     * @see Type
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof ShortType);

    }

    /// ----
    /// Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ShortType}.</p>
     * @return The internal {@link String} signature representing the {@link ShortType}.
     * @since 1.0.0
     * @see String
     */
    @Override
    public final String getSignature() {

        return "S";

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
    /// IntegralType

    /**
     * <p>Returns the minimum value that can be represented by the {@link ShortType} as a long.</p>
     * @return the minimum value that can be represented by the {@link ShortType} as a long.
     * @since 1.0.0
     */
    @Override
    protected final long getRangeMinimum() {

        return ShortType.Minimum;

    }

    /**
     * <p>Returns the maximum value that can be represented by the {@link ShortType} as a long.</p>
     * @return the maximum value that can be represented by the {@link ShortType} as a long.
     * @since 1.0.0
     */
    @Override
    protected final long getRangeMaximum() {

        return ShortType.Maximum;

    }

}