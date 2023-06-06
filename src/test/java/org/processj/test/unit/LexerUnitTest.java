package org.processj.test.unit;

import java_cup.runtime.Symbol;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.processj.compiler.phases.phase.Lexer;
import org.processj.compiler.phases.phase.sym;
import org.processj.test.ProcessJTest;
import org.processj.test.TestInputFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Collection of tests that check the ProcessJ lexer against the current collection of test inputs.</p>
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @see ProcessJTest
 * @since 0.1.0
 */
public class LexerUnitTest extends ProcessJTest {

    /// -----------------------
    /// Public Static Constants

    public final static boolean TestRegression                  = true  ;
    public final static boolean OverwriteRegressionWithStaged   = false ;

    /// ------------------------
    /// Private Static Constants

    private final static String RegressionStage = "src/test/resources/inputs/regression/lexer_unit_tests/staged/";
    private final static String RegressionPath  = "src/test/resources/inputs/regression/lexer_unit_tests/check/";

    /// ----------------------
    /// Private Static Methods

    /**
     * <p>Attempts to initialize a {@link FileWriter} that corresponds to the {@link TestInputFile}'s output file path.
     * @param testInputFile Instance with a corresponding output file path to instantiate the {@link FileWriter} with.
     * @return {@link FileWriter} instance corresponding to the {@link TestInputFile}'s output file path.</p>
     * @see TestInputFile
     * @see FileWriter
     * @since 0.1.0
     */
    private static FileWriter FileWriterFor(final TestInputFile testInputFile) {

        // Initialize a handle to the file writer
        FileWriter fileWriter = null;

        // Attempt to
        try {

            // Create any missing intermediate directories
            new File(testInputFile.getAbsoluteOutputPath()).mkdirs();

            // Instantiate a FileWriter
            fileWriter = new FileWriter((new File(testInputFile.getAbsoluteOutputFilePath())).getAbsolutePath());

        // Otherwise
        } catch(final IOException ioException) {

            // Print the error
            System.out.println(ioException.getMessage());

        }

        // Check
        Assertions.assertNotNull(fileWriter);

        // Return the result
        return fileWriter;

    }

    /**
     * <p>Returns the {@link TestInputFile}'s stream of tokens as a {@link List} of {@link Symbol}s as produced by the
     * {@link Lexer}. This method will also stage a regression test input in the path specified
     * by {@link LexerUnitTest#RegressionStage}. This method will fail an assertion if a
     * {@link Lexer} with the specified {@link TestInputFile} or the {@link FileWriter} corresponding
     * to the output text file cannot be instantiated.</p>
     * @param testInputFile Instance representing a ProcessJ source file.
     * @return The {@link TestInputFile}'s corresponding token stream as a {@link List} of {@link Symbol}s.
     * @see TestInputFile
     * @see Lexer
     * @see Symbol
     * @since 0.1.0
     */
    private static List<Symbol> StreamOf(final TestInputFile testInputFile) {

        // Update the base input path to that of the source files & the base output path to that of the staged
        // regression input
        TestInputFile.BaseInputPath   = InputDirectory;
        TestInputFile.BaseOutputPath  = RegressionStage;

        // Initialize the result & retrieve a lexer with the specified source &
        // a FileWriter for the corresponding text file
        final List<Symbol> symbols      = new ArrayList<>();
        final Lexer        lexer        = LexerFor(testInputFile);
        final FileWriter   fileWriter   = FileWriterFor(new TestInputFile(testInputFile.getRelativePath(),
                testInputFile.getName(), "txt", "txt"));

        // Attempt to
        try {

            do {

                // Retrieve the next token
                final Symbol symbol = lexer.next_token();

                // Write the line
                fileWriter.write(symbol.sym + ":" + symbol + '\n');

                // Add the symbol to the result
                symbols.add(symbol);

            // While we haven't reached the end of file
            } while(symbols.get(symbols.size() - 1).sym != sym.EOF);

            // Close the FileWriter
            fileWriter.close();

        // Otherwise
        } catch(final IOException ioException) {

            // Print the error
            System.out.println(ioException.getMessage());

        }

        // Return the result
        return symbols;

    }

