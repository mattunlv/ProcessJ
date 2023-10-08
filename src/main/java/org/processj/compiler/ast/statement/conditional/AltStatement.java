package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.statement.ExpressionStatement;
import org.processj.compiler.ast.statement.Statements;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.statement.yielding.YieldingContext;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

import java.util.List;

/**
 * <p>Class that encapsulates deterministic or non-deterministic choice; additionally,
 * the {@link AltStatement} may be specified as prioritized, replicated, or both.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see List
 * @see Statement
 * @see SymbolMap
 */
public class AltStatement extends BlockStatement implements YieldingContext, ConditionalContext {

    /// --------------
    /// Private Fields

    /**
     * <p>Flag indicating if the {@link AltStatement} is a prioritized choice.</p>
     * @since 1.0.0
     */
    private final boolean       isPrioritizedChoice         ;

    /**
     * <p>Flag indicating if the {@link AltStatement} is replicated.</p>
     * @since 1.0.0
     */
    private final boolean       isReplicated                ;

    /**
     * <p>The set of initialization {@link Statements} specified if the {@link AltStatement} is replicated.</p>
     * @since 1.0.0
     * @see Statement
     */
    private final Statements    initializationStatements    ;

    /**
     * <p>The evaluation {@link Expression} dictating control of the {@link AltStatement}, if replicated.</p>
     * @since 1.0.0
     * @see Expression
     */
    private final Expression    evaluationExpression        ;

