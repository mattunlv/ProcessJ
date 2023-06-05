package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class RecordMemberLiteral extends AST {

    private final Name name;

    public RecordMemberLiteral(Name name, Expression expr) {
        super(new AST[] { name, expr });
        this.name = name;
    }

    @Override
    public final String toString() {

        return this.name.toString();

    }

    public Name name() {
        return (Name) children[0];
    }

    public Expression expr() {
	return (Expression) children[1];
    }


    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitRecordMemberLiteral(this);
    }
}