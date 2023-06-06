package org.processj.compiler.phases;

import org.processj.compiler.ast.Compilation;
import org.processj.compiler.phases.phase.*;
import org.processj.compiler.utilities.ProcessJSourceFile;
import org.processj.compiler.utilities.Settings;
import org.processj.compiler.phases.phase.Phase;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.processj.compiler.phases.phase.ResolveImports.ProcessJSourceFileRegex;
import static org.processj.compiler.phases.phase.ResolveImports.SymbolPaths;
import static org.processj.compiler.utilities.Files.RetrieveMatchingFilesListWithName;
import static org.processj.compiler.utilities.Reflection.NewInstanceOf;

public class Phases {

    /// ------------------------
    /// Private Static Constants

    /**
     * <p>A handle to the {@link Executor} that is currently handling a compilation.</p>
     */
    public static Executor Executor = null;

    /**
     * <p>Contains a mapping of the {@link Phase}s executed by the compiler to the corresponding {@link Phase.Listener}
     * that will handle the {@link Phase.Message} callbacks. This {@link Map} is used to initialize a set of
     * {@link Phases} that will be executed by the {@link Phases.Executor}</p>
     * @see Phase
     * @see Phase.Message
     * @see Phase.Listener
     * @see Map
     * @see Executor
     */
    private final static Map<Class<? extends Phase>, Class<? extends Phase.Listener>> Phases = Map.of(
        ProcessJParser.class,   Phase.Listener.class,
        ResolveImports.class,   Phase.Listener.class,
        NameChecker.class,      Phase.Listener.class
    );

    /**
     * <p>Contains a mapping of the {@link Phase}s executed by the compiler to the corresponding {@link Phase.Listener}
     * that will handle the {@link Phase.Message} callbacks. This {@link Map} is used to initialize a set of
     * {@link Phases} that will be executed by the {@link Phases.Executor}</p>
     * @see Phase
     * @see Phase.Message
     * @see Phase.Listener
     * @see Map
     * @see Executor
     */
    private final static Map<Class<? extends Phase>, Phase> ActivePhases = new HashMap<>();

    /**
     * <p>Contains a mapping of the next {@link Phase} executed by the compiler from the corresponding previous
     * {@link Phase}.</p>
     * @see Phase
     * @see Phase.Message
     * @see Phase.Listener
     * @see Map
     * @see Executor
     */
    private final static Map<Class<? extends Phase>, Class<? extends Phase>> NextPhaseOf = Map.of(
            ProcessJParser.class,   ValidatePragmas.class,
            ValidatePragmas.class,  ResolveImports.class,
            ResolveImports.class,   NameChecker.class
    );

    /// --------------------------
    /// Protected Static Constants

    /**
     * <p>Maintains the set of {@link ProcessJSourceFile}s that have been opened.</p>
     */
    protected final static Map<String, ProcessJSourceFile> Opened = new HashMap<>();

    /// ----------------------
    /// Private Static Methods

    /**
     * <p>Returns the next {@link Phase} corresponding with the {@link ProcessJSourceFile}'s most recently completed
     * {@link Phase}.</p>
     * @param processJSourceFile The {@link ProcessJSourceFile} to retrieve the next {@link Phase} for.
     * @return {@link Phase} corresponding with the {@link ProcessJSourceFile}'s most recently completed
     * {@link Phase}.
     * @see Phase
     * @see ProcessJSourceFile
     * @since 0.1.0
     */
    private static Phase PhaseFor(final ProcessJSourceFile processJSourceFile) {

        // Retrieve the most recent phase (if any) & the next phase
        final Phase                     lastPhase       = processJSourceFile.getLastCompletedPhase();
        final Class<? extends Phase>    recentPhase     = (lastPhase) != null ? lastPhase.getClass() : null;
        final Class<? extends Phase>    nextPhase       = NextPhaseOf.getOrDefault(recentPhase, null);

        // Initialize the result
        Phase result = ActivePhases.getOrDefault(nextPhase, null);

        // If the Phase has not been instantiated
        if((result == null) && (nextPhase != null)) {

            // Attempt to
            try {

                // Build the Phase.Listener & the Phase
                result = NewInstanceOf(nextPhase, new Phase.Listener());

                // Emplace the Phase
                ActivePhases.put(nextPhase, result);

            } catch(final InstantiationException | IllegalAccessException | InvocationTargetException exception) {

                // TODO: Emit error here

            }

        }

        // Return the result
        return result;

    }

