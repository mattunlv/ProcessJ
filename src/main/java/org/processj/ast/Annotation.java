package org.processj.ast;

import org.processj.Phase;
import org.processj.utilities.Visitor;

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