package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.Token;

public class CharacterLiteral extends PrimitiveLiteralExpression {

    public CharacterLiteral(final String string) {
        super(new Token(string), PrimitiveLiteralExpression.CharKind);
    }

}
