package org.processj.compiler.utilities;

/**
 * This class is used for ansi color code manipulation on a terminal
 * console that supports ansi color codes.
 * 
 * Usage:
 *   ANSI_PREFIX + (Attribute | Attribute + ANSI_COMMA +
 *   (AnsiForeground | AnsiBackground)) + ANSI_POSTFIX
 * 
 * Example:
 *   1.) \033[ + 0 + m = \033[0m (Ansi Reset)
 *   2.) \033[ + 1 + ";" + 31 + "m" = \033[1;31m (Ansi RED)
 * 
 * @author ben
 * @version 10/06/2018
 * @since 1.2
 */
public class ANSICode {
    
    public static final String Prefix       = "\033[";
    public static final String Suffix       = "m";
    public static final String Semicolon    = ";";
    public static final String Reset        = "\033[0m";
    
    /**
     * The enum AnsiForeground represents each ansi foreground color code.
     * 
     * @author ben
     * @version 10/06/2018
     * @since 1.2
     */
    public enum ANSIForeground {
        
        BLACK ("30"),
        RED ("31"),
        GREEN ("32"),
        YELLOW ("33"),
        BLUE ("34"),
        MAGENTA ("35"),
        CYAN ("36"),
        WHITE ("37"),
        NONE ("");
        
        private String code;
        
        ANSIForeground(String code) {
            this.code = code;
        }
        
        @Override
        public String toString() {
            return code;
        }
    }
    
    /**
     * The enum AnsiForeground represents each ansi background color code.
     * 
     * @author ben
     * @version 10/06/2018
     * @since 1.2
     */
    public enum ANSIBackground {
        
        BLACK ("40"),
        RED ("41"),
        GREEN ("42"),
        YELLOW ("43"),
        BLUE ("44"),
        MAGENTA ("45"),
        CYAN ("46"),
        WHITE ("47"),
        NONE ("");
        
        private String code;
        
        ANSIBackground(String code) {
            this.code = code;
        }
        
        @Override
        public String toString() {
            return code;
        }
    }
    
    /**
     * The enum Attribute represents each ansi attribute color code.
     * 
     * @author ben
     * @version 10/06/2018
     * @since 1.2
     */
    public enum Attribute {
        
        DEFAULT ("0"),
        BOLD ("1"),
        LIGHT ("1"),
        DARK ("2"),
        UNDERLINE ("4"),
        HIDDEN ("8"),
        NONE ("");
        
        private String code;
        
        Attribute(String code) {
            this.code = code;
        }
        
        @Override
        public String toString() {
            return code;
        }
    }
    
    public static String setColor(String tag, PJMessage.ErrorSeverity severity) {
        StringBuilder sb = new StringBuilder();
        sb.append(Prefix);
        sb.append(Attribute.BOLD);
        sb.append(Semicolon);
        switch (severity) {
        case WARNING:
            sb.append(ANSIForeground.YELLOW);
            break;
        case ERROR:
            sb.append(ANSIForeground.RED);
            break;
        default:
            break;
        }
        sb.append(Suffix);
        sb.append(tag);
        sb.append(Reset);
        return sb.toString();
    }
}
