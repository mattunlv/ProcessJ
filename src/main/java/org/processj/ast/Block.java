package org.processj.ast;

import org.processj.Phase;
import org.processj.utilities.Visitor;

public class Block extends Statement implements SymbolMap.Context {

    // This is set to true in the rewriting of channel read expressions inside other expressions when blocks are created
    public boolean canBeMerged = false;
    private SymbolMap scope;



    public Block(Sequence<Statement> stats) {
        super(stats);
        nchildren = 1;
        children = new AST[] { stats };
        this.scope = null;
    }

    public Sequence<Statement> stats() {
        return (Sequence<Statement>) children[0];
    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        final SymbolMap scope = this.openScope(visitor.getScope());

        // Visit
        S result = visitor.visitBlock(this);

        // Close the scope
        visitor.setScope(scope.getEnclosingScope());

        return result;

    }

}