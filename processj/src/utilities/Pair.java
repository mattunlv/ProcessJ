package utilities;

/**
 * A class that represents a pair of two elements of the same or
 * different data type. Note, while a tuple has _inmutable_ values,
 * an element of a Pair object can be made _mutable_.
 * 
 * @author ben
 *
 * @param <K> key
 * @param <V> value
 */
public class Pair<K, V> extends Tuple<Object> {
    
    /** The key. */
    private K key;
    /** The value. */
    private V value;
    /** The number of elements */
    private final int N;
    
    public Pair(final K key, final V value) {
        super(new Object[] { key, value });
        this.key = key;
        this.value = value;
        N = 2;
    }
    
    @Override
    public int size() {
        return N;
    }
    
    @Override
    public Object get(int index) {
        Object val = null;
        if (index == 0)
            val = get(0);
        else if (index == 1)
            val = get(1);
        else if (index >= 2)
            throw new IndexOutOfBoundsException("Index out of range.");
        return val;
    }
    
    public K getFirst() {
        return key;
    }
    
    public V getSecond() {
        return value;
    }
    
    public K getKey() {
        return key;
    }
    
    public V getValue() {
        return value;
    }
    
    public void setFirst(K key) {
        this.key = key;
    }
    
    public void setSecond(V value) {
        this.value = value;
    }
    
    public void setKey(K key) {
        this.key = key;
    }
    
    public void setValue(V value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return String.format("(%s, %s)", key, value);
    }
}
