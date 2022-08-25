package ast;

import syntax.NodeCtx;
import syntax.RuleCtx;
import syntax.TerminalCtx;
import syntax.Types;

/**
 * @author ben
 */
public class Token extends NodeCtx {
    
    /** Gets its value from sym.java */
    public int kind = Types.INSTANCE.UNKNOWN;
    /** The actual text scanned for this token */
    public String lexeme = "";
    /** The line number on which this token appears */
    public int line = -1;
    /** The column number in which the token begins */
    public int start = -1;
    /** The column number in which the token ends */
    public int stop = -1;

    public Token(int kind, String text, int line, int start, int stop) {
        this.kind = kind;
        this.lexeme = text;
        this.line = line;
        this.start = start;
        this.stop = stop;
    }
    
    public Token(String text) {
    	this.lexeme = text;
    }
    
    public Token() {
    }
    
    @Override
    public String getText() {
        return lexeme;
    }

    @Override
    public int getStartLine() {
        return line;
    }

    @Override
    public int getStartColumn() {
        return start;
    }
    
    public int getEndColumn() {
        return stop;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public NodeCtx get(int idx) {
        if (idx > 0)
            throw new AssertionError("Can only access the root node");
        return this;
    }

    @Override
    public Token getRoot() {
        return this;
    }

    @Override
    public String toString() {
        // TODO: 'kind' should be a tag, e.g., <identifier> instead of a 114
        return "Token: '" + lexeme + "', line " + line
            + " [" + start + ":" + stop + "] (kind: " + kind + ")"; // Types.INSTANCE.getText(kind)
    }

    @Override
    public RuleCtx asRuleContext() {
        return new RuleCtx(this);
    }
    
    @Override
    public TerminalCtx asTerminalContext() {
        return new TerminalCtx(this);
    }
}