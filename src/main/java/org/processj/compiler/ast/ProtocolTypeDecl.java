package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class ProtocolTypeDecl extends Type implements DefineTopLevelDecl, SymbolMap.Context {

    /// --------------
    /// Private Fields

    /**
     * <p>{@link Name} corresponding to the {@link ProtocolTypeDecl}.</p>
     */
    private final Name                      name        ;
    private final Sequence<ProtocolCase>    body        ;
    private final Sequence<Name>            extend      ;
    private final Sequence<Type>            extendTypes ;
    private SymbolMap scope;

    /// ------------
    /// Constructors

    public ProtocolTypeDecl(Sequence<Modifier> modifiers, Name name,
                            Sequence<Name> extend, Annotations annotations,
                            Sequence<ProtocolCase> body) {
        super(name);
        nchildren = 5;
        children = new AST[] { modifiers, name, extend, annotations, body };
        this.name = name;
        this.body = body;
        this.scope = null;
        this.extend = extend;
        this.extendTypes = new Sequence<>();
    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ProtocolTypeDecl} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ProtocolTypeDecl} & both
     *         represent the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof ProtocolTypeDecl);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link ProtocolTypeDecl}.</p>
     * @return Literal {@link String} representation of the {@link ProtocolTypeDecl}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.name.toString();

    }

    /// --------------------
    /// org.processj.ast.AST
    
    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ProtocolTypeDecl}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitProtocolTypeDecl(ProtocolTypeDecl)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        visitor.setScope(this.openScope(visitor.getScope()));

        // Visit
        S result = visitor.visitProtocolTypeDecl(this);

        // Close the scope
        visitor.setScope(visitor.getScope().getEnclosingScope());

        return result;

    }

    /// ---------------------
    /// org.processj.ast.Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ProtocolTypeDecl}.</p>
     * @return The internal {@link String} signature representing the {@link ProtocolTypeDecl}.
     * @since 0.1.0
     */
    @Override
    public final String getSignature() {

        return "<P" + this.name + ";";

    }

    public final void setTypeForEachExtend(final TypeReturnCallback typeReturnCallback) {

        if((this.extend != null) && (typeReturnCallback != null)) {

            // Clear the extend Types
            this.extendTypes.clear();

            // Append the results
            this.extend.forEach(name -> {

                Type type;

                try {

                    type = typeReturnCallback.Invoke(name);

                } catch (final Phase.Error phaseError) {

                    type = new ErrorType();

                }

                this.extendTypes.append(type);

            });
        }

    }

    public final void setTypeForEachRecordMember(final TypeReturnCallback typeReturnCallback) {

        if((this.body != null) && (typeReturnCallback != null)) {

            this.body.forEach(protocolCase -> protocolCase.body().forEach(recordMember -> {

                Type type;

                try {

                    type = typeReturnCallback.Invoke(recordMember.getName());

                } catch (final Phase.Error phaseError) {

                    type = new ErrorType();

                }

                recordMember.setType(type);

            }));

        }

    }

    // *************************************************************************
    // ** Accessor Methods

    public Sequence<Modifier> modifiers() {
        return (Sequence<Modifier>) children[0];
    }

    public Name name() {
        return (Name) children[1];
    }

    public Sequence<Name> extend() {
        return (Sequence<Name>) children[2];
    }

    public Annotations annotations() {
        return (Annotations) children[3];
    }

    public Sequence<ProtocolCase> body() {
        return (Sequence<ProtocolCase>) children[4];
    }

    public final ProtocolCase getCaseFrom(final String name) {

        ProtocolCase result = null;

        if((name != null) && (this.body != null))
            for(final ProtocolCase protocolCase: this.body)
                if(protocolCase.toString().equals(name)) {

                    result = protocolCase;
                    break;

                }

        return result;

    }

    // *************************************************************************
    // ** Type Related Methods

    public boolean extendsProtocol(ProtocolTypeDecl pd) {
        if (typeEqual(pd))
            return true;
        boolean b = false;
        for (Name n : extend())
            b = ((ProtocolTypeDecl) n.myDecl).extendsProtocol(pd) || b;
        return b;
    }

    public ProtocolCase getCase(String name) {
        /** Search our own body first */
        if (body() != null) {
            for (ProtocolCase pc : body()) {
                if (pc.name().getName().equals(name))
                    return pc;
            }
        }
        /** This protocol type did not have the case */
        ProtocolCase p = null;
        for (Name n : extend()) {
            p = ((ProtocolTypeDecl) n.myDecl).getCase(name);
            if (p != null)
                return p;
        }
        return null;
    }

    @Override
    public boolean typeEqual(final Type that) {

        return this.equals(that);

    }

    @Override
    public boolean typeEquivalent(final Type that) {

        return this.equals(that);

    }

    // TODO
    @Override
    public boolean typeAssignmentCompatible(Type t) {
        if (!(t instanceof ProtocolTypeDecl))
            return false;
        ProtocolTypeDecl pt = (ProtocolTypeDecl) t;
        return pt.extendsProtocol(this);
    }

    @FunctionalInterface
    public interface TypeReturnCallback {

        Type Invoke(final Name name) throws Phase.Error;

    }

}