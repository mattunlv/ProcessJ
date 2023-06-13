package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class SyncStatement extends Statement {

    public SyncStatement(final Expression barrier) {
        super(barrier);
        nchildren = 1;
        children = new AST[] { barrier };
    }

    public Expression getBarrierExpression() {
        return (Expression) children[0];
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitSyncStatement(this);
    }
}