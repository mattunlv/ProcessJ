package org.processj.compiler.utilities;

import org.processj.compiler.ast.AST;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This class is used to create generic messages for the ProcessJ compiler.
 * 
 * @author ben
 * @since 1.2
 */
public class PJMessage {

    /** String template file locator */
    protected static final String ERROR_FILE = "src/main/resources/stringtemplates/messages/errorTemplate.stg";

    /** Template for error messages */
    protected static final STGroup stGroup = new STGroupFile(ERROR_FILE);

    /** Current running AST */
    protected final AST ast;

    /** Type of error message */
    protected final MessageNumber errorNumber;

    /** Attributes used in templates */
    protected final Object[] arguments;

    /** Reason for the error message */
    protected final Throwable throwable;

    /** Source of the message */
    protected final String fileName;

    /** Location of the input file */
    protected final String packageName;

    /** Line in file */
    protected int rowNum;

    /** Character that generated the error/warning */
    protected int colNum;
    private boolean doTrace;
    
    public PJMessage(Builder builder) {

        ast = builder.ast;
        errorNumber = builder.error;
        arguments = builder.arguments;
        throwable = builder.throwable;
        fileName = builder.fileName;
        packageName = builder.packageName;

    }

    public AST ast() {
        return ast;
    }

    public MessageNumber getMessageNumber() {
        return errorNumber;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getRowNumber() {
        return rowNum;
    }

    public int getColNumber() {
        return colNum;
    }

    public ST getST() {
        int argCount = 0;
        ST message = new ST(stGroup.getInstanceOf("Report"));
        if (errorNumber != null)
            message = new ST(errorNumber.getMessage());
        if (arguments != null && arguments.length > 0)
            argCount = arguments.length;
        if (errorNumber != null)
            for (int i = 0; i < argCount; ++i)
                message.add("arg" + i, arguments[i]);
        else
            message.add("messages", arguments);
        ST stFile = stGroup.getInstanceOf("File");
        ST stTag = stGroup.getInstanceOf("Tag");
        ST stStackInfo = stGroup.getInstanceOf("StackInfo");
        ST stMessage = stGroup.getInstanceOf("Message");
        ST stInfoMessage = stGroup.getInstanceOf("InfoMessage");
        
        if (ast != null) {
            stFile.add("fileName", fileName);
            stFile.add("lineNumber", ast);
        }
        if (errorNumber != null) {
            //errno = errorNumber.getErrorSeverity();
            stTag.add("tag", errorNumber.getErrorSeverity());
            stTag.add("number", errorNumber.getNumber());
        }
        if (throwable != null) {
            stStackInfo.add("reason", throwable);
            stStackInfo.add("stack", throwable.getStackTrace());
        }
        if (ast != null) {
            stInfoMessage.add("token", ast);
//            stInfoMessage.add("info", ASTStringCompiler.INSTANCE.codeAnalysis(org.processj.ast, info, link, errno));
        }
        // Apply color code if allowed on terminal
        String tag = stTag.render();
        //if (Compiler.ShowColor)
            //tag = ANSICode.setColor(tag, errno);
        
        stMessage.add("tag", tag);
        stMessage.add("message", message.render());
        stMessage.add("info", stInfoMessage.render());
        stMessage.add("location", stFile.render());
        stMessage.add("stack", stStackInfo.render());
        
        return stMessage;
    }

    public String getRenderedMessage() {
        ST stResult = getST();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(stResult.render());
        if (doTrace && throwable != null)
            stringBuilder.append(throwable);
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "(filename="        + (fileName.isEmpty() ? "none" : fileName) +
                ", package="        + (packageName.isEmpty() ? "none" : packageName) +
                ", errorNumber="    + errorNumber.getNumber() +
                ", errorMessage="   + errorNumber.getMessage() +
                ", arguments="      + (arguments != null? "{" +
                Arrays.stream(arguments)
                        .map(arg -> arg + "")
                        .collect(Collectors.joining(",")) + "}"
                : "none") +
                ", reason="         + (throwable != null?
                throwable.getMessage()
                : "none") +
                ", row="            + rowNum +
                ", column="         + colNum +
                ")";
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int hash = 1;
        hash = prime * hash + ast.hashCode();
        hash = prime * hash + errorNumber.hashCode();
        hash = prime * hash + Arrays.hashCode(arguments);
        hash = prime * hash + throwable.hashCode();
        hash = prime * hash + fileName.hashCode();
        hash = prime * hash + packageName.hashCode();
        hash = prime * hash + rowNum;
        hash = prime * hash + colNum;
        return hash;
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        PJMessage other = (PJMessage) obj;
        if (this.rowNum != other.rowNum || this.colNum != other.colNum)
            return false;
        if (!this.fileName.equals(other.fileName) || !this.packageName.equals(other.packageName))
            return false;
        if (this.ast != other.ast) // This should be OK
            return false;
        if (!this.errorNumber.equals(other.errorNumber))
            return false;
        if (!Arrays.equals(this.arguments, other.arguments))
            return false;
        if (!this.throwable.equals(other.throwable))
            return false;
        return true;
    }
    public boolean hasStackTrace() {
        return doTrace;
    }
    
    // *************************************************************************
    // ** BUILDER
    
    /**
     * Builder for this basic error message type.
     * 
     * @author ben
     * @version 10/20/2018
     * @since 1.2
     */
    public static final class Builder {

        private static final Object[] EMPTY_ARGUMENTS = new Object[0];

        protected boolean doTrace;
        protected String info;
        protected String link;
        
        public Builder() {
            ast = null;
            error = null;
            arguments = EMPTY_ARGUMENTS;
            throwable = null;
            fileName = null;
            packageName = null;
        }

        protected Builder builder() {
            return this;
        }

        public <E extends PJMessage> E build() {
            @SuppressWarnings("unchecked")
            E error = (E) new PJMessage(this);
            return error;
        }

        public AST           ast         ;
        public MessageNumber error       ;
        public Object[]      arguments   ;
        public Throwable    throwable       ;
        public String       fileName        ;
        public String       packageName     ;
        public int rowNum;
        public int colNum;

        public Builder addAST(AST ast) {
            this.ast = ast;
            return builder();
        }

        public Builder addError(MessageNumber error) {
            this.error = error;
            return builder();
        }

        public Builder addArguments(Object... arguments) {
            this.arguments = arguments;
            return builder();
        }

        public Builder addThrowable(Throwable throwable) {
            this.throwable = throwable;
            return builder();
        }

        public Builder addFileName(String fileName) {
            this.fileName = fileName;
            return builder();
        }

        public Builder addPackageName(String packageName) {
            this.packageName = packageName;
            return builder();
        }

        public Builder addRowNumber(int rowNum) {
            this.rowNum = rowNum;
            return builder();
        }

        public Builder addColNumber(int colNum) {
            this.colNum = colNum;
            return builder();
        }

    }

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

        int UNKNOWN = 0;

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
}
