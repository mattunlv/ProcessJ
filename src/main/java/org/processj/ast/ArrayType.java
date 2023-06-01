package org.processj.ast;

import org.processj.utilities.Visitor;

/**
 * Where is ArrayType constructed?
 *  {@link org.processj.parser.Parser}
 *      - primitiveType Array
 *      - ChannelType Array
 *      - Name with Dims identifier[][]...[]
 *      - Package Type (Static Import)
 *  {@link org.processj.namechecker.ArrayTypeConstructor}
 *  {@link org.processj.phases.TypeChecker}
 *      {@link org.processj.phases.TypeChecker#visitArrayAccessExpr(ArrayAccessExpr)}
 *      {@link org.processj.phases.TypeChecker#arrayAssignmentCompatible(Type, Expression)}
 *      {@link org.processj.phases.TypeChecker#visitNewArray(NewArray)}
 * Which Order do these occur?
 *      1) Parser
 *      2) ArrayTypeConstructor
 *      3) TypeChecker
 *      As {@link ParamDecl}, {@link LocalDecl}, {@link ConstantDecl}
 *
 * Also Check {@link NewArray}, {@link ArrayAccessExpr} & {@link org.processj.rewriters.ArraysRewrite}
 */
public class ArrayType extends Type {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link ArrayType}'s component (base) {@link Type}. Must not be an instance of {@link ArrayType}.</p>
     */
    private final Type  componentType   ;

    /**
     * <p>The specified {@link Integer} value corresponding to the {@link ArrayType}'s depth. Must be greater than
     * 1.</p>
     */
    private final int   depth           ;

    /// ------------
    /// Constructors

    public ArrayType(final Type baseType, final int depth) {
        super(new AST[] { baseType });

        this.componentType  = (baseType instanceof ArrayType) ? ((ArrayType) baseType).getComponentType() : baseType;
        this.depth          = (baseType instanceof ArrayType) ? ((ArrayType) baseType).getDepth() + 1 : depth;

    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ArrayType} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ArrayType} & both
     *         represent the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        // The extra check is done to be sure any named types aren't trying to undermine the check
        return super.equals(that) && (that instanceof ArrayType)
                && this.componentType.equals(((ArrayType) that).componentType);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link RecordTypeDecl}.</p>
     * @return Literal {@link String} representation of the {@link RecordTypeDecl}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.componentType + "[]".repeat(this.depth);

    }

    /// --------------------
    /// org.processj.ast.AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ArrayType}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitArrayType} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) {

        return visitor.visitArrayType(this);

    }

    /// ---------------------
    /// org.processj.ast.Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ArrayType}.</p>
     * @return The internal {@link String} signature representing the {@link ArrayType}.
     * @since 0.1.0
     */
    @Override
    public final String getSignature() {

        // Initialize the StringBuilder
        StringBuilder stringBuilder = new StringBuilder(this.componentType.getSignature());

        // Iterate through the depth & sandwich the current string
        for(int currentDepth = 0; currentDepth < this.depth; currentDepth++)
            stringBuilder = new StringBuilder("[" + stringBuilder + ";");

        // Return the result
        return stringBuilder.toString();

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the component (base) {@link Type} composing the {@link ArrayType}; can never be an instance of
     * {@link ArrayType}.</p>
     * @return The component (base) {@link Type} composing the {@link ArrayType}.
     * @see Type
     * @since 0.1.0
     */
    public final Type getComponentType() {

        return this.componentType;

    }

    /**
     * <p>Returns the {@link Integer} value corresponding with the {@link ArrayType}'s length. Must always be greater
     * than 1.</p>
     * @return The {@link Integer} value corresponding with the {@link ArrayType}'s length.
     * @since 0.1.0
     */
    public final int getDepth() {

        return this.depth;

    }


    // *************************************************************************
    // ** Type Related Methods

    // if α=Array(t1,I1) ∧ β=Array(t2,I2)
    // α =T β ⇔ Array?(α) ∧ Array?(β) ∧ (t1 =T t2) ∧ ((I1 =I2) ∨ (I1 =⊥) ∨ (I2 =⊥))
    @Override
    public boolean typeEqual(final Type type) {

        return this.equals(type);

    }

    @Override
    public boolean typeEquivalent(Type t) {

        return this.equals(t);

    }

    @Override
    public boolean typeAssignmentCompatible(Type t) {

        return this.equals(t);

    }

}