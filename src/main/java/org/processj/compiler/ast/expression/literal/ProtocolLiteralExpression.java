package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.type.ProtocolType;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class ProtocolLiteralExpression extends LiteralExpression {

    private ProtocolType.Case aCase;
    private final Name name            ;
    private final Name      tag             ;
    private final Sequence<RecordMemberLiteralExpression> expressions;

    public ProtocolLiteralExpression(final Name name, final Name tag, final Sequence<RecordMemberLiteralExpression> expressions) {
        super(new AST[] { name, tag, expressions });
        this.tag            = tag;
        this.name           = name;
        this.aCase = null;
        this.expressions = expressions;
    }

    @Override
    public final String toString() {

        return this.name.toString();

    }

    public Name getTag() {
        return this.tag;
    }

    public final Name getName() {

        return this.name;

    }

    public final String getTagLiteral() {

        return this.tag.toString();

    }

    public final void setProtocolCase(final ProtocolType.Case aCase) {

        this.aCase = aCase;

    }

    public final Sequence<RecordMemberLiteralExpression> getExpressions() {
        return this.expressions;
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