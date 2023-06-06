package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

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

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitAnnotation(this);
    }
}