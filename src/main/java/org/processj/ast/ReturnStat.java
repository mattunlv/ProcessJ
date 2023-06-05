package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class ReturnStat extends Statement {

    /* Note that expr() can return null */

    public ReturnStat(Token r, Expression expr) {
        super(r);
        nchildren = 1;
        children = new AST[] { expr };
    }

    public Expression getExpression() {
        return (Expression) children[0];
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitReturnStat(this);
    }
}