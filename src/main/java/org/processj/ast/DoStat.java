package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class DoStat extends LoopStatement {

    /// --------------
    /// Private Fields

    private Statement statement;

    /// ------------
    /// Constructors

    public DoStat(final Statement statement, final Expression evaluationExpression) {
        super(evaluationExpression);
        nchildren = 2;
        children = new AST[] { statement, evaluationExpression };
        this.statement = statement;
    }

    public final boolean definesStatement() {

        return this.statement != null;

    }

    public final Statement getStatement() {

        return (Statement) children[0];

    }

    public final void setStatement(final Statement statement) {

        this.statement = statement;
        this.children[0] = statement;

    }

    public Expression getEvaluationExpression() {
        return (Expression) children[1];
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitDoStat(this);
    }
}