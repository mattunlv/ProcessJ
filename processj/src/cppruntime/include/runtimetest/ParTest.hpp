/*!
 * ProcessJTest::ParTest declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_PAR_TEST_HPP
#define UNLV_PROCESS_J_PAR_TEST_HPP

namespace ProcessJTest {

    class par_process   ;
    class par_anon      ;
    class par_test      ;

}

/* now we inline-define our other processes */
class ProcessJTest::par_anon : public ProcessJRuntime::pj_process {

            public:
                par_anon() = delete;
                par_anon(int32_t id,
                     ProcessJRuntime::pj_scheduler* sched,
                     ProcessJRuntime::pj_par* par)
                : id(id)
                {
                    this->sched = sched;
                    this->par   = par;
                }

                virtual ~par_anon() = default;

                void run()
                {
                    switch(get_label())
                    {
                        case 0: goto ANONL0;   break;
                        case 1: goto ANONLEND; break;
                    }
                ANONL0:
                    std::cout << "Hello from L0! (proc " << id << " on cpu "
                              << sched_getcpu() << ")\n";
                    set_label(1);
                    return;

                ANONLEND:
                    std::cout << "END (proc " << id << " on cpu "
                              << sched_getcpu() << ")\n";
                    /* NOTE: this might be in a method called finalize() that calls
                     * a lambda we pass to this function that captures the par from
                     * the process that "owns" the par, just so that it's easier
                     * to generalize these par_anonymous functions within the pars.
                     * or maybe it'll be easier... not sure yet.
                     */
                    this->par->decrement();
                    terminate();
                    return;
                }

                /* NOTE: local classes cannot have friend functions defined */

            private:
                int32_t id;
                ProcessJRuntime::pj_scheduler* sched;
                ProcessJRuntime::pj_par* par;
};

class ProcessJTest::par_process: public ProcessJRuntime::pj_process {

    public:
        par_process() = delete;

        par_process(int32_t id, ProcessJRuntime::pj_scheduler* sched)
        : id(id)
        {
            this->sched = sched;
        }

        virtual ~par_process() = default;

        void run()
        {
            static ProcessJRuntime::pj_par* par = new ProcessJRuntime::pj_par(2, this);

            switch(get_label())
            {
                case 0: goto PAR_PROCESSL0; break;
                case 1: goto PAR_PROCESSL1; break;
            }

        PAR_PROCESSL0:
            std::cout << "Hello from L0! (process " << this->id << " on cpu "
                      << sched_getcpu() << ")\n";

            /* plunk 2 new anon procs into the scheduler */
            this->sched->insert(new ProcessJTest::par_anon(this->id + 1, this->sched, par));
            this->sched->insert(new ProcessJTest::par_anon(this->id + 2, this->sched, par));

            /* more code generated for our par block below */
            if(par->should_yield())
            {
                this->set_label(1);
                return;
            }

        PAR_PROCESSL1:
            std::cout << "END (proc " << this->id << " on cpu "
                      << sched_getcpu() << ")\n";
            delete par;
            terminate();
            return;
        }

        friend std::ostream& operator<<(std::ostream& o, par_process& p)
        {
            o << p.id;
            return o;
        }

    private:
        int32_t id;
        ProcessJRuntime::pj_scheduler* sched;
    };

class ProcessJTest::par_test {

public:

    par_test() {

        std::cout << "instantiating test...\n";

    }

    void run() {

        std::cout << "\n *** CREATING SCHEDULER *** \n\n";
        ProcessJRuntime::pj_scheduler sched;

        std::cout << "\n *** CREATING PROCESS *** \n\n";
        ProcessJTest::par_process* pp = new ProcessJTest::par_process(0, &sched);

        std::cout << "\n *** SCHEDULING PROCESS *** \n\n";
        sched.insert(pp);

        std::cout << "\n *** STARTING SCHEDULER *** \n\n";
        sched.start();

    }

};


#endif
