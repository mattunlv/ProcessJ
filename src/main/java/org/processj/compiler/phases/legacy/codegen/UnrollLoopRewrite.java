package org.processj.compiler.phases.legacy.codegen;

import java.util.Hashtable;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.alt.AltCase;
import org.processj.compiler.ast.alt.AltStat;
import org.processj.compiler.phases.phase.sym;
import org.processj.compiler.utilities.Log;
import org.processj.compiler.utilities.PJBugManager;
import org.processj.compiler.phases.phase.Visitor;

/**
 * A re-implementation of the Visitor that traverses the tree to rewrite while,
 * do and for loop constructs in to if-statements with labels and gotos.
 *
 * @author Matt Pedersen
 * @version 10/28/19
 * @since 1.2
 */
public class UnrollLoopRewrite implements Visitor<AST> {

    private int labelNo = 0;

    private int newLabel() {
        return labelNo++;
    }

    // Constants for break/continue outside loops checking
    private final static int OUTSIDE = 0;
    private final static int INSIDE_LOOP = 1;
    private final static int INSIDE_SWITCH = 2;
    // We start by being outside any loops
    private int inside = OUTSIDE;

    // break label
    private int breakLabel;
    // continue label
    private int continueLabel;
    // labels (from labeled statements) to label numbers break table.
    private Hashtable<String, Integer> bls = new Hashtable<String, Integer>();
    // labels (from labeled statements) to label numbers continue table.
    private Hashtable<String, Integer> cls = new Hashtable<String, Integer>();

    private void addLabels(Hashtable<String, Integer> bls, Hashtable<String, Integer> cls,
            String label, int bl, int cl) {
        if (bls.get(label) != null) {
            // TODO: ERROR
            System.out.println("label: '" + label + "' already in scope.");
        } else {
            bls.put(label, bl);
            cls.put(label, cl);
        }
    }

    // Contructs a 'Label(...)' invocation
    private Statement makeLabel(int no, AST a) {
        return new ExprStat(new Invocation(null, new Name("LABEL"), new Sequence<>(new PrimitiveLiteral(
                new Token(sym.INTEGER_LITERAL, "" + no, a.line, a.charBegin, a.charBegin + ("" + no).length() - 1),
                PrimitiveLiteral.IntKind)), true));
    }

    // Constructs a 'Goto(...) invocation.
    private Statement makeGoto(int no, AST a) {
        return new ExprStat(new Invocation(null, new Name("GOTO"), new Sequence<>(new PrimitiveLiteral(
                new Token(sym.INTEGER_LITERAL, "" + no, a.line, a.charBegin, a.charBegin + ("" + no).length() - 1),
                PrimitiveLiteral.IntKind)), true));
    }

