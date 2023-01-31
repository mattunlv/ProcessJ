package processj.runtime;

import java.util.ArrayList;
import java.util.List;

public class PJAlt {
    
    /** Can be skips, timers or channel-reads */
    private List<Object> guards;
    
    /** Boolean guards */
    private List<Boolean> bguards;
    
    /** Process declaring the 'alt' */
    private PJProcess process;
    
    private List<AltGuard> dynamicAlts;
    
    public static final String SKIP = "skip";
    
    public PJAlt(PJProcess p) {
        process = p;
        guards = new ArrayList<>();
        bguards = new ArrayList<>();
    }
    
    public PJAlt(int count, PJProcess p) {
        process = p;
        guards = new ArrayList<>(count);
        bguards = new ArrayList<>(count);
    }
    
    public boolean setGuards(List<Boolean> bguards, List<Object> guards) {
        this.guards = guards;
        this.bguards = bguards;
        
        for (Boolean b : bguards)
            if (b.booleanValue())
                return true;
        
        return false;
    }
    
    public void setDynamicAlts(List<AltGuard> dynamicAlts) {
        this.dynamicAlts = dynamicAlts;
    }
    
    public AltGuard getDynamicAlts(int index) {
        return dynamicAlts.get(index);
    }
    
    @SuppressWarnings("rawtypes")
    public int enable() {
        for (int i = 0; i < guards.size(); ++i) {
            // If no boolean guard is ready then continue
            if (!bguards.get(i))
                continue;
            // A skip?
            if (guards.get(i) == PJAlt.SKIP) {
                process.setReady();
                return i;
            }
            // A channel?
            if (guards.get(i) instanceof PJChannel) {
                PJChannel chan = (PJChannel) guards.get(i);
                if (chan.altGetWriter(process) != null) {
                    process.setReady();
                    return i;
                }
            }
            // A timer?
            if (guards.get(i) instanceof PJTimer) {
                // TODO: Shouldn't this be formally verified??
                PJTimer t = (PJTimer) guards.get(i);
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
            i = guards.size() - 1;
        for (int j = i; j >= 0; --j) {
            // If no boolean guard is ready then continue
            if (!bguards.get(j))
                continue;
            // A skip?
            if (guards.get(j) == PJAlt.SKIP)
                selected = j;
            // A channel?
            if (guards.get(j) instanceof PJChannel) { 
                // No race condition on this channel as it is a one-to-one and only THIS
                // process has access to it. This simply means that we are de-registering
                // from the channel for now, but may still read from it if selected is not
                // updated with a value < j.
                PJChannel chan = (PJChannel) guards.get(j);
                if (chan.setReaderGetWriter(null) != null)
                    selected = j;
            }
            // A timer?
            if (guards.get(j) instanceof PJTimer) {
                // TODO: Shouldn't this be formally verified??
                PJTimer timer = (PJTimer) guards.get(j);
                if (timer.isExpired())
                    selected = j;
                else
                    timer.kill();
            }
        }
        return selected;
    }
}
