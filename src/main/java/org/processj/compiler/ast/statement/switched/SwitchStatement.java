package org.processj.compiler.ast.statement.switched;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.ast.statement.ConditionalStatement;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class SwitchStatement extends Statement implements ConditionalStatement {

    private final BlockStatement body;

    public SwitchStatement(final Expression evaluationExpression, final Sequence<SwitchGroupStatement> switchGroups) {
        super(new AST[] { evaluationExpression, new BlockStatement(switchGroups) });

        this.body = (BlockStatement) this.children[1];

    }

    public Expression getEvaluationExpression() {
        return (Expression) children[0];
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitSwitchStatement(this);
    }

    public final BlockStatement getBody() {

        return this.body;

    }

    public final BlockStatement getMergeBody() {

        return this.body.getMergeBody();

    }

    @Override
    public final BlockStatement getClearedMergeBody() {

        return this.body.getClearedMergeBody();

    }

    @Override
    public boolean definesEndLabel() {
        return false;
    }

    @Override
    public void setEndLabel(String label) {

    }

    @Override
    public String getEndLabel() {
        return null;
    }

}