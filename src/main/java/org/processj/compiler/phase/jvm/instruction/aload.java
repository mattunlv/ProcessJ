package org.processj.compiler.phase.jvm.instruction;

/**
 * <p>Load reference from local variable.</p>
 * <p>Value: 0x19 </p>
 * <p>Format: { 0x19, index } </p>
 * <p>Stack:
 *      ... →
 *      ..., objectref
 * </p>
 * <p>Description:
 *      The index is an unsigned byte that must be an index into the local variable array of the current frame (§2.6).
 *      The local variable at index must contain a reference. The objectref in the local variable at index is pushed
 *      onto the operand stack.</p>
 * <p>Linking Exceptions:
 *      None.</p>
 * <p>Run-time Exceptions:
 *      None.</p>
 * <p>Notes:
 *      The index is an unsigned byte that must be an index into the local variable array of the current frame (§2.6).
 *      The local variable at index must contain a reference. The objectref in the local variable at index is pushed
 *      onto the operand stack.</p>
 * @see Instruction
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public class aload implements Instruction {

    /// -----------------
    /// Private Constants

    /**
     * <p>The byte value of the {@link Instruction}.</p>
     */
    private final static byte Value     = 0x19  ;

    /**
     * <p>The amount of bytes this {@link Instruction} requires.</p>
     * @since 0.1.0
     */
    private final static int  ByteCount = 0x02  ;

    /// --------------
    /// Private Fields

    /**
     * <p>The position corresponding to the local in the activation record.</p>
     */
    private final byte      localIndex          ;

    /**
     * <p>The byte encoding of this {@link Instruction}.</p>
     * @since 0.1.0
     */
    private final byte[]    bytes               ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link aload} {@link Instruction} to its' default state with
     * the specified local index.</p>
     * @param localIndex The byte value of the local index corresponding to the local to push
     *                   onto the operand stack when the {@link Instruction} is executed by the JVM.
     * @since 0.1.0
     */
    public aload(final byte localIndex) {

        this.localIndex = localIndex                            ;
        this.bytes      = new byte[] { Value, this.localIndex } ;

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

        return "aload #" + this.localIndex;

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
     * <p>Returns the byte value of the local index.</p>
     * @return The byte value of the local index.
     * @since 0.1.0
     */
    public final byte getLocalIndex() {

        return this.localIndex;

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
