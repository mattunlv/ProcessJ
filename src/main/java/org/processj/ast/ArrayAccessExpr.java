package org.processj.ast;

import org.processj.utilities.Visitor;

public class ArrayAccessExpr extends Expression {

    public ArrayAccessExpr(Expression target, Expression index) {
        super(target);
        nchildren = 2;
        children = new AST[] { target, index };
    }

    @Override
    public String toString() {
        return targetExpression() + "[" + indexExpression() + "]";
    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) {

        return visitor.visitArrayAccessExpr(this);

    }

    public final Expression targetExpression() {
        return (Expression) children[0];
    }

    public final Expression indexExpression() {
        return (Expression) children[1];
    }

}