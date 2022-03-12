package rewriters;

import utilities.Visitor;
import ast.*;
import java.util.ArrayList;

public class ParFor extends Visitor<AST> {

    private boolean inParFor = false;
    private ForStat currentParFor = null;

    public AST visitForStat(ForStat fs) {
        if (fs.isPar()) {
            ForStat oldForStat = currentParFor;
            currentParFor = fs;
            boolean oldInParFor = inParFor;
            inParFor = true;
            fs.vars = new ArrayList<Expression>();
            fs.expr().visit(this);
            fs.incr().visit(this);
            fs.stats().visit(this);
            currentParFor = oldForStat;
            inParFor = oldInParFor;
        }
        return null;
    }

    public AST visitAssignment(Assignment as) {
        if (inParFor) {
            currentParFor.vars.add(as.left());
        }
        as.right().visit(this);
        return null;
    }

    public AST visitUnaryPreExpr(UnaryPreExpr up) {
        if (inParFor) {
            if (up.op() == UnaryPreExpr.PLUSPLUS || up.op() == UnaryPreExpr.MINUSMINUS) {
                currentParFor.vars.add(up.expr());
            }
        }
        return null;
    }

    public AST visitUnaryPostExpr(UnaryPostExpr up) {
        if (inParFor) {
            if (up.op() == UnaryPreExpr.PLUSPLUS || up.op() == UnaryPreExpr.MINUSMINUS) {
                currentParFor.vars.add(up.expr());
            }
        }
        return null;
    }

}