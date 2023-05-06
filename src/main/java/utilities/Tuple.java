package utilities;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

/**
 * A class that represents a sequences of _inmutable_ objects.
 * 
 * @author ben
 */
public class Tuple<T> extends AbstractList<T> {

    private final T[] values;

    public Tuple(T... values) {
        this.values = values;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        final int size = toIndex - fromIndex;
        T[] newValues = (T[]) new Object[size];
        System.arraycopy(values, fromIndex, newValues, 0, size);
        return new Tuple<>(newValues);
    }

    @Override
    public T get(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public boolean isEmpty() {
        return values == null || values.length == 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (final T v : values)
            result = prime * result + v.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Tuple<?>))
            return false;
        Tuple<?> other = (Tuple<?>) o;
        if (size() != other.size())
            return false;
        return Arrays.equals(values, other.values);
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
