package org.processj;

import org.processj.ast.Compilation;
import org.processj.ast.SymbolMap;
import org.processj.utilities.ProcessJSourceFile;
import org.processj.utilities.Visitor;

import java.util.*;

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
     * request a {@link org.processj.ast.Compilation}.</p>
     */
    public static Request Request = null;

    /// --------------
    /// Private Fields

    /**
     * <p>{@link Listener} instance that receives any {@link Phase.Message} instances from the {@link Phase}.</p>
     */
    private final Listener      listener            ;

    /**
     * <p>The current scope when traversing the {@link org.processj.ast.Compilation}.</p>
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

    /// -------------------------
    /// org.processj.ast.IVisitor

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
     * <p>Asserts that the {@link Phase} has not produced any errors by inspecting the {@link Listener}. If
     * the {@link Listener} has collected at least one {@link Phase.Error}, this method will throw it.</p>
     * @throws Phase.Error If the {@link Listener} has collected at least one error.
     * @since 0.1.0
     */
    protected final void assertNoErrors() throws Phase.Error {

        // Retrieve the Listener's list of phase errors
        final List<Phase.Error> errors = this.getListener().getErrorList();

        // If the List is not empty, throw the most recent one
        if(!errors.isEmpty()) throw errors.get(errors.size() - 1);

    }

    /**
     * <p>Returns a valid {@link Compilation} instance from the {@link ProcessJSourceFile}. This method successfully
     * returns if the {@link ProcessJSourceFile} contains a valid {@link Compilation}.</p>
     * @return {@link Compilation} corresponding to the {@link ProcessJSourceFile}.
     * @throws Phase.Error If the {@link ProcessJSourceFile} does not contain a {@link Compilation}.
     * @since 0.1.0
     */
    protected Compilation retrieveValidCompilation() throws Phase.Error {

        // Retrieve the ProcessJ Source File
        final ProcessJSourceFile processJSourceFile = this.getProcessJSourceFile();

        // If a null value was specified for the ProcessJ source file
        if(processJSourceFile == null)
            throw new NullProcessJSourceFile(this).commit();

            // If the processJ source file does not contain a Compilation
        else if(!processJSourceFile.containsCompilation())
            throw new NullCompilationException(this).commit();

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
            throw new NullListenerException(this).commit();

        // If a null value was specified for the ProcessJ source file
        else if(processJSourceFile == null)
            throw new NullProcessJSourceFile(this).commit();

        // If the file has not been completed by this
        if(!processJSourceFile.hasBeenCompletedBy(this)) {

            // Otherwise, update the ProcessJSourceFile
            this.processJSourceFile = processJSourceFile;

            // Execute the phase
            this.executePhase();

            // Sanity check
            this.assertNoErrors();

            // Clear
            this.processJSourceFile = null;

            // Mark the file as completed by this Phase
            processJSourceFile.setCompletedPhase(this);

        }

    }

    /// ---------------
    /// Message Classes

    // TODO: Maybe Private?
    protected static class Message extends Exception {

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

        /// -----------------
        /// Protected Methods

        protected final Warning commit() {

            this.getPhase().getListener().notify(this);

            return this;

        }

    }

    public static class Error extends Message {

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

    /// ------
    /// Errors

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information regarding a specified null
     * {@link Listener}.</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    protected static class NullListenerException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Null Phase.Listener specified";

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link Phase.NullListenerException} with the culprit instance.</p>
         * @param culpritInstance The {@link Phase} instance that raised the error.
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected NullListenerException(final Phase culpritInstance) {
            super(culpritInstance);
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error message.</p>
         * @return {@link String} value of the error message.
         */
        @Override
        public String getMessage() {

            // Retrieve the culprit & initialize the StringBuilder
            final Phase culpritInstance = this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName()
                    + ": " + Message + " for " + this.getPhase() ;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information regarding a specified null
     * {@link ProcessJSourceFile}.</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    protected static class NullProcessJSourceFile extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Null source file specified";

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link Phase.NullProcessJSourceFile} with the culprit instance.</p>
         * @param culpritInstance The {@link Phase} instance that raised the error.
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        public NullProcessJSourceFile(final Phase culpritInstance) {
            super(culpritInstance);
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error message.</p>
         * @return {@link String} value of the error message.
         */
        @Override
        public String getMessage() {

            // Retrieve the culprit & initialize the StringBuilder
            final Phase culpritInstance = this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName()
                    + ": " + Message + " for " + this;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an invalid
     * {@link org.processj.ast.Compilation}.</p>
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    protected static class NullCompilationException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Invalid Compilation provided";

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link Phase.NullCompilationException} with the culprit instance.
         * @param culpritInstance The {@link Phase} instance that raised the error.
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        public NullCompilationException(final Phase culpritInstance) {
            super(culpritInstance);
        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying the error.</p>
         * @return {@link String} value of the error message.
         */
        @Override
        public String getMessage() {

            // Retrieve the culprit & initialize the StringBuilder
            final Phase culpritInstance = this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName()
                    + ": " + Message + " for " + this.getPhase();

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
        protected final static LogInfo      Info = org.processj.utilities.Log::Info     ;

        /**
         * <p>The warning logging method that handles warning messages.</p>
         */
        protected final static LogWarning   Warning = org.processj.utilities.Log::Warn  ;

        /**
         * <p>The error logging method that handles error messages.</p>
         */
        protected final static LogError     Error = org.processj.utilities.Log::Error   ;

        /// --------------
        /// Private Fields

        /**
         * <p>{@link List} containing all of the {@link Phase}'s informative messages.</p>
         */
        private final List<Phase.Info>    infoList    ;

        /**
         * <p>{@link List} containing all of the {@link Phase}'s warning messages.</p>
         */
        private final List<Phase.Warning> warningList ;

        /**
         * <p>{@link List} containing all of the {@link Phase}'s error messages.</p>
         */
        private final List<Phase.Error>   errorList   ;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link Phase.Listener} to its' default state.</p>
         * @since 0.1.0
         */
        protected Listener() {

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

        /// --------------------------
        /// Protected Abstract Methods

        /**
         * <p>Callback that's invoked when the {@link Phase} emits an informative {@link Phase.Message}</p>
         * @param phaseInfo The {@link Phase.Info} message to handle.
         * @see Phase.Info
         * @since 0.1.0
         */
        protected void notify(final Phase.Info phaseInfo) {

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
        protected void notify(final Phase.Warning phaseWarning) {

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
        protected void notify(final Phase.Error phaseError)  {

            final ProcessJSourceFile file = phaseError.getPhase().getProcessJSourceFile();

            // Log the message
            Error.Log(file + ": " + phaseError.getMessage());

            // Push the error
            this.push(phaseError);

        }

        /// --------------
        /// Public Methods

        /**
         * <p>Retrieves the {@link Listener}'s collection of {@link Phase.Info} messages.</p>
         * @since 0.1.0
         */
        public final List<Phase.Info> getInfoList() {

            return this.infoList;

        }

        /**
         * <p>Retrieves the {@link Listener}'s collection of {@link Phase.Warning} messages.</p>
         * @since 0.1.0
         */
        public final List<Phase.Warning> getWarningList() {

            return this.warningList;

        }

        /**
         * <p>Retrieves the {@link Listener}'s collection of {@link Phase.Error} messages.</p>
         * @since 0.1.0
         */
        public final List<Phase.Error> getErrorList() {

            return this.errorList;

        }

        /// -------------------
        /// Function Interfaces

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

    /// ---------------------
    /// Functional Interfaces

    /**
     * <p>Defines a functional interface for a method to be specified to the {@link Phase} class that
     * provides a means to request a {@link org.processj.ast.Compilation} specified at the {@link String}
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
