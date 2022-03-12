/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syntax;

import ast.Token;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ben
 */
public class TreeCtx {
    
    private List<NodeCtx> children;
    
    public <T extends NodeCtx> T addAnyChild(T n) {
        if (children == null)
            children = new ArrayList<>();
        children.add(n);
        return n;
    }
    
    public NodeCtx get(int idx) {
        if (size() > 0)
            return (NodeCtx) children.get(idx);
        return null;
    }
    
    public NodeCtx addChild(NodeCtx n) {
        return addAnyChild(n);
    }
    
    public void addTerminalNode(Token termToken, NodeCtx rule) {
        TerminalCtx ctx = termToken.asTerminalContext();
        if (rule != null)
            rule.add(ctx);
        else
            addAnyChild(ctx);
    }
    
    public void addErrorNode(Token badToken, NodeCtx rule) {
        ErrorCtx ctx = new ErrorCtx(badToken);
        if (rule != null)
            rule.add(ctx);
        else
            addAnyChild(ctx);
    }
    
    public RuleCtx addRuleNode(Token ruleToken) {
        RuleCtx ctx = ruleToken.asRuleContext();
        return addAnyChild(ctx);
    }
    
    public int size() {
        return children.size();
    }
    
    public boolean hasChildren() {
        return (size() > 0);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (hasChildren()) {
            sb.append('(');
            int len = size();
            for (int i = 0; i < len; ++i)
                sb.append(children.get(i).printLISPformat());
            sb.append(')');
        } else
            sb.append("(...)");
        return sb.toString();
    }
}
