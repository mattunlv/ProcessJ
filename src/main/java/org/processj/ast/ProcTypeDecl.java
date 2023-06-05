package org.processj.ast;

import org.processj.Phase;
import org.processj.utilities.Visitor;

public class ProcTypeDecl extends Type implements SymbolMap.Context, DefineTopLevelDecl {

    /// --------------
    /// Private Fields

    private final Sequence<Modifier>    modifiers       ;
    private final Annotations           annotations     ;

    /**
     * <p>{@link String} value of the {@link ProcTypeDecl}'s name.</p>
     */
    private final String                name            ;
    private final Block                 body            ;
    private final Sequence<ParamDecl>   parameters      ;
    private final Sequence<Name>        implement       ;
    private final Sequence<Type>        implementTypes  ;
    private String  packageName                         ;
    private boolean isDeclaredNative                    ;
    private boolean isDeclaredMobile                    ;
    private SymbolMap scope;
    private Type returnType;

    /// ------------
    /// Constructors

    public ProcTypeDecl(final Sequence<Modifier> modifiers, final Type returnType,
                        final Name name, final Sequence<ParamDecl> formalParameters,
                        final Sequence<Name> implement, final Annotations annotations,
                        final Block body) {
        super(name);
        nchildren = 7;
        children = new AST[] { modifiers, returnType, name, formalParameters, implement, annotations, body };
        this.modifiers          = modifiers         ;
        this.annotations        = annotations       ;
        this.name               = (name != null) ? name.toString() : "";
        this.parameters         = formalParameters  ;
        this.implement          = implement         ;
        this.implementTypes     = new Sequence<>()  ;
        this.body               = body              ;
        this.packageName        = ""                ;
        this.isDeclaredNative   = false             ;
        this.isDeclaredMobile   = false             ;
        this.returnType         = returnType        ;

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
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        final SymbolMap scope = this.openScope(visitor.getScope());

        // Visit
        S result = visitor.visitProcTypeDecl(this);

        // Close the scope
        visitor.setScope(scope.getEnclosingScope());

        return result;

    }

    /**
     * <p>Invoked when the specified {@link IVisitor} intends to visit the {@link ProcTypeDecl}.
     * This method will dispatch the {@link IVisitor}'s {@link IVisitor#visitProcTypeDecl(ProcTypeDecl)} method.</p>
     * @param visitor The {@link IVisitor} to dispatch.
     * @throws Phase.Error if the {@link Visitor#visitProcTypeDecl(ProcTypeDecl)} visitation invocation operation
     * failed.
     * @since 0.1.0
     */
    @Override
    public final <T> T visit(final IVisitor<T> visitor) throws Phase.Error, ContextDoesNotDefineScopeException {

        // Open the scope
        final SymbolMap scope = this.openScope(visitor.getScope());

        // Visit
        T result = visitor.visitProcTypeDecl(this);

        // Close the scope
        visitor.setScope(scope.getEnclosingScope());

        return result;

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
        for(final ParamDecl parameterDeclaration: getParameters())
            stringBuilder.append(parameterDeclaration.getType().getSignature());

        // Append the suffix
        stringBuilder.append(")").append(getReturnType().getSignature());

        // Return the result
        return stringBuilder.toString();

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link ProcTypeDecl} is annotated with the specified {@link String}
     * value.</p>
     * @param annotation The {@link String} value corresponding to the annotation to check.
     * @return Flag indicating if the {@link ProcTypeDecl} is annotated with the specified {@link String}
     * value.
     * @see Annotation
     * @since 0.1.0
     */
    public final boolean isAnnotatedWith(final String annotation) {

        return this.annotations.isDefined(annotation)
                && Boolean.parseBoolean(this.annotations.get(annotation));

    }

