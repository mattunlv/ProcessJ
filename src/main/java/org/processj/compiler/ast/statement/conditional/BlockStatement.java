package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.statement.BarrierSet;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.statement.Statements;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;
import org.processj.compiler.ast.Context;

import java.util.Iterator;

/**
 * <p>Class that encapsulates an ordered set of {@link Statement}s with a bound {@link SymbolMap}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see Statement
 * @see BreakableContext
 * @see Iterable
 */
public class BlockStatement extends Statement implements BreakableContext, Iterable<Statement> {

    /// --------------
    /// Private Fields

    /**
     * <p>The set of {@link Statements} contained in the {@link BlockStatement}.</p>
     * @since 1.0.0
     * @see Statements
     */
    private final Statements body       ;

    /**
     * <p>The {@link Statements} instance to be used as a buffer to hold {@link Statement}s merged from inner
     * {@link Context}s.</p>
     * @since 1.0.0
     * @see Statements
     * @see Statement
     * @see Context
     */
    private final Statements mergeBody  ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link BlockStatement} with the specified {@link Statement} & {@link BarrierSet}.
     * If the {@link Statement} is a {@link BlockStatement}, this binds the {@link BlockStatement}'s
     * {@link BlockStatement#body} & {@link BlockStatement#mergeBody} handles to the specified {@link BlockStatement}'s
     * handles; otherwise new instances of {@link BlockStatement#body} & {@link BlockStatement#mergeBody} are created.
     * </p>
     * @param barrierSet The set of barrier {@link org.processj.compiler.ast.expression.Expression}s enrolled on the
     *                   {@link BlockStatement}.
     * @param statement The {@link Statement} to initialize the {@link BlockStatement} with.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link BlockStatement}.
     * @since 1.0.0
     * @see Statement
     */
    public BlockStatement(final BarrierSet barrierSet,
                          final Statement statement,
                          final Token... tokens) {
        super(barrierSet, tokens);

        if(statement instanceof BlockStatement) {

            this.body       = ((BlockStatement) statement).body         ;
            this.mergeBody  = ((BlockStatement) statement).mergeBody    ;

        } else {

            this.body       = new Statements(statement);
            this.mergeBody  = new Statements();

        }

    }

    /**
     * <p>Initializes the {@link BlockStatement} with the specified {@link Statement}. If the {@link Statement}
     * is a {@link BlockStatement}, this binds the {@link BlockStatement}'s {@link BlockStatement#body} &
     * {@link BlockStatement#mergeBody} handles to the specified {@link BlockStatement}'s handles; otherwise new
     * instances of {@link BlockStatement#body} & {@link BlockStatement#mergeBody} are created.</p>
     * @param statement The {@link Statement} to initialize the {@link BlockStatement} with.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link BlockStatement}.
     * @since 1.0.0
     * @see Statement
     */
    public BlockStatement(final Statement statement,
                          final Token... tokens) {
        super(tokens);

        if(statement instanceof BlockStatement) {

            this.body       = ((BlockStatement) statement).body;
            this.mergeBody  = ((BlockStatement) statement).mergeBody    ;

        } else {

            this.body       = new Statements(statement);
            this.mergeBody  = new Statements();

        }

    }

    /**
     * <p>Initializes the {@link BlockStatement} with the specified {@link Statements}. </p>
     * @param statements The {@link Statement} to initialize the {@link BlockStatement} with.
     * @since 1.0.0
     * @see Statement
     */
    public BlockStatement(final Statements statements) {
        super(statements);

        this.body = new Statements();
        this.mergeBody = new Statements();

        this.body.appendAllFrom(statements);

    }

    /**
     * <p>Initializes the {@link BlockStatement} to its' default state with new instances of
     * {@link BlockStatement#body} & {@link BlockStatement#mergeBody}.</p>
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link BlockStatement}.
     * @since 1.0.0
     */
    public BlockStatement(final Token... tokens) {
        super(tokens);

        this.body       = new Statements();
        this.mergeBody  = new Statements();

    }

    /// --------
    /// Iterable

    /**
     * <p>Returns the {@link Iterator} instance that allows the {@link BlockStatement} instance to be iterated using an
     * enhanced for-loop.</p>
     * @return The {@link Iterator} instance to iterate the {@link BlockStatement} instance's {@link Statement} children.
     * @since 1.0.0
     * @see Iterable
     * @see Iterator
     * @see Statement
     */
    @Override
    public final Iterator<Statement> iterator() {

        return this.body.iterator();

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context} & scope, and dispatches the
     * {@link Visitor} to the evaluation {@link Expression} via {@link Expression#accept(Visitor)}, the
     * {@link BlockStatement} via {@link Visitor#visitBlockStatement(BlockStatement)}, & {@link Statement} children
     * via {@link Statement#accept(Visitor)} before updating the {@link org.processj.compiler.ast.Context} to the
     * enclosing {@link Context}.
     * @since 1.0.0
     * @see Visitor
     * @see Statement
     * @see Phase.Error
     */
    @Override
    public void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the Block Statement
        this.openScope();

        // Dispatch the Block Statement
        visitor.visitBlockStatement(this);

        // Dispatch the children
        this.body.accept(visitor);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    /// -----------------
    /// Protected Methods

    /**
     * <p>Returns the {@link Statements} instance that represents the {@link BlockStatement}'s body.</p>
     * @return The {@link Statements} instance that represents the {@link BlockStatement}'s body.
     * @since 1.0.0
     * @see Statements
     */
    public final Statements getBody() {

        return this.body;

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Clears the managed set of {@link Statements} of all handles corresponding to the each of the
     * {@link BlockStatement}'s, {@link Statement} children.</p>
     * @since 1.0.0
     * @see Statement
     * @see Statements
     */
    public final void clear() {

        this.body.clear();
        this.mergeBody.clear();

    }

    /**
     * <p>Returns a flag indicating if the {@link BlockStatement} instance contains any {@link Statement} children.</p>
     * @return A flag indicating if the {@link BlockStatement} instance contains any {@link Statement} children.
     * @since 1.0.0
     * @see Statement
     * @see Statements
     */
    public final boolean isEmpty() {

        return this.body.isEmpty();

    }

    /**
     * <p>Returns an integer value corresponding to the amount of {@link Statement} children contained in the
     * {@link BlockStatement} instance.</p>
     * @return an integer value corresponding to the amount of {@link Statement} children contained in the
     * {@link BlockStatement} instance.
     * @since 1.0.0
     * @see Statements
     */
    public final int size() {

        return this.body.size();

    }

    /**
     * <p>Returns the first {@link Statement} instance contained in the {@link BlockStatement} instance, if any.</p>
     * @return The first {@link Statement} instance contained in the {@link BlockStatement} instance, if any.
     * @since 1.0.0
     * @see Statement
     */
    public final Statement getFirst() {

        return this.body.getFirst();

    }

    /**
     * <p>Returns the last {@link Statement} instance contained in the {@link BlockStatement} instance, if any.</p>
     * @return The last {@link Statement} instance contained in the {@link BlockStatement} instance, if any.
     * @since 1.0.0
     * @see Statement
     */
    public final Statement getLast() {

        return this.body.getLast();

    }

    /**
     * <p>Retrieves the {@link Statement} at the specified index.</p>
     * @param index The index corresponding to the desired child {@link Statement} to retrieve.
     * @return {@link Statement} instance.
     * @since 1.0.0
     * @see Statement
     */
    public final Statement statementAt(final int index) {

        return this.body.child(index);

    }

    /**
     * <p>Appends the {@link Statement} as the next element into the managed set of {@link Statements}
     * & returns a reference to the target {@link BlockStatement} instance.</p>
     * @param statement The {@link Statement} to append to the end of the {@link Statements}.
     * @return Reference to the target {@link BlockStatement} instance.
     * @since 1.0.0
     * @see Statement
     * @see Statements
     */
    public final BlockStatement append(final Statement statement) {

        this.body.append(statement);

        return this;

    }

    /**
     * <p>Appends the entire collection of {@link Statements} from the specified {@link BlockStatement} to the end of
     * the {@link BlockStatement} instance & returns a reference to the mutated {@link BlockStatement} instance.</p>
     * @param blockStatement The {@link BlockStatement} instance to append the {@link Statement} children from.
     * @return Reference to the mutated {@link BlockStatement} instance.
     * @since 1.0.0
     * @see Statements
     */
    public final BlockStatement appendAllFrom(final BlockStatement blockStatement) {

        this.body.appendAllFrom(blockStatement.body);

        return this;

    }

    /**
     * <p>Appends the {@link Statement} as the next element into the {@link BlockStatement}'s managed
     * {@link Statements} & returns a reference to the target {@link BlockStatement} instance.</p>
     * @param statement The {@link Statement} to append to the end of the {@link BlockStatement}'s managed
     * {@link Statements}.
     * @return Reference to the target {@link BlockStatement} instance.
     * @since 1.0.0
     * @see Statement
     * @see Statements
     */
    public final BlockStatement appendToMergeBody(final Statement statement) {

        this.mergeBody.append(statement);

        return this;

    }

    /**
     * <p>Appends the entire collection of {@link Statement}s from the specified {@link BlockStatement} to the end of
     * the {@link BlockStatement}'s {@link Statements} instance & returns a reference to the mutated
     * {@link BlockStatement} instance.</p>
     * @param blockStatement The {@link BlockStatement} instance to append the {@link Statement} children from.
     * @return Reference to the mutated {@link BlockStatement} instance.
     * @since 1.0.0
     * @see Statement
     * @see Statements
     */
    public final BlockStatement appendAllToMergeBodyFrom(final BlockStatement blockStatement) {

        this.mergeBody.appendAllFrom(blockStatement.body);

        return this;

    }

}