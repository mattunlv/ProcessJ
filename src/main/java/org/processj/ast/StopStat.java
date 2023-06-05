package org.processj.ast;

import org.processj.Phase;
import org.processj.utilities.Visitor;

public class StopStat extends Statement {

    public StopStat(Token t) {
        super(t);
        nchildren = 0;
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitStopStat(this);
    }
}