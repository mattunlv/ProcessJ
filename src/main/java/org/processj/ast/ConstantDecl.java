package org.processj.ast;

import org.processj.utilities.Visitor;

// This ought to be fixed!!! Top-level constants should extend 'Type'
// instead of AST -- see 'visitCompilation' in CodeGeneratorJava.java
public class ConstantDecl extends AST implements VarDecl, DefineTopLevelDecl {

    /// --------------
    /// Private Fields

    private final Sequence<Modifier>    modifiers           ;
    private final Var                   variable            ;
    private boolean                     isDeclaredNative    ;

    public ConstantDecl(final Sequence<Modifier> modifiers, final Type type, final Var var) {
        super(new AST[] { modifiers, type, var });
        this.modifiers          = modifiers ;
        this.variable           = var       ;
        this.isDeclaredNative   = false     ;
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

        return (this.variable != null) ? this.variable.toString() : "";

    }

    public void setType(Type t) {
        children[1] = t;
    }

    public Sequence<Modifier> modifiers() {
        return this.modifiers;
    }

    public Type type() {
        return (Type) children[1];
    }

    public Var var() {
        return (Var) children[2];
    }

    public String name() {
        return var().name().getname();
    }

    public <S> S visit(Visitor<S> v) {
        return v.visitConstantDecl(this);
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

        return (this.variable != null) && this.variable.isInitialized();

    }

}