package org.processj.compiler.phases.legacy.codegen;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.alt.AltCase;
import org.processj.compiler.ast.alt.AltStat;
import org.processj.compiler.ast.expression.ArrayAccessExpr;
import org.processj.compiler.ast.expression.Assignment;
import org.processj.compiler.ast.expression.BinaryExpr;
import org.processj.compiler.ast.Block;
import org.processj.compiler.ast.CastExpr;
import org.processj.compiler.ast.ChannelEndExpr;
import org.processj.compiler.ast.ChannelReadExpr;
import org.processj.compiler.ast.ChannelWriteStat;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.ExprStat;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.alt.Guard;
import org.processj.compiler.ast.IfStat;
import org.processj.compiler.ast.Invocation;
import org.processj.compiler.ast.LocalDecl;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.NameExpr;
import org.processj.compiler.ast.ParBlock;
import org.processj.compiler.ast.ProcTypeDecl;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.Statement;
import org.processj.compiler.ast.SwitchGroup;
import org.processj.compiler.ast.SwitchStat;
import org.processj.compiler.ast.Ternary;
import org.processj.compiler.ast.TimeoutStat;
import org.processj.compiler.ast.Type;
import org.processj.compiler.ast.UnaryPostExpr;
import org.processj.compiler.ast.UnaryPreExpr;
import org.processj.compiler.ast.Var;
import org.processj.compiler.utilities.printers.PrettyPrinter;
import org.processj.compiler.utilities.Log;
import org.processj.compiler.utilities.Pair;
import org.processj.compiler.phases.phase.Visitor;

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
public class ChannelRead implements Visitor<Pair<Sequence, Expression>> {

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
        for (AST decl : co.getTypeDeclarations()) {
            if (decl instanceof Type && decl != null) {
                Type t = ((Type) decl);
                if (t instanceof ProcTypeDecl)
                    try {
                        t.visit(this);
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
            }
        }
        return (Pair<Sequence, Expression>) null;
    }

