package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

public class ReturnStatement extends Statement {

    /* Note that expr() can return null */

    public ReturnStatement(Token r, Expression expr) {
        super(new AST[] { expr });
    }

    public Expression getExpression() {
        return (Expression) children[0];
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitReturnStatement(this);
    }

    public final boolean definesExpression() {

        return this.children[0] != null;

    }
}