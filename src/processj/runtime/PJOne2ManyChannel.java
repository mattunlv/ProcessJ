package processj.runtime;

import java.util.LinkedList;
import java.util.Queue;

public class PJOne2ManyChannel<T> extends PJOne2OneChannel<T> {
    
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
}
