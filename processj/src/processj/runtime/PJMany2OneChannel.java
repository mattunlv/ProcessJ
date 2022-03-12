package processj.runtime;

import java.util.LinkedList;
import java.util.Queue;

public class PJMany2OneChannel<T> extends PJOne2OneChannel<T> {
    
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
