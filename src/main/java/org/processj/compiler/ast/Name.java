package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

/**
 * Package Access (The ::) can be used for:
 *      Type Names      type_list, new mobile, binary_expression: expr (RHS), new RecordLiteral, new Protocol Literal
 *      Identifiers     NameExpr
 *      Invocations     Invocation
 *      Type Members? (For Protocol & Records)? NamedType
 *
 *      NamedType is contained in CastExpr, ExternTypeDeclaration?, ArrayType, NewArrayExpression
 *
 *      Note: Extern Types kind of end right then and there. They are coupled with Name
 */
public class Name extends AST {

    /// --------------
    /// Private Fields

    /**
     * <p>{@link String} value of the fully-qualified package name.</p>
     */
    private final String    packageName ;

    /**
     * <p>{@link String} value of the {@link Name}.</p>
     */
    private String          name        ;

    /**
     * <p>Integer value of the amount of brackets appended to the right of the {@link Name}.</p>
     */
    private int             arrayDepth  ;

    // TODO: Remove me
    public AST myDecl;

    /// ------------
    /// Constructors

    public Name(final String name) {
        super(0, 0);

        this.name           = (name != null) ? name : ""    ;
        this.packageName    = ""                            ;
        this.arrayDepth     = 0                             ;

    }

    public Name(final Name name, final int arrayDepth) {
        super(name.line, name.charBegin);

        this.name           = name.getName()        ;
        this.packageName    = name.getPackageName() ;
        this.arrayDepth     = arrayDepth            ;

    }

    public Name(final Token token, final Sequence<Name> packageName) {
        super(token.line, token.start);

        this.name           = token.lexeme                                   ;
        this.packageName    = packageName.synthesizeStringWith(".") ;
        this.arrayDepth     = 0                                              ;

    }

    public Name(final Token token) {
        super(token.line, token.start);

        this.name           = token.lexeme  ;
        this.packageName    = ""            ;
        this.arrayDepth     = 0             ;

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

        return ((this.specifiesPackage()) ? this.packageName + "::" : "") + this.name;

    }
    
    /// --------------------
    /// org.processj.ast.AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link Name}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitName(Name)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @return Type result of the visitation.
     * @param <S> Parametric type parameter.
     */
    @Override
    public final <S> S visit(Visitor<S> visitor) throws Phase.Error {

        return visitor.visitName(this);

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link Name} is prefixed with a fully-qualified package name.</p>
     * @return Flag indicating if the {@link Name} is prefixed with a fully-qualified package name.
     * @since 0.1.0
     */
    public final boolean specifiesPackage() {

        return !this.packageName.isEmpty() && !this.packageName.isBlank();

    }

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

        return this.packageName;

    }

    /**
     * <p>Returns the {@link String} value of the name; the {@link String} value is equal to {@link Name#toString()}
     * if a fully-qualified package name is not specified.</p>
     * @return {@link String} value of the {@link Name}.
     * @since 0.1.0
     */
    public final String getName() {

        return this.name;

    }

    public final void setDepth(final int depth) {

        this.arrayDepth = depth;

    }

    public final void setName(final String name) {

        this.name = (name != null) ? name : "";

    }

}