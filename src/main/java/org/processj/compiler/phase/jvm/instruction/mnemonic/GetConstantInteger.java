package org.processj.compiler.phase.jvm.instruction.mnemonic;

import org.processj.compiler.phase.jvm.instruction.Instruction;

public class GetConstantInteger implements Instruction {

    /// --------------
    /// Private Fields

    private final int constantValue;

    /// ------------
    /// Constructors

    public GetConstantInteger(final int constantValue) {

        this.constantValue = constantValue;

    }

}