    /**
     * <p>Performs a regression test on the staged text file produced from the specified {@link TestInputFile}
     * against the existing regression test if {@link LexerUnitTest#TestRegression} is true.
     * This will fail the assertion if the existing regression test exists (non-blank) and the contents do not match.
     * Finally, if {@link LexerUnitTest#OverwriteRegressionWithStaged} is true, then this will overwrite the current
     * regression test with the staged regression test.</p>
     * @param testInputFile Instance representing a ProcessJ source file to test its' Token stream against a corresponding
     *                    regression input.
     * @see TestInputFile
     * @since 0.1.0
     */
    private static void TestRegression(final TestInputFile testInputFile) {

        // Update the base input path to the regression input
        TestInputFile.BaseInputPath = RegressionPath;

        // Instantiate a text representation
        final TestInputFile test = new TestInputFile(testInputFile.getRelativePath(), testInputFile.getName(),
                "txt", "txt");

        // Update the base input & output paths to the regression stage & input respectively
        TestInputFile.BaseInputPath  = RegressionStage;
        TestInputFile.BaseOutputPath = RegressionPath ;

        // Instantiate a text representation of the staged input
        final TestInputFile staged = new TestInputFile(testInputFile.getRelativePath(), testInputFile.getName(),
                "txt", "txt");

        // Assert. If the test is blank, we pass; otherwise we return the result of the
        // equality of the staged against the test.
        if(TestRegression) Assertions.assertTrue(test.toString().isBlank() || test.equals(staged));

        // Overwrite if applicable
        if(OverwriteRegressionWithStaged) staged.write();

    }

    /**
     * <p>Generic {@link Lexer} unit test procedure. This executes a test that satisfies all
     * Test Oracles defined in the specification.</p>
     * @param testInputFile Instance representing a ProcessJ source file to unit test against the
     *                    {@link Lexer}
     * @see Lexer
     * @see TestInputFile
     * @since 0.1.0
     */
    private static void UnitTestOf(final TestInputFile testInputFile) {

        // Retrieve the stream & output the staged regression test input
        final List<Symbol> tokenStream = StreamOf(testInputFile);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization; the token stream should always contain an end-of-file at the end.
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

        // Test Regression
        TestRegression(testInputFile);

    }

    /// ---------------
    /// Book Code Tests

    @Test
    public void testCode_chapter1Section2SynchronizedCommunication_lexerUnitTest() {

        UnitTestOf(BookSynchronizedCommunication);

    }

    @Test
    public void testCode_chapter3Section5TimeInProcessJ_lexerUnitTest() {

        UnitTestOf(BookTimeInProcessJ);

    }

    @Test
    public void testCode_chapter3Section6OneToOneChannels_lexerUnitTest() {

        UnitTestOf(BookOneToOneChannels);

    }

    @Test
    public void testCode_chapter3Section7Fibonacci_lexerUnitTest() {

        UnitTestOf(BookFibonacci);

    }

    @Test
    public void testCode_chapter3Section7Fifo6_lexerUnitTest() {

        UnitTestOf(BookFifo6);

    }

    @Test
    public void testCode_chapter3Section7Integrate_lexerUnitTest() {

        UnitTestOf(BookIntegrate);

    }

    @Test
    public void testCode_chapter3Section7Numbers_lexerUnitTest() {

        UnitTestOf(BookNumbers);

    }

    @Test
    public void testCode_chapter3Section7Pairs_lexerUnitTest() {

        UnitTestOf(BookPairs);

    }

    @Test
    public void testCode_chapter3Section7Squares_lexerUnitTest() {

        UnitTestOf(BookSquares);

    }

    @Test
    public void testCode_chapter3Section7StructuredExecution_lexerUnitTest() {

        UnitTestOf(BookStructuredExecution);

    }

    @Test
    public void testCode_chapter3Section8OneBitAdder_lexerUnitTest() {

        UnitTestOf(BookOneBitAdder);

    }

