package rewriters;

import ast.AST;
import ast.AltCase;
import ast.AltStat;
import ast.ChannelReadExpr;
import ast.Invocation;
import ast.Sequence;
import ast.UnaryPostExpr;
import ast.UnaryPreExpr;
import utilities.Log;
import utilities.PJBugManager;
import utilities.PJMessage;
import utilities.Visitor;
import utilities.VisitorMessageNumber;

/**
 * @author ben
 */
public class AltStatRewrite extends Visitor<AST> {
    
    @Override
    public AST visitAltStat(AltStat as) {
        Log.log(as, "Visiting an AltStat");
        Sequence<AltCase> se = as.body();
        for (int i = 0; i < se.size(); ++i)
            se.child(i).visit(this);
        return (AST) null;
    }
    
    @Override
    public AST visitAltCase(AltCase ac) {
        Log.log(ac, "Visiting an AltCase");
        // Check precondition
        if (ac.precondition() != null) {
            if (ac.precondition().doesYield()) {
                if (ac.precondition() instanceof ChannelReadExpr)
                    PJBugManager.INSTANCE.reportMessage(
                            new PJMessage.Builder()
                            .addAST(ac)
                            .addError(VisitorMessageNumber.REWRITE_1000)
                            .build());
                else
                    ; // TODO: Throw an error for expressions that yields??
            }
            if (ac.precondition() instanceof UnaryPreExpr)
                PJBugManager.INSTANCE.reportMessage(
                        new PJMessage.Builder()
                        .addAST(ac)
                        .addError(VisitorMessageNumber.REWRITE_1001)
                        .build());
            if (ac.precondition() instanceof UnaryPostExpr)
                PJBugManager.INSTANCE.reportMessage(
                        new PJMessage.Builder()
                        .addAST(ac)
                        .addError(VisitorMessageNumber.REWRITE_1002)
                        .build());
            if (ac.precondition() instanceof Invocation)
                PJBugManager.INSTANCE.reportMessage(
                        new PJMessage.Builder()
                        .addAST(ac)
                        .addError(VisitorMessageNumber.REWRITE_1003)
                        .build());
        }
        return (AST) null;
    }
}
