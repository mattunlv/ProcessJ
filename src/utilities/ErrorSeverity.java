package utilities;

/**
 * This enum represents various kind of 'severity' error messages
 * produced by the ProcessJ compiler.
 * 
 * @author ben
 * @version 09/02/2018
 * @since 1.2
 */
public enum ErrorSeverity {
    
    INFO ("info"),
    WARNING ("warning"),
    ERROR ("error");
    
    private final String text;
    
    ErrorSeverity(String text) {
        this.text = text;
    }
    
    @Override
    public String toString() {
        return text;
    }
}
