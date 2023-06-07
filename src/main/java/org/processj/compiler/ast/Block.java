package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class Block extends Statement implements SymbolMap.Context {

    /// --------------
    /// Private Fields

    private final Sequence<? extends Statement> statements;

    // This is set to true in the rewriting of channel read expressions inside other expressions when blocks are created
    public boolean canBeMerged = false;
    private SymbolMap scope;

    public Block(final Sequence<? extends Statement> statements) {
        super(statements);

        this.scope      = null;
        this.statements = statements;

    }

    public Block(final Statement statement) {
        this((statement != null) ? new Sequence<>(statement) : new Sequence<>());
    }

    public final Sequence<? extends Statement> getStatements() {

        return this.statements;

    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        visitor.setScope(this.openScope(visitor.getScope()));

        // Visit
        S result = visitor.visitBlock(this);

        // Close the scope
        visitor.setScope(visitor.getScope().getEnclosingScope());

        return result;

    }

}