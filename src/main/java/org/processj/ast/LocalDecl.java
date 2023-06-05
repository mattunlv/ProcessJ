package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class LocalDecl extends Statement implements VarDecl {

    private final boolean   isConstant                  ;
    private final Name      name                        ;
    private Type            type                        ;
    private Expression initializationExpression    ;

    public LocalDecl(final Type type, final Var var, final boolean isConstant) {
        super(type);
        nchildren                       = 3                                         ;
        children                        = new AST[] { type, var.getName(), var.getInitializationExpression() };
        this.isConstant                 = isConstant                                ;
        this.type                       = type                                      ;
        this.name                       = var.getName()                             ;
        this.initializationExpression   = var.getInitializationExpression()         ;
    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns the {@link String} value of the {@link ConstantDecl}'s name.</p>
     * @return {@link String} value of the {@link ConstantDecl}'s name.
     * @since 0.1.0
     */
    @Override
    public String toString() {

        return this.name.toString();

    }

    /**
     * <p>Returns a flag indicatind if the {@link LocalDecl} is initialized.</p>
     * @return Flag indicatind if the {@link LocalDecl} is initialized.
     * @since 0.1.0
     */
    public final boolean isInitialized() {

        return this.initializationExpression != null;

    }

    /**
     * <p>Returns the {@link LocalDecl}'s initialization {@link Expression}.</p>
     * @return The {@link LocalDecl}'s initialization {@link Expression}.
     * @since 0.1.0
     */
    public final Expression getInitializationExpression() {

        return this.initializationExpression;

    }

    public final void setInitializationExpression(final Expression initializationExpression) {

        this.initializationExpression   = initializationExpression;
        this.children[2]                = initializationExpression;

    }

    public final boolean isConstant() {

        return this.isConstant;

    }

    public final Type getType() {

        return this.type;

    }

    public final Name getName() {

        return this.name;

    }

    public final void setType(final Type type) {

        this.type           = type;
        this.children[0]    = type;

    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitLocalDecl(this);

    }

}