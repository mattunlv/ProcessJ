package org.processj.compiler.ast;

import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

/**
 * Package Access (The ::) can be used for:
 *      Type Names      type_list, new mobile, binary_expression: expr (RHS), new RecordLiteral, new Protocol Literal
 *      Identifiers     NameExpr
 *      Invocations     Invocation
 *      Type Members? (For Protocol & Records)? NamedType
 *      NamedType is contained in CastExpr, ExternTypeDeclaration?, ArrayType, NewArrayExpression
 *      Note: Extern Types kind of end right then and there. They are coupled with Name
 */
public class Name extends AST {

    /// --------------
    /// Private Fields

    /**
     * <p>{@link String} value of the {@link Name}.</p>
     */
    private final String name;

    /**
     * <p>Integer value of the amount of brackets appended to the right of the {@link Name}.</p>
     */
    private int arrayDepth  ;

    // TODO: Remove me
    public AST myDecl;

    /// ------------
    /// Constructors

    public Name(final String name, final Name sibling) {
        super(new Token(name));

        this.name = ((name != null) ? name : "") + ((sibling != null) ? "." + sibling : "");

    }

    public Name(final String name) {
        this(name, null);
    }

    public Name(final Name name, final int arrayDepth) {
        super(name);

        this.name = name.toString();
        this.arrayDepth = arrayDepth;

    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns a literal {@link String} representation of the {@link Name} including a prefixed package name
     * if it is specified.</p>
     * @return Literal {@link String} representation of the {@link Name}.
     * @since 0.1.0
     */
    @Override
    public final String toString() {

        return this.name;

    }

    @Override
    public final int hashCode() {

        return this.name.hashCode();

    }
    
    /// ---
    /// AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link Name}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitName(Name)} method.</p>
     *
     * @param visitor The {@link Visitor} to dispatch.
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        visitor.visitName(this);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the integer value of the amount of square brackets appended to the right of the {@link Name}.</p>
     * @return Integer value of the amount of square brackets appended to the right of the {@link Name}.
     * @since 0.1.0
     */
    public final int getDepth() {

        return this.arrayDepth;

    }

    /**
     * <p>Returns the {@link String} value of the fully-qualified package name if the {@link Name} specifies it.</p>
     * @return {@link String} value of the fully-qualified package name if the {@link Name} specifies it.
     * @since 0.1.0
     */
    public final String getPackageName() {

        return "";

    }

}