package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class SuspendStat extends Statement {

    public SuspendStat(Token t, Sequence<ParamDecl> params) {
        super(t);
        nchildren = 1;
        children = new AST[] { params };
    }

    public Sequence<ParamDecl> params() {
        return (Sequence<ParamDecl>) children[0];
    }

    public String signature() {
        String s = "(";
        for (ParamDecl pd : params())
            s = s + pd.getType().getSignature();
        s = s + ")V";
        return s;
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitSuspendStat(this);
    }
}