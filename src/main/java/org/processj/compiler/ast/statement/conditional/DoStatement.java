package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

public class DoStatement extends Statement implements IterativeContext {

    /// --------------
    /// Private Fields

    private final BlockStatement body ;

    /// ------------
    /// Constructors

    public DoStatement(final Statement statement, final Expression evaluationExpression) {
        super(new AST[] { (statement instanceof BlockStatement) ? statement : new BlockStatement(statement), evaluationExpression });
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
    public void clearMergeBody() {

    }

    @Override
    public final BlockStatement getClearedMergeBody() {

        return this.body.getClearedMergeBody();

    }

    public Expression getEvaluationExpression() {
        return (Expression) children[1];
    }

    @Override
    public final void accept(Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the If Statement
        this.openScope();

        // Visit
        visitor.visitDoStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }
}