package org.processj.compiler.ast;

import java.math.BigDecimal;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class UnaryPreExpr extends Expression {

    public static final int PLUSPLUS = 0; // ++
    public static final int MINUSMINUS = 1; // --
    public static final int PLUS = 2; // +
    public static final int MINUS = 3; // -
    public static final int COMP = 4; // ~
    public static final int NOT = 5; // !
    private static final String[] opSyms = { "++", "--", "+", "-", "~", "!" };

    private int kind;

    public UnaryPreExpr(Expression expr, int op) {
        super(expr);
        nchildren = 1;
        kind = op;
        children = new AST[] { expr };
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

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitUnaryPreExpr(this);
    }
}