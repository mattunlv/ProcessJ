package org.processj.compiler.ast.type;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.modifier.Modifier;
import org.processj.compiler.ast.modifier.Modifiers;
import org.processj.compiler.ast.modifier.Native;
import org.processj.compiler.ast.modifier.Mobile;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.statement.declarative.DeclarativeContext;
import org.processj.compiler.ast.statement.declarative.Names;
import org.processj.compiler.ast.statement.declarative.ParameterDeclaration;
import org.processj.compiler.ast.statement.declarative.Parameters;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a declaration of a {@link Context}'s procedure.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see Context
 * @see DeclarativeContext
 * @see BlockStatement
 */
public class ProcedureType extends BlockStatement implements Type, DeclarativeContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link ProcedureType}'s specified {@link Annotations}.</p>
     * @since 1.0.0
     * @see Annotations
     */
    private final Annotations annotations;

    /**
     * <p>The {@link ProcedureType}'s specified {@link Modifiers}.</p>
     * @since 1.0.0
     * @see Modifiers
     */
    private final Modifiers modifiers;

    /**
     * <p>The {@link ProcedureType}'s {@link Name}.</p>
     * @since 1.0.0
     * @see Name
     */
    private final Name name;

    /**
     * <p>The {@link ProcedureType}'s set of {@link ParameterDeclaration}s.</p>
     * @since 1.0.0
     * @see ParameterDeclaration
     */
    private final Parameters parameters;

    /**
     * <p>The {@link ProcedureType}'s set of implement {@link Names}.</p>
     * @since 1.0.0
     * @see Names
     */
    private final Names implement;

    /**
     * <p>The {@link ProcedureType}'s return {@link Type}.</p>
     * @since 1.0.0
     * @see Type
     */
    private Type returnType;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ProcedureType} with the specified {@link Annotations}, {@link Modifiers},
     * return {@link Type}, {@link Name}, {@link Parameters}, implement {@link Names}, & {@link Statement} body.</p>
     * @param annotations The {@link Annotations} specified with the {@link ProcedureType}.
     * @param modifiers The {@link Modifiers} specifying access privileges.
     * @param returnType The {@link ProcedureType}'s return {@link Type}.
     * @param name The {@link Name} corresponding to the {@link ProcedureType}.
     * @param parameters The {@link Parameters} specified with the {@link ProcedureType}.
     * @param implement The set of implement {@link Names}.
     * @param body The {@link ProcedureType}'s {@link Statement} body.
     * @since 1.0.0
     * @see Annotations
     * @see Modifiers
     * @see Name
     * @see Names
     * @see Parameters
     * @see Statement
     * @see Type
     */
    public ProcedureType(final Annotations annotations,
                         final Modifiers modifiers,
                         final Type returnType,
                         final Name name,
                         final Parameters parameters,
                         final Names implement,
                         final Statement body) {
        super(body);

        this.annotations            = (annotations  != null) ? annotations  : new Annotations() ;
        this.modifiers              = (modifiers    != null) ? modifiers    : new Modifiers()   ;
        this.name                   = (name         != null) ? name         : new Name("")      ;
        this.parameters             = (parameters   != null) ? parameters   : new Parameters()  ;
        this.implement              = (implement    != null) ? implement    : new Names()       ;
        this.returnType             = returnType                                                ;

    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} value of the {@link ProcedureType}'s {@link Name}.</p>
     * @return {@link String} value of the {@link ProcedureType}'s {@link Name}.
     * @since 1.0.0
     * @see Name
     * @see String
     */
    @Override
    public final String toString() {

        return this.name.toString();

    }

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ProcedureType}
     * & both represent the same {@link Type} via {@link Name}, return {@link Type}, & {@link Parameters}.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ProcedureType} & both
     *         represent the same {@link Type} via {@link Name}, return {@link Type}, & {@link Parameters}s.
     * @since 1.0.0
     * @see Name
     * @see Object
     * @see Parameters
     * @see Type
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof ProcedureType)
                && this.name.equals(((ProcedureType) that).getName())
                && this.returnType.equals(((ProcedureType) that).getReturnType())
                && this.parameters.equals(((ProcedureType) that).getParameters());

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context} & scope, and dispatches the
     * {@link Visitor} to the {@link Annotations} via {@link Annotations#accept(Visitor)}, the {@link Modifiers} via
     * {@link Modifiers#accept(Visitor)}, the return {@link Type} via {@link Type#accept(Visitor)}, the
     * {@link Parameters} via {@link Parameters#accept(Visitor)}, the set of implement {@link Names} via
     * {@link Names#accept(Visitor)}, the {@link ProcedureType} via
     * {@link Visitor#visitProcedureTypeDeclaration(ProcedureType)}, & {@link Statement} children
     * via {@link Statement#accept(Visitor)} before updating the {@link org.processj.compiler.ast.Context} to the
     * enclosing {@link Context}.
     * @since 1.0.0
     * @see Annotations
     * @see Modifiers
     * @see Name
     * @see Names
     * @see Parameters
     * @see Statement
     * @see Type
     * @see Visitor
     * @see Phase.Error
     */
    @Override
    public void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the Block Statement
        this.openScope();

        // Dispatch the annotations
        this.annotations.accept(visitor);

        // Dispatch the Modifiers
        this.modifiers.accept(visitor);

        // Dispatch the Return Type
        this.returnType.accept(visitor);

        // Dispatch the Name
        this.name.accept(visitor);

        // Dispatch the Implement Names
        this.implement.accept(visitor);

        // Dispatch the Parameters
        this.parameters.accept(visitor);

        // Dispatch the ProcedureTypeDeclaration
        visitor.visitProcedureTypeDeclaration(this);

        // Dispatch the children
        this.getBody().accept(visitor);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// ---------------
    /// YieldingContext

    /**
     * <p>Marks the {@link ProcedureType} as 'yielding' (i.e. annotated with the 'yield' {@link Annotation}
     * by aggregating a 'yield' {@link Annotation} if it doesn't already contain one.</p>
     * @since 1.0.0
     * @see Annotation
     */
    @Override
    public final boolean setYields() {

        if(!this.doesYield())
            this.getAnnotations().add("yield", "true");

        return true;

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link ProcedureType} is specified with the {@link Mobile}
     * {@link Modifier}.</p>
     * @return A flag indicating if the {@link ProcedureType} is specified with the {@link Mobile}
     *         {@link Modifier}.
     * @since 1.0.0
     * @see Mobile
     * @see Modifier
     */
    public final boolean isMobile() {

        return this.modifiers.isMobile();

    }

    /**
     * <p>Returns a flag indicating if the {@link ProcedureType} is specified with the {@link Native}
     * {@link Modifier}.</p>
     * @return A flag indicating if the {@link ProcedureType} is specified with the {@link Native}
     *         {@link Modifier}.
     * @since 1.0.0
     * @see Native
     * @see Modifier
     */
    public final boolean isNative() {

        return this.modifiers.isNative();

    }

    /**
     * <p>Returns a flag indicating if the {@link ProcedureType} is marked as yielding.</p>
     * @return Flag indicating if the {@link ProcedureType} is marked as yielding.
     * @since 1.0.0
     */
    public final boolean doesYield() {

        return this.annotations.isDefined("yield")
                && Boolean.parseBoolean(this.annotations.get("yield"));

    }

    /**
     * <p>Returns the amount of {@link ParameterDeclaration}s specified with the {@link ProcedureType}.</p>
     * @return The amount of {@link ParameterDeclaration}s specified with the {@link ProcedureType}.
     * @since 1.0.0
     * @see ParameterDeclaration
     */
    public final int getParameterCount() {

        return this.parameters.size();

    }

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ProcedureType}.</p>
     * @return The internal {@link String} signature representing the {@link ProcedureType}.
     * @since 1.0.0
     * @see String
     */
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

    @Override
    public Type addDimension() {
        return null;
    }

    @Override
    public Type clearDepth() {
        return null;
    }

    @Override
    public int getDepth() {
        return 0;
    }

    /**
     * <p>Returns the {@link ProcedureType}'s specified set of {@link Annotations}.</p>
     * @return The {@link ProcedureType}'s specified set of {@link Annotations}.
     * @since 1.0.0
     * @see String
     * @see Annotations
     */
    public final Annotations getAnnotations() {

        return this.annotations;

    }

    /**
     * <p>Returns the {@link ProcedureType}'s specified set of {@link Modifiers}.</p>
     * @return The {@link ProcedureType}'s specified set of {@link Modifiers}.
     * @since 1.0.0
     * @see String
     * @see Modifiers
     */
    public final Modifiers getModifiers() {

        return this.modifiers;

    }

    /**
     * <p>Returns the {@link ProcedureType}'s specified return {@link Type}.</p>
     * @return The {@link ProcedureType}'s specified return {@link Type}.
     * @since 1.0.0
     * @see Type
     */
    public final Type getReturnType() {

        return this.returnType;

    }

    /**
     * <p>Returns the {@link ProcedureType}'s specified {@link Name}.</p>
     * @return The {@link ProcedureType}'s specified {@link Name}.
     * @since 1.0.0
     * @see Name
     */
    public final Name getName() {

        return this.name;

    }

    /**
     * <p>Returns the {@link ProcedureType}'s specified set of {@link Parameters}.</p>
     * @return The {@link ProcedureType}'s specified set of {@link Parameters}.
     * @since 1.0.0
     * @see String
     * @see Parameters
     */
    public final Parameters getParameters() {

        return this.parameters;

    }

    /**
     * <p>Returns the {@link ProcedureType}'s specified set of implement {@link Names}.</p>
     * @return The {@link ProcedureType}'s specified set of implement {@link Names}.
     * @since 1.0.0
     * @see String
     * @see Names
     */
    public final Names getImplements() {

        return this.implement;

    }

    /**
     * <p>Returns the {@link Type} corresponding to the {@link ParameterDeclaration} at the specified index.</p>
     * @param index The integer index corresponding to the {@link ParameterDeclaration} whose {@link Type} to retrieve.
     * @return The {@link Type} corresponding to the {@link ParameterDeclaration} at the specified index.
     * @since 1.0.0
     * @see Type
     * @see ParameterDeclaration
     */
    public final Type getTypeForParameterAt(final int index) {

        return this.parameters.getParameterAt(index).getType();

    }

    /**
     * <p>Marks the {@link ProcedureType} as 'native' (i.e. defined with {@link Native} by aggregating
     * a {@link Native} {@link Modifier} to the {@link ProcedureType}'s {@link Modifiers} if it doesn't
     * already contain one.</p>
     * @since 1.0.0
     * @see Native
     * @see Modifiers
     */
    public final void setNative() {

        this.modifiers.add(new Native());

    }

    /**
     * <p>Mutates the {@link ProcedureType}'s return {@link Type} with the specified {@link Type}.</p>
     * @param returnType The desired {@link Type} to mutate the {@link ProcedureType} with.
     * @since 1.0.0
     * @see Type
     */
    public final void setReturnType(final Type returnType) {

        this.returnType = returnType;

    }

    /**
     * <p>Iterates through each {@link Name} in the {@link ProcedureType}'s set of implement {@link Names},
     * calls back the {@link CandidatesReturnCallback} to retrieve a candidate {@link SymbolMap}, & maps it to the
     * corresponding {@link Name}.</p>
     * @param candidatesReturnCallback The {@link CandidatesReturnCallback} to invoke in order to retrieve the
     *        {@link SymbolMap}
     * @throws Phase.Error if the {@link CandidatesReturnCallback} failed.
     * @since 1.0.0
     * @see CandidatesReturnCallback
     * @see Name
     * @see Names
     * @see SymbolMap
     * @see Phase.Error
     */
    public final void setCandidateForEachImplement(final CandidatesReturnCallback candidatesReturnCallback)
            throws Phase.Error {

        if(candidatesReturnCallback != null) {

            int index = 0;

            // Iterate through each Implement Name
            for(final Name name: this.implement) {

                // Initialize a handle to the Type
                final SymbolMap candidates = candidatesReturnCallback.Invoke(name);

                // Update the mapping
                this.implement.insert(index, name, candidates);

                // Increment the index
                index++;

            }

        }

    }

    /**
     * <p>Iterates through each {@link ParameterDeclaration} in the {@link ProcedureType}'s set of
     * {@link Parameters}, calls back the {@link TypeReturnCallback} to retrieve a {@link Type}, & maps it to the
     * corresponding {@link ParameterDeclaration}.</p>
     * @param typeReturnCallback The {@link TypeReturnCallback} to invoke in order to retrieve the
     *        {@link SymbolMap}
     * @throws Phase.Error if the {@link TypeReturnCallback} failed.
     * @since 1.0.0
     * @see ParameterDeclaration
     * @see Parameters
     * @see Phase.Error
     * @see Type
     * @see TypeReturnCallback
     */
    public final void setTypeForEachParameter(final TypeReturnCallback typeReturnCallback)
            throws Phase.Error {

        if(typeReturnCallback != null) {

            int index = 0;

            // Iterate through each Implement Name
            for(final ParameterDeclaration parameterDeclaration: this.parameters) {

                // Initialize a handle to the Type
                final Type type = typeReturnCallback.Invoke(parameterDeclaration);

                // Assert the Parameter Declaration hase a Type bound
                parameterDeclaration.setType(type);

                // Update the mapping
                this.parameters.insert(index, parameterDeclaration);

                // Increment
                index++;

            }

        }

    }

    // α = procedure(name1, {t1,1, . . . , t1,m1 }, t1) ∧ β = procedure(name2,
    // {t2,1, . . . , t2,m2 }, t2)
    // α =T β ⇔ procedure?(α) ∧ procedure?(β) ∧ (m1 = m2) ∧ (t1 =T t2) ∧ (name1 =
    // name2) ∧ ∧^m1_i=1 (t1,i =T t2,i)

    public boolean typeEqual(Type t) {
        // procedure?(β)
        if (!(t instanceof ProcedureType))
            return false;
        ProcedureType other = (ProcedureType) t;
        // (m1 = m2)
        if (getParameters().size() != other.getParameters().size())
            return false;
        // (t1 =T t2)
        if (!getReturnType().isTypeEqualTo(other.getReturnType()))
            return false;
        // (name1 = name2) ∧
        if (!this.toString().equals(other.toString()))
            return false;
        // ∧^m1_i=1 (t1,i =T t2,i)
        boolean eq = true;
        for (int i = 0; i < getParameters().size(); i++) {
            eq = eq && getParameters().getParameterAt(i).getType().isTypeEqualTo(other.getParameters().getParameterAt(i).getType());
        }
        return eq;
    }

    // α ∼T β ⇔ α =T β

    public boolean typeEquivalent(Type t) {
        return this.isTypeEquivalentTo(t);
    }

    // α = procedure(name1, {t1,1, . . . , t1,m1 }, t1) ∧ β = procedure(name2,
    // {t2,1, . . . , t2,m2 }, t2)
    // α "=T β ⇔ procedure?(α) ∧ procedure?(β) ∧ (m1 = m2) ∧ (t2 :=T t1) ∧ ∧^m1_i=1
    // (t1,i :=T t2,i)

    public boolean typeAssignmentCompatible(Type t) {
        // procedure?(β)
        if (!(t instanceof ProcedureType))
            return false;
        ProcedureType other = (ProcedureType) t;
        // (m1 = m2)
        if (getParameters().size() != other.getParameters().size())
            return false;
        // (t2 :=T t1)
        if (!other.getReturnType().isAssignmentCompatibleTo(this))
            return false;
        // ∧^m1_i=1 (t1,i =T t2,i)
        boolean eq = true;
        for (int i = 0; i < getParameters().size(); i++) {
            eq = eq && getParameters().getParameterAt(i).getType().isAssignmentCompatibleTo(other.getParameters().getParameterAt(i).getType());
        }
        return eq;
    }

    @Override
    public boolean isTypeEqualTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeEquivalentTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeLessThan(Object that) {
        return false;
    }

    @Override
    public boolean isTypeGreaterThan(Object that) {
        return false;
    }

    @Override
    public boolean isTypeLessThanOrEqualTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeGreaterThanOrEqualTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeCeilingOf(Object that) {
        return false;
    }

    @Override
    public boolean isSubTypeOf(Object that) {
        return false;
    }

    @Override
    public boolean isAssignmentCompatibleTo(Object that) {
        return false;
    }

    /// ---------------------
    /// Functional Interfaces

    /**
     * <p>Functional Interface that provides a means of retrieving a {@link SymbolMap} corresponding to the specified
     * {@link Name}.</p>
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 1.0.0
     * @see Name
     * @see SymbolMap
     * @see Phase.Error
     */
    @FunctionalInterface
    public interface CandidatesReturnCallback {

        SymbolMap Invoke(final Name name) throws Phase.Error;

    }

    /**
     * <p>Functional Interface that provides a means of retrieving a {@link Type} corresponding to the specified
     * {@link ParameterDeclaration}.</p>
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 1.0.0
     * @see ParameterDeclaration
     * @see Type
     * @see Phase.Error
     */
    @FunctionalInterface
    public interface TypeReturnCallback {

        Type Invoke(final ParameterDeclaration parameterDeclaration) throws Phase.Error;

    }

}