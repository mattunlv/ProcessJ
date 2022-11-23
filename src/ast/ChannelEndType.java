package ast;

import utilities.Visitor;

public class ChannelEndType extends Type {

    public static final int SHARED = 0;
    public static final int NOT_SHARED = 1;

    public static final int READ_END = 0;
    public static final int WRITE_END = 1;
    public static final int byteSizeC = 4; // 32-bit pointer.

    private int shared;
    private int end;

    public ChannelEndType(int shared, Type baseType, int end) {
        super(baseType);
        this.shared = shared;
        this.end = end;
        nchildren = 1;
        children = new AST[] { baseType };
    }

    public String typeName() {
        return "chan<" + baseType() + ">." + (isRead() ? "read" : "write");
    }

    // TODO: perhaps the base type of a channel end type ought to be a channel ;->

    public int byteSizeC() {
        return byteSizeC;
    }

    public String signature() {
        return "{" + baseType().signature() + ";" + (isRead() ? "?" : "!");
    }

    public String toString() {
        return typeName();
    }

    public boolean isShared() {
        return shared == SHARED;
    }

    public boolean isRead() {
        return end == READ_END;
    }

    public boolean isWrite() {
        return end == WRITE_END;
    }

    public Type baseType() {
        return (Type) children[0];
    }

    public boolean isChannelEndType() {
        return true;
    }


    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitChannelEndType(this);
    }
    
    // α =T β ⇔ Channel?(α) ∧ Channel?(β) ∧ α = β ∧ (m1 = m2)
    @Override
    public boolean typeEqual(Type t) {
        if (!t.isChannelEndType())
            return false;
        boolean b = false;
        /** Protocols and records are NamedTyped for channel ends and channels. We
         * must use the `actualType' which can be retrieved from the type() method */
        ChannelEndType cet = (ChannelEndType) t;
        if (cet.baseType().isNamedType() && this.baseType().isNamedType())
            b = ((NamedType)baseType()).type().typeAssignmentCompatible(((NamedType)cet.baseType()).type());
        else
            b = baseType().typeAssignmentCompatible(cet.baseType());
        /* Check if both channels' ends shared in the same way */
        if (b) {
            boolean bb = false;
            if (this.isShared() && cet.isShared())
                bb = true;
            else if ((this.isRead() && cet.isRead()) || (this.isWrite() && cet.isWrite()))
                bb = true;
            b = b && bb;
        }
        return b;
    }
    
    // α =T β ⇔ Channel?(α) ∧ Channel?(β) ∧ α = β ∧ (m1 = m2)
    @Override
    public boolean typeEquivalent(Type t) {
        return this.typeEqual(t);
    }

    @Override
    public boolean typeAssignmentCompatible(Type t) {
        return this.typeEqual(t);
    }
}