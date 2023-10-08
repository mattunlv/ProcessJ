package org.processj.compiler.ast.expression.constructing;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.expression.Expression;
import org.processj.compiler.ast.expression.literal.ArrayLiteralExpression;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.ast.type.ArrayType;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class NewArrayExpression extends Expression {

    private final Sequence<Expression> bracketExpressions      ;
    private final ArrayLiteralExpression arrayLiteralExpression  ;
    private final int                   depth                   ;
    private Type componentType           ;

    public NewArrayExpression(final Type type, final Sequence<Expression> bracketExpressions,
                              final Sequence<AST> dims, final ArrayLiteralExpression arrayLiteralExpression) {
        super(new AST[] { bracketExpressions, arrayLiteralExpression });

        this.componentType          = type               ;
        this.bracketExpressions     = bracketExpressions                    ;
        this.arrayLiteralExpression = arrayLiteralExpression                ;
        this.depth                  = ((dims != null) ? dims.size() : 0)    ;

    }

    public NewArrayExpression(final Type type, final ArrayLiteralExpression arrayLiteralExpression) {
        super(new AST[] { new Sequence<>(), arrayLiteralExpression });

        this.componentType          = type                                          ;
        this.bracketExpressions     = new Sequence<>()                                                  ;
        this.arrayLiteralExpression = arrayLiteralExpression                                            ;
        this.depth                  = (type instanceof ArrayType) ? ((ArrayType) type).getDepth() : 0   ;

    }

    public final void setComponentType(final Type componentType) {

        this.componentType = componentType;

    }

    public final int getDepth() {

        return ((this.bracketExpressions != null)
                ? this.bracketExpressions.size() : 0) + this.depth;

    }

    public Type getComponentType() {
        return this.componentType;
    }

    public Sequence<Expression> getBracketExpressions() {
        return this.bracketExpressions;
    }

    public Sequence<Expression> dims() {
        return this.bracketExpressions;
    }

    public ArrayLiteralExpression getInitializationExpression() {
        return this.arrayLiteralExpression;
    }

    public String toString() {
        return "" + getComponentType() + " " + getBracketExpressions() + " " + dims();
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitNewArrayExpression(this);
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