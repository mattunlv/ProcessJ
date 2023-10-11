package org.processj.test;

import org.junit.jupiter.api.Assertions;
import org.processj.compiler.Compiler;
import org.processj.compiler.SourceFile;
import org.processj.compiler.ast.Compilation;
import org.processj.compiler.phase.Parser;
import org.processj.compiler.phase.Phase;

import javax.tools.*;
import java.net.URI;
import java.util.List;

public class ProcessJTest extends Phase.Listener {

    /// --------------------------
    /// Protected Static Constants

    /**
     * <p>The directory within the local system where the ProcessJ compiler writes the generated Java output.</p>
     * @since 0.1.0
     */
    protected final static String WorkingDirectory  = "/Users/cuenca/workingpj/";

    /**
     * <p>The directory within the local system of the target test ProcessJ source input to specify to the compiler.</p>
     * @since 0.1.0
     */
    protected final static String InputDirectory    = "src/test/resources/";

    protected final static SourceFile BookSynchronizedCommunication
            = new SourceFile("src/test/resources/code/book/chapter_1/section_2/SynchronizedCommunication.pj");
    protected final static SourceFile BookTimeInProcessJ
            = new SourceFile("src/test/resources/code/book/chapter_3/section_5/TimeInProcessJ.pj");
    protected final static SourceFile BookOneToOneChannels
            = new SourceFile("src/test/resources/code/book/chapter_3/section_6/OneToOneChannels.pj");
    protected final static SourceFile BookFibonacci
            = new SourceFile("src/test/resources/code/book/chapter_3/section_7/Fibonacci.pj");
    protected final static SourceFile BookFifo6
            = new SourceFile("src/test/resources/code/book/chapter_3/section_7/Fifo6.pj");
    protected final static SourceFile BookIntegrate
            = new SourceFile("src/test/resources/code/book/chapter_3/section_7/Integrate.pj");
    protected final static SourceFile BookNumbers
            = new SourceFile("src/test/resources/code/book/chapter_3/section_7/Numbers.pj");
    protected final static SourceFile BookPairs
            = new SourceFile("src/test/resources/code/book/chapter_3/section_7/Pairs.pj");
    protected final static SourceFile BookSquares
            = new SourceFile("src/test/resources/code/book/chapter_3/section_7/Squares.pj");
    protected final static SourceFile BookStructuredExecution
            = new SourceFile("src/test/resources/code/book/chapter_3/section_7/StructuredExecution.pj");
    protected final static SourceFile BookEightBitAdder
            = new SourceFile("src/test/resources/code/book/chapter_3/section_8/EightBitAdder.pj");
    protected final static SourceFile BookFourBitAdder
            = new SourceFile("src/test/resources/code/book/chapter_3/section_8/FourBitAdder.pj");
    protected final static SourceFile BookOneBitAdder
            = new SourceFile("src/test/resources/code/book/chapter_3/section_8/OneBitAdder.pj");
    protected final static SourceFile BookOutput
            = new SourceFile("src/test/resources/code/book/chapter_3/section_9/Output.pj");
    protected final static SourceFile BookSwitch
            = new SourceFile("src/test/resources/code/book/chapter_4/section_1/Switch.pj");
    protected final static SourceFile BookFor
            = new SourceFile("src/test/resources/code/book/chapter_4/section_2/For.pj");
    protected final static SourceFile BookWhile
            = new SourceFile("src/test/resources/code/book/chapter_4/section_2/While.pj");
    protected final static SourceFile BookRepetition
            = new SourceFile("src/test/resources/code/book/chapter_4/section_3/Repetition.pj");
    protected final static SourceFile BookAlt
            = new SourceFile("src/test/resources/code/book/chapter_4/section_4/Alt.pj");
    protected final static SourceFile BookMux
            = new SourceFile("src/test/resources/code/book/chapter_4/section_4/Mux.pj");
    protected final static SourceFile BookTimeoutGuard
            = new SourceFile("src/test/resources/code/book/chapter_4/section_4/TimeoutGuard.pj");
    protected final static SourceFile BookWatchdog
            = new SourceFile("src/test/resources/code/book/chapter_4/section_4/Watchdog.pj");
    protected final static SourceFile BookPriAlt
            = new SourceFile("src/test/resources/code/book/chapter_4/section_5/PriAlt.pj");

    /// ------------
    /// General Code

