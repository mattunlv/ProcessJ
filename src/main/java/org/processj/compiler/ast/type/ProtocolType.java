package org.processj.compiler.ast.type;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.modifier.Modifiers;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.statement.Statements;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.ast.statement.declarative.DeclarativeContext;
import org.processj.compiler.ast.statement.declarative.Names;
import org.processj.compiler.ast.statement.declarative.ProtocolExtends;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a declaration of a {@link Context}'s protocol.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see Context
 * @see DeclarativeContext
 * @see BlockStatement
 */
public class ProtocolType extends BlockStatement implements Type, DeclarativeContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link ProtocolType}'s specified {@link Annotations}.</p>
     * @since 1.0.0
     * @see Annotations
     */
    private final Annotations annotations;

    /**
     * <p>The {@link ProtocolType}'s specified {@link Modifiers}.</p>
     * @since 1.0.0
     * @see Modifiers
     */
    private final Modifiers modifiers;

    /**
     * <p>The {@link ProtocolType}'s {@link Name}.</p>
     * @since 1.0.0
     * @see Name
     */
    private final Name name;

    /**
     * <p>The {@link ProtocolType}'s set of extend {@link ProtocolExtends}.</p>
     * @since 1.0.0
     * @see ProtocolExtends
     */
    private ProtocolExtends extend;


    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ProtocolType} with the specified {@link Annotations}, {@link Modifiers},
     * {@link Name}, extend {@link ProtocolExtends}, & {@link Statement} body.</p>
     * @param annotations The {@link Annotations} specified with the {@link ProtocolType}.
     * @param modifiers The {@link Modifiers} specifying access privileges.
     * @param name The {@link Name} corresponding to the {@link ProtocolType}.
     * @param extend The set of implement {@link ProtocolExtends}.
     * @param body The {@link ProtocolType}'s {@link Statement} body.
     * @since 1.0.0
     * @see Annotations
     * @see Modifiers
     * @see Name
     * @see ProtocolExtends
     * @see Statement
     */
    public ProtocolType(final Modifiers modifiers,
                        final Name name,
                        final Names extend,
                        final Annotations annotations,
                        final Statement body) {
        super(body);

        this.annotations    = annotations   ;
        this.modifiers      = modifiers     ;
        this.name           = name          ;
        //this.extend         = extend        ;

    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} value of the {@link ProtocolType}'s {@link Name}.</p>
     * @return {@link String} value of the {@link ProtocolType}'s {@link Name}.
     * @since 1.0.0
     * @see Name
     * @see String
     */
    @Override
    public final String toString() {

        return this.name.toString();

    }

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link ProtocolType}
     * & both represent the same {@link Type} via {@link Name}.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link ProtocolType} & both
     *         represent the same {@link Type} via {@link Name}.
     * @since 1.0.0
     * @see Name
     * @see Object
     * @see Type
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof ProtocolType)
                && this.name.equals(((ProtocolType) that).getName());

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context} & scope, and dispatches the
     * {@link Visitor} to the {@link Annotations} via {@link Annotations#accept(Visitor)}, the {@link Modifiers} via
     * {@link Modifiers#accept(Visitor)}, the set of extends {@link ProtocolExtends} via {@link ProtocolExtends#accept(Visitor)},
     * the {@link ProtocolType} via {@link Visitor#visitProtocolTypeDeclaration(ProtocolType)},
     * & {@link Statement} children via {@link Statement#accept(Visitor)} before updating the
     * {@link org.processj.compiler.ast.Context} to the enclosing {@link Context}.
     * @since 1.0.0
     * @see Annotations
     * @see Modifiers
     * @see Name
     * @see ProtocolExtends
     * @see Statement
     * @see Visitor
     * @see Phase.Error
     */
    @Override
    public void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the Block Statement
        this.openScope();

        // Dispatch the annotations
        this.annotations.accept(visitor);

        // Dispatch the Modifiers
        this.modifiers.accept(visitor);

        // Dispatch the Name
        this.name.accept(visitor);

        // Dispatch the Implement Extends
        this.extend.accept(visitor);

        // Dispatch the ProtocolTypeDeclaration
        visitor.visitProtocolTypeDeclaration(this);

        // Dispatch the children
        this.getBody().accept(visitor);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    @Override
    public boolean isTypeEqualTo(Object that) {
        return this.equals(that);
    }

    @Override
    public boolean isTypeEquivalentTo(Object that) {
        return this.equals(that);
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
        if (!(that instanceof ProtocolType))
            return false;
        ProtocolType pt = (ProtocolType) that;
        //return pt.extendsProtocol(this);
        return true;
    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ProtocolType}.</p>
     * @return The internal {@link String} signature representing the {@link ProtocolType}.
     * @since 1.0.0
     * @see String
     */
    public final String getSignature() {

        // Return the result
        return "<P" + this.name + ";";

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

    /**
     * <p>Returns the {@link ProtocolType}'s specified set of {@link Annotations}.</p>
     * @return The {@link ProtocolType}'s specified set of {@link Annotations}.
     * @since 1.0.0
     * @see String
     * @see Annotations
     */
    public final Annotations getAnnotations() {

        return this.annotations;

    }

    /**
     * <p>Returns the {@link ProtocolType}'s specified set of {@link Modifiers}.</p>
     * @return The {@link ProtocolType}'s specified set of {@link Modifiers}.
     * @since 1.0.0
     * @see String
     * @see Modifiers
     */
    public final Modifiers getModifiers() {

        return this.modifiers;

    }

    /**
     * <p>Returns the {@link ProtocolType}'s specified {@link Name}.</p>
     * @return The {@link ProtocolType}'s specified {@link Name}.
     * @since 1.0.0
     * @see Name
     */
    public final Name getName() {

        return this.name;

    }

    /**
     * <p>Returns the {@link ProtocolType}'s specified set of {@link ProtocolExtends}.</p>
     * @return The {@link ProtocolType}'s specified set of {@link ProtocolExtends}.
     * @since 1.0.0
     * @see String
     * @see ProtocolExtends
     */
    public final ProtocolExtends getExtends() {

        return this.extend;

    }

    /**
     * <p>Iterates through each {@link Name} in the {@link ProtocolType}'s set of {@link ProtocolExtends},
     * calls back the {@link CandidatesReturnCallback} to retrieve a candidate {@link ProtocolType}, & maps
     * it to the corresponding {@link Name}.</p>
     * @param candidatesReturnCallback The {@link CandidatesReturnCallback} to invoke in order to retrieve the
     *        {@link ProtocolType}
     * @throws Phase.Error if the {@link CandidatesReturnCallback} failed.
     * @since 1.0.0
     * @see CandidatesReturnCallback
     * @see Name
     * @see ProtocolExtends
     * @see Phase.Error
     */
    public final void setCandidateForEachExtend(final CandidatesReturnCallback candidatesReturnCallback)
            throws Phase.Error {

        if(candidatesReturnCallback != null) {

            int index = 0;

            // Iterate through each extend Name
            for(final Name name: this.extend) {

                // Initialize a handle to the Type
                final ProtocolType candidate = candidatesReturnCallback.Invoke(name);

                // Place the mapping
                this.extend.insert(index, name, candidate);

            }

        }

    }

    public final void append(final Case case_) {

    }

    /// -------
    /// Classes

    /**
     * <p>Class that encapsulates a {@link ProtocolType}'s individual case.</p>
     * @author Jan B. Pedersen
     * @author Cabel Shrestha
     * @author Benjamin Cisneros
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 1.0.0
     * @see BlockStatement
     * @see DeclarativeContext
     */
    public static class Case extends BlockStatement implements DeclarativeContext {

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Case}'s {@link Name}.</p>
         * @since 1.0.0
         * @see Name
         */
        private final Name name;

        /// -----------
        /// Constructor

        /**
         * <p>Initializes the {@link Case} to its' default state with the specified set of
         * {@link Name}s & {@link Statement} body.</p>
         * @param name The {@link Case}'s {@link Name}.
         * @param body The set of {@link Statements} contained in the {@link Case}
         * @since 1.0.0
         * @see Name
         * @see Statement
         */
        public Case(final Name name, final Statement body) {
            super(body);

            this.name = name;

        }

        /// ---
        /// AST

        /**
         * <p>Updates the {@link Visitor}'s {@link org.processj.compiler.ast.Context} & scope, and dispatches the
         * {@link Visitor} to the {@link Name} via {@link Name#accept(Visitor)}, the {@link Case} via
         * {@link Visitor#visitProtocolCase(Case)}, & {@link Statement} children via
         * {@link Statement#accept(Visitor)} before updating the {@link org.processj.compiler.ast.Context} to the
         * enclosing {@link Context}.
         * @since 1.0.0
         * @see Visitor
         * @see Statement
         * @see Phase.Error
         */
        @Override
        public final void accept(final Visitor visitor) throws Phase.Error {

            // Open the Context
            visitor.setContext(this.openContext(visitor.getContext()));

            // Open a scope for the Protocol Case Block
            this.openScope();

            // Dispatch the Name
            this.name.accept(visitor);

            // Dispatch the Protocol Case
            visitor.visitProtocolCase(this);

            // Dispatch the children
            this.getBody().accept(visitor);

            // Close the scope
            visitor.setContext(this.closeContext());

        }

        /// --------------
        /// Public Methods

        /**
         * <p>Returns the {@link Case}'s specified {@link Name}.</p>
         * @return The {@link Case}'s specified {@link Name}.
         * @since 1.0.0
         * @see Name
         */
        public final Name getName() {

            return this.name;

        }

    }

    /// ---------------------
    /// Functional Interfaces

    /**
     * <p>Functional Interface that provides a means of retrieving a {@link ProtocolType} corresponding to
     * the specified {@link Name}.</p>
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 1.0.0
     * @see Name
     * @see ProtocolType
     * @see Phase.Error
     */
    @FunctionalInterface
    public interface CandidatesReturnCallback {

        ProtocolType Invoke(final Name name) throws Phase.Error;

    }

    /// TODO:
    public boolean extendsProtocol(ProtocolType pd) {
        if(this.equals(pd))
            return true;
        boolean b = false;
        for (Name n : this.extend)
            b = ((ProtocolType) n.myDecl).extendsProtocol(pd) || b;
        return b;
    }

    public Case getCase(String name) {
        /** Search our own body first */
        for(final Statement pc : this)
            if (pc.toString().equals(name))
                return (Case) pc;

        /** This protocol type did not have the case */
        Case p = null;
        for (Name n : this.extend) {
            p = ((ProtocolType) n.myDecl).getCase(name);
            if (p != null)
                return p;
        }
        return null;
    }


}