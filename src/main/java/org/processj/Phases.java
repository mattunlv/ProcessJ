package org.processj;

import org.processj.ast.Compilation;
import org.processj.phases.*;
import org.processj.parser.CanonicalParserListener;
import org.processj.parser.ProcessJParser;
import org.processj.utilities.ProcessJSourceFile;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.processj.utilities.Reflection.NewInstanceOf;

public class Phases {

    /// ------------------------
    /// Private Static Constants

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
        ProcessJParser.class,               CanonicalParserListener.class,
        ResolveTopLevelDeclarations.class,  CanonicalResolveTopLevelDeclarationsListener.class,
        ValidatePragmas.class,              CanonicalValidatePragmasListener.class
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
            ProcessJParser.class,               ResolveTopLevelDeclarations.class,
            ResolveTopLevelDeclarations.class,  ValidatePragmas.class,
            ValidatePragmas.class,              Imports.class
    );

    /**
     * <p>Contains a mapping of each {@link Phase} to the order in which each {@link ProcessJSourceFile} should be
     * completed.</p>
     * @see Phase
     * @see Map
     * @see ProcessJSourceFile
     */
    private final static Map<Class<? extends Phase>, Integer> PhaseOrders = Map.of(
            ProcessJParser.class,               0,
            ResolveTopLevelDeclarations.class,  1,
            ValidatePragmas.class,              2,
            Imports.class,                      3
    );

    /// --------------------------
    /// Protected Static Constants

    /**
     * <p>Maintains the set of {@link ProcessJSourceFile}s that have been opened.</p>
     */
    protected final static Map<String, ProcessJSourceFile> Opened = new HashMap<>();

    /**
     * <p>The {@link List} of {@link ProcessJSourceFile}s available to each {@link Phase}.</p>
     */
    protected final static List<ProcessJSourceFile> FileList = new ArrayList<>();

    /// ----------------------
    /// Private Static Methods

    /**
     * <p>Returns an {@link Integer} value corresponding with the execution order of the specified {@link Phase} that
     * should operate or validate a {@link ProcessJSourceFile}.</p>
     * @param phase The {@link Phase} to retrieve the {@link Integer} order for.
     * @return {@link Integer} value corresponding with the execution order of the specified {@link Phase} that
     *         should operate or validate a {@link ProcessJSourceFile}.
     * @since 0.1.0
     */
    private static Integer PhaseOrderFor(final Phase phase) {

        return PhaseOrders.getOrDefault(phase.getClass(), -1);

    }

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
        final Class<? extends Phase> recentPhase = (processJSourceFile.getLastCompletedPhase()) != null
                ? processJSourceFile.getLastCompletedPhase().getClass() : null;
        final Class<? extends Phase> nextPhase = NextPhaseOf.getOrDefault(recentPhase, ProcessJParser.class);

        // Initialize the result
        Phase result = null;

        // If the Phase has not been instantiated
        if(!ActivePhases.containsKey(nextPhase)) {

            // Attempt to
            try {

                // Build the Phase.Listener & the Phase
                result = NewInstanceOf(nextPhase, NewInstanceOf(Phases.get(nextPhase)));

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
     * <p>Returns a flag indicating if the file at the specified path {@link String} value has been opened.</p>
     * @param filePath Path {@link String} value to check.
     * @return Flag indicating if the file at the specified path {@link String} value has been opened.
     * @since 0.1.0
     */
    private static boolean IsOpen(final String filePath) {

        return (filePath != null) && Opened.containsKey(filePath);

    }

    /**
     * <p>Instantiates & opens the file corresponding to the specified {@link String} file path in order to aggregate
     * it into the compilation queue.</p>
     * @param filePath {@link String} file path corresponding to the file to open & aggregate into the compilation
     *                               queue.
     * @since 0.1.0
     */
    private static void RequestOpen(final String filePath) {

        // If the path is value & has not been opened
        if((filePath != null) && !Opened.containsKey(filePath)) {

            // Initialize a new ProcessJSourceFile
            final ProcessJSourceFile processJSourceFile = new ProcessJSourceFile(filePath);

            // TODO: push it onto the queue

            // Mark it as opened
            Opened.put(filePath, processJSourceFile);

        }

    }

    /// ------------------
    /// Static Initializer

    static {

        Phase.GetOrder          = org.processj.Phases::PhaseOrderFor    ;
        Phase.Is                = org.processj.Phases::IsOpen           ;
        Phase.Request           = org.processj.Phases::RequestOpen      ;

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
        protected final static RequestPhase RequestPhase  = org.processj.Phases::PhaseFor;

        /// --------------------------
        /// Protected Abstract Methods

        /**
         * <p>Returns the {@link List} of file paths corresponding to the {@link Executor}.</p>
         * @return {@link List} of file paths corresponding to the {@link Executor}.
         */
        protected abstract List<String> getFileList();

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
        interface RequestPhase {

            Phase For(final ProcessJSourceFile processJSourceFile);

        }

    }

}
