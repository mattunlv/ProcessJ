package org.processj.compiler.phases.phase;

import org.processj.compiler.ProcessJSourceFile;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.binary.AssignmentExpression;
import org.processj.compiler.ast.expression.constructing.NewArrayExpression;
import org.processj.compiler.ast.expression.constructing.NewMobileExpression;
import org.processj.compiler.ast.expression.literal.ArrayLiteralExpression;
import org.processj.compiler.ast.expression.literal.LiteralExpression;
import org.processj.compiler.ast.expression.literal.ProtocolLiteralExpression;
import org.processj.compiler.ast.expression.literal.RecordLiteralExpression;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.expression.result.CastExpression;
import org.processj.compiler.ast.expression.unary.UnaryPreExpression;
import org.processj.compiler.ast.expression.yielding.ChannelReadExpression;
import org.processj.compiler.ast.expression.result.InvocationExpression;
import org.processj.compiler.ast.statement.alt.AltCase;
import org.processj.compiler.ast.statement.alt.AltStatement;
import org.processj.compiler.ast.expression.*;
import org.processj.compiler.ast.statement.*;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.ast.statement.conditional.ForStatement;
import org.processj.compiler.ast.statement.conditional.IfStatement;
import org.processj.compiler.ast.statement.control.BreakStatement;
import org.processj.compiler.ast.statement.control.ContinueStatement;
import org.processj.compiler.ast.statement.control.StopStatement;
import org.processj.compiler.ast.statement.declarative.LocalDeclaration;
import org.processj.compiler.ast.statement.declarative.ProtocolCase;
import org.processj.compiler.ast.expression.result.SwitchLabel;
import org.processj.compiler.ast.statement.declarative.RecordMemberDeclaration;
import org.processj.compiler.ast.statement.yielding.ChannelWriteStatement;
import org.processj.compiler.ast.statement.yielding.ParBlock;
import org.processj.compiler.ast.type.*;
import org.processj.compiler.phases.Phases;
import org.processj.compiler.utilities.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static org.processj.compiler.utilities.Files.RetrieveMatchingFilesListFrom;
import static org.processj.compiler.utilities.Files.RetrieveMatchingFilesListWithName;
import static org.processj.compiler.utilities.Reflection.NewInstanceOf;

/**
 * <p>Class that encapsulates a compiler {@link Phase} to execute upon a {@link ProcessJSourceFile} to provide
 * proper error reporting & handling between the {@link Phase} and compiler. Allows for loosely-coupled dependencies
 * between the compiler's {@link Phase}s.</p></p>
 * @author Carlos L. Cuenca
 * @see ProcessJSourceFile
 * @version 1.0.0
 * @since 0.1.0
 */
public abstract class Phase implements Visitor<Void> {

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
    private SymbolMap           scope               ;

