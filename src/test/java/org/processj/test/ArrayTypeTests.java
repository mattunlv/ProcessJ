package org.processj.test;

import org.junit.jupiter.api.Test;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.Context;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class ArrayTypeTests extends ProcessJTest implements Visitor {

    private Context.SymbolMap currentScope;

    @Override
    public Context.SymbolMap getScope() {

        return this.currentScope;

    }

    @Override
    public void setScope(final Context.SymbolMap symbolMap) {

        this.currentScope = symbolMap;

    }


    /// -------------------------
    /// org.processj.ast.IVisitor

    @Test
    public void testCode_array01_endToEnd() throws Phase.Error {

        final Compilation compilation = CompilationFor(Array01);

        compilation.accept(this);

    }

}
