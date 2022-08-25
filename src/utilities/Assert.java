package utilities;

import java.util.Collection;

/**
 * The class Assert provides a convenient way for checking 'null'
 * objects.
 * 
 * @author ben
 * @version 08/06/2018
 * @since 1.2
 */
public class Assert {
    
    /**
     * Throws a NullPointerException if a given object is null.
     * 
     * @param obj
     *          The object to be checked.
     * @return The same object.
     */
    public static <T> T noNull(T obj) {
        return nonNull(obj, "null object!");
    }

    /**
     * Throws a NullPointerException if a given object is null.
     * 
     * @param obj
     *          The object to be checked.
     * @param msg
     *          The detail message.
     * @return The same object.
     */
    public static <T> T nonNull(T obj, String msg) {
        if (obj == null)
            throw new NullPointerException(msg);
        return obj;
    }
    
    /**
     * Throws a NullPointerException if a given object is null.
     * 
     * @param obj
     *          The object to be checked.
     * @param args
     *          A collection of messages.
     * @return The same object.
     */
    public static <T> T nonNull(T obj, Collection<String> args) {
        return nonNull(obj, PJUtil.join(args, ", "));
    }
}
