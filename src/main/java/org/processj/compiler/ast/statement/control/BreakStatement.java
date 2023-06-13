package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class BreakStatement extends Statement {

    public BreakStatement(Token t, Name target) {
        super(t);
        nchildren = 1;
        children = new AST[] { target };
    }

    public Name getTarget() {
        return (Name) children[0];
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitBreakStatement(this);
    }

}