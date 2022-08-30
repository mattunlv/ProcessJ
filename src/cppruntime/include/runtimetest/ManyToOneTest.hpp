/*!
 * ProcessJTest::ManyToOneTest declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_MANY_TO_ONE_TEST_HPP
#define UNLV_PROCESS_J_MANY_TO_ONE_TEST_HPP

namespace ProcessJTest {

    template<typename Type>
    class many2one_writer;

    template<typename Type>
    class many2one_reader;

    class many2one_test;

}

template <typename T>
class ProcessJTest::many2one_writer : public ProcessJRuntime::pj_process {
    public:
        many2one_writer() = delete;

        many2one_writer(int32_t                              id,
                       ProcessJRuntime::pj_many2one_channel<T>* chan,
                       T                                   data)
        : id(id), data(data)
        {
            this->chan = chan;
        }

        virtual ~many2one_writer() = default;

        void run()
        {
            switch(get_label())
            {
                case 0: goto L0;   break;
                case 1: goto L1;   break;
                case 2: goto LEND; break;
            }
        L0:
            std::cout << "process " << this->id << " writing from channel\n";
            if(!this->chan->claim_write(this))
            {
                std::cout << "process " << this->id << " did not claim write\n";
                set_label(1);
                return;
            }
            else
            {
                std::cout << "process " << this->id << " claimed the write\n";
            }
        L1:
            std::cout << "process " << this->id
                      << " writing data...\n";
            this->chan->write(this, this->data);
            std::cout << "process " << this->id << " wrote data "
                      << this->data << std::endl;
            set_label(2);
            return;
        LEND:
            std::cout << "END (proc " << id << ")\n";
            this->chan->unclaim_write();
            terminate();
            return;
        }

    private:
        int32_t                               id;
        T                                   data;
        ProcessJRuntime::pj_many2one_channel<T>* chan;
    };

template <typename T>
class ProcessJTest::many2one_reader : public ProcessJRuntime::pj_process {
    public:
        many2one_reader() = delete;

        many2one_reader(int32_t                              id,
                       ProcessJRuntime::pj_many2one_channel<T>* chan,
                       uint32_t                         writers)
        : id(id), writers(writers)
        {
            this->chan = chan;
        }

        virtual ~many2one_reader() = default;

        void run()
        {
            /* NOTE: this only exits when it reads as many values
             * to the channel as there are writers writing
             */
            static uint32_t writes = 0;
            switch(get_label())
            {
                case 0: goto L0;   break;
                case 1: goto LEND; break;
            }
        L0:
            std::cout << "process " << this->id << " reading from channel\n";
            if(this->chan->is_ready_to_read(this))
            {
                std::cout << "process " << this->id << " reading data...\n";
                data = this->chan->read(this);
                std::cout << "process " << this->id << " read data "
                          << data << std::endl;
                ++writes;
                if(writes == writers)
                {
                    set_label(1);
                }
            }
            return;
        LEND:
            std::cout << "END (proc " << id << ")\n";
            terminate();
            return;
        }

    private:
        int32_t                               id;
        T                                   data;
        ProcessJRuntime::pj_many2one_channel<T>* chan;
        uint32_t                         writers;
    };

class ProcessJTest::many2one_test {
    public:
        many2one_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            std::cout << "\n *** CREATING MANY2ONE CHANNEL<int32_t> *** \n\n";
            ProcessJRuntime::pj_many2one_channel<int32_t> mto_ch;

            std::cout << "\n *** CREATING PROCESSES FOR R/W *** \n\n";
            many2one_writer<int32_t>* writers[3];
            many2one_reader<int32_t>* reader = new many2one_reader<int32_t>(3,
                                                                            &mto_ch,
                                                                            3);
            int32_t i;
            for(i = 0; i < 3; ++i)
            {
                writers[i] = new many2one_writer<int32_t>(i,
                                                          &mto_ch,
                                                          69);
            }

            std::cout << "\n *** RANDOMIZING PROCESS ORDER *** \n\n";
            std::vector<ProcessJRuntime::pj_process*> processes(4);
            for(i = 0; i < 3; ++i)
            {
                processes[i] = writers[i];
            }
            processes[3] = reader;

            std::random_shuffle(processes.begin(), processes.end());

            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            ProcessJRuntime::pj_scheduler sch;

            std::cout << "\n *** SCHEDULING PROCESSES *** \n\n";
            for(i = 0; i < 4; ++i)
            {
                sch.insert(processes[i]);
            }

            std::cout << "\n *** STARTING SCHEDULER *** \n\n";
            sch.start();
        }
    };

#endif
