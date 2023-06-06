package org.processj.compiler.ast;


import org.processj.compiler.ast.expression.Expression;

public abstract class Literal extends Expression {

    public Literal(Token t) {
        super(t);
    }

    public Literal(AST a) {
        super(a);
    }

}