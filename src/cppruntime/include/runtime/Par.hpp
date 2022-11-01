/*!
 * ProcessJRuntime::Par declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_PAR_HPP
#define UNLV_PROCESS_J_PAR_HPP

namespace ProcessJRuntime { class pj_par; }

    class ProcessJRuntime::pj_par
    {
    public:
        pj_par() = delete;

        pj_par(int process_count, ProcessJRuntime::pj_process* p)
        : process_count(process_count)
        {
            this->process = p;
        }

        ~pj_par() = default;

        void set_process_count(int32_t count)
        {
            this->process_count = count;
        }

        void decrement()
        {
            std::lock_guard<std::mutex> lock(this->mtx);

            this->process_count--;

            if(this->process_count == 0)
            {
                this->process->set_ready();
            }
        }

        bool should_yield()
        {
            std::lock_guard<std::mutex> lock(this->mtx);

            if(this->process_count == 0)
            {
                return false;
            }

            this->process->set_not_ready();
            return true;
        }

    protected:
    private:
        ProcessJRuntime::pj_process* process;
        int32_t process_count;
        std::mutex mtx;
    };


#endif
