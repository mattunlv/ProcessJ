package utilities;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * A helper iterator for arrays.
 * 
 * @author ben
 * @version 11/04/2018
 * @since 1.2
 */
public class ArrayTypeIterator implements Iterator<Object> {
    
    private Object array;
    private int position;
    private int size;
    
    public ArrayTypeIterator(Object array) {
        this.array = Assert.nonNull(array, "ArrayTypeIterator cannot be " + 
                "used with a 'null' array.");
        size = Array.getLength(array);
        position = -1;
    }

    @Override
    public boolean hasNext() {
        if (position >= 0 && size > 0)
            return true;
        return false;
    }

    @Override
    public Object next() {
        if (!hasNext())
            throw new RuntimeException("Index out of bound!");
        return Array.get(array, position++);
    }
}
