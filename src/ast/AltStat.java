package ast;

import utilities.Visitor;

public class AltStat extends Statement {

    public boolean pri;         // is this a pri alt
    public boolean replicated;  // is this a replicated alt
    public boolean dynamic;     // if it contains any replicated alts, it is dynamic
    public AltStat(Sequence<AltCase> body, boolean pri) {
        super(body);
        nchildren = 4;
        this.pri = pri;
        this.replicated = false;
        children = new AST[] { null, null, null, body };
    }

    public AltStat(Sequence<Statement> init,
                   Expression expr,
                   Sequence<ExprStat> incr,
                   Sequence<AltCase> body, boolean pri) {
        super(body);
        nchildren = 4;
        this.pri = pri;
        this.replicated = true;
	this.dynamic = true;
        children = new AST[] { init, expr, incr, body };
    }

    public boolean isPri() {
        return pri;
    }

    public boolean isReplicated() {
        return replicated;
    }

    public boolean isDynamic() {
	return dynamic;
    }    
    
    public Sequence<Statement> init() {
        return (Sequence<Statement>) children[0];
    }

    public Expression expr() {
        return (Expression) children[1];
    }

    public Sequence<ExprStat> incr() {
        return (Sequence<ExprStat>) children[2];
    }

    public Sequence<AltCase> body() {
        return (Sequence<AltCase>) children[3];
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitAltStat(this);
    }
}
