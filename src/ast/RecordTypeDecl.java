package ast;

import utilities.Visitor;

public class RecordTypeDecl extends Type implements DefineTopLevelDecl {

    public RecordTypeDecl(Sequence<Modifier> modifiers, Name name, 
                          Sequence<AST> extend, Annotations annotations,
                          Sequence<RecordMember> body) {
        super(name);
        nchildren = 5;
        children = new AST[] { modifiers, name, extend, annotations, body };
    }

    // *************************************************************************
    // ** Accessor Methods

    public Sequence<Modifier> modifiers() {
        return (Sequence<Modifier>) children[0];
    }

    public Name name() {
        return (Name) children[1];
    }

    public Sequence<Name> extend() {
        return (Sequence<Name>) children[2];
    }

    public Annotations annotations() {
        return (Annotations) children[3];
    }

    public Sequence<RecordMember> body() {
        return (Sequence<RecordMember>) children[4];
    }

    // *************************************************************************
    // ** Misc. Methods

    public RecordMember getMember(String name) {
        for (RecordMember rm : body())
            if (rm.name().getname().equals(name))
                return rm;
        return null;
    }

    public String toString() {
        return typeName();
    }
    
    public boolean extendsRecord(RecordTypeDecl rt) {
        if (typeEqual(rt))
            return true;
        boolean b = false;
        for (Name n : extend())
            b = ((RecordTypeDecl) n.myDecl).extendsRecord(rt) || b;
        return b;
    }

    // *************************************************************************
    // ** Visitor Related Methods

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitRecordTypeDecl(this);
    }

    // *************************************************************************
    // ** Type Related Methods
    
    @Override
    public String signature() {
        return "<R" + name().getname() + ";";
    }

    public String typeName() {
        return "Record: " + name();
    }

    @Override
    public boolean isRecordType() {
        return true;
    }

    // α =T β ⇔ Record?(α) ∧ Record?(β) ∧ (name1 = name2)
    // We implement NAME EQUALITY not structural equality
    @Override
    public boolean typeEqual(Type t) {
        if (!t.isRecordType())
            return false;
        RecordTypeDecl rt = (RecordTypeDecl) t;
        return name().getname().equals(rt.name().getname());
    }

    // α∼T β ⇔ α =T β
    @Override
    public boolean typeEquivalent(Type t) {
        return typeEqual(t);
    }

    // α :=T β ⇔ α ∼T β ⇔ α =T β
    @Override
    public boolean typeAssignmentCompatible(Type t) {
        if (!t.isRecordType())
            return false;
        RecordTypeDecl rt = (RecordTypeDecl) t;
        return rt.extendsRecord(this);
    }
}