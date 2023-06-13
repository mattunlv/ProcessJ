package org.processj.compiler.ast.expression.yielding;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class ChannelReadExpression extends Expression {

    final BlockStatement extendedRendezvous;

    public ChannelReadExpression(final Expression channel, final BlockStatement extendedRendezvous) {
        super(channel);
        nchildren = 2;
        children = new AST[] { channel, extendedRendezvous };
        this.extendedRendezvous = extendedRendezvous;
    }

    public Expression getChannelExpression() {
        return (Expression) children[0];
    }

    public BlockStatement getExtendedRendezvous() {

        return this.extendedRendezvous;

    }

    public final boolean definesExtendedRendezvous() {

        return this.extendedRendezvous != null;

    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitChannelReadExpression(this);
    }
}