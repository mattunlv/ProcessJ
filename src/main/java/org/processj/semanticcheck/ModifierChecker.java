package org.processj.semanticcheck;

import org.processj.Phase;
import org.processj.ast.ProcTypeDecl;
import org.processj.ast.SymbolMap;
import org.processj.utilities.Log;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.PJMessage;
import org.processj.utilities.VisitorMessageNumber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifierChecker extends Phase {
    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link Listener}.</p>
     *
     * @param listener The {@link Listener} to bind to the {@link Phase}.
     * @since 0.1.0
     */
    public ModifierChecker(Listener listener) {
        super(listener);
    }

    @Override
    protected void executePhase() throws Error {

    }

    @Override
    public Void visitProcTypeDecl(final ProcTypeDecl procedureTypeDeclaration) {

        // Procedures can be overloaded, so an entry in the symbol table for a procedure
        // is another symbol table which is indexed by signature.
        // TODO: Should probably do this in Modifier Checking
        if(procedureTypeDeclaration.isMobile() && !procedureTypeDeclaration.getReturnType().isVoidType())
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addAST(procedureTypeDeclaration)
                    .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_205)
                    .addArguments(procedureTypeDeclaration.toString())
                    .build());

        return null;

    }

}
