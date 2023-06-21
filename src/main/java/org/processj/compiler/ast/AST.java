package org.processj.compiler.ast;

import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public abstract class AST extends Context {

    private final int line      ;
    private final int column    ;
    public final AST[] children;

    public AST(final Token token, final AST... children) {

        // Initialize the children first
        this.children = (children != null) ? children : new AST[0];

        int line    = (token != null) ? token.line   : 0 ;
        int column  = (token != null) ? token.column : 0 ;

        // Iterate through the children
        if(token == null) for(final AST child: this.children)
            if(child != null) {

                // The first non-null child will set the line & column
                line    = child.line    ;
                column  = child.column  ;

                break;

            }

        // Initialize the line & column
        this.line   = line   ;
        this.column = column ;

    }

    public AST(final AST... children) {
        this(null, children);
    }

    public final int getLine() {

        return this.line;

    }

    public final int getColumn() {

        return this.column;

    }

    public final AST getChildAt(final int index) {

        return (index < this.children.length) ? this.children[index] : null;

    }

    public String toString() {
        return "";
    }

    public void print(java.io.PrintStream out, int depth) {
        out.print("line " + " ".repeat(20) + line + ": ");
        out.print(" ".repeat(depth * 2));

        org.processj.compiler.Compiler.Info(this.getClass().getName() + " " + this);
        for (int c = 0; c < this.children.length; c++) {
            if (children[c] == null) {
                out.print("line " + " ".repeat(20) + line + ": ");
                out.print(" ".repeat(depth * 2 + 2));
                org.processj.compiler.Compiler.Info("empty");
            } else {
                children[c].print(out, depth + 1);
            }
        }
    }

    public void print(java.io.PrintStream out) {
        this.print(out, 0);
    }

    public void print() {
        this.print(System.out);
    }

    /* *********************************************************** */
    /* **                                                       ** */
    /* ** Generic Visitor Stuff                                 ** */
    /* **                                                       ** */
    /* *********************************************************** */

    public abstract void accept(final Visitor visitor) throws Phase.Error;

    /**
     * Visit all children of this node from left to right. Usually called from within a visitor
     */
    public <T> T visitChildren(final Visitor visitor) throws Phase.Error {
        for(int c = 0; c < this.children.length; c++) {
            if(children[c] != null) children[c].accept(visitor);
        }
        return null;
    }

}