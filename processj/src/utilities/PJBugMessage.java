package utilities;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import ast.AST;

/**
 * This class is used to create messages during a tree-traversal node
 * when processing the contents of a ProcessJ file, when processing
 * the syntax and/or semantics errors when compiling or generating
 * Java source code from a ProcessJ file, or when processing command
 * line options and/or arguments.
 * 
 * @author ben
 * @version 10/07/2018
 * @since 1.2
 */
public abstract class PJBugMessage {
    
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];

    /** String template file locator */
    protected static final String ERROR_FILE = "resources/stringtemplates/messages/errorTemplate.stg";
    
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
    
    public PJBugMessage(Builder<?> builder) {
        ast = builder.ast;
        arguments = builder.arguments;
        errorNumber = builder.error;
        throwable = builder.throwable;
        fileName = builder.fileName == null? new File(PJBugManager.INSTANCE.getFileName()).getAbsolutePath() :
                                             new File(builder.fileName).getAbsolutePath();
        packageName = builder.packageName == null? PJBugManager.INSTANCE.getFileName() : builder.packageName;
        rowNum = builder.rowNum;
        colNum = builder.colNum;
    }
    
    // *************************************************************************
    // ** GETTERS
    
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
        return message;
    }
    
    public abstract String getRenderedMessage();
    
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
        
        PJBugMessage other = (PJBugMessage) obj;
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
    
    // *************************************************************************
    // ** BUILDER
    
    /**
     * 
     * The class Builder uses descriptive methods to create error
     * messages with default or initial values.
     * 
     * @author ben
     * @version 10/20/2018
     * @since 1.2
     *
     * @param <B>
     *            The builder type.
     */
    public static abstract class Builder<B> {
        
        protected AST ast;
        protected MessageNumber error;
        protected Object[] arguments;
        protected Throwable throwable;
        protected String fileName;
        protected String packageName;
        protected int rowNum;
        protected int colNum;
        
        public Builder() {
            ast = null;
            error = null;
            arguments = EMPTY_ARGUMENTS;
            throwable = null;
            fileName = null;
            packageName = null;
        }
        
        protected abstract B builder();
        
        public abstract <E extends PJBugMessage> E build();
        
        public B addAST(AST ast) {
            this.ast = ast;
            return builder();
        }
        
        public B addError(MessageNumber error) {
            this.error = error;
            return builder();
        }
        
        public B addArguments(Object... arguments) {
            this.arguments = arguments;
            return builder();
        }
        
        public B addThrowable(Throwable throwable) {
            this.throwable = throwable;
            return builder();
        }
        
        public B addFileName(String fileName) {
            this.fileName = fileName;
            return builder();
        }
        
        public B addPackageName(String packageName) {
            this.packageName = packageName;
            return builder();
        }
        
        public B addRowNumber(int rowNum) {
            this.rowNum = rowNum;
            return builder();
        }
        
        public B addColNumber(int colNum) {
            this.colNum = colNum;
            return builder();
        }
    }
}
