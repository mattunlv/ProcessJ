package org.processj.compiler.ast.expression.unary;

import java.math.BigDecimal;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class UnaryPreExpression extends Expression {

    public static final int PLUSPLUS = 0; // ++
    public static final int MINUSMINUS = 1; // --
    public static final int PLUS = 2; // +
    public static final int MINUS = 3; // -
    public static final int COMP = 4; // ~
    public static final int NOT = 5; // !
    private static final String[] opSyms = { "++", "--", "+", "-", "~", "!" };

    private int kind;

    public UnaryPreExpression(Expression expr, int op) {
        super(new AST[] { expr });
        kind = op;
    }

    public Expression getExpression() {
        return (Expression) children[0];
    }

    public int getOperator() {
        return kind;
    }

    public String opString() {
        return opSyms[kind];
    }

    public boolean isConstant() {
        if (kind != PLUSPLUS && kind != MINUSMINUS)
            return getExpression().isConstant();
        return false;
    }

    public Object constantValue() {
        Object o = getExpression().constantValue();
        if (o instanceof Boolean)
            return !(Boolean) o;

        BigDecimal val = (BigDecimal) o;
        switch (kind) {
            case PLUS:
                return val;
            case MINUS:
                return val.negate();
            case COMP:
                return new BigDecimal(Integer.toString(~val.intValue()));
        }
        return null;
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitUnaryPreExpression(this);
    }
}