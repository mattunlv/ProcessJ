package org.processj.compiler.ast;

import org.processj.compiler.ast.packages.Imports;
import org.processj.compiler.ast.packages.Pragma;
import org.processj.compiler.ast.packages.Pragmas;
import org.processj.compiler.ast.type.Types;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class Compilation extends AST {

    public String fileName = "";    // The name of the file that caused this compilation to exist. Read by the ProcTypeDel.

    /// --------------
    /// Private Fields

    private final Pragmas pragmas;
    private final Name packageName;
    private final Imports imports;
    private final Types types;

    /// ------------
    /// Constructors

    public Compilation(final Pragmas pragmas,
                       final Name packageName,
                       final Imports imports,
                       final Types types) {
        super(pragmas, packageName, imports, types);

        // TODO: AST.line & AST.charBegin was originally derived from typeDecls             ;
        this.pragmas = pragmas;
        this.imports = imports;
        this.types = types;
        this.packageName = packageName;

    }

    @Override
    public final String toString() {

        return this.packageName.toString();

    }
    /// --------------------
    /// org.processj.ast.AST

    /**
     * <p>Invoked when the specified {@link Visitor} intends to visit the {@link Compilation}.
     * This method will dispatch the {@link Visitor}'s {@link Visitor#visitCompilation(Compilation)} method.</p>
     *
     * @param visitor The {@link Visitor} to dispatch.
     * @throws Phase.Error if the {@link Visitor#visitCompilation(Compilation)} visitation invocation operation
     *                     failed.
     * @since 0.1.0
     */
    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        // Open the Context
        visitor.setContext(this.openContext(visitor.getContext()));

        // Open a scope for the If Statement
        this.openScope();

        // Visit
        visitor.visitCompilation(this);

        // Close the scope
        visitor.setContext(this.closeContext());

    }

    public final Pragmas getPragmas() {

        return this.pragmas;

    }

    public final Imports getImports() {

        return this.imports;

    }

    public final Types getTypeDeclarations() {

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

        return (this.packageName != null);// && !this.packageName.isEmpty() && !this.packageName.isBlank();

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

        return this.packageName.toString();

    }

}