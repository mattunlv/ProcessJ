package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

public class SyncStatement extends Statement {

    /// --------------
    /// Private Fields

    private Expression barrierExpression;


    public SyncStatement(final Expression barrierExpression) {
        super(new AST[] { barrierExpression });
        this.barrierExpression = barrierExpression;
    }

    public final Expression getBarrierExpression() {

        return this.barrierExpression;

    }

    public final void setBarrierExpression(final Expression barrierExpression) {

        this.barrierExpression  = barrierExpression;
        this.children[0]        = barrierExpression;

    }

    @Override
    public void accept(final Visitor visitor) throws Phase.Error {
        visitor.visitSyncStatement(this);
    }
}