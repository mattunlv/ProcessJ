package org.processj.compiler.ast.statement.conditional;

import org.processj.compiler.ast.Context;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.statement.Statements;

/**
 * <p>A {@link Context} that represents a construct that executes {@link Statements} that can be paused or terminated
 * by one of its' contained {@link Statement}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see Context
 */
public interface BreakableContext  { /* Empty */ }
