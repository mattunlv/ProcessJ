package org.processj.compiler.ast.statement;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.expression.Expression;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Statement extends AST {

    // This sequence is used in the rewriting phase.                                                                                                                                                               
    // It holds Declarations and Assignments of the form:                                                                                                                                                          
    //                                                                                                                                                                                                             
    // T temp_1;                                                                                                                                                                                                   
    // temp_1 = c.read();

    // Barriers from which a process should resign
    private final Set<Expression> barrierSet;
    private final Sequence<Expression> barriers;
    private String label;
    private String endLabel;

    public Statement(Token t) {
        super(t);
        this.barrierSet = new LinkedHashSet<>();
        this.barriers   = new Sequence<>();
        this.label = "";
        this.endLabel = "";
    }

    public Statement(final AST a) {
        super(new AST[] { a, new Sequence<Expression>() });
        this.barrierSet = new LinkedHashSet<>();
        this.barriers   = (Sequence<Expression>) this.children[1];
        this.label = "";
        this.endLabel = "";
    }

    public Statement(final AST... children) {
        super(children);
        this.barrierSet = new LinkedHashSet<>();
        this.barriers   = new Sequence<>();
        this.label = "";
        this.endLabel = "";
    }

    public Statement(final String label, final Expression expression) {
        super(new AST[] { expression });
        this.label      = label;
        this.barrierSet = new LinkedHashSet<>();
        this.barriers   = new Sequence<>();
        this.label = "";
        this.endLabel = "";
    }

    public final Set<Expression> getBarrierSet() {

        return this.barrierSet;

    }

    public final Sequence<Expression> getBarriers() {

        return this.barriers;

    }

    public final void addBarrier(final Expression expression) {

        this.barrierSet.add(expression);

    }

    public final void addBarriers(final Set<Expression> barriers) {

        this.barrierSet.addAll(barriers);

    }

    public void setLabel(final String label) {

        this.label = label;

    }

    public void setEndLabel(final String endLabel) {

        this.endLabel = endLabel;

    }

    public String getLabel() {

        return label;

    }

    public String getEndLabel() {

        return this.endLabel;

    }

    public boolean definesLabel() {

        return !this.label.isBlank();

    }

    public boolean definesEndLabel() {

        return !this.endLabel.isBlank();

    }

    public void clear() {

        // Clear here

    }

}