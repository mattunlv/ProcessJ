package org.processj.compiler.phases.phase;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.SymbolMap.Context;

public class ReachabilityChecker extends Phase {

    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link Listener}.</p>
     *
     * @param listener The {@link Listener} to bind to the {@link Phase}.
     * @since 0.1.0
     */
    public ReachabilityChecker(Listener listener) {
        super(listener);
    }


    /**
     * <p>Asserts that the {@link BreakStat} is not enclosed in a parallel {@link Context} & is enclosed
     * in a breakable {@link Context}.</p>
     * @param breakStatement The {@link BreakStat} to validate.
     * @throws Error If the {@link BreakStat} is enclosed in a parallel or non-breakable {@link Context}.
     */
    @Override
    public final Void visitBreakStat(final BreakStat breakStatement) throws Error {

        // TODO: Check that if the Break Statement is in a loop, it's not in a Switch Statement
        // Assert the Break Statement is enclosed in a Breakable Context
        ReachabilityAssert.EnclosingIterativeContextBreaksAndReachable(this, breakStatement);

        return null;

    }

    /**
     * <p>Asserts that the {@link ContinueStat} is not enclosed in a parallel {@link Context}
     * & is enclosed in an iterative {@link Context}.</p>
     * @param continueStatement The {@link ContinueStat} to validate.
     * @throws Error If the {@link ContinueStat} is enclosed in a parallel or non-iterative {@link Context}.
     */
    @Override
    public final Void visitContinueStat(final ContinueStat continueStatement) throws Error {

        // Assert the Continue Statement is enclosed in a Breakable Context
        ReachabilityAssert.EnclosingIterativeContextBreaksAndReachable(this, continueStatement);

        // TODO: Mark Loop as having ContinueStat?

        return null;

    }

    /**
     * <p>Asserts that both branches of the {@link IfStat} are reachable.</p>
     * @param ifStatement The {@link IfStat} to validate.
     * @throws Error If the one of the {@link IfStat}'s branches are unreachable.
     */
    @Override
    public final Void visitIfStat(final IfStat ifStatement) throws Error {

        // Assert the Continue Statement is enclosed in a Breakable Context
        ReachabilityAssert.EnclosingIterativeContextBreaksAndReachable(this, ifStatement);

        // TODO: Mark Loop as having ContinueStat?

        return null;

    }

    /**
     * <p>Asserts that the {@link ParBlock} is not empty.</p>
     * @param parBlock The {@link ParBlock} to validate.
     * @throws Error If the {@link ParBlock} is empty.
     */
    @Override
    public final Void visitParBlock(final ParBlock parBlock) throws Error {

        // Assert the Par Block is not empty
        SemanticAssert.NotEmptyParallelContext(this);

        return null;

    }

    /**
     * <p>Asserts that the {@link ReturnStat} is not enclosed in a parallel or choice {@link Context}.</p>
     * @param returnStatement The {@link ReturnStat} to validate.
     * @throws Error If the {@link ReturnStat} is enclosed in a parallel or choice {@link Context}.
     */
    @Override
    public final Void visitReturnStat(final ReturnStat returnStatement) throws Error {

        // Assert the Return Statement is not enclosed in a parallel or choice Context
        ReachabilityAssert.NotEnclosedInParallelOrChoiceContext(this, returnStatement);

        // TODO: Mark loop as having a Return Statement?

        return null;

    }

}
