package org.processj.compiler.ast.expression.binary;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.type.primitive.BooleanType;
import org.processj.compiler.ast.type.primitive.numeric.integral.IntegralType;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.AST;
import org.processj.compiler.phase.Visitor;

import java.math.*;

public class BinaryExpression extends Expression {

    public static final int PLUS       = 0;
    public static final int MINUS      = 1;
    public static final int MULT       = 2;
    public static final int DIV        = 3;
    public static final int MOD        = 4;
    public static final int LSHIFT     = 5;
    public static final int RSHIFT     = 6;
    public static final int RRSHIFT    = 7;
    public static final int LT         = 8;
    public static final int GT         = 9;
    public static final int LTEQ       = 10;
    public static final int GTEQ       = 11;
    public static final int INSTANCEOF = 12;
    public static final int EQEQ       = 13;
    public static final int NOTEQ      = 14;
    public static final int AND        = 15;
    public static final int OR         = 16;
    public static final int XOR        = 17;
    public static final int ANDAND     = 18;
    public static final int OROR       = 19;


    public static final String [] opSyms = {
            "+", "-", "*", "/", "%", "<<", ">>", ">>>", "<", ">",
            "<=", ">=", "instanceof", "==", "!=", "&", "|", "^",
            "&&", "||" };

    private int kind;
    private final Expression leftExpression;
    private final Expression rightExpression;

    public BinaryExpression(Expression left, Expression right, int op) {
        super(new AST[] { left, right});
        kind = op;
        this.leftExpression = left;
        this.rightExpression = right;
    }

    public Expression getLeftExpression()  { return this.leftExpression; }
    public Expression getRightExpression() { return this.rightExpression; }
    public int op()           { return kind; }

    public String opString() { return opSyms[kind]; }

    public boolean isConstant() {
        return getLeftExpression().isConstant() && getRightExpression().isConstant();
    }


    // This method should ONLY be called if both the left and the right expressions
    // are sure to be constant value - otherwise this method will crash!
    public Object constantValue() {
        if (getLeftExpression().getType() instanceof BooleanType && getRightExpression().getType() instanceof BooleanType) {
            boolean lval = (Boolean) getLeftExpression().constantValue();
            boolean rval = (Boolean) getRightExpression().constantValue();


            switch(kind) {
                case ANDAND: return lval && rval;
                case OROR:   return lval || rval;
                case XOR:    return lval ^ rval;
                case AND:    return lval & rval;
                case OR:     return lval | rval;
                case EQEQ:   return lval == rval;
                case NOTEQ:  return lval != rval;
            }
        }

        BigDecimal lval = (BigDecimal) getLeftExpression().constantValue();
        BigDecimal rval = (BigDecimal) getRightExpression().constantValue();

        switch(kind) {
            case PLUS:  return lval.add(rval);
            case MINUS: return lval.subtract(rval);
            case MULT:  return lval.multiply(rval);
            case DIV:
                if (getLeftExpression().getType() instanceof IntegralType && getRightExpression().getType() instanceof IntegralType)
                    return new BigDecimal(lval.toBigInteger().divide(rval.toBigInteger()));
                new BigDecimal(lval.doubleValue()/rval.doubleValue());

            case LT: return lval.compareTo(rval) == -1;
            case GT: return lval.compareTo(rval) == 1;
            case LTEQ: return lval.compareTo(rval) != 1;
            case GTEQ: return lval.compareTo(rval) != -1;
            case MOD:
            case LSHIFT:
            case RSHIFT:
            case RRSHIFT:
            case AND:
            case OR:
            case XOR:
                int lint = lval.intValue();
                int rint = rval.intValue();
                switch(kind) {
                    case MOD:    return new BigDecimal(Integer.toString(lint % rint));
                    case LSHIFT: return new BigDecimal(Integer.toString(lint << rint));
                    case RSHIFT: return new BigDecimal(Integer.toString(lint >> rint));
                    case RRSHIFT: return new BigDecimal(Integer.toString(lint >>> rint));
                    case AND:    return new BigDecimal(Integer.toString(lint & rint));
                    case OR:     return new BigDecimal(Integer.toString(lint | rint));
                    case XOR:    return new BigDecimal(Integer.toString(lint ^ rint));
                }

            case EQEQ:  return lval.equals(rval);
            case NOTEQ: return !lval.equals(rval);
        }
        return null;
    }


    public String toString() {
        return getLeftExpression() + " " +  opSyms[op()] + " " + getRightExpression();
    }


    public void accept(Visitor v) throws Phase.Error {
        v.visitBinaryExpression(this);
    }
}