package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class WhileStat extends LoopStatement {

    private Expression expression  ;
    private Statement   statement   ;

    public WhileStat(final Expression expression, final Statement statement) {
        super(expression);
        nchildren = 2;
        children = new AST[] { expression, statement };
        this.statement = statement;
    }

    public final boolean definesStatement() {

        return this.statement != null;

    }

    public final Expression getEvaluationExpression() {
        return (Expression) children[0];
    }

    public final Statement getStatement() {
        return (Statement) children[1];
    }

    public final void setStatement(final Statement statement) {

        this.statement      = statement;
        this.children[1]    = statement;

    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitWhileStat(this);
    }
}