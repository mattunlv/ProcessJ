package org.processj.compiler.ast.statement;

import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.statement.BreakableStatement;

public interface ConditionalStatement extends BreakableStatement {

    Expression getEvaluationExpression();

}
