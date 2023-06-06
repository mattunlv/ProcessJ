package org.processj.compiler.phases.phase;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.alt.AltStat;
import org.processj.compiler.ast.Block;
import org.processj.compiler.ast.BreakStat;
import org.processj.compiler.ast.ChannelWriteStat;
import org.processj.compiler.ast.ClaimStat;
import org.processj.compiler.ast.ContinueStat;
import org.processj.compiler.ast.DoStat;
import org.processj.compiler.ast.ExprStat;
import org.processj.compiler.ast.ForStat;
import org.processj.compiler.ast.IfStat;
import org.processj.compiler.ast.LocalDecl;
import org.processj.compiler.ast.LoopStatement;
import org.processj.compiler.ast.ParBlock;
import org.processj.compiler.ast.ReturnStat;
import org.processj.compiler.ast.SkipStat;
import org.processj.compiler.ast.StopStat;
import org.processj.compiler.ast.SuspendStat;
import org.processj.compiler.ast.SwitchGroup;
import org.processj.compiler.ast.SwitchStat;
import org.processj.compiler.ast.SyncStat;
import org.processj.compiler.ast.TimeoutStat;
import org.processj.compiler.ast.WhileStat;
import org.processj.compiler.utilities.PJBugManager;
import org.processj.compiler.utilities.Log;
import org.processj.compiler.utilities.PJMessage;
import org.processj.compiler.phases.phase.Visitor;
import org.processj.compiler.utilities.VisitorMessageNumber;

/**
 * If a visit returns 'true', then is it because the code
 * represented by that node can sometimes run to completion,
 * i.e., never _always_ returns or breaks or continues.
 */

public class Reachability implements Visitor<Boolean> {
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
        if (is.evaluationExpression().isConstant() && ((Boolean) is.evaluationExpression().constantValue())
                && is.getElseBody() != null)
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(is)
                    .addError(VisitorMessageNumber.REACHABILITY_800)
                    .build());
        // if (false) S1 ... - S1 is unreachable
        if (is.evaluationExpression().isConstant() && (!(Boolean) is.evaluationExpression().constantValue()))
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(is)
                    .addError(VisitorMessageNumber.REACHABILITY_801)
                    .build());
        boolean thenBranch = true;
        boolean elseBranch = true;
        try {
            thenBranch = is.getThenStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        if (is.getElseBody() != null)
            try {
                elseBranch = is.getElseBody().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        return thenBranch || elseBranch;
    }

    // DONE
    public Boolean visitWhileStat(WhileStat ws) {
        Log.log(ws, "Visiting a while-statement.");
        LoopStatement oldLoopConstruct = loopConstruct;
        loopConstruct = ws;

        boolean b = false; // Only the compiler will know
        try {
            b = ws.getStatements() != null ? ws.getStatements().visit(this) : true;
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        if (ws.getEvaluationExpression().isConstant() && ((Boolean) ws.getEvaluationExpression().constantValue())
                && ((b && // the statement can run to completion
                !ws.hasBreak && !ws.hasReturn) // but has no breaks, so it will loop forever
                || !b)) {
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(ws.getEvaluationExpression())
                    .addError(VisitorMessageNumber.REACHABILITY_802)
                    .addCodeAnalysis("statement may not run to completion")
                    .build());
            ws.foreverLoop = true;
            loopConstruct = oldLoopConstruct;
            return false;
        }

        if (ws.getEvaluationExpression() != null && ws.getEvaluationExpression().isConstant()
                && (!(Boolean) ws.getEvaluationExpression().constantValue())) {
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

        boolean b = false;
        try {
            b = ds.getStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

        if (ds.getEvaluationExpression().isConstant() && ((Boolean) ds.getEvaluationExpression().constantValue())
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

    public Boolean visitAltStat(AltStat as) throws Phase.Error {
        Log.log(as, "Visiting a alt statement.");
        Visitor.super.visitAltStat(as);
        return true;
    }

    // DONE
    public Boolean visitBlock(Block bl) {
        Log.log(bl, "Visiting a block." + bl.getStatements().size());
        boolean canFinish = true;
        boolean b = true;
        for (int i = 0; i < bl.getStatements().size(); i++) {
            if (bl.getStatements().child(i) != null) {
                Log.log("visiting child: " + i);
                try {
                    b = bl.getStatements().child(i).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                Log.log("visiting child: " + i + " done");
                if (!b && bl.getStatements().size() - 1 > i) {
                    PJBugManager.INSTANCE.reportMessage(
                            new PJMessage.Builder()
                            .addAST(bl.getStatements().child(i))
                            .addError(VisitorMessageNumber.REACHABILITY_803)
                            .addArguments(bl.getStatements().child(i).line)
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
        if (fs.getEvaluationExpression() != null && fs.getEvaluationExpression().isConstant()
                && (!(Boolean) fs.getEvaluationExpression().constantValue())) {
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(fs)
                    .addError(VisitorMessageNumber.REACHABILITY_804)
                    .build());
            loopConstruct = oldLoopConstruct;
            return true;
        }

        boolean b = true;
        if (fs.getStatements() != null)
            try {
                b = fs.getStatements().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }

        // for (... ; true; ...) S1
        if ((fs.getEvaluationExpression() == null || (fs.getEvaluationExpression().isConstant() && ((Boolean) fs
                .getEvaluationExpression().constantValue()))) && (b && // the statement can run to completion
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
        if (pb.getStatements().size() == 0)
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(pb)
                    .addError(VisitorMessageNumber.REACHABILITY_813)
                    .build());
        inParBlock = true;
        try {
            pb.getStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
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
        try {
            return sg.getStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
    }

    public Boolean visitSwitchStat(SwitchStat ss) {
        Log.log(ss, "Visiting a switch-statement.");
        boolean oldInsideSwitch = insideSwitch;
        insideSwitch = true;
        SwitchStat oldSwitchConstruct = switchConstruct;
        switchConstruct = ss;
        try {
            ss.switchBlocks().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
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