package org.processj.test;

import org.junit.jupiter.api.Test;
import org.processj.ProcessJc;

public class EndToEndTests extends ProcessJTest {

    @Test
    public void bookCodeChapter1Section2_synchronizedCommunication_endToEnd() {

        final String[] arguments = {CODE_BOOK_1_2_SYNCHRONIZED_COMMUNICATION};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section5_timeInProcessJ_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_5_TIME_IN_PROCESS_J};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section6_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_6_ONE_TO_ONE_CHANNELS};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section7_fibonacci_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_7_FIBONACCI};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section7_fifo6_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_7_FIFO6};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section7_integrate_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_7_INTEGRATE};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section7_numbers_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_7_NUMBERS};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section7_pairs_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_7_PAIRS};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section7_squares_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_7_SQUARES};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section7_structuredExecution_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_7_STRUCTURED_EXECUTION};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section8_oneBitAdder_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_8_ONE_BIT_ADDER};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section8_fourBitAdder_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_8_FOUR_BIT_ADDER};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section8_eightBitAdder_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_8_EIGHT_BIT_ADDER};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section9_output_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_9_OUTPUT};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter4Section1_switch_endToEnd() {

        final String[] arguments = {CODE_BOOK_4_1_SWITCH};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter4Section2_for_endToEnd() {

        final String[] arguments = {CODE_BOOK_4_2_FOR};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter4Section2_while_endToEnd() {

        final String[] arguments = {CODE_BOOK_4_2_WHILE};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter4Section3_repetition_endToEnd() {

        final String[] arguments = {CODE_BOOK_4_3_REPETITION};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter4Section4_alt_endToEnd() {

        final String[] arguments = {CODE_BOOK_4_4_ALT};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter4Section4_mux_endToEnd() {

        final String[] arguments = {CODE_BOOK_4_4_MUX};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter4Section4_timeoutGuard_endToEnd() {

        final String[] arguments = {CODE_BOOK_4_4_TIMEOUT_GUARD};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter4Section4_watchdog_endToEnd() {

        final String[] arguments = {CODE_BOOK_4_4_WATCHDOG};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter4Section5_priAlt_endToEnd() {

        final String[] arguments = {CODE_BOOK_4_5_PRI_ALT};

        ProcessJc.main(arguments);

    }

    /// ---------
    /// Test Code

    @Test
    public void testCode_altTest01_endToEnd() {

        final String[] arguments = {CODE_TEST_ALT_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_arrayTest01_endToEnd() {

        final String[] arguments = {CODE_TEST_ARRAY_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_arrayTest02_endToEnd() {

        final String[] arguments = {CODE_TEST_ARRAY_02};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_barrier01_endToEnd() {

        final String[] arguments = {CODE_TEST_BARRIER_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_barrier02_endToEnd() {

        final String[] arguments = {CODE_TEST_BARRIER_02};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_binaryExpression01_endToEnd() {

        final String[] arguments = {CODE_TEST_BINARY_EXPRESSION_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_byteCode01_endToEnd() {

        final String[] arguments = {CODE_TEST_BYTECODE_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_channelArray01_endToEnd() {

        final String[] arguments = {CODE_TEST_CHANNEL_ARRAY_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_channelArray02_endToEnd() {

        final String[] arguments = {CODE_TEST_CHANNEL_ARRAY_02};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_channelRead01_endToEnd() {

        final String[] arguments = {CODE_TEST_CHANNEL_READ_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_channelRead02_endToEnd() {

        final String[] arguments = {CODE_TEST_CHANNEL_READ_02};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_channelWrite01_endToEnd() {

        final String[] arguments = {CODE_TEST_CHANNEL_WRITE_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_channelWrite02_endToEnd() {

        final String[] arguments = {CODE_TEST_CHANNEL_WRITE_02};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_channelWrite03_endToEnd() {

        final String[] arguments = {CODE_TEST_CHANNEL_WRITE_03};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_channelWrite04_endToEnd() {

        final String[] arguments = {CODE_TEST_CHANNEL_WRITE_04};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_enroll01_endToEnd() {

        final String[] arguments = {CODE_TEST_ENROLL_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_fibonacci_endToEnd() {

        final String[] arguments = {CODE_TEST_FIBONACCI};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_for01_endToEnd() {

        final String[] arguments = {CODE_TEST_FOR_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_fullAdder_endToEnd() {

        final String[] arguments = {CODE_TEST_FULL_ADDER};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_hello_endToEnd() {

        final String[] arguments = {CODE_TEST_HELLO};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_if01_endToEnd() {

        final String[] arguments = {CODE_TEST_IF_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_integrate_endToEnd() {

        final String[] arguments = {CODE_TEST_INTEGRATE};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_localDeclaration01_endToEnd() {

        final String[] arguments = {CODE_TEST_LOCAL_DECLARATION_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_mandelbrot01_endToEnd() {

        final String[] arguments = {CODE_TEST_MANDELBROT_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_mandelbrot02_endToEnd() {

        final String[] arguments = {CODE_TEST_MANDELBROT_02};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_mandelbrot03_endToEnd() {

        final String[] arguments = {CODE_TEST_MANDELBROT_03};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_mandelbrot04_endToEnd() {

        final String[] arguments = {CODE_TEST_MANDELBROT_04};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_par01_endToEnd() {

        final String[] arguments = {CODE_TEST_PAR_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_par02_endToEnd() {

        final String[] arguments = {CODE_TEST_PAR_02};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_par03_endToEnd() {

        final String[] arguments = {CODE_TEST_PAR_03};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_priAlt01_endToEnd() {

        final String[] arguments = {CODE_TEST_PRI_ALT_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_protocol01_endToEnd() {

        final String[] arguments = {CODE_TEST_PROTOCOL_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_protocol02_endToEnd() {

        final String[] arguments = {CODE_TEST_PROTOCOL_02};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_record01_endToEnd() {

        final String[] arguments = {CODE_TEST_RECORD_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_record02_endToEnd() {

        final String[] arguments = {CODE_TEST_RECORD_02};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_record03_endToEnd() {

        final String[] arguments = {CODE_TEST_RECORD_03};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_record04_endToEnd() {

        final String[] arguments = {CODE_TEST_RECORD_04};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_santa01_endToEnd() {

        final String[] arguments = {CODE_TEST_SANTA_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_santa02_endToEnd() {

        final String[] arguments = {CODE_TEST_SANTA_02};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_santa03_endToEnd() {

        final String[] arguments = {CODE_TEST_SANTA_03};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_sharedChannel01_endToEnd() {

        final String[] arguments = {CODE_TEST_SHARED_CHANNEL_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_sharedChannelRead01_endToEnd() {

        final String[] arguments = {CODE_TEST_SHARED_CHANNEL_READ_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_sharedChannelWrite01_endToEnd() {

        final String[] arguments = {CODE_TEST_SHARED_CHANNEL_WRITE_01};

        ProcessJc.main(arguments);

    }

    @Test
    public void testCode_silly_endToEnd() {

        final String[] arguments = {CODE_TEST_SILLY};

        ProcessJc.main(arguments);

    }

}
