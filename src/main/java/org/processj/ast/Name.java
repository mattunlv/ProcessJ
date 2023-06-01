package org.processj.ast;

import org.processj.utilities.Visitor;

/**
 * Package Access (The ::) can be used for:
 *      Type Names      type_list, new mobile, binary_expression: expr (RHS), new RecordLiteral, new Protocol Literal
 *      Identifiers     NameExpr
 *      Invocations     Invocation
 *      Type Members? (For Protocol & Records)? NamedType
 *
 *      NamedType is contained in CastExpr, ExternTypeDeclaration?, ArrayType, NewArrayExpression
 *
 *      Note: Extern Types kind of end right then and there. They are coupled with Name
 */
public class Name extends AST {

    /// --------------
    /// Private Fields

    private final Sequence<Name> packageAccess;

    public AST myDecl; // used for places in the the grammar where a Name is used (e.g., in extends of protocols) instead of a NameExpr.

    private String id;
    private int arrayDepth; // somewhat of a hack - we keep track of whether this name is an id in a variable declaration with [] on.

    // points to what ever packageAccess() resolved to.
    public DefineTopLevelDecl resolvedPackageAccess = null;
    // the package hierarchy caused to be loaded because of the packageAccess
    public Compilation c = null;

    // a string with "."s that determines the package / in which this name was declared (if any) -- only useful for invocations for now.
    public String packagePrefix;

    public Name(Token p_id) {
        super(p_id);
        nchildren = 1;
        this.id = p_id.lexeme;
        this.arrayDepth = 0;
        children = new AST[] { new Sequence() };
        this.packageAccess = (Sequence<Name>) this.children[0];
    }

    public Name(String name) {
        super(0, 0);
        nchildren = 1;
        this.id = name;
        this.arrayDepth = 0;
        children = new AST[] { new Sequence() };
        this.packageAccess = (Sequence<Name>) this.children[0];
    }

    public Name(Name n, int arrayDepth) {
        super(new AST[] { new Sequence() });
        this.id = n.getname();
        this.arrayDepth = arrayDepth;
        this.packageAccess = (Sequence<Name>) this.children[0];
    }

    public Name(Token p_id, Sequence<Name> package_access) {
        super(new AST[] { package_access });
        this.id = p_id.lexeme;
        this.arrayDepth = 0;
        this.packageAccess = package_access;
    }

    /**
     * <p>Returns a flag indicating if the {@link Name} is prefixed with a fully-qualified package name.</p>
     * @return Flag indicating if the {@link Name} is prefixed with a fully-qualified package name.
     * @since 0.1.0
     */
    public final boolean specifiesPackage() {

        return !this.packageAccess.isEmpty();

    }

    public Sequence<Name> packageAccess() {
        return (Sequence<Name>) children[0];
    }

    public String getname() {
        return this.toString(); //this.id;  // TODO: changed back to full name
    }

    public String simplename() {
        return this.id;
    }

    public boolean isSimple() {
        return packageAccess().size() == 0;
    }

    public void setName(String na) {
        id = na;
    }

    public String toString() {
        String s = "";
        if (packageAccess() != null) {
            int size = packageAccess().children.size();
            for (int i = 0; i < size; i++) {
                s = s + packageAccess().child(i);
                if (i != size - 1)
                    s = s + ".";
                else
                    s = s + "::";
            }
        }
        return s + this.id;
    }

    public int getArrayDepth() {
        return this.arrayDepth;
    }

    public void setArrayDepth(int d) {
        this.arrayDepth = d;
    }

    public <S> S visit(Visitor<S> v) {
        return v.visitName(this);
    }
}