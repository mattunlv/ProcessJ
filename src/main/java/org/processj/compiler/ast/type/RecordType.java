package org.processj.compiler.ast.type;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.modifier.Modifiers;
import org.processj.compiler.ast.statement.Statement;
import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.ast.statement.declarative.DeclarativeContext;
import org.processj.compiler.ast.statement.declarative.Names;
import org.processj.compiler.ast.statement.declarative.RecordExtends;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * <p>Class that encapsulates a declaration of a {@link Context}'s record.</p>
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
public class RecordType extends BlockStatement implements Type, DeclarativeContext {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link RecordType}'s specified {@link Annotations}.</p>
     * @since 1.0.0
     * @see Annotations
     */
    private final Annotations annotations;

    /**
     * <p>The {@link RecordType}'s specified {@link Modifiers}.</p>
     * @since 1.0.0
     * @see Modifiers
     */
    private final Modifiers modifiers;

    /**
     * <p>The {@link RecordType}'s {@link Name}.</p>
     * @since 1.0.0
     * @see Name
     */
    private final Name name;

    /**
     * <p>The {@link RecordType}'s set of extend {@link RecordExtends}.</p>
     * @since 1.0.0
     * @see RecordExtends
     */
    private final RecordExtends extend              ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link RecordType} with the specified {@link Annotations}, {@link Modifiers},
     * {@link Name}, extend {@link RecordExtends}, & {@link Statement} body.</p>
     * @param annotations The {@link Annotations} specified with the {@link RecordType}.
     * @param modifiers The {@link Modifiers} specifying access privileges.
     * @param name The {@link Name} corresponding to the {@link RecordType}.
     * @param extend The set of implement {@link RecordExtends}.
     * @param body The {@link RecordType}'s {@link Statement} body.
     * @since 1.0.0
     * @see Annotations
     * @see Modifiers
     * @see Name
     * @see RecordExtends
     * @see Statement
     */
    public RecordType(final Modifiers modifiers,
                      final Name name,
                      final Names extend,
                      final Annotations annotations,
                      final Statement body) {
        super(body);

        this.annotations    = annotations   ;
        this.modifiers      = modifiers     ;
        this.name           = name          ;
        this.extend         = null        ;

    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} value of the {@link RecordType}'s {@link Name}.</p>
     * @return {@link String} value of the {@link RecordType}'s {@link Name}.
     * @since 1.0.0
     * @see Name
     * @see String
     */
    @Override
    public final String toString() {

        return this.name.toString();

    }

    /**
     * <p>Returns a flag indicating if the specified {@link Object} is an instance of {@link RecordType}
     * & both represent the same {@link Type} via {@link Name}.</p>
     * @param that The {@link Object} instance to check.
     * @return Flag indicating if the specified {@link Object} is an instance of {@link RecordType} & both
     *         represent the same {@link Type} via {@link Name}.
     * @since 1.0.0
     * @see Name
     * @see Object
     * @see Type
     */
    @Override
    public final boolean equals(final Object that) {

        return super.equals(that) && (that instanceof RecordType)
                && this.name.equals(((RecordType) that).getName());

    }

    /// ---
    /// AST

    /**
     * <p>Updates the {@link Visitor}'s {@link Context} & scope, and dispatches the {@link Visitor} to the
     * {@link Annotations} via {@link Annotations#accept(Visitor)}, the {@link Modifiers} via
     * {@link Modifiers#accept(Visitor)}, the set of extends {@link RecordExtends} via
     * {@link RecordExtends#accept(Visitor)}, the {@link RecordType} via
     * {@link Visitor#visitRecordTypeDeclaration(RecordType)}, & {@link Statement} children via
     * {@link Statement#accept(Visitor)} before updating the {@link org.processj.compiler.ast.Context} to the enclosing
     * {@link Context}.
     * @since 1.0.0
     * @see Annotations
     * @see Modifiers
     * @see Name
     * @see RecordExtends
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

        // Dispatch the Implement RecordExtends
        this.extend.accept(visitor);

        // Dispatch the RecordType Declaration
        visitor.visitRecordTypeDeclaration(this);

        // Dispatch the children
        this.getBody().accept(visitor);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    // α =T β ⇔ Record?(α) ∧ Record?(β) ∧ (name1 = name2)
    // We implement NAME EQUALITY not structural equality
    @Override
    public boolean isTypeEqualTo(Object that) {
        return this.equals(that);
    }

    // α∼T β ⇔ α =T β
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

    // α :=T β ⇔ α ∼T β ⇔ α =T β
    @Override
    public boolean isAssignmentCompatibleTo(Object that) {
        if(!(that instanceof RecordType))
            return false;
        RecordType rt = (RecordType) that;
        return rt.extendsRecord(this);
    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the internal {@link String} signature representing the {@link RecordType}.</p>
     * @return The internal {@link String} signature representing the {@link RecordType}.
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
     * <p>Returns the {@link RecordType}'s specified set of {@link Annotations}.</p>
     * @return The {@link RecordType}'s specified set of {@link Annotations}.
     * @since 1.0.0
     * @see String
     * @see Annotations
     */
    public final Annotations getAnnotations() {

        return this.annotations;

    }

    /**
     * <p>Returns the {@link RecordType}'s specified set of {@link Modifiers}.</p>
     * @return The {@link RecordType}'s specified set of {@link Modifiers}.
     * @since 1.0.0
     * @see String
     * @see Modifiers
     */
    public final Modifiers getModifiers() {

        return this.modifiers;

    }

    /**
     * <p>Returns the {@link RecordType}'s specified {@link Name}.</p>
     * @return The {@link RecordType}'s specified {@link Name}.
     * @since 1.0.0
     * @see Name
     */
    public final Name getName() {

        return this.name;

    }

    /**
     * <p>Returns the {@link RecordType}'s specified set of {@link RecordExtends}.</p>
     * @return The {@link RecordType}'s specified set of {@link RecordExtends}.
     * @since 1.0.0
     * @see String
     * @see RecordExtends
     */
    public final RecordExtends getRecordExtends() {

        return this.extend;

    }

    /**
     * <p>Iterates through each {@link Name} in the {@link RecordType}'s set of {@link RecordExtends},
     * calls back the {@link CandidatesReturnCallback} to retrieve a candidate {@link RecordType}, & maps it
     * to the corresponding {@link Name}.</p>
     * @param candidatesReturnCallback The {@link CandidatesReturnCallback} to invoke in order to retrieve the
     *        {@link RecordType}
     * @throws Phase.Error if the {@link CandidatesReturnCallback} failed.
     * @since 1.0.0
     * @see CandidatesReturnCallback
     * @see Name
     * @see RecordExtends
     * @see Phase.Error
     */
    public final void setCandidateForEachExtend(final CandidatesReturnCallback candidatesReturnCallback)
            throws Phase.Error {

        if(candidatesReturnCallback != null) {

            int index = 0;

            // Iterate through each extend Name
            for(final Name name: this.extend) {

                // Initialize a handle to the Type
                final RecordType candidate = candidatesReturnCallback.Invoke(name);

                // Place the mapping
                this.extend.insert(index, name, candidate);

            }

        }

    }

    /// ---------------------
    /// Functional Interfaces

    /**
     * <p>Functional Interface that provides a means of retrieving a {@link RecordType} corresponding to
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

        RecordType Invoke(final Name name) throws Phase.Error;

    }

    /// -------
    /// Classes

    /**
     * <p>Class that encapsulates a {@link RecordType}'s individual member.</p>
     * @author Jan B. Pedersen
     * @author Cabel Shrestha
     * @author Benjamin Cisneros
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 1.0.0
     * @see Statement
     * @see DeclarativeContext
     */
    public static class Member extends Statement implements DeclarativeContext {

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Name} corresponding to the {@link Member}.</p>
         * @since 1.0.0
         * @see Name
         */
        private final Name  name   ;

        /**
         * <p>The {@link Type} bound to the {@link Member}.</p>
         * @since 1.0.0
         * @see Type
         */
        private Type        type   ;

        /// -----------
        /// Constructor

        /**
         * <p>Initializes the {@link Member} to its' default state with the specified {@link Type} & {@link Name}.</p>
         * @param type the {@link Member}'s {@link Type}.
         * @param name The {@link Member}'s {@link Name}.
         * @since 1.0.0
         * @see Name
         * @see Type
         */
        public Member(final Type type, final Name name) {
            super(name);

            this.type = type;
            this.name = name;

        }

        /// ------
        /// Object

        /**
         * <p>Returns the {@link String} value of the {@link Member}'s {@link Name}.</p>
         * @return {@link String} value of the {@link Member}'s {@link Name}.
         * @since 1.0.0
         * @see Name
         * @see String
         */
        @Override
        public String toString() {

            return this.name.toString();

        }

        /// ---
        /// AST

        /**
         * <p>Updates the {@link Visitor}'s {@link Context}, and dispatches the {@link Visitor} to the bound {@link Type}
         * via {@link Type#accept(Visitor)}, {@link Name} via {@link Name#accept(Visitor)}, & {@link Member} via
         * {@link Visitor#visitRecordTypeDeclarationMember(Member)} before updating the {@link Context} to the enclosing
         * {@link Context}.
         * @since 1.0.0
         * @see Visitor
         * @see Phase.Error
         */
        @Override
        public final void accept(final Visitor visitor) throws Phase.Error {

            // Open the Context
            visitor.setContext(this.openContext(visitor.getContext()));

            // Dispatch the Type
            this.type.accept(visitor);

            // Dispatch the Name
            this.name.accept(visitor);

            // Dispatch the Member
            visitor.visitRecordTypeDeclarationMember(this);

            // Close the Context
            visitor.setContext(this.closeContext());

        }

        /// --------------
        /// Public Methods

        /**
         * <p>Returns the {@link Member}'s bound {@link Type}.</p>
         * @return The {@link Member}'s bound {@link Type}.
         * @since 1.0.0
         * @see Type
         */
        public final Type getType() {

            return this.type;

        }

        /**
         * <p>Returns the {@link Member}'s bound {@link Name}.</p>
         * @return The {@link Member}'s bound {@link Name}.
         * @since 1.0.0
         * @see Name
         */
        public final Name getName() {

            return this.name;

        }

        /**
         * <p>Mutates the {@link Member}'s bound {@link Type}.</p>
         * @param type The desired {@link Type} to bind to the {@link Member}.
         * @since 1.0.0
         * @see Name
         */
        public final void setType(final Type type) {

            this.type = type;

        }

    }

    // TODO:
    public Member getMember(String name) {
        for (Statement rm : getBody())
            if (rm.toString().equals(name))
                return (Member) rm;
        return null;
    }

    public boolean extendsRecord(RecordType rt) {
        if (this.equals(rt))
            return true;
        boolean b = false;
        for (Name n : getRecordExtends())
            b = ((RecordType) n.myDecl).extendsRecord(rt) || b;
        return b;
    }


}