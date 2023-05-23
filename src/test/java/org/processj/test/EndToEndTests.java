package org.processj.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * <p>Collection of tests that check ProcessJ transpilation and subsequent generated java output compilation.
 * Each of these tests check soundness against the Syntax & Java Syntax Test Oracles as outlined in the
 * specification.</p>
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @see ProcessJTest
 * @since 0.1.0
 */
public class EndToEndTests extends ProcessJTest {

    /// ---------------------
    /// Private Static Fields

    /**
     * <p>The directory within the local system where the ProcessJ compiler writes the generated Java output.</p>
     * @since 0.1.0
     */
    private final static String WorkingDirectory   = "/Users/cuenca/workingpj/";

    /**
     * <p>The directory within the local system of the target test ProcessJ source input to specify to the compiler.</p>
     * @since 0.1.0
     */
    private final static String InputDirectory     = "src/test/resources/code/";

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
    public void testCode_chapter1Section2SynchronizedCommunication_endToEnd() {

        Assertions.assertTrue(BookSynchronizedCommunication.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter3Section5TimeInProcessJ_endToEnd() {

        Assertions.assertTrue(BookTimeInProcessJ.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter3Section6OneToOneChannels_endToEnd() {

        Assertions.assertTrue(BookOneToOneChannels.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter3Section7Fibonacci_endToEnd() {

        Assertions.assertTrue(BookFibonacci.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter3Section7Fifo6_endToEnd() {

        Assertions.assertTrue(BookFifo6.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter3Section7Integrate_endToEnd() {

        Assertions.assertTrue(BookIntegrate.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter3Section7Numbers_endToEnd() {

        Assertions.assertTrue(BookNumbers.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter3Section7Pairs_endToEnd() {

        Assertions.assertTrue(BookPairs.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter3Section7Squares_endToEnd() {

        Assertions.assertTrue(BookSquares.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter3Section7StructuredExecution_endToEnd() {

        Assertions.assertTrue(BookStructuredExecution.successfulJavaCompile());


    }

    @Test
    public void testCode_chapter3Section8OneBitAdder_endToEnd() {

        Assertions.assertTrue(BookOneBitAdder.successfulJavaCompile());


    }

    @Test
    public void testCode_chapter3Section8FourBitAdder_endToEnd() {

        Assertions.assertTrue(BookFourBitAdder.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter3Section8EightBitAdder_endToEnd() {

        Assertions.assertTrue(BookEightBitAdder.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter3Section9Output_endToEnd() {

        Assertions.assertTrue(BookOutput.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter4Section1Switch_endToEnd() {

        Assertions.assertTrue(BookSwitch.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter4Section2For_endToEnd() {

        Assertions.assertTrue(BookFor.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter4Section2While_endToEnd() {

        Assertions.assertTrue(BookWhile.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter4Section3Repetition_endToEnd() {

        Assertions.assertTrue(BookRepetition.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter4Section4Alt_endToEnd() {

        Assertions.assertTrue(BookAlt.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter4Section4Mux_endToEnd() {

        Assertions.assertTrue(BookMux.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter4Section4TimeoutGuard_endToEnd() {

        Assertions.assertTrue(BookTimeoutGuard.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter4Section4Watchdog_endToEnd() {

        Assertions.assertTrue(BookWatchdog.successfulJavaCompile());

    }

    @Test
    public void testCode_chapter4Section5PriAlt_endToEnd() {

        Assertions.assertTrue(BookPriAlt.successfulJavaCompile());

    }

    /// ---------
    /// Test Code

    @Test
    public void testCode_alt01_endToEnd() {

        Assertions.assertTrue(Alt01.successfulJavaCompile());

    }

    @Test
    public void testCode_array01_endToEnd() {

        Assertions.assertTrue(Array01.successfulJavaCompile());

    }

    @Test
    public void testCode_array02_endToEnd() {

        Assertions.assertTrue(Array02.successfulJavaCompile());

    }

    @Test
    public void testCode_barrier01_endToEnd() {

        Assertions.assertTrue(Barrier01.successfulJavaCompile());

    }

    @Test
    public void testCode_barrier02_endToEnd() {

        Assertions.assertTrue(Barrier02.successfulJavaCompile());

    }

    @Test
    public void testCode_binaryExpression01_endToEnd() {

        Assertions.assertTrue(BinaryExpression01.successfulJavaCompile());

    }

    @Test
    public void testCode_binaryExpression02_endToEnd() {

        Assertions.assertTrue(BinaryExpression02.successfulJavaCompile());

    }

    @Test
    public void testCode_byteCode01_endToEnd() {

        Assertions.assertTrue(ByteCode01.successfulJavaCompile());

    }

    @Test
    public void testCode_channelArray01_endToEnd() {

        Assertions.assertTrue(ChannelArray01.successfulJavaCompile());

    }

    @Test
    public void testCode_channelArray02_endToEnd() {

        Assertions.assertTrue(ChannelArray02.successfulJavaCompile());

    }

    @Test
    public void testCode_channelEndArray01_endToEnd() {

        Assertions.assertTrue(ChannelEndArray01.successfulJavaCompile());

    }

    @Test
    public void testCode_channelEndArray02_endToEnd() {

        Assertions.assertTrue(ChannelEndArray02.successfulJavaCompile());

    }

    @Test
    public void testCode_channelRead01_endToEnd() {

        Assertions.assertTrue(ChannelRead01.successfulJavaCompile());

    }

    @Test
    public void testCode_channelRead02_endToEnd() {

        Assertions.assertTrue(ChannelRead02.successfulJavaCompile());

    }

    @Test
    public void testCode_channelWrite01_endToEnd() {

        Assertions.assertTrue(ChannelWrite01.successfulJavaCompile());

    }

    @Test
    public void testCode_channelWrite02_endToEnd() {

        Assertions.assertTrue(ChannelWrite02.successfulJavaCompile());

    }

    @Test
    public void testCode_channelWrite03_endToEnd() {

        Assertions.assertTrue(ChannelWrite03.successfulJavaCompile());

    }

    @Test
    public void testCode_channelWrite04_endToEnd() {

        Assertions.assertTrue(ChannelWrite04.successfulJavaCompile());

    }

    @Test
    public void testCode_enroll01_endToEnd() {

        Assertions.assertTrue(Enroll01.successfulJavaCompile());

    }

    @Test
    public void testCode_fibonacci_endToEnd() {

        Assertions.assertTrue(Fibonacci.successfulJavaCompile());

    }

    @Test
    public void testCode_for01_endToEnd() {

        Assertions.assertTrue(For01.successfulJavaCompile());

    }

    @Test
    public void testCode_fullAdder_endToEnd() {

        Assertions.assertTrue(FullAdder.successfulJavaCompile());

    }

    @Test
    public void testCode_hello_endToEnd() {

        Assertions.assertTrue(Hello.successfulJavaCompile());

    }

    @Test
    public void testCode_if01_endToEnd() {

        Assertions.assertTrue(If01.successfulJavaCompile());

    }

    @Test
    public void testCode_integrate_endToEnd() {

        Assertions.assertTrue(Integrate.successfulJavaCompile());

    }

    @Test
    public void testCode_localDeclaration01_endToEnd() {

        Assertions.assertTrue(LocalDeclaration01.successfulJavaCompile());

    }

    @Test
    public void testCode_mandelbrot01_endToEnd() {

        Assertions.assertTrue(Mandelbrot01.successfulJavaCompile());

    }

    @Test
    public void testCode_mandelbrot02_endToEnd() {

        Assertions.assertTrue(Mandelbrot02.successfulJavaCompile());

    }

    @Test
    public void testCode_mandelbrot03_endToEnd() {

        Assertions.assertTrue(Mandelbrot03.successfulJavaCompile());

    }

    @Test
    public void testCode_mandelbrot04_endToEnd() {

        Assertions.assertTrue(Mandelbrot04.successfulJavaCompile());

    }

    @Test
    public void testCode_par01_endToEnd() {

        Assertions.assertTrue(Par01.successfulJavaCompile());

    }

    @Test
    public void testCode_par02_endToEnd() {

        Assertions.assertTrue(Par02.successfulJavaCompile());

    }

    @Test
    public void testCode_par03_endToEnd() {

        Assertions.assertTrue(Par03.successfulJavaCompile());

    }

    @Test
    public void testCode_priAlt01_endToEnd() {

        Assertions.assertTrue(PriAlt01.successfulJavaCompile());

    }

    @Test
    public void testCode_protocol01_endToEnd() {

        Assertions.assertTrue(Protocol01.successfulJavaCompile());

    }

    @Test
    public void testCode_protocol02_endToEnd() {

        Assertions.assertTrue(Protocol02.successfulJavaCompile());

    }

    @Test
    public void testCode_protocol03_endToEnd() {

        Assertions.assertTrue(Protocol03.successfulJavaCompile());

    }

    @Test
    public void testCode_record01_endToEnd() {

        Assertions.assertTrue(Record01.successfulJavaCompile());

    }

    @Test
    public void testCode_record02_endToEnd() {

        Assertions.assertTrue(Record02.successfulJavaCompile());


    }

    @Test
    public void testCode_record03_endToEnd() {

        Assertions.assertTrue(Record03.successfulJavaCompile());

    }

    @Test
    public void testCode_record04_endToEnd() {

        Assertions.assertTrue(Record04.successfulJavaCompile());

    }

    @Test
    public void testCode_record05_endToEnd() {

        Assertions.assertTrue(Record05.successfulJavaCompile());

    }

    @Test
    public void testCode_santa01_endToEnd() {

        Assertions.assertTrue(Santa01.successfulJavaCompile());

    }

    @Test
    public void testCode_santa02_endToEnd() {

        Assertions.assertTrue(Santa02.successfulJavaCompile());

    }

    @Test
    public void testCode_santa03_endToEnd() {

        Assertions.assertTrue(Santa03.successfulJavaCompile());

    }

    @Test
    public void testCode_sharedChannel01_endToEnd() {

        Assertions.assertTrue(SharedChannel01.successfulJavaCompile());

    }

    @Test
    public void testCode_sharedChannelRead01_endToEnd() {

        Assertions.assertTrue(SharedChannelRead01.successfulJavaCompile());

    }

    @Test
    public void testCode_sharedChannelWrite01_endToEnd() {

        Assertions.assertTrue(SharedChannelWrite01.successfulJavaCompile());

    }

    @Test
    public void testCode_silly_endToEnd() {

        Assertions.assertTrue(Silly.successfulJavaCompile());

    }

    @Test
    public void testCode_sortPump_endToEnd() {

        Assertions.assertTrue(SortPump.successfulJavaCompile());

    }

    @Test
    public void testCode_switch01_endToEnd() {

        Assertions.assertTrue(Switch01.successfulJavaCompile());

    }

    @Test
    public void testCode_timer01_endToEnd() {

        Assertions.assertTrue(Timer01.successfulJavaCompile());

    }

}
