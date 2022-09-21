package rewriters;

import ast.AST;
import ast.ArrayLiteral;
import ast.ArrayType;
import ast.Expression;
import ast.Literal;
import ast.LocalDecl;
import ast.NewArray;
import ast.Sequence;
import utilities.Log;
import utilities.Visitor;

public class ArraysRewrite extends Visitor<AST> {
	
	public ArraysRewrite() {
        Log.logHeader("****************************************");
        Log.logHeader("       A R R A Y S   R E W R I T E      ");
        Log.logHeader("****************************************");
	}
	
	// DONE
	@Override
	public AST visitLocalDecl(LocalDecl ld) {
		Log.log(ld, "Attempting to rewrite array");
		
		if (ld.type().isArrayType() && ld.var().init() != null && ld.var().init() instanceof Literal) {
			int depth = ((ArrayType) ld.type()).getDepth();
			Sequence<AST> se = new Sequence<AST>();
			for (int i = 0; i < depth; ++i)
				se.append(null);
			ld.var().children[1] = new NewArray(((ArrayType) ld.type()).baseType(),
			                                    new Sequence<Expression>() /* dimsExpr */,
			                                    se, ((ArrayLiteral) ld.var().init()));
		}
		
		return null;
	}
}
