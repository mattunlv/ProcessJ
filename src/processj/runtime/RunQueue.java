package processj.runtime;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A very simple linked-list implementation of a queue of processes - a run
 * queue. All this class' methods are called from Scheduler.java
 *
 * @author Cabel Shrestha
 * @version 1.0
 * @since 2016-05-01
 */
public class RunQueue {
    /**
     * A linked list of PJProcess objects. Both ready and not ready processes are
     * stored in this list.
     */
    protected Queue<PJProcess> queue = new LinkedList<>();

    /**
     * @param p
     *          Inserts process p (of type PJProcess) into the run queue.
     */
    synchronized public void insert(PJProcess p) {
        queue.add(p);
    }

    /**
     * Returns the next process from the run queue - ready or not.
     *
     * @return PJProcess The process at the head of the queue.
     */
    synchronized public PJProcess getNext() {
        return queue.remove();
    }

    /**
     * Returns the size of the run queue - the total number of processes (both ready
     * and not ready) currently in the system.
     *
     * @return int The size of the run queue.
     */
    synchronized public int size() {
        return queue.size();
    }
}