package org.processj.reachability;

import org.processj.ast.AltStat;
import org.processj.ast.Block;
import org.processj.ast.BreakStat;
import org.processj.ast.ChannelWriteStat;
import org.processj.ast.ClaimStat;
import org.processj.ast.ContinueStat;
import org.processj.ast.DoStat;
import org.processj.ast.ExprStat;
import org.processj.ast.ForStat;
import org.processj.ast.IfStat;
import org.processj.ast.LocalDecl;
import org.processj.ast.LoopStatement;
import org.processj.ast.ParBlock;
import org.processj.ast.ReturnStat;
import org.processj.ast.SkipStat;
import org.processj.ast.StopStat;
import org.processj.ast.SuspendStat;
import org.processj.ast.SwitchGroup;
import org.processj.ast.SwitchStat;
import org.processj.ast.SyncStat;
import org.processj.ast.TimeoutStat;
import org.processj.ast.WhileStat;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.Log;
import org.processj.utilities.PJMessage;
import org.processj.utilities.Visitor;
import org.processj.utilities.VisitorMessageNumber;

/**
 * If a visit returns 'true', then is it because the code
 * represented by that node can sometimes run to completion,
 * i.e., never _always_ returns or breaks or continues.
 */

public class Reachability extends Visitor<Boolean> {
    boolean insideSwitch = false;
    LoopStatement loopConstruct = null;
    SwitchStat switchConstruct = null;
    boolean inParBlock = false;

    public Reachability() {
        Log.logHeader("****************************************");
        Log.logHeader("*        R E A C H A B I L I T Y       *");
        Log.logHeader("****************************************");
    }

