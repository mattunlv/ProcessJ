package org.processj.phases;

import org.processj.Phase;
import org.processj.ast.*;
import org.processj.utilities.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.processj.utilities.Files.*;

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

    /// --------------
    /// Private Fields

    /**
     * <p>An effective stack structure that contains the previously pushed mapping of a {@link Compilation}'s
     * collection of names that define mobile {@link ProcTypeDecl}s.</p>
     */
    private final Deque<Map<String, Boolean>>   mobileMaps      ;

    /**
     * <p>Contains a mapping of {@link String} values corresponding to a declaration's name & a flag indicating if
     * it's a mobile {@link ProcTypeDecl}.</p>
     */
    private Map<String, Boolean>                mobileMap       ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link ResolveImports.Listener}.</p>
     * @param listener The {@link Phase.Listener} that receives any {@link org.processj.Phase.Message},
     * {@link org.processj.Phase.Warning}, or {@link org.processj.Phase.Error} messages.
     * @since 0.1.0
     */
    public ResolveImports(final Phase.Listener listener) {
        super(listener);

        this.mobileMap  = new HashMap<>()       ;
        this.mobileMaps = new ArrayDeque<>()    ;

    }

    /// -------------------------
    /// org.processj.ast.IVisitor

    /**
     * <p>Preserves the current mapping of {@link ProcTypeDecl}s that have been defined in the {@link Compilation}
     * that was being resolved before this was invoked.</p>
     * @param compilation The {@link Compilation} to visit.
     * @throws Phase.Error If Resolving the {@link Compilation}'s {@link Import}s failed.
     * @since 0.1.0
     */
    @Override
    public final Void visitCompilation(final Compilation compilation) throws Phase.Error {

        this.mobileMaps.push(this.mobileMap);

        this.mobileMap = new HashMap<>();

        super.visitCompilation(compilation);

        this.mobileMap.clear();

        this.mobileMap = this.mobileMaps.pop();

        return null;

    }

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

    /**
     * <p>Defines the {@link ConstantDecl} in the current scope if it doesn't already exist.</p>
     * @param constantDeclaration The {@link ConstantDecl} to define.
     * @throws Phase.Error If defining the {@link ConstantDecl} failed.
     */
    @Override
    public final Void visitConstantDecl(final ConstantDecl constantDeclaration) throws Phase.Error {

        Log.log(constantDeclaration.line + ": Visiting a ConstantDecl " + constantDeclaration);

        if(!this.getScope().put(constantDeclaration))
            throw new ResolveImports.TypeAlreadyDeclaredException(this, constantDeclaration.toString()).commit();

        return null;

    }

    /**
     * <p>Inserts a {@link String}-{@link SymbolMap} pair into the {@link Compilation}'s symbol table where
     * the {@link SymbolMap} contains the different overloads of the {@link ProcTypeDecl} as long as it is not
     * qualified as mobile.</p>
     * @param procedureTypeDeclaration The {@link ProcTypeDecl} to map.
     * @throws Phase.Error If the {@link ProcTypeDecl}'s name is already defined in the {@link Compilation}'s symbol
     * table, if it overloads a mobile {@link ProcTypeDecl}, or if it is qualified as mobile and attempts to overload
     * a non-mobile {@link ProcTypeDecl}.
     * @since 0.1.0
     */
    @Override
    public final Void visitProcTypeDecl(final ProcTypeDecl procedureTypeDeclaration) throws Phase.Error {

        Log.log(procedureTypeDeclaration.line + ": Visiting a ProcTypeDecl " + procedureTypeDeclaration);

        // Initialize a handle to the current scope & retrieve the result
        final SymbolMap     scope   = this.getScope();
        final List<Object>  result  = scope.get(procedureTypeDeclaration.toString());

        // If the procedure has not been defined
        if(!result.isEmpty()) {

            // Initialize a handle to the result
            final Object preliminary = result.get(0);

            // Assert the result is a Symbol Map
            if(!(preliminary instanceof SymbolMap))
                throw new NonProcedureTypeAlreadyDeclaredException(this, procedureTypeDeclaration.toString()).commit();

            // If the Procedure Definition is qualified as mobile
            else if(procedureTypeDeclaration.isMobile())
                throw (this.mobileMap.containsKey(procedureTypeDeclaration.getSignature()))
                        ? new NonMobileProcedureAlreadyExists(this, procedureTypeDeclaration).commit()
                        : new CannotOverloadMobileException(this, procedureTypeDeclaration).commit();

            // Cast the result
            final SymbolMap procedures = (SymbolMap) result;

            // Assert the procedure definition is unique
            if(!procedures.put(procedureTypeDeclaration.getSignature(), procedureTypeDeclaration))
                throw new TypeAlreadyDeclaredException(this, procedureTypeDeclaration.toString());

        // Otherwise
        } else {

            // Initialize a new SymbolMap
            final SymbolMap symbolMap = new SymbolMap();

            // Insert the Procedure
            symbolMap.put(procedureTypeDeclaration.getSignature(), procedureTypeDeclaration);

            // Emplace the entry
            scope.put(procedureTypeDeclaration.toString(), procedureTypeDeclaration);

        }

        return null;

    }

    /**
     * <p>Inserts a {@link String}-{@link ProtocolTypeDecl} pair into the {@link Compilation}'s symbol table.</p>
     * @param protocolTypeDeclaration The {@link ProtocolTypeDecl} to map.
     * @throws Phase.Error If the {@link ProtocolTypeDecl}'s name is already defined in the {@link Compilation}'s symbol
     * table.
     * @since 0.1.0
     */
    @Override
    public final Void visitProtocolTypeDecl(final ProtocolTypeDecl protocolTypeDeclaration) throws Phase.Error {

        // If the Protocol Declaration's name exists in the symbol table
        // TODO: Originally Error Code 203- Had the same exact message defined
        if(!this.getScope().put(protocolTypeDeclaration))
            throw new ResolveImports.TypeAlreadyDeclaredException(this, protocolTypeDeclaration.toString()).commit();

        return null;

    }

    /**
     * <p>Inserts a {@link String}-{@link RecordTypeDecl} pair into the {@link Compilation}'s symbol table.</p>
     * @param recordTypeDeclaration The {@link RecordTypeDecl} to map.
     * @throws Phase.Error If the {@link RecordTypeDecl}'s name is already defined in the {@link Compilation}'s symbol
     * table.
     * @since 0.1.0
     */
    @Override
    public final Void visitRecordTypeDecl(final RecordTypeDecl recordTypeDeclaration) throws Phase.Error {

        // If the Protocol Declaration's name exists in the symbol table
        // TODO: Originally Error Code 203- Had the same exact message defined
        if(!this.getScope().put(recordTypeDeclaration))
            throw new ResolveImports.TypeAlreadyDeclaredException(this, recordTypeDeclaration.toString()).commit();

        return null;

    }

    /// ---------------
    /// Private Methods

    private List<String> getPackagePrefixes(final List<String> prefixes) {

        return prefixes.stream().map(path -> path.replace('/', '.')).collect(Collectors.toList());

    }

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
         * <p>Constructs the {@link ResolveImports.AmbiguousImportStatementException}.</p>
         * @param culpritInstance The {@link ResolveImports} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
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
         * <p>Constructs the {@link ResolveImports.AmbiguousImportStatementException}.</p>
         * @param culpritInstance The {@link ResolveImports} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
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

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of attempting to define a {@link Type}
     * that is already defined.</p>
     * <p>Error Code: 200</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class TypeAlreadyDeclaredException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Type with name '%s' already declared in this scope";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the type that was already defined.</p>
         */
        private final String typename;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ResolveImports.TypeAlreadyDeclaredException}.</p>
         * @param culpritInstance The {@link ResolveImports.TypeAlreadyDeclaredException} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected TypeAlreadyDeclaredException(final ResolveImports culpritInstance,
                                               final String typename) {
            super(culpritInstance);
            this.typename = typename;
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
            return String.format(Message, this.typename);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of attempting to define a
     * {@link ProcTypeDecl} that is already defined as a different {@link Type}.</p>
     * <p>Error Code: 200</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class NonProcedureTypeAlreadyDeclaredException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Type with name '%s' already declared in this scope";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the type that was already defined.</p>
         */
        private final String typename;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ResolveImports.TypeAlreadyDeclaredException}.</p>
         * @param culpritInstance The {@link ResolveImports.TypeAlreadyDeclaredException} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected NonProcedureTypeAlreadyDeclaredException(final ResolveImports culpritInstance,
                                                           final String typename) {
            super(culpritInstance);
            this.typename = typename;
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
            return String.format(Message, this.typename);

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of attempting to overload a
     * {@link ProcTypeDecl} specified as 'mobile'.</p>
     * <p>Error Code: 206</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class CannotOverloadMobileException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Only one declaration of mobile procedure '%s' may exists";

        /// --------------
        /// Private Fields

        /**
         * <p>The mobile {@link ProcTypeDecl} that cannot be overloaded.</p>
         */
        private final ProcTypeDecl procedureTypeDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ResolveImports.TypeAlreadyDeclaredException}.</p>
         * @param culpritInstance The {@link ResolveImports.TypeAlreadyDeclaredException} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected CannotOverloadMobileException(final ResolveImports culpritInstance,
                                                final ProcTypeDecl procedureTypeDeclaration) {
            super(culpritInstance);
            this.procedureTypeDeclaration = procedureTypeDeclaration;
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
            return String.format(Message, this.procedureTypeDeclaration.toString());

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of attempting to overload a
     * {@link ProcTypeDecl} with {@link ProcTypeDecl} specified as 'mobile'.</p>
     * <p>Error Code: 208</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class NonMobileProcedureAlreadyExists extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Non-mobile procedure '%s' already exists";

        /// --------------
        /// Private Fields

        /**
         * <p>The mobile {@link ProcTypeDecl} that cannot be overloaded.</p>
         */
        private final ProcTypeDecl procedureTypeDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ResolveImports.TypeAlreadyDeclaredException}.</p>
         * @param culpritInstance The {@link ResolveImports.TypeAlreadyDeclaredException} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected NonMobileProcedureAlreadyExists(final ResolveImports culpritInstance,
                                                  final ProcTypeDecl procedureTypeDeclaration) {
            super(culpritInstance);
            this.procedureTypeDeclaration = procedureTypeDeclaration;
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
            return String.format(Message, this.procedureTypeDeclaration.toString());

        }

    }

}
