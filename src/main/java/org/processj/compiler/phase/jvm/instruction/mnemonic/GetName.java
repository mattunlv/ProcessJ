package org.processj.compiler.phase.jvm.instruction.mnemonic;

import org.processj.compiler.phase.jvm.instruction.Instruction;

public class GetName implements Instruction {

    /// --------------
    /// Private Fields

    private final String name;

    /// ------------
    /// Constructors

    public GetName(final String name) {

        this.name = name;

    }

}
