/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syntax;

import ast.Token;

/**
 * @author ben
 */
public class TerminalCtx extends NodeCtx {
    
    private Token symbol;
    
    public TerminalCtx(Token symbol) {
        this.symbol = symbol;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public NodeCtx get(int index) {
        if (index > 0)
            throw new AssertionError("Can only access the root node");
        return (NodeCtx) symbol;
    }

    @Override
    public Token getRoot() {
        return symbol;
    }
}
