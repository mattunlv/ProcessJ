package org.processj.compiler.phases.phase;

import org.processj.compiler.ProcessJSourceFile;
import org.processj.compiler.ast.*;
import org.processj.compiler.ast.alt.AltCase;
import org.processj.compiler.ast.alt.AltStat;
import org.processj.compiler.ast.expression.ArrayLiteral;
import org.processj.compiler.ast.expression.Assignment;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.Phases;
import org.processj.compiler.utilities.*;

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
         * <p>{@link Phase.Error} to be emitted if a {@link ProcessJSourceFile} {@link Parser} encountered an
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
                                                              final ProcTypeDecl procedureTypeDeclaration)
                throws Phase.Error {

            // Initialize the result
            final boolean found = (type instanceof ProcTypeDecl);

            // Assert for all entries, if a Procedure Type was found, the existing Procedure Type is not
            // declared 'mobile'.
            if(found && ((ProcTypeDecl) type).isMobile())
                MobileProcedureOverloaded.Assert(phase, procedureTypeDeclaration);

            // Return the result
            return found;

        }

        protected static boolean NotOverloadingNonMobileProcedure(final Phase phase,
                                                                  final Type type,
                                                                  final ProcTypeDecl procedureTypeDeclaration)
                throws Phase.Error {

            // Initialize the result
            final boolean found = (type instanceof ProcTypeDecl);

            // Assert for all entries, if a Procedure Type was found, the specified Procedure Type
            // is not declared 'mobile'.
            if(found && procedureTypeDeclaration.isMobile())
                NonMobileProcedureTypeDefined.Assert(phase, type);

            // Return the result
            return found;

        }

        protected static void MobileProcedureSpecifiesNonVoidReturnType(final Phase phase,
                                                                        final ProcTypeDecl procedureTypeDeclaration)
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
                                       final ProcTypeDecl procedureTypeDeclaration)
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
         * <p>{@link Error} to be emitted if the {@link ProcTypeDecl} is mobile procedure with a specified
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
                    .forEachContextRetrieve(context -> (context instanceof ParBlock) || (context instanceof AltStat));

            // Initialize a handle to the result
            final boolean enclosingContextParallelOrChoice = enclosingContext != null;

            // Assert the construct is not enclosed in a parallel Context
            if(enclosingContext instanceof ParBlock)
                EnclosedInParallelContext.Assert(phase, construct, phase.getScope().getContext());

            // Assert the construct is not enclosed in a choice Context
            if(enclosingContext instanceof AltStat)
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
                                                                 final Block block)
                throws Phase.Error {

            // Initialize a handle to the Statements
            final Sequence<? extends Statement> statements = block.getStatements();

            // Initialize the preliminary result
            boolean haltsBeforeCompletion = false;

            // Iterate

            int index = 0; for(;!haltsBeforeCompletion && (index < statements.size()); index++)
                haltsBeforeCompletion = (statements.child(index) instanceof StopStat)
                        && ((index < statements.size() - 1));

            // Assert that if the Context contains a Stop Statement, it's the last one
            if(!haltsBeforeCompletion)
                ContextDoesNotTerminate.Assert(phase, statements.child(index), block);

            return haltsBeforeCompletion;

        }

        protected static boolean ConditionalContextReachable(final Phase phase,
                                                             final IfStat ifStatement)
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

    // TODO: Tidy
    protected static class SemanticAssert {

        protected static void SetEnclosingContextYields(final Phase phase)
                throws Phase.Error {

            phase.getScope().forEachContextUntil(SymbolMap.Context::setYields);

        }

        protected static void PriAltNotEnclosedByAltStatement(final Phase phase,
                                                              final AltStat altStatement)
                throws Phase.Error {

            phase.getScope().forEachContextUntil(context -> {

                final boolean found = context instanceof AltStat;

                // Assert that if the Context is an Alt Statement and the specified Alt Statement
                // is pri, that the Context is not a non-pri Alt Statement
                if(found && (!((AltStat) context).isPri() && altStatement.isPri()))
                    PJBugManager.INSTANCE.reportMessage(
                            new PJMessage.Builder()
                                    .addAST(altStatement)
                                    .addError(VisitorMessageNumber.REWRITE_1006)
                                    .build());

                return found;

            });

        }

        protected static void SingleInitializationForReplicatedAlt(final AltStat altStatement)
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
                    final Assignment inputGuardExpression = ((AltCase) context).getInputGuardExpression();

                    // Assert the Channel Read's Expression is not the specified Expression
                    if((inputGuardExpression != null) && inputGuardExpression.getRight() == expression) {

                        // Initialize a handle to the Scope
                        final SymbolMap scope = context.openScope();

                        // Assert any enclosing alt statements are not replicated alts
                        scope.forEachEnclosingContext(outerContext -> {
                            if((outerContext instanceof AltStat) && ((AltStat) outerContext).isReplicated())
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
                                                        final ChannelReadExpr channelReadExpression)
                throws Phase.Error {

            phase.getScope().forEachContextUntil(context -> {

                final boolean found = context instanceof Literal;

                // Assert the Context is not a Literal
                if(context instanceof Literal)
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(channelReadExpression)
                            .addError(VisitorMessageNumber.SEMATIC_CHECKS_900)
                            .addArguments(channelReadExpression)
                            .build());

                return found;

            });

        }

        protected static void NonYieldingPrecondition(final Phase phase,
                                                      final ChannelReadExpr channelReadExpression)
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

                final boolean found = context instanceof AltStat;

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
                    && ((ParBlock) context).getStatements().isEmpty();

            // Assert the parallel Context is not empty
            if(isParallelContextEmpty)
                ParallelContextEmpty.Assert(phase, context);

            // Return the result
            return isParallelContextEmpty;

        }

        protected static void VisibleToEnclosingParFor(final Phase phase,
                                                       final Expression expression) throws Error {

            phase.getScope().forEachContextUntil(context -> {

                final boolean found = (context instanceof ForStat) && ((ForStat) context).isPar();

                if(found) ((ForStat) context).vars.add(expression);

                return found;

            });

        }

        protected static void RewriteArrayType(final ConstantDecl constantDeclaration) {

            // Initialize a handle to the Constant Declaration's Name
            final Type          type        = constantDeclaration.getType();
            final Name          name        = constantDeclaration.getName();
            final Expression    expression  = constantDeclaration.getInitializationExpression();

            // Assert the Constant Declaration's Type is not an ArrayType
            if((name.getDepth() > 0) || (expression instanceof ArrayLiteral)) {

                // Initialize a handle to the synthesized Type
                final Type synthesized = (name.getDepth() > 0) ? new ArrayType(type, name.getDepth()) : type;

                // Assert the Constant Declaration is not initialized with an ArrayLiteral Expression
                if(expression instanceof ArrayLiteral)
                    constantDeclaration.setInitializationExpression(
                            new NewArray(synthesized, (ArrayLiteral) expression));

                // Overwrite the Constant Declaration's Type
                constantDeclaration.setType(synthesized);

            }

            // Overwrite the Constant Declaration's Name
            constantDeclaration.setName(new Name(name, 0));

        }

        protected static void RewriteArrayType(final ParamDecl parameterDeclaration) throws Phase.Error {

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

        protected static void RewriteArrayType(final LocalDecl localDeclaration) throws Phase.Error {

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

        protected static void FlattenedAltStatement(final AltStat altStatement)
                throws Error {

            // TODO: Wrap body in block
            // Initialize a handle to the AltCases and new Sequence
            final Sequence<AltCase> cases     = altStatement.getStatements();
            final Sequence<AltCase> flattened = new Sequence<>();

            // Iterate through the current children
            for(final AltCase altCase: cases) {

                // If the Alt Case is not nested, simply append it to the body
                if(!altCase.isNestedAltStatement()) flattened.append(altCase);

                    // Otherwise
                else {

                    // Retrieve a handle to the Alt Statement
                    final AltStat nested = (AltStat) altCase.getStatements();

                    // TODO: for now replicated alts just left.
                    if(nested.isReplicated()) flattened.append(altCase);

                        // Otherwise, merge
                    else flattened.merge(altStatement.getStatements());

                }

            }

            // Clear the cases
            cases.clear();
            cases.merge(flattened);

        }

        protected static void FlattenedParBlock(final ParBlock parBlock)
                throws Error {

            // Initialize a new Sequence
            final Sequence<Statement> result        = new Sequence<>();
            final Sequence<Statement> statements    = parBlock.getStatements();

            // Iterate through each Statement
            for(final Statement statement: statements) {

                // Aggregate enrolls TODO: Originally, addEnrolls was invoked only on statements that were not of Type ParBlock
                parBlock.aggregateEnrollsOf(statement);

                // Merge Par Block statements
                if(statement instanceof ParBlock)
                    result.merge(((ParBlock) statement).getStatements());

                    // Otherwise
                else result.append(statement);

            }

            // Set the Block
            statements.clear();
            statements.merge(result);

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
            if(expression instanceof NewMobile) {

                // Assert the result is a SymbolMap
                if(result instanceof SymbolMap)
                    ((NewMobile) expression).setCandidates((SymbolMap) result);

            } else if(expression instanceof Invocation) {

                // Assert the result is a SymbolMap
                if(result instanceof SymbolMap)
                    ((Invocation) expression).setCandidates((SymbolMap) result);

                else NonProcedureInvocation.Assert(phase, expression);

            } else expression.setType((Type) result);

        }

        protected static void Resolved(final Phase phase, final ConstantDecl constantDeclaration) throws Phase.Error {

            // Assert the name is visible
            constantDeclaration.setType((Type) Resolved(phase, constantDeclaration.getType().getName()));

        }

        protected static void Resolved(final Phase phase, final LocalDecl localDeclaration) throws Phase.Error {

            localDeclaration.setType((Type) Resolved(phase, localDeclaration.getType().getName()));

        }

        protected static void Resolved(final Phase phase, final ProcTypeDecl procedureTypeDeclaration) throws Phase.Error {

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

        protected static void Resolved(final Phase phase, final ProtocolTypeDecl protocolTypeDeclaration) throws Phase.Error {

            // Assert the Implements Type list is bound
            protocolTypeDeclaration.setTypeForEachExtend(implement -> {

                // Assert the Name is visible
                final Object result = Resolved(phase, implement);

                // Assert the result is a SymbolMap
                if(!(result instanceof ProtocolTypeDecl))
                    NonProtocolName.Assert(phase, protocolTypeDeclaration);

                // Return the result
                return (ProtocolTypeDecl) result;

            });

        }

        protected static void Resolved(final Phase phase, final RecordTypeDecl recordTypeDeclaration) throws Phase.Error {

            // Initialize a handle to the encountered extends
            final Set<String> seen = new HashSet<>();

            // Assert the Implements Type list is bound
            recordTypeDeclaration.setTypeForEachExtend(implement -> {

                // Assert the Name is visible
                final Object result = Resolved(phase, implement);

                // Assert the result is a SymbolMap
                if(!(result instanceof ProtocolTypeDecl))
                    NonProtocolName.Assert(phase, recordTypeDeclaration);

                // Initialize a handle to the RecordType Declaration
                final RecordTypeDecl recordExtend = (RecordTypeDecl) result;

                // Assert the Name is specified once
                if(!seen.add(recordExtend.getName().toString()))
                    ExtendDefined.Assert(phase, recordTypeDeclaration);

                // Return the result
                return recordExtend;

            });

        }

        protected static void Resolved(final Phase phase, final ProtocolLiteral protocolLiteral) throws Phase.Error {

            // Initialize a handle to the result
            final Object result = Resolved(phase, protocolLiteral.getName());

            // Assert the Expression is an ProtocolTypeDecl
            if(result instanceof ProtocolTypeDecl)
                    protocolLiteral.setType((ProtocolTypeDecl) result);

            // Otherwise
            else NonProtocolName.Assert(phase, protocolLiteral);

            // Initialize a handle to the ProtocolLiteral's Type & Protocol Case
            final ProtocolTypeDecl type     = (ProtocolTypeDecl) protocolLiteral.getType();
            final ProtocolCase     tagCase  = type.getCaseFrom(protocolLiteral.getTagLiteral());

            // Assert the case was found
            if(tagCase == null)
                TagNotDefined.Assert(phase, protocolLiteral);

            // Update the case
            protocolLiteral.setProtocolCase(tagCase);

        }

        protected static void Resolved(final Phase phase, final RecordLiteral recordLiteral) throws Phase.Error {

            // Initialize a handle to the result
            final Object result = Resolved(phase, recordLiteral.getName());

            // Assert the Expression is an ProtocolTypeDecl
            if(result instanceof RecordTypeDecl)
                recordLiteral.setType((RecordTypeDecl) result);

            // Otherwise
            else NonRecordName.Assert(phase, recordLiteral);

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

        protected static void Resolved(final Phase phase, final CastExpr castExpression) throws Phase.Error {

            castExpression.setType((Type) Resolved(phase, castExpression.getCastType().getName()));

        }

        protected static void Resolved(final Phase phase, final NewArray newArray) throws Phase.Error {

            newArray.setType((Type) Resolved(phase, newArray.getComponentType().getName()));

        }

        protected static void Resolved(final Phase phase, final BreakStat breakStatement) throws Phase.Error {

            // Assert the target is visible
            Resolved(phase, breakStatement.getTarget());

        }

        protected static void Resolved(final Phase phase, final ContinueStat continueStatement) throws Phase.Error {

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
         * <p>{@link Error} to be emitted if the {@link Name}'s {@link Type} is not a {@link ProcTypeDecl}.</p>
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
         * <p>{@link Error} to be emitted if the {@link Name}'s {@link Type} is not a {@link ProcTypeDecl}.</p>
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
         * <p>{@link Error} to be emitted if the {@link Name}'s {@link Type} is not a {@link ProtocolTypeDecl}.</p>
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
         * <p>{@link Error} to be emitted if the {@link Name}'s {@link Type} is not a {@link RecordTypeDecl}.</p>
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
         * <p>{@link Error} to be emitted if a {@link ProtocolLiteral} specified an undefined tag {@link Name}.</p>
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

        protected static void TargetExpressionTypeEqual(final ChannelWriteStat channelWriteStatement)
                throws Phase.Error {

            // Initialize a handle to the Channel Write Statement's Component Type & the Write Expression's Type
            final Type componentType        = channelWriteStatement.getTargetExpression().getType().getComponentType();
            final Type writeExpressionType  = channelWriteStatement.getWriteExpression().getType();

            // Assert the Channel Write Statement Expression's Type is defined & is not Type Equal to
            // the component type
            if(writeExpressionType != null && !writeExpressionType.typeEqual(componentType))
                // Wrap the Channel Write Expression in a Cast Expression
                channelWriteStatement.setWriteExpression(
                        new CastExpr(componentType, channelWriteStatement.getWriteExpression()));

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
