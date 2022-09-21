package processj.runtime;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

import utilities.Log;

/**
 * An instance of a TimerQueue runs in its own Java Thread and handles all
 * timeout statements.
 *
 * @author Cabel Shrestha
 * @version 1.0
 * @since 2016-05-01
 */
public class TimerQueue {
    /*
     * Holds the PJTimer objects
     */
    public static BlockingQueue<PJTimer> delayQueue = new DelayQueue<>();
    private int size = 0;

    private Thread timerThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    // Take out timedout Timer objects from delay queue.
                    // Thread will wait here until one is available.
                    PJTimer timer = (PJTimer) delayQueue.take();
                    // Set the timer's expired flag to true.
                    timer.expire();
                    // Get the process in which the timeout was initiated.
                    PJProcess p = timer.getProcess();
                    // If the process is still around then set it ready to run again.
                    // time.getProcess() will return 'null' if the process has terminated.
                    if (p != null) {
                        synchronized (p) {
                            p.setReady();
                        }
                    }
                    size--;
                }
            } catch (InterruptedException e) {
//                System.err.println("[TimerQueue] Unexpected interrupt exception encountered.");
                return;
            }
        }
    });

    /*
     * insert() is called by insertTimer() from Scheduler.java
     */
    public synchronized void insert(PJTimer timer) throws InterruptedException {
        size++;
        delayQueue.offer(timer);
    }

    /* The methods below are used only by Scheduler.java */

    /*
     * start() is called once from the Scheduler class.
     */
    public void start() {
        Log.log("[TimerQueue] Timer Queue Running");
        this.timerThread.start();
    }

    /*
     * kill() is called once from the Scheduler class.
     */
    public synchronized void kill() {
        this.timerThread.interrupt();
    }

    public int size() {
        return size;
    }
}