    /**
     * <p>The {@link ProcessJSourceFile} instance that is associated with this {@link Phase}. This field is updated
     * for each {@link Phase#execute(ProcessJSourceFile)} invocation.</p>
     */
    private ProcessJSourceFile  processJSourceFile  ;

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
        this.processJSourceFile = null      ;

    }

    /// -------
    /// Visitor

    @Override
    public final SymbolMap getScope() {

        return this.scope;

    }

    public final SymbolMap.Context getContext() {

        return this.scope.getContext();

    }

    public final SymbolMap getEnclosingScope() {

        return this.scope.getEnclosingScope();

    }

    public final SymbolMap.Context getEnclosingContext() {

        return this.scope.getEnclosingScope().getContext();

    }

    @Override
    public final void setScope(final SymbolMap symbolMap) {

        this.scope = symbolMap;

    }

    /// --------------------------
    /// Protected Abstract Methods

    /**
     * <p>Method that is invoked within {@link Phase#execute(ProcessJSourceFile)}. All Phase-dependent
     * procedures should be executed here if not following the default visitor pattern.</p>
     * @see Phase.Error
     * @since 0.1.0
     */
    protected void executePhase() throws Phase.Error {

        // Begin the traversal
        this.retrieveValidCompilation().visit(this);

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
     * <p>Returns the {@link ProcessJSourceFile} instance bound to the {@link Phase}.</p>
     * @return {@link ProcessJSourceFile} instance bound to the {@link Phase}.
     * @since 0.1.1
     */
    protected final ProcessJSourceFile getProcessJSourceFile() {

        return this.processJSourceFile;

    }

    /**
     * <p>Returns a valid {@link Compilation} instance from the {@link ProcessJSourceFile}. This method successfully
     * returns if the {@link ProcessJSourceFile} contains a valid {@link Compilation}.</p>
     * @return {@link Compilation} corresponding to the {@link ProcessJSourceFile}.
     * @since 0.1.0
     */
    protected final Compilation retrieveValidCompilation() {

        // Retrieve the ProcessJ Source File
        final ProcessJSourceFile processJSourceFile = this.getProcessJSourceFile();

        // If a null value was specified for the ProcessJ source file
        if(processJSourceFile == null)
            FatalAssert.NullProcessJSourceFile.Assert(this);

        // If the processJ source file does not contain a Compilation
        else if(!processJSourceFile.containsCompilation())
            FatalAssert.NullCompilation.Assert(this);

        // Return the compilation
        return processJSourceFile.getCompilation();

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Executes the {@link Phase}. Invokes the {@link Phase}'s specific implementation.</p>
     * @throws Phase.Error If a null value was specified for the {@link Phase}.
     * @since 0.1.0
     */
    public final void execute(final ProcessJSourceFile processJSourceFile) throws Phase.Error {

        // If a null value was specified for the Listener, emit the error
        if(this.listener == null)
            FatalAssert.NullListener.Assert(this);

        // If a null value was specified for the ProcessJ source file
        else if(processJSourceFile == null)
            FatalAssert.NullProcessJSourceFile.Assert(this);

        // If the file has not been completed by this
        if(!processJSourceFile.hasBeenCompletedBy(this)) {

            // Otherwise, update the ProcessJSourceFile
            this.processJSourceFile = processJSourceFile;

            // Execute the phase
            this.executePhase();

            // Mark the file as completed by this Phase
            processJSourceFile.setCompletedPhase(this);

            // Clear
            this.processJSourceFile = null;

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

    }

    public static class Info extends Message {

        /// ------------
        /// Constructors

        protected Info(final Phase phase) {
            super(phase);
        }

        /// -----------------
        /// Protected Methods

        public final void commit() {

            this.getPhase().getListener().notify(this);

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

            // Initialize a handle to the message instance
            Phase.Error messageInstance;

            try {

                messageInstance = NewInstanceOf(messageType, phase, parameters);

            } catch(final InvocationTargetException | InstantiationException | IllegalAccessException exception) {

                // TODO: Fatal Error
                messageInstance = null;

            }

            // Simple assert
            assert messageInstance != null;

            // Notify the listener
            phase.getListener().notify(messageInstance);

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
    public static class Listener {

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

            final ProcessJSourceFile file = phaseInfo.getPhase().getProcessJSourceFile();

            // Simply log the info
            Info.Log(file + ": " + phaseInfo.getMessage());

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

            final ProcessJSourceFile file = phaseWarning.getPhase().getProcessJSourceFile();

            // Simply log the warning
            Warning.Log(file + ": " + phaseWarning.getMessage());

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

            final ProcessJSourceFile file = phaseError.getPhase().getProcessJSourceFile();

            // Log the message
            Error.Log(file + ": " + phaseError.getMessage());

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

        protected Compilation onRequestCompilation(final String filepath, final String packageName)
                throws Phase.Error { return null; }

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

    /// ----------
    /// Assertions

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
         * <p>{@link Phase.Error} to be emitted if the {@link Phase}'s corresponding {@link ProcessJSourceFile}
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

    protected static class ParserAssert {

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ProcessJSourceFile} failed to open during
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
            protected static void Assert(final Phase phase, final ProcessJSourceFile processJSourceFile) {

                Error.Assert(FatalAssert.NullListener.class, phase);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>Invalid file.</p>
             */
            private final ProcessJSourceFile invalidFile;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link FileOpenFailure} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected FileOpenFailure(final Phase culprit, final ProcessJSourceFile processJSourceFile) {
                super(culprit);

                this.invalidFile = processJSourceFile;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ProcessJSourceFile} failed to parse.</p>
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
            protected static void Assert(final Phase phase, final ProcessJSourceFile processJSourceFile) {

                Error.Assert(ParserFailure.class, phase);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>Invalid file.</p>
             */
            private final ProcessJSourceFile invalidFile;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ParserFailure} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ParserFailure(final Phase culprit, final ProcessJSourceFile processJSourceFile) {
                super(culprit);

                this.invalidFile = processJSourceFile;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if a {@link ProcessJSourceFile}'s {@link ProcessJParser} encountered an
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
         * <p>{@link Phase.Error} to be emitted if a {@link ProcessJSourceFile} contained invalid syntax.</p>
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
         * <p>{@link Phase.Error} to be emitted if a {@link ProcessJSourceFile} encountered an illegal cast expression.</p>
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
         * <p>{@link Phase.Error} to be emitted if a {@link ProcessJSourceFile} encoded a malformed Package Access.</p>
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

    protected static class ImportAssert {

        /// ------------------------
        /// Protected Static Methods

        private static void UnambiguousImportFile(final Phase phase,
                                                  final String packageName,
                                                  final List<String> files) throws Phase.Error {

            // Assert that the amount of found files is strictly one
            if(files.size() != 1) AmbiguousImportFile.Assert(phase, packageName, files);

        }

        private static void FileExists(final Phase phase,
                                       final String packageName,
                                       final String filepath,
                                       final List<String> files) throws Phase.Error {

            // Initialize a handle to the result
            final boolean fileExists = !files.isEmpty();

            // Assert files were found
            if(!fileExists)
                FileNotFound.Assert(phase, packageName, filepath);

            // Assert the File Import was not ambiguous
            UnambiguousImportFile(phase, packageName, files);

        }

        private static void PackageExists(final Phase phase,
                                          final List<String> files,
                                          final String packageName,
                                          final boolean isWildcard) throws Phase.Error {

            // Initialize a handle to the result
            final boolean packageNotFound = files.isEmpty() && isWildcard;

            // Assert files were found
            if(packageNotFound)
                PackageNotFound.Assert(phase, packageName, isWildcard);

        }

        /**
         * <p>Attempts to import the file corresponding with the specified fully-qualified package name.</p>
         * @param packageName The {@link String} value of the fully-qualified package name corresponding with the
         *                    file to import.
         * @throws Error If the import failed.
         * @since 0.1.0
         */
        protected static void ImportSpecified(final Phase phase,
                                              final String packageName) throws Error {

            // Initialize the last separator index, path & file name
            final int       separatorIndex  = packageName.lastIndexOf('.');
            final String    path            = packageName.substring(0, separatorIndex).replace('.', '/') + '/';
            final String    filename        = packageName.substring(separatorIndex + 1);

            // Set the wildcard flag
            final boolean isWildcard = filename.equals("*");

            // Retrieve the list of files
            final List<String> files = (isWildcard)
                    ? RetrieveMatchingFilesListFrom(path, Phases.IncludePaths, Phases.ProcessJSourceFileRegex)
                    : RetrieveMatchingFilesListWithName(path, filename, Phases.IncludePaths, Phases.ProcessJSourceFileRegex);

            // Assert the package import was successful
            PackageExists(phase, files, packageName, isWildcard);

            // Assert the file import was successful & not ambiguous
            FileExists(phase, packageName, path, files);

            // Aggregate all the files
            for(final String filePath: files) {

                // Initialize a handle to the Compilation using the fully-qualified package name
                Compilation compilation = phase.getListener().onRequestCompilation(filePath, packageName);

                // TODO: Assert the Compilation has had its' Imports resolved
                if(compilation != null) {

                    // Begin the visitation
                    compilation.visit(phase);

                    // Restore the Scope
                    phase.setScope(phase.getScope());

                    // Emplace the Compilation's scope into the Compilation's Import Symbols
                    phase.getScope().putImport(packageName, compilation.openScope());

                }

            }

        }

        /// -----------
        /// Phase.Error

        /**
         * <p>{@link Phase.Error} to be emitted if the target import file was ambiguous.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class AmbiguousImportFile extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link AmbiguousImportFile} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param packageName The {@link String} value of the specified package name
             * @param files The total amount of found files.
             */
            protected static void Assert(final Phase phase, final String packageName, final List<String> files) {

                Phase.Error.Assert(PackageNotFound.class, phase, packageName, files);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The total amount of found files.</p>
             */
            private final List<String>  foundFiles    ;

            /**
             * <p>The {@link String} value of the package name.</p>
             */
            private final String        packageName    ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link AmbiguousImportFile} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected AmbiguousImportFile(final Phase culprit, final String packageName, final List<String> foundFiles) {
                super(culprit);

                this.packageName = packageName  ;
                this.foundFiles  = foundFiles   ;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the target import file was not found.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class FileNotFound extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link FileNotFound} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param packageName The {@link String} value of the specified package name
             * @param filepath Flag indicating if the package import was a wildcard import
             */
            protected static void Assert(final Phase phase, final String packageName, final String filepath) {

                Phase.Error.Assert(FileNotFound.class, phase, packageName, filepath);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The original filepath.</p>
             */
            private final String filepath;

            /**
             * <p>The {@link String} value of the package name.</p>
             */
            private final String packageName    ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link FileNotFound} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected FileNotFound(final Phase culprit, final String packageName, final String filepath) {
                super(culprit);

                this.packageName = packageName  ;
                this.filepath = filepath   ;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the target import package was not found.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class PackageNotFound extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link PackageNotFound} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param packageName The {@link String} value of the specified package name
             * @param isWildcard Flag indicating if the package import was a wildcard import
             */
            protected static void Assert(final Phase phase, final String packageName, final boolean isWildcard) {

                Phase.Error.Assert(PackageNotFound.class, phase, packageName, isWildcard);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>Flag indicating if the package was specified as a wildcard.</p>
             */
            private final boolean isWildcard    ;

            /**
             * <p>The {@link String} value of the package name.</p>
             */
            private final String packageName    ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link DeclarationAssert.TypeDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected PackageNotFound(final Phase culprit,
                                      final String packageName,
                                      final boolean isWildcard) {
                super(culprit);

                this.packageName = packageName  ;
                this.isWildcard  = isWildcard   ;

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
            final SymbolMap     scope   = phase.getScope();
            final List<Object>  result  = scope.get(procedureTypeDeclaration.toString());

            // If the procedure has not been defined
            if(!result.isEmpty()) {

                // Initialize a handle to the result
                final Object preliminary = result.get(0);

                // Assert for all overloads, the Procedure Type or the existing Type are not declared 'mobile'
                if(!(preliminary instanceof SymbolMap))
                    NonProcedureTypeDefined.Assert(phase, procedureTypeDeclaration);

                // Assert for all existing Procedure Types, the Procedure Type of the existing Type are not declared
                // 'mobile'
                scope.forEachEntryUntil(type -> MobileProcedureNotOverloaded(phase, (Type) type, procedureTypeDeclaration)
                        || NotOverloadingNonMobileProcedure(phase, (Type) type, procedureTypeDeclaration));

                // Cast the result
                final SymbolMap procedures = (SymbolMap) result;

                // Assert the procedure definition is unique
                if(!procedures.put(procedureTypeDeclaration.getSignature(), procedureTypeDeclaration))
                    TypeDefined.Assert(phase, procedureTypeDeclaration);

            // Otherwise
            } else {

                // Initialize a new SymbolMap
                final SymbolMap symbolMap = new SymbolMap();

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
            final SymbolMap.Context enclosingContext = phase.getScope()
                    .forEachContextRetrieve(context -> (context instanceof ParBlock) || (context instanceof AltStatement));

            // Initialize a handle to the result
            final boolean enclosingContextParallelOrChoice = enclosingContext != null;

            // Assert the construct is not enclosed in a parallel Context
            if(enclosingContext instanceof ParBlock)
                EnclosedInParallelContext.Assert(phase, construct, phase.getScope().getContext());

            // Assert the construct is not enclosed in a choice Context
            if(enclosingContext instanceof AltStatement)
                EnclosedInParallelContext.Assert(phase, construct, phase.getScope().getContext());

            // Return the result
            return !enclosingContextParallelOrChoice;

        }

        @SafeVarargs
        protected static void EnclosingIterativeContextBreaksAndReachable(final Phase phase,
                                                                          final Statement statement,
                                                                          final Set<String>... failures)
                throws Phase.Error {

            // Initialize a handle to the Context
            final SymbolMap.Context enclosingContext = phase.getScope().forEachContextRetrieve(
                    context -> (context instanceof ConditionalStatement)
                            || (context instanceof ParBlock));

            // Assert the statement's enclosing Context isn't parallel
            if(enclosingContext instanceof ParBlock)
                EnclosedInParallelContext.Assert(phase, statement, enclosingContext);

            // Initialize a handle to the result
            final boolean enclosingIterativeContextBreaks = enclosingContext != null;

            // Assert the Statement is enclosed in a Breakable Context
            if(!enclosingIterativeContextBreaks)
                NotEnclosedInIterativeContext.Assert(phase, statement, phase.getScope().getContext());

            // Assert that the enclosing Context does not define a constant true evaluation expression
            else if(TypeAssert.IsConstantTrue(
                    ((ConditionalStatement) enclosingContext).getEvaluationExpression()))
                EnclosingIterativeContextDoesNotTerminate.Assert(phase, statement, enclosingContext);

            // Assert that the enclosing Context does not define a constant false evaluation expression
            else if(TypeAssert.IsConstantFalse(
                    ((ConditionalStatement) enclosingContext).getEvaluationExpression()))
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
         * <p>{@link Phase.Error} to be emitted if the conditional {@link org.processj.compiler.ast.SymbolMap.Context}'s
         * alternative {@link org.processj.compiler.ast.SymbolMap.Context} is not reachable.</p>
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
         * <p>{@link Phase.Error} to be emitted if the conditional {@link org.processj.compiler.ast.SymbolMap.Context}
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
         * <p>{@link Phase.Error} to be emitted if the {@link org.processj.compiler.ast.SymbolMap.Context} does not
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
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(ContextDoesNotTerminate.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

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
                                              final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an {@link AST} is enclosed in a choice
         * {@link org.processj.compiler.ast.SymbolMap.Context}.</p>
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
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(EnclosedInChoiceContext.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

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
                                              final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an {@link AST} is enclosed in a parallel
         * {@link org.processj.compiler.ast.SymbolMap.Context}.</p>
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
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(EnclosedInParallelContext.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

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
                                                final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the enclosing {@link org.processj.compiler.ast.SymbolMap.Context}
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
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(EnclosingIterativeContextDoesNotTerminate.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

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
                                                                final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the enclosing {@link org.processj.compiler.ast.SymbolMap.Context}
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
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(EnclosingIterativeContextIsNotReachable.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

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
                                                              final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an {@link AST} is not enclosed in an iterative
         * {@link org.processj.compiler.ast.SymbolMap.Context}.</p>
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
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(NotEnclosedInIterativeContext.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

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
                                                    final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an {@link AST} is not enclosed in an breakable
         * {@link org.processj.compiler.ast.SymbolMap.Context}.</p>
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
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(NotEnclosedInIterativeContext.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

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
                                                    final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

    }

    protected static class RewriteAssert {

        public static int labelNo = 0;
        private static int tempCounter = 0;

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

        protected static void FlattenedAltStatement(final AltStatement altStatement) throws Phase.Error {

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

        }

        protected static void FlattenedParBlock(final ParBlock parBlock) throws Phase.Error {

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

        }

        public static void Flattened(final Phase phase, final BlockStatement body) throws Error {

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
                statement.visit(phase);

                // TODO: Logic for final Statement

            }

            // Clear the current body
            body.clear();

        }

        public static Expression YieldedUnrolledInto(final BlockStatement blockStatement, final Expression expression)
                throws Error {

            // Initialize a handle to the Expression
            Expression resultExpression = expression;

            // Assert the Target Expression Yields
            if(expression.doesYield()) {

                // Initialize a handle to the Local Name & Type
                final String localName  = nextTemp();
                final Type   type       = expression.getType();

                // Aggregate the synthetic Statements
                blockStatement.append(LocalDeclarationFrom(type, localName));
                blockStatement.append(AssignmentStatementFrom(localName, expression));

                // Synthesize a Name Expression for the Result
                resultExpression = new NameExpression(new Name(localName));

            }

            // Return the result
            return resultExpression;

        }

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
         * hierarchy using the {@link SymbolMap} to resolve the ancestor {@link ProtocolTypeDeclaration} names.</p>
         * @param members The {@link Set} where all the {@link Name}s are aggregated.
         * @param symbolMap The {@link SymbolMap} that is used to resolve all ancestor {@link ProtocolTypeDeclaration} names.
         * @param protocolTypeDeclaration The {@link ProtocolTypeDeclaration} to recur from.
         * @since 0.1.0
         */
        private static void EmplaceAncestorNames(final Set<Name> members, final SymbolMap symbolMap,
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

                Log.log(protocolTypeDeclaration, "adding member " + ancestorName);

                if(!members.add(ancestorName))
                    Log.log(protocolTypeDeclaration, String.format("Name '%s' already in (%s)",
                            ancestorName, protocolTypeDeclaration));

            }

        }

        private static void RewriteProtocol(final Phase phase, final ProtocolTypeDeclaration protocolTypeDeclaration) {

            Log.log(protocolTypeDeclaration, "Visiting a RecordTypeDecl (" + protocolTypeDeclaration + ")");

            // Initialize the Set of Names
            final Set<Name> ancestors = new LinkedHashSet<>();

            // Recursively aggregate the Name
            EmplaceAncestorNames(ancestors, phase.getScope(), protocolTypeDeclaration);

            // Clear the existing Names
            protocolTypeDeclaration.extend().clear();

            // Emplace all the RecordMembers
            ancestors.forEach(name -> protocolTypeDeclaration.extend().append(name));

            // Log
            Log.log(protocolTypeDeclaration, String.format("record %s with %s member(s)",
                    protocolTypeDeclaration, protocolTypeDeclaration.extend().size()));

            // Print
            protocolTypeDeclaration.extend().forEach(name ->
                    Log.log(protocolTypeDeclaration, "> Ancestor: " + name));

        }

        /**
         * <p>Recursively aggregates all of the {@link RecordMemberDeclaration}s in the {@link RecordTypeDeclaration}'s inheritance
         * hierarchy using the {@link SymbolMap} to resolve the ancestor {@link RecordTypeDeclaration} names.</p>
         * @param members The {@link Set} where all the {@link RecordMemberDeclaration}s are aggregated.
         * @param symbolMap The {@link SymbolMap} that is used to resolve all ancestor {@link RecordTypeDeclaration} names.
         * @param recordTypeDeclaration The {@link RecordTypeDeclaration} to recur from.
         * @since 0.1.0
         */
        private static void EmplaceRecordMembers(final Set<RecordMemberDeclaration> members, final SymbolMap symbolMap,
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

                Log.log(recordTypeDeclaration, "adding member " + recordMemberDeclaration.getType() + " " + recordMemberDeclaration);

                if(!members.add(recordMemberDeclaration))
                    Log.log(recordTypeDeclaration, String.format("Name '%s' already in (%s)",
                            recordMemberDeclaration, recordTypeDeclaration));

            }

        }

        private static void RewriteRecord(final Phase phase, final RecordTypeDeclaration recordTypeDeclaration) {

            Log.log(recordTypeDeclaration, "Visiting a RecordTypeDecl (" + recordTypeDeclaration + ")");

            // Initialize the Set of RecordMembers
            final Set<RecordMemberDeclaration> members = new LinkedHashSet<>();

            // Recursively aggregate the RecordMembers
            EmplaceRecordMembers(members, phase.getScope(), recordTypeDeclaration);

            // Clear the existing RecordMembers
            recordTypeDeclaration.getBody().clear();

            // Emplace all the RecordMembers
            members.forEach(recordMember -> recordTypeDeclaration.getBody().append(recordMember));

            // Log
            Log.log(recordTypeDeclaration, String.format("record %s with %s member(s)",
                    recordTypeDeclaration, recordTypeDeclaration.getBody().size()));

            // Print
            recordTypeDeclaration.getBody().forEach(recordMember ->
                    Log.log(recordTypeDeclaration, "> member " + recordMember.getType() + " " + recordMember.getName()));


        }
    }

    // TODO: Tidy
    protected static class SemanticAssert {

        protected static void SetEnclosingContextYields(final Phase phase)
                throws Phase.Error {

            phase.getScope().forEachContextUntil(SymbolMap.Context::setYields);

        }

        protected static void PriAltNotEnclosedByAltStatement(final Phase phase,
                                                              final AltStatement altStatement)
                throws Phase.Error {

            phase.getScope().forEachContextUntil(context -> {

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

            phase.getScope().forEachContextUntil(context -> {

                final boolean found = context instanceof AltCase;

                // Assert the Context is not an Alt Case & does not define an Input Guard
                if(found && ((AltCase) context).definesInputGuardExpression()) {

                    // Initialize a handle to the input guard Expression's Channel Read Expression
                    final AssignmentExpression inputGuardExpression = ((AltCase) context).getInputGuardExpression();

                    // Assert the Channel Read's Expression is not the specified Expression
                    if((inputGuardExpression != null) && inputGuardExpression.getRightExpression() == expression) {

                        // Initialize a handle to the Scope
                        final SymbolMap scope = context.openScope();

                        // Assert any enclosing alt statements are not replicated alts
                        scope.forEachEnclosingContext(outerContext -> {
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

            phase.getScope().forEachContextUntil(context -> {

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

            phase.getScope().forEachContextUntil(context -> {

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

            phase.getScope().forEachContextUntil(context -> {

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

            phase.getScope().forEachEntryUntil(context -> {

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

            phase.getScope().forEachContextUntil(context -> {

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

            phase.getScope().forEachEntryUntil(context -> {

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
            final SymbolMap.Context context = phase.getScope().getContext();

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

            phase.getScope().forEachContextUntil(context -> {

                final boolean found = (context instanceof ForStatement) && ((ForStatement) context).isPar();

                if(found) ((ForStatement) context).vars.add(expression);

                return found;

            });

        }

        /// -----------
        /// Phase.Error

        /**
         * <p>{@link Phase.Error} to be emitted if a parallel {@link org.processj.compiler.ast.SymbolMap.Context}
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
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final SymbolMap.Context context) {

                Phase.Error.Assert(ParallelContextEmpty.class, phase, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

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
                                           final SymbolMap.Context context) {
                super(culprit);

                this.context      = context                                         ;
                this.contextClass = (context != null) ? context.getClass() : null   ;

            }

        }

    }

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

                // Assert the result is a SymbolMap
                if(result instanceof SymbolMap)
                    ((NewMobileExpression) expression).setCandidates((SymbolMap) result);

            } else if(expression instanceof InvocationExpression) {

                // Assert the result is a SymbolMap
                if(result instanceof SymbolMap)
                    ((InvocationExpression) expression).setCandidates((SymbolMap) result);

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

                // Assert the result is a SymbolMap
                if(!(result instanceof SymbolMap))
                    NonProcedureImplements.Assert(phase, procedureTypeDeclaration);

                // Return the result
                return (SymbolMap) result;

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

                // Assert the result is a SymbolMap
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

                // Assert the result is a SymbolMap
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

    // TODO: Tidy
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
         * <p>Validates the {@link ConstantDeclaration}'s native declaration.</p>
         * @param validatePragmas The invoking {@link Phase}
         * @param constantDeclaration The {@link ConstantDeclaration} to validate
         * @param isNative flag indicating if the containing {@link Compilation} is specified as NATIVE
         * @throws Error If the {@link ConstantDeclaration} is invalid.
         * @since 0.1.0
         */
        protected static void ValidateNativeConstantDeclaration(final Phase validatePragmas,
                                                                final ConstantDeclaration constantDeclaration,
                                                                final boolean isNative)
                throws Error {

            if(!isNative && !constantDeclaration.isDeclaredNative())
                throw new PragmaAssert.ConstantDeclarationNonNativeException(validatePragmas, constantDeclaration).commit();

            else if(isNative && constantDeclaration.isDeclaredNative())
                throw new PragmaAssert.IllegalConstantDeclarationNativeException(validatePragmas, constantDeclaration).commit();

            else if(constantDeclaration.isInitialized())
                throw new PragmaAssert.ConstantDeclarationInitializedException(validatePragmas, constantDeclaration).commit();

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
                throw new PragmaAssert.ConstantDeclarationNativeException(validatePragmas, constantDeclaration).commit();

            else if(!constantDeclaration.isInitialized())
                throw new PragmaAssert.ConstantDeclarationNotInitializedException(validatePragmas, constantDeclaration);

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
                throw new PragmaAssert.ProcedureDeclarationDoesNotDefineBodyException(validatePragmas,
                        procedureTypeDeclaration).commit();

            // Procedures must not be declared native
            else if(procedureTypeDeclaration.isNative())
                throw new PragmaAssert.ProcedureDeclarationNativeException(validatePragmas,
                        procedureTypeDeclaration).commit();

        }

        /**
         * <p>Validates the {@link ProcedureTypeDeclaration}'s native procedure declaration.</p>
         * @param validatePragmas The invoking {@link Phase}
         * @param procedureTypeDeclaration The {@link ProcedureTypeDeclaration} to validate
         * @throws Error If the {@link ProcedureTypeDeclaration} is invalid.
         * @since 0.1.0
         */
        protected static void ValidateNativeProcedureDeclaration(final Phase validatePragmas,
                                                                 final ProcedureTypeDeclaration procedureTypeDeclaration)
                throws Error {

            // NATIVELIB and NATIVE library files cannot contain procedures with ProcessJ bodies.
            if(procedureTypeDeclaration.definesBody())
                throw new PragmaAssert.ProcedureDeclarationDefinesBodyException(validatePragmas, procedureTypeDeclaration).commit();

                // If the Procedure is not declared native
            else if(!procedureTypeDeclaration.isNative())
                throw new PragmaAssert.ProcedureDeclarationNonNativeException(validatePragmas, procedureTypeDeclaration).commit();

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
                throw new PragmaAssert.IllegalProcedureReturnTypeException(validatePragmas, procedureTypeDeclaration).commit();

            // If the Procedure specifies a barrier or timer return type
            else if(returnType.isBarrierType() || returnType.isTimerType())
                throw new PragmaAssert.IllegalProcedureBarrierOrTimerReturnTypeException(validatePragmas,
                        procedureTypeDeclaration).commit();

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
                    throw new PragmaAssert.IllegalProcedureParameterTypeException(validatePragmas,
                            procedureTypeDeclaration, parameterType).commit();

                    // Otherwise, If the procedure specifies a barrier or timer parameter
                else if(parameterType.isBarrierType() || parameterType.isTimerType())
                    throw new PragmaAssert.IllegalProcedureBarrierOrTimerParameterTypeException(validatePragmas,
                            procedureTypeDeclaration, parameterType).commit();

            }

        }

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
                    throw new PragmaAssert.IllegalPragmaException(phase, pragma, pragma.getLine()).commit();

                    // If the Pragma requires arguments but none were given
                else if(PragmaAssert.PragmaArguments.get(pragma.toString()) > 0 && pragma.getValue() == null)
                    throw new PragmaAssert.PragmaRequiresArgumentException(phase, pragma, pragma.getLine()).commit();

                    // If the Pragma requires no arguments, but some were given
                else if(PragmaAssert.PragmaArguments.get(pragma.toString()) == 0 && pragma.getValue() != null)
                    throw new PragmaAssert.PragmaRequiresNoArgumentException(phase, pragma, pragma.getLine()).commit();

                    // If the Pragma has already been decoded
                else if(pragmaMap.containsKey(pragma.toString()))
                    throw new PragmaAssert.PragmaAlreadySpecifiedException(phase, pragma, pragma.getLine()).commit();

                // Send out the message
                new PragmaAssert.EnteringPragma(phase, pragma).commit();

                // Aggregate the Pragma
                pragmaMap.put(pragma.toString(), pragma.getValue().replace("\"", ""));

            }

            // Return the result
            return pragmaMap;

        }

        /**
         * <p>Validates the specified {@link Pragma} {@link Map} to make sure the {@link Compilation}'s
         * defined {@link Pragma}s are consistent.</p>
         * @param phase The invoking {@link Phase}.
         * @param pragmaMap The {@link Map} containing {@link Pragma}s as key-value pairs.
         * @throws Error If the {@link Compilation}'s {@link Pragma}s are invalid.
         * @since 0.1.0
         */
        protected static void ValidatePragmaMap(final Phase phase,
                                                final Map<String, String> pragmaMap) throws Error {

            // Initialize the flags
            final boolean isNative          = pragmaMap.containsKey("NATIVE");
            final boolean isNativeLibrary   = pragmaMap.containsKey("NATIVELIB");

            // If a LANGUAGE Pragma is not specified, throw an error
            if(!pragmaMap.containsKey("LANGUAGE"))
                throw new PragmaAssert.MissingLanguagePragmaException(phase).commit();

            // If a FILE Pragma is not specified
            else if(!pragmaMap.containsKey("FILE"))
                throw new PragmaAssert.MissingFilePragmaException(phase).commit();

            // If NATIVE & NATIVELIB are both defined
            if(isNative && isNativeLibrary)
                throw new PragmaAssert.NativeAndNativeLibException(phase).commit();

            // Retrieve the specified language
            final String language = pragmaMap.get("LANGUAGE");

            // If Native is specified but c wasn't the specified language
            if((isNative || isNativeLibrary) && !language.equals("C"))
                throw (language.equals("PROCESSJ") ? new NativeWithProcessJException(phase)
                        : new InvalidNativeLanguageException(phase, language)).commit();

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
        protected static void GenerateHeaderFileFrom(final Phase validatePragmas,
                                                     final String sourceFilename,
                                                     final String includeFilename,
                                                     final List<String> signatures) throws Error {

            // Initialize the header file name
            final String headerFileName = sourceFilename + ".h";

            // Emit the informative message
            new ToGenerateNativeLibraryHeaderFileWrite(validatePragmas, headerFileName).commit();

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
                new NativeLibraryHeaderFileWriteSuccess(validatePragmas, includeFilename).commit();

                // Otherwise
            } catch(final IOException exception) {

                // Transform the error.
                throw new PragmaAssert.NativeLibraryHeaderFileWriteFailureException(validatePragmas, headerFileName).commit();

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
        protected static void GenerateImplementationFileFrom(final Phase validatePragmas,
                                                             final String sourceFileName,
                                                             final List<String> signatures) throws Error {

            // Initialize the header file name
            final String implementationFileName = sourceFileName + ".c";
            final String headerFileName         = sourceFileName + ".h";

            // Emit the informative message
            new ToGenerateNativeLibraryImplementationFile(validatePragmas, implementationFileName).commit();

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

                new NativeLibraryImplementationFileWriteSuccess(validatePragmas,
                        implementationFileName, headerFileName).commit();

            // Otherwise
            } catch(final IOException exception) {

                throw new PragmaAssert.NativeLibraryImplementationFileWriteFailureException(validatePragmas, headerFileName);

            }

            // TODO: Potentially move the generated file to lib/C/include & update the success message to omit that spec

        }

        /**
         * <p>Generates the native library code from the specified parameters.</p>
         * @param validatePragmas The invoking {@link Phase}.
         * @param nativeSignatures Any procedure signatures generated by the {@link Phase}.
         * @param packageName The package name corresponding to the current {@link Compilation}.
         * @param nativeLibraryFilename The filename corresponding to the native file name
         * @param processJHeaderFilename The filename corresponding to the processj header.
         * @param sourceFilename The filename corresponding to the generated implementation
         * @throws Error If the generation fails.
         * @since 0.1.0
         */
        protected static void GenerateNativeLibraryCodeFrom(final Phase validatePragmas,
                                                            final List<String> nativeSignatures,
                                                            final String packageName,
                                                            final String nativeLibraryFilename,
                                                            final String processJHeaderFilename,
                                                            final String sourceFilename) throws Error {

            // Print the Informative messages
            new ToGenerateNativeLibrary(validatePragmas, nativeLibraryFilename).commit();
            new ToGenerateNativeLibraryHeaderProcessJHeaderFile(validatePragmas, processJHeaderFilename).commit();

            // TODO: Maybe we want to retrieve strings for these and store them into the current ProcessJSource File object
            // TODO: for writing and/or processing to be handled elsewhere. This Phase should only concern itself with
            // TODO: Pragma validation & processing rather than file writing
            // Generate the files
            GenerateHeaderFileFrom(validatePragmas, sourceFilename, nativeLibraryFilename, nativeSignatures);
            GenerateImplementationFileFrom(validatePragmas, sourceFilename, nativeSignatures);

            // Print the last informative message
            new NativeLibraryFileWriteSuccess(validatePragmas, processJHeaderFilename,
                    packageName + "/" + processJHeaderFilename + ".inc").commit();

        }

        /**
         * <p>{@link Info} class that encapsulates the pertinent information of a Pragma that is being registered.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class EnteringPragma extends Info {

            /// --------------
            /// Private Fields

            /**
             * <p>The invalid {@link Pragma} instance.</p>
             */
            private final Pragma pragma        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link EnteringPragma} with the culprit instance, current line,
             * & current line count.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param pragma The invalid {@link Pragma}.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected EnteringPragma(final Phase culpritInstance, final Pragma pragma) {
                super(culpritInstance);

                this.pragma = pragma            ;

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

                // Return the resultant error message
                return "Entering <" + this.pragma
                        + ((this.pragma.getValue() != null)
                        ? ',' + this.pragma.getValue().substring(1, this.pragma.getValue().length() - 1)
                        : "") + "> into pragmaTable.";

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information detected LIBRARY {@link Pragma}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class LibraryPragmaDetected extends Info {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "LIBRARY pragma detected";

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link Phase}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected LibraryPragmaDetected(final Phase culpritInstance) {
                super(culpritInstance);
            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a native library.</p>
         * @see Phase
         * @see Info
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ToGenerateNativeLibrary extends Info {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Native Library";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the to generate native library.</p>
             */
            private final String nativeLibraryName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ToGenerateNativeLibrary}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param nativeLibraryName The {@link String} value of the to generate native library.
             * @see Phase
             * @see Info
             * @since 0.1.0
             */
            protected ToGenerateNativeLibrary(final Phase culpritInstance,
                                              final String nativeLibraryName) {
                super(culpritInstance);

                this.nativeLibraryName = nativeLibraryName;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.nativeLibraryName;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a native library processj header file.</p>
         * @see Phase
         * @see Info
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ToGenerateNativeLibraryHeaderProcessJHeaderFile extends Info {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "ProcessJ header file";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the to generate native library processj header file.</p>
             */
            private final String nativeLibraryProcessJHeaderFileName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ToGenerateNativeLibraryHeaderProcessJHeaderFile}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param nativeLibraryProcessJHeaderFileName The {@link String} value of the to generate native library processj header file.
             * @see Phase
             * @see Info
             * @since 0.1.0
             */
            protected ToGenerateNativeLibraryHeaderProcessJHeaderFile(final Phase culpritInstance,
                                                                      final String nativeLibraryProcessJHeaderFileName) {
                super(culpritInstance);

                this.nativeLibraryProcessJHeaderFileName = nativeLibraryProcessJHeaderFileName;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.nativeLibraryProcessJHeaderFileName;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a native library header file.</p>
         * @see Phase
         * @see Info
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ToGenerateNativeLibraryHeaderFileWrite extends Info {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "ProcessJ C header file";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the to generate native library header file.</p>
             */
            private final String nativeLibraryHeaderFileName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ToGenerateNativeLibraryHeaderFileWrite}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param nativeLibraryHeaderFileName The {@link String} value of the to generate native library header file.
             * @see Phase
             * @see Info
             * @since 0.1.0
             */
            protected ToGenerateNativeLibraryHeaderFileWrite(final Phase culpritInstance,
                                                             final String nativeLibraryHeaderFileName) {
                super(culpritInstance);

                this.nativeLibraryHeaderFileName = nativeLibraryHeaderFileName;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Retrieve the culprit & initialize the StringBuilder
                final Phase culpritInstance = (Phase) this.getPhase();

                // Return the resultant error message
                return Message + ": " + this.nativeLibraryHeaderFileName;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a native library implementation file.</p>
         * @see Phase
         * @see Info
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ToGenerateNativeLibraryImplementationFile extends Info {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "ProcessJ C implementation file";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the to generate native library implementation file.</p>
             */
            private final String nativeLibraryHeaderFileName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ToGenerateNativeLibraryImplementationFile}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param nativeLibraryHeaderFileName The {@link String} value of the to generate native library implementation file.
             * @see Phase
             * @see Info
             * @since 0.1.0
             */
            protected ToGenerateNativeLibraryImplementationFile(final Phase culpritInstance,
                                                                final String nativeLibraryHeaderFileName) {
                super(culpritInstance);

                this.nativeLibraryHeaderFileName = nativeLibraryHeaderFileName;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.nativeLibraryHeaderFileName;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a native library file output
         * success.</p>
         * @see Phase
         * @see Info
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NativeLibraryHeaderFileWriteSuccess extends Info {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Generated file";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the generated native library header file.</p>
             */
            private final String nativeLibraryHeaderFileName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeLibraryHeaderFileWriteSuccess}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param nativeLibraryHeaderFileName The {@link String} value of the generated native library header file.
             * @see Phase
             * @see Info
             * @since 0.1.0
             */
            protected NativeLibraryHeaderFileWriteSuccess(final Phase culpritInstance,
                                                          final String nativeLibraryHeaderFileName) {
                super(culpritInstance);

                this.nativeLibraryHeaderFileName = nativeLibraryHeaderFileName;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.nativeLibraryHeaderFileName + " - this file must be moved to lib/C/include/";

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a native library file output
         * success.
         * </p>
         * @see Phase
         * @see Info
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NativeLibraryImplementationFileWriteSuccess extends Info {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Constant declaration must be declared native";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the generated native library implementation file.</p>
             */
            private final String nativeLibraryImplementationFileName;

            /**
             * <p>The {@link String} value of the corresponding native library header file.</p>
             */
            private final String nativeLibraryHeaderFileName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeLibraryImplementationFileWriteSuccess}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NativeLibraryImplementationFileWriteSuccess(final Phase culpritInstance,
                                                                  final String nativeLibraryImplementationFileName,
                                                                  final String nativeLibraryHeaderFileName) {
                super(culpritInstance);

                this.nativeLibraryImplementationFileName    = nativeLibraryImplementationFileName   ;
                this.nativeLibraryHeaderFileName            = nativeLibraryHeaderFileName           ;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Retrieve the culprit & initialize the StringBuilder
                final Phase culpritInstance = (Phase) this.getPhase();

                // Return the resultant error message
                return Message + ": " + this.nativeLibraryImplementationFileName + " with " + this.nativeLibraryHeaderFileName
                        + " - this file must be moved to lib/C/include/";

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a native library file output
         * success.</p>
         * @see Phase
         * @see Info
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NativeLibraryFileWriteSuccess extends Info {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Provided file";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the generated native library processj header file.</p>
             */
            private final String nativeLibraryProcessJHeaderFileName;

            /**
             * <p>The {@link String} value of the generated native library processj header file path.</p>
             */
            private final String nativeLibraryProcessJHeaderFilePath;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeLibraryFileWriteSuccess}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param nativeLibraryProcessJHeaderFileName The {@link String} value of the generated native library processj header file.
             * @see Phase
             * @see Info
             * @since 0.1.0
             */
            protected NativeLibraryFileWriteSuccess(final Phase culpritInstance,
                                                    final String nativeLibraryProcessJHeaderFileName,
                                                    final String nativeLibraryProcessJHeaderFilePath) {
                super(culpritInstance);

                this.nativeLibraryProcessJHeaderFileName = nativeLibraryProcessJHeaderFileName;
                this.nativeLibraryProcessJHeaderFilePath = nativeLibraryProcessJHeaderFilePath;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.nativeLibraryProcessJHeaderFileName + " must be moved to lib/C/include/"
                        + this.nativeLibraryProcessJHeaderFilePath;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of missing package name from a
         * {@link Compilation}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class MissingPackageNameException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Library files must specify a package name";

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link MissingPackageNameException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected MissingPackageNameException(final Phase culpritInstance) {
                super(culpritInstance);
            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of an Illegal Pragma.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class IllegalPragmaException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Illegal Pragma";

            /// --------------
            /// Private Fields

            /**
             * <p>The invalid {@link Pragma} instance.</p>
             */
            private final Pragma    pragma        ;

            /**
             * <p>The line within the source file where the error occurred.</p>
             */
            private final int       currentLine   ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalPragmaException} with the culprit instance, current line,
             * & current line count.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param pragma The invalid {@link Pragma}.
             * @param currentLine The integer value of where the error occurred within the source file.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalPragmaException(final Phase culpritInstance,
                                             final Pragma pragma, final int currentLine) {
                super(culpritInstance);

                this.pragma             = pragma            ;
                this.currentLine        = currentLine       ;

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

                // Return the resultant error message
                return Message + ":" + this.currentLine + " '" + this.pragma + "'";

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a Pragma with no arguments
         * when they're necessary.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class PragmaRequiresArgumentException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Illegal Pragma";

            /// --------------
            /// Private Fields

            /**
             * <p>The invalid {@link Pragma} instance.</p>
             */
            private final Pragma    pragma        ;

            /**
             * <p>The line within the source file where the error occurred.</p>
             */
            private final int       currentLine   ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalPragmaException} with the culprit instance, current line,
             * & current line count.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param pragma The invalid {@link Pragma}.
             * @param currentLine The integer value of where the error occurred within the source file.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected PragmaRequiresArgumentException(final Phase culpritInstance,
                                                      final Pragma pragma, final int currentLine) {
                super(culpritInstance);

                this.pragma             = pragma            ;
                this.currentLine        = currentLine       ;

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

                // Return the resultant error message
                return Message + ":" + this.currentLine + " '" + this.pragma + "' requires 1 parameter, none was given.";

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a Pragma with arguments
         * when they're not necessary.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class PragmaRequiresNoArgumentException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Illegal Pragma";

            /// --------------
            /// Private Fields

            /**
             * <p>The invalid {@link Pragma} instance.</p>
             */
            private final Pragma    pragma        ;

            /**
             * <p>The line within the source file where the error occurred.</p>
             */
            private final int       currentLine   ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalPragmaException} with the culprit instance, current line,
             * & current line count.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param pragma The invalid {@link Pragma}.
             * @param currentLine The integer value of where the error occurred within the source file.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected PragmaRequiresNoArgumentException(final Phase culpritInstance,
                                                        final Pragma pragma, final int currentLine) {
                super(culpritInstance);

                this.pragma             = pragma            ;
                this.currentLine        = currentLine       ;

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
                final Phase culpritInstance = (Phase) this.getPhase();

                // Return the resultant error message
                return Message + ":" + this.currentLine + " '" + this.pragma + "' does not require any parameters.";

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a Pragma that has already
         * been specified.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class PragmaAlreadySpecifiedException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Illegal Pragma";

            /// --------------
            /// Private Fields

            /**
             * <p>The invalid {@link Pragma} instance.</p>
             */
            private final Pragma    pragma        ;

            /**
             * <p>The line within the source file where the error occurred.</p>
             */
            private final int       currentLine   ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalPragmaException} with the culprit instance, current line,
             * & current line count.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param pragma The invalid {@link Pragma}.
             * @param currentLine The integer value of where the error occurred within the source file.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected PragmaAlreadySpecifiedException(final Phase culpritInstance,
                                                      final Pragma pragma, final int currentLine) {
                super(culpritInstance);

                this.pragma             = pragma            ;
                this.currentLine        = currentLine       ;

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

                // Return the resultant error message
                return Message + ":" + this.currentLine + " '" + this.pragma + "' already specified.";

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of missing language {@link Pragma}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class MissingLanguagePragmaException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Missing LANGUAGE pragma";

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link MissingLanguagePragmaException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected MissingLanguagePragmaException(final Phase culpritInstance) {
                super(culpritInstance);
            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of missing file {@link Pragma}.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class MissingFilePragmaException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Missing FILE pragma";

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link MissingFilePragmaException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected MissingFilePragmaException(final Phase culpritInstance) {
                super(culpritInstance);
            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of NATIVE & NATIVELIB
         * {@link Pragma}s used in conjunction.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NativeAndNativeLibException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "pragmas NATIVE and NATIVELIB cannot be used together";

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeAndNativeLibException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NativeAndNativeLibException(final Phase culpritInstance) {
                super(culpritInstance);
            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information PROCESSJ specified for {@link Pragma}s
         * NATIVE or NATIVELIB.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NativeWithProcessJException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "pragmas NATIVE and NATIVELIB cannot be used together";

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeWithProcessJException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NativeWithProcessJException(final Phase culpritInstance) {
                super(culpritInstance);
            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information for a invalid native language.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class InvalidNativeLanguageException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Invalid native language";

            /// --------------
            /// Private Fields

            /**
             * <p>The invalid native language.</p>
             */
            private final String language;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link InvalidNativeLanguageException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected InvalidNativeLanguageException(final Phase culpritInstance,
                                                     final String language) {
                super(culpritInstance);

                this.language = language;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Retrieve the culprit & initialize the StringBuilder
                final Phase culpritInstance = (Phase) this.getPhase();

                // Return the resultant error message
                return Message + " '" + this.language + "' ";

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a native library containing
         * {@link ProtocolTypeDeclaration}s.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class LibraryContainsProtocolDeclarationException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Native libraries cannot declare Protocols";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProtocolTypeDeclaration} that was declared in the native library</p>
             */
            private final ProtocolTypeDeclaration protocolTypeDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link LibraryContainsProtocolDeclarationException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param protocolTypeDeclaration The {@link ProtocolTypeDeclaration} declared in the library.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected LibraryContainsProtocolDeclarationException(final Phase culpritInstance,
                                                                  final ProtocolTypeDeclaration protocolTypeDeclaration) {
                super(culpritInstance);

                this.protocolTypeDeclaration = protocolTypeDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.protocolTypeDeclaration;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a native library containing
         * {@link RecordTypeDeclaration}s.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class LibraryContainsRecordDeclarationException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Native libraries cannot declare Records";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link RecordTypeDeclaration} that was declared in the native library</p>
             */
            private final RecordTypeDeclaration recordTypeDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link LibraryContainsRecordDeclarationException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param recordTypeDeclaration The {@link RecordTypeDeclaration} declared in the library.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected LibraryContainsRecordDeclarationException(final Phase culpritInstance,
                                                                final RecordTypeDeclaration recordTypeDeclaration) {
                super(culpritInstance);

                this.recordTypeDeclaration = recordTypeDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {


                // Return the resultant error message
                return Message + ": " + this.recordTypeDeclaration;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ConstantDeclaration} that was
         * declared native in conjunction with a NATIVE {@link Pragma}.
         * </p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class IllegalConstantDeclarationNativeException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Illegal usage of 'native' keyword with NATIVE pragma";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ConstantDeclaration} that was declared native</p>
             */
            private final ConstantDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalConstantDeclarationNativeException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalConstantDeclarationNativeException(final Phase culpritInstance,
                                                                final ConstantDeclaration constantDeclaration) {
                super(culpritInstance);

                this.constantDeclaration = constantDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Retrieve the culprit & initialize the StringBuilder
                final Phase culpritInstance = (Phase) this.getPhase();

                // Return the resultant error message
                return Message + ": " + this.constantDeclaration;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ConstantDeclaration} that was
         * declared native.
         * </p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ConstantDeclarationNativeException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Constant declaration cannot be declared native";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ConstantDeclaration} that was declared native</p>
             */
            private final ConstantDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ConstantDeclarationNativeException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ConstantDeclarationNativeException(final Phase culpritInstance,
                                                         final ConstantDeclaration constantDeclaration) {
                super(culpritInstance);

                this.constantDeclaration = constantDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.constantDeclaration;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ConstantDeclaration} that was
         * not declared native.
         * </p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ConstantDeclarationNonNativeException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Constant declaration must be declared native";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ConstantDeclaration} that was not declared native</p>
             */
            private final ConstantDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ConstantDeclarationNonNativeException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ConstantDeclarationNonNativeException(final Phase culpritInstance,
                                                            final ConstantDeclaration constantDeclaration) {
                super(culpritInstance);

                this.constantDeclaration = constantDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.constantDeclaration;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ConstantDeclaration} that was
         * initialized.
         * </p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ConstantDeclarationInitializedException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Constant declaration cannot have an initialization expression";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ConstantDeclaration} that was declared native</p>
             */
            private final ConstantDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ConstantDeclarationInitializedException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ConstantDeclarationInitializedException(final Phase culpritInstance,
                                                              final ConstantDeclaration constantDeclaration) {
                super(culpritInstance);

                this.constantDeclaration = constantDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Retrieve the culprit & initialize the StringBuilder
                final Phase culpritInstance = (Phase) this.getPhase();

                // Return the resultant error message
                return Message + ": " + this.constantDeclaration;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ConstantDeclaration} that was
         * not initialized.
         * </p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ConstantDeclarationNotInitializedException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Constant declaration must be initialized";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ConstantDeclaration} that was not declared native</p>
             */
            private final ConstantDeclaration constantDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ConstantDeclarationNotInitializedException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ConstantDeclarationNotInitializedException(final Phase culpritInstance,
                                                                 final ConstantDeclaration constantDeclaration) {
                super(culpritInstance);

                this.constantDeclaration = constantDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Retrieve the culprit & initialize the StringBuilder
                final Phase culpritInstance = (Phase) this.getPhase();

                // Return the resultant error message
                return Message + ": " + this.constantDeclaration;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ProcedureTypeDeclaration} that was
         * declared native.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ProcedureDeclarationNativeException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Procedure declaration cannot be declared native";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that was declared native</p>
             */
            private final ProcedureTypeDeclaration procedureDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ProcedureDeclarationNativeException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ProcedureDeclarationNativeException(final Phase culpritInstance,
                                                          final ProcedureTypeDeclaration procedureDeclaration) {
                super(culpritInstance);

                this.procedureDeclaration = procedureDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.procedureDeclaration;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ProcedureTypeDeclaration} that was
         * not declared native.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ProcedureDeclarationNonNativeException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Procedure declaration must be declared native";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that was not declared native</p>
             */
            private final ProcedureTypeDeclaration procedureDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ProcedureDeclarationNonNativeException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ProcedureDeclarationNonNativeException(final Phase culpritInstance,
                                                             final ProcedureTypeDeclaration procedureDeclaration) {
                super(culpritInstance);

                this.procedureDeclaration = procedureDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.procedureDeclaration;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ProcedureTypeDeclaration} that was
         * initialized.
         * </p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ProcedureDeclarationDefinesBodyException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Procedure declaration cannot have a body in a non-ProcessJ library file";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that defines a body</p>
             */
            private final ProcedureTypeDeclaration procedureDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ProcedureDeclarationDefinesBodyException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ProcedureDeclarationDefinesBodyException(final Phase culpritInstance,
                                                               final ProcedureTypeDeclaration procedureDeclaration) {
                super(culpritInstance);

                this.procedureDeclaration = procedureDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.procedureDeclaration;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ProcedureTypeDeclaration} that did not
         * define a body.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ProcedureDeclarationDoesNotDefineBodyException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Procedure declaration must define a body";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that did not define a body</p>
             */
            private final ProcedureTypeDeclaration procedureDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ProcedureDeclarationDoesNotDefineBodyException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected ProcedureDeclarationDoesNotDefineBodyException(final Phase culpritInstance,
                                                                     final ProcedureTypeDeclaration procedureDeclaration) {
                super(culpritInstance);

                this.procedureDeclaration = procedureDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.procedureDeclaration;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ProcedureTypeDeclaration} that does not
         * specify a primitive return type.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class IllegalProcedureReturnTypeException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message
                    = "Illegal return type specification; Native C library procedures must return a primitive type";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that does not specify a primitive return type</p>
             */
            private final ProcedureTypeDeclaration procedureDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalProcedureReturnTypeException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalProcedureReturnTypeException(final Phase culpritInstance,
                                                          final ProcedureTypeDeclaration procedureDeclaration) {
                super(culpritInstance);

                this.procedureDeclaration = procedureDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.procedureDeclaration + " specifies '" + this.procedureDeclaration.getReturnType() + "'";

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ProcedureTypeDeclaration} that specifies a
         * barrier or timer primitive return type.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class IllegalProcedureBarrierOrTimerReturnTypeException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message
                    = "Illegal return type specification; Native C library procedures must not return a barrier or timer";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that specifies a barrier or timer primitive return type</p>
             */
            private final ProcedureTypeDeclaration procedureDeclaration;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalProcedureBarrierOrTimerReturnTypeException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalProcedureBarrierOrTimerReturnTypeException(final Phase culpritInstance,
                                                                        final ProcedureTypeDeclaration procedureDeclaration) {
                super(culpritInstance);

                this.procedureDeclaration = procedureDeclaration;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.procedureDeclaration + " specifies '" + this.procedureDeclaration.getReturnType() + "'";

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ProcedureTypeDeclaration} that specifies
         * at least one non primitive parameter type.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class IllegalProcedureParameterTypeException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message
                    = "Illegal parameter type specification; Native C library procedures must specify primitive parameters";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that specifies a non-primitive parameter type</p>
             */
            private final ProcedureTypeDeclaration procedureDeclaration;

            /**
             * <p>The non primitive {@link Type}</p>
             */
            private final Type parameterType;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalProcedureParameterTypeException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalProcedureParameterTypeException(final Phase culpritInstance,
                                                             final ProcedureTypeDeclaration procedureDeclaration,
                                                             final Type parameterType) {
                super(culpritInstance);

                this.procedureDeclaration = procedureDeclaration;
                this.parameterType        = parameterType       ;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.procedureDeclaration + " specifies '" + this.parameterType+ "'";

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a {@link ProcedureTypeDeclaration} that specifies a
         * barrier or timer primitive parameter type.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class IllegalProcedureBarrierOrTimerParameterTypeException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message
                    = "Illegal parameter type specification; Native C library procedures must not specify a barrier or timer parameter";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link ProcedureTypeDeclaration} that specifies a barrier or timer primitive return type</p>
             */
            private final ProcedureTypeDeclaration procedureDeclaration;

            /**
             * <p>The non primitive {@link Type}</p>
             */
            private final Type parameterType;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link IllegalProcedureBarrierOrTimerParameterTypeException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected IllegalProcedureBarrierOrTimerParameterTypeException(final Phase culpritInstance,
                                                                           final ProcedureTypeDeclaration procedureDeclaration,
                                                                           final Type parameterType) {
                super(culpritInstance);

                this.procedureDeclaration   = procedureDeclaration  ;
                this.parameterType          = parameterType         ;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.procedureDeclaration + " specifies '" + this.parameterType + "'";

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of missing {@link List} of native signatures.
         * </p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class InvalidNativeSignaturesException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Missing Native signatures";

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link InvalidNativeSignaturesException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected InvalidNativeSignaturesException(final Phase culpritInstance) {
                super(culpritInstance);
            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a native library header file
         * write failure.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NativeLibraryHeaderFileWriteFailureException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Failed to write Native Library header file";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the native library header file that failed to write.</p>
             */
            private final String nativeLibraryHeaderFileName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeLibraryHeaderFileWriteFailureException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @param nativeLibraryHeaderFileName The {@link String} value of the native library header file that failed
             *                                    to generate.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NativeLibraryHeaderFileWriteFailureException(final Phase culpritInstance,
                                                                   final String nativeLibraryHeaderFileName) {
                super(culpritInstance);

                this.nativeLibraryHeaderFileName = nativeLibraryHeaderFileName;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return Message + ": " + this.nativeLibraryHeaderFileName;

            }

        }

        /**
         * <p>{@link Error} class that encapsulates the pertinent information of a native library implementation file
         * write failure.</p>
         * @see Phase
         * @see Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NativeLibraryImplementationFileWriteFailureException extends Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Failed to write Native Library header file";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the native library header file that failed to write.</p>
             */
            private final String nativeLibraryImplementationFileName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NativeLibraryImplementationFileWriteFailureException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Error
             * @since 0.1.0
             */
            protected NativeLibraryImplementationFileWriteFailureException(final Phase culpritInstance,
                                                                           final String nativeLibraryImplementationFileName) {
                super(culpritInstance);

                this.nativeLibraryImplementationFileName = nativeLibraryImplementationFileName;

            }

            /// -------------------
            /// java.lang.Exception

            /**
             * <p>Returns a newly constructed message specifying the error.</p>
             * @return {@link String} value of the error message.
             * @since 0.1.0
             */
            @Override
            public String getMessage() {

                // Return the resultant error message
                return this.nativeLibraryImplementationFileName;

            }

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
