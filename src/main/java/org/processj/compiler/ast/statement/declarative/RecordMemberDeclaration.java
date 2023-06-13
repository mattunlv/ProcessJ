package org.processj.compiler.ast.statement.declarative;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.type.Type;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;

public class RecordMemberDeclaration extends AST {

    /// --------------
    /// Private Fields

    private Type type;
    private Name name;

    public RecordMemberDeclaration(final Type type, final Name name) {
        super(new AST[] { type, name });

        this.type = type;
        this.name = name;

    }

    public final void setType(final Type type) {

        this.type           = type;
        this.children[0]    = type;

    }

    @Override
    public final String toString() {

        return this.name.toString();

    }

    public Type getType() {
        return this.type;
    }

    public Name getName() {
        return (Name) children[1];
    }

    public <S> S visit(Visitor<S> v) throws Phase.Error {
        return v.visitRecordMemberDeclaration(this);
    }
}