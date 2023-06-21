package org.processj.compiler.ast.statement.yielding;

import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.expression.binary.AssignmentExpression;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.phase.Visitor;
import org.processj.compiler.utilities.Error;

// TODO: Maybe Extend Statement
public class AltCase extends Statement {

    /// --------------
    /// Private Fields

    private final Expression    preconditionExpression  ;
    private final GuardStatement guardStatement;
    private final BlockStatement body                    ;
    private int                 caseNumber              ;
    public boolean              isAltStat               ;
    private SymbolMap           scope                   ;

    /// ------------
    /// Constructors

    public AltCase(final Expression preconditionExpression, final GuardStatement guardStatement, final Statement statement) {
        super(new AST[] { preconditionExpression, guardStatement, new BlockStatement(statement) });

        this.preconditionExpression = preconditionExpression    ;
        this.guardStatement = guardStatement;
        this.body                   = (BlockStatement) this.children[2]  ;
        this.isAltStat              = false                     ;
        this.caseNumber             = -1                        ;
        this.scope                  = null                      ;

    }

    public AltCase(final AltStatement altStatement) {
        super(new AST[] { null, null, new BlockStatement(altStatement) });

        this.preconditionExpression = null                      ;
        this.guardStatement = null                      ;
        this.body                   = (BlockStatement) this.children[2]  ;
        this.isAltStat              = true                      ;
        this.caseNumber             = -1                        ;
        this.scope                  = null                      ;

    }

    /// ------------------------------
    /// org.processj.utilities.Visitor

    @Override
    public final void accept(Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the If Statement
        this.openScope();

        // Visit
        visitor.visitAltCase(this);

        // Close the scope
        visitor.setContext(this.closeContext());
    }

    /// --------------
    /// Public Methods

    public final boolean definesPrecondition() {

        return this.preconditionExpression != null;

    }

    public final boolean definesGuard() {

        return this.guardStatement != null;

    }

    public final boolean isNestedAltStatement() {

        return this.isAltStat || (this.body.getStatements().child(0) instanceof AltStatement);

    }

    public final Expression getPreconditionExpression() {

        return this.preconditionExpression;

    }

    public final GuardStatement getGuard() {

        return this.guardStatement;

    }

    public final boolean definesInputGuardExpression() {

        return (this.guardStatement != null) && (this.guardStatement.isInputGuard());

    }

    public final AssignmentExpression getInputGuardExpression() {

        return ((this.guardStatement != null) && (this.guardStatement.isInputGuard()))
                ? this.guardStatement.getInputExpression() : null;

    }

    public final Statement getBody() {

        return this.body;

    }

    public final void setNestedAltStatement() {

        // TODO: Get rid of this flag as soon as possible
        this.isAltStat = true;

    }

    public final int getCaseNumber() {

        if(caseNumber == -1)
            Error.error("AltCase Error: The caseNumber for this AltCase was never set!");

        return caseNumber;
    }

    public final void setCaseNumber(int n) {

        caseNumber = n;

    }

}
