package org.processj.ast;

import org.processj.Phase;
import org.processj.utilities.Visitor;

public class ContinueStat extends Statement {

    public ContinueStat(Token t, Name target) {
        super(t);
        nchildren = 1;
        children = new AST[] { target };
    }

    public Name target() {
        return (Name) children[0];
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitContinueStat(this);
    }
}