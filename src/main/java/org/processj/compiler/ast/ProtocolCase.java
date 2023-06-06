package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class ProtocolCase extends AST {

    public ProtocolCase(Name caseName, Sequence<RecordMember> body) {
        super(caseName);
        nchildren = 2;
        children = new AST[] { caseName, body };
    }

    public Name name() {
        return (Name) children[0];
    }

    public Sequence<RecordMember> body() {
        return (Sequence<RecordMember>) children[1];
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitProtocolCase(this);
    }
}