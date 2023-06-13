package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

// 4/19/16: the 'channels' Sequence ONLY holds one channel.
public class ClaimStatement extends Statement {

    public ClaimStatement(final Sequence<AST> channels, final Statement statement) {
        super(new AST[] { channels, statement });
    }

    public Sequence<AST> getChannels() {
        return (Sequence<AST>) children[0];
    }

    public Statement getStatement() {
        return (Statement) children[1];
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitClaimStatement(this);
    }

}