    public AST visitAltCase(AltCase ac) {
        Log.log("LoopRewriter:\tVisiting an AltCase");
	// if ac.guard() is null, it is because we have a nested alt.
	if (ac.getGuard() != null)
        try {
            ac.getGuard().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        try {
            ac.children[2] = ac.getStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
    }

    public AST visitAltStat(AltStat as) throws Phase.Error {
        Log.log("LoopRewriter:\tVisiting an AltStat");
        // TODO: Replicated ALT
        Visitor.super.visitAltStat(as);
        return as;
    }

    /*
	 * R[{ S1; S2; ... ;Sn; }](...) = { R[S1](...); R[S2](...); ...; R[Sn](...); }
	 * 
	 * Rewritten loops are wrapped in Blocks with canBeMerged == true, so we flatten
	 * them by merging the statements in the block into the new block.
     */
    public AST visitBlock(Block bl) {

        Log.log("LoopRewriter:\tVisiting a Block");
        Sequence<Statement> stmts = (Sequence<Statement>) bl.getStatements();
        Sequence<Statement> newStmts = new Sequence<Statement>();

        for (int i = 0; i < stmts.size(); i++) {
            if ((Statement) stmts.child(i) != null) {
                AST a = null;
                try {
                    a = stmts.child(i).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (a instanceof Block) {
                    Block b = (Block) a;
                    if (b.canBeMerged) {
                        // block with canBeMerged == true => MERGE into new statements.
                        newStmts.merge(b.getStatements());
                    } else {
                        // otherwise if canBeMerged == false => just append.
                        newStmts.append(b);
                    }
                } else {
                    // Wasn't a block => just append.
                    newStmts.append((Statement) a);
                }
            } else {
                // was null => keep it as null.
                newStmts.append(null);
            }

        }
        // just update the statements in the existing block.
        bl.children[0] = newStmts;
        return bl;
    }

    /*
	 * R[break](bl, cl, bls, cls) = goto(bl); R[break l](bl, cl, bls, cls) =
	 * goto(bls.get(l));
     */
    public AST visitBreakStat(BreakStat bs) {
        Log.log("LoopRewriter:\tVisiting a BreakStat");
        if(inside == OUTSIDE) {

            // TODO: ERROR
            PJBugManager.ReportMessageAndExit("ERROR: break statement outside loop or switch");

        }

        // all breaks with labels get turned into a goto(...); breaks not OUTSIDE and
        // not INSIDE_LOOP must be in switch statements - they never become gotos - just
        // remain as a 'break'.
        if (bs.getTarget() == null) {

            if(inside == INSIDE_LOOP) {

                // break to nearest break label.
                return makeGoto(breakLabel, bs);

            } else return bs; // don't change the break in a switch.

        } else {

            if(bls.get(bs.toString()) == null)
                PJBugManager.ReportMessageAndExit("ERROR: Unknown break label '" + bs + "' in line " + bs.line);

            int target = bls.get(bs.toString());

            // Goto(...);
            return makeGoto(target, bs);

        }

    }

    public AST visitChannelReadExpr(ChannelReadExpr cr) {
        Log.log("LoopRewriter:\tVisiting an ChannelReadExpr");

        if(cr.getExtendedRendezvous() != null) {
            try {
                cr.children[1] = cr.getExtendedRendezvous().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        }
        return null;
    }

    public AST visitChannelWriteStat(ChannelWriteStat cws) {
        Log.log("LoopRewriter:\tVisiting a ChannelWriteStat");
        return cws;
    }

    /*
	 * R[continue](bl, cl, bls, cls) = goto(cl); R[continue l](bl, cl, bls, cls) =
	 * goto(cls.get(l));
     */
    public AST visitContinueStat(ContinueStat cs) {
        Log.log("LoopRewriter:\tVisiting a ContinueStat");
        if (inside != INSIDE_LOOP) {

            PJBugManager.ReportMessageAndExit("ERROR: continue statement outside loop");

        }
        // no target => continue to closest continue label.
        if (cs.getTarget() == null) {
            return makeGoto(continueLabel, cs);
        } else {

            // look up the target in the hash table.
            if(cls.get(cs.getTarget().toString()) == null) {

                // TODO: ERROR
                PJBugManager.ReportMessageAndExit("ERROR: Unknown continue label '" + cs.getTarget() + "' in line " + cs.line);

            }
            int target = cls.get(cs.getTarget().toString());
            // Goto(...);
            return makeGoto(target, cs);
        }
    }

    /*
     * R[do S while (e)](bl, cl, bls, cls)= { label(cl'); R[S](bl', cl', bls, cls);
     * if (e) { goto(cl'); } label(bl'); }
     *
     * where bl' = newLabel(), cl'= newLabel();
     *
     * R[l: do S while (e)](bl, cl, bls, cls)= { label(cl'); R[S](bl', cl', bls',
     * cls'); if (e) { goto(cl'); } label(bl'); }
     *
     * where bl' = newLabel(), cl'= newLabel(); bls' = bls U {l = bl'}; cls' = cls U
     * {l = cl'};
     */
    public AST visitDoStat(DoStat ds) {
        Log.log("LoopRewriter:\tVisiting a DoStat");
        int old_inside = inside;
        inside = INSIDE_LOOP;

        Sequence<Statement> stmts = new Sequence<Statement>();

        int bl_ = newLabel();
        int cl_ = newLabel();
        // is the statement labeled?
        if(!ds.getLabel().equals("")) {
            addLabels(bls, cls, ds.getLabel(), bl_, cl_);
        }
        int bl_old = breakLabel;
        breakLabel = bl_;
        int cl_old = continueLabel;
        continueLabel = cl_;
        // Label(cl');
        stmts.append(makeLabel(cl_, ds));
        Statement st = null;
        try {
            st = (Statement) ds.getStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        stmts.merge(st);
        // if (e) { goto(cl'); }
        stmts.append(new IfStat(ds.getEvaluationExpression(), new Block(new Sequence(makeGoto(cl_, ds))), null));
        // Label(bl');
        stmts.append(makeLabel(bl_, ds));
        // is the statement labeled?
        if (!ds.getLabel().equals("")) {
            bls.remove(ds.getLabel());
            cls.remove(ds.getLabel());
        }
        inside = old_inside;
        Block returnBlock = new Block(stmts);
        returnBlock.canBeMerged = true;
        return returnBlock;
    }

    public AST visitExprStat(ExprStat es) {
        Log.log("LoopRewriter:\tVisiting an ExprStat");
        try {
            es.getExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return es;
    }

    public AST visitIfStat(IfStat is) {
        Log.log("LoopRewriter:\tVisiting an IfStat");
        Statement thenPart = null;
        try {
            thenPart = (Statement) is.getThenStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        Statement elsePart = null;
        try {
            elsePart = (is.getElseBody() == null ? null : (Statement) is.getElseBody().visit(this));
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

        return new IfStat(is.getEvaluationExpression(), thenPart, elsePart);
    }

    public AST visitLocalDecl(LocalDecl ld) {
        return ld;
    }

    public AST visitParBlock(ParBlock pb) {
        Log.log("LoopRewriter:\tVisiting a ParBlock");
        Sequence<Statement> stmts = pb.getStatements();
        for (int i = 0; i < stmts.size(); i++) {
            if (stmts.child(i) != null) {
                Statement s = null;
                try {
                    s = (Statement) stmts.child(i).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                stmts.insert(i, s);
            }
        }
        return pb;
    }

    public AST visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log("LoopRewriter:\tVisiting a ProcTypeDecl");
        if (pd.doesYield())
            try {
                pd.getBody().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        return null;
    }

    public AST visitReturnStat(ReturnStat rs) {
        Log.log("LoopRewriter:\tVisiting a ReturnStat");
        return rs;
    }

    public AST visitSkipStat(SkipStat ss) {
        Log.log("LoopRewriter:\tVisiting a SkipStat");
        return ss;
    }

    public AST visitStopStat(StopStat ss) {
        Log.log("LoopRewriter:\tVisiting a StopStat");
        return ss;
    }

    public AST visitSuspendStat(SuspendStat ss) {
        Log.log("LoopRewriter:\tVisiting a SuspendStat");
        return ss;
    }

    public AST visitSyncStat(SyncStat ss) {
        Log.log("LoopRewriter:\tVisiting a SyncStat");
        return ss;
    }

    public AST visitTimeoutStat(TimeoutStat ts) {
        Log.log("LoopRewriter:\tVisiting a TimeoutStat");
        return ts;
    }

    public AST visitSwitchStat(SwitchStat ss) {
        Log.log("LoopRewriter:\tVisiting a SwitchStat");
        int old_inside = inside;
        inside = INSIDE_SWITCH;
        for (int i = 0; i < ss.switchBlocks().size(); i++) {
            SwitchGroup sg = null;
            try {
                sg = (SwitchGroup) ss.switchBlocks().child(i).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            ss.switchBlocks().insert(i, sg);
        }
        inside = old_inside;
        return ss;
    }

    // Handled much like a Block - see the comments in the visitBlock() above.
    public AST visitSwitchGroup(SwitchGroup sg) {
        Log.log("LoopRewriter:\tVisiting a SwitchGroup");
        Sequence<Statement> stmts = sg.getStatements();
        Sequence<Statement> newStmts = new Sequence<Statement>();
        for(int i = 0; i < stmts.size(); i++) {
            if ((Statement) stmts.child(i) != null) {
                AST a = null;
                try {
                    a = stmts.child(i).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (a instanceof Block) {
                    Block b = (Block) a;
                    if (b.canBeMerged) {
                        newStmts.merge(b);
                    } else {
                        newStmts.append(b);
                    }
                } else {
                    newStmts.append((Statement) a);
                }
            } else {
                newStmts.append(null);
            }
        }
        sg.children[1] = newStmts;
        return sg;
    }

    /*
	 * R[for (e1; e2; e3) S](bl, cl, bls, cls)= { e1; label(cl'); if (e2) {
	 * R[S](bl', cl', bls, cls); e3; goto(cl'); } label(bl'); }
	 *
	 * where bl' = newLabel(); and cl' = newLabel();
	 *
	 * bls' = bls U {l = bl'}; cls' = cls U {l = cl'};
     */
    public AST visitForStat(ForStat fs) {
        Log.log("LoopRewriter:\tVisiting a ForStat");
        int old_inside = inside;
        inside = INSIDE_LOOP;
        int bl_old;
        int cl_old;
        Sequence<Statement> stmts = new Sequence<Statement>();
        int sl_ = newLabel();
        int bl_ = newLabel();
        int cl_ = newLabel();

        // is the statement labeled?
        if (!fs.getLabel().equals("")) {
            addLabels(bls, cls, fs.getLabel(), bl_, cl_);
        }
        bl_old = breakLabel;
        breakLabel = bl_;
        cl_old = continueLabel;
        continueLabel = cl_;
        
        // <--
        // Ignore par-for statement
        if (fs.isPar()) {
            inside = old_inside;
            stmts.append(fs);
            return new Block(stmts);
        }
        // -->

        // if e1 != null
        if (fs.getInitializationExpression() != null) {
            stmts.merge(fs.getInitializationExpression());
        }
        // Label(cl');
        stmts.append(makeLabel(sl_, fs));

        // [ R[S](...); e3 ]
        Statement st = null;
        try {
            st = (Statement) fs.getStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        Sequence<Statement> body = new Sequence<Statement>();
        body.merge(st);

        body.append(makeLabel(cl_, fs));
        if (fs.getIncrementExpression() != null) {
            body.merge(fs.getIncrementExpression());
        }
        // [ R[S](...); e3; Goto(cl'); ]
        body.append(makeGoto(sl_, fs));

        // { R[S](...); e3; Goto(cl'); }
        Block block = new Block(body);

        // if e2 == null => e2 = true
        Expression expr;
        if (fs.getEvaluationExpression() == null) {
            expr = new PrimitiveLiteral(new Token(sym.BOOLEAN_LITERAL, "true", fs.line, fs.charBegin, fs.charBegin + 4),
                    PrimitiveLiteral.BooleanKind);
        } else {
            expr = fs.getEvaluationExpression();
        }

        // if (e2) { R[S](...); e3; Goto(cl'); }
        IfStat is = new IfStat(expr, block, null);
        stmts.append(is);
        stmts.append(makeLabel(bl_, fs));
        // is the statement labeled?
        if (!fs.getLabel().equals("")) {
            bls.remove(fs.getLabel());
            cls.remove(fs.getLabel());
        }
        inside = old_inside;
        return new Block(stmts);
    }

    /*
	 * R[do S while (e)](bl, cl, bls, cls) = { label(cl'); R[S](bl',cl',bls,cls); if
	 * (e) { goto(cl'); } label(bl;))
	 *
	 * where bl' = newLabel(), cl'= newLabel();
	 *
	 * R[l: do S while (e)](bl, cl, bls, cls) = { label(cl');
	 * R[S](bl',cl',bls',cls'); if (e) { goto(cl'); } label(bl;))
	 *
	 * where bl' = newLabel(), cl'= newLabel(); bls' = bls U {l = bl'}; cls' = cls U
	 * {l = cl'};
	 *
     */
    public AST visitWhileStat(WhileStat ws) {
        Log.log("LoopRewriter:\tVisiting a WhileStat");
        int inside_old = inside;
        inside = INSIDE_LOOP;
        int bl_old;
        int cl_old;
        Sequence<Statement> stmts = new Sequence<Statement>();

        // make new labels for break and continue for this while statement
        int bl_ = newLabel();
        int cl_ = newLabel();
        // is the statement labeled?
        if(!ws.getLabel().equals(""))
            addLabels(bls, cls, ws.getLabel(), bl_, cl_);

        // update bl and cl
        bl_old = breakLabel;
        breakLabel = bl_;
        cl_old = continueLabel;
        continueLabel = cl_;
        // Label(cl');
        stmts.append(makeLabel(cl_, ws));

        Sequence<Statement> body = new Sequence<Statement>();
        Statement st = null;
        try {

            st = (Statement) ws.getStatements().visit(this);

        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        // st = R[S](bl',cl',bls',cls');
        if(st instanceof Block) {
            body.merge(((Block) st).getStatements());
        } else {
            body.merge(st);
        }
        body.append(makeGoto(cl_, ws));
        // block = { R[S](...); Goto(cl'); }
        Block block = new Block(body);
        // is = if (e) { R[S](...); Goto(cl'); }
        IfStat is = new IfStat(ws.getEvaluationExpression(), block, null);

        stmts.append(is);
        stmts.append(makeLabel(bl_, ws));
        // is the statement labeled?
        if (!ws.getLabel().equals("")) {
            bls.remove(ws.getLabel());
            cls.remove(ws.getLabel());
        }
        // reset labels
        breakLabel = bl_old;
        continueLabel = cl_old;
        inside = inside_old;
        Block returnBlock = new Block(stmts);
        returnBlock.canBeMerged = true;
        return returnBlock;
    }
}