    protected final static SourceFile Alt01
            = new SourceFile("src/test/resources/code/test/Alt01.pj");
    protected final static SourceFile Array01
            = new SourceFile("src/test/resources/code/test/Array01.pj");
    protected final static SourceFile Array02
            = new SourceFile("src/test/resources/code/test/Array02.pj");
    protected final static SourceFile Barrier01
            = new SourceFile("src/test/resources/code/test/Barrier01.pj");
    protected final static SourceFile Barrier02
            = new SourceFile("src/test/resources/code/test/Barrier02.pj");
    protected final static SourceFile BinaryExpression01
            = new SourceFile("src/test/resources/code/test/BinaryExpression01.pj");
    protected final static SourceFile BinaryExpression02
            = new SourceFile("src/test/resources/code/test/BinaryExpression02.pj");
    protected final static SourceFile ByteCode01
            = new SourceFile("src/test/resources/code/test/ByteCode01.pj");
    protected final static SourceFile ChannelArray01
            = new SourceFile("src/test/resources/code/test/ChannelArray01.pj");
    protected final static SourceFile ChannelArray02
            = new SourceFile("src/test/resources/code/test/ChannelArray02.pj");
    protected final static SourceFile ChannelEndArray01
            = new SourceFile("src/test/resources/code/test/ChannelEndArray01.pj");
    protected final static SourceFile ChannelEndArray02
            = new SourceFile("src/test/resources/code/test/ChannelEndArray02.pj");
    protected final static SourceFile ChannelRead01
            = new SourceFile("src/test/resources/code/test/ChannelRead01.pj");
    protected final static SourceFile ChannelRead02
            = new SourceFile("src/test/resources/code/test/ChannelRead02.pj");
    protected final static SourceFile ChannelWrite01
            = new SourceFile("src/test/resources/code/test/ChannelWrite01.pj");
    protected final static SourceFile ChannelWrite02
            = new SourceFile("src/test/resources/code/test/ChannelWrite02.pj");
    protected final static SourceFile ChannelWrite03
            = new SourceFile("src/test/resources/code/test/ChannelWrite03.pj");
    protected final static SourceFile ChannelWrite04
            = new SourceFile("src/test/resources/code/test/ChannelWrite04.pj");
    protected final static SourceFile Enroll01
            = new SourceFile("src/test/resources/code/test/Enroll01.pj");
    protected final static SourceFile Fibonacci
            = new SourceFile("src/test/resources/code/test/Fibonacci.pj");
    protected final static SourceFile For01
            = new SourceFile("src/test/resources/code/test/For01.pj");
    protected final static SourceFile FullAdder
            = new SourceFile("src/test/resources/code/test/FullAdder.pj");
    protected final static SourceFile Hello
            = new SourceFile("src/test/resources/code/test/Hello.pj");
    protected final static SourceFile If01
            = new SourceFile("src/test/resources/code/test/If01.pj");
    protected final static SourceFile ImportDeclaration01
            = new SourceFile("src/test/resources/code/test/ImportDeclaration01.pj");
    protected final static SourceFile ImportDeclaration02
            = new SourceFile("src/test/resources/code/test/ImportDeclaration02.pj");
    protected final static SourceFile ImportDeclaration03
            = new SourceFile("src/test/resources/code/test/ImportDeclaration03.pj");
    protected final static SourceFile ImportDeclaration04
            = new SourceFile("src/test/resources/code/test/ImportDeclaration04.pj");
    protected final static SourceFile ImportDeclaration05
            = new SourceFile("src/test/resources/code/test/ImportDeclaration05.pj");
    protected final static SourceFile ImportDeclaration06
            = new SourceFile("src/test/resources/code/test/ImportDeclaration06.pj");
    protected final static SourceFile Integrate
            = new SourceFile("src/test/resources/code/test/Integrate.pj");
    protected final static SourceFile LocalDeclaration01
            = new SourceFile("src/test/resources/code/test/LocalDeclaration01.pj");
    protected final static SourceFile Mandelbrot01
            = new SourceFile("src/test/resources/code/test/Mandelbrot01.pj");
    protected final static SourceFile Mandelbrot02
            = new SourceFile("src/test/resources/code/test/Mandelbrot02.pj");
    protected final static SourceFile Mandelbrot03
            = new SourceFile("src/test/resources/code/test/Mandelbrot03.pj");
    protected final static SourceFile Mandelbrot04
            = new SourceFile("src/test/resources/code/test/Mandelbrot04.pj");
    protected final static SourceFile PackageDeclaration01
            = new SourceFile("src/test/resources/code/test/PackageDeclaration01.pj");
    protected final static SourceFile PackageDeclaration02
            = new SourceFile("src/test/resources/code/test/PackageDeclaration02.pj");
    protected final static SourceFile PackageDeclaration03
            = new SourceFile("src/test/resources/code/test/PackageDeclaration03.pj");
    protected final static SourceFile Par01
            = new SourceFile("src/test/resources/code/test/Par01.pj");
    protected final static SourceFile Par02
            = new SourceFile("src/test/resources/code/test/Par02.pj");
    protected final static SourceFile Par03
            = new SourceFile("src/test/resources/code/test/Par03.pj");
    protected final static SourceFile Pragma01
            = new SourceFile("src/test/resources/code/test/Pragma01.pj");
    protected final static SourceFile Pragma02
            = new SourceFile("src/test/resources/code/test/Pragma02.pj");
    protected final static SourceFile Pragma03
            = new SourceFile("src/test/resources/code/test/Pragma03.pj");
    protected final static SourceFile PragmaLibraryFail01
            = new SourceFile("src/test/resources/code/test/PragmaLibraryFail01.pj");
    protected final static SourceFile PragmaLibraryFail02
            = new SourceFile("src/test/resources/code/test/PragmaLibraryFail02.pj");
    protected final static SourceFile PriAlt01
            = new SourceFile("src/test/resources/code/test/PriAlt01.pj");
    protected final static SourceFile Protocol01
            = new SourceFile("src/test/resources/code/test/Protocol01.pj");
    protected final static SourceFile Protocol02
            = new SourceFile("src/test/resources/code/test/Protocol02.pj");
    protected final static SourceFile Protocol03
            = new SourceFile("src/test/resources/code/test/Protocol03.pj");
    protected final static SourceFile Santa01
            = new SourceFile("src/test/resources/code/test/Santa01.pj");
    protected final static SourceFile Santa02
            = new SourceFile("src/test/resources/code/test/Santa02.pj");
    protected final static SourceFile Santa03
            = new SourceFile("src/test/resources/code/test/Santa03.pj");
    protected final static SourceFile SharedChannel01
            = new SourceFile("src/test/resources/code/test/SharedChannel01.pj");
    protected final static SourceFile SharedChannelRead01
            = new SourceFile("src/test/resources/code/test/SharedChannelRead01.pj");
    protected final static SourceFile SharedChannelWrite01
            = new SourceFile("src/test/resources/code/test/SharedChannelWrite01.pj");
    protected final static SourceFile Silly
            = new SourceFile("src/test/resources/code/test/Silly.pj");
    protected final static SourceFile SortPump
            = new SourceFile("src/test/resources/code/test/SortPump.pj");
    protected final static SourceFile Switch01
            = new SourceFile("src/test/resources/code/test/Switch01.pj");
    protected final static SourceFile Timer01
            = new SourceFile("src/test/resources/code/test/Timer01.pj");

