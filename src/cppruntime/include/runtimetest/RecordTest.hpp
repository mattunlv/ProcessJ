/*!
 * ProcessJTest::RecordTest declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_RECORD_TEST_HPP
#define UNLV_PROCESS_J_RECORD_TEST_HPP

namespace ProcessJTest {

    struct some_record      ;
    class record_process    ;
    class record_test       ;

}

struct ProcessJTest::some_record: public ProcessJRuntime::pj_record {

    int   a;
    bool  b;
    float y;

};

class ProcessJTest::record_process : public ProcessJRuntime::pj_process {
    public:

        record_process() = delete;

        record_process(int32_t id)
        : id(id)
        {

        }

        virtual ~record_process() = default;

        void run()
        {
            switch(get_label())
            {
                case 0: goto L0;   break;
                case 1: goto LEND; break;
            }
        L0:
            std::cout << "process " << this->id
                      << " manipulating a record...\n";
            rt.a = 13;
            rt.b = true;
            rt.y = 5.3;
            std::cout << std::boolalpha
                      << "contents of record:"
                      << "\nrt.a = " << rt.a
                      << "\nrt.b = " << rt.b
                      << "\nrt.y = " << rt.y
                      << std::endl;
        LEND:
            std::cout << "END (proc " << this->id << ")\n";
            terminate();
            return;
        }

    private:
        int32_t id;
        struct ProcessJTest::some_record rt;

};

class ProcessJTest::record_test {
    public:
        record_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            ProcessJRuntime::pj_scheduler sched;

            std::cout << "\n *** CREATING PROCESS *** \n\n";
            auto* r_proc = new record_process(0);

            std::cout << "\n *** SCHEDULING PROCESS *** \n\n";
            sched.insert(r_proc);

            std::cout << "\n *** RUNNING SCHEDULER *** \n\n";
            sched.start();
        }

};

#endif
