package org.processj.compiler.ast.statement.declarative;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.modifier.Modifier;
import org.processj.compiler.ast.modifier.Constant;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.modifier.Modifiers;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a declaration of a {@link Context}'s local variable.</p>
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
public class VariableDeclaration extends Statement implements InitializedContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The set of {@link Modifiers} corresponding to the {@link VariableDeclaration}.</p>
     * @since 1.0.0
     * @see Modifiers
     */
    private final Modifiers modifiers ;

    /**
     * <p>The {@link Name} corresponding to the {@link VariableDeclaration}.</p>
     * @since 1.0.0
     * @see Name
     */
    private Name name ;

    /**
     * <p>The {@link Type} bound to the {@link VariableDeclaration}.</p>
     * @since 1.0.0
     * @see Type
     */
    private Type type                        ;

    /**
     * <p>The {@link Expression} whose evaluation defaults the {@link VariableDeclaration}'s value.</p>
     * @since 1.0.0
     * @see Expression
     */
    private Expression initializationExpression    ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link VariableDeclaration} to its' default state with the specified {@link Type}, {@link Name},
     * {@link Expression}, & flag indicating if the {@link VariableDeclaration} was specified with the {@link Constant}
     * {@link Modifier}.</p>
     * @param modifiers The {@link Modifiers} specified with the {@link VariableDeclaration}.
     * @param type The {@link Type} bound to the {@link VariableDeclaration}.
     * @param name The {@link Name} bound to the {@link VariableDeclaration}.
     * @param initializationExpression The {@link Expression} that defaults the {@link VariableDeclaration}'s value.
     */
    public VariableDeclaration(final Modifiers modifiers,
                               final Type type,
                               final Name name,
                               final Expression initializationExpression) {
        super(modifiers, name, initializationExpression);

        this.modifiers                  = modifiers                 ;
        this.type                       = type                      ;
        this.name                       = name                      ;
        this.initializationExpression   = initializationExpression  ;

    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} value of the {@link VariableDeclaration}'s {@link Name}.</p>
     * @return {@link String} value of the {@link VariableDeclaration}'s {@link Name}.
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
     * {@link Expression} via {@link Expression#accept(Visitor)}, & {@link VariableDeclaration} via
     * {@link Visitor#visitVariableDeclaration(VariableDeclaration)} before updating the {@link Context} to the enclosing
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
        visitor.visitVariableDeclaration(this);

        // Close the Context
        visitor.setContext(this.closeContext());

    }

    /// ------------------
    /// InitializedContext

    /**
     * <p>Returns a flag indicating if the {@link VariableDeclaration} is initialized.</p>
     * @return Flag indicating if the {@link VariableDeclaration} is initialized.
     * @since 1.0.0
     */
    @Override
    public final boolean isInitialized() {

        return this.initializationExpression != null;

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link VariableDeclaration} was specified with the {@link Constant}
     * {@link Modifier}.</p>
     * @return A flag indicating if the {@link VariableDeclaration} was specified with the {@link Constant}
     *         {@link Modifier}.
     * @since 1.0.0
     * @see Constant
     * @see Modifier
     */
    public final boolean isConstant() {

        return this.modifiers.isConstant();

    }

    /**
     * <p>Returns the {@link Modifiers} bound to the {@link VariableDeclaration}.</p>
     * @return The {@link Modifiers} bound to the {@link VariableDeclaration}.
     * @since 1.0.0
     * @see Modifiers
     */
    public final Modifiers getModifiers() {

        return this.modifiers;

    }

    /**
     * <p>Returns the {@link VariableDeclaration}'s bound {@link Type}.</p>
     * @return The {@link VariableDeclaration}'s bound {@link Type}.
     * @since 1.0.0
     * @see Type
     */
    public final Type getType() {

        return this.type;

    }

    /**
     * <p>Returns the {@link VariableDeclaration}'s bound {@link Name}.</p>
     * @return The {@link VariableDeclaration}'s bound {@link Name}.
     * @since 1.0.0
     * @see Name
     */
    public final Name getName() {

        return this.name;

    }

    /**
     * <p>Returns the {@link VariableDeclaration}'s initialization {@link Expression}.</p>
     * @return The {@link VariableDeclaration}'s initialization {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final Expression getInitializationExpression() {

        return this.initializationExpression;

    }

    /**
     * <p>Mutates the {@link VariableDeclaration}'s initialization {@link Expression}.</p>
     * @param initializationExpression The desired initialization {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final void setInitializationExpression(final Expression initializationExpression) {

        this.initializationExpression = initializationExpression;

    }

    /**
     * <p>Mutates the {@link VariableDeclaration}'s bound {@link Name}.</p>
     * @param name The desired {@link Name} to bind to the {@link VariableDeclaration}.
     * @since 1.0.0
     * @see Name
     */
    public final void setName(final Name name) {

        this.name = name;

    }

    /**
     * <p>Mutates the {@link VariableDeclaration}'s bound {@link Type}.</p>
     * @param type The desired {@link Type} to bind to the {@link VariableDeclaration}.
     * @since 1.0.0
     * @see Name
     */
    public final void setType(final Type type) {

        this.type = type;

    }

}