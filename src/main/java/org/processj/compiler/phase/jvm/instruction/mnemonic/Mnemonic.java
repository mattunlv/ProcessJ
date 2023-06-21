package org.processj.compiler.phase.jvm.instruction.mnemonic;

import org.processj.compiler.phase.jvm.instruction.Instruction;

import java.util.List;

public interface Mnemonic extends Instruction {

    List<Instruction> getInstructions();

}
