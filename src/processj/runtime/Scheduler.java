package processj.runtime;

public class Scheduler extends Thread {

    private final TimerQueue tq = new TimerQueue();
    private final RunQueue rq = new RunQueue();
    public final InactivePool inactivePool = new InactivePool();
    private long startTime = 0L;

    synchronized void insert(PJProcess p) {
        rq.insert(p);
    }

    synchronized void insertTimer(PJTimer t) throws InterruptedException {
        tq.insert(t);
    }

    synchronized int size() {
        return rq.size();
    }

    private int contextSwitches = 0;
    private int maxrqsize = 0;

    synchronized void incContextSwitches() {
        contextSwitches++;
    }

    synchronized void incMaxrqsize(int size) {
        if (size > maxrqsize) {
            maxrqsize = size;
        }
    }

    @Override
    public void run() {
        startTime = System.nanoTime();
        // System.err.println("[Scheduler] Scheduler running");

        tq.start();

        int contextSwitches = 0;
        int maxrqsize = 0;

        while (rq.size() > 0) {
            if (rq.size() > maxrqsize) {
                maxrqsize = rq.size();
            }

            // grab the next process in the run queue
            PJProcess p = rq.getNext();

            // is it ready to run?
            if (p.isReady()) {
                // yes, so run it
                p.run();
                contextSwitches++;
                if (!p.terminated()) {
                    // did not terminate, so insert in run queue
                    // Note, it is the process' own job to
                    // set the 'ready' flag.
                    rq.insert(p);
                } else {
                    // did terminate so do nothing
                    p.finalize();
                }
            } else {
                // no, not ready, put it back in the run queue
                // and count it as not ready
                rq.insert(p);
            }

            // System.out.println("rq=" + rq.size() + " inactivePool=" +
            // inactivePool.getCount() + " timerqueue=" + tq.size());
//            if (inactivePool.getCount() == rq.size() && rq.size() > 0 && tq.size() == 0) {
//                System.err.println("No processes ready to run. System is deadlocked");
//                tq.kill();
//
//                // System.err.println("[Scheduler] Total Context Switches: " + contextSwitches);
//                // System.err.println("[Scheduler] Max RunQueue Size: " + maxrqsize);
//
//                logExecutionTime();
//                System.exit(1);
//            }
        }

        tq.kill();

        System.err.println("[Scheduler] Total Context Switches: " + contextSwitches);
        System.err.println("[Scheduler] Max RunQueue Size: " + maxrqsize);

        logExecutionTime();
    }

    @Override
    public String toString() {
        String str = "";
        for (PJProcess p : rq.queue) {
            str += p.isReady() + " " + p.runLabel + " " + p.getClass().getName() + "\n";
        }
        return str;
    }

    private void logExecutionTime() {
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        double seconds = (double) elapsedTime / 1000000000.0;
        System.out.println("Total execution time: " + (seconds) + " secs");
    }
}