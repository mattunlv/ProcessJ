package org.processj.compiler.ast.expression.result;

import java.math.BigDecimal;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.ast.type.primitive.numeric.integral.IntegralType;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class CastExpression extends Expression {

    private final Type castType;
    private final Expression expression;

    public CastExpression(Type ct, Expression expr) {
        super(new AST[] { expr });
        this.castType = ct;
        this.expression = expr;
    }

    public Type getCastType() {
        return this.castType;
    }

    public void setType(final Type type) {

        super.setType(type);
        this.type = type;
    }

    public Expression getExpression() {
        return this.expression;
    }

    public boolean isConstant() {
        return getExpression().isConstant();
    }

    public Object constantValue() {
        if (this.type instanceof IntegralType)
            return new BigDecimal(
                    ((BigDecimal) getExpression().constantValue()).toBigInteger());
        return getExpression().constantValue();
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitCastExpression(this);
    }
}