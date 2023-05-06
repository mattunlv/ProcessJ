package ast;

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
}