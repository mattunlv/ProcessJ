package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;
import java.util.ArrayList;

public class ForStat extends LoopStatement implements SymbolMap.Context {

    /// --------------
    /// Private Fields

    private Statement statement;

    // This list is filled by the ParFor visitor and will contain the expressions inside a 
    // par for that alter states (assignment, pre and post increment/decrements).
    public ArrayList<Expression> vars = null;

    /* Note that init() and incr() can be null */
    public boolean par;

    private SymbolMap scope;

    public ForStat(Token t, Sequence<Statement> init,
                   Expression expr,
                   Sequence<ExprStat> incr ,
                   Sequence<Expression> barriers,
                   Statement stat,
                   boolean par) {
        super(t);
        nchildren = 5;
        this.par = par;
        children = new AST[] { init, expr, incr, barriers, stat };
        this.scope = null;
        this.statement = stat;
    }

    public final boolean definesStatement() {

        return this.statement != null;

    }

    public final boolean definesInitializationExpression() {

        return this.getInitializationExpression() != null;

    }

    public final boolean definesIncrement() {

        return this.getIncrementExpression() != null;

    }

    public final boolean definesEvaluationExpression() {

        return this.getEvaluationExpression() != null;

    }

    public final void setStatement(final Statement statement) {

        this.statement      = statement;
        this.children[4]    = statement;

    }

    public boolean isPar() {
        return par;
    }

    public Sequence<Statement> getInitializationExpression() {
        return (Sequence<Statement>) children[0];
    }

    public Expression getEvaluationExpression() {
        return (Expression) children[1];
    }

    public Sequence<ExprStat> getIncrementExpression() {
        return (Sequence<ExprStat>) children[2];
    }

    public Sequence<Expression> getBarrierExpressions() {
        return (Sequence<Expression>) children[3];
    }

    public final Statement getStatement() {

        return (Statement) children[4];

    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        final SymbolMap scope = this.openScope(visitor.getScope());

        // Visit
        S result = visitor.visitForStat(this);

        // Close the scope
        visitor.setScope(scope.getEnclosingScope());

        return result;

    }

}