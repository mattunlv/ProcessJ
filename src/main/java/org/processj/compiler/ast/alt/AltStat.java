package org.processj.compiler.ast.alt;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class AltStat extends Statement implements SymbolMap.Context {

    public boolean pri;         // is this a pri alt
    public boolean replicated;  // is this a replicated alt
    private final Sequence<Statement>   initialization          ;
    private final Expression            evaluationExpression    ;
    private final Sequence<ExprStat>    increment               ;
    private final Block                 body                    ;
    private SymbolMap scope;

    public AltStat(Sequence<AltCase> body, boolean pri) {
        super(new AST[] { null, null, null, new Block(body) });
        this.pri = pri;
        this.replicated = false;
        this.body                   = (Block) this.children[3];
        this.initialization         = null;
        this.evaluationExpression   = null;
        this.increment              = null;
        this.scope                  = null;
    }

    public AltStat(Sequence<Statement> init,
                   Expression expr,
                   Sequence<ExprStat> incr,
                   Sequence<AltCase> body, boolean pri) {
        super(new AST[] { init, expr, incr, new Block(body) });
        this.pri                    = pri;
        this.replicated             = true;
        this.body                   = (Block) this.children[3];
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

    public final Sequence<ExprStat> getIncrementStatements() {

        return this.increment;

    }

    public Sequence<AltCase> getStatements() {
        return (Sequence<AltCase>) this.body.getStatements();
    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        final SymbolMap scope = this.openScope(visitor.getScope());

        // Visit
        S result = visitor.visitAltStat(this);

        // Close the scope
        visitor.setScope(scope.getEnclosingScope());

        return result;

    }

}
