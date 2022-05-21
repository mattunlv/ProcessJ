/*!
 * \brief ProcessJRuntime::Scheduler implementation
 *
 * \author Carlos L. Cuenca
 * \author Alexander C. Thomason
 * \date 05/20/2022
 * \version 1.3.0
 */

#include<Scheduler.hpp>

/*!
 * Default Constructor. Initializes the Scheduler
 * with the amount of cores the cpu can handle
 * with default affinity of 0
 */

Scheduler::Scheduler():
    cpu(0), cpus(std::thread::hardware_concurrency()) { /* Empty */ }


/*!
 * Secondary Constructor. Initializes the Scheduler
 * with the amount of cores the cpu can handle with
 * the given affinity.
 * \param cpu The affinity to set the scheduler
 */

Scheduler::Scheduler(uint32_t cpu):
    cpu(cpu), cpus(std::thread::hardware_concurrency()) { /* Empty */ }

/*!
 * Deconstructor. Tears down the scheduler.
 */

Scheduler::~Scheduler() {

    if(this->sched_thread.joinable())
        this->sched_thread.join();

    /* timer queue should only be killed once */
    tq.kill();

    std::cerr << "[Scheduler " << cpu << "] Total Context Switches: "
              << context_switches
              << "\n[Scheduler " << cpu << "] Max RunQueue Size: "
              << max_rq_size
              << std::endl;

}

/*!
 * Inserts the process into the runqueue of the Scheduler
 * to be executed.
 */

void Scheduler::insert(ProcessJRuntime::pj_process* p) {

    std::lock_guard<std::mutex> lk(mutex);
    rq.insert(p);

}

/*!
 * Inserts the time into the Run Queue of the Scheduler
 * to be executed.
 * \param timer The Timer to insert.
 */

void Scheduler::insert(pj_timer* t) {

    std::lock_guard<std::mutex> lk(mutex);
    tq.insert(t);

}

/*!
 * Starts the Scheduler.
 */

void Scheduler::start() {

    /* only need to start the timer queue once */
    tq.start();
    this->sched_thread = std::thread(&Scheduler::run, this);

    /* this allows us to use heap-allocated variables
 * in the main driver for the code (i.e. run() in alt_test).
 * without this, we would have to nondeterministically choose
 * if the alt_writer process or the alt_process process
 * "owns" the channel and thus has to free it -- it's owned
 * by the runtime, and should be deleted there.
 * ---
 * later on, shared_ptrs will be used probably exclusively,
 * so no raw pointers will be allowed (and thus, no random
 * pointers that don't at least have a deletor for them
 * when they really go out of scope)
 */
    if(this->sched_thread.joinable())
        this->sched_thread.join();

}

/*!
 * Begins the main loop to execute processes.
 */

void Scheduler::run(void) {

    this->isolate_thread();

    while(rq.size() > 0) {

        if(static_cast<size_t>(rq.size()) > max_rq_size)
            max_rq_size = rq.size();

        ProcessJRuntime::pj_process* p = rq.next();

        if(p->is_ready()) {

            p->run();
            context_switches++;

            if(!p->is_terminated())
                rq.insert(p);

            else {

                p->finalize();
                delete p;

            }

        } else rq.insert(p);

    }

}


/*!
 * Returns the amount of processes executing
 * in the scheduler.
 * \return Number of processes executing in the scheduler.
 */

int Scheduler::size() {

    std::lock_guard<std::mutex> lk(mutex);
    return rq.size();

}

/*!
 * Increments the amount of context switches
 * the Scheduler has performed.
 */

void Scheduler::inc_context_switches() {

    std::lock_guard<std::mutex> lk(mutex);
    context_switches++;

}

/*!
 * Increments the amount of processes the runqueue
 * can handle.
 */

void Scheduler::inc_max_rq_size(size_t size) {

    std::lock_guard<std::mutex> lk(mutex);
    if(size > max_rq_size) max_rq_size = size;

}
