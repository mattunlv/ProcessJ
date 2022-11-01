/*!
 * ProcessJRuntime::Barrier declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_BARRIER_HPP
#define UNLV_PROCESS_J_BARRIER_HPP

/* (from ProcessJ java impl)
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

namespace ProcessJRuntime { class pj_barrier; }

class ProcessJRuntime::pj_barrier
    {
    public:
        std::vector<ProcessJRuntime::pj_process*> synced;
        uint32_t enrolled;

        pj_barrier()
        : enrolled(0)
        {
            ProcessJRuntime::pj_logger::log("barrier instantiated");
        }

        ~pj_barrier()
        {

        }

        void enroll(uint32_t proc_count)
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            ProcessJRuntime::pj_logger::log("enrolled was ", enrolled);
            this->enrolled += proc_count;
            ProcessJRuntime::pj_logger::log("enrolled is now ", enrolled);
        }

        void resign()
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            if(this->enrolled > 1)
            {
                ProcessJRuntime::pj_logger::log("enrolled decrementing from ", enrolled);
                --this->enrolled;
                ProcessJRuntime::pj_logger::log("enrolled is now ", enrolled);
            }
        }

        void sync(ProcessJRuntime::pj_process* process)
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            ProcessJRuntime::pj_logger::log("process ", process, " called sync()");
            process->set_not_ready();
            this->synced.push_back(process);
            if(this->synced.size() == enrolled)
            {
                ProcessJRuntime::pj_logger::log("firing off processes again");
                for(uint32_t i = 0; i < this->synced.size(); ++i)
                {
                    this->synced[i]->set_ready();
                }
                synced.clear();
            }
        }

    protected:
    private:
        std::mutex mtx;
    };

#endif
