package org.processj.compiler.ast.type;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.ast.statement.declarative.ProtocolCase;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

import java.util.HashMap;
import java.util.Map;

public class ProtocolTypeDeclaration extends Type implements SymbolMap.Context {

    /// --------------
    /// Private Fields

    /**
     * <p>{@link Name} corresponding to the {@link ProtocolTypeDeclaration}.</p>
     */
    private final Name                          name                ;
    private final Sequence<ProtocolCase> body                ;
    private final Sequence<Name>                extend              ;
    private final Map<String, ProtocolTypeDeclaration> extendTypes         ;
    private SymbolMap scope;

    /// ------------
    /// Constructors

    public ProtocolTypeDeclaration(Sequence<Modifier> modifiers, Name name,
                                   Sequence<Name> extend, Annotations annotations,
                                   Sequence<ProtocolCase> body) {
        super(name);
        nchildren = 5;
        children = new AST[] { modifiers, name, extend, annotations, body };
        this.name = name;
        this.body = body;
        this.scope = null;
        this.extend = extend;
        this.extendTypes = new HashMap<>();
    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ProtocolTypeDeclaration} & both
     * represent the same {@link Type} via name.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ProtocolTypeDeclaration} & both
     *         represent the same {@link Type} via name.
     * @since 0.1.0
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof ProtocolTypeDeclaration);

    }

    /**
     * <p>Returns a literal {@link String} representation of the {@link ProtocolTypeDeclaration}.</p>
     * @return Literal {@link String} representation of the {@link ProtocolTypeDeclaration}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.name.toString();

    }

    /// --------------------
    /// org.processj.ast.AST
    
    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link ProtocolTypeDeclaration}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitProtocolTypeDeclaration(ProtocolTypeDeclaration)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        visitor.setScope(this.openScope(visitor.getScope()));

        // Visit
        S result = visitor.visitProtocolTypeDeclaration(this);

        // Close the scope
        visitor.setScope(visitor.getScope().getEnclosingScope());

        return result;

    }

    /// ---------------------
    /// org.processj.ast.Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ProtocolTypeDeclaration}.</p>
     * @return The internal {@link String} signature representing the {@link ProtocolTypeDeclaration}.
     * @since 0.1.0
     */
    @Override
    public final String getSignature() {

        return "<P" + this.name + ";";

    }

    public final void setTypeForEachExtend(final CandidatesReturnCallback candidatesReturnCallback) throws Phase.Error {

        if((this.extendTypes != null) && (candidatesReturnCallback != null)) {

            // Clear the Types
            this.extendTypes.clear();

            // Iterate through each extend Name
            for(final Name name: this.extend) {

                // Initialize a handle to the Type
                final ProtocolTypeDeclaration candidate = candidatesReturnCallback.Invoke(name);

                // Place the mapping
                this.extendTypes.put(name.toString(), candidate);

            }

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

    public Sequence<ProtocolCase> getBody() {
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

    public boolean extendsProtocol(ProtocolTypeDeclaration pd) {
        if (typeEqual(pd))
            return true;
        boolean b = false;
        for (Name n : extend())
            b = ((ProtocolTypeDeclaration) n.myDecl).extendsProtocol(pd) || b;
        return b;
    }

    public ProtocolCase getCase(String name) {
        /** Search our own body first */
        if (getBody() != null) {
            for (ProtocolCase pc : getBody()) {
                if (pc.getName().getName().equals(name))
                    return pc;
            }
        }
        /** This protocol type did not have the case */
        ProtocolCase p = null;
        for (Name n : extend()) {
            p = ((ProtocolTypeDeclaration) n.myDecl).getCase(name);
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
        if (!(t instanceof ProtocolTypeDeclaration))
            return false;
        ProtocolTypeDeclaration pt = (ProtocolTypeDeclaration) t;
        return pt.extendsProtocol(this);
    }

    @Override
    public BlockStatement getMergeBody() {
        return null;
    }

    @Override
    public BlockStatement getClearedMergeBody() {
        return null;
    }

    @Override
    public boolean definesLabel() {
        return false;
    }

    @Override
    public boolean definesEndLabel() {
        return false;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public void setEndLabel(String label) {

    }

    @Override
    public String getEndLabel() {
        return null;
    }

    @FunctionalInterface
    public interface TypeReturnCallback {

        Type Invoke(final Name name) throws Phase.Error;

    }

    @FunctionalInterface
    public interface CandidatesReturnCallback {

        ProtocolTypeDeclaration Invoke(final Name name) throws Phase.Error;

    }

}