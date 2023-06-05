package org.processj.rewriters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.processj.ast.*;
import org.processj.ast.expression.Expression;
import org.processj.utilities.printers.PrettyPrinter;
import org.processj.utilities.Log;
import org.processj.utilities.Pair;
import org.processj.utilities.Visitor;

/**
 * @author ben
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ParBlockRewrite implements Visitor<Pair<Sequence, Sequence>> {

    ArrayList<Expression> barriers = new ArrayList<>();
    
    HashMap<String, Integer> enrolls = new HashMap<>();
    
    private void andEnroll(HashMap<String, Integer> hm, Expression e) {
        if (hm.isEmpty())
            hm.put(((NameExpr) e).toString(), 1);
        else {
            Set<String> set = hm.keySet();
            for (String e1 : set) {
                String e2 = ((NameExpr) e).toString();
                if (e1.equals(e2)) {
                    hm.put(e1, hm.get(e1) + 1);
                    return;
                }
            }
            hm.put(((NameExpr) e).toString(), 1);
        }
    }

    private void addBarriers(Sequence<Expression> se, Expression e) {
        if (se.size() > 0) {
            boolean found = false;
            for (int i = 0; !found && i < se.size(); ++i) {
                NameExpr ne1 = (NameExpr) se.child(i);
                NameExpr ne2 = (NameExpr) e;
                if (ne1.toString().equals(ne2.toString()))
                    found = true;
            }
            if (!found)
                se.append(e);
        } else
            se.append(e);
    }

    public ParBlockRewrite() {
        Log.logHeader("*****************************************");
        Log.logHeader("*   P A R B L O C K -  R E W R I T E    *");
        Log.logHeader("*****************************************");
    }

    public Pair<Sequence, Sequence> visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a ProcTypeDecl");
        try {
            pd.getBody().visit(this);
        } catch (org.processj.Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
    }
    
    public Pair<Sequence, Sequence> visitBlock(Block bl) {
        Log.log(bl, "Visiting a Block");
        Sequence<Statement> se = bl.stats();
        for (int i = 0; i < se.size(); ++i) {
            if (se.child(i) instanceof ParBlock) {
                HashMap<String, Integer> prevEnrolls = enrolls;
                enrolls = new HashMap<>();
                Pair<Sequence, Sequence> p = null;
                try {
                    p = se.child(i).visit(this);
                } catch (org.processj.Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (p != null) {
                    ParBlock par = new ParBlock(p.getFirst(), p.getSecond());
                    par.enrolls = enrolls;
                    if (Log.doLog)
                        try {
                            par.visit(new PrettyPrinter());
                        } catch (org.processj.Phase.Error error) {
                            throw new RuntimeException(error);
                        }
                    se.set(i, par);
                }
                enrolls = prevEnrolls;
            }
        }
        return null;
    }
    
    public Pair<Sequence, Sequence> visitParBlock(ParBlock pb) {
        Log.log(pb, "Visiting a ParBlock");
        // Don't generate code for an empty par statement
        if (pb.stats().size() == 0)
            return null;
        Sequence<Statement> stmts = new Sequence();
        Sequence<Expression> barSeq = new Sequence();
        barSeq.merge(pb.barriers());
        // Rewrite and flatten the par-block
        Sequence<Statement> statements = pb.stats();
        for (Statement st : statements) {
            if (st instanceof ParBlock) {
                Pair<Sequence, Sequence> p = null;
                try {
                    p = (Pair<Sequence, Sequence>) st.visit(this);
                } catch (org.processj.Phase.Error error) {
                    throw new RuntimeException(error);
                }
                if (p.getFirst().size() > 0)
                    stmts.merge(p.getFirst());
                if (p.getSecond().size() > 0) {
                    Sequence<Expression> se = p.getSecond();
                    for (Expression e : se)
                        addBarriers(barSeq, e);
                }
            } else {
                if (pb.barriers().size() > 0) {
                    st.barrierNames = new Sequence<>();
                    for (Expression e : pb.barriers())
                        st.barrierNames.append(e);
                    for (Expression e : st.barrierNames)
                        andEnroll(enrolls, e);
                }
                // No rewrite needed for this section of code :-)
                stmts.append(st);
            }
        }
        return new Pair<>(stmts, barSeq);
    }
}
