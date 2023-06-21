package org.processj.compiler.phase.jvm.instruction.mnemonic;

import org.processj.compiler.phase.jvm.instruction.Instruction;
import org.processj.compiler.phase.jvm.instruction.aload;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>{@link Mnemonic} for pushing the 'this' reference onto the operand stack within the context of a
 * class method.</p>
 * @see Mnemonic
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public class PushThis implements Mnemonic {

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

    /// -----------
    /// Constructor

    /**
     * <p>Initializes the {@link Mnemonic} to its' default state.</p>
     * @since 0.1.0
     */
    public PushThis() {

        this.aload              = new aload((byte) 0x00)    ;
        this.instructions = new ArrayList<>()         ;

        this.instructions.add(this.aload);

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

        return aload.toString();

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

        return this.aload.getBytes();

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
