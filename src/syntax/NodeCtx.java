package syntax;

import ast.Token;

/**
 * An abstract base class for nodes in the context syntax tree.
 * 
 * @author ben
 */
public abstract class NodeCtx {
    
    public abstract int size();
    
    public abstract NodeCtx get(int idx);
    
    public abstract Token getRoot();
    
    public String getText() {
        return getRoot().getText();
    }
    
    public int getStartLine() {
        return getRoot().getStartLine();
    }

    public int getStartColumn() {
        return getRoot().getStartColumn();
    }
    
    public boolean isEmpty() {
        return false;
    }
    
    public int children() {
        int num = size();
        if (num > 1)
            return num - 1;
        return 0;
    }
    
    public boolean hasChildren() {
        return (children() > 0);
    }
    
    public String getRootText() {
        Token root = getRoot();
        return root.getText();
    }
    
    public RuleCtx asRuleContext() {
        throw new AssertionError("asRuleContext() is not supported for this type of node");
    }
    
    public TerminalCtx asTerminalContext() {
        throw new AssertionError("asTerminalContext() is not supported for this type of node");
    }
    
    public NodeCtx add(NodeCtx n) {
        throw new AssertionError("add() is not supported for this type of node");
    }
    
    public NodeCtx remove(int index) {
        throw new AssertionError("remove() is not supported for this type of node");
    }
    
    public NodeCtx set(int index, NodeCtx n) {
        throw new AssertionError("set() is not supported for this type of node");
    }
    
    public void addChildrenOf(NodeCtx n) {
        for (int i = 1; i < n.size(); ++i)
            add(n.get(i));
    }
    
    public NodeCtx getLeaf() {
        throw new AssertionError("getLeaf() is not supported for this type of node");
    }
    
    public void printContext() {
        Token root = getRoot();
        System.out.print(root.getText() + " ");
        if (children() > 0) {
            int count = size();
            for (int i = 1; i < count; ++i)
                System.out.print(get(i).getText() + " ");
        }
    }
    
    public String printLISPformat() {
        StringBuilder sb = new StringBuilder();
        Token root = getRoot();
        sb.append('(');
        sb.append(root.getText());
        sb.append(' ');
        if (children() > 0) {
            int count = size();
            for (int i = 1; i < count; ++i) {
                sb.append(get(i).getText());
                sb.append(' ');
            }
        }
        sb.append(')');
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Token root = getRoot();
        sb.append(root.getText());
        sb.append(' ');
        if (children() > 0) {
            int count = size();
            for (int i = 1; i < count; ++i) {
                sb.append(get(i).getText());
                sb.append(' ');
            }
        }
        return sb.toString();
    }
}
