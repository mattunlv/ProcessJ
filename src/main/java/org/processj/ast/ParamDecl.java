package org.processj.ast;

import org.processj.Phase;
import org.processj.utilities.Visitor;

public class ParamDecl extends AST implements VarDecl {

    /// --------------
    /// Private Fields

    private final boolean isConstant;
    private final Name name ;
    private Type type;

    /// ------------
    /// Constructors

    public ParamDecl(final Type type, final Name name, boolean isConstant) {
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

        return visitor.visitParamDecl(this);

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

    public final String getName() {

        return this.name.getName();

    }

    public final int getDepth() {

        return this.name.getDepth();

    }

    public final void setType(Type t) {

        this.type           = t;
        this.children[0]    = t;

    }

}