package org.processj.test;

import org.junit.jupiter.api.Test;
import org.processj.ast.ArrayType;
import org.processj.ast.Compilation;
import org.processj.ast.IVisitor;

public class ArrayTypeTests extends ProcessJTest implements IVisitor<Void> {


    /// -------------------------
    /// org.processj.ast.IVisitor




    @Test
    public void testCode_array01_endToEnd() {

        final Compilation compilation = CompilationFor(Array01);

        compilation.visit(this);

    }

}
