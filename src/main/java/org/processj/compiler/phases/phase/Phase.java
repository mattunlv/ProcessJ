package org.processj.compiler.phases.phase;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.alt.AltCase;
import org.processj.compiler.ast.alt.AltStat;
import org.processj.compiler.ast.expression.ArrayLiteral;
import org.processj.compiler.ast.expression.Assignment;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.utilities.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static org.processj.compiler.utilities.Files.HasContent;
import static org.processj.compiler.utilities.Reflection.NewInstanceOf;

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

        protected static boolean MobileProcedureNotOverloaded(final Phase phase,
                                                              final Type type,
                                                              final ProcTypeDecl procedureTypeDeclaration)
                throws Phase.Error {

            // Initialize the result
            final boolean found = (type instanceof ProcTypeDecl);

            // Assert for all entries, if a Procedure Type was found, the existing Procedure Type is not
            // declared 'mobile'.
            if(found && ((ProcTypeDecl) type).isMobile())
                MobileProcedureOverloaded.Assert(phase, procedureTypeDeclaration);

            // Return the result
            return found;

        }

        protected static boolean NotOverloadingNonMobileProcedure(final Phase phase,
                                                                  final Type type,
                                                                  final ProcTypeDecl procedureTypeDeclaration)
                throws Phase.Error {

            // Initialize the result
            final boolean found = (type instanceof ProcTypeDecl);

            // Assert for all entries, if a Procedure Type was found, the specified Procedure Type
            // is not declared 'mobile'.
            if(found && procedureTypeDeclaration.isMobile())
                NonMobileProcedureTypeDefined.Assert(phase, type);

            // Return the result
            return found;

        }

        protected static void MobileProcedureSpecifiesNonVoidReturnType(final Phase phase,
                                                                        final ProcTypeDecl procedureTypeDeclaration)
                throws Phase.Error {

            if(procedureTypeDeclaration.isMobile() && !procedureTypeDeclaration.getReturnType().isVoidType())
                MobileProcedureSpecifiesNonVoidReturnType.Assert(phase, procedureTypeDeclaration);

        }

        protected static void Defines(final Phase phase,
                                      final Type type)
                throws Phase.Error {

            if(!phase.getScope().put(type.toString(), type))
                TypeDefined.Assert(phase, type);

        }

        protected static void Defines(final Phase phase,
                                      final ProcTypeDecl procedureTypeDeclaration)
                throws Phase.Error {

            // Assert that if the Procedure is specified as 'mobile', it also specifies a void
            // return Type
            MobileProcedureSpecifiesNonVoidReturnType(phase, procedureTypeDeclaration);

            // Initialize a handle to the current scope & retrieve the result
            final SymbolMap     scope   = phase.getScope();
            final List<Object>  result  = scope.get(procedureTypeDeclaration.toString());

            // If the procedure has not been defined
            if(!result.isEmpty()) {

                // Initialize a handle to the result
                final Object preliminary = result.get(0);

                // Assert for all overloads, the Procedure Type or the existing Type are not declared 'mobile'
                if(!(preliminary instanceof SymbolMap))
                    NonProcedureTypeDefined.Assert(phase, procedureTypeDeclaration);

                // Assert for all existing Procedure Types, the Procedure Type of the existing Type are not declared
                // 'mobile'
                scope.forEachEntryUntil(type -> MobileProcedureNotOverloaded(phase, (Type) type, procedureTypeDeclaration)
                        || NotOverloadingNonMobileProcedure(phase, (Type) type, procedureTypeDeclaration));

                // Cast the result
                final SymbolMap procedures = (SymbolMap) result;

                // Assert the procedure definition is unique
                if(!procedures.put(procedureTypeDeclaration.getSignature(), procedureTypeDeclaration))
                    TypeDefined.Assert(phase, procedureTypeDeclaration);

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




        protected static void VisibleToEnclosingParFor(final Phase phase,
                                                       final Expression expression) throws Phase.Error {

            phase.getScope().forEachContextUntil(context -> {

                final boolean found = (context instanceof ForStat) && ((ForStat) context).isPar();

                if(found) ((ForStat) context).vars.add(expression);

                return found;

            });

        }

        protected static void DefinesLabel(final Phase phase,
                                           final String label,
                                           final Statement statement)
                throws Phase.Error {

            if(!phase.getScope().put(label, statement))
                throw new TypeDefinedException(phase, label).commit();

        }

        protected static void Define(final Phase phase,
                                     final Name name)
                throws Phase.Error {

            if(!phase.getScope().put(name))
                throw new NameDefinedException(phase, name.toString()).commit();

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

        /**
         * <p>{@link Phase.Error} to be emitted if the {@link Type}'s {@link Name} is already defined in the
         * current scope.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class TypeDefined extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link TypeDefined} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Phase.Error.Assert(TypeDefined.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link TypeDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected TypeDefined(final Phase culprit,
                                  final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the {@link Type}'s {@link Name} is already defined in the
         * current scope as a non-procedure {@link Type}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NonProcedureTypeDefined extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NonProcedureTypeDefined} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Phase.Error.Assert(NonProcedureTypeDefined.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NonProcedureTypeDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected NonProcedureTypeDefined(final Phase culprit,
                                              final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the {@link Type}'s {@link Name} is already defined in the
         * current scope as a non-mobile procedure {@link Type}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NonMobileProcedureTypeDefined extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NonMobileProcedureTypeDefined} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Phase.Error.Assert(NonMobileProcedureTypeDefined.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NonMobileProcedureTypeDefined} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected NonMobileProcedureTypeDefined(final Phase culprit, final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the {@link Type}'s {@link Name} is already defined in the
         * current scope.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class MobileProcedureOverloaded extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link MobileProcedureOverloaded} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Phase.Error.Assert(MobileProcedureOverloaded.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link MobileProcedureOverloaded} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected MobileProcedureOverloaded(final Phase culprit,
                                                final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the {@link ProcTypeDecl} is mobile procedure with a specified
         * non-void return {@link Type}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class MobileProcedureSpecifiesNonVoidReturnType extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link MobileProcedureSpecifiesNonVoidReturnType} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Phase.Error.Assert(MobileProcedureSpecifiesNonVoidReturnType.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link MobileProcedureSpecifiesNonVoidReturnType} to its default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected MobileProcedureSpecifiesNonVoidReturnType(final Phase culprit,
                                                                final AST instance) {
                super(culprit);

                this.instance       = instance    ;
                this.instanceClass  = (instance != null) ? instance.getClass() : null;

            }

        }



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
         * <p>{@link Phase.Error} class that encapsulates the pertinent information of attempting to define a {@link Type}
         * that is already defined.</p>
         * <p>Error Code: 200</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        private static class NameDefinedException extends Phase.Error {

            /// ------------------------
            /// Private Static Constants

            /**
             * <p>Standard error message {@link String} for reporting.</p>
             */
            private final static String Message = "Name '%s' already declared in this scope";

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link String} value of the name that was already defined.</p>
             */
            private final String name;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NameDefinedException}.</p>
             * @param culpritInstance The {@link NameDefinedException} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected NameDefinedException(final Phase culpritInstance,
                                           final String name) {
                super(culpritInstance);
                this.name = name;
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
                return String.format(Message, this.name);

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

    protected static class ReachabilityAssert {

        protected static Set<String> WithFailures(final Class<?>... classes) {

            return Arrays.stream(classes).map(Class::getName).collect(Collectors.toSet());

        }

        protected static boolean EnclosedInIterativeContext(final Phase phase) throws Error {

            return phase.getScope().forEachContextUntil(context -> (context instanceof IterativeStatement));

        }

        protected static boolean EnclosedInBreakableContext(final Phase phase) throws Error {

            return phase.getScope().forEachContextUntil(context -> context instanceof BreakableStatement);

        }

        protected static boolean EnclosedInParallelContext(final Phase phase) throws Error {

            return phase.getScope().forEachContextUntil(context -> context instanceof ParBlock);

        }

        protected static boolean EnclosedInChoiceContext(final Phase phase) throws Error {

            return phase.getScope().forEachContextUntil(context -> context instanceof AltStat);

        }

        protected static boolean EnclosedInParallelOrChoiceContext(final Phase phase) throws Error {

            return phase.getScope().forEachContextUntil(context ->
                    (context instanceof AltStat)|| (context instanceof ParBlock));

        }

        protected static boolean EnclosedInBreakableContext(final Phase phase,
                                                            final AST construct) throws Phase.Error {

            final boolean inBreakableContext = EnclosedInBreakableContext(phase);

            if(!inBreakableContext)
                NotEnclosedInBreakableContext.Assert(phase, construct, phase.getScope().getContext());

            return inBreakableContext;

        }

        protected static boolean EnclosedInIterativeContext(final Phase phase,
                                                            final AST construct) throws Phase.Error {

            final boolean inIterativeContext = EnclosedInIterativeContext(phase);

            if(!inIterativeContext)
                NotEnclosedInIterativeContext.Assert(phase, construct, phase.getScope().getContext());

            return inIterativeContext;

        }

        protected static boolean NotEnclosedInChoiceContext(final Phase phase,
                                                            final AST construct) throws Phase.Error {

            final boolean inChoiceContext = EnclosedInChoiceContext(phase);

            // Assert the construct is not enclosed in a choice Context
            if(inChoiceContext)
                EnclosedInChoiceContext.Assert(phase, construct, phase.getScope().getContext());

            // Return the result
            return !inChoiceContext;

        }

        protected static boolean NotEnclosedInParallelContext(final Phase phase,
                                                              final AST construct) throws Phase.Error {

            final boolean inParallelContext = EnclosedInParallelContext(phase);

            // Assert the construct is not enclosed in a parallel Context
            if(inParallelContext)
                EnclosedInParallelContext.Assert(phase, construct, phase.getScope().getContext());

            // Return the result
            return !inParallelContext;

        }

        protected static boolean NotEnclosedInParallelOrChoiceContext(final Phase phase,
                                                                      final AST construct) throws Phase.Error {

            // Initialize a handle to the Context
            final SymbolMap.Context enclosingContext = phase.getScope()
                    .forEachContextRetrieve(context -> (context instanceof ParBlock) || (context instanceof AltStat));

            // Initialize a handle to the result
            final boolean enclosingContextParallelOrChoice = enclosingContext != null;

            // Assert the construct is not enclosed in a parallel Context
            if(enclosingContext instanceof ParBlock)
                EnclosedInParallelContext.Assert(phase, construct, phase.getScope().getContext());

            // Assert the construct is not enclosed in a choice Context
            if(enclosingContext instanceof AltStat)
                EnclosedInParallelContext.Assert(phase, construct, phase.getScope().getContext());

            // Return the result
            return !enclosingContextParallelOrChoice;

        }

        protected static boolean EnclosingIterativeContextBreaksAndReachable(final Phase phase,
                                                                             final Statement statement,
                                                                             final Set<String>... failures)
                throws Phase.Error {

            // Initialize a handle to the Context
            final SymbolMap.Context enclosingContext = phase.getScope().forEachContextRetrieve(
                    context -> (context instanceof ConditionalStatement)
                            || (context instanceof ParBlock));

            // Assert the statement's enclosing Context isn't parallel
            if(enclosingContext instanceof ParBlock)
                EnclosedInParallelContext.Assert(phase, statement, enclosingContext);

            // Initialize a handle to the result
            final boolean enclosingIterativeContextBreaks = enclosingContext != null;

            // Assert the Statement is enclosed in a Breakable Context
            if(!enclosingIterativeContextBreaks)
                NotEnclosedInIterativeContext.Assert(phase, statement, phase.getScope().getContext());

                // Assert that the enclosing Context does not define a constant true evaluation expression
            else if(TypeAssert.IsConstantTrue(
                    ((ConditionalStatement) enclosingContext).getEvaluationExpression()))
                EnclosingIterativeContextDoesNotTerminate.Assert(phase, statement, enclosingContext);

                // Assert that the enclosing Context does not define a constant false evaluation expression
            else if(TypeAssert.IsConstantFalse(
                    ((ConditionalStatement) enclosingContext).getEvaluationExpression()))
                EnclosingIterativeContextIsNotReachable.Assert(phase, statement, enclosingContext);


            return enclosingIterativeContextBreaks;

        }

        protected static boolean DoesNotContainHaltingProcedures(final Phase phase,
                                                                 final Block block)
                throws Phase.Error {

            // Initialize a handle to the Statements
            final Sequence<? extends Statement> statements = block.getStatements();

            // Initialize the preliminary result
            boolean haltsBeforeCompletion = false;

            // Iterate

            int index = 0; for(;!haltsBeforeCompletion && (index < statements.size()); index++)
                haltsBeforeCompletion = (statements.child(index) instanceof StopStat)
                        && ((index < statements.size() - 1));

            // Assert that if the Context contains a Stop Statement, it's the last one
            if(!haltsBeforeCompletion)
                ContextDoesNotTerminate.Assert(phase, statements.child(index), block);

            return haltsBeforeCompletion;

        }

        protected static boolean ConditionalContextReachable(final Phase phase,
                                                             final IfStat ifStatement)
                throws Phase.Error {

            if(TypeAssert.IsConstantTrue(ifStatement.getEvaluationExpression()))
                BranchConditionalContextNotReachable.Assert(phase, ifStatement);

            if(TypeAssert.IsConstantFalse(ifStatement.getEvaluationExpression()))
                ConditionalContextNotReachable.Assert(phase, ifStatement);

            return true;

        }

        /// -----------
        /// Phase.Error

        /**
         * <p>{@link Phase.Error} to be emitted if the conditional {@link org.processj.compiler.ast.SymbolMap.Context}'s
         * alternative {@link org.processj.compiler.ast.SymbolMap.Context} is not reachable.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class BranchConditionalContextNotReachable extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link BranchConditionalContextNotReachable} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Phase.Error.Assert(BranchConditionalContextNotReachable.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ConditionalContextNotReachable} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected BranchConditionalContextNotReachable(final Phase culprit,
                                                           final AST instance) {
                super(culprit);

                this.instance = instance    ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the conditional {@link org.processj.compiler.ast.SymbolMap.Context}
         * is not reachable.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ConditionalContextNotReachable extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ConditionalContextNotReachable} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             */
            protected static void Assert(final Phase phase, final AST instance) {

                Phase.Error.Assert(ConditionalContextNotReachable.class, phase, instance);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ConditionalContextNotReachable} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected ConditionalContextNotReachable(final Phase culprit,
                                                     final AST instance) {
                super(culprit);

                this.instance = instance    ;
                this.instanceClass = (instance != null) ? instance.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the {@link org.processj.compiler.ast.SymbolMap.Context} does not
         * terminate.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ContextDoesNotTerminate extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ContextDoesNotTerminate} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(ContextDoesNotTerminate.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ContextDoesNotTerminate} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected ContextDoesNotTerminate(final Phase culprit,
                                              final AST instance,
                                              final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an {@link AST} is enclosed in a choice
         * {@link org.processj.compiler.ast.SymbolMap.Context}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class EnclosedInChoiceContext extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link EnclosedInChoiceContext} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(EnclosedInChoiceContext.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link EnclosedInChoiceContext} to its' default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected EnclosedInChoiceContext(final Phase culprit,
                                              final AST instance,
                                              final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an {@link AST} is enclosed in a parallel
         * {@link org.processj.compiler.ast.SymbolMap.Context}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class EnclosedInParallelContext extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link EnclosedInParallelContext} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(EnclosedInParallelContext.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link EnclosedInParallelContext} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected EnclosedInParallelContext(final Phase culprit,
                                                final AST instance,
                                                final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the enclosing {@link org.processj.compiler.ast.SymbolMap.Context}
         * does not terminate.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class EnclosingIterativeContextDoesNotTerminate extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link EnclosingIterativeContextDoesNotTerminate} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(EnclosingIterativeContextDoesNotTerminate.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link EnclosingIterativeContextDoesNotTerminate} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected EnclosingIterativeContextDoesNotTerminate(final Phase culprit,
                                                                final AST instance,
                                                                final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if the enclosing {@link org.processj.compiler.ast.SymbolMap.Context}
         * is not reachable.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class EnclosingIterativeContextIsNotReachable extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link EnclosingIterativeContextIsNotReachable} to the specified {@link Phase}'s
             * {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(EnclosingIterativeContextIsNotReachable.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link EnclosingIterativeContextIsNotReachable} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected EnclosingIterativeContextIsNotReachable(final Phase culprit,
                                                              final AST instance,
                                                              final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an {@link AST} is not enclosed in an iterative
         * {@link org.processj.compiler.ast.SymbolMap.Context}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NotEnclosedInIterativeContext extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NotEnclosedInIterativeContext} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(NotEnclosedInIterativeContext.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link EnclosedInParallelContext} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected NotEnclosedInIterativeContext(final Phase culprit,
                                                    final AST instance,
                                                    final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

        /**
         * <p>{@link Phase.Error} to be emitted if an {@link AST} is not enclosed in an breakable
         * {@link org.processj.compiler.ast.SymbolMap.Context}.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class NotEnclosedInBreakableContext extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link NotEnclosedInBreakableContext} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param instance The problem {@link AST} instance.
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final AST instance, final SymbolMap.Context context) {

                Phase.Error.Assert(NotEnclosedInIterativeContext.class, phase, instance, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The class object of the {@link AST} whose assertion failed.</p>
             */
            private final Class<? extends AST>                  instanceClass   ;

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The {@link AST} instance that failed the assertion.</p>
             */
            private final AST                                   instance        ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link NotEnclosedInBreakableContext} to its'.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected NotEnclosedInBreakableContext(final Phase culprit,
                                                    final AST instance,
                                                    final SymbolMap.Context context) {
                super(culprit);

                this.instance = instance    ;
                this.context  = context     ;

                this.instanceClass = (instance != null) ? instance.getClass() : null;
                this.contextClass  = (context != null) ? context.getClass() : null;

            }

        }

    }


    protected static class SemanticAssert {

        protected static boolean DefinesConstantEvaluationExpression(final DoStat doStatement)
                throws Phase.Error {

            // Initialize a handle to the Do Statement's Evaluation Expression
            final Expression evaluationExpression = doStatement.getEvaluationExpression();

            // Return the result
            return (evaluationExpression != null) && (evaluationExpression.isConstant());

        }

        protected static void RewriteArrayType(final ConstantDecl constantDeclaration) {

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

        protected static boolean NotEmptyParallelContext(final Phase phase) throws Phase.Error {

            // Initialize a handle to the Context
            final SymbolMap.Context context = phase.getScope().getContext();

            // Initialize the result
            final boolean isParallelContextEmpty = (context instanceof ParBlock)
                    && ((ParBlock) context).getStatements().isEmpty();

            // Assert the parallel Context is not empty
            if(isParallelContextEmpty)
                ParallelContextEmpty.Assert(phase, context);

            // Return the result
            return isParallelContextEmpty;

        }



        /**
         * <p>{@link Phase.Error} to be emitted if a parallel {@link org.processj.compiler.ast.SymbolMap.Context}
         * contains an empty body.</p>
         * @see Phase
         * @see Phase.Error
         * @version 1.0.0
         * @since 0.1.0
         */
        protected static class ParallelContextEmpty extends Phase.Error {

            /// ------------------------
            /// Protected Static Methods

            /**
             * <p>Emits the {@link ParallelContextEmpty} to the specified {@link Phase}'s {@link Listener}.</p>
             * @param phase The invoking {@link Phase}.
             * @param context The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}.
             */
            protected static void Assert(final Phase phase, final SymbolMap.Context context) {

                Phase.Error.Assert(ParallelContextEmpty.class, phase, context);

            }

            /// --------------
            /// Private Fields

            /**
             * <p>The {@link AST}'s enclosing {@link org.processj.compiler.ast.SymbolMap.Context}'s class object.</p>
             */
            private final Class<? extends SymbolMap.Context>    contextClass    ;

            /**
             * <p>The enclosing {@link org.processj.compiler.ast.SymbolMap.Context} instance.</p>
             */
            private final SymbolMap.Context                     context         ;

            /// ------------
            /// Constructors

            /**
             * <p>Constructs the {@link ParallelContextEmpty} to its' default state.</p>
             * @param culprit The {@link Phase} instance that raised the error.
             * @see Phase
             * @see Phase.Error
             * @since 0.1.0
             */
            protected ParallelContextEmpty(final Phase culprit,
                                           final SymbolMap.Context context) {
                super(culprit);

                this.context      = context                                         ;
                this.contextClass = (context != null) ? context.getClass() : null   ;

            }

        }

    }


    protected static class TypeAssert {

        protected static boolean IsConstant(final Expression expression) {

            // Return the result
            return (expression != null) && expression.isConstant();

        }

        protected static boolean IsConstantTrue(final Expression expression) {

            return IsConstant(expression) && ((Boolean) expression.constantValue());

        }

        protected static boolean IsConstantFalse(final Expression expression) {

            return IsConstant(expression) && !((Boolean) expression.constantValue());

        }

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

        protected final void commit() {

            this.getPhase().getListener().notify(this);

        }

    }

    public static class Error extends Message {

        /// ------------------------
        /// Protected Static Methods

        /**
         * <p>Constructs an {@link Phase.Error} from the specified class object, {@link Phase}, & variadic parameters.
         * Once the {@link Phase.Error} is instantiated, this method will notify the specified {@link Phase}'s
         * {@link Phase.Listener} of the {@link Phase.Error}</p>
         * @param messageType The class object corresponding to the {@link Phase.Error} to instantiate.
         * @param phase The {@link Phase} where the assertion was raised.
         * @param parameters Any parameters that pertain to the {@link Phase.Error}.
         * @param <MessageType> Parameteric Type of the {@link Phase.Error}.
         */
        protected static <MessageType extends Error> void Assert(final Class<MessageType> messageType,
                                                                 final Phase phase,
                                                                 final Object... parameters) {

            // Initialize a handle to the message instance
            Phase.Error messageInstance;

            try {

                messageInstance = NewInstanceOf(messageType, phase, parameters);

            } catch(final InvocationTargetException | InstantiationException | IllegalAccessException exception) {

                // TODO: Fatal Error
                messageInstance = null;

            }

            // Simple assert
            assert messageInstance != null;

            // Notify the listener
            phase.getListener().notify(messageInstance);

        }

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
