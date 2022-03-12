package processj.runtime;

/**
 * @author Ben
 * @version 07/02/19
 * @since 1.2
 *
 * @param <T>
 */
public class PJChannel<T> {
    
    protected T data;
    
    protected PJChannelType type;
    
    //
    // The methods below must be defined by the parent
    // class of all channel types
    //
    
    // ************************************
    // One-2-One Channel
    // ************************************
    public void write(PJProcess p, T data) { }
    
    public T read(PJProcess p) { return (T) null; }
    
    public boolean isReadyToRead(PJProcess p) { return false; }
    
    public boolean isReadyToWrite() { return false; }
    
    public T preReadRendezvous(PJProcess p) { return (T) null; }
    
    public void postReadRendezvous(PJProcess p) { }
    
    //
    // The methods below must be overridden by the appropriate
    // subclass (channel type)
    //
    
    // ************************************
    // One-2-Many Channel: Shared read end
    // ************************************
    public boolean claimRead(PJProcess p) {
        return false;
    }
    
    public void unclaimRead() {
        // empty on purpose
    }
    
    // ************************************
    // Many-2-One Channel: Shared read end
    // ************************************
    public boolean claimWrite(PJProcess p) {
        return false;
    }
    
    public void unclaimWrite() {
        // empty on purpose
    }
    
    // ************************************
    // Alternations (long for 'alt')
    // ************************************
    public PJProcess altGetWriter(PJProcess p) { return (PJProcess) null; }
    
    public PJProcess setReaderGetWriter(PJProcess p) { return (PJProcess) null; }
}
