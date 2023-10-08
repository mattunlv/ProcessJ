package org.processj.compiler.ast.expression.result;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class TernaryExpression extends Expression {

    private final Expression evalutationExpression;
    private final Expression thenExpression;
    private final Expression elseExpression;

    public TernaryExpression(Expression expr, Expression trueBranch,
                             Expression falseBranch) {
        super(new AST[] { expr, trueBranch, falseBranch });
        evalutationExpression = expr;
        this.thenExpression = trueBranch;
        this.elseExpression = falseBranch;
    }

    public Expression getEvaluationExpression() {
        return this.evalutationExpression;
    }

    public Expression getThenExpression() {
        return this.thenExpression;
    }

    public Expression getElseExpression() {
        return this.elseExpression;
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