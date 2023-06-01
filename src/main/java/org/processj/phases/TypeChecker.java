package org.processj.phases;

import org.processj.Phase;
import org.processj.ast.Modifier;
import org.processj.ast.ProcTypeDecl;
import org.processj.ast.Type;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.PJMessage;
import org.processj.utilities.VisitorMessageNumber;

public class TypeChecker extends Phase {


    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link Listener}.
     *
     * @param listener The {@link Listener} to bind to the {@link Phase}.
     */
    public TypeChecker(Listener listener) {
        super(listener);
    }

    @Override
    protected void executePhase() throws Error {

    }

    @Override
    public Void visitProcTypeDecl(final ProcTypeDecl procedureTypeDeclaration) throws Phase.Error {

        // TODO: Originally Error Code 205
        if(procedureTypeDeclaration.isDeclaredMobile() && !procedureTypeDeclaration.returnType().isVoidType())
            throw new MobileVoidReturnTypeNotSpecified(this, procedureTypeDeclaration);


        return null;

    }

    /// ------
    /// Errors

    /**
     * <p>{@link Phase.Error} class that encapsulates the pertinent information of a {@link ProcTypeDecl} that
     * does not specify a 'void' return {@link Type}.</p>
     * <p>Error Code: 200</p>
     * @see Phase
     * @see org.processj.Phase.Error
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class MobileVoidReturnTypeNotSpecified extends Phase.Error {

        /// ------------------------
        /// Private Static Constants

        /**
         * <p>Standard error message {@link String} for reporting.</p>
         */
        private final static String Message = "Type with name '%s' already declared in this scope";

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value of the mobile {@link ProcTypeDecl} that does not specify a 'void' return
         * {@link Type}.</p>
         */
        private final ProcTypeDecl procedureTypeDeclaration;

        /// ------------
        /// Constructors

        /**
         * <p>Constructs the {@link TypeChecker.MobileVoidReturnTypeNotSpecified}.</p>
         * @param culpritInstance The {@link TypeChecker.MobileVoidReturnTypeNotSpecified} instance that raised the
         *                        error.
         * @see Phase
         * @see org.processj.Phase.Error
         * @since 0.1.0
         */
        protected MobileVoidReturnTypeNotSpecified(final TypeChecker culpritInstance,
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
            final TypeChecker culpritInstance = (TypeChecker) this.getPhase();

            // Return the resultant error message
            return culpritInstance.getProcessJSourceFile().getName() + ": "
                    + String.format(Message, this.procedureTypeDeclaration.toString());

        }

    }



}
