package org.processj.compiler.phase.jvm.instruction.mnemonic;

import org.processj.compiler.phase.jvm.instruction.Instruction;

public class GetConstantLong implements Instruction {

    /// --------------
    /// Private Fields

    private final long constantValue;

    /// ------------
    /// Constructors

    public GetConstantLong(final long constantValue) {

        this.constantValue = constantValue;

    }

}
