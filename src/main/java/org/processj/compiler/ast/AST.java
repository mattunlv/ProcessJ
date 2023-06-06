package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.utilities.syntax.NodeCtx;
import org.processj.compiler.utilities.Log;
import org.processj.compiler.phases.phase.Visitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AST {
    /*implements java.io.Serializable*/// temporarily removed.
    public Compilation myCompilation = null; // Points to the compilation in which this node is defines. Only used for nodes that implement TopLevelDecl.
    public NodeCtx ctx = null; // this is used for sytanx-error and static code analysis (not all nodes generate a token!)

    public String myPackage; // set this field to the name of the package in which the parse tree node lives! (only for top level decls)
    public int line, charBegin;
    public int nchildren = 0;
    public AST[] children;
    
    // This variable is used in the rewriting phase to indicate that
    // an expression has already been rewritten.
    public boolean rewrite = false;

    public AST() {
        // this constructor must ONLY be used in ErrorType
    }

    public AST(int p_line, int p_charBegin) {
        line = p_line;
        charBegin = p_charBegin;
    }

    public AST(Token t) {
        ctx = t;
        line = t.line;
        charBegin = t.start;
    }

    public AST(AST n) {
        if (n == null) {
            line = 0;
            charBegin = 0;
        } else {
            line = n.line;
            charBegin = n.charBegin;
        }
    }

    public AST(final AST[] children) {

        this.children   = children                                  ;
        this.nchildren  = (children != null) ? children.length : 0  ;
        this.line       = GetLineStartFrom(children);
        this.charBegin  = GetCharBeginFrom(children);

    }

    public AST(final AST[] children, final Token token) {

        this.children   = children                                  ;
        this.nchildren  = (children != null) ? children.length : 0  ;
        this.line       = token.line                                ;
        this.charBegin  = token.start                               ;

    }

    private static int GetLineStartFrom(final AST[] children) {

        int result = 0;

        if(children != null)
            for(final AST child: children)
                if(child != null) {

                    result = child.line;

                    break;

                }

        return result;

    }

    private static int GetCharBeginFrom(final AST[] children) {

        int result = 0;

        if(children != null)
            for(final AST child: children)
                if(child != null) {

                    result = child.charBegin;

                    break;

                }

        return result;

    }

    public final int getLine() {

        return this.line;

    }

    public final int getColumn() {

        return this.charBegin;

    }

    public String getname() {
        return "Blah.";
    }

    public String toString() {
        return "";
    }

    private void tab(java.io.PrintStream out, int amount) {
        int i;
        for (i = 0; i < amount; i++)
            out.print(" ");
    }

    private String intToString(int i, int w) {
        String s = "                    " + Integer.toString(i);
        int length = s.length();
        return s.substring(length - w);
    }

    public void print(java.io.PrintStream out, int depth) {
        out.print("line " + this.intToString(line, 3) + ": ");
        tab(out, depth * 2);
        Log.log(this.getClass().getName() + " " + this.toString());
        for (int c = 0; c < nchildren; c++) {
            if (children[c] == null) {
                out.print("line " + this.intToString(line, 3) + ": ");
                tab(out, depth * 2 + 2);
                Log.log("empty");
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

    public abstract <T> T visit(Visitor<T> v) throws Phase.Error;

    public <T> T visit(final IVisitor<T> visitor) throws Phase.Error,
            SymbolMap.Context.ContextDoesNotDefineScopeException {

        // Attempt to
        try {

            // Retrieve the corresponding visitor method
            Method visitorMethod = visitor.getClass().getDeclaredMethod(
                    "visit" + this.getClass().getSimpleName(), this.getClass());

            // And then Invoke it
            visitorMethod.invoke(visitor,this);

        // Otherwise
        } catch(final NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

        }

        // And now, the children
        for(int index = 0; (this.children != null) && (index < this.nchildren); index++)
            if(this.children[index] != null) this.children[index].visit(visitor);

        // Return the result
        return null;

    }

    /**
     * Visit all children of this node from left to right. Usually called from within a visitor
     */
    public <T> T visitChildren(Visitor<T> v) throws Phase.Error {
        for(int c = 0; c < nchildren; c++) {
            if(children[c] != null) children[c].visit(v);

        }
        return null;
    }

}