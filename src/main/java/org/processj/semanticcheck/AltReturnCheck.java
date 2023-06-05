package org.processj.semanticcheck;

import org.processj.Phase;
import org.processj.ast.alt.AltCase;
import org.processj.ast.alt.AltStat;
import org.processj.ast.Block;
import org.processj.ast.DoStat;
import org.processj.ast.ForStat;
import org.processj.ast.IfStat;
import org.processj.ast.ReturnStat;
import org.processj.ast.Sequence;
import org.processj.ast.Statement;
import org.processj.ast.WhileStat;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.PJMessage;
import org.processj.utilities.VisitorMessageNumber;

public class AltReturnCheck extends Phase {
    
    boolean inAltStat = false;

    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link Listener}.</p>
     *
     * @param listener The {@link Listener} to bind to the {@link Phase}.
     * @since 0.1.0
     */
    public AltReturnCheck(Phase.Listener listener) {
        super(listener);
    }

    @Override
    public final Void visitAltStat(AltStat as) {
        inAltStat = true;
        Sequence<AltCase> se = as.body();
        if (se != null)
            for (AltCase ac : se)
                try {
                    ac.visit(this);
                } catch (org.processj.Phase.Error error) {
                    throw new RuntimeException(error);
                }
        return null;
    }
    
    @Override
    public final Void visitAltCase(AltCase ac) {
        if (ac.getStatement() != null)
            try {
                ac.getStatement().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }
        return null;
    }
    
    @Override
    public final Void visitReturnStat(ReturnStat rs) {
        if (inAltStat) {
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(rs)
                    .addError(VisitorMessageNumber.SEMATIC_CHECKS_903)
                    .build());
        }
        return null;
    }
    
    @Override
    public final Void visitBlock(Block b) {
        for (Statement st : b.stats())
            try {
                st.visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }
        return null;
    }
    
    @Override
    public final Void visitWhileStat(WhileStat ws) {
        if (ws.getStatement() != null)
            try {
                ws.getStatement().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }
        return null;
    }
    
    @Override
    public final Void visitDoStat(DoStat ds) {
        if (ds.getStatement() != null)
            try {
                ds.getStatement().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }
        return null;
    }
    
    @Override
    public final Void visitForStat(ForStat fs) {
        if (fs.getStatement() != null)
            try {
                fs.getStatement().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }
        return null;
    }
    
    @Override
    public final Void visitIfStat(IfStat is) {
        if (is.getThenPart() != null)
            try {
                is.getThenPart().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }
        if (is.getElsePart() != null)
            try {
                is.getElsePart().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }
        return null;
    }
}
