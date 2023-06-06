package org.processj.compiler.phases.phase;

import org.processj.compiler.ast.*;
import org.processj.compiler.utilities.*;

import java.util.*;

import static org.processj.compiler.utilities.Files.*;

/**
 * <p>Resolves the current {@link Compilation}'s {@link Import} statements and fully-qualified {@link Name}s
 * & aggregates their {@link Compilation}s into the {@link ResolveImports} {@link Phase}'s current
 * {@link Compilation}'s collection of imported symbols. Once the {@link Import} statements for a {@link Compilation}
 * have been resolved, this will aggregate the current {@link Compilation}'s top level declarations into its'
 * corresponding symbol table to prepare the {@link Compilation} for the next {@link Phase}.</p>
 * @see Compilation
 * @see DefineTopLevelDecl
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class ResolveImports extends Phase {

    /// ------------------------
    /// Private Static Constants

    /**
     * <p>Contains a mapping of {@link String} values corresponding to a path {@link String} value &
     * {@link Compilation}.</p>
     */
    private final static Map<String, Compilation>   Imported                = new HashMap<>()   ;

    /**
     * <p>Regex pattern that corresponds to a ProcessJ source file.</p>
     */
    public static String                            ProcessJSourceFileRegex = ".*"              ;

    /**
     * <p>{@link List} containing the set of paths to look for source files.</p>
     */
    public static List<String>                      SymbolPaths             = new ArrayList<>() ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link ResolveImports.Listener}.</p>
     * @param listener The {@link Phase.Listener} that receives any {@link Phase.Message},
     * {@link Phase.Warning}, or {@link Phase.Error} messages.
     * @since 0.1.0
     */
    public ResolveImports(final Phase.Listener listener) {
        super(listener);
    }

    /// -------------------------
    /// org.processj.ast.IVisitor

    /**
     * <p>Imports the package corresponding with the {@link Import}'s package name if it's defined.</p>
     * @param importStatement The {@link Import} specifying a package to process.
     * @throws Phase.Error If the {@link Import} handling failed.
     * @see Import
     * @see Phase.Error
     * @since 0.1.0
     */
    @Override
    public final Void visitImport(final Import importStatement) throws Phase.Error {

        Log.log(importStatement.line + ": Visiting import statement '" + importStatement + "'");

        if(!importStatement.isEmpty())
            importPackage(importStatement.toString());

        return null;

    }

    /**
     * <p>Imports the package corresponding with the {@link Name}'s package name if it's defined.</p>
     * @param name The {@link Name} specifying a package to process.
     * @throws Phase.Error If the {@link Import} handling failed.
     * @see Import
     * @see Phase.Error
     * @since 0.1.0
     */
    @Override
    public final Void visitName(final Name name) throws Phase.Error {

        if(name.specifiesPackage()) {

            Log.log(name.line + ": Visiting an import (of file: " + name + ")");

            importPackage(name.getPackageName());

        }

        return null;

    }

    /// ---------------
    /// Private Methods

    /**
     * <p>Retries the {@link List} of path {@link String} values found in the path corresponding with the
     * specified fully-qualified package name {@link String}.</p>
     * @param packageName The fully-qualified package name {@link String} that is converted to a path & searched for
     * @return {@link List} of path {@link String} values found in the path corresponding with the
     * specified fully-qualified package name {@link String}.
     * @throws Phase.Error If no files were found, or more than one file was found for a non-wildcard
     * fully-qualified package name.
     * @since 0.1.0
     */
    private List<String> retrieveFilesFor(final String packageName) throws Phase.Error {

        // Initialize the last separator index, path & file name
        final int       separatorIndex  = packageName.lastIndexOf('.');
        final String    path            = packageName.substring(0, separatorIndex).replace('.', '/') + '/';
        final String    filename        = packageName.substring(separatorIndex + 1);

        // Set the wildcard flag
        final boolean isWildcard = filename.equals("*");

        // Retrieve the list of files
        final List<String> files = (isWildcard)
                ? RetrieveMatchingFilesListFrom(path, SymbolPaths, ProcessJSourceFileRegex)
                : RetrieveMatchingFilesListWithName(path, filename, SymbolPaths, ProcessJSourceFileRegex);

        // Throw an ambiguity error if we found more than one candidate path
        if(!isWildcard && (files.size() > 1))
            throw new AmbiguousImportStatementException(this, packageName).commit();

        // TODO: Wildcard Error Files not found #103
        else if(files.isEmpty() && isWildcard)
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addError(VisitorMessageNumber.RESOLVE_IMPORTS_103)
                    .addArguments(filename, path)
                    .build());

        // Single File Error not found
        else if(files.isEmpty())
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addError(VisitorMessageNumber.RESOLVE_IMPORTS_105)
                    .addArguments(filename, path)
                    .build());

        // Return the result
        return files;

    }

    /**
     * <p>Attempts to recover an already imported {@link Compilation} or request a {@link Compilation} before
     * placing it in the current scope's mapping of imported symbols.</p>
     * @param filePath The desired {@link Compilation}'s file path.
     * @throws Phase.Error If the {@link Compilation} request failed.
     * @since 0.1.0
     */
    private void emplaceCompilationAt(final String filePath, final String packageName) throws Phase.Error {

        // Initialize a handle to the Compilation using the fully-qualified package name
        Compilation compilation = Imported.getOrDefault(packageName, null);

        // If the file has not been imported
        if(compilation == null) {

            // Preserve the Scope
            final SymbolMap preserved = this.getScope();

            // Request the Compilation
            compilation = Request.CompilationFor(filePath);

            // Insert the Compilation into the imported mapping
            Imported.put(packageName, compilation);

            // Begin the visitation
            compilation.visit(this);

            // Restore the Scope
            this.setScope(preserved);

        }

        // Either way, emplace the Compilation's scope into the current Compilation's import symbols
        // using the fully-qualified package name
        this.getScope().putImport(packageName, compilation.openScope());

    }

    /**
     * <p>Attempts to import the file corresponding with the specified fully-qualified package name.</p>
     * @param packageName The {@link String} value of the fully-qualified package name corresponding with the
     *                    file to import.
     * @throws Phase.Error If the import failed.
     * @since 0.1.0
     */
    private void importPackage(final String packageName) throws Phase.Error {

        // Empty Import Statement error
        if(packageName.isBlank() || packageName.isEmpty())
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addError(VisitorMessageNumber.RESOLVE_IMPORTS_102)
                    .addArguments(packageName)
                    .build());

        // Retrieve the valid set of file paths
        final List<String> filePaths = retrieveFilesFor(packageName);

        // Import each file
        for(final String filePath: filePaths) emplaceCompilationAt(filePath, packageName);

    }

    /// ------
    /// Errors

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of attempting to define an ambiguous
     * {@link Import} path.</p>
     * @see Phase
     * @see Phase.Error
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
         * <p>Constructs the {@link ResolveImports.AmbiguousImportStatementException}.</p>
         * @param culpritInstance The {@link ResolveImports} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected AmbiguousImportStatementException(final ResolveImports culpritInstance, final String importPath) {
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

            // Return the resultant error message
            return String.format(Message, this.importPath);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of attempting to define an imported
     * top-level declaration that is already defined.</p>
     * @see Phase
     * @see Phase.Error
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
         * <p>Constructs the {@link ResolveImports.AmbiguousImportStatementException}.</p>
         * @param culpritInstance The {@link ResolveImports} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ImportNameClashException(final ResolveImports culpritInstance, final String importPath) {
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

            // Return the resultant error message
            return String.format(Message, this.importPath);

        }

    }

}
