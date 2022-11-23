package ast;

import utilities.Visitor;

public class PrimitiveType extends Type {

    public final static int BooleanKind = PrimitiveLiteral.BooleanKind;
    public final static int CharKind = PrimitiveLiteral.CharKind;
    public final static int ByteKind = PrimitiveLiteral.ByteKind;
    public final static int ShortKind = PrimitiveLiteral.ShortKind;
    public final static int IntKind = PrimitiveLiteral.IntKind;
    public final static int LongKind = PrimitiveLiteral.LongKind;
    public final static int FloatKind = PrimitiveLiteral.FloatKind;
    public final static int DoubleKind = PrimitiveLiteral.DoubleKind;
    public final static int StringKind = PrimitiveLiteral.StringKind;
    public final static int VoidKind = PrimitiveLiteral.NullKind;
    public final static int BarrierKind = PrimitiveLiteral.BarrierKind;
    public final static int TimerKind = PrimitiveLiteral.TimerKind;

    private static String[] names = {
            "boolean", "byte", "short", "char", "int", "long", "float",
            "double", "string", "void", "barrier", "timer" };

    private int kind;

    public PrimitiveType(Token p_t, int kind) {
        super(p_t);
        this.kind = kind;
    }

    public PrimitiveType(int kind) {
        super((AST) null);
        this.kind = kind;
    }

    /** Return the size of this type in bytes in C. */
    public int byteSizeC() {
        switch (kind) {
        case BooleanKind:
            return 1;
        case ByteKind:
            return 1;
        case ShortKind:
            return 2;
        case CharKind:
            return 1;
        case IntKind:
            return 4;
        case LongKind:
            return 8;
        case FloatKind:
            return 4;
        case DoubleKind:
            return 8;
        case StringKind:
            return 4;
        case BarrierKind:
            // TODO
            return 4;
        case TimerKind:
            // TODO
            return 4;
        default:
            return -1;
        }
    }

    public static int ceiling(PrimitiveType p1, PrimitiveType p2) {
        if (p1.kind < p2.kind)
            return p2.kind;
        return p1.kind;
    }

    public String toString() {
        return typeName();
    }

    public String typeName() {
        return names[kind];
    }

    public int getKind() {
        return kind;
    }

    public String signature() {
        switch (kind) {
        case BooleanKind:
            return "Z";
        case ByteKind:
            return "B";
        case ShortKind:
            return "S";
        case CharKind:
            return "C";
        case IntKind:
            return "I";
        case LongKind:
            return "J";
        case FloatKind:
            return "F";
        case DoubleKind:
            return "D";
        case StringKind:
            return "T";
        case VoidKind:
            return "V";
        case BarrierKind:
            return "R";
        case TimerKind:
            return "M";
        default:
            return "UNKNOWN TYPE";
        }
    }

    // *************************************************************************
    // ** Visitor Related Methods

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitPrimitiveType(this);
    }

    // *************************************************************************
    // ** Type Related Methods

    // α =T β ⇔ Primitive?(α) ∧ Primitive?(β) ∧ α = β
    @Override
    public boolean typeEqual(Type t) {
        if (!t.isPrimitiveType())
            return false;
        PrimitiveType other = (PrimitiveType) t;
        return (this.kind == other.kind);
    }

    // α ∼T β ⇔ Primitive?(α) ∧ Primitive?(β) ∧ α =T β
    @Override
    public boolean typeEquivalent(Type t) {
        return this.typeEqual(t);
    }

    // α :=T β ⇔ Primitive?(α) ∧ Primitive?(β) ∧ β ≤ α
    @Override
    public boolean typeAssignmentCompatible(Type t) {
        if (!t.isPrimitiveType())
            return false;
        PrimitiveType other = (PrimitiveType) t;
        return other.typeLessThanEqual(this);
    }

    // α <T β ⇔ Numeric?(α) ∧ Numeric?(β)
    // Definition:
    // byte <T short <T char <T int <T long <T float <T double
    public boolean typeLessThan(Type t) {
        if (!this.isNumericType() || !t.isNumericType())
            return false;
        PrimitiveType other = (PrimitiveType) t;
        return this.kind < other.kind;
    }

    // α <=T β ⇔ Primitive?(α) ∧ Primitive?(β) ∧
    // (α =T β || (Numeric?(α) ∧ Numeric?(β) ∧ α <T β))
    public boolean typeLessThanEqual(Type t) {
        if (!t.isPrimitiveType())
            return false;
        if (t.typeEqual(this))
            return true;
        return this.typeLessThan(t);
    }

    public Type typeCeiling(PrimitiveType t) {
        // TODO: This should probably be an assertion as
        // ceiling should only ever be called on numeric types anyways
        if (!this.isNumericType() || !t.isNumericType())
            return new ErrorType();
        if (this.kind < IntKind && t.kind < IntKind)
            return new PrimitiveType(IntKind);

        if (this.kind < t.kind)
            return t;
        return this;
    }

    @Override
    public boolean isPrimitiveType() {
        return true;
    }

    @Override
    public boolean isIntegerType() {
        return (kind == IntKind);
    }

    @Override
    public boolean isTimerType() {
        return (kind == TimerKind);
    }

    @Override
    public boolean isBarrierType() {
        return (kind == BarrierKind);
    }

    @Override
    public boolean isBooleanType() {
        return (kind == BooleanKind);
    }

    @Override
    public boolean isByteType() {
        return (kind == ByteKind);
    }

    @Override
    public boolean isShortType() {
        return (kind == ShortKind);
    }

    @Override
    public boolean isCharType() {
        return (kind == CharKind);
    }

    @Override
    public boolean isLongType() {
        return (kind == LongKind);
    }

    @Override
    public boolean isVoidType() {
        return (kind == VoidKind);
    }

    @Override
    public boolean isStringType() {
        return (kind == StringKind);
    }

    @Override
    public boolean isFloatType() {
        return (kind == FloatKind);
    }

    @Override
    public boolean isDoubleType() {
        return (kind == DoubleKind);
    }

    @Override
    public boolean isNumericType() {
        return (isFloatType() || isDoubleType() || isIntegralType());
    }

    @Override
    public boolean isIntegralType() {
        return (isIntegerType() || isShortType() || isByteType() || isCharType() || isLongType());
    }
}