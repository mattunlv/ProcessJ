package org.processj.semanticcheck;

import org.processj.ast.alt.AltCase;
import org.processj.ast.alt.AltStat;
import org.processj.ast.expression.ArrayAccessExpr;
import org.processj.ast.expression.Assignment;
import org.processj.ast.expression.BinaryExpr;
import org.processj.ast.ChannelReadExpr;
import org.processj.ast.ExprStat;
import org.processj.ast.expression.Expression;
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
public class ReplicatedAlts implements Visitor<Object> {
    
    @Override
    public Object visitAltStat(AltStat as) {
        if ( as.isReplicated() ) {
            // Check for multiple loop control variables
            if ( as.initializationStatements()!=null ) {
                if ( as.initializationStatements().size()>1 )
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(as)
                            .addError(VisitorMessageNumber.SEMATIC_CHECKS_901)
                            .build());
            }
            
            // Check for any side effect
            for (int i=0; i<as.body().size(); ++i) {
                AltCase ac = (AltCase) as.body().child(i);
                if ( ac.isNestedAltStatement() )
                    try {
                        ac.visit(this);
                    } catch (org.processj.Phase.Error error) {
                        throw new RuntimeException(error);
                    }
                else {
                    Statement stat = ac.getGuard().getStatement();
                    if ( stat instanceof ExprStat ) {
                        Expression e = ((ExprStat) stat).expr();
                        ChannelReadExpr cr = null;
                        if ( e instanceof Assignment ) {
                            cr = (ChannelReadExpr) ((Assignment) e).right();
                            if ( cr.getExpression() instanceof ArrayAccessExpr ) {
                                ArrayAccessExpr ae = (ArrayAccessExpr) cr.getExpression();
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
