/*!
 * ProcessJTest::StaticMethodTest declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_STATIC_METHOD_TEST_HPP
#define UNLV_PROCESS_J_STATIC_METHOD_TEST_HPP

namespace ProcessJTest {

    class static_method_process ;
    class static_method_test    ;

}

/* static method -- would be in the file's namespace but our namespace is already ProcessJTest,
 * which actually simulates this issue very well :^)
 */

static void foo() {

    std::cout << "I'm foo!\n";

}

class ProcessJTest::static_method_process : public ProcessJRuntime::pj_process {
    public:
        static_method_process() = delete;

        static_method_process(int32_t id, ProcessJRuntime::pj_scheduler* sched)
        : id(id)
        {
            this->sched = sched;
        }

        virtual ~static_method_process() = default;

        void run()
        {
            switch(get_label())
            {
                case 0: goto STATIC_METHOD_PROCESSL0; break;
                case 1: goto STATIC_METHOD_PROCESSL1; break;
            }
        STATIC_METHOD_PROCESSL0:
            std::cout << "Hello from L0! (proc " << this->id << " on cpu "
                      << sched_getcpu() << ")\n";
            foo();
            set_label(1);
            return;
        STATIC_METHOD_PROCESSL1:
            std::cout << "END (proc " << this->id << "on cpu "
                      << sched_getcpu() << ")\n";
            terminate();
            return;
        }

        friend std::ostream& operator<<(std::ostream& o, static_method_process& p)
        {
            return o << p.id;
        }

    private:
        int32_t id;
        ProcessJRuntime::pj_scheduler* sched;
    };

    class ProcessJTest::static_method_test
    {
    public:
        static_method_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            ProcessJRuntime::pj_scheduler sched;

            std::cout << "\n *** CREATING PROCESS *** \n\n";
            ProcessJTest::static_method_process* sm_proc =
                new ProcessJTest::static_method_process(0, &sched);

            std::cout << "\n *** SCHEDULING PROCESS *** \n\n";
            sched.insert(sm_proc);

            std::cout << "\n *** STARTING SCHEDULER *** \n\n";
            sched.start();
        }
    };

#endif
