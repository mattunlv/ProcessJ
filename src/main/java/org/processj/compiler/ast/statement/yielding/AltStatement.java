package org.processj.compiler.ast.statement.yielding;

import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.ast.statement.ExpressionStatement;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

public class AltStatement extends Statement implements YieldingContext {

    public boolean pri;         // is this a pri alt
    public boolean replicated;  // is this a replicated alt
    private final Sequence<Statement>   initialization          ;
    private final Expression            evaluationExpression    ;
    private final Sequence<ExpressionStatement>    increment               ;
    private final BlockStatement body                    ;
    private SymbolMap scope;

    public AltStatement(Sequence<AltCase> body, boolean pri) {
        super(new AST[] { null, null, null, new BlockStatement(body) });
        this.pri = pri;
        this.replicated = false;
        this.body                   = (BlockStatement) this.children[3];
        this.initialization         = null;
        this.evaluationExpression   = null;
        this.increment              = null;
        this.scope                  = null;
    }

    public AltStatement(Sequence<Statement> init,
                        Expression expr,
                        Sequence<ExpressionStatement> incr,
                        Sequence<AltCase> body, boolean pri) {
        super(new AST[] { init, expr, incr, new BlockStatement(body) });
        this.pri                    = pri;
        this.replicated             = true;
        this.body                   = (BlockStatement) this.children[3];
        this.initialization         = init;
        this.evaluationExpression   = expr;
        this.increment              = incr;
    }

    public boolean isPri() {
        return pri;
    }

    public boolean isReplicated() {
        return replicated;
    }

    public final void setReplicated(final boolean isReplicated) {

        this.replicated = isReplicated;

    }

    public final boolean definesInitializationStatements() {

        return this.initialization != null;

    }

    public final boolean definesMultipleInitializationStatements() {

        return (this.initialization != null) && (this.initialization.size() > 1);

    }

    public final boolean definesEvaluationExpression() {

        return this.evaluationExpression != null;

    }

    public final boolean definesIncrementExpression() {

        return this.increment != null;

    }

    public final Sequence<Statement> initializationStatements() {

        return this.initialization;

    }

    public final Expression evaluationExpression() {

        return this.evaluationExpression;

    }

    public final Sequence<ExpressionStatement> getIncrementStatements() {

        return this.increment;

    }

    public Sequence<AltCase> getBody() {
        return (Sequence<AltCase>) this.body.getStatements();
    }

    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the If Statement
        this.openScope();

        // Visit
        visitor.visitAltStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    @Override
    public BlockStatement getMergeBody() {
        return this.body.getMergeBody();
    }

    @Override
    public void clearMergeBody() {

        this.body.clearMergeBody();

    }

    @Override
    public BlockStatement getClearedMergeBody() {

        this.body.clearMergeBody();

        return this.body.getMergeBody();
    }
}