    /**
     * <p>Instantiates & opens the file corresponding to the specified {@link String} file path in order to aggregate
     * it into the compilation queue.</p>
     * @param filePath {@link String} file path corresponding to the file to open & aggregate into the compilation
     *                               queue.
     * @since 0.1.0
     */
    private static ProcessJSourceFile RequestOpen(final String filePath) {

        // Attempt to retrieve the ProcessJ Source file
        ProcessJSourceFile processJSourceFile = Opened.getOrDefault(filePath, null);

        // If the path is value & has not been opened
        if(processJSourceFile == null) {

            final int       separatorIndex  = filePath.lastIndexOf("/");
            final String    filename        = filePath.substring(separatorIndex + 1);
            final String    path            = filePath.substring(0, separatorIndex + 1);

            // Set the wildcard flag
            // Retrieve the list of files
            final List<String> files = RetrieveMatchingFilesListWithName(path, filename, SymbolPaths, ProcessJSourceFileRegex);

            // Initialize a new ProcessJSourceFile with the first path
            processJSourceFile = new ProcessJSourceFile(files.get(0));

            // Mark it as opened
            Opened.put(files.get(0), processJSourceFile);

        }

        // Return the result
        return processJSourceFile;

    }

    /**
     * <p>Requests a {@link Compilation} from the file specified in the {@link String} name & path.</p>
     * @param filepath {@link String} value of the file path.
     * @return A valid, preliminary {@link Compilation}.
     */
    private static Compilation RequestCompilation(final String filepath) throws Phase.Error {

        // Initialize a handle to the result
        Compilation compilation = null;

        // If a handle to the Executor is valid, retrieve a Compilation
        if(Executor != null) compilation = Executor.getPreliminaryCompilationFor(filepath);

        // Return the result
        return compilation;

    }

    /// ------------------
    /// Static Initializer

    static {

        Phase.Request = org.processj.compiler.phases.Phases::RequestCompilation;

        ProcessJSourceFileRegex  = ".*\\.([pP][jJ])"                                                 ;
        SymbolPaths.add("");
        SymbolPaths.add(Settings.includeDir + "/" + Settings.language + "/");

        // TODO: Set Executor here

    }

    /// --------
    /// Executor

    /**
     * <p>Class the represents a compiler {@link Phase} executor. This class exists to decouple the desired compiler
     * phases from the actual implementation.</p>
     * @see Phase
     * @version 1.0.0
     * @since 0.1.0
     */
    public static abstract class Executor {

        /// --------------------------
        /// Protected Static Constants

        /**
         * <p>The {@link Phase} request method that is effectively black-boxed from the {@link Executor}.</p>
         */
        protected final static RequestPhase RequestPhase    = org.processj.compiler.phases.Phases::PhaseFor;

        /**
         * <p>File open request method that provides the {@link Executor} with a method to notify the appropriate entity
         * to open a {@link ProcessJSourceFile}.</p>
         */
        protected final static Request      Request         = org.processj.compiler.phases.Phases::RequestOpen;

        /// --------------------------
        /// Protected Abstract Methods

        /**
         * <p>Constructs a preliminary {@link Compilation} that has not undergone any type resolution or checking.</p>
         * @param filePath The path {@link String} value corresponding to the resultant {@link Compilation}
         * @return A preliminary {@link Compilation}
         * @since 0.1.0
         */
        protected Compilation getPreliminaryCompilationFor(final String filePath) throws Phase.Error {

            // Initialize a Phase listener & a ProcessJSourceFile
            final Phase.Listener        listener            = new Phase.Listener();
            final ProcessJSourceFile    processJSourceFile  = Request.Open(filePath);

            // Initialize the phases
            final ProcessJParser    processJParser      = new ProcessJParser(listener);
            final ValidatePragmas   validatePragmas     = new ValidatePragmas(listener);

            // Tokenize & parse the input file
            processJParser.execute(processJSourceFile);
            validatePragmas.execute(processJSourceFile);

            // Return the transformed, preliminary result
            return processJSourceFile.getCompilation();

        }

        /// ---------------------
        /// Functional Interfaces

        /**
         * <p>Defines the {@link Phase} request method signature for the method that retrieves the {@link Phase}
         * corresponding with the {@link ProcessJSourceFile}'s most recently executed {@link Phase}.</p>
         * @see Phase
         * @see ProcessJSourceFile
         * @author Carlos L. Cuenca
         * @since 0.1.0
         */
        @FunctionalInterface
        public interface RequestPhase {

            Phase For(final ProcessJSourceFile processJSourceFile);

        }

        /**
         * <p>Defines a functional interface for a method to be specified to the {@link Executor} class that
         * provides requests a file specified at the {@link String} file path.</p>
         * @see Phase
         * @author Carlos L. Cuenca
         * @version 1.0.0
         * @since 0.1.0
         */
        @FunctionalInterface
        public interface Request {

            ProcessJSourceFile Open(final String filePath);

        }

    }

}
