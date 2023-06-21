package org.processj.compiler.ast.expression.yielding;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.ExpressionContext;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class ChannelReadExpression extends Expression implements ExpressionContext {

    /// --------------
    /// Private Fields

    private final BlockStatement extendedRendezvous ;
    private final Expression     channelExpression  ;

    /// ------------
    /// Constructors

    public ChannelReadExpression(final Expression channelExpression, final BlockStatement extendedRendezvous) {
        super(new AST[] { channelExpression, extendedRendezvous });

        this.channelExpression  = channelExpression     ;
        this.extendedRendezvous = extendedRendezvous    ;

    }

    /// ---
    /// AST

    @Override
    public void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Visit
        visitor.visitChannelReadExpression(this);

        // Close the scope
        visitor.setContext(this.closeContext());
    }

    /// ----------
    /// Expression

    @Override
    public final boolean doesYield() {

        return true;

    }

    public final boolean definesExtendedRendezvous() {

        return this.extendedRendezvous != null;

    }

    public Expression getTargetExpression() {
        return (Expression) children[0];
    }

    public BlockStatement getExtendedRendezvous() {

        return this.extendedRendezvous;

    }

}