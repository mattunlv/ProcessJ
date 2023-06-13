package org.processj.compiler.ast.type;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.expression.literal.PrimitiveLiteralExpression;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class PrimitiveType extends Type {

    public final static int BooleanKind = PrimitiveLiteralExpression.BooleanKind;
    public final static int CharKind = PrimitiveLiteralExpression.CharKind;
    public final static int ByteKind = PrimitiveLiteralExpression.ByteKind;
    public final static int ShortKind = PrimitiveLiteralExpression.ShortKind;
    public final static int IntKind = PrimitiveLiteralExpression.IntKind;
    public final static int LongKind = PrimitiveLiteralExpression.LongKind;
    public final static int FloatKind = PrimitiveLiteralExpression.FloatKind;
    public final static int DoubleKind = PrimitiveLiteralExpression.DoubleKind;
    public final static int StringKind = PrimitiveLiteralExpression.StringKind;
    public final static int VoidKind = PrimitiveLiteralExpression.NullKind;
    public final static int BarrierKind = PrimitiveLiteralExpression.BarrierKind;
    public final static int TimerKind = PrimitiveLiteralExpression.TimerKind;

    private static String[] names = {
            "boolean", "byte", "short", "char", "int", "long", "float",
            "double", "string", "void", "barrier", "timer" };

    private int kind;

    /// ------------
    /// Constructors

    public PrimitiveType(final Token p_t, final int kind) {
        super(p_t);
        this.kind = kind;
    }

    public PrimitiveType(final int kind) {
        super((AST) null);
        this.kind = kind;
    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link PrimitiveType} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link PrimitiveType} & both
     *         represent the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof PrimitiveType);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link PrimitiveType}.</p>
     * @return Literal {@link String} representation of the {@link PrimitiveType}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return names[kind];

    }

    /// --------------------
    /// org.processj.ast.AST
    
    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link PrimitiveType}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitPrimitiveType(PrimitiveType)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor)
            throws Phase.Error {

        return visitor.visitPrimitiveType(this);

    }

    /// ---------------------
    /// org.processj.ast.Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ExternType}.</p>
     * @return The internal {@link String} signature representing the {@link ExternType}.
     * @since 0.1.0
     */
    @Override
    public final String getSignature() {
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

    public int getKind() {
        return kind;
    }

    // *************************************************************************
    // ** Type Related Methods

    // α =T β ⇔ Primitive?(α) ∧ Primitive?(β) ∧ α = β
    @Override
    public boolean typeEqual(final Type that) {

        return this.equals(that);

    }

    // α ∼T β ⇔ Primitive?(α) ∧ Primitive?(β) ∧ α =T β
    @Override
    public boolean typeEquivalent(Type that) {

        return this.equals(that);

    }

    // α :=T β ⇔ Primitive?(α) ∧ Primitive?(β) ∧ β ≤ α
    @Override
    public boolean typeAssignmentCompatible(Type t) {
        if (!(t instanceof PrimitiveType))
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
        if (!(t instanceof PrimitiveType))
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