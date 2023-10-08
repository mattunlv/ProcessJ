package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates an iterative {@link Statement} that executes as long as the evaluated {@link Expression}
 * holds true.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see BlockStatement
 * @see IterativeContext
 * @see Expression
 */
public class WhileStatement extends BlockStatement implements IterativeContext  {

    /// --------------
    /// Private Fields

    /**
     * <p>The evaluation {@link Expression} dictating execution of the {@link WhileStatement}.</p>
     * @since 1.0.0
     * @see Expression
     */
    private Expression evaluationExpression;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link WhileStatement} to its' default state with the specified evaluation {@link Expression}
     * & {@link Statement} body.</p>
     * @param evaluationExpression The {@link Expression} dictating execution of the {@link WhileStatement}.
     * @param body The {@link WhileStatement}'s body.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link WhileStatement}.
     * @since 1.0.0
     * @see Statement
     * @see Expression
     * @see Token
     */
    public WhileStatement(final Expression evaluationExpression,
                          final Statement body,
                          final Token... tokens) {
        super(body, tokens);

        this.evaluationExpression = evaluationExpression;

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context} & scope, and dispatches the
     * {@link Visitor} to the evaluation {@link Expression} via {@link Expression#accept(Visitor)}, the
     * {@link WhileStatement} via {@link Visitor#visitWhileStatement(WhileStatement)}, & {@link Statement} children
     * via {@link Statement#accept(Visitor)} before updating the {@link org.processj.compiler.ast.Context} to the
     * enclosing {@link Context}.
     * @since 1.0.0
     * @see Visitor
     * @see Statement
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope
        this.openScope();

        // Dispatch the Evaluation Expression
        this.evaluationExpression.accept(visitor);

        // Dispatch the WhileStatement
        visitor.visitWhileStatement(this);

        // Dispatch the children
        this.getBody().accept(visitor);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// ------------------
    /// ConditionalContext

    /**
     * <p>Returns the evaluation {@link Expression} dictating execution of the {@link WhileStatement}.</p>
     * @return The evaluation {@link Expression} dictating execution of the {@link WhileStatement}.
     * @since 1.0.0
     * @see Expression
     */
    @Override
    public final Expression getEvaluationExpression() {

        return this.evaluationExpression;

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Mutates the {@link WhileStatement}'s evaluation {@link Expression} to the specified value.</p>
     * @param evaluationExpression The evaluation {@link Expression} to mutate to.
     * @since 1.0.0
     * @see Expression
     */
    public final void setEvaluationExpression(final Expression evaluationExpression) {

        this.evaluationExpression   = evaluationExpression;

    }

}