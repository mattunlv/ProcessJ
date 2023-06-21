package org.processj.compiler.phase.jvm.instruction;

import org.processj.compiler.phase.jvm.ClassFile;

/**
 * <p>Set field in object.</p>
 * <p>Value: 0xb5 </p>
 * <p>Format: { 0xb5, indexbyte1, indexbyte2 } </p>
 * <p>Stack:
 *      ..., objectref, value →
 *      ...
 * </p>
 * <p>Description:
 *      The unsigned indexbyte1 and indexbyte2 are used to construct an index into the run-time constant pool of the
 *      current class (§2.6), where the value of the index is (indexbyte1 << 8) | indexbyte2. The run-time constant
 *      pool entry at the index must be a symbolic reference to a field (§5.1), which gives the name and descriptor of
 *      the field as well as a symbolic reference to the class in which the field is to be found. The referenced
 *      field is resolved (§5.4.3.2).
 *      The type of a value stored by a putfield instruction must be compatible with the descriptor of the referenced
 *      field (§4.3.2). If the field descriptor type is boolean, byte, char, short, or int, then the value must be
 *      an int. If the field descriptor type is float, long, or double, then the value must be a float, long,
 *      or double, respectively. If the field descriptor type is a reference type, then the value must be of a
 *      type that is assignment compatible (JLS §5.2) with the field descriptor type. If the field is final,
 *      it must be declared in the current class, and the instruction must occur in an instance initialization method
 *      of the current class (§2.9.1).
 *      The value and objectref are popped from the operand stack.
 *      The objectref must be of type reference but not an array type.
 *      If the value is of type int and the field descriptor type is boolean, then the int value is narrowed by
 *      taking the bitwise AND of value and 1, resulting in value'. The referenced field in objectref is set to value'.
 *      Otherwise, the referenced field in objectref is set to value.</p>
 * <p>Linking Exceptions:
 *      During resolution of the symbolic reference to the field, any of the exceptions pertaining to field resolution
 *      (§5.4.3.2) can be thrown.
 *      Otherwise, if the resolved field is a static field, putfield throws an IncompatibleClassChangeError.
 *      Otherwise, if the resolved field is final, it must be declared in the current class, and the instruction must
 *      occur in an instance initialization method of the current class. Otherwise, an IllegalAccessError is thrown.</p>
 * <p>Run-time Exceptions:
 *      Otherwise, if objectref is null, the {@link putfield} instruction throws a NullPointerException.</p>
 * <p>Notes:
 *      None.</p>
 * @see Instruction
 * @see ClassFile.Field
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public class putfield implements Instruction {

    /// -----------------
    /// Private Constants

    /**
     * <p>The byte value of the {@link Instruction}.</p>
     */
    private final static byte Value     = (byte) 0xb5   ;

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
     * <p>Initializes the {@link putfield} {@link Instruction} to its' default state with
     * the specified local index.</p>
     * @param constantPoolIndex The short value of the local index corresponding to the local to push
     *                   onto the operand stack when the {@link Instruction} is executed by the JVM.
     * @since 0.1.0
     */
    public putfield(final short constantPoolIndex) {

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

        return "putfield #" + this.constantPoolIndex;

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
