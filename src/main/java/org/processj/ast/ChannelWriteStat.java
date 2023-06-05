package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class ChannelWriteStat extends Statement {

    public ChannelWriteStat(Expression channel, Expression writeExpression) {
        super(channel);
        nchildren = 2;
        children = new AST[] { channel, writeExpression };
    }

    public Expression getTargetExpression() {

        return (Expression) children[0];

    }

    public Expression getWriteExpression() {
        return (Expression) children[1];
    }

    public final void setWriteExpression(final Expression expression) {

        this.children[1] = expression;

    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitChannelWriteStat(this);
    }
}