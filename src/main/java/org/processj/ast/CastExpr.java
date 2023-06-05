package org.processj.ast;

import java.math.BigDecimal;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class CastExpr extends Expression {

    public CastExpr(Type ct, Expression expr) {
        super(ct);
        nchildren = 2;
        children = new AST[] { ct, expr };

    }

    public Type type() {
        return (Type) children[0];
    }

    public void setType(final Type type) {

        super.setType(type);

        this.children[0] = type;

    }

    public Expression getExpression() {
        return (Expression) children[1];
    }

    public boolean isConstant() {
        return getExpression().isConstant();
    }

    public Object constantValue() {
        if (type().isIntegralType())
            return new BigDecimal(
                    ((BigDecimal) getExpression().constantValue()).toBigInteger());
        return getExpression().constantValue();
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitCastExpr(this);
    }
}