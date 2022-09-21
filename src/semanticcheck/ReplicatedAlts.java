package semanticcheck;

import ast.AltCase;
import ast.AltStat;
import ast.ArrayAccessExpr;
import ast.Assignment;
import ast.BinaryExpr;
import ast.ChannelReadExpr;
import ast.ExprStat;
import ast.Expression;
import ast.Literal;
import ast.NameExpr;
import ast.Statement;
import utilities.PJBugManager;
import utilities.PJMessage;
import utilities.Visitor;
import utilities.VisitorMessageNumber;

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
                                if ( ae.index() instanceof Assignment ||
                                     ae.index() instanceof BinaryExpr ||
                                     ae.index() instanceof ChannelReadExpr )
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
