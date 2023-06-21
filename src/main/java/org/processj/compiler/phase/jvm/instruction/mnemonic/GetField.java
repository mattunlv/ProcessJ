package org.processj.compiler.phase.jvm.instruction.mnemonic;

import org.processj.compiler.phase.jvm.instruction.Instruction;
import org.processj.compiler.phase.jvm.instruction.aload;
import org.processj.compiler.phase.jvm.instruction.getfield;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>{@link Mnemonic} for pushing a class's field onto the operand stack</p>
 * @see Mnemonic
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public class GetField implements Mnemonic {

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
     * <p>The {@link getfield} {@link Instruction} that pushes the field onto the operand stack.</p>
     * @since 0.1.0
     */
    private final getfield          getfield        ;

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
    public GetField(final short constantPoolIndex) {

        this.aload          = new aload((byte) 0x00)            ;
        this.getfield       = new getfield(constantPoolIndex)   ;
        this.instructions   = new ArrayList<>()                 ;

        this.instructions.add(this.aload);
        this.instructions.add(this.getfield);

        // Initialize a handle to the aload & getfield bytes
        final byte[] aloadBytes     = this.aload.getBytes();
        final byte[] getfieldBytes  = this.getfield.getBytes();

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

        return this.aload.toString() + '\n' + this.getfield.toString();

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
