package org.processj.ast;

public interface VarDecl {

    public Type type();

    public String name();

    public void setType(Type t);
}