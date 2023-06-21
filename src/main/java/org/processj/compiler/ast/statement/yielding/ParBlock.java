package org.processj.compiler.ast.statement.yielding;

import java.util.*;

import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phase.Visitor;

public class ParBlock extends Statement implements YieldingContext {
    
    public  final Map<String, Integer>  enrolls                 ;
    private BlockStatement              body                    ;
    private Set<String>                 readSet                 ;
    private Set<String>                 writeSet                ;

    // TODO: Maybe construct with Block
    public ParBlock(final Sequence<? extends Statement> statements, final Sequence<Expression> barriers) {
        super(statements);
        this.body       = (BlockStatement) this.children[0];
        this.enrolls    = new HashMap<>();
        this.readSet    = new LinkedHashSet<>();
        this.writeSet   = new LinkedHashSet<>();

        if(barriers != null) barriers.forEach(this::addBarrier);

    }

    public final boolean toReadSet(final Expression expression) {

        return this.readSet.add(expression.toString());

    }

    public final boolean toWriteSet(final Expression expression) {

        return this.writeSet.add(expression.toString());

    }

    public final boolean inReadSet(final Expression expression) {

        return this.readSet.contains(expression.toString());

    }

    public final boolean inWriteSet(final Expression expression) {

        return this.writeSet.contains(expression.toString());

    }

    public final BlockStatement getBody() {

        return this.body;

    }

    public final Map<String, Integer> getEnrolls() {

        return this.enrolls;

    }

    public final void aggregateEnrollsOf(final Statement statement) {

        this.addBarriers(statement.getBarrierSet());

        // Update the enrolls
        statement.getBarrierSet().forEach(barrierExpression -> {

            // TODO: Might have to set default value to -1 because of ParBlock Collapse
            this.enrolls.put(barrierExpression.toString(),
                    this.enrolls.getOrDefault(barrierExpression.toString(), 0) + 1);

        });

    }

    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the Par Block
        this.openScope();

        // Visit
        visitor.visitParBlockStatement(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    @Override
    public BlockStatement getMergeBody() {
        return this.body.getMergeBody();
    }

    @Override
    public void clearMergeBody() {

    }

    @Override
    public BlockStatement getClearedMergeBody() {
        return this.body.getClearedMergeBody();
    }
}