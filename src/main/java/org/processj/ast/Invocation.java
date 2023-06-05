package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class Invocation extends Expression {

    public ProcTypeDecl targetProc = null;
    private final Name                  name            ;
    private final Expression            target          ;
    private final Sequence<Expression>  parameters      ;
    private final Sequence<Type>        parameterTypes  ;
    private Type                        returnType      ;
    public boolean ignore = false; // This is used to ignore invocations for 'labels' and 'gotos' in the org.processj.codegen phase.

    public Invocation(final Expression target, final Name name, final Sequence<Expression> parameters) {
        super(name);
        nchildren = 3;
        children            = new AST[] { target, name, parameters };
        this.name           = name              ;
        this.target         = target            ;
        this.parameters     = parameters        ;
        this.parameterTypes = new Sequence<>()  ;
        this.returnType     = null              ;
    }
    
    public Invocation(final Expression target, final Name name,
                      final Sequence<Expression> parameters, final boolean ignore) {
    	this(target, name, parameters);
    	this.ignore = ignore;
    }

    public String toString() {
        String s = (getTarget() == null ? "" : getTarget() + ".") + getProcedureName()
            + "(";
        for (int i = 0; i < getParameters().size(); i++) {
            s += getParameters().child(i);
            if (i < getParameters().size() - 1)
                s += ",";
        }
        s += ")";
        return s;
    }

    /**
     * <p>Returns a flag indicating if the {@link Invocation} has a target.</p>
     * @return flag indicating if the {@link Invocation} has a target.
     * @since 0.1.0
     */
    public final boolean definesTarget() {

        return this.target != null;

    }

    public final String getProcedureName() {

        return this.name.getName();

    }

    public final Expression getTarget() {

        return this.target;

    }

    public final int getParameterCount() {

        return this.parameters.size();

    }

    public final Type getReturnType() {

        return this.returnType;

    }

    public final void setReturnType(final Type type) {

        this.returnType = type;

    }

    public final Type getTypeForParameter(final int index) {

        // Return the result if we can access it
        return ((this.parameterTypes != null) && (index < this.parameterTypes.size()))
                ? this.parameterTypes.child(index) : null;

    }

    public Sequence<Expression> getParameters() {
        return (Sequence<Expression>) children[2];
    }

    public final Sequence<Type> getParameterTypes() {

        return this.parameterTypes;

    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitInvocation(this);
    }

    public final void setTypeForEachParameter(final TypeReturnCallback typeReturnCallback) {

        if((this.parameters != null) && (typeReturnCallback != null)) {

            // Clear the implement types
            this.parameterTypes.clear();

            // Append the results
            this.parameters.forEach(expression -> {

                Type type;

                try {

                    type = typeReturnCallback.Invoke(expression);

                } catch (final Phase.Error phaseError) {

                    type = new ErrorType();

                }

                this.parameterTypes.append(type);

            });

        }

    }

    @FunctionalInterface
    public interface TypeReturnCallback {

        Type Invoke(final Expression name) throws Phase.Error;

    }

}