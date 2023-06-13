package org.processj.compiler.ast.expression.binary;

import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.AST;
import org.processj.compiler.phases.phase.Visitor;

public class AssignmentExpression extends Expression {

    public static final int EQ = 0;
    public static final int MULTEQ = 1;
    public static final int DIVEQ = 2;
    public static final int MODEQ = 3;
    public static final int PLUSEQ = 4;
    public static final int MINUSEQ = 5;
    public static final int LSHIFTEQ = 6;
    public static final int RSHIFTEQ = 7;
    public static final int RRSHIFTEQ = 8;
    public static final int ANDEQ = 9;
    public static final int OREQ = 10;
    public static final int XOREQ = 11;

    public static final String[] opSyms = { "=", "*=", "/=", "%=", "+=", "-=",
            "<<=", ">>=", ">>>=", "&=", "|=", "^=" };

    private int kind;

    public AssignmentExpression(Expression /* Name, FieldRef or ArrayAccess only */left,
                                Expression right, int op) {
        super(left);
        nchildren = 2;
        kind = op;
        children = new AST[] { left, right };
    }

    public Expression getLeftExpression() {
        return (Expression) children[0];
    }

    public Expression getRightExpression() {
        return (Expression) children[1];
    }

    public int getOperator() {
        return kind;
    }

    public String opString() {
        return opSyms[kind];
    }

    public <S extends Object> S visit(Visitor<S> v)
            throws Phase.Error {
        return v.visitAssignmentExpression(this);
    }
}