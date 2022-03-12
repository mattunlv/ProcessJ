package ast;

import utilities.Log;

public abstract class Type extends AST {

    public Type() {
        // must only be called from ErrorType
        super();
    }

    public Type(AST a) {
        super(a);
    }

    public Type(Token t) {
        super(t);
    }

    public abstract String typeName();
    
    public boolean assignable() {
        return (!typeName().equals("null") && !typeName().equals("void"));
    }

    public abstract String signature();

    public abstract boolean typeEqual(Type other);

    public abstract boolean typeEquivalent(Type other);

    public abstract boolean typeAssignmentCompatible(Type other);
    
    /*
     * NOTE: the general implementation of all these should be to return 'false'.
     * Each type should in turn implement which ever one applies to it. 
     */

    // Reimplemented in PrimitiveType
    public boolean isIntegerType() {
        return false;
    }

    public boolean isErrorType() {
        return false;
    }

    public boolean isExternType() {
        return false;
    }

    public boolean isArrayType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isBooleanType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isByteType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isShortType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isCharType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isLongType() {
        return false;
    }

    public boolean isTimerType() {
        return false;
    }

    public boolean isBarrierType() {
        return false;
    }

    public boolean isChannelType() {
        return false;
    }

    public boolean isChannelEndType() {
        return false;
    }

    public boolean isRecordType() {
        return false;
    }

    public boolean isProtocolType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isVoidType() {
        return false;
    }
    
    public boolean isNullType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isStringType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isFloatType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isDoubleType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isNumericType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isIntegralType() {
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isPrimitiveType() {
        return false;
    }

    public boolean isNamedType() {
        return false;
    }

    public boolean isProcType() {
        return false;
    }
}
