/*!
 * ProcessJRuntime::OneToManyChannel declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_ONE_TO_MANY_CHANNEL_HPP
#define UNLV_PROCESS_J_ONE_TO_MANY_CHANNEL_HPP

namespace ProcessJRuntime {

    template<typename Type>
    class pj_one2many_channel;

}

    template <typename T>
    class ProcessJRuntime::pj_one2many_channel : public ProcessJRuntime::pj_one2one_channel<T>
    {
    public:
        pj_one2many_channel()
        : read_claim(nullptr)
        {
            this->type = pj_channel_types::ONE2MANY;
        }

        ~pj_one2many_channel()
        {
            read_claim = nullptr;
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

    protected:
        ProcessJRuntime::pj_process* read_claim = nullptr;
        std::queue<ProcessJRuntime::pj_process*> read_queue;
    };

#endif
