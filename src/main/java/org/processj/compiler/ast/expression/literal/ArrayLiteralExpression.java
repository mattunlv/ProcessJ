package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.SymbolMap;
import org.processj.compiler.phases.phase.Visitor;

public class ArrayLiteralExpression extends LiteralExpression implements SymbolMap.Context {

    final Sequence<Expression> expressions;

    public ArrayLiteralExpression(Sequence<Expression> seq) {
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

    public <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        visitor.setScope(this.openScope(visitor.getScope()));

        // Visit
        S result = visitor.visitArrayLiteralExpression(this);

        // Close the scope
        visitor.setScope(visitor.getScope().getEnclosingScope());

        return result;

    }

    @Override
    public BlockStatement getMergeBody() {
        return null;
    }

    @Override
    public BlockStatement getClearedMergeBody() {
        return null;
    }

    @Override
    public boolean definesLabel() {
        return false;
    }

    @Override
    public boolean definesEndLabel() {
        return false;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public void setEndLabel(String label) {

    }

    @Override
    public String getEndLabel() {
        return null;
    }
}