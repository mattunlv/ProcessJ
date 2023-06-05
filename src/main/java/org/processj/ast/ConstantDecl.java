package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

// This ought to be fixed!!! Top-level constants should extend 'Type'
// instead of AST -- see 'visitCompilation' in CodeGeneratorJava.java
public class ConstantDecl extends Type implements VarDecl, DefineTopLevelDecl {

    /// --------------
    /// Private Fields

    private final Sequence<Modifier>    modifiers                   ;
    private final Name                  name                        ;
    private final Expression initializationExpression    ;
    private Type                        type                        ;
    private boolean                     isDeclaredNative            ;

    public ConstantDecl(final Sequence<Modifier> modifiers, final Type type, final Var var) {
        super(new AST[] { modifiers, type, var.getName(), var.getInitializationExpression() });
        this.modifiers                  = modifiers     ;
        this.name                       = var.getName()    ;
        this.initializationExpression   = var.getInitializationExpression()    ;
        this.isDeclaredNative           = false         ;
        this.type                       = type          ;
    }

    /// ----------------
    /// java.lang.Object

    /**
     * <p>Returns the {@link String} value of the {@link ConstantDecl}'s name.</p>
     * @return {@link String} value of the {@link ConstantDecl}'s name.
     * @since 0.1.0
     */
    @Override
    public String toString() {

        return this.name.toString();

    }

    public void setType(final Type type) {

        this.type           = type;
        this.children[1]    = type;

    }

    public Sequence<Modifier> modifiers() {
        return this.modifiers;
    }

    public final Type getType() {

        return this.type;

    }

    public final Name getName() {

        return this.name;

    }

    public final String getPackageName() {

        return this.name.getPackageName();

    }

    @Override
    public boolean typeEqual(Type other) {
        return false;
    }

    @Override
    public boolean typeEquivalent(Type other) {
        return false;
    }

    @Override
    public boolean typeAssignmentCompatible(Type other) {
        return false;
    }

    @Override
    public final <S> S visit(final Visitor<S> visitor)
            throws Phase.Error {

        return visitor.visitConstantDecl(this);

    }

    /**
     * <p>Returns a flag indicating if the {@link ConstantDecl} is declared native.</p>
     * @return flag indicating if the {@link ConstantDecl} is declared native.
     * @since 0.1.0
     */
    public final boolean isDeclaredNative() {

        // Iterate through the modifiers
        for(final Modifier modifier: this.modifiers()) {

            // Check for break
            if(this.isDeclaredNative) break;

            // Update the result
            this.isDeclaredNative = modifier.isNative();

        }

        // Return the result
        return this.isDeclaredNative;

    }

    /**
     * <p>Returns a flag indicating if the {@link ConstantDecl} is initialized.</p>
     * @return flag indicating if the {@link ConstantDecl} is initialized.
     * @since 0.1.0
     */
    public final boolean isInitialized() {

        return this.initializationExpression != null;

    }

    public final Expression getInitializationExpression() {

        return this.initializationExpression;

    }

}