    @Test
    public void testCode_chapter3Section8FourBitAdder_lexerUnitTest() {

        UnitTestOf(BookFourBitAdder);

    }

    @Test
    public void testCode_chapter3Section8EightBitAdder_lexerUnitTest() {

        UnitTestOf(BookEightBitAdder);

    }

    @Test
    public void testCode_chapter3Section9Output_lexerUnitTest() {

        UnitTestOf(BookOutput);

    }

    @Test
    public void testCode_chapter4Section1Switch_lexerUnitTest() {

        UnitTestOf(BookSwitch);

    }

    @Test
    public void testCode_chapter4Section2For_lexerUnitTest() {

        UnitTestOf(BookFor);

    }

    @Test
    public void testCode_chapter4Section2While_lexerUnitTest() {

        UnitTestOf(BookWhile);

    }

    @Test
    public void testCode_chapter4Section3Repetition_lexerUnitTest() {

        UnitTestOf(BookRepetition);

    }

    @Test
    public void testCode_chapter4Section4Alt_lexerUnitTest() {

        UnitTestOf(BookAlt);

    }

    @Test
    public void testCode_chapter4Section4Mux_lexerUnitTest() {

        UnitTestOf(BookMux);

    }

    @Test
    public void testCode_chapter4Section4TimeoutGuard_lexerUnitTest() {

        UnitTestOf(BookTimeoutGuard);

    }

    @Test
    public void testCode_chapter4Section4Watchdog_lexerUnitTest() {

        UnitTestOf(BookWatchdog);

    }

    @Test
    public void testCode_chapter4Section5PriAlt_lexerUnitTest() {

        UnitTestOf(BookPriAlt);

    }

    /// ---------
    /// Test Code

    @Test
    public void testCode_alt01_lexerUnitTest() {

        UnitTestOf(Alt01);

    }

    @Test
    public void testCode_array01_lexerUnitTest() {

        UnitTestOf(Array01);

    }

    @Test
    public void testCode_array02_lexerUnitTest() {

        UnitTestOf(Array02);

    }

    @Test
    public void testCode_barrier01_lexerUnitTest() {

        UnitTestOf(Barrier01);

    }

    @Test
    public void testCode_barrier02_lexerUnitTest() {

        UnitTestOf(Barrier02);

    }

    @Test
    public void testCode_binaryExpression01_lexerUnitTest() {

        UnitTestOf(BinaryExpression01);

    }

    @Test
    public void testCode_binaryExpression02_lexerUnitTest() {

        UnitTestOf(BinaryExpression02);

    }

    @Test
    public void testCode_byteCode01_lexerUnitTest() {

        UnitTestOf(ByteCode01);

    }

    @Test
    public void testCode_channelArray01_lexerUnitTest() {

        UnitTestOf(ChannelArray01);

    }

    @Test
    public void testCode_channelArray02_lexerUnitTest() {

        UnitTestOf(ChannelArray02);

    }

    @Test
    public void testCode_channelEndArray01_lexerUnitTest() {

        UnitTestOf(ChannelEndArray01);

    }

    @Test
    public void testCode_channelEndArray02_lexerUnitTest() {

        UnitTestOf(ChannelEndArray02);

    }

    @Test
    public void testCode_channelRead01_lexerUnitTest() {

        UnitTestOf(ChannelRead01);

    }

    @Test
    public void testCode_channelRead02_lexerUnitTest() {

        UnitTestOf(ChannelRead02);

    }

    @Test
    public void testCode_channelWrite01_lexerUnitTest() {

        UnitTestOf(ChannelWrite01);

    }

    @Test
    public void testCode_channelWrite02_lexerUnitTest() {

        UnitTestOf(ChannelWrite02);

    }

    @Test
    public void testCode_channelWrite03_lexerUnitTest() {

        UnitTestOf(ChannelWrite03);

    }

    @Test
    public void testCode_channelWrite04_lexerUnitTest() {

        UnitTestOf(ChannelWrite04);

    }

    @Test
    public void testCode_enroll01_lexerUnitTest() {

        UnitTestOf(Enroll01);

    }

