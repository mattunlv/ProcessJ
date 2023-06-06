package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class UnaryPostExpr extends Expression {

    public static final int PLUSPLUS = 0; // ++
    public static final int MINUSMINUS = 1; // --
    public static final String[] opSyms = { "++", "--" };

    private int kind;

    public UnaryPostExpr(Expression expr, int op) {
        super(expr);
        nchildren = 1;
        kind = op;
        children = new AST[] { expr };
    }

    public String opString() {
        return opSyms[kind];
    }

    public Expression getExpression() {
        return (Expression) children[0];
    }

    public int getOperator() {
        return kind;
    }

    public String toString() {
        return getExpression().toString() + opSyms[kind];
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitUnaryPostExpr(this);
    }
}