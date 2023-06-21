package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

public class IfStatement extends Statement implements ConditionalContext {

    /// --------------
    /// Private Fields

    private Expression evaluationExpression;
    private final BlockStatement thenBody    ;
    private final BlockStatement elseBody    ;

    /** Note that elsepart() can return null when having code
     * like
     *
     *      if ( expression )
     *          statement
     * */

    public IfStatement(final String label, final Expression evaluationExpression, final Statement thenPart, final Statement elsePart) {
        super(evaluationExpression, ((thenPart instanceof BlockStatement) ? thenPart : new BlockStatement(thenPart)),
                ((elsePart instanceof BlockStatement) ? elsePart : new BlockStatement(elsePart)));

        this.evaluationExpression   = evaluationExpression                   ;
        this.thenBody               = (BlockStatement) this.children[1]      ;
        this.elseBody               = (BlockStatement) this.children[2]      ;
    }

    public IfStatement(final String label, final Expression evaluationExpression, final Statement thenPart) {
        this(label, evaluationExpression, thenPart, null);
    }

    public IfStatement(final Expression evaluationExpression, final Statement thenPart, final Statement elsePart) {
        this("", evaluationExpression, thenPart, elsePart);
    }

    public IfStatement(final Expression evaluationExpression, final Statement thenPart) {
        this(evaluationExpression, thenPart, null);
    }



    @Override
    public void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the If Statement
        this.openScope();

        // Visit
        visitor.visitIfStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    public final Expression getEvaluationExpression() {

        return this.evaluationExpression;

    }

    public final boolean definesThenStatement() {

        return this.children[1] != null;

    }

    public final boolean definesElseStatement() {

        return this.children[2] != null;

    }

    public final BlockStatement getThenBody() {

        return this.thenBody;

    }

    public final BlockStatement getElseBody() {

        return this.elseBody;

    }

    @FunctionalInterface
    public interface AggregateReturnCallback {

        Statement Invoke(final Statement statement) throws Phase.Error;

    }

    public final BlockStatement getBody() {

        return this.thenBody.getBody();

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

        return this.thenBody.getClearedMergeBody();

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