package org.processj.ast.alt;

import org.processj.Phase;
import org.processj.ast.AST;
import org.processj.ast.expression.Expression;
import org.processj.ast.Statement;
import org.processj.utilities.Visitor;
import org.processj.utilities.Error;

public class AltCase extends AST {

    /// --------------
    /// Private Fields

    private final Expression preconditionExpression  ;
    private final Guard guard                   ;
    private Statement statement               ;
    private int               caseNumber              ;
    public boolean            isAltStat               ;

    /// ------------
    /// Constructors

    public AltCase(final Expression preconditionExpression, final Guard guard, final Statement statement) {
        super(new AST[] { preconditionExpression, guard, statement });

        this.preconditionExpression = preconditionExpression    ;
        this.guard                  = guard                     ;
        this.statement              = statement                 ;
        this.isAltStat              = false                     ;
        this.caseNumber             = -1                        ;

    }

    public AltCase(final AltStat altStatement) {
        super(new AST[] { null, null, altStatement });

        this.preconditionExpression = null          ;
        this.guard                  = null          ;
        this.statement              = altStatement  ;
        this.isAltStat              = true          ;
        this.caseNumber             = -1            ;

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

    public final boolean definesStatement() {

        return this.statement != null;

    }

    public final boolean isNestedAltStatement() {

        return this.isAltStat || (this.statement instanceof AltStat);

    }

    public final Expression getPreconditionExpression() {

        return this.preconditionExpression;

    }

    public final Guard getGuard() {

        return this.guard;

    }

    public final Statement getStatement() {

        return this.statement;

    }

    public final void setNestedAltStatement() {

        // TODO: Get rid of this flag as soon as possible
        this.isAltStat = true;

    }

    public final void setStatement(final Statement statement) {

        this.statement = statement;
        this.children[2] = statement;

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
