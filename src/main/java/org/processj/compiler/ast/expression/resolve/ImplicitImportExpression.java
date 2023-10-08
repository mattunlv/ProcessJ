package org.processj.compiler.ast.expression.resolve;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class ImplicitImportExpression extends Expression {

    private final Name packageName;
    private final Name fileName;
    private final Name typeName;

    public ImplicitImportExpression(Name packageName, Name fileName, Name typeName) {
        super(new AST[] { packageName, fileName, typeName });
        this.packageName = packageName;
        this.fileName = fileName;
        this.typeName = typeName;
    }

    public Name packageName() {
        return this.packageName;
    }

    public Name fileName() {
        return this.fileName;
    }

    public Name typeName() {
        return this.typeName;
    }

    public String toString() {
        return ((packageName() == null) ? "" : packageName()) + "."
            + fileName() + "." + typeName();
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitImplicitImportExpression(this);
    }
}