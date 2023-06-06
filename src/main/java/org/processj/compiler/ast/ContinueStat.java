package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class ContinueStat extends Statement {

    public ContinueStat(Token t, Name target) {
        super(t);
        nchildren = 1;
        children = new AST[] { target };
    }

    public Name getTarget() {
        return (Name) children[0];
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitContinueStat(this);
    }
}