package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

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

    public Name getName() {
        return (Name) children[0];
    }

    public Expression getExpression() {
	return (Expression) children[1];
    }


    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitRecordMemberLiteral(this);
    }
}