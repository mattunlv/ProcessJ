package ast;

import utilities.Visitor;

public class ErrorType extends Type {
    public static int errorCount = 0;

    public ErrorType() {
        super();
    }

    public String signature() {
        return "";
    }

    public String typeName() {
        return "Error type";
    }

    public String toString() {
        return "<Error>";
    }

    // *************************************************************************
    // ** Visitor Related Methods

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitErrorType(this);
    }

    // *************************************************************************
    // ** Type Related Methods

    @Override 
    public boolean isErrorType() {
        return true;
    }

    // Error types should not be checked like this...
    @Override 
    public boolean typeEqual(Type t) {
        return false;
    }

    // Error types should not be checked like this...
    @Override 
    public boolean typeEquivalent(Type t) {
        return false;
    }

    // Error types should not be checked like this...
    @Override 
    public boolean typeAssignmentCompatible(Type t) {
        return false;
    }
}