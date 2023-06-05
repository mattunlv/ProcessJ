package org.processj.ast;

import org.processj.Phase;
import org.processj.utilities.Visitor;

public class SkipStat extends Statement {

    public SkipStat(Token t) {
        super(t);
        nchildren = 0;
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitSkipStat(this);
    }
}