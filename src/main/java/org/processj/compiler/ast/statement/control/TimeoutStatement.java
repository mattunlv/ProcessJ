package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class TimeoutStatement extends Statement {

    /// --------------
    /// Private Fields

    private final Expression timerExpression    ;
    private final Expression delayExpression    ;

    /// ------------
    /// Constructors

    public TimeoutStatement(final Expression timerExpression, final Expression delayExpression) {
        super(timerExpression);

        this.nchildren          = 2;
        this.children           = new AST[] {timerExpression, delayExpression};
        this.timerExpression    = timerExpression;
        this.delayExpression    = delayExpression;

    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitTimeoutStatement(this);

    }

    public final Expression getTimerExpression() {

        return this.timerExpression;

    }

    public final Expression getDelayExpression() {

        return this.delayExpression;

    }

}