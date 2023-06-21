package org.processj.compiler.ast.type;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Encapsulates a top-level {@link ConstantDeclaration}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see Sequence
 * @see Modifier
 * @see Type
 * @see Name
 * @see Expression
 */
public class ConstantDeclaration extends Type {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Sequence} of {@link Modifier}s dictating the {@link ConstantDeclaration}'s access.</p>
     * @since 1.0.0
     */
    private final Modifiers    modifiers                    ;

    /**
     * <p>The {@link ConstantDeclaration}'s bound {@link Type}.</p>
     * @since 1.0.0
     */
    private Type                        type                         ;

    /**
     * <p>The {@link Name} bound to the {@link ConstantDeclaration}.</p>
     * @since 1.0.0
     */
    private Name                        name                         ;

    /**
     * <p>The initialization {@link Expression} bound to the {@link ConstantDeclaration}.</p>
     * @since 1.0.0
     */
    private Expression                  initializationExpression     ;

    /**
     * <p>Flag indicating if the {@link ConstantDeclaration} is declared 'native'.</p>
     * @since 1.0.0
     */
    private boolean                     isDeclaredNative             ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ConstantDeclaration} to its' default state with the specified {@link Sequence} of
     * {@link Modifier}s, {@link Type}, {@link Name}, & initialization {@link Expression}.</p>
     * @param modifiers The {@link Sequence} of {@link Modifier}s to bind to the {@link ConstantDeclaration}.
     * @param type The {@link Type} to bind to the {@link ConstantDeclaration}.
     * @param var {@link Var} instance containing the {@link Name} & initialization {@link Expression} to bind to the
     *            {@link ConstantDeclaration}.
     * @since 1.0.0
     * @see AST
     * @see Sequence
     * @see Modifier
     * @see Type
     * @see Name
     * @see Expression
     */
    public ConstantDeclaration(final Modifiers modifiers, final Type type, final Var var) {
        super(modifiers, type, var.getName(), var.getInitializationExpression());

        this.modifiers                  = modifiers                             ;
        this.name                       = var.getName()                         ;
        this.initializationExpression   = var.getInitializationExpression()     ;
        this.isDeclaredNative           = false                                 ;
        this.type                       = type                                  ;

    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} value of the {@link ConstantDeclaration}'s name.</p>
     * @return {@link String} value of the {@link ConstantDeclaration}'s name.
     * @since 1.0.0
     * @see Name
     * @see String
     */
    @Override
    public final String toString() {

        return this.name.toString();

    }

    /// ---
    /// AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ConstantDeclaration}; Updates the
     * {@link Visitor}'s {@link Context} & dispatches the {@link Visitor}'s
     * {@link Visitor#visitConstantDeclaration(ConstantDeclaration)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @since 1.0.0
     * @see Visitor
     * @see Phase.Error
     * @see Context
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        visitor.setContext(this.openContext(visitor.getContext()));

        visitor.visitConstantDeclaration(this);

        visitor.setContext(this.closeContext());

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link ConstantDeclaration} is declared native.</p>
     * @return flag indicating if the {@link ConstantDeclaration} is declared native.
     * @since 1.0.0
     * @see Modifier
     */
    public final boolean isDeclaredNative() {

        // Return the result
        return this.modifiers.isNative();

    }

    /**
     * <p>Returns a flag indicating if the {@link ConstantDeclaration} is initialized; i.e. is bound to an
     * initialization {@link Expression}.</p>
     * @return flag indicating if the {@link ConstantDeclaration} is initialized.
     * @since 1.0.0
     * @see Expression
     */
    public final boolean isInitialized() {

        return this.initializationExpression != null;

    }

    /**
     * <p>Returns the {@link Modifiers} bound to the {@link ConstantDeclaration}.</p>
     * @return The {@link Modifiers} bound to the {@link ConstantDeclaration}.
     * @since 1.0.0
     * @see Modifiers
     * @see Modifier
     */
    public final Modifiers getModifiers() {

        return this.modifiers;

    }

    /**
     * <p>Returns the {@link Type} bound to the {@link ConstantDeclaration}.</p>
     * @return The {@link Type} bound to the {@link ConstantDeclaration}.
     * @since 1.0.0
     * @see Type
     */
    public final Type getType() {

        return this.type;

    }

    /**
     * <p>Returns the {@link Name} bound to the {@link ConstantDeclaration}.</p>
     * @return The {@link Name} bound to the {@link ConstantDeclaration}.
     * @since 1.0.0
     * @see Name
     */
    public final Name getName() {

        return this.name;

    }

    /**
     * <p>Returns the {@link Name} bound to the {@link ConstantDeclaration}.</p>
     * @return The {@link Name} bound to the {@link ConstantDeclaration}.
     * @since 1.0.0
     * @see Name
     */
    public final Expression getInitializationExpression() {

        return this.initializationExpression;

    }

    /**
     * <p>Binds the specified {@link Type} to the {@link ConstantDeclaration}.</p>
     * @param type The {@link Type} to bind to the {@link ConstantDeclaration}.
     * @since 1.0.0
     * @see Type
     */
    public final void setType(final Type type) {

        this.type           = type;
        this.children[1]    = type;

    }

    /**
     * <p>Binds the specified {@link Name} to the {@link ConstantDeclaration}.</p>
     * @param name The {@link Name} to bind to the {@link ConstantDeclaration}.
     * @since 1.0.0
     * @see Name
     */
    public final void setName(final Name name) {

        this.name        = name;
        this.children[2] = name;

    }

    /**
     * <p>Binds the specified initialization {@link Expression} to the {@link ConstantDeclaration}.</p>
     * @param initializationExpression The initialization {@link Expression} to bind to the {@link ConstantDeclaration}.
     * @since 1.0.0
     * @see Expression
     */
    public final void setInitializationExpression(final Expression initializationExpression) {

        this.initializationExpression = initializationExpression;

    }

}