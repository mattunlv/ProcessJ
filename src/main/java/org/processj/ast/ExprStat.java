package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class ExprStat extends Statement {
    public ExprStat(Expression expr) {
        super(expr);
        nchildren = 1;
        children = new AST[] { expr };
    }

    public Expression expr() {
        return (Expression) children[0];
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitExprStat(this);
    }
}