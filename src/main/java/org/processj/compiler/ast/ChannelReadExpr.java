package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class ChannelReadExpr extends Expression {

    final Block extendedRendezvous;

    public ChannelReadExpr(final Expression channel, final Block extendedRendezvous) {
        super(channel);
        nchildren = 2;
        children = new AST[] { channel, extendedRendezvous };
        this.extendedRendezvous = extendedRendezvous;
    }

    public Expression getExpression() {
        return (Expression) children[0];
    }

    public Block getExtendedRendezvous() {

        return this.extendedRendezvous;

    }

    public final boolean definesExtendedRendezvous() {

        return this.extendedRendezvous != null;

    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitChannelReadExpr(this);
    }
}