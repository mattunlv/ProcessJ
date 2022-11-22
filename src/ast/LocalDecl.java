package ast;

import utilities.Visitor;

public class LocalDecl extends Statement implements VarDecl {

    private boolean     constant        ;
    private Type        type            ;
    private Name        name            ;
    private Expression  initializer     ;

    public LocalDecl(Type type, Var var, boolean constant) {
        super(type);

        this.type           = type                              ;
        this.name           = (Name)        var.children[0]     ;
        this.initializer    = (Expression)  var.children[1]     ;


        nchildren = 2;
        this.constant = constant;
        children = new AST[] { type, var };
    }

    public boolean isConst() {
        return constant;
    }

    public Type type() {
        return (Type) children[0];
    }

    public Var var() {
        return (Var) children[1];
    }

    public void setType(Type t) {
        children[0] = t;
    }

    public String name() {
        return var().name().getname();
    }

    public String toString() {
        return "LocalDecl (Type:" + type() + " " + "Name:" + var() + ")";
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitLocalDecl(this);
    }
}