package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.ExpressionStatement;
import org.processj.compiler.ast.statement.IterativeStatement;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;
import java.util.ArrayList;

public class ForStatement extends Statement implements IterativeStatement {

    /// --------------
    /// Private Fields

    private BlockStatement body  ;

    // This list is filled by the ParFor visitor and will contain the expressions inside a 
    // par for that alter states (assignment, pre and post increment/decrements).
    public ArrayList<Expression> vars = null;

    /* Note that init() and incr() can be null */
    public boolean par;

    private SymbolMap scope;

    public ForStatement(Token t, Sequence<Statement> init,
                        Expression expr,
                        Sequence<ExpressionStatement> incr ,
                        Sequence<Expression> barriers,
                        Statement stat,
                        boolean par) {
        super(t);
        this.par = par;
        this.scope = null;

        if(stat instanceof BlockStatement) this.body = (BlockStatement) stat;

        else this.body = new BlockStatement(new Sequence<>(stat));
        nchildren = 5;
        children = new AST[] { init, expr, incr, barriers, this.body };

    }

    public final BlockStatement getBody() {

        return this.body.getBody();

    }

    @Override
    public BlockStatement getMergeBody() {
        return null;
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
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        visitor.setScope(this.openScope(visitor.getScope()));

        // Visit
        S result = visitor.visitForStatement(this);

        // Close the scope
        visitor.setScope(visitor.getScope().getEnclosingScope());

        return result;

    }

}