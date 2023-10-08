package org.processj.compiler.ast.modifier;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;

/**
 * <p>Encapsulates a {@link Private} {@link Modifier}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see Modifier
 */
public class Private extends Modifier {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Private} to its' default state with the specified {@link Token}.</p>
     * @param token The {@link Token} used to bind to the {@link Private}'s placement in the source file.
     * @since 1.0.0
     * @see AST
     * @see Modifier
     */
    public Private(final Token token) {
        super(token);
    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} value of the {@link Private}'s name.</p>
     * @return {@link String} value of the {@link Private}'s name.
     * @since 1.0.0
     * @see Name
     * @see String
     */
    @Override
    public final String toString() {

        return "private";

    }

}
