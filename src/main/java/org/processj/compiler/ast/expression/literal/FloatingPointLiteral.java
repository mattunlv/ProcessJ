package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.Token;

public class FloatingPointLiteral extends PrimitiveLiteralExpression {
    public FloatingPointLiteral(final String string) {
        super(new Token(string), PrimitiveLiteralExpression.FloatKind);
    }



}
