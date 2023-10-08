package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class RecordMemberLiteralExpression extends AST {

    private final Name name;
    private final Expression expression;

    public RecordMemberLiteralExpression(Name name, Expression expr) {
        super(name);
        this.name = name;
        this.expression = expr;
    }

    @Override
    public final String toString() {

        return this.name.toString();

    }

    public Name getName() {
        return this.name;
    }

    public Expression getExpression() {
	return this.expression;
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitRecordMemberLiteral(this);
    }
}