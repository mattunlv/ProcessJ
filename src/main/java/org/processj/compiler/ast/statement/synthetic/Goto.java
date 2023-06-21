package org.processj.compiler.ast.statement.synthetic;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class Goto extends Statement {

    public Goto(final String label, final String target) {
        super(new AST[]{ new Name(label), new Name(target) });

    }

    @Override
    public void accept(Visitor v) throws Phase.Error {



    }
}
