package ast;

import utilities.Visitor;

public class StopStat extends Statement {

    public StopStat(Token t) {
        super(t);
        nchildren = 0;
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitStopStat(this);
    }
}