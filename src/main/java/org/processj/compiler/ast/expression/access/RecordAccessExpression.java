package org.processj.compiler.ast.expression.access;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class RecordAccessExpression extends Expression {

    public boolean isArraySize = false;
    public boolean isStringLength = false;
    private final Name field;
    private final Expression record;

    public RecordAccessExpression(Expression record, Name field) {
        super(new AST[] { record, field });
        this.field = field;
        this.record = record;
    }

    public Expression getTarget() {
        return (Expression) children[0];
    }

    public Name field() {
        return (Name) children[1];
    }

    public String toString() {
        return this.record + "." + this.field;
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitRecordAccessExpression(this);
    }
}