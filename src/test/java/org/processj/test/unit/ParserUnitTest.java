package org.processj.test.unit;

import org.processj.compiler.ast.packages.Pragmas;
import org.processj.compiler.ast.packages.Pragma;
import org.processj.compiler.ast.packages.Import;
import org.processj.compiler.ast.packages.Imports;
import org.processj.compiler.ast.Name;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.processj.compiler.ast.Compilation;
import org.processj.test.ProcessJTest;

public class ParserUnitTest extends ProcessJTest {

    private static void AssertNonNullImports(final Compilation compilation) {

        Assertions.assertNotNull(compilation.getImports());

    }

    private static void AssertNonNullPragmas(final Compilation compilation) {

        Assertions.assertNotNull(compilation.getPragmas());

    }

    private static void AssertNonNullPackageName(final Compilation compilation) {

        Assertions.assertNotNull(compilation.getPackageName());

    }

    private static void AssertExpectedPackageName(final Compilation compilation, final String expectedPackageName) {

        Assertions.assertEquals(expectedPackageName, compilation.getPackageName());

    }

    private static void AssertContainsEmptyPragmas(final Compilation compilation) {

        Assertions.assertTrue(compilation.getPragmas().isEmpty());

    }

    private static void AssertContainsNonEmptyPragmas(final Compilation compilation) {

        Assertions.assertFalse(compilation.getPragmas().isEmpty());

    }

    private static void AssertContainsPragmas(final Compilation compilation, final int expectedPragmas) {

        Assertions.assertEquals(expectedPragmas, compilation.getPragmas().size());

    }

