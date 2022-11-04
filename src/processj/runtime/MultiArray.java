package processj.runtime;

import java.util.List;
import java.util.ArrayList;

public class MultiArray {

    /// --------------
    /// Private Fields

    private List array;

    /// -----------
    /// Constructor

    public MultiArray(int ... dimensions) {

        this.array = new ArrayList<>();

        this.allocate(array, 0, dimensions);

    }

    /**
     * Recursively allocates a potentially mult-dimensional array
     */
    private void allocate(final List parent, int dimension, int ... dimensions) {

        if(dimension == dimensions.length - 1)
            for(int index = 0; index < dimensions[dimension]; index++)
                parent.add(new PJOne2OneChannel());

        else for(int index = 0; index < dimensions[dimension]; index++) {

            final List child = new ArrayList<>();

            allocate(child, dimension + 1, dimensions);

            parent.add(child);

        }

    }

    /// --------------
    /// Public Members

    public Object get(int ... indices) {

        Object result = this.array;

        for(int index = 0; index < indices.length; index++)
            result = ((List) result).get(indices[index]);

        return result;

    }


}
