package org.processj.compiler.ast;

import org.processj.compiler.ast.statement.conditional.BlockStatement;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class Compilation extends AST implements SymbolMap.Context {

    public String fileName = "";    // The name of the file that caused this compilation to exist. Read by the ProcTypeDel.

    /// --------------
    /// Private Fields

    private final Sequence<Pragma>  pragmas     ;
    private final String            packageName ;
    private final Sequence<Import>  imports     ;
    private final Sequence<Type>    types       ;
    private SymbolMap               scope       ;

    /// ------------
    /// Constructors

    public Compilation(final Sequence<Pragma> pragmas,
                       final Sequence<Name> packageName,
                       final Sequence<Import> imports,
                       final Sequence<Type> types) {
        super(new AST[] { (pragmas != null) ? pragmas : new Sequence<>(), imports, types });

        // TODO: AST.line & AST.charBegin was originally derived from typeDecls             ;
        this.pragmas            = (pragmas != null) ? pragmas : new Sequence<>();
        this.imports            = (imports != null) ? imports : new Sequence<>();
        this.types              = (types != null) ? types : new Sequence<>();
        this.packageName        = (packageName != null) ? packageName.synthesizeStringWith(".") : "";
        this.scope              = null;

    }
    @Override
    public final String toString() {

        return this.packageName;

    }
    /// --------------------
    /// org.processj.ast.AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link Compilation}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitCompilation(Compilation)} method.</p>
     * @param visitor The {@link Visitor} to dispatch.
     * @throws Phase.Error if the {@link Visitor#visitCompilation(Compilation)} visitation invocation operation
     * failed.
     * @since 0.1.0
     */
    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        // Open the scope
        visitor.setScope(this.openScope());

        // Visit
        S result = visitor.visitCompilation(this);

        // Close the scope
        visitor.setScope(visitor.getScope().getEnclosingScope());

        return result;
    }

    public final Sequence<Pragma> getPragmas() {

        return this.pragmas;

    }

    public final Sequence<Import> getImports() {

        return this.imports;

    }

    public final Sequence<Type> getTypeDeclarations() {

        return this.types;

    }

    public String fileNoExtension() {

        return fileName.substring(0, fileName.lastIndexOf("."));

    }

    /**
     * <p>Returns a flag indicating if the {@link Compilation} specifies a package name.</p>
     * @return flag indicating if the {@link Compilation} specifies a package name.
     * @since 0.1.0
     */
    public final boolean definesPackageName() {

        return (this.packageName != null) && !this.packageName.isEmpty() && !this.packageName.isBlank();

    }

    /**
     * <p>Returns a flag indicating if the {@link Compilation} contains {@link Pragma} definitions.</p>
     * @return flag indicating if the {@link Compilation} contains {@link Pragma} definitions.
     * @since 0.1.0
     */
    public final boolean definesPragmas() {

        return !this.getPragmas().isEmpty();

    }

    /**
     * <p>Returns the dot-delimited {@link String} value of the package name corresponding to the
     * {@link Compilation}.</p>
     * @return Dot-delimited {@link String} value of the package name corresponding to the {@link Compilation}.
     * @since 0.1.0
     */
    public final String getPackageName() {

        return this.packageName;

    }

    @Override
    public BlockStatement getMergeBody() {
        return null;
    }

    @Override
    public BlockStatement getClearedMergeBody() {
        return null;
    }

    @Override
    public boolean definesLabel() {
        return false;
    }

    @Override
    public boolean definesEndLabel() {
        return false;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public void setEndLabel(String label) {

    }

    @Override
    public String getEndLabel() {
        return null;
    }

}