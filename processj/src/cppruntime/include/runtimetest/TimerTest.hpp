/*!
 * ProcessJTest::TimerTest declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_TIMER_TEST_HPP
#define UNLV_PROCESS_J_TIMER_TEST_HPP

namespace ProcessJTest {

    class timer_process ;
    class timer_test    ;

}

class ProcessJTest::timer_process : public ProcessJRuntime::pj_process {
    public:
        timer_process(ProcessJRuntime::pj_scheduler* sched)
        : m_sched(sched)
        { }

        ~timer_process() = default;

        void run()
        {
            std::cout << "timer_process run: new timer\n";
            static ProcessJRuntime::pj_timer* t = new ProcessJRuntime::pj_timer(this, 0);

            switch(get_label())
            {
                case 0: goto L0; break;
                case 1: goto L1; break;
            }

            L0:
            std::cout << "timer_process run: start timer\n";
            t->timeout(0);
            t->start();
            m_sched->insert(t);
            this->set_not_ready();
            this->set_label(1);
            return;

            L1:
            std::cout << "timer_process run: delete timer\n";
            delete t;
            terminate();
            return;
        }

    private:
        ProcessJRuntime::pj_scheduler* m_sched;
    };

class ProcessJTest::timer_test {
    public:
        timer_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            ProcessJRuntime::pj_scheduler sched;
            sched.insert(new timer_process(&sched));
            sched.start();
        }
    };

#endif
