package org.processj.compiler.ast.expression.unary;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class UnaryPostExpression extends Expression {

    public static final int PLUSPLUS = 0; // ++
    public static final int MINUSMINUS = 1; // --
    public static final String[] opSyms = { "++", "--" };

    private int kind;

    private final Expression expression;

    public UnaryPostExpression(Expression expr, int op) {
        super(new AST[] { expr });
        this.expression = expr;
        kind = op;
    }

    public String opString() {
        return opSyms[kind];
    }

    public Expression getExpression() {
        return this.expression;
    }

    public int getOperator() {
        return kind;
    }

    public String toString() {
        return getExpression().toString() + opSyms[kind];
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitUnaryPostExpression(this);
    }
}