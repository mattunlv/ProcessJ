package org.processj.compiler.ast.statement.declarative;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class ProtocolCase extends AST {

    public ProtocolCase(Name caseName, Sequence<RecordMemberDeclaration> body) {
        super(new AST[] { caseName, body });
    }

    public Name getName() {

        return (Name) children[0];

    }

    public Sequence<RecordMemberDeclaration> getBody() {

        return (Sequence<RecordMemberDeclaration>) children[1];

    }

    public final void accept(final Visitor visitor) throws Phase.Error {

        visitor.visitProtocolCase(this);

    }

}