package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.Token;

public class IntegerLiteral extends PrimitiveLiteralExpression {
    public IntegerLiteral(final String string) {
        super(new Token(string), PrimitiveLiteralExpression.FloatKind);
    }

}
