package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

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