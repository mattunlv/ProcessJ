package org.processj.ast;

import org.processj.Phase;
import org.processj.utilities.Visitor;

public class QualifiedName extends AST {

    public QualifiedName(Name package_, Name file, Name name) {
        super(name);
        nchildren = 3;
        children = new AST[] { package_, file, name };
    }

    public QualifiedName(Name file, Name name) {
        this(null, file, name);
    }

    public Name packageName() {
        return (Name) children[0];
    }

    public Name fileName() {
        return (Name) children[1];
    }

    public Name name() {
        return (Name) children[2];
    }

    @Override
    public <T extends Object> T visit(Visitor<T> v) throws Phase.Error {
        v.visitQualifiedName(this);
        return null;
    }
}