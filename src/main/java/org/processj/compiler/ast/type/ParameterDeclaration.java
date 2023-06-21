package org.processj.compiler.ast.type;

import org.processj.compiler.ast.*;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Encapsulates a {@link ParameterDeclaration}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see Type
 * @see Name
 */
public class ParameterDeclaration extends Type {

    /// --------------
    /// Private Fields

    /**
     * <p>Flag indicating if the {@link ParameterDeclaration} is declared 'constant'.</p>
     * @since 1.0.0
     */
    private final boolean   isDeclaredConstant  ;

    /**
     * <p>The {@link ParameterDeclaration}'s bound {@link Type}.</p>
     * @since 1.0.0
     */
    private Type            type                ;

    /**
     * <p>The {@link Name} bound to the {@link ParameterDeclaration}.</p>
     * @since 1.0.0
     */
    private Name            name                ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ParameterDeclaration} to its' default state with the specified {@link Type},
     * {@link Name}, & flag indicating if the {@link ParameterDeclaration} is declared 'constant'.</p>
     * @param type The {@link Type} to bind to the {@link ConstantDeclaration}.
     * @param name The {@link Name} to bind to the {@link ConstantDeclaration}.
     * @param isDeclaredConstant flag indicating if the {@link ParameterDeclaration} is declared 'constant'.
     * @since 1.0.0
     * @see AST
     * @see Type
     * @see Name
     */
    public ParameterDeclaration(final Type type, final Name name, boolean isDeclaredConstant) {
        super(new AST[] { type, name });

        this.isDeclaredConstant = isDeclaredConstant    ;
        this.type               = type                  ;
        this.name               = name                  ;

    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} value of the {@link ParameterDeclaration}'s name.</p>
     * @return {@link String} value of the {@link ParameterDeclaration}'s name.
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
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ParameterDeclaration}; Updates the
     * {@link Visitor}'s {@link Context} & dispatches the {@link Visitor}'s
     * {@link Visitor#visitParameterDeclaration(ParameterDeclaration)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @since 1.0.0
     * @see Visitor
     * @see Phase.Error
     * @see Context
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        visitor.setContext(this.openContext(visitor.getContext()));

        visitor.visitParameterDeclaration(this);

        visitor.setContext(this.closeContext());

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link ParameterDeclaration} is declared 'constant'.</p>
     * @return flag indicating if the {@link ParameterDeclaration} is declared 'constant'.
     * @since 1.0.0
     */
    public final boolean isDeclaredConstant() {

        // Return the result
        return this.isDeclaredConstant;

    }

    /**
     * <p>Returns the {@link Type} bound to the {@link ParameterDeclaration}.</p>
     * @return The {@link Type} bound to the {@link ParameterDeclaration}.
     * @since 1.0.0
     * @see Type
     */
    public final Type getType() {

        return this.type;

    }

    /**
     * <p>Returns the {@link Name} bound to the {@link ParameterDeclaration}.</p>
     * @return The {@link Name} bound to the {@link ParameterDeclaration}.
     * @since 1.0.0
     * @see Name
     */
    public final Name getName() {

        return this.name;

    }

    /**
     * <p>Binds the specified {@link Type} to the {@link ParameterDeclaration}.</p>
     * @param type The {@link Type} to bind to the {@link ParameterDeclaration}.
     * @since 1.0.0
     * @see Type
     */
    public final void setType(final Type type) {

        this.type           = type;
        this.children[0]    = type;

    }

    /**
     * <p>Binds the specified {@link Name} to the {@link ParameterDeclaration}.</p>
     * @param name The {@link Name} to bind to the {@link ParameterDeclaration}.
     * @since 1.0.0
     * @see Name
     */
    public final void setName(final Name name) {

        this.name        = name;
        this.children[1] = name;

    }

}