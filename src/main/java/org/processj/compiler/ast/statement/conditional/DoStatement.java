package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.IterativeStatement;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class DoStatement extends Statement implements IterativeStatement {

    /// --------------
    /// Private Fields

    private final BlockStatement body ;

    /// ------------
    /// Constructors

    public DoStatement(final Statement statement, final Expression evaluationExpression) {
        super(evaluationExpression);
        nchildren = 2;
        children = new AST[] { (statement instanceof BlockStatement) ? statement : new BlockStatement(statement), evaluationExpression };
        this.body = (BlockStatement) this.children[0];
    }

    public final BlockStatement getBody() {

        return this.body.getBody();

    }

    public final boolean definesBody() {

        return !this.body.getStatements().isEmpty();

    }

    @Override
    public BlockStatement getMergeBody() {
        return null;
    }

    @Override
    public final BlockStatement getClearedMergeBody() {

        return this.body.getClearedMergeBody();

    }

    public Expression getEvaluationExpression() {
        return (Expression) children[1];
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitDoStatement(this);
    }
}