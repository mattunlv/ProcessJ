package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class NameExpr extends Expression {

    public AST myDecl = null;

    private final Name name;

    public NameExpr(Name name) {
        super(name);
        nchildren = 1;
        children = new AST[] { name };
        this.name = name;
    }

    public final String getPackageName() {

        return this.name.getPackageName();

    }

    @Override
    public final String toString() {

        return this.name.toString();

    }

    public Name getName() {
        return (Name) children[0];
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitNameExpr(this);
    }
}