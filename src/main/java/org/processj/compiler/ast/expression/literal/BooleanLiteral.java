package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.Token;

public class BooleanLiteral extends PrimitiveLiteralExpression {
    public BooleanLiteral(final String string) {
        super(new Token(string), PrimitiveLiteralExpression.BooleanKind);
    }



}
