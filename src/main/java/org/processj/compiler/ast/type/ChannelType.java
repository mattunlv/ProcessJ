package org.processj.compiler.ast.type;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class ChannelType extends AST implements Type {

    // These are the different values the field `shared' can take.
    public static final int SHARED_READ         = 0;
    public static final int SHARED_WRITE        = 1;
    public static final int SHARED_READ_WRITE   = 2;
    public static final int NOT_SHARED          = 3;

    public String[] modSyms = { "shared read", "shared write", "shared", "" };

    /// --------------
    /// Private Fields

    private Type  componentType       ;

    private final int   shared              ;

    private final boolean isShared;
    private final boolean isRead;

    /// ------------
    /// Constructors

    public ChannelType(final Type componentType, final int shared) {
        super(new AST[] { });

        this.componentType  = componentType     ;
        this.shared         = shared            ;
        this.isShared       = false             ;
        this.isRead         = false             ;

    }

    public ChannelType(final Type componentType, final boolean isShared, final boolean isRead) {
        super(new AST[] { });

        this.componentType  = componentType     ;
        this.shared         = 0                 ;
        this.isShared       = isShared          ;
        this.isRead         = isRead            ;


    }


    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ChannelType} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ChannelType} & both
     *         represent the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        // TODO: Maybe replace with .equals(); it may or may not intrude with .typeEqual()
        return (that instanceof ChannelType)
                && (this.componentType.isTypeEqualTo(((ChannelType) that).componentType))
                && (this.shared == ((ChannelType) that).shared);

    }

    // TODO: add sharing stuff
    /**
     * <p>Returns a literal {@link String} representation of the {@link ChannelType}.</p>
     * @return Literal {@link String} representation of the {@link ChannelType}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return "chan<" + this.componentType + ">";

    }

    /// --------------------
    /// org.processj.ast.AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ChannelType}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitChannelType(ChannelType)} method.</p>
     *
     * @param visitor The {@link Visitor} to dispatch.
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        visitor.visitChannelType(this);

    }

    @Override
    public boolean isTypeEqualTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeEquivalentTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeLessThan(Object that) {
        return false;
    }

    @Override
    public boolean isTypeGreaterThan(Object that) {
        return false;
    }

    @Override
    public boolean isTypeLessThanOrEqualTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeGreaterThanOrEqualTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeCeilingOf(Object that) {
        return false;
    }

    @Override
    public boolean isSubTypeOf(Object that) {
        return false;
    }

    @Override
    public boolean isAssignmentCompatibleTo(Object that) {
        return false;
    }

    /// ---------------------
    /// org.processj.ast.Type

    @Override
    public Name getName() {
        return null;
    }

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ChannelType}.</p>
     * @return The internal {@link String} signature representing the {@link ChannelType}.
     * @since 0.1.0
     */
    @Override
    public final String getSignature() {

        return "{" + getComponentType().getSignature() + ";";

    }

    @Override
    public Type addDimension() {
        return null;
    }

    @Override
    public Type clearDepth() {
        return null;
    }

    @Override
    public int getDepth() {
        return 0;
    }

    // if α = Channel(t1, a1) ∧ β = Channel(t2, a2)
    // α =T β ⇔ Channel?(α) ∧ Channel?(β) ∧ (t1 =T t2) ∧ (a1 = a2)
    public final boolean typeEqual(final Type that) {

        // Channel?(β) -- is t a channel?
        // (a1 = a2) -- are both channels' ends shared in the same way?
        // (t1 =T t2) -- are the base types type equal?
        return this.equals(that);

    }

    // α ∼T β ⇔ α =T β
    public final boolean typeEquivalent(final Type that) {

        return this.equals(that);

    }

    // Channels cannot be assigned.
    public final boolean typeAssignmentCompatible(final Type that) {

        return false;

    }

    /// --------------
    /// Public Methods

    public final int isShared() {

        return this.shared;

    }

    public final Type getComponentType() {

        return this.componentType;

    }

    public final String modString() {

        return modSyms[shared];

    }

    public final void setComponentType(final Type componentType) {

        this.componentType = componentType;

    }

}