package org.processj.rewriters;

import org.processj.ast.AST;
import org.processj.ast.alt.AltCase;
import org.processj.ast.alt.AltStat;
import org.processj.ast.Block;
import org.processj.ast.ChannelReadExpr;
import org.processj.ast.Invocation;
import org.processj.ast.Sequence;
import org.processj.ast.UnaryPostExpr;
import org.processj.ast.UnaryPreExpr;
import org.processj.utilities.Log;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.PJMessage;
import org.processj.utilities.Visitor;
import org.processj.utilities.VisitorMessageNumber;

/**
 * @author ben
 */
public class AltStatRewrite implements Visitor<AST> {

    @Override
    public AST visitAltStat(AltStat as) {
        Log.log(as, "Visiting an AltStat");
        Sequence<AltCase> se = as.body();
        Sequence<AltCase> newBody = new Sequence<AltCase>();
        for (int i = 0; i < se.size(); ++i) {
            try {
                se.child(i).visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }
            if (se.child(i).isNestedAltStatement()) {
                // Note: the actual alt statement has been wrapped in a block - take the 0th
                // child of that block.
                AltStat as2 = (AltStat) ((Block) se.child(i).getStatement()).stats().child(0);
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
                if(as2.isReplicated()) {
                    as2.setReplicated(true);
                    newBody.append(se.child(i));
                } else
                    newBody.merge(as2.body());

                as.setReplicated(as.isReplicated() || as2.isReplicated());
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
        if (ac.definesPrecondition()) {
            if (ac.getPreconditionExpression().doesYield()) {
                if (ac.getPreconditionExpression() instanceof ChannelReadExpr)
                    PJBugManager.INSTANCE.reportMessage(
                            new PJMessage.Builder()
                                .addAST(ac)
                                .addError(VisitorMessageNumber.REWRITE_1000)
                                .build());
                else
                    ; // TODO: Throw an error for expressions that yields??
            }
            if (ac.getPreconditionExpression() instanceof UnaryPreExpr)
                PJBugManager.INSTANCE.reportMessage(
                        new PJMessage.Builder()
                            .addAST(ac)
                            .addError(VisitorMessageNumber.REWRITE_1001)
                            .build());
            if (ac.getPreconditionExpression() instanceof UnaryPostExpr)
                PJBugManager.INSTANCE.reportMessage(
                        new PJMessage.Builder()
                            .addAST(ac)
                            .addError(VisitorMessageNumber.REWRITE_1002)
                            .build());
            if (ac.getPreconditionExpression() instanceof Invocation)
                PJBugManager.INSTANCE.reportMessage(
                        new PJMessage.Builder()
                            .addAST(ac)
                            .addError(VisitorMessageNumber.REWRITE_1003)
                            .build());
        }
        if(ac.isNestedAltStatement())
            try {
                ac.getStatement().visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }
        return null;
    }
}
