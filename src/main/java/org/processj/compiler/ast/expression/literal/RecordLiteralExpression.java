package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class RecordLiteralExpression extends LiteralExpression implements SymbolMap.Context {

    private final Name name;
    private final Sequence<RecordMemberLiteralExpression> recordMemberLiterals;

    public RecordLiteralExpression(final Name name, final Sequence<RecordMemberLiteralExpression> members) {
        super(name);
        nchildren = 2;
        children = new AST[] { name, (members != null) ? members : new Sequence<>() };
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

    public <S> S visit(final Visitor<S> visitor) throws Phase.Error {
        // Open the scope
        visitor.setScope(this.openScope(visitor.getScope()));

        // Visit
        S result = visitor.visitRecordLiteralExpression(this);

        // Close the scope
        visitor.setScope(visitor.getScope().getEnclosingScope());

        return result;
    }

    @Override
    public BlockStatement getMergeBody() {
        return null;
    }

    @Override
    public BlockStatement getClearedMergeBody() {
        return null;
    }

    @Override
    public boolean definesLabel() {
        return false;
    }

    @Override
    public boolean definesEndLabel() {
        return false;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public void setEndLabel(String label) {

    }

    @Override
    public String getEndLabel() {
        return null;
    }
}