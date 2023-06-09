/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.processj.compiler.utilities.syntax;

import org.processj.compiler.ast.Token;

/**
 * Represents a token that was consumed during a invalid match.
 * 
 * @author ben
 */
public class ErrorCtx extends NodeCtx {
    
    private final Token symbol;
    
    public ErrorCtx(Token symbol) {
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
    
    @Override
    public String toString() {
        return symbol.toString();
    }
}