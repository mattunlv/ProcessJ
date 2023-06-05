package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class SwitchLabel extends AST {

    private boolean isDefault;

    public SwitchLabel(Expression const_expr, boolean def) {
        super(const_expr);
        nchildren = 1;
        children = new AST[] { const_expr };
        isDefault = def;
    }

    public Expression getExpression() {
        return (Expression) children[0];
    }

    public boolean isDefault() {
        return isDefault;
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitSwitchLabel(this);
    }
}