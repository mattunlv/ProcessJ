package org.processj.test.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.processj.compiler.ast.Compilation;
import org.processj.test.ProcessJTest;

public class GrammarRecordTypeDeclarationUnitTest extends ProcessJTest {

    /**
     * <p>We assert that when a Record Declaration is present, then the parse tree contains a non-empty Types set.
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_recordDeclaration_IS_present_THEN_parseTreeContainsNonEmptyTypes() {

        final Compilation compilation = CompilationFor(Record01);

        Assertions.assertFalse(compilation.getTypeDeclarations().isEmpty());

    }

}
