package org.processj.rewriters;

import org.processj.ast.AST;
import org.processj.ast.AltCase;
import org.processj.ast.AltStat;
import org.processj.ast.ArrayAccessExpr;
import org.processj.ast.Assignment;
import org.processj.ast.BinaryExpr;
import org.processj.ast.Block;
import org.processj.ast.CastExpr;
import org.processj.ast.ChannelEndExpr;
import org.processj.ast.ChannelReadExpr;
import org.processj.ast.ChannelWriteStat;
import org.processj.ast.Compilation;
import org.processj.ast.ExprStat;
import org.processj.ast.Expression;
import org.processj.ast.Guard;
import org.processj.ast.IfStat;
import org.processj.ast.Invocation;
import org.processj.ast.LocalDecl;
import org.processj.ast.Name;
import org.processj.ast.NameExpr;
import org.processj.ast.ParBlock;
import org.processj.ast.ProcTypeDecl;
import org.processj.ast.Sequence;
import org.processj.ast.Statement;
import org.processj.ast.SwitchGroup;
import org.processj.ast.SwitchStat;
import org.processj.ast.Ternary;
import org.processj.ast.TimeoutStat;
import org.processj.ast.Type;
import org.processj.ast.UnaryPostExpr;
import org.processj.ast.UnaryPreExpr;
import org.processj.ast.Var;
import org.processj.printers.PrettyPrinter;
import org.processj.utilities.Log;
import org.processj.utilities.Pair;
import org.processj.utilities.Visitor;

