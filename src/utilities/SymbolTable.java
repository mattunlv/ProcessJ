package utilities;

import ast.DefineTopLevelDecl;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The class {@code SymbolTable}. Each symbol table contains a Hashtable
 * that contains the symbols defined in the scope that it corresponds
 * to. A symbol table has a reference  to the symbol table for its
 * enclosing scope (if any) -- called 'parent' -- or to the closest
 * import statement going backwards -- called import parent. 
 * 
 * A single symbol table may be used to hold all declarations of
 * methods with the same name but with different signatures.
 */
public class SymbolTable implements DefineTopLevelDecl {
    // This hook is used to hold on the global type table
    // and to transport the closest table from TopLevelDecls.
    public static SymbolTable hook = null;

    // Hack
    public boolean isMobileProcedure = false;

    // link to symbol table for enclosing scope (always null for the main file)
    private SymbolTable parent = null;
    // link to the symbol table for the nearest import statement
    private SymbolTable importParent = null; 
    
    // The name of the symbol table -- if any is given.
    private String name;

    // The actual entries in the table.
    public Hashtable<String, Object> entries;

    public SymbolTable() {
        this("<anonymous>");
    }

    public SymbolTable(String name) {
        this.parent = null;
        this.importParent = null;
        this.name = name;
        this.entries = new Hashtable<String, Object>();
    }

    public SymbolTable(SymbolTable parent, String name) {
        this(name);
        this.parent = parent;
    }

    public SymbolTable(SymbolTable parent) {
        this(parent, "<anonymous>");
        this.parent = parent;
    }

    public void setParent(SymbolTable st) {
        this.parent = st;
    }

    // Delete an entry from the symbol table.
    public void remove(String name) {
        entries.remove(name);
    }

    // Get the parent table of this table. 
    public SymbolTable getParent() {
        return parent;
    }

    public void setImportParent(SymbolTable st) {
        importParent = st;
    }

    public SymbolTable getImportParent() {
        return importParent;
    }

    public String getname() {
        return name;
    }

    /**
     * Enters a new entry into the symbol table.
     *
     * @param name
     *            The name of the entry object.
     * @param entry
     *            The entry.
     */
    public boolean put(String name, Object entry) {
        Object lookup = entries.get(name);
        if (lookup != null)
            return false;
        entries.put(name, entry);
        return true;
    }
    
    public Object get(String name) {
        Object result = entries.get(name);
        if (result != null)
            return result;
        if (parent == null) {
            return null;
        }
        return parent.get(name);
    }

    public Object getIncludeImports(String name) {
        Object result = entries.get(name);
        if (result != null)
            return result;
        if (importParent == null)
            return null;
        return importParent.get(name);
    }
    
    public Object getShallow(String name) {
        Object result = entries.get(name);
        return result;
    }

    public String toString() {
        String s = "SymbolTable: Name: " + name + "\n";
        if (parent != null)
            s = "\n" + parent.toString();
        return s + " -> " + entries.toString();
    }

    public void print(String indent) {
        if (parent != null) {
            parent.print(indent + "  ");
            Log.log(indent + "-->");
        }
        Enumeration<String> col = entries.keys();
        Log.log(indent + "Name: " + name + "(" + entries.size() + ")");
        Log.log(indent + "Has importParent? " + (importParent != null));
        for (; col.hasMoreElements();) {
            String element = col.nextElement();
            Object o = entries.get(element);
            System.out.print(
                    indent + element + "\t" + (o.getClass().getName().equals("utilities.SymbolTable") ? "Procedure: "
                            : o.getClass().getName()));
            if (o instanceof SymbolTable) {
                Log.log("\n" + indent + "  --------------------------------");
                ((SymbolTable) o).print(indent + "  ");
                Log.log(indent + "  ================================");

            } else {
                Log.log("");
            }
        }
    }

    /**
     * Opens a new scope and returns it.
     *
     * @return The new scope.
     */
    public SymbolTable openScope() {
        return new SymbolTable(this, "<anonymous>");
    }

    public SymbolTable openScope(String name) {
        return new SymbolTable(this, name);
    }

    /**
     * Closes the current scope and returns its parent scope
     *
     * @return the current scope's parent scope.
     */
    public SymbolTable closeScope() {
        return parent;
    }

    // result of the -sts compiler flag
    public void printStructure(String indent) {
        Log.log(indent + "name.........: " + this.name);
        Log.log(indent + "Content ");
        for (Object o : entries.keySet().toArray()) {
            Object o2 = entries.get((String) o);
            Log.log("  [*] " + ((String) o) + " == " + o2);
        }
        Log.log(indent + "parent.......: " + (parent == null ? "--//" : ""));
        if (parent != null)
            parent.printStructure(indent + "|  ");
        Log.log(indent + "importParent.: " + (importParent == null ? "--//" : ""));
        if (importParent != null)
            importParent.printStructure(indent + "|  ");
    }
}