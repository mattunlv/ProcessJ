package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class ChannelEndExpr extends Expression {

    public static final int READ = 0;
    public static final int WRITE = 1;

    private int end;

    public ChannelEndExpr(Expression channel, int end) {
        super(channel);
        this.end = end;
        nchildren = 1;
        children = new AST[] { channel };
    }

    public boolean isRead() {
        return end == READ;
    }

    public boolean isWrite() {
        return end == WRITE;
    }

    public Expression getChannelType() {
        return (Expression) children[0];
    }
    
    public int endType() {
        return end;
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitChannelEndExpr(this);
    }
}