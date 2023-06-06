package org.processj.test;

import org.junit.jupiter.api.Test;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.IVisitor;
import org.processj.compiler.ast.SymbolMap;

public class ArrayTypeTests extends ProcessJTest implements IVisitor<Void> {

    private SymbolMap currentScope;

    @Override
    public SymbolMap getScope() {

        return this.currentScope;

    }

    @Override
    public void setScope(final SymbolMap symbolMap) {

        this.currentScope = symbolMap;

    }


    /// -------------------------
    /// org.processj.ast.IVisitor

    @Test
    public void testCode_array01_endToEnd() throws Phase.Error {

        final Compilation compilation = CompilationFor(Array01);

        compilation.visit(this);

    }

}
