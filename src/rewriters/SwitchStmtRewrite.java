package rewriters;

import java.util.HashSet;
import java.util.Set;

import ast.BreakStat;
import ast.LocalDecl;
import ast.NameExpr;
import ast.NamedType;
import ast.PrimitiveType;
import ast.Sequence;
import ast.Statement;
import ast.SwitchGroup;
import ast.SwitchStat;
import utilities.Log;
import utilities.PJBugManager;
import utilities.PJMessage;
import utilities.Visitor;
import utilities.VisitorMessageNumber;

/**
 * This visitor is used to check for break statements that are required
 * when accessing the value (or values) associated with a protocol's
 * tag via the __dot__ syntax, e.g., <tag>.<value>
 * 
 * @author ben
 */
public class SwitchStmtRewrite extends Visitor<Object> {
    
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
        
        Boolean flag = (Boolean) ld.type().visit(this);
        // A visit to a local declaration might return 'null', so
        // we must check to avoid throwing an exception at runtime
        if (flag != null && flag)
            protocols.add(ld.var().name().getname());
        
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
        
        return ne.name().getname();
    }
    
    @Override
    public Object visitNamedType(NamedType nt) {
        Log.log(nt, "Visiting a NamedType");
        
        if (nt.type() != null && nt.type().isProtocolType())
            return Boolean.TRUE;
        
        return Boolean.FALSE;
    }
    
    @Override
    public Object visitSwitchStat(SwitchStat st) {
        Log.log(st, "Visiting a SwitchStat");
        
        ne = (String) st.expr().visit(this);
        
        if (ne != null && protocols.contains(ne))
            inProtocol = true;
        
        for (SwitchGroup sg : st.switchBlocks())
            sg.visit(this);
        
        inProtocol = false;
        
        return null;
    }
    
    @Override
    public Object visitSwitchGroup(SwitchGroup sg) {
        Log.log(sg, "Visiting a SwitchGroup");
        
        boolean hasBreak = false;
        Sequence<Statement> se = sg.statements();
        
        if (sg.labels().size() > 1 && inProtocol)
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
