package org.processj.compiler.ast;

import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class Annotation extends AST {

    private final String name;
    private Expression  value;

    public Annotation(String name, Expression value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return name;
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitAnnotation(this);
    }
}