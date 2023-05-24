package org.processj.test.unit;

import java_cup.runtime.Symbol;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.processj.lexer.Lexer;
import org.processj.parser.sym;
import org.processj.test.ProcessJTest;
import org.processj.test.SourceInput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LexerUnitTest extends ProcessJTest {

    /// ----------------------
    /// Private Static Methods

    private static List<Symbol> StreamOf(final Lexer lexer) {

        // Initialize the result
        final List<Symbol> symbols = new ArrayList<>();

        // Keep a local to check
        do {

            // Attempt
            try {

                // To append the next token
                symbols.add(lexer.next_token());

            // Otherwise
            } catch(final IOException ioException) {

                // Print the error
                System.out.println(ioException.getMessage());

                // Leave
                break;

            }

        } while(symbols.get(symbols.size() - 1).sym != sym.EOF);

        // Return the result
        return symbols;

    }

    /// ------
    /// Before

    /**
     * <p>Initializes the local test environment. Updates the {@link SourceInput#InputPath} &
     * {@link SourceInput#OutputPath} static fields to specify the proper location of the ProcessJ test input source
     * files to the ProcessJ compiler in addition to specifying the proper location of the generated Java source files.
     * </p>
     * @since 0.1.0
     */
    @BeforeAll
    public static void initializeDirectories() {

        // Update the input & output paths
        SourceInput.InputPath   = InputDirectory    ;
        SourceInput.OutputPath  = WorkingDirectory  ;

    }

    /// ---------------
    /// Book Code Tests

    @Test
    public void testCode_chapter1Section2SynchronizedCommunication_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookSynchronizedCommunication.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section5TimeInProcessJ_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookTimeInProcessJ.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section6OneToOneChannels_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookOneToOneChannels.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section7Fibonacci_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookFibonacci.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section7Fifo6_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookFifo6.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section7Integrate_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookIntegrate.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section7Numbers_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookNumbers.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section7Pairs_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookPairs.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section7Squares_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookSquares.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section7StructuredExecution_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookStructuredExecution.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section8OneBitAdder_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookOneBitAdder.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section8FourBitAdder_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookFourBitAdder.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section8EightBitAdder_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookEightBitAdder.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter3Section9Output_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookOutput.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter4Section1Switch_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookSwitch.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter4Section2For_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookFor.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter4Section2While_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookWhile.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter4Section3Repetition_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookRepetition.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter4Section4Alt_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookAlt.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter4Section4Mux_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookMux.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter4Section4TimeoutGuard_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookTimeoutGuard.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter4Section4Watchdog_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookWatchdog.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_chapter4Section5PriAlt_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BookPriAlt.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    /// ---------
    /// Test Code

    @Test
    public void testCode_alt01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Alt01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_array01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Array01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_array02_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Array02.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_barrier01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Barrier01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_barrier02_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Barrier02.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_binaryExpression01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BinaryExpression01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_binaryExpression02_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(BinaryExpression02.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_byteCode01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(ByteCode01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_channelArray01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(ChannelArray01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_channelArray02_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(ChannelArray02.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_channelEndArray01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(ChannelEndArray01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_channelEndArray02_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(ChannelEndArray02.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_channelRead01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(ChannelRead01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_channelRead02_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(ChannelRead02.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_channelWrite01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(ChannelWrite01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_channelWrite02_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(ChannelWrite02.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_channelWrite03_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(ChannelWrite03.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_channelWrite04_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(ChannelWrite04.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_enroll01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Enroll01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_fibonacci_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Fibonacci.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_for01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(For01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_fullAdder_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(FullAdder.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_hello_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Hello.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_if01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(If01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_integrate_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Integrate.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_localDeclaration01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(LocalDeclaration01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_mandelbrot01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Mandelbrot01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_mandelbrot02_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Mandelbrot02.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_mandelbrot03_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Mandelbrot03.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_mandelbrot04_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Mandelbrot04.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_par01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Par01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_par02_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Par02.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_par03_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Par03.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_priAlt01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(PriAlt01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_protocol01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Protocol01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_protocol02_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Protocol02.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_protocol03_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Protocol03.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_record01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Record01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_record02_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Record02.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_record03_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Record03.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_record04_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Record04.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_record05_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Record05.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_santa01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Santa01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_santa02_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Santa02.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_santa03_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Santa03.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_sharedChannel01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(SharedChannel01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_sharedChannelRead01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(SharedChannelRead01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_sharedChannelWrite01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(SharedChannelWrite01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_silly_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Silly.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_sortPump_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(SortPump.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_switch01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Switch01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

    @Test
    public void testCode_timer01_lexerUnitTest() {

        // Retrieve a lexer with the specified file
        final Lexer lexer = lexerWith(Timer01.getInputPath());

        // Check
        Assertions.assertNotNull(lexer);

        // Retrieve the stream
        final List<Symbol> tokenStream = StreamOf(lexer);

        // Assert non-empty
        Assertions.assertFalse(tokenStream.isEmpty());

        // Assert Successful tokenization
        Assertions.assertEquals(tokenStream.get(tokenStream.size() - 1).sym, sym.EOF);

    }

}
