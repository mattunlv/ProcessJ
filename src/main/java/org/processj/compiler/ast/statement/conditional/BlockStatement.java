package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.statement.BreakableContext;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class BlockStatement extends Statement implements BreakableContext {

    /// --------------
    /// Private Fields

    private final Sequence<? extends Statement> statements;

    // This is set to true in the rewriting of channel read expressions inside other expressions when blocks are created
    public boolean canBeMerged = false;
    private final BlockStatement mergeBody;
    private SymbolMap scope;

    public BlockStatement(final Sequence<? extends Statement> statements) {
        super(statements);

        this.scope      = null;
        this.statements = statements;
        this.mergeBody = new BlockStatement();

    }

    public BlockStatement(final Statement statement) {
        this((statement != null) ? new Sequence<>(statement) : new Sequence<>());
    }

    public BlockStatement() {
        this(new Sequence<>());
    }

    public final Sequence<? extends Statement> getStatements() {

        return this.statements;

    }

    public final void aggregate(final Sequence<Statement> statements) {

        final Sequence<Statement> result = (Sequence<Statement>) this.statements;

        // Assert the specified Sequence is valid & aggregate the specified Statements
        if(statements != null) for(final Statement statement: statements)
            result.append(statement);

    }

    public final void aggregate(final BlockStatement blockStatement) {

        this.aggregate((Sequence<Statement>) blockStatement.getStatements());

    }

    public final void clear() {

        this.statements.clear();
        this.mergeBody.clear();
        this.scope = null;

    }

    public final void append(final Statement statement) {

        ((Sequence<Statement>) this.statements).append(statement);

    }

    public final BlockStatement getBody() {

        return this.mergeBody;

    }

    @Override
    public BlockStatement getMergeBody() {
        return null;
    }

    @Override
    public void clearMergeBody() {

        if(this.mergeBody != null)
            this.mergeBody.clear();

    }

    @Override
    public final BlockStatement getClearedMergeBody() {

        // Clear the Merge body
        this.mergeBody.clear();

        // Return the result
        return this.mergeBody;

    }

    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // If the BlockStatement didn't inherit a scope, open one up
        if(this.getScope() == null) this.openScope();

        // Visit
        visitor.visitBlockStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

}