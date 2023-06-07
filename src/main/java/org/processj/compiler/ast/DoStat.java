package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class DoStat extends LoopStatement implements IterativeStatement {

    /// --------------
    /// Private Fields

    private final Block body;

    /// ------------
    /// Constructors

    public DoStat(final Statement statement, final Expression evaluationExpression) {
        super(evaluationExpression);
        nchildren = 2;
        children = new AST[] { (statement instanceof Block) ? statement : new Block(statement), evaluationExpression };
        this.body = (Block) this.children[0];
    }

    public final Sequence<Statement> getStatements() {

        return (Sequence<Statement>) this.body.getStatements();

    }

    public Expression getEvaluationExpression() {
        return (Expression) children[1];
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitDoStat(this);
    }
}