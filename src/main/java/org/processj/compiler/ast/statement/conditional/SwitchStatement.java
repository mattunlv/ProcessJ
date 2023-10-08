package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.ExpressionStatement;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.statement.Statements;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a set of {@link Group}s with an evaluation {@link Expression} that dictates which
 * {@link Group} to execute.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see BlockStatement
 * @see ConditionalContext
 */
public class SwitchStatement extends BlockStatement implements ConditionalContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The evaluation {@link Expression} that dictates which {@link Group} to execute.</p>
     * @since 1.0.0
     * @see Expression
     * @see Group
     */
    private final Expression evaluationExpression;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link SwitchStatement} to its' default state with the specified {@link Statement} &
     * evaluation {@link Expression}.</p>
     * @param evaluationExpression The {@link Expression} that dictates which {@link Group} to execute.
     * @param body The collection of {@link Group}s contained in the {@link SwitchStatement}.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link SwitchStatement}.
     * @since 1.0.0
     * @see Expression
     * @see Statement
     * @see Token
     */
    public SwitchStatement(final Expression evaluationExpression,
                           final Statement body,
                           final Token... tokens) {
        super(body, tokens);

        this.evaluationExpression = evaluationExpression ;

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context} & scope, and dispatches the
     * {@link Visitor} to the evaluation {@link Expression} via {@link Expression#accept(Visitor)}, the
     * {@link SwitchStatement} via {@link Visitor#visitSwitchStatement(SwitchStatement)}, & {@link Group} children
     * via {@link Group#accept(Visitor)} before updating the {@link org.processj.compiler.ast.Context} to the
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

        // Dispatch the SwitchStatement
        visitor.visitSwitchStatement(this);

        // Dispatch the children
        this.getBody().accept(visitor);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// ------------------
    /// ConditionalContext

    /**
     * <p>Returns the evaluation {@link Expression} dictating control of the {@link SwitchStatement}.</p>
     * @return The evaluation {@link Expression} dictating control of the {@link SwitchStatement}.
     * @since 1.0.0
     * @see Expression
     */
    @Override
    public final Expression getEvaluationExpression() {

        return this.evaluationExpression;

    }

    /// -------
    /// Classes

    /**
     * <p>Class that encapsulates a {@link SwitchStatement}'s individual group.</p>
     * @author Jan B. Pedersen
     * @author Cabel Shrestha
     * @author Benjamin Cisneros
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 1.0.0
     * @see BlockStatement
     * @see BreakableContext
     */
    public static class Group extends BlockStatement implements BreakableContext {

        /// --------------
        /// Private Fields

        /**
         * <p>The set of {@link Case}s that occur before the {@link Group}'s body.</p>
         * @since 1.0.0
         * @see Case
         */
        private final Statements groupCases;

        /// -----------
        /// Constructor

        /**
         * <p>Initializes the {@link Group} to its' default state with the specified set of
         * {@link Case}s & {@link Statement} body.</p>
         * @param body The set of {@link Statements} contained in the {@link Group}
         * @param groupCases The set of {@link Case}s that occur before the {@link Group}'s body.
         * @since 1.0.0
         * @see Case
         * @see Statement
         */
        public Group(final Statements groupCases, final Statement body) {
            super(body);

            this.groupCases = groupCases;

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

            // Open a scope for the Par Block
            this.openScope();

            // Dispatch the SwitchGroupStatement
            visitor.visitSwitchStatementGroup(this);

            // Dispatch the children
            this.getBody().accept(visitor);

            // Close the scope
            visitor.setContext(this.closeContext());

        }

        /// --------------
        /// Public Methods

        /**
         * <p>Returns the set of {@link Case}s that occur before the {@link Group}'s body.</p>
         * @return The set of {@link Case}s that occur before the {@link Group}'s body.
         * @since 1.0.0
         * @see Case
         */
        public final Statements getCases() {

            return this.groupCases;

        }

        /// -------
        /// Classes

        /**
         * <p>Class that encapsulates a case {@link Statement} that occurs before the body of a {@link Group}.</p>
         * @author Jan B. Pedersen
         * @author Cabel Shrestha
         * @author Benjamin Cisneros
         * @author Carlos L. Cuenca
         * @version 1.0.0
         * @since 1.0.0
         * @see Statement
         * @see ExpressionStatement
         * @see Group
         */
        public static class Case extends ExpressionStatement {

            /// -----------
            /// Constructor

            /**
             * <p>Initializes the {@link Case} to its' default state with the specified {@link Expression}.</p>
             * @param expression The specified {@link Expression}.
             * @since 1.0.0
             * @see Expression
             */
            public Case(final Expression expression) {
                super(expression);
            }

            /// --------------
            /// Public Methods

            /**
             * <p>Returns a flag indicating if the {@link Case} is specified as 'default'.</p>
             * @return Flag indicating if the {@link Case} is specified as 'default'.
             * @since 1.0.0
             */
            public final boolean isDefault() {

                return !this.definesExpression();

            }

        }

    }

}