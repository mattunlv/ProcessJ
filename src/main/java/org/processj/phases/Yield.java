package org.processj.phases;

import org.processj.Phase;
import org.processj.ast.*;
import org.processj.ast.alt.AltCase;
import org.processj.ast.alt.AltStat;
import org.processj.ast.expression.ArrayAccessExpr;
import org.processj.ast.expression.ArrayLiteral;
import org.processj.ast.expression.Assignment;
import org.processj.ast.expression.BinaryExpr;
import org.processj.ast.alt.Guard;
import org.processj.utilities.Log;
import org.processj.utilities.Visitor;

/**
 * Adds the annotation [yield=true] to all procs that
 * may issue a org.processj.yield call.
 *
 * @author Matt Pedersen
 */
public class Yield extends Phase {

    /// ----------------------
    /// Private Static Methods

    private static void MarkYieldingContext(final SymbolMap scope) {

        if(scope != null) scope.forEachContext(SymbolMap.Context::setYields);

    }

    public Yield(final Phase.Listener listener) {
        super(listener);

    }

    @Override
    public final Void visitAltCase(AltCase ac) {

        MarkYieldingContext(this.getScope());

        return null;

    }

    @Override
    public final Void visitAltStat(AltStat as) {

        MarkYieldingContext(this.getScope());

        return null;

    }

    @Override
    public final Void visitChannelReadExpr(ChannelReadExpr cr) {

        MarkYieldingContext(this.getScope());

        return null;

    }

    @Override
    public final Void visitChannelWriteStat(ChannelWriteStat cw) {

        MarkYieldingContext(this.getScope());

        return null;

    }

    @Override
    public final Void visitClaimStat(ClaimStat cs) {

        MarkYieldingContext(this.getScope());

        return null;

    }

    @Override
    public final Void visitGuard(Guard gu) {

        MarkYieldingContext(this.getScope());

        return null;

    }

    @Override
    public final Void visitParBlock(ParBlock pb) {

        MarkYieldingContext(this.getScope());

        return null;

    }

    @Override
    public final Void visitSuspendStat(SuspendStat ss) {

        MarkYieldingContext(this.getScope());

        return null;

    }

    @Override
    public final Void visitSyncStat(SyncStat st) {

        MarkYieldingContext(this.getScope());

        return null;

    }

    @Override
    public final Void visitTimeoutStat(TimeoutStat ts) {

        MarkYieldingContext(this.getScope());

        return null;

    }

}