package org.processj.compiler.ast.statement;

import org.processj.compiler.ast.statement.conditional.BlockStatement;

public interface BreakableContext  {

    BlockStatement getMergeBody();

    void clearMergeBody();

    BlockStatement getClearedMergeBody();

}
