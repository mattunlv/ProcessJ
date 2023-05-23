package org.processj.test;

import org.junit.jupiter.api.Test;
import org.processj.ProcessJc;

public class EndToEndTests {

    @Test
    public void bookCodeChapter1Section2_endToEnd() {

        final String[] arguments = {"src/test/resources/code/book/chapter_1/section_2/synchronized_communication.pj"};

        ProcessJc.main(arguments);

    }

}
