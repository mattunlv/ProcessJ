package org.processj.ast;

import org.processj.Phase;
import org.processj.utilities.Visitor;

public class SwitchGroup extends AST implements SymbolMap.Context {

    private SymbolMap scope;

    public SwitchGroup(Sequence<SwitchLabel> labels, Sequence<Statement> stmts) {
        super(labels);
        nchildren = 2;
        children = new AST[] { labels, stmts };
        this.scope = null;
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
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link SwitchGroup}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitSwitchGroup(SwitchGroup)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        final SymbolMap scope = this.openScope(visitor.getScope());

        // Visit
        S result = visitor.visitSwitchGroup(this);

        // Close the scope
        visitor.setScope(scope.getEnclosingScope());

        return result;

    }

}