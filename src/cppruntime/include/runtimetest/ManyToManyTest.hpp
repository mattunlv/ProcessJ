/*!
 * ProcessJTest::ManyToManyTest declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef PROCESS_J_MANY_TO_MANY_TEST_HPP
#define PROCESS_J_MANY_TO_MANY_TEST_HPP

namespace ProcessJTest {

    template<typename Type>
    class many2many_writer;

    template<typename Type>
    class many2many_reader;

    class many2many_test;

}

template <typename T>
class ProcessJTest::many2many_writer : public ProcessJRuntime::pj_process {
    public:
        many2many_writer() = delete;

        many2many_writer(int32_t                              id,
                       ProcessJRuntime::pj_many2many_channel<T>* chan,
                       T                                    data)
        {
            this->id = id;
            this->chan = chan;
            this->data = data;
        }

        virtual ~many2many_writer() = default;

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
        int32_t id;
        T data = reinterpret_cast<T>(0);
        ProcessJRuntime::pj_many2many_channel<T>* chan = nullptr;
    };

template <typename T>
class ProcessJTest::many2many_reader : public ProcessJRuntime::pj_process {

    public:
        many2many_reader() = delete;

        many2many_reader(int32_t                              id,
                       ProcessJRuntime::pj_many2many_channel<T>* chan)
        {
            this->id = id;
            this->chan = chan;
        }

        virtual ~many2many_reader() = default;

        void run()
        {
            switch(get_label())
            {
                case 0: goto L0;   break;
                case 1: goto L1;   break;
                case 2: goto L2;   break;
                case 3: goto LEND; break;
            }
        L0:
            std::cout << "process " << this->id << " reading from channel\n";
            if(!this->chan->claim_read(this))
            {
                std::cout << "process " << this->id << " did not claim read\n";
                set_label(1);
                return;
            }
            else
            {
                std::cout << "process " << this->id << " claimed the read\n";
            }
        L1:
            if(!this->chan->is_ready_to_read(this))
            {
                std::cout << "process " << this->id << "'s channel is not ready\n";
                set_label(2);
                return;
            }
            else
            {
                std::cout << "process " << this->id << "'s channel is ready\n";
            }
        L2:
            std::cout << "process " << this->id
                      << " reading data...\n";
            data = this->chan->read(this);
            std::cout << "process " << this->id << " read data "
                      << this->data << std::endl;
            set_label(3);
            this->chan->unclaim_read();
            return;
        LEND:
            std::cout << "END (proc " << id << ")\n";
            terminate();
            return;
        }

    private:
        int32_t id;
        T data = reinterpret_cast<T>(0);
        ProcessJRuntime::pj_many2many_channel<T>* chan = nullptr;
    };

class ProcessJTest::many2many_test {
    public:
        many2many_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            std::cout << "\n *** CREATING MANY2MANY CHANNEL<int32_t> *** \n\n";
            ProcessJRuntime::pj_many2many_channel<int32_t> mtm_ch;

            std::cout << "\n *** CREATING PROCESSES FOR R/W *** \n\n";
            many2many_writer<int32_t>* writers[4];
            many2many_reader<int32_t>* readers[4];
            int32_t i;
            for(i = 0; i < 4; ++i)
            {
                writers[i] = new many2many_writer<int32_t>(i,
                                                           &mtm_ch,
                                                           69);
                readers[i] = new many2many_reader<int32_t>(i + 4, &mtm_ch);
            }

            std::cout << "\n *** RANDOMIZING PROCESS ORDER *** \n\n";
            std::vector<ProcessJRuntime::pj_process*> processes(8);

            for(i = 0; i < 4; ++i)
            {
                processes[i] = writers[i];
                processes[i + 4] = readers[i];
            }
            std::random_shuffle(processes.begin(), processes.end());

            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            ProcessJRuntime::pj_scheduler sch;

            std::cout << "\n *** SCHEDULING PROCESSES *** \n\n";
            for(i = 0; i < 8; ++i)
            {
                sch.insert(processes[i]);
            }

            std::cout << "\n *** STARTING SCHEDULER *** \n\n";
            sch.start();
        }
    };

#endif
