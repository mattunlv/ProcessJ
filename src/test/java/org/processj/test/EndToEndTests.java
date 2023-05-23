package org.processj.test;

import org.junit.jupiter.api.Test;
import org.processj.ProcessJc;

public class EndToEndTests extends ProcessJTest {

    @Test
    public void bookCodeChapter1Section2_endToEnd() {

        final String[] arguments = {CODE_BOOK_1_2_SYNCHRONIZED_COMMUNICATION};

        ProcessJc.main(arguments);

    }

    @Test
    public void bookCodeChapter3Section5_endToEnd() {

        final String[] arguments = {CODE_BOOK_3_5_TIME_IN_PROCESS_J};

        ProcessJc.main(arguments);

    }



}
