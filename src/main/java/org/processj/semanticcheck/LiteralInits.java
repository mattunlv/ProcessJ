package org.processj.semanticcheck;

import org.processj.Phase;
import org.processj.ast.expression.ArrayLiteral;
import org.processj.utilities.*;
import org.processj.ast.*;

/**
 * 
 * @author Matt Pedersen
 * @version 02/16/2019
 * @since 1.2
 */
public class LiteralInits implements Visitor<Object> {
    Literal insideLiteral = null;

    public Object visitChannelReadExpr(ChannelReadExpr cre) {
        if (insideLiteral != null) {
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addAST(cre)
                    .addError(VisitorMessageNumber.SEMATIC_CHECKS_900)
//                    .addArguments(cre)
                    .build());
        }
        return null;
    }

    public Object visitArrayLiteral(ArrayLiteral al) throws Phase.Error {
        Literal insideLiteralOld = insideLiteral;
        insideLiteral = al;
        Visitor.super.visitArrayLiteral(al);
        insideLiteral = insideLiteralOld;
        return null;
    }

    public Object visitRecordLiteral(RecordLiteral rl) throws Phase.Error {
        Literal insideLiteralOld = insideLiteral;
        insideLiteral = rl;
        Visitor.super.visitRecordLiteral(rl);
        insideLiteral = insideLiteralOld;
        return null;
    }

    public Object visitProtocolLiteral(ProtocolLiteral pl) throws Phase.Error {
        Literal insideLiteralOld = insideLiteral;
        insideLiteral = pl;
        Visitor.super.visitProtocolLiteral(pl);
        insideLiteral = insideLiteralOld;
        return null;
    }

}