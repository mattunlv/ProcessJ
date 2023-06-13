package org.processj.compiler.ast.expression;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.type.Type;

public abstract class Expression extends AST {

    public Type type = null;
    private boolean yields;
    public boolean hasParens = false;

    public Expression(Token t) {
        super(t);
    }

    public Expression(AST a) {
        super(a);
    }
    public Expression(final Name name, final Type type) {
        super(name);
        this.type = type;
    }
    public Expression(final AST[] children) {
        super(children);

    }

    public Name getName() { return new Name(""); }

    public boolean isConstant() {
        return false;
    }

    public Object constantValue() {
        return null;
    }
    
    public boolean doesYield() { 
        return yields;
    }

    public void setYield() {
        yields = true;
    }

    public final Type getType() {

        return this.type;

    }

    public void setType(final Type type) {

        this.type = type;

    }



}