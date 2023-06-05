package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class Var extends AST {

    /// --------------
    /// Private Fields

    private final Expression initializationExpression   ;
    private final Name          name                       ;

    public Var(final Name name, final Expression init) {
        super(new AST[] { name, init });
        this.initializationExpression   = init;
        this.name                       = name;
    }

    public Var(final Name name) {
        this(name, null);
    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns the {@link String} value of the {@link Var}'s name.</p>
     * @return {@link String} value of the {@link Var}'s name.
     * @since 0.1.0
     */
    @Override
    public String toString() {

        return this.name.toString();

    }

    public final Name getName() {
        return this.name;
    }

    public final Expression getInitializationExpression() {
        return this.initializationExpression;
    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitVar(this);

    }

}