package org.processj.compiler.ast.expression.result;

import org.processj.compiler.ast.AST;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class SwitchLabel extends AST {

    private boolean isDefault;

    public SwitchLabel(final Expression constantExpression, boolean def) {
        super(constantExpression);
        nchildren = 1;
        children = new AST[] { constantExpression };
        isDefault = def;
    }

    public Expression getExpression() {
        return (Expression) children[0];
    }

    public boolean isDefault() {
        return isDefault;
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitSwitchLabelExpression(this);
    }
}