package org.processj.compiler.ast.statement.conditional;

import java.util.*;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.BarrierSet;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.statement.Statements;
import org.processj.compiler.ast.statement.yielding.YieldingContext;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a block of concurrently executed {@link Statement}s.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see BlockStatement
 * @see ParBlock
 * @see YieldingContext
 * @see Iterable
 */
public class ParBlock extends BlockStatement implements YieldingContext, Iterable<Statement> {

    /// --------------
    /// Private Fields

    /**
     * <p>The set of {@link String} corresponding to names that are currently reading for a given time point.</p>
     * @since 1.0.0
     * @see Set
     * @see String
     */
    private final Set<String>   readSet                 ;

    /**
     * <p>The set of {@link String} corresponding to names that are currently writing for a given time point.</p>
     * @since 1.0.0
     * @see Set
     * @see String
     */
    private final Set<String>   writeSet                ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ParBlock} to its' default state with the specified {@link Statement} body &
     * {@link BarrierSet}.</p>
     * @param body The set of {@link Statements} composing the {@link ParBlock}'s body.
     * @param barrierSet The {@link BarrierSet} of barrier {@link Expression}s corresponding to the {@link ParBlock}.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link ParBlock}.
     * @since 1.0.0
     * @see Statement
     * @see Expression
     */
    public ParBlock(final Statement body, final BarrierSet barrierSet, final Token... tokens) {
        super(barrierSet, body, tokens);

        this.readSet    = new LinkedHashSet<>();
        this.writeSet   = new LinkedHashSet<>();

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context} & scope, and dispatches the
     * {@link Visitor} to the evaluation {@link Expression} via {@link Expression#accept(Visitor)}, the
     * {@link ParBlock} via {@link Visitor#visitParBlockStatement(ParBlock)}, & {@link Statement} children
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

        // Dispatch the ParBlock
        visitor.visitParBlockStatement(this);

        // Dispatch the children
        this.getBody().accept(visitor);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the specified {@link Expression} is in the {@link ParBlock}'s read set.</p>
     * @param expression The {@link Expression} to check for membership in the {@link ParBlock}'s read set.
     * @since 1.0.0
     * @see Expression
     */
    public final boolean inReadSet(final Expression expression) {

        return this.readSet.contains(expression.toString());

    }

    /**
     * <p>Returns a flag indicating if the specified {@link Expression} is in the {@link ParBlock}'s write set.</p>
     * @param expression The {@link Expression} to check for membership in the {@link ParBlock}'s write set.
     * @since 1.0.0
     * @see Expression
     */
    public final boolean inWriteSet(final Expression expression) {

        return this.writeSet.contains(expression.toString());

    }

    /**
     * <p>Aggregates the specified {@link Expression} to the {@link ParBlock}'s read set.</p>
     * @param expression The {@link Expression} to aggregate to the {@link ParBlock}'s read set.
     * @since 1.0.0
     * @see Expression
     */
    public final void toReadSet(final Expression expression) {

        this.readSet.add(expression.toString());

    }

    /**
     * <p>Aggregates the specified {@link Expression} to the {@link ParBlock}'s write set.</p>
     * @param expression The {@link Expression} to aggregate to the {@link ParBlock}'s write set.
     * @since 1.0.0
     * @see Expression
     */
    public final void toWriteSet(final Expression expression) {

        this.writeSet.add(expression.toString());

    }

}