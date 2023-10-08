package org.processj.compiler.ast.type.primitive;

import org.processj.compiler.ast.Token;
import org.processj.compiler.ast.type.ExternType;
import org.processj.compiler.ast.type.Type;

public class TimerType extends PrimitiveType {
    public TimerType(Token p_t) {
        super(p_t);
    }

    public TimerType() {
        super(new Token(""));
    }

    /// ---------------------
    /// org.processj.ast.Type

    /**
     * <p>Returns the internal {@link String} signature representing the {@link ExternType}.</p>
     * @return The internal {@link String} signature representing the {@link ExternType}.
     * @since 0.1.0
     */
    @Override
    public String getSignature() {

        return "M";

    }

    @Override
    public Type addDimension() {
        return null;
    }

    @Override
    public Type clearDepth() {
        return null;
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public boolean isTypeEqualTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeEquivalentTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeLessThan(Object that) {
        return false;
    }

    @Override
    public boolean isTypeGreaterThan(Object that) {
        return false;
    }

    @Override
    public boolean isTypeLessThanOrEqualTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeGreaterThanOrEqualTo(Object that) {
        return false;
    }

    @Override
    public boolean isTypeCeilingOf(Object that) {
        return false;
    }

    @Override
    public boolean isSubTypeOf(Object that) {
        return false;
    }

    @Override
    public boolean isAssignmentCompatibleTo(Object that) {
        return false;
    }

}