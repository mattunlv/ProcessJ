package org.processj.ast.expression;

import org.processj.Phase;
import org.processj.ast.AST;
import org.processj.ast.Literal;
import org.processj.ast.Sequence;
import org.processj.utilities.Visitor;

public class ArrayLiteral extends Literal {

    final Sequence<Expression> expressions;

    public ArrayLiteral(Sequence<Expression> seq) {
        super(seq);
        nchildren = 1;
        children = new AST[] { seq };
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

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitArrayLiteral(this);
    }
}