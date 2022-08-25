package typechecker;

import ast.ArrayType;
import ast.ChannelEndType;
import ast.ChannelType;
import ast.LocalDecl;
import ast.NamedType;
import ast.ParamDecl;
import ast.ProcTypeDecl;
import ast.ProtocolCase;
import ast.ProtocolTypeDecl;
import ast.RecordMember;
import ast.RecordTypeDecl;
import ast.Sequence;
import ast.Type;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Visitor;

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
    public Type visitArrayType(ArrayType at) {
        Log.log(at, "Visiting an array type.");
        if ( at.baseType().isNamedType() )
            at.children[0] = at.baseType().visit(this);
        return null;
    }
    
    @Override
    public Type visitChannelEndType(ChannelEndType ct) {
        Log.log(ct, "Visiting a channel end type.");
        if ( ct.baseType().isNamedType() )
            ct.children[0] = ct.baseType().visit(this);
        return null;
    }
    
    @Override
    public Type visitChannelType(ChannelType ct) {
        Log.log(ct, "Visiting a channel type.");
        if ( ct.baseType().isNamedType() )
            ct.children[0] = ct.baseType().visit(this);
        return null;
    }
    
    @Override
    public Type visitLocalDecl(LocalDecl ld) {
        Log.log(ld, "Visiting a local decl");
        if ( ld.type().isArrayType() )
            ld.type().visit(this);
        else if ( ld.type().isChannelType() || ld.type().isChannelEndType() )
            ld.type().visit(this);
        else if ( ld.type().isNamedType() )
            ld.children[0] = ld.type().visit(this);
        return null;
    }
    
    @Override
    public Type visitNamedType(NamedType nt) {
        Log.log(nt, "Visiting a named type.");
        Type type = nt.type();
        if ( type==null ) {
            // Look up the type in the closest import statement going
            // backwards (i.e., look for a record or protocol type)
            type = (Type) currentScope.getIncludeImports(nt.name().getname());
            // If null, check if it was a external packaged
            // type (i.e., something with ::)
            if ( type==null ) {
                if ( nt.name().resolvedPackageAccess!=null ) {
                    Log.log("Package type accessed with ::");
                    type = (Type) nt.name().resolvedPackageAccess;
                } else
                    ; // TODO: Error??
            }
        }
        return type;
    }
    
    @Override
    public Type visitParamDecl(ParamDecl pd) {
        Log.log(pd, "Visiting a paramdecl.");
        if ( pd.type().isArrayType() )
            pd.type().visit(this);
        else if ( pd.type().isChannelType() || pd.type().isChannelEndType() )
            pd.type().visit(this);
        else if ( pd.type().isNamedType() )
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
                if ( rm.type().isNamedType() )
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
            if ( rm.type().isNamedType() )
                rm.children[0] = rm.type().visit(this);
        }
        return null;
    }
}
