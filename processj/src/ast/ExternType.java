package ast;

import utilities.Visitor;

public class ExternType extends Type {

    public ExternType(Name name) {
        super(name);
        nchildren = 1;
        children = new AST[] { name };
    }

    public Name name() {
        return (Name) children[0];
    }

    public String typeName() {
        return "ExternType: " + name();
    }

    public String signature() {
        return "E" + name().getname() + ";";
    }

    public String toString() {
        return typeName();
    }

    // *************************************************************************
    // ** Visitor Related Methods

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitExternType(this);
    }

    // *************************************************************************
    // ** Type Related Methods

    @Override
    public boolean isExternType() {
        return true;
    }

    // α = ExternType(n1) and β = ExternType(n2)
    // α =T β ⇔ n1 == n2
    @Override
    public boolean typeEqual(Type t) {
        if (!t.isExternType())
            return false;
        ExternType other = (ExternType) t;
        return name().getname().equals(other.name().getname());
    }

    // α ~T β ⇔ α =T β
    @Override
    public boolean typeEquivalent(Type t) {
        return this.typeEqual(t);
    }

    // α :=T β ⇔ α =T β
    @Override
    public boolean typeAssignmentCompatible(Type t) {
        return this.typeEqual(t);
    }
}