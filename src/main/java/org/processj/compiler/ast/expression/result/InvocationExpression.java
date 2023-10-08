package org.processj.compiler.ast.expression.result;

import org.processj.compiler.ast.*;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.type.ProcedureType;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

import java.util.HashMap;
import java.util.Map;

public class InvocationExpression extends Expression {

    public ProcedureType targetProc = null;
    private final Name name            ;
    private final Expression            target          ;
    private final Sequence<Expression> parameters      ;
    private final Map<String, Type>     parameterTypes  ;
    private Type                        returnType      ;
    private SymbolMap candidates      ;
    public boolean ignore = false; // This is used to ignore invocations for 'labels' and 'gotos' in the org.processj.codegen phase.

    public InvocationExpression(final Expression target, final Name name, final Sequence<Expression> parameters) {
        super(new AST[] { target, name, parameters });
        this.name           = name              ;
        this.target         = target            ;
        this.parameters     = parameters        ;
        this.parameterTypes = new HashMap<>()   ;
        this.returnType     = null              ;
        this.candidates     = null              ;
    }
    
    public InvocationExpression(final Expression target, final Name name,
                                final Sequence<Expression> parameters, final boolean ignore) {
    	this(target, name, parameters);
    	this.ignore = ignore;
    }

    public InvocationExpression(final Name name,
                                final Sequence<Expression> parameters, final boolean ignore) {
        this(null, name, parameters);
        this.ignore = ignore;
    }

    public String toString() {
        String s = (getTarget() == null ? "" : getTarget() + ".") + getProcedureName()
            + "(";
        for (int i = 0; i < getParameterExpressions().size(); i++) {
            s += getParameterExpressions().child(i);
            if (i < getParameterExpressions().size() - 1)
                s += ",";
        }
        s += ")";
        return s;
    }

    /**
     * <p>Returns a flag indicating if the {@link InvocationExpression} has a target.</p>
     * @return flag indicating if the {@link InvocationExpression} has a target.
     * @since 0.1.0
     */
    public final boolean definesTarget() {

        return this.target != null;

    }

    public final SymbolMap getCandidates() {

        return this.candidates;

    }

    public final void setCandidates(final SymbolMap symbolMap) {

        this.candidates = symbolMap;

    }

    public final String getProcedureName() {

        return this.name.toString();

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

    public final Name getName() {

        return this.name;

    }

    public final void setReturnType(final Type type) {

        this.returnType = type;

    }

    public final Type getTypeForParameter(final int index) {

        return (Type) this.parameterTypes.entrySet().toArray()[index];

    }

    public Sequence<Expression> getParameterExpressions() {
        return parameters;
    }

    public final Map<String, Type> getParameterTypes() {

        return this.parameterTypes;

    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitInvocationExpression(this);
    }

    public final void setTypeForEachParameter(final TypeReturnCallback typeReturnCallback) throws Phase.Error {

        if((this.parameters != null) && (typeReturnCallback != null)) {

            // Iterate through each Implement Name
            for(final Expression parameter: this.parameters) {

                // Initialize a handle to the Type
                final Type type = typeReturnCallback.Invoke(parameter);

                // Place the mapping
                this.parameterTypes.put(parameter.toString(), type);

                // Assert the Parameter Declaration hase a Type bound
                parameter.setType(type);

            }

        }

    }

    @FunctionalInterface
    public interface TypeReturnCallback {

        Type Invoke(final Expression expression) throws Phase.Error;

    }

}