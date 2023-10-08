package org.processj.compiler.ast.statement;

import org.processj.compiler.ast.AST;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;
import org.processj.compiler.ast.expression.Expression;

import java.util.*;

/**
 * <p>Class that encapsulates an ordered {@link Map} of barrier {@link Expression}s.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see Map
 * @see Expression
 * @see Map.Entry
 */
public class BarrierSet extends AST implements Iterable<Map.Entry<Expression, Integer>> {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Map} of {@link Expression}-{@link Integer} quantity pairs managed by the {@link BarrierSet}
     * instance.</p>
     * @since 1.0.0
     * @see Map
     * @see Expression
     * @see Integer
     */
    private final Map<Expression, Integer> barriers;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link BarrierSet} instance to its' default state as an empty sequence.</p>
     * @since 1.0.0
     * @see Expression
     */
    public BarrierSet() {
        super();

        this.barriers = new HashMap<>();

    }

    /**
     * <p>Initializes the {@link BarrierSet} instance to its' default state with the specified {@link Expression} as
     * the initial {@link Expression}.</p>
     * @since 1.0.0
     * @see Expression
     */
    public BarrierSet(final Expression expression) {
        super(expression);

        this.barriers = new HashMap<>();

        this.append(expression);

    }

    /// --------
    /// Iterable

    /**
     * <p>Returns the {@link Iterator} instance that allows the {@link BarrierSet} instance to be iterated using an
     * enhanced for-loop.</p>
     * @return The {@link Iterator} instance to iterate the {@link BarrierSet} instance {@link Expression} barriers.
     * @since 1.0.0
     * @see Iterable
     * @see Iterator
     * @see Expression
     * @see Integer
     * @see Map
     * @see Map.Entry
     */
    @Override
    public final Iterator<Map.Entry<Expression, Integer>> iterator() {

        return this.barriers.entrySet().iterator();

    }

    /// ---
    /// AST

    /**
     * <p>Iterates through the contained {@link Expression}s & dispatches the specified {@link Visitor} via
     * {@link Expression#accept(Visitor)}.</p>
     * @param visitor The {@link Visitor} to dispatch to each contained {@link Expression}.
     * @throws Phase.Error If {@link Expression#accept(Visitor)} failed.
     * @since 1.0.0
     * @see Visitor
     * @see Expression
     * @see Phase.Error
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        for(final Map.Entry<Expression, Integer> entry: this)
            entry.getKey().accept(visitor);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Clears the managed {@link Map} of all handles corresponding to the barrier {@link Expression}s.</p>
     * @since 1.0.0
     * @see Map
     * @see Expression
     */
    public final void clear() {

        this.barriers.clear();

    }

    /**
     * <p>Returns a flag indicating if the {@link BarrierSet} instance contains any barrier {@link Expression}.</p>
     * @return A flag indicating if the {@link BarrierSet} instance contains any barrier {@link Expression}.
     * @since 1.0.0
     * @see Expression
     */
    public final boolean isEmpty() {

        return this.barriers.isEmpty();

    }

    /**
     * <p>Maps the {@link Expression} as to the amount of times it has enrolled on the {@link BarrierSet}.</p>
     * @param expression The {@link Expression} to map as a barrier.
     * @return Reference to the target {@link BarrierSet} instance.
     * @since 1.0.0
     * @see Expression
     */
    public final BarrierSet append(final Expression expression) {

        // TODO: Might have to set default value to -1 because of ParBlock Collapse
        this.barriers.put(expression, this.barriers.getOrDefault(expression, 0) + 1);

        return this;

    }

    /**
     * <p>Appends the entire collection of barrier {@link Expression}s from the specified {@link BarrierSet}& returns a
     * reference to the mutated {@link BarrierSet} instance.</p>
     * @param barrierSet The {@link BarrierSet} instance to append the barrier {@link Expression}s from.
     * @return Reference to the mutated {@link BarrierSet} instance.
     * @since 1.0.0
     * @see Expression
     */
    public final BarrierSet appendAllFrom(final BarrierSet barrierSet) {

        barrierSet.barriers.forEach((expression, frequency) -> this.append(expression));

        return this;

    }

}