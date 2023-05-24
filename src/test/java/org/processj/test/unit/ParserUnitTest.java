package org.processj.test.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.processj.ast.AST;
import org.processj.ast.Compilation;
import org.processj.ast.Sequence;
import org.processj.test.ProcessJTest;

public class ParserUnitTest extends ProcessJTest {

    private static void AssertEmpty(final Sequence<? extends AST> sequence) {

        Assertions.assertNotNull(sequence);
        Assertions.assertEquals(0, sequence.size());

    }

    /**
     * Notes:
     * - A non-existent package declaration will result in a null value compared to non-existent pragmas resulting in
     *   an empty sequence
     */

    @Test
    public void testCode_empty_emptyPragmaSequence_parserUnitTest() {

        // Retrieve the compilation
        final Compilation compilation = CompilationFor(Empty);

        // Assert an empty imports sequence
        AssertEmpty(compilation.pragmas());

    }

    @Test
    public void testCode_empty_emptyImportsSequence_parserUnitTest() {

        // Retrieve the compilation
        final Compilation compilation = CompilationFor(Empty);

        // Assert an empty imports sequence
        AssertEmpty(compilation.imports());

    }

    @Test
    public void testCode_empty_emptyTypeDeclarationsSequence_parserUnitTest() {

        // Retrieve the compilation
        final Compilation compilation = CompilationFor(Empty);

        // Assert an empty imports sequence
        AssertEmpty(compilation.typeDecls());

    }

    @Test
    public void testCode_empty_nullPackageName_parserUnitTest() {

        // Retrieve the compilation
        final Compilation compilation = CompilationFor(Empty);

        // Check null package declaration
        Assertions.assertNull(compilation.packageName());

    }

}