    /// ------------
    /// Import Tests

    protected final static SourceFile ImportsBatch1
            = new SourceFile("org/batch1/ruby.pj");

    /// ----------
    /// Test Cases

    public static class Case {

        public final static SourceFile Alt01
                = new SourceFile("src/test/resources/code/test/Alt01.pj");
        public final static SourceFile Alt02
                = new SourceFile("src/test/resources/code/test/Alt02.pj");
        public final static SourceFile Alt03
                = new SourceFile("src/test/resources/code/test/Alt03.pj");
        public final static SourceFile Alt04
                = new SourceFile("src/test/resources/code/test/Alt04.pj");
        public final static SourceFile Alt05
                = new SourceFile("src/test/resources/code/test/Alt05.pj");
        public final static SourceFile Alt06
                = new SourceFile("src/test/resources/code/test/Alt06.pj");
        public final static SourceFile Alt07
                = new SourceFile("src/test/resources/code/test/Alt07.pj");
        public final static SourceFile Alt08
                = new SourceFile("src/test/resources/code/test/Alt08.pj");
        public final static SourceFile Alt09
                = new SourceFile("src/test/resources/code/test/Alt09.pj");
        public final static SourceFile Alt10
                = new SourceFile("src/test/resources/code/test/Alt10.pj");
        public final static SourceFile Empty
                = new SourceFile("src/test/resources/code/test/Empty.pj");
        public final static SourceFile Expression01
                = new SourceFile("src/test/resources/code/test/Expression01.pj");
        public final static SourceFile ImportDeclaration01
                = new SourceFile("src/test/resources/code/test/ImportDeclaration01.pj");
        public final static SourceFile ImportDeclaration02
                = new SourceFile("src/test/resources/code/test/ImportDeclaration02.pj");
        public final static SourceFile ImportDeclaration03
                = new SourceFile("src/test/resources/code/test/ImportDeclaration03.pj");
        public final static SourceFile ImportDeclaration04
                = new SourceFile("src/test/resources/code/test/ImportDeclaration04.pj");
        public final static SourceFile ImportDeclaration05
                = new SourceFile("src/test/resources/code/test/ImportDeclaration05.pj");
        public final static SourceFile ImportDeclaration06
                = new SourceFile("src/test/resources/code/test/ImportDeclaration06.pj");
        public final static SourceFile ImportDeclaration07
                = new SourceFile("src/test/resources/code/test/ImportDeclaration07.pj");
        public final static SourceFile PackageDeclaration01
                = new SourceFile("src/test/resources/code/test/PackageDeclaration01.pj");
        public final static SourceFile PackageDeclaration02
                = new SourceFile("src/test/resources/code/test/PackageDeclaration02.pj");
        public final static SourceFile PackageDeclaration03
                = new SourceFile("src/test/resources/code/test/PackageDeclaration03.pj");
        public final static SourceFile Pragma01
                = new SourceFile("src/test/resources/code/test/Pragma01.pj");
        public final static SourceFile Pragma02
                = new SourceFile("src/test/resources/code/test/Pragma02.pj");
        public final static SourceFile Pragma03
                = new SourceFile("src/test/resources/code/test/Pragma03.pj");
        public final static SourceFile Pragma04
                = new SourceFile("src/test/resources/code/test/Pragma04.pj");
        public final static SourceFile Record01
                = new SourceFile("src/test/resources/code/test/Record01.pj");
        public final static SourceFile Record02
                = new SourceFile("src/test/resources/code/test/Record02.pj");
        public final static SourceFile Record03
                = new SourceFile("src/test/resources/code/test/Record03.pj");
        public final static SourceFile Record04
                = new SourceFile("src/test/resources/code/test/Record04.pj");
        public final static SourceFile Record05
                = new SourceFile("src/test/resources/code/test/Record05.pj");
        public static class Check {

