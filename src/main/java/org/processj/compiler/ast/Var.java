package org.processj.compiler.ast;

import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class Var extends AST {

    /// --------------
    /// Private Fields

    private final Expression initializationExpression   ;
    private final Name       name                       ;

    public Var(final Name name, final Expression init) {

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

    @Override
    public <T> T visit(Visitor<T> v) throws Phase.Error {
        return null;
    }

    public final Name getName() {
        return this.name;
    }

    public final Expression getInitializationExpression() {
        return this.initializationExpression;
    }

}