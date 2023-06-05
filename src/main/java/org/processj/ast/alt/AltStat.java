package org.processj.ast.alt;

import org.processj.Phase;
import org.processj.ast.*;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class AltStat extends Statement implements SymbolMap.Context {

    public boolean pri;         // is this a pri alt
    public boolean replicated;  // is this a replicated alt
    private SymbolMap symbolMap;
    private final Sequence<Statement> initialization          ;
    private final Expression evaluationExpression    ;
    private final Sequence<ExprStat>    increment               ;
    private SymbolMap scope;

    public AltStat(Sequence<AltCase> body, boolean pri) {
        super(body);
        nchildren = 4;
        this.pri = pri;
        this.replicated = false;
        children = new AST[] { null, null, null, body };
        this.symbolMap              = null;
        this.initialization         = null;
        this.evaluationExpression   = null;
        this.increment              = null;
        this.scope                  = null;
    }

    public AltStat(Sequence<Statement> init,
                   Expression expr,
                   Sequence<ExprStat> incr,
                   Sequence<AltCase> body, boolean pri) {
        super(body);
        nchildren = 4;
        this.pri = pri;
        this.replicated = true;
        children = new AST[] { init, expr, incr, body };
        this.symbolMap = null;
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

    public final void setReplicated() {

        this.replicated = true;

    }

    public final void setReplicated(final boolean isReplicated) {

        this.replicated = isReplicated;

    }

    public final boolean definesInitializationStatements() {

        return this.initialization != null;

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

    public Sequence<AltCase> body() {
        return (Sequence<AltCase>) children[3];
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