    /**
     * <p>The set of increment {@link Statements} specified if the {@link AltStatement} is replicated.</p>
     * @since 1.0.0
     * @see Statement
     */
    private final Statements    incrementStatements         ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link AltStatement} to its' default state with the specified set of {@link Case}s &
     * flag indicating if the {@link AltStatement} is a prioritized choice.</p>
     * @param body The set of {@link Case}s composing the {@link AltStatement}'s body.
     * @param isPrioritizedChoice Flag indicating if the {@link AltStatement} is a prioritized choice.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link DoStatement}.
     * @since 1.0.0
     * @see Case
     * @see Statement
     */
    public AltStatement(final Statement body,
                        final boolean isPrioritizedChoice,
                        final Token... tokens) {
        super(body, tokens);

        this.isPrioritizedChoice        = isPrioritizedChoice                       ;
        this.isReplicated               = false                                     ;
        this.initializationStatements   = new Statements()                          ;
        this.evaluationExpression       = null                                      ;
        this.incrementStatements        = new Statements()                          ;

    }

    /**
     * <p>Initializes the {@link AltStatement} to its' default state with the specified set of initialization
     * {@link Statements}, evaluation {@link Expression}, increment {@link Statements}, set of {@link Case}s &
     * flag indicating if the {@link AltStatement} is a prioritized choice. This constructor will set
     * {@link AltStatement#isReplicated} to true by default.</p>
     * @param initializationStatements The set of {@link Statements} defining local variables for use in the replicated
     *                                 {@link AltStatement}.
     * @param evaluationExpression The {@link Expression} dictating control of the {@link AltStatement}.
     * @param incrementStatements The set of {@link Statements} that are executed after each evaluation of the
     *                            replicated {@link AltStatement}.
     * @param body The set of {@link Case}s composing the replicated {@link AltStatement}'s body.
     * @param isPrioritizedChoice Flag indicating if the replicated {@link AltStatement} is a prioritized choice.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link DoStatement}.
     * @since 1.0.0
     * @see Case
     * @see Statement
     * @see Expression
     */
    public AltStatement(final Statements initializationStatements,
                        final Expression evaluationExpression,
                        final Statements incrementStatements,
                        final Statement body,
                        final boolean isPrioritizedChoice,
                        final Token... tokens) {

        super(body, tokens);

        this.isReplicated               = true                  ;
        this.isPrioritizedChoice        = isPrioritizedChoice   ;
        this.evaluationExpression       = evaluationExpression  ;

        this.initializationStatements   = (initializationStatements != null)
                ? initializationStatements : new Statements();

        this.incrementStatements        = (incrementStatements != null)
                ? incrementStatements : new Statements();

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context} & scope, and dispatches the
     * {@link Visitor} to the initialization {@link Statements} via {@link Statements#accept(Visitor)}, evaluation
     * {@link Expression} via {@link Expression#accept(Visitor)}, increment {@link Statements} via
     * {@link Statements#accept(Visitor)}, if the {@link AltStatement} is replicated, the {@link AltStatement} via
     * {@link Visitor#visitAltStatement(AltStatement)} & {@link Statement} children via
     * {@link Statement#accept(Visitor)} before updating the {@link org.processj.compiler.ast.Context} to the enclosing
     * {@link Context}.
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

        // Dispatch the initialization statements
        this.initializationStatements.accept(visitor);

        // Dispatch the evaluation Expression, if any
        if(this.evaluationExpression != null) this.evaluationExpression.accept(visitor);

        // Dispatch the increment statements
        this.incrementStatements.accept(visitor);

        // Dispatch the Alt Statement
        visitor.visitAltStatement(this);

        // Dispatch the children
        this.getBody().accept(visitor);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// ------------------
    /// ConditionalContext

    /**
     * <p>Returns the evaluation {@link Expression} dictating execution of the {@link AltStatement}.</p>
     * @return The evaluation {@link Expression} dictating execution of the {@link AltStatement}.
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
     * <p>Returns a flag indicating if the {@link AltStatement} was specified as a prioritized choice.</p>
     * @return Flag indicating if the {@link AltStatement} was specified as a prioritized choice.
     * @since 1.0.0
     */
    public final boolean isPrioritizedChoice() {

        return this.isPrioritizedChoice;

    }

    /**
     * <p>Returns a flag indicating if the {@link AltStatement} was specified as replicated.</p>
     * @return Flag indicating if the {@link AltStatement} was specified as replicated.
     * @since 1.0.0
     */
    public final boolean isReplicated() {

        return this.isReplicated;

    }

    /**
     * <p>Returns a flag indicating if the {@link AltStatement} was specified with initialization {@link Statements}.
     * This invocation evaluates to true if and only if the {@link AltStatement} was specified as replicated.</p>
     * @return A flag indicating if the {@link AltStatement} was specified with initialization {@link Statements}.
     * @since 1.0.0
     * @see Statement
     * @see Statements
     */
    public final boolean definesInitializationStatements() {

        return this.initializationStatements != null;

    }

    /**
     * <p>Returns a flag indicating if the {@link AltStatement} was specified with multiple initialization
     * {@link Statements}. This invocation evaluates to true if and only if the {@link AltStatement} was specified as
     * replicated.</p>
     * @return A flag indicating if the {@link AltStatement} was specified with initialization {@link Statements}.
     * @since 1.0.0
     * @see Statement
     * @see Statements
     */
    public final boolean definesMultipleInitializationStatements() {

        return this.initializationStatements.size() > 1;

    }

    /**
     * <p>Returns a flag indicating if the {@link AltStatement} was specified with an evaluation {@link Expression}.
     * This invocation evaluates to true if and only if the {@link AltStatement} was specified as replicated.</p>
     * @return A flag indicating if the {@link AltStatement} was specified with an evaluation {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final boolean definesEvaluationExpression() {

        return this.evaluationExpression != null;

    }

    /**
     * <p>Returns a flag indicating if the {@link AltStatement} was specified with increment {@link Statements}.
     * This invocation evaluates to true if and only if the {@link AltStatement} was specified as replicated.</p>
     * @return A flag indicating if the {@link AltStatement} was specified with increment {@link Statements}.
     * @since 1.0.0
     * @see Statement
     * @see Statements
     */
    public final boolean definesIncrementExpression() {

        return this.incrementStatements != null;

    }

    /**
     * <p>Returns the set of initialization {@link Statements} specified with the replicated {@link AltStatement}.</p>
     * @return The set of initialization {@link Statements} specified with the replicated {@link AltStatement}.
     * @since 1.0.0
     * @see Statement
     * @see Statements
     */
    public final Statements initializationStatements() {

        return this.initializationStatements;

    }

    /**
     * <p>Returns the set of increment {@link Statements} specified with the replicated {@link AltStatement}.</p>
     * @return The set of increment {@link Statements} specified with the replicated {@link AltStatement}.
     * @since 1.0.0
     * @see Statement
     * @see Statements
     */
    public final Statements getIncrementStatements() {

        return this.incrementStatements;

    }

    /// -------
    /// Classes

    /**
     * <p>Class that encapsulates a case {@link Statement} that occurs in the body of an {@link AltStatement}.</p>
     * @author Jan B. Pedersen
     * @author Cabel Shrestha
     * @author Benjamin Cisneros
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 1.0.0
     * @see BlockStatement
     */
    public static class Case extends BlockStatement {

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Case} {@link Guard}'s precondition {@link Expression} that dictates execution of
         * the {@link Case}'s {@link Guard}'s execution. A precondition {@link Expression} is evaluated
         * strictly once for each of the enclosing {@link AltStatement}'s execution.</p>
         * @since 1.0.0
         * @see Expression
         */
        private final Expression        preconditionExpression  ;

        /**
         * <p>One of: Input, Timer, or Skip {@link Statement}s that dictate the execution of the {@link Case}'s body.
         * </p>
         * @since 1.0.0
         * @see Guard
         */
        private final Guard guard;

        /**
         * <p>Integral value indicating which position the {@link Case} occurs within the {@link AltStatement}.</p>
         * @since 1.0.0
         * @see AltStatement
         */
        private int                     caseNumber              ;

        /**
         * <p>Flag indicating if the {@link Case} encloses any {@link AltStatement}.</p>
         * @since 1.0.0
         * @see AltStatement
         */
        public  boolean                 isAltStat               ;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link Case} to its' default state with the specified precondition {@link Expression},
         * {@link Guard}, & {@link Statement} body.</p>
         * @param preconditionExpression a boolean {@link Expression} that dictates execution of the {@link Case}'s
         *                               {@link Guard} execution, evaluated strictly once for each of the
         *                               enclosing {@link Guard}'s execution.
         * @param guard {@link Statement} that dictates execution of the {@link Case}'s body.
         * @param body The {@link Statement} body.
         * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link Case}.
         * @since 1.0.0
         * @see Expression
         * @see Guard
         * @see Statement
         * @see Token
         */
        public Case(final Expression preconditionExpression,
                    final Guard guard,
                    final Statement body,
                    final Token... tokens) {
            super(body, tokens);

            this.preconditionExpression = preconditionExpression        ;
            this.guard = guard;
            this.isAltStat              = body instanceof AltStatement  ;
            this.caseNumber             = -1                            ;

        }

        /**
         * <p>Initializes the {@link Case} to its' default state with the specified {@link Statement} body.</p>
         * @param body The {@link Statement} body.
         * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link Case}.
         * @since 1.0.0
         * @see Statement
         * @see Token
         */
        public Case(final Statement body,
                    final Token... tokens) {
            super(body, tokens);

            this.preconditionExpression = null                          ;
            this.guard = null                          ;
            this.isAltStat              = body instanceof AltStatement  ;
            this.caseNumber             = -1                            ;

        }

        /// ------------------------------
        /// org.processj.utilities.Visitor

        /**
         * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context} & scope, and dispatches the
         * {@link Visitor} to the precondition {@link Expression} via {@link Expression#accept(Visitor)}, the
         * {@link Guard} via {@link Guard#accept(Visitor)}, the {@link Case} via
         * {@link Visitor#visitAltStatementCase(Case)}, & {@link Statement} children
         * via {@link Statements#accept(Visitor)} before updating the {@link org.processj.compiler.ast.Context} to the
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

            // Dispatch the precondition Expression
            if(this.preconditionExpression != null) this.preconditionExpression.accept(visitor);

            // Dispatch the Guard Statement
            if(this.guard != null) this.guard.accept(visitor);

            // Dispatch the Case
            visitor.visitAltStatementCase(this);

            // Dispatch the children
            this.getBody().accept(visitor);

            // Close the scope
            visitor.setContext(this.closeContext());

        }

        /// --------------
        /// Public Methods

        /**
         * <p>Returns a flag indicating if the {@link Case} is specified with a precondition {@link Expression}.</p>
         * @return A flag indicating if the {@link Case} is specified with a precondition {@link Expression}.
         * @since 1.0.0
         * @see Expression
         */
        public final boolean definesPrecondition() {

            return this.preconditionExpression != null;

        }

        /**
         * <p>Returns a flag indicating if the {@link Case} is specified with a {@link Guard}; one of input,
         * timer, or skip.</p>
         * @return A flag indicating if the {@link Case} is specified with a {@link Guard}.
         * @since 1.0.0
         * @see Guard
         */
        public final boolean definesGuard() {

            return this.guard != null;

        }

        /**
         * <p>Returns a flag indicating if the {@link Case}'s body is an {@link AltStatement}.</p>
         * @return A flag indicating if the {@link Case}'s body is an {@link AltStatement}.
         * @since 1.0.0
         * @see AltStatement
         */
        public final boolean isNestedAltStatement() {

            return this.isAltStat || (this.getFirst() instanceof AltStatement);

        }

        /**
         * <p>Returns an integral value corresponding with the {@link Case}'s position within the {@link AltStatement}.
         * </p>
         * @return An integral value corresponding with the {@link Case}'s position within the {@link AltStatement}.
         * @since 1.0.0
         * @see AltStatement
         */
        public final int getCaseNumber() {

            return this.caseNumber;

        }

        /**
         * <p>Returns the {@link Case}'s bound precondition {@link Expression}.</p>
         * @return The {@link Case}'s bound precondition {@link Expression}.
         * @since 1.0.0
         * @see Expression
         */
        public final Expression getPreconditionExpression() {

            return this.preconditionExpression;

        }

        /**
         * <p>Returns the {@link Case}'s bound {@link Guard}.</p>
         * @return The {@link Case}'s bound {@link Guard}.
         * @since 1.0.0
         * @see Expression
         */
        public final Guard getGuardStatement() {

            return this.guard;

        }

        /**
         * <p>Mutates the value of the {@link Case}'s case number corresponding to its' position in the
         * {@link AltStatement}.</p>
         * @param caseNumber The value of the {@link Case}'s case number corresponding to its' position in the
         *                   {@link AltStatement}.
         * @since 1.0.0
         * @see AltStatement
         */
        public final void setCaseNumber(int caseNumber) {

            this.caseNumber = caseNumber;

        }

        /// -------
        /// Classes

        /**
         * <p>Class that represents a {@link Guard} within an {@link AltStatement}'s {@link Case}.</p>
         * @author Jan B. Pedersen
         * @author Cabel Shrestha
         * @author Benjamin Cisneros
         * @author Carlos L. Cuenca.
         * @version 1.0.0
         * @since 0.1.0
         * @see Case
         * @see Statement
         */
        public static class Guard extends AST {

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link Statement} that dictates the corresponding {@link Case}'s execution.</p>
             * @since 1.0.0
             * @see Statement
             * @see Case
             */
            private final Statement statement;

            /// ------------
            /// Constructors

            /**
             * <p>Initializes the {@link Guard} to its' default state with the specified {@link Statement}.</p>
             * @param statement The {@link Statement} that dictates the corresponding {@link Case}'s execution.
             * @since 1.0.0
             */
            public Guard(final Statement statement) {
                super(statement);

                this.statement = statement;

            }

            /// ------
            /// Object

            /**
             * <p>Returns a boolean flag indicating if the specified {@link Guard} is identical to the
             * given {@link Guard}. This method will check the super class invocation of {@link Object#equals(Object)},
             * & the instance of the specified {@link Object}.</p>
             * @param that The {@link Object} instance to check against the given instance.
             * @return boolean flag indicating if the specified instance is identical to this.
             * @since 0.1.0
             */
            @Override
            public final boolean equals(final Object that) {

                return super.equals(that) && (that instanceof Guard)
                        && this.statement.equals(((Guard) that).statement);

            }

            /**
             * <p>Returns the {@link String} representation of the {@link Guard}.</p>
             * @return {@link String} representation of the {@link Guard}.
             * @since 1.0.0
             */
            @Override
            public final String toString() {

                return this.statement.toString();

            }

            /// ---
            /// AST

            /**
             * <p>Updates the {@link Visitor}'s {@link Context}, and dispatches the {@link Visitor} to the {@link Statement}
             * via {@link Statement#accept(Visitor)} & {@link Guard} via
             * {@link Visitor#visitGuardStatement(Guard)} before updating the {@link Context} to the enclosing
             * {@link Context}.
             * @since 1.0.0
             * @see Visitor
             * @see Phase.Error
             */
            @Override
            public final void accept(final Visitor visitor) throws Phase.Error {

                // Open the Context
                visitor.setContext(this.openContext(visitor.getContext()));

                // Dispatch the Type
                this.statement.accept(visitor);

                // Dispatch the Guard Statement
                visitor.visitGuardStatement(this);

                // Close the Context
                visitor.setContext(this.closeContext());

            }

            /// --------------
            /// Public Methods

            /**
             * <p>Returns the {@link Statement} instance contained in the {@link Guard}.</p>
             * @return {@link Statement} instance contained in the {@link Guard}.
             * @since 1.0.0
             * @see Statement
             */
            public final Statement getStatement() {

                return this.statement;

            }

            /**
             * <p>Returns the {@link Expression} specified in the {@link Statement}, if any.</p>
             * @return The {@link Expression} specified in the {@link Statement}, if any.
             * @since 1.0.0
             * @see Expression
             * @see Statement
             */
            public final Expression getExpression() {

                return (this.statement instanceof ExpressionStatement)
                        ? ((ExpressionStatement) this.statement).getExpression() : null;

            }

        }
    }

}
