package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

import java.util.HashMap;
import java.util.Map;

public class RecordTypeDecl extends Type implements DefineTopLevelDecl, SymbolMap.Context {

    /// --------------
    /// Private Fields

    /**
     * <p>{@link Name} value of the {@link RecordTypeDecl}'s name.</p>
     */
    private final Name name;
    private SymbolMap scope;
    private final Map<String, RecordTypeDecl> extendTypes;
    private final Sequence<Name> extend;
    private final Sequence<RecordMember> recordMembers;

    /// ------------
    /// Constructors

    public RecordTypeDecl(Sequence<Modifier> modifiers, Name name, 
                          Sequence<Name> extend, Annotations annotations,
                          Sequence<RecordMember> body) {
        super(name);
        nchildren = 5;
        children = new AST[] { modifiers, name, extend, annotations, body };
        this.name = name;
        this.scope = null;
        this.extendTypes = new HashMap<>();
        this.extend = extend;
        this.recordMembers = body;
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

        return this.name.toString();

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
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        visitor.setScope(this.openScope(visitor.getScope()));

        // Visit
        S result = visitor.visitRecordTypeDecl(this);

        // Close the scope
        visitor.setScope(visitor.getScope().getEnclosingScope());

        return result;

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

    public final void setTypeForEachExtend(final CandidatesReturnCallback candidatesReturnCallback) throws Phase.Error {

        if((this.extend != null) && (candidatesReturnCallback != null)) {

            // Clear the Types
            this.extendTypes.clear();

            // Iterate through each extend Name
            for(final Name name: this.extend) {

                // Initialize a handle to the Type
                final RecordTypeDecl recordTypeDecl = candidatesReturnCallback.Invoke(name);

                // Place the Mapping
                this.extendTypes.put(name.toString(), recordTypeDecl);

            }

        }

    }

    // *************************************************************************
    // ** Accessor Methods

    public Sequence<Modifier> modifiers() {
        return (Sequence<Modifier>) children[0];
    }

    public Sequence<Name> getExtends() {
        return (Sequence<Name>) children[2];
    }

    public Annotations getAnnotations() {
        return (Annotations) children[3];
    }

    public Sequence<RecordMember> getBody() {
        return (Sequence<RecordMember>) children[4];
    }

    // *************************************************************************
    // ** Misc. Methods

    public RecordMember getMember(String name) {
        for (RecordMember rm : getBody())
            if (rm.getName().getName().equals(name))
                return rm;
        return null;
    }

    public boolean extendsRecord(RecordTypeDecl rt) {
        if (typeEqual(rt))
            return true;
        boolean b = false;
        for (Name n : getExtends())
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

    @FunctionalInterface
    public interface TypeReturnCallback {

        Type Invoke(final Name name) throws Phase.Error;

    }

    @FunctionalInterface
    public interface CandidatesReturnCallback {

        RecordTypeDecl Invoke(final Name name) throws Phase.Error;

    }

}