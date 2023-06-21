package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

public class SwitchStatement extends Statement implements ConditionalContext {

    private final BlockStatement body;

    public SwitchStatement(final Expression evaluationExpression, final Sequence<SwitchGroupStatement> switchGroups) {
        super(new AST[] { evaluationExpression, new BlockStatement(switchGroups) });

        this.body = (BlockStatement) this.children[1];

    }

    public Expression getEvaluationExpression() {
        return (Expression) children[0];
    }

    public void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the If Statement
        this.openScope();

        // Visit
        visitor.visitSwitchStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());
    }

    public final BlockStatement getBody() {

        return this.body;

    }

    public final BlockStatement getMergeBody() {

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

}