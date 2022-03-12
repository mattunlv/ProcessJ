package utilities;

/**
 * This enum allows the ProcessJ compiler to register messages at
 * compile-time or runtime that can be (1) displayed on the screen,
 * (2) not displayed on the screen, or (3) displayed before terminating
 * the execution of the program. A message of type PRINT_CONTINUE
 * instructs the compiler to display a message and resume program
 * execution; a type PRINT_STOP instructs the compiler to display a
 * message and terminate the execution of the program; and a type
 * DONT_PRINT_CONTINUE instructs the compiler to resume program
 * execution at the point where the program last stopped.
 * 
 * @author Ben
 * @version 11/05/2018
 * @since 1.2
 */
public enum MessageType {
    
    PRINT_CONTINUE,
    PRINT_STOP,
    DONT_PRINT_CONTINUE;
}
