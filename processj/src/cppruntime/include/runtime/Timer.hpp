/*!
 *
 * TODO: inherits from concurrent libs in java, find out equivalents
 * ---
 * more specifically, find an equivalent use for:
 *     java.util.concurrent.Delayed;
 *     java.util.concurrent.TimeUnit;
 * ---
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_TIMER_HPP
#define UNLV_PROCESS_J_TIMER_HPP

namespace ProcessJRuntime { class pj_timer; }

class ProcessJRuntime::pj_timer {

private:
        friend class pj_timer_queue;
        friend class Alternation;

    public:
        bool m_started;
        bool m_expired;

        pj_timer()
        : m_started(false),
          m_expired(false),
          m_delay(0),
          m_real_delay(std::chrono::time_point_cast<std::chrono::milliseconds>(std::chrono::system_clock::time_point::min())),
          m_killed(false),
          m_process(static_cast<ProcessJRuntime::pj_process*>(0))
        {

        }

        pj_timer(long timeout)
        : m_started(false),
          m_expired(false),
          m_delay(timeout),
          m_real_delay(std::chrono::time_point_cast<std::chrono::milliseconds>(std::chrono::system_clock::time_point::min())),
          m_killed(false),
          m_process(static_cast<ProcessJRuntime::pj_process*>(0))
        {

        }

        pj_timer(ProcessJRuntime::pj_process* process, long timeout)
        : m_started(false),
          m_expired(false),
          m_delay(timeout),
          m_real_delay(std::chrono::time_point_cast<std::chrono::milliseconds>(std::chrono::system_clock::time_point::min())),
          m_killed(false),
          m_process(process)
        {

        }

        ~pj_timer() = default;

        void start()
        {
            m_real_delay = std::chrono::system_clock::time_point(std::chrono::milliseconds(pj_timer::read() + this->get_delay()));
            m_started = true;
        }

        void timeout(long timeout)
        {
            m_delay = timeout;
        }

        static long read()
        {
            auto now = std::chrono::system_clock::now();
            auto now_ms = std::chrono::time_point_cast<std::chrono::milliseconds>(now);
            auto now_epoch = now_ms.time_since_epoch();
            auto value = std::chrono::duration_cast<std::chrono::milliseconds>(now_epoch);
            return static_cast<long>(value.count());
        }

        void kill()
        {
            m_killed = true;
        }

        bool killed()
        {
            return m_killed;
        }

        void expire()
        {
            m_expired = true;
        }

        bool expired()
        {
            return m_expired;
        }

        long get_delay()
        {
            return m_delay;
        }

        void set_process(ProcessJRuntime::pj_process* p)
        {
            m_process = p;
        }

        ProcessJRuntime::pj_process* get_process()
        {
            return (m_killed) ? static_cast<ProcessJRuntime::pj_process*>(0) : m_process;
        }

        std::chrono::system_clock::time_point get_real_delay()
        {
            return std::chrono::time_point_cast<std::chrono::milliseconds>(m_real_delay);
        }

        friend std::ostream& operator<<(std::ostream& o, pj_timer& t)
        {
            return o << "Process: " << t.m_process;
        }

    private:
        long m_delay;
        std::chrono::system_clock::time_point m_real_delay;
        long m_timeout;
        bool m_killed;
        ProcessJRuntime::pj_process* m_process;
    };


#endif
