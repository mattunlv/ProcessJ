package org.processj.compiler;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.*;

import org.processj.compiler.phase.NameChecker;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.ProcessJParser;
import org.processj.compiler.phase.TypeChecker;

import static org.processj.compiler.utilities.Files.RetrieveMatchingFilesListFrom;
import static org.processj.compiler.utilities.Reflection.*;

/**
 * <p>ProcessJ Compiler</p>.
 *
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public class Compiler extends Phase.Listener {

    /// -----------------
    /// Private Constants

    /**
     * <p>Maintains the set of {@link SourceFile}s that have been opened.</p>
     */
    private final static Map<String, SourceFile>                                Opened          = new HashMap<>()   ;

    /**
     * <p>Contains a mapping of the {@link Phase}s executed by the compiler to the corresponding {@link Phase.Listener}
     * that will handle the {@link Phase.Message} callbacks. This {@link Map} is used to initialize a set of
     * {@link Phase} instances that will be executed by the {@link Compiler}</p>
     * @see Phase
     * @see Phase.Message
     * @see Phase.Listener
     * @see Map
     * @see Compiler
     */
    private final static Map<Class<? extends Phase>, Phase>                     ActivePhases    = new HashMap<>()   ;

    /**
     * <p>Contains a mapping of the next {@link Phase} executed by the {@link Compiler} from the corresponding previous
     * {@link Phase}.</p>
     * @see Phase
     * @see Phase.Message
     * @see Phase.Listener
     * @see Map
     */
    private final static Map<Class<? extends Phase>, Class<? extends Phase>>    NextPhaseOf     = Map.of(
            ProcessJParser.class,   NameChecker.class,
            NameChecker.class,      TypeChecker.class
    );

    /**
     * <p>The current ProcessJ version.</p>
     * @since 1.0.0
     */
    private final static String         Version                 = "2.1.1"                           ;

    /**
     * <p>{@link String} value of the basic {@link Compiler} usage.</p>
     * @since 1.0.0
     */
    private final static String         Usage                   = "Usage: 'pjc [<option>] <files>"  ;

    /**
     * <p>The current ProcessJ version.</p>
     * @since 1.0.0
     */
    private final static String         UserHome                = System.getProperty("user.home")   ;

    /**
     * <p>Regex pattern that corresponds to a ProcessJ source file.</p>
     * @since 1.0.0
     */
    private final static String         ProcessJSourceFileRegex = ".*\\.([pP][jJ])"                 ;

    /**
     * <p>The standard library relative include directory.</p>
     * @since 1.0.0
     */
    private final static String         IncludeDirectory        = "include"                         ;

    /**
     * <p>{@link Set} containing the {@link String} paths the {@link Compiler} will look in for source files.</p>
     * @since 1.0.0
     */
    private final static Set<String>    IncludePaths            = new LinkedHashSet<>()             ;

    /**
     * <p>{@link Set} containing the {@link String} paths the {@link Compiler} will look in for library source files.</p>
     * @since 1.0.0
     */
    private final static Set<String>    LibraryIncludePaths     = new LinkedHashSet<>()             ;

    /**
     * <p>The set of files specified in the command line to be compiled.</p>
     * @since 1.0.0
     */
    private final static Set<String>    FileSet                 = new LinkedHashSet<>()             ;

    /**
     * <p>The standard {@link PrintStream} that handles reporting informative messages.</p>
     * @since 1.0.0
     * @see PrintStream
     */
    private final static PrintStream    InfoStream              = System.out                        ;

    /**
     * <p>The standard {@link PrintStream} that handles reporting warning messages.</p>
     * @since 1.0.0
     * @see PrintStream
     */
    private final static PrintStream    WarningStream           = System.out                        ;

    /**
     * <p>The standard {@link PrintStream} that handles reporting error messages.</p>
     * @since 1.0.0
     * @see PrintStream
     */
    private final static PrintStream    ErrorStream             = System.err                        ;

    /// ---------------------
    /// Private Static Fields

    /**
     * <p>The compilation output target. One of: JVM or CPP</p>
     * @since 1.0.0
     */
    private static String       Target                          = "JVM"                             ;

    /**
     * <p>The {@link Compiler}'s working directory.</p>
     * @since 1.0.0
     */
    private static String       WorkingDirectory                = ".processj"                       ;

    /**
     * <p>Flag that indicates if the {@link Compiler} should output colored messages.</p>
     * @since 1.0.0
     */
    private static boolean      ShowingColor                    = false                             ;

    /**
     * <p>Flag that indicates if the {@link Compiler} should emit informative, warning, and error messages.</p>
     * @since 1.0.0
     */
    private static boolean      ShowMessage                     = false                             ; // TODO: Log.startLogging

    /**
     * <p>Flag that indicates if a library file should be installed.</p>
     * @since 1.0.0
     */
    private static boolean      Install                         = false                             ;

    /**
     * <p>Flag that indicates if the {@link Compiler} should output the parse tree.</p>
     * @since 1.0.0
     */
    private static boolean      ShowTree                        = false                             ; // TODO: Uses Parse Tree Printer after Parsing

    /// ------------------
    /// Static Initializer

    static {

        Phase.ImportAssert.Retrieve = Compiler::SourceFilesAt;

    }

    /// ----------------------
    /// Private Static Methods

    /**
     * <p>Reports the specified informative {@link String} using {@link Compiler#InfoStream}.</p>
     * @param message The {@link String} message to report.
     * @since 1.0.0
     * @see PrintStream
     * @see String
     * @see Compiler#InfoStream
     */
    public static void Info(final String message) {

        if(Compiler.ShowMessage && (message != null))
            InfoStream.println("[info] " + message);

    }

    /**
     * <p>Reports the specified warning {@link String} using {@link Compiler#WarningStream}.</p>
     * @param message The {@link String} message to report.
     * @since 1.0.0
     * @see PrintStream
     * @see String
     * @see Compiler#WarningStream
     */
    public static void Warn(final String message) {

        if(Compiler.ShowMessage && (message != null))
            WarningStream.println("[warning] " + message);

    }

    /**
     * <p>Reports the specified error {@link String} using {@link Compiler#ErrorStream}.</p>
     * @param message The {@link String} message to report.
     * @since 1.0.0
     * @see PrintStream
     * @see String
     * @see Compiler#ErrorStream
     */
    public static void Error(final String message) {

        if(message != null)
            ErrorStream.println("[error] " + message);

    }

    /**
     * <p>Prints the {@link Compiler#Usage} {@link String}.</p>
     * @since 1.0.0
     */
    private static void PrintUsage() {

        Compiler.Info(Compiler.Usage);

    }

    /**
     * Returns a flag indicating if the {@link String} value is a valid argument.
     * @param string The {@link String} argument to validate.
     * @return Flag indicating if the {@link String} value is a valid argument.
     * @since 1.0.0
     */
    private static boolean IsValidArgument(final String string) {

        return (string != null) && !string.isBlank() && string.startsWith("-");

    }

    /**
     * <p>Attempts the invoke the specified {@link Method} that has been checked to be annotated as a
     * {@link CompilerOption} & returns a flag indicating if the execution should terminate.</p>
     * @param compilerOption The {@link Method} corresponding to a compiler option, annotated as {@link CompilerOption}.
     * @param argument The {@link String} value of the argument.
     * @param value The {@link Object} instance containing the argument's parameter values, if any
     * @return A flag indicating if the execution should terminate.
     * @since 1.0.0
     */
    private static boolean Invoked(final Method compilerOption, final String argument, final Object value) {

        boolean shouldTerminate = true;

        // Assert the CompilerOption is valid and attempt to
        if(compilerOption != null) try {

            // Initialize a handle to the result of invoking the Compiler Option
            final Object result = (compilerOption.getParameterCount() > 0)
                    ? compilerOption.invoke(null, value) : compilerOption.invoke(null);

            // Update the terminate flag
            shouldTerminate = (result != null) && ((Boolean) result);

            // Otherwise, if the invocation failed,
        } catch(final InvocationTargetException | IllegalAccessException exception) {

            // Report the failure
            Compiler.Info("Argument '" + argument + "' failed.");

            // Otherwise, report the invalid argument
        } else Compiler.Info("Invalid argument: '" + argument + "'.");

        // Return the terminate flag
        return shouldTerminate;

    }

    /**
     * <p>Extracts the specified {@link String} array (arguments) and invokes the corresponding {@link Method} marked
     * as a {@link CompilerOption}.</p>
     * @param inputArguments The {@link String} array to extract.
     * @return Flag indicating if the execution that invoked this method should terminate.
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    private static boolean SetEnvironment(final String[] inputArguments) {

        // Initialize a handle to the terminate flag; @0: -include, @1: path
        boolean shouldTerminate = (inputArguments == null) || (inputArguments.length <= 2);

        // Assert we received a valid amount of arguments
        if(!shouldTerminate) {

            // Retrieve the compiler options mapping & parse the arguments
            final Map<String, Method> compilerOptions   = RetrieveCompilerOptions();
            final Map<String, Object> arguments         = ParseArguments(inputArguments);

            // Set the FileSet
            Compiler.FileSet.addAll(Collections.checkedList((List<String>)
                    arguments.getOrDefault("files", FileSet), String.class));

            // Set the Include Paths
            Compiler.IncludePaths.addAll(List.of("", Compiler.IncludeDirectory + "/" + Compiler.Target + "/"));

            // Iterate through the specified arguments
            for(final Map.Entry<String, Object> argument: arguments.entrySet()) {

                // Initialize a handle to the String value of the argument
                final String key = argument.getKey();

                // Retrieve the flag indicating if the execution should terminate from the invoked compiler option
                shouldTerminate |= Invoked(compilerOptions.get(key), key, argument.getValue());

            }

        // Otherwise
        } else PrintUsage();

        // Return the flag indicating if the execution should terminate
        return shouldTerminate;

    }

    /**
     * <p>Extracts the parameters from the specified {@link String} array into the specified {@link List} starting
     * at the specified index.</p>
     * @param parameters The {@link String} array composed of parameters to extract.
     * @param result The {@link List} that will contain the results.
     * @param startIndex The integral index to start.
     * @return ending index.
     * @since 1.0.0
     */
    private static int ParseParameters(final String[] parameters, final List<String> result, final int startIndex) {

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

    /**
     * Returns a flag indicating if the {@link String} value is a valid parameter.
     * @param string The {@link String} parameter to validate.
     * @return Flag indicating if the {@link String} value is a valid parameter.
     * @since 1.0.0
     */
    private static String ValidParameter(final String string) {

        return ((string != null) && !string.startsWith("-")) ? string.trim() : "" ;

    }

    /**
     * <p>Returns the next {@link Phase} with the specified {@link Phase.Listener} corresponding with the
     * {@link SourceFile}'s most recently completed {@link Phase}.</p>
     * @param sourceFile The {@link SourceFile} to retrieve the next {@link Phase} for.
     * @param phaseListener The {@link Phase.Listener} bound to the corresponding {@link Phase}.
     * @return The next {@link Phase} corresponding with the {@link SourceFile}'s most recently completed {@link Phase}.
     * @see Phase
     * @see SourceFile
     * @since 0.1.0
     */
    private static Phase NextPhaseFor(final SourceFile sourceFile, final Phase.Listener phaseListener) {

        // Initialize a handle to the Source File's next Phase & the result
        final Class<? extends Phase> nextPhase = NextPhaseOf.get(sourceFile.getLastCompletedPhase());
        final Phase result;

        // If there's a next phase
        if(nextPhase != null) {

            // Set the result to the next phase
            result = ActivePhases.getOrDefault(nextPhase, NewInstanceOf(nextPhase, phaseListener));

            // And assert the Mapping contains the instance
            ActivePhases.putIfAbsent(nextPhase, result);

        // Otherwise set the result
        } else result = null;

        // Return the result
        return result;

    }

    /**
     * <p>Instantiates & opens the {@link SourceFile} corresponding to the specified {@link String} file path, executes
     * the initial compilation {@link Phase}, & aggregates the file to the {@link Compiler#Opened} mapping &
     * {@link Compiler#FileSet}.
     * @param filePath {@link String} file path corresponding to the file to open & aggregate into the compilation
     *                               queue.
     * @return Opened & minimally process {@link SourceFile}.
     * @since 0.1.0
     */
    private static SourceFile SourceFileAt(final String filePath, final Phase.Listener phaseListener) throws Phase.Error {

        // Attempt to retrieve the ProcessJ Source file
        SourceFile sourceFile = Opened.get(filePath);

        // If the SourceFile at the specified path has not been opened
        if(sourceFile == null) {

            // Initialize a new ProcessJSourceFile with the first path.
            sourceFile = new SourceFile(filePath);

            // Execute the initial Compilation
            NextPhaseFor(sourceFile, phaseListener).execute(sourceFile);

            // Mark it as opened
            Compiler.Opened.put(filePath, sourceFile);

            // Aggregate it to the file set
            Compiler.FileSet.add(filePath);

        }

        // Return the result
        return sourceFile;

    }

    /**
     * <p>Instantiates & opens the {@link SourceFile} corresponding to the specified {@link String} file path, executes
     * the initial compilation {@link Phase}, & aggregates the file to the {@link Compiler#Opened} mapping &
     * {@link Compiler#FileSet}.
     * @param filePath {@link String} file path corresponding to the file to open & aggregate into the compilation
     *                               queue.
     * @return Opened & minimally process {@link SourceFile}.
     * @since 0.1.0
     */
    private static List<SourceFile> SourceFilesAt(final String filePath, final Phase.Listener phaseListener) throws Phase.Error {

        // Initialize a handle to the resultant SourceFile List & the matching file paths.
        final List<String> files = RetrieveMatchingFilesListFrom(filePath, IncludePaths, ProcessJSourceFileRegex);
        final List<SourceFile> sourceFiles = new ArrayList<>();

        // Iterate through each file path & Open the file
        for(final String filepath: files)
            sourceFiles.add(SourceFileAt(filepath, phaseListener));

        // Return the result
        return sourceFiles;

    }

    /**
     * <p>Extracts the {@link String} arguments & their corresponding {@link String} parameters into a {@link Map}.</p>
     * @param arguments The {@link String} array containing the arguments to extract.
     * @return {@link Map} containing the argument-parameter mapping.
     * @since 1.0.0
     */
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

    /**
     * <p>Retrieves a {@link Map} containing all the {@link Compiler}'s {@link Method}s that are annotated as
     * {@link CompilerOption} mapped to the corresponding {@link String} value of {@link CompilerOption#name()}.</p>
     * @return {@link Map} containing all the {@link Compiler}'s {@link Method}s that are annotated as
     * {@link CompilerOption} mapped to the corresponding {@link String} value of {@link CompilerOption#name()}.
     * @since 1.0.0
     */
    private static Map<String, Method> RetrieveCompilerOptions() {

        // Initialize a handle to the result & retrieve all the Methods annotated with CompilerOption
        final Map<String, Method>   result              = new HashMap<>();
        final List<Method>          compilerOptions     = MethodsWithAnnotationOf(Compiler.class, CompilerOption.class);

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

    /// --------------
    /// CompilerOption

    /**
     * <p>{@link CompilerOption} that enables the {@link Compiler#ShowingColor} flag.</p>
     * @return Flag indicating the execution should not terminate.
     * @since 1.0.0
     */
    @CompilerOption(name = "showColor", description = "Use color on terminals that support ansi escape codes.")
    private static boolean EnableColor() {

        // Update the Showing Color flag
        Compiler.ShowingColor = true;

        // Indicate the execution should not terminate
        return false;

    }

    /**
     * <p>{@link CompilerOption} that enables the {@link Compiler#ShowMessage} flag.</p>
     * @return Flag indicating the execution should not terminate.
     * @since 1.0.0
     */
    @CompilerOption(name = "showMessage", description = "Show all info, error, & warning messages when available.")
    private static boolean EnableLogging() {

        // Update the show message flag
        Compiler.ShowMessage = true;

        // Indicate the execution should not terminate
        return false;

    }

    /**
     * <p>{@link CompilerOption} that enables the {@link Compiler#Install} flag that installs the specified library
     * after compilation.</p>
     * @return Flag indicating the execution should not terminate.
     * @since 1.0.0
     */
    @CompilerOption(name = "install", description = "Install an existing or user-defined library.")
    private static boolean InstallLibrary(final String arguments) {

        // Update the install flag
        Compiler.Install = true;

        // Open the file & process nativelib
        // Retrieve the Compilation // TODO: Specify proper arguments
        // final Compilation compilation = SourceFileAt("", null).getCompilation();

        // Run Butters
        // Butters.decodePragmas(compilation);
        // TODO: move files to the correct directory??

        // Return a flag indicating the execution should not terminate
        return false;

    }

    /**
     * <p>{@link CompilerOption} that overwrites the {@link Compiler#IncludePaths} with the path(s) specified by the
     * user.</p>
     * @return Flag indicating the execution should not terminate.
     * @since 1.0.0
     */
    @CompilerOption(name = "include", description = "Override the default include directories.")
    private static boolean OverwriteIncludePaths(final String includePaths) {

        // Clear the include paths
        Compiler.IncludePaths.clear();

        // Add each specified path
        Compiler.IncludePaths.addAll(List.of(includePaths.split(":")));

        // Return a flag indicating the execution should not terminate
        return false;

    }

    /**
     * <p>{@link CompilerOption} that overwrites the {@link Compiler#LibraryIncludePaths} with the path(s) specified by the
     * user.</p>
     * @return Flag indicating the execution should not terminate.
     * @since 1.0.0
     */
    @CompilerOption(name = "libinclude", description = "Override the default library include directories.")
    private static boolean OverwriteLibraryIncludeDirectory(final String includePaths) {

        // Clear the include paths
        Compiler.LibraryIncludePaths.clear();

        // Add each specified path
        Compiler.LibraryIncludePaths.addAll(List.of(includePaths.split(":")));

        // Return a flag indicating the execution should not terminate
        return false;

    }

    /**
     * <p>{@link CompilerOption} that retrieves all {@link Method}s annotated with {@link CompilerOption} & prints
     * the specified metadata.</p>
     * @return Flag indicating the execution should terminate.
     * @since 1.0.0
     */
    @CompilerOption(name = "help", description = "Show this help message.")
    private static boolean PrintHelp() {

        // Retrieve the set of annotations
        final List<java.lang.annotation.Annotation> compilerOptions =
                DeclaredMethodAnnotations(Compiler.class, CompilerOption.class);

        // Iterate through each annotation
        for(final java.lang.annotation.Annotation annotation: compilerOptions) {

            // Initialize a handle to the CompilerOption
            final CompilerOption compilerOption = (CompilerOption) annotation;

            // Output the option name & description
            Compiler.Info("-" + compilerOption.name() + "    " + compilerOption.description());

        }

        // Return a flag indicating the execution should terminate
        return true;

    }

    /**
     * <p>{@link CompilerOption} that prints the current {@link Compiler#Version} {@link String}.</p>
     * @return Flag indicating the execution should terminate.
     * @since 1.0.0
     */
    @CompilerOption(name = "version", description = "Print version information and exit")
    private static boolean PrintVersion() {

        Compiler.Info("ProcessJ Version: " + Version);

        // Return a flag indicating the execution should terminate
        return true;

    }

    /**
     * <p>{@link CompilerOption} sets the current {@link Compiler#Target} output language. Can be either CPP (C++) or
     * JVM (Java).</p>
     * @return Flag indicating the execution should not terminate.
     * @since 1.0.0
     */
    @CompilerOption(name = "target", description = "Specify the target language; C++ or Java (default).", value = "JVM")
    private static boolean SetTargetLanguage(final String targetLanguage) {

        // Assert the target language is valid
        if(targetLanguage != null) {

            // Assert the target language is 'CPP' or 'JVM'
            if(targetLanguage.equals("CPP") || targetLanguage.equals("JVM"))
                Compiler.Target = targetLanguage;

            // Otherwise, report the error
            else Compiler.Error("Invalid target: '" + targetLanguage + "'; must be 'CPP' or 'JVM'.");

        // Otherwise, report the error
        } else Compiler.Error("Internal Error: Target language specified as 'null'.");

        // Return a flag indicating the execution should not terminate
        return false;

    }

    /**
     * <p>{@link CompilerOption} that enables the {@link Compiler#ShowTree} flag that outputs each file's resultant
     * parse tree..</p>
     * @return Flag indicating the execution should not terminate.
     * @since 1.0.0
     */
    @CompilerOption(name = "showTree", description = "Show the AST constructed from the parser.")
    private static boolean ShowTree() {

        // Update the flag
        Compiler.ShowTree = true;

        // Return a flag indicating the execution should not terminate
        return false;

    }

    /// ---------------------
    /// Public Static Methods

    /**
     * <p>Execution entry point. Sets the {@link Compiler} environment via {@link Compiler#SetEnvironment(String[])}
     * with the specified arguments corresponding to the {@link Method}s annotated as {@link CompilerOption}. If an
     * argument that indicates the execution should terminate was specified, the compilation will not continue.
     * Otherwise, a {@link Compiler} is instantiated & each {@link Phase} executed on each {@link SourceFile} that
     * was specified or subsequently imported.</p>
     * @param arguments The {@link String} array containing the command-line arguments specified by the user.
     * @since 1.0.0
     */
    public static void main(final String[] arguments) throws Phase.Error, MalformedURLException {

        // Assert the arguments are valid
        if(!SetEnvironment(arguments)) {

            // Initialize an instance to the compiler
            final Compiler compiler = new Compiler();

            // For each Source File
            for(final String filepath: Compiler.FileSet) {

                // Initialize the corresponding handle TODO: Handle clashes?
                final SourceFile sourceFile = SourceFilesAt(filepath, compiler).get(0);

                // Execute each remaining Phase
                Phase phase; while((phase = NextPhaseFor(sourceFile, compiler)) != null)
                    phase.execute(sourceFile); // TODO: Check Errors here

            }

            // TODO: Write to File with working directory and extension in utf-8 encoding

        }

    }

    /// -----------
    /// Annotations

    /**
     * <p>Annotation that is used an an indicator for a {@link Method} that potentially updates a {@link Compiler}
     * variable or reports pertinent {@link Compiler} information.</p>
     * @see Compiler
     * @author Carlos L. Cuenca
     * @since 1.0.0
     * @version 1.0.0
     */
    private @interface CompilerOption {

        String  name()          default "none"  ;
        String  description()   default "none"  ;
        String  value()         default ""      ;

    }

}
