package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.ast.statement.declarative.ProtocolCase;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class ProtocolLiteralExpression extends LiteralExpression implements SymbolMap.Context {

    private ProtocolCase protocolCase    ;
    private final Name name            ;
    private final Name      tag             ;

    public ProtocolLiteralExpression(final Name name, final Name tag, final Sequence<RecordMemberLiteralExpression> expressions) {
        super(name);
        nchildren = 3;
        children = new AST[] { name, tag, expressions };
        this.tag            = tag;
        this.name           = name;
        this.protocolCase   = null;
    }

    @Override
    public final String toString() {

        return this.name.toString();

    }

    public Name getTag() {
        return (Name) children[1];
    }

    public final Name getName() {

        return this.name;

    }

    public final String getTagLiteral() {

        return this.tag.toString();

    }

    public final void setProtocolCase(final ProtocolCase protocolCase) {

        this.protocolCase = protocolCase;

    }

    public final Sequence<RecordMemberLiteralExpression> getExpressions() {
        return (Sequence<RecordMemberLiteralExpression>) children[2];
    }

    public final <S> S visit(Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        visitor.setScope(this.openScope(visitor.getScope()));

        // Visit
        S result = visitor.visitProtocolLiteralExpression(this);

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