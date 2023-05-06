package processj.runtime;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * The runtime representation of the ProcessJ 'timer' type.
 *
 * @author Cabel Shrestha
 * @version 1.0
 * @since 2016-05-01
 */
public class PJTimer implements Delayed {
    private PJProcess process;

    private long delay;
    private boolean killed = false;
    private boolean started = false;
    private boolean expired = false;

    public final long timeout;

    public PJTimer() {
        this.timeout = 0L;
    }

    public PJTimer(PJProcess process, long timeout) {
        this.process = process;
        this.timeout = timeout;
    }

    public void start() throws InterruptedException {
        this.delay = /*System.currentTimeMillis() +*/ timeout;
        PJProcess.scheduler.insertTimer(this);
        started = true;
    }

    public synchronized void expire() {
        expired = true;
    }
    
    public synchronized boolean isExpired() {
        return expired;
    }

    public static long read() {
        return System.currentTimeMillis();
    }
    
    public long getDelay() {
        return delay;
    }

    public synchronized void kill() {
        killed = true;
    }

    public synchronized PJProcess getProcess() {
        if (killed) {
            return null;
        } else {
            return process;
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = delay - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        int retVal = Long.valueOf(this.delay).compareTo(((PJTimer) o).delay);
        return retVal;
    }
}