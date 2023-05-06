package semanticcheck;

import ast.AltCase;
import ast.AltStat;
import ast.Block;
import ast.DoStat;
import ast.ForStat;
import ast.IfStat;
import ast.ReturnStat;
import ast.Sequence;
import ast.Statement;
import ast.WhileStat;
import utilities.PJBugManager;
import utilities.PJMessage;
import utilities.Visitor;
import utilities.VisitorMessageNumber;

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
