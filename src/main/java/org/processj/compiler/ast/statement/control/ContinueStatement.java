package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a control {@link Statement} that directs the path of execution to the {@link Statement}
 * corresponding with the specified {@link Name}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see Context
 * @see ControlContext
 * @see Name
 * @see Statement
 */
public class ContinueStatement extends Statement implements ControlContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Name} corresponding to the label of the {@link Context} to direct to.</p>
     * @since 1.0.0
     * @see Name
     * @see Context
     */
    private final Name target;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ContinueStatement} with the specified {@link Name}.</p>
     * @param target The {@link Name} corresponding to the label of the {@link Context} to direct the path of execution
     *               to.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link ContinueStatement}.
     * @since 1.0.0
     * @see Name
     * @see Token
     */
    public ContinueStatement(final Name target, final Token... tokens) {
        super(tokens);

        this.target = target;

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context}, and dispatches the
     * {@link Visitor} to the {@link ContinueStatement} via {@link Visitor#visitContinueStatement(ContinueStatement)}
     * before updating the {@link org.processj.compiler.ast.Context} to the enclosing {@link Context}.
     * @since 1.0.0
     * @see Visitor
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Dispatch the Continue Statement
        visitor.visitContinueStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the {@link Name} corresponding to the label of the {@link Context} to direct the path of execution
     * to.</p>
     * @return The {@link Name} corresponding to the label of the {@link Context} to direct the path of execution to.
     * @since 1.0.0
     * @see Name
     * @see Context
     */
    public final Name getTarget() {

        return this.target;

    }

}