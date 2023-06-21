package org.processj.compiler.phase.jvm.instruction.mnemonic;

import org.processj.compiler.phase.jvm.instruction.Instruction;

public class GetConstantDouble implements Instruction {

    /// --------------
    /// Private Fields

    private final double constantValue;

    /// ------------
    /// Constructors

    public GetConstantDouble(final double constantValue) {

        this.constantValue = constantValue;

    }

}
