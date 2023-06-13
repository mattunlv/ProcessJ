package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.statement.IterativeStatement;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class WhileStatement extends Statement implements IterativeStatement {

    private final Expression    expression  ;
    private final BlockStatement body        ;

    public WhileStatement(final Expression expression, final Statement body) {
        super(new AST[] { expression, new BlockStatement(body) });
        this.body       = (BlockStatement) this.children[1];
        this.expression = expression;
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

    public final Expression getEvaluationExpression() {
        return this.expression;
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitWhileStatement(this);
    }
}