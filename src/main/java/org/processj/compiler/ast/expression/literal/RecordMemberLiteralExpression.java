package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class RecordMemberLiteralExpression extends AST {

    private final Name name;

    public RecordMemberLiteralExpression(Name name, Expression expr) {
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

    public void accept(Visitor v) throws Phase.Error {
        v.visitRecordMemberLiteral(this);
    }
}