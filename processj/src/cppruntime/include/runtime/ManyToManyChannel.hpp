/*!
 * ProcessJRuntim::ManyToManyChannel declaration.
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_MANY_TO_MANY_CHANNEL_HPP
#define UNLV_PROCESS_J_MANY_TO_MANY_CHANNEL_HPP

namespace ProcessJRuntime {

    template<typename Type>
    class pj_many2many_channel;

}

template <typename T>
    class ProcessJRuntime::pj_many2many_channel : public ProcessJRuntime::pj_one2one_channel<T>
    {
    public:
        pj_many2many_channel()
        {

        }

        ~pj_many2many_channel()
        {

        }

        bool claim_read(ProcessJRuntime::pj_process* p)
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            if(!read_claim || read_claim == p)
            {
                read_claim = p;
                return true;
            }
            else
            {
                p->set_not_ready();
                read_queue.push(p);
            }

            return false;
        }

        void unclaim_read()
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            if(read_queue.empty())
            {
                read_claim = nullptr;
            }
            else
            {
                ProcessJRuntime::pj_process* p = read_queue.front();
                read_queue.pop();
                read_claim = p;
                p->set_ready();
            }
        }

        bool claim_write(ProcessJRuntime::pj_process* p)
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            if(!write_claim || write_claim == p)
            {
                write_claim = p;
                return true;
            }
            else
            {
                p->set_not_ready();
                write_queue.push(p);
            }

            return false;
        }

        void unclaim_write()
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            if(write_queue.empty())
            {
                write_claim = nullptr;
            }
            else
            {
                ProcessJRuntime::pj_process* p = write_queue.front();
                write_queue.pop();
                write_claim = p;
                p->set_ready();
            }
        }

    protected:
        ProcessJRuntime::pj_process* read_claim = nullptr;
        ProcessJRuntime::pj_process* write_claim = nullptr;
        std::queue<ProcessJRuntime::pj_process*> read_queue;
        std::queue<ProcessJRuntime::pj_process*> write_queue;
    };


#endif
