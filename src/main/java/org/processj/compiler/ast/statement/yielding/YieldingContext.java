package org.processj.compiler.ast.statement.yielding;

import org.processj.compiler.ast.statement.conditional.BlockStatement;

public interface YieldingContext {

    BlockStatement getMergeBody();

    void clearMergeBody();

    BlockStatement getClearedMergeBody();


}
