package org.processj.typechecker;

import org.processj.ast.ArrayType;
import org.processj.ast.ChannelEndType;
import org.processj.ast.ChannelType;
import org.processj.ast.LocalDecl;
import org.processj.ast.NamedType;
import org.processj.ast.ParamDecl;
import org.processj.ast.ProcTypeDecl;
import org.processj.ast.ProtocolCase;
import org.processj.ast.ProtocolTypeDecl;
import org.processj.ast.RecordMember;
import org.processj.ast.RecordTypeDecl;
import org.processj.ast.Sequence;
import org.processj.ast.Type;
import org.processj.utilities.Log;
import org.processj.utilities.SymbolTable;
import org.processj.utilities.Visitor;

/**
 * Visitor responsible for resolving all NamedType to their
 * actual types. Note that the visitor __only__ affects
 * Protocol and Record types.
 * 
 * @author ben
 */
public class ResolveNamedType extends Visitor<Type> {
    
    SymbolTable currentScope;
    
    public ResolveNamedType(SymbolTable currentScope) {
        this.currentScope = currentScope;
        Log.log("*******************************************");
        Log.log("*    R E S O L V E   N A M E D T Y P E    *");
        Log.log("*******************************************");
    }

    @Override
    public Type visitNamedType(final NamedType namedType) {
        Log.log(namedType, "Visiting a named type.");

        // If the NamedType has not resolved a Type; we should have already bound a Type
        // to the NamedType if it specified a Package Access
        if(!namedType.hasResolvedType()) {

            // Attempt to retrieve a type from the current scope
            final Type retrieved = (Type) currentScope.getIncludeImports(namedType.toString());

            // If it wasn't in the include imports
            if(retrieved == null)
                Log.Error("No Type in Scope: " + namedType);
            // TODO: Error here
            //Error.addError(namedType, "Undefined named type '" + namedType + "'.", 3028);

            //retrieved = new ErrorType();

            // Otherwise set from the current scope
            namedType.setType(retrieved);

        }

        return null;

    }

    @Override
    public Type visitArrayType(ArrayType at) {
        Log.log(at, "Visiting an array type.");
        if(at.getComponentType() instanceof NamedType)
            at.children[0] = at.getComponentType().visit(this);
        return null;
    }

    @Override
    public Type visitChannelType(ChannelType ct) {
        Log.log(ct, "Visiting a channel type.");
        ct.getComponentType().visit(this);
        return null;
    }

    @Override
    public Type visitChannelEndType(ChannelEndType ct) {
        Log.log(ct, "Visiting a channel end type.");
        ct.getComponentType().visit(this);
        return null;
    }

    @Override
    public Type visitLocalDecl(LocalDecl ld) {
        Log.log(ld, "Visiting a local decl");
        if (ld.type() instanceof ArrayType)
            ld.type().visit(this);
        else if (ld.type() instanceof ChannelType || ld.type() instanceof ChannelEndType)
            ld.type().visit(this);
        else if ( ld.type() instanceof NamedType )
            ld.children[0] = ld.type().visit(this);
        return null;
    }

    @Override
    public Type visitParamDecl(ParamDecl pd) {
        Log.log(pd, "Visiting a paramdecl.");
        if (pd.type() instanceof ArrayType)
            pd.type().visit(this);
        else if ( pd.type() instanceof ChannelType || pd.type() instanceof ChannelEndType )
            pd.type().visit(this);
        else if (pd.type() instanceof NamedType)
            pd.children[0] = pd.type().visit(this);
        return null;
    }
    
    @Override
    public Type visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a procedure type decl.");
        super.visitProcTypeDecl(pd);
        return null;
    }

    @Override
    public Type visitProtocolTypeDecl(ProtocolTypeDecl pt) {
        Log.log(pt, "Visiting a protocol type decl.");
        if ( pt.body()==null )
            return pt;
        Sequence<ProtocolCase> se = pt.body();
        for (int i=0; i<se.size(); ++i) {
            ProtocolCase pc = se.child(i);
            Sequence<RecordMember> se2 = pc.body();
            for (int j=0; j<se2.size(); ++j) {
                RecordMember rm = se2.child(j);
                if ( rm.type() instanceof NamedType )
                    rm.children[0] = rm.type().visit(this);
            }
        }
        return null;
    }

    @Override
    public Type visitRecordTypeDecl(RecordTypeDecl rt) {
        Log.log(rt, "Visiting a record type decl.");
        Sequence<RecordMember> se = rt.body();
        for (int i=0; i<se.size(); ++i) {
            RecordMember rm = se.child(i);
            if ( rm.type() instanceof NamedType )
                rm.children[0] = rm.type().visit(this);
        }
        return null;
    }
}
