package org.processj.ast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.processj.utilities.Visitor;

public class Compilation extends AST {
    
    public boolean visited = false;
    public String fileName = "";    // The name of the file that caused this compilation to exist. Read by the ProcTypeDel.
    public String path = "";        // Absolute path to where the file is located.
    public boolean isImport = false;
    public String packageName = "";

    /// --------------
    /// Private Fields

    private final Map<String, Object>   topLevelDeclarations    ;
    private final Sequence<Import>      importStatements        ;
    private final String                _packageName            ;
    private final String                _path                   ;

    /// ------------
    /// Constructors

    public Compilation(final Sequence<Pragma> pragmas,
                       final Sequence<Name> packageName,
                       final Sequence<Import> imports,
                       final Sequence<Type> typeDecls) {
        super(new AST[] {
                (pragmas != null) ? pragmas : new Sequence<>(),
                (packageName != null) ? packageName : new Sequence<>(), imports, typeDecls });

        final Sequence<Name> __packageName = (Sequence<Name>) this.children[1];

        // TODO: AST.line & AST.charBegin was originally derived from typeDecls             ;
        this._packageName = __packageName.synthesizeStringWith(".");
        this._path        = (__packageName.isEmpty()) ? "" : __packageName.synthesizeStringWith("/") + "/";
        this.topLevelDeclarations   = new HashMap<>();
        this.importStatements       = imports;

    }

    public Sequence<Pragma> getPragmas() {
        return (Sequence<Pragma>) children[0];
    }

    public Sequence<Name> getPackageName() {
        return (Sequence<Name>) children[1];
    }

    public Sequence<Import> imports() {
        return (Sequence<Import>) children[2];
    }

    public Sequence<Type> typeDecls() {
        return (Sequence<Type>) children[3];
    }

    public <S> S visit(Visitor<S> v) {
        return v.visitCompilation(this);
    }

    public String fileNoExtension() {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
    
    public String packageNoName() {
        return packageName.isEmpty() ? path.substring(path.lastIndexOf(File.separator) + 1) : packageName;
    }

    /**
     * <p>Returns a flag indicating if the {@link Compilation} specifies a package name.</p>
     * @return flag indicating if the {@link Compilation} specifies a package name.
     * @since 0.1.0
     */
    public final boolean definesPackageName() {

        // TODO: Set the Compilation's package name as a string
        return this.getPackageName() != null;

    }

    /**
     * <p>Returns a flag indicating if the {@link Compilation} contains {@link Pragma} definitions.</p>
     * @return flag indicating if the {@link Compilation} contains {@link Pragma} definitions.
     * @since 0.1.0
     */
    public final boolean definesPragmas() {

        // TODO: Set the Compilation's package name as a string
        return !this.getPragmas().isEmpty();

    }

    /**
     * <p>Returns a flag indicating if the {@link Compilation} has any {@link Import} statements defined.</p>
     * @return Flag indicating if the {@link Compilation} has any {@link Import} statements defined.
     * @since 0.1.0
     */
    public final boolean definesImports() {

        return (this.importStatements != null) && !this.importStatements.isEmpty();

    }

    /**
     * <p>Returns a flag indicating if the {@link Compilation} defines a {@link DefineTopLevelDecl} with the specified
     * {@link String} name.</p>
     * @param name The {@link String} name to check
     * @return Flag indicating if the {@link Compilation} defines a {@link DefineTopLevelDecl} with the specified
     * {@link String} name.
     * @since 0.1.0
     */
    public final boolean definesTopLevelDeclarationWith(final String name) {

        return this.topLevelDeclarations.containsKey(name);

    }

    /**
     * <p>Returns the dot-delimited {@link String} value of the package name corresponding to the
     * {@link Compilation}.</p>
     * @return Dot-delimited {@link String} value of the package name corresponding to the {@link Compilation}.
     * @since 0.1.0
     */
    public final String get_packageName() {

        return this._packageName;

    }

    /**
     * <p>Returns the forward slash-delimited {@link String} value of the path corresponding to the
     * {@link Compilation}.</p>
     * @return forward slash-delimited {@link String} value of the path corresponding to the {@link Compilation}.
     * @since 0.1.0
     */
    public final String get_filePath() {

        return this._path;

    }

    /**
     * <p>Returns the {@link DefineTopLevelDecl}s visible by the {@link Compilation}.</p>
     * @return the {@link DefineTopLevelDecl} visible by the {@link Compilation}.
     * @since 0.1.0
     */
    public final Map<String, Object> getTopLevelDeclarations() {

        return this.topLevelDeclarations;

    }

    /**
     * <p>Emplaces a {@link String} name value & {@link Object} instance corresponding with a
     * {@link DefineTopLevelDecl} into the {@link Compilation}'s {@link DefineTopLevelDecl} mapping.</p>
     * @param name The {@link String} name corresponding to the {@link DefineTopLevelDecl}.
     * @param value The {@link DefineTopLevelDecl} value.
     * @since 0.1.0
     */
    public final void defineTopLevelDeclarationWith(final String name, final Object value) {

        this.topLevelDeclarations.put(name, value);

    }

}