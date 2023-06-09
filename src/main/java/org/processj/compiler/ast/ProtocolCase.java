package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class ProtocolCase extends AST {

    public ProtocolCase(Name caseName, Sequence<RecordMember> body) {
        super(caseName);
        nchildren = 2;
        children = new AST[] { caseName, body };
    }

    public Name getName() {

        return (Name) children[0];

    }

    public Sequence<RecordMember> getBody() {

        return (Sequence<RecordMember>) children[1];

    }

    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitProtocolCase(this);

    }

}