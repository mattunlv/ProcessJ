package org.processj.compiler.ast.expression.resolve;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class NameExpression extends Expression {

    public AST myDecl = null;

    private final Name name;

    public NameExpression(final Name name, final Type type) {
        super(name, type);
        this.name = name;
    }
    public NameExpression(Name name) {
        super(new AST[] { name });
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
        return this.name;
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitNameExpression(this);
    }
}