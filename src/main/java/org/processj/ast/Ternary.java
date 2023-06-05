package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class Ternary extends Expression {

    public Ternary(Expression expr, Expression trueBranch,
                   Expression falseBranch) {
        super(expr);
        nchildren = 3;
        children = new AST[] { expr, trueBranch, falseBranch };
    }

    public Expression getEvaluationExpression() {
        return (Expression) children[0];
    }

    public Expression thenPart() {
        return (Expression) children[1];
    }

    public Expression elsePart() {
        return (Expression) children[2];
    }

    public boolean isConstant() {
        return getEvaluationExpression().isConstant() && thenPart().isConstant()
            && elsePart().isConstant();
    }

    public Object constantValue() {
        Boolean b = (Boolean) getEvaluationExpression().constantValue();
        if (b.booleanValue())
            return thenPart().constantValue();
        return elsePart().constantValue();
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitTernary(this);
    }
}