package org.processj.compiler.ast.expression.literal;

import org.processj.compiler.ast.Token;

public class NullLiteral extends PrimitiveLiteralExpression {

    public NullLiteral(final String string) {
        super(new Token(string), PrimitiveLiteralExpression.NullKind);


    }

}