            public static class PackageName {

                public final static String Empty = "";
                public final static String ImportDeclaration01 = "";
                public final static String ImportDeclaration02 = "";
                public final static String ImportDeclaration03 = "";
                public final static String ImportDeclaration04 = "";
                public final static String ImportDeclaration05 = "";
                public final static String ImportDeclaration06 = "";
                public final static String ImportDeclaration07 = "";
                public final static String PackageDeclaration01 = "org.processj.test";
                public final static String PackageDeclaration02 = "org.processj";
                public final static String PackageDeclaration03 = "org";
                public final static String Pragma01 = "";
                public final static String Pragma02 = "";
                public final static String Pragma03 = "";
                public final static String Pragma04 = "";

            }

            public static class ImportName {

                public final static String ImportDeclaration01 = "org.processj.test";
                public final static String ImportDeclaration02 = "org.processj";
                public final static String ImportDeclaration03 = "org";
                public final static String ImportDeclaration04 = "org.processj.test.*";
                public final static String ImportDeclaration05 = "org.processj.*";
                public final static String ImportDeclaration06 = "org.*";

            }

            public static class RecordType {

                public final static String Record01 = "Car";

            }

        }

    }
    
    /// ----------------------
    /// Private Static Methods

    private static JavaCompiler getJavaCompiler() {

        // Attempt to retrieve the java compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // If the compiler is null, attempt
        if(compiler == null) try {

            // To retrieve via reflection
            compiler = (JavaCompiler) Class.forName("com.sun.tools.javac.api.JavacTool")
                    .getMethod("create")
                    .invoke(null);

        // Otherwise
        } catch (final Exception exception) {

            // Throw the error
            throw new AssertionError(exception);

        }

        // Return the result
        return compiler;

    }

    private static SimpleJavaFileObject SimpleJavaFileObjectOf(final SourceFile testInputFile) {

        return new SimpleJavaFileObject(URI.create("string:///" + testInputFile.getPath()), JavaFileObject.Kind.SOURCE) {

            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {

                return testInputFile.toString();

            }

        };

    }

    /// ------------------------
    /// Protected Static Methods

    protected static boolean CompileJava(final SourceFile testInputFile) {

        // Initialize the Diagnostics Collector
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        // Invoke & return the result
        return getJavaCompiler().getTask(
                null, null, diagnostics,
                null, null, List.of(SimpleJavaFileObjectOf(testInputFile))).call();

    }

    protected static Compilation CompilationFor(final SourceFile testInputFile) {

        // Initialize the result
        Compilation compilation = null;

        // Attempt to
        try {

            final Parser parser = new Parser(new Compiler());

            // Execute the parsing Phase
            parser.execute(testInputFile);

            // Update the Compilation
            compilation = testInputFile.getCompilation();

        // Otherwise
        } catch(final Exception exception) {

            // Print the error message
            System.out.println(exception.getMessage());

        }

        // Check
        Assertions.assertNotNull(compilation);

        // Return the result
        return compilation;

    }

}
