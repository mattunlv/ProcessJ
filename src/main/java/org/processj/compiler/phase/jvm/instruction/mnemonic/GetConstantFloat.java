package org.processj.compiler.phase.jvm.instruction.mnemonic;

import org.processj.compiler.phase.jvm.instruction.Instruction;

public class GetConstantFloat implements Instruction {

    /// --------------
    /// Private Fields

    private final float constantValue;

    /// ------------
    /// Constructors

    public GetConstantFloat(final float constantValue) {

        this.constantValue = constantValue;

    }

}
