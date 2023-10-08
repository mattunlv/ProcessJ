package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a conditional {@link Statement} that executes one {@link Statement} if the evaluation
 * {@link Expression} holds true, or the other if the evaluation {@link Expression} is false.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see BlockStatement
 * @see ConditionalContext
 */
public class IfStatement extends BlockStatement implements ConditionalContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The evaluation {@link Expression} dictating execution of the {@link IfStatement}.</p>
     * @since 1.0.0
     * @see Expression
     */
    private Expression evaluationExpression ;

    /**
     * <p>The {@link Statement} to execute if the evaluation {@link Expression} evaluates to false.</p>
     * @since 1.0.0
     * @see Statement
     * @see Expression
     */
    private final Statement elseStatement   ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link IfStatement} to its' default state with the specified evaluation {@link Expression},
     * 'then' {@link Statement}, & 'else' {@link Statement}.</p>
     * @param evaluationExpression The {@link Expression} dictating execution of the {@link IfStatement}.
     * @param thenStatement
     * The {@link Statement} corresponding to the {@link IfStatement}'s 'then' {@link Statement}.
     * @param elseStatement The {@link Statement} corresponding to the {@link IfStatement}'s 'else' {@link Statement}.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link IfStatement}.
     * @since 1.0.0
     * @see Statement
     * @see Expression
     */
    public IfStatement(final Expression evaluationExpression,
                       final Statement thenStatement,
                       final Statement elseStatement,
                       final Token... tokens) {
        super(thenStatement, tokens);

        this.evaluationExpression   = evaluationExpression  ;
        this.elseStatement          = elseStatement         ;

    }

    /**
     * <p>Initializes the {@link IfStatement} to its' default state with the specified evaluation {@link Expression},
     * & 'then' {@link Statement}.</p>
     * @param evaluationExpression The {@link Expression} dictating execution of the {@link IfStatement}.
     * @param thenStatement The {@link Statement} corresponding to the {@link IfStatement}'s 'then' {@link Statement}.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link IfStatement}.
     * @since 1.0.0
     * @see Statement
     * @see Expression
     */
    public IfStatement(final Expression evaluationExpression,
                       final Statement thenStatement,
                       final Token... tokens) {
        super(thenStatement, tokens);

        this.evaluationExpression   = evaluationExpression              ;
        this.elseStatement          = new BlockStatement()              ;

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context} & scope, and dispatches the
     * {@link Visitor} to the else {@link Statement} via {@link Statement#accept(Visitor)}, evaluation
     * {@link Expression} via {@link Expression#accept(Visitor)}, the {@link IfStatement} via
     * {@link Visitor#visitIfStatement(IfStatement)}, & {@link Statement} children via {@link Statement#accept(Visitor)}
     * before updating the {@link org.processj.compiler.ast.Context} to the
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

        // Dispatch the Else Statement
        this.elseStatement.accept(visitor);

        // Dispatch the Evaluation Expression, if any
        this.evaluationExpression.accept(visitor);

        // Dispatch the If Statement
        visitor.visitIfStatement(this);

        // Dispatch the children
        this.getBody().accept(visitor);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// ------------------
    /// ConditionalContext

    /**
     * <p>Returns the evaluation {@link Expression} dictating execution of the {@link IfStatement}.</p>
     * @return The evaluation {@link Expression} dictating execution of the {@link IfStatement}.
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
     * <p>Returns the {@link IfStatement}'s 'else' {@link Statement}.</p>
     * @return The {@link IfStatement}'s 'else' {@link Statement}.
     * @since 1.0.0
     * @see Statement
     */
    public final Statement getElseStatement() {

        return this.elseStatement;

    }

    /**
     * <p>Mutates the {@link IfStatement}'s evaluation {@link Expression} to the specified value.</p>
     * @param evaluationExpression The evaluation {@link Expression} to mutate to.
     * @since 1.0.0
     * @see Expression
     */
    public final void setEvaluationExpression(final Expression evaluationExpression) {

        this.evaluationExpression = evaluationExpression;

    }

}