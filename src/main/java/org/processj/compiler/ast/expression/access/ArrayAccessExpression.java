package org.processj.compiler.ast.expression.access;

import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.AST;
import org.processj.compiler.phase.Visitor;

public class ArrayAccessExpression extends Expression {

    /// --------------
    /// Private Fields

    private final Expression targetExpression;

    private final Expression indexExpression;

    public ArrayAccessExpression(Expression targetExpression, Expression indexExpression) {
        super(new AST[] { targetExpression, indexExpression });

        this.targetExpression = targetExpression    ;
        this.indexExpression  = indexExpression     ;

    }

    @Override
    public String toString() {
        return getTargetExpression() + "[" + getIndexExpression() + "]";
    }

    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        visitor.visitArrayAccessExpression(this);

    }

    public final Expression getTargetExpression() {

        return this.targetExpression;

    }

    public final Expression getIndexExpression() {

        return this.indexExpression;

    }

}