package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.statement.BreakableContext;

public interface ConditionalContext extends BreakableContext {

    Expression getEvaluationExpression();

}
