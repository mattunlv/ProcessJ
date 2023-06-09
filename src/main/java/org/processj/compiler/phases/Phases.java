package org.processj.compiler.phases;

import org.processj.compiler.ast.Compilation;
import org.processj.compiler.phases.legacy.butters.Butters;
import org.processj.compiler.phases.phase.*;
import org.processj.compiler.utilities.Log;
import org.processj.compiler.ProcessJSourceFile;
import org.processj.compiler.utilities.Settings;
import org.processj.compiler.phases.phase.Phase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.*;

import static org.processj.compiler.utilities.Files.RetrieveMatchingFilesListWithName;
import static org.processj.compiler.utilities.Reflection.*;

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

    /**
     * <p>Contains a mapping of {@link String} values corresponding to a package name {@link String} value &
     * {@link Compilation}.</p>
     */
    private final static Map<String, Compilation> Imported = new HashMap<>()   ;

    /**
     * <p>Maintains the set of {@link ProcessJSourceFile}s that have been opened.</p>
     */
    private final static Map<String, ProcessJSourceFile> Opened = new HashMap<>();

    private final static String UserHome                    = System.getProperty("user.home")  ;
    private final static String Version                     = "2.1.1"                          ;
    private final static String IncludeDirectory            = "include"                        ;
    private static List<String> FileSet                     = new ArrayList<>()                ;
    private static String       WorkingDirectory            = ".processj"                      ;
    private static String       Target                      = "JVM"                            ;
    private static boolean      ShowingColor                = false                            ;
    private static boolean      ShowMessage                 = false                            ; // TODO: Log.startLogging
    private static boolean      ShowTree                    = false                            ; // TODO: Uses Parse Tree Printer after Parsing
    private static boolean      Install                     = false                            ;

    /**
     * <p>Regex pattern that corresponds to a ProcessJ source file.</p>
     */
    public static String       ProcessJSourceFileRegex  = ".*\\.([pP][jJ])";

    /**
     * <p>{@link List} containing the set of paths to look for source files.</p>
     */
    public static List<String> IncludePaths             = new ArrayList<>() ;

    /**
     * <p>{@link List} containing the set of paths to look for source files.</p>
     */
    public static List<String> LibraryIncludePaths      = new ArrayList<>() ;

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
            final List<String> files = RetrieveMatchingFilesListWithName(path, filename, IncludePaths, ProcessJSourceFileRegex);

            // Initialize a new ProcessJSourceFile with the first path
            processJSourceFile = new ProcessJSourceFile(files.get(0));

            // Mark it as opened
            Opened.put(files.get(0), processJSourceFile);

        }

        // Return the result
        return processJSourceFile;

    }

    /**
     * <p>Requests a {@link Compilation} mapped to the {@link String} value of the package name, if it exists.</p>
     * @param packageName {@link String} value of the package name.
     * @return A valid {@link Compilation}, or null.
     * @since 0.1.0
     */
    public static Compilation GetImported(final String packageName) {

        // Return the result
        return Imported.getOrDefault(packageName, null);

    }

    /**
     * <p>Aggregates a mapping of a package name {@link String} value, to a {@link Compilation}.</p>
     * @param packageName {@link String} value of the package name.
     */
    public static void SetImported(final String packageName, final Compilation compilation) {

        // Set the result
        Imported.put(packageName, compilation);

    }

    private static boolean IsValidArgument(final String string) {

        return (string != null) && !string.isBlank() && string.startsWith("-");

    }

    private static String ValidParameter(final String string) {

        return ((string != null) && !string.startsWith("-")) ? string.trim() : "" ;

    }

    private static int ParseParameters(final String[] parameters, final List<String> result,
                                       final int startIndex) {

        // Iterate while the arguments are valid, or the index is in range
        int index = startIndex; while(index < parameters.length) {

            // Initialize a handle to the parameter
            final String parameter = parameters[index];

            // Assert the parameter is not empty & does not begin with a '-'
            if(ValidParameter(parameter).isBlank()) break;

            // Aggregate the parameter
            result.add(parameter);

        }

        // Return the latest index
        return index;

    }

    private static Map<String, Object> ParseArguments(final String[] arguments) {

        // Initialize a handle to the result & the file list
        final Map<String, Object>   result  = new HashMap<>();
        final List<String>          files   = new ArrayList<>();

        // Iterate through the arguments
        int index = 0; while(index < arguments.length) {

            // Assert the specified argument is valid
            if(IsValidArgument(arguments[index])) {

                // Initialize a handle to the argument & parameter
                final String argument   = arguments[index++].replaceFirst("(-)+", "");
                final String parameter  = ((index < arguments.length)) ? arguments[index] : "";

                // Assert the argument was not a nativelib specification
                if(argument.equals("nativelib") || argument.equals("userlib")) {

                    final String currentValue = (String) result.getOrDefault("install", "");

                    result.put("install", ((!currentValue.isBlank() && !currentValue.endsWith(":"))
                            ? ":" : "") +  ValidParameter(parameter));

                } else result.put(argument, ValidParameter(parameter));

                // Increment the index
                if(!parameter.startsWith("-")) index++;

            // Otherwise, we must have encountered files, aggregate them
            } else index = ParseParameters(arguments, files, index);

        }

        // Set the files
        result.put("files", files);

        // Return the result
        return result;

    }

    private static Map<String, Method> RetrieveCompilerOptions() {

        // Initialize a handle to the result & retrieve all the Methods annotated with CompilerOption
        final Map<String, Method>   result              = new HashMap<>();
        final List<Method>          compilerOptions     = MethodsWithAnnotationOf(Phases.class, CompilerOption.class);

        // Iterate through the CompilerOptions
        for(final Method compilerOption: compilerOptions) {

            // Initialize a handle to the CompilerOption
            final CompilerOption annotation = compilerOption.getDeclaredAnnotation(CompilerOption.class);

            // Map the name to the Method
            result.put(annotation.name(), compilerOption);

        }

        // Return the result
        return result;

    }

    private static void PrintUsage() {

        Log.log("Usage: 'pjc [<option>] <files>");

    }

    /// ---------------
    /// CompilerOptions

    @CompilerOption(name = "showColor", description = "Use color on terminals that support ansi escape codes.", flag = true)
    private static void EnableColor() {

        ShowingColor = true;

    }

    @CompilerOption(name = "showMessage", description = "Show all info, error, & warning messages when available.")
    private static void EnableLogging() {

        ShowingColor = true;

    }

    // -libinclude
    @CompilerOption(name = "install", description = "Install an existing or user-defined library.")
    public static void InstallLibrary(final String arguments)
            throws MalformedURLException, ClassNotFoundException {

        // TODO: Split paths from arguments
        // Process nativelib
        // Open the file
        final ProcessJSourceFile processJSourceFile = RequestOpen("");

        // Process the initial Phase
        //processPhaseFor(processJSourceFile);

        // Retrieve the Compilation
        final Compilation compilation = processJSourceFile.getCompilation();

        // Run Butters
        Butters.decodePragmas(compilation);
        // TODO: move files to the correct directory??

    }

    @CompilerOption(name = "include", description = "Override the default include directories.")
    private static void OverwriteIncludeDirectory(final String includeDirectory) {


    }

    @CompilerOption(name = "libinclude", description = "Override the default library include directories.")
    private static void OverwriteLibraryIncludeDirectory(final String includeDirectory) {


    }

    @CompilerOption(name = "help", description = "Show this help message.", terminate = true)
    private static void PrintHelp() {

        // Retrieve the set of annotations
        final List<java.lang.annotation.Annotation> compilerOptions =
                DeclaredMethodAnnotations(Phases.class, CompilerOption.class);

        // Iterate through each annotation
        for(final java.lang.annotation.Annotation annotation: compilerOptions) {

            // Initialize a handle to the CompilerOption
            final CompilerOption compilerOption = (CompilerOption) annotation;

            // Output the option name & description
            Log.log("-" + compilerOption.name() + "    " + compilerOption.description());

        }

    }

    @CompilerOption(name = "version", description = "Print version information and exit", terminate = true)
    private static void PrintVersion() {

        Log.log("ProcessJ Version: " + Version);

    }

    @CompilerOption(name = "target", description = "Specify the target language; C++ or Java (default).", value = "JVM")
    private static void SetTargetLanguage(final String targetLanguage) { }

    @CompilerOption(name = "showTree", description = "Show the AST constructed from the parser.")
    private static void ShowTree() {


    }

    /// ---------------------
    /// Public Static Methods

    @SuppressWarnings("unchecked")
    public static boolean SetEnvironment(final String[] input) throws InvocationTargetException, IllegalAccessException {

        // Initialize a handle to the resultant flag
        boolean shouldTerminate = (input == null) || (input.length <= 2);

        // Assert we received a valid amount of arguments
        // @0: -include, @1: path
        if(shouldTerminate) PrintUsage();

        // Otherwise
        else {

            // Retrieve the compiler options mapping & parse the arguments
            final Map<String, Method> compilerOptions   = RetrieveCompilerOptions();
            final Map<String, Object> arguments         = ParseArguments(input);

            // Retrieve the fileset
            FileSet = Collections.checkedList((List<String>)
                    arguments.getOrDefault("files", FileSet), String.class);

            // Iterate through the specified arguments
            for(final Map.Entry<String, Object> argument: arguments.entrySet()) {

                // Initialize a handle to the argument & the Compiler Option
                final String key            = argument.getKey();
                final Method compilerOption = compilerOptions.getOrDefault(key, null);

                // Assert the Compiler Option is valid
                if(compilerOption == null) { Log.log("Invalid argument: '" + key + "'"); break; }

                // Initialize a handle to the result of invoking the Compiler Option
                final Object result = (compilerOption.getParameterCount() > 0)
                        ? compilerOption.invoke(null, argument.getValue()) : compilerOption.invoke(null);

                // Update the terminate flag
                shouldTerminate = (result != null) && ((Boolean) result);

                // Assert the execution should terminate
                if(shouldTerminate) break;

            }

        }

        // Return the flag indicating if the execution should terminate
        return shouldTerminate;

    }

    /// ------------------
    /// Static Initializer

    static {

        Phase.Listener.Info     = Log::Info     ;
        Phase.Listener.Warning  = Log::Warn     ;
        Phase.Listener.Error    = Log::Error    ;

        Phases.Executor.RequestPhase    = org.processj.compiler.phases.Phases::PhaseFor    ;
        Phases.Executor.RequestOpen     = org.processj.compiler.phases.Phases::RequestOpen ;
        Phases.Executor.SetImported     = org.processj.compiler.phases.Phases::SetImported ;
        Phases.Executor.GetImported     = org.processj.compiler.phases.Phases::GetImported ;

        IncludePaths.add("")                                                     ;
        IncludePaths.add(IncludeDirectory + "/" + Settings.language + "/")       ;

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
    public static abstract class Executor extends Phase.Listener {

        /// --------------------------
        /// Protected Static Constants

        /**
         * <p>The {@link Phase} request method that is effectively black-boxed from the {@link Executor}.</p>
         */
        protected static RequestPhase RequestPhase    = null;

        /**
         * <p>File open request method that provides the {@link Executor} with a method to notify the appropriate entity
         * to open a {@link ProcessJSourceFile}.</p>
         */
        protected static RequestOpen  RequestOpen     = null;

        /**
         * <p>Method that provides the {@link Executor} with a method to aggregate a {@link Compilation} to the
         * imported mapping.</p>
         */
        protected static SetImported  SetImported     = null;

        /**
         * <p>Method that provides the {@link Executor} with a method to retrieve a {@link Compilation} from the
         * imported mapping, if it exists.</p>
         */
        protected static GetImported  GetImported     = null;

        /// --------------------------
        /// Protected Abstract Methods

        /**
         * <p>Constructs a preliminary {@link Compilation} that has not undergone any type resolution or checking.</p>
         * @param filePath The path {@link String} value corresponding to the resultant {@link Compilation}
         * @return A preliminary {@link Compilation}
         * @since 0.1.0
         */
        protected abstract Compilation onRequestCompilation(final String filePath, final String packageName)
                throws Phase.Error;

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
        public interface RequestOpen {

            ProcessJSourceFile File(final String filePath);

        }

        /**
         * <p>Defines a functional interface for a method to be specified to the {@link Executor} class that
         * provides a request to aggregate a {@link Compilation} to the imported mapping.</p>
         * @see Phase
         * @author Carlos L. Cuenca
         * @version 1.0.0
         * @since 0.1.0
         */
        @FunctionalInterface
        public interface SetImported {

            void Compilation(final String packageName, final Compilation compilation);

        }

        /**
         * <p>Defines a functional interface for a method to be specified to the {@link Executor} class that
         * provides a request to retrieve an imported {@link Compilation} if it exists.</p>
         * @see Phase
         * @author Carlos L. Cuenca
         * @version 1.0.0
         * @since 0.1.0
         */
        @FunctionalInterface
        public interface GetImported {

            Compilation Compilation(final String packageName);

        }

    }

    private @interface CompilerOption {

        String  name()          default "none"  ;
        String  description()   default "none"  ;
        String  value()         default ""      ;
        boolean flag()          default false   ;
        boolean terminate()     default false   ;

    }

}
