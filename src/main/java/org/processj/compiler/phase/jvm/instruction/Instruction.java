package org.processj.compiler.phase.jvm.instruction;

public interface Instruction {


    default byte[] getBytes() {

        return new byte[0];

    }


}
