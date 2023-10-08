package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a {@link Statement} that terminates the top-most enclosing {@link Context}.</p>
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
public class StopStatement extends Statement implements ControlContext {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link StopStatement} to its' default state.</p>
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link StopStatement}.
     * @since 1.0.0
     * @see Name
     * @see Token
     */
    public StopStatement(final Token... tokens) {
        super(tokens);
    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context}, and dispatches the
     * {@link Visitor} to the {@link StopStatement} via {@link Visitor#visitStopStatement(StopStatement)}
     * before updating the {@link org.processj.compiler.ast.Context} to the enclosing {@link Context}.
     * @since 1.0.0
     * @see Visitor
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Dispatch the Skip Statement
        visitor.visitStopStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

}