package rewriters;

import ast.AST;
import ast.ArrayType;
import ast.Block;
import ast.Expression;
import ast.LocalDecl;
import ast.NewArray;
import ast.ProcTypeDecl;
import ast.Sequence;
import ast.Statement;
import utilities.Log;
import utilities.Visitor;

public class ChannelArrayDeclRewrite {
    
    public void go(AST a) {
        if ( a instanceof ProcTypeDecl ) {
            // Rewrite the body
            ProcTypeDecl pd = (ProcTypeDecl) a;
            go(pd.body());
        } else if ( a instanceof Sequence ) { 
            Sequence<?> seq = (Sequence<?>) a;
            for (int i = 0; i < seq.size(); ++i) {
                if ( seq.child(i) != null && seq.child(i) instanceof Statement ) {
                    Statement stat = (Statement) seq.child(i);
                    if ( stat instanceof Block ) {
                        Block b = (Block) stat;
                        if ( b != null ) go(b.stats());
                    } else if ( stat instanceof LocalDecl ) {
                        LocalDecl ld = (LocalDecl) stat;
                        if ( ld.type().isArrayType() ) {
                            ArrayType type = (ArrayType) ld.type();
                            if ( type.getActualBaseType().isChannelType() || type.getActualBaseType().isChannelEndType() ) {
                                // Rewrite the local declaration 
                                System.out.println(">>>>>>>> " + ld.name());
                            }
                        }
                    }
                } else if (seq.child(i) != null)
                    go(seq.child(i));
            }
        } else {
            for (int i = 0; i < a.nchildren; ++i) {
                if (a.children[i] != null)
                    go(a.children[i]);
            }
        }
    }
    
    private Object createNewArray(LocalDecl ld, ArrayType type) {
//        | NEW channel_type:ct dims:d array_initializer:i
//        {: RESULT = new NewArray(ct, new Sequence<Expression>(), d, i); :}
        int dims = type.getDepth();
        Sequence<Expression> init = new Sequence<>();
        for (int i = 0; i < dims; ++i) {
            //
        }
        NewArray newArray = null;
        return null;
    }
}
