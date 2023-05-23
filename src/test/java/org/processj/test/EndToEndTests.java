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



}
