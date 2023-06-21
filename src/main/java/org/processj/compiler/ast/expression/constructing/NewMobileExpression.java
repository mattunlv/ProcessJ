package org.processj.compiler.ast.expression.constructing;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class NewMobileExpression extends Expression {

    private SymbolMap candidates      ;

    public NewMobileExpression(Name name) {
        super(new AST[] { name });
        this.candidates = null;
    }

    public Name name() {
        return (Name) children[0];
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitNewMobileExpression(this);
    }

    public final SymbolMap getCandidates() {

        return this.candidates;

    }

    public final void setCandidates(final SymbolMap symbolMap) {

        this.candidates = symbolMap;

    }



}