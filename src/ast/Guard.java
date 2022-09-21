package ast;

import utilities.Visitor;

public class Guard extends AST {

    public Guard(Statement guard) {
        super(guard);
        nchildren = 1;
        children = new AST[] { guard };
    }

    public Statement guard() {
        return (Statement) children[0];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitGuard(this);
    }
}