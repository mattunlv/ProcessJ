package org.processj.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.processj.Phase;
import org.processj.ProcessJc;

import java.net.MalformedURLException;

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

    /// ----------------------
    /// Private Static Methods

    /**
     * <p>Generic end-to-end test procedure. This executes a test that satisfies all Test Oracles defined in the
     * specification.</p>
     * @param testInputFile Instance representing a ProcessJ source file to test against the toolchain.
     * @see org.processj.ProcessJc
     * @see TestInputFile
     * @since 0.1.0
     */
    private static void UnitTestOf(final TestInputFile testInputFile)  {

        // Update the input & output paths
        TestInputFile.BaseInputPath   = InputDirectory    ;
        TestInputFile.BaseOutputPath  = WorkingDirectory  ;

        try {

            // Transpile with ProcessJ
            ProcessJc.main(new String[]{testInputFile.getAbsoluteInputFilePath()});

        } catch(final Phase.Error | MalformedURLException | ClassNotFoundException error) {

            Assertions.assertTrue(false);

        }

        // Check
        Assertions.assertTrue(CompileJava(testInputFile));

    }

    /**
     * <p>Generic end-to-end test procedure. This executes a test that satisfies all Test Oracles defined in the
     * specification.</p>
     * @param testInputFile Instance representing a ProcessJ source file to test against the toolchain.
     * @see org.processj.ProcessJc
     * @see TestInputFile
     * @since 0.1.0
     */
    @SuppressWarnings({"deprecation", "unused"})
    private static void FailUnitTestOf(final TestInputFile testInputFile) throws Phase.Error {

        // Update the input & output paths
        TestInputFile.BaseInputPath   = InputDirectory    ;
        TestInputFile.BaseOutputPath  = WorkingDirectory  ;

    }

    /// ---------------
    /// Book Code Tests

    @Test
    public void testCode_chapter1Section2SynchronizedCommunication_endToEnd() {

        UnitTestOf(BookSynchronizedCommunication);

    }

    @Test
    public void testCode_chapter3Section5TimeInProcessJ_endToEnd() {

        UnitTestOf(BookTimeInProcessJ);

    }

    @Test
    public void testCode_chapter3Section6OneToOneChannels_endToEnd() {

        UnitTestOf(BookOneToOneChannels);

    }

    @Test
    public void testCode_chapter3Section7Fibonacci_endToEnd() {

        UnitTestOf(BookFibonacci);

    }

    @Test
    public void testCode_chapter3Section7Fifo6_endToEnd() {

        UnitTestOf(BookFifo6);

    }

    @Test
    public void testCode_chapter3Section7Integrate_endToEnd() {

        UnitTestOf(BookIntegrate);

    }

    @Test
    public void testCode_chapter3Section7Numbers_endToEnd() {

        UnitTestOf(BookNumbers);

    }

    @Test
    public void testCode_chapter3Section7Pairs_endToEnd() {

        UnitTestOf(BookPairs);

    }

    @Test
    public void testCode_chapter3Section7Squares_endToEnd() {

        UnitTestOf(BookSquares);

    }

    @Test
    public void testCode_chapter3Section7StructuredExecution_endToEnd() {

        UnitTestOf(BookStructuredExecution);


    }

    @Test
    public void testCode_chapter3Section8OneBitAdder_endToEnd() {

        UnitTestOf(BookOneBitAdder);


    }

    @Test
    public void testCode_chapter3Section8FourBitAdder_endToEnd() {

        UnitTestOf(BookFourBitAdder);

    }

    @Test
    public void testCode_chapter3Section8EightBitAdder_endToEnd() {

        UnitTestOf(BookEightBitAdder);

    }

    @Test
    public void testCode_chapter3Section9Output_endToEnd() {

        UnitTestOf(BookOutput);

    }

    @Test
    public void testCode_chapter4Section1Switch_endToEnd() {

        UnitTestOf(BookSwitch);

    }

    @Test
    public void testCode_chapter4Section2For_endToEnd() {

        UnitTestOf(BookFor);

    }

    @Test
    public void testCode_chapter4Section2While_endToEnd() {

        UnitTestOf(BookWhile);

    }

    @Test
    public void testCode_chapter4Section3Repetition_endToEnd() {

        UnitTestOf(BookRepetition);

    }

    @Test
    public void testCode_chapter4Section4Alt_endToEnd() {

        UnitTestOf(BookAlt);

    }

    @Test
    public void testCode_chapter4Section4Mux_endToEnd() {

        UnitTestOf(BookMux);

    }

    @Test
    public void testCode_chapter4Section4TimeoutGuard_endToEnd() {

        UnitTestOf(BookTimeoutGuard);

    }

    @Test
    public void testCode_chapter4Section4Watchdog_endToEnd() {

        UnitTestOf(BookWatchdog);

    }

    @Test
    public void testCode_chapter4Section5PriAlt_endToEnd() {

        UnitTestOf(BookPriAlt);

    }

    /// ---------
    /// Test Code

    @Test
    public void testCode_alt01_endToEnd() {

        UnitTestOf(Alt01);

    }

    @Test
    public void testCode_array01_endToEnd() {

        UnitTestOf(Array01);

    }

    @Test
    public void testCode_array02_endToEnd() {

        UnitTestOf(Array02);

    }

    @Test
    public void testCode_barrier01_endToEnd() {

        UnitTestOf(Barrier01);

    }

    @Test
    public void testCode_barrier02_endToEnd() {

        UnitTestOf(Barrier02);

    }

    @Test
    public void testCode_binaryExpression01_endToEnd() {

        UnitTestOf(BinaryExpression01);

    }

    @Test
    public void testCode_binaryExpression02_endToEnd() {

        UnitTestOf(BinaryExpression02);

    }

    @Test
    public void testCode_byteCode01_endToEnd() {

        UnitTestOf(ByteCode01);

    }

    @Test
    public void testCode_channelArray01_endToEnd() {

        UnitTestOf(ChannelArray01);

    }

    @Test
    public void testCode_channelArray02_endToEnd() {

        UnitTestOf(ChannelArray02);

    }

    @Test
    public void testCode_channelEndArray01_endToEnd() {

        UnitTestOf(ChannelEndArray01);

    }

    @Test
    public void testCode_channelEndArray02_endToEnd() {

        UnitTestOf(ChannelEndArray02);

    }

    @Test
    public void testCode_channelRead01_endToEnd() {

        UnitTestOf(ChannelRead01);

    }

    @Test
    public void testCode_channelRead02_endToEnd() {

        UnitTestOf(ChannelRead02);

    }

    @Test
    public void testCode_channelWrite01_endToEnd() {

        UnitTestOf(ChannelWrite01);

    }

    @Test
    public void testCode_channelWrite02_endToEnd() {

        UnitTestOf(ChannelWrite02);

    }

    @Test
    public void testCode_channelWrite03_endToEnd() {

        UnitTestOf(ChannelWrite03);

    }

    @Test
    public void testCode_channelWrite04_endToEnd() {

        UnitTestOf(ChannelWrite04);

    }

    @Test
    public void testCode_empty_endToEnd() {

        UnitTestOf(Empty);

    }

    @Test
    public void testCode_enroll01_endToEnd() {

        UnitTestOf(Enroll01);

    }

    @Test
    public void testCode_fibonacci_endToEnd() {

        UnitTestOf(Fibonacci);

    }

    @Test
    public void testCode_for01_endToEnd() {

        UnitTestOf(For01);

    }

    @Test
    public void testCode_fullAdder_endToEnd() {

        UnitTestOf(FullAdder);

    }

    @Test
    public void testCode_hello_endToEnd() {

        UnitTestOf(Hello);

    }

    @Test
    public void testCode_if01_endToEnd() {

        UnitTestOf(If01);

    }

    @Test
    public void testCode_integrate_endToEnd() {

        UnitTestOf(Integrate);

    }

    @Test
    public void testCode_localDeclaration01_endToEnd() {

        UnitTestOf(LocalDeclaration01);

    }

    @Test
    public void testCode_mandelbrot01_endToEnd() {

        UnitTestOf(Mandelbrot01);

    }

    @Test
    public void testCode_mandelbrot02_endToEnd() {

        UnitTestOf(Mandelbrot02);

    }

    @Test
    public void testCode_mandelbrot03_endToEnd() {

        UnitTestOf(Mandelbrot03);

    }

    @Test
    public void testCode_mandelbrot04_endToEnd() {

        UnitTestOf(Mandelbrot04);

    }

    @Test
    public void testCode_par01_endToEnd() {

        UnitTestOf(Par01);

    }

    @Test
    public void testCode_par02_endToEnd() {

        UnitTestOf(Par02);

    }

    @Test
    public void testCode_par03_endToEnd() {

        UnitTestOf(Par03);

    }

    @Test
    public void testCode_pragma01_endToEnd() {

        UnitTestOf(Pragma01);

    }

    @Test
    public void testCode_priAlt01_endToEnd() {

        UnitTestOf(PriAlt01);

    }

    @Test
    public void testCode_protocol01_endToEnd() {

        UnitTestOf(Protocol01);

    }

    @Test
    public void testCode_protocol02_endToEnd() {

        UnitTestOf(Protocol02);

    }

    @Test
    public void testCode_protocol03_endToEnd() {

        UnitTestOf(Protocol03);

    }

    @Test
    public void testCode_record01_endToEnd() {

        UnitTestOf(Record01);

    }

    @Test
    public void testCode_record02_endToEnd() {

        UnitTestOf(Record02);


    }

    @Test
    public void testCode_record03_endToEnd() {

        UnitTestOf(Record03);

    }

    @Test
    public void testCode_record04_endToEnd() {

        UnitTestOf(Record04);

    }

    @Test
    public void testCode_record05_endToEnd() {

        UnitTestOf(Record05);

    }

    @Test
    public void testCode_santa01_endToEnd() {

        UnitTestOf(Santa01);

    }

    @Test
    public void testCode_santa02_endToEnd() {

        UnitTestOf(Santa02);

    }

    @Test
    public void testCode_santa03_endToEnd() {

        UnitTestOf(Santa03);

    }

    @Test
    public void testCode_sharedChannel01_endToEnd() {

        UnitTestOf(SharedChannel01);

    }

    @Test
    public void testCode_sharedChannelRead01_endToEnd() {

        UnitTestOf(SharedChannelRead01);

    }

    @Test
    public void testCode_sharedChannelWrite01_endToEnd() {

        UnitTestOf(SharedChannelWrite01);

    }

    @Test
    public void testCode_silly_endToEnd() {

        UnitTestOf(Silly);

    }

    @Test
    public void testCode_sortPump_endToEnd() {

        UnitTestOf(SortPump);

    }

    @Test
    public void testCode_switch01_endToEnd() {

        UnitTestOf(Switch01);

    }

    @Test
    public void testCode_timer01_endToEnd() {

        UnitTestOf(Timer01);

    }

    /// -------------
    /// Failure Tests

    @Test
    public void testCode_pragmaLibraryFail01_endToEnd() {

        //FailUnitTestOf(PragmaLibraryFail01);

    }

}
