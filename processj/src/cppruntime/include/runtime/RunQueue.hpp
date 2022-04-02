/*!
 * ProcessJRuntime::RunQueue declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_RUN_QUEUE_HPP
#define UNLV_PROCESS_J_RUN_QUEUE_HPP

namespace ProcessJRuntime { class pj_run_queue; }

class ProcessJRuntime::pj_run_queue
    {
    public:

        pj_run_queue()
        {

        }

        ~pj_run_queue()
        {

        }

        void insert(ProcessJRuntime::pj_process* p)
        {
            std::lock_guard<std::mutex> lk(rq_mutex);
            queue.push(p);
        }

        ProcessJRuntime::pj_process* next()
        {
            std::lock_guard<std::mutex> lk(rq_mutex);
            ProcessJRuntime::pj_process* next = queue.front();
            queue.pop();
            return next;
        }

        size_t size()
        {
            std::lock_guard<std::mutex> lk(rq_mutex);
            size_t size = queue.size();
            return size;
        }

    protected:
        std::queue<ProcessJRuntime::pj_process*> queue;

    private:
        std::mutex rq_mutex;
    };


#endif
