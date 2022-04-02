/*!
 * ProcessJRuntime::Alternationernation declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_ALTERNATION_HPP
#define UNLV_PROCESS_J_ALTERNATION_HPP

namespace ProcessJRuntime {

    typedef std::variant<std::string, pj_channel*, pj_timer*> AlternationGuardType;

    class Alternation;

}

class ProcessJRuntime::Alternation {
    public:
        inline static const std::string SKIP = "skip";

        Alternation() = delete;

        Alternation(uint64_t count, ProcessJRuntime::pj_process* p)
        {
            this->process = p;
        }

        ~Alternation()
        {

        }

        bool set_guards(std::vector<bool>            b_guards,
                        std::vector<AlternationGuardType> guards)
        {
            this->b_guards = b_guards;
            this->guards   = guards;

            for(uint32_t i = 0; i < this->b_guards.size(); ++i)
            {
                if(b_guards[i])
                {
                    return true;
                }
            }

            return false;
        }

        int32_t enable(void)
        {
            for(uint32_t i = 0; i < this->guards.size(); ++i)
            {
                if(!this->b_guards[i])
                {
                    continue;
                }

                if(std::holds_alternative<std::string>(this->guards[i]))
                {
                    if(std::get<std::string>(this->guards[i]) == SKIP)
                    {
                        this->process->set_ready();
                        return static_cast<int32_t>(i);
                    }
                }
                else if(std::holds_alternative<ProcessJRuntime::pj_channel*>(this->guards[i]))
                {
                    if(std::get<ProcessJRuntime::pj_channel*>(this->guards[i])->alt_get_writer(this->process) != nullptr)
                    {
                        this->process->set_ready();
                        return static_cast<int32_t>(i);
                    }
                }
                else if(std::holds_alternative<ProcessJRuntime::pj_timer*>(this->guards[i]))
                {
                    if(std::get<ProcessJRuntime::pj_timer*>(this->guards[i])->get_real_delay() <= std::chrono::time_point_cast<std::chrono::milliseconds>(std::chrono::system_clock::now()))
                    {
                        this->process->set_ready();
                        std::get<ProcessJRuntime::pj_timer*>(this->guards[i])->expire();
                        return static_cast<int32_t>(i);
                    }
                    else
                    {
                        std::get<ProcessJRuntime::pj_timer*>(this->guards[i])->start();
                    }
                }
            }
            return -1;
        }

        int32_t disable(int32_t i)
        {
            int32_t selected = -1;
            if(i == -1)
            {
                i = this->guards.size() - 1;
            }

            for(int32_t j = i; j >= 0; --j)
            {
                if(!this->b_guards[i])
                {
                    continue;
                }

                if(std::holds_alternative<std::string>(this->guards[j]))
                {
                    if(std::get<std::string>(this->guards[j]) == SKIP)
                    {
                        selected = j;
                    }
                }
                else if(std::holds_alternative<ProcessJRuntime::pj_channel*>(this->guards[j]))
                {
                    if(std::get<ProcessJRuntime::pj_channel*>(this->guards[j])->set_reader_get_writer(nullptr) != nullptr)
                    {
                        selected = j;
                    }
                }
                else if(std::holds_alternative<ProcessJRuntime::pj_timer*>(this->guards[j]))
                {
                    if(std::get<ProcessJRuntime::pj_timer*>(this->guards[j])->expired())
                    {
                        selected = j;
                    }
                    else
                    {
                        std::get<ProcessJRuntime::pj_timer*>(this->guards[j])->kill();
                    }
                }
            }
            return selected;
        }

    private:
        ProcessJRuntime::pj_process* process;
        std::vector<AlternationGuardType> guards;
        std::vector<bool> b_guards;
    };

#endif
