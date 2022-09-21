package utilities;

/**
 * This interface declares and defines methods that when building
 * useful compiler messages would enforce enums to provide an
 * implementation for getMessage(), and for getErrorSeverity()
 * or getNumber() and getMessageType() if needed.
 * 
 * @author ben
 * @version 10/21/2018
 * @since 1.2
 */
public interface MessageNumber {
    
    public static final int UNKNOWN = 0;
    
    default ErrorSeverity getErrorSeverity() {
        return ErrorSeverity.ERROR;
    }
    
    default int getNumber() {
        return UNKNOWN;
    }
    
    default String getMessage() {
        return "<empty message>";
    }
    
    // If none is given then abort/terminate
    default MessageType getMessageType() {
        return MessageType.PRINT_STOP;
    }
}
