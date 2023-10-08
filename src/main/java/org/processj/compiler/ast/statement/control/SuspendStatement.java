package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.statement.declarative.ParameterDeclaration;
import org.processj.compiler.ast.statement.declarative.Parameters;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a {@link Statement} that yields the top-most enclosing {@link Context}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see Context
 * @see ControlContext
 * @see Statement
 */
public class SuspendStatement extends Statement implements ControlContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The set of {@link ParameterDeclaration}s the enclosing {@link Context} will resume on.</p>
     * @since 1.0.0
     * @see Parameters
     * @see AST
     */
    private final Parameters parameters;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link SuspendStatement} with the specified {@link Sequence} of {@link ParameterDeclaration}s
     * .</p>
     * @param parameters The {@link Sequence} of {@link ParameterDeclaration}s the enclosing {@link Context} will
     *                   resume on.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link ClaimStatement}.
     * @since 1.0.0
     * @see ParameterDeclaration
     * @see Sequence
     * @see Token
     */
    public SuspendStatement(final Parameters parameters, final Token... tokens) {
        super(tokens);

        this.parameters = parameters;

    }
    
    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link Context}, and dispatches the {@link Visitor} to each
     * {@link ParameterDeclaration} specified in the {@link SuspendStatement}, & {@link ClaimStatement} via
     * {@link Visitor#visitSuspendStatement(SuspendStatement)} before updating the {@link Context} to the enclosing
     * {@link Context}.
     * @since 1.0.0
     * @see Visitor
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Dispatch the children
        this.parameters.accept(visitor);

        // Dispatch the Suspend Statement
        visitor.visitSuspendStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the {@link Sequence} of {@link ParameterDeclaration}s the {@link SuspendStatement}'s enclosing
     * {@link Context} will resume with.</p>
     * @return The {@link Sequence} of {@link ParameterDeclaration}s the {@link SuspendStatement}'s enclosing
     *          {@link Context} will resume with.
     * @since 1.0.0
     * @see Sequence
     * @see ParameterDeclaration
     */
    public final Parameters getParameters() {

        return this.parameters;

    }

}