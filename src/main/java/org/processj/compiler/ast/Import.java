package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class Import extends AST {

    /// --------------
    /// Private Fields

    private final String symbol             ;
    private final String packageName        ;
    private final String path               ;

    /// ------------
    /// Constructors

    public Import(final Sequence<Name> packageName) {
        super(packageName.line, packageName.charBegin);

        this.symbol             = packageName.getLast().getName()                    ;
        this.packageName        = packageName.synthesizeStringWith(".")     ;
        this.path               = this.packageName.replace('.', '/') ;

    }

    /// ----------------
    /// java.lang.Object

    @Override
    public final String toString() {

        return this.packageName;

    }

    @Override
    public final <S> S visit(final Visitor<S> visitor) throws Phase.Error {

        return visitor.visitImport(this);

    }

    /**
     * <p>Returns a flag indicating if the {@link Import} path is empty.</p>
     * @return flag indicating if the {@link Import} path is empty.
     * @since 0.1.0
     */
    public final boolean isEmpty() {

        return this.path.isEmpty() || this.path.isBlank();

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