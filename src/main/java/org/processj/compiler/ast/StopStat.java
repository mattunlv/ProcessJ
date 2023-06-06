package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class StopStat extends Statement {

    public StopStat(Token t) {
        super(t);
        nchildren = 0;
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitStopStat(this);
    }
}