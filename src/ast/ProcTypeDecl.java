package ast;

import utilities.Visitor;

public class ProcTypeDecl extends Type implements DefineTopLevelDecl {

    public boolean isNative = false;
    public String library; // Name of the library, e.g. math.h
    public String filename; // Name of the file, e.g math
    public String nativeFunction; // Name of the native function, e.g. fabs
    public boolean yields = false;

    public ProcTypeDecl(Sequence<Modifier> modifiers, Type returnType, Name name, Sequence<ParamDecl> formals,
            Sequence<Name> implement, Annotations annotations, Block body) {
        super(name);
        nchildren = 7;
        children = new AST[] { modifiers, returnType, name, formals, implement, annotations, body };
    }

    public Sequence<Modifier> modifiers() {
        return (Sequence<Modifier>) children[0];
    }

    public Type returnType() {
        return (Type) children[1];
    }

    public Name name() {
        return (Name) children[2];
    }

    public Sequence<ParamDecl> formalParams() {
        return (Sequence<ParamDecl>) children[3];
    }

    public Sequence<Name> implement() {
        return (Sequence<Name>) children[4];
    }

    public Annotations annotations() {
        return (Annotations) children[5];
    }

    public Block body() {
        return (Block) children[6];
    }

    public String typeName() {
        return "Proc: " + name();
    }

    public String signature() {
        String s = "(";
        for (ParamDecl pd : formalParams())
            s = s + pd.type().signature();
        s = s + ")" + returnType().signature();
        return s;
    }

    // *************************************************************************
    // ** Visitor Related Methods

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitProcTypeDecl(this);
    }

    // *************************************************************************
    // ** Type Related Methods

    @Override
    public boolean isProcType() {
        return true;
    }

    // α = procedure(name1, {t1,1, . . . , t1,m1 }, t1) ∧ β = procedure(name2,
    // {t2,1, . . . , t2,m2 }, t2)
    // α =T β ⇔ procedure?(α) ∧ procedure?(β) ∧ (m1 = m2) ∧ (t1 =T t2) ∧ (name1 =
    // name2) ∧ ∧^m1_i=1 (t1,i =T t2,i)
    @Override
    public boolean typeEqual(Type t) {
        // procedure?(β)
        if (!t.isProcType())
            return false;
        ProcTypeDecl other = (ProcTypeDecl) t;
        // (m1 = m2)
        if (formalParams().size() != other.formalParams().size())
            return false;
        // (t1 =T t2)
        if (!returnType().typeEqual(other.returnType()))
            return false;
        // (name1 = name2) ∧
        if (!name().getname().equals(other.name().getname()))
            return false;
        // ∧^m1_i=1 (t1,i =T t2,i)
        boolean eq = true;
        for (int i = 0; i < formalParams().size(); i++) {
            eq = eq && formalParams().child(i).type().typeEqual(other.formalParams().child(i).type());
        }
        return eq;
    }

    // α ∼T β ⇔ α =T β
    @Override
    public boolean typeEquivalent(Type t) {
        return this.typeEqual(t);
    }

    // α = procedure(name1, {t1,1, . . . , t1,m1 }, t1) ∧ β = procedure(name2,
    // {t2,1, . . . , t2,m2 }, t2)
    // α "=T β ⇔ procedure?(α) ∧ procedure?(β) ∧ (m1 = m2) ∧ (t2 :=T t1) ∧ ∧^m1_i=1
    // (t1,i :=T t2,i)
    @Override
    public boolean typeAssignmentCompatible(Type t) {
        // procedure?(β)
        if (!t.isProcType())
            return false;
        ProcTypeDecl other = (ProcTypeDecl) t;
        // (m1 = m2)
        if (formalParams().size() != other.formalParams().size())
            return false;
        // (t2 :=T t1)
        if (!other.returnType().typeAssignmentCompatible(this))
            return false;
        // ∧^m1_i=1 (t1,i =T t2,i)
        boolean eq = true;
        for (int i = 0; i < formalParams().size(); i++) {
            eq = eq && formalParams().child(i).type().typeAssignmentCompatible(other.formalParams().child(i).type());
        }
        return eq;
    }
}