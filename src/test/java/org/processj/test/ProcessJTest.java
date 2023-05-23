package org.processj.test;

public class ProcessJTest {

    /// --------------------------
    /// Protected Static Constants

    protected final static SourceInput BookSynchronizedCommunication
            = new SourceInput("book/chapter_1/section_2/", "synchronized_communication");
    protected final static SourceInput BookTimeInProcessJ
            = new SourceInput("book/chapter_3/section_5/", "time_in_process_j");
    protected final static SourceInput BookOneToOneChannels
            = new SourceInput("book/chapter_3/section_6/", "one_to_one_channels");
    protected final static SourceInput BookFibonacci
            = new SourceInput("book/chapter_3/section_7/", "fibonacci");
    protected final static SourceInput BookFifo6
            = new SourceInput("book/chapter_3/section_7/", "fifo6");
    protected final static SourceInput BookIntegrate
            = new SourceInput("book/chapter_3/section_7/", "integrate");
    protected final static SourceInput BookNumbers
            = new SourceInput("book/chapter_3/section_7/", "numbers");
    protected final static SourceInput BookPairs
            = new SourceInput("book/chapter_3/section_7/", "pairs");
    protected final static SourceInput BookSquares
            = new SourceInput("book/chapter_3/section_7/", "squares");
    protected final static SourceInput BookStructuredExecution
            = new SourceInput("book/chapter_3/section_7/", "structured_execution");
    protected final static SourceInput BookEightBitAdder
            = new SourceInput("book/chapter_3/section_8/", "eight_bit_adder");
    protected final static SourceInput BookFourBitAdder
            = new SourceInput("book/chapter_3/section_8/", "four_bit_adder");
    protected final static SourceInput BookOneBitAdder
            = new SourceInput("book/chapter_3/section_8/", "one_bit_adder");
    protected final static SourceInput BookOutput
            = new SourceInput("book/chapter_3/section_9/", "output");
    protected final static SourceInput BookSwitch
            = new SourceInput("book/chapter_4/section_1/", "switch");
    protected final static SourceInput BookFor
            = new SourceInput("book/chapter_4/section_2/", "for");
    protected final static SourceInput BookWhile
            = new SourceInput("book/chapter_4/section_2/", "while");
    protected final static SourceInput BookRepetition
            = new SourceInput("book/chapter_4/section_3/", "repetition");
    protected final static SourceInput BookAlt
            = new SourceInput("book/chapter_4/section_4/", "alt");
    protected final static SourceInput BookMux
            = new SourceInput("book/chapter_4/section_4/", "mux");
    protected final static SourceInput BookTimeoutGuard
            = new SourceInput("book/chapter_4/section_4/", "timeoutguard");
    protected final static SourceInput BookWatchdog
            = new SourceInput("book/chapter_4/section_4/", "watchdog");
    protected final static SourceInput BookPriAlt
            = new SourceInput("book/chapter_4/section_5/", "pri_alt");

    /// ------------
    /// General Code

