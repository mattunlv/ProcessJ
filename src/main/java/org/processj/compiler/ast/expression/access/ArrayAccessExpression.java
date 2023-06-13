package org.processj.compiler.ast.expression.access;

import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.AST;
import org.processj.compiler.phases.phase.Visitor;

public class ArrayAccessExpression extends Expression {

    public ArrayAccessExpression(Expression target, Expression index) {
        super(target);
        nchildren = 2;
        children = new AST[] { target, index };
    }

    @Override
    public String toString() {
        return getTargetExpression() + "[" + getIndexExpression() + "]";
    }

    @Override
    public final <S> S visit(final Visitor<S> visitor)
            throws Phase.Error {

        return visitor.visitArrayAccessExpression(this);

    }

    public final Expression getTargetExpression() {
        return (Expression) children[0];
    }

    public final Expression getIndexExpression() {
        return (Expression) children[1];
    }

}