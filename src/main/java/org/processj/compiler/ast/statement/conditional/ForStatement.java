package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.ExpressionStatement;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;
import java.util.ArrayList;

public class ForStatement extends Statement implements IterativeContext {

    /// --------------
    /// Private Fields

    private BlockStatement body  ;

    // This list is filled by the ParFor visitor and will contain the expressions inside a 
    // par for that alter states (assignment, pre and post increment/decrements).
    public ArrayList<Expression> vars = null;

    /* Note that init() and incr() can be null */
    public boolean par;

    public ForStatement(Token t, Sequence<Statement> init,
                        Expression expr,
                        Sequence<ExpressionStatement> incr ,
                        Sequence<Expression> barriers,
                        Statement stat,
                        boolean par) {
        super(init, expr, incr, barriers, (stat instanceof BlockStatement) ? stat : new BlockStatement(stat));
        this.par = par;
        this.body = (BlockStatement) this.children[4];

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

    public final boolean definesInitializationStatements() {

        return this.getInitializationStatements() != null;

    }

    public final boolean definesIncrementStatements() {

        return this.getIncrementStatements() != null;

    }

    public final boolean definesEvaluationExpression() {

        return this.getEvaluationExpression() != null;

    }

    public boolean isPar() {
        return par;
    }

    public Sequence<Statement> getInitializationStatements() {
        return (Sequence<Statement>) children[0];
    }

    public Expression getEvaluationExpression() {
        return (Expression) children[1];
    }

    public Sequence<ExpressionStatement> getIncrementStatements() {
        return (Sequence<ExpressionStatement>) children[2];
    }

    public Sequence<Expression> getBarrierExpressions() {
        return (Sequence<Expression>) children[3];
    }

    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the If Statement
        this.openScope();

        // Visit
        visitor.visitForStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

}