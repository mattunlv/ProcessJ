package org.processj.phases;

import org.processj.Phase;
import org.processj.ast.*;
import org.processj.ast.expression.Expression;

/**
 * <p>Traverses a {@link Compilation} to see if the {@link ChannelWriteStat}'s write {@link Expression} is type equal
 * to the underlying Channel's component {@link Type} in order to wrap the write {@link Expression} in a
 * {@link CastExpr}.</p>
 * @see ChannelWriteStat
 * @see ChannelEndType
 * @see ChannelType
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class CastRewrite extends Phase {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link CastRewrite} to its' default state with the specified {@link Phase.Listener}.</p>
     * @param listener The {@link Phase.Listener} that receives any {@link Phase.Message}, {@link Phase.Warning},
     *                 or {@link Phase.Error} messages.
     * @since 0.1.0
     */
    public CastRewrite(final Phase.Listener listener) {
        super(listener);
    }

    /// ------------------------------
    /// org.processj.utilities.Visitor

    /**
     * <p>For all {@link ChannelWriteStat}s, attempt to wrap the write {@link Expression} in a {@link CastExpr} if
     * the {@link ChannelWriteStat} Write {@link Expression} is Type Equal to the Channel's component
     * {@link Type}.</p>
     * @param channelWriteStatement The {@link ChannelWriteStat} to check.
     * @since 0.1.0
     */
    @Override
    public final Void visitChannelWriteStat(ChannelWriteStat channelWriteStatement) {

        // Initialize the handle to the channel's component Type
        final Type channelComponentType;

        // If the ChannelWrite Statement write expression's Type is a ChannelEnd Type, cast to retrieve the
        // component type
        if(channelWriteStatement.getTargetExpression().getType() instanceof ChannelEndType)
            channelComponentType = ((ChannelEndType) channelWriteStatement
                    .getTargetExpression().getType()).getComponentType();

        // Otherwise, it should be a Channel Type; retrieve the ComponentType accordingly
        else
            channelComponentType = ((ChannelType) channelWriteStatement
                    .getTargetExpression().getType()).getComponentType();

        // Retrieve the Write expression's Type
        final Type writeExpressionType = channelWriteStatement.getWriteExpression().getType();

        // If the Types are TypeEqual, go ahead and wrap the Expression in a Cast Expression
        if(writeExpressionType != null && !writeExpressionType.typeEqual(channelComponentType))
            channelWriteStatement.setWriteExpression(
                    new CastExpr(channelComponentType, channelWriteStatement.getWriteExpression()));


        return null;

    }

}
