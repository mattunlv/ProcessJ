package org.processj.semanticcheck;

import org.processj.ast.AltCase;
import org.processj.ast.AltStat;
import org.processj.ast.ArrayAccessExpr;
import org.processj.ast.Assignment;
import org.processj.ast.BinaryExpr;
import org.processj.ast.ChannelReadExpr;
import org.processj.ast.ExprStat;
import org.processj.ast.Expression;
import org.processj.ast.Statement;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.PJMessage;
import org.processj.utilities.Visitor;
import org.processj.utilities.VisitorMessageNumber;

/**
 * Checks for multiple loop control variables and for any side effect
 * in an array expression in replicated alts.
 * 
 * @author ben
 */
public class ReplicatedAlts extends Visitor<Object> {
    
    @Override
    public Object visitAltStat(AltStat as) {
        if ( as.isDynamic() || as.isReplicated() ) {
            // Check for multiple loop control variables
            if ( as.init()!=null ) {
                if ( as.init().size()>1 )
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(as)
                            .addError(VisitorMessageNumber.SEMATIC_CHECKS_901)
                            .build());
            }
            
            // Check for any side effect
            for (int i=0; i<as.body().size(); ++i) {
                AltCase ac = (AltCase) as.body().child(i);
                if ( ac.isAltStat )
                    ac.visit(this);
                else {
                    Statement stat = ac.guard().guard();
                    if ( stat instanceof ExprStat ) {
                        Expression e = ((ExprStat) stat).expr();
                        ChannelReadExpr cr = null;
                        if ( e instanceof Assignment ) {
                            cr = (ChannelReadExpr) ((Assignment) e).right();
                            if ( cr.channel() instanceof ArrayAccessExpr ) {
                                ArrayAccessExpr ae = (ArrayAccessExpr) cr.channel();
                                if ( ae.indexExpression() instanceof Assignment ||
                                     ae.indexExpression() instanceof BinaryExpr ||
                                     ae.indexExpression() instanceof ChannelReadExpr )
                                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                            .addAST(as)
                                            .addError(VisitorMessageNumber.SEMATIC_CHECKS_902)
                                            .build());
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
}
