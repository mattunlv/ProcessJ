package org.processj.compiler.phase.jvm.instruction;

import org.processj.compiler.phase.jvm.ClassFile;

/**
 * <p>Fetch field from object.</p>
 * <p>Value: 0xb4 </p>
 * <p>Format: { 0xb4, indexbyte1, indexbyte2 } </p>
 * <p>Stack:
 *      ..., objectref →
 *      ..., value
 * </p>
 * <p>Description:
 *      The unsigned indexbyte1 and indexbyte2 are used to construct an index into the run-time constant pool of the
 *      current class (§2.6), where the value of the index is (indexbyte1 << 8) | indexbyte2.
 *      The run-time constant pool entry at the index must be a symbolic reference to a field (§5.1)
 *      ({@link ClassFile.FieldInfo}), which gives the name and descriptor of
 *      the field as well as a symbolic reference to the class in which the field is to be found. The referenced field
 *      is resolved (§5.4.3.2).
 *      The objectref, which must be of type reference but not an array type, is popped from the operand stack.
 *      The value of the referenced field in objectref is fetched and pushed onto the operand stack.</p>
 * <p>Linking Exceptions:
 *      During resolution of the symbolic reference to the field, any of the errors pertaining to field resolution
 *      (§5.4.3.2) can be thrown. Otherwise, if the resolved field is a static field, {@link getfield} throws an
 *      IncompatibleClassChangeError.</p>
 * <p>Run-time Exceptions:
 *      Otherwise, if objectref is null, the {@link getfield} instruction throws a NullPointerException.</p>
 * <p>Notes:
 *      The {@link getfield} instruction cannot be used to access the length field of an array. The arraylength
 *      instruction (§arraylength) is used instead.</p>
 * @see Instruction
 * @see ClassFile.Field
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public class getfield implements Instruction {

    /// -----------------
    /// Private Constants

    /**
     * <p>The byte value of the {@link Instruction}.</p>
     */
    private final static byte Value     = (byte) 0xb4   ;

    /**
     * <p>The amount of bytes this {@link Instruction} requires.</p>
     * @since 0.1.0
     */
    private final static int  ByteCount = 0x03          ;

    /// --------------
    /// Private Fields

    /**
     * <p>The position corresponding to the field in the current class's constant pool.</p>
     */
    private final short     constantPoolIndex   ;

    /**
     * <p>The byte encoding of this {@link Instruction}.</p>
     * @since 0.1.0
     */
    private final byte[]    bytes               ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link getfield} {@link Instruction} to its' default state with
     * the specified local index.</p>
     * @param constantPoolIndex The short value of the local index corresponding to the local to push
     *                   onto the operand stack when the {@link Instruction} is executed by the JVM.
     * @since 0.1.0
     */
    public getfield(final short constantPoolIndex) {

        this.constantPoolIndex  = constantPoolIndex;
        this.bytes              = new byte[] { Value,
                (byte) (this.constantPoolIndex >> 8), (byte) this.constantPoolIndex};

    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} representation of the {@link Instruction}.</p>
     * @return The {@link String} representation of the {@link Instruction}.
     * @since 0.1.0
     */
    @Override
    public final String toString()  {

        return "getfield #" + this.constantPoolIndex;

    }

    /// -----------
    /// Instruction

    /**
     * <p>Returns the byte encoding corresponding to the {@link Instruction}.</p>
     * @return The byte encoding corresponding to the {@link Instruction}.
     * @since 0.1.0
     */
    @Override
    public final byte[] getBytes() {

        return this.bytes;

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the short value of the local index corresponding the
     * {@link ClassFile.FieldInfo} in the current class's constant pool.</p>
     * @return The short value of the local index corresponding the
     *          {@link ClassFile.FieldInfo} in the current class's constant pool.
     * @since 0.1.0
     */
    public final short getConstantPoolIndex() {

        return this.constantPoolIndex;

    }

    /**
     * <p>Returns the integer value of amount of bytes used by this {@link Instruction}.</p>
     * @return The integer value of amount of bytes used by this {@link Instruction}.
     * @since 0.1.0
     */
    public final int getByteCount() {

        return ByteCount;

    }

}
