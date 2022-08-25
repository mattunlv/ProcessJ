package std;

public class strings {
    
    public static char charAt(String str, int pos) {
        return str.charAt(pos);
    }
    
    public static int length(String str) {
        return str.length();
    }
    
    public static boolean isEmpty(String str) {
        return str.isEmpty();
    }
    
    public static boolean equals(String s1, String s2) {
        return s1.equals(s2);
    }
    
    public static boolean equalsIgnoreCase(String s1, String s2) {
        return s1.equalsIgnoreCase(s2);
    }
    
    public static int compareTo(String s1, String s2) {
        return s1.compareTo(s2);
    }
    
    public static int compareToIgnoreCase(String s1, String s2) {
        return s1.compareToIgnoreCase(s2);
    }
    
    public static boolean startsWith(String s1, String prefix) {
        return s1.startsWith(prefix);
    }
    
    public static boolean startsWith(String s1, String prefix, int offset) {
        return s1.startsWith(prefix, offset);
    }
    
    public static boolean endsWith(String str, String suffix) {
        return str.startsWith(suffix, str.length() - suffix.length());
    }
    
    public static int indexOf(String str, int ch) {
        return str.indexOf(ch);
    }
    
    public static int indexOf(String str, int ch, int fromIndex) {
        return str.indexOf(ch, fromIndex);
    }
    
    public static int indexOf(String str, String s) {
        return str.indexOf(s);
    }
    
    public static int indexOf(String str, String s, int fromIndex) {
        return str.indexOf(s, fromIndex);
    }
    
    public static int lastIndexOf(String str, String s) {
        return str.lastIndexOf(s);
    }
    
    public static int lastIndexOf(String str, int ch) {
        return str.lastIndexOf(ch);
    }
    
    public static int lastIndexOf(String str, int ch, int fromIndex) {
        return str.lastIndexOf(ch, fromIndex);
    }
    
    public static String substring(String str, int beginIndex) {
        return str.substring(beginIndex);
    }
    
    public static String substring(String str, int beginIndex, int endIndex) {
        return str.substring(beginIndex, endIndex);
    }
    
    public static String concat(String s1, String s2) {
        return s1.concat(s2);
    }
    
    public static String replace(String str, char oldChar, char newChar) {
        return str.replace(oldChar, newChar);
    }
    
    public static boolean matches(String str, String regex) {
        return str.matches(regex);
    }
    
    public static String replaceFirst(String str, String regex, String replacement) {
        return str.replaceFirst(regex, replacement);
    }
    
    public static String replaceAll(String str, String regex, String replacement) {
        return str.replaceAll(regex, replacement);
    }
    
    public static String[] split(String str, String regex) {
        return str.split(regex);
    }
    
    public static String[] split(String str, String regex, int limit) {
        return str.split(regex, limit);
    }
    
    public static String toLowerCase(String str) {
        return str.toLowerCase();
    }
    
    public static String toUpperCase(String str) {
        return str.toUpperCase();
    }
    
    public static String trim(String str) {
        return str.trim();
    }
    
    public static char[] toCharArray(String str) {
        return str.toCharArray();
    }
}
