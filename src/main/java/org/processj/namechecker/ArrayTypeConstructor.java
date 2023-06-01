package org.processj.namechecker;

import org.processj.ast.AST;
import org.processj.ast.ArrayType;
import org.processj.ast.ConstantDecl;
import org.processj.ast.LocalDecl;
import org.processj.ast.Name;
import org.processj.ast.ParamDecl;
import org.processj.ast.Type;
import org.processj.utilities.Visitor;

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

    /// ----------------------
    /// Private Static Methods

    /**
     * <p>Constructs an {@link ArrayType} from the specified {@link Type} & {@link Name} only if the {@link Name} has
     * a depth greater than 0.</p>
     * @param type The {@link Type} to construct a consolidated {@link ArrayType}.
     * @param name The {@link Name} that potentially has a depth value defined.
     * @return {@link ArrayType} or identity {@link Type}.
     * @see Name
     * @see Type
     * @see ArrayType
     * @since 0.1.0
     */
    private static Type ConstructArrayTypeFrom(final Type type, final Name name) {

        // Initialize the name depth & resultant Type
        final int nameDepth = (name != null) ? name.getArrayDepth() : 0;
        Type result = type;

        // If the name had some depth
        if(nameDepth > 0) {

            // If the specified Type is an ArrayType
            if(type instanceof ArrayType) {

                // Initialize a handle to the ArrayType
                final ArrayType arrayType = (ArrayType) type;

                // Instantiate the result
                result = new ArrayType(arrayType.getComponentType(), arrayType.getDepth() + nameDepth);

                // Otherwise
            } else {

                // Instantiate a new ArrayType with the specified depth
                result = new ArrayType(type, nameDepth);

            }

        }

        // Return the result
        return result;

    }

    /// ------------
    /// Constructors

    public ArrayTypeConstructor() { /* Empty */ }

    /// ------------------------------
    /// org.processj.utilities.Visitor

    @Override
    public AST visitParamDecl(ParamDecl parameterDeclaration) {

        parameterDeclaration.setType(
                ConstructArrayTypeFrom(parameterDeclaration.type(), parameterDeclaration.paramName()));

        return null;

    }

    @Override
    public AST visitLocalDecl(LocalDecl localDeclaration) {

        localDeclaration.setType(
                ConstructArrayTypeFrom(localDeclaration.type(), localDeclaration.var().name()));

        return null;

    }

    @Override
    public AST visitConstantDecl(ConstantDecl cd) {

        cd.setType(ConstructArrayTypeFrom(cd.type(), cd.var().name()));

        return null;

    }

}