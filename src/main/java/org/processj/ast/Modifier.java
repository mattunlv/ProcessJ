package org.processj.ast;

import org.processj.Phase;
import org.processj.utilities.Error;
import org.processj.utilities.Visitor;

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

    public String toString() {
        return names[modifier];
    }

    public Modifier(Token t, int modifier) {
        super(t);
        this.modifier = modifier;
    }

    public Modifier(int modifier) {
        super((AST) null);
        this.modifier = modifier;
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

    public static boolean hasModifierSet(Sequence<Modifier> mo, int modifier) {
        for (Modifier m : mo) {
            if (m.getModifier() == modifier)
                return true;
        }
        return false;
    }

    public static void noRepeats(Sequence<Modifier> mo) {
        int mods[] = new int[MAX_MODIFIER + 1];
        for (Modifier m : mo) {
            if (mods[m.getModifier()] == 1)
                Error.error(mo, "Repeated modifier '" + mo + "'.");
            else
                mods[m.getModifier()] = 1;
        }
    }

    public <S extends Object> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitModifier(this);
    }
}