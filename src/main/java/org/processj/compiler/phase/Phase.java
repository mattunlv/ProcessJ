package org.processj.compiler.phase;

import org.processj.compiler.SourceFile;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.access.ArrayAccessExpression;
import org.processj.compiler.ast.expression.binary.AssignmentExpression;
import org.processj.compiler.ast.expression.binary.BinaryExpression;
import org.processj.compiler.ast.expression.constructing.NewArrayExpression;
import org.processj.compiler.ast.expression.constructing.NewMobileExpression;
import org.processj.compiler.ast.expression.literal.ArrayLiteralExpression;
import org.processj.compiler.ast.expression.literal.LiteralExpression;
import org.processj.compiler.ast.expression.literal.ProtocolLiteralExpression;
import org.processj.compiler.ast.expression.literal.RecordLiteralExpression;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.expression.result.CastExpression;
import org.processj.compiler.ast.expression.result.TernaryExpression;
import org.processj.compiler.ast.expression.unary.UnaryPostExpression;
import org.processj.compiler.ast.expression.unary.UnaryPreExpression;
import org.processj.compiler.ast.expression.yielding.ChannelReadExpression;
import org.processj.compiler.ast.expression.result.InvocationExpression;
import org.processj.compiler.ast.statement.yielding.*;
import org.processj.compiler.ast.expression.*;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.conditional.*;
import org.processj.compiler.ast.statement.control.*;
import org.processj.compiler.ast.statement.declarative.LocalDeclaration;
import org.processj.compiler.ast.statement.declarative.ProtocolCase;
import org.processj.compiler.ast.expression.result.SwitchLabel;
import org.processj.compiler.ast.statement.declarative.RecordMemberDeclaration;
import org.processj.compiler.ast.statement.conditional.SwitchGroupStatement;
import org.processj.compiler.ast.statement.conditional.SwitchStatement;
import org.processj.compiler.ast.type.*;
import org.processj.compiler.utilities.*;
import org.processj.compiler.ast.Context;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.processj.compiler.utilities.Files.RetrieveMatchingFilesListFrom;
import static org.processj.compiler.utilities.Files.RetrieveMatchingFilesListWithName;
import static org.processj.compiler.utilities.Reflection.NewInstanceOf;

/**
 * <p>Class that encapsulates a compiler {@link Phase} to execute upon a {@link SourceFile} to provide
 * proper error reporting & handling between the {@link Phase} and compiler. Allows for loosely-coupled dependencies
 * between the compiler's {@link Phase}s.</p></p>
 * @author Carlos L. Cuenca
 * @see SourceFile
 * @version 1.0.0
 * @since 0.1.0
 */
public abstract class Phase implements Visitor {

    /// --------------------
    /// Public Static Fields

    /**
     * <p>Functional interface for a method to be specified to the {@link Phase} class that provides a means to
     * request a {@link Compilation}.</p>
     */
    public static Request Request = null;

    /// --------------
    /// Private Fields

    /**
     * <p>{@link Listener} instance that receives any {@link Phase.Message} instances from the {@link Phase}.</p>
     */
    private final Listener      listener            ;

    /**
     * <p>The current scope when traversing the {@link Compilation}.</p>
     */
    private Context.SymbolMap   scope               ;

    /**
     * <p>The {@link SourceFile} instance that is associated with this {@link Phase}. This field is updated
     * for each {@link Phase#execute(SourceFile)} invocation.</p>
     */
    private SourceFile sourceFile;

