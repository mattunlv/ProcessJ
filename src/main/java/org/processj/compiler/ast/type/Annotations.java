package org.processj.compiler.ast.type;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Sequence;
import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class Annotations extends AST {

    java.util.Hashtable<String, String> annotations;

    public Annotations() {
        annotations = new java.util.Hashtable<String, String>();
    }

    public Annotations(Sequence /* Annotation */s) {
        annotations = new java.util.Hashtable<String, String>();
        for (int i = 0; i < s.size(); i++) {
            Annotation a = (Annotation) s.child(i);
            annotations.put(a.getName(), a.getValue());
        }
    }

    public Annotations(final Annotation annotation) {

        annotations.put(annotation.getName(), annotation.getValue());

    }

    public void add(String name, String value) {
        annotations.put(name, value);
    }

    public AST add(final Annotation annotation) {


        return this;
    }

    public Annotations appendAll(final Annotations annotations) {

        return this;

    }

    public boolean isDefined(String name) {
        return annotations.containsKey(name);
    }

    public String get(String name) {
        return annotations.get(name);
    }

    public String toString() {
        return annotations.toString();
    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitAnnotations(this);
    }
}