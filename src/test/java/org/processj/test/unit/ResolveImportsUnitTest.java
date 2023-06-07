package org.processj.test.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.Phases;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.SymbolMap;
import org.processj.compiler.phases.phase.ResolveImports;
import org.processj.test.ProcessJTest;
import org.processj.test.TestInputFile;
import org.processj.compiler.utilities.ProcessJSourceFile;

public class ResolveImportsUnitTest extends ProcessJTest {

    private boolean UnitTestFor(final TestInputFile testInputFile, final ProcessJTest processJTest) {

        boolean result = true;

        try {

            Phases.Executor = processJTest;

            // Build the File path
            final String filepath = testInputFile.getRelativePath() + testInputFile.getName() + ".pj";

            // Attempt to open the file
            final ProcessJSourceFile processJSourceFile = Request.Open(filepath);

            // Retrieve the compilation
            final Compilation compilation = this.getPreliminaryCompilationFor(processJSourceFile.getPath());

            // Request the next phase
            final Phase nextPhase = RequestPhase.For(processJSourceFile);

            // Assert we're at ResolveImports
            Assertions.assertTrue(nextPhase instanceof ResolveImports);

            // Execute
            nextPhase.execute(processJSourceFile);

            // Initialize a handle to the Scope
            final SymbolMap symbolMap = compilation.openScope();

            // Assert that all Imports are visible
            compilation.getImports().forEach(importStatement ->
                    Assertions.assertNotNull(symbolMap.get(importStatement.getPackageName())));

        } catch(final Exception exception) {

            System.out.println(exception.getMessage());

            result = false;

        }

        return result;

    }

    @BeforeAll
    public static void initializeSymbolPath() {

        ResolveImports.SymbolPaths.add("src/test/resources/code/imports/");

    }

    @Test
    public void testCode_batch1_hasImportSequence_resolveImportsUnitTest() {

        Assertions.assertTrue(UnitTestFor(ImportsBatch1, this));

    }

}
