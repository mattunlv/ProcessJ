package rewriters;

import ast.AST;
import ast.AltCase;
import ast.AltStat;
import ast.Block;
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
        Sequence<AltCase> newBody = new Sequence<AltCase>();
        for (int i = 0; i < se.size(); ++i) {
            se.child(i).visit(this);
            if (se.child(i).isAltStat) {
                // Note: the actual alt statement has been wrapped in a block - take the 0th
                // child of that block.
                AltStat as2 = (AltStat) ((Block) se.child(i).stat()).stats().child(0);
                if (as2.isPri() && !as.isPri()) {
                    // 1006: no pri alt inside a non-pri alt
                    PJBugManager.INSTANCE.reportMessage(
                            new PJMessage.Builder()
                                .addAST(as)
                                .addError(VisitorMessageNumber.REWRITE_1006)
                                .build());
                }
                // now flatten by either merging se.child(i)'s children (if it is a nested alt)
                // or just re-append.
                // TODO: for now replicated alts just left.
                if (as2.isReplicated()) {
                    as2.dynamic = true;
                    newBody.append(se.child(i));
                } else
                    newBody.merge(as2.body());
                as.dynamic = as.dynamic || as2.isDynamic();
            } else
                newBody.append(se.child(i));
        }
        as.children[3] = newBody;
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
        if (ac.isAltStat())
            ac.stat().visit(this);
        return (AST) null;
    }
}
