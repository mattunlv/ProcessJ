package ast;

import utilities.Visitor;
import java.util.ArrayList;

public class ForStat extends LoopStatement {

    // This list is filled by the ParFor visitor and will contain the expressions inside a 
    // par for that alter states (assignment, pre and post increment/decrements).
    public ArrayList<Expression> vars = null;

    /* Note that init() and incr() can be null */
    public boolean par;

    public ForStat(Token t, Sequence<Statement> init,
                   Expression expr,
                   Sequence<ExprStat> incr ,
                   Sequence<Expression> barriers,
                   Statement stat,
                   boolean par) {
        super(t);
        nchildren = 5;
        this.par = par;
        children = new AST[] { init, expr, incr, barriers, stat };
    }

    public boolean isPar() {
        return par;
    }

    public Sequence<Statement> init() {
        return (Sequence<Statement>) children[0];
    }

    public Expression expr() {
        return (Expression) children[1];
    }

    public Sequence<ExprStat> incr() {
        return (Sequence<ExprStat>) children[2];
    }

    public Sequence<Expression> barriers() {
        return (Sequence<Expression>) children[3];
    }

    public Statement stats() {
        return (Statement) children[4];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitForStat(this);
    }
}