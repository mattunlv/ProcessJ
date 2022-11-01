/*!
 * ProcessJRuntime::OneToOneChannel declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_ONE_TO_ONE_CHANNEL_HPP
#define UNLV_PROCESS_J_ONE_TO_ONE_CHANNEL_HPP

namespace ProcessJRuntime {

    template<typename Type>
    class pj_one2one_channel;

}

template <typename T>
    class ProcessJRuntime::pj_one2one_channel : public ProcessJRuntime::pj_channel
    {
    public:
        pj_one2one_channel()
        {
            this->type = pj_channel_types::ONE2ONE;
        }

        ~pj_one2one_channel()
        {

        }

        void write(ProcessJRuntime::pj_process* p, T data)
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            this->data = data;
            writer = p;
            writer->set_not_ready();
            if(reader)
            {
                reader->set_ready();
            }
        }

        T read(ProcessJRuntime::pj_process* p)
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            writer->set_ready();
            writer = nullptr;
            reader = nullptr;
            return this->data;
        }

        bool is_ready_to_read(ProcessJRuntime::pj_process* p)
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            if(writer)
            {
                return true;
            }
            else
            {
                reader = p;
                reader->set_not_ready();
            }
            return false;
        }

        bool is_ready_to_write(ProcessJRuntime::pj_process* p)
        {
            return true;
        }

        T pre_read_rendezvous(ProcessJRuntime::pj_process* p)
        {
            T data = this->data;
            this->data = reinterpret_cast<T>(0);
            return data;
        }

        void post_read_rendezvous(ProcessJRuntime::pj_process* p)
        {
            writer->set_ready();
            writer = nullptr;
            reader = nullptr;
        }

    protected:
    T data;
    };

#endif
