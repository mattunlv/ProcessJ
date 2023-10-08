package org.processj.compiler.ast.modifier;


import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.Token;

/**
 * <p>Encapsulates a {@link Mobile} {@link Modifier}.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 * @see Modifier
 */
public class Mobile extends Modifier {

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Mobile} to its' default state with the specified {@link Token}.</p>
     * @param token The {@link Token} used to bind to the {@link Mobile}'s placement in the source file.
     * @since 1.0.0
     * @see AST
     * @see Modifier
     */
    public Mobile(final Token token) {
        super(token);
    }

    /// ------
    /// Object

    /**
     * <p>Returns the {@link String} value of the {@link Mobile}'s name.</p>
     * @return {@link String} value of the {@link Mobile}'s name.
     * @since 1.0.0
     * @see Name
     * @see String
     */
    @Override
    public final String toString() {

        return "mobile";

    }

}
