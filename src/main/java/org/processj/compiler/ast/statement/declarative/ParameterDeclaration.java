package org.processj.compiler.ast.statement.declarative;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.modifier.Modifier;
import org.processj.compiler.ast.modifier.Constant;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.modifier.Modifiers;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a declaration of a {@link Context}'s parameter variable.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see Context
 * @see DeclarativeContext
 * @see Statement
 */
public class ParameterDeclaration extends Statement implements DeclarativeContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The set of {@link Modifiers} corresponding to the {@link ParameterDeclaration}.</p>
     * @since 1.0.0
     * @see Modifiers
     */
    private final Modifiers modifiers                   ;

    /**
     * <p>The {@link Name} corresponding to the {@link ParameterDeclaration}.</p>
     * @since 1.0.0
     * @see Name
     */
    private Name            name                        ;

    /**
     * <p>The {@link Type} bound to the {@link ParameterDeclaration}.</p>
     * @since 1.0.0
     * @see Type
     */
    private Type            type                        ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ParameterDeclaration} to its' default state with the specified {@link Modifiers},
     * {@link Type}, & {@link Name}.</p>
     * @param modifiers The {@link Modifiers} bound to the {@link ParameterDeclaration}.
     * @param type The {@link Type} bound to the {@link ParameterDeclaration}.
     * @param name The {@link Name} bound to the {@link ParameterDeclaration}.
     */
    public ParameterDeclaration(final Modifiers modifiers,
                                final Type type,
                                final Name name,
                                final Expression expression) {
        super(name);

        this.modifiers  = modifiers     ;
        this.type       = type          ;
        this.name       = name          ;

    }

    /**
     * <p>Initializes the {@link ParameterDeclaration} to its' default state with the specified {@link Modifiers},
     * {@link Type}, & {@link Name}.</p>
     * @param modifiers The {@link Modifiers} bound to the {@link ParameterDeclaration}.
     * @param type The {@link Type} bound to the {@link ParameterDeclaration}.
     * @param name The {@link Name} bound to the {@link ParameterDeclaration}.
     */
    public ParameterDeclaration(final Modifiers modifiers,
                                final Type type,
                                final Name name) {
        this(modifiers, type, name, null);
    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} value of the {@link ParameterDeclaration}'s {@link Name}.</p>
     * @return {@link String} value of the {@link ParameterDeclaration}'s {@link Name}.
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
     * via {@link Type#accept(Visitor)}, {@link Name} via {@link Name#accept(Visitor)}, & {@link ParameterDeclaration}
     * via {@link Visitor#visitParameterDeclaration(ParameterDeclaration)} before updating the {@link Context} to the
     * enclosing {@link Context}.
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

        // Dispatch the Sync Statement
        visitor.visitParameterDeclaration(this);

        // Close the Context
        visitor.setContext(this.closeContext());

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link ParameterDeclaration} was specified with the {@link Constant}
     * {@link Modifier}.</p>
     * @return A flag indicating if the {@link ParameterDeclaration} was specified with the {@link Constant}
     *         {@link Modifier}.
     * @since 1.0.0
     * @see Constant
     * @see Modifier
     */
    public final boolean isConstant() {

        return this.modifiers.isConstant();

    }

    /**
     * <p>Returns the {@link ParameterDeclaration}'s bound {@link Type}.</p>
     * @return The {@link ParameterDeclaration}'s bound {@link Type}.
     * @since 1.0.0
     * @see Type
     */
    public final Type getType() {

        return this.type;

    }

    /**
     * <p>Returns the {@link ParameterDeclaration}'s bound {@link Name}.</p>
     * @return The {@link ParameterDeclaration}'s bound {@link Name}.
     * @since 1.0.0
     * @see Name
     */
    public final Name getName() {

        return this.name;

    }

    /**
     * <p>Mutates the {@link ParameterDeclaration}'s bound {@link Name}.</p>
     * @param name The desired {@link Name} to bind to the {@link ParameterDeclaration}.
     * @since 1.0.0
     * @see Name
     */
    public final void setName(final Name name) {

        this.name = name;

    }

    /**
     * <p>Mutates the {@link ParameterDeclaration}'s bound {@link Type}.</p>
     * @param type The desired {@link Type} to bind to the {@link ParameterDeclaration}.
     * @since 1.0.0
     * @see Name
     */
    public final void setType(final Type type) {

        this.type = type;

    }

}