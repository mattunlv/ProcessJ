package org.processj.compiler.ast.type;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class ParameterDeclaration extends Type {

    /// --------------
    /// Private Fields

    private final boolean isConstant;
    private Name name   ;
    private Type type   ;

    /// ------------
    /// Constructors

    public ParameterDeclaration(final Type type, final Name name, boolean isConstant) {
        super(new AST[] { type, name });

        this.isConstant = isConstant;
        this.type       = type      ;
        this.name       = name      ;

    }

    @Override
    public final String toString() {

        return this.name.toString();

    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitParameterDeclaration(this);

    }

    public final boolean isConstant() {

        return isConstant;

    }

    public final Type getType() {

        return this.type;

    }

    public final String getPackageName() {

        return this.name.getPackageName();

    }

    public final Name getName() {

        return this.name;

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

    public final int getDepth() {

        return this.name.getDepth();

    }

    public final void setType(Type t) {

        this.type           = t;
        this.children[0]    = t;

    }

    public final void setName(final Name name) {

        this.name           = name;
        this.children[0]    = name;

    }

}