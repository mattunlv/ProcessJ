package ast;

import utilities.Visitor;

public class ContinueStat extends Statement {

    public ContinueStat(Token t, Name target) {
        super(t);
        nchildren = 1;
        children = new AST[] { target };
    }

    public Name target() {
        return (Name) children[0];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitContinueStat(this);
    }
}