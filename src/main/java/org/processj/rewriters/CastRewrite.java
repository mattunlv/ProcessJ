package org.processj.rewriters;

import org.processj.utilities.Log;
import org.processj.utilities.Visitor;
import org.processj.ast.*;

public class CastRewrite extends Visitor<AST> {
    
    public CastRewrite() {
        Log.logHeader("****************************************");
        Log.logHeader("*        C A S T   R E W R I T E       *");
        Log.logHeader("****************************************");
    }
    
    // DONE
    @Override
    public AST visitChannelWriteStat(ChannelWriteStat cs) {
        Type chanBaseType;
        if (cs.channel().type instanceof ChannelEndType)
            chanBaseType = ((ChannelEndType)cs.channel().type).getComponentType();
        else
            chanBaseType = ((ChannelType)cs.channel().type).getComponentType();
        Type exprType = cs.expr().type;
        if (exprType != null && !exprType.typeEqual(chanBaseType)) {
            // replace the expression in the channel writer by a new cast expression,
            // that is:    write ( <expr> ) becomes
            //             write ( (...) <expr> )
            CastExpr ce = new CastExpr(chanBaseType, cs.expr());
            cs.children[1] = ce;
        }
        return null;
    }
}
