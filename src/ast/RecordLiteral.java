package ast;

import utilities.Visitor;

public class RecordLiteral extends Literal {

    public RecordTypeDecl myTypeDecl = null; // set in NameChecker/NameChecker/visitRecordLiteral()

    public RecordLiteral(Name name, Sequence<RecordMemberLiteral> members) {
        super(name);
        nchildren = 2;
        children = new AST[] { name, members };
    }

    public Name name() {
        return (Name) children[0];
    }

    public Sequence<RecordMemberLiteral> members() {
        return (Sequence<RecordMemberLiteral>) children[1];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitRecordLiteral(this);
    }
}