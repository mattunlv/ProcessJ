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

}
