package org.processj.compiler.ast.expression.result;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class TernaryExpression extends Expression {

    public TernaryExpression(Expression expr, Expression trueBranch,
                             Expression falseBranch) {
        super(new AST[] { expr, trueBranch, falseBranch });
    }

    public Expression getEvaluationExpression() {
        return (Expression) children[0];
    }

    public Expression getThenExpression() {
        return (Expression) children[1];
    }

    public Expression getElseExpression() {
        return (Expression) children[2];
    }

    public boolean isConstant() {
        return getEvaluationExpression().isConstant() && getThenExpression().isConstant()
            && getElseExpression().isConstant();
    }

    public Object constantValue() {
        Boolean b = (Boolean) getEvaluationExpression().constantValue();
        if (b.booleanValue())
            return getThenExpression().constantValue();
        return getElseExpression().constantValue();
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitTernaryExpression(this);
    }
}