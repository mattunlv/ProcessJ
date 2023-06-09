package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.ast.expression.ArrayLiteral;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.phases.phase.Visitor;

public class NewArray extends Expression {

    private final Sequence<Expression>  bracketExpressions      ;
    private final ArrayLiteral          arrayLiteralExpression  ;
    private final int                   depth                   ;
    private Type                        componentType           ;

    public NewArray(final Type type, final Sequence<Expression> bracketExpressions,
                    final Sequence<AST> dims, final ArrayLiteral arrayLiteralExpression) {
        super(new AST[] { type.getComponentType(), bracketExpressions, arrayLiteralExpression });

        this.componentType          = (Type) this.children[0]               ;
        this.bracketExpressions     = bracketExpressions                    ;
        this.arrayLiteralExpression = arrayLiteralExpression                ;
        this.depth                  = ((dims != null) ? dims.size() : 0)    ;

    }

    public NewArray(final Type type, final ArrayLiteral arrayLiteralExpression) {
        super(new AST[] { type.getComponentType(), new Sequence<>(), arrayLiteralExpression });

        this.componentType          = (Type) this.children[0]                                           ;
        this.bracketExpressions     = (Sequence<Expression>) this.children[1]                           ;
        this.arrayLiteralExpression = arrayLiteralExpression                                            ;
        this.depth                  = (type instanceof ArrayType) ? ((ArrayType) type).getDepth() : 0   ;

    }

    public final void setComponentType(final Type componentType) {

        this.componentType  = componentType.getComponentType();
        this.children[0]    = componentType.getComponentType();

    }

    public final int getDepth() {

        return ((this.bracketExpressions != null)
                ? this.bracketExpressions.size() : 0) + this.depth;

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