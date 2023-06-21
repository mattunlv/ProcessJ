package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class StopStatement extends Statement {

    public StopStatement(Token t) {
        super(t);
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitStopStatement(this);
    }
}