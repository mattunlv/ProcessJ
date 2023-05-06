package ast;

import utilities.Visitor;

public class RecordMemberLiteral extends AST {

    public RecordMemberLiteral(Name name, Expression expr) {
        super(name);
        nchildren = 2;
        children = new AST[] { name, expr };
    }

    public Name name() {
        return (Name) children[0];
    }

    public Expression expr() {
	return (Expression) children[1];
    }


    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitRecordMemberLiteral(this);
    }
}