package rewriters;

import java.util.Hashtable;

import ast.*;
import ast.AST;
import ast.Block;
import ast.BreakStat;
import ast.ContinueStat;
import ast.DoStat;
import ast.ExprStat;
import ast.Expression;
import ast.ForStat;
import ast.IfStat;
import ast.Invocation;
import ast.Name;
import ast.PrimitiveLiteral;
import ast.ProcTypeDecl;
import ast.Sequence;
import ast.Statement;
import ast.Token;
import ast.WhileStat;
import codegen.Helper;
import parser.sym;
import printers.PrettyPrinter;
import utilities.Log;
import utilities.Visitor;

/**
 * A re-implementation of the Visitor that traverses the tree to rewrite while,
 * do and for loop constructs in to if-statements with labels and gotos.
 *
 * @author Matt Pedersen
 * @version 10/28/19
 * @since 1.2
 */
public class UnrollLoopRewrite extends Visitor<AST> {

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
    private int bl;
    // continue label
    private int cl;
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
        return new ExprStat(new Invocation(null, new Name("LABEL"), new Sequence<Expression>(new PrimitiveLiteral(
                new Token(sym.INTEGER_LITERAL, "" + no, a.line, a.charBegin, a.charBegin + ("" + no).length() - 1),
                PrimitiveLiteral.IntKind)), true));
    }

    // Constructs a 'Goto(...) invocation.
    private Statement makeGoto(int no, AST a) {
        return new ExprStat(new Invocation(null, new Name("GOTO"), new Sequence<Expression>(new PrimitiveLiteral(
                new Token(sym.INTEGER_LITERAL, "" + no, a.line, a.charBegin, a.charBegin + ("" + no).length() - 1),
                PrimitiveLiteral.IntKind)), true));
    }

    public UnrollLoopRewrite() {
        Log.logHeader("*****************************************");
        Log.logHeader("*      L O O P  -  R E W R I T E R      *");
        Log.logHeader("*****************************************");
    }

    public AST visitAltCase(AltCase ac) {
        Log.log("LoopRewriter:\tVisiting an AltCase");
	// if ac.guard() is null, it is because we have a nested alt.
	if (ac.guard() != null)
	    ac.guard().visit(this);
        ac.children[2] = ac.stat().visit(this);
        return null;
    }

    public AST visitAltStat(AltStat as) {
        Log.log("LoopRewriter:\tVisiting an AltStat");
        // TODO: Replicated ALT
        super.visitAltStat(as);
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
        Sequence<Statement> stmts = bl.stats();
        Sequence<Statement> newStmts = new Sequence<Statement>();

        for (int i = 0; i < stmts.size(); i++) {
            if ((Statement) stmts.child(i) != null) {
                AST a = stmts.child(i).visit(this);
                if (a instanceof Block) {
                    Block b = (Block) a;
                    if (b.canBeMerged) {
                        // block with canBeMerged == true => MERGE into new statements.
                        newStmts.merge(b.stats());
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
        if (inside == OUTSIDE) {
            // TODO: ERROR
            Log.log("ERROR: break statement outside loop or switch");
            Log.log("");
            System.exit(1);
        }
        // all breaks with labels get turned into a goto(...); breaks not OUTSIDE and
        // not INSIDE_LOOP must be in switch statements - they never become gotos - just
        // remain as a 'break'.
        if (bs.target() == null) {
            if (inside == INSIDE_LOOP) {
                // break to nearest break label.
                return makeGoto(bl, bs);
            } else {
                return bs; // don't change the break in a switch.
            }
        } else {
            if (bls.get(bs.target().getname()) == null) {
                // TODO: ERROR
                Log.log("ERROR: Unknown break label '" + bs.target().getname() + "' in line " + bs.line);
                Log.log("");
                System.exit(1);
            }
            int target = bls.get(bs.target().getname());
            // Goto(...);
            return makeGoto(target, bs);
        }
    }

    public AST visitChannelReadExpr(ChannelReadExpr cr) {
        Log.log("LoopRewriter:\tVisiting an ChannelReadExpr");
        if (cr.extRV() != null) {
            cr.children[1] = cr.extRV().visit(this);
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
            // TODO: ERROR
            Log.log("ERROR: continue statement outside loop");
            Log.log("");
            System.exit(1);
        }
        // no target => continue to closest continue label.
        if (cs.target() == null) {
            return makeGoto(cl, cs);
        } else {
            // look up the target in the hash table.
            if (cls.get(cs.target().getname()) == null) {
                // TODO: ERROR
                Log.log("ERROR: Unknown continue label '" + cs.target().getname() + "' in line " + cs.line);
                Log.log("");
                System.exit(1);
            }
            int target = cls.get(cs.target().getname());
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
        int bl_old;
        int cl_old;
        Sequence<Statement> stmts = new Sequence<Statement>();

        int bl_ = newLabel();
        int cl_ = newLabel();
        // is the statement labeled?
        if (!ds.getLabel().equals("")) {
            addLabels(bls, cls, ds.getLabel(), bl_, cl_);
        }
        bl_old = bl;
        bl = bl_;
        cl_old = cl;
        cl = cl_;
        // Label(cl');
        stmts.append(makeLabel(cl_, ds));
        Statement st = (Statement) ds.stat().visit(this);
        stmts.merge(st);
        // if (e) { goto(cl'); }
        stmts.append(new IfStat(ds.expr(), new Block(new Sequence(makeGoto(cl_, ds))), null));
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
        es.expr().visit(this);
        return es;
    }

    public AST visitIfStat(IfStat is) {
        Log.log("LoopRewriter:\tVisiting an IfStat");
        Statement thenPart = (Statement) is.thenpart().visit(this);
        Statement elsePart = (is.elsepart() == null ? null : (Statement) is.elsepart().visit(this));

        return new IfStat(is.expr(), thenPart, elsePart);
    }

    public AST visitLocalDecl(LocalDecl ld) {
        return ld;
    }

    public AST visitParBlock(ParBlock pb) {
        Log.log("LoopRewriter:\tVisiting a ParBlock");
        Sequence<Statement> stmts = pb.stats();
        for (int i = 0; i < stmts.size(); i++) {
            if ((Statement) stmts.child(i) != null) {
                Statement s = (Statement) stmts.child(i).visit(this);
                stmts.set(i, s);
            }
        }
        return pb;
    }

    public AST visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log("LoopRewriter:\tVisiting a ProcTypeDecl");
        if (Helper.doesProcYield(pd))
            pd.body().visit(this);
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
            SwitchGroup sg = (SwitchGroup) ss.switchBlocks().child(i).visit(this);
            ss.switchBlocks().set(i, sg);
        }
        inside = old_inside;
        return ss;
    }

    // Handled much like a Block - see the comments in the visitBlock() above.
    public AST visitSwitchGroup(SwitchGroup sg) {
        Log.log("LoopRewriter:\tVisiting a SwitchGroup");
        Sequence<Statement> stmts = sg.statements();
        Sequence<Statement> newStmts = new Sequence<Statement>();
        for (int i = 0; i < stmts.size(); i++) {
            if ((Statement) stmts.child(i) != null) {
                AST a = stmts.child(i).visit(this);
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
        bl_old = bl;
        bl = bl_;
        cl_old = cl;
        cl = cl_;
        
        // <--
        // Ignore par-for statement
        if (fs.isPar()) {
            inside = old_inside;
            stmts.append(fs);
            return new Block(stmts);
        }
        // -->

        // if e1 != null
        if (fs.init() != null) {
            stmts.merge(fs.init());
        }
        // Label(cl');
        stmts.append(makeLabel(sl_, fs));

        // [ R[S](...); e3 ]
        Statement st = (Statement) fs.stats().visit(this);
        Sequence<Statement> body = new Sequence<Statement>();
        if (fs.stats() instanceof Block) {
            body.merge(((Block) st).stats());
        } else {
            body.merge(st);
        }
        body.append(makeLabel(cl_, fs));
        if (fs.incr() != null) {
            body.merge(fs.incr());
        }
        // [ R[S](...); e3; Goto(cl'); ]
        body.append(makeGoto(sl_, fs));

        // { R[S](...); e3; Goto(cl'); }
        Block block = new Block(body);

        // if e2 == null => e2 = true
        Expression expr;
        if (fs.expr() == null) {
            expr = new PrimitiveLiteral(new Token(sym.BOOLEAN_LITERAL, "true", fs.line, fs.charBegin, fs.charBegin + 4),
                    PrimitiveLiteral.BooleanKind);
        } else {
            expr = fs.expr();
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
        if (!ws.getLabel().equals("")) {
            addLabels(bls, cls, ws.getLabel(), bl_, cl_);
        }
        // update bl and cl
        bl_old = bl;
        bl = bl_;
        cl_old = cl;
        cl = cl_;
        // Label(cl');
        stmts.append(makeLabel(cl_, ws));

        Sequence<Statement> body = new Sequence<Statement>();
        Statement st = (Statement) ws.stat().visit(this);
        // st = R[S](bl',cl',bls',cls');
        if (st instanceof Block) {
            body.merge(((Block) st).stats());
        } else {
            body.merge(st);
        }
        body.append(makeGoto(cl_, ws));
        // block = { R[S](...); Goto(cl'); }
        Block block = new Block(body);
        // is = if (e) { R[S](...); Goto(cl'); }
        IfStat is = new IfStat(ws.expr(), block, null);
        stmts.append(is);
        stmts.append(makeLabel(bl_, ws));
        // is the statement labeled?
        if (!ws.getLabel().equals("")) {
            bls.remove(ws.getLabel());
            cls.remove(ws.getLabel());
        }
        // reset labels
        bl = bl_old;
        cl = cl_old;
        inside = inside_old;
        Block returnBlock = new Block(stmts);
        returnBlock.canBeMerged = true;
        return returnBlock;
    }
}