    private Context context;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link Listener}.</p>
     * @param listener The {@link Listener} to bind to the {@link Phase}.
     * @since 0.1.0
     */
    public Phase(final Listener listener) {

        this.listener           = listener  ;
        this.scope              = null      ;
        this.sourceFile = null      ;
        this.context = null;

    }

    /// -------
    /// Visitor

    @Override
    public final Context.SymbolMap getScope() {

        return this.scope;

    }

    public final Context getContext() {

        return this.context;

    }

    public final Context.SymbolMap getEnclosingScope() {

        return this.scope.getEnclosingScope();

    }

    public final Context getEnclosingContext() {

        return this.context.getEnclosingContext();

    }

    @Override
    public final void setScope(final Context.SymbolMap symbolMap) {

        this.scope = symbolMap;

    }

    @Override
    public final void setContext(final Context context) {

        this.context = context;

    }

    /// --------------------------
    /// Protected Abstract Methods

    /**
     * <p>Method that is invoked within {@link Phase#execute(SourceFile)}. All Phase-dependent
     * procedures should be executed here if not following the default visitor pattern.</p>
     * @see Phase.Error
     * @since 0.1.0
     */
    protected void executePhase() throws Phase.Error {

        // Begin the traversal
        this.retrieveValidCompilation().accept(this);

    }

    /// -----------------
    /// Protected Methods

    /**
     * <p>Returns the {@link Listener} instance bound to the {@link Phase}.</p>
     * @return {@link Listener} instance bound to the {@link Phase}.
     * @since 0.1.0
     */
    protected final Listener getListener() {

        return this.listener;

    }

    /**
     * <p>Returns the {@link SourceFile} instance bound to the {@link Phase}.</p>
     * @return {@link SourceFile} instance bound to the {@link Phase}.
     * @since 0.1.1
     */
    protected final SourceFile getSourceFile() {

        return this.sourceFile;

    }

    /**
     * <p>Returns a valid {@link Compilation} instance from the {@link SourceFile}. This method successfully
     * returns if the {@link SourceFile} contains a valid {@link Compilation}.</p>
     * @return {@link Compilation} corresponding to the {@link SourceFile}.
     * @since 0.1.0
     */
    protected final Compilation retrieveValidCompilation() {

        // Retrieve the ProcessJ Source File
        final SourceFile sourceFile = this.getSourceFile();

        // If a null value was specified for the ProcessJ source file
        if(sourceFile == null)
            FatalAssert.NullProcessJSourceFile.Assert(this);

        // If the processJ source file does not contain a Compilation
        else if(sourceFile.isNotBoundedToCompilation())
            FatalAssert.NullCompilation.Assert(this);

        // Return the compilation
        return sourceFile.getCompilation();

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Executes the {@link Phase}. Invokes the {@link Phase}'s specific implementation.</p>
     * @throws Phase.Error If a null value was specified for the {@link Phase}.
     * @since 0.1.0
     */
    public final void execute(final SourceFile sourceFile) throws Phase.Error {

        // If a null value was specified for the Listener, emit the error
        if(this.listener == null)
            FatalAssert.NullListener.Assert(this);

        // If a null value was specified for the ProcessJ source file
        else if(sourceFile == null)
            FatalAssert.NullProcessJSourceFile.Assert(this);

        // If the file has not been completed by this
        if(!sourceFile.hasBeenCompletedBy(this)) {

            // Otherwise, update the ProcessJSourceFile
            this.sourceFile = sourceFile;

            // Execute the phase
            this.executePhase();

            // Mark the file as completed by this Phase
            sourceFile.setCompletedPhase(this);

            // Clear
            this.sourceFile = null;

        }

    }

    /// ---------------
    /// Message Classes

    public static class Message extends Exception {

        /// --------------
        /// Private Fields

        private final Phase phase;

        /// ------------
        /// Constructors

        protected Message(final Phase phase) {

            // Fail if the Message was given an invalid Phase
            assert phase != null;

            this.phase = phase;

        }

        /// -----------------
        /// Protected Methods

        protected final Phase getPhase() {

            return this.phase;

        }

        protected final SourceFile getSourceFile() {

            return this.phase.getSourceFile();

        }

    }

    public static class Info extends Message {

        /// ------------------------
        /// Protected Static Methods

        /**
         * <p>Constructs an {@link Phase.Error} from the specified class object, {@link Phase}, & variadic parameters.
         * Once the {@link Phase.Error} is instantiated, this method will notify the specified {@link Phase}'s
         * {@link Phase.Listener} of the {@link Phase.Error}</p>
         * @param messageType The class object corresponding to the {@link Phase.Error} to instantiate.
         * @param phase The {@link Phase} where the assertion was raised.
         * @param parameters Any parameters that pertain to the {@link Phase.Error}.
         * @param <MessageType> Parameteric Type of the {@link Phase.Error}.
         */
        protected static <MessageType extends Info> void Assert(final Class<MessageType> messageType,
                                                                final Phase phase,
                                                                final Object... parameters) {

            // Notify the listener
            phase.getListener().notify(NewInstanceOf(messageType, phase, parameters));

        }

        /// ------------
        /// Constructors

        protected Info(final Phase phase) {
            super(phase);
        }

    }

    public static class Warning extends Message {

        /// ------------
        /// Constructors

        protected Warning(final Phase phase) {
            super(phase);
        }

    }

    public static class Error extends Message {

        /// ------------------------
        /// Protected Static Methods

        /**
         * <p>Constructs an {@link Phase.Error} from the specified class object, {@link Phase}, & variadic parameters.
         * Once the {@link Phase.Error} is instantiated, this method will notify the specified {@link Phase}'s
         * {@link Phase.Listener} of the {@link Phase.Error}</p>
         * @param messageType The class object corresponding to the {@link Phase.Error} to instantiate.
         * @param phase The {@link Phase} where the assertion was raised.
         * @param parameters Any parameters that pertain to the {@link Phase.Error}.
         * @param <MessageType> Parameteric Type of the {@link Phase.Error}.
         */
        protected static <MessageType extends Error> void Assert(final Class<MessageType> messageType,
                                                                 final Phase phase,
                                                                 final Object... parameters) {

            // Notify the listener
            phase.getListener().notify(NewInstanceOf(messageType, phase, parameters));

        }


        /// ------------
        /// Constructors

        protected Error(final Phase phase) {
            super(phase);
        }

        /// -----------------
        /// Protected Methods

        public final Error commit() {

            this.getPhase().getListener().notify(this);

            return this;

        }

    }

    /// --------
    /// Listener

    /**
     * <p>Listener class that handles a {@link Phase}'s informative, warning, & error messages appropriately.</p>
     * @see Phase
     * @see Phase.Info
     * @see Phase.Warning
     * @see Phase.Error
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 0.1.0
     */
    public abstract static class Listener {

        /// --------------------
        /// Public Static Fields

        /**
         * <p>The info logging method that handles informative messages.</p>
         */
        public static LogInfo       Info      = null        ;

        /**
         * <p>The warning logging method that handles warning messages.</p>
         */
        public static LogWarning    Warning   = null        ;

        /**
         * <p>The error logging method that handles error messages.</p>
         */
        public static LogError      Error     = null        ;

        /// --------------
        /// Private Fields

        /**
         * <p>{@link List} containing all of the {@link Phase}'s informative messages.</p>
         */
        private final List<Phase.Info>    infoList          ;

        /**
         * <p>{@link List} containing all of the {@link Phase}'s warning messages.</p>
         */
        private final List<Phase.Warning> warningList       ;

        /**
         * <p>{@link List} containing all of the {@link Phase}'s error messages.</p>
         */
        private final List<Phase.Error>   errorList         ;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link Phase.Listener} to its' default state.</p>
         * @since 0.1.0
         */
        public Listener() {

            this.infoList       = new ArrayList<>();
            this.warningList    = new ArrayList<>();
            this.errorList      = new ArrayList<>();

        }

        /// ---------------
        /// Private Methods

        /**
         * <p>Aggregates the specified {@link Phase.Info} to the corresponding list.</p>
         * @param phaseInfo The {@link Phase.Info} to aggregate.
         * @since 0.1.0
         */
        private void push(final Phase.Info phaseInfo) {

            if(phaseInfo != null)
                this.infoList.add(phaseInfo);

        }

        /**
         * <p>Aggregates the specified {@link Phase.Warning} to the corresponding list.</p>
         * @param phaseWarning The {@link Phase.Warning} to aggregate.
         * @since 0.1.0
         */
        private void push(final Phase.Warning phaseWarning) {

            if(phaseWarning != null)
                this.warningList.add(phaseWarning);

        }

        /**
         * <p>Aggregates the specified {@link Phase.Error} to the corresponding list.</p>
         * @param phaseError The {@link Phase.Error} to aggregate.
         * @since 0.1.0
         */
        private void push(final Phase.Error phaseError) {

            if(phaseError != null)
                this.errorList.add(phaseError);

        }

        /// -----------------
        /// Protected Methods

        /**
         * <p>Callback that's invoked when the {@link Phase} emits an informative {@link Phase.Message}</p>
         * @param phaseInfo The {@link Phase.Info} message to handle.
         * @see Phase.Info
         * @since 0.1.0
         */
        protected final void notify(final Phase.Info phaseInfo) {

            // Simply log the info
            Info.Log(phaseInfo.getSourceFile() + ": " + phaseInfo.getMessage());

            // Push the info
            this.push(phaseInfo);

        }

        /**
         * <p>Callback that's invoked when the {@link Phase} emits a warning {@link Phase.Message}</p>
         * @param phaseWarning The {@link Phase.Warning} message to handle.
         * @see Phase.Warning
         * @since 0.1.0
         */
        protected final void notify(final Phase.Warning phaseWarning) {

            // Simply log the warning
            Warning.Log(phaseWarning.getSourceFile() + ": " + phaseWarning.getMessage());

            // Push the warning
            this.push(phaseWarning);

        }

        /**
         * <p>Callback that's invoked when the {@link Phase} emits an error {@link Phase.Message}</p>
         * @param phaseError The {@link Phase.Error} message to handle.
         * @see Phase.Error
         * @since 0.1.0
         */
        protected final void notify(final Phase.Error phaseError)  {

            // Log the message
            Error.Log(phaseError.getSourceFile() + ": " + phaseError.getMessage());

            // Push the error
            this.push(phaseError);

        }

        /**
         * <p>Retrieves the {@link Listener}'s collection of {@link Phase.Info} messages.</p>
         * @since 0.1.0
         */
        protected final List<Phase.Info> getInfoList() {

            return this.infoList;

        }

        /**
         * <p>Retrieves the {@link Listener}'s collection of {@link Phase.Warning} messages.</p>
         * @since 0.1.0
         */
        protected final List<Phase.Warning> getWarningList() {

            return this.warningList;

        }

        /**
         * <p>Retrieves the {@link Listener}'s collection of {@link Phase.Error} messages.</p>
         * @since 0.1.0
         */
        protected final List<Phase.Error> getErrorList() {

            return this.errorList;

        }

        /// ---------------------
        /// Functional Interfaces

        /**
         * <p>Defines the {@link Phase.Listener} info logging method signature for the stream responsible for outputting
         * informative messages.</p>
         */
        @FunctionalInterface
        public interface LogInfo {

            void Log(final String message);

        }

        /**
         * <p>Defines the {@link Phase.Listener} warning logging method signature for the stream responsible for
         * outputting warning messages.</p>
         */
        @FunctionalInterface
        public interface LogWarning {

            void Log(final String message);

        }

        /**
         * <p>Defines the {@link Phase.Listener} error logging method signature for the stream responsible for
         * outputting error messages.</p>
         */
        @FunctionalInterface
        public interface LogError {

            void Log(final String message);

        }

    }
    private final static String PropertiesPath = "src/main/resources/properties/";
    private static final Map<Integer, String> ErrorTable = new HashMap<>();

    static {

        // Initialize the Properties
        final Properties properties = new Properties();

        // Attempt to
        try {

            // Load the properties file
            properties.load(new FileInputStream(PropertiesPath));

        } catch(final Exception exception) {

            // Empty

        }

        // Initialize the error messages if we successfully loaded the file
        if(!properties.isEmpty()) {

            ErrorTable.put(100, properties.getProperty("RESOLVE_IMPORTS_100"));
            ErrorTable.put(101, properties.getProperty("RESOLVE_IMPORTS_101"));
            ErrorTable.put(102, properties.getProperty("RESOLVE_IMPORTS_102"));
            ErrorTable.put(103, properties.getProperty("RESOLVE_IMPORTS_103"));
            ErrorTable.put(104, properties.getProperty("RESOLVE_IMPORTS_104"));

        }

    }

    /// ----------
    /// Assertions

    /// -------------------
    /// Internal Assertions

    private static class FatalAssert {

        /// -----------
        /// Phase.Error

        /**
         * <p>{@link Phase.Error} to be emitted if the {@link Phase}'s {@link Listener} is not specified.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NullListener extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NullListener}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase) {

                Error.Assert(NullListener.class, phase);

            }

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NullListener} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NullListener(final Phase culprit) {
                super(culprit);
            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the {@link Phase}'s corresponding {@link SourceFile}
         * is not specified.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NullProcessJSourceFile extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NullProcessJSourceFile} to its' specified {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase) {

                Error.Assert(NullProcessJSourceFile.class, phase);

            }

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NullListener} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NullProcessJSourceFile(final Phase culprit) {
                super(culprit);
            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the {@link Phase}'s corresponding {@link Compilation}
         * is not specified.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NullCompilation extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NullCompilation} to its' specified {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase) {

                Error.Assert(NullCompilation.class, phase);

            }

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NullCompilation} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NullCompilation(final Phase culprit) {
                super(culprit);
            }

        }

    }

    /// ------------
    /// Parser Phase

    protected static class ParserAssert {

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link SourceFile} failed to open during
         * execution of a {@link Phase}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class FileOpenFailure extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link FatalAssert.NullListener}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final SourceFile sourceFile) {

                Error.Assert(FatalAssert.NullListener.class, phase);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>Invalid file.</p>
             */
            private final SourceFile invalidFile;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link FileOpenFailure} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected FileOpenFailure(final Phase culprit, final SourceFile sourceFile) {
                super(culprit);

                this.invalidFile = sourceFile;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link SourceFile} failed to parse.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ParserFailure extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ParserFailure}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final SourceFile sourceFile) {

                Error.Assert(ParserFailure.class, phase);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>Invalid file.</p>
             */
            private final SourceFile invalidFile;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ParserFailure} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ParserFailure(final Phase culprit, final SourceFile sourceFile) {
                super(culprit);

                this.invalidFile = sourceFile;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link SourceFile}'s {@link ProcessJParser} encountered an
         * unexpected end-of-file.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class UnexpectedEndOfFile extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link UnexpectedEndOfFile}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final int line) {

                Error.Assert(UnexpectedEndOfFile.class, phase, line);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>Invalid file.</p>
             */
            private final int line;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link UnexpectedEndOfFile} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected UnexpectedEndOfFile(final Phase culprit, final int line) {
                super(culprit);

                this.line = line;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link SourceFile} contained invalid syntax.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class SyntaxErrorException extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link SyntaxErrorException}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final int line, final int lineCount, final int length) {

                Error.Assert(SyntaxErrorException.class, phase, line, lineCount, length);

            }

            /// --------------
            /// Private Fields

            final int line      ;
            final int lineCount ;
            final int length    ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link SyntaxErrorException} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected SyntaxErrorException(final Phase culprit, final int line, final int lineCount, final int length) {
                super(culprit);
                this.line       = line      ;
                this.lineCount  = lineCount ;
                this.length     = length    ;
            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link SourceFile} encountered an illegal cast expression.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class IllegalCastExpression extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link IllegalCastExpression}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final int line, final int lineCount, final int length) {

                Error.Assert(IllegalCastExpression.class, phase, line, lineCount, length);

            }

            /// --------------
            /// Private Fields

            final int line      ;
            final int lineCount ;
            final int length    ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalCastExpression} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalCastExpression(final Phase culprit, final int line, final int lineCount, final int length) {
                super(culprit);
                this.line       = line      ;
                this.lineCount  = lineCount ;
                this.length     = length    ;
            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link SourceFile} encoded a malformed Package Access.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class MalformedPackageAccess extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link MalformedPackageAccess}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final int line, final int lineCount, final int length) {

                Error.Assert(MalformedPackageAccess.class, phase, line, lineCount, length);

            }

            /// --------------
            /// Private Fields

            final int line      ;
            final int lineCount ;
            final int length    ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link MalformedPackageAccess} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected MalformedPackageAccess(final Phase culprit, final int line, final int lineCount, final int length) {
                super(culprit);
                this.line       = line      ;
                this.lineCount  = lineCount ;
                this.length     = length    ;
            }

        }

    }

    protected static class PragmaAssert {

        /**
         * <p>The set of valid {@link Pragma}s.</p>
         */
        protected final static Set<String> ValidPragmas = Set.of(
                "LIBRARY",
                "LANGUAGE",
                "NATIVE",
                "NATIVELIB",
                "FILE");

        /**
         * <p>A {@link Map} containing the set of valid {@link Pragma} arguments.</p>
         */
        protected final static Map<String, Integer> PragmaArguments = Map.of(
                "LIBRARY", 0,
                "LANGUAGE", 1,
                "NATIVE", 0,
                "NATIVELIB", 1,
                "FILE", 1);

        /**
         * <p>Decodes the specified {@link Sequence} of {@link Pragma}s into a {@link Map}.
         * This verifies the current {@link Compilation}'s set of {@link Pragma}s are specified correctly.</p>
         * @param phase The invoking {@link Phase}.
         * @param pragmas The {@link Sequence} of {@link Pragma}s to decode.
         * @return {@link Map} containing the {@link Sequence} of {@link Pragma}s as key-value pairs.
         * @throws Error when the {@link Phase} encounters a malformed {@link Pragma}.
         * @since 0.1.0
         */
        protected static Map<String, String> DecodedPragmas(final Phase phase,
                                                            final Sequence<Pragma> pragmas) throws Error {

            // TODO: check values against pragmaArgValues[][];
            // Retrieve the compilation & a Pragma Map
            final Map<String, String> pragmaMap = new HashMap<>();

            // Iterate through the pragmas
            for(final Pragma pragma: pragmas) {

                // If the Pragma is Invalid
                if(!PragmaAssert.ValidPragmas.contains(pragma.toString()))
                    IllegalPragma.Assert(phase, pragma);

                    // If the Pragma requires arguments but none were given
                else if(PragmaAssert.PragmaArguments.get(pragma.toString()) > 0 && pragma.getValue() == null)
                    PragmaRequiresArgument.Assert(phase, pragma);

                    // If the Pragma requires no arguments, but some were given
                else if(PragmaAssert.PragmaArguments.get(pragma.toString()) == 0 && pragma.getValue() != null)
                    PragmaRequiresNoArgument.Assert(phase, pragma);

                    // If the Pragma has already been decoded
                else if(pragmaMap.containsKey(pragma.toString()))
                    PragmaAlreadySpecified.Assert(phase, pragma);

                // Send out the message
                new PragmaAssert.EnteringPragma(phase, pragma).commit();

                // Aggregate the Pragma
                pragmaMap.put(pragma.toString(), pragma.getValue().replace("\"", ""));

            }

            // Return the result
            return pragmaMap;

        }

        /**
         * <p>Asserts that if the {@link Compilation} declares {@link Pragma}s, the set of {@link Pragma}s are valid.</p>
         * @param phase The invoking {@link Phase}.
         * @param compilation The {@link Compilation} to validate.
         * @throws Error If the {@link Compilation}'s {@link Pragma}s are invalid.
         * @since 0.1.0
         */
        protected static void ValidateNativeLibrary(final Phase phase,
                                                    final Compilation compilation) throws Error {

            // Initialize a handle to the current scope
            final Context.SymbolMap scope = phase.getScope();

            // Assert the decoded pragmas are defined in this scope
            scope.setPragmasTo(PragmaAssert.DecodedPragmas(phase, compilation.getPragmas()));

            // Assert the Scope defines a library Pragma
            if(scope.definesLibraryPragma()) {

                // Assert that a native Compilation declares a package name
                PragmaAssert.ValidateCompilationPackageName(phase, compilation);

                // Initialize a handle to the decoded pragmas
                final Map<String, String> pragmaMap = scope.getPragmaMap();

                // Send an informative message
                PragmaAssert.LibraryPragmaDetected.Assert(phase);

                // Initialize the flags
                final boolean isNative          = pragmaMap.containsKey("NATIVE");
                final boolean isNativeLibrary   = pragmaMap.containsKey("NATIVELIB");

                // If a LANGUAGE Pragma is not specified, throw an error
                if(!pragmaMap.containsKey("LANGUAGE"))
                    MissingLanguagePragma.Assert(phase);

                    // If a FILE Pragma is not specified
                else if(!pragmaMap.containsKey("FILE"))
                    MissingFilePragma.Assert(phase);

                // If NATIVE & NATIVELIB are both defined
                if(isNative && isNativeLibrary)
                    NativeAndNativeLib.Assert(phase);

                // Retrieve the specified language
                final String language = pragmaMap.get("LANGUAGE");

                // Assert native was specified
                if(isNative || isNativeLibrary) {

                    // C wasn't the specified language
                    if(!language.equals("C"))
                        PragmaAssert.NativeWithProcessJ.Assert(phase);

                        // Invalid language
                    else PragmaAssert.InvalidNativeLanguage.Assert(phase);

                }

            }

        }

        /**
         * <p>Validates the {@link ConstantDeclaration}'s native declaration.</p>
         * @param phase The invoking {@link Phase}
         * @param constantDeclaration The {@link ConstantDeclaration} to validate
         * @throws Error If the {@link ConstantDeclaration} is invalid.
         * @since 0.1.0
         */
        protected static void ValidateNativeConstantDeclaration(final Phase phase,
                                                                final ConstantDeclaration constantDeclaration) throws Error {

            // Initialize a handle to the Scope
            final Context.SymbolMap scope = phase.getScope();

            if(scope.isNativeLibrary() || scope.isNative()) {

                if(!scope.isNative() && !constantDeclaration.isDeclaredNative())
                    ConstantDeclarationNonNative.Assert(phase, constantDeclaration);

                else if(scope.isNative() && constantDeclaration.isDeclaredNative())
                    IllegalConstantDeclarationNative.Assert(phase, constantDeclaration);

                else if(constantDeclaration.isInitialized())
                    ConstantDeclarationInitialized.Assert(phase, constantDeclaration);

            } else PragmaAssert.ValidateNonNativeConstantDeclaration(phase, constantDeclaration);

        }

        /**
         * <p>Validates the {@link ConstantDeclaration}'s non-native declaration.</p>
         * @param validatePragmas The invoking {@link Phase}
         * @param constantDeclaration The {@link ConstantDeclaration} to validate
         * @throws Error If the {@link ConstantDeclaration} is invalid.
         * @since 0.1.0
         */
        protected static void ValidateNonNativeConstantDeclaration(final Phase validatePragmas,
                                                                   final ConstantDeclaration constantDeclaration)
                throws Error {

            if(constantDeclaration.isDeclaredNative())
                ConstantDeclarationNative.Assert(validatePragmas, constantDeclaration);

            else if(!constantDeclaration.isInitialized())
                ConstantDeclarationNotInitialized.Assert(validatePragmas, constantDeclaration);

        }

        /**
         * <p>Validates the {@link ProcedureTypeDeclaration}'s non-native procedure declaration.</p>
         * @param validatePragmas The invoking {@link Phase}
         * @param procedureTypeDeclaration The {@link ProcedureTypeDeclaration} to validate
         * @throws Error If the {@link ProcedureTypeDeclaration} is invalid.
         * @since 0.1.0
         */
        protected static void ValidateNonNativeProcedureDeclaration(final Phase validatePragmas,
                                                                    final ProcedureTypeDeclaration procedureTypeDeclaration)
                throws Error {

            // Procedures must contain bodies
            if(!procedureTypeDeclaration.definesBody())
                ProcedureDeclarationDoesNotDefineBody
                        .Assert(validatePragmas, procedureTypeDeclaration);

                // Procedures must not be declared native
            else if(procedureTypeDeclaration.isNative())
                ProcedureDeclarationNative
                        .Assert(validatePragmas, procedureTypeDeclaration);

        }

        /**
         * <p>Validates the {@link ProcedureTypeDeclaration}'s native procedure declaration.</p>
         * @param phase The invoking {@link Phase}
         * @param procedureTypeDeclaration The {@link ProcedureTypeDeclaration} to validate
         * @throws Error If the {@link ProcedureTypeDeclaration} is invalid.
         * @since 0.1.0
         */
        protected static void ValidateNativeProcedureDeclaration(final Phase phase,
                                                                 final ProcedureTypeDeclaration procedureTypeDeclaration)
                throws Error {

            // Initialize a handle to the Scope
            final Context.SymbolMap scope = phase.getScope();

            if(scope.isNativeLibrary() || scope.isNative()) {

                // NATIVELIB and NATIVE library files cannot contain procedures with ProcessJ bodies.
                if(procedureTypeDeclaration.definesBody())
                    ProcedureDeclarationDefinesBody.Assert(phase, procedureTypeDeclaration);

                    // If the Procedure is not declared native
                else if(!procedureTypeDeclaration.isNative())
                    ProcedureDeclarationNonNative.Assert(phase, procedureTypeDeclaration);

                // Validate the return type
                PragmaAssert.ValidateNativeProcedureReturnType(phase, procedureTypeDeclaration);

                // Validate the parameter types
                PragmaAssert.ValidateNativeProcedureParameterTypes(phase, procedureTypeDeclaration);

                // Finally, aggregate the Procedure's native signature
                if(scope.isNative()) {

                    scope.aggregateNativeSignature(PragmaAssert.NativeTypeStringFor(procedureTypeDeclaration.getReturnType())
                            + " " + procedureTypeDeclaration.getName().getPackageName() + "_" + procedureTypeDeclaration
                            + "_" + PragmaAssert.NativeTypeListStringFor(procedureTypeDeclaration));

                    // TODO: Should this cover NATIVELIB?
                    procedureTypeDeclaration.setNative();

                }

            } else {

                PragmaAssert.ValidateNonNativeProcedureDeclaration(phase, procedureTypeDeclaration);

            }

        }

        /**
         * <p>Validates the {@link ProcedureTypeDeclaration}'s native procedure return type.</p>
         * @param validatePragmas The invoking {@link Phase}
         * @param procedureTypeDeclaration The {@link ProcedureTypeDeclaration} to validate
         * @throws Error If the {@link ProcedureTypeDeclaration} is invalid.
         * @since 0.1.0
         */
        protected static void ValidateNativeProcedureReturnType(final Phase validatePragmas,
                                                                final ProcedureTypeDeclaration procedureTypeDeclaration)
                throws Error {

            // Retrieve the Procedure's return type
            final Type returnType = procedureTypeDeclaration.getReturnType();

            // Moved From GenerateNativeCode Visitor
            // If the Procedure does not specify a primitive return type
            if(!(returnType instanceof PrimitiveType))
                IllegalProcedureReturnType.Assert(validatePragmas, procedureTypeDeclaration);

                // If the Procedure specifies a barrier or timer return type
            else if(returnType.isBarrierType() || returnType.isTimerType())
                IllegalProcedureBarrierOrTimerReturnType.Assert(validatePragmas, procedureTypeDeclaration);

        }

        /**
         * <p>Validates the {@link ProcedureTypeDeclaration}'s native procedure parameter types.</p>
         * @param validatePragmas The invoking {@link Phase}
         * @param procedureTypeDeclaration The {@link ProcedureTypeDeclaration} to validate
         * @throws Error If the {@link ProcedureTypeDeclaration}'s parameter types are invalid.
         * @since 0.1.0
         */
        protected static void ValidateNativeProcedureParameterTypes(final Phase validatePragmas,
                                                                    final ProcedureTypeDeclaration procedureTypeDeclaration)
                throws Error {

            for(final ParameterDeclaration parameterDeclaration: procedureTypeDeclaration.getParameters()) {

                // Initialize a handle to the parameter type
                final Type parameterType = parameterDeclaration.getType();

                // If the ProcTypeDecl specified a non-primitive type
                if(!(parameterType instanceof PrimitiveType))
                    IllegalProcedureParameterType.Assert(validatePragmas, procedureTypeDeclaration, parameterType);

                    // Otherwise, If the procedure specifies a barrier or timer parameter
                else if(parameterType.isBarrierType() || parameterType.isTimerType())
                    IllegalProcedureBarrierOrTimerParameterType.Assert(validatePragmas,
                            procedureTypeDeclaration, parameterType);

            }

        }

        /**
         * <p>Validates a {@link ProtocolTypeDeclaration}'s is not defined in a native library.</p>
         * @param phase The invoking {@link Phase}
         * @param protocolTypeDeclaration The {@link ProtocolTypeDeclaration} that was declared in native library.
         * @throws Error If the native library contained a {@link ProtocolTypeDeclaration} is invalid.
         * @since 0.1.0
         */
        protected static void ValidateNativeLibraryDoesNotContainProtocolDeclaration(final Phase phase,
                                                                                     final ProtocolTypeDeclaration protocolTypeDeclaration)
                throws Error {

            // Initialize a handle to the Scope
            final Context.SymbolMap scope = phase.getScope();

            if(scope.isNativeLibrary() || scope.isNative())
                LibraryContainsProtocolDeclaration.Assert(phase, protocolTypeDeclaration);

        }

        /**
         * <p>Validates a {@link RecordTypeDeclaration}'s is not defined in a native library.</p>
         * @param phase The invoking {@link Phase}
         * @param recordTypeDeclaration The {@link RecordTypeDeclaration} that was declared in native library.
         * @throws Error If the native library contained a {@link RecordTypeDeclaration} is invalid.
         * @since 0.1.0
         */
        protected static void ValidateNativeLibraryDoesNotContainRecordDeclaration(final Phase phase,
                                                                                   final RecordTypeDeclaration recordTypeDeclaration)
                throws Error {

            // Initialize a handle to the Scope
            final Context.SymbolMap scope = phase.getScope();

            if(scope.isNativeLibrary() || scope.isNative())
                LibraryContainsRecordDeclaration.Assert(phase, recordTypeDeclaration);

        }

        /**
         * <p>Validates a {@link Compilation}'s native {@link Pragma} is valid.</p>
         * @param phase The invoking {@link Phase}
         * @param compilation The {@link Compilation} that was declared in native library.
         * @throws Error If the native library contained a {@link Compilation} is invalid.
         * @since 0.1.0
         */
        protected static void ValidateNativeCompilation(final Phase phase, final Compilation compilation)
                throws Error {

            final Context.SymbolMap scope = phase.getScope();

            // This smells. It might not be invalid
            if(scope.isNative() && !scope.definesNativeSignatures())
                InvalidNativeSignatures.Assert(phase, compilation);

        }

        /**
         * <p>Validates a {@link Compilation}'s native {@link Pragma} is valid.</p>
         * @param phase The invoking {@link Phase}
         * @param compilation The {@link Compilation} that was declared in native library.
         * @throws Error If the native library contained a {@link Compilation} is invalid.
         * @since 0.1.0
         */
        protected static void ValidateCompilationPackageName(final Phase phase, final Compilation compilation)
                throws Error {

            // Assert a specified package name if the Compilation contains Pragmas
            if(compilation.definesPragmas() && !compilation.definesPackageName())
                PragmaAssert.MissingPackageName.Assert(phase, compilation);

        }

        /**
         * <p>Returns the native type string for the specified type.</p>
         * @param type The {@link Type} to retrieve the corresponding native type.
         * @return {@link String} value of the corresponding native type.
         * @since 0.1.0
         */
        protected static String NativeTypeStringFor(final Type type) {

            // Initialize the Type as a Primitive; This should already be checked.
            final PrimitiveType primitiveType = (PrimitiveType) type;

            // Return the result
            return (primitiveType.isStringType() ? "char*"
                    : (primitiveType.isBooleanType() ? "int" : primitiveType.toString()));

        }

        /**
         * <p>Returns the native type string for the specified {@link ParameterDeclaration}.</p>
         * @param parameterDeclaration The {@link ParameterDeclaration} to retrieve the corresponding native type.
         * @return {@link String} value of the corresponding native type.
         * @since 0.1.0
         */
        protected static String NativeTypeStringFor(final ParameterDeclaration parameterDeclaration) {

            return (parameterDeclaration != null) && (parameterDeclaration.getType() != null) ?
                    NativeTypeStringFor(parameterDeclaration.getType()) : "";

        }

        /**
         * <p>Returns the native parameter type list for the specified {@link ProcedureTypeDeclaration}.</p>
         * @param procedureTypeDeclaration The {@link ProcedureTypeDeclaration} to retrieve the corresponding native parameter type
         *                                 list.
         * @return {@link String} value of the corresponding native parameter type list.
         * @since 0.1.0
         */
        protected static String NativeTypeListStringFor(final ProcedureTypeDeclaration procedureTypeDeclaration) {

            // Initialize the StringBuilder & parameters
            final StringBuilder         stringBuilder   = new StringBuilder("(");
            final Sequence<ParameterDeclaration>   parameters      = procedureTypeDeclaration.getParameters();

            // Iterate through the list of Parameter Declarations
            for(int index = 0; index < parameters.size(); index++)
                stringBuilder.append(NativeTypeStringFor(parameters.child(index)))
                        .append(index == (parameters.size() - 1) ? "" : ", ");

            // Return the result
            return stringBuilder.append(")").toString();

        }

        /**
         * <p>Replaces all . (dots/periods) contained in the specified {@link String} with underscores, if any.</p>
         * @param string The {@link String} to replace all contained dots/periods
         * @return {@link String} value of the transformed {@link String} value.
         * @since 0.1.0
         */
        protected static String Flatten(final String string) {

            return (string == null) ? "" : string.replace(".", "_") + "_";

        }

        /**
         * <p>Constructs a C preprocessor directive checking for a definition of the specified string. This will
         * flatten the specified string and convert all the characters to uppercase.</p>
         * @param string The {@link String} to create the preprocessor directive with.
         * @return {@link String} value of the preprocessor directive.
         * @since 0.1.0
         */
        protected static String LibPreprocessorDirectiveOf(final String string) {

            // Initialize a transformed String
            final String flat = Flatten(string).toUpperCase();

            // Return the result
            return "#ifndef _LIB_" + flat + '\n' + "#defin _LIB_" + flat + '\n';

        }

        /**
         * <p>Constructs a C preprocessor directive checking for a definition of the specified string. This will
         * flatten the specified string and convert all the characters to uppercase.</p>
         * @param string The {@link String} to create the preprocessor directive with.
         * @return {@link String} value of the preprocessor directive.
         * @since 0.1.0
         */
        protected static String PreprocessorDirectiveOf(final String string) {

            // Initialize a transformed String
            final String flat = Flatten(string).toUpperCase();

            // Return the result
            return "#ifndef " + flat + '\n' + "#defin _LIB_" + flat + "H\n";

        }

        /**
         * <p>Attempts to generate a native library header file with the specified {@link String} value of the source file
         * name & {@link String} native library file name.</p>
         * @param sourceFilename The {@link String} value of the source file name.
         * @param includeFilename {@link String} native library file name.
         * @throws Error If the native library header file failed to write.
         * @since 0.1.0
         */
        protected static void GenerateHeaderFileFrom(final Phase phase,
                                                     final String sourceFilename,
                                                     final String includeFilename,
                                                     final List<String> signatures) throws Error {

            // Initialize the header file name
            final String headerFileName = sourceFilename + ".h";

            // Emit the informative message
            PragmaAssert.ToGenerateNativeLibraryHeaderFileWrite.Assert(phase, headerFileName);

            // Attempt to
            try {

                // Generate the file
                final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(headerFileName));

                // Write the contents
                bufferedWriter.write(LibPreprocessorDirectiveOf(headerFileName));
                bufferedWriter.write("#include<" + includeFilename + ">\n");

                // If the library is specified as NATIVE, generate the forward declarations
                // An empty signatures List will be specified for non-NATIVE specifications
                for(final String signature: signatures)
                    bufferedWriter.write(signature + ";\n\n");

                bufferedWriter.write("#endif\n\n");
                bufferedWriter.close();

                // Emit the informative message
                PragmaAssert.NativeLibraryHeaderFileWriteSuccess.Assert(phase, includeFilename);

                // Otherwise
            } catch(final IOException exception) {

                // Transform the error.
                PragmaAssert.NativeLibraryHeaderFileWriteFailure.Assert(phase, headerFileName);

            }

            // TODO: Potentially move the generated file to lib/C/include & update the success message to omit that spec

        }

        /**
         * <p>Attempts to generate a native library implementation file with the specified {@link String} value of the
         * source file name & {@link String} native library header file name.</p>
         * @param sourceFileName The {@link String} value of the source file name.
         * @throws Error If the native library source file failed to write.
         * @since 0.1.0
         */
        protected static void GenerateImplementationFileFrom(final Phase phase,
                                                             final String sourceFileName,
                                                             final List<String> signatures) throws Error {

            // Initialize the header file name
            final String implementationFileName = sourceFileName + ".c";
            final String headerFileName         = sourceFileName + ".h";

            // Emit the informative message
            ToGenerateNativeLibraryImplementationFile.Assert(phase, implementationFileName);

            // Attempt to
            try {

                // Generate the file
                final BufferedWriter file = new BufferedWriter(new FileWriter(implementationFileName));

                file.write(PreprocessorDirectiveOf(sourceFileName));
                file.write("#include \"" + headerFileName + "\"\n");

                // If the library is specified as NATIVE, generate the empty, concrete implementations
                // An empty signatures List will be specified for non-NATIVE specifications
                for(final String signature: signatures)
                    file.write(signature + "{\n\n    // Implementation code goes here.\n\n}\n\n");

                file.write("#endif\n\n");
                file.close();

                PragmaAssert.NativeLibraryImplementationFileWriteSuccess.Assert(phase,
                        implementationFileName, headerFileName);

                // Otherwise
            } catch(final IOException exception) {

                NativeLibraryImplementationFileWriteFailure.Assert(phase, headerFileName);

            }

            // TODO: Potentially move the generated file to lib/C/include & update the success message to omit that spec

        }

        /**
         * <p>Generates the native library code from the specified parameters.</p>
         * @param phase The invoking {@link Phase}.
         * @param nativeSignatures Any procedure signatures generated by the {@link Phase}.
         * @param packageName The package name corresponding to the current {@link Compilation}.
         * @param nativeLibraryFilename The filename corresponding to the native file name
         * @param processJHeaderFilename The filename corresponding to the processj header.
         * @param sourceFilename The filename corresponding to the generated implementation
         * @throws Error If the generation fails.
         * @since 0.1.0
         */
        protected static void GenerateNativeLibraryCodeFrom(final Phase phase,
                                                            final List<String> nativeSignatures,
                                                            final String packageName,
                                                            final String nativeLibraryFilename,
                                                            final String processJHeaderFilename,
                                                            final String sourceFilename) throws Error {

            // Print the Informative messages
            PragmaAssert.ToGenerateNativeLibrary.Assert(phase, nativeLibraryFilename);
            PragmaAssert.ToGenerateNativeLibraryHeaderProcessJHeaderFile.Assert(phase, processJHeaderFilename);

            // TODO: Maybe we want to retrieve strings for these and store them into the current ProcessJSource File object
            // TODO: for writing and/or processing to be handled elsewhere. This Phase should only concern itself with
            // TODO: Pragma validation & processing rather than file writing
            // Generate the files
            GenerateHeaderFileFrom(phase, sourceFilename, nativeLibraryFilename, nativeSignatures);
            GenerateImplementationFileFrom(phase, sourceFilename, nativeSignatures);

            // Print the last informative message
            PragmaAssert.NativeLibraryFileWriteSuccess.Assert(phase, processJHeaderFilename,
                    packageName + "/" + processJHeaderFilename + ".inc");

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of a Pragma that is being registered.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class EnteringPragma extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link EnteringPragma}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final Pragma pragma) {

                Error.Assert(FatalAssert.NullListener.class, phase, pragma);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The invalid {@link Pragma}</p>
             */
            private final Pragma pragma;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link EnteringPragma} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected EnteringPragma(final Phase culprit,
                                     final Pragma pragma) {
                super(culprit);

                this.pragma = pragma ;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an invalid {@link Pragma} declaration.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class IllegalPragma extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link IllegalPragma}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final Pragma pragma) {

                Error.Assert(FatalAssert.NullListener.class, phase, pragma);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The invalid {@link Pragma}</p>
             */
            private final Pragma pragma;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalPragma} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalPragma(final Phase culprit,
                                    final Pragma pragma) {
                super(culprit);

                this.pragma                 = pragma                ;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link Pragma} that was declared without an argument.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class PragmaRequiresArgument extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link PragmaRequiresArgument}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final Pragma pragma) {

                Error.Assert(FatalAssert.NullListener.class, phase, pragma);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The invalid {@link Pragma}</p>
             */
            private final Pragma pragma;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link PragmaRequiresArgument} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected PragmaRequiresArgument(final Phase culprit,
                                             final Pragma pragma) {
                super(culprit);

                this.pragma = pragma ;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link Pragma} that was declared with an argument.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class PragmaRequiresNoArgument extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link PragmaRequiresNoArgument}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final Pragma pragma) {

                Error.Assert(FatalAssert.NullListener.class, phase, pragma);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The invalid {@link Pragma}</p>
             */
            private final Pragma pragma;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link PragmaRequiresNoArgument} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected PragmaRequiresNoArgument(final Phase culprit,
                                               final Pragma pragma) {
                super(culprit);

                this.pragma = pragma ;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link Pragma} that was declared with an argument.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class PragmaAlreadySpecified extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link PragmaAlreadySpecified}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final Pragma pragma) {

                Error.Assert(FatalAssert.NullListener.class, phase, pragma);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The invalid {@link Pragma}</p>
             */
            private final Pragma pragma;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link PragmaAlreadySpecified} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected PragmaAlreadySpecified(final Phase culprit,
                                             final Pragma pragma) {
                super(culprit);

                this.pragma = pragma ;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ConstantDeclaration} was not declared native.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class ConstantDeclarationNonNative extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ConstantDeclarationNonNative}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ConstantDeclaration constantDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ConstantDeclaration} that was not declared native.</p>
             */
            private final ConstantDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ConstantDeclarationNonNative} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ConstantDeclarationNonNative(final Phase culprit,
                                                   final ConstantDeclaration constantDeclaration) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ConstantDeclaration} was declared native in conjunction
         * with a NATIVE {@link Pragma}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class IllegalConstantDeclarationNative extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link IllegalConstantDeclarationNative}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ConstantDeclaration constantDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ConstantDeclaration} that was declared native.</p>
             */
            private final ConstantDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalConstantDeclarationNative} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalConstantDeclarationNative(final Phase culprit,
                                                       final ConstantDeclaration constantDeclaration) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ConstantDeclaration} was initialized.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class ConstantDeclarationInitialized extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ConstantDeclarationInitialized}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ConstantDeclaration constantDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ConstantDeclaration} that was initialized.</p>
             */
            private final ConstantDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ConstantDeclarationInitialized} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ConstantDeclarationInitialized(final Phase culprit,
                                                     final ConstantDeclaration constantDeclaration) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ConstantDeclaration} that was declared native.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class ConstantDeclarationNative extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ConstantDeclarationNative}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ConstantDeclaration constantDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ConstantDeclaration} that was declared native.</p>
             */
            private final ConstantDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ConstantDeclarationNative} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ConstantDeclarationNative(final Phase culprit,
                                                final ConstantDeclaration constantDeclaration) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ConstantDeclaration} that was not initialized.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class ConstantDeclarationNotInitialized extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ConstantDeclarationNotInitialized}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ConstantDeclaration constantDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ConstantDeclaration} that was not initialized.</p>
             */
            private final ConstantDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ConstantDeclarationNotInitialized} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ConstantDeclarationNotInitialized(final Phase culprit,
                                                        final ConstantDeclaration constantDeclaration) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ProcedureTypeDeclaration} that did not define a body.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class ProcedureDeclarationDoesNotDefineBody extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ProcedureDeclarationDoesNotDefineBody}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ProcedureTypeDeclaration constantDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that was not initialized.</p>
             */
            private final ProcedureTypeDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ProcedureDeclarationDoesNotDefineBody} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ProcedureDeclarationDoesNotDefineBody(final Phase culprit,
                                                            final ProcedureTypeDeclaration constantDeclaration) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ProcedureTypeDeclaration} was declared native.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class ProcedureDeclarationNative extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ProcedureDeclarationNative}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ProcedureTypeDeclaration constantDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that was declared native.</p>
             */
            private final ProcedureTypeDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ProcedureDeclarationNative} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ProcedureDeclarationNative(final Phase culprit,
                                                 final ProcedureTypeDeclaration constantDeclaration) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ProcedureTypeDeclaration} was declared with a body.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class ProcedureDeclarationDefinesBody extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ProcedureDeclarationDefinesBody}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ProcedureTypeDeclaration constantDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that was declared native.</p>
             */
            private final ProcedureTypeDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ProcedureDeclarationDefinesBody} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ProcedureDeclarationDefinesBody(final Phase culprit,
                                                      final ProcedureTypeDeclaration constantDeclaration) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ProcedureTypeDeclaration} was not declared native.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class ProcedureDeclarationNonNative extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ProcedureDeclarationNonNative}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ProcedureTypeDeclaration constantDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that was declared native.</p>
             */
            private final ProcedureTypeDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ProcedureDeclarationNonNative} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ProcedureDeclarationNonNative(final Phase culprit,
                                                    final ProcedureTypeDeclaration constantDeclaration) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ProcedureTypeDeclaration} that does not specify a
         * primitive return type.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class IllegalProcedureReturnType extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link IllegalProcedureReturnType}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ProcedureTypeDeclaration constantDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that was declared native.</p>
             */
            private final ProcedureTypeDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalProcedureReturnType} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalProcedureReturnType(final Phase culprit,
                                                 final ProcedureTypeDeclaration constantDeclaration) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ProcedureTypeDeclaration} that specifies a barrier or timer
         * primitive return type.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class IllegalProcedureBarrierOrTimerReturnType extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link IllegalProcedureBarrierOrTimerReturnType}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ProcedureTypeDeclaration constantDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that was declared native.</p>
             */
            private final ProcedureTypeDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalProcedureBarrierOrTimerReturnType} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalProcedureBarrierOrTimerReturnType(final Phase culprit,
                                                               final ProcedureTypeDeclaration constantDeclaration) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ProcedureTypeDeclaration} that specifies at least one non
         * primitive parameter type.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class IllegalProcedureParameterType extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link IllegalProcedureParameterType}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ProcedureTypeDeclaration constantDeclaration, final Type type) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration, type);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that was declared native.</p>
             */
            private final ProcedureTypeDeclaration constantDeclaration;

            /**
             * <p>The specified {@link Type}.</p>
             */
            private final Type type;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalProcedureParameterType} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalProcedureParameterType(final Phase culprit,
                                                    final ProcedureTypeDeclaration constantDeclaration,
                                                    final Type type) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;
                this.type                = type;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ProcedureTypeDeclaration} that specifies at least one
         * barrier or timer primitive parameter type.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class IllegalProcedureBarrierOrTimerParameterType extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link IllegalProcedureBarrierOrTimerParameterType}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ProcedureTypeDeclaration constantDeclaration,
                                         final Type type) {

                Error.Assert(FatalAssert.NullListener.class, phase, constantDeclaration, type);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that was declared native.</p>
             */
            private final ProcedureTypeDeclaration constantDeclaration;

            /**
             * <p>The specified {@link Type}.</p>
             */
            private final Type type;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalProcedureBarrierOrTimerParameterType} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalProcedureBarrierOrTimerParameterType(final Phase culprit,
                                                                  final ProcedureTypeDeclaration constantDeclaration,
                                                                  final Type type) {
                super(culprit);

                this.constantDeclaration = constantDeclaration;
                this.type                = type;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a native library contains a {@link ProtocolTypeDeclaration}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class LibraryContainsProtocolDeclaration extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link LibraryContainsProtocolDeclaration}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final ProtocolTypeDeclaration protocolTypeDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, protocolTypeDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProtocolTypeDeclaration} that was declared.</p>
             */
            private final ProtocolTypeDeclaration protocolTypeDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link LibraryContainsProtocolDeclaration} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected LibraryContainsProtocolDeclaration(final Phase culprit,
                                                         final ProtocolTypeDeclaration protocolTypeDeclaration) {
                super(culprit);

                this.protocolTypeDeclaration = protocolTypeDeclaration;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a native library contains a {@link RecordTypeDeclaration}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class LibraryContainsRecordDeclaration extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link LibraryContainsRecordDeclaration}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final RecordTypeDeclaration recordTypeDeclaration) {

                Error.Assert(FatalAssert.NullListener.class, phase, recordTypeDeclaration);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link RecordTypeDeclaration} that was declared.</p>
             */
            private final RecordTypeDeclaration recordTypeDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link LibraryContainsRecordDeclaration} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected LibraryContainsRecordDeclaration(final Phase culprit,
                                                       final RecordTypeDeclaration recordTypeDeclaration) {
                super(culprit);

                this.recordTypeDeclaration = recordTypeDeclaration;

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of of missing language {@link Pragma}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class MissingLanguagePragma extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link MissingLanguagePragma}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase) {

                Error.Assert(FatalAssert.NullListener.class, phase);

            }

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link MissingLanguagePragma} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected MissingLanguagePragma(final Phase culprit) {
                super(culprit);
            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of of missing language {@link Pragma}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class MissingFilePragma extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link MissingFilePragma}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase) {

                Error.Assert(FatalAssert.NullListener.class, phase);

            }

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link MissingFilePragma} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected MissingFilePragma(final Phase culprit) {
                super(culprit);

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of NATIVE & NATIVELIB {@link Pragma}s
         * used in conjunction.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NativeAndNativeLib extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NativeAndNativeLib}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase) {

                Error.Assert(FatalAssert.NullListener.class, phase);

            }

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeAndNativeLib} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NativeAndNativeLib(final Phase culprit) {
                super(culprit);

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of PROCESSJ specified for {@link Pragma}s
         * NATIVE or NATIVELIB.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NativeWithProcessJ extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NativeWithProcessJ}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase) {

                Error.Assert(FatalAssert.NullListener.class, phase);

            }

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeWithProcessJ} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NativeWithProcessJ(final Phase culprit) {
                super(culprit);

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information for a invalid native language.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class InvalidNativeLanguage extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link InvalidNativeLanguage}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase) {

                Error.Assert(FatalAssert.NullListener.class, phase);

            }

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link InvalidNativeLanguage} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected InvalidNativeLanguage(final Phase culprit) {
                super(culprit);

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of a native library header file write
         * failure.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NativeLibraryHeaderFileWriteFailure extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NativeLibraryHeaderFileWriteFailure}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final String headerFileName) {

                Error.Assert(FatalAssert.NullListener.class, phase, headerFileName);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The header filename of the file that failed to write.</p>
             */
            private final String headerFileName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeLibraryHeaderFileWriteFailure} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NativeLibraryHeaderFileWriteFailure(final Phase culprit, final String headerFileName) {
                super(culprit);

                this.headerFileName = headerFileName;

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of a native implementation file write
         * failure.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NativeLibraryImplementationFileWriteFailure extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NativeLibraryImplementationFileWriteFailure}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final String headerFileName) {

                Error.Assert(FatalAssert.NullListener.class, phase, headerFileName);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The header filename of the file that failed to write.</p>
             */
            private final String headerFileName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeLibraryHeaderFileWriteFailure} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NativeLibraryImplementationFileWriteFailure(final Phase culprit, final String headerFileName) {
                super(culprit);

                this.headerFileName = headerFileName;

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of missing {@link List} of native
         * signatures.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class InvalidNativeSignatures extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link InvalidNativeSignatures}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final Compilation compilation) {

                Error.Assert(FatalAssert.NullListener.class, phase, compilation);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link Compilation} with invalid signatures.</p>
             */
            private final Compilation compilation;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link InvalidNativeSignatures} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected InvalidNativeSignatures(final Phase culprit, final Compilation compilation) {
                super(culprit);

                this.compilation = compilation;

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of missing package name from a
         * {@link Compilation}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class MissingPackageName extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link MissingPackageName}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final Compilation compilation) {

                Error.Assert(FatalAssert.NullListener.class, phase, compilation);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link Compilation} with invalid signatures.</p>
             */
            private final Compilation compilation;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link MissingPackageName} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected MissingPackageName(final Phase culprit, final Compilation compilation) {
                super(culprit);

                this.compilation = compilation;

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information detected {@link Pragma}s.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class LibraryPragmaDetected extends Info {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link LibraryPragmaDetected}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase) {

                Info.Assert(PragmaAssert.LibraryPragmaDetected.class, phase);

            }

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link LibraryPragmaDetected} to its default state.</p>
             * @param culprit The {@link Phase} instance that emitted the message.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected LibraryPragmaDetected(final Phase culprit) {
                super(culprit);
            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information native library generation.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class ToGenerateNativeLibrary extends Info {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ToGenerateNativeLibrary}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final String filename) {

                Info.Assert(PragmaAssert.ToGenerateNativeLibrary.class, phase, filename);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The filename that is to be generated.</p>
             */
            private final String filename;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ToGenerateNativeLibrary} to its default state.</p>
             * @param culprit The {@link Phase} instance that emitted the message.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ToGenerateNativeLibrary(final Phase culprit, final String filename) {
                super(culprit);

                this.filename = filename;

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of a native library header file generation.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class ToGenerateNativeLibraryHeaderProcessJHeaderFile extends Info {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ToGenerateNativeLibraryHeaderProcessJHeaderFile}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final String headerFileName) {

                Info.Assert(PragmaAssert.ToGenerateNativeLibraryHeaderProcessJHeaderFile.class, phase, headerFileName);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The header filename to be generated.</p>
             */
            private final String headerFilename;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ToGenerateNativeLibraryHeaderProcessJHeaderFile} to its default state.</p>
             * @param culprit The {@link Phase} instance that emitted the message.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ToGenerateNativeLibraryHeaderProcessJHeaderFile(final Phase culprit, final String headerFilename) {
                super(culprit);

                this.headerFilename = headerFilename;

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of a native library header file write.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class ToGenerateNativeLibraryHeaderFileWrite extends Info {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ToGenerateNativeLibraryHeaderFileWrite}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final String headerFileName) {

                Info.Assert(PragmaAssert.ToGenerateNativeLibraryHeaderFileWrite.class, phase, headerFileName);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The native header filename that was written.</p>
             */
            private final String headerFileName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ToGenerateNativeLibraryHeaderFileWrite} to its default state.</p>
             * @param culprit The {@link Phase} instance that emitted the message.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ToGenerateNativeLibraryHeaderFileWrite(final Phase culprit, final String headerFileName) {
                super(culprit);

                this.headerFileName = headerFileName;

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of a native library implementation file generation.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class ToGenerateNativeLibraryImplementationFile extends Info {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ToGenerateNativeLibraryImplementationFile}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final String headerFileName) {

                Info.Assert(PragmaAssert.ToGenerateNativeLibraryImplementationFile.class, phase, headerFileName);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The native header filename that was written.</p>
             */
            private final String headerFileName;


            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ToGenerateNativeLibraryImplementationFile} to its default state.</p>
             * @param culprit The {@link Phase} instance that emitted the message.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ToGenerateNativeLibraryImplementationFile(final Phase culprit, final String headerFileName) {
                super(culprit);

                this.headerFileName = headerFileName;

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of a native library header
         * file write success.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NativeLibraryHeaderFileWriteSuccess extends Info {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NativeLibraryHeaderFileWriteSuccess}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final String headerFileName) {

                Info.Assert(PragmaAssert.NativeLibraryHeaderFileWriteSuccess.class, phase, headerFileName);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The filename of the written header file.</p>
             */
            private final String headerFileName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeLibraryHeaderFileWriteSuccess} to its default state.</p>
             * @param culprit The {@link Phase} instance that emitted the message.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NativeLibraryHeaderFileWriteSuccess(final Phase culprit, final String headerFileName) {
                super(culprit);

                this.headerFileName = headerFileName;

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of a native library implementation
         * file write success.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NativeLibraryImplementationFileWriteSuccess extends Info {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NativeLibraryImplementationFileWriteSuccess}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final String headerFileName, final String implementationFileName) {

                Info.Assert(PragmaAssert.NativeLibraryImplementationFileWriteSuccess.class, phase);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The filename of the written header file.</p>
             */
            private final String headerFileName;

            /**
             * <p>The filename of the written implementation file.</p>
             */
            private final String implementationFileName;


            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeLibraryImplementationFileWriteSuccess} to its default state.</p>
             * @param culprit The {@link Phase} instance that emitted the message.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NativeLibraryImplementationFileWriteSuccess(final Phase culprit, final String headerFileName,
                                                                  final String implementationFileName) {
                super(culprit);

                this.headerFileName             = headerFileName;
                this.implementationFileName     = implementationFileName;

            }

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of a native library file write success.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NativeLibraryFileWriteSuccess extends Info {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NativeLibraryFileWriteSuccess}.</p>
             * @param phase The invoking {@link Phase}.
             */
            protected static void Assert(final Phase phase, final String headerFilename, final String filepath) {

                Info.Assert(PragmaAssert.NativeLibraryFileWriteSuccess.class, phase, headerFilename, filepath);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The header file name that was successfully written.</p>
             */
            private final String headerFilename;

            /**
             * <p>The path where the header file was written.</p>
             */
            private final String filepath;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeLibraryFileWriteSuccess} to its default state.</p>
             * @param culprit The {@link Phase} instance that emitted the message.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NativeLibraryFileWriteSuccess(final Phase culprit, final String headerFilename,
                                                    final String filepath) {
                super(culprit);

                this.headerFilename = headerFilename    ;
                this.filepath       = filepath          ;

            }

        }

    }

    protected static class DeclarationAssert {

        /// ------------------------
        /// Protected Static Methods

        protected static boolean MobileProcedureNotOverloaded(final Phase phase,
                                                              final Type type,
                                                              final ProcedureTypeDeclaration procedureTypeDeclaration)
                throws Phase.Error {

            // Initialize the result
            final boolean found = (type instanceof ProcedureTypeDeclaration);

            // Assert for all entries, if a Procedure Type was found, the existing Procedure Type is not
            // declared 'mobile'.
            if(found && ((ProcedureTypeDeclaration) type).isMobile())
                MobileProcedureOverloaded.Assert(phase, procedureTypeDeclaration);

            // Return the result
            return found;

        }

        protected static boolean NotOverloadingNonMobileProcedure(final Phase phase,
                                                                  final Type type,
                                                                  final ProcedureTypeDeclaration procedureTypeDeclaration)
                throws Phase.Error {

            // Initialize the result
            final boolean found = (type instanceof ProcedureTypeDeclaration);

            // Assert for all entries, if a Procedure Type was found, the specified Procedure Type
            // is not declared 'mobile'.
            if(found && procedureTypeDeclaration.isMobile())
                NonMobileProcedureTypeDefined.Assert(phase, type);

            // Return the result
            return found;

        }

        protected static void MobileProcedureSpecifiesNonVoidReturnType(final Phase phase,
                                                                        final ProcedureTypeDeclaration procedureTypeDeclaration)
                throws Phase.Error {

            if(procedureTypeDeclaration.isMobile() && !procedureTypeDeclaration.getReturnType().isVoidType())
                MobileProcedureSpecifiesNonVoidReturnType.Assert(phase, procedureTypeDeclaration);

        }

        protected static void Declares(final Phase phase,
                                       final Type type)
                throws Phase.Error {

            if(!phase.getScope().put(type.toString(), type))
                TypeDefined.Assert(phase, type);

        }

        protected static void Declares(final Phase phase,
                                       final ProcedureTypeDeclaration procedureTypeDeclaration)
                throws Phase.Error {

            // Assert that if the Procedure is specified as 'mobile', it also specifies a void
            // return Type
            MobileProcedureSpecifiesNonVoidReturnType(phase, procedureTypeDeclaration);

            // Initialize a handle to the current scope & retrieve the result
            final Context.SymbolMap     scope   = phase.getScope();
            final List<Object>  result  = scope.get(procedureTypeDeclaration.toString());

            // If the procedure has not been defined
            if(!result.isEmpty()) {

                // Initialize a handle to the result
                final Object preliminary = result.get(0);

                // Assert for all overloads, the Procedure Type or the existing Type are not declared 'mobile'
                if(!(preliminary instanceof Context.SymbolMap))
                    NonProcedureTypeDefined.Assert(phase, procedureTypeDeclaration);

                // Assert for all existing Procedure Types, the Procedure Type of the existing Type are not declared
                // 'mobile'
                scope.forEachEntryUntil(type -> MobileProcedureNotOverloaded(phase, (Type) type, procedureTypeDeclaration)
                        || NotOverloadingNonMobileProcedure(phase, (Type) type, procedureTypeDeclaration));

                // Cast the result
                final Context.SymbolMap procedures = (Context.SymbolMap) result;

                // Assert the procedure definition is unique
                if(!procedures.put(procedureTypeDeclaration.getSignature(), procedureTypeDeclaration))
                    TypeDefined.Assert(phase, procedureTypeDeclaration);

                // Otherwise
            } else {

                // Initialize a new Context.SymbolMap
                final Context.SymbolMap symbolMap = new Context.SymbolMap();

                // Insert the Procedure
                symbolMap.put(procedureTypeDeclaration.getSignature(), procedureTypeDeclaration);

                // Emplace the entry
                scope.put(procedureTypeDeclaration.toString(), procedureTypeDeclaration);

            }

        }

        protected static void Defines(final Phase phase, final AST ast) throws Error {

            if(!phase.getScope().put(ast.toString(), ast))
                NameAssert.NameDefined.Assert(phase, ast);

        }

        protected static void DefinesLabel(final Phase phase,
                                           final String label,
                                           final Statement statement) throws Error {

            if(!phase.getScope().put(label, statement))
                NameAssert.LabelDefined.Assert(phase, statement);

        }

        /// -----------
        /// Phase.Error

        /**
         * <p>{@link Error} to be emitted if the {@link Type}'s {@link Name} is already defined in the
         * current scope.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class TypeDefined extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link TypeDefined} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(TypeDefined.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link TypeDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected TypeDefined(final Phase culprit,
                                  final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Error} to be emitted if the {@link Type}'s {@link Name} is already defined in the
         * current scope as a non-procedure {@link Type}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NonProcedureTypeDefined extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NonProcedureTypeDefined} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(NonProcedureTypeDefined.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NonProcedureTypeDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NonProcedureTypeDefined(final Phase culprit,
                                              final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Error} to be emitted if the {@link Type}'s {@link Name} is already defined in the
         * current scope as a non-mobile procedure {@link Type}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NonMobileProcedureTypeDefined extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NonMobileProcedureTypeDefined} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(NonMobileProcedureTypeDefined.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NonMobileProcedureTypeDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NonMobileProcedureTypeDefined(final Phase culprit, final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Error} to be emitted if the {@link Type}'s {@link Name} is already defined in the
         * current scope.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class MobileProcedureOverloaded extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link MobileProcedureOverloaded} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(MobileProcedureOverloaded.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link MobileProcedureOverloaded} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected MobileProcedureOverloaded(final Phase culprit,
                                                final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Error} to be emitted if the {@link ProcedureTypeDeclaration} is mobile procedure with a specified
         * non-void return {@link Type}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class MobileProcedureSpecifiesNonVoidReturnType extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link MobileProcedureSpecifiesNonVoidReturnType} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(MobileProcedureSpecifiesNonVoidReturnType.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link MobileProcedureSpecifiesNonVoidReturnType} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected MobileProcedureSpecifiesNonVoidReturnType(final Phase culprit,
                                                                final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

    }

    protected static class ReachabilityAssert {

        protected static Set<String> WithFailures(final Class<?>... classes) {

            return Arrays.stream(classes).map(Class::getName).collect(Collectors.toSet());

        }

        protected static boolean NotEnclosedInParallelOrChoiceContext(final Phase phase,
                                                                      final AST construct) throws Phase.Error {

            // Initialize a handle to the Context
            final Context enclosingContext = phase.getContext().forEachContextRetrieve(
                    context -> (context instanceof ParBlock) || (context instanceof AltStatement));

            // Initialize a handle to the result
            final boolean enclosingContextParallelOrChoice = enclosingContext != null;

            // Assert the construct is not enclosed in a parallel Context
            if(enclosingContext instanceof ParBlock)
                EnclosedInParallelContext.Assert(phase, construct, phase.getContext());

            // Assert the construct is not enclosed in a choice Context
            if(enclosingContext instanceof AltStatement)
                EnclosedInParallelContext.Assert(phase, construct, phase.getContext());

            // Return the result
            return !enclosingContextParallelOrChoice;

        }

        @SafeVarargs
        protected static void EnclosingIterativeContextBreaksAndReachable(final Phase phase,
                                                                          final Statement statement,
                                                                          final Set<String>... failures)
                throws Phase.Error {

            // Initialize a handle to the Context
            final Context enclosingContext = phase.getContext().forEachContextRetrieve(
                    context -> (context instanceof ConditionalContext)
                            || (context instanceof ParBlock));

            // Assert the statement's enclosing Context isn't parallel
            if(enclosingContext instanceof ParBlock)
                EnclosedInParallelContext.Assert(phase, statement, enclosingContext);

            // Initialize a handle to the result
            final boolean enclosingIterativeContextBreaks = enclosingContext != null;

            // Assert the Statement is enclosed in a Breakable Context
            if(!enclosingIterativeContextBreaks)
                NotEnclosedInIterativeContext.Assert(phase, statement, phase.getContext());

                // Assert that the enclosing Context does not define a constant true evaluation expression
            else if(TypeAssert.IsConstantTrue(
                    ((ConditionalContext) enclosingContext).getEvaluationExpression()))
                EnclosingIterativeContextDoesNotTerminate.Assert(phase, statement, enclosingContext);

                // Assert that the enclosing Context does not define a constant false evaluation expression
            else if(TypeAssert.IsConstantFalse(
                    ((ConditionalContext) enclosingContext).getEvaluationExpression()))
                EnclosingIterativeContextIsNotReachable.Assert(phase, statement, enclosingContext);

        }

        protected static boolean DoesNotContainHaltingProcedures(final Phase phase,
                                                                 final BlockStatement blockStatement)
                throws Phase.Error {

            // Initialize a handle to the Statements
            final Sequence<? extends Statement> statements = blockStatement.getStatements();

            // Initialize the preliminary result
            boolean haltsBeforeCompletion = false;

            // Iterate

            int index = 0; for(;!haltsBeforeCompletion && (index < statements.size()); index++)
                haltsBeforeCompletion = (statements.child(index) instanceof StopStatement)
                        && ((index < statements.size() - 1));

            // Assert that if the Context contains a Stop Statement, it's the last one
            if(!haltsBeforeCompletion)
                ContextDoesNotTerminate.Assert(phase, statements.child(index), blockStatement);

            return haltsBeforeCompletion;

        }

        protected static boolean ConditionalContextReachable(final Phase phase,
                                                             final IfStatement ifStatement)
                throws Phase.Error {

            if(TypeAssert.IsConstantTrue(ifStatement.getEvaluationExpression()))
                BranchConditionalContextNotReachable.Assert(phase, ifStatement);

            if(TypeAssert.IsConstantFalse(ifStatement.getEvaluationExpression()))
                ConditionalContextNotReachable.Assert(phase, ifStatement);

            return true;

        }

        /// -----------
        /// Phase.Error

        /**
         * <p>{@link Phase.Error} to be emitted if the conditional {@link Context}'s
         * alternative {@link Context} is not reachable.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class BranchConditionalContextNotReachable extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link BranchConditionalContextNotReachable} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Phase.Error.Assert(BranchConditionalContextNotReachable.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ConditionalContextNotReachable} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected BranchConditionalContextNotReachable(final Phase culprit,
                                                           final AST instance) {
                super(culprit);

                this.instance = instance    ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the conditional {@link Context}
         * is not reachable.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ConditionalContextNotReachable extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ConditionalContextNotReachable} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Phase.Error.Assert(ConditionalContextNotReachable.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ConditionalContextNotReachable} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected ConditionalContextNotReachable(final Phase culprit,
                                                     final AST instance) {
                super(culprit);

                this.instance = instance    ;
                this.instanceClass = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the {@link Context} does not
         * terminate.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ContextDoesNotTerminate extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ContextDoesNotTerminate} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final Context context) {

                Phase.Error.Assert(ContextDoesNotTerminate.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link Context}'s class object.</p>
             */
            private final Class<? extends Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link Context} instance.</p>
             */
            private final Context context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ContextDoesNotTerminate} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected ContextDoesNotTerminate(final Phase culprit,
                                              final AST instance,
                                              final Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an {@link AST} is enclosed in a choice
         * {@link Context}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class EnclosedInChoiceContext extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link EnclosedInChoiceContext} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final Context context) {

                Phase.Error.Assert(EnclosedInChoiceContext.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link Context}'s class object.</p>
             */
            private final Class<? extends Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link Context} instance.</p>
             */
            private final Context context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link EnclosedInChoiceContext} to its' default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected EnclosedInChoiceContext(final Phase culprit,
                                              final AST instance,
                                              final Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an {@link AST} is enclosed in a parallel
         * {@link Context}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class EnclosedInParallelContext extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link EnclosedInParallelContext} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final Context context) {

                Phase.Error.Assert(EnclosedInParallelContext.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link Context}'s class object.</p>
             */
            private final Class<? extends Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link Context} instance.</p>
             */
            private final Context context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link EnclosedInParallelContext} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected EnclosedInParallelContext(final Phase culprit,
                                                final AST instance,
                                                final Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the enclosing {@link Context}
         * does not terminate.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class EnclosingIterativeContextDoesNotTerminate extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link EnclosingIterativeContextDoesNotTerminate} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final Context context) {

                Phase.Error.Assert(EnclosingIterativeContextDoesNotTerminate.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link Context}'s class object.</p>
             */
            private final Class<? extends Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link Context} instance.</p>
             */
            private final Context context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link EnclosingIterativeContextDoesNotTerminate} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected EnclosingIterativeContextDoesNotTerminate(final Phase culprit,
                                                                final AST instance,
                                                                final Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the enclosing {@link Context}
         * is not reachable.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class EnclosingIterativeContextIsNotReachable extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link EnclosingIterativeContextIsNotReachable} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final Context context) {

                Phase.Error.Assert(EnclosingIterativeContextIsNotReachable.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link Context}'s class object.</p>
             */
            private final Class<? extends Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link Context} instance.</p>
             */
            private final Context context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link EnclosingIterativeContextIsNotReachable} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected EnclosingIterativeContextIsNotReachable(final Phase culprit,
                                                              final AST instance,
                                                              final Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an {@link AST} is not enclosed in an iterative
         * {@link Context}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NotEnclosedInIterativeContext extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NotEnclosedInIterativeContext} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final Context context) {

                Phase.Error.Assert(NotEnclosedInIterativeContext.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link Context}'s class object.</p>
             */
            private final Class<? extends Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link Context} instance.</p>
             */
            private final Context context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link EnclosedInParallelContext} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected NotEnclosedInIterativeContext(final Phase culprit,
                                                    final AST instance,
                                                    final Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an {@link AST} is not enclosed in an breakable
         * {@link Context}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NotEnclosedInBreakableContext extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NotEnclosedInBreakableContext} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final Context context) {

                Phase.Error.Assert(NotEnclosedInIterativeContext.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link Context}'s class object.</p>
             */
            private final Class<? extends Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link Context} instance.</p>
             */
            private final Context context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NotEnclosedInBreakableContext} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected NotEnclosedInBreakableContext(final Phase culprit,
                                                    final AST instance,
                                                    final Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

    }

    // TODO: Tidy
    protected static class SemanticAssert {

        protected static void SetEnclosingContextYields(final Phase phase)
                throws Phase.Error {

            phase.getContext().forEachContextUntil(Context::setYields);

        }

        protected static void PriAltNotEnclosedByAltStatement(final Phase phase,
                                                              final AltStatement altStatement)
                throws Phase.Error {

            phase.getContext().forEachContextUntil(context -> {

                final boolean found = context instanceof AltStatement;

                // Assert that if the Context is an Alt Statement and the specified Alt Statement
                // is pri, that the Context is not a non-pri Alt Statement
                if(found && (!((AltStatement) context).isPri() && altStatement.isPri()))
                    PJBugManager.INSTANCE.reportMessage(
                            new PJMessage.Builder()
                                    .addAST(altStatement)
                                    .addError(VisitorMessageNumber.REWRITE_1006)
                                    .build());

                return found;

            });

        }

        protected static void SingleInitializationForReplicatedAlt(final AltStatement altStatement)
                throws Phase.Error {

            // Assert that if the Alt Statement is replicated, it does not define multiple initialization
            // statements.
            if(altStatement.isReplicated() && altStatement.definesMultipleInitializationStatements())
                PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                        .addAST(altStatement)
                        .addError(VisitorMessageNumber.SEMATIC_CHECKS_901)
                        .build());

        }

        protected static void NotReplicatedAltInputGuardWriteExpression(final Phase phase,
                                                                        final Expression expression)
                throws Phase.Error {

            phase.getContext().forEachContextUntil(context -> {

                final boolean found = context instanceof AltCase;

                // Assert the Context is not an Alt Case & does not define an Input Guard
                if(found && ((AltCase) context).definesInputGuardExpression()) {

                    // Initialize a handle to the input guard Expression's Channel Read Expression
                    final AssignmentExpression inputGuardExpression = ((AltCase) context).getInputGuardExpression();

                    // Assert the Channel Read's Expression is not the specified Expression
                    if((inputGuardExpression != null) && inputGuardExpression.getRightExpression() == expression) {

                        // Assert any enclosing alt statements are not replicated alts
                        context.forEachEnclosingContext(outerContext -> {
                            if((outerContext instanceof AltStatement) && ((AltStatement) outerContext).isReplicated())
                                PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                        .addAST(expression)
                                        .addError(VisitorMessageNumber.SEMATIC_CHECKS_902)
                                        .build());
                        });

                    }

                }

                return found;

            });

        }

        protected static void InParBlockReadSet(final Phase phase,
                                                final Expression expression)
                throws Phase.Error {

            phase.getContext().forEachContextUntil(context -> {

                // TODO: Errors 701, 702, 703, 704, 705, 706, 707
                final boolean foundPar = (context instanceof ParBlock);

                // Check the result
                if(foundPar) {

                    // Initialize a handle to the ParBlock
                    final ParBlock parBlock = (ParBlock) context;

                    // Assert the Expression is not in the Par Block's Write Set
                    if(parBlock.inWriteSet(expression))
                        PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(expression)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_701)
                                .addArguments(expression)
                                .build());

                    // Aggregate the Expression to the read set
                    parBlock.toReadSet(expression);
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(expression)
                            .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_702)
                            .addArguments(expression)
                            .build());

                }

                return foundPar;

            });

        }

        protected static void InParBlockWriteSet(final Phase phase,
                                                 final Expression expression)
                throws Phase.Error {

            phase.getContext().forEachContextUntil(context -> {

                // TODO: Errors 701, 702
                final boolean foundPar = (context instanceof ParBlock);

                // Check the result
                if(foundPar) {

                    // Initialize a handle to the ParBlock
                    final ParBlock parBlock = (ParBlock) context;

                    // Assert the Expression is not in the Par Block's Write Set
                    if(parBlock.inWriteSet(expression))
                        PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(expression)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_701)
                                .addArguments(expression)
                                .build());

                    // Aggregate the Expression to the read set
                    parBlock.toWriteSet(expression);
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(expression)
                            .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECKER_702)
                            .addArguments(expression)
                            .build());

                }

                return foundPar;

            });

        }

        protected static void NotInLiteralExpression(final Phase phase,
                                                     final ChannelReadExpression channelReadExpression)
                throws Phase.Error {

            phase.getContext().forEachContextUntil(context -> {

                final boolean found = context instanceof LiteralExpression;

                // Assert the Context is not a Literal
                if(context instanceof LiteralExpression)
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(channelReadExpression)
                            .addError(VisitorMessageNumber.SEMATIC_CHECKS_900)
                            .addArguments(channelReadExpression)
                            .build());

                return found;

            });

        }

        protected static void NonYieldingPrecondition(final Phase phase,
                                                      final ChannelReadExpression channelReadExpression)
                throws Phase.Error{

            phase.getContext().getScope().forEachEntryUntil(context -> {

                final boolean found = context instanceof AltCase;

                // If the Context is an AltCase
                if(found) {

                    // Initialize a handle to the AltCase
                    final AltCase altCase = (AltCase) context;

                    // Assert the Alt Case does not define a yielding precondition
                    if(altCase.definesPrecondition()
                            && altCase.getPreconditionExpression().doesYield())
                        PJBugManager.INSTANCE.reportMessage(
                                new PJMessage.Builder()
                                        .addAST(channelReadExpression)
                                        .addError(VisitorMessageNumber.REWRITE_1001)
                                        .build());

                }

                return found;

            });

        }

        protected static void NotPreconditionExpression(final Phase phase,
                                                        final Expression expression)
                throws Phase.Error {

            phase.getContext().forEachContextUntil(context -> {

                final boolean found = context instanceof AltCase;

                // If the Context is an AltCase
                if(found) {

                    // Initialize a handle to the AltCase
                    final AltCase altCase = (AltCase) context;

                    // Assert we're not the AltCase's precondition
                    if (altCase.definesPrecondition()
                            && altCase.getPreconditionExpression() == expression)
                        PJBugManager.INSTANCE.reportMessage(
                                new PJMessage.Builder()
                                        .addAST(expression)
                                        .addError(VisitorMessageNumber.REWRITE_1001)
                                        .build());

                }

                return found;

            });

        }

        protected static void NotInAltStatement(final Phase phase,
                                                final Statement statement)
                throws Phase.Error {

            phase.getContext().getScope().forEachEntryUntil(context -> {

                final boolean found = context instanceof AltStatement;

                // Assert the Context is not an Alt Statement
                if(found)
                    PJBugManager.INSTANCE.reportMessage(
                            new PJMessage.Builder()
                                    .addAST(statement)
                                    .addError(VisitorMessageNumber.SEMATIC_CHECKS_903)
                                    .build());

                return found;

            });

        }

        protected static boolean NotEmptyParallelContext(final Phase phase) throws Phase.Error {

            // Initialize a handle to the Context
            final Context context = phase.getContext();

            // Initialize the result
            final boolean isParallelContextEmpty = (context instanceof ParBlock)
                    && ((ParBlock) context).getBody().getStatements().isEmpty();

            // Assert the parallel Context is not empty
            if(isParallelContextEmpty)
                ParallelContextEmpty.Assert(phase, context);

            // Return the result
            return isParallelContextEmpty;

        }

        protected static void VisibleToEnclosingParFor(final Phase phase,
                                                       final Expression expression) throws Error {

            phase.getContext().forEachContextUntil(context -> {

                final boolean found = (context instanceof ForStatement) && ((ForStatement) context).isPar();

                if(found) ((ForStatement) context).vars.add(expression);

                return found;

            });

        }

        /// -----------
        /// Phase.Error

        /**
         * <p>{@link Phase.Error} to be emitted if a parallel {@link Context}
         * contains an empty body.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ParallelContextEmpty extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ParallelContextEmpty} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param context The {@link AST}'s enclosing {@link Context}.
             */
            protected static void Assert(final Phase phase, final Context context) {

                Phase.Error.Assert(ParallelContextEmpty.class, phase, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link AST}'s enclosing {@link Context}'s class object.</p>
             */
            private final Class<? extends Context>    contextClass    ;

            /**
             * <p>The enclosing {@link Context} instance.</p>
             */
            private final Context context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ParallelContextEmpty} to its' default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected ParallelContextEmpty(final Phase culprit,
                                           final Context context) {
                super(culprit);

                this.context      = context                                         ;
                this.contextClass = (context != null) ? context.getClass() : null   ;

            }

        }

    }

    /// ------------
    /// Import Phase

    /**
     * <p>Defines the import assertions that validate a {@link Compilation}'s import statements have been bound to a
     * {@link SourceFile} and are accessible from the importing {@link Compilation}'s set of import symbols in addition
     * to the appropriate {@link Phase.Info}, {@link Phase.Warning}, & {@link Phase.Error} messages.</p>
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 1.0.0
     * @see Compilation
     * @see SourceFile
     */
    public static class ImportAssert {

        /// --------------------
        /// Public Static Fields

        /**
         * <p>Functional interface field that provides a method to the {@link ImportAssert} class that retrieves a
         * {@link List} of files that match the specified {@link String} path.</p>
         * @since 1.0.0
         * @see ImportAssert
         */
        public static Retrieve Retrieve = null;

        /// ------------------------
        /// Protected Static Methods

        /**
         * <p>Attempts to import the {@link Compilation} corresponding with the specified package name
         * {@link String} into the specified {@link Phase}'s import symbols.</p>
         * @param packageName The {@link String} value of the fully-qualified package name corresponding with the
         *                    {@link Compilation} to import.
         * @throws AmbiguousImportFile If more than one {@link SourceFile} was found & the {@link String} package name
         *                             was specified as a regular import.
         * @throws FileNotFound If no {@link SourceFile}s were found & the {@link String} package name was specified
         *                      as a regular import.
         * @throws FileNotFoundInPackage If no matching {@link SourceFile}s were found in the specified package name
         * @throws PackageNotFound If no {@link SourceFile}s were found & the {@link String} package name was specified
         *                         as a wildcard import.
         * @since 1.0.0
         * @see Compilation
         * @see String
         * @see AmbiguousImportFile
         * @see FileNotFoundInPackage
         * @see FileNotFound
         * @see PackageNotFound
         */
        protected static void ImportSpecified(final Phase phase, final String packageName) throws Phase.Error {

            // Initialize a handle to the package name & the matching source files
            final String            path        = packageName.replace('.', '/');
            final List<SourceFile>  sourceFiles = Retrieve.MatchingSourceFilesAt(path, phase.getListener());

            // Assert the import yielded files
            if((packageName.lastIndexOf("*") != -1) && sourceFiles.isEmpty())
                PackageNotFound.Assert(phase, packageName);

            // Otherwise
            else {

                // Assert at least one file was found
                if(sourceFiles.isEmpty()) FileNotFound.Assert(phase, packageName);

                // Assert Strictly one file was found
                else if (sourceFiles.size() != 1) AmbiguousImportFile.Assert(phase, packageName, sourceFiles);

            }

            final Context importContext = phase.getContext();

            // Iterate through each Source File
            for(final SourceFile sourceFile: sourceFiles) {

                // Initialize a handle to the corresponding Compilation
                final Compilation compilation = sourceFile.getCompilation();

                // Clear out the Context
                phase.setContext(null);

                // Assert resolved imports
                compilation.accept(phase);

                // Emplace the Imported Compilation's Scope into the current Compilation's Import Symbols
                phase.getContext().getScope().putImport(packageName, compilation.getScope());

            }

        }

        /// -----------
        /// Phase.Error

        /**
         * <p>A {@link Phase.Error} to be emitted if an import with a regular {@link String} package name yielded more
         * than one {@link SourceFile}.</p>
         * @author Carlos L. Cuenca
         * @version 1.0.0
         * @since 1.0.0
         * @see SourceFile
         * @see Phase
         * @see Phase.Error
         */
        private final static class AmbiguousImportFile extends Phase.Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>The integer value corresponding to the {@link AmbiguousImportFile}'s message index.</p>
             * @since 1.0.0
             */
            private final static int    MessageIndex    = 101                   ;

            /**
             * <p>The suffix aggregated to the end of each file path.</p>
             * @since 1.0.0
             * @see String
             */
            private final static String FilePathPrefix  = " ".repeat(8)         ;

            /**
             * <p>The suffix aggregated to the end of each file path.</p>
             * @since 1.0.0
             * @see String
             */
            private final static String FilePathSuffix  = ",\n"                 ;

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link AmbiguousImportFile} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param culprit The {@link Phase} instance that emitted the {@link AmbiguousImportFile}.
             * @param packageName The {@link String} value of the package name attempted to import.
             * @param foundFiles The {@link List} of found {@link SourceFile}s.
             * @since 1.0.0
             * @see SourceFile
             * @see Phase
             * @see String
             */
            private static void Assert(final Phase culprit, final String packageName,
                                       final List<SourceFile> foundFiles) {

                // Notify the culprit's Listener of the error
                culprit.getListener().notify(new AmbiguousImportFile(culprit, packageName, foundFiles));

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} error message constructed by the {@link AmbiguousImportFile}.</p>
             * @since 1.0.0
             */
            private final String message;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link AmbiguousImportFile} to its default state.</p>
             * @param culprit The {@link Phase} instance that emitted the {@link AmbiguousImportFile}.
             * @param packageName The {@link String} value of the package name attempted to import.
             * @param foundFiles The {@link List} of found {@link SourceFile}s.
             * @since 1.0.0
             * @see Phase
             * @see Phase.Error
             * @see SourceFile
             */
            private AmbiguousImportFile(final Phase culprit, final String packageName,
                                        final List<SourceFile> foundFiles) {
                super(culprit);

                // Build the resultant String
                final String filesString = foundFiles.stream()
                        .map(element -> FilePathPrefix + element.toString() + FilePathSuffix)
                        .collect(Collectors.joining(FilePathSuffix));

                // Initialize the message
                this.message = ErrorTable.get(MessageIndex).formatted(packageName, filesString);

            }

            /// ------
            /// Object

            /**
             * <p>Returns the {@link String} value of the {@link AmbiguousImportFile}'s constructed message.</p>
             * @return The {@link String} value of the {@link AmbiguousImportFile}'s constructed message.
             * @since 1.0.0
             * @see Object
             * @see String
             */
            @Override
            public String toString() {

                return this.message;

            }

        }

        /**
         * <p>A {@link Phase.Error} to be emitted if an import with a regular {@link String} package name yielded no
         * matching {@link SourceFile}s.</p>
         * @author Carlos L. Cuenca
         * @version 1.0.0
         * @since 1.0.0
         * @see SourceFile
         * @see Phase
         * @see Phase.Error
         */
        private final static class FileNotFound extends Phase.Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>The integer value corresponding to the {@link FileNotFound}'s message index.</p>
             * @since 1.0.0
             */
            private final static int MessageIndex = 103;

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link FileNotFound} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param culprit The {@link Phase} instance that emitted the {@link FileNotFound}.
             * @param filename The {@link String} value of the filename that was not found.
             * @since 1.0.0
             * @see SourceFile
             * @see Phase
             * @see String
             */
            private static void Assert(final Phase culprit, final String filename) {

                // Notify the culprit's Listener of the error
                culprit.getListener().notify(new FileNotFound(culprit, filename));

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} error message constructed by the {@link FileNotFound}.</p>
             * @since 1.0.0
             */
            private final String message;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link FileNotFound} to its default state.</p>
             * @param culprit The {@link Phase} instance that emitted the {@link FileNotFound}.
             * @param filename The {@link String} value of the filename that was not found.
             * @since 1.0.0
             * @see Phase
             * @see Phase.Error
             * @see SourceFile
             */
            private FileNotFound(final Phase culprit, final String filename) {
                super(culprit);

                // Initialize the message
                this.message = ErrorTable.get(MessageIndex).formatted(filename);

            }

            /// ------
            /// Object

            /**
             * <p>Returns the {@link String} value of the {@link FileNotFound}'s constructed message.</p>
             * @return The {@link String} value of the {@link FileNotFound}'s constructed message.
             * @since 1.0.0
             * @see Object
             * @see String
             */
            @Override
            public String toString() {

                return this.message;

            }

        }

        /**
         * <p>A {@link Phase.Error} to be emitted if an import with a regular {@link String} package name yielded no
         * matching {@link SourceFile}s.</p>
         * @author Carlos L. Cuenca
         * @version 1.0.0
         * @since 1.0.0
         * @see SourceFile
         * @see Phase
         * @see Phase.Error
         */
        private final static class FileNotFoundInPackage extends Phase.Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>The integer value corresponding to the {@link FileNotFoundInPackage}'s message index.</p>
             * @since 1.0.0
             */
            private final static int MessageIndex = 104;

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link FileNotFoundInPackage} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param culprit The {@link Phase} instance that emitted the {@link FileNotFoundInPackage}.
             * @param packageName The {@link String} value of the package name attempted to import.
             * @param filename The {@link String} value of the filename that was not found.
             * @since 1.0.0
             * @see SourceFile
             * @see Phase
             * @see String
             */
            private static void Assert(final Phase culprit, final String packageName, final String filename) {

                // Notify the culprit's Listener of the error
                culprit.getListener().notify(new FileNotFoundInPackage(culprit, packageName, filename));

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} error message constructed by the {@link FileNotFoundInPackage}.</p>
             * @since 1.0.0
             */
            private final String message;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link FileNotFoundInPackage} to its default state.</p>
             * @param culprit The {@link Phase} instance that emitted the {@link FileNotFoundInPackage}.
             * @param packageName The {@link String} value of the package name attempted to import.
             * @param filename The {@link String} value of the filename that was not found.
             * @since 1.0.0
             * @see Phase
             * @see Phase.Error
             * @see SourceFile
             */
            private FileNotFoundInPackage(final Phase culprit, final String packageName, final String filename) {
                super(culprit);

                // Initialize the message
                this.message = ErrorTable.get(MessageIndex).formatted(packageName, filename);

            }

            /// ------
            /// Object

            /**
             * <p>Returns the {@link String} value of the {@link FileNotFoundInPackage}'s constructed message.</p>
             * @return The {@link String} value of the {@link FileNotFoundInPackage}'s constructed message.
             * @since 1.0.0
             * @see Object
             * @see String
             */
            @Override
            public String toString() {

                return this.message;

            }

        }

        /**
         * <p>A {@link Phase.Error} to be emitted if an import with a wildcard {@link String} package name yielded no
         * matching {@link SourceFile}s.</p>
         * @author Carlos L. Cuenca
         * @version 1.0.0
         * @since 1.0.0
         * @see SourceFile
         * @see Phase
         * @see Phase.Error
         */
        private final static class PackageNotFound extends Phase.Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>The integer value corresponding to the {@link PackageNotFound}'s message index.</p>
             * @since 1.0.0
             */
            private final static int MessageIndex = 102;

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link PackageNotFound} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param culprit The {@link Phase} instance that emitted the {@link PackageNotFound}.
             * @param packageName The {@link String} value of the package that was not found.
             * @since 1.0.0
             * @see SourceFile
             * @see Phase
             * @see String
             */
            private static void Assert(final Phase culprit, final String packageName) {

                // Notify the culprit's Listener of the error
                culprit.getListener().notify(new PackageNotFound(culprit, packageName));

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} error message constructed by the {@link PackageNotFound}.</p>
             * @since 1.0.0
             */
            private final String message;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link PackageNotFound} to its default state.</p>
             * @param culprit The {@link Phase} instance that emitted the {@link PackageNotFound}.
             * @param packageName The {@link String} value of the package that was not found.
             * @since 1.0.0
             * @see Phase
             * @see Phase.Error
             * @see SourceFile
             */
            private PackageNotFound(final Phase culprit, final String packageName) {
                super(culprit);

                // Initialize the message
                this.message = ErrorTable.get(MessageIndex).formatted(packageName);

            }

            /// ------
            /// Object

            /**
             * <p>Returns the {@link String} value of the {@link PackageNotFound}'s constructed message.</p>
             * @return The {@link String} value of the {@link PackageNotFound}'s constructed message.
             * @since 1.0.0
             * @see Object
             * @see String
             */
            @Override
            public String toString() {

                return this.message;

            }

        }

        /// ---------------------
        /// Functional Interfaces

        /**
         * <p>Defines a means to implicitly expose a method that retrieves a {@link List} of {@link SourceFile}s that
         * match the specified {@link String} path.</p>
         * @see ImportAssert
         * @see SourceFile
         * @see List
         * @author Carlos L. Cuenca
         * @since 1.0.0
         * @version 1.0.0
         */
        @FunctionalInterface
        public interface Retrieve {

            List<SourceFile> MatchingSourceFilesAt(final String path, final Phase.Listener phaseListener)
                    throws Phase.Error;

        }

    }

    /// ------------------
    /// NameChecking Phase

    protected static class NameAssert {

        /// ------------------------
        /// Protected Static Methods

        private static Object Resolved(final Phase phase, final Name symbol) throws Phase.Error {

            // Initialize a handle to the name & package name
            final String name           = symbol.getName()          ;
            final String packageName    = symbol.getPackageName()   ;

            // Attempt to retrieve a result
            List<Object> names = phase.getScope().get(packageName, name);

            // Assert the Name is not visible
            if(names.isEmpty())
                if((packageName == null) || (packageName.isEmpty()) || packageName.isBlank())
                    NameNotDefined.Assert(phase, symbol);

                else NameNotDefined.Assert(phase, symbol);

                // Otherwise, we may have gotten an ambiguous result (name clash)
            else if(names.size() > 1)
                AmbiguousResolution.Assert(phase, symbol, names);

            // Initialize a handle to the result
            Object result = names.get(0);

            // Recur on the NamedType
            if(result instanceof NamedType)
                result = Resolved(phase, ((NamedType) result).getName());

            return result;

        }

        protected static void Resolved(final Phase phase, final Expression expression) throws Phase.Error {

            // Initialize a handle to the result
            final Object result = Resolved(phase, expression.getName());

            // Assert the Expression is an Invocation or New Mobile Expression
            if(expression instanceof NewMobileExpression) {

                // Assert the result is a Context.SymbolMap
                if(result instanceof Context.SymbolMap)
                    ((NewMobileExpression) expression).setCandidates((Context.SymbolMap) result);

            } else if(expression instanceof InvocationExpression) {

                // Assert the result is a Context.SymbolMap
                if(result instanceof Context.SymbolMap)
                    ((InvocationExpression) expression).setCandidates((Context.SymbolMap) result);

                else NonProcedureInvocation.Assert(phase, expression);

            } else expression.setType((Type) result);

        }

        protected static void Resolved(final Phase phase, final ConstantDeclaration constantDeclaration) throws Phase.Error {

            // Assert the name is visible
            constantDeclaration.setType((Type) Resolved(phase, constantDeclaration.getType().getName()));

        }

        protected static void Resolved(final Phase phase, final LocalDeclaration localDeclaration) throws Phase.Error {

            localDeclaration.setType((Type) Resolved(phase, localDeclaration.getType().getName()));

        }

        protected static void Resolved(final Phase phase, final ProcedureTypeDeclaration procedureTypeDeclaration) throws Phase.Error {

            // Initialize a handle to the scope & return Type
            final Type returnType = procedureTypeDeclaration.getReturnType()  ;

            // Assert the return Type is bound
            procedureTypeDeclaration.setReturnType((Type) NameAssert.Resolved(phase, returnType.getName()));

            // Assert the Implements Type list is bound
            procedureTypeDeclaration.setCandidateForEachImplement(implement -> {

                // Assert the Name is visible
                final Object result = Resolved(phase, implement);

                // Assert the result is a Context.SymbolMap
                if(!(result instanceof Context.SymbolMap))
                    NonProcedureImplements.Assert(phase, procedureTypeDeclaration);

                // Return the result
                return (Context.SymbolMap) result;

            });

            // Assert each parameter type is bound
            procedureTypeDeclaration.setTypeForEachParameter(parameterDeclaration -> {

                // Assert the Name is visible
                final Object result = Resolved(phase, parameterDeclaration.getType().getName());

                // Return the result
                return (Type) result;

            });

        }

        protected static void Resolved(final Phase phase, final ProtocolTypeDeclaration protocolTypeDeclaration) throws Phase.Error {

            // Assert the Implements Type list is bound
            protocolTypeDeclaration.setTypeForEachExtend(implement -> {

                // Assert the Name is visible
                final Object result = Resolved(phase, implement);

                // Assert the result is a Context.SymbolMap
                if(!(result instanceof ProtocolTypeDeclaration))
                    NonProtocolName.Assert(phase, protocolTypeDeclaration);

                // Return the result
                return (ProtocolTypeDeclaration) result;

            });

        }

        protected static void Resolved(final Phase phase, final RecordTypeDeclaration recordTypeDeclaration) throws Phase.Error {

            // Initialize a handle to the encountered extends
            final Set<String> seen = new HashSet<>();

            // Assert the Implements Type list is bound
            recordTypeDeclaration.setTypeForEachExtend(implement -> {

                // Assert the Name is visible
                final Object result = Resolved(phase, implement);

                // Assert the result is a Context.SymbolMap
                if(!(result instanceof ProtocolTypeDeclaration))
                    NonProtocolName.Assert(phase, recordTypeDeclaration);

                // Initialize a handle to the RecordType Declaration
                final RecordTypeDeclaration recordExtend = (RecordTypeDeclaration) result;

                // Assert the Name is specified once
                if(!seen.add(recordExtend.getName().toString()))
                    ExtendDefined.Assert(phase, recordTypeDeclaration);

                // Return the result
                return recordExtend;

            });

        }

        protected static void Resolved(final Phase phase, final ProtocolLiteralExpression protocolLiteral) throws Phase.Error {

            // Initialize a handle to the result
            final Object result = Resolved(phase, protocolLiteral.getName());

            // Assert the Expression is an ProtocolTypeDecl
            if(result instanceof ProtocolTypeDeclaration)
                protocolLiteral.setType((ProtocolTypeDeclaration) result);

                // Otherwise
            else NonProtocolName.Assert(phase, protocolLiteral);

            // Initialize a handle to the ProtocolLiteral's Type & Protocol Case
            final ProtocolTypeDeclaration type     = (ProtocolTypeDeclaration) protocolLiteral.getType();
            final ProtocolCase tagCase  = type.getCaseFrom(protocolLiteral.getTagLiteral());

            // Assert the case was found
            if(tagCase == null)
                TagNotDefined.Assert(phase, protocolLiteral);

            // Update the case
            protocolLiteral.setProtocolCase(tagCase);

        }

        protected static void Resolved(final Phase phase, final RecordLiteralExpression recordLiteralExpression) throws Phase.Error {

            // Initialize a handle to the result
            final Object result = Resolved(phase, recordLiteralExpression.getName());

            // Assert the Expression is an ProtocolTypeDecl
            if(result instanceof RecordTypeDeclaration)
                recordLiteralExpression.setType((RecordTypeDeclaration) result);

                // Otherwise
            else NonRecordName.Assert(phase, recordLiteralExpression);

        }

        protected static void Resolved(final Phase phase, final ArrayType arrayType) throws Phase.Error {

            arrayType.setComponentType((Type) Resolved(phase, arrayType.getComponentType().getName()));

        }

        protected static void Resolved(final Phase phase, final ChannelType channelType) throws Phase.Error {

            channelType.setComponentType((Type) Resolved(phase, channelType.getComponentType().getName()));

        }

        protected static void Resolved(final Phase phase, final ChannelEndType channelEndType) throws Phase.Error {

            channelEndType.setComponentType((Type) Resolved(phase, channelEndType.getComponentType().getName()));

        }

        protected static void Resolved(final Phase phase, final CastExpression castExpression) throws Phase.Error {

            castExpression.setType((Type) Resolved(phase, castExpression.getCastType().getName()));

        }

        protected static void Resolved(final Phase phase, final NewArrayExpression newArrayExpression) throws Phase.Error {

            newArrayExpression.setType((Type) Resolved(phase, newArrayExpression.getComponentType().getName()));

        }

        protected static void Resolved(final Phase phase, final BreakStatement breakStatement) throws Phase.Error {

            // Assert the target is visible
            Resolved(phase, breakStatement.getTarget());

        }

        protected static void Resolved(final Phase phase, final ContinueStatement continueStatement) throws Phase.Error {

            // Assert the target is visible
            Resolved(phase, continueStatement.getTarget());

        }

        /// -----------
        /// Phase.Error

        /**
         * <p>{@link Error} to be emitted if the {@link Name} resolution retrieved multiple results.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class AmbiguousResolution extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link AmbiguousResolution} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param names List containing all the resolved {@link Name}s.
             * @since 0.1.0
             */
            protected static void Assert(final Phase phase, final Name instance, final List<Object> names) {

                Error.Assert(AmbiguousResolution.class, phase, instance, names);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>Collection of resolved {@link Name}s.</p>
             */
            private final List<Object>                         names           ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NameDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected AmbiguousResolution(final Phase culprit,
                                          final Name instance,
                                          final List<Object> names) {
                super(culprit);

                this.names          = names         ;
                this.instance       = instance      ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Error} to be emitted if the declaration specifies an extend {@link Type} twice.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ExtendDefined extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ExtendDefined} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(ExtendDefined.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ExtendDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ExtendDefined(final Phase culprit,
                                    final AST instance) {
                super(culprit);

                this.instance       = instance                                          ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null   ;

            }

        }

        /**
         * <p>{@link Error} to be emitted if the {@link Statement}'s Label is defined in the current scope.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class LabelDefined extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link LabelDefined} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(LabelDefined.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NameDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected LabelDefined(final Phase culprit,
                                   final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Error} to be emitted if the {@link Name} is defined in the current scope.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NameDefined extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NameDefined} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(NameDefined.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NameDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NameDefined(final Phase culprit,
                                  final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Error} to be emitted if the {@link Name} is not defined in the current scope.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NameNotDefined extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NameNotDefined} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(NameNotDefined.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NameDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NameNotDefined(final Phase culprit,
                                     final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Error} to be emitted if the {@link Name}'s {@link Type} is not a {@link ProcedureTypeDeclaration}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NonProcedureInvocation extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NonProcedureInvocation} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(NonProcedureInvocation.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NameDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NonProcedureInvocation(final Phase culprit,
                                             final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Error} to be emitted if the {@link Name}'s {@link Type} is not a {@link ProcedureTypeDeclaration}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NonProcedureImplements extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NonProcedureImplements} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(NonProcedureImplements.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NameDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NonProcedureImplements(final Phase culprit,
                                             final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Error} to be emitted if the {@link Name}'s {@link Type} is not a {@link ProtocolTypeDeclaration}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NonProtocolName extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NonProtocolName} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(NonProcedureInvocation.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NonProtocolName} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NonProtocolName(final Phase culprit,
                                      final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Error} to be emitted if the {@link Name}'s {@link Type} is not a {@link RecordTypeDeclaration}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NonRecordName extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NonRecordName} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(NonRecordName.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NonRecordName} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NonRecordName(final Phase culprit,
                                    final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Error} to be emitted if a {@link ProtocolLiteralExpression} specified an undefined tag {@link Name}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class TagNotDefined extends Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link TagNotDefined} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Error.Assert(TagNotDefined.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link TagNotDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected TagNotDefined(final Phase culprit,
                                    final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

    }

    /// -------------------
    /// Type Checking Phase

    // TODO: Tidy
    protected static class TypeAssert {

        protected static boolean IsConstant(final Expression expression) {

            // Return the result
            return (expression != null) && expression.isConstant();

        }

        protected static boolean IsConstantTrue(final Expression expression) {

            return IsConstant(expression) && ((Boolean) expression.constantValue());

        }

        protected static boolean IsConstantFalse(final Expression expression) {

            return IsConstant(expression) && !((Boolean) expression.constantValue());

        }

        protected static void TargetExpressionTypeEqual(final ChannelWriteStatement channelWriteStatement)
                throws Phase.Error {

            // Initialize a handle to the Channel Write Statement's Component Type & the Write Expression's Type
            final Type componentType        = channelWriteStatement.getTargetExpression().getType().getComponentType();
            final Type writeExpressionType  = channelWriteStatement.getWriteExpression().getType();

            // Assert the Channel Write Statement Expression's Type is defined & is not Type Equal to
            // the component type
            if(writeExpressionType != null && !writeExpressionType.typeEqual(componentType))
                // Wrap the Channel Write Expression in a Cast Expression
                channelWriteStatement.setWriteExpression(
                        new CastExpression(componentType, channelWriteStatement.getWriteExpression()));

        }

        protected static void SwitchLabelConstantOrProtocolType(final Phase phase, final SwitchLabel switchLabel) {

            // Initialize a handle to the Switch Label's Expression
            final Expression switchLabelExpression = switchLabel.getExpression();

            // Assert the Switch Label Expression is constant or is a Protocol Tag
            if(!switchLabelExpression.isConstant())
                SwitchLabelExpressionNotConstantOrProtocolTag.Assert(phase, switchLabelExpression);

        }

        /// -----------
        /// Phase.Error

        /**
         * <p>{@link Error} to be emitted if the {@link SwitchLabel} is not constant or a protocol tag.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class SwitchLabelExpressionNotConstantOrProtocolTag extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link SwitchLabelExpressionNotConstantOrProtocolTag} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @since 0.1.0
             */
            protected static void Assert(final Phase phase, final Expression instance) {

                Error.Assert(NameAssert.AmbiguousResolution.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link SwitchLabelExpressionNotConstantOrProtocolTag} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected SwitchLabelExpressionNotConstantOrProtocolTag(final Phase culprit,
                                                                    final Name instance) {
                super(culprit);

                this.instance       = instance      ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

    }

    /// ---------------------
    /// Code Generation Phase

    protected static class RewriteAssert {

        public static int labelNo = 0;
        private static int tempCounter = 0;

        public static LocalDeclaration LocalDeclarationFrom(final Type type, final String name) {

            return LocalDeclarationFrom(type, name, false);

        }

        public static LocalDeclaration LocalDeclarationFrom(final Type type, final String name, final boolean isConstant) {

            return new LocalDeclaration(type, new Name(name), null, isConstant);

        }

        public static AssignmentExpression AssignmentFrom(final String name, final Expression expression) {

            return new AssignmentExpression(new NameExpression(new Name(name)), expression, AssignmentExpression.EQ);

        }

        public static NameExpression NameExpressionFrom(final String name) {

            return new NameExpression(new Name(name));

        }

        public static Sequence<Expression> ParameterListFrom(final Expression... expressions) {

            // Initialize a handle to the result
            final Sequence<Expression> parameterList = new Sequence<>();

            // Iterate through the Expressions
            if(expressions != null) for(final Expression expression: expressions)
                if(expression != null) parameterList.append(expression);

            // Return the result
            return parameterList;

        }

        public static Expression InvertedUnaryPreExpressionFrom(final Expression expression) {

            return new UnaryPreExpression(expression, UnaryPreExpression.NOT);

        }

        public static Expression InvertedExpressionFrom(final Expression expression) {

            return InvertedUnaryPreExpressionFrom(expression);

        }

        public static InvocationExpression InvocationFrom(final Name name, final boolean ignore, final Expression... expressions) {

            return new InvocationExpression(name, ParameterListFrom(expressions), ignore);

        }

        public static InvocationExpression IgnoreInvocationFrom(final Name name, final Expression... expressions) {

            return InvocationFrom(name, true, expressions);

        }

        public static Statement IgnoreInvocationStatementFrom(final Name name, final Expression... expressions) {

            return new ExpressionStatement(IgnoreInvocationFrom(name, expressions));

        }

        public static Statement LabelledIgnoreInvocationStatementFrom(final String label, final Name name, final Expression... expressions) {

            return new ExpressionStatement(label, IgnoreInvocationFrom(name, expressions));

        }

        public static Statement AssignmentStatementFrom(final String name, final Expression expression) {

            return new ExpressionStatement(AssignmentFrom(name, expression));

        }

        public static Statement GotoStatementFor(final String label) {

            return IgnoreInvocationStatementFrom(new Name("GOTO"), NameExpressionFrom(label));

        }

        public static Statement LabelledGotoStatementFrom(final String label, final String target) {

            return LabelledIgnoreInvocationStatementFrom(label, new Name("GOTO"), NameExpressionFrom(target));

        }

        public static Statement BreakConditionStatementFrom(final String startLabel, final String endLabel, final Expression evaluationExpression) {

            return new IfStatement(startLabel, evaluationExpression, GotoStatementFor(endLabel));

        }

        public static Statement InvertedBreakConditionStatementFrom(final String startLabel, final String endLabel, final Expression expression) {

            return new IfStatement(startLabel, InvertedExpressionFrom(expression), GotoStatementFor(endLabel));

        }

        public static String nextTemp() {
            return "temp_" + tempCounter++;
        }

        /**
         * <p>Recursively aggregates all of the {@link Name}s in the {@link ProtocolTypeDeclaration}'s inheritance
         * hierarchy using the {@link Context.SymbolMap} to resolve the ancestor {@link ProtocolTypeDeclaration} names.</p>
         * @param members The {@link Set} where all the {@link Name}s are aggregated.
         * @param symbolMap The {@link Context.SymbolMap} that is used to resolve all ancestor {@link ProtocolTypeDeclaration} names.
         * @param protocolTypeDeclaration The {@link ProtocolTypeDeclaration} to recur from.
         * @since 0.1.0
         */
        private static void EmplaceAncestorNames(final Set<Name> members, final Context.SymbolMap symbolMap,
                                                 final ProtocolTypeDeclaration protocolTypeDeclaration) {

            // Iterate through any parent names
            for(final Name parent: protocolTypeDeclaration.extend()) {

                // Initialize the preliminary result
                final Object result = symbolMap.get(parent.toString());

                // Check for a ProtocolTypeDecl (This probably shouldn't be necessary) & recur
                if(result instanceof ProtocolTypeDeclaration)
                    EmplaceAncestorNames(members, symbolMap, (ProtocolTypeDeclaration) result);

            }

            // Iterate through the local RecordMembers
            for(final Name ancestorName: protocolTypeDeclaration.extend()) {

                org.processj.compiler.Compiler.Info(protocolTypeDeclaration + "adding member " + ancestorName);

                if(!members.add(ancestorName))
                    org.processj.compiler.Compiler.Info(protocolTypeDeclaration + String.format("Name '%s' already in (%s)",
                            ancestorName, protocolTypeDeclaration));

            }

        }

        /**
         * <p>Recursively aggregates all of the {@link RecordMemberDeclaration}s in the {@link RecordTypeDeclaration}'s inheritance
         * hierarchy using the {@link Context.SymbolMap} to resolve the ancestor {@link RecordTypeDeclaration} names.</p>
         * @param members The {@link Set} where all the {@link RecordMemberDeclaration}s are aggregated.
         * @param symbolMap The {@link Context.SymbolMap} that is used to resolve all ancestor {@link RecordTypeDeclaration} names.
         * @param recordTypeDeclaration The {@link RecordTypeDeclaration} to recur from.
         * @since 0.1.0
         */
        private static void EmplaceRecordMembers(final Set<RecordMemberDeclaration> members, final Context.SymbolMap symbolMap,
                                                 final RecordTypeDeclaration recordTypeDeclaration) {

            // Iterate through any parent names
            for(final Name parent: recordTypeDeclaration.getExtends()) {

                // Initialize the preliminary result
                final Object result = symbolMap.get(parent.toString());

                // Check for a RecordTypeDecl (This probably shouldn't be necessary) & recur
                if(result instanceof RecordTypeDeclaration)
                    EmplaceRecordMembers(members, symbolMap, (RecordTypeDeclaration) result);

            }

            // Iterate through the local RecordMembers
            for(final RecordMemberDeclaration recordMemberDeclaration : recordTypeDeclaration.getBody()) {

                org.processj.compiler.Compiler.Info(recordTypeDeclaration + "adding member " + recordMemberDeclaration.getType() + " " + recordMemberDeclaration);

                if(!members.add(recordMemberDeclaration))
                    org.processj.compiler.Compiler.Info(recordTypeDeclaration + String.format("Name '%s' already in (%s)",
                            recordMemberDeclaration, recordTypeDeclaration));

            }

        }

        protected static void RewriteArrayType(final ConstantDeclaration constantDeclaration) {

            // Initialize a handle to the Constant Declaration's Name
            final Type          type        = constantDeclaration.getType();
            final Name          name        = constantDeclaration.getName();
            final Expression    expression  = constantDeclaration.getInitializationExpression();

            // Assert the Constant Declaration's Type is not an ArrayType
            if((name.getDepth() > 0) || (expression instanceof ArrayLiteralExpression)) {

                // Initialize a handle to the synthesized Type
                final Type synthesized = (name.getDepth() > 0) ? new ArrayType(type, name.getDepth()) : type;

                // Assert the Constant Declaration is not initialized with an ArrayLiteral Expression
                if(expression instanceof ArrayLiteralExpression)
                    constantDeclaration.setInitializationExpression(
                            new NewArrayExpression(synthesized, (ArrayLiteralExpression) expression));

                // Overwrite the Constant Declaration's Type
                constantDeclaration.setType(synthesized);

            }

            // Overwrite the Constant Declaration's Name
            constantDeclaration.setName(new Name(name, 0));

        }

        protected static void RewriteArrayType(final ParameterDeclaration parameterDeclaration) throws Error {

            // Initialize a handle to the Constant Declaration's Name
            final Type          type        = parameterDeclaration.getType();
            final Name          name        = parameterDeclaration.getName();

            // Assert the Constant Declaration's Type is not an ArrayType
            if((name.getDepth() > 0)) {

                // Initialize a handle to the synthesized Type
                final Type synthesized = (name.getDepth() > 0) ? new ArrayType(type, name.getDepth()) : type;

                // Overwrite the Constant Declaration's Type
                parameterDeclaration.setType(synthesized);

            }

            // Overwrite the Constant Declaration's Name
            parameterDeclaration.setName(new Name(name, 0));

        }

        protected static void RewriteArrayType(final LocalDeclaration localDeclaration) throws Error {

            // Initialize a handle to the Constant Declaration's Name
            final Type          type        = localDeclaration.getType();
            final Name          name        = localDeclaration.getName();

            // Assert the Constant Declaration's Type is not an ArrayType
            if((name.getDepth() > 0)) {

                // Initialize a handle to the synthesized Type
                final Type synthesized = (name.getDepth() > 0) ? new ArrayType(type, name.getDepth()) : type;

                // Overwrite the Constant Declaration's Type
                localDeclaration.setType(synthesized);

            }

            // Overwrite the Constant Declaration's Name
            localDeclaration.setName(new Name(name, 0));

        }

        protected static void Rewritten(final Phase phase, final AltStatement altStatement) throws Phase.Error {

            // Initialize a handle to the AltCases and new Sequence
            final Sequence<AltCase> cases     = altStatement.getBody();
            final Sequence<AltCase> flattened = new Sequence<>();

            // Iterate through the current children
            for(final AltCase altCase: cases) {

                // If the Alt Case is not nested, simply append it to the body
                if(!altCase.isNestedAltStatement()) flattened.append(altCase);

                    // Otherwise
                else {

                    // Retrieve a handle to the Alt Statement
                    final AltStatement nested = (AltStatement) altCase.getBody();

                    // TODO: for now replicated alts just left.
                    if(nested.isReplicated()) flattened.append(altCase);

                        // Otherwise, merge
                    else flattened.merge(altStatement.getBody());

                }

            }

            // Clear the cases
            cases.clear();
            cases.merge(flattened);

            // TODO: handle Replicated Alts & synthesize labels like the other unrolled contexts
            // Aggregate the children to the enclosing context
            phase.getContext().forEachContextUntil(context -> {

                boolean hasBody = false;

                if(context instanceof BreakableContext) {

                    ((BreakableContext) context).getMergeBody().aggregate(altStatement.getMergeBody());

                    hasBody = true;

                } else if(context instanceof YieldingContext) {

                    ((YieldingContext) context).getMergeBody().aggregate(altStatement.getMergeBody());

                    hasBody = true;

                }

                return hasBody;

            });

        }

        protected static void Rewritten(final Phase phase, final BlockStatement body) throws Error {

            // Initialize a handle to the Merge Body
            // TODO: Change this to the parent's body
            final BlockStatement mergeBody = body.getMergeBody();

            // Initialize a handle to the Statements
            final Sequence<Statement> statements = (Sequence<Statement>) body.getStatements();

            // Resolve the children
            for(int index = 0; index < statements.size(); index++) {

                // Initialize a handle to the Statement
                final Statement statement     = statements.child(index);
                final Statement nextStatement = (index < (statements.size() - 1)) ? statements.child(index + 1) : null;

                // Initialize a handle to the starting & ending label
                final String label      = (statement.definesLabel()) ? statement.getLabel() : "START_" + labelNo;
                final String endLabel   = (nextStatement != null) && nextStatement.definesLabel()
                        ? nextStatement.getLabel() : "END_" + labelNo++;

                // Update the Statement's Labels
                statement.setLabel(label);
                statement.setEndLabel(endLabel);

                // Resolve the Statement
                statement.accept(phase);

                // TODO: Logic for final Statement

            }

            // Clear the current body
            body.clear();

            // Aggregate the children to the enclosing context
            mergeBody.aggregate(body.getMergeBody());

        }

        protected static void YieldedUnrolledInto(final Context.SymbolMap scope, final BlockStatement blockStatement, final Expression expression) throws Error {

            // Assert the Target Expression Yields
            if(expression.doesYield()) {

                // Initialize a handle to the Local Name & Type
                final String localName  = nextTemp();
                final Type   type       = expression.getType();

                // Aggregate the synthetic Statements
                blockStatement.append(LocalDeclarationFrom(type, localName));
                blockStatement.append(AssignmentStatementFrom(localName, expression));

                // Synthesize a NameExpression to cache into the Scope
                scope.setPreviousYieldingName(new NameExpression(new Name(localName)));

            }

        }

        protected static void Rewritten(final Phase phase, final ProtocolTypeDeclaration protocolTypeDeclaration) {

            org.processj.compiler.Compiler.Info(protocolTypeDeclaration + "Visiting a RecordTypeDecl (" + protocolTypeDeclaration + ")");

            // Initialize the Set of Names
            final Set<Name> ancestors = new LinkedHashSet<>();

            // Recursively aggregate the Name
            EmplaceAncestorNames(ancestors, phase.getScope(), protocolTypeDeclaration);

            // Clear the existing Names
            protocolTypeDeclaration.extend().clear();

            // Emplace all the RecordMembers
            ancestors.forEach(name -> protocolTypeDeclaration.extend().append(name));

            // Log
            org.processj.compiler.Compiler.Info(protocolTypeDeclaration + String.format("record %s with %s member(s)",
                    protocolTypeDeclaration, protocolTypeDeclaration.extend().size()));

            // Print
            protocolTypeDeclaration.extend().forEach(name ->
                    org.processj.compiler.Compiler.Info(protocolTypeDeclaration + "> Ancestor: " + name));

        }

        protected static void Rewritten(final Phase phase, final RecordTypeDeclaration recordTypeDeclaration) {

            org.processj.compiler.Compiler.Info(recordTypeDeclaration + "Visiting a RecordTypeDecl (" + recordTypeDeclaration + ")");

            // Initialize the Set of RecordMembers
            final Set<RecordMemberDeclaration> members = new LinkedHashSet<>();

            // Recursively aggregate the RecordMembers
            EmplaceRecordMembers(members, phase.getScope(), recordTypeDeclaration);

            // Clear the existing RecordMembers
            recordTypeDeclaration.getBody().clear();

            // Emplace all the RecordMembers
            members.forEach(recordMember -> recordTypeDeclaration.getBody().append(recordMember));

            // Log
            org.processj.compiler.Compiler.Info(recordTypeDeclaration + String.format("record %s with %s member(s)",
                    recordTypeDeclaration, recordTypeDeclaration.getBody().size()));

            // Print
            recordTypeDeclaration.getBody().forEach(recordMember ->
                    org.processj.compiler.Compiler.Info(recordTypeDeclaration + "> member " + recordMember.getType() + " " + recordMember.getName()));


        }

        protected static void Rewritten(final Phase phase, final ArrayAccessExpression arrayAccessExpression) {

            // Initialize a handle to the target & index expression
            final Expression targetExpression   = arrayAccessExpression.getTargetExpression();
            final Expression indexExpression    = arrayAccessExpression.getIndexExpression();

            // Assert the target expression yields
            if(targetExpression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the ArrayAccessExpression

            }


            // Assert the index expression yields
            if(indexExpression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the ArrayAccessExpression

            }


        }

        protected static void Rewritten(final Phase phase, final BinaryExpression binaryExpression) {

            // Assert the Left Expression Yields
            if(binaryExpression.getLeftExpression().doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the BinaryExpression

            }

            // Assert the Right Expression Yields
            if(binaryExpression.getRightExpression().doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the BinaryExpression

            }

        }

        protected static void Rewritten(final Phase phase, final CastExpression castExpression) {

            final Expression expression = castExpression.getExpression();

            // Assert the Expression Yields
            if(expression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the CastExpression

            }

        }

        protected static void Rewritten(final Phase phase, final ChannelReadExpression channelReadExpression) throws Phase.Error {

            phase.getContext().forEachContextUntil(context -> {

                boolean hasBody = false;

                if(context instanceof BreakableContext) {

                    // Assert unrolled
                    // TODO: Check if extended Rendezvous needs to be unrolled
                    RewriteAssert.YieldedUnrolledInto(phase.getContext().getScope(),
                            ((BreakableContext) context).getMergeBody(), channelReadExpression)  ;

                    hasBody = true;

                } else if(context instanceof YieldingContext) {

                    RewriteAssert.YieldedUnrolledInto(phase.getContext().getScope(),
                            ((YieldingContext) context).getMergeBody(), channelReadExpression)  ;

                    hasBody = true;

                }

                return hasBody;

            });

        }

        protected static void Rewritten(final Phase phase, final InvocationExpression invocationExpression) throws Phase.Error {

            // Initialize a handle to the enclosing Context's Merge Body & the Parameter Expressions
            final Sequence<Expression> parameters = invocationExpression.getParameterExpressions();

            // Resolve the Parameters
            for(int index = 0; index < parameters.size(); index++) {

                // Initialize a handle to the parameter Expression
                final Expression parameterExpression = parameters.child(index);

                // Resolve the Parameter Expression
                parameterExpression.accept(phase);

                // Assert the Expression Yields
                if(parameterExpression.doesYield()) {

                    // TODO: Collect the NameExpressions from the temp statement to emplace into the InvocationExpression's
                    // TODO: Parameter Expressions

                }

            }

        }

        protected static void Rewritten(final Phase phase, final TernaryExpression ternaryExpression) throws Phase.Error {

            // Initialize a handle to the enclosing Context's Merge Body
            // TODO: Get the parent's merge body
            //final BlockStatement mergeBody = phase.getContext().getMergeBody();

            // Initialize a handle to the Evaluation Expression, then Expression, & Else Expression
            final Expression evaluationExpression = ternaryExpression.getEvaluationExpression() ;
            final Expression thenExpression       = ternaryExpression.getThenExpression()                ;
            final Expression elseExpression       = ternaryExpression.getElseExpression()                ;

            // Resolve the Evaluation Expression
            evaluationExpression.accept(phase);

            // Assert the Evaluation Expression Yields
            if(evaluationExpression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the TernaryExpression

            }

            // Resolve the Then Expression
            thenExpression.accept(phase);

            // Assert the Evaluation Expression Yields
            if(thenExpression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the TernaryExpression

            }

            // Resolve the Else Expression
            thenExpression.accept(phase);

            // Assert the Evaluation Expression Yields
            if(elseExpression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the TernaryExpression

            }

        }

        protected static void Rewritten(final Phase phase, final UnaryPreExpression unaryPreExpression) throws Phase.Error {

            final Expression expression = unaryPreExpression.getExpression();

            // Assert the Evaluation Expression Yields
            if(expression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the UnaryPreExpression

            }

        }

        protected static void Rewritten(final Phase phase, final UnaryPostExpression unaryPostExpression) throws Phase.Error {

            final Expression expression = unaryPostExpression.getExpression();

            // Assert the Evaluation Expression Yields
            if(expression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the UnaryPreExpression

            }

        }

        protected static void Rewritten(final Phase phase, final AltCase altCase) throws Phase.Error {

            // TODO: Synthesize Labels like the other unrolled Contexts
            // Aggregate the children to the enclosing context
            phase.getContext().forEachContextUntil(context -> {

                boolean hasBody = false;

                if(context instanceof BreakableContext) {

                    ((BreakableContext) context).getMergeBody().aggregate((BlockStatement) altCase.getBody());

                    hasBody = true;

                } else if(context instanceof YieldingContext) {

                    ((YieldingContext) context).getMergeBody().aggregate((BlockStatement) altCase.getBody());

                    hasBody = true;

                }

                return hasBody;

            });

        }

        protected static void Rewritten(final Phase phase, final BreakStatement breakStatement) throws Phase.Error {

            // Aggregate the synthesized goto TODO: Maybe Rewrite in CodeGen?
            phase.getContext().forEachContextUntil(context -> {

                boolean hasBody = false;

                if(context instanceof BreakableContext) {

                    ((BreakableContext) context).getMergeBody().append(
                            RewriteAssert.GotoStatementFor(breakStatement.getTarget().toString()));

                    hasBody = true;

                } else if(context instanceof YieldingContext) {

                    ((YieldingContext) context).getMergeBody().append(
                            RewriteAssert.GotoStatementFor(breakStatement.getTarget().toString()));

                    hasBody = true;

                }

                return hasBody;

            });
        }

        protected static void Rewritten(final Phase phase, final ChannelWriteStatement channelWriteStatement) throws Phase.Error {

            // Initialize a handle to the enclosing Context's Merge Body
            // TODO: Get the closest Mergebody
            //final BlockStatement mergeBody = phase.getContext().getMergeBody();

            // Initialize a handle to the target & write Expressions
            Expression targetExpression = channelWriteStatement.getTargetExpression();
            Expression writeExpression  = channelWriteStatement.getWriteExpression();

            // Resolve the target Expression
            targetExpression.accept(phase);

            // Assert the Target Expression Yields
            if(targetExpression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the ChannelWriteStatement

            }

            // Resolve the write Expression
            writeExpression.accept(phase);

            // Assert the Write Expression Yields
            if(writeExpression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the ChannelWriteStatement

            }

            // Aggregate the synthetic Write Statement
            //mergeBody.append(channelWriteStatement);


        }

        protected static void Rewritten(final Phase phase, final ClaimStatement claimStatement) throws Phase.Error {

            // Merge the Claim Statement
            phase.getContext().forEachContextUntil(context -> {

                boolean hasBody = false;

                if(context instanceof BreakableContext) {

                    ((BreakableContext) context).getMergeBody().append(claimStatement);

                    hasBody = true;

                } else if(context instanceof YieldingContext) {

                    ((YieldingContext) context).getMergeBody().append(claimStatement);

                    hasBody = true;

                }

                return hasBody;

            });

        }

        protected static void Rewritten(final Phase phase, final ContinueStatement continueStatement) throws Phase.Error {

            // Aggregate the synthesized goto TODO: Maybe rewrite in CodeGen?

            phase.getContext().forEachContextUntil(context -> {

                boolean hasBody = false;

                if(context instanceof BreakableContext) {

                    ((BreakableContext) context).getMergeBody().append(
                            RewriteAssert.GotoStatementFor(continueStatement.getTarget().toString()));

                    hasBody = true;

                } else if(context instanceof YieldingContext) {

                    ((YieldingContext) context).getMergeBody().append(
                            RewriteAssert.GotoStatementFor(continueStatement.getTarget().toString()));

                    hasBody = true;

                }

                return hasBody;

            });


        }

        protected static void Rewritten(final Phase phase, final DoStatement doStatement) throws Phase.Error {

            // Initialize a handle to the synthetic labels
            // TODO: When synthesizing labels, make sure they do not clash with another name
            // TODO: Check BarrierSet & to-be Jump labels

            // Initialize a handle to the enclosing Context's & While Statement's evaluation Expression
            //final BlockStatement enclosingMergeBody = phase.getEnclosingContext().getMergeBody();

            // Assert the WhileStatement's Merge Body is cleared
            doStatement.clearMergeBody();

            // Assert the Do Statement defines a body
            if(doStatement.definesBody())
                doStatement.getBody().getStatements().child(0).setLabel(doStatement.getLabel());

            // Assert Flattened Statements
            RewriteAssert.Rewritten(phase, doStatement.getBody());

            // Merge the children
            //enclosingMergeBody.aggregate(doStatement.getMergeBody());

            // TODO: aggregate yield

            final Expression evaluationExpression = doStatement.getEvaluationExpression();

            // Append the synthesized break condition statement
            //enclosingMergeBody.append(RewriteAssert.BreakConditionStatementFrom("",
                    //doStatement.getLabel(), evaluationExpression));

        }

        protected static void Rewritten(final Phase phase, final ExpressionStatement expressionStatement) throws Phase.Error {

            // Initialize a handle to the Expression
            Expression expression = expressionStatement.getExpression();

            // Resolve the Expression
            expression.accept(phase);

            // Assert the Target Expression Yields
            if(expression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the ExpressionStatement

            }

            //phase.getContext().getMergeBody().append(expressionStatement);

        }

        protected static void Rewritten(final Phase phase, final ForStatement forStatement) throws Phase.Error {

            // Initialize a handle to the enclosing Context's Merge Body
            //final BlockStatement enclosingMergeBody = phase.getEnclosingContext().getMergeBody();

            // Initialize a handle to the label
            String startLabel = forStatement.getLabel();

            // Clear the For Statement's Merge Body
            forStatement.getBody().clearMergeBody();

            // Assert the For Statement defines initialization Statements
            if(forStatement.definesInitializationStatements()) {

                // Initialize a handle to the first Statement
                final Statement initializationStatement = forStatement.getInitializationStatements().child(0);

                // Assert the Initialization Statement does not define a label
                if(!initializationStatement.definesLabel()) {

                    initializationStatement.setLabel(startLabel);
                    startLabel += "_EVALUATION";

                }

                // Resolve the initialization Statements
                for(final Statement statement: forStatement.getInitializationStatements())
                    statement.accept(phase);

            }

            // Aggregate the synthesized break condition statement
            // TODO: Resolve Evaluation Expression & check for yielding
            //enclosingMergeBody.append(RewriteAssert.InvertedBreakConditionStatementFrom(startLabel, forStatement.getEndLabel(),
                    //forStatement.getEvaluationExpression()));

            // Assert the statements have been flattened
            RewriteAssert.Rewritten(phase, forStatement.getBody());

            // Assert the For Statement defines increment statements
            // TODO: Check for yielding Expressions
            if(forStatement.definesIncrementStatements())
                forStatement.getIncrementStatements().accept(phase);

            // Merge the children
            //for(final Statement statement: forStatement.getMergeBody().getStatements())
                //enclosingMergeBody.append(statement);

            // TODO: aggregate yield & jump label

            // Merge the epilogue
            //enclosingMergeBody.append(RewriteAssert.LabelledGotoStatementFrom("", startLabel));

        }

        protected static void Rewritten(final Phase phase, final GuardStatement guardStatement) throws Phase.Error {

            // Initialize a handle to the GuardStatement's Expression
            Expression expression = guardStatement.getExpression();

            // Resolve the Expression
            expression.accept(phase);

            // Assert the Expression Yields
            if(expression.doesYield()) {

                // TODO: Collect the NameExpressions from the temp statement to emplace into the GuardStatement
                // TODO: Aggregate to merge body

            }

            // Resolve the Statement, if any
            guardStatement.getStatement().accept(phase);


        }

        protected static void Rewritten(final Phase phase, final IfStatement ifStatement) throws Phase.Error {

            // Initialize a handle to the synthetic labels
            // TODO: When synthesizing labels, make sure they do not clash with another name
            // TODO: Check BarrierSet & to-be Jump labels
            // Initialize a handle to the enclosing Context's & While Statement's evaluation Expression
            //final BlockStatement enclosingMergeBody = phase.getEnclosingContext().getMergeBody();

            String label     = ifStatement.getLabel();
            String elseLabel = ifStatement.getEndLabel();

            // Assert the If Statement defines an else statement
            if(ifStatement.definesElseStatement()) {

                // Initialize a handle to the Else Statement
                final BlockStatement elseBody = ifStatement.getElseBody();

                // Initialize the Else Statement's end label
                elseBody.setEndLabel(elseLabel);

                // Initialize a handle to the Else Statement's Label
                elseLabel = (elseBody.definesLabel()) ? elseBody.getLabel() : label + "_ELSE";

                // Update the Else Statement's Label
                elseBody.setLabel(elseLabel);

            }

            // Resolve the Evaluation Expression
            // TODO: Visit Evaluation Expression, check for yielding Expressions, & Merge
            final Expression evaluationExpression = ifStatement.getEvaluationExpression();

            // Append the synthesized break condition statement
            //enclosingMergeBody.append(RewriteAssert.InvertedBreakConditionStatementFrom(
                    //ifStatement.getLabel(), elseLabel, evaluationExpression));

            // Assert the If Statement's Then Merge Body is cleared
            ifStatement.getThenBody().clearMergeBody();

            // Assert Flattened Statements
            RewriteAssert.Rewritten(phase, ifStatement.getThenBody());

            // Merge the children
            //enclosingMergeBody.aggregate(ifStatement.getThenBody().getMergeBody());

            // Assert the IfStatement defines an Else Statement
            if(ifStatement.definesElseStatement()) {

                // Aggregate the End Goto
                //enclosingMergeBody.append(RewriteAssert.LabelledGotoStatementFrom("", ifStatement.getEndLabel()));

                // Resolve the Else Statement
                ifStatement.getElseBody().accept(phase);

                // Merge the Children
                //enclosingMergeBody.aggregate(ifStatement.getElseBody().getMergeBody());

            }

        }

        protected static void Rewritten(final Phase phase, final LocalDeclaration localDeclaration) throws Phase.Error {

            // Initialize a handle to the enclosing Context's merge body
            //final BlockStatement mergeBody = phase.getContext().getMergeBody();

            // Resolve the Type
            localDeclaration.getType().accept(phase);

            // Assert the Local Declaration is initialized
            if(localDeclaration.isInitialized()) {

                // Initialize a handle to the initialization Expression
                final Expression initializationExpression = localDeclaration.getInitializationExpression();

                // Resolve the initialization Expression
                initializationExpression.accept(phase);

                // Assert the Target Expression Yields
                if(initializationExpression.doesYield()) {

                    // TODO: Collect the NameExpressions from the temp statement to emplace into the LocalDeclaration's

                }

            }

            // Append the Local Declaration as-is
            //mergeBody.append(localDeclaration);

        }

        protected static void Rewritten(final Phase phase, final ParBlock parBlock) throws Phase.Error {

            // Initialize a new Sequence
            final Sequence<Statement> result        = new Sequence<>();
            final Sequence<Statement> statements    = (Sequence<Statement>) parBlock.getBody().getStatements();

            // Iterate through each Statement
            for(final Statement statement: statements) {

                // Aggregate enrolls TODO: Originally, aggregateEnrollsOf was invoked only on statements that were not of Type ParBlock
                parBlock.aggregateEnrollsOf(statement);

                // Merge Par Block statements
                if(statement instanceof ParBlock)
                    result.merge(((ParBlock) statement).getBody());

                    // Otherwise
                else result.append(statement);

            }

            // Set the Block
            statements.clear();
            statements.merge(result);

            // Initialize a handle to the synthetic labels
            // TODO: When synthesizing labels, make sure they do not clash with another name
            // TODO: Check BarrierSet & to-be Jump labels
            final String jumpLabel  = ""; // TODO

            // Initialize a handle to the enclosing Context's Body
            //final BlockStatement enclosingMergeBody = phase.getEnclosingContext().getMergeBody();

            // Assert the WhileStatement's Merge Body is cleared
            parBlock.clearMergeBody();

            // Assert Flattened Statements
            RewriteAssert.Rewritten(phase, parBlock.getBody());

            // TODO: Aggregate statement enrolls to ParBlock for each visit

            // TODO: Aggregate b.enroll(enrollees) statements first

            // Merge the children
            //enclosingMergeBody.aggregate(parBlock.getMergeBody());

            // TODO: aggregate yield

            // Merge the epilogue
            //enclosingMergeBody.append(RewriteAssert.LabelledGotoStatementFrom(jumpLabel, parBlock.getLabel()));

        }

        protected static void Rewritten(final Phase phase, final ReturnStatement returnStatement) throws Phase.Error {

            // Resolve the Return Statement
            if(returnStatement.definesExpression()) {

                // Initialize a handle to the Expression
                final Expression expression = returnStatement.getExpression();

                // Resolve the Expression
                expression.accept(phase);

                // Assert the Expression Yields
                if(expression.doesYield()) {

                    // TODO: Collect the NameExpressions from the temp statement to emplace into the ReturnStatement
                    // TODO: Over the original Expression

                }

            }

        }

        protected static void Rewritten(final Phase phase, final SkipStatement skipStatement) throws Phase.Error {

            // Aggregate the Skip Statement
            //phase.getContext().getMergeBody().append(skipStatement);

        }

        protected static void Rewritten(final Phase phase, final StopStatement stopStatement) throws Phase.Error {


            // Aggregate the Stop Statement
            //phase.getContext().getMergeBody().append(stopStatement);


        }

        protected static void Rewritten(final Phase phase, final SuspendStatement suspendStatement) throws Phase.Error {

            // Resolve the Parameters
            // TODO: Resolve for parameter, an unroll and check if the current needs replacing
            suspendStatement.getParameters().accept(phase);

            // Aggregate the suspend statement
            //phase.getContext().getMergeBody().append(suspendStatement);


        }

        protected static void Rewritten(final Phase phase, final SwitchStatement switchStatement) throws Phase.Error {

            // TODO: Insert Yield & Jump Label
            //final BlockStatement enclosingMergeBody = phase.getContext().getMergeBody();

            if(switchStatement.getEvaluationExpression().doesYield()) {

                // Initialize a handle to the Enclosing Context's merge body & the Switch Statement's
                // evaluation expression
                final String            evaluationExpressionName    = RewriteAssert.nextTemp();
                final Expression        evaluationExpression        = switchStatement.getEvaluationExpression();
                final LocalDeclaration  localDeclaration            =
                        RewriteAssert.LocalDeclarationFrom(evaluationExpression.getType(), evaluationExpressionName);

                //enclosingMergeBody.append(localDeclaration);
                //enclosingMergeBody.append(RewriteAssert.AssignmentStatementFrom(evaluationExpressionName, evaluationExpression));

            }

            // Initialize a handle to the Switch Groups
            final Sequence<SwitchGroupStatement> switchGroups =
                    (Sequence<SwitchGroupStatement>) switchStatement.getBody().getStatements();

            for(final SwitchGroupStatement switchGroup: switchGroups) {

                // TODO: Create if statements for each label

                // TODO: Aggregate the child statements

            }

        }

        protected static void Rewritten(final Phase phase, final WhileStatement whileStatement) throws Error {

            // Initialize a handle to the current scope, the enclosing Context's Merge body, & the synthetic labels
            final Context.SymbolMap         scope               = phase.getScope()                              ;
            //final BlockStatement    enclosingMergeBody  = phase.getEnclosingContext().getMergeBody()    ;
            final String            jumpLabel           = ""                                            ; // TODO

            // Resolve the Evaluation Expression
            whileStatement.getEvaluationExpression().accept(phase);

            // TODO: Set Start label for the new yield unroll
            // TODO: When synthesizing labels, make sure they do not clash with another name
            // TODO: Check BarrierSet & to-be Jump labels
            // Assert the Evaluation Expression yields & update the While Statement's evaluation Expression
            if(whileStatement.getEvaluationExpression().doesYield())
                whileStatement.setEvaluationExpression(scope.getLatestYieldingName());

            // Append the synthesized break condition statement
            ///enclosingMergeBody.append(InvertedBreakConditionStatementFrom(
                    //whileStatement.getLabel(), whileStatement.getEndLabel(), whileStatement.getEvaluationExpression()));

            // Assert the WhileStatement's Merge Body is cleared
            whileStatement.getMergeBody().clear();

            // Assert Flattened Statements
            Rewritten(phase, whileStatement.getBody());

            // Merge the children
            //enclosingMergeBody.aggregate(whileStatement.getMergeBody());

            // Aggregate a yield
            // TODO: Check for infinite loop?
            //enclosingMergeBody.append(new Yield());

            // Merge the synthetic Goto
            //enclosingMergeBody.append(new Goto(jumpLabel, whileStatement.getLabel()));

        }

        protected static void Rewritten(final Phase phase, final TimeoutStatement timeoutStatement) throws Error {

            // Initialize a handle to the target & write Expressions
            final Expression timerExpression = timeoutStatement.getTimerExpression();
            final Expression delayExpression = timeoutStatement.getDelayExpression();

            // Resolve the Timer Statement
            timerExpression.accept(phase);

            // Assert the Expression Yields & the Timer Expression is updated
            if(timerExpression.doesYield())
                timeoutStatement.setTimerExpression(phase.getScope().getLatestYieldingName());

            // Resolve the Delay Expression
            delayExpression.accept(phase);

            // Assert the Expression Yields & the delay Expression is updated
            if(delayExpression.doesYield())
                timeoutStatement.setDelayExpression(phase.getScope().getLatestYieldingName());

        }

        protected static void Rewritten(final Phase phase, final SyncStatement syncStatement) throws Error {

            // Resolve the barrier Expression
            syncStatement.getBarrierExpression().accept(phase);

            // Assert the BarrierExpression yields & update the BarrierExpression
            if(syncStatement.getBarrierExpression().doesYield())
                syncStatement.setBarrierExpression(phase.getScope().getLatestYieldingName());

            // Merge the sync statement
            //phase.getContext().getMergeBody().append(syncStatement);

        }

        protected static void Rewritten(final Phase phase, final ProcedureTypeDeclaration procedureTypeDeclaration) throws Phase.Error {

            // Update the Body with the flattened statements
            //procedureTypeDeclaration.getBody().aggregate(procedureTypeDeclaration.getMergeBody());

        }

    }

    /// ---------------------
    /// Functional Interfaces

    /**
     * <p>Defines a functional interface for a method to be specified to the {@link Phase} class that
     * provides a means to request a {@link Compilation} specified at the {@link String}
     * file path.</p>
     * @see Phase
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 0.1.0
     */
    @FunctionalInterface
    public interface Request {

        Compilation CompilationFor(final String filepath) throws Phase.Error;

    }

}
