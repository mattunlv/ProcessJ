/*!
 * ProcessJRuntim::Scheduler declaration.
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.3.0
 */

#ifndef UNLV_PROCESS_J_SCHEDULER_HPP
#define UNLV_PROCESS_J_SCHEDULER_HPP

#include<atomic>
#include<vector>
#include<condition_variable>
#include<algorithm>
#include<thread>
#include<queue>

#include<Logger.hpp>
#include<Process.hpp>
#include<Timer.hpp>
#include<DelayQueue.hpp>
#include<TimerQueue.hpp>
#include<RunQueue.hpp>
#include<InactivePool.hpp>

/// --------------------
/// Namespace Resolution

#ifdef SCHEDULER_NAMESPACE

    #define SchedulerNamespace SCHEDULER_NAMESPACE

#else

    #define SchedulerNamespace ProcessJRuntime

#endif

/// --------------
/// Inline options

#ifdef SCHEDULER_INLINE_CONSTRUCTORS

    #define SchedulerConstructorInline inline __attribute__((always_inline))

#else

    #define SchedulerConstructorInline

#endif

#ifdef SCHEDULER_INLINE_OPERATORS

    #define SchedulerOperatorInline inline __attribute__((always_inline))

#else

    #define SchedulerOperatorInline

#endif

#ifdef SCHEDULER_INLINE_METHODS

    #define SchedulerMethodInline inline __attribute__((always_inline))

#else

    #define SchedulerMethodInline

#endif

/// ---------------------
/// Namespace Declaration

namespace SchedulerNamespace { class Scheduler; }

/// -----
/// Alias

using Scheduler = SchedulerNamespace::Scheduler;

/// -----------------
/// Class Declaration

class SchedulerNamespace::Scheduler {

    /// -----------------
    /// Protected Members

protected:

    /*!
     * Returns the amount of processes executing
     * in the scheduler.
     * \return Number of processes executing in the scheduler.
     */

    int size();

    /*!
     * Increments the amount of context switches
     * the Scheduler has performed.
     */

    void inc_context_switches();

    /*!
     * Increments the amount of processes the runqueue
     * can handle.
     */

    void inc_max_rq_size(size_t);

public:

    pj_inactive_pool ip;

    /*!
     * Default Constructor. Initializes the Scheduler
     * with the amount of cores the cpu can handle
     * with default affinity of 0
     */

    Scheduler();

    /*!
     * Secondary Constructor. Initializes the Scheduler
     * with the amount of cores the cpu can handle with
     * the given affinity.
     * \param cpu The affinity to set the scheduler
     */

    Scheduler(uint32_t cpu);

    /*!
     * Deconstructor. Tears down the scheduler.
     */

    ~Scheduler();

    /*!
     * Inserts the process into the runqueue of the Scheduler
     * to be executed.
     */

    void insert(ProcessJRuntime::pj_process*);

    /*!
     * Inserts the time into the Run Queue of the Scheduler
     * to be executed.
     * \param timer The Timer to insert.
     */

    void insert(pj_timer* t);

    /*!
     * Starts the Scheduler.
     */

    void start();

    /*!
     * Begins the main loop to execute processes.
     */

    void run(void);

private:

        ProcessJRuntime::pj_timer_queue tq;
        ProcessJRuntime::pj_run_queue   rq;

        std::mutex mutex;
        std::mutex iomutex;

        uint64_t start_time       = 0;
        int32_t  context_switches = 0;
        size_t   max_rq_size      = 0;

        std::thread sched_thread;
        uint32_t             cpu;
        uint32_t            cpus;

        void isolate_thread(void) {

            std::unique_lock<std::mutex> lock(this->iomutex, std::defer_lock);
            std::thread::id th_id = this->sched_thread.get_id();

            lock.lock();

            ProcessJRuntime::pj_logger::log("isolating thread ", th_id, " to cpu "
                      , cpu);

            lock.unlock();

            cpu_set_t cur_set;
            cpu_set_t new_set;
            pthread_t    p_th;
            uint32_t        i;

            CPU_ZERO(&new_set);

            if(!this->cpus) {

                lock.lock();
                std::cerr << "error: hardware_concurrency not set/determinable\n";
                lock.unlock();
                abort();

            }

            uint8_t arr_cur_set[this->cpus];
            uint8_t arr_new_set[this->cpus];

            for(i = 0; i < this->cpus; ++i) {

                arr_cur_set[i] = 0;
                arr_new_set[i] = 0;

            }

            p_th = this->sched_thread.native_handle();
            lock.lock();

            ProcessJRuntime::pj_logger::log("the native_handle is ", p_th);
            lock.unlock();

            if(!p_th) {

                lock.lock();
                std::cerr << "error: native_handle() returned null\n";
                lock.unlock();
                abort();

            }

            lock.lock();
            ProcessJRuntime::pj_logger::log("getting thread cpu_set...");
            lock.unlock();

            CPU_ZERO(&cur_set);

            if(pthread_getaffinity_np(p_th, sizeof(cpu_set_t), &cur_set)) {

                lock.lock();
                perror("pthread_getaffinity_np");
                lock.unlock();
                abort();

            }

            lock.lock();
            for(i = 0; i < this->cpus; ++i) {

                if(CPU_ISSET(i, &cur_set)) {

                    ProcessJRuntime::pj_logger::log("cpu ", i, " is in thread ", th_id, "'s current cpu set");

                }

            }

            ProcessJRuntime::pj_logger::log("now setting thread ", th_id, "'s cpu_set to ", cpu);
            lock.unlock();

            CPU_SET(cpu, &new_set);
            arr_new_set[cpu] = 1;

            lock.lock();
            ProcessJRuntime::pj_logger::log("new cpu_set is:");
            for(i = 0; i < cpus; ++i) {

                std::cout << static_cast<uint32_t>(arr_new_set[i]) << " ";

            }

            ProcessJRuntime::pj_logger::log("which implies:");
            for(i = 0; i < cpus; ++i) {

                if(CPU_ISSET(i, &new_set))
                    ProcessJRuntime::pj_logger::log(i);

            }

            std::cout << std::endl;
            lock.unlock();

            if(pthread_setaffinity_np(p_th, sizeof(cpu_set_t), &new_set)) {

                lock.lock();
                perror("pthread_setaffinity_np");
                lock.unlock();
                abort();

            }

            lock.lock();
            ProcessJRuntime::pj_logger::log("verifying thread ", th_id, "'s cpu_set...");
            lock.unlock();

            CPU_ZERO(&cur_set);
            if(pthread_getaffinity_np(p_th, sizeof(cpu_set_t), &cur_set)) {

                lock.lock();
                perror("pthread_getaffinity_np");
                lock.unlock();
                abort();

            }

            lock.lock();
            for(i = 0; i < cpus; ++i) {

                if(CPU_ISSET(i, &cur_set)) {

                    ProcessJRuntime::pj_logger::log("cpu ", i, " is in new current cpu set");
                    arr_cur_set[i] = 1;

                }

            }

            for(i = 0; i < cpus; ++i) {

                if(arr_cur_set[i] != arr_new_set[i]) {

                    std::cerr << "error: cpu " << i << " is in thread " << th_id
                              << "'s cpu_set\n";
                    lock.unlock();
                    abort();

                }

            }

            lock.unlock();

            lock.lock();
            ProcessJRuntime::pj_logger::log("thread ", th_id, "'s cpu_set successfully modified\n");
            lock.unlock();

        }

};

#endif
