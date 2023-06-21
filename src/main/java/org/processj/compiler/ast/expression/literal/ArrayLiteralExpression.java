package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.phase.Visitor;

public class ArrayLiteralExpression extends LiteralExpression {

    final Sequence<Expression> expressions;

    public ArrayLiteralExpression(Sequence<Expression> seq) {
        super(new AST[] { seq });
        this.expressions = seq;
    }

    public final int getDepth() {

        return (this.expressions != null) ? this.expressions.size() : 0;

    }

    public final Sequence<Expression> getExpressions() {

        return this.expressions;

    }

    public Sequence elements() {
        return (Sequence<Expression>) children[0];
    }

    // TODO
    public String toString() {
        return "{.,.,.,.,.}";
    }

    public void accept(final Visitor visitor) throws Phase.Error {

        // Open the scope
        visitor.setContext(this.openContext(visitor.getContext()));

        // Visit
        visitor.visitArrayLiteralExpression(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

}