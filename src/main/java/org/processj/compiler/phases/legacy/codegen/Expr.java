package org.processj.compiler.phases.legacy.codegen;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.Assignment;
import org.processj.compiler.ast.expression.BinaryExpr;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;
import org.processj.compiler.utilities.printers.ParseTreePrinter;
import org.processj.compiler.utilities.printers.PrettyPrinter;

public class Expr implements Visitor<AST> {
    private int tempCounter = 0;
    private String nextTemp() {
        return "temp" + tempCounter++;
    }

    public AST visitAssignment(Assignment as) {
        if (as.getRight().doesYield()) {
            // If the assingment already looks like v = c.read() then just return it.
            if (as.getRight() instanceof ChannelReadExpr) {
                System.out.println("[Expr]: Right hand side is a single channel read.");
                return new Block(new Sequence(new ExprStat(as)));
            } else if (as.getRight() instanceof BinaryExpr) {
                System.out.println("[Expr]: Right hand side is a yielding binary expression.");
                BinaryExpr be = (BinaryExpr)as.getRight();
                Expression left  = be.left();
                Expression right = be.right();
                Sequence se = new Sequence();
                // we now have three cases:
                // 1.) left yields, right does not
                // 2.) left yeilds, right also yields
                // 3.) left does not org.processj.yield, but right does.
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
                    Block b1 = null;
                    try {
                        b1 = (Block)a1.visit(this);
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
                    // We got a block back that looks like { .... }
                    // Grab the statements from that block and merge them to 'se'
                    se.merge(b1.getStatements());
                    // append 'v = t1 <op> e2'
                    se.append(new ExprStat(new Assignment(as.left(),
                                             new BinaryExpr(new NameExpr(new Name(temp1)), right, be.op()), as.getOperator())));
                    Block bl = new Block(se);
                    System.out.println("Result of rewriting (Binary expression, left org.processj.yield, right does not):");

                    System.out.println("Pretty Printed:");
                    try {
                        bl.visit(new PrettyPrinter());
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
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
                    Block b1 = null;
                    try {
                        b1 = (Block)a1.visit(this);
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
                    // We got a block back that looks like { .... }
                    // Grab the statements from that block and merge them to 'se'
                    se.merge(b1.getStatements());
                    // Deal with e2 then.
                    String temp2 = nextTemp();
                    // T2 temp2; (T2 is the type of e2)
                    se.append(new LocalDecl(right.type, new Var(new Name(temp2), null), false));
                    // temp2 = e2
                    ExprStat a2 = new ExprStat(new Assignment(new NameExpr(new Name(temp2)), right, Assignment.EQ));
                    // Rewrite 'temp2 = e2'
                    Block b2 = null;
                    try {
                        b2 = (Block)a2.visit(this);
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
                    // We got a block back that looks like { .... }
                    // Grab the statements from that block and merge them to 'se'
                    se.merge(b2.getStatements());
                    // Append 'v = t1 <op> t2'
                    se.append(new ExprStat(new Assignment(as.left(),
                                           new BinaryExpr(new NameExpr(new Name(temp1)), new NameExpr(new Name(temp2)), be.op()), as.getOperator())));
                    Block bl = new Block(se);
                    System.out.println("Result of rewriting (Binary expression, left org.processj.yield, right does not):");

                    System.out.println("Pretty Printed:");
                    try {
                        bl.visit(new PrettyPrinter());
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
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
                    Block b2 = null;
                    try {
                        b2 = (Block)a2.visit(this);
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
                    // We got a block back that looks like { .... }
                    // Grab the statements from that block and merge them to 'se'
                    se.merge(b2.getStatements());
                    // append 'v = e1 <op> t2'
                    se.append(new ExprStat(new Assignment(as.left(),
                                             new BinaryExpr(left,new NameExpr(new Name(temp2)), be.op()), as.getOperator())));
                    Block bl = new Block(se);
                    System.out.println("Result of rewriting (Binary expression, left org.processj.yield, right does not):");

                    System.out.println("Pretty Printed:");
                    try {
                        bl.visit(new PrettyPrinter());
                    } catch (Phase.Error error) {
                        throw new RuntimeException(error);
                    }
                    System.out.println();
                    return bl;
                }
            } else if (as.getRight() instanceof UnaryPreExpr) {
                Sequence se = new Sequence();
                UnaryPreExpr up = (UnaryPreExpr)as.getRight();
                Expression expr = up.getExpression();
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
                Block b = null;
                try {
                    b = (Block)a.visit(this);
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                // We got a block back that looks like { .... }
                // Grab the statements from that block and merge them to 'se'
                se.merge(b.getStatements());
                // append 'v = temp'
                se.append(new ExprStat(new Assignment(as.left(), new UnaryPreExpr(new NameExpr(new Name(temp)), up.getOperator()), as.getOperator())));
                Block bl = new Block(se);
                System.out.println("Result of rewriting (Unary Pre Expressions):");

                System.out.println("Pretty Printed:");
                try {
                    bl.visit(new PrettyPrinter());
                } catch (Phase.Error error) {
                    throw new RuntimeException(error);
                }
                System.out.println();
                return bl;
            } else if (as.getRight() instanceof Invocation) {



                return new Block(new Sequence(new ExprStat(as)));
            }
            // We should never get here.
            return null;
        } else {
            System.out.println("[Expr]: Right-hand side of expression is not yielding.");
            System.out.println("Result of rewriting:");
            System.out.println("Pretty Printed:");
            try {
                as.visit(new PrettyPrinter());
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            return new Block(new Sequence(new ExprStat(as)));
        }
    }




    public AST visitExprStat(ExprStat es) throws SymbolMap.Context.ContextDoesNotDefineScopeException {
        System.out.println("[Expr]: Visiting an ExprStat");
        Statement a = null;
        try {
            a = (Statement)es.getExpression().visit(this);
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        if (a instanceof Block)
            return a;
        else
            return es;
    }

    public AST visitLocalDecl(LocalDecl ld) {
        System.out.println("[Expr] Visiting a LocalDecl");
        return ld;
    }


    public AST visitBlock(Block bl) throws SymbolMap.Context.ContextDoesNotDefineScopeException {
        System.out.println("[Expr]: Visiting a Block");
        System.out.println("[Expr]: Block before of rewriting:");
        try {
            bl.visit(new ParseTreePrinter());
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println("[Expr]: Block before rewriting:");
        try {
            bl.visit(new PrettyPrinter());
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }

        for (int i = 0; i<bl.getStatements().size(); i++) {
            AST s = bl.getStatements().child(i);
            AST a = null;
            try {
                a = s.visit(this);
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            System.out.println("Result of rewriting this one statement:");
            try {
                a.visit(new ParseTreePrinter());
            } catch (Phase.Error error) {
                throw new RuntimeException(error);
            }
            // Don't bother with the Block wrapper if there is only one statement.
            //if(a instanceof Block && ((Block) a).getStatements().size() == 1)
            //    bl.getStatements().insert(i,  ((Block) a).getStatements().child(0));
            //else
            //    bl.getStatements().insert(i, (Statement) a);
            System.out.println("---------------------------------------");
        }

        System.out.println("Expr]: Block after rewriting:");
        try {
            bl.visit(new ParseTreePrinter());
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        System.out.println("Expr]: Block after rewriting:");
        try {
            bl.visit(new PrettyPrinter());
        } catch (Phase.Error error) {
            throw new RuntimeException(error);
        }
        return bl;
    }

}
