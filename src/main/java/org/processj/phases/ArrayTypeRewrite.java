package org.processj.phases;

import org.processj.Phase;
import org.processj.ast.*;
import org.processj.utilities.Visitor;

/**
 * <p>Rewrite {@link Visitor} that traverses the {@link Compilation} & converts any {@link ArrayType} to a
 * canonical form since array dimensions are syntactically valid in front of names. For Example:
 *      int[] a[]; is transformed to int[][] a;
 * In addition, all {@link ArrayType}s are made to consist of just on set of brackets and a component type.</p>
 * @see ArrayType
 * @author Jan B. Pedersen
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class ArrayTypeRewrite extends Phase {

    /// ----------------------
    /// Private Static Methods

    /**
     * <p>Constructs an {@link ArrayType} from the specified {@link Type} & depth only if the depth is greater than
     * 0.</p>
     * @param type The {@link Type} to construct a consolidated {@link ArrayType}.
     * @param nameDepth The integer value corresponding with the amount of brackets appended to the name.
     * @return {@link ArrayType} or identity {@link Type}.
     * @see Name
     * @see Type
     * @see ArrayType
     * @since 0.1.0
     */
    private static Type ConstructArrayTypeFrom(final Type type, final int nameDepth) {

        // Initialize the name depth & resultant Type
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

    /**
     * <p>Initializes the {@link ArrayTypeRewrite} to its' default state with the specified {@link Phase.Listener}.</p>
     * @param listener The {@link Phase.Listener} that receives any {@link Phase.Message}, {@link Phase.Warning},
     *                 or {@link Phase.Error} messages.
     * @since 0.1.0
     */
    public ArrayTypeRewrite(final Phase.Listener listener) {
        super(listener);
    }

    /// ------------------------------
    /// org.processj.utilities.Visitor

    /**
     * <p>Resets the {@link Type} bound to the {@link ParamDecl}. If the {@link ParamDecl}'s {@link Type} &
     * {@link Name} depth combined are greater than the {@link ArrayType}'s depth, an {@link ArrayType} will be
     * constructed and the {@link ParamDecl}'s {@link Type} subsequently mutated.</p>
     * @param parameterDeclaration The {@link ParamDecl} to mutate.
     * @since 0.1.0
     */
    @Override
    public final Void visitParamDecl(final ParamDecl parameterDeclaration) {

        // Update the Type
        parameterDeclaration.setType(
                ConstructArrayTypeFrom(parameterDeclaration.getType(), parameterDeclaration.getDepth()));

        return null;

    }

    /**
     * <p>Resets the {@link Type} bound to the {@link LocalDecl}. If the {@link LocalDecl}'s {@link Type} &
     * {@link Name} depth combined are greater than the {@link ArrayType}'s depth, an {@link ArrayType} will be
     * constructed and the {@link LocalDecl}'s {@link Type} subsequently mutated.</p>
     * @param localDeclaration The {@link ParamDecl} to mutate.
     * @since 0.1.0
     */
    @Override
    public final Void visitLocalDecl(final LocalDecl localDeclaration) {

        // Update the Type
        localDeclaration.setType(
                ConstructArrayTypeFrom(localDeclaration.getType(), localDeclaration.getName().getDepth()));

        return null;

    }

    /**
     * <p>Resets the {@link Type} bound to the {@link ConstantDecl}. If the {@link ConstantDecl}'s {@link Type} &
     * {@link Name} depth combined are greater than the {@link ArrayType}'s depth, an {@link ArrayType} will be
     * constructed and the {@link ConstantDecl}'s {@link Type} subsequently mutated.</p>
     * @param constantDeclaration The {@link ConstantDecl} to mutate.
     * @since 0.1.0
     */
    @Override
    public final Void visitConstantDecl(final ConstantDecl constantDeclaration) {

        // Update the Type
        constantDeclaration.setType(
                ConstructArrayTypeFrom(constantDeclaration.getType(), constantDeclaration.getName().getDepth()));

        return null;

    }

}