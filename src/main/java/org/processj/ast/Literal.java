package org.processj.ast;


import org.processj.ast.expression.Expression;

public abstract class Literal extends Expression {

    public Literal(Token t) {
        super(t);
    }

    public Literal(AST a) {
        super(a);
    }

}