package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a {@link Statement} that does nothing.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see Statement
 */
public class SkipStatement extends Statement {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link SkipStatement} to its' default state.</p>
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link SkipStatement}.
     * @since 1.0.0
     * @see Name
     * @see Token
     */
    public SkipStatement(final Token... tokens) {
        super(tokens);
    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context}, and dispatches the
     * {@link Visitor} to the {@link SkipStatement} via {@link Visitor#visitSkipStatement(SkipStatement)}
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
        visitor.visitSkipStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

}