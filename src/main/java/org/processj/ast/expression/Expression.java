package org.processj.ast.expression;

import org.processj.ast.AST;
import org.processj.ast.Token;
import org.processj.ast.Type;

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