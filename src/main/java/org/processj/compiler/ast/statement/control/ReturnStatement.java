package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a control {@link Statement} that directs the path of execution to the {@link Statement}
 * immediately after the enclosing {@link Context}'s call site.</p>
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
public class ReturnStatement extends Statement implements ControlContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Expression} corresponding to the result to return.</p>
     * @since 1.0.0
     * @see Expression
     */
    private final Expression expression;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ReturnStatement} with the specified {@link Expression}.</p>
     * @param expression The {@link Expression} corresponding to result to return.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link ReturnStatement}.
     * @since 1.0.0
     * @see Expression
     * @see Token
     */
    public ReturnStatement(final Expression expression, final Token... tokens) {
        super(tokens);

        this.expression = expression;

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link Context}, and dispatches the {@link Visitor} to the contained
     * {@link Expression} & {@link ReturnStatement} via {@link Visitor#visitReturnStatement(ReturnStatement)} before
     * updating the {@link Context} to the enclosing {@link Context}.
     * @since 1.0.0
     * @see Visitor
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Dispatch the Expression, if any
        if(this.expression != null) this.expression.accept(visitor);

        // Dispatch the Return Statement
        visitor.visitReturnStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link ReturnStatement} was specified with an {@link Expression}.</p>
     * @return A flag indicating if the {@link ReturnStatement} was specified with an {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final boolean definesExpression() {

        return this.expression != null;

    }

    /**
     * <p>Returns the {@link Expression} whose result the {@link ReturnStatement} will return.</p>
     * @return The {@link Expression} whose result the {@link ReturnStatement} will return.
     * @since 1.0.0
     * @see Expression
     */
    public final Expression getExpression() {

        return this.expression;

    }

}