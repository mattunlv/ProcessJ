package processj.runtime;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import utilities.Pair;
import utilities.PJUtil;

/**
 * @author ben
 */
public class PJRecord {
    
    private static Pair<Object, Integer> getField(Map<Pair<Object, Integer>, String> hm, Object o) {
        for (Map.Entry<Pair<Object,Integer>, String> e : hm.entrySet()) {
            Pair<Object, Integer> p = (Pair<Object, Integer>) e.getKey();
            if (p.getFirst() == o)
                return p;
        }
        return null;
    }
    
    public String toString(Map<Pair<Object, Integer>, String> hm, int count) {
        if (hm == null)
            hm = new HashMap<>();
        Class<? extends PJRecord> c = getClass();
        StringBuilder sb = new StringBuilder("[");
        try {
            Field[] fields = c.getFields();
            for (int i = 0; i < fields.length; ++i) {
                Object o = fields[i].get(this);
                if (o instanceof PJRecord) {
                    Pair<Object, Integer> tuple = getField(hm, o);
                    if (tuple != null) {
                        String fname = PJUtil.addChar('*', ++count);
                        sb.append(fields[i].getName());
                        sb.append(":");
                        sb.append(fname);
                        tuple.setValue(count);
                    } else {
                        tuple = new Pair<>(o, 0);
                        hm.put(tuple, fields[i].getName());
                        String s = ((PJRecord) o).toString(hm, count);
                        tuple = getField(hm, o);
                        if (tuple != null) {
                            String fname = PJUtil.addChar('*', tuple.getValue());
                            s = fname + s;
                        }
                        sb.append(fields[i].getName());
                        sb.append(":");
                        sb.append(s);
                    }
                } else {
                    sb.append(fields[i].getName());
                    sb.append(":");
                    sb.append(o);
                }
                if (i+1 != fields.length)
                    sb.append(", ");
            }
        } catch (Exception e) {
            // An exception should never be thrown because the members of a
            // PJRecord are 'public' by default!
            System.out.println("Failed to access field of PJRecord");
            System.exit(1);
        }
        sb.append("]");
        return sb.toString();
    }
}
