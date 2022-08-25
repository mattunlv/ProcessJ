package ast;

import utilities.Visitor;

public class NewMobile extends Expression {

    public DefineTopLevelDecl myDecl = null;

    public NewMobile(Name name) {
        super(name);
        nchildren = 1;
        children = new AST[] { name };
    }

    public Name name() {
        return (Name) children[0];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitNewMobile(this);
    }
}