    /// ----------------------------------------------------------------------------------------------------------- ///
    /// Import Declaration Sanity Tests                                                                             ///
    /// ----------------------------------------------------------------------------------------------------------- ///

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the Empty test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_empty_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.Empty);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration01}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration01_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration01);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration02}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration02_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration02);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration03}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration03_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration03);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration04}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration04_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration04);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration05}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration05_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration05);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration06}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration06_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration06);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#PackageDeclaration01}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration01_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration01);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#PackageDeclaration02}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration02_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration02);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#PackageDeclaration03}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration03_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration03);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#Pragma01}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma01_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.Pragma01);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#Pragma02}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma02_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.Pragma02);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#Pragma03}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma03_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.Pragma03);

        AssertNonNullImports(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Imports} instance is non-null for the
     * {@link ProcessJTest.Case#Pragma04}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma04_containsNonNullImports() {

        final Compilation compilation = CompilationFor(Case.Pragma04);

        AssertNonNullImports(compilation);

    }

    /// ----------------------------------------------------------------------------------------------------------- ///
    /// Package Declaration Sanity Tests                                                                            ///
    /// ----------------------------------------------------------------------------------------------------------- ///

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the Empty test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_empty_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.Empty);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration01} Test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration01_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration01);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration02} Test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration02_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration02);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration03} Test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration03_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration03);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration04} Test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration04_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration04);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration05} Test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration05_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration05);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration06} Test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration06_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration06);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#PackageDeclaration01} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration01_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration01);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#PackageDeclaration02} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration02_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration02);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#PackageDeclaration03} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration03_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration03);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#Pragma01} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma01_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.Pragma01);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#Pragma02} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma02_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.Pragma02);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#Pragma03} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma03_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.Pragma03);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is non-null for the
     * {@link ProcessJTest.Case#Pragma04} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma04_containsNonNullPackageName() {

        final Compilation compilation = CompilationFor(Case.Pragma04);

        AssertNonNullPackageName(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#Empty}.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_empty_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.Empty);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.Empty);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#ImportDeclaration01} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration01_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration01);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.ImportDeclaration01);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#ImportDeclaration02} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration02_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration02);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.ImportDeclaration02);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#ImportDeclaration03} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration03_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration03);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.ImportDeclaration03);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#ImportDeclaration04} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration04_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration04);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.ImportDeclaration04);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#ImportDeclaration05} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration05_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration05);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.ImportDeclaration05);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#ImportDeclaration06} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration06_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration06);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.ImportDeclaration06);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#PackageDeclaration01} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration01_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration01);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.PackageDeclaration01);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#PackageDeclaration02} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration02_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration02);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.PackageDeclaration02);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#PackageDeclaration03} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration03_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration03);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.PackageDeclaration03);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#Pragma01} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma01_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.Pragma01);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.Pragma01);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#Pragma02} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma02_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.Pragma02);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.Pragma02);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#Pragma03} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma03_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.Pragma03);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.Pragma03);

    }

    /**
     * Assert that the {@link Compilation}'s package {@link Name} instance is the expected value for the
     * {@link ProcessJTest.Case#Pragma04} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma04_containsExpectedPackageName() {

        final Compilation compilation = CompilationFor(Case.Pragma04);

        AssertExpectedPackageName(compilation, Case.Check.PackageName.Pragma04);

    }

    /// ----------------------------------------------------------------------------------------------------------- ///
    /// Pragma Sanity Tests                                                                                         ///
    /// ----------------------------------------------------------------------------------------------------------- ///

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the Empty test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_empty_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.Empty);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration01}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration01_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration01);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration02}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration02_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration02);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration03}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration03_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration03);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration04}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration04_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration04);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration05}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration05_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration05);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the
     * {@link ProcessJTest.Case#ImportDeclaration06}
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration06_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration06);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the
     * {@link ProcessJTest.Case#PackageDeclaration01} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration01_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration01);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the
     * {@link ProcessJTest.Case#PackageDeclaration02} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration02_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration02);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the
     * {@link ProcessJTest.Case#PackageDeclaration03} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration03_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration03);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the Pragma01 test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma01_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.Pragma01);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the Pragma02 test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma02_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.Pragma02);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the Pragma03 test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma03_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.Pragma03);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the Pragma04 test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma04_containsNonNullPragmas() {

        final Compilation compilation = CompilationFor(Case.Pragma04);

        AssertNonNullPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the Empty test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_empty_containsNonEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.Empty);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is empty for the
     * {@link ProcessJTest.Case#ImportDeclaration01} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration01_containsEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration01);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is empty for the
     * {@link ProcessJTest.Case#ImportDeclaration02} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration02_containsEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration02);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is empty for the
     * {@link ProcessJTest.Case#ImportDeclaration03} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration03_containsEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration03);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is empty for the
     * {@link ProcessJTest.Case#ImportDeclaration04} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration04_containsEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration04);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is empty for the
     * {@link ProcessJTest.Case#ImportDeclaration05} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration05_containsEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration05);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is empty for the
     * {@link ProcessJTest.Case#ImportDeclaration06} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_importDeclaration06_containsEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration06);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is empty for the
     * {@link ProcessJTest.Case#PackageDeclaration01} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration01_containsEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration01);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is empty for the
     * {@link ProcessJTest.Case#PackageDeclaration02} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration02_containsEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration02);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is empty for the
     * {@link ProcessJTest.Case#PackageDeclaration03} test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_packageDeclaration03_containsEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration03);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the Pragma01 test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma01_containsNonEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.Pragma01);

        AssertNonNullPragmas(compilation);
        AssertContainsNonEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the Pragma02 test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma02_containsNonEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.Pragma02);

        AssertNonNullPragmas(compilation);
        AssertContainsNonEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the Pragma03 test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma03_containsNonEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.Pragma03);

        AssertNonNullPragmas(compilation);
        AssertContainsNonEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance is non-null for the Pragma04 test.
     * @see Compilation
     * @see Pragmas
     */
    @Test
    public void CompilationFor_pragma04_containsNonEmptyPragmas() {

        final Compilation compilation = CompilationFor(Case.Pragma04);

        AssertNonNullPragmas(compilation);
        AssertContainsNonEmptyPragmas(compilation);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains no {@link Pragma} instances for the
     * Empty test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_empty_containsNoPragmas() {

        final Compilation compilation = CompilationFor(Case.Empty);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 0);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains no {@link Pragma} instances for the
     * {@link ProcessJTest.Case#ImportDeclaration01} test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_importDeclaration01_containsNoPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration01);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 0);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains no {@link Pragma} instances for the
     * {@link ProcessJTest.Case#ImportDeclaration02} test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_importDeclaration02_containsNoPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration02);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 0);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains no {@link Pragma} instances for the
     * {@link ProcessJTest.Case#ImportDeclaration03} test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_importDeclaration03_containsNoPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration03);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 0);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains no {@link Pragma} instances for the
     * {@link ProcessJTest.Case#ImportDeclaration04} test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_importDeclaration04_containsNoPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration04);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 0);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains no {@link Pragma} instances for the
     * {@link ProcessJTest.Case#ImportDeclaration05} test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_importDeclaration05_containsNoPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration05);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 0);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains no {@link Pragma} instances for the
     * {@link ProcessJTest.Case#ImportDeclaration06} test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_importDeclaration06_containsNoPragmas() {

        final Compilation compilation = CompilationFor(Case.ImportDeclaration06);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 0);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains no {@link Pragma} instances for the
     * {@link ProcessJTest.Case#PackageDeclaration01} test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_packageDeclaration01_containsNoPragmas() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration01);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 0);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains no {@link Pragma} instances for the
     * {@link ProcessJTest.Case#PackageDeclaration02} test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_packageDeclaration02_containsNoPragmas() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration02);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 0);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains no {@link Pragma} instances for the
     * {@link ProcessJTest.Case#PackageDeclaration03} test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_packageDeclaration03_containsNoPragmas() {

        final Compilation compilation = CompilationFor(Case.PackageDeclaration03);

        AssertNonNullPragmas(compilation);
        AssertContainsEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 0);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains 1 {@link Pragma} instance for the
     * Pragma01 test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_pragma01_containsOnePragma() {

        final Compilation compilation = CompilationFor(Case.Pragma01);

        AssertNonNullPragmas(compilation);
        AssertContainsNonEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 1);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains 1 {@link Pragma} instance for the
     * Pragma02 test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_pragma02_containsOnePragmas() {

        final Compilation compilation = CompilationFor(Case.Pragma02);

        AssertNonNullPragmas(compilation);
        AssertContainsNonEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 1);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains 2 {@link Pragma} instances for the
     * Pragma03 test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_pragma03_containsTwoPragmas() {

        final Compilation compilation = CompilationFor(Case.Pragma03);

        AssertNonNullPragmas(compilation);
        AssertContainsNonEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 2);

    }

    /**
     * Assert that the {@link Compilation}'s {@link Pragmas} instance contains 33 {@link Pragma} instances for the
     * Pragma04 test.
     * @see Compilation
     * @see Pragmas
     * @see Pragma
     */
    @Test
    public void CompilationFor_pragma04_contains33Pragmas() {

        final Compilation compilation = CompilationFor(Case.Pragma04);

        AssertNonNullPragmas(compilation);
        AssertContainsNonEmptyPragmas(compilation);
        AssertContainsPragmas(compilation, 33);

    }

}
