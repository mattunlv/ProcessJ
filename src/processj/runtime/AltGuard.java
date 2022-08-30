package processj.runtime;

import java.util.Arrays;

public class AltGuard {
    
    /** Case number */
    private int altCase = -1;
    /** Number of elements in the set */
    private int size;
    /** A set of indices pertaining a replicated alt */
    private Integer[] indices;
    
    private int pointer = 0;
    
    public AltGuard(int size) {
        this.size = size;
        this.indices = new Integer[size];
    }
    
    public AltGuard addAltCaseNumber(int altCase) {
        this.altCase = altCase;
        return this;
    }
    
    public AltGuard addIndex(Integer...numbers) {
        int idx = 0;
        for (Integer num : numbers)
            indices[idx++] = num;
        return this;
    }
    
    public int getIndex(int idx) {
        return (int) indices[idx];
    }
    
    public int getSize() {
        return size;
    }
    
    public int getAltCaseNumber() {
        return altCase;
    }
    
    public int getIndex() {
        return indices[pointer++];
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj==null)
            return false;
        if (obj==this)
            return true;
        if (this.getClass()!=obj.getClass())
            return false;
        AltGuard other = (AltGuard) obj;
        return altCase == other.altCase &&
               size == other.size &&
               Arrays.deepEquals(indices, other.indices);
    }
    
    @Override
    public int hashCode() {
        return altCase * 31 +
               size * 31 +
               Arrays.deepHashCode(indices) * 31;
    }
}
