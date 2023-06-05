package org.processj.ast;

import java.util.HashMap;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class ParBlock extends Statement implements SymbolMap.Context {
    
    public HashMap<String, Integer> enrolls;
    private SymbolMap scope;

    public ParBlock(Sequence<Statement> stats, Sequence<Expression> barriers) {
        super(stats);
        nchildren = 2;
        children = new AST[] { stats, barriers };
        this.scope = null;
    }

    public Sequence<Statement> stats() {
        return (Sequence<Statement>) children[0];
    }

    public Sequence<Expression> barriers() {
        return (Sequence<Expression>) children[1];
    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        final SymbolMap scope = this.openScope(visitor.getScope());

        // Visit
        S result = visitor.visitParBlock(this);

        // Close the scope
        visitor.setScope(scope.getEnclosingScope());

        return result;

    }

}