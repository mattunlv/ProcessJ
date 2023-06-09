package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class NewMobile extends Expression {

    public DefineTopLevelDecl myDecl = null;

    private SymbolMap                   candidates      ;

    public NewMobile(Name name) {
        super(name);
        nchildren = 1;
        children = new AST[] { name };
        this.candidates = null;
    }

    public Name name() {
        return (Name) children[0];
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitNewMobile(this);
    }

    public final SymbolMap getCandidates() {

        return this.candidates;

    }

    public final void setCandidates(final SymbolMap symbolMap) {

        this.candidates = symbolMap;

    }



}