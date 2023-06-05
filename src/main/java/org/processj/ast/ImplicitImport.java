package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class ImplicitImport extends Expression {

    public ImplicitImport(Name packageName, Name fileName, Name typeName) {
        super((AST) null);
        nchildren = 3;
        children = new AST[] { packageName, fileName, typeName };
    }

    public Name packageName() {
        return (Name) children[0];
    }

    public Name fileName() {
        return (Name) children[1];
    }

    public Name typeName() {
        return (Name) children[2];
    }

    public String toString() {
        return ((packageName() == null) ? "" : packageName()) + "."
            + fileName() + "." + typeName();
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitImplicitImport(this);
    }
}