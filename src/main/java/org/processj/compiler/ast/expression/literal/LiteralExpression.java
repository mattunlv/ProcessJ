package org.processj.compiler.ast.expression.literal;


import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.expression.Expression;

public abstract class LiteralExpression extends Expression {

    public LiteralExpression(Token t) {
        super(t);
    }

    public LiteralExpression(AST a) {
        super(a);
    }

}