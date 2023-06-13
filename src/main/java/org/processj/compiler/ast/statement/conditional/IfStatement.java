package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.ConditionalStatement;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class IfStatement extends Statement implements ConditionalStatement {

    /// --------------
    /// Private Fields

    private Expression  expression  ;
    private final BlockStatement thenBody    ;
    private final BlockStatement elseBody    ;

    /** Note that elsepart() can return null when having code
     * like
     *
     *      if ( expression )
     *          statement
     * */

    public IfStatement(final String label, final Expression evaluationExpression, final Statement thenPart, final Statement elsePart) {
        super(label, evaluationExpression);
        nchildren = 3;
        children = new AST[] { evaluationExpression, (thenPart instanceof BlockStatement) ? thenPart : new BlockStatement(thenPart), new BlockStatement(elsePart)};
        this.expression = evaluationExpression          ;
        this.thenBody   = (BlockStatement) this.children[1]      ;
        this.elseBody   = (BlockStatement) this.children[2]      ;
    }

    public IfStatement(final String label, final Expression evaluationExpression, final Statement thenPart) {
        this(label, evaluationExpression, thenPart, null);
    }

    public IfStatement(final Expression evaluationExpression, final Statement thenPart, final Statement elsePart) {
        this("", evaluationExpression, thenPart, elsePart);
    }

    public IfStatement(final Expression evaluationExpression, final Statement thenPart) {
        this(evaluationExpression, thenPart, null);
    }



    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitIfStatement(this);
    }

    public final Expression getEvaluationExpression() {

        return this.expression;

    }

    public final boolean definesThenStatement() {

        return this.children[1] != null;

    }

    public final boolean definesElseStatement() {

        return this.children[2] != null;

    }

    public final BlockStatement getThenBody() {

        return this.thenBody;

    }

    public final BlockStatement getElseBody() {

        return this.elseBody;

    }

    @FunctionalInterface
    public interface AggregateReturnCallback {

        Statement Invoke(final Statement statement) throws Phase.Error;

    }

    public final BlockStatement getBody() {

        return this.thenBody.getBody();

    }

    @Override
    public BlockStatement getMergeBody() {
        return null;
    }

    @Override
    public final BlockStatement getClearedMergeBody() {

        return this.thenBody.getClearedMergeBody();

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