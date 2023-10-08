package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.expression.Expression;

/**
 * <p>A {@link org.processj.compiler.ast.Context} that represents a construct that executes a sequence
 * of {@link org.processj.compiler.ast.statement.Statement}s bound to some condition.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see ConditionalContext
 */
public interface ConditionalContext extends BreakableContext {

    Expression getEvaluationExpression();

}
