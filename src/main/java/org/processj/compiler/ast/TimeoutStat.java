package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class TimeoutStat extends Statement {

    /// --------------
    /// Private Fields

    private final Expression timerExpression    ;
    private final Expression delayExpression    ;

    /// ------------
    /// Constructors

    public TimeoutStat(final Expression timerExpression, final Expression delayExpression) {
        super(timerExpression);

        this.nchildren          = 2;
        this.children           = new AST[] {timerExpression, delayExpression};
        this.timerExpression    = timerExpression;
        this.delayExpression    = delayExpression;

    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitTimeoutStat(this);

    }

    public final Expression getTimerExpression() {

        return this.timerExpression;

    }

    public final Expression getDelayExpression() {

        return this.delayExpression;

    }

}