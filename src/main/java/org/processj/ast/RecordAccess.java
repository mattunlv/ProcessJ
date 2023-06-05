package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class RecordAccess extends Expression {

    public boolean isArraySize = false;
    public boolean isStringLength = false;
    private final Name field;
    private final Expression record;

    public RecordAccess(Expression record, Name field) {
        super(record);
        nchildren = 2;
        children = new AST[] { record, field };
        this.field = field;
        this.record = record;
    }

    public Expression record() {
        return (Expression) children[0];
    }

    public Name field() {
        return (Name) children[1];
    }

    public String toString() {
        return this.record + "." + this.field;
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitRecordAccess(this);
    }
}