package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

public class TimeoutStatement extends Statement {

    /// --------------
    /// Private Fields

    private Expression timerExpression    ;
    private Expression delayExpression    ;

    /// ------------
    /// Constructors

    public TimeoutStatement(final Expression timerExpression, final Expression delayExpression) {
        super(new AST[] { timerExpression, delayExpression });

        this.timerExpression    = timerExpression;
        this.delayExpression    = delayExpression;

    }

    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        visitor.visitTimeoutStatement(this);

    }

    public final Expression getTimerExpression() {

        return this.timerExpression;

    }

    public final Expression getDelayExpression() {

        return this.delayExpression;

    }

    public final void setTimerExpression(final Expression timerExpression) {

        this.timerExpression    = timerExpression;
        this.children[0]        = timerExpression;

    }

    public final void setDelayExpression(final Expression delayExpression) {

        this.delayExpression    = delayExpression;
        this.children[1]        = delayExpression;

    }

}