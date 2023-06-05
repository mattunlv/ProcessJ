package org.processj.semanticcheck;

import org.processj.ast.*;
import org.processj.utilities.PJBugManager;

import java.util.HashSet;

/**
 * This check is concerned with labelled break and continue statements like:
 *
 * break inner;
 * and
 * continue outer;
 * It performs a simple check that no labels are reused, and that any label
 * referred to is in scope.
 * Errors generated in this file:
 * XXX - Undefined break label '...' (or not in scope)
 * XXX - Undefined continue label '...' (or not in scope)
 * XXX - Label '...' already in use.
 */
public class LabeledBreakContinueCheck {
    HashSet<String> hs = new HashSet<String>();

    public void go(AST a) {
        if (a instanceof BreakStat) {
            BreakStat bs = (BreakStat) a;
            if (bs.target() != null) {
                String target = bs.target().toString();
                if(!hs.contains(target)) {

                    // TODO: Proper error
                    PJBugManager.ReportMessageAndExit("Undefined break label '" + target + "' (or not in scope).");

                }
            }
        } else if (a instanceof ContinueStat) {
            ContinueStat cs = (ContinueStat) a;
            if (cs.target() != null) {
                String target = cs.target().toString();
                if(!hs.contains(target)) {
                    // TODO: Proper Error
                    PJBugManager.ReportMessageAndExit("Undefined continue label '" + target + "' (or not in scope).");

                }
            }
        } else if (a instanceof Statement) {
            Statement s = (Statement) a;
            String label = s.getLabel();
            if (!label.equals("")) {
                // Check if we already have a label of that name?
                if(hs.contains(label)) {
                    // TODO: Proper Error
                    PJBugManager.ReportMessageAndExit("Label '" + label + "' already in use.");

                }
                hs.add(label);
            }
            for (int i = 0; i < s.nchildren; i++) {
                if (s.children[i] != null)
                    go(s.children[i]);
            }
            if (!label.equals(""))
                hs.remove(label);
        } else if (a instanceof Sequence) {
            Sequence s = (Sequence) a;
            for (int i = 0; i < s.size(); i++)
                if (s.child(i) != null)
                    go(s.child(i));
        } else {
            for (int i = 0; i < a.nchildren; i++) {
                if (a.children[i] != null)
                    go(a.children[i]);
            }
        }
    }
}