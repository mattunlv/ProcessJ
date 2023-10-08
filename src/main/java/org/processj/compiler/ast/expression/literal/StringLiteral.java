package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.Token;

public class StringLiteral extends PrimitiveLiteralExpression {
    public StringLiteral(final String string) {
        super(new Token(string), PrimitiveLiteralExpression.StringKind);
    }



}
