package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class ExprStat extends Statement {
    public ExprStat(Expression expr) {
        super(expr);
        nchildren = 1;
        children = new AST[] { expr };
    }

    public Expression getExpression() {
        return (Expression) children[0];
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitExprStat(this);
    }
}