    // DONE
    public Boolean visitIfStat(IfStat is) {
        Log.log(is, "Visiting an if-Statement.");
        // if (true) S1 else S2 - S2 is unreachable
        if (is.expr().isConstant() && ((Boolean) is.expr().constantValue())
                && is.elsepart() != null)
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(is)
                    .addError(VisitorMessageNumber.REACHABILITY_800)
                    .build());
        // if (false) S1 ... - S1 is unreachable
        if (is.expr().isConstant() && (!(Boolean) is.expr().constantValue()))
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(is)
                    .addError(VisitorMessageNumber.REACHABILITY_801)
                    .build());
        boolean thenBranch = true;
        boolean elseBranch = true;
        thenBranch = is.thenpart().visit(this);
        if (is.elsepart() != null)
            elseBranch = is.elsepart().visit(this);
        return thenBranch || elseBranch;
    }

    // DONE
    public Boolean visitWhileStat(WhileStat ws) {
        Log.log(ws, "Visiting a while-statement.");
        LoopStatement oldLoopConstruct = loopConstruct;
        loopConstruct = ws;

        boolean b = ws.stat() != null ? ws.stat().visit(this) : true; // Only the compiler will know
        if (ws.expr().isConstant() && ((Boolean) ws.expr().constantValue())
                && ((b && // the statement can run to completion
                !ws.hasBreak && !ws.hasReturn) // but has no breaks, so it will loop forever
                || !b)) {
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(ws.expr())
                    .addError(VisitorMessageNumber.REACHABILITY_802)
                    .addCodeAnalysis("statement may not run to completion")
                    .build());
            ws.foreverLoop = true;
            loopConstruct = oldLoopConstruct;
            return false;
        }

        if (ws.expr() != null && ws.expr().isConstant()
                && (!(Boolean) ws.expr().constantValue())) {
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(ws)
                    .addError(VisitorMessageNumber.REACHABILITY_810)
                    .build());
            loopConstruct = oldLoopConstruct;
            return true;
        }

        loopConstruct = oldLoopConstruct;
        if (ws.hasReturn && !b)
            return false;
        return true;
    }

    // DONE
    public Boolean visitDoStat(DoStat ds) {
        Log.log(ds, "Visiting a do-statement.");
        LoopStatement oldLoopConstruct = loopConstruct;
        loopConstruct = ds;

        boolean b = ds.stat().visit(this);

        if (ds.expr().isConstant() && ((Boolean) ds.expr().constantValue())
                && (b && // the statement can run to completion
                !ds.hasBreak && !ds.hasReturn) || !b) { // but has no breaks, so it will loop forever
            loopConstruct = oldLoopConstruct;
            ds.foreverLoop = true;
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(ds)
                    .addError(VisitorMessageNumber.REACHABILITY_809)
                    .build());
            return false;
        }
        loopConstruct = oldLoopConstruct;
        return true;
    }

    public Boolean visitAltStat(AltStat as) {
        Log.log(as, "Visiting a alt statement.");
        super.visitAltStat(as);
        return true;
    }

    // DONE
    public Boolean visitBlock(Block bl) {
        Log.log(bl, "Visiting a block." + bl.stats().size());
        boolean canFinish = true;
        boolean b = true;
        for (int i = 0; i < bl.stats().size(); i++) {
            if (bl.stats().child(i) != null) {
                Log.log("visiting child: " + i);
                b = bl.stats().child(i).visit(this);
                Log.log("visiting child: " + i + " done");
                if (!b && bl.stats().size() - 1 > i) {
                    PJBugManager.INSTANCE.reportMessage(
                            new PJMessage.Builder()
                            .addAST(bl.stats().child(i))
                            .addError(VisitorMessageNumber.REACHABILITY_803)
                            .addArguments(bl.stats().child(i).line)
                            .build());
                    canFinish = false;
                }
            }
        }
        if (!b)
            canFinish = b;
        return canFinish;
    }

    // DONE
    public Boolean visitForStat(ForStat fs) {
        Log.log(fs, "Visiting a for-statement.");
        LoopStatement oldLoopConstruct = loopConstruct;
        loopConstruct = fs;

        // for (....; false ; ....) S1
        if (fs.expr() != null && fs.expr().isConstant()
                && (!(Boolean) fs.expr().constantValue())) {
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(fs)
                    .addError(VisitorMessageNumber.REACHABILITY_804)
                    .build());
            loopConstruct = oldLoopConstruct;
            return true;
        }

        boolean b = true;
        if (fs.stats() != null)
            b = fs.stats().visit(this);

        // for (... ; true; ...) S1
        if ((fs.expr() == null || (fs.expr().isConstant() && ((Boolean) fs
                .expr().constantValue()))) && (b && // the statement can run to completion
                !fs.hasBreak && !fs.hasReturn) || !b) // but has no breaks, so it will loop forever
        {
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(fs)
                    .addError(VisitorMessageNumber.REACHABILITY_805)
                    .build());
            fs.foreverLoop = true;
            loopConstruct = oldLoopConstruct;
            return false;
        }
        loopConstruct = oldLoopConstruct;
        return true;
    }

    // AltStat
    // just visit the children - default implementation

    // DONE
    public Boolean visitBreakStat(BreakStat bs) {
        Log.log(bs, "Visiting a break-statement.");
        if (inParBlock)
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(bs)
                    .addError(VisitorMessageNumber.REACHABILITY_808)
                    .build());

        if (loopConstruct == null && switchConstruct == null) {
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(bs)
                    .addError(VisitorMessageNumber.REACHABILITY_806)
                    .build());
            return true; // this break doesn't matter cause it can't be here anyways!
        }
        if (loopConstruct != null && !insideSwitch)
            loopConstruct.hasBreak = true;
        return false;
    }

    // DONE
    public Boolean visitChannelWriteStat(ChannelWriteStat cws) {
        Log.log(cws, "Visiting a channel-write-statement.");
        return true;
    }

    // DONE
    public Boolean visitClaimStat(ClaimStat cs) {
        Log.log(cs, "Visiting a claim-statement.");
        return true;
    }

    // DONE
    public Boolean visitContinueStat(ContinueStat cs) {
        Log.log(cs, "Visiting a continue-statement.");
        if (inParBlock)
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(cs)
                    .addError(VisitorMessageNumber.REACHABILITY_811)
                    .build());
        if (loopConstruct == null) {
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(cs)
                    .addError(VisitorMessageNumber.REACHABILITY_812)
                    .build());
            return true; // this continue doesn't matter cause it can't be here anyways!
        }
        if (loopConstruct != null)
            loopConstruct.hasContinue = true;
        return false;
    }

    // DONE
    public Boolean visitLocalDecl(LocalDecl ld) {
        Log.log(ld, "Visiting a local-decl-statement.");
        return true;
    }

    // DONE
    public Boolean visitParBlock(ParBlock pb) {
        Log.log(pb, "Visiting a par statement.");
        boolean oldInParBlock = inParBlock;
        /* Warning generated for having an empty par-block */
        if (pb.stats().size() == 0)
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(pb)
                    .addError(VisitorMessageNumber.REACHABILITY_813)
                    .build());
        inParBlock = true;
        pb.stats().visit(this);
        inParBlock = oldInParBlock;
        return true;
    }

    // DONE
    public Boolean visitReturnStat(ReturnStat rs) {
        Log.log(rs, "Visiting a return-statement.");
        if (inParBlock)
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(rs)
                    .addError(VisitorMessageNumber.REACHABILITY_807)
                    .build());
        if (loopConstruct != null)
            loopConstruct.hasReturn = true;
        return false;
    }

    // DONE
    public Boolean visitSkipStat(SkipStat ss) {
        Log.log(ss, "Visiting a skip-statement.");
        return true;
    }

    // DONE
    public Boolean visitStopStat(StopStat ss) {
        Log.log(ss, "Visiting a stop-statement.");
        return false;
    }

    // DONE
    public Boolean visitSuspendStat(SuspendStat ss) {
        Log.log(ss, "Visiting a suspend-statement.");
        return true;
    }

    public Boolean visitSwitchGroup(SwitchGroup sg) {
        return new Block(sg.statements()).visit(this);
    }

    public Boolean visitSwitchStat(SwitchStat ss) {
        Log.log(ss, "Visiting a switch-statement.");
        boolean oldInsideSwitch = insideSwitch;
        insideSwitch = true;
        SwitchStat oldSwitchConstruct = switchConstruct;
        switchConstruct = ss;
        ss.switchBlocks().visit(this);
        // TODO finish this!
        insideSwitch = oldInsideSwitch;
        switchConstruct = oldSwitchConstruct;
        return true;
    }

    // DONE
    public Boolean visitSyncStat(SyncStat ss) {
        Log.log(ss, "Visiting a while-statement.");
        return true;
    }

    // DONE
    public Boolean visitTimeoutStat(TimeoutStat ts) {
        Log.log(ts, "Visiting a timeout-statement.");
        return true;
    }

    // DONE
    public Boolean visitExprStat(ExprStat es) {
        Log.log(es, "Visiting an expr-statement.");
        return true;
    }
}