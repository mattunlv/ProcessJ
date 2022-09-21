package parallel_usage_check;

import java.util.Hashtable;

import ast.AST;
import ast.AltStat;
import ast.ArrayAccessExpr;
import ast.Assignment;
import ast.NameExpr;
import ast.ParBlock;
import ast.RecordAccess;
import ast.UnaryPostExpr;
import ast.UnaryPreExpr;
import utilities.PJMessage;
import utilities.PJBugManager;
import utilities.Log;
import utilities.MessageType;
import utilities.Visitor;
import utilities.VisitorMessageNumber;

public class ParallelUsageCheck extends Visitor<Object> {

    private Hashtable<String, AST> readSet;
    private Hashtable<String, AST> writeSet;

    private boolean inPar = false;

    public ParallelUsageCheck() {
        Log.logHeader("********************************************");
        Log.logHeader("* P A R A L L E L   U S A G E    C H E C K *");
        Log.logHeader("********************************************");
    }

    public Object visitParBlock(ParBlock pb) {
        Log.log(pb, "Visiting a Par Block.");
        boolean oldInPar = inPar;
        inPar = true;

        // if this is an outermost par block - create new tables.
        if (!oldInPar) {
            Log.log(pb, "Outermost par - creating new read and wrilte sets.");
            readSet = new Hashtable<String, AST>();
            writeSet = new Hashtable<String, AST>();
        }

        super.visitParBlock(pb);

        inPar = oldInPar;
        return null;
    }

