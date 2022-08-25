/*!
 * ProcessJRuntime::Channel declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_CHANNEL_HPP
#define UNLV_PROCESS_J_CHANNEL_HPP

namespace ProcessJRuntime { class pj_channel; }

class ProcessJRuntime::pj_channel
    {
    public:
        pj_channel()
        : type(ProcessJRuntime::pj_channel_types::NONE)
        {

        }

        pj_channel(ProcessJRuntime::pj_channel_types t)
        : type(pj_channel_type(t))
        {

        }

        pj_channel(ProcessJRuntime::pj_channel_type t)
        : type(t)
        {

        }

        /* TODO: copy and move constructors/assignment operators
         * if necessary
         */
         pj_channel(pj_channel&& other)
         {
            this->reader = other.reader;
            this->writer = other.writer;
         }

         pj_channel& operator=(pj_channel&& other)
         {
            this->reader = other.reader;
            this->writer = other.writer;
            return *this;
         }

        virtual ~pj_channel()
        {
            reader = nullptr;
            writer = nullptr;
        }

        ProcessJRuntime::pj_channel_type get_channel_type()
        {
            return type;
        }

        /* NOTE: removed for non-type-generic fix */
        // virtual void        write(ProcessJRuntime::pj_process* p, T data)         = 0;
        // virtual T           read(ProcessJRuntime::pj_process* p)                  = 0;

        // virtual bool        is_ready_to_read(ProcessJRuntime::pj_process* p)      = 0;
        // virtual bool        is_ready_to_write(ProcessJRuntime::pj_process* p)     = 0;

        // virtual T           pre_read_rendezvous(ProcessJRuntime::pj_process* p)   = 0;
        // virtual void        post_read_rendezvous(ProcessJRuntime::pj_process* p)  = 0;

        /* NOTE: commented out to take impls from one2one */
        // virtual ProcessJRuntime::pj_process* alt_get_writer(ProcessJRuntime::pj_process* p)        = 0;
        // virtual ProcessJRuntime::pj_process* set_reader_get_writer(ProcessJRuntime::pj_process* p) = 0;

        ProcessJRuntime::pj_process* alt_get_writer(ProcessJRuntime::pj_process* p)
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            if(!writer)
            {
                reader = p;
            }

            return writer;
        }

        ProcessJRuntime::pj_process* set_reader_get_writer(ProcessJRuntime::pj_process* p)
        {
            reader = p;
            return writer;
        }

        virtual bool claim_read(ProcessJRuntime::pj_process* p)
        {
            return false;
        }

        virtual void unclaim_read(ProcessJRuntime::pj_process* p)
        {

        }

        virtual bool claim_write(ProcessJRuntime::pj_process* p)
        {
            return false;
        }

        virtual void unclaim_write(ProcessJRuntime::pj_process* p)
        {

        }

        friend std::ostream& operator<<(std::ostream& o, pj_channel& c)
        {
            o << c.type;
            return o;
        }

    protected:
        ProcessJRuntime::pj_channel_type type;
        std::mutex mtx;
        ProcessJRuntime::pj_process* writer = nullptr;
        ProcessJRuntime::pj_process* reader = nullptr;
    };

#endif
