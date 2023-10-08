package org.processj.compiler.ast.type.primitive;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.expression.literal.PrimitiveLiteralExpression;
import org.processj.compiler.ast.type.ExternType;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public abstract class PrimitiveType extends AST implements Type {

    private final Name name;

    /// ------------
    /// Constructors
    public PrimitiveType(final Token p_t) {
        super(p_t);

        this.name = new Name("");

    }

    public PrimitiveType(final Name name) {

        this.name = name;

    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link PrimitiveType} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link PrimitiveType} & both
     *         represent the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public boolean equals(final Object that) {

        return super.equals(that) && (that instanceof PrimitiveType);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link PrimitiveType}.</p>
     * @return Literal {@link String} representation of the {@link PrimitiveType}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.getName().toString();

    }

    @Override
    public Name getName() {
        return this.name;
    }

    /// --------------------
    /// org.processj.ast.AST
    
    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link PrimitiveType}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitPrimitiveType(PrimitiveType)} method.</p>
     *
     * @param visitor The {@link Visitor} to dispatch.
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        visitor.visitPrimitiveType(this);

    }

}