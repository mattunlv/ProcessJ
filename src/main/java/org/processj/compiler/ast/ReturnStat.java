package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

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