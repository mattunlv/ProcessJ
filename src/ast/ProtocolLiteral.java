package ast;

import utilities.Visitor;

public class ProtocolLiteral extends Literal {

    public ProtocolTypeDecl myTypeDecl = null; // set in NameChecker/NameChecker/visitProtocolLiteral()
    public ProtocolCase myChosenCase = null; // set in NameChecker/NameChecker/visitProtocolLiteral()

    public ProtocolLiteral(Name name, Name tag, Sequence<RecordMemberLiteral> expressions) {
        super(name);
        nchildren = 3;
        children = new AST[] { name, tag, expressions };
    }

    public Name name() {
        return (Name) children[0];
    }

    public Name tag() {
        return (Name) children[1];
    }

    public Sequence<RecordMemberLiteral> expressions() {
        return (Sequence<RecordMemberLiteral>) children[2];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitProtocolLiteral(this);
    }
}