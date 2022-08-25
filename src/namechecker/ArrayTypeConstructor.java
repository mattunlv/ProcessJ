package namechecker;

import ast.AST;
import ast.ArrayType;
import ast.ConstantDecl;
import ast.LocalDecl;
import ast.Name;
import ast.ParamDecl;
import ast.Type;
import utilities.Log;
import utilities.Visitor;

/**
 * ArrayTypeConstructor traverses the parse tree and converts an array types to a canonical form.
 *
 * For example:
 *
 * int[] a[]; is transformed to int[][] a;
 *
 * In addition all array types are made to consist of just one set of brackets and a base type, so
 *
 * int[][] is an ArrayType with a base type of int[] which is an ArrayType with base type int
 *
 * @author Matt Pedersen
 *
 */
public class ArrayTypeConstructor extends Visitor<AST> {

    public ArrayTypeConstructor() {
        debug = true;
        Log.log("**************************************");
        Log.log("*  A R R A Y   C O N S T R U C T O R *");
        Log.log("**************************************");

    }

    public Type constructArrayType(Type t, Name n) {
        Type array = t;
        if (t instanceof ArrayType || (n != null && n.getArrayDepth() != 0)) {
            if (t instanceof ArrayType) {
                ArrayType at = (ArrayType) t;
                Type baseType = at.baseType();
                array = baseType;
                for (int i = 0; i < at.getDepth(); i++)
                    array = new ArrayType(array, 1);
            }
            if (n != null)
                for (int i = 0; i < n.getArrayDepth(); i++)
                    array = new ArrayType(array, 1);
        }
        return array;
    }

    public AST visitParamDecl(ParamDecl pd) {
        if (pd.type().isArrayType() || pd.paramName().getArrayDepth() > 0) {
            pd.setType(constructArrayType(pd.type(), pd.paramName()));
            pd.paramName().setArrayDepth(0);
        }
        return null;
    }

    public AST visitLocalDecl(LocalDecl ld) {
        if (ld.type().isArrayType() || ld.var().name().getArrayDepth() > 0) {
            ld.setType(constructArrayType(ld.type(), ld.var().name()));
            ld.var().name().setArrayDepth(0);
        }
        return null;
    }

    public AST visitConstantDecl(ConstantDecl cd) {
        if (cd.type().isArrayType() || cd.var().name().getArrayDepth() > 0) {
            cd.setType(constructArrayType(cd.type(), cd.var().name()));
            cd.var().name().setArrayDepth(0);
        }
        return null;
    }

    public AST visitArrayType(ArrayType at) {
        Type t = constructArrayType(at, null);
        at.setBaseType(t);
        return null;
    }
}