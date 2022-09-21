package rewriters;

import java.util.LinkedHashSet;
import java.util.Set;

import ast.AST;
import ast.Name;
import ast.ProtocolTypeDecl;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * Visitor used to rewrite the body of protocols that inherit tags
 * from one or more than one protocol. Note that this visitor adds
 * _shallow_ copies of tags (and their members) to a protocol that
 * extends multiple protocols.
 * 
 * @author ben
 */
public class ProtocolRewrite extends Visitor<AST> {
    
    /** Top level declarations */
    private SymbolTable sym;
    
    public ProtocolRewrite(SymbolTable sym) {
        this.sym = sym;
        Log.logHeader("****************************************");
        Log.logHeader("*   P R O T O C O L   R E W R I T E    *");
        Log.logHeader("****************************************");
    }
    
    public boolean addProtocolName(Set<Name> set, Name name) {
    	boolean found = false;
        for (Name n : set) {
            if (n.getname().equals(name.getname())) {
            	found = true;
            	break;
            }
        }
        if (!found)
            set.add(name);
        return found;
    }
    
    public Set<Name> addExtendProtocolName(AST a) {
        Log.log(a, "extends a ProtocolypeDecl (" + ((ProtocolTypeDecl) a).name().getname() + ")");
        ProtocolTypeDecl pd = (ProtocolTypeDecl) a;
        Set<Name> set = new LinkedHashSet<>();
        Log.log(pd, "adding protocol " + pd.name().getname());
        set.add(pd.name());
        // Add extended protocols
        for (Name parent : pd.extend()) {
            if (sym.get(parent.getname()) != null) {
                Set<Name> setNames = addExtendProtocolName((ProtocolTypeDecl) sym.get(parent.getname()));
                for (Name pdt : setNames) {
                    boolean found = addProtocolName(set, pdt);
                    if (!found)
                        Log.log(pd, String.format("Name '%s' already added", pdt.getname()));
                }
            }
        }
        return (Set<Name>) set;
    }
    
    // DONE
    @Override
    public AST visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        Log.log(pd, "Visiting a ProtocolTypeDecl (" + pd.name().getname() + ")");
        Set<Name> set = new LinkedHashSet<>();
        // Merge the member tags of all extended protocols
        for (Name name : pd.extend())
            if (sym.get(name.getname()) != null)
                set.addAll(addExtendProtocolName((ProtocolTypeDecl) sym.get(name.getname())));
        for (Name n : pd.extend())
            addProtocolName(set, n);
        pd.extend().clear();
        // Rewrite the extend node
        for (Name n : set)
            pd.extend().append(n);
        Log.log(pd, String.format("Protocol %s with %s parent(s)", pd.name().getname(), pd.extend().size()));
        for (Name n : pd.extend())
            Log.log(pd, "> protocol " + n);
        return null;
    }
}
