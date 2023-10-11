package org.processj.test.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.statement.conditional.AltStatement;
import org.processj.compiler.ast.type.ProcedureType;
import org.processj.compiler.ast.type.Type;
import org.processj.test.ProcessJTest;

public class ParsedAltTest extends ProcessJTest {

    private static void AssertContainsAltStatement(final Compilation compilation, final String procedureTypeName) {

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(procedureTypeName)) {

                Assertions.assertTrue(type instanceof ProcedureType);

                boolean didFindAltStatement = false;

                for(final Statement statement: ((ProcedureType) type).getBody())
                    didFindAltStatement |= statement instanceof AltStatement;

                Assertions.assertTrue(didFindAltStatement);

            }

    }

    private static void AssertAltContainsNonNullInitializationStatements(final Compilation compilation, final String procedureTypeName) {

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(procedureTypeName)) {

                Assertions.assertTrue(type instanceof ProcedureType);

                for(final Statement statement: ((ProcedureType) type).getBody()) {

                    if(statement instanceof AltStatement)
                        Assertions.assertNotNull(((AltStatement) statement).initializationStatements());

                }

            }

    }

    private static void AssertAltContainsNullInitializationStatements(final Compilation compilation, final String procedureTypeName) {

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(procedureTypeName)) {

                Assertions.assertTrue(type instanceof ProcedureType);

                for(final Statement statement: ((ProcedureType) type).getBody()) {

                    if(statement instanceof AltStatement)
                        Assertions.assertNull(((AltStatement) statement).initializationStatements());

                }

            }

    }

    private static void AssertAltContainsNonNullIncrementStatements(final Compilation compilation, final String procedureTypeName) {

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(procedureTypeName)) {

                Assertions.assertTrue(type instanceof ProcedureType);

                for(final Statement statement: ((ProcedureType) type).getBody()) {

                    if(statement instanceof AltStatement)
                        Assertions.assertNotNull(((AltStatement) statement).getIncrementStatements());

                }

            }

    }

    private static void AssertAltContainsNonNullEvaluationExpression(final Compilation compilation, final String procedureTypeName) {

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(procedureTypeName)) {

                Assertions.assertTrue(type instanceof ProcedureType);

                for(final Statement statement: ((ProcedureType) type).getBody()) {

                    if(statement instanceof AltStatement)
                        Assertions.assertNotNull(((AltStatement) statement).getEvaluationExpression());

                }

            }

    }

    private static void AssertAltContainsNullEvaluationExpression(final Compilation compilation, final String procedureTypeName) {

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(procedureTypeName)) {

                Assertions.assertTrue(type instanceof ProcedureType);

                for(final Statement statement: ((ProcedureType) type).getBody()) {

                    if(statement instanceof AltStatement)
                        Assertions.assertNull(((AltStatement) statement).getEvaluationExpression());

                }

            }

    }

    private static void AssertAltContainsNonNullCases(final Compilation compilation, final String procedureTypeName) {

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(procedureTypeName)) {

                Assertions.assertTrue(type instanceof ProcedureType);

                for(final Statement statement: ((ProcedureType) type).getBody()) {

                    if(statement instanceof AltStatement) {

                        Assertions.assertNotNull(((AltStatement) statement).getBody());

                        for(final Statement altCase : ((AltStatement) statement).getBody()) {

                            Assertions.assertNotNull(altCase);

                        }

                    }

                }

            }

    }

    private static void AssertAltContainsOnlyCases(final Compilation compilation, final String procedureTypeName) {

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(procedureTypeName)) {

                Assertions.assertTrue(type instanceof ProcedureType);

                for(final Statement statement: ((ProcedureType) type).getBody()) {

                    if(statement instanceof AltStatement) {

                        Assertions.assertNotNull(((AltStatement) statement).getBody());

                        for(final Statement altCase : ((AltStatement) statement).getBody()) {

                            Assertions.assertNotNull(altCase);
                            Assertions.assertTrue(altCase instanceof AltStatement.Case);

                        }

                    }

                }

            }

    }

    @Test
    public void CompilationFor_alt01_containsNonNullAlt() {

        //final Compilation compilation = CompilationFor(Case.Alt01);

    }

    @Test
    public void CompilationFor_alt02_containsNonNullAlt() {

        final Compilation compilation = CompilationFor(Case.Alt02);

        AssertContainsAltStatement(compilation, "main");
        AssertAltContainsNonNullInitializationStatements(compilation, "main");
        AssertAltContainsNonNullIncrementStatements(compilation, "main");
        AssertAltContainsNullEvaluationExpression(compilation, "main");
        AssertAltContainsNonNullCases(compilation, "main");
        AssertAltContainsOnlyCases(compilation, "main");

    }

    @Test
    public void CompilationFor_alt03_containsNonNullAlt() {

        final Compilation compilation = CompilationFor(Case.Alt03);

        AssertContainsAltStatement(compilation, "main");
        AssertAltContainsNonNullInitializationStatements(compilation, "main");
        AssertAltContainsNonNullIncrementStatements(compilation, "main");
        AssertAltContainsNullEvaluationExpression(compilation, "main");
        AssertAltContainsNonNullCases(compilation, "main");
        AssertAltContainsOnlyCases(compilation, "main");

    }

    @Test
    public void CompilationFor_alt04_containsNonNullAlt() {

        final Compilation compilation = CompilationFor(Case.Alt04);

        AssertContainsAltStatement(compilation, "main");
        AssertAltContainsNonNullInitializationStatements(compilation, "main");
        AssertAltContainsNonNullIncrementStatements(compilation, "main");
        AssertAltContainsNullEvaluationExpression(compilation, "main");
        AssertAltContainsNonNullCases(compilation, "main");
        AssertAltContainsOnlyCases(compilation, "main");

    }

    @Test
    public void CompilationFor_alt05_containsNonNullAlt() {

        final Compilation compilation = CompilationFor(Case.Alt05);

        AssertContainsAltStatement(compilation, "main");
        AssertAltContainsNonNullInitializationStatements(compilation, "main");
        AssertAltContainsNonNullIncrementStatements(compilation, "main");
        AssertAltContainsNullEvaluationExpression(compilation, "main");
        AssertAltContainsNonNullCases(compilation, "main");
        AssertAltContainsOnlyCases(compilation, "main");

    }

    @Test
    public void CompilationFor_alt06_containsNonNullAlt() {

        final Compilation compilation = CompilationFor(Case.Alt06);

        AssertContainsAltStatement(compilation, "main");
        AssertAltContainsNonNullInitializationStatements(compilation, "main");
        AssertAltContainsNonNullIncrementStatements(compilation, "main");
        AssertAltContainsNonNullEvaluationExpression(compilation, "main");
        AssertAltContainsNonNullCases(compilation, "main");
        AssertAltContainsOnlyCases(compilation, "main");

    }

    @Test
    public void CompilationFor_alt07_containsNonNullAlt() {

        final Compilation compilation = CompilationFor(Case.Alt07);

        AssertContainsAltStatement(compilation, "main");
        AssertAltContainsNonNullInitializationStatements(compilation, "main");
        AssertAltContainsNonNullIncrementStatements(compilation, "main");
        AssertAltContainsNonNullEvaluationExpression(compilation, "main");
        AssertAltContainsNonNullCases(compilation, "main");
        AssertAltContainsOnlyCases(compilation, "main");

    }

    @Test
    public void CompilationFor_alt08_containsNonNullAlt() {

        final Compilation compilation = CompilationFor(Case.Alt08);

        AssertContainsAltStatement(compilation, "main");
        AssertAltContainsNonNullInitializationStatements(compilation, "main");
        AssertAltContainsNonNullIncrementStatements(compilation, "main");
        AssertAltContainsNullEvaluationExpression(compilation, "main");
        AssertAltContainsNonNullCases(compilation, "main");
        AssertAltContainsOnlyCases(compilation, "main");

    }

    @Test
    public void CompilationFor_alt09_containsNonNullAlt() {

        final Compilation compilation = CompilationFor(Case.Alt09);

        AssertContainsAltStatement(compilation, "main");
        AssertAltContainsNonNullInitializationStatements(compilation, "main");
        AssertAltContainsNonNullIncrementStatements(compilation, "main");
        AssertAltContainsNonNullEvaluationExpression(compilation, "main");
        AssertAltContainsNonNullCases(compilation, "main");
        AssertAltContainsOnlyCases(compilation, "main");

    }

}
