package org.processj.test.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.type.RecordType;
import org.processj.compiler.ast.type.Type;
import org.processj.test.ProcessJTest;

public class ParsedRecordUnitTest extends ProcessJTest {

    private static void AssertContainsTypeName(final Compilation compilation, final String name) {

        String foundTypeName = "";

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(name)) foundTypeName = name;

        Assertions.assertEquals(name, foundTypeName);

    }

    private static void AssertContainsRecordType(final Compilation compilation, final String recordTypeName) {

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(recordTypeName)) Assertions.assertTrue(type instanceof RecordType);

    }

    private static void AssertRecordTypeContainsNonEmptyBody(final Compilation compilation, final String recordTypeName) {

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(recordTypeName)) {

                Assertions.assertTrue(type instanceof RecordType);

                Assertions.assertFalse(((RecordType) type).getBody().isEmpty());

            }

    }

    private static void AssertRecordTypeContainsEmptyBody(final Compilation compilation, final String recordTypeName) {

        for(final Type type: compilation.getTypeDeclarations())
            if(type.toString().equals(recordTypeName)) {

                Assertions.assertTrue(type instanceof RecordType);

                Assertions.assertTrue(((RecordType) type).getBody().isEmpty());

            }

    }

    private static void AssertRecordTypeBodyContainsOnlyRecordMembers(final Compilation compilation, final String name) {

        for(final Type type: compilation.getTypeDeclarations()) {

            // Check the type name
            if(type.getName().toString().equals(name)) {

                // Assert it's RecordType
                Assertions.assertTrue(type instanceof RecordType);

                // A RecordType is a BlockStatement, so just iterate
                for(final Statement statement: (RecordType) type)
                    Assertions.assertTrue(statement instanceof RecordType.Member);

            }

        }

    }

    private static void AssertRecordTypeBodyContainsMemberCount(final Compilation compilation, final String name, final int memberCount) {

        for(final Type type: compilation.getTypeDeclarations()) {

            // Check the type name
            if(type.getName().toString().equals(name)) {

                // Assert it's RecordType
                Assertions.assertTrue(type instanceof RecordType);

                // Assert the RecordType have the specified count
                Assertions.assertEquals(memberCount, ((RecordType) type).getBody().size());

            }

        }

    }

    @Test
    public void CompilationFor_record01_containsCarRecord() {

        final Compilation compilation = CompilationFor(ProcessJTest.Case.Record01);

        AssertContainsTypeName(compilation, Case.Check.RecordType.Record01);
        AssertContainsRecordType(compilation, ProcessJTest.Case.Check.RecordType.Record01);

    }

    @Test
    public void CompilationFor_record01_bodyOnlyRecordMembers() {

        final Compilation compilation = CompilationFor(Case.Record01);

        AssertContainsTypeName(compilation, Case.Check.RecordType.Record01);
        AssertRecordTypeBodyContainsOnlyRecordMembers(compilation, Case.Check.RecordType.Record01);

    }

    @Test
    public void CompilationFor_record01_bodyNonEmpty() {

        final Compilation compilation = CompilationFor(Case.Record01);

        AssertRecordTypeContainsNonEmptyBody(compilation, Case.Check.RecordType.Record01);

    }

    @Test
    public void CompilationFor_record01_containsTwoMembers() {

        final Compilation compilation = CompilationFor(Case.Record01);

        AssertContainsTypeName(compilation, Case.Check.RecordType.Record01);
        AssertRecordTypeBodyContainsMemberCount(compilation, Case.Check.RecordType.Record01, 2);

    }

}
