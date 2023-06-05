package org.processj.rewriters;

import org.processj.ast.AST;
import org.processj.ast.expression.Assignment;
import org.processj.ast.Block;
import org.processj.ast.DoStat;
import org.processj.ast.ExprStat;
import org.processj.ast.expression.Expression;
import org.processj.ast.ForStat;
import org.processj.ast.LocalDecl;
import org.processj.ast.Name;
import org.processj.ast.NameExpr;
import org.processj.ast.PrimitiveLiteral;
import org.processj.ast.PrimitiveType;
import org.processj.ast.ProcTypeDecl;
import org.processj.ast.Sequence;
import org.processj.ast.Statement;
import org.processj.ast.Token;
import org.processj.ast.Var;
import org.processj.ast.WhileStat;

/**
 * Fix for unreachable code due to infinite loop.
 * 
 * @author ben
 */
public class InfiniteLoopRewrite {
	
	int tempCounter = 0;

    private String nextTemp() {
        return "foreverLoop" + tempCounter++ + "$";
    }
    
    /**
     * This tree-traversal method rewrites 'WhileStat', 'DoStat', and 'ForStat'
     * parse-tree nodes if the code represented by each of these nodes cannot
     * or may not run to completion due to infinite loop.
     * 
     * 1.) While-loop rewrite:
     *                                                  boolean foreverLoop0 = true;
     *      while (true) {                              while (foreverLoop0) {
     *          while (true) {                              boolean foreverLoop1 = true;
     *              ...                 becomes             while (foreverLoop1) {
     *          }                                               ...
     *      }                                               }
     *                                                  }
     * 
     * 2.) Do-while loop rewrite:
     *                                                  boolean foreverLoop0 = true;
     *      do {                                        do {
     *          do {                                        boolean foreverLoop1 = true;
     *          ...                     becomes             do {
     *          } while (true)                                  ...
     *      } while (true);                                 } while (foreverLoop1);
     *                                                  } while (foreverLoop0);
     * 
     * 3.) For-loop rewrite:
     *                                                  boolean foreverLoop0 = true;
     *      for (...; ...; ...;) {                      for (...; foreverLoop0; ...) {
     *          for (...; ...; ...;) {                      boolean foreverLoop1 = true;
     *              ...                 becomes             for (...; foreverLoop1; ...) {
     *          }                                               ...
     *      }                                               }
     *                                                  }
     */
    public void go(AST a) {
        if (a instanceof ProcTypeDecl) {
            // Rewrite the body if needed
            ProcTypeDecl pd = (ProcTypeDecl) a;
            go(pd.getBody());
        } else if (a instanceof Sequence) {
            Sequence<AST> s = (Sequence<AST>) a;
            // Iterate through all the nodes in the sequence
            for (int i = 0; i < s.size(); ++i) {
                if (s.child(i) != null && s.child(i) instanceof Statement) {
                    Statement stat = (Statement) s.child(i);
                    if (stat instanceof WhileStat) { // WhileStat -- done
                        WhileStat ws = (WhileStat) stat;
                        if (ws.foreverLoop) {
                            String temp = nextTemp();
                            // Create a local declaration to replace the boolean literal value with in the while-stmt
                            LocalDecl ld = new LocalDecl(new PrimitiveType(PrimitiveType.BooleanKind),
                                    new Var(new Name(temp), null), true /* constant */);
                            // Replace the boolean literal value with the new local variable
                            NameExpr ne = new NameExpr(new Name(temp));
                            ne.type = ld.getType();
                            ExprStat es = new ExprStat(new Assignment(ne, ws.getEvaluationExpression(), Assignment.EQ));
                            // Rewrite the expression for the while-loop
                            ws.children[0] = ne;
                            // Rewrite the i'th sequence of statements
                            Sequence<Statement> stats = new Sequence<Statement>();
                            stats.append(ld);
                            stats.append(es);
                            stats.append(stat);
                            Block b = new Block(stats);
                            s.set(i, b);
                        }
                        go(ws.getStatement());
                    } else if (stat instanceof DoStat) { // DoStat -- done
                        DoStat ds = (DoStat) stat;
                        if (ds.foreverLoop) {
                            String temp = nextTemp();
                            // Create a local declaration to replace the boolean literal value with in the do-stmt
                            LocalDecl ld = new LocalDecl(new PrimitiveType(PrimitiveType.BooleanKind),
                                    new Var(new Name(temp), null), true /* constant */);
                            // Replace the boolean literal value with the new local variable
                            NameExpr ne = new NameExpr(new Name(temp));
                            ne.type = ld.getType();
                            ExprStat es = new ExprStat(new Assignment(ne, ds.getEvaluationExpression(), Assignment.EQ));
                            // Rewrite the expression for the do-while loop
                            ds.children[1] = ne;
                            // Rewrite the i'th sequence of statements
                            Sequence<Statement> stats = new Sequence<Statement>();
                            stats.append(ld);
                            stats.append(es);
                            stats.append(stat);
                            Block b = new Block(stats);
                            s.set(i, b);
                        }
                        go(ds.getStatement());
                    } else if (stat instanceof ForStat) { // ForStat -- done
                        ForStat fs = (ForStat) stat;
                        if (fs.foreverLoop) {
                            String temp = nextTemp();
                            // Create a local declaration to replace the boolean literal value with in the for-stmt
                            LocalDecl ld = new LocalDecl(new PrimitiveType(PrimitiveType.BooleanKind),
                                    new Var(new Name(temp), null), true /* constant */);
                            // Rewrite the expression if it isn't of the form:
                            //		for (...; true ; ...) S1
                            Expression newExpr = fs.getEvaluationExpression();
                            if (newExpr == null)
                                newExpr = new PrimitiveLiteral(new Token(0, Boolean.toString(true), 0, 0, 0), 0 /* kind */);
                            // Replace the boolean literal value with the new local variable
                            NameExpr ne = new NameExpr(new Name(temp));
                            ne.type = ld.getType();
                            ExprStat es = new ExprStat(new Assignment(ne, newExpr, Assignment.EQ));
                            // Rewrite the expression for the for-loop
                            fs.children[1] = ne;
                            // Rewrite the i'th sequence of statements
                            Sequence<Statement> stats = new Sequence<Statement>();
                            stats.append(ld);
                            stats.append(es);
                            stats.append(stat);
                            Block b = new Block(stats);
                            s.set(i, b);
                        }
                        go(fs.getStatement());
                    } else if (s.child(i) != null) // Block, IfStat, ParBlock, SwitchStat
                        go(s.child(i));
                } else if (s.child(i) != null)
                    go(s.child(i));
            }
        } else {
            for (int i = 0; i < a.nchildren; ++i) {
                if (a.children[i] != null)
                    go(a.children[i]);
            }
        }
    }
}
