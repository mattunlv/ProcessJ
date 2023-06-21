package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.*;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class RecordLiteralExpression extends LiteralExpression {

    private final Name name;
    private final Sequence<RecordMemberLiteralExpression> recordMemberLiterals;

    public RecordLiteralExpression(final Name name, final Sequence<RecordMemberLiteralExpression> members) {
        super(new AST[] { name, (members != null) ? members : new Sequence<>() });
        this.name = name;
        this.recordMemberLiterals = (Sequence<RecordMemberLiteralExpression>) this.children[1];
    }

    @Override
    public final String toString() {

        return this.name.toString();

    }

    public Name getName() {
        return (Name) children[0];
    }

    public Sequence<RecordMemberLiteralExpression> getRecordMemberLiterals() {
        return this.recordMemberLiterals;
    }

    public void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Visit
        visitor.visitRecordLiteralExpression(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

}