package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class ChannelWriteStat extends Statement {

    private  Expression writeExpression    ;
    private final Expression channel            ;

    public ChannelWriteStat(final Expression channel, final Expression writeExpression) {
        super(new AST[] { channel, writeExpression });
        this.writeExpression = writeExpression  ;
        this.channel         = channel          ;
    }

    public Expression getTargetExpression() {

        return this.channel;

    }

    public Expression getWriteExpression() {

        return this.writeExpression;

    }

    public final void setWriteExpression(final Expression expression) {

        this.children[1] = expression;
        this.writeExpression = expression;

    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitChannelWriteStat(this);
    }
}