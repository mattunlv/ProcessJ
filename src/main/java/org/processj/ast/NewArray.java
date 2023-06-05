package org.processj.ast;

import org.processj.Phase;
import org.processj.ast.expression.ArrayLiteral;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Visitor;

public class NewArray extends Expression {

    private Type componentType;
    private final Sequence<Expression>  bracketExpressions      ;
    private final ArrayLiteral arrayLiteralExpression  ;
    private final Sequence<AST>         dims                    ;

    public NewArray(Type baseType, Sequence<Expression> dimsExpr,
                    Sequence<AST> dims, ArrayLiteral init) {
        super(baseType);
        nchildren = 4;
        children = new AST[] { baseType, dimsExpr, dims, init };
        this.componentType          = baseType  ;
        this.bracketExpressions     = dimsExpr  ;
        this.arrayLiteralExpression = init      ;
        this.dims                   = dims      ;
    }

    public final void setComponentType(final Type componentType) {

        this.componentType = componentType;
        this.children[0] = componentType;

    }

    public final int getEmptyDepth() {

        return (this.dims != null) ? this.dims.size() : 0;

    }

    public final int getDepth() {

        return ((this.bracketExpressions != null) ? this.bracketExpressions.size() : 0)
                + ((this.dims != null) ? this.dims.size() : 0);

    }

    public Type getComponentType() {
        return (Type) children[0];
    }

    public Sequence<Expression> getBracketExpressions() {
        return (Sequence<Expression>) children[1];
    }

    public Sequence<AST> dims() {
        return (Sequence<AST>) children[2];
    }

    public ArrayLiteral getInitializationExpression() {
        return (ArrayLiteral) children[3];
    }

    public String toString() {
        return "" + getComponentType() + " " + getBracketExpressions() + " " + dims();
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitNewArray(this);
    }

    public final boolean definesBracketExpressions() {

        return (this.bracketExpressions != null) && !this.bracketExpressions.isEmpty();

    }

    public final boolean isLiteralInitialized() {

        return this.arrayLiteralExpression != null;

    }

    public final boolean definesLiteralExpression() {

        return this.arrayLiteralExpression != null;

    }

    public final void forEachBracketExpression(final ExpressionCallback expressionCallback) throws Phase.Error {

        if((this.bracketExpressions != null) && (expressionCallback != null)) {

            // Call back the results
            for(final Expression expression: this.bracketExpressions)
                expressionCallback.Invoke(expression);

        }

    }

    @FunctionalInterface
    public interface ExpressionCallback {

        void Invoke(final Expression expression) throws Phase.Error;

    }

}