    /**
     * <p>Returns a flag indicating if the {@link ProcTypeDecl} is declared mobile.</p>
     * @return flag indicating if the {@link ProcTypeDecl} is mobile native.
     * @since 0.1.0
     */
    public final boolean isMobile() {

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
     * <p>Returns a flag indicating if the {@link ProcTypeDecl} is declared native.</p>
     * @return flag indicating if the {@link ProcTypeDecl} is declared native.
     * @since 0.1.0
     */
    public final boolean isNative() {

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
     * <p>Returns a flag indicating if the {@link ProcTypeDecl} defines a body.</p>
     * @return flag indicating if the {@link ProcTypeDecl} defines a body.
     * @since 0.1.0
     */
    public final boolean definesBody() {

        // Return the result
        return this.body != null;

    }

    /**
     * <p>Returns a flag indicating if the {@link ProcTypeDecl} is marked as yielding.</p>
     * @return Flag indicating if the {@link ProcTypeDecl} is marked as yielding.
     * @since 0.1.0
     */
    public final boolean doesYield() {

        return this.isAnnotatedWith("yield");

    }

    /**
     * <p>Returns the {@link String} value of the {@link ProcTypeDecl}'s package name.</p>
     * @return {@link String} value of the {@link ProcTypeDecl}'s package name.
     * @since 0.1.0
     */
    public final String getPackageName() {

        return this.packageName;

    }

    /**
     * <p>Marks the {@link ProcTypeDecl} as 'native' (i.e. defined with the 'native' {@link Modifier} by aggregating
     * a 'native' {@link Modifier} if it doesn't already contain one.</p>
     * @since 0.1.0
     */
    public final void setNative() {

        if(!this.isNative())
            this.modifiers.append(new Modifier(Modifier.NATIVE));

    }

    /**
     * <p>Mutates the {@link ProcTypeDecl}'s package name.</p>
     * @param packageName The {@link String} value of the desired package name.
     * @since 0.1.0
     */
    public final void setPackageName(final String packageName) {

        if(packageName != null)
            this.packageName = packageName;

    }

    /**
     * <p>Marks the {@link ProcTypeDecl} as 'yielding' (i.e. annotated with the 'yield' {@link Annotation} by
     * aggregating a 'yield' {@link Annotation} if it doesn't already contain one.</p>
     * @since 0.1.0
     */
    @Override
    public final void setYields() {

        if(!this.doesYield())
            this.getAnnotations().add("yield", "true");

    }

    public final int getParameterCount() {

        return this.parameters.size();

    }

    public final Type getTypeForParameter(final int index) {

        // Return the result if we can access it
        return ((this.parameters != null) && (index < this.parameters.size()))
                ? this.parameters.child(index).getType() : null;

    }

    public final void setTypeForEachImplement(final TypeReturnCallback typeReturnCallback) {

        if((this.implementTypes != null) && (typeReturnCallback != null)) {

            // Clear the implement types
            this.implementTypes.clear();

            // Append the results
            this.implement.forEach(name -> {

                Type type;

                try {

                    type = typeReturnCallback.Invoke(name);

                } catch (final Phase.Error phaseError) {

                    type = new ErrorType();

                }

                this.implementTypes.append(type);

            });
        }

    }

    public final void setReturnType(final Type returnType) {

        this.returnType     = returnType;
        this.children[1]    = returnType;

    }

    public Sequence<Modifier> modifiers() {
        return (Sequence<Modifier>) children[0];
    }

    public Type getReturnType() {
        return (Type) children[1];
    }

    public Sequence<ParamDecl> getParameters() {
        return (Sequence<ParamDecl>) children[3];
    }

    public Sequence<Name> implement() {
        return (Sequence<Name>) children[4];
    }

    public final Annotations getAnnotations() {

        return this.annotations;

    }

    public Block getBody() {
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
        if (getParameters().size() != other.getParameters().size())
            return false;
        // (t1 =T t2)
        if (!getReturnType().typeEqual(other.getReturnType()))
            return false;
        // (name1 = name2) ∧
        if (!this.toString().equals(other.toString()))
            return false;
        // ∧^m1_i=1 (t1,i =T t2,i)
        boolean eq = true;
        for (int i = 0; i < getParameters().size(); i++) {
            eq = eq && getParameters().child(i).getType().typeEqual(other.getParameters().child(i).getType());
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
        if (getParameters().size() != other.getParameters().size())
            return false;
        // (t2 :=T t1)
        if (!other.getReturnType().typeAssignmentCompatible(this))
            return false;
        // ∧^m1_i=1 (t1,i =T t2,i)
        boolean eq = true;
        for (int i = 0; i < getParameters().size(); i++) {
            eq = eq && getParameters().child(i).getType().typeAssignmentCompatible(other.getParameters().child(i).getType());
        }
        return eq;
    }

    @FunctionalInterface
    public interface TypeReturnCallback {

        Type Invoke(final Name name) throws Phase.Error;

    }

}