/**
 * This rewriter returns a Pair of two elements: Sequence and Expression,
 * where the first element is the extra generated statements and the second
 * is the expression that the rewrite resulted in (if any). If the first
 * element is empty, then the second element is entry result. However, if
 * the first element is null, then no expression was returned.
 * 
 * Note, we implement a function ρ(.) which rewrites statements and expressions
 * as follow. ρ(.) = ([stmts], expr), where [...] is a sequence of statements.
 * If ρ is applied to a statement, the expr part will _always_ be null. The
 * [stmts] part will contain any necessary code in order to rewrite the
 * original statement or expression in such a way that all occurrences for
 * channel read expression (e.g., e.read()) _only_ ever appear on the RHS of
 * an assignment all by itself. For example, ρ(c.read() + d.read()) results
 * in ([T1 t1; t1 = c.read(); T2 t2; t2 = d.read();], t1 + t2). It should be
 * noted that when applying ρ to an expression, the second part of the
 * resulting pair will be the rewritten version of that expression. If the
 * above binary expression with channel reads were on the RHS of a simple
 * such as: x = c.read() + d.read(), the rewritten code would be:
 * 
 * T1 t1; t1 = c.read(); T2 t2; t2 = d.read(); c = t1 + t2;
 * 
 * @author ben
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ChannelRead extends Visitor<Pair<Sequence, Expression>> {

    int temp;
    
    boolean log = false;

    private String nextTemp() {
        return "tmp" + temp++ + "$";
    }
    
    private LocalDecl createLocalDecl(String name, Type type) {
        return new LocalDecl(type, new Var(new Name(name), null), false);
    }
    
    private Assignment createAssignment(String name, Expression expr) {
        return new Assignment(new NameExpr(new Name(name)), expr, Assignment.EQ);
    }
    
    public ChannelRead() {
        Log.logHeader("*****************************************");
        Log.logHeader("* C H A N N E L - E N D   R E W R I T E *");
        Log.logHeader("*****************************************");
    }

    @Override
    public Pair<Sequence, Expression> visitCompilation(Compilation co) {
        Log.log(co, "Visiting Compilation");
        // TODO: Don't wee need to traverse everything else??
        for (AST decl : co.typeDecls()) {
            if (decl instanceof Type && decl != null) {
                Type t = ((Type) decl);
                if (t instanceof ProcTypeDecl)
                    t.visit(this);
            }
        }
        return (Pair<Sequence, Expression>) null;
    }

    @Override
    public Pair<Sequence, Expression> visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a ProcTypeDecl");
        Pair<Sequence, Expression> p = pd.body().visit(this);
        if (p != null)
            pd.children[6] = p.getFirst().child(0);
        if (log)
            pd.visit(new PrettyPrinter());
        return (Pair<Sequence, Expression>) null;
    }

    @Override
    public Pair<Sequence, Expression> visitSequence(Sequence se) {
        Log.log(se, "Visiting a Sequence");
        Sequence s = new Sequence();
        for (int i = 0; i < se.size(); ++i) {
            Pair<Sequence, Expression> p = se.child(i).visit(this);
            if (p != null) {
                if (p.getFirst().size() > 1) {
                    s.merge(new Block(p.getFirst()));
                    if (log) {
                        System.out.println("====== BEGIN Sequence ======");
                        p.getFirst().child(0).visit(new PrettyPrinter());
                        System.out.println("====== END Sequence ======");
                    }
                } else {
                    if (log)
                        System.out.println("We have an expression in a Sequence!");
                    s.merge(p.getFirst().child(0));
                }
            } else {
                // No rewrite needed for this section of code :-)
                if (se.child(i) instanceof Sequence)
                    s.merge((Sequence) se.child(i));
                else
                    s.append(se.child(i));
            }
        }
        return new Pair<>(s, null);
    }
    
    @Override
    public Pair<Sequence, Expression> visitBlock(Block bl) {
        Log.log(bl, "Visiting a Block");
        Pair<Sequence, Expression> p = bl.stats().visit(this);
        Sequence se = new Sequence(new Block(p.getFirst()));
        if ( log ) {
            se.visit(new PrettyPrinter());
        }
        return new Pair<Sequence, Expression>(se, null);
    }

    @Override
    public Pair<Sequence, Expression> visitParBlock(ParBlock pb) {
        Log.log(pb, "Visiting a ParBlock");
        Sequence se = pb.stats();
        for (int i = 0; i < se.size(); ++i) {
            Pair<Sequence, Expression> p = se.child(i).visit(this);
            if (p != null) {
                if (p.getFirst().size() > 1) {
                    se.set(i, new Block(p.getFirst()));
                    if (log) {
                        System.out.println("====== BEGIN ParBlock ======");
                        p.getFirst().visit(new PrettyPrinter());
                        System.out.println("====== END ParBlock ======");
                    }
                } else {
                    if (log)
                        System.out.println("We have an expression in ParBlock!");
                    se.set(i, p.getFirst().child(0));
                }
            }
        }
        se = new Sequence(new ParBlock(se, pb.barriers()));
        return new Pair<Sequence, Expression>(se, null);
    }

    @Override
    public Pair<Sequence, Expression> visitAssignment(Assignment as) {
        Log.log(as, "Visiting an Assignment");
        Pair<Sequence, Expression> p = null;
        if (as.right().doesYield()) {
            Log.log("---- Case #1 Assignment");
            Pair<Sequence, Expression> t = as.right().visit(this);
            p = new Pair<>(t.getFirst(), new Assignment(as.left(), t.getSecond(), as.op()));
        } else {
            Log.log("---- Case #2 Assignment");
            p = new Pair<>(new Sequence(), as);
        }
        return p;
    }

    @Override
    public Pair<Sequence, Expression> visitExprStat(ExprStat es) {
        Log.log(es, "Visiting an ExprStat");
        Pair<Sequence, Expression> p = es.expr().visit(this);
        Sequence se = p.getFirst();
        se.append((Statement) new ExprStat(p.getSecond()));
        if (log) {
            System.out.println("====== BEGIN ExprStat ======");
            se.visit(new PrettyPrinter());
            p.getSecond().visit(new PrettyPrinter());
            System.out.println();
            System.out.println("====== END ExprStat ======");
        }
        return new Pair<Sequence, Expression>(se, null);
    }

    @Override
    public Pair<Sequence, Expression> visitBinaryExpr(BinaryExpr be) {
        Log.log(be, "Visiting a BinaryExpr");
        Pair<Sequence, Expression> p = null;
        if (be.left().doesYield() && !be.right().doesYield()) {
            Log.log("---- Case #1 BinaryExpr");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, be.left().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = new ExprStat(createAssignment(name, be.left())).visit(this);
            // Make t <op> e
            BinaryExpr newExpr = new BinaryExpr(new NameExpr(new Name(name)), be.right(), be.op());
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            p = new Pair<>(se, newExpr);
        } else if (be.right().doesYield()) {
            Log.log("---- Case #2 BinaryExpr");
            String name1 = nextTemp();
            String name2 = nextTemp();
            // T t1; where T represents the type of e1
            LocalDecl ld1 = createLocalDecl(name1, be.left().type);
            // T t2; where T represents the type of e2
            LocalDecl ld2 = createLocalDecl(name2, be.right().type);
            // Rewrite the expression to t1 = e1;
            Pair<Sequence, Expression> t1 = new ExprStat(createAssignment(name1, be.left())).visit(this);
            // Rewrite the expression to t2 = e2;
            Pair<Sequence, Expression> t2 = new ExprStat(createAssignment(name2, be.right())).visit(this);
            // Make t1 <op> t2
            NameExpr ne1 = new NameExpr(new Name(name1));
            ne1.type = ld1.type();
            NameExpr ne2 = new NameExpr(new Name(name2));
            ne2.type = ld2.type();
            BinaryExpr newExpr = new BinaryExpr(ne1, ne2, be.op());
            Sequence se = new Sequence(ld1);
            se.append(ld2);
            se.merge(t1.getFirst());
            se.merge(t2.getFirst());
            p = new Pair<>(se, newExpr);
        } else {
            Log.log("---- Case #2 BinaryExpr");
            p = new Pair<>(new Sequence(), be);
        }
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitChannelReadExpr(ChannelReadExpr cr) {
        Log.log(cr, "Visiting a ChannelReadExpr");
        Pair<Sequence, Expression> p = null;
        if (cr.channel().doesYield()) {
            Log.log("---- Case #1 ChannelReadExpr");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, cr.channel().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = new ExprStat(createAssignment(name, cr.channel())).visit(this);
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            // Have extended rendezvous?
            Block extRV = null;
            if (cr.extRV() != null) {
                Pair<Sequence, Expression> rv = cr.extRV().visit(this);
                extRV = (Block) rv.getFirst().child(0);
            }
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.type();
            p = new Pair<>(se, new ChannelReadExpr(ne, extRV));
        } else {
            Log.log("---- Case #2 ChannelReadExpr");
            p = new Pair<>(new Sequence(), cr);
        }
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitChannelWriteStat(ChannelWriteStat cw) {
        Log.log(cw, "Visiting a ChannelWriteStat");
        Pair<Sequence, Expression> p = null;
        if (cw.channel().doesYield() && !cw.expr().doesYield()) {
            Log.log("---- Case #1 ChannelWriteStat");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, cw.channel().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = new ExprStat(createAssignment(name, cw.channel())).visit(this);
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            p = new Pair<>(se, null);
        } else if (cw.expr().doesYield()) {
            Log.log("---- Case #2 ChannelWriteStat");
            String name1 = nextTemp();
            String name2 = nextTemp();
            // T t1; where T represents the type of e1
            LocalDecl ld1 = createLocalDecl(name1, cw.channel().type);
            // T t2; where T represents the type of e2
            LocalDecl ld2 = createLocalDecl(name2, cw.expr().type);
            // Rewrite the expression to t1 = e1;
            Pair<Sequence, Expression> t1 = new ExprStat(createAssignment(name1, cw.channel())).visit(this);
            // Rewrite the expression to t2 = e2;
            Pair<Sequence, Expression> t2 = new ExprStat(createAssignment(name2, cw.expr())).visit(this);
            Sequence se = new Sequence(ld1);
            se.merge(t1.getFirst());
            se.append(ld2);
            se.merge(t2.getFirst());
            NameExpr ne1 = new NameExpr(new Name(name1));
            ne1.type = ld1.type();
            NameExpr ne2 = new NameExpr(new Name(name2));
            ne2.type = ld2.type();
            se.append(new ChannelWriteStat(ne1, ne2));
            if (log) {
                System.out.println("====== BEGIN ChannelWriteStat ======");
                se.visit(new PrettyPrinter());
                System.out.println("====== END ChannelWriteStat ======");
            }
            p = new Pair<>(se, null);
        } else {
            Log.log("---- Case #3 ChannelWriteStat");
            p = new Pair<>(new Sequence(cw), null);
        }
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitLocalDecl(LocalDecl ld) {
        Log.log(ld, "Visiting a LocalDecl");
        Pair<Sequence, Expression> p = null;
        if (ld.var().init() == null || (ld.var().init() != null && !ld.var().init().doesYield())) {
            Log.log("---- Case #1 LocalDecl");
            p = new Pair<>(new Sequence(ld), null);
        } else {
            Log.log("---- Case #2 LocalDecl");
            LocalDecl ld1 = createLocalDecl(ld.var().name().getname(), ld.type());
            Pair<Sequence, Expression> t = new ExprStat(createAssignment(ld.var().name().getname(), ld.var().init())).visit(this);
            Sequence se = new Sequence(ld1);
            se.merge(t.getFirst());
            p = new Pair<>(se, null);
        }
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitCastExpr(CastExpr ce) {
        Log.log(ce, "Visiting a CastExpr");
        Pair<Sequence, Expression> p = null;
        if (ce.expr().doesYield()) {
            Log.log("---- Case #1 CastExpr");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, ce.expr().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = new ExprStat(createAssignment(name, ce.expr())).visit(this);
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.type();
            p = new Pair<>(se, new CastExpr(ce.type(), ne));
        } else {
            Log.log("---- Case #2 CastExpr");
            p = new Pair<>(new Sequence(), ce);
        }
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitIfStat(IfStat is) {
        Log.log(is, "Visiting an IfStat");
        Pair<Sequence, Expression> p = null;
        Sequence se = new Sequence();
        if (is.expr().doesYield()) {
            Log.log("---- Case #1 IfStat: then-part");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, is.expr().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = new ExprStat(createAssignment(name, is.expr())).visit(this);
            se = new Sequence(ld);
            se.merge(t.getFirst());
            // <-- 
            // Apply the rewrite to 'then-part'
            if (is.thenpart() != null) {
                Sequence thenpart = is.thenpart().visit(this).getFirst();
                is.children[1] = new Block(thenpart);
            }
            // -->
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.type();
            is.children[0] = ne;
        } else {
            Log.log("---- Case #2 IfStat: then-part (does not org.processj.yield)");
            if ( is.thenpart()!=null ) {
                Sequence thenpart = is.thenpart().visit(this).getFirst();
                is.children[1] = new Block(thenpart);
            }
        }
        // If we skip the first part, then expr() does not org.processj.yield!
        // Now check the else-part which it is already a Block
        Pair<Sequence, Expression> elsepart = null;
        if (is.elsepart() != null) {
            Log.log("---- Case #3 IfStat: else-part");
            elsepart = is.elsepart().visit(this);
            Sequence stmt = elsepart.getFirst();
            if (log) {
                System.out.println("====== BEGIN IfState::elsepart() ======");
                stmt.visit(new PrettyPrinter());
                System.out.println("====== END IfState::elsepart() ======");
            }
            if (stmt.size() > 1)
                is.children[2] = new Block(stmt);
            else
                is.children[2] = stmt.child(0);
        }
        se.append(is);
        p = new Pair<>(se, null);
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitInvocation(Invocation in) {
        Log.log(in, "Visiting an Invocation");
        // TODO: remember to handle mobiles!!
        Pair<Sequence, Expression> p = null;
        Sequence<Expression> params = in.params();
        int yieldPos = -1;
        for (int i = params.size() - 1; i >= 0; --i)
            if (params.child(i).doesYield())
                yieldPos = Math.max(yieldPos, i);
        if (in.doesYield() && yieldPos >= 0){
            Log.log("---- Case #2 Invocation");
            Sequence se = new Sequence();
            for (int i = 0; i <= yieldPos; ++i) {
                Expression e = params.child(i);
                String name = nextTemp();
                // T t; where T represents the type of e
                LocalDecl ld = createLocalDecl(name, e.type);
                // Rewrite the expression to t = e;
                Pair<Sequence, Expression> t = new ExprStat(createAssignment(name, e)).visit(this);
                se.append(ld);
                se.merge(t.getFirst());
                NameExpr ne = new NameExpr(new Name(name));
                ne.type = ld.type();
                params.set(i, ne);
            }
            p = new Pair<>(se, in);
        } else {
            Log.log("---- Case #3 Invocation");
            p = new Pair<>(new Sequence(), in);
        }
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitArrayAccessExpr(ArrayAccessExpr ae) {
        Log.log(ae, "Visiting an ArrayAccessExpr");
        Pair<Sequence, Expression> p = null;
        if (ae.targetExpression().doesYield() && !ae.indexExpression().doesYield()) {
            Log.log("---- Case #1 ArrayAccessExpr");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, ae.targetExpression().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = new ExprStat(createAssignment(name, ae.targetExpression())).visit(this);
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            p = new Pair<>(se, new ArrayAccessExpr(new NameExpr(new Name(name)), ae.indexExpression()));
        } else if (ae.indexExpression().doesYield()) {
            Log.log("---- Case #2 ArrayAccessExpr");
            String name1 = nextTemp();
            String name2 = nextTemp();
            // T t1; where T represents the type of e1
            LocalDecl ld1 = createLocalDecl(name1, ae.targetExpression().type);
            // Rewrite the expression to t1 = e1;
            Pair<Sequence, Expression> t1 = new ExprStat(createAssignment(name1, ae.targetExpression())).visit(this);
            // T t2; where T represents the type of e2
            LocalDecl ld2 = createLocalDecl(name2, ae.indexExpression().type);
            // Rewrite the expression to t2 = e2;
            Pair<Sequence, Expression> t2 = new ExprStat(createAssignment(name2, ae.indexExpression())).visit(this);
            Sequence se = new Sequence(ld1);
            se.merge(t1.getFirst());
            se.append(ld2);
            se.merge(t2.getFirst());
            NameExpr ne1 = new NameExpr(new Name(name1));
            ne1.type = ld1.type();
            NameExpr ne2 = new NameExpr(new Name(name2));
            ne2.type = ld2.type();
            p = new Pair<>(se, new ArrayAccessExpr(ne1, ne2));
        } else {
            Log.log("---- Case #3 ArrayAccessExpr");
            p = new Pair<>(new Sequence(), ae);
        }
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitChannelEndExpr(ChannelEndExpr ce) {
        Log.log(ce, "Visiting a ChannelEndExpr");
        Pair<Sequence, Expression> p = null;
        if (ce.channel().doesYield()) {
            Log.log("---- Case #1");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, ce.channel().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = new ExprStat(createAssignment(name, ce.channel())).visit(this);
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.type();
            p = new Pair<>(se, new ChannelEndExpr(ne, ce.endType()));
        } else {
            Log.log("---- Case #1");
            p = new Pair<>(new Sequence(), ce);
        }
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitSwitchStat(SwitchStat st) {
        Log.log(st, "Visiting a SwitchStat");
        Pair<Sequence, Expression> p = null;
        Sequence se = new Sequence();
        if (st.expr().doesYield()) {
            Log.log("---- Case #1 SwitchStat: expr");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, st.expr().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = new ExprStat(createAssignment(name, st.expr())).visit(this);
            se.append(ld);
            se.merge(t.getFirst());
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.type();
            st.children[0] = ne;
        }
        // If we skip the first part, then expr() does not org.processj.yield!
        // Now check the else-part which it is already a Block
        if (st.switchBlocks().size() > 0) {
            Log.log("---- Case #2 SwitchStat: switch-block");
            Sequence<SwitchGroup> sg = st.switchBlocks();
            for (int i = 0; i < sg.size(); ++i)
                sg.child(i).visit(this);
        }
        se.append(st);
        p = new Pair<>(se, null);
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitSwitchGroup(SwitchGroup sg) {
        Log.log(sg, "Visiting a SwitchGroup");
        Sequence<Statement> se = sg.statements();
        for (int i = 0; i < se.size(); ++i) {
            Pair<Sequence, Expression> p = se.child(i).visit(this);
            if (p != null) {
                if (p.getFirst().size() > 1)
                    se.set(i, new Block(p.getFirst()));
                else
                    se.set(i, (Statement) p.getFirst().child(0));
            }
        }
        return (Pair<Sequence, Expression>) null;
    }
    
    @Override
    public Pair<Sequence, Expression> visitTernary(Ternary te) {
        Log.log(te, "Visiting a Ternary");
        if (te.trueBranch().doesYield() || te.falseBranch().doesYield())
            ; // TODO: Throw error message??
        Pair<Sequence, Expression> p = null;
        if (te.expr().doesYield()) {
            Log.log("---- Case #1 Ternary");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, te.expr().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = new ExprStat(createAssignment(name, te.expr())).visit(this);
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.type();
            p = new Pair<>(se, new Ternary(ne, te.trueBranch(), te.falseBranch()));
        } else {
            Log.log("---- Case #1 Ternary");
            p = new Pair<>(new Sequence(), te);
        }
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitTimeoutStat(TimeoutStat ts) {
        Log.log(ts, "Visiting a TimeoutStat");
        Pair<Sequence, Expression> p = null;
        if (ts.timer().doesYield() && !ts.delay().doesYield()) {
            Log.log("---- Case #1 TimeoutStat");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, ts.timer().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = new ExprStat(createAssignment(name, ts.timer())).visit(this);
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.type();
            se.append(new TimeoutStat(ne, ts.delay()));
            p = new Pair<>(se, null);
        } else if (ts.delay().doesYield()) {
            Log.log("---- Case #2 TimeoutStat");
            String name1 = nextTemp();
            String name2 = nextTemp();
            // T t1; where T represents the type of e1
            LocalDecl ld1 = createLocalDecl(name1, ts.timer().type);
            // Rewrite the expression to t1 = e1;
            Pair<Sequence, Expression> t1 = new ExprStat(createAssignment(name1, ts.timer())).visit(this);
            // T t2; where T represents the type of e2
            LocalDecl ld2 = createLocalDecl(name2, ts.delay().type);
            // Rewrite the expression to t2 = e2;
            Pair<Sequence, Expression> t2 = new ExprStat(createAssignment(name2, ts.delay())).visit(this);
            Sequence se = new Sequence(ld1);
            se.merge(t1.getFirst());
            se.append(ld2);
            se.merge(t2.getFirst());
            NameExpr ne1 = new NameExpr(new Name(name1));
            ne1.type = ld1.type();
            NameExpr ne2 = new NameExpr(new Name(name2));
            ne2.type = ld2.type();
            se.append(new TimeoutStat(ne1, ne2));
            p = new Pair<>(se, null);
        } else {
            Log.log("---- Case #3 TimeoutStat");
            p = new Pair<>(new Sequence(ts), null);
        }
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitUnaryPostExpr(UnaryPostExpr up) {
        Log.log(up, "Visiting a UnaryPostExpr");
        if (up.expr().doesYield())
            ; // TODO: Throw error message??
        return new Pair<>(new Sequence(), up);
    }
    
    @Override
    public Pair<Sequence, Expression> visitUnaryPreExpr(UnaryPreExpr up) {
        Log.log(up, "Visitng a UnaryPreExpr");
        if (UnaryPreExpr.PLUSPLUS == up.op() || UnaryPreExpr.MINUSMINUS == up.op())
            ; // TODO: Throw error message??
        Pair<Sequence, Expression> p = null;
        if (up.doesYield()) {
            Log.log("---- Case #1 UnaryPreExpr");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, up.expr().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = new ExprStat(createAssignment(name, up.expr())).visit(this);
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.type();
            p = new Pair<>(se, new UnaryPreExpr(ne, up.op()));
        } else {
            Log.log("---- Case #2 UnaryPreExpr");
            p = new Pair<>(new Sequence(), up);
        }
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitAltStat(AltStat as) {
        Log.log(as, "Visiting an AltStat");
        Sequence<AltCase> body = as.body();
        for (int i = 0; i < body.size(); ++i)
            body.child(i).visit(this);
        return new Pair<>(new Sequence(as), null);
    }
    
    @Override
    public Pair<Sequence, Expression> visitAltCase(AltCase ac) {
        Log.log(ac, "Visiting an AltCase");
        Pair<Sequence, Expression> p = null;
        // Rewrite the guard statement if needed (if null => nested alt)
        if (ac.guard() != null)
            p = ac.guard().visit(this);
        // Rewrite the statement if needed
        p = ac.stat().visit(this);
        if (p != null) {
            if (p.getFirst().size() > 1)
                ac.children[2] = new Block(p.getFirst());
            else
                ac.children[2] = p.getFirst().child(0);
        }
        return (Pair<Sequence, Expression>) null;
    }
    
    @Override
    public Pair<Sequence, Expression> visitGuard(Guard gu) {
        Log.log(gu, "Visiting a Guard");
        Pair<Sequence, Expression> p = null;
        Statement stat = gu.guard();
        if (stat instanceof ExprStat) {
            ExprStat es = (ExprStat) stat;
            p = es.visit(this);
            if (p != null) {
                if (p.getFirst().size() > 1)
                    gu.children[0] = new Block(p.getFirst());
                else
                    gu.children[0] = p.getFirst().child(0);
            }
        }
        return (Pair<Sequence, Expression>) null;
    }
    
    // TODO: Record
    // TODO: Protocol
    // TODO: ReturnStat -- Should processes return a value??
}
