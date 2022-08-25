/*!
 * ProcessJRuntime::TimerQueue declaration.
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_TIMER_QUEUE_HPP
#define UNLV_PROCESS_J_TIMER_QUEUE_HPP

namespace ProcessJRuntime { class pj_timer_queue; }

class ProcessJRuntime::pj_timer_queue {

private:

    /* the ProcessJRuntime::pj_timer_queue essentially needs to grab a timer, and wait for
     * it to timeout before setting it's _expired flag to true, and
     * handling the the process that had that timer appropriately
     * ---
     * allow a "fake timer" to be placed in the timer queue, which will
     * both disallow permanent waiting for a timer from the delayqueue,
     * and make it so that when that is placed in the queue, it is
     * taken out immediately, the flag is then checked, and if
     * it is set to true the timerqueue thread dies, else we have
     * not received a fake timer, and we should handle that timer
     * however we are supposed to
     */

    ProcessJUtilities::delay_queue<ProcessJRuntime::pj_timer*> dq;
    std::thread                timer_thread;
    std::mutex                          mtx;

    /* TODO: need to implement exit_value counter
     * ---
     * exit_value must be 1 initially (first process does not
     * increment), and must be 0 for the TimerQueue to be
     * allowed to exit properly
     * ---
     * incrememted on fork, decremented on process end
     */
    std::atomic<int32_t> exit_value;
    std::atomic<bool>    kill_flag;

    ProcessJRuntime::pj_timer* kill_timer = nullptr;

    std::vector<std::pair<ProcessJRuntime::pj_timer*, std::chrono::system_clock::time_point>> vec;
    std::mutex mutex;
    std::condition_variable cv;
    bool closed = false;

    friend class ProcessJRuntime::pj_timer;

    ProcessJRuntime::pj_timer* dequeue() {

        /* need a unique_lock<> because we're giving up ownership of the lock
         * if the queue is empty, or if we're waiting for a timer to expire
         */
        std::unique_lock<std::mutex> lk(mutex);
        std::chrono::system_clock::time_point now = std::chrono::system_clock::now();
        now = std::chrono::time_point_cast<std::chrono::milliseconds>(now);

        /* while the queue is empty and closed, or is not empty and
         * there is a timepoint that hasn't expired yet
         */
        while(!(vec.empty() && closed) &&
                  !(!vec.empty() && vec.back().second <= now)) {

            /* if the queue is empty, wait for it to be populated,
             * otherwise wait until the timepoint of the next item
             * has expired
             */
            if(vec.empty()) {

                cv.wait(lk);

            } else {

                cv.wait_until(lk, vec.back().second);

            }

            /* update current time */
            now = std::chrono::system_clock::now();

        }

        /* if the queue is empty and closed, return nothing */
        if(vec.empty() && closed) {

                return {};

        }

        /* grab an item off the queue */
        ProcessJRuntime::pj_timer* res = std::move(vec.back().first);
        vec.pop_back();

        /* if the queue is empty and closed, wake everyone waiting up */
        if(vec.empty() && closed) {

                cv.notify_all();

        }

        /* return the item */
        return res;

    }

public:

    pj_timer_queue(): exit_value(1) { /* Empty */ }

    pj_timer_queue(const pj_timer_queue&) = delete;

    pj_timer_queue(size_t size) {

        vec.reserve(size);

    }

    ~pj_timer_queue() {

        if(timer_thread.joinable())
            timer_thread.join();

        /* make sure we delete our kill_timer sanely */
        if(kill_timer) delete kill_timer;

    }

    pj_timer_queue& operator=(const pj_timer_queue&) = delete;

    /* returns 1 on failure due to closed queue, 0 otherwise */
    void enqueue(ProcessJRuntime::pj_timer* val, std::chrono::system_clock::time_point tp) {

        std::lock_guard<std::mutex> lk(mutex);

        if(closed) {

            std::cerr << "error: delay_queue is closed\n";


        }

        /* create a tuple of the item being enqueued with its time */
        vec.emplace_back(std::move(val), tp);

        /* sort the queue based on time -- earliest one first */
        std::sort(begin(vec),
                      end(vec),
                      [](auto&& a, auto&& b) { return a.second > b.second; });

        /* notify whoever is waiting that there is something in the queue */
        cv.notify_one();

    }

    void insert(ProcessJRuntime::pj_timer* timer) {

        std::lock_guard<std::mutex> lock(this->mtx);
        dq.enqueue(timer, timer->get_real_delay());

    }

    void start() {

        timer_thread = std::thread([this]() {

            while(1) {

                ProcessJRuntime::pj_timer* timer = dq.dequeue();

                timer->expire();

                pj_process* p = timer->get_process();

                /* check if we can safely exit as a thread */
                if(!p && exit_value) { return; }

                if(p) p->set_ready();

                delete timer;

            }

        });

    }

    void kill() {

        /* we're ready to die */
        kill_flag.exchange(true);

        /* make our kill_timer and place it in the queue */
        kill_timer = new ProcessJRuntime::pj_timer();

        /* drop the bomb */
        this->insert(kill_timer);

    }

    void close() {

        std::lock_guard<std::mutex> lk(this->mutex);
        this->closed = true;
        this->cv.notify_all();

    }

    size_t size() {

        std::lock_guard<std::mutex> lk(this->mutex);
        size_t res = this->vec.size();
        return res;

    }

};


#endif
