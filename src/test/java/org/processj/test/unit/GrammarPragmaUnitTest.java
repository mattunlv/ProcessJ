package org.processj.test.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.processj.compiler.ast.Compilation;
import org.processj.test.ProcessJTest;

public class GrammarPragmaUnitTest extends ProcessJTest {

    /**
     * <p>We assert that when a pragma without a string literal is present, then the parse tree contains pragma entries.
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_pragmaWithNoLiteral_IS_present_THEN_parseTreeContainsNonZeroPragmas() {

        final Compilation compilation = CompilationFor(Pragma01);

        Assertions.assertFalse(compilation.getPragmas().isEmpty());

    }

    /**
     * <p>We assert that when a pragma with a string literal is present, then the parse tree contains pragma entries.
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_pragmaWithLiteral_IS_present_THEN_parseTreeContainsNonZeroPragmas() {

        final Compilation compilation = CompilationFor(Pragma02);

        Assertions.assertFalse(compilation.getPragmas().isEmpty());

    }

    /**
     * <p>We assert that when a pragma is present, then the parse tree contains non-null pragma string literal
     * entries.</p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_pragma_IS_present_THEN_pragmaLiteralValueIsNonNull() {

        final Compilation compilation = CompilationFor(Pragma03);

        compilation.getPragmas().forEach(pragma -> Assertions.assertNotNull(pragma.getValue()));

    }

}
