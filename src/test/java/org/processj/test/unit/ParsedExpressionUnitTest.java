package org.processj.test.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.type.RecordType;
import org.processj.compiler.ast.type.Type;
import org.processj.test.ProcessJTest;

public class ParsedExpressionUnitTest extends ProcessJTest {

    private static void AssertContainsProcedureType(final Compilation compilation, final String recordTypeName) {

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(recordTypeName)) Assertions.assertTrue(type instanceof RecordType);

    }

    @Test
    public void CompilationFor_expression01_() {

        final Compilation compilation = CompilationFor(Case.Expression01);

    }

}
