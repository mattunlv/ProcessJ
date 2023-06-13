package org.processj.compiler.ast.type;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

import java.util.HashMap;
import java.util.Map;

public class ProcedureTypeDeclaration extends Type implements SymbolMap.Context {

    /// --------------
    /// Private Fields

    private final Annotations annotations             ;
    private final Sequence<Modifier> modifiers               ;

    /**
     * <p>{@link String} value of the {@link ProcedureTypeDeclaration}'s name.</p>
     */
    private final Name                      name                    ;
    private final BlockStatement body                    ;
    private final Sequence<ParameterDeclaration>       parameters              ;
    private final Sequence<Name>            implement               ;
    private final Map<String, SymbolMap>    implementCandidates     ;
    private final Map<String, Type>         parameterTypes          ;
    private String                          packageName             ;
    private boolean                         isDeclaredNative        ;
    private boolean                         isDeclaredMobile        ;
    private SymbolMap                       scope                   ;
    private Type                            returnType              ;
    private BlockStatement mergeBody               ;

    /// ------------
    /// Constructors

    public ProcedureTypeDeclaration(final Sequence<Modifier> modifiers,
                                    final Type returnType,
                                    final Name name,
                                    final Sequence<ParameterDeclaration> parameter,
                                    final Sequence<Name> implement,
                                    final Annotations annotations,
                                    final BlockStatement body) {
        super(new AST[] { modifiers, returnType, name, parameter, implement, annotations,
                (body != null) ? body : new BlockStatement(new Sequence<>()) });

        this.modifiers              = modifiers                             ;
        this.annotations            = annotations                           ;
        this.name                   = (name != null) ? name : new Name("")  ;
        this.parameters             = parameter                             ;
        this.implement              = implement                             ;
        this.body                   = body                                  ;
        this.packageName            = ""                                    ;
        this.isDeclaredNative       = false                                 ;
        this.isDeclaredMobile       = false                                 ;
        this.returnType             = returnType                            ;
        this.implementCandidates    = new HashMap<>()                       ;
        this.parameterTypes         = new HashMap<>()                       ;
        this.mergeBody              = new BlockStatement()                           ;

    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ProcedureTypeDeclaration} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ProcedureTypeDeclaration} & both
     *         represent the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        // TODO: Check Parameters?
        return super.equals(that) && (that instanceof ProcedureTypeDeclaration);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link ProcedureTypeDeclaration}.</p>
     * @return Literal {@link String} representation of the {@link ProcedureTypeDeclaration}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.name.toString();

    }

    /// --------------------
    /// org.processj.ast.AST
    
    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ProcedureTypeDeclaration}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitProcedureTypeDeclaration(ProcedureTypeDeclaration)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        visitor.setScope(this.openScope(visitor.getScope()));

        // Visit
        S result = visitor.visitProcedureTypeDeclaration(this);

        // Close the scope
        visitor.setScope(visitor.getScope().getEnclosingScope());

        return result;

    }

    /// ---------------------
    /// org.processj.ast.Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ProcedureTypeDeclaration}.</p>
     * @return The internal {@link String} signature representing the {@link ProcedureTypeDeclaration}.
     * @since 0.1.0
     */
    @Override
    public final String getSignature() {

        // Initialize the StringBuilder
        final StringBuilder stringBuilder = new StringBuilder("(");

        // Iterate through the Parameters & append the corresponding signature
        for(final ParameterDeclaration parameterDeclaration: getParameters())
            stringBuilder.append(parameterDeclaration.getType().getSignature());

        // Append the suffix
        stringBuilder.append(")").append(getReturnType().getSignature());

        // Return the result
        return stringBuilder.toString();

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link ProcedureTypeDeclaration} is annotated with the specified {@link String}
     * value.</p>
     * @param annotation The {@link String} value corresponding to the annotation to check.
     * @return Flag indicating if the {@link ProcedureTypeDeclaration} is annotated with the specified {@link String}
     * value.
     * @see Annotation
     * @since 0.1.0
     */
    public final boolean isAnnotatedWith(final String annotation) {

        return this.annotations.isDefined(annotation)
                && Boolean.parseBoolean(this.annotations.get(annotation));

    }

    /**
     * <p>Returns a flag indicating if the {@link ProcedureTypeDeclaration} is declared mobile.</p>
     * @return flag indicating if the {@link ProcedureTypeDeclaration} is mobile native.
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
     * <p>Returns a flag indicating if the {@link ProcedureTypeDeclaration} is declared native.</p>
     * @return flag indicating if the {@link ProcedureTypeDeclaration} is declared native.
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
     * <p>Returns a flag indicating if the {@link ProcedureTypeDeclaration} defines a body.</p>
     * @return flag indicating if the {@link ProcedureTypeDeclaration} defines a body.
     * @since 0.1.0
     */
    public final boolean definesBody() {

        // Return the result
        return !this.body.getStatements().isEmpty();

    }

    /**
     * <p>Returns a flag indicating if the {@link ProcedureTypeDeclaration} is marked as yielding.</p>
     * @return Flag indicating if the {@link ProcedureTypeDeclaration} is marked as yielding.
     * @since 0.1.0
     */
    public final boolean doesYield() {

        return this.isAnnotatedWith("yield");

    }

    /**
     * <p>Returns the {@link String} value of the {@link ProcedureTypeDeclaration}'s package name.</p>
     * @return {@link String} value of the {@link ProcedureTypeDeclaration}'s package name.
     * @since 0.1.0
     */
    public final String getPackageName() {

        return this.packageName;

    }

    @Override
    public BlockStatement getMergeBody() {
        return this.body.getMergeBody();
    }

    @Override
    public BlockStatement getClearedMergeBody() {

        return this.body.getClearedMergeBody();

    }


    public final BlockStatement getBody() {

        return this.body;

    }

    public final int getParameterCount() {

        return this.parameters.size();

    }

    public final Type getTypeForParameter(final int index) {

        return this.parameters.child(index).getType();

    }

    public final Type getTypeForParameter(final Name name) {

        return this.parameterTypes.get(name.toString());

    }

    /**
     * <p>Marks the {@link ProcedureTypeDeclaration} as 'yielding' (i.e. annotated with the 'yield' {@link Annotation} by
     * aggregating a 'yield' {@link Annotation} if it doesn't already contain one.</p>
     * @since 0.1.0
     */
    @Override
    public final boolean setYields() {

        if(!this.doesYield())
            this.getAnnotations().add("yield", "true");

        return true;

    }

    @Override
    public boolean definesLabel() {
        return false;
    }

    @Override
    public boolean definesEndLabel() {
        return false;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public void setEndLabel(String label) {

    }

    @Override
    public String getEndLabel() {
        return null;
    }

    /**
     * <p>Marks the {@link ProcedureTypeDeclaration} as 'native' (i.e. defined with the 'native' {@link Modifier} by aggregating
     * a 'native' {@link Modifier} if it doesn't already contain one.</p>
     * @since 0.1.0
     */
    public final void setNative() {

        if(!this.isNative())
            this.modifiers.append(new Modifier(Modifier.NATIVE));

    }

    /**
     * <p>Mutates the {@link ProcedureTypeDeclaration}'s package name.</p>
     * @param packageName The {@link String} value of the desired package name.
     * @since 0.1.0
     */
    public final void setPackageName(final String packageName) {

        if(packageName != null)
            this.packageName = packageName;

    }

    public final void setReturnType(final Type returnType) {

        this.returnType     = returnType;
        this.children[1]    = returnType;

    }

    public final void setCandidateForEachImplement(final CandidatesReturnCallback candidatesReturnCallback) throws Phase.Error {

        if((this.implementCandidates != null) && (candidatesReturnCallback != null)) {


            // Iterate through each Implement Name
            for(final Name name: this.implement) {

                // Initialize a handle to the Type
                final SymbolMap candidates = candidatesReturnCallback.Invoke(name);

                // Place the mapping
                this.implementCandidates.put(name.toString(), candidates);

            }

        }

    }

    public final void setTypeForEachParameter(final TypeReturnCallback typeReturnCallback) throws Phase.Error {

        if((this.parameterTypes != null) && (typeReturnCallback != null)) {

            // Iterate through each Implement Name
            for(final ParameterDeclaration parameterDeclaration: this.parameters) {

                // Initialize a handle to the Type
                final Type type = typeReturnCallback.Invoke(parameterDeclaration);

                // Place the mapping
                this.parameterTypes.put(parameterDeclaration.toString(), type);

                // Assert the Parameter Declaration hase a Type bound
                parameterDeclaration.setType(type);

            }

        }

    }

    public final void forEachModifier(final ModifierCallback modifierCallback) throws Phase.Error {

        if((this.modifiers != null) && (modifierCallback != null)) {

            // Iterate through each Modifier & Invoke the ModifierCallback
            for(final Modifier modifier: this.modifiers)
                modifierCallback.Invoke(modifier);

        }

    }

    public final void forEachStatement(final StatementCallback statementCallback) throws Phase.Error {

        if((this.body.getStatements() != null) && (statementCallback != null)) {

            // Iterate through each Statement & Invoke the StatementCallback
            for(final Statement statement: this.body.getStatements())
                statementCallback.Invoke(statement);

        }

    }

    public Sequence<Modifier> modifiers() {
        return (Sequence<Modifier>) children[0];
    }

    public Type getReturnType() {
        return (Type) children[1];
    }

    public Sequence<ParameterDeclaration> getParameters() {
        return (Sequence<ParameterDeclaration>) children[3];
    }

    public Sequence<Name> getImplements() {
        return (Sequence<Name>) children[4];
    }

    public final Annotations getAnnotations() {

        return this.annotations;

    }

    // α = procedure(name1, {t1,1, . . . , t1,m1 }, t1) ∧ β = procedure(name2,
    // {t2,1, . . . , t2,m2 }, t2)
    // α =T β ⇔ procedure?(α) ∧ procedure?(β) ∧ (m1 = m2) ∧ (t1 =T t2) ∧ (name1 =
    // name2) ∧ ∧^m1_i=1 (t1,i =T t2,i)
    @Override
    public boolean typeEqual(Type t) {
        // procedure?(β)
        if (!(t instanceof ProcedureTypeDeclaration))
            return false;
        ProcedureTypeDeclaration other = (ProcedureTypeDeclaration) t;
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
        if (!(t instanceof ProcedureTypeDeclaration))
            return false;
        ProcedureTypeDeclaration other = (ProcedureTypeDeclaration) t;
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
    public interface AnnotationCallback {

        void Invoke(final Annotation annotation) throws Phase.Error;

    }

    @FunctionalInterface
    public interface ModifierCallback {

        void Invoke(final Modifier modifier) throws Phase.Error;

    }

    @FunctionalInterface
    public interface StatementCallback {

        void Invoke(final Statement statement) throws Phase.Error;

    }

    @FunctionalInterface
    public interface CandidatesReturnCallback {

        SymbolMap Invoke(final Name name) throws Phase.Error;

    }

    @FunctionalInterface
    public interface TypeReturnCallback {

        Type Invoke(final ParameterDeclaration parameterDeclaration) throws Phase.Error;

    }

}