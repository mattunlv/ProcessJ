package org.processj.phases;

import org.processj.Phase;
import org.processj.ast.*;
import org.processj.utilities.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Maps the current {@link Compilation}'s {@link DefineTopLevelDecl}s name to their corresponding instances.</p>
 * @see Compilation
 * @see DefineTopLevelDecl
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class ResolveTopLevelDeclarations extends Phase {

    /// --------------
    /// Private Fields

    /**
     * <p>Contains a mapping of {@link String} values corresponding to a declaration's name & a flag indicating if
     * it's a mobile {@link ProcTypeDecl}.</p>
     */
    private final Map<String, Boolean>  mobileMap   ;

    /**
     * <p>Contains a mapping of a {@link Compilation}'s set of names to their corresponding
     * {@link DefineTopLevelDecl}s.</p>
     */
    private Map<String, Object>         symbolMap   ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link Listener}.</p>
     * @param listener The {@link Phase.Listener} that receives any {@link org.processj.Phase.Message},
     * {@link org.processj.Phase.Warning}, or {@link org.processj.Phase.Error} messages.
     * @since 0.1.0
     */
    public ResolveTopLevelDeclarations(final ResolveTopLevelDeclarations.Listener listener) {
        super(listener);
        this.symbolMap  = null              ;
        this.mobileMap  = new HashMap<>()   ;
    }

    /// ------------------
    /// org.processj.Phase

    /**
     * <p>Traverses the {@link Compilation} & forms the mapping of {@link String} names to {@link DefineTopLevelDecl}s
     * visible to the {@link Compilation}.</p>
     * @throws Phase.Error If the mapping fails.
     * @since 0.1.0
     */
    @Override
    protected void executePhase() throws Phase.Error {

        // Retrieve the Compilation
        final Compilation compilation = this.retrieveValidCompilation();

        // Update the Map
        this.symbolMap = compilation.getTopLevelDeclarations();

        // Visit the Compilation
        compilation.visit(this);

    }

    /// -------------------------
    /// org.processj.ast.IVisitor

    /**
     * <p>Inserts a {@link String}-{@link ConstantDecl} pair into the {@link Compilation}'s symbol table.</p>
     * @param constantDecl The {@link ConstantDecl} to map.
     * @throws Phase.Error If the {@link ConstantDecl}'s name is already defined in the {@link Compilation}'s symbol
     * table.
     * @since 0.1.0
     */
    @Override
    public final Void visitConstantDecl(final ConstantDecl constantDecl) throws Phase.Error {

        // If the Constant Declaration's name exists in the symbol table
        if(this.symbolMap.containsKey(constantDecl.toString()))
            throw new TypeAlreadyDeclaredException(this, constantDecl.toString()).commit();

        // Insert the Constant Declaration
        this.symbolMap.put(constantDecl.toString(), constantDecl);

        return null;

    }

    /**
     * <p>Inserts a {@link String}-{@link NamedType} pair into the {@link Compilation}'s symbol table.</p>
     * @param namedType The {@link NamedType} to map.
     * @throws Phase.Error If the {@link NamedType}'s name is already defined in the {@link Compilation}'s symbol
     * table.
     * @since 0.1.0
     */
    @Override
    public final Void visitNamedType(final NamedType namedType) throws Phase.Error {

        // If the Record Declaration's name exists in the symbol table
        // TODO: Originally Error Code 207- Had the same exact message defined
        if(this.symbolMap.containsKey(namedType.toString()))
            throw new TypeAlreadyDeclaredException(this, namedType.toString()).commit();

        // Insert the Record Declaration
        this.symbolMap.put(namedType.toString(), namedType);

        return null;

    }

    /**
     * <p>Inserts a {@link String}-{@link ProcTypeDecl} pair into the {@link Compilation}'s symbol table.</p>
     * @param procedureTypeDeclaration The {@link ProcTypeDecl} to map.
     * @throws Phase.Error If the {@link ProcTypeDecl}'s name is already defined in the {@link Compilation}'s symbol
     * table.
     * @since 0.1.0
     */
    @Override
    public final Void visitProcTypeDecl(final ProcTypeDecl procedureTypeDeclaration) throws Phase.Error {

        // Retrieve the name and corresponding entry
        final String name   = procedureTypeDeclaration.toString();
        final Object entry  = (this.symbolMap.containsKey(name)) ? this.symbolMap.get(name) : new HashMap<>();

        // If the name exists
        if(this.symbolMap.containsKey(name)) {

            // Throw an error for an existing non-procedure type
            if(!(entry instanceof Map<?, ?>))
                throw new NonProcedureTypeAlreadyDeclaredException(this, name).commit();

            // TODO: Maybe this belongs in modifier checking
            // Throw an error if we're attempting to aggregate a mobile procedure
            else if(procedureTypeDeclaration.isDeclaredMobile())
                throw (this.mobileMap.get(name))
                        ? new CannotOverloadMobileException(this, procedureTypeDeclaration).commit()
                        : new NonMobileProcedureAlreadyExists(this, procedureTypeDeclaration).commit();

            // If the same overload exists
            else if(((Map<?, ?>) entry).containsKey(procedureTypeDeclaration.getSignature()))
                throw new TypeAlreadyDeclaredException(this, name).commit();

        // Otherwise insert the overload table
        } else {

            // Insert the Overload table & emplace the mobile flag
            this.symbolMap.put(name, entry);
            this.mobileMap.put(name, procedureTypeDeclaration.isDeclaredMobile());

        }

        // Insert the entry
        ((Map<String, ProcTypeDecl>) entry).put(procedureTypeDeclaration.getSignature(), procedureTypeDeclaration);

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
        if(this.symbolMap.containsKey(protocolTypeDeclaration.toString()))
            throw new TypeAlreadyDeclaredException(this, protocolTypeDeclaration.toString());

        // Insert the Protocol Declaration
        this.symbolMap.put(protocolTypeDeclaration.toString(), protocolTypeDeclaration);

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

        // If the Record Declaration's name exists in the symbol table
        // TODO: Originally Error Code 202- Had the same exact message defined
        if(this.symbolMap.containsKey(recordTypeDeclaration.toString()))
            throw new TypeAlreadyDeclaredException(this, recordTypeDeclaration.toString()).commit();

        // Insert the Record Declaration
        this.symbolMap.put(recordTypeDeclaration.toString(), recordTypeDeclaration);

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
         * <p>Constructs the {@link ResolveTopLevelDeclarations.TypeAlreadyDeclaredException}.</p>
         * @param culpritInstance The {@link TypeAlreadyDeclaredException} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected TypeAlreadyDeclaredException(final ResolveTopLevelDeclarations culpritInstance,
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

            // Retrieve the culprit & initialize the StringBuilder
            final ResolveTopLevelDeclarations culpritInstance = (ResolveTopLevelDeclarations) this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName() + ": "
                    + String.format(Message, this.typename);

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
         * <p>Constructs the {@link ResolveTopLevelDeclarations.TypeAlreadyDeclaredException}.</p>
         * @param culpritInstance The {@link TypeAlreadyDeclaredException} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected NonProcedureTypeAlreadyDeclaredException(final ResolveTopLevelDeclarations culpritInstance,
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

            // Retrieve the culprit & initialize the StringBuilder
            final ResolveTopLevelDeclarations culpritInstance = (ResolveTopLevelDeclarations) this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName() + ": "
                    + String.format(Message, this.typename);

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
         * <p>Constructs the {@link ResolveTopLevelDeclarations.TypeAlreadyDeclaredException}.</p>
         * @param culpritInstance The {@link TypeAlreadyDeclaredException} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected CannotOverloadMobileException(final ResolveTopLevelDeclarations culpritInstance,
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

            // Retrieve the culprit & initialize the StringBuilder
            final ResolveTopLevelDeclarations culpritInstance = (ResolveTopLevelDeclarations) this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName() + ": "
                    + String.format(Message, this.procedureTypeDeclaration.toString());

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
         * <p>Constructs the {@link ResolveTopLevelDeclarations.TypeAlreadyDeclaredException}.</p>
         * @param culpritInstance The {@link TypeAlreadyDeclaredException} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected NonMobileProcedureAlreadyExists(final ResolveTopLevelDeclarations culpritInstance,
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

            // Retrieve the culprit & initialize the StringBuilder
            final ResolveTopLevelDeclarations culpritInstance = (ResolveTopLevelDeclarations) this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName() + ": "
                    + String.format(Message, this.procedureTypeDeclaration.toString());

        }

    }

    /// --------
    /// Listener

    /**
     * <p>Represents the corresponding {@link ResolveTopLevelDeclarations.Listener} that handles receiving
     * {@link Phase.Message}s from the {@link ResolveTopLevelDeclarations} Phase.</p>
     * @see Phase
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 0.1.0
     */
    public static abstract class Listener extends Phase.Listener { /* Placeholder */ }

}
