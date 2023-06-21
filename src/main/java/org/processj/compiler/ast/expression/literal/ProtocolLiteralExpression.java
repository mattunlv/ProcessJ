package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.declarative.ProtocolCase;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class ProtocolLiteralExpression extends LiteralExpression {

    private ProtocolCase protocolCase    ;
    private final Name name            ;
    private final Name      tag             ;

    public ProtocolLiteralExpression(final Name name, final Name tag, final Sequence<RecordMemberLiteralExpression> expressions) {
        super(new AST[] { name, tag, expressions });
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

    public final void accept(Visitor visitor) throws Phase.Error {

        // Open the scope
        visitor.setContext(this.openContext(visitor.getContext()));

        // Visit
        visitor.visitProtocolLiteralExpression(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

}