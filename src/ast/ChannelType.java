package ast;

import utilities.Visitor;

public class ChannelType extends Type {
    // These are the different values the field `shared' can take.
    public static final int SHARED_READ = 0;
    public static final int SHARED_WRITE = 1;
    public static final int SHARED_READ_WRITE = 2;
    public static final int NOT_SHARED = 3;

    public String modSyms[] = { "shared read", "shared write", "shared", "" };

    private int shared;

    public ChannelType(Type baseType, int shared) {
        super(baseType);
        this.shared = shared;
        nchildren = 1;
        children = new AST[] { baseType };
    }

    public int shared() {
        return shared;
    }

    public Type baseType() {
        return (Type) children[0];
    }

    public String modString() {
        return modSyms[shared];
    }

    public String signature() {
        return "{" + baseType().signature() + ";";
    }

    public String toString() {
        return "chan<" + baseType() + ">";
    }

    // TODO: add sharing stuff
    public String typeName() {
        return "chan<" + baseType() + ">";
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitChannelType(this);
    }

    public int byteSizeC() {
        // TODO.
        return 4;
    }

    // *************************************************************************
    // ** Visitor Related Methods

    @Override
    public boolean isChannelType() {
        return true;
    }

    // *************************************************************************
    // ** Type Related Methods

    // if α = Channel(t1, a1) ∧ β = Channel(t2, a2)
    // α =T β ⇔ Channel?(α) ∧ Channel?(β) ∧ (t1 =T t2) ∧ (a1 = a2)
    @Override
    public boolean typeEqual(Type t) {
        // Channel?(β) -- is t a channel?
        if (!t.isChannelType())
            return false;
        ChannelType other = (ChannelType) t;
        // (a1 = a2) -- are both channels' ends shared in the same way?
        if (shared != other.shared)
            return false;
        // (t1 =T t2) -- are the base types type equal?
        return baseType().typeEqual(other.baseType());
    }

    // α ∼T β ⇔ α =T β
    @Override
    public boolean typeEquivalent(Type t) {
        return this.typeEqual(t);
    }

    // Channels cannot be assigned.
    @Override
    public boolean typeAssignmentCompatible(Type t) {
        return false;
    }
}