package org.processj.compiler.ast.statement;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class ExpressionStatement extends Statement {
    public ExpressionStatement(Expression expr) {
        super(expr);
    }

    public ExpressionStatement(final String label, Expression expr) {
        super(label, expr);
    }


    public Expression getExpression() {
        return (Expression) children[0];
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitExpressionStatement(this);
    }
}