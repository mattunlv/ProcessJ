package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class RecordLiteral extends Literal implements SymbolMap.Context {

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

    public <S> S visit(final Visitor<S> visitor) throws Phase.Error {
        // Open the scope
        visitor.setScope(this.openScope(visitor.getScope()));

        // Visit
        S result = visitor.visitRecordLiteral(this);

        // Close the scope
        visitor.setScope(visitor.getScope().getEnclosingScope());

        return result;
    }
}