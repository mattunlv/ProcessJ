package org.processj.compiler.ast.statement.control;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.type.ParameterDeclaration;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.Token;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class SuspendStatement extends Statement {

    public SuspendStatement(Token t, Sequence<ParameterDeclaration> params) {
        super(t);
        nchildren = 1;
        children = new AST[] { params };
    }

    public Sequence<ParameterDeclaration> getParameters() {
        return (Sequence<ParameterDeclaration>) children[0];
    }

    public String signature() {
        String s = "(";
        for (ParameterDeclaration pd : getParameters())
            s = s + pd.getType().getSignature();
        s = s + ")V";
        return s;
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitSuspendStatement(this);
    }
}