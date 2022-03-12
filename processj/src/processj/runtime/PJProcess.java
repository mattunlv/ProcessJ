package processj.runtime;

/**
 * The runtime representation of a ProcessJ process.
 *
 * @author Cabel Shrestha
 * @version 08/29/2018
 * @since 1.2
 */

public class PJProcess {

    /**
     * The next label to restart from.
     */
    protected int runLabel = 0;

    /**
     * Is the process ready to run?
     */
    protected boolean ready = true;

    /**
     * Has the process terminated?
     */
    protected boolean terminated = false;

    /**
     * Scheduler instance.
     */
    public static Scheduler scheduler;

    /**
     * Abstract run method. This method is implemented when the class is extended.
     * or when it is instantiated.
     */
    public void run() {
    };

    /**
     * Inserts itself into the scheduler's runqueue.
     */
    public void schedule() {
        scheduler.insert(this);
    }

    /**
     * The finalize method is run by the scheduler once the process terminates.
     */
    public void finalize() {
    }

    /**
     * Returns true if the process is ready to be scheduled.
     *
     * @return Returns the 'ready' field.
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Sets the process ready to run.
     */
    public synchronized void setReady() {
        if (!ready) {
            ready = true;
            scheduler.inactivePool.decrement();
        }
    }

    /**
     * Sets the process not-ready to run.
     */
    public void setNotReady() {
        if (ready) {
            ready = false;
            scheduler.inactivePool.increment();
        }
    }

    /**
     * Sets the terminated field to true.
     */
    public void terminate() {
        terminated = true;
    }

    /**
     * Returns true if the process has terminated.
     *
     * @return returns the 'terminated' field.
     */
    public boolean terminated() {
        return terminated;
    }

    /**
     * Dummy method used by the instrumentation pass (ASM).
     */
    public void yield() {
    }
    
    /**
     * Dummy method used by the instrumentation pass (ASM).
     *
     * @param label
     *            The label number represented by this label.
     */
    public void label(int label) {
    }

    /**
     * Dummy method used by the instrumentation pass (ASM).
     *
     * @param label
     *            The label to which the next resumption should jump.
     */
    public void resume(int label) {
    }
    
    // ------------------------------------------
    
    // Dummy LABEL for ASM bytecode rewrite for loops
    public static void LABEL(int label) {
    }
    
    // Dummy GOTO for ASM bytecode rewrite for loops
    public static void GOTO(int label) {
    }
}