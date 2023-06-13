package org.processj.compiler.ast.type;

import org.processj.compiler.ast.AST;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class ChannelEndType extends Type {

    /// -----------------------
    /// Public Static Constants

    public static final int SHARED      = 0 ;
    public static final int NOT_SHARED  = 1 ;
    public static final int READ_END    = 0 ;
    public static final int WRITE_END   = 1 ;

    /// --------------
    /// Private Fields

    private Type  componentType       ;
    private final int   shared              ;
    private final int   end                 ;

    /// ------------
    /// Constructors

    public ChannelEndType(final int shared, final Type componentType, final int end) {
        super(new AST[] { componentType });
        this.componentType  = componentType ;
        this.shared         = shared        ;
        this.end            = end           ;
    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ChannelEndType} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ChannelEndType} & both
     *         represent the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public boolean equals(final Object that) {

        // Check Type & Strings (and potentially recur), then trivial checks, then component checks.
        return super.equals(that) && (that instanceof ChannelEndType)
                && ((this.shared == ((ChannelEndType) that).shared) || (this.end == ((ChannelEndType) that).end))
                && this.componentType.equals(((ChannelEndType) that).componentType);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link ChannelEndType}.</p>
     * @return Literal {@link String} representation of the {@link ChannelEndType}.
     * @since 0.1.0
     */
    @Override
    public String toString() {

        return "chan<" + this.componentType + ">." + ((this.end == READ_END) ? "read" : "write");

    }

    /// --------------------
    /// org.processj.ast.AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ChannelEndType}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitChannelEndType(ChannelEndType)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitChannelEndType(this);

    }

    /// ---------------------
    /// org.processj.ast.Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ChannelEndType}.</p>
     * @return The internal {@link String} signature representing the {@link ChannelEndType}.
     * @since 0.1.0
     */
    // TODO: perhaps the base type of a channel end type ought to be a channel ;->
    @Override
    public final String getSignature() {

        return "{" + getComponentType().getSignature() + ";" + (isReadEnd() ? "?" : "!");

    }

    /// --------------
    /// Public Methods

    public final boolean isSharedEnd() {

        return this.shared == SHARED;

    }

    public final boolean isReadEnd() {

        return this.end == READ_END;

    }

    public final boolean isWriteEnd() {

        return end == WRITE_END;

    }

    public final Type getComponentType() {

        return this.componentType;

    }

    // α =T β ⇔ Channel?(α) ∧ Channel?(β) ∧ α = β ∧ (m1 = m2)
    @Override
    public final boolean typeEqual(final Type that) {

        System.out.println("Checking Type: " + this + " of class " + this.getClass());
        System.out.println("With component Type: " + this.componentType + " of Class " + this.componentType.getClass());
        // Note that .equals handles the cases where either side is a NamedType by allowing .equals to recur on NamedTypes
        // resolved Types in the case one was encountered on the right.
        return this.equals(that);

    }

    // α =T β ⇔ Channel?(α) ∧ Channel?(β) ∧ α = β ∧ (m1 = m2)
    @Override
    public final boolean typeEquivalent(final Type that) {

        return this.equals(that);

    }

    @Override
    public final boolean typeAssignmentCompatible(final Type that) {

        return this.equals(that);

    }

    public final void setComponentType(final Type componentType) {

        this.componentType = componentType;
        this.children[0] = componentType;

    }

}