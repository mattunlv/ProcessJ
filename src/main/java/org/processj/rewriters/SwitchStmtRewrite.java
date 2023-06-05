package org.processj.rewriters;

import java.util.HashSet;
import java.util.Set;

import org.processj.ast.*;
import org.processj.utilities.Log;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.PJMessage;
import org.processj.utilities.Visitor;
import org.processj.utilities.VisitorMessageNumber;

/**
 * This visitor is used to check for break statements that are required
 * when accessing the value (or values) associated with a protocol's
 * tag via the __dot__ org.processj.syntax, e.g., <tag>.<value>
 * 
 * @author ben
 */
public class SwitchStmtRewrite implements Visitor<Object> {
    
    private Set<String> protocols = new HashSet<>();
    private boolean inProtocol = false;
    private String ne = null;
    
    public SwitchStmtRewrite() {
        Log.logHeader("*******************************************");
        Log.logHeader("*  S W I T C H - S T M T   R E W R I T E  *");
        Log.logHeader("*******************************************");
    }
    
    @Override
    public Object visitLocalDecl(LocalDecl ld) {
        Log.log(ld, "Visiting a LocalDecl");

        Boolean flag = null;
        try {
            flag = (Boolean) ld.getType().visit(this);
        } catch (org.processj.Phase.Error error) {
            throw new RuntimeException(error);
        }
        // A visit to a local declaration might return 'null', so
        // we must check to avoid throwing an exception at runtime
        if(flag != null && flag)
            protocols.add(ld.toString());

        return null;

    }
    
    @Override
    public Object visitPrimitiveType(PrimitiveType pt) {
        Log.log(pt, "Visiting a PrimitiveType");
        
        return Boolean.FALSE;
    }
    
    @Override
    public Object visitNameExpr(NameExpr ne) {
        Log.log(ne, "Visiting a NameExpr");
        
        return ne.toString();
    }
    
    @Override
    public Object visitNamedType(NamedType nt) {
        Log.log(nt, "Visiting a NamedType");

        if(nt.getType() != null && nt.getType() instanceof ProtocolTypeDecl)
            return Boolean.TRUE;

        return Boolean.FALSE;

    }
    
    @Override
    public Object visitSwitchStat(SwitchStat st) {
        Log.log(st, "Visiting a SwitchStat");

        try {
            ne = (String) st.getEvaluationExpression().visit(this);
        } catch (org.processj.Phase.Error error) {
            throw new RuntimeException(error);
        }

        if (ne != null && protocols.contains(ne))
            inProtocol = true;
        
        for (SwitchGroup sg : st.switchBlocks())
            try {
                sg.visit(this);
            } catch (org.processj.Phase.Error error) {
                throw new RuntimeException(error);
            }

        inProtocol = false;
        
        return null;
    }
    
    @Override
    public Object visitSwitchGroup(SwitchGroup sg) {
        Log.log(sg, "Visiting a SwitchGroup");
        
        boolean hasBreak = false;
        Sequence<Statement> se = sg.getStatements();
        
        if (sg.getLabels().size() > 1 && inProtocol)
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(sg)
                    .addError(VisitorMessageNumber.REWRITE_1004)
                    .addArguments(ne)
                    .build());
        
        for (int i = 0; i < se.size() && !hasBreak; ++i) {
            Statement st = se.child(i);
            if (st instanceof BreakStat)
                hasBreak = true;
        }
        
        if (!hasBreak && inProtocol)
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(sg)
                    .addError(VisitorMessageNumber.REWRITE_1005)
                    .addArguments(ne)
                    .build());
        return null;
    }
}
