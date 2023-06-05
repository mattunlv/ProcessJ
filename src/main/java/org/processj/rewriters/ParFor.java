package org.processj.rewriters;

import org.processj.ast.expression.Assignment;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;
import org.processj.ast.*;
import java.util.ArrayList;

public class ParFor implements Visitor<AST> {

    private boolean inParFor = false;
    private ForStat currentParFor = null;

    public AST visitForStat(ForStat fs) {
        if (fs.isPar()) {
            ForStat oldForStat = currentParFor;
            currentParFor = fs;
            boolean oldInParFor = inParFor;
            inParFor = true;
            fs.vars = new ArrayList<Expression>();
            try {
                fs.getEvaluationExpression().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }
            try {
                fs.getIncrementExpression().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }
            try {
                fs.getStatement().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }
            currentParFor = oldForStat;
            inParFor = oldInParFor;
        }
        return null;
    }

    public AST visitAssignment(Assignment as) {
        if (inParFor) {
            currentParFor.vars.add(as.left());
        }
        try {
            as.right().visit(this);
        } catch (org.processj.Phase.Error error) {
            throw new RuntimeException(error);
        }
        return null;
    }

    public AST visitUnaryPreExpr(UnaryPreExpr up) {
        if (inParFor) {
            if (up.getOperator() == UnaryPreExpr.PLUSPLUS || up.getOperator() == UnaryPreExpr.MINUSMINUS) {
                currentParFor.vars.add(up.expr());
            }
        }
        return null;
    }

    public AST visitUnaryPostExpr(UnaryPostExpr up) {
        if (inParFor) {
            if (up.op() == UnaryPreExpr.PLUSPLUS || up.op() == UnaryPreExpr.MINUSMINUS) {
                currentParFor.vars.add(up.getExpression());
            }
        }
        return null;
    }

}