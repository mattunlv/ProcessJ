package org.processj.semanticcheck;

import org.processj.ast.AltCase;
import org.processj.ast.AltStat;
import org.processj.ast.Block;
import org.processj.ast.DoStat;
import org.processj.ast.ForStat;
import org.processj.ast.IfStat;
import org.processj.ast.ReturnStat;
import org.processj.ast.Sequence;
import org.processj.ast.Statement;
import org.processj.ast.WhileStat;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.PJMessage;
import org.processj.utilities.Visitor;
import org.processj.utilities.VisitorMessageNumber;

public class AltReturnCheck extends Visitor<Object> {
    
    boolean inAltStat = false;
    
    @Override
    public Object visitAltStat(AltStat as) {
        inAltStat = true;
        Sequence<AltCase> se = as.body();
        if (se != null)
            for (AltCase ac : se)
                ac.visit(this);
        return null;
    }
    
    @Override
    public Object visitAltCase(AltCase ac) {
        if (ac.stat() != null)
            ac.stat().visit(this);
        return null;
    }
    
    @Override
    public Object visitReturnStat(ReturnStat rs) {
        if (inAltStat) {
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(rs)
                    .addError(VisitorMessageNumber.SEMATIC_CHECKS_903)
                    .build());
        }
        return null;
    }
    
    @Override
    public Object visitBlock(Block b) {
        for (Statement st : b.stats())
            st.visit(this);
        return null;
    }
    
    @Override
    public Object visitWhileStat(WhileStat ws) {
        if (ws.stat() != null)
            ws.stat().visit(this);
        return null;
    }
    
    @Override
    public Object visitDoStat(DoStat ds) {
        if (ds.stat() != null)
            ds.stat().visit(this);
        return null;
    }
    
    @Override
    public Object visitForStat(ForStat fs) {
        if (fs.stats() != null)
            fs.stats().visit(this);
        return null;
    }
    
    @Override
    public Object visitIfStat(IfStat is) {
        if (is.thenpart() != null)
            is.thenpart().visit(this);
        if (is.elsepart() != null)
            is.elsepart().visit(this);
        return null;
    }
}
