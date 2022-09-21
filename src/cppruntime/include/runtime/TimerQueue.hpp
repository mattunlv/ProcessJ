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

    friend class ProcessJRuntime::pj_timer;

    public:
        pj_timer_queue()
        : exit_value(1)
        {

        }

        ~pj_timer_queue()
        {
            if(timer_thread.joinable())
            {
                timer_thread.join();
            }

            /* make sure we delete our kill_timer sanely */
            if(kill_timer)
            {
                delete kill_timer;
            }
        }

        void insert(ProcessJRuntime::pj_timer* timer)
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            dq.enqueue(timer, timer->get_real_delay());
        }

        void start()
        {
            timer_thread = std::thread([this]()
            {
                while(1)
                {
                    ProcessJRuntime::pj_timer* timer = dq.dequeue();

                    timer->expire();

                    pj_process* p = timer->get_process();

                    /* check if we can safely exit as a thread */
                    if(!p && exit_value)
                    {
                        return;
                    }

                    if(p)
                    {
                        p->set_ready();
                    }

                    delete timer;
                }
            });
        }

        void kill()
        {
            /* we're ready to die */
            kill_flag.exchange(true);

            /* make our kill_timer and place it in the queue */
            kill_timer = new ProcessJRuntime::pj_timer();

            /* drop the bomb */
            this->insert(kill_timer);
        }

        size_t size()
        {
            return dq.size();
        }

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
    };


#endif
