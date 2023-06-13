package org.processj.compiler.ast.statement.declarative;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Var;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.type.ConstantDeclaration;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class LocalDeclaration extends Statement {

    private final boolean   isConstant                  ;
    private Name name                        ;
    private Type type                        ;
    private Expression      initializationExpression    ;

    public LocalDeclaration(final Type type, final Name name,
                            final Expression initializationExpression, final boolean isConstant) {
        super(new AST[] { type, name, initializationExpression });

        this.isConstant                 = isConstant                ;
        this.type                       = type                      ;
        this.name                       = name                      ;
        this.initializationExpression   = initializationExpression  ;

    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns the {@link String} value of the {@link ConstantDeclaration}'s name.</p>
     * @return {@link String} value of the {@link ConstantDeclaration}'s name.
     * @since 0.1.0
     */
    @Override
    public String toString() {

        return this.name.toString();

    }

    /**
     * <p>Returns a flag indicatind if the {@link LocalDeclaration} is initialized.</p>
     * @return Flag indicatind if the {@link LocalDeclaration} is initialized.
     * @since 0.1.0
     */
    public final boolean isInitialized() {

        return this.initializationExpression != null;

    }

    /**
     * <p>Returns the {@link LocalDeclaration}'s initialization {@link Expression}.</p>
     * @return The {@link LocalDeclaration}'s initialization {@link Expression}.
     * @since 0.1.0
     */
    public final Expression getInitializationExpression() {

        return this.initializationExpression;

    }

    public final void setInitializationExpression(final Expression initializationExpression) {

        this.initializationExpression   = initializationExpression;
        this.children[2]                = initializationExpression;

    }

    public final void setName(final Name name) {

        this.name           = name;
        this.children[1]    = name;

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

        return visitor.visitLocalDeclaration(this);

    }

}