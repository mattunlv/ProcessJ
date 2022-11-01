package ast;

import utilities.Visitor;

public class ProtocolTypeDecl extends Type implements DefineTopLevelDecl {

    public ProtocolTypeDecl(Sequence<Modifier> modifiers, Name name,
                            Sequence<AST> extend, Annotations annotations,
                            Sequence<ProtocolCase> body) {
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

    public Sequence<ProtocolCase> body() {
        return (Sequence<ProtocolCase>) children[4];
    }

    // *************************************************************************
    // ** Misc. Methods

    public String toString() {
        return typeName();
    }

    // *************************************************************************
    // ** Visitor Related Methods

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitProtocolTypeDecl(this);
    }

    // *************************************************************************
    // ** Type Related Methods

    public boolean extendsProtocol(ProtocolTypeDecl pd) {
        if (typeEqual(pd))
            return true;
        boolean b = false;
        for (Name n : extend())
            b = ((ProtocolTypeDecl) n.myDecl).extendsProtocol(pd) || b;
        return b;
    }

    public ProtocolCase getCase(String name) {
        /** Search our own body first */
        if (body() != null) {
            for (ProtocolCase pc : body()) {
                if (pc.name().getname().equals(name))
                    return pc;
            }
        }
        /** This protocol type did not have the case */
        ProtocolCase p = null;
        for (Name n : extend()) {
            p = ((ProtocolTypeDecl) n.myDecl).getCase(name);
            if (p != null)
                return p;
        }
        return null;
    }

    public String signature() {
        return "<P" + name().getname() + ";";
    }

    public String typeName() {
        return "Protocol: " + name();
    }

    @Override
    public boolean isProtocolType() {
        return true;
    }

    @Override
    public boolean typeEqual(Type t) {
        if (!t.isProtocolType())
            return false;
        ProtocolTypeDecl pt = (ProtocolTypeDecl) t;
        return name().getname().equals(pt.name().getname());
    }

    @Override
    public boolean typeEquivalent(Type t) {
        return typeEqual(t);
    }

    // TODO
    @Override
    public boolean typeAssignmentCompatible(Type t) {
        if (!t.isProtocolType())
            return false;
        ProtocolTypeDecl pt = (ProtocolTypeDecl) t;
        return pt.extendsProtocol(this);
    }
}