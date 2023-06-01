package org.processj.phases;

import org.processj.Phase;
import org.processj.ast.*;
import org.processj.utilities.ProcessJSourceFile;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Aggregates the current {@link Compilation}'s imported {@link DefineTopLevelDecl}s.</p>
 * @see Compilation
 * @see DefineTopLevelDecl
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class Imports extends Phase {

    /// ----------------------
    /// Final Static Constants

    /**
     * <p>Regex pattern that corresponds to a ProcessJ source file.</p>
     */
    private final static String ProcessJSourceFileRegex = ".*\\.([pP][jJ])";

    /// --------------
    /// Private Fields

    /**
     * <p>{@link List} containing the set of paths to look for source files.</p>
     */
    private final static List<String> SymbolPath = new ArrayList<>();

    /// --------------
    /// Private Fields

    /**
     * <p>Flag that indicates if the {@link Imports} {@link Phase} requested a file open.</p>
     */
    private boolean didRequestOpen;

    /// ----------------------
    /// Private Static Methods

    /**
     * <p>Returns a {@link String} value of the specified path ending with a forward slash.</p>
     * @param path The {@link String} value of the path to check.
     * @return {@link String} value of the valid path.
     * @since 0.1.0
     */
    private static String ValidPath(final String path) {

        // Assert that the path ends with a forward slash
        return (path == null) ? "" : path + ((path.charAt(path.length() - 1) == '/') ? "" : '/');

    }

    /**
     * <p>Returns a flag indicating if the specified {@link String} path exists & is a directory.</p>
     * @param path {@link String} value representing a path.
     * @return Flag indicating if the specified {@link String} path exists & is a directory.
     * @since 0.1.0
     */
    private static boolean IsDirectory(final String path) {

        // Initialize the result
        boolean result = path != null;

        // If the path is valid
        if(result) {

            // Instantiate a file
            final File file = new File(path);

            // Set the final result
            result = file.exists() && file.isDirectory();

        }

        // Return the result
        return result;

    }

    /**
     * <p>Returns a flag indicating if the specified {@link String} path exists & is a file.</p>
     * @param path {@link String} value representing a path.
     * @return Flag indicating if the specified {@link String} path exists & is a file.
     * @since 0.1.0
     */
    private static boolean IsFile(final String path) {

        // Initialize the result
        boolean result = path != null;

        // If the path is valid
        if(result) {

            // Instantiate a file
            final File file = new File(path);

            // Set the final result
            result = file.exists() && file.isFile();

        }

        // Return the result
        return result;

    }

    /**
     * <p>Searches the symbol path for all pairs of directories that exist with the specified path {@link String} value.
     * If the specified path {@link String} value is null, this returns an empty {@link List}.</p>
     * @param path The {@link String} value of the path to check against all prefix paths.
     * @return The set of existing {@link String} paths.
     * @since 0.1.0
     */
    private static List<String> SearchSymbolPathForDirectory(final String path) {

        // For each path prefix in the Symbol Path
        return path != null ? SymbolPath.stream()
                // Skip null values
                .filter(Objects::nonNull)
                // Join each path pair
                .map(prefix -> ValidPath(prefix) + path)
                // Keep the existing & valid directories
                .filter(Imports::IsDirectory)
                // Return the results
                .collect(Collectors.toList()) : List.of();

    }

    /**
     * <p>Searches the symbol path for all pairs of files that exist with the specified path.</p>
     * @param path The {@link String} value of the path to check against all prefix paths.
     * @return The set of existing {@link String} file paths.
     * @since 0.1.0
     */
    private static List<String> SearchSymbolPathForFile(final String path) {

        // For each path prefix in the Symbol Path
        return path != null ? SymbolPath.stream()
                // Skip null values
                .filter(Objects::nonNull)
                // Join each pair
                .map(prefix -> ValidPath(prefix) + path)
                // Keep the existing & valid directories
                .filter(Imports::IsFile)
                // Return the results
                .collect(Collectors.toList()) : List.of();

    }

    /**
     * <p>Returns a {@link List} of {@link String} values corresponding to each file contained in the specified
     * {@link String} path that match the specified regex as a {@link String} value.</p>
     * @param path The {@link String} value of the path to extract a {@link List} of {@link String} paths.
     * @param regex The {@link String} value of the regex to test each file path against.
     * @return {@link List} of {@link String} values corresponding to each file contained in the specified
     * {@link String} path that match the specified regex as a {@link String} value.
     * @since 0.1.0
     */
    private static List<String> FilesOf(final String path, final String regex) {

        // Initialize the list of files
        final File[] files = new File(path).listFiles();

        // For each file
        return (files != null) ? Arrays.stream(files)
                // Omit null values
                .filter(Objects::nonNull)
                // Extract each name
                .map(File::getName)
                // Keep the files that match
                .filter(name -> name.matches(regex))
                // Return the results
                .collect(Collectors.toList()) : List.of();

    }

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link Imports.Listener}.</p>
     * @param listener The {@link Phase.Listener} that receives any {@link org.processj.Phase.Message},
     * {@link org.processj.Phase.Warning}, or {@link org.processj.Phase.Error} messages.
     * @since 0.1.0
     */
    public Imports(final Listener listener) {
        super(listener);
        this.didRequestOpen = false;
    }

    /// ------------------
    /// org.processj.Phase

    /**
     * <p>Attempts to resolve the current {@link ProcessJSourceFile}'s {@link Import} statements. This will check
     * if the {@link ProcessJSourceFile} has been completed by the {@link Imports} {@link Phase} and if not, will
     * traverse the {@link Compilation} & aggregate all files found by searching the Symbol Path. If a file has not
     * been opened or aggregated to the compilation queue, this {@link Phase} will request it and add all top-level
     * declarations on the next pass.</p>
     * @throws Phase.Error If the {@link Import} resolution failed.
     * @see Phase
     * @see Import
     * @see Compilation
     * @see ProcessJSourceFile
     * @since 0.1.0
     */
    @Override
    protected void executePhase() throws Phase.Error {

        // Retrieve the current source file
        final ProcessJSourceFile processJSourceFile = this.getProcessJSourceFile();

        // If the source file has not been completed by this Phase
        if(!processJSourceFile.hasBeenCompletedBy(this)) {

            // Retrieve the Compilation
            final Compilation compilation = this.retrieveValidCompilation();

            // Reset the did request open flag
            this.didRequestOpen = false;

            // If the imports have not been resolved, visit
            if(!processJSourceFile.hasResolvedImports())
                compilation.visit(this);

            // If we did not request an open
            if(!this.didRequestOpen) {

                // Iterate through each file path
                for(final String filePath: processJSourceFile.getImportFiles()) {

                    // Retrieve the corresponding ProcessJSourceFile & its top level declarations
                    final ProcessJSourceFile importedProcessJSourceFile = GetProcessJSourceFile.For(filePath);
                    final Map<String, Object> importedTopLevelDeclarations = importedProcessJSourceFile.getCompilation()
                            .getTopLevelDeclarations();

                    // Assert the same amount of Phases have operated on the Compilation
                    assert importedProcessJSourceFile.hasBeenCompletedBy(processJSourceFile.getLastCompletedPhase());

                    // Iterate through each
                    for(final Map.Entry<String, Object> entry: importedTopLevelDeclarations.entrySet()) {

                        // Throw an error if the name is already defined.
                        if(compilation.definesTopLevelDeclarationWith(entry.getKey()))
                            throw new ImportNameClashException(this, entry.getKey()).commit();

                        // Otherwise, insert it into the Compilation
                        compilation.defineTopLevelDeclarationWith(entry.getKey(), entry.getValue());

                    }

                }

                // Finally, update the most recent completed Phase
                processJSourceFile.setCompletedPhase(this);

            }

        }

    }

    /// -------------------------
    /// org.processj.ast.IVisitor

    /**
     * <p>Resolves the {@link Import} statement by checking if the current {@link ProcessJSourceFile} has handled the
     * {@link Import} statement. If not, the file list corresponding to the {@link Import} statement is built and
     * emplaced into the {@link ProcessJSourceFile}. If any unopened files have been encountered, a file open request
     * is made and the {@link Imports#didRequestOpen} flag is enabled.</p>
     * @param importStatement The {@link Import} statement to process.
     * @throws Phase.Error If the {@link Import} handling failed.
     * @see Import
     * @see ProcessJSourceFile
     * @see Phase.Error
     * @since 0.1.0
     */
    @Override
    public Void visitImport(final Import importStatement) throws Phase.Error {

        // Attempt to find file paths
        final List<String> found = (importStatement.isWildcard())
                ? SearchSymbolPathForDirectory(importStatement.getPath())
                : SearchSymbolPathForFile(importStatement.getPath());

        // Throw an ambiguity error if we found more than one candidate path
        if(found.size() != 1)
            throw new AmbiguousImportStatementException(this, importStatement.getPath()).commit();

        // Initialize the list of found & non-opened files
        final List<String> fileList         = FilesOf(found.get(0), ProcessJSourceFileRegex);
        final List<String> unopenedFiles    = fileList.stream()
                .filter(filePath -> !Is.Opened(filePath)).toList();

        // Request all unopened files
        unopenedFiles.forEach(filePath -> Request.Open(filePath));

        // Update the did request open flag
        this.didRequestOpen = !unopenedFiles.isEmpty();

        // Aggregate the import files
        this.getProcessJSourceFile().addImport(fileList);

        return null;

    }

    /// ---------------
    /// Private Methods

    /**
     * <p>Returns a valid {@link Compilation} instance from the {@link ProcessJSourceFile}. This method successfully
     * returns if the {@link ProcessJSourceFile} contains a valid {@link Compilation}.</p>
     * @return {@link Compilation} corresponding to the {@link ProcessJSourceFile}.
     * @throws Phase.Error If the {@link ProcessJSourceFile} does not contain a {@link Compilation}.
     * @since 0.1.0
     */
    private Compilation retrieveValidCompilation() throws Phase.Error {

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

    /// ------
    /// Errors

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of attempting to define an ambiguous
     * {@link Import} path.</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class AmbiguousImportStatementException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Ambiguous import path '%s'; found multiple candidates in symbol path.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the ambiguous {@link Import} path.</p>
         */
        private final String importPath;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link Imports.AmbiguousImportStatementException}.</p>
         * @param culpritInstance The {@link Imports} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected AmbiguousImportStatementException(final Imports culpritInstance, final String importPath) {
            super(culpritInstance);
            this.importPath = importPath;
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
            final Imports culpritInstance = (Imports) this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName() + ": "
                    + String.format(Message, this.importPath);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of attempting to define an imported
     * top-level declaration that is already defined.</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ImportNameClashException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Import name '%s' clashes with top level declaration.";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the ambiguous {@link Import} path.</p>
         */
        private final String importPath;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link Imports.AmbiguousImportStatementException}.</p>
         * @param culpritInstance The {@link Imports} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected ImportNameClashException(final Imports culpritInstance, final String importPath) {
            super(culpritInstance);
            this.importPath = importPath;
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
            final Imports culpritInstance = (Imports) this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName() + ": "
                    + String.format(Message, this.importPath);

        }

    }

    /// --------------
    /// Phase.Listener

    /**
     * <p>Represents the corresponding {@link Imports.Listener} that handles receiving {@link Phase.Message}s
     * from the {@link Imports} phase.</p>
     * @see Phase
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 0.1.0
     */
    public static abstract class Listener extends Phase.Listener { /* Placeholder */ }

}
