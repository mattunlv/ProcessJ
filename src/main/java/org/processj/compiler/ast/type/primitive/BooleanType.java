package org.processj.compiler.ast.type.primitive;

import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.type.ExternType;
import org.processj.compiler.ast.type.Type;

/**
 * <p>Encapsulates a {@link Type} that represents either 'true' or 'false'.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see PrimitiveType
 */
public class BooleanType extends PrimitiveType {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link BooleanType} to its' default state.</p>
     * @since 1.0.0
     */
    public BooleanType() {
        super(new Name("boolean"));
    }

    /**
     * <p>Initializes the {@link BooleanType} to its' default state.</p>
     * @since 1.0.0
     */
    public BooleanType(final Token token) {
        super(token);
    }


    /// -------------
    /// PrimitiveType

    /// ---------------------
    /// org.processj.ast.Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ExternType}.</p>
     * @return The internal {@link String} signature representing the {@link ExternType}.
     * @since 0.1.0
     */
    @Override
    public String getSignature() {

        return "Z";

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
     * <p>Returns a flag indicating if the {@link BooleanType} instance is {@link Type} equal to the specified
     * {@link Object}. A {@link BooleanType} is {@link Type} equal to some {@link Type} if and only if the {@link Type}
     * is an {@link BooleanType}; {@link Type} equality is asserted by name equality.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type equal to the {@link BooleanType}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public final boolean isTypeEqualTo(final Object that) {

        return this.equals(that);

    }

    /**
     * <p>Returns a flag indicating if the {@link BooleanType} instance is {@link Type} equivalent to the specified
     * {@link Object}. An {@link BooleanType} is {@link Type} equivalent to some {@link Type} if and only if the
     * {@link Type} is a {@link BooleanType}; {@link Type} equivalence is asserted by name equivalence.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type equivalent to the {@link BooleanType}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public final boolean isTypeEquivalentTo(final Object that) {

        return this.equals(that);

    }

    /**
     * <p>Returns a flag indicating if the {@link BooleanType} instance is {@link Type} less than the specified
     * {@link Object}. An {@link BooleanType} is {@link Type} less than some {@link Type} if and only if the
     * {@link Type} is a {@link BooleanType} and the range of the {@link BooleanType} is a proper subset of the
     * specified {@link Type}; The {@link Type} less than condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type greater than the {@link BooleanType}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public boolean isTypeLessThan(final Object that) {

        // TODO:
        return true;

    }

    /**
     * <p>Returns a flag indicating if the {@link BooleanType} instance is {@link Type} greater than the specified
     * {@link Object}. An {@link BooleanType} is {@link Type} greater than some {@link Type} if and only if the
     * {@link Type} is a {@link BooleanType} and the range of the specified {@link Type} is a proper subset of the
     * {@link BooleanType}; The {@link Type} greater than condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type less than to the {@link BooleanType}.
     * @since 1.0.0
     * @see Type
     * @see Object
     */
    @Override
    public final boolean isTypeGreaterThan(final Object that) {

        // Simply invert the check
        return (that instanceof BooleanType) && ((BooleanType) that).isTypeLessThan(this);

    }

    /**
     * <p>Returns a flag indicating if the {@link BooleanType} instance is {@link Type} less than or equal to the
     * specified {@link Object}. An {@link BooleanType} is {@link Type} less than or equal to some {@link Type} if
     * and only if the the range of the {@link BooleanType} is a proper subset or equal set of the specified
     * {@link Type}'s range; The {@link Type} less than or equal to to condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type greater than or equal to the
     * {@link BooleanType}.
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
     * <p>Returns a flag indicating if the {@link BooleanType} instance is {@link Type} greater than or equal to the
     * specified {@link Object}. An {@link BooleanType} is {@link Type} less than or equal to some {@link Type} if
     * and only if the the range of the {@link BooleanType} is a proper superset or equal set of the specified
     * {@link Type}'s range; The {@link Type} greater than or equal to to condition is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the specified {@link Object} is type less than or equal to the
     * {@link BooleanType}.
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
     * <p>Returns a flag indicating if the {@link BooleanType} instance is the {@link Type} ceiling of both the
     * specified {@link Type} & itself. {@link Type} ceiling is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the {@link BooleanType} instance is the {@link Type} ceiling of both the
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
     * <p>Returns a flag indicating if the {@link BooleanType} instance is a sub{@link Type} of the
     * specified {@link Type}. Sub{@link Type} is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the {@link BooleanType} instance is the sub{@link Type} of the
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
     * <p>Returns a flag indicating if the {@link BooleanType} instance is assignment compatible to the
     * specified {@link Type}. Assignment compatability is asserted by range.</p>
     * @param that The {@link Object} instance to assert against.
     * @return A flag indicating if the {@link BooleanType} instance is assignment compatible to the
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
