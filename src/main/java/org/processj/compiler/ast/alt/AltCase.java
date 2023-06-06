package org.processj.compiler.ast.alt;

import org.processj.compiler.ast.Block;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.SymbolMap;
import org.processj.compiler.ast.expression.Assignment;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.Statement;
import org.processj.compiler.phases.phase.Visitor;
import org.processj.compiler.utilities.Error;

// TODO: Maybe Extend Statement
public class AltCase extends Statement implements SymbolMap.Context {

    /// --------------
    /// Private Fields

    private final Expression    preconditionExpression  ;
    private final Guard         guard                   ;
    private final Block         body                    ;
    private int                 caseNumber              ;
    public boolean              isAltStat               ;
    private SymbolMap           scope                   ;

    /// ------------
    /// Constructors

    public AltCase(final Expression preconditionExpression, final Guard guard, final Statement statement) {
        super(new AST[] { preconditionExpression, guard, new Block(statement) });

        this.preconditionExpression = preconditionExpression    ;
        this.guard                  = guard                     ;
        this.body                   = (Block) this.children[2]  ;
        this.isAltStat              = false                     ;
        this.caseNumber             = -1                        ;
        this.scope                  = null                      ;

    }

    public AltCase(final AltStat altStatement) {
        super(new AST[] { null, null, new Block(altStatement) });

        this.preconditionExpression = null                      ;
        this.guard                  = null                      ;
        this.body                   = (Block) this.children[2]  ;
        this.isAltStat              = true                      ;
        this.caseNumber             = -1                        ;
        this.scope                  = null                      ;

    }

    /// ------------------------------
    /// org.processj.utilities.Visitor

    @Override
    public final <S> S visit(Visitor<S> visitor)
            throws Phase.Error {

        return visitor.visitAltCase(this);

    }

    /// --------------
    /// Public Methods

    public final boolean definesPrecondition() {

        return this.preconditionExpression != null;

    }

    public final boolean definesGuard() {

        return this.guard != null;

    }

    public final boolean isNestedAltStatement() {

        return this.isAltStat || (this.body.getStatements().child(0) instanceof AltStat);

    }

    public final Expression getPreconditionExpression() {

        return this.preconditionExpression;

    }

    public final Guard getGuard() {

        return this.guard;

    }

    public final boolean definesInputGuardExpression() {

        return (this.guard != null) && (this.guard.isInputGuard());

    }

    public final Assignment getInputGuardExpression() {

        return ((this.guard != null) && (this.guard.isInputGuard()))
                ? this.guard.getInputExpression() : null;

    }

    public final Statement getStatements() {

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
