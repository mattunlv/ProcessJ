package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class SyncStat extends Statement {

    public SyncStat(Expression barrier) {
        super(barrier);
        nchildren = 1;
        children = new AST[] { barrier };
    }

    public Expression barrier() {
        return (Expression) children[0];
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitSyncStat(this);
    }
}