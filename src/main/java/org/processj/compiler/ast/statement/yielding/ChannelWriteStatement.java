package org.processj.compiler.ast.statement.yielding;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class ChannelWriteStatement extends Statement {

    private  Expression writeExpression    ;
    private final Expression channel            ;

    public ChannelWriteStatement(final Expression channel, final Expression writeExpression) {
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
        return v.visitChannelWriteStatement(this);
    }
}