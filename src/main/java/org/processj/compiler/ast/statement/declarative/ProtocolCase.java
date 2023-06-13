package org.processj.compiler.ast.statement.declarative;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.statement.declarative.RecordMemberDeclaration;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class ProtocolCase extends AST {

    public ProtocolCase(Name caseName, Sequence<RecordMemberDeclaration> body) {
        super(caseName);
        nchildren = 2;
        children = new AST[] { caseName, body };
    }

    public Name getName() {

        return (Name) children[0];

    }

    public Sequence<RecordMemberDeclaration> getBody() {

        return (Sequence<RecordMemberDeclaration>) children[1];

    }

    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitProtocolCase(this);

    }

}