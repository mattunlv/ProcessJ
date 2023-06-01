package org.processj.ast;

import org.processj.utilities.Visitor;

public class RecordTypeDecl extends Type implements DefineTopLevelDecl {

    /// --------------
    /// Private Fields

    /**
     * <p>{@link String} value of the {@link RecordTypeDecl}'s name.</p>
     */
    private final String name;

    /// ------------
    /// Constructors

    public RecordTypeDecl(Sequence<Modifier> modifiers, Name name, 
                          Sequence<AST> extend, Annotations annotations,
                          Sequence<RecordMember> body) {
        super(name);
        nchildren = 5;
        children = new AST[] { modifiers, name, extend, annotations, body };
        this.name = (name != null) ? name.toString() : "";
    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link RecordTypeDecl} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link RecordTypeDecl} & both
     *         represent the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof RecordTypeDecl);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link RecordTypeDecl}.</p>
     * @return Literal {@link String} representation of the {@link RecordTypeDecl}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.name;

    }

    /// --------------------
    /// org.processj.ast.AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link RecordTypeDecl}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitRecordTypeDecl(RecordTypeDecl)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) {

        return visitor.visitRecordTypeDecl(this);

    }

    /// ---------------------
    /// org.processj.ast.Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link RecordTypeDecl}.</p>
     * @return The internal {@link String} signature representing the {@link RecordTypeDecl}.
     * @since 0.1.0
     */
    @Override
    public final String getSignature() {

        return "<R" + this.name + ";";

    }

    // *************************************************************************
    // ** Accessor Methods

    public Sequence<Modifier> modifiers() {
        return (Sequence<Modifier>) children[0];
    }

    public Sequence<Name> extend() {
        return (Sequence<Name>) children[2];
    }

    public Annotations annotations() {
        return (Annotations) children[3];
    }

    public Sequence<RecordMember> body() {
        return (Sequence<RecordMember>) children[4];
    }

    // *************************************************************************
    // ** Misc. Methods

    public RecordMember getMember(String name) {
        for (RecordMember rm : body())
            if (rm.name().getname().equals(name))
                return rm;
        return null;
    }

    public boolean extendsRecord(RecordTypeDecl rt) {
        if (typeEqual(rt))
            return true;
        boolean b = false;
        for (Name n : extend())
            b = ((RecordTypeDecl) n.myDecl).extendsRecord(rt) || b;
        return b;
    }

    // α =T β ⇔ Record?(α) ∧ Record?(β) ∧ (name1 = name2)
    // We implement NAME EQUALITY not structural equality
    @Override
    public boolean typeEqual(final Type that) {

        return this.equals(that);

    }

    // α∼T β ⇔ α =T β
    @Override
    public boolean typeEquivalent(final Type that) {

        return this.equals(that);

    }

    // α :=T β ⇔ α ∼T β ⇔ α =T β
    @Override
    public boolean typeAssignmentCompatible(Type t) {
        if (!(t instanceof RecordTypeDecl))
            return false;
        RecordTypeDecl rt = (RecordTypeDecl) t;
        return rt.extendsRecord(this);
    }
}