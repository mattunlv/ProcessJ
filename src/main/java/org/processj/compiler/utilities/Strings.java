package org.processj.compiler.utilities;

public class Strings {

    /**
     * <p>Returns a {@link String} value containing the specified amount of spaces.</p>
     * @param length The integer value corresponding to the length of the blank {@link String}.
     * @return blank {@link String}
     * @since 0.1.0
     */
    public static String BlankStringOf(final int length) {

        // Initialize the StringBuilder
        final StringBuilder stringBuilder = new StringBuilder();

        // Iterate
        int index = 1; while(index++ < length) stringBuilder.append(' ');

        // Return the result
        return stringBuilder.toString();

    }

}
