package org.processj.compiler.ast;

import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class Annotation extends AST {

    private String name, value;

    public Annotation(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitAnnotation(this);
    }
}