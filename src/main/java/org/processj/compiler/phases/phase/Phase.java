package org.processj.compiler.phases.phase;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.alt.AltCase;
import org.processj.compiler.ast.alt.AltStat;
import org.processj.compiler.ast.expression.ArrayLiteral;
import org.processj.compiler.ast.expression.Assignment;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.utilities.*;

import java.util.*;

import static org.processj.compiler.utilities.Files.HasContent;

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

    /// -------
    /// Classes

    protected static class NameAssert {

        /// ------------------------
        /// Protected Static Methods

        protected static boolean NotMobileOverload(final Phase phase,
                                                   final Type type,
                                                   final ProcTypeDecl procedureTypeDeclaration)
                throws Phase.Error {

            // Initialize the result
            final boolean found = (type instanceof ProcTypeDecl);

            // Assert for all entries, if a Procedure Type was found, the existing Procedure Type is not
            // declared 'mobile'.
            if(found && ((ProcTypeDecl) type).isMobile())
                throw new MobileOverloadException(phase, procedureTypeDeclaration);

            // Return the result
            return found;

        }

        protected static boolean NonMobileProcedureDefined(final Phase phase,
                                                           final Type type,
                                                           final ProcTypeDecl procedureTypeDeclaration)
                throws Phase.Error {

            // Initialize the result
            final boolean found = (type instanceof ProcTypeDecl);

            // Assert for all entries, if a Procedure Type was found, the specified Procedure Type
            // is not declared 'mobile'.
            if(found && procedureTypeDeclaration.isMobile())
                throw new NonMobileProcedureDefinedException(phase, procedureTypeDeclaration).commit();

            // Return the result
            return found;

        }

        protected static void VisibleToEnclosingParFor(final Phase phase,
                                                       final Expression expression) throws Phase.Error {

            phase.getScope().forEachContextUntil(context -> {

                final boolean found = (context instanceof ForStat) && ((ForStat) context).isPar();

                if(found) ((ForStat) context).vars.add(expression);

                return found;

            });

        }

        protected static void Define(final Phase phase,
                                     final Type type)
                throws Phase.Error {

            if(!phase.getScope().put(type))
                throw new TypeDefinedException(phase, type.toString()).commit();

        }

        protected static void Define(final Phase phase,
                                     final Statement statement)
                throws Phase.Error {

            // Initialize a handle to the label
            final String label = statement.getLabel();

            // Insert the label, if any
            if(HasContent(label) && !phase.getScope().put(statement.getLabel(), statement))
                throw new LabelDefinedException(phase, statement).commit();

        }

        protected static void Define(final Phase phase,
                                     final ProcTypeDecl procedureTypeDeclaration)
                throws Phase.Error {

            // Initialize a handle to the current scope & retrieve the result
            final SymbolMap     scope   = phase.getScope();
            final List<Object>  result  = scope.get(procedureTypeDeclaration.toString());

            // If the procedure has not been defined
            if(!result.isEmpty()) {

                // Initialize a handle to the result
                final Object preliminary = result.get(0);

                // Assert for all overloads, the Procedure Type or the existing Type are not declared 'mobile'
                // Assert the result is a Symbol Map
                if(!(preliminary instanceof SymbolMap))
                    throw new NonProcedureTypeDefinedException(phase, procedureTypeDeclaration.toString()).commit();

                // Assert for all existing Procedure Types, the Procedure Type of the existing Type are not declared
                // 'mobile'
                scope.forEachEntryUntil(type -> NotMobileOverload(phase, (Type) type, procedureTypeDeclaration)
                    || NonMobileProcedureDefined(phase, (Type) type, procedureTypeDeclaration));

                // Cast the result
                final SymbolMap procedures = (SymbolMap) result;

                // Assert the procedure definition is unique
                if(!procedures.put(procedureTypeDeclaration.getSignature(), procedureTypeDeclaration))
                    throw new TypeDefinedException(phase, procedureTypeDeclaration.toString()).commit();

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

        protected static Object Resolved(final Phase phase, final Name symbol) throws Phase.Error {

            // Initialize a handle to the name & package name
            final String name           = symbol.getName()          ;
            final String packageName    = symbol.getPackageName()   ;

            // Attempt to retrieve a result
            List<Object> names = phase.getScope().get(packageName, name);

            // If the List is empty, the name is undefined in the scope
            if(names.isEmpty())
                if((packageName == null) || (packageName.isEmpty()) || packageName.isBlank())
                    throw new SymbolNotDefinedInPackageException(phase, name, packageName).commit();

                else throw new SymbolNotDefinedInScopeException(phase, name).commit();

                // Otherwise, we may have gotten an ambiguous result (name clash)
            else if(names.size() > 1)
                throw new AmbiguousSymbolException(phase, name, names).commit();

            // Initialize a handle to the result
            Object result = names.get(0);

            // Recur on the NamedType
            if(result instanceof NamedType)
                Resolved(phase, ((NamedType) result).getName());

            return result;

        }

        protected static void Resolved(final Phase phase, final ConstantDecl constantDeclaration) throws Phase.Error {

            constantDeclaration.setType((Type) Resolved(phase, constantDeclaration.getName()));

        }

        protected static void Resolved(final Phase phase, final LocalDecl localDeclaration) throws Phase.Error {

            localDeclaration.setType((Type) Resolved(phase, localDeclaration.getName()));

        }

        protected static void Resolved(final Phase phase, final ParamDecl parameterDeclaration) throws Phase.Error {

            parameterDeclaration.setType((Type) Resolved(phase, parameterDeclaration.getName()));

        }

        protected static void Resolved(final Phase phase, final ArrayType arrayType) throws Phase.Error {

            arrayType.setComponentType((Type) Resolved(phase, arrayType.getName()));

        }

        protected static void Resolved(final Phase phase, final CastExpr castExpression) throws Phase.Error {

            castExpression.setType((Type) Resolved(phase, castExpression.getCastType().getName()));

        }

        protected static void Resolved(final Phase phase, final ChannelType channelType) throws Phase.Error {

            channelType.setComponentType((Type) Resolved(phase, channelType.getName()));

        }

        protected static void Resolved(final Phase phase, final ChannelEndType channelEndType) throws Phase.Error {

            channelEndType.setComponentType((Type) Resolved(phase, channelEndType.getName()));

        }

        protected static void Resolved(final Phase phase, final Invocation invocation) throws Phase.Error {

            invocation.setType((Type) Resolved(phase, invocation.getName()));

        }

        protected static void Resolved(final Phase phase, final NameExpr nameExpression) throws Phase.Error {

            nameExpression.setType((Type) Resolved(phase, nameExpression.getName()));

        }

        protected static void Resolved(final Phase phase, final NewArray newArray) throws Phase.Error {

            newArray.setType((Type) Resolved(phase, newArray.getComponentType().getName()));

        }

        protected static void Resolved(final Phase phase, final RecordLiteral recordLiteral) throws Phase.Error {

            recordLiteral.setType((Type) Resolved(phase, recordLiteral.getName()));

        }

        protected static void Resolved(final Phase phase, final ProtocolLiteral protocolLiteral) throws Phase.Error {

            protocolLiteral.setType((Type) Resolved(phase, protocolLiteral.getName()));

            NameAssert.ResolvedTagCase(phase, protocolLiteral);

        }

        protected static void ResolvedTagCase(final Phase phase, final ProtocolLiteral protocolLiteral) throws Phase.Error {

            // Initialize a handle to the ProtocolLiteral's Type & Protocol Case
            final ProtocolTypeDecl type     = (ProtocolTypeDecl) protocolLiteral.getType();
            final ProtocolCase     tagCase  = type.getCaseFrom(protocolLiteral.getTagLiteral());

            // Assert the case was found
            if(tagCase == null)
                PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                        .addAST(protocolLiteral)
                        .addError(VisitorMessageNumber.NAME_CHECKER_402)
                        .addArguments("Protocol Case " + protocolLiteral.getTagLiteral() + " not defined.")
                        .build());

            // Update the case
            protocolLiteral.setProtocolCase(tagCase);

        }

        protected static void FlattenedAltStatement(final AltStat altStatement)
                throws Phase.Error {

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
                throws Phase.Error {

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

        // TODO: Error Code 203
        /**
         * <p>{@link Phase.Error} class that encapsulates the pertinent information of attempting to define a {@link Type}
         * that is already defined.</p>
         * <p>Error Code: 200</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class TypeDefinedException extends Phase.Error {

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
             * <p>Constructs the {@link TypeDefinedException}.</p>
             * @param culpritInstance The {@link TypeDefinedException} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected TypeDefinedException(final Phase culpritInstance,
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
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NonProcedureTypeDefinedException extends Phase.Error {

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
             * <p>Constructs the {@link NonProcedureTypeDefinedException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected NonProcedureTypeDefinedException(final Phase culpritInstance,
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

        // TODO: Error Code 400
        /**
         * <p>{@link Phase.Error} class that encapsulates the pertinent information of attempting to define a label
         * that is already defined.</p>
         * <p>Error Code: 200</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class LabelDefinedException extends Phase.Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message
                    = "Label with name '%s' already declared in this scope; found: '%s'";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link Statement} that specified a label.</p>
             */
            private final Statement labelledStatement;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link LabelDefinedException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected LabelDefinedException(final Phase culpritInstance,
                                            final Statement labelledStatement) {
                super(culpritInstance);
                this.labelledStatement = labelledStatement;
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
                return String.format(Message, this.labelledStatement.getLabel(), this.labelledStatement);

            }

        }

        /**
         * <p>{@link Phase.Error} class that encapsulates the pertinent information of attempting to overload a
         * {@link ProcTypeDecl} specified as 'mobile'.</p>
         * <p>Error Code: 206</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class MobileOverloadException extends Phase.Error {

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
             * <p>Constructs the {@link MobileOverloadException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected MobileOverloadException(final Phase culpritInstance,
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
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NonMobileProcedureDefinedException extends Phase.Error {

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
             * <p>Constructs the {@link NonMobileProcedureDefinedException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected NonMobileProcedureDefinedException(final Phase culpritInstance,
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
         * <p>{@link Phase.Error} class that encapsulates the pertinent information of a symbol that was not defined
         * or visible in the current scope.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class SymbolNotDefinedInScopeException extends Phase.Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Symbol '%s' not defined in scope.";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the undefined symbol.</p>
             */
            private final String symbolName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link SymbolNotDefinedInScopeException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected SymbolNotDefinedInScopeException(final Phase culpritInstance, final String symbolName) {
                super(culpritInstance);
                this.symbolName = symbolName;
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
                return String.format(Message, this.symbolName);

            }

        }

        /**
         * <p>{@link Phase.Error} class that encapsulates the pertinent information of a symbol that was not defined
         * or visible in the current package.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class SymbolNotDefinedInPackageException extends Phase.Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Symbol '%s' not defined in package: %s.";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the undefined symbol.</p>
             */
            private final String symbolName;

            /**
             * <p>The {@link String} value of the undefined symbol.</p>
             */
            private final String packageName;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link SymbolNotDefinedInScopeException}.</p>
             * @param culpritInstance The {@link NameChecker} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected SymbolNotDefinedInPackageException(final Phase culpritInstance,
                                                         final String symbolName,
                                                         final String packageName) {
                super(culpritInstance);
                this.symbolName     = symbolName    ;
                this.packageName    = packageName   ;
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
                return String.format(Message, this.symbolName, this.packageName);

            }

        }

        /**
         * <p>{@link Phase.Error} class that encapsulates the pertinent information of an ambiguous symbol.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class AmbiguousSymbolException extends Phase.Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Ambiguous symbol '%s'. Found: %s";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the undefined symbol.</p>
             */
            private final String symbolName;

            /**
             * <p>The {@link List} containing the alternative visible symbols.</p>
             */
            private final List<Object> symbols;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link SymbolNotDefinedInScopeException}.</p>
             * @param culpritInstance The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected AmbiguousSymbolException(final Phase culpritInstance,
                                               final String symbolName,
                                               final List<Object> symbols) {
                super(culpritInstance);
                this.symbolName = symbolName    ;
                this.symbols    = symbols       ;
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

                // Instantiate the StringBuilder
                final StringBuilder stringBuilder = new StringBuilder("{ ");

                // Append the found names
                this.symbols.forEach(element -> stringBuilder.append(element).append(", "));

                // Build the String
                final String listString = stringBuilder.toString();

                // Return the resultant error message
                return String.format(Message, this.symbolName,
                        listString.substring(0, listString.length() - 2) + " }");

            }

        }

    }

    protected static class SemanticAssert {

        protected static void RewriteArrayType(final ConstantDecl constantDeclaration) throws Phase.Error {

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

        protected static void MobileProcedureVoidYielding(final Phase phase,
                                                          final ProcTypeDecl procedureTypeDeclaration)
                throws Phase.Error {

            if(procedureTypeDeclaration.isMobile() && !procedureTypeDeclaration.getReturnType().isVoidType())
                PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                        .addAST(procedureTypeDeclaration)
                        .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_205)
                        .addArguments(procedureTypeDeclaration.toString())
                        .build());

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

        protected static void SetEnclosingContextYields(final Phase phase)
                throws Phase.Error {

            phase.getScope().forEachContextUntil(SymbolMap.Context::setYields);

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

    }

    protected static class TypeAssert {

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
     * @see Phase.Error
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
         * @see Phase.Error
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
     * @see Phase.Error
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
         * @see Phase.Error
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
     * {@link Compilation}.</p>
     * @see Phase.Error
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
         * @see Phase.Error
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
        protected final static LogInfo      Info = Log::Info     ;

        /**
         * <p>The warning logging method that handles warning messages.</p>
         */
        protected final static LogWarning   Warning = Log::Warn  ;

        /**
         * <p>The error logging method that handles error messages.</p>
         */
        protected final static LogError     Error = Log::Error   ;

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
