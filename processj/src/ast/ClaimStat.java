package ast;

import utilities.Visitor;

// 4/19/16: the 'channels' Sequence ONLY holds one channel.
public class ClaimStat extends Statement {

    public ClaimStat(Sequence<AST> channels, Statement stat) {
        super(channels);
        nchildren = 2;
        children = new AST[] { channels, stat };
    }

    public Sequence<AST> channels() {
        return (Sequence<AST>) children[0];
    }

    public Statement stat() {
        return (Statement) children[1];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitClaimStat(this);
    }
}