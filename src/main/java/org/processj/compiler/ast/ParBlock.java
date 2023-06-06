package org.processj.compiler.ast;

import java.util.*;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class ParBlock extends Statement implements SymbolMap.Context {
    
    public  final Map<String, Integer>  enrolls                 ;
    private Block                       body                    ;
    private SymbolMap                   scope                   ;
    private Set<String>                 readSet                 ;
    private Set<String>                 writeSet                ;

    // TODO: Maybe construct with Block
    public ParBlock(final Sequence<? extends Statement> statements, final Sequence<Expression> barriers) {
        super(statements);
        this.scope      = null;
        this.body       = (Block) this.children[0];
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

    public final Sequence<Statement> getStatements() {

        return (Sequence<Statement>) this.body.getStatements();

    }

    public final Map<String, Integer> getEnrolls() {

        return this.enrolls;

    }

    public final void addEnrolls(final Set<Expression> barriers) {

        // Add the Barriers First
        this.addBarriers(barriers);

        // Update the Enrolls
        barriers.forEach(this::addEnroll);

    }

    public final void aggregateEnrollsOf(final Statement statement) {

        this.addBarriers(statement.getBarrierSet());

        // Update the enrolls
        statement.getBarrierSet().forEach(this::addEnroll);

    }

    public final void addEnroll(final Expression expression) {

        // TODO: Might have to set default value to -1 because of ParBlock Collapse
        this.enrolls.put(expression.toString(), this.enrolls.getOrDefault(expression.toString(), 0) + 1);

    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        visitor.setScope(this.openScope(visitor.getScope()));

        // Visit
        S result = visitor.visitParBlock(this);

        // Close the scope
        visitor.setScope(scope.getEnclosingScope());

        return result;

    }

}