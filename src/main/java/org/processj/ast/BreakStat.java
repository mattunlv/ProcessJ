package org.processj.ast;

import org.processj.Phase;
import org.processj.utilities.Visitor;

public class BreakStat extends Statement {

    public BreakStat(Token t, Name target) {
        super(t);
        nchildren = 1;
        children = new AST[] { target };
    }

    public Name target() {
        return (Name) children[0];
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitBreakStat(this);
    }

}