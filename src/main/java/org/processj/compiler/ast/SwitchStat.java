package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class SwitchStat extends Statement implements ConditionalStatement {

    public SwitchStat(Expression expr, Sequence<SwitchGroup> switchGroups) {
        super(expr);
        nchildren = 2;
        children = new AST[] { expr, switchGroups };
    }

    public Expression getEvaluationExpression() {
        return (Expression) children[0];
    }

    public Sequence<SwitchGroup> switchBlocks() {
        return (Sequence<SwitchGroup>) children[1];
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitSwitchStat(this);
    }
}