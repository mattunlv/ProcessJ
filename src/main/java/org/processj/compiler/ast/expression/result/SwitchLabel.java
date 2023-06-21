package org.processj.compiler.ast.expression.result;

import org.processj.compiler.ast.AST;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

public class SwitchLabel extends AST {

    private boolean isDefault;

    public SwitchLabel(final Expression constantExpression, boolean def) {
        super(new AST[] { constantExpression });
        isDefault = def;
    }

    public Expression getExpression() {
        return (Expression) children[0];
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitSwitchLabelExpression(this);
    }
}