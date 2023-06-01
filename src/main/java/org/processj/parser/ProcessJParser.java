package org.processj.parser;

import org.processj.Phase;
import org.processj.ast.Compilation;
import org.processj.ast.Expression;
import org.processj.ast.Token;
import org.processj.lexer.Lexer;
import org.processj.utilities.ProcessJSourceFile;
import org.processj.utilities.Strings;

/**
 * <p>Encapsulates a {@link Parser} instance in order to provide proper error handling during the parsing phase.
 * Allows for loosely-coupled dependencies between the {@link Parser} & the rest of the compiler.</p>
 * @see Parser
 * @see Phase
 * @see Parser.Handler
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class ProcessJParser extends Phase implements Parser.Handler {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ProcessJParser} to its' default state with the specified
     * {@link ProcessJParser.Listener} & corresponding {@link ProcessJSourceFile}.
     * @param listener The {@link Phase.Listener} that receives any {@link org.processj.Phase.Message},
     * {@link org.processj.Phase.Warning}, or {@link org.processj.Phase.Error} messages from the {@link ProcessJParser}.
     * @since 0.1.0
     */
    public ProcessJParser(final ProcessJParser.Listener listener) {
        super(listener);
    }

    /// ----------------------------------
    /// org.processj.parser.Parser.Handler

    /**
     * <p>Callback that's invoked when the {@link Parser} encounters a syntax error.</p>
     * @param token The {@link Token} that was last received by the {@link Parser}
     * @param line The integer value of the line where the error occurred.
     * @param lineLength The integer value corresponding to the length of the current line.
     * @param lineCount The integer value of the amount of lines in the source file.
     * @since 0.1.0
     */
    @Override
    public void onSyntaxError(final Token token,
                              final int line, final int lineLength, final int lineCount) {

        if(token == null) new UnexpectedEndOfFileException(this, line).commit();

        else new SyntaxErrorException(this, line, lineCount, lineLength - token.lexeme.length()).commit();

    }

    /**
     * <p>Callback that's invoked when the {@link Parser} encounters an illegal cast expression.</p>
     * @param expression The illegal {@link Expression}.
     * @param lineLength The integer value corresponding to the length of the current line.
     * @param lineCount The integer value of the amount of lines in the source file.
     * @since 0.1.0
     */
    @Override
    public void onIllegalCastExpression(final Expression expression,
                                        final int lineLength, final int lineCount) {

        // Create the exception & send it off
        new IllegalCastExpressionException(this, expression.getLine(),
                lineLength - expression.getColumn(), lineCount).commit();

    }

    /**
     * <p>Callback that's invoked when the {@link Parser} encounters a malformed package access expression.</p>
     * @param expression The illegal {@link Expression}.
     * @param lineLength The integer value corresponding to the length of the current line.
     * @param lineCount The integer value of the amount of lines in the source file.
     * @since 0.1.0
     */
    @Override
    public void onMalformedPackageAccessExpression(final Expression expression,
                                                   final int lineLength, final int lineCount) {

        // Create the exception & send it off
        new MalformedPackageAccessException(this, expression.getLine(),
                lineLength - expression.getColumn(), lineCount).commit();

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Parses the specified {@link ProcessJSourceFile} to produce a corresponding {@link Compilation}. This
     * method mutates the {@link Compilation} contained by the {@link ProcessJSourceFile}.</p>
     * @throws Phase.Error If an error occurred during parsing.
     * @since 0.1.0
     */
    @Override
    protected final void executePhase() throws Phase.Error{

        // Initialize a handle to the Compilation & Parser
        Parser parser = null;

        // Retrieve the ProcessJSource file
        final ProcessJSourceFile processJSourceFile = this.getProcessJSourceFile();

        // Attempt to
        try {

            // TODO: Maybe create the Lexer within the parser & pass the file instead
            // Initialize the Lexer & Parser
            parser = new Parser(new Lexer(processJSourceFile.getCorrespondingFileReader()));

            // Retrieve the Compilation
            processJSourceFile.setCompilation((Compilation) parser.parse().value);

        // Otherwise
        } catch(final Exception exception) {

            // Initialize & throw the error
            throw ((parser == null) ? new FileOpenFailureException(this, processJSourceFile).commit()
                    : new ParserFailureException(this, exception.getMessage()).commit());

        }

    }

    /// ------
    /// Errors

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a file open failure.</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class FileOpenFailureException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Failed to open file";

        /// --------------
        /// Private Fields

        /**
         * <p>Invalid file.</p>
         */
        private final ProcessJSourceFile invalidFile;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link FileOpenFailureException} with the culprit instance.</p>
         * @param culpritInstance The {@link ProcessJParser} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected FileOpenFailureException(final ProcessJParser culpritInstance, final ProcessJSourceFile processJSourceFile) {
            super(culpritInstance);

            this.invalidFile = processJSourceFile;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying a file failed to open.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            return Message + ": " + this.invalidFile.getName() + " for " + this;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link Parser} failure.</p>
     * @see Phase
     * @see Parser
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ParserFailureException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Failed to open file";

        /// --------------
        /// Private Fields

        /**
         * <p>Invalid file.</p>
         */
        private final String errorMessage;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ParserFailureException} with the culprit instance.</p>
         * @param culpritInstance The {@link ProcessJParser} instance that raised the error.
         * @param errorMessage The {@link String} value containing the error emitted by the {@link Parser}.
         * @see Phase
         * @see Parser
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected ParserFailureException(final ProcessJParser culpritInstance, final String errorMessage) {
            super(culpritInstance);

            this.errorMessage = errorMessage;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying a file failed to open.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            return Message + ": " + this.errorMessage + " for " + this;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an unexpected end of file.</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class UnexpectedEndOfFileException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Unexpected end of file";

        /// --------------
        /// Private Fields

        /**
         * <p>The line within the source file where the error occurred.</p>
         */
        private final int currentLine;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link UnexpectedEndOfFileException} with the culprit instance & current line.</p>
         * @param culpritInstance The {@link ProcessJParser} instance that raised the error.
         * @param currentLine The integer value of where the error occurred within the source file.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected UnexpectedEndOfFileException(final ProcessJParser culpritInstance, final int currentLine) {
            super(culpritInstance);

            this.currentLine = currentLine;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying where in the source file the error occurred.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            return Message + ": " + this.currentLine;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a syntax error.</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class SyntaxErrorException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Syntax error:\n\n";

        /// --------------
        /// Private Fields

        /**
         * <p>The line within the source file where the error occurred.</p>
         */
        private final int currentLine   ;

        /**
         * <p>Integer value corresponding to the amount of lines in the current input file.</p>
         */
        private final int lineCount     ;

        /**
         * <p>Integer value corresponding to the amount of columns to print.</p>
         */
        private final int blankColumns  ;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link SyntaxErrorException} with the culprit instance, current line, & current line
         * count.</p>
         * @param culpritInstance The {@link ProcessJParser} instance that raised the error.
         * @param currentLine The integer value of where the error occurred within the source file.
         * @param lineCount Integer value corresponding to the amount of lines in the current input file.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected SyntaxErrorException(final ProcessJParser culpritInstance,
                                       final int currentLine, final int lineCount, final int blankColumns) {
            super(culpritInstance);

            this.currentLine        = currentLine       ;
            this.lineCount          = lineCount         ;
            this.blankColumns       = blankColumns      ;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying where in the source file the error occurred.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Retrieve the culprit & initialize the StringBuilder
            final ProcessJParser culpritInstance = (ProcessJParser) this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName()
                    + ":" + this.lineCount + ": " + Message + this.currentLine + "\n"
                    + Strings.BlankStringOf(this.blankColumns) + '^';

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an illegal cast expression error.</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class IllegalCastExpressionException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Illegal Cast Expression:\n\n";

        /// --------------
        /// Private Fields

        /**
         * <p>The line within the source file where the error occurred.</p>
         */
        private final int currentLine   ;

        /**
         * <p>Integer value corresponding to the amount of lines in the current input file.</p>
         */
        private final int lineCount     ;

        /**
         * <p>Integer value corresponding to the amount of columns to print.</p>
         */
        private final int blankColumns  ;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link SyntaxErrorException} with the culprit instance, current line, & current line
         * count.</p>
         * @param culpritInstance The {@link ProcessJParser} instance that raised the error.
         * @param currentLine The integer value of where the error occurred within the source file.
         * @param lineCount Integer value corresponding to the amount of lines in the current input file.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected IllegalCastExpressionException(final ProcessJParser culpritInstance,
                                       final int currentLine, final int lineCount, final int blankColumns) {
            super(culpritInstance);

            this.currentLine        = currentLine       ;
            this.lineCount          = lineCount         ;
            this.blankColumns       = blankColumns      ;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying where in the source file the error occurred.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Retrieve the culprit & initialize the StringBuilder
            final ProcessJParser culpritInstance = (ProcessJParser) this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName()
                    + ":" + this.lineCount + ": " + Message + this.currentLine + "\n"
                    + Strings.BlankStringOf(this.blankColumns) + '^';

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a malformed package access
     * expression.</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class MalformedPackageAccessException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = """
                Malformed package access expression; not a name expression or record access:
                
                
                """;

        /// --------------
        /// Private Fields

        /**
         * <p>The line within the source file where the error occurred.</p>
         */
        private final int currentLine   ;

        /**
         * <p>Integer value corresponding to the amount of lines in the current input file.</p>
         */
        private final int lineCount     ;

        /**
         * <p>Integer value corresponding to the amount of columns to print.</p>
         */
        private final int blankColumns  ;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link MalformedPackageAccessException} with the culprit instance, current line, &
         * current line count.</p>
         * @param culpritInstance The {@link ProcessJParser} instance that raised the error.
         * @param currentLine The integer value of where the error occurred within the source file.
         * @param lineCount Integer value corresponding to the amount of lines in the current input file.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected MalformedPackageAccessException(final ProcessJParser culpritInstance,
                                                 final int currentLine, final int lineCount, final int blankColumns) {
            super(culpritInstance);

            this.currentLine        = currentLine       ;
            this.lineCount          = lineCount         ;
            this.blankColumns       = blankColumns      ;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying where in the source file the error occurred.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Retrieve the culprit & initialize the StringBuilder
            final ProcessJParser culpritInstance = (ProcessJParser) this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName()
                    + ":" + this.lineCount + ": " + Message + this.currentLine + "\n"
                    + Strings.BlankStringOf(this.blankColumns) + '^';

        }

    }

    /// --------------
    /// Phase.Listener

    /**
     * <p>Represents the corresponding {@link ProcessJParser.Listener} that handles receiving {@link Phase.Message}s
     * from the {@link ProcessJParser}.</p>
     * @see Parser
     * @see Phase
     * @see Parser.Handler
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 0.1.0
     */
    public static abstract class Listener extends Phase.Listener { /* Placeholder */ }

}
