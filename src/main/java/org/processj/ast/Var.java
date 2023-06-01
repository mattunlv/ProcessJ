package org.processj.ast;

import org.processj.utilities.Visitor;

public class Var extends AST {

    /// --------------
    /// Private Fields

    private final Expression initializationExpression   ;
    private final Name       name                       ;
    private final String     nameLiteral                ;

    public VarDecl myDecl = null;

    /* Note init() can return null */

    public Var(Name name, Expression init) {
        super(name);
        nchildren = 2;
        children = new AST[] { name, init };
        this.initializationExpression   = init;
        this.name                       = name;
        this.nameLiteral                = (name != null) ? name.getname() : "";
    }

    public Var(Name name) {
        super(name);
        nchildren = 2;
        children = new AST[] { name, null };
        this.initializationExpression = null;
        this.name                     = name;
        this.nameLiteral              = (name != null) ? name.getname() : "";
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

        return this.nameLiteral;

    }

    public Name name() {
        return this.name;
    }

    public Expression init() {
        return this.initializationExpression;
    }

    public <S> S visit(final Visitor<S> visitor) {
        return visitor.visitVar(this);
    }

    /**
     * <p>Returns a flag indicating if the {@link Var} is initialized.</p>
     * @return flag indicating if the {@link Var} is initialized.
     * @since 0.1.0
     */
    public final boolean isInitialized() {

        return this.initializationExpression != null;

    }

}