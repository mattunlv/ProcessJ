package org.processj.compiler.ast;

import org.processj.compiler.ast.expression.Expression;

public interface ConditionalStatement extends BreakableStatement {

    Expression getEvaluationExpression();

}
