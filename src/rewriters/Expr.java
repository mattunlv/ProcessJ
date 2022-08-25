package rewriters;

import ast.*;
import utilities.Visitor;
import printers.*;

public class Expr extends Visitor<AST> {
    private int tempCounter = 0;
    private String nextTemp() {
        return "temp" + tempCounter++;
    }

    public AST visitAssignment(Assignment as) {
        if (as.right().doesYield()) {
            // If the assingment already looks like v = c.read() then just return it.
            if (as.right() instanceof ChannelReadExpr) {
                System.out.println("[Expr]: Right hand side is a single channel read.");
                return new Block(new Sequence(new ExprStat(as)));
            } else if (as.right() instanceof BinaryExpr) {
                System.out.println("[Expr]: Right hand side is a yielding binary expression.");
                BinaryExpr be = (BinaryExpr)as.right();
                Expression left  = be.left();
                Expression right = be.right();
                Sequence se = new Sequence();
                // we now have three cases:
                // 1.) left yields, right does not
                // 2.) left yeilds, right also yields
                // 3.) left does not yield, but right does.
                if (left.doesYield() && !right.doesYield()) {
                    System.out.println("[Expr]: Right hand side is a yielding binary expression that yields on the left expression only.");
                    // Generate the following code:
                    // for v = e1 <op> e2:
                    //
                    // T1 temp1;
                    // temp1 = e1;     [ this line gets rewritten ]
                    // v = t1 <op> e2;
                    String temp1 = nextTemp();
                    // T1 temp; (T1 is the type of e1)
                    se.append(new LocalDecl(left.type, new Var(new Name(temp1), null), false));
                    // temp1 = e1
                    ExprStat a1 = new ExprStat(new Assignment(new NameExpr(new Name(temp1)), left, Assignment.EQ));
                    // Rewrite 'temp1 = e1'
                    Block b1 = (Block)a1.visit(this);
                    // We got a block back that looks like { .... }
                    // Grab the statements from that block and merge them to 'se'
                    se.merge(b1.stats());
                    // append 'v = t1 <op> e2'
                    se.append(new ExprStat(new Assignment(as.left(),
                                             new BinaryExpr(new NameExpr(new Name(temp1)), right, be.op()), as.op())));
                    Block bl = new Block(se);
                    System.out.println("Result of rewriting (Binary expression, left yield, right does not):");
                    bl.visit(new ParseTreePrinter());
                    System.out.println("Pretty Printed:");
                    bl.visit(new PrettyPrinter());
                    System.out.println();
                    return bl;
                } else if (left.doesYield() && right.doesYield()) {
                    System.out.println("[Expr]: Right hand side is a yielding binary expression that yields on both left and right expression.");
                    // Generate the following code:
                    // for v = e1 <op> e2:
                    //
                    // T1 temp1;
                    // temp1 = e1;     [ this line gets rewritten ]
                    // T2 temp2;
                    // temp2 = e2      [ this line gets rewritten ]
                    //
                    // v = t1 <op> t2;
                    // Deal with e1 first.
                    String temp1 = nextTemp();
                    // T1 temp1; (T1 is the type of e1)
                    se.append(new LocalDecl(left.type, new Var(new Name(temp1), null), false));
                    // temp1 = e1
                    ExprStat a1 = new ExprStat(new Assignment(new NameExpr(new Name(temp1)), left, Assignment.EQ));
                    // Rewrite 'temp1 = e1'
                    Block b1 = (Block)a1.visit(this);
                    // We got a block back that looks like { .... }
                    // Grab the statements from that block and merge them to 'se'
                    se.merge(b1.stats());
                    // Deal with e2 then.
                    String temp2 = nextTemp();
                    // T2 temp2; (T2 is the type of e2)
                    se.append(new LocalDecl(right.type, new Var(new Name(temp2), null), false));
                    // temp2 = e2
                    ExprStat a2 = new ExprStat(new Assignment(new NameExpr(new Name(temp2)), right, Assignment.EQ));
                    // Rewrite 'temp2 = e2'
                    Block b2 = (Block)a2.visit(this);
                    // We got a block back that looks like { .... }
                    // Grab the statements from that block and merge them to 'se'
                    se.merge(b2.stats());
                    // Append 'v = t1 <op> t2'
                    se.append(new ExprStat(new Assignment(as.left(),
                                           new BinaryExpr(new NameExpr(new Name(temp1)), new NameExpr(new Name(temp2)), be.op()), as.op())));
                    Block bl = new Block(se);
                    System.out.println("Result of rewriting (Binary expression, left yield, right does not):");
                    bl.visit(new ParseTreePrinter());
                    System.out.println("Pretty Printed:");
                    bl.visit(new PrettyPrinter());
                    System.out.println();
                    return bl;
                } else {
                    System.out.println("[Expr]: Right hand side is a yielding binary expression that yields on the left expression only.");
                    // Generate the following code:
                    // for v = e1 <op> e2:
                    //
                    // T2 temp2;
                    // temp2 = e2;     [ this line gets rewritten ]
                    // v = e1 <op> tt;
                    String temp2 = nextTemp();
                    // T2 temp; (T2 is the type of e2)
                    se.append(new LocalDecl(right.type, new Var(new Name(temp2), null), false));
                    // temp2 = e2
                    ExprStat a2 = new ExprStat(new Assignment(new NameExpr(new Name(temp2)), right, Assignment.EQ));
                    // Rewrite 'temp2 = e2'
                    Block b2 = (Block)a2.visit(this);
                    // We got a block back that looks like { .... }
                    // Grab the statements from that block and merge them to 'se'
                    se.merge(b2.stats());
                    // append 'v = e1 <op> t2'
                    se.append(new ExprStat(new Assignment(as.left(),
                                             new BinaryExpr(left,new NameExpr(new Name(temp2)), be.op()), as.op())));
                    Block bl = new Block(se);
                    System.out.println("Result of rewriting (Binary expression, left yield, right does not):");
                    bl.visit(new ParseTreePrinter());
                    System.out.println("Pretty Printed:");
                    bl.visit(new PrettyPrinter());
                    System.out.println();
                    return bl;
                }
            } else if (as.right() instanceof UnaryPreExpr) {
                Sequence se = new Sequence();
                UnaryPreExpr up = (UnaryPreExpr)as.right();
                Expression expr = up.expr();
                // Generate the following code:
                // for v = <op>e:
                //
                // T temp;
                // temp = e   [ This line gets rewritten ]
                // v = temp;
                String temp = nextTemp();
                // T temp; (T is the type of e)
                se.append(new LocalDecl(expr.type, new Var(new Name(temp), null), false));
                // temp = e
                ExprStat a = new ExprStat(new Assignment(new NameExpr(new Name(temp)), expr, Assignment.EQ));
                // Rewrite 'temp = e'
                Block b = (Block)a.visit(this);
                // We got a block back that looks like { .... }
                // Grab the statements from that block and merge them to 'se'
                se.merge(b.stats());
                // append 'v = temp'
                se.append(new ExprStat(new Assignment(as.left(), new UnaryPreExpr(new NameExpr(new Name(temp)), up.op()), as.op())));
                Block bl = new Block(se);
                System.out.println("Result of rewriting (Unary Pre Expressions):");
                bl.visit(new ParseTreePrinter());
                System.out.println("Pretty Printed:");
                bl.visit(new PrettyPrinter());
                System.out.println();
                return bl;
            } else if (as.right() instanceof Invocation) {



                return new Block(new Sequence(new ExprStat(as)));
            }
            // We should never get here.
            return null;
        } else {
            System.out.println("[Expr]: Right-hand side of expression is not yielding.");
            System.out.println("Result of rewriting:");
            as.visit(new ParseTreePrinter());
            System.out.println("Pretty Printed:");
            as.visit(new PrettyPrinter());
            return new Block(new Sequence(new ExprStat(as)));
        }
    }




