package org.processj.compiler.ast;

import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AST extends Context {

    private final int line      ;
    private final int column    ;

    public final List<AST> children;
    public final Set<Token> tokens;

    public AST(final Token... tokens) {

        this.children   = new ArrayList<>();
        this.tokens     = new LinkedHashSet<>();

        int line    = 0x7fffffff;
        int column  = 0x7fffffff;

        for(final Token token: tokens) {

            this.tokens.add(token);

            line    = Math.min(line, token.line)    ;
            column  = Math.min(line, token.column)  ;

        }

        // Initialize the line & column
        this.line   = line   ;
        this.column = column ;

    }

    public AST(final AST... children) {

        this.children   = new ArrayList<>();
        this.tokens     = new LinkedHashSet<>();

        int line    = 0x7fffffff;
        int column  = 0x7fffffff;

        for(final AST child: children) {

            line    = Math.min(line, (child != null) ? child.line : -1)    ;
            column  = Math.min(line, (child != null) ? child.column : -1)  ;

            this.children.add(child);

        }

        // Initialize the line & column
        this.line   = line   ;
        this.column = column ;

    }

    public AST() {

        this.children   = new ArrayList<>();
        this.tokens     = new LinkedHashSet<>();

        // Initialize the line & column
        this.line   = 0x7FFFFFFF ;
        this.column = 0xFFFFFFFF ;

    }


    public final int getLine() {

        return this.line;

    }

    public final int getColumn() {

        return this.column;

    }

    protected final Set<Token> getTokens() {

        return this.tokens;

    }

    public final AST getChildAt(final int index) {

        return this.children.get(index);

    }

    public String toString() {
        return "";
    }

    public void print(java.io.PrintStream out, int depth) {
        out.print("line " + " ".repeat(20) + line + ": ");
        out.print(" ".repeat(depth * 2));

        org.processj.compiler.Compiler.Info(this.getClass().getName() + " " + this);
        for (int c = 0; c < this.children.size(); c++) {
            if (children.get(c) == null) {
                out.print("line " + " ".repeat(20) + line + ": ");
                out.print(" ".repeat(depth * 2 + 2));
                org.processj.compiler.Compiler.Info("empty");
            } else {
                children.get(c).print(out, depth + 1);
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

    public void accept(final Visitor visitor) throws Phase.Error {}

    /**
     * Visit all children of this node from left to right. Usually called from within a visitor
     */
    public <T> T visitChildren(final Visitor visitor) throws Phase.Error {
        for(int c = 0; c < this.children.size(); c++) {
            if(children.get(c) != null) children.get(c).accept(visitor);
        }
        return null;
    }

}