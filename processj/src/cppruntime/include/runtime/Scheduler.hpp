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

    /// ---------------
    /// Private Members

private:

    /*!
     * TBD
     */

    void isolate_thread(void);

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

};

#endif
