package org.processj.phases;

import org.processj.Phase;
import org.processj.ast.*;
import org.processj.ast.expression.ArrayLiteral;
import org.processj.ast.expression.Expression;
import org.processj.utilities.Log;
import org.processj.utilities.Visitor;

/**
 * <p>Rewrite {@link Visitor} that transforms a {@link Compilation}'s {@link ArrayLiteral} {@link Expression}s
 * into {@link NewArray} {@link Expression}s.</p>
 * @see Statement
 * @author Jan B. Pedersen
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class ArraysRewrite extends Phase {

	/// -----------
	/// Constructor

	/**
	 * <p>Initializes the {@link ArraysRewrite} to its' default state with the specified {@link Phase.Listener}.</p>
	 * @param listener The {@link Phase.Listener} that receives any {@link Phase.Message}, {@link Phase.Warning},
	 *                 or {@link Phase.Error} messages.
	 * @since 0.1.0
	 */
	public ArraysRewrite(final Phase.Listener listener) {
		super(listener);
	}

	/// ------------------------------
	/// org.processj.utilities.Visitor

	/**
	 * <p>Transforms the {@link LocalDecl}'s {@link ArrayLiteral} into a {@link NewArray} {@link Expression}.</p>
	 * @param localDeclaration The {@link LocalDecl} to mutate.
	 * @since 0.1.0
	 */
	@Override
	public final Void visitLocalDecl(final LocalDecl localDeclaration) {

		Log.log(localDeclaration, "Attempting to rewrite array");

		// If the Local Declaration's Type is an ArrayType and it's initialized with an ArrayLiteral
		if((localDeclaration.getType() instanceof ArrayType)
				&& (localDeclaration.getInitializationExpression() instanceof Literal)) {

			// Initialize a handle to the depth, a new Sequence, & the LocalDeclaration's component type
			final Sequence<AST> dims 			= new Sequence<>()												;
			final Type 			componentType 	= ((ArrayType) localDeclaration.getType()).getComponentType()	;
			final int 			depth 			= ((ArrayType) localDeclaration.getType()).getDepth()			;

			// Construct the dummy dims
			for(int index = 0; index < depth; index++)
				dims.append(null);

			// Set the new parse tree node
			localDeclaration.setInitializationExpression(
					new NewArray(componentType, new Sequence<>(),
							dims, (ArrayLiteral) localDeclaration.getInitializationExpression()));

		}
		
		return null;

	}

}
