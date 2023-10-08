package org.processj.test.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.processj.compiler.SourceFile;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.Context;
import org.processj.compiler.phase.Phase;
import org.processj.test.ProcessJTest;
import org.processj.test.TestInputFile;

import static org.processj.compiler.phase.Phase.Request;

public class ResolveImportsUnitTest extends ProcessJTest {


    @BeforeAll
    public static void initializeSymbolPath() {

        //ResolveImports.SymbolPaths.add("src/test/resources/code/imports/");

    }

}
