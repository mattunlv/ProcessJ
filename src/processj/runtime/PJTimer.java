package processj.runtime;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * The runtime representation of the ProcessJ 'timer' type.
 *
 * @author Cabel Shrestha
 * @version 1.0
 * @since 2016-05-01
 *
 * @author Benjamin Zofcin
 * @version 1.1
 * @since 2026-09-09
 */
public class PJTimer implements Delayed {

    /**
     * ElapsedTimeException
     * 
     * Internal exception to be thrown when attempting to start a timer whose target
     * time has already passed; possibly true for absolute timers only.
     */
    public static class ElapsedTimeException extends Exception {
        public ElapsedTimeException() {}
        public ElapsedTimeException(String msg) {
            super(msg);
        }
    }

    private PJProcess process; // Process to which the timer belongs

    private long delay; // Relative millisecond time a timer will exist in the DelayQueue
    private boolean killed = false; // A timer that was forcibly removed from the DelayQueue
    private boolean started = false; // A timer that has been offered to the DelayQueue
    private boolean expired = false; // A timer that was offered and taken from the DelayQueue normally 
    private boolean absolute = true; // A timer whose delay is based on an absolute time (future or past)

    public final long timeout; // Amount of time to wait or absolute deadline 

    public PJTimer() {
        this.timeout = 0L;
    }

    /**
     * Constructor for "relative time" timers invoked by t.timeout() calls
     * 
     * Remove if needed: currently all timers must be abolute timers.
     * @param process
     * @param timeout
     */
    public PJTimer(PJProcess process, long timeout) {
        this.process = process;
        this.timeout = timeout;
    }

    // /**
    //  * Constructor for "absolute time" timers invoked by t.deadline() calls
    //  * 
    //  * Note: Absolute timers have yet to be implemented by the grammar.
    //  *       This is for planned future implementations
    //  * @param process
    //  * @param deadline
    //  * @param absolute
    //  */
    // public PJTimer(PJProcess process, long deadline, boolean absolute) {
    //     this.process = process;
    //     this.timeout = deadline; // how is "deadline" time formatted? is deadline validated? do we validate here?
    //     // this.absolute = false; // dont allow "false" injection
    // }

    /**
     * Attempt to start a timer by sending it to the scheduler
     * 
     * @throws InterruptedException Thrown from delayQueue.offer()
     * @throws ElapsedTimeException Thrown if the timers declared deadline has passed
     */
    public void start() throws InterruptedException, ElapsedTimeException {

        // Currently, only absolute timers are supported. (should be renamed to t.deadline())
        this.delay = absolute ? timeout - System.currentTimeMillis() : System.currentTimeMillis() + timeout;

        if (this.delay <= 0l || timeout == 0) {
            throw new ElapsedTimeException();
            // throw new ElapsedTimeException("PJTimer\t\t:::\tTimer " + this + " is past deadline or 0.");
        }

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

    // public long getDelay() {
    //     return delay;
    // }

    // public boolean isAbsolute() {
    //     return absolute;
    // }

    public void kill() {
        killed = true;
        PJProcess.scheduler.removeTimer(this);
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
        // long diff = delay - System.currentTimeMillis(); // Value of delay was ambiguous 
        long diff = timeout - System.currentTimeMillis(); // This diff is only for absolute timers
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        int retVal = Long.valueOf(this.delay).compareTo(((PJTimer) o).delay);
        return retVal;
    }
}