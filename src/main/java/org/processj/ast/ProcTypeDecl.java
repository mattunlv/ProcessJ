package org.processj.ast;

import org.processj.utilities.Visitor;

public class ProcTypeDecl extends Type implements DefineTopLevelDecl {

    /// --------------
    /// Private Fields

    private boolean isDeclaredNative;
    private boolean isDeclaredMobile;

    /**
     * <p>{@link String} value of the {@link ProcTypeDecl}'s name.</p>
     */
    private final String name;
    private final Block body;

    public boolean isNative = false;
    public String library; // Name of the org.processj.library, e.g. math.h
    public String filename; // Name of the file, e.g math
    public String nativeFunction; // Name of the native function, e.g. fabs
    public boolean yields = false;

    /// ------------
    /// Constructors

    public ProcTypeDecl(Sequence<Modifier> modifiers, Type returnType, Name name, Sequence<ParamDecl> formals,
            Sequence<Name> implement, Annotations annotations, Block body) {
        super(name);
        nchildren = 7;
        children = new AST[] { modifiers, returnType, name, formals, implement, annotations, body };
        this.isDeclaredNative = false;
        this.isDeclaredMobile = false;
        this.body = body;
        this.name = (name != null) ? name.toString() : "";
    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ProcTypeDecl} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ProcTypeDecl} & both
     *         represent the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        // TODO: Check Parameters?
        return super.equals(that) && (that instanceof ProcTypeDecl);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link ProcTypeDecl}.</p>
     * @return Literal {@link String} representation of the {@link ProcTypeDecl}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.name;

    }

    /// --------------------
    /// org.processj.ast.AST
    
    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ProcTypeDecl}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitProcTypeDecl(ProcTypeDecl)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) {

        return visitor.visitProcTypeDecl(this);

    }

    /// ---------------------
    /// org.processj.ast.Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ProcTypeDecl}.</p>
     * @return The internal {@link String} signature representing the {@link ProcTypeDecl}.
     * @since 0.1.0
     */
    @Override
    public final String getSignature() {

        // Initialize the StringBuilder
        final StringBuilder stringBuilder = new StringBuilder("(");

        // Iterate through the Parameters & append the corresponding signature
        for(final ParamDecl parameterDeclaration: formalParams())
            stringBuilder.append(parameterDeclaration.type().getSignature());

        // Append the suffix
        stringBuilder.append(")").append(returnType().getSignature());

        // Return the result
        return stringBuilder.toString();

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link ProcTypeDecl} is declared native.</p>
     * @return flag indicating if the {@link ProcTypeDecl} is declared native.
     * @since 0.1.0
     */
    public final boolean isDeclaredNative() {

        // Iterate through the modifiers
        for(final Modifier modifier: this.modifiers()) {

            // Check for break
            if(this.isDeclaredNative) break;

            // Update the result
            this.isDeclaredNative = modifier.isNative();

        }

        // Return the result
        return this.isDeclaredNative;

    }

    /**
     * <p>Returns a flag indicating if the {@link ProcTypeDecl} is declared mobile.</p>
     * @return flag indicating if the {@link ProcTypeDecl} is mobile native.
     * @since 0.1.0
     */
    public final boolean isDeclaredMobile() {

        // Iterate through the modifiers
        for(final Modifier modifier: this.modifiers()) {

            // Check for break
            if(this.isDeclaredMobile) break;

            // Update the result
            this.isDeclaredMobile = modifier.isMobile();

        }

        // Return the result
        return this.isDeclaredMobile;

    }

    /**
     * <p>Returns a flag indicating if the {@link ProcTypeDecl} defines a body.</p>
     * @return flag indicating if the {@link ProcTypeDecl} defines a body.
     * @since 0.1.0
     */
    public final boolean definesBody() {

        // Return the result
        return this.body != null;

    }





    public Sequence<Modifier> modifiers() {
        return (Sequence<Modifier>) children[0];
    }

    public Type returnType() {
        return (Type) children[1];
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

    // *************************************************************************
    // ** Type Related Methods

    // α = procedure(name1, {t1,1, . . . , t1,m1 }, t1) ∧ β = procedure(name2,
    // {t2,1, . . . , t2,m2 }, t2)
    // α =T β ⇔ procedure?(α) ∧ procedure?(β) ∧ (m1 = m2) ∧ (t1 =T t2) ∧ (name1 =
    // name2) ∧ ∧^m1_i=1 (t1,i =T t2,i)
    @Override
    public boolean typeEqual(Type t) {
        // procedure?(β)
        if (!(t instanceof ProcTypeDecl))
            return false;
        ProcTypeDecl other = (ProcTypeDecl) t;
        // (m1 = m2)
        if (formalParams().size() != other.formalParams().size())
            return false;
        // (t1 =T t2)
        if (!returnType().typeEqual(other.returnType()))
            return false;
        // (name1 = name2) ∧
        if (!this.toString().equals(other.toString()))
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
        if (!(t instanceof ProcTypeDecl))
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