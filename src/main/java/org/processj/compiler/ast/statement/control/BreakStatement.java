package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class BreakStatement extends Statement {

    public BreakStatement(Token t, Name target) {
        super(new AST[] { target });
    }

    public Name getTarget() {
        return (Name) children[0];
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitBreakStatement(this);
    }

}