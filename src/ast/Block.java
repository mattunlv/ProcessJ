package ast;

import utilities.Visitor;

public class Block extends Statement {

    // This is set to true in the rewriting of channel read expressions inside other expressions when blocks are created
    public boolean canBeMerged = false;


    public Block(Sequence<Statement> stats) {
        super(stats);
        nchildren = 1;
        children = new AST[] { stats };
    }

    public Sequence<Statement> stats() {
        return (Sequence<Statement>) children[0];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitBlock(this);
    }
}