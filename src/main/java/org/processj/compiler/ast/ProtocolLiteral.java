package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class ProtocolLiteral extends Literal implements SymbolMap.Context {

    private ProtocolCase    protocolCase    ;
    private final Name      name            ;
    private final Name      tag             ;

    public ProtocolLiteral(final Name name, final Name tag, final Sequence<RecordMemberLiteral> expressions) {
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

    public final Sequence<RecordMemberLiteral> getExpressions() {
        return (Sequence<RecordMemberLiteral>) children[2];
    }

    public final <S> S visit(Visitor<S> visitor) throws Phase.Error {

        return visitor.visitProtocolLiteral(this);

    }

}