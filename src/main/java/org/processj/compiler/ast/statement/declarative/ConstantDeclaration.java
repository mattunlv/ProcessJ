package org.processj.compiler.ast.statement.declarative;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.modifier.Modifier;
import org.processj.compiler.ast.modifier.Native;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.modifier.Modifiers;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a declaration of a {@link Context}'s constant variable.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see Context
 * @see InitializedContext
 * @see Statement
 */
public class ConstantDeclaration extends Statement implements Type, InitializedContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The set of {@link Modifiers} corresponding to the {@link ConstantDeclaration}.</p>
     * @since 1.0.0
     * @see Modifiers
     */
    private final Modifiers modifiers;

    /**
     * <p>The {@link Name} corresponding to the {@link ConstantDeclaration}.</p>
     * @since 1.0.0
     * @see Name
     */
    private Name name;

    /**
     * <p>The {@link Type} bound to the {@link ConstantDeclaration}.</p>
     * @since 1.0.0
     * @see Type
     */
    private Type type;

    /**
     * <p>The {@link Expression} whose evaluation defaults the {@link ConstantDeclaration}'s value.</p>
     * @since 1.0.0
     * @see Expression
     */
    private Expression initializationExpression;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ConstantDeclaration} to its' default state with the specified {@link Type}, {@link Name},
     * {@link Expression}, & flag indicating if the {@link ConstantDeclaration} was specified with the {@link Native}
     * {@link Modifier}.</p>
     * @param modifiers The {@link Modifiers} specified with the {@link ConstantDeclaration}.
     * @param type The {@link Type} bound to the {@link ConstantDeclaration}.
     * @param name The {@link Name} bound to the {@link ConstantDeclaration}.
     * @param initializationExpression The {@link Expression} that defaults the {@link ConstantDeclaration}'s value.
     */
    public ConstantDeclaration(final Modifiers modifiers,
                               final Type type,
                               final Name name,
                               final Expression initializationExpression) {
        super(name, initializationExpression);

        this.modifiers                  = modifiers                 ;
        this.type                       = type                      ;
        this.name                       = name                      ;
        this.initializationExpression   = initializationExpression  ;

    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} value of the {@link ConstantDeclaration}'s {@link Name}.</p>
     * @return {@link String} value of the {@link ConstantDeclaration}'s {@link Name}.
     * @since 1.0.0
     * @see Name
     * @see String
     */
    @Override
    public String toString() {

        return this.name.toString();

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link Context}, and dispatches the {@link Visitor} to the bound {@link Type}
     * via {@link Type#accept(Visitor)}, {@link Name} via {@link Name#accept(Visitor)}, initialization
     * {@link Expression} via {@link Expression#accept(Visitor)}, & {@link ConstantDeclaration} via
     * {@link Visitor#visitConstantDeclaration(ConstantDeclaration)} before updating the {@link Context} to the enclosing
     * {@link Context}.
     * @since 1.0.0
     * @see Visitor
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Dispatch the Type
        this.type.accept(visitor);

        // Dispatch the Name
        this.name.accept(visitor);

        // Dispatch the initialization expression, if any
        if(this.initializationExpression != null) this.initializationExpression.accept(visitor);

        // Dispatch the Sync Statement
        visitor.visitConstantDeclaration(this);

        // Close the Context
        visitor.setContext(this.closeContext());

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

    /// ------------------
    /// InitializedContext

    /**
     * <p>Returns a flag indicating if the {@link ConstantDeclaration} is initialized.</p>
     * @return Flag indicating if the {@link ConstantDeclaration} is initialized.
     * @since 1.0.0
     */
    @Override
    public final boolean isInitialized() {

        return this.initializationExpression != null;

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link ConstantDeclaration} was specified with the {@link Native}
     * {@link Modifier}.</p>
     * @return A flag indicating if the {@link ConstantDeclaration} was specified with the {@link Native}
     *         {@link Modifier}.
     * @since 1.0.0
     * @see Native
     * @see Modifier
     */
    public final boolean isNative() {

        return this.modifiers.isNative();

    }

    /**
     * <p>Returns the {@link Modifiers} bound to the {@link ConstantDeclaration}.</p>
     * @return The {@link Modifiers} bound to the {@link ConstantDeclaration}.
     * @since 1.0.0
     * @see Modifiers
     */
    public final Modifiers getModifiers() {

        return this.modifiers;

    }

    /**
     * <p>Returns the {@link ConstantDeclaration}'s bound {@link Type}.</p>
     * @return The {@link ConstantDeclaration}'s bound {@link Type}.
     * @since 1.0.0
     * @see Type
     */
    public final Type getType() {

        return this.type;

    }

    /**
     * <p>Returns the {@link ConstantDeclaration}'s bound {@link Name}.</p>
     * @return The {@link ConstantDeclaration}'s bound {@link Name}.
     * @since 1.0.0
     * @see Name
     */
    public final Name getName() {

        return this.name;

    }

    @Override
    public String getSignature() {
        return null;
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
     * <p>Returns the {@link ConstantDeclaration}'s initialization {@link Expression}.</p>
     * @return The {@link ConstantDeclaration}'s initialization {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final Expression getInitializationExpression() {

        return this.initializationExpression;

    }

    /**
     * <p>Mutates the {@link ConstantDeclaration}'s initialization {@link Expression}.</p>
     * @param initializationExpression The desired initialization {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final void setInitializationExpression(final Expression initializationExpression) {

        this.initializationExpression = initializationExpression;

    }

    /**
     * <p>Mutates the {@link ConstantDeclaration}'s bound {@link Name}.</p>
     * @param name The desired {@link Name} to bind to the {@link ConstantDeclaration}.
     * @since 1.0.0
     * @see Name
     */
    public final void setName(final Name name) {

        this.name = name;

    }

    /**
     * <p>Mutates the {@link ConstantDeclaration}'s bound {@link Type}.</p>
     * @param type The desired {@link Type} to bind to the {@link ConstantDeclaration}.
     * @since 1.0.0
     * @see Name
     */
    public final void setType(final Type type) {

        this.type = type;

    }

}