    @Override
    public Pair<Sequence, Expression> visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a ProcTypeDecl");
        Pair<Sequence, Expression> p = null;
        try {
            p = pd.getBody().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        if (p != null)
            pd.children[6] = p.getFirst().child(0);
        if (log)
            try {
                pd.visit(new PrettyPrinter());
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        return (Pair<Sequence, Expression>) null;
    }

    @Override
    public Pair<Sequence, Expression> visitSequence(Sequence se) throws Phase.Error {
        Log.log(se, "Visiting a Sequence");
        Sequence s = new Sequence();
        for (int i = 0; i < se.size(); ++i) {
            Pair<Sequence, Expression> p = se.child(i).visit(this);
            if (p != null) {
                if (p.getFirst().size() > 1) {
                    s.merge(new Block(p.getFirst()));
                } else {
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
    public Pair<Sequence, Expression> visitBlock(Block bl) throws Phase.Error {
        Log.log(bl, "Visiting a Block");
        Pair<Sequence, Expression> p = null;

        p = bl.getStatements().visit(this);

        Sequence se = new Sequence(new Block(p.getFirst()));

        return new Pair<Sequence, Expression>(se, null);

    }

    @Override
    public Pair<Sequence, Expression> visitParBlock(ParBlock pb) {
        Log.log(pb, "Visiting a ParBlock");
        Sequence se = pb.getStatements();
        for (int i = 0; i < se.size(); ++i) {
            Pair<Sequence, Expression> p = null;
            try {
                p = se.child(i).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            if (p != null) {
                if (p.getFirst().size() > 1) {
                    se.insert(i, new Block(p.getFirst()));
                    if (log) {
                        System.out.println("====== BEGIN ParBlock ======");
                        try {
                            p.getFirst().visit(new PrettyPrinter());
                        } catch (Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                        System.out.println("====== END ParBlock ======");
                    }
                } else {
                    if (log)
                        System.out.println("We have an expression in ParBlock!");
                    se.insert(i, p.getFirst().child(0));
                }
            }
        }

        final Sequence<Expression> barriers = new Sequence<>();
        pb.getBarrierSet().forEach(barriers::append);

        se = new Sequence(new ParBlock(se, barriers));
        return new Pair<Sequence, Expression>(se, null);
    }

    @Override
    public Pair<Sequence, Expression> visitAssignment(Assignment as) {
        Log.log(as, "Visiting an Assignment");
        Pair<Sequence, Expression> p = null;
        if (as.getRight().doesYield()) {
            Log.log("---- Case #1 Assignment");
            Pair<Sequence, Expression> t = null;
            try {
                t = as.getRight().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            p = new Pair<>(t.getFirst(), new Assignment(as.left(), t.getSecond(), as.getOperator()));
        } else {
            Log.log("---- Case #2 Assignment");
            p = new Pair<>(new Sequence(), as);
        }
        return p;
    }

    @Override
    public Pair<Sequence, Expression> visitExprStat(ExprStat es) {
        Log.log(es, "Visiting an ExprStat");
        Pair<Sequence, Expression> p = null;
        try {
            p = es.getExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        Sequence se = p.getFirst();
        se.append((Statement) new ExprStat(p.getSecond()));
        if (log) {
            System.out.println("====== BEGIN ExprStat ======");
            try {
                se.visit(new PrettyPrinter());
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            try {
                p.getSecond().visit(new PrettyPrinter());
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            System.out.println();
            System.out.println("====== END ExprStat ======");
        }
        return new Pair<Sequence, Expression>(se, null);
    }

    @Override
    public Pair<Sequence, Expression> visitBinaryExpr(BinaryExpr be) throws Phase.Error {
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
            Pair<Sequence, Expression> t1 = null;
            try {
                t1 = new ExprStat(createAssignment(name1, be.left())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            // Rewrite the expression to t2 = e2;
            Pair<Sequence, Expression> t2 = null;
            try {
                t2 = new ExprStat(createAssignment(name2, be.right())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            // Make t1 <op> t2
            NameExpr ne1 = new NameExpr(new Name(name1));
            ne1.type = ld1.getType();
            NameExpr ne2 = new NameExpr(new Name(name2));
            ne2.type = ld2.getType();
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
        if (cr.getExpression().doesYield()) {
            Log.log("---- Case #1 ChannelReadExpr");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, cr.getExpression().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = null;
            try {
                t = new ExprStat(createAssignment(name, cr.getExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            // Have extended rendezvous?
            Block extRV = null;
            if (cr.getExtendedRendezvous() != null) {
                Pair<Sequence, Expression> rv = null;
                try {
                    rv = cr.getExtendedRendezvous().visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                extRV = (Block) rv.getFirst().child(0);
            }
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.getType();
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
        if (cw.getTargetExpression().doesYield() && !cw.getWriteExpression().doesYield()) {
            Log.log("---- Case #1 ChannelWriteStat");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, cw.getTargetExpression().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = null;
            try {
                t = new ExprStat(createAssignment(name, cw.getTargetExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            p = new Pair<>(se, null);
        } else if (cw.getWriteExpression().doesYield()) {
            Log.log("---- Case #2 ChannelWriteStat");
            String name1 = nextTemp();
            String name2 = nextTemp();
            // T t1; where T represents the type of e1
            LocalDecl ld1 = createLocalDecl(name1, cw.getTargetExpression().type);
            // T t2; where T represents the type of e2
            LocalDecl ld2 = createLocalDecl(name2, cw.getWriteExpression().type);
            // Rewrite the expression to t1 = e1;
            Pair<Sequence, Expression> t1 = null;
            try {
                t1 = new ExprStat(createAssignment(name1, cw.getTargetExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            // Rewrite the expression to t2 = e2;
            Pair<Sequence, Expression> t2 = null;
            try {
                t2 = new ExprStat(createAssignment(name2, cw.getWriteExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            Sequence se = new Sequence(ld1);
            se.merge(t1.getFirst());
            se.append(ld2);
            se.merge(t2.getFirst());
            NameExpr ne1 = new NameExpr(new Name(name1));
            ne1.type = ld1.getType();
            NameExpr ne2 = new NameExpr(new Name(name2));
            ne2.type = ld2.getType();
            se.append(new ChannelWriteStat(ne1, ne2));
            if (log) {
                System.out.println("====== BEGIN ChannelWriteStat ======");
                try {
                    se.visit(new PrettyPrinter());
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
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
        if (!ld.isInitialized() || !ld.getInitializationExpression().doesYield()) {
            Log.log("---- Case #1 LocalDecl");
            p = new Pair<>(new Sequence(ld), null);
        } else {
            Log.log("---- Case #2 LocalDecl");
            LocalDecl ld1 = createLocalDecl(ld.toString(), ld.getType());
            Pair<Sequence, Expression> t = null;
            try {
                t = new ExprStat(createAssignment(ld.toString(), ld.getInitializationExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
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
        if (ce.getExpression().doesYield()) {
            Log.log("---- Case #1 CastExpr");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, ce.getExpression().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = null;
            try {
                t = new ExprStat(createAssignment(name, ce.getExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.getType();
            p = new Pair<>(se, new CastExpr(ce.getCastType(), ne));
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
        if (is.getEvaluationExpression().doesYield()) {
            Log.log("---- Case #1 IfStat: then-part");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, is.getEvaluationExpression().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = null;
            try {
                t = new ExprStat(createAssignment(name, is.getEvaluationExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            se = new Sequence(ld);
            se.merge(t.getFirst());
            // <-- 
            // Apply the rewrite to 'then-part'
            if (is.getThenStatements() != null) {
                Sequence thenpart = null;
                try {
                    thenpart = is.getThenStatements().visit(this).getFirst();
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                is.children[1] = new Block(thenpart);
            }
            // -->
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.getType();
            is.children[0] = ne;
        } else {
            Log.log("---- Case #2 IfStat: then-part (does not org.processj.yield)");
            if ( is.getThenStatements()!=null ) {
                Sequence thenpart = null;
                try {
                    thenpart = is.getThenStatements().visit(this).getFirst();
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                is.children[1] = new Block(thenpart);
            }
        }
        // If we skip the first part, then expr() does not org.processj.yield!
        // Now check the else-part which it is already a Block
        Pair<Sequence, Expression> elsepart = null;
        if (is.getElseBody() != null) {
            Log.log("---- Case #3 IfStat: else-part");
            try {
                elsepart = is.getElseBody().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            Sequence stmt = elsepart.getFirst();
            if (log) {
                System.out.println("====== BEGIN IfState::elsepart() ======");
                try {
                    stmt.visit(new PrettyPrinter());
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
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
        Sequence<Expression> params = in.getParameters();
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
                Pair<Sequence, Expression> t = null;
                try {
                    t = new ExprStat(createAssignment(name, e)).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                se.append(ld);
                se.merge(t.getFirst());
                NameExpr ne = new NameExpr(new Name(name));
                ne.type = ld.getType();
                params.insert(i, ne);
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
            Pair<Sequence, Expression> t = null;
            try {
                t = new ExprStat(createAssignment(name, ae.targetExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
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
            Pair<Sequence, Expression> t1 = null;
            try {
                t1 = new ExprStat(createAssignment(name1, ae.targetExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            // T t2; where T represents the type of e2
            LocalDecl ld2 = createLocalDecl(name2, ae.indexExpression().type);
            // Rewrite the expression to t2 = e2;
            Pair<Sequence, Expression> t2 = null;
            try {
                t2 = new ExprStat(createAssignment(name2, ae.indexExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            Sequence se = new Sequence(ld1);
            se.merge(t1.getFirst());
            se.append(ld2);
            se.merge(t2.getFirst());
            NameExpr ne1 = new NameExpr(new Name(name1));
            ne1.type = ld1.getType();
            NameExpr ne2 = new NameExpr(new Name(name2));
            ne2.type = ld2.getType();
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
        if (ce.getChannelType().doesYield()) {
            Log.log("---- Case #1");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, ce.getChannelType().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = null;
            try {
                t = new ExprStat(createAssignment(name, ce.getChannelType())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.getType();
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
        if (st.getEvaluationExpression().doesYield()) {
            Log.log("---- Case #1 SwitchStat: expr");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, st.getEvaluationExpression().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = null;
            try {
                t = new ExprStat(createAssignment(name, st.getEvaluationExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            se.append(ld);
            se.merge(t.getFirst());
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.getType();
            st.children[0] = ne;
        }
        // If we skip the first part, then expr() does not org.processj.yield!
        // Now check the else-part which it is already a Block
        if (st.switchBlocks().size() > 0) {
            Log.log("---- Case #2 SwitchStat: switch-block");
            Sequence<SwitchGroup> sg = st.switchBlocks();
            for (int i = 0; i < sg.size(); ++i)
                try {
                    sg.child(i).visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
        }
        se.append(st);
        p = new Pair<>(se, null);
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitSwitchGroup(SwitchGroup sg) {
        Log.log(sg, "Visiting a SwitchGroup");
        Sequence<Statement> se = sg.getStatements();
        for (int i = 0; i < se.size(); ++i) {
            Pair<Sequence, Expression> p = null;
            try {
                p = se.child(i).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            if (p != null) {
                if (p.getFirst().size() > 1)
                    se.insert(i, new Block(p.getFirst()));
                else
                    se.insert(i, (Statement) p.getFirst().child(0));
            }
        }
        return (Pair<Sequence, Expression>) null;
    }
    
    @Override
    public Pair<Sequence, Expression> visitTernary(Ternary te) {
        Log.log(te, "Visiting a Ternary");
        if (te.thenPart().doesYield() || te.elsePart().doesYield())
            ; // TODO: Throw error message??
        Pair<Sequence, Expression> p = null;
        if (te.getEvaluationExpression().doesYield()) {
            Log.log("---- Case #1 Ternary");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, te.getEvaluationExpression().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = null;
            try {
                t = new ExprStat(createAssignment(name, te.getEvaluationExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.getType();
            p = new Pair<>(se, new Ternary(ne, te.thenPart(), te.elsePart()));
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
        if (ts.getTimerExpression().doesYield() && !ts.getDelayExpression().doesYield()) {
            Log.log("---- Case #1 TimeoutStat");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, ts.getTimerExpression().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = null;
            try {
                t = new ExprStat(createAssignment(name, ts.getTimerExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.getType();
            se.append(new TimeoutStat(ne, ts.getDelayExpression()));
            p = new Pair<>(se, null);
        } else if (ts.getDelayExpression().doesYield()) {
            Log.log("---- Case #2 TimeoutStat");
            String name1 = nextTemp();
            String name2 = nextTemp();
            // T t1; where T represents the type of e1
            LocalDecl ld1 = createLocalDecl(name1, ts.getTimerExpression().type);
            // Rewrite the expression to t1 = e1;
            Pair<Sequence, Expression> t1 = null;
            try {
                t1 = new ExprStat(createAssignment(name1, ts.getTimerExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            // T t2; where T represents the type of e2
            LocalDecl ld2 = createLocalDecl(name2, ts.getDelayExpression().type);
            // Rewrite the expression to t2 = e2;
            Pair<Sequence, Expression> t2 = null;
            try {
                t2 = new ExprStat(createAssignment(name2, ts.getDelayExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            Sequence se = new Sequence(ld1);
            se.merge(t1.getFirst());
            se.append(ld2);
            se.merge(t2.getFirst());
            NameExpr ne1 = new NameExpr(new Name(name1));
            ne1.type = ld1.getType();
            NameExpr ne2 = new NameExpr(new Name(name2));
            ne2.type = ld2.getType();
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
        if (up.getExpression().doesYield())
            ; // TODO: Throw error message??
        return new Pair<>(new Sequence(), up);
    }
    
    @Override
    public Pair<Sequence, Expression> visitUnaryPreExpr(UnaryPreExpr up) {
        Log.log(up, "Visitng a UnaryPreExpr");
        if (UnaryPreExpr.PLUSPLUS == up.getOperator() || UnaryPreExpr.MINUSMINUS == up.getOperator())
            ; // TODO: Throw error message??
        Pair<Sequence, Expression> p = null;
        if (up.doesYield()) {
            Log.log("---- Case #1 UnaryPreExpr");
            String name = nextTemp();
            // T t; where T represents the type of e
            LocalDecl ld = createLocalDecl(name, up.getExpression().type);
            // Rewrite the expression to t = e;
            Pair<Sequence, Expression> t = null;
            try {
                t = new ExprStat(createAssignment(name, up.getExpression())).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            Sequence se = new Sequence(ld);
            se.merge(t.getFirst());
            NameExpr ne = new NameExpr(new Name(name));
            ne.type = ld.getType();
            p = new Pair<>(se, new UnaryPreExpr(ne, up.getOperator()));
        } else {
            Log.log("---- Case #2 UnaryPreExpr");
            p = new Pair<>(new Sequence(), up);
        }
        return p;
    }
    
    @Override
    public Pair<Sequence, Expression> visitAltStat(AltStat as) {
        Log.log(as, "Visiting an AltStat");
        Sequence<AltCase> body = as.getStatements();
        for (int i = 0; i < body.size(); ++i)
            try {
                body.child(i).visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        return new Pair<>(new Sequence(as), null);
    }
    
    @Override
    public Pair<Sequence, Expression> visitAltCase(AltCase ac) {
        Log.log(ac, "Visiting an AltCase");
        Pair<Sequence, Expression> p = null;
        // Rewrite the guard statement if needed (if null => nested alt)
        if (ac.getGuard() != null)
            try {
                p = ac.getGuard().visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
        // Rewrite the statement if needed
        try {
            p = ac.getStatements().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
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
        Statement stat = gu.getStatement();
        if (stat instanceof ExprStat) {
            ExprStat es = (ExprStat) stat;
            try {
                p = es.visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
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
