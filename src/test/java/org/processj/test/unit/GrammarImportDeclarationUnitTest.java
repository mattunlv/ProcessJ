package org.processj.test.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.processj.compiler.ast.Compilation;
import org.processj.test.ProcessJTest;

public class GrammarImportDeclarationUnitTest extends ProcessJTest {

    /**
     * <p>We assert that when a Import Declaration is present, then the parse tree contains a non-empty set of imports.
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonEmptyImportName_01() {

        final Compilation compilation = CompilationFor(ImportDeclaration01);

        Assertions.assertFalse(compilation.getImports().isEmpty());

    }

    /**
     * <p>We assert that when a Import Declaration is present, then the parse tree contains a non-null set of imports
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonNullImportName_01() {

        final Compilation compilation = CompilationFor(ImportDeclaration01);

        Assertions.assertNotNull(compilation.getImports());

    }

    /**
     * <p>We assert that when a Import Declaration is present, then the parse tree contains a non-empty set of imports.
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonEmptyImportName_02() {

        final Compilation compilation = CompilationFor(ImportDeclaration02);

        Assertions.assertFalse(compilation.getImports().isEmpty());

    }

    /**
     * <p>We assert that when a Import Declaration is present, then the parse tree contains a non-null set of imports
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonNullImportName_02() {

        final Compilation compilation = CompilationFor(ImportDeclaration02);

        Assertions.assertNotNull(compilation.getImports());

    }

    /**
     * <p>We assert that when a Import Declaration is present, then the parse tree contains a non-empty set of imports.
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonEmptyImportName_03() {

        final Compilation compilation = CompilationFor(ImportDeclaration03);

        Assertions.assertFalse(compilation.getImports().isEmpty());

    }

    /**
     * <p>We assert that when a Import Declaration is present, then the parse tree contains a non-null set of imports
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonNullImportName_03() {

        final Compilation compilation = CompilationFor(ImportDeclaration03);

        Assertions.assertNotNull(compilation.getImports());

    }

    /**
     * <p>We assert that when a Import Declaration is present, then the parse tree contains a non-empty set of imports.
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonEmptyImportName_04() {

        final Compilation compilation = CompilationFor(ImportDeclaration04);

        Assertions.assertFalse(compilation.getImports().isEmpty());

    }

    /**
     * <p>We assert that when a Import Declaration is present, then the parse tree contains a non-null set of imports
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonNullImportName_04() {

        final Compilation compilation = CompilationFor(ImportDeclaration04);

        Assertions.assertNotNull(compilation.getImports());

    }

    /**
     * <p>We assert that when a Import Declaration is present, then the parse tree contains a non-empty set of imports.
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonEmptyImportName_05() {

        final Compilation compilation = CompilationFor(ImportDeclaration05);

        Assertions.assertFalse(compilation.getImports().isEmpty());

    }

    /**
     * <p>We assert that when a Import Declaration is present, then the parse tree contains a non-null set of imports
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonNullImportName_05() {

        final Compilation compilation = CompilationFor(ImportDeclaration05);

        Assertions.assertNotNull(compilation.getImports());

    }

    /**
     * <p>We assert that when a Import Declaration is present, then the parse tree contains a non-empty set of imports.
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonEmptyImportName_06() {

        final Compilation compilation = CompilationFor(ImportDeclaration06);

        Assertions.assertFalse(compilation.getImports().isEmpty());

    }

    /**
     * <p>We assert that when a Import Declaration is present, then the parse tree contains a non-null set of imports
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonNullImportName_06() {

        final Compilation compilation = CompilationFor(ImportDeclaration06);

        Assertions.assertNotNull(compilation.getImports());

    }

}
