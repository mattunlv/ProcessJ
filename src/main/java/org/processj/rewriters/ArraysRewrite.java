package org.processj.rewriters;

import org.processj.ast.AST;
import org.processj.ast.ArrayLiteral;
import org.processj.ast.ArrayType;
import org.processj.ast.Expression;
import org.processj.ast.Literal;
import org.processj.ast.LocalDecl;
import org.processj.ast.NewArray;
import org.processj.ast.Sequence;
import org.processj.utilities.Log;
import org.processj.utilities.Visitor;

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
		
		if ((ld.type() instanceof ArrayType) && ld.var().init() != null && ld.var().init() instanceof Literal) {
			int depth = ((ArrayType) ld.type()).getDepth();
			Sequence<AST> se = new Sequence<>();
			for(int i = 0; i < depth; ++i)
				se.append(null);
			ld.var().children[1] = new NewArray(((ArrayType) ld.type()).getComponentType(),
			                                    new Sequence<>() /* dimsExpr */,
			                                    se, ((ArrayLiteral) ld.var().init()));
		}
		
		return null;
	}
}
