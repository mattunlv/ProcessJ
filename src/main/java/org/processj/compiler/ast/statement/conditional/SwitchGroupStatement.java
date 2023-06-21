package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.result.SwitchLabel;
import org.processj.compiler.ast.statement.BreakableContext;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class SwitchGroupStatement extends Statement implements BreakableContext {

    private SymbolMap scope;
    private boolean containsBreakStatement;
    private final BlockStatement body;

    public SwitchGroupStatement(final Sequence<SwitchLabel> labels, final Sequence<Statement> statements) {
        super(new AST[] { labels, new BlockStatement(statements) });
        this.scope = null;
        this.containsBreakStatement = false;
        this.body = (BlockStatement) this.children[1];
    }

    @Override
    public BlockStatement getMergeBody() {
        return this.body.getMergeBody();
    }

    @Override
    public void clearMergeBody() {

    }

    @Override
    public final BlockStatement getClearedMergeBody() {

        return this.body.getClearedMergeBody();

    }

    @Override
    public boolean definesEndLabel() {
        return false;
    }

    @Override
    public void setEndLabel(String label) {

    }

    @Override
    public String getEndLabel() {
        return null;
    }

    public final BlockStatement getBody() {

        return this.body.getBody();

    }

    public final boolean containsBreakStatement() {

        return this.containsBreakStatement;

    }

    public final void setContainsBreakStatement(final boolean containsBreakStatement) {

        this.containsBreakStatement = containsBreakStatement;

    }

    public Sequence<SwitchLabel> getLabels() {
        return (Sequence<SwitchLabel>) children[0];
    }

    public Sequence<Statement> getStatements() {
        return (Sequence<Statement>) children[1];
    }

    /// --------------------
    /// org.processj.ast.AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link SwitchGroupStatement}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitSwitchGroupStatement(SwitchGroupStatement)} method.</p>
     *
     * @param visitor The {@link Visitor} to dispatch.
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the If Statement
        this.openScope();

        // Visit
        visitor.visitSwitchGroupStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

}