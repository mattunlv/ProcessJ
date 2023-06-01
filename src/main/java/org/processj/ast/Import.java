package org.processj.ast;

import org.processj.utilities.Visitor;

public class Import extends AST {

    /// --------------
    /// Private Fields

    private final String symbol         ;
    private final String packageName    ;
    private final String path           ;

    // All the compilations that this import perform are stored in compilations.
    private Sequence<Compilation> compilations = new Sequence<>();

    public Import(Sequence<Name> imp) {
        super(imp);
        nchildren = 2;
        Name file = imp.removeLast();
        children = new AST[] { imp, file };
        this.symbol         = file.getname();
        this.packageName    = imp.synthesizeStringWith(".");
        this.path           = imp.synthesizeStringWith("/");
    }

    public Sequence<Name> path() {
        return (Sequence<Name>) children[0];
    }

    public Name file() {
        return (Name) children[1];
    }

    public String toString() {
        String str = "";

        for (int i = 0; i < path().size(); i++) {
            str += path().child(i);
            if (i < path().size())
                str += ".";
        }
        str += file().getname();
        return str;
    }

    public void addCompilation(Compilation co) {
        compilations.append(co);
    }

    public Sequence<Compilation> getCompilations() {
        return compilations;
    }

    public <S> S visit(Visitor<S> v) {
        return v.visitImport(this);
    }

    /**
     * <p>Returns a flag indicating if the {@link Import} is a wildcard import.</p>
     * @return Flag indicating if the {@link Import} is a wildcard import.
     * @since 0.1.0
     */
    public final boolean isWildcard() {

        return this.symbol.equals("*");

    }

    /**
     * <p>Returns the {@link Import}'s symbol.</p>
     * @return The {@link Import}'s symbol.
     * @since 0.1.0
     */
    public final String getSymbol() {

        return this.symbol;

    }

    /**
     * <p>Returns the {@link Import}'s dot-delimited package name.</p>
     * @return The {@link Import}'s dot-delimited package name.
     * @since 0.1.0
     */
    public final String getPackageName() {

        return this.packageName;

    }

    /**
     * <p>Returns the {@link Import}'s forward slash-delimited path.</p>
     * @return The {@link Import}'s forward slash-delimited path.
     * @since 0.1.0
     */
    public final String getPath() {

        return this.path;

    }

    /**
     * <p>Returns a flag indicating if the {@link Import} path is empty.</p>
     * @return flag indicating if the {@link Import} path is empty.
     * @since 0.1.0
     */
    public final boolean isEmpty() {

        return !this.path.isEmpty() && !this.path.isBlank();

    }

}