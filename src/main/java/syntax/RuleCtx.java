package syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ast.Token;

/**
 * A syntax node produce by the Parser.
 * 
 * @author ben
 */
public class RuleCtx extends NodeCtx {
    
    // This list is used to track all tokens and rules associated
    // with this node context. This should be empty for special tokens
    // such parenthesis, etc., as we don't need to track the details
    // about how we parse them.
    private List<NodeCtx> children;
    
    public RuleCtx(Token root) {
        children = new ArrayList<>();
        set(0, root);
    }
    
    public RuleCtx() {
        children = Collections.emptyList();
    }
    
    @Override
    public int size() {
        return children.size();
    }
    
    @Override
    public NodeCtx get(int idx) {
        if (idx < size())
            return (NodeCtx) children.get(idx);
        return (NodeCtx) null;
    }
    
    @Override
    public Token getRoot() {
        if (size() > 0)
            return (Token) children.get(0);
        return null;
    }
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public NodeCtx add(NodeCtx n) {
        return set(size(), n);
    }
    
    @Override
    public NodeCtx remove(int idx) {
        if (idx < 1)
            throw new AssertionError("Can't remove the root from the list");
        return (NodeCtx) children.remove(idx);
    }
    
    @Override
    public NodeCtx set(int index, NodeCtx n) {
        if (children == null)
            throw new AssertionError("Can't insert a node into an EMPTY list");
        final int count = size();
        if (index >= count)
            for (int i = count; i <= index; ++i)
                children.add(null);
        children.set(index, n);
        return n;
    }
    
    @Override
    public NodeCtx getLeaf() {
        int index = children.size() - 1;
        return (NodeCtx) children.get(index);
    }
    
    @Override
    public RuleCtx asRuleContext() {
        return this;
    }
}