    public Object visitRecordAccess(RecordAccess ra) {
        if (inPar) {
            Log.log(ra, "Visiting a RecordAccess.");
            String name = ra.toString();
            if (writeSet.containsKey(name)) {
                PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(ra)
                            .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_700)
                            .addArguments(name)
                            .build());
            } else {
                Log.log(ra, "RecordAccess: '" + name + "' is added to the read set.");
                readSet.put(name, ra);
            }
        }
        return null;
    }

    public Object visitArrayAccessExpr(ArrayAccessExpr aae) {
        if (inPar) {
            Log.log(aae, "Visiting a ArrayAccessExpr.");
            String name = aae.toString();
            if (writeSet.containsKey(name)) {
                PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(aae)
                            .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_701)
                            .addArguments(name)
                            .build());
            } else {
                Log.log(aae, "ArrayAccessExpr: '" + name + "' is added to the read set.");
                readSet.put(name, aae);
                PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(aae)
                            .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_702)
                            .addArguments(name)
                            .build());
                aae.index().visit(this);
            }
        }
        return null;
    }

    public Object visitAltStat(AltStat as) {
        /* alt {
             x = c.read() : { x = 1; }
               }
           causes issues!
         */
        Log.log(as, "AltStat ignore in parallel usage checking.");
        return null;
    }

    public Object visitAssignment(Assignment as) {
        if (inPar) {
            Log.log(as, "Visiting an Assignment.");
            // the left hand side must go into the read set!
            // can be NameExpr, ArrayAccessExpr, or RecordAccess
            if (as.left() instanceof NameExpr) {
                String name = ((NameExpr) as.left()).name().getname();
                if (writeSet.containsKey(name))
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(as)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_703)
                                .addArguments(name)
                                .build());
                else {
                    Log.log(as, "NameExpr: '" + name + "' is added to the write set.");
                    writeSet.put(name, as.left());
                }
            } else if (as.left() instanceof RecordAccess) {
                // TODO: the toString() of as.left() if probably not complete
                String name = as.left().toString();
                if (writeSet.containsKey(name))
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(as)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_704)
                                .addArguments(name)
                                .build());
                else {
                    Log.log(as, "RecordAccess: '" + name + "' is added to the write set.");
                    writeSet.put(name, as.left());
                }
            } else if (as.left() instanceof ArrayAccessExpr) {
                // TODO: the toString() of as.left() is probably not complete!
                String name = as.left().toString();
                if (writeSet.containsKey(name))
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(as)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_705)
                                .addArguments(name)
                                .build());
                else {
                    Log.log(as, "ArrayAccessExpr: '" + name + "' is added to the write set.");
                    writeSet.put(name, as.left());
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(as.left())
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_706)
                                .addArguments(name)
                                .build());
                }
            }
        }
        return null;
    }

    public Object visitNameExpr(NameExpr ne) {
        if (inPar) {
            Log.log(ne, "Visiting a NameExpr.");
            // This should only be reads!
            String name = ne.name().getname();
            if (writeSet.containsKey(name))
                PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(ne)
                            .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_707)
                            .addArguments(name)
                            .build());
            else {
                Log.log(ne, "NameExpr: '" + name + "' is added to the read set.");
                readSet.put(name, ne);
            }
        }
        return null;
    }

    public Object visitUnaryPostExpr(UnaryPostExpr up) {
        if (inPar) {
            Log.log(up, "Visiting a UnaryPostExpr.");
            if (up.expr() instanceof NameExpr) {
                String name = ((NameExpr) up.expr()).name().getname();
                if (writeSet.containsKey(name))
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(up)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_708)
                                .addArguments(name)
                                .build());
                else {
                    Log.log(up, "NameExpr: '" + name + "' is added to the write set.");
                    writeSet.put(name, up.expr());
                }
            } else if (up.expr() instanceof RecordAccess) {
                // TODO: the toString() of up.expr() if probably not complete
                String name = up.expr().toString();
                if (writeSet.containsKey(name))
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(up)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_709)
                                .addArguments(name)
                                .build());
                else {
                    Log.log(up, "RecordAccess: '" + name + "' is added to the write set.");
                    writeSet.put(name, up.expr());
                }
            } else if (up.expr() instanceof ArrayAccessExpr) {
                // TODO: the toString() of up.expr() is probably not complete!
                String name = up.expr().toString();
                if (writeSet.containsKey(name))
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(up)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_710)
                                .addArguments(name)
                                .build());
                else {
                    Log.log(up, "ArrayAccessExpr: '" + name + "' is added to the write set.");
                    writeSet.put(name, up.expr());
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(up.expr())
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_711)
                                .addArguments(name)
                                .build());
                }
            }
        }
        return null;
    }

    public Object visitUnaryPreExpr(UnaryPreExpr up) {
        if (inPar) {
            Log.log(up, "Visiting a UnaryPreExpr.");
            if (up.op() == UnaryPreExpr.PLUSPLUS
                    || up.op() == UnaryPreExpr.MINUSMINUS) {
                if (up.expr() instanceof NameExpr) {
                    String name = ((NameExpr) up.expr()).name().getname();
                    if (writeSet.containsKey(name))
                        PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                    .addAST(up)
                                    .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_712)
                                    .addArguments(name)
                                    .build());
                    else {
                        Log.log(up, "NameExpr: '" + name + "' is added to the write set.");
                        writeSet.put(name, up.expr());
                    }
                } else if (up.expr() instanceof RecordAccess) {
                    // TODO: the toString() of up.expr() if probably not complete
                    String name = up.expr().toString();
                    if (writeSet.containsKey(name))
                        PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                    .addAST(up)
                                    .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_713)
                                    .addArguments(name)
                                    .build());
                    else {
                        Log.log(up, "RecordAccess: '" + name + "' is added to the write set.");
                        writeSet.put(name, up.expr());
                    }
                } else if (up.expr() instanceof ArrayAccessExpr) {
                    // TODO: the toString() of up.expr() is probably not complete!
                    String name = up.expr().toString();
                    if (writeSet.containsKey(name))
                        PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                    .addAST(up)
                                    .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_714)
                                    .addArguments(name)
                                    .build());
                    else {
                        Log.log(up, "ArrayAccessExpr: '" + name + "' is added to the write set.");
                        writeSet.put(name, up.expr());
                        PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                    .addAST(up.expr())
                                    .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_715)
                                    .addArguments(name)
                                    .build());
                    }
                }
            } else
                up.expr().visit(this);
        }
        return null;
    }

}