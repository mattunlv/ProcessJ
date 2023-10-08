package org.processj.compiler.ast.modifier;

import org.processj.compiler.ast.AST;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class Modifiers extends AST {

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link Set} of unique {@link Modifier}s that were specified in a given sequence of {@link Modifier}s.</p>
     * @since 1.0.0
     * @see Modifier
     */
    private final Set<Modifier> modifierSet;

    /**
     * <p>The {@link Set} of unique {@link Modifier} names that specified in a given sequence of {@link Modifier}s.</p>
     * @since 1.0.0
     * @see Modifier
     */
    private final Set<String> modifierNameSet;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Modifiers} to its' default state with the specified variadic {@link Modifier}.</p>
     * @param modifiers The {@link Modifier}s used to initialize the {@link Modifiers}'s.
     * @since 1.0.0
     * @see AST
     * @see Modifier
     */
    public Modifiers(final Modifier... modifiers) {
        super(modifiers);

        this.modifierSet     = new LinkedHashSet<>();
        this.modifierNameSet = new LinkedHashSet<>();

        this.modifierSet.addAll(Arrays.stream(modifiers).toList());

        this.modifierNameSet.addAll(
                Arrays.stream(modifiers)
                        .map(Modifier::getClass)
                        .map(Class::getSimpleName).toList());

    }

    /// ---
    /// AST

    @Override
    public void accept(Visitor visitor) throws Phase.Error {



    }

    /// --------------
    /// Public Methods

    public final boolean contains(final Class<? extends Modifier> modifierClass) {

        return this.modifierNameSet.contains(modifierClass.getSimpleName());

    }

    public final Modifiers add(final Modifier modifier) {

        this.modifierSet.add(modifier);

        return this;

    }

    public final boolean isEmpty() {

        return this.modifierNameSet.isEmpty();

    }

    public final boolean isNative() {

        return this.modifierNameSet.contains(Native.class.getSimpleName());

    }

    public final boolean isConstant() {

        return this.modifierNameSet.contains(Constant.class.getSimpleName());

    }

    public final boolean isMobile() {

        return this.modifierNameSet.contains(Mobile.class.getSimpleName());

    }

}
