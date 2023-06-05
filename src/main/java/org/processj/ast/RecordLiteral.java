package org.processj.ast;

import org.processj.Phase;
import org.processj.utilities.Visitor;

public class RecordLiteral extends Literal {

    private final Name name;
    private final Sequence<RecordMemberLiteral> recordMemberLiterals;

    public RecordLiteral(final Name name, final Sequence<RecordMemberLiteral> members) {
        super(name);
        nchildren = 2;
        children = new AST[] { name, (members != null) ? members : new Sequence<>() };
        this.name = name;
        this.recordMemberLiterals = (Sequence<RecordMemberLiteral>) this.children[1];
    }

    @Override
    public final String toString() {

        return this.name.toString();

    }

    public Name getName() {
        return (Name) children[0];
    }

    public Sequence<RecordMemberLiteral> getRecordMemberLiterals() {
        return this.recordMemberLiterals;
    }

    public <S> S visit(Visitor<S> v)
            throws Phase.Error {
        return v.visitRecordLiteral(this);
    }
}