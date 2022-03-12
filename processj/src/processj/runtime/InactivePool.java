package processj.runtime;

/**
 * Currently only used to keep a count of the number of processes that are not
 * ready to run.
 *
 * @author Cabel Shrestha
 * @version 1.0
 * @since 2016-05-01
 */

public class InactivePool {

    /**
     * The number of inactive processes.
     */
    private int count = 0;

    /**
     * Decrement the number of inactive processes.
     */
    public synchronized void decrement() {
        this.count--;
    }

    /**
     * Increment the number of inactive processes.
     */
    public synchronized void increment() {
        this.count++;
    }

    /**
     * Returns the number of inactive processes.
     *
     * @return The number of inactive processes.
     */
    public int getCount() {
        return this.count;
    }
}