    protected final static SourceInput Alt01
            = new SourceInput("test/", "Alt01");
    protected final static SourceInput Array01
            = new SourceInput("test/", "Array01");
    protected final static SourceInput Array02
            = new SourceInput("test/", "Array02");
    protected final static SourceInput Barrier01
            = new SourceInput("test/", "Barrier01");
    protected final static SourceInput Barrier02
            = new SourceInput("test/", "Barrier02");
    protected final static SourceInput BinaryExpression01
            = new SourceInput("test/", "BinaryExpression01");
    protected final static SourceInput BinaryExpression02
            = new SourceInput("test/", "BinaryExpression02");
    protected final static SourceInput ByteCode01
            = new SourceInput("test/", "ByteCode01");
    protected final static SourceInput ChannelArray01
            = new SourceInput("test/", "ChannelArray01");
    protected final static SourceInput ChannelArray02
            = new SourceInput("test/", "ChannelArray02");
    protected final static SourceInput ChannelEndArray01
            = new SourceInput("test/", "ChannelEndArray01");
    protected final static SourceInput ChannelEndArray02
            = new SourceInput("test/", "ChannelEndArray02");
    protected final static SourceInput ChannelRead01
            = new SourceInput("test/", "ChannelRead01");
    protected final static SourceInput ChannelRead02
            = new SourceInput("test/", "ChannelRead02");
    protected final static SourceInput ChannelWrite01
            = new SourceInput("test/", "ChannelWrite01");
    protected final static SourceInput ChannelWrite02
            = new SourceInput("test/", "ChannelWrite02");
    protected final static SourceInput ChannelWrite03
            = new SourceInput("test/", "ChannelWrite03");
    protected final static SourceInput ChannelWrite04
            = new SourceInput("test/", "ChannelWrite04");
    protected final static SourceInput Enroll01
            = new SourceInput("test/", "Enroll01");
    protected final static SourceInput Fibonacci
            = new SourceInput("test/", "Fibonacci");
    protected final static SourceInput For01
            = new SourceInput("test/", "For01");
    protected final static SourceInput FullAdder
            = new SourceInput("test/", "FullAdder");
    protected final static SourceInput Hello
            = new SourceInput("test/", "Hello");
    protected final static SourceInput If01
            = new SourceInput("test/", "If01");
    protected final static SourceInput Integrate
            = new SourceInput("test/", "Integrate");
    protected final static SourceInput LocalDeclaration01
            = new SourceInput("test/", "LocalDeclaration01");
    protected final static SourceInput Mandelbrot01
            = new SourceInput("test/", "Mandelbrot01");
    protected final static SourceInput Mandelbrot02
            = new SourceInput("test/", "Mandelbrot02");
    protected final static SourceInput Mandelbrot03
            = new SourceInput("test/", "Mandelbrot03");
    protected final static SourceInput Mandelbrot04
            = new SourceInput("test/", "Mandelbrot04");
    protected final static SourceInput Par01
            = new SourceInput("test/", "Par01");
    protected final static SourceInput Par02
            = new SourceInput("test/", "Par02");
    protected final static SourceInput Par03
            = new SourceInput("test/", "Par03");
    protected final static SourceInput PriAlt01
            = new SourceInput("test/", "PriAlt01");
    protected final static SourceInput Protocol01
            = new SourceInput("test/", "Protocol01");
    protected final static SourceInput Protocol02
            = new SourceInput("test/", "Protocol02");
    protected final static SourceInput Protocol03
            = new SourceInput("test/", "Protocol03");
    protected final static SourceInput Record01
            = new SourceInput("test/", "Record01");
    protected final static SourceInput Record02
            = new SourceInput("test/", "Record02");
    protected final static SourceInput Record03
            = new SourceInput("test/", "Record03");
    protected final static SourceInput Record04
            = new SourceInput("test/", "Record04");
    protected final static SourceInput Record05
            = new SourceInput("test/", "Record05");
    protected final static SourceInput Santa01
            = new SourceInput("test/", "Santa01");
    protected final static SourceInput Santa02
            = new SourceInput("test/", "Santa02");
    protected final static SourceInput Santa03
            = new SourceInput("test/", "Santa03");
    protected final static SourceInput SharedChannel01
            = new SourceInput("test/", "SharedChannel01");
    protected final static SourceInput SharedChannelRead01
            = new SourceInput("test/", "SharedChannelRead01");
    protected final static SourceInput SharedChannelWrite01
            = new SourceInput("test/", "SharedChannelWrite01");
    protected final static SourceInput Silly
            = new SourceInput("test/", "Silly");
    protected final static SourceInput SortPump
            = new SourceInput("test/", "SortPump");
    protected final static SourceInput Switch01
            = new SourceInput("test/", "Switch01");
    protected final static SourceInput Timer01
            = new SourceInput("test/", "Timer01");

}
