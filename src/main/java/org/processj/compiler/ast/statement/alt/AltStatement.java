package org.processj.compiler.ast.statement.alt;

import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.ast.statement.ExpressionStatement;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class AltStatement extends Statement implements SymbolMap.Context {

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
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        visitor.setScope(this.openScope(visitor.getScope()));

        // Visit
        S result = visitor.visitAltStatement(this);

        // Close the scope
        visitor.setScope(visitor.getScope().getEnclosingScope());

        return result;

    }

    @Override
    public BlockStatement getMergeBody() {
        return null;
    }

    @Override
    public BlockStatement getClearedMergeBody() {
        return null;
    }
}
