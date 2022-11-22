package ast;

import utilities.Visitor;

// This ought to be fixed!!! Top-level constants should extend 'Type'
// instead of AST -- see 'visitCompilation' in CodeGeneratorJava.java
public class ConstantDecl extends AST implements VarDecl, DefineTopLevelDecl {

    /// ---------------
    /// Private Members

    private Type          type        ;
    private Name          name        ;
    private Expression    initializer ;

    public ConstantDecl(Sequence<Modifier> modifiers, Type type, Var var) {
        super(type);

        this.type           = type          ;
        this.name           = var.name()    ;
        this.initializer    = var.init()    ;

        nchildren = 3;
        children = new AST[] { modifiers, type, var };
    }

    public void setType(Type t) {
        this.type = t;
        children[1] = t;
    }

    public Sequence<Modifier> modifiers() {
        return (Sequence<Modifier>) children[0];
    }

    public Type getType() {

        return this.type;

    }

    public Name getName() {

        return this.name;

    }

    public Expression getInitializerExpression() {

        return this.initializer;

    }

    public Type type() {
        return (Type) children[1];
    }

    public Var var() {
        return (Var) children[2];
    }

    public String name() {
        return var().name().getname();
    }

    public String toString() {
        return "ConstantDecl (Type:" + type() + " Name:" + var() + ")";
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitConstantDecl(this);
    }
}