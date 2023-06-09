package org.processj.compiler.utilities;

import java.util.Collection;

/**
 * Utility class.
 * 
 * @author ben
 */
public final class PJUtil {
    
    /**
     * Returns 'true' if the parameter is null, or if the runtime class
     * of the parameter is not an array type, or if the length of the
     * parameter (after being cast to an array type) is zero, or if all
     * elements in the array are null.
     *
     * @throws IllegalArgumentException
     *             When the class type of parameter is not an array class.
     * @return true if the parameter parameter represents an empty
     *         array, or false otherwise.
     */
    public static String join(Collection<String> src, CharSequence delimiter) {
        StringBuilder sb = new StringBuilder();
        boolean delim = true;
        for (String str : src) {
            if (delim)
                delim = false;
            else
                sb.append(delimiter);
            sb.append(str);
        }
        return sb.toString();
    }
    
    public static String addChar(char ch, int n) {
        StringBuilder sb = new StringBuilder();
        while (--n >= 0)
            sb.append(ch);
        return sb.toString();
    }
}