    public AST visitExprStat(ExprStat es) {
        System.out.println("[Expr]: Visiting an ExprStat");
        Statement a = (Statement)es.expr().visit(this);
        if (a instanceof Block)
            return a;
        else
            return es;
    }

    public AST visitLocalDecl(LocalDecl ld) {
        System.out.println("[Expr] Visiting a LocalDecl");
        return ld;
    }


    public AST visitBlock(Block bl) {
        System.out.println("[Expr]: Visiting a Block");
        System.out.println("[Expr]: Block before of rewriting:");
        bl.visit(new ParseTreePrinter());
        System.out.println("[Expr]: Block before rewriting:");
        bl.visit(new PrettyPrinter());

        for (int i = 0; i<bl.stats().size(); i++) {
            AST s = bl.stats().child(i);
            AST a = s.visit(this);
            System.out.println("Result of rewriting this one statement:");
            a.visit(new ParseTreePrinter());
            // Don't bother with the Block wrapper if there is only one statement.
            if (a instanceof Block && ((Block)a).stats().size() == 1)
                bl.stats().set(i, ((Block)a).stats().child(0));
            else
                bl.stats().set(i, (Statement)a);
            System.out.println("---------------------------------------");
        }

        System.out.println("Expr]: Block after rewriting:");
        bl.visit(new ParseTreePrinter());
        System.out.println("Expr]: Block after rewriting:");
        bl.visit(new PrettyPrinter());
        return bl;
    }

}
