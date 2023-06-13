package org.processj.compiler.ast.expression.resolve;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class NameExpression extends Expression {

    public AST myDecl = null;

    private final Name name;

    public NameExpression(final Name name, final Type type) {
        super(name, type);
        this.name = name;
    }
    public NameExpression(Name name) {
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
        return v.visitNameExpression(this);
    }
}