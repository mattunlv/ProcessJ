package ast;

import utilities.Visitor;

public class IfStat extends Statement {

    /** Note that elsepart() can return null when having code
     * like
     *
     *      if ( expression )
     *          statement
     * */

    public IfStat(Expression expr, Statement thenpart, Statement elsepart) {
        super(expr);
        nchildren = 3;
        children = new AST[] { expr, thenpart, elsepart };
    }

    public Expression expr() {
        return (Expression) children[0];
    }

    public Statement thenpart() {
        return (Statement) children[1];
    }

    public Statement elsepart() {
        return (Statement) children[2];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitIfStat(this);
    }
}