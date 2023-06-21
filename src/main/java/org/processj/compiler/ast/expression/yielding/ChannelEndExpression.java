package org.processj.compiler.ast.expression.yielding;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class ChannelEndExpression extends Expression {

    public static final int READ = 0;
    public static final int WRITE = 1;

    private int end;

    public ChannelEndExpression(Expression channel, int end) {
        super(new AST[] { channel });
        this.end = end;
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

    public void accept(Visitor v) throws Phase.Error {
        v.visitChannelEndExpression(this);
    }
}