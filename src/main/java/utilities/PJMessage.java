package utilities;

import org.stringtemplate.v4.ST;

/**
 * This class is used to create generic messages for the ProcessJ compiler.
 * 
 * @author ben
 * @since 1.2
 */
public class PJMessage extends PJBugMessage {
    
    private boolean doTrace;
    private String info;
    private String link;
    
    public PJMessage(Builder builder) {
        super(builder);
        doTrace = builder.doTrace;
        info = builder.info;
        link = builder.link;
    }
    
    @Override
    public ST getST() {
        ErrorSeverity errno = ErrorSeverity.ERROR;
        ST stFile = stGroup.getInstanceOf("File");
        ST stTag = stGroup.getInstanceOf("Tag");
        ST stStackInfo = stGroup.getInstanceOf("StackInfo");
        ST stMessage = stGroup.getInstanceOf("Message");
        ST stInfoMessage = stGroup.getInstanceOf("InfoMessage");
        
        if (ast != null) {
            stFile.add("fileName", fileName);
            stFile.add("lineNumber", ast.line);
        }
        if (errorNumber != null) {
            errno = errorNumber.getErrorSeverity();
            stTag.add("tag", errorNumber.getErrorSeverity());
            stTag.add("number", errorNumber.getNumber());
        }
        if (throwable != null) {
            stStackInfo.add("reason", throwable);
            stStackInfo.add("stack", throwable.getStackTrace());
        }
        if (ast != null) {
            stInfoMessage.add("token", ast.ctx);
//            stInfoMessage.add("info", ASTStringCompiler.INSTANCE.codeAnalysis(ast, info, link, errno));
        }
        // Apply color code if allowed on terminal
        String tag = stTag.render();
        if (Settings.showColor)
            tag = ANSICode.setColor(tag, errno);
        
        stMessage.add("tag", tag);
        stMessage.add("message", super.getST().render());
        stMessage.add("info", stInfoMessage.render());
        stMessage.add("location", stFile.render());
        stMessage.add("stack", stStackInfo.render());
        
        return stMessage;
    }
    
    @Override
    public String getRenderedMessage() {
        ST stResult = getST();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(stResult.render());
        if (doTrace && throwable != null)
            stringBuilder.append(throwable.toString());
        return stringBuilder.toString();
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
    public static final class Builder extends PJBugMessage.Builder<Builder> {
        
        protected boolean doTrace;
        protected String info;
        protected String link;
        
        public Builder() {
            doTrace = false;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public <E extends PJBugMessage> E build() {
            @SuppressWarnings("unchecked")
            E error = (E) new PJMessage(this);
            return error;
        }
        
        public Builder addStackTrace(boolean doTrace) {
            this.doTrace = doTrace;
            return builder();
        }
        
        public Builder addCodeAnalysis(String info) {
            this.info = info;
            return builder();
        }
        
        public Builder addErrorLink(String link) {
            this.link = link;
            return builder();
        }
    }
}
