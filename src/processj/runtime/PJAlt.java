package processj.runtime;

public class PJAlt {
    
    /** Can be skips, timers or channel-reads */
    private Object[] guards;
    
    /** Boolean guards */
    private boolean[] bguards;
    
    /** Process declaring the 'alt' */
    private PJProcess process;
    
    public static final String SKIP = "skip";
    
    public PJAlt(int count, PJProcess p) {
        process = p;
        guards = new Object[count];
        bguards = new boolean[count];
    }
    
    public boolean setGuards(boolean[] bguards, Object[] guards) {
        this.guards = guards;
        this.bguards = bguards;
        
        for (boolean b : bguards)
            if (b)
                return true;
        
        return false;
    }
    
    @SuppressWarnings("rawtypes")
    public int enable() {
        for (int i = 0; i < guards.length; ++i) {
            // If no boolean guard is ready then continue
            if (!bguards[i])
                continue;
            // A skip?
            if (guards[i] == PJAlt.SKIP) {
                process.setReady();
                return i;
            }
            // A channel?
            if (guards[i] instanceof PJChannel) {
                PJChannel chan = (PJChannel) guards[i];
                if (chan.altGetWriter(process) != null) {
                    process.setReady();
                    return i;
                }
            }
            // A timer?
            if (guards[i] instanceof PJTimer) {
                // TODO: Shouldn't this be formally verified??
                PJTimer t = (PJTimer) guards[i];
                if (t.getDelay() <= 0L) {
                    process.setReady();
                    t.expire();
                    return i;
                } else {
                    try {
                        t.start();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return -1;
    }
    
    @SuppressWarnings("rawtypes")
    public int disable(int i) {
        int selected = -1;
        if (i == -1)
            i = guards.length - 1;
        for (int j = i; j >= 0; --j) {
            // If no boolean guard is ready then continue
            if (!bguards[j])
                continue;
            // A skip?
            if (guards[j] == PJAlt.SKIP)
                selected = j;
            // A channel?
            if (guards[j] instanceof PJChannel) { 
                // No race condition on this channel as it is a one-to-one and only THIS
                // process has access to it. This simply means that we are de-registering
                // from the channel for now, but may still read from it if selected is not
                // updated with a value < j.
                PJChannel chan = (PJChannel) guards[j];
                if (chan.setReaderGetWriter(null) != null)
                    selected = j;
            }
            // A timer?
            if (guards[j] instanceof PJTimer) {
                // TODO: Shouldn't this be formally verified??
                PJTimer timer = (PJTimer) guards[j];
                if (timer.isExpired())
                    selected = j;
                else
                    timer.kill();
            }
        }
        return selected;
    }
}
