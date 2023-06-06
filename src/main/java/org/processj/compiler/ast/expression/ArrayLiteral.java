package org.processj.compiler.ast.expression;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Literal;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.SymbolMap;
import org.processj.compiler.phases.phase.Visitor;

public class ArrayLiteral extends Literal implements SymbolMap.Context {

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