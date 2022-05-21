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
