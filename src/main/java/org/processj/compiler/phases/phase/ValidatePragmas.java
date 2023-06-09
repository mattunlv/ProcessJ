package org.processj.compiler.phases.phase;

import org.processj.compiler.ast.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * <P>Encapsulates a {@link Pragma} validation {@link Phase} that iterates a {@link Compilation}'s {@link Map} of
 * key-value pairs that correspond to its' {@link Pragma}s (if any).
 * Allows for loosely-coupled dependencies between the {@link ValidatePragmas} & the rest of the compiler.</p>
 * @see Phase
 * @author Jan B. Pedersen
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class ValidatePragmas extends Phase {

    /// ------------------------
    /// Private Static Constants

    /**
     * <p>The set of valid {@link Pragma}s.</p>
     */
    private final static Set<String> ValidPragmas = Set.of(
            "LIBRARY",
            "LANGUAGE",
            "NATIVE",
            "NATIVELIB",
            "FILE");

    /**
     * <p>A {@link Map} containing the set of valid {@link Pragma} arguments.</p>
     */
    private final static Map<String, Integer> PragmaArguments = Map.of(
            "LIBRARY", 0,
            "LANGUAGE", 1,
            "NATIVE", 0,
            "NATIVELIB", 1,
            "FILE", 1);

    /// ----------------------
    /// Private Static Methods

    /// ----------
    /// Assertions

    // TODO: Refactor these to Phase.LibraryAssert
    /**
     * <p>Validates the {@link ConstantDecl}'s native declaration.</p>
     * @param validatePragmas The invoking {@link Phase}
     * @param constantDeclaration The {@link ConstantDecl} to validate
     * @param isNative flag indicating if the containing {@link Compilation} is specified as NATIVE
     * @throws Phase.Error If the {@link ConstantDecl} is invalid.
     * @since 0.1.0
     */
    private static void ValidateNativeConstantDeclaration(final ValidatePragmas validatePragmas,
                                                          final ConstantDecl constantDeclaration,
                                                          final boolean isNative)
            throws Phase.Error {

        if(!isNative && !constantDeclaration.isDeclaredNative())
            throw new ConstantDeclarationNonNativeException(validatePragmas, constantDeclaration).commit();

        else if(isNative && constantDeclaration.isDeclaredNative())
            throw new IllegalConstantDeclarationNativeException(validatePragmas, constantDeclaration).commit();

        else if(constantDeclaration.isInitialized())
            throw new ConstantDeclarationInitializedException(validatePragmas, constantDeclaration).commit();

    }

    /**
     * <p>Validates the {@link ConstantDecl}'s non-native declaration.</p>
     * @param validatePragmas The invoking {@link Phase}
     * @param constantDeclaration The {@link ConstantDecl} to validate
     * @throws Phase.Error If the {@link ConstantDecl} is invalid.
     * @since 0.1.0
     */
    private static void ValidateNonNativeConstantDeclaration(final ValidatePragmas validatePragmas,
                                                             final ConstantDecl constantDeclaration)
            throws Phase.Error {

        if(constantDeclaration.isDeclaredNative())
            throw new ConstantDeclarationNativeException(validatePragmas, constantDeclaration).commit();

        else if(!constantDeclaration.isInitialized())
            throw new ConstantDeclarationNotInitializedException(validatePragmas, constantDeclaration);

    }

    /**
     * <p>Validates the {@link ProcTypeDecl}'s non-native procedure declaration.</p>
     * @param validatePragmas The invoking {@link Phase}
     * @param procedureTypeDeclaration The {@link ProcTypeDecl} to validate
     * @throws Phase.Error If the {@link ProcTypeDecl} is invalid.
     * @since 0.1.0
     */
    private static void ValidateNonNativeProcedureDeclaration(final ValidatePragmas validatePragmas,
                                                              final ProcTypeDecl procedureTypeDeclaration)
            throws Phase.Error {

        // Procedures must contain bodies
        if(!procedureTypeDeclaration.definesBody())
            throw new ProcedureDeclarationDoesNotDefineBodyException(validatePragmas,
                    procedureTypeDeclaration).commit();

        // Procedures must not be declared native
        else if(procedureTypeDeclaration.isNative())
            throw new ProcedureDeclarationNativeException(validatePragmas,
                    procedureTypeDeclaration).commit();

    }

    /**
     * <p>Validates the {@link ProcTypeDecl}'s native procedure declaration.</p>
     * @param validatePragmas The invoking {@link Phase}
     * @param procedureTypeDeclaration The {@link ProcTypeDecl} to validate
     * @throws Phase.Error If the {@link ProcTypeDecl} is invalid.
     * @since 0.1.0
     */
    private static void ValidateNativeProcedureDeclaration(final ValidatePragmas validatePragmas,
                                                           final ProcTypeDecl procedureTypeDeclaration)
            throws Phase.Error {

        // NATIVELIB and NATIVE library files cannot contain procedures with ProcessJ bodies.
        if(procedureTypeDeclaration.definesBody())
            throw new ProcedureDeclarationDefinesBodyException(validatePragmas, procedureTypeDeclaration).commit();

            // If the Procedure is not declared native
        else if(!procedureTypeDeclaration.isNative())
            throw new ProcedureDeclarationNonNativeException(validatePragmas, procedureTypeDeclaration).commit();

    }

    /**
     * <p>Validates the {@link ProcTypeDecl}'s native procedure return type.</p>
     * @param validatePragmas The invoking {@link Phase}
     * @param procedureTypeDeclaration The {@link ProcTypeDecl} to validate
     * @throws Phase.Error If the {@link ProcTypeDecl} is invalid.
     * @since 0.1.0
     */
    private static void ValidateNativeProcedureReturnType(final ValidatePragmas validatePragmas,
                                                           final ProcTypeDecl procedureTypeDeclaration)
            throws Phase.Error {

        // Retrieve the Procedure's return type
        final Type returnType = procedureTypeDeclaration.getReturnType();

        // Moved From GenerateNativeCode Visitor
        // If the Procedure does not specify a primitive return type
        if(!(returnType instanceof PrimitiveType))
            throw new IllegalProcedureReturnTypeException(validatePragmas, procedureTypeDeclaration).commit();

        // If the Procedure specifies a barrier or timer return type
        else if(returnType.isBarrierType() || returnType.isTimerType())
            throw new IllegalProcedureBarrierOrTimerReturnTypeException(validatePragmas,
                    procedureTypeDeclaration).commit();

    }

    /**
     * <p>Validates the {@link ProcTypeDecl}'s native procedure parameter types.</p>
     * @param validatePragmas The invoking {@link Phase}
     * @param procedureTypeDeclaration The {@link ProcTypeDecl} to validate
     * @throws Phase.Error If the {@link ProcTypeDecl}'s parameter types are invalid.
     * @since 0.1.0
     */
    private static void ValidateNativeProcedureParameterTypes(final ValidatePragmas validatePragmas,
                                                              final ProcTypeDecl procedureTypeDeclaration)
            throws Phase.Error {

        for(final ParamDecl parameterDeclaration: procedureTypeDeclaration.getParameters()) {

            // Initialize a handle to the parameter type
            final Type parameterType = parameterDeclaration.getType();

            // If the ProcTypeDecl specified a non-primitive type
            if(!(parameterType instanceof PrimitiveType))
                throw new IllegalProcedureParameterTypeException(validatePragmas,
                        procedureTypeDeclaration, parameterType).commit();

                // Otherwise, If the procedure specifies a barrier or timer parameter
            else if(parameterType.isBarrierType() || parameterType.isTimerType())
                throw new IllegalProcedureBarrierOrTimerParameterTypeException(validatePragmas,
                        procedureTypeDeclaration, parameterType).commit();

        }

    }

    /**
     * <p>Decodes the specified {@link Sequence} of {@link Pragma}s into a {@link Map}.
     * This verifies the current {@link Compilation}'s set of {@link Pragma}s are specified correctly.</p>
     * @param validatePragmas The invoking {@link Phase}.
     * @param pragmas The {@link Sequence} of {@link Pragma}s to decode.
     * @return {@link Map} containing the {@link Sequence} of {@link Pragma}s as key-value pairs.
     * @throws Phase.Error when the {@link Phase} encounters a malformed {@link Pragma}.
     * @since 0.1.0
     */
    private static Map<String, String> DecodePragmas(final ValidatePragmas validatePragmas,
                                                     final Sequence<Pragma> pragmas) throws Phase.Error {

        // TODO: check values against pragmaArgValues[][];
        // Retrieve the compilation & a Pragma Map
        final Map<String, String> pragmaMap = new HashMap<>();

        // Iterate through the pragmas
        for(final Pragma pragma: pragmas) {

            // If the Pragma is Invalid
            if(!ValidPragmas.contains(pragma.toString()))
                throw new IllegalPragmaException(validatePragmas, pragma, pragma.getLine()).commit();

                // If the Pragma requires arguments but none were given
            else if(PragmaArguments.get(pragma.toString()) > 0 && pragma.getValue() == null)
                throw new PragmaRequiresArgumentException(validatePragmas, pragma, pragma.getLine()).commit();

                // If the Pragma requires no arguments, but some were given
            else if(PragmaArguments.get(pragma.toString()) == 0 && pragma.getValue() != null)
                throw new PragmaRequiresNoArgumentException(validatePragmas, pragma, pragma.getLine()).commit();

                // If the Pragma has already been decoded
            else if(pragmaMap.containsKey(pragma.toString()))
                throw new PragmaAlreadySpecifiedException(validatePragmas, pragma, pragma.getLine()).commit();

            // Send out the message
            new EnteringPragma(validatePragmas, pragma).commit();

            // Aggregate the Pragma
            pragmaMap.put(pragma.toString(), pragma.getValue().replace("\"", ""));

        }

        // Return the result
        return pragmaMap;

    }

    /**
     * <p>Validates the specified {@link Pragma} {@link Map} to make sure the {@link Compilation}'s
     * defined {@link Pragma}s are consistent.</p>
     * @param validatePragmas The invoking {@link Phase}.
     * @param pragmaMap The {@link Map} containing {@link Pragma}s as key-value pairs.
     * @throws Phase.Error If the {@link Compilation}'s {@link Pragma}s are invalid.
     * @since 0.1.0
     */
    private static void ValidatePragmaMap(final ValidatePragmas validatePragmas,
                                          final Map<String, String> pragmaMap) throws Phase.Error {

        // Initialize the flags
        final boolean isNative          = pragmaMap.containsKey("NATIVE");
        final boolean isNativeLibrary   = pragmaMap.containsKey("NATIVELIB");

        // If a LANGUAGE Pragma is not specified, throw an error
        if(!pragmaMap.containsKey("LANGUAGE"))
            throw new MissingLanguagePragmaException(validatePragmas).commit();

        // If a FILE Pragma is not specified
        else if(!pragmaMap.containsKey("FILE"))
            throw new MissingFilePragmaException(validatePragmas).commit();

        // If NATIVE & NATIVELIB are both defined
        if(isNative && isNativeLibrary)
            throw new NativeAndNativeLibException(validatePragmas).commit();

        // Retrieve the specified language
        final String language = pragmaMap.get("LANGUAGE");

        // If Native is specified but c wasn't the specified language
        if((isNative || isNativeLibrary) && !language.equals("C"))
            throw (language.equals("PROCESSJ") ? new NativeWithProcessJException(validatePragmas)
                    : new InvalidNativeLanguageException(validatePragmas, language)).commit();

    }

    /// ---------------
    /// Code Generation

    /**
     * <p>Returns the native type string for the specified type.</p>
     * @param type The {@link Type} to retrieve the corresponding native type.
     * @return {@link String} value of the corresponding native type.
     * @since 0.1.0
     */
    private static String NativeTypeStringFor(final Type type) {

        // Initialize the Type as a Primitive; This should already be checked.
        final PrimitiveType primitiveType = (PrimitiveType) type;

        // Return the result
        return (primitiveType.isStringType() ? "char*"
                : (primitiveType.isBooleanType() ? "int" : primitiveType.toString()));

    }

    /**
     * <p>Returns the native type string for the specified {@link ParamDecl}.</p>
     * @param parameterDeclaration The {@link ParamDecl} to retrieve the corresponding native type.
     * @return {@link String} value of the corresponding native type.
     * @since 0.1.0
     */
    private static String NativeTypeStringFor(final ParamDecl parameterDeclaration) {

        return (parameterDeclaration != null) && (parameterDeclaration.getType() != null) ?
                NativeTypeStringFor(parameterDeclaration.getType()) : "";

    }

    /**
     * <p>Returns the native parameter type list for the specified {@link ProcTypeDecl}.</p>
     * @param procedureTypeDeclaration The {@link ProcTypeDecl} to retrieve the corresponding native parameter type
     *                                 list.
     * @return {@link String} value of the corresponding native parameter type list.
     * @since 0.1.0
     */
    private static String NativeTypeListStringFor(final ProcTypeDecl procedureTypeDeclaration) {

        // Initialize the StringBuilder & parameters
        final StringBuilder         stringBuilder   = new StringBuilder("(");
        final Sequence<ParamDecl>   parameters      = procedureTypeDeclaration.getParameters();

        // Iterate through the list of Parameter Declarations
        for(int index = 0; index < parameters.size(); index++)
            stringBuilder.append(NativeTypeStringFor(parameters.child(index)))
                    .append(index == (parameters.size() - 1) ? "" : ", ");

        // Return the result
        return stringBuilder.append(")").toString();

    }

    /**
     * <p>Replaces all . (dots/periods) contained in the specified {@link String} with underscores, if any.</p>
     * @param string The {@link String} to replace all contained dots/periods
     * @return {@link String} value of the transformed {@link String} value.
     * @since 0.1.0
     */
    private static String Flatten(final String string) {

        return (string == null) ? "" : string.replace(".", "_") + "_";

    }

    /**
     * <p>Constructs a C preprocessor directive checking for a definition of the specified string. This will
     * flatten the specified string and convert all the characters to uppercase.</p>
     * @param string The {@link String} to create the preprocessor directive with.
     * @return {@link String} value of the preprocessor directive.
     * @since 0.1.0
     */
    private static String LibPreprocessorDirectiveOf(final String string) {

        // Initialize a transformed String
        final String flat = Flatten(string).toUpperCase();

        // Return the result
        return "#ifndef _LIB_" + flat + '\n' + "#defin _LIB_" + flat + '\n';

    }

    /**
     * <p>Constructs a C preprocessor directive checking for a definition of the specified string. This will
     * flatten the specified string and convert all the characters to uppercase.</p>
     * @param string The {@link String} to create the preprocessor directive with.
     * @return {@link String} value of the preprocessor directive.
     * @since 0.1.0
     */
    private static String PreprocessorDirectiveOf(final String string) {

        // Initialize a transformed String
        final String flat = Flatten(string).toUpperCase();

        // Return the result
        return "#ifndef " + flat + '\n' + "#defin _LIB_" + flat + "H\n";

    }

    /**
     * <p>Attempts to generate a native library header file with the specified {@link String} value of the source file
     * name & {@link String} native library file name.</p>
     * @param sourceFilename The {@link String} value of the source file name.
     * @param includeFilename {@link String} native library file name.
     * @throws Phase.Error If the native library header file failed to write.
     * @since 0.1.0
     */
    private static void GenerateHeaderFileFrom(final ValidatePragmas validatePragmas,
                                               final String sourceFilename,
                                               final String includeFilename,
                                               final List<String> signatures) throws Phase.Error {

        // Initialize the header file name
        final String headerFileName = sourceFilename + ".h";

        // Emit the informative message
        new ToGenerateNativeLibraryHeaderFileWrite(validatePragmas, headerFileName).commit();

        // Attempt to
        try {

            // Generate the file
            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(headerFileName));

            // Write the contents
            bufferedWriter.write(LibPreprocessorDirectiveOf(headerFileName));
            bufferedWriter.write("#include<" + includeFilename + ">\n");

            // If the library is specified as NATIVE, generate the forward declarations
            // An empty signatures List will be specified for non-NATIVE specifications
            for(final String signature: signatures)
                bufferedWriter.write(signature + ";\n\n");

            bufferedWriter.write("#endif\n\n");
            bufferedWriter.close();

            // Emit the informative message
            new NativeLibraryHeaderFileWriteSuccess(validatePragmas, includeFilename).commit();

            // Otherwise
        } catch(final IOException exception) {

            // Transform the error.
            throw new NativeLibraryHeaderFileWriteFailureException(validatePragmas, headerFileName).commit();

        }

        // TODO: Potentially move the generated file to lib/C/include & update the success message to omit that spec

    }

    /**
     * <p>Attempts to generate a native library implementation file with the specified {@link String} value of the
     * source file name & {@link String} native library header file name.</p>
     * @param sourceFileName The {@link String} value of the source file name.
     * @throws Phase.Error If the native library source file failed to write.
     * @since 0.1.0
     */
    private static void GenerateImplementationFileFrom(final ValidatePragmas validatePragmas,
                                                       final String sourceFileName,
                                                       final List<String> signatures) throws Phase.Error {

        // Initialize the header file name
        final String implementationFileName = sourceFileName + ".c";
        final String headerFileName         = sourceFileName + ".h";

        // Emit the informative message
        new ToGenerateNativeLibraryImplementationFile(validatePragmas, implementationFileName).commit();

        // Attempt to
        try {

            // Generate the file
            final BufferedWriter file = new BufferedWriter(new FileWriter(implementationFileName));

            file.write(PreprocessorDirectiveOf(sourceFileName));
            file.write("#include \"" + headerFileName + "\"\n");

            // If the library is specified as NATIVE, generate the empty, concrete implementations
            // An empty signatures List will be specified for non-NATIVE specifications
            for(final String signature: signatures)
                file.write(signature + "{\n\n    // Implementation code goes here.\n\n}\n\n");

            file.write("#endif\n\n");
            file.close();

            new NativeLibraryImplementationFileWriteSuccess(validatePragmas,
                    implementationFileName, headerFileName).commit();

        // Otherwise
        } catch(final IOException exception) {

            throw new NativeLibraryImplementationFileWriteFailureException(validatePragmas, headerFileName);

        }

        // TODO: Potentially move the generated file to lib/C/include & update the success message to omit that spec

    }

    /**
     * <p>Generates the native library code from the specified parameters.</p>
     * @param validatePragmas The invoking {@link Phase}.
     * @param nativeSignatures Any procedure signatures generated by the {@link ValidatePragmas}.
     * @param packageName The package name corresponding to the current {@link Compilation}.
     * @param nativeLibraryFilename The filename corresponding to the native file name
     * @param processJHeaderFilename The filename corresponding to the processj header.
     * @param sourceFilename The filename corresponding to the generated implementation
     * @throws Phase.Error If the generation fails.
     * @since 0.1.0
     */
    private static void GenerateNativeLibraryCodeFrom(final ValidatePragmas validatePragmas,
                                                      final List<String> nativeSignatures,
                                                      final String packageName,
                                                      final String nativeLibraryFilename,
                                                      final String processJHeaderFilename,
                                                      final String sourceFilename) throws Phase.Error {

        // Print the Informative messages
        new ToGenerateNativeLibrary(validatePragmas, nativeLibraryFilename).commit();
        new ToGenerateNativeLibraryHeaderProcessJHeaderFile(validatePragmas, processJHeaderFilename).commit();

        // TODO: Maybe we want to retrieve strings for these and store them into the current ProcessJSource File object
        // TODO: for writing and/or processing to be handled elsewhere. This Phase should only concern itself with
        // TODO: Pragma validation & processing rather than file writing
        // Generate the files
        GenerateHeaderFileFrom(validatePragmas, sourceFilename, nativeLibraryFilename, nativeSignatures);
        GenerateImplementationFileFrom(validatePragmas, sourceFilename, nativeSignatures);

        // Print the last informative message
        new NativeLibraryFileWriteSuccess(validatePragmas, processJHeaderFilename,
                packageName + "/" + processJHeaderFilename + ".inc").commit();

    }

    /// --------------
    /// Private Fields

    /**
     * <p>{@link String} value of the package name corresponding to the current {@link Compilation}.</p>
     */
    private String          packageName         ;

    /**
     * <p>{@link String} value of the filename corresponding to the current {@link Compilation}.</p>
     */
    private String          fileName            ;

    /**
     * <p>Flag indicating if the {@link Compilation} is specified as a native library</p>
     */
    private boolean         isNativeLibrary     ;

    /**
     * <p>Flag indicating if the {@link Compilation} is specified as native</p>
     */
    private boolean         isNative            ;

    /**
     * <p>{@link List} containing generated values of native {@link ProcTypeDecl} signatures.</p>
     */
    private List<String>    nativeSignatures    ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ValidatePragmas} phase to its' default state with the specified
     * {@link Phase.Listener}.</p>
     * @param listener The {@link Phase.Listener} that receives any {@link Message},
     * {@link Phase.Warning}, or {@link Phase.Error} messages from the {@link ValidatePragmas}.
     */
    public ValidatePragmas(final Phase.Listener listener) {
        super(listener);
        this.nativeSignatures   = null  ;
        this.packageName        = ""    ;
        this.fileName           = ""    ;
        this.isNative           = false ;
        this.isNativeLibrary    = false ;
    }

    /// -----
    /// Phase

    /**
     * <p>Executes the {@link Pragma} validation {@link Phase}. This verifies the current {@link Compilation}'s
     * set of {@link Pragma}s are specified correctly from the current {@link Compilation}'s map containing the
     * key-value pairs of each {@link Pragma}.</p>
     * @throws Phase.Error when the {@link Phase} encounters a malformed {@link Pragma}
     * @since 0.1.0
     */
    @Override
    public void executePhase() throws Phase.Error {

        // Retrieve the Compilation & its Pragma Map
        final Compilation compilation = this.retrieveValidCompilation();

        // Assert a specified package name if the Compilation contains Pragmas
        if(compilation.definesPragmas() && !compilation.definesPackageName())
            throw new MissingPackageNameException(this).commit();

        final Map<String, String> pragmaMap = DecodePragmas(this, compilation.getPragmas());

        // If the pragma map contains a LIBRARY key
        if(pragmaMap.containsKey("LIBRARY")) {

            // Initialize the native lib specifications, package name, & native signatures list.
            this.isNative           = pragmaMap.containsKey("NATIVE")       ;
            this.isNativeLibrary    = pragmaMap.containsKey("NATIVELIB")    ;
            this.fileName           = pragmaMap.getOrDefault("FILE", "");
            this.packageName        = compilation.getPackageName()          ;
            this.nativeSignatures   = new ArrayList<>()                     ;

            // Validate the pragma map
            ValidatePragmaMap(this, pragmaMap);

            // Traverse the parse tree
            compilation.visit(this);

            // This smells. It might not be invalid
            if(this.isNative && this.nativeSignatures.isEmpty())
                throw new InvalidNativeSignaturesException(this).commit();

            // Initialize the source file name
            final String sourceFileName = this.packageName.replace('.', '/') + "_" + this.fileName;

            // TODO: Generate ProcessJFiles for LIBRARY & PROCESSJ specifications
            GenerateNativeLibraryCodeFrom(this, this.nativeSignatures, this.packageName,
                    pragmaMap.get("NATIVELIB"), this.fileName, sourceFileName);

            // Send an informative message
            new LibraryPragmaDetected(this).commit();

        }

    }

    /// -------
    /// Visitor

    /**
     * <p>Validates the {@link ConstantDecl}. Verifies that:
     *     1. If the {@link Compilation} is specified as a native library, the {@link ConstantDecl} is declared native
     *     and is not initialized.
     *     otherwise
     *     2. If the {@link Compilation} is not specified as a native library, the {@link ConstantDecl} is not declared
     *     native and is initialized.</p>
     * @param constantDeclaration The {@link ConstantDecl} to validate.
     * @return null
     * @throws Phase.Error If either of the described conditions are false.
     * @since 0.1.0
     */
    @Override
    public Void visitConstantDecl(final ConstantDecl constantDeclaration) throws Phase.Error {

        if(this.isNativeLibrary || this.isNative)
            ValidateNativeConstantDeclaration(this, constantDeclaration, this.isNative);

        else ValidateNonNativeConstantDeclaration(this, constantDeclaration);

        return null;

    }

    /**
     * <p>Validates the {@link ProcTypeDecl}. Verifies that:
     *     1. If the {@link Compilation} is specified as a native library, the {@link ProcTypeDecl} is declared native
     *     and does not define a body.
     *     otherwise
     *     2. If the {@link Compilation} is not specified as a native library, the {@link ProcTypeDecl} is not declared
     *     native and defines a body.</p>
     * @param procedureTypeDeclaration The {@link ProcTypeDecl} to validate.
     * @return null
     * @throws Phase.Error If either of the described conditions are false.
     * @since 0.1.0
     */
    @Override
    public Void visitProcTypeDecl(final ProcTypeDecl procedureTypeDeclaration) throws Phase.Error {

        if(this.isNativeLibrary || this.isNative) {

            // Validate the declaration
            ValidateNativeProcedureDeclaration(this, procedureTypeDeclaration);

            // Validate the return type
            ValidateNativeProcedureReturnType(this, procedureTypeDeclaration);

            // Validate the parameter types
            ValidateNativeProcedureParameterTypes(this, procedureTypeDeclaration);

            // Finally, aggregate the Procedure's native signature
            if(this.isNative) {

                this.nativeSignatures.add(NativeTypeStringFor(procedureTypeDeclaration.getReturnType())
                        + " " + this.packageName + "_" + procedureTypeDeclaration
                        + "_" + NativeTypeListStringFor(procedureTypeDeclaration));

                // TODO: Should this cover NATIVELIB?
                procedureTypeDeclaration.setNative();
                procedureTypeDeclaration.setPackageName(this.packageName);

            }

        } else {

            ValidateNonNativeProcedureDeclaration(this, procedureTypeDeclaration);

        }

        return null;

    }

    /**
     * <p>Validates the {@link ProtocolTypeDecl}. Verifies that If the {@link Compilation} is specified as a native
     * library, it does not declare any {@link ProtocolTypeDecl}.
     * @param protocolTypeDeclaration The {@link ProtocolTypeDecl} to validate.
     * @return null
     * @throws Phase.Error If the {@link Compilation} is specified as a native library and contains at least one
     * {@link ProtocolTypeDecl}.
     * @since 0.1.0
     */
    @Override
    public Void visitProtocolTypeDecl(final ProtocolTypeDecl protocolTypeDeclaration) throws Phase.Error {

        if(this.isNativeLibrary || this.isNative)
            throw new LibraryContainsProtocolDeclarationException(this, protocolTypeDeclaration).commit();

        return null;

    }

    /**
     * <p>Validates the {@link RecordTypeDecl}. Verifies that If the {@link Compilation} is specified as a native
     * library, it does not declare any {@link RecordTypeDecl}.
     * @param recordTypeDeclaration The {@link RecordTypeDecl} to validate.
     * @return null
     * @throws Phase.Error If the {@link Compilation} is specified as a native library and contains at least one
     * {@link RecordTypeDecl}.
     * @since 0.1.0
     */
    @Override
    public Void visitRecordTypeDecl(final RecordTypeDecl recordTypeDeclaration) throws Phase.Error {

        if(this.isNativeLibrary || this.isNative)
            throw new LibraryContainsRecordDeclarationException(this, recordTypeDeclaration).commit();

        return null;

    }

    /// ----
    /// Info

    /**
     * <p>{@link Phase.Info} class that encapsulates the pertinent information of a Pragma that is being registered.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class EnteringPragma extends Phase.Info {

        /// --------------
        /// Private Fields

        /**
         * <p>The invalid {@link Pragma} instance.</p>
         */
        private final Pragma pragma        ;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link EnteringPragma} with the culprit instance, current line,
         * & current line count.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param pragma The invalid {@link Pragma}.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected EnteringPragma(final ValidatePragmas culpritInstance, final Pragma pragma) {
            super(culpritInstance);

            this.pragma = pragma            ;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying where in the source file the error occurred.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return "Entering <" + this.pragma
                    + ((this.pragma.getValue() != null)
                    ? ',' + this.pragma.getValue().substring(1, this.pragma.getValue().length() - 1)
                    : "") + "> into pragmaTable.";

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information detected LIBRARY {@link Pragma}.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class LibraryPragmaDetected extends Phase.Info {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "LIBRARY pragma detected";

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.InvalidNativeLanguageException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected LibraryPragmaDetected(final ValidatePragmas culpritInstance) {
            super(culpritInstance);
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
            return Message;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a native library.</p>
     * @see Phase
     * @see Phase.Info
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ToGenerateNativeLibrary extends Phase.Info {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Native Library";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the to generate native library.</p>
         */
        private final String nativeLibraryName;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ToGenerateNativeLibrary}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param nativeLibraryName The {@link String} value of the to generate native library.
         * @see Phase
         * @see Phase.Info
         * @since 0.1.0
         */
        protected ToGenerateNativeLibrary(final ValidatePragmas culpritInstance,
                                          final String nativeLibraryName) {
            super(culpritInstance);

            this.nativeLibraryName = nativeLibraryName;

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
            return Message + ": " + this.nativeLibraryName;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a native library processj header file.</p>
     * @see Phase
     * @see Phase.Info
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ToGenerateNativeLibraryHeaderProcessJHeaderFile extends Phase.Info {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "ProcessJ header file";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the to generate native library processj header file.</p>
         */
        private final String nativeLibraryProcessJHeaderFileName;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ToGenerateNativeLibraryHeaderProcessJHeaderFile}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param nativeLibraryProcessJHeaderFileName The {@link String} value of the to generate native library processj header file.
         * @see Phase
         * @see Phase.Info
         * @since 0.1.0
         */
        protected ToGenerateNativeLibraryHeaderProcessJHeaderFile(final ValidatePragmas culpritInstance,
                                                                  final String nativeLibraryProcessJHeaderFileName) {
            super(culpritInstance);

            this.nativeLibraryProcessJHeaderFileName = nativeLibraryProcessJHeaderFileName;

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
            return Message + ": " + this.nativeLibraryProcessJHeaderFileName;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a native library header file.</p>
     * @see Phase
     * @see Phase.Info
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ToGenerateNativeLibraryHeaderFileWrite extends Phase.Info {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "ProcessJ C header file";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the to generate native library header file.</p>
         */
        private final String nativeLibraryHeaderFileName;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ToGenerateNativeLibraryHeaderFileWrite}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param nativeLibraryHeaderFileName The {@link String} value of the to generate native library header file.
         * @see Phase
         * @see Phase.Info
         * @since 0.1.0
         */
        protected ToGenerateNativeLibraryHeaderFileWrite(final ValidatePragmas culpritInstance,
                                                         final String nativeLibraryHeaderFileName) {
            super(culpritInstance);

            this.nativeLibraryHeaderFileName = nativeLibraryHeaderFileName;

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
            final ValidatePragmas culpritInstance = (ValidatePragmas) this.getPhase();

            // Return the resultant error message
            return Message + ": " + this.nativeLibraryHeaderFileName;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a native library implementation file.</p>
     * @see Phase
     * @see Phase.Info
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ToGenerateNativeLibraryImplementationFile extends Phase.Info {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "ProcessJ C implementation file";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the to generate native library implementation file.</p>
         */
        private final String nativeLibraryHeaderFileName;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ToGenerateNativeLibraryImplementationFile}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param nativeLibraryHeaderFileName The {@link String} value of the to generate native library implementation file.
         * @see Phase
         * @see Phase.Info
         * @since 0.1.0
         */
        protected ToGenerateNativeLibraryImplementationFile(final ValidatePragmas culpritInstance,
                                                            final String nativeLibraryHeaderFileName) {
            super(culpritInstance);

            this.nativeLibraryHeaderFileName = nativeLibraryHeaderFileName;

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
            return Message + ": " + this.nativeLibraryHeaderFileName;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a native library file output
     * success.</p>
     * @see Phase
     * @see Phase.Info
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class NativeLibraryHeaderFileWriteSuccess extends Phase.Info {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Generated file";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the generated native library header file.</p>
         */
        private final String nativeLibraryHeaderFileName;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link NativeLibraryHeaderFileWriteSuccess}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param nativeLibraryHeaderFileName The {@link String} value of the generated native library header file.
         * @see Phase
         * @see Phase.Info
         * @since 0.1.0
         */
        protected NativeLibraryHeaderFileWriteSuccess(final ValidatePragmas culpritInstance,
                                                      final String nativeLibraryHeaderFileName) {
            super(culpritInstance);

            this.nativeLibraryHeaderFileName = nativeLibraryHeaderFileName;

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
            return Message + ": " + this.nativeLibraryHeaderFileName + " - this file must be moved to lib/C/include/";

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a native library file output
     * success.
     * </p>
     * @see Phase
     * @see Phase.Info
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class NativeLibraryImplementationFileWriteSuccess extends Phase.Info {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Constant declaration must be declared native";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the generated native library implementation file.</p>
         */
        private final String nativeLibraryImplementationFileName;

        /**
         * <p>The {@link String} value of the corresponding native library header file.</p>
         */
        private final String nativeLibraryHeaderFileName;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link NativeLibraryImplementationFileWriteSuccess}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected NativeLibraryImplementationFileWriteSuccess(final ValidatePragmas culpritInstance,
                                                              final String nativeLibraryImplementationFileName,
                                                              final String nativeLibraryHeaderFileName) {
            super(culpritInstance);

            this.nativeLibraryImplementationFileName    = nativeLibraryImplementationFileName   ;
            this.nativeLibraryHeaderFileName            = nativeLibraryHeaderFileName           ;

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
            final ValidatePragmas culpritInstance = (ValidatePragmas) this.getPhase();

            // Return the resultant error message
            return Message + ": " + this.nativeLibraryImplementationFileName + " with " + this.nativeLibraryHeaderFileName
                    + " - this file must be moved to lib/C/include/";

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a native library file output
     * success.</p>
     * @see Phase
     * @see Phase.Info
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class NativeLibraryFileWriteSuccess extends Phase.Info {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Provided file";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the generated native library processj header file.</p>
         */
        private final String nativeLibraryProcessJHeaderFileName;

        /**
         * <p>The {@link String} value of the generated native library processj header file path.</p>
         */
        private final String nativeLibraryProcessJHeaderFilePath;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link NativeLibraryFileWriteSuccess}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param nativeLibraryProcessJHeaderFileName The {@link String} value of the generated native library processj header file.
         * @see Phase
         * @see Phase.Info
         * @since 0.1.0
         */
        protected NativeLibraryFileWriteSuccess(final ValidatePragmas culpritInstance,
                                                final String nativeLibraryProcessJHeaderFileName,
                                                final String nativeLibraryProcessJHeaderFilePath) {
            super(culpritInstance);

            this.nativeLibraryProcessJHeaderFileName = nativeLibraryProcessJHeaderFileName;
            this.nativeLibraryProcessJHeaderFilePath = nativeLibraryProcessJHeaderFilePath;

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
            return Message + ": " + this.nativeLibraryProcessJHeaderFileName + " must be moved to lib/C/include/"
                    + this.nativeLibraryProcessJHeaderFilePath;

        }

    }

    /// ------
    /// Errors

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of missing package name from a
     * {@link Compilation}.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class MissingPackageNameException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Library files must specify a package name";

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.MissingPackageNameException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected MissingPackageNameException(final ValidatePragmas culpritInstance) {
            super(culpritInstance);
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
            return Message;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of an Illegal Pragma.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class IllegalPragmaException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Illegal Pragma";

        /// --------------
        /// Private Fields

        /**
         * <p>The invalid {@link Pragma} instance.</p>
         */
        private final Pragma    pragma        ;

        /**
         * <p>The line within the source file where the error occurred.</p>
         */
        private final int       currentLine   ;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.IllegalPragmaException} with the culprit instance, current line,
         * & current line count.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param pragma The invalid {@link Pragma}.
         * @param currentLine The integer value of where the error occurred within the source file.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected IllegalPragmaException(final ValidatePragmas culpritInstance,
                                         final Pragma pragma, final int currentLine) {
            super(culpritInstance);

            this.pragma             = pragma            ;
            this.currentLine        = currentLine       ;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying where in the source file the error occurred.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return Message + ":" + this.currentLine + " '" + this.pragma + "'";

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a Pragma with no arguments
     * when they're necessary.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class PragmaRequiresArgumentException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Illegal Pragma";

        /// --------------
        /// Private Fields

        /**
         * <p>The invalid {@link Pragma} instance.</p>
         */
        private final Pragma    pragma        ;

        /**
         * <p>The line within the source file where the error occurred.</p>
         */
        private final int       currentLine   ;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.IllegalPragmaException} with the culprit instance, current line,
         * & current line count.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param pragma The invalid {@link Pragma}.
         * @param currentLine The integer value of where the error occurred within the source file.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected PragmaRequiresArgumentException(final ValidatePragmas culpritInstance,
                                                  final Pragma pragma, final int currentLine) {
            super(culpritInstance);

            this.pragma             = pragma            ;
            this.currentLine        = currentLine       ;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying where in the source file the error occurred.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return Message + ":" + this.currentLine + " '" + this.pragma + "' requires 1 parameter, none was given.";

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a Pragma with arguments
     * when they're not necessary.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class PragmaRequiresNoArgumentException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Illegal Pragma";

        /// --------------
        /// Private Fields

        /**
         * <p>The invalid {@link Pragma} instance.</p>
         */
        private final Pragma    pragma        ;

        /**
         * <p>The line within the source file where the error occurred.</p>
         */
        private final int       currentLine   ;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.IllegalPragmaException} with the culprit instance, current line,
         * & current line count.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param pragma The invalid {@link Pragma}.
         * @param currentLine The integer value of where the error occurred within the source file.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected PragmaRequiresNoArgumentException(final ValidatePragmas culpritInstance,
                                                    final Pragma pragma, final int currentLine) {
            super(culpritInstance);

            this.pragma             = pragma            ;
            this.currentLine        = currentLine       ;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying where in the source file the error occurred.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Retrieve the culprit & initialize the StringBuilder
            final ValidatePragmas culpritInstance = (ValidatePragmas) this.getPhase();

            // Return the resultant error message
            return Message + ":" + this.currentLine + " '" + this.pragma + "' does not require any parameters.";

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a Pragma that has already
     * been specified.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class PragmaAlreadySpecifiedException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Illegal Pragma";

        /// --------------
        /// Private Fields

        /**
         * <p>The invalid {@link Pragma} instance.</p>
         */
        private final Pragma    pragma        ;

        /**
         * <p>The line within the source file where the error occurred.</p>
         */
        private final int       currentLine   ;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.IllegalPragmaException} with the culprit instance, current line,
         * & current line count.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param pragma The invalid {@link Pragma}.
         * @param currentLine The integer value of where the error occurred within the source file.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected PragmaAlreadySpecifiedException(final ValidatePragmas culpritInstance,
                                                  final Pragma pragma, final int currentLine) {
            super(culpritInstance);

            this.pragma             = pragma            ;
            this.currentLine        = currentLine       ;

        }

        /// -------------------
        /// java.lang.Exception

        /**
         * <p>Returns a newly constructed message specifying where in the source file the error occurred.</p>
         * @return {@link String} value of the error message.
         * @since 0.1.0
         */
        @Override
        public String getMessage() {

            // Return the resultant error message
            return Message + ":" + this.currentLine + " '" + this.pragma + "' already specified.";

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of missing language {@link Pragma}.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class MissingLanguagePragmaException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Missing LANGUAGE pragma";

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.MissingLanguagePragmaException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected MissingLanguagePragmaException(final ValidatePragmas culpritInstance) {
            super(culpritInstance);
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
            return Message;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of missing file {@link Pragma}.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class MissingFilePragmaException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Missing FILE pragma";

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.MissingFilePragmaException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected MissingFilePragmaException(final ValidatePragmas culpritInstance) {
            super(culpritInstance);
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
            return Message;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of NATIVE & NATIVELIB
     * {@link Pragma}s used in conjunction.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class NativeAndNativeLibException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "pragmas NATIVE and NATIVELIB cannot be used together";

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.NativeAndNativeLibException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected NativeAndNativeLibException(final ValidatePragmas culpritInstance) {
            super(culpritInstance);
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
            return Message;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information PROCESSJ specified for {@link Pragma}s
     * NATIVE or NATIVELIB.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class NativeWithProcessJException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "pragmas NATIVE and NATIVELIB cannot be used together";

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.NativeWithProcessJException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected NativeWithProcessJException(final ValidatePragmas culpritInstance) {
            super(culpritInstance);
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
            return Message;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information for a invalid native language.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidNativeLanguageException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Invalid native language";

        /// --------------
        /// Private Fields

        /**
         * <p>The invalid native language.</p>
         */
        private final String language;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.InvalidNativeLanguageException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidNativeLanguageException(final ValidatePragmas culpritInstance,
                                                 final String language) {
            super(culpritInstance);

            this.language = language;

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
            final ValidatePragmas culpritInstance = (ValidatePragmas) this.getPhase();

            // Return the resultant error message
            return Message + " '" + this.language + "' ";

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a native library containing
     * {@link ProtocolTypeDecl}s.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class LibraryContainsProtocolDeclarationException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Native libraries cannot declare Protocols";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ProtocolTypeDecl} that was declared in the native library</p>
         */
        private final ProtocolTypeDecl protocolTypeDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.LibraryContainsProtocolDeclarationException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param protocolTypeDeclaration The {@link ProtocolTypeDecl} declared in the library.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected LibraryContainsProtocolDeclarationException(final ValidatePragmas culpritInstance,
                                                              final ProtocolTypeDecl protocolTypeDeclaration) {
            super(culpritInstance);

            this.protocolTypeDeclaration = protocolTypeDeclaration;

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
            return Message + ": " + this.protocolTypeDeclaration;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a native library containing
     * {@link RecordTypeDecl}s.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class LibraryContainsRecordDeclarationException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Native libraries cannot declare Records";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link RecordTypeDecl} that was declared in the native library</p>
         */
        private final RecordTypeDecl recordTypeDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.LibraryContainsRecordDeclarationException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param recordTypeDeclaration The {@link RecordTypeDecl} declared in the library.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected LibraryContainsRecordDeclarationException(final ValidatePragmas culpritInstance,
                                                            final RecordTypeDecl recordTypeDeclaration) {
            super(culpritInstance);

            this.recordTypeDeclaration = recordTypeDeclaration;

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
            return Message + ": " + this.recordTypeDeclaration;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ConstantDecl} that was
     * declared native in conjunction with a NATIVE {@link Pragma}.
     * </p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class IllegalConstantDeclarationNativeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Illegal usage of 'native' keyword with NATIVE pragma";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ConstantDecl} that was declared native</p>
         */
        private final ConstantDecl constantDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link IllegalConstantDeclarationNativeException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected IllegalConstantDeclarationNativeException(final ValidatePragmas culpritInstance,
                                                            final ConstantDecl constantDeclaration) {
            super(culpritInstance);

            this.constantDeclaration = constantDeclaration;

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
            final ValidatePragmas culpritInstance = (ValidatePragmas) this.getPhase();

            // Return the resultant error message
            return Message + ": " + this.constantDeclaration;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ConstantDecl} that was
     * declared native.
     * </p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ConstantDeclarationNativeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Constant declaration cannot be declared native";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ConstantDecl} that was declared native</p>
         */
        private final ConstantDecl constantDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ConstantDeclarationNativeException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ConstantDeclarationNativeException(final ValidatePragmas culpritInstance,
                                                     final ConstantDecl constantDeclaration) {
            super(culpritInstance);

            this.constantDeclaration = constantDeclaration;

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
            return Message + ": " + this.constantDeclaration;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ConstantDecl} that was
     * not declared native.
     * </p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ConstantDeclarationNonNativeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Constant declaration must be declared native";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ConstantDecl} that was not declared native</p>
         */
        private final ConstantDecl constantDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ConstantDeclarationNonNativeException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ConstantDeclarationNonNativeException(final ValidatePragmas culpritInstance,
                                                        final ConstantDecl constantDeclaration) {
            super(culpritInstance);

            this.constantDeclaration = constantDeclaration;

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
            return Message + ": " + this.constantDeclaration;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ConstantDecl} that was
     * initialized.
     * </p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ConstantDeclarationInitializedException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Constant declaration cannot have an initialization expression";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ConstantDecl} that was declared native</p>
         */
        private final ConstantDecl constantDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ConstantDeclarationInitializedException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ConstantDeclarationInitializedException(final ValidatePragmas culpritInstance,
                                                          final ConstantDecl constantDeclaration) {
            super(culpritInstance);

            this.constantDeclaration = constantDeclaration;

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
            final ValidatePragmas culpritInstance = (ValidatePragmas) this.getPhase();

            // Return the resultant error message
            return Message + ": " + this.constantDeclaration;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ConstantDecl} that was
     * not initialized.
     * </p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ConstantDeclarationNotInitializedException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Constant declaration must be initialized";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ConstantDecl} that was not declared native</p>
         */
        private final ConstantDecl constantDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ConstantDeclarationNotInitializedException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ConstantDeclarationNotInitializedException(final ValidatePragmas culpritInstance,
                                                             final ConstantDecl constantDeclaration) {
            super(culpritInstance);

            this.constantDeclaration = constantDeclaration;

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
            final ValidatePragmas culpritInstance = (ValidatePragmas) this.getPhase();

            // Return the resultant error message
            return Message + ": " + this.constantDeclaration;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ProcTypeDecl} that was
     * declared native.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ProcedureDeclarationNativeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Procedure declaration cannot be declared native";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ProcTypeDecl} that was declared native</p>
         */
        private final ProcTypeDecl procedureDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ProcedureDeclarationNativeException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ProcedureDeclarationNativeException(final ValidatePragmas culpritInstance,
                                                      final ProcTypeDecl procedureDeclaration) {
            super(culpritInstance);

            this.procedureDeclaration = procedureDeclaration;

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
            return Message + ": " + this.procedureDeclaration;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ProcTypeDecl} that was
     * not declared native.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ProcedureDeclarationNonNativeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Procedure declaration must be declared native";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ProcTypeDecl} that was not declared native</p>
         */
        private final ProcTypeDecl procedureDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ProcedureDeclarationNonNativeException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ProcedureDeclarationNonNativeException(final ValidatePragmas culpritInstance,
                                                         final ProcTypeDecl procedureDeclaration) {
            super(culpritInstance);

            this.procedureDeclaration = procedureDeclaration;

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
            return Message + ": " + this.procedureDeclaration;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ProcTypeDecl} that was
     * initialized.
     * </p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ProcedureDeclarationDefinesBodyException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Procedure declaration cannot have a body in a non-ProcessJ library file";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ProcTypeDecl} that defines a body</p>
         */
        private final ProcTypeDecl procedureDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ProcedureDeclarationDefinesBodyException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ProcedureDeclarationDefinesBodyException(final ValidatePragmas culpritInstance,
                                                           final ProcTypeDecl procedureDeclaration) {
            super(culpritInstance);

            this.procedureDeclaration = procedureDeclaration;

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
            return Message + ": " + this.procedureDeclaration;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ProcTypeDecl} that did not
     * define a body.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class ProcedureDeclarationDoesNotDefineBodyException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Procedure declaration must define a body";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ProcTypeDecl} that did not define a body</p>
         */
        private final ProcTypeDecl procedureDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ProcedureDeclarationDoesNotDefineBodyException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected ProcedureDeclarationDoesNotDefineBodyException(final ValidatePragmas culpritInstance,
                                                                 final ProcTypeDecl procedureDeclaration) {
            super(culpritInstance);

            this.procedureDeclaration = procedureDeclaration;

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
            return Message + ": " + this.procedureDeclaration;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ProcTypeDecl} that does not
     * specify a primitive return type.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class IllegalProcedureReturnTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message
                = "Illegal return type specification; Native C library procedures must return a primitive type";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ProcTypeDecl} that does not specify a primitive return type</p>
         */
        private final ProcTypeDecl procedureDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link IllegalProcedureReturnTypeException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected IllegalProcedureReturnTypeException(final ValidatePragmas culpritInstance,
                                                      final ProcTypeDecl procedureDeclaration) {
            super(culpritInstance);

            this.procedureDeclaration = procedureDeclaration;

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
            return Message + ": " + this.procedureDeclaration + " specifies '" + this.procedureDeclaration.getReturnType() + "'";

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ProcTypeDecl} that specifies a
     * barrier or timer primitive return type.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class IllegalProcedureBarrierOrTimerReturnTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message
                = "Illegal return type specification; Native C library procedures must not return a barrier or timer";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ProcTypeDecl} that specifies a barrier or timer primitive return type</p>
         */
        private final ProcTypeDecl procedureDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link IllegalProcedureBarrierOrTimerReturnTypeException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected IllegalProcedureBarrierOrTimerReturnTypeException(final ValidatePragmas culpritInstance,
                                                                    final ProcTypeDecl procedureDeclaration) {
            super(culpritInstance);

            this.procedureDeclaration = procedureDeclaration;

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
            return Message + ": " + this.procedureDeclaration + " specifies '" + this.procedureDeclaration.getReturnType() + "'";

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ProcTypeDecl} that specifies
     * at least one non primitive parameter type.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class IllegalProcedureParameterTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message
                = "Illegal parameter type specification; Native C library procedures must specify primitive parameters";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ProcTypeDecl} that specifies a non-primitive parameter type</p>
         */
        private final ProcTypeDecl procedureDeclaration;

        /**
         * <p>The non primitive {@link Type}</p>
         */
        private final Type parameterType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link IllegalProcedureParameterTypeException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected IllegalProcedureParameterTypeException(final ValidatePragmas culpritInstance,
                                                         final ProcTypeDecl procedureDeclaration,
                                                         final Type parameterType) {
            super(culpritInstance);

            this.procedureDeclaration = procedureDeclaration;
            this.parameterType        = parameterType       ;

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
            return Message + ": " + this.procedureDeclaration + " specifies '" + this.parameterType+ "'";

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ProcTypeDecl} that specifies a
     * barrier or timer primitive parameter type.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class IllegalProcedureBarrierOrTimerParameterTypeException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message
                = "Illegal parameter type specification; Native C library procedures must not specify a barrier or timer parameter";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link ProcTypeDecl} that specifies a barrier or timer primitive return type</p>
         */
        private final ProcTypeDecl procedureDeclaration;

        /**
         * <p>The non primitive {@link Type}</p>
         */
        private final Type parameterType;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link IllegalProcedureBarrierOrTimerParameterTypeException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected IllegalProcedureBarrierOrTimerParameterTypeException(final ValidatePragmas culpritInstance,
                                                                       final ProcTypeDecl procedureDeclaration,
                                                                       final Type parameterType) {
            super(culpritInstance);

            this.procedureDeclaration   = procedureDeclaration  ;
            this.parameterType          = parameterType         ;

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
            return Message + ": " + this.procedureDeclaration + " specifies '" + this.parameterType + "'";

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of missing {@link List} of native signatures.
     * </p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class InvalidNativeSignaturesException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Missing Native signatures";

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link ValidatePragmas.InvalidNativeSignaturesException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected InvalidNativeSignaturesException(final ValidatePragmas culpritInstance) {
            super(culpritInstance);
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
            return Message;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a native library header file
     * write failure.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class NativeLibraryHeaderFileWriteFailureException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Failed to write Native Library header file";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the native library header file that failed to write.</p>
         */
        private final String nativeLibraryHeaderFileName;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link NativeLibraryHeaderFileWriteFailureException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @param nativeLibraryHeaderFileName The {@link String} value of the native library header file that failed
         *                                    to generate.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected NativeLibraryHeaderFileWriteFailureException(final ValidatePragmas culpritInstance,
                                                               final String nativeLibraryHeaderFileName) {
            super(culpritInstance);

            this.nativeLibraryHeaderFileName = nativeLibraryHeaderFileName;

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
            return Message + ": " + this.nativeLibraryHeaderFileName;

        }

    }

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a native library implementation file
     * write failure.</p>
     * @see Phase
     * @see Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class NativeLibraryImplementationFileWriteFailureException extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Failed to write Native Library header file";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the native library header file that failed to write.</p>
         */
        private final String nativeLibraryImplementationFileName;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link NativeLibraryImplementationFileWriteFailureException}.</p>
         * @param culpritInstance The {@link ValidatePragmas} instance that raised the error.
         * @see Phase
         * @see Phase.Error
         * @since 0.1.0
         */
        protected NativeLibraryImplementationFileWriteFailureException(final ValidatePragmas culpritInstance,
                                                                       final String nativeLibraryImplementationFileName) {
            super(culpritInstance);

            this.nativeLibraryImplementationFileName = nativeLibraryImplementationFileName;

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
            return this.nativeLibraryImplementationFileName;

        }

    }

}
