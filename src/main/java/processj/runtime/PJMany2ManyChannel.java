package processj.runtime;

import java.util.LinkedList;
import java.util.Queue;

public class PJMany2ManyChannel<T> extends PJOne2OneChannel<T> {
    
    // ************************************
    // Shared read end
    // ************************************
    protected PJProcess readclaim = null;
    
    protected Queue<PJProcess> readQueue = new LinkedList<>();
    
    @Override
    public synchronized boolean claimRead(PJProcess p) {
        if (readclaim == null || readclaim == p) {
            readclaim = p;
            return true;
        } else {
            p.setNotReady();
            readQueue.add(p);
        }
        return false;
    }
    
    @Override
    public synchronized void unclaimRead() {
        if (readQueue.isEmpty()) {
            readclaim = null;
        } else {
            PJProcess p = readQueue.remove();
            readclaim = p;
            p.setReady();
        }
    }
    
    // ************************************
    // Shared write end
    // ************************************
    protected PJProcess writeclaim = null;
    
    protected Queue<PJProcess> writeQueue = new LinkedList<>();
    
    @Override
    public synchronized boolean claimWrite(PJProcess p) {
        if (writeclaim == null || writeclaim == p) {
            writeclaim = p;
            return true;
        } else {
            p.setNotReady();
            writeQueue.add(p);
        }
        return false;
    }
    
    @Override
    public synchronized void unclaimWrite() {
        if (writeQueue.isEmpty()) {
            writeclaim = null;
        } else {
            PJProcess p = writeQueue.remove();
            writeclaim = p;
            p.setReady();
        }
    }
}
