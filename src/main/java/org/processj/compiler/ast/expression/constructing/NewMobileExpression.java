package org.processj.compiler.ast.expression.constructing;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.SymbolMap;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class NewMobileExpression extends Expression {

    private SymbolMap candidates      ;

    public NewMobileExpression(Name name) {
        super(name);
        nchildren = 1;
        children = new AST[] { name };
        this.candidates = null;
    }

    public Name name() {
        return (Name) children[0];
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitNewMobileExpression(this);
    }

    public final SymbolMap getCandidates() {

        return this.candidates;

    }

    public final void setCandidates(final SymbolMap symbolMap) {

        this.candidates = symbolMap;

    }



}