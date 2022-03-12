package ast;

import java.util.HashMap;

import utilities.Visitor;

public class ParBlock extends Statement {
    
    public HashMap<String, Integer> enrolls;
    
    public ParBlock(Sequence<Statement> stats, Sequence<Expression> barriers) {
        super(stats);
        nchildren = 2;
        children = new AST[] { stats, barriers };
    }

    public Sequence<Statement> stats() {
        return (Sequence<Statement>) children[0];
    }

    public Sequence<Expression> barriers() {
        return (Sequence<Expression>) children[1];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitParBlock(this);
    }
}