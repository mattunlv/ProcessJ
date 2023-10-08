package org.processj.compiler.ast.packages;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class Import extends AST {

    /// --------------
    /// Private Fields

    private final String packageName        ;
    private final String path               ;
    private final boolean isWildcard ;

    /// ------------
    /// Constructors

    public Import(final Name packageName, final boolean isWildcard) {
        super(packageName);

        this.packageName        = packageName.toString();
        this.path               = this.packageName.replace('.', '/') ;
        this.isWildcard         = isWildcard;

    }

    /// ----------------
    /// java.lang.Object

    @Override
    public final String toString() {

        return this.packageName + ((this.isWildcard) ? ".*" : "");

    }

    @Override
    public final void accept(final Visitor visitor) throws Phase.Error {

        visitor.visitImport(this);

    }

    /**
     * <p>Returns a flag indicating if the {@link Import} path is empty.</p>
     * @return flag indicating if the {@link Import} path is empty.
     * @since 0.1.0
     */
    public final boolean isEmpty() {

        return this.path.isEmpty();

    }

    /**
     * <p>Returns a flag indicating if the {@link Import} is a wildcard import.</p>
     * @return Flag indicating if the {@link Import} is a wildcard import.
     * @since 0.1.0
     */
    public final boolean isWildcard() {

        return this.isWildcard;

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

}