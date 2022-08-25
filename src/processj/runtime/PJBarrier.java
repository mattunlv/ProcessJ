package processj.runtime;

import java.util.ArrayList;
import java.util.List;

/*
 * ProcessJ code will have something of this sort:
 * 
 * enroll(b):                                                                                                                     
 *                                                                                                                                 
 * b.addProc(this);                                                                                                               
 *                                                                                                                                  
 * b.sync():                                                                                                                       
 *                                                                                                                                
 * b.decrement();                                                                                                                
 * yield(......., X);                                                                                                            
 * X: ...                                                                                                                           
 */
public class PJBarrier {

    /**
     * List of processes that have synced on the barrier.
     */
    public List<PJProcess> synced = new ArrayList<PJProcess>();
    /*
     * The number of processes enrolled on the barrier.
     */
    public int enrolled = 0;

    /**
     * Constructor. Any process that declares a barrier is itself
     * enrolled on it; so enrolled count starts at 1.
     */
    public PJBarrier() {
        this.enrolled = 1;
    }

    /**
     * Enroll on the barrier. (m-1) will be enrolled.
     * 
     * @param m
     *            the number of processes to enroll.
     */
    public synchronized void enroll(int m) {
        this.enrolled = this.enrolled + m - 1;
    }

    /**
     * Resign from the barrier.
     */
    public synchronized void resign() {
        if (this.enrolled > 1) {
            this.enrolled = this.enrolled - 1;
        }
    }

    /**
     * Synchronizes on the barrier. A process is needed to set all processes
     * ready when everyone has synced on the barrier.
     *
     * @param process
     *            A reference to the process syncing.
     */
    public synchronized void sync(PJProcess process) {
        process.setNotReady();
        synced.add(process);
        if (synced.size() == enrolled) {
            for (PJProcess p : synced) {
                p.setReady();
            }
            synced.clear();
        }
    }
}
