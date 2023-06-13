package org.processj.compiler.ast.expression.result;

import java.math.BigDecimal;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class CastExpression extends Expression {

    public CastExpression(Type ct, Expression expr) {
        super(ct);
        nchildren = 2;
        children = new AST[] { ct, expr };

    }

    public Type getCastType() {
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
        if (getCastType().isIntegralType())
            return new BigDecimal(
                    ((BigDecimal) getExpression().constantValue()).toBigInteger());
        return getExpression().constantValue();
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitCastExpression(this);
    }
}