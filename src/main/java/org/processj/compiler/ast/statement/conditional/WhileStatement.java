package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

public class WhileStatement extends Statement implements IterativeContext {

    private Expression      evaluationExpression    ;
    private BlockStatement  body                    ;

    public WhileStatement(final Expression evaluationExpression, final Statement body) {
        super(new AST[] { evaluationExpression, (body instanceof BlockStatement) ? body : new BlockStatement(body) });

        this.evaluationExpression   = evaluationExpression;
        this.body                   = (BlockStatement) this.children[1];

    }

    public final BlockStatement getBody() {

        return this.body.getBody();

    }

    @Override
    public BlockStatement getMergeBody() {
        return null;
    }

    @Override
    public void clearMergeBody() {

    }

    @Override
    public final BlockStatement getClearedMergeBody() {

        return this.body.getClearedMergeBody();

    }

    public final void setEvaluationExpression(final Expression evaluationExpression) {

        this.evaluationExpression   = evaluationExpression;
        this.children[1]            = evaluationExpression;

    }

    public final Expression getEvaluationExpression() {

        return this.evaluationExpression;

    }

    /**
     * <p>Invalidates the {@link WhileStatement}'s handle to any reference types.</p>
     * @since 0.1.0
     */
    public final void clear() {

        // Delegate to super
        super.clear();

        // Clear the body
        this.body.clear();

        // Invalidate the Evaluation Expression & body
        this.evaluationExpression   = null;
        this.body                   = null;

    }

    @Override
    public void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the If Statement
        this.openScope();

        // Visit
        visitor.visitWhileStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());
    }
}