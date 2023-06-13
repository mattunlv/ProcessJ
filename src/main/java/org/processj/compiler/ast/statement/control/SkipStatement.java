package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class SkipStatement extends Statement {

    public SkipStatement(Token t) {
        super(t);
        nchildren = 0;
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitSkipStatement(this);
    }
}