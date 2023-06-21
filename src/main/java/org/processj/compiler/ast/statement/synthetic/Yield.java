package org.processj.compiler.ast.statement.synthetic;

import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class Yield extends Statement {


    public Yield(Token t) {
        super(t);
    }

    public Yield() {
        super(new Token(""));
    }

    @Override
    public void accept(Visitor v) throws Phase.Error {

    }
}
