package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class WhileStat extends LoopStatement implements IterativeStatement {

    private final Expression    expression  ;
    private final Block         body        ;

    public WhileStat(final Expression expression, final Statement body) {
        super(new AST[] { expression, new Block(body) });
        this.body       = (Block) this.children[1];
        this.expression = expression;
    }

    public final Expression getEvaluationExpression() {
        return this.expression;
    }

    public final Sequence<Statement> getStatements() {

        return (Sequence<Statement>) this.body.getStatements();

    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitWhileStat(this);
    }
}