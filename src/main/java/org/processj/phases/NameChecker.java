package org.processj.phases;

import java.util.List;

import org.processj.Phase;
import org.processj.ast.*;
import org.processj.utilities.*;

// TODO: Error 405 (Not found
// TODO: Errors 416 & 417 (Record Literal)
// TODO: Errors 409, 412, & 413 (Protocol Literal)
// TODO: Errors 418 & 419 (Record Type Declaration
// TODO: Errors 414 & 415 (Protocol Type Declaration)
// TODO: Errors 408 & 410 (Procedure Type Declaration)
public class NameChecker extends Phase {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link Phase.Listener}.</p>
     * @param listener The {@link Phase.Listener} that receives any {@link Phase.Message}, {@link Phase.Warning},
     *                 or {@link Phase.Error} messages.
     * @since 0.1.0
     */
    public NameChecker(final Phase.Listener listener) {
        super(listener);
    }

    /// -------------------------
    /// org.processj.ast.Visitor

    @Override
    public final Void visitConstantDecl(final ConstantDecl constantDeclaration) throws Phase.Error {

        Log.log(constantDeclaration.line + ": Visiting ConstantDecl '" + constantDeclaration + "' (Setting myDecl)");

        // Resolve any initialization Expressions
        if(constantDeclaration.isInitialized()) constantDeclaration.getInitializationExpression().visit(this);

        // Retrieve the Type
        final Type type = constantDeclaration.getType();

        // Visit the Type if it's not a NamedType
        if(!(type instanceof NamedType)) type.visit(this);

        // Update the Type
        constantDeclaration.setType((Type) resolveName(type.getName()));

        return null;

    }

    @Override
    public final Void visitProcTypeDecl(final ProcTypeDecl procedureTypeDeclaration) throws Phase.Error {

        Log.log(procedureTypeDeclaration.line + ": Visiting ProcTypeDecl (" + procedureTypeDeclaration + ").");

        // Initialize a handle to the scope & return Type
        final Type returnType = procedureTypeDeclaration.getReturnType()  ;

        // Visit the Type if it's not a NamedType
        if(!(returnType instanceof NamedType)) returnType.visit(this);

        // Update the return type
        procedureTypeDeclaration.setReturnType((Type) resolveName(returnType.getName()));

        // Bind the implement type list
        procedureTypeDeclaration.setTypeForEachImplement((name) -> (Type) resolveName(name));

        // Resolve the parameter list
        procedureTypeDeclaration.getParameters().visit(this);

        // Recur on the body
        procedureTypeDeclaration.getBody().visit(this);

        return null;

    }

    @Override
    public final Void visitProtocolTypeDecl(final ProtocolTypeDecl protocolTypeDeclaration) throws Phase.Error {

        Log.log(protocolTypeDeclaration.line + ": Visiting ProtocolTypeDecl (" + protocolTypeDeclaration + ").");

        // Bind the extend Type list
        protocolTypeDeclaration.setTypeForEachExtend((name) -> (Type) resolveName(name));

        // Bind the Record Member Types
        protocolTypeDeclaration.setTypeForEachRecordMember((name) -> (Type) resolveName(name));

        return null;

    }

    @Override
    public final Void visitRecordTypeDecl(final RecordTypeDecl recordTypeDeclaration) throws Phase.Error {

        Log.log(recordTypeDeclaration.line + ": Visiting RecordTypeDecl (" + recordTypeDeclaration + ").");

        // Bind the extend Type list
        recordTypeDeclaration.setTypeForEachExtend(name -> (Type) resolveName(name));

        // Bind the Record Member Types
        recordTypeDeclaration.setTypeForEachRecordMember(name -> (Type) resolveName(name));

        return null;

    }

    @Override
    public final Void visitLocalDecl(final LocalDecl localDeclaration) throws Phase.Error {

        Log.log(localDeclaration.line + ": Visiting LocalDecl (" + localDeclaration.getType()
                + " " + localDeclaration + ")");

        // Insert the local declaration into the current scope
        if(!this.getScope().put(localDeclaration))
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addAST(localDeclaration)
                    .addError(VisitorMessageNumber.NAME_CHECKER_400)
                    .addArguments(localDeclaration.toString())
                    .build());

        // Resolve any initialization Expressions
        if(localDeclaration.isInitialized()) localDeclaration.getInitializationExpression().visit(this);

        // Retrieve the Type
        final Type type = localDeclaration.getType();

        // Visit the Type if it's not a NamedType
        if(!(type instanceof NamedType)) type.visit(this);

        // Update the Type
        localDeclaration.setType((Type) resolveName(type.getName()));

        return null;

    }

    @Override
    public final Void visitParamDecl(final ParamDecl parameterDeclaration) throws Phase.Error {

        // Attempt to insert the Parameter Declaration's Name
        if(!this.getScope().put(parameterDeclaration))
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addAST(parameterDeclaration)
                    .addError(VisitorMessageNumber.NAME_CHECKER_402)
                    .addArguments(parameterDeclaration.toString())
                    .build());

        // Retrieve the Type
        final Type type = parameterDeclaration.getType();

        // Visit the Type if it's not a NamedType
        if(!(type instanceof NamedType)) type.visit(this);

        // Update the Type
        parameterDeclaration.setType((Type) resolveName(type.getName()));

        return null;

    }

    @Override
    public final Void visitArrayType(final ArrayType arrayType) throws Phase.Error {

        final Type type = arrayType.getComponentType();

        // Visit the Type if it's not a NamedType
        if(!(type instanceof NamedType)) type.visit(this);

        arrayType.setComponentType((Type) resolveName(type.getName()));

        return null;

    }

    @Override
    public final Void visitChannelType(final ChannelType channelType) throws Phase.Error {

        final Type type = channelType.getComponentType();

        // Visit the Type if it's not a NamedType
        if(!(type instanceof NamedType)) type.visit(this);

        channelType.setComponentType((Type) resolveName(type.getName()));

        return null;

    }

    @Override
    public final Void visitChannelEndType(final ChannelEndType channelEndType) throws Phase.Error {

        final Type type = channelEndType.getComponentType();

        // Visit the Type if it's not a NamedType
        if(!(type instanceof NamedType)) type.visit(this);

        channelEndType.setComponentType((Type) resolveName(type.getName()));

        return null;

    }

    @Override
    public final Void visitCastExpr(final CastExpr castExpression) throws Phase.Error {

        final Type type = castExpression.getType();

        // Visit the Type if it's not a NamedType
        if(!(type instanceof NamedType)) type.visit(this);

        castExpression.setType((Type) resolveName(type.getName()));

        castExpression.getExpression().visit(this);

        return null;

    }

    @Override
    public final Void visitInvocation(final Invocation invocation) throws Phase.Error {

        Log.log(invocation.line + ": Visiting Invocation (" + invocation + ")");

        // If the Invocation has a Target visit since it could be a NameExpression
        if(invocation.definesTarget()) invocation.getTarget().visit(this);

        // Either way, bind the parameter Types
        invocation.setTypeForEachParameter(expression -> ((expression instanceof NameExpr)
                ? (Type) resolveName(((NameExpr) expression).getName()) : expression.getType()));

        return null;

    }

    @Override
    public final Void visitNameExpr(final NameExpr nameExpression) throws Phase.Error {

        // Simply update the Type
        nameExpression.setType((Type) resolveName(nameExpression.getName()));

        return null;

    }

    @Override
    public final Void visitNewArray(final NewArray newArray) throws Phase.Error {

        // Visit if the new array expression has expressions in the square brackets
        if(newArray.definesBracketExpressions())
            newArray.getBracketExpressions().visit(this);

        // Visit if the new array expression is initialized with an array literal expression
        if(newArray.isLiteralInitialized())
            newArray.getInitializationExpression().visit(this);

        // Initialize a handle to the component type
        final Type type = newArray.getComponentType();

        // Visit if it's not a NamedType
        if(!(type instanceof NamedType)) type.visit(this);

        // Update the Type
        newArray.setComponentType((Type) resolveName(type.getName()));

        return null;

    }

    @Override
    public final Void visitProtocolLiteral(final ProtocolLiteral protocolLiteral) throws Phase.Error {

        Log.log(protocolLiteral.line + ": Visiting ProtocolLiteral (" + protocolLiteral + ").");

        // Resolve the Type first
        protocolLiteral.setType((Type) resolveName(protocolLiteral.getName()));

        // Initialize a handle to the ProtocolLiteral's Type
        final ProtocolTypeDecl protocolTypeDeclaration = (ProtocolTypeDecl) protocolLiteral.getType();

        // Retrieve the corresponding ProtocolCase
        final ProtocolCase protocolCase = protocolTypeDeclaration.getCaseFrom(protocolLiteral.getTagLiteral());

        // Assert the case was found
        if(protocolCase == null)
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addAST(protocolLiteral)
                    .addError(VisitorMessageNumber.NAME_CHECKER_402)
                    .addArguments("Protocol Case " + protocolLiteral.getTagLiteral() + " not defined.")
                    .build());

        // Update the case
        protocolLiteral.setProtocolCase(protocolCase);

        // Finally, visit the Protocol Literal's Expressions
        protocolLiteral.getExpressions().visit(this);

        return null;

    }

    @Override
    public final Void visitRecordLiteral(final RecordLiteral recordLiteral) throws Phase.Error {

        Log.log(recordLiteral.line + ": Visiting RecordLiteral (" + recordLiteral + ").");

        // Resolve the Type first
        recordLiteral.setType((Type) resolveName(recordLiteral.getName()));

        // Visit the Record Member literals
        recordLiteral.getRecordMemberLiterals().visit(this);

        return null;

    }

    /// ---------------
    /// Private Methods

    private Object resolveName(final Name symbol) throws Error {

        // Initialize a handle to the name & package name
        final String name           = symbol.getName()           ;
        final String packageName    = symbol.getPackageName()    ;

        // Attempt to retrieve a result
        List<Object> names = this.getScope().get(packageName, name);

        // If the List is empty, the name is undefined in the scope
        if(names.isEmpty())
            if((packageName == null) || (packageName.isEmpty()) || packageName.isBlank())
                throw new SymbolNotDefinedInPackageException(this, name, packageName).commit();

            else throw new SymbolNotDefinedInScopeException(this, name).commit();

            // Otherwise, we may have gotten an ambiguous result (name clash)
        else if(names.size() > 1)
            throw new AmbiguousSymbolException(this, name, names).commit();

        // Initialize a handle to the result
        Object result = names.get(0);

        // Update the result if it's a NamedType
        if(result instanceof NamedType)
            result = resolveName(((NamedType) result).getName());

        return result;

    }

    /// ------
    /// Errors

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a symbol that was not defined
     * or visible in the current scope.</p>
     * @see Phase
     * @see org.processj.Phase.Error
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
         * @param culpritInstance The {@link NameChecker} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected SymbolNotDefinedInScopeException(final NameChecker culpritInstance, final String symbolName) {
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
     * @see org.processj.Phase.Error
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
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected SymbolNotDefinedInPackageException(final NameChecker culpritInstance,
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
     * @see org.processj.Phase.Error
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
         * @param culpritInstance The {@link NameChecker} instance that raised the error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected AmbiguousSymbolException(final NameChecker culpritInstance,
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
