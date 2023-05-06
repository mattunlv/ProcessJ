package ast;

import utilities.Visitor;

public class BreakStat extends Statement {

    public BreakStat(Token t, Name target) {
        super(t);
        nchildren = 1;
        children = new AST[] { target };
    }

    public Name target() {
        return (Name) children[0];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitBreakStat(this);
    }

}