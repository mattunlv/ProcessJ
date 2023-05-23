package org.processj.rewriters;

import org.processj.ast.AST;
import org.processj.ast.ArrayType;
import org.processj.ast.Assignment;
import org.processj.ast.BinaryExpr;
import org.processj.ast.Block;
import org.processj.ast.ExprStat;
import org.processj.ast.Expression;
import org.processj.ast.LocalDecl;
import org.processj.ast.Name;
import org.processj.ast.NameExpr;
import org.processj.ast.NewArray;
import org.processj.ast.PrimitiveLiteral;
import org.processj.ast.PrimitiveType;
import org.processj.ast.ProcTypeDecl;
import org.processj.ast.Sequence;
import org.processj.ast.Statement;
import org.processj.ast.Token;
import org.processj.ast.UnaryPreExpr;
import org.processj.ast.Var;
import org.processj.codegen.Helper;
import org.processj.codegen.Tag;
import org.processj.utilities.Tuple;

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
    
    private Object createNewArray(LocalDecl ld, NewArray na) {
//        | NEW channel_type:ct dims:d array_initializer:i
//        {: RESULT = new NewArray(ct, new Sequence<Expression>(), d, i); :}
        Sequence<AST> dims = na.dims();
        for (int i = 0; i < dims.size(); ++i) { 
        }
        return null;
    }
    
    int index = 0;
    
    private Tuple<?> createLocalDeclForLoop(String dims) {
        final String localDeclName = Helper.makeVariableName("loop", index++, Tag.LOCAL_NAME).replace("_ld$", "");
        Name n = new Name(localDeclName);
        NameExpr ne = new NameExpr(n);
        PrimitiveLiteral pl = new PrimitiveLiteral(new Token(0, "0", 0, 0, 0), 4 /* kind */);
        LocalDecl ld = new LocalDecl(
                new PrimitiveType(PrimitiveType.IntKind),
                new Var(n, null),
                false /* not constant */);
        BinaryExpr be = new BinaryExpr(ne, new NameExpr(new Name(dims)), BinaryExpr.LT);
        ExprStat es = new ExprStat(new UnaryPreExpr(ne, UnaryPreExpr.PLUSPLUS));
        Sequence<Statement> init = new Sequence<>();
        init.append(new ExprStat((Expression) new Assignment(ne, pl, Assignment.EQ)));
        Sequence<ExprStat> incr = new Sequence<>();
        incr.append(es);
        return new Tuple(init, be, incr, ld);
    }
}