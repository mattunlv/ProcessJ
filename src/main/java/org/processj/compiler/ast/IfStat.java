package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class IfStat extends Statement {

    /// --------------
    /// Private Fields

    private Expression  expression  ;
    private final Block thenBody    ;
    private final Block elseBody    ;

    /** Note that elsepart() can return null when having code
     * like
     *
     *      if ( expression )
     *          statement
     * */

    public IfStat(final Expression expression, final Statement thenPart, final Statement elsePart) {
        super(expression);
        nchildren = 3;
        children = new AST[] { expression, (thenPart instanceof Block) ? thenPart : new Block(thenPart), new Block(elsePart)};
        this.expression = expression                    ;
        this.thenBody   = (Block) this.children[1]      ;
        this.elseBody   = (Block) this.children[2]      ;
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitIfStat(this);
    }

    public final Expression evaluationExpression() {

        return this.expression;

    }

    public final Sequence<Statement> getThenStatements() {

        return (Sequence<Statement>) this.thenBody.getStatements();

    }

    public final Sequence<Statement> getElseBody() {

        return (Sequence<Statement>) this.elseBody.getStatements();

    }

}