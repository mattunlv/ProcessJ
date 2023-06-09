package org.processj.compiler.phases.phase;

import org.processj.compiler.ast.*;

/**
 * <p>Resolves the current {@link Compilation}'s {@link Import} statements and fully-qualified {@link Name}s
 * & aggregates their {@link Compilation}s into the {@link ResolveImports} {@link Phase}'s current
 * {@link Compilation}'s collection of imported symbols. Once the {@link Import} statements for a {@link Compilation}
 * have been resolved, this will aggregate the current {@link Compilation}'s top level declarations into its'
 * corresponding symbol table to prepare the {@link Compilation} for the next {@link Phase}.</p>
 * @see Compilation
 * @see DefineTopLevelDecl
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @since 0.1.0
 * @version 1.0.0
 */
public class ResolveImports extends Phase {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link ResolveImports.Listener}.</p>
     * @param listener The {@link Phase.Listener} that receives any {@link Phase.Message},
     * {@link Phase.Warning}, or {@link Phase.Error} messages.
     * @since 0.1.0
     */
    public ResolveImports(final Phase.Listener listener) {
        super(listener);
    }

    /// -------------------------
    /// org.processj.ast.IVisitor

    /**
     * <p>Imports the package corresponding with the {@link Import}'s package name if it's defined.</p>
     * @param importStatement The {@link Import} specifying a package to process.
     * @throws Phase.Error If the {@link Import} handling failed.
     * @see Import
     * @see Phase.Error
     * @since 0.1.0
     */
    @Override
    public final Void visitImport(final Import importStatement) throws Phase.Error {

        if(!importStatement.isEmpty())
            ImportAssert.ImportSpecified(this, importStatement.toString());

        return null;

    }

    /**
     * <p>Imports the package corresponding with the {@link Name}'s package name if it's defined.</p>
     * @param name The {@link Name} specifying a package to process.
     * @throws Phase.Error If the {@link Import} handling failed.
     * @see Import
     * @see Phase.Error
     * @since 0.1.0
     */
    @Override
    public final Void visitName(final Name name) throws Phase.Error {

        if(name.specifiesPackage())
            ImportAssert.ImportSpecified(this, name.getPackageName());

        return null;

    }

}
