package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class IfStat extends Statement {

    /// --------------
    /// Private Fields

    private Expression expression  ;
    private Statement   thenPart    ;
    private Statement   elsePart    ;

    /** Note that elsepart() can return null when having code
     * like
     *
     *      if ( expression )
     *          statement
     * */

    public IfStat(final Expression expression, final Statement thenPart, final Statement elsePart) {
        super(expression);
        nchildren = 3;
        children = new AST[] { expression, thenPart, elsePart };
        this.expression = expression    ;
        this.thenPart   = thenPart      ;
        this.elsePart   = elsePart      ;
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitIfStat(this);
    }

    public final Expression evaluationExpression() {

        return (Expression) children[0];

    }

    public final Statement getThenPart() {

        return (Statement) children[1];

    }

    public final Statement getElsePart() {

        return (Statement) children[2];

    }

    public final boolean definesThenPart() {

        return this.thenPart != null;

    }

    public final boolean definesElsePart() {

        return this.elsePart != null;

    }

    public final void setThenPart(final Statement thenPart) {

        this.thenPart       = thenPart;
        this.children[1]    = thenPart;

    }

    public final void setElsePart(final Statement elsePart) {

        this.thenPart       = elsePart;
        this.children[1]    = elsePart;

    }

}