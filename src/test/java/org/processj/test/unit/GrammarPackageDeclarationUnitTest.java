package org.processj.test.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.processj.compiler.ast.Compilation;
import org.processj.test.ProcessJTest;

public class GrammarPackageDeclarationUnitTest extends ProcessJTest {

    /**
     * <p>We assert that when a Package Declaration is present, then the parse tree contains a non-empty package name.
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonEmptyPackageName_01() {

        final Compilation compilation = CompilationFor(PackageDeclaration01);

        Assertions.assertFalse(compilation.getPackageName().isEmpty());

    }

    /**
     * <p>We assert that when a Package Declaration is present, then the parse tree contains a non-null package name
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonNullPackageName_01() {

        final Compilation compilation = CompilationFor(PackageDeclaration01);

        Assertions.assertNotNull(compilation.getPackageName());

    }

    /**
     * <p>We assert that when a Package Declaration is present, then the parse tree defines a package name
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeDefinesPackageName_01() {

        final Compilation compilation = CompilationFor(PackageDeclaration01);

        Assertions.assertTrue(compilation.definesPackageName());

    }

    /**
     * <p>We assert that when a Package Declaration is present, then the parse tree contains a non-empty package name.
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonEmptyPackageName_02() {

        final Compilation compilation = CompilationFor(PackageDeclaration02);

        Assertions.assertFalse(compilation.getPackageName().isEmpty());

    }

    /**
     * <p>We assert that when a Package Declaration is present, then the parse tree contains a non-null package name
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonNullPackageName_02() {

        final Compilation compilation = CompilationFor(PackageDeclaration02);

        Assertions.assertNotNull(compilation.getPackageName());

    }

    /**
     * <p>We assert that when a Package Declaration is present, then the parse tree defines a package name
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeDefinesPackageName_02() {

        final Compilation compilation = CompilationFor(PackageDeclaration02);

        Assertions.assertTrue(compilation.definesPackageName());

    }

    /**
     * <p>We assert that when a Package Declaration is present, then the parse tree contains a non-empty package name.
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonEmptyPackageName_03() {

        final Compilation compilation = CompilationFor(PackageDeclaration03);

        Assertions.assertFalse(compilation.getPackageName().isEmpty());

    }

    /**
     * <p>We assert that when a Package Declaration is present, then the parse tree contains a non-null package name
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeContainsNonNullPackageName_03() {

        final Compilation compilation = CompilationFor(PackageDeclaration03);

        Assertions.assertNotNull(compilation.getPackageName());

    }

    /**
     * <p>We assert that when a Package Declaration is present, then the parse tree defines a package name
     * </p>
     * @since 0.1.0
     * @see Compilation
     * @see org.processj.compiler.ast.packages.Pragmas
     * @see org.processj.compiler.ast.packages.Pragma
     */
    @Test
    public final void WHEN_packageDeclaration_IS_present_THEN_parseTreeDefinesPackageName_03() {

        final Compilation compilation = CompilationFor(PackageDeclaration03);

        Assertions.assertTrue(compilation.definesPackageName());

    }

}
