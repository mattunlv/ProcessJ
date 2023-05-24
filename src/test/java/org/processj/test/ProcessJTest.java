package org.processj.test;

import org.junit.jupiter.api.Assertions;
import org.processj.lexer.Lexer;

import javax.tools.*;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class ProcessJTest {

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

    protected final static TestInputFile BookSynchronizedCommunication
            = new TestInputFile("code/book/chapter_1/section_2/", "SynchronizedCommunication",
                        "pj", "java");
    protected final static TestInputFile BookTimeInProcessJ
            = new TestInputFile("code/book/chapter_3/section_5/", "TimeInProcessJ",
                        "pj", "java");
    protected final static TestInputFile BookOneToOneChannels
            = new TestInputFile("code/book/chapter_3/section_6/", "OneToOneChannels",
                        "pj", "java");
    protected final static TestInputFile BookFibonacci
            = new TestInputFile("code/book/chapter_3/section_7/", "Fibonacci",
                        "pj", "java");
    protected final static TestInputFile BookFifo6
            = new TestInputFile("code/book/chapter_3/section_7/", "Fifo6",
                        "pj", "java");
    protected final static TestInputFile BookIntegrate
            = new TestInputFile("code/book/chapter_3/section_7/", "Integrate",
                        "pj", "java");
    protected final static TestInputFile BookNumbers
            = new TestInputFile("code/book/chapter_3/section_7/", "Numbers",
                        "pj", "java");
    protected final static TestInputFile BookPairs
            = new TestInputFile("code/book/chapter_3/section_7/", "Pairs",
                        "pj", "java");
    protected final static TestInputFile BookSquares
            = new TestInputFile("code/book/chapter_3/section_7/", "Squares",
                        "pj", "java");
    protected final static TestInputFile BookStructuredExecution
            = new TestInputFile("code/book/chapter_3/section_7/", "StructuredExecution",
                        "pj", "java");
    protected final static TestInputFile BookEightBitAdder
            = new TestInputFile("code/book/chapter_3/section_8/", "EightBitAdder",
                        "pj", "java");
    protected final static TestInputFile BookFourBitAdder
            = new TestInputFile("code/book/chapter_3/section_8/", "FourBitAdder",
                        "pj", "java");
    protected final static TestInputFile BookOneBitAdder
            = new TestInputFile("code/book/chapter_3/section_8/", "OneBitAdder",
                        "pj", "java");
    protected final static TestInputFile BookOutput
            = new TestInputFile("code/book/chapter_3/section_9/", "Output",
                        "pj", "java");
    protected final static TestInputFile BookSwitch
            = new TestInputFile("code/book/chapter_4/section_1/", "Switch",
                        "pj", "java");
    protected final static TestInputFile BookFor
            = new TestInputFile("code/book/chapter_4/section_2/", "For",
                        "pj", "java");
    protected final static TestInputFile BookWhile
            = new TestInputFile("code/book/chapter_4/section_2/", "While",
                        "pj", "java");
    protected final static TestInputFile BookRepetition
            = new TestInputFile("code/book/chapter_4/section_3/", "Repetition",
                        "pj", "java");
    protected final static TestInputFile BookAlt
            = new TestInputFile("code/book/chapter_4/section_4/", "Alt",
                        "pj", "java");
    protected final static TestInputFile BookMux
            = new TestInputFile("code/book/chapter_4/section_4/", "Mux",
                        "pj", "java");
    protected final static TestInputFile BookTimeoutGuard
            = new TestInputFile("code/book/chapter_4/section_4/", "TimeoutGuard",
                        "pj", "java");
    protected final static TestInputFile BookWatchdog
            = new TestInputFile("code/book/chapter_4/section_4/", "Watchdog",
                        "pj", "java");
    protected final static TestInputFile BookPriAlt
            = new TestInputFile("code/book/chapter_4/section_5/", "PriAlt",
                        "pj", "java");

    /// ------------
    /// General Code

    protected final static TestInputFile Alt01
            = new TestInputFile("code/test/", "Alt01",
                        "pj", "java");
    protected final static TestInputFile Array01
            = new TestInputFile("code/test/", "Array01",
                        "pj", "java");
    protected final static TestInputFile Array02
            = new TestInputFile("code/test/", "Array02",
                        "pj", "java");
    protected final static TestInputFile Barrier01
            = new TestInputFile("code/test/", "Barrier01",
                        "pj", "java");
    protected final static TestInputFile Barrier02
            = new TestInputFile("code/test/", "Barrier02",
                        "pj", "java");
    protected final static TestInputFile BinaryExpression01
            = new TestInputFile("code/test/", "BinaryExpression01",
                        "pj", "java");
    protected final static TestInputFile BinaryExpression02
            = new TestInputFile("code/test/", "BinaryExpression02",
                        "pj", "java");
    protected final static TestInputFile ByteCode01
            = new TestInputFile("code/test/", "ByteCode01",
                        "pj", "java");
    protected final static TestInputFile ChannelArray01
            = new TestInputFile("code/test/", "ChannelArray01",
                        "pj", "java");
    protected final static TestInputFile ChannelArray02
            = new TestInputFile("code/test/", "ChannelArray02",
                        "pj", "java");
    protected final static TestInputFile ChannelEndArray01
            = new TestInputFile("code/test/", "ChannelEndArray01",
                        "pj", "java");
    protected final static TestInputFile ChannelEndArray02
            = new TestInputFile("code/test/", "ChannelEndArray02",
                        "pj", "java");
    protected final static TestInputFile ChannelRead01
            = new TestInputFile("code/test/", "ChannelRead01",
                        "pj", "java");
    protected final static TestInputFile ChannelRead02
            = new TestInputFile("code/test/", "ChannelRead02",
                        "pj", "java");
    protected final static TestInputFile ChannelWrite01
            = new TestInputFile("code/test/", "ChannelWrite01",
                        "pj", "java");
    protected final static TestInputFile ChannelWrite02
            = new TestInputFile("code/test/", "ChannelWrite02",
                        "pj", "java");
    protected final static TestInputFile ChannelWrite03
            = new TestInputFile("code/test/", "ChannelWrite03",
                        "pj", "java");
    protected final static TestInputFile ChannelWrite04
            = new TestInputFile("code/test/", "ChannelWrite04",
                        "pj", "java");
    protected final static TestInputFile Enroll01
            = new TestInputFile("code/test/", "Enroll01",
                        "pj", "java");
    protected final static TestInputFile Fibonacci
            = new TestInputFile("code/test/", "Fibonacci",
                        "pj", "java");
    protected final static TestInputFile For01
            = new TestInputFile("code/test/", "For01",
                        "pj", "java");
    protected final static TestInputFile FullAdder
            = new TestInputFile("code/test/", "FullAdder",
                        "pj", "java");
    protected final static TestInputFile Hello
            = new TestInputFile("code/test/", "Hello",
                        "pj", "java");
    protected final static TestInputFile If01
            = new TestInputFile("code/test/", "If01",
                        "pj", "java");
    protected final static TestInputFile Integrate
            = new TestInputFile("code/test/", "Integrate",
                        "pj", "java");
    protected final static TestInputFile LocalDeclaration01
            = new TestInputFile("code/test/", "LocalDeclaration01",
                        "pj", "java");
    protected final static TestInputFile Mandelbrot01
            = new TestInputFile("code/test/", "Mandelbrot01",
                        "pj", "java");
    protected final static TestInputFile Mandelbrot02
            = new TestInputFile("code/test/", "Mandelbrot02",
                        "pj", "java");
    protected final static TestInputFile Mandelbrot03
            = new TestInputFile("code/test/", "Mandelbrot03",
                        "pj", "java");
    protected final static TestInputFile Mandelbrot04
            = new TestInputFile("code/test/", "Mandelbrot04",
                        "pj", "java");
    protected final static TestInputFile Par01
            = new TestInputFile("code/test/", "Par01",
                        "pj", "java");
    protected final static TestInputFile Par02
            = new TestInputFile("code/test/", "Par02",
                        "pj", "java");
    protected final static TestInputFile Par03
            = new TestInputFile("code/test/", "Par03",
                        "pj", "java");
    protected final static TestInputFile PriAlt01
            = new TestInputFile("code/test/", "PriAlt01",
                        "pj", "java");
    protected final static TestInputFile Protocol01
            = new TestInputFile("code/test/", "Protocol01",
                        "pj", "java");
    protected final static TestInputFile Protocol02
            = new TestInputFile("code/test/", "Protocol02",
                        "pj", "java");
    protected final static TestInputFile Protocol03
            = new TestInputFile("code/test/", "Protocol03",
                        "pj", "java");
    protected final static TestInputFile Record01
            = new TestInputFile("code/test/", "Record01",
                        "pj", "java");
    protected final static TestInputFile Record02
            = new TestInputFile("code/test/", "Record02",
                        "pj", "java");
    protected final static TestInputFile Record03
            = new TestInputFile("code/test/", "Record03",
                        "pj", "java");
    protected final static TestInputFile Record04
            = new TestInputFile("code/test/", "Record04",
                        "pj", "java");
    protected final static TestInputFile Record05
            = new TestInputFile("code/test/", "Record05",
                        "pj", "java");
    protected final static TestInputFile Santa01
            = new TestInputFile("code/test/", "Santa01",
                        "pj", "java");
    protected final static TestInputFile Santa02
            = new TestInputFile("code/test/", "Santa02",
                        "pj", "java");
    protected final static TestInputFile Santa03
            = new TestInputFile("code/test/", "Santa03",
                        "pj", "java");
    protected final static TestInputFile SharedChannel01
            = new TestInputFile("code/test/", "SharedChannel01",
                        "pj", "java");
    protected final static TestInputFile SharedChannelRead01
            = new TestInputFile("code/test/", "SharedChannelRead01",
                        "pj", "java");
    protected final static TestInputFile SharedChannelWrite01
            = new TestInputFile("code/test/", "SharedChannelWrite01",
                        "pj", "java");
    protected final static TestInputFile Silly
            = new TestInputFile("code/test/", "Silly",
                        "pj", "java");
    protected final static TestInputFile SortPump
            = new TestInputFile("code/test/", "SortPump",
                        "pj", "java");
    protected final static TestInputFile Switch01
            = new TestInputFile("code/test/", "Switch01",
                        "pj", "java");
    protected final static TestInputFile Timer01
            = new TestInputFile("code/test/", "Timer01",
                        "pj", "java");

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

        return compiler;

    }

    private static URI uriOf(final TestInputFile testInputFile) {

        return URI.create("string:///" + testInputFile.getAbsoluteOutputFilePath());

    }

    private static SimpleJavaFileObject SimpleJavaFileObjectOf(final TestInputFile testInputFile) {

        return new SimpleJavaFileObject(uriOf(testInputFile), JavaFileObject.Kind.SOURCE) {

            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {

                return TestInputFile.stringOf(testInputFile.getAbsoluteOutputFilePath());

            }

        };

    }

    /// ------------------------
    /// Protected Static Methods

    protected static boolean CompileJava(final TestInputFile testInputFile) {

        // Initialize the Diagnostics Collector
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        // Invoke & return the result
        return getJavaCompiler().getTask(
                null, null, diagnostics,
                null, null, List.of(SimpleJavaFileObjectOf(testInputFile))).call();

    }

    protected static Lexer LexerFor(final TestInputFile testInputFile) {

        // Initialize a handle to the result
        Lexer lexer = null  ;

        // Attempt to
        try {

            // Instantiate a FileInputStream with the specified path
            final FileReader fileReader = new FileReader(testInputFile.getAbsoluteInputFilePath());

            // Instantiate a lexer
            lexer = new Lexer(fileReader);

            // Otherwise
        } catch(final IOException ioException) {

            // Print the error
            System.out.println(ioException.getMessage());

        }

        // Check
        Assertions.assertNotNull(lexer);

        // Return a result
        return lexer;

    }

}
