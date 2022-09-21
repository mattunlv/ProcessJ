package ast;

import utilities.Visitor;

public class NewArray extends Expression {

    public NewArray(Type baseType, Sequence<Expression> dimsExpr,
                    Sequence<AST> dims, ArrayLiteral init) {
        super(baseType);
        nchildren = 4;
        children = new AST[] { baseType, dimsExpr, dims, init };
    }

    public Type baseType() {
        return (Type) children[0];
    }

    public Sequence<Expression> dimsExpr() {
        return (Sequence<Expression>) children[1];
    }

    public Sequence<AST> dims() {
        return (Sequence<AST>) children[2];
    }

    public ArrayLiteral init() {
        return (ArrayLiteral) children[3];
    }

    public String toString() {
        return "" + baseType() + " " + dimsExpr() + " " + dims();
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitNewArray(this);
    }
}