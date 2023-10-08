package org.processj.compiler.ast.statement;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Token;

/**
 * <p>Class that encapsulates a statement.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 1.0.0
 * @see AST
 */
public abstract class Statement extends AST {

    /**
     * <p>The set of barriers from which a process should resign.</p>
     * @since 1.0.0
     */
    // TODO: Move this strictly to only ForStatement & Par Block
    private final BarrierSet barrierSet;

    /**
     * <p>The {@link String} value of the label preceding the {@link Statement}.</p>
     * @since 1.0.0
     * @see String
     */
    private String label;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link Statement} to its' default state with the specified {@link BarrierSet}.</p>
     * @param barrierSet The set of barriers from which a process should resign.
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link Statement}.
     * @since 1.0.0
     * @see BarrierSet
     * @see Token
     */
    public Statement(final BarrierSet barrierSet, final Token... tokens) {
        super(tokens);

        this.barrierSet = (barrierSet != null) ? barrierSet : new BarrierSet()     ;
        this.label      = ""                                                       ;

    }

    /**
     * <p>Initializes the {@link Statement} to its' default state with the specified {@link BarrierSet}.</p>
     * @param barrierSet The set of barriers from which a process should resign.
     * @param children Variadic list of {@link AST} nodes the parser specifies pertinent to the {@link Statement}.
     * @since 1.0.0
     * @see BarrierSet
     * @see AST
     */
    public Statement(final BarrierSet barrierSet, final AST... children) {
        super(children);

        this.barrierSet = (barrierSet != null)  ? barrierSet : new BarrierSet()     ;
        this.label      = ""                                                        ;

    }

    /**
     * <p>Initializes the {@link Statement} to its' default state.</p>
     * @param tokens Variadic list of {@link Token}s the parser specifies pertinent to the {@link Statement}.
     * @since 1.0.0
     * @see Token
     */
    public Statement(final Token... tokens) {
        super(tokens);

        this.barrierSet = new BarrierSet()  ;
        this.label      = ""                ;

    }

    /**
     * <p>Initializes the {@link Statement} to its' default state.</p>
     * @param children Variadic list of {@link AST}s the parser specifies pertinent to the {@link Statement}.
     * @since 1.0.0
     * @see Token
     */
    public Statement(final AST... children) {
        super(children);

        this.barrierSet = new BarrierSet();
        this.label      = "";

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns a flag indicating if the {@link Statement} was specified with label.</p>
     * @return A flag indicating if the {@link Statement} was specified with label.
     * @since 1.0.0
     */
    public final boolean definesLabel() {

        return !(this.label.isBlank() || this.label.isEmpty());

    }

    /**
     * <p>Returns the {@link BarrierSet} from which a process should resign on.</p>
     * @return the {@link BarrierSet} from which a process should resign on.
     * @since 1.0.0
     * @see BarrierSet
     */
    public final BarrierSet getBarrierSet() {

        return this.barrierSet;

    }

    /**
     * <p>Returns the {@link String} value of the label preceding the {@link Statement}.</p>
     * @return The {@link String} value of the label preceding the {@link Statement}.
     * @since 1.0.0
     * @see String
     */
    public final String getLabel() {

        return this.label;

    }

    /**
     * <p>Mutates the {@link Statement}'s {@link String} value corresponding to its' preceding label.</p>
     * @param label The desired {@link String} value to bind to the {@link Statement}'s label.
     * @since 1.0.0
     * @see String
     */
    public void setLabel(final String label) {

        this.label = label;

    }

    /**
     * <p>Clears the {@link Statement}'s {@link BarrierSet} & label {@link String} value.</p>
     * @since 1.0.0
     * @see BarrierSet
     */
    public void clear() {

        this.barrierSet.clear();
        this.label = "";

    }

}