    @Test
    public void testCode_fibonacci_lexerUnitTest() {

        UnitTestOf(Fibonacci);

    }

    @Test
    public void testCode_for01_lexerUnitTest() {

        UnitTestOf(For01);

    }

    @Test
    public void testCode_fullAdder_lexerUnitTest() {

        UnitTestOf(FullAdder);

    }

    @Test
    public void testCode_hello_lexerUnitTest() {

        UnitTestOf(Hello);

    }

    @Test
    public void testCode_if01_lexerUnitTest() {

        UnitTestOf(If01);

    }

    @Test
    public void testCode_integrate_lexerUnitTest() {

        UnitTestOf(Integrate);

    }

    @Test
    public void testCode_localDeclaration01_lexerUnitTest() {

        UnitTestOf(LocalDeclaration01);

    }

    @Test
    public void testCode_mandelbrot01_lexerUnitTest() {

        UnitTestOf(Mandelbrot01);

    }

    @Test
    public void testCode_mandelbrot02_lexerUnitTest() {

        UnitTestOf(Mandelbrot02);

    }

    @Test
    public void testCode_mandelbrot03_lexerUnitTest() {

        UnitTestOf(Mandelbrot03);

    }

    @Test
    public void testCode_mandelbrot04_lexerUnitTest() {

        UnitTestOf(Mandelbrot04);

    }

    @Test
    public void testCode_par01_lexerUnitTest() {

        UnitTestOf(Par01);

    }

    @Test
    public void testCode_par02_lexerUnitTest() {

        UnitTestOf(Par02);

    }

    @Test
    public void testCode_par03_lexerUnitTest() {

        UnitTestOf(Par03);

    }

    @Test
    public void testCode_pragma01_lexerUnitTest() {

        UnitTestOf(Pragma01);

    }

    @Test
    public void testCode_priAlt01_lexerUnitTest() {

        UnitTestOf(PriAlt01);

    }

    @Test
    public void testCode_protocol01_lexerUnitTest() {

        UnitTestOf(Protocol01);

    }

    @Test
    public void testCode_protocol02_lexerUnitTest() {

        UnitTestOf(Protocol02);

    }

    @Test
    public void testCode_protocol03_lexerUnitTest() {

        UnitTestOf(Protocol03);

    }

    @Test
    public void testCode_record01_lexerUnitTest() {

        UnitTestOf(Record01);

    }

    @Test
    public void testCode_record02_lexerUnitTest() {

        UnitTestOf(Record02);

    }

    @Test
    public void testCode_record03_lexerUnitTest() {

        UnitTestOf(Record03);

    }

    @Test
    public void testCode_record04_lexerUnitTest() {

        UnitTestOf(Record04);

    }

    @Test
    public void testCode_record05_lexerUnitTest() {

        UnitTestOf(Record05);

    }

    @Test
    public void testCode_santa01_lexerUnitTest() {

        UnitTestOf(Santa01);

    }

    @Test
    public void testCode_santa02_lexerUnitTest() {

        UnitTestOf(Santa02);

    }

    @Test
    public void testCode_santa03_lexerUnitTest() {

        UnitTestOf(Santa03);

    }

    @Test
    public void testCode_sharedChannel01_lexerUnitTest() {

        UnitTestOf(SharedChannel01);

    }

    @Test
    public void testCode_sharedChannelRead01_lexerUnitTest() {

        UnitTestOf(SharedChannelRead01);

    }

    @Test
    public void testCode_sharedChannelWrite01_lexerUnitTest() {

        UnitTestOf(SharedChannelWrite01);

    }

    @Test
    public void testCode_silly_lexerUnitTest() {

        UnitTestOf(Silly);

    }

    @Test
    public void testCode_sortPump_lexerUnitTest() {

        UnitTestOf(SortPump);

    }

    @Test
    public void testCode_switch01_lexerUnitTest() {

        UnitTestOf(Switch01);

    }

    @Test
    public void testCode_timer01_lexerUnitTest() {

        UnitTestOf(Timer01);

    }

}
