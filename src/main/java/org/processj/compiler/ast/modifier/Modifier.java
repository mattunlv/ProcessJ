package org.processj.compiler.ast.modifier;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Token;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class Modifier extends AST {

    private int modifier = -1;

    public final static int PUBLIC = 0;
    public final static int PRIVATE = 1;
    public final static int PROTECTED = 2;
    public final static int NATIVE = 3;
    public final static int CONST = 4;
    public final static int MOBILE = 5;

    public final static int MAX_MODIFIER = MOBILE; // set this to be the biggest number used in the list above.

    public static final String[] names = { "public", "private", "protected",
        "native", "const", "mobile" };

    public Modifier(final Token token) {
        super(token);
    }

    /// ------
    /// Object

    @Override
    public final int hashCode() {

        return this.toString().hashCode();

    }

    public int getModifier() {
        return this.modifier;
    }

    /**
     * <p>Returns a flag indicating if the {@link Modifier} is specified as native.</p>
     * @return flag indicating if the {@link Modifier} is specified as native.
     * @since 0.1.0
     */
    public final boolean isNative() {

        return this.modifier == NATIVE;

    }

    /**
     * <p>Returns a flag indicating if the {@link Modifier} is specified as mobile.</p>
     * @return flag indicating if the {@link Modifier} is specified as mobile.
     * @since 0.1.0
     */
    public final boolean isMobile() {

        return this.modifier == MOBILE;

    }

    public void accept(final Visitor visitor) throws Phase.Error {

        visitor.visitModifier(this);

    }

}