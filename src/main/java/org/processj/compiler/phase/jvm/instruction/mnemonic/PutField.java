package org.processj.compiler.phase.jvm.instruction.mnemonic;

import org.processj.compiler.phase.jvm.instruction.Instruction;
import org.processj.compiler.phase.jvm.instruction.aload;
import org.processj.compiler.phase.jvm.instruction.putfield;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>{@link Mnemonic} for popping a value from the operand stack into the object's field</p>
 * @see Mnemonic
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public class PutField implements Mnemonic {
    // TODO: Make sure 'this' is pushed in the correct order; i.e. before the value
    /// --------------
    /// Private Fields

    /**
     * <p>The {@link java.util.List} instance holding the {@link Mnemonic}'s {@link Instruction}s.</p>
     * @since 0.1.0
     */
    private final List<Instruction> instructions    ;

    /**
     * <p>The {@link org.processj.compiler.phase.jvm.instruction.aload} instruction that pushes the 'this' reference onto the operand stack.</p>
     * @since 0.1.0
     */
    private final aload             aload           ;

    /**
     * <p>The {@link org.processj.compiler.phase.jvm.instruction.putfield} {@link Instruction} that pushes the field onto the operand stack.</p>
     * @since 0.1.0
     */
    private final putfield          putfield        ;

    /**
     * <p>The byte encoding of this {@link Mnemonic}.</p>
     * @since 0.1.0
     */
    private final byte[]            bytes           ;

    /// -----------
    /// Constructor

    /**
     * <p>Initializes the {@link Mnemonic} to its' default state.</p>
     * @since 0.1.0
     */
    public PutField(final short constantPoolIndex) {

        this.aload          = new aload((byte) 0x00)            ;
        this.putfield       = new putfield(constantPoolIndex)   ;
        this.instructions   = new ArrayList<>()                 ;

        this.instructions.add(this.aload);
        this.instructions.add(this.putfield);

        // Initialize a handle to the aload & getfield bytes
        final byte[] aloadBytes     = this.aload.getBytes();
        final byte[] getfieldBytes  = this.putfield.getBytes();

        // Initialize the result
        this.bytes = new byte[aloadBytes.length + getfieldBytes.length];

        // Copy over the contents
        System.arraycopy(aloadBytes, 0, bytes, 0, aloadBytes.length);
        System.arraycopy(getfieldBytes, 0, bytes, aloadBytes.length, getfieldBytes.length);

    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} representation of the {@link Mnemonic}.</p>
     * @return The {@link String} representation of the {@link Mnemonic}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.aload.toString() + '\n' + this.putfield.toString();

    }

    /// -----------
    /// Instruction

    /**
     * <p>Returns the byte encoding corresponding to the {@link Mnemonic}.</p>
     * @return The byte encoding corresponding to the {@link Mnemonic}.
     * @since 0.1.0
     */
    @Override
    public final byte[] getBytes() {

        return this.bytes;

    }

    /// --------
    /// Mnemonic

    /**
     * <p>Returns the {@link List} of {@link Instruction}s that compose the {@link Mnemonic}.</p>
     * @return The {@link List} of {@link Instruction}s that compose the {@link Mnemonic}.
     */
    @Override
    public final List<Instruction> getInstructions() {

        return this.instructions;

    }

}
