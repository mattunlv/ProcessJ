/*!
 * ProcessJTest::OneToManyTest declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_ONE_TO_MANY_TEST_HPP
#define UNLV_PROCESS_J_ONE_TO_MANY_TEST_HPP

namespace ProcessJTest {

    template<typename Type>
    class one2many_writer;

    template<typename Type>
    class one2many_reader;

    class one2many_test;

}

template <typename T>
class ProcessJTest::one2many_writer : public ProcessJRuntime::pj_process {
    public:
        one2many_writer() = delete;

        one2many_writer(int32_t                              id,
                       ProcessJRuntime::pj_one2many_channel<T>* chan,
                       T                                   data,
                       uint32_t                         readers)
        : id(id), data(data), readers(readers)
        {
            this->chan = chan;
        }

        virtual ~one2many_writer() = default;

        void run()
        {
            /* NOTE: this only exits when it writes as many values
             * to the channel as there are readers reading
             */
            static uint32_t reads = 0;
            switch(get_label())
            {
                case 0: goto L0;   break;
                case 1: goto LEND; break;
            }
        L0:
            std::cout << "process " << this->id
                      << " writing data " << this->data << "...\n";
            this->chan->write(this, this->data);
            std::cout << "process " << this->id << " wrote data "
                      << this->data << std::endl;
            ++reads;
            if(reads == readers)
            {
                set_label(1);
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
        ProcessJRuntime::pj_one2many_channel<T>* chan;
        uint32_t                         readers;
    };

template <typename T>
class ProcessJTest::one2many_reader : public ProcessJRuntime::pj_process {

public:
        one2many_reader() = delete;

        one2many_reader(int32_t                              id,
                       ProcessJRuntime::pj_one2many_channel<T>* chan)
        : id(id)
        {
            this->chan = chan;
        }

        virtual ~one2many_reader() = default;

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
        int32_t                               id;
        T                                   data;
        ProcessJRuntime::pj_one2many_channel<T>* chan;
};

class ProcessJTest::one2many_test {

public:
    one2many_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            std::cout << "\n *** CREATING ONE2MANY CHANNEL<INT> *** \n\n";
            ProcessJRuntime::pj_one2many_channel<int32_t> otm_ch;

            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            ProcessJRuntime::pj_scheduler sch;

            std::cout << "\n *** CREATING PROCESSES FOR R/W *** \n\n";
            one2many_writer<int32_t>* otm_w = new one2many_writer<int32_t>(3,
                                                                           &otm_ch,
                                                                           69,
                                                                           3);
            one2many_reader<int32_t>* otm_rs[3];
            int32_t i;
            for(i = 0; i < 3; ++i)
            {
                otm_rs[i] = new one2many_reader<int32_t>(i, &otm_ch);
            }

            std::cout << "\n *** RANDOMIZING PROCESS ORDER *** \n\n";
            std::vector<ProcessJRuntime::pj_process*> processes(4);
            for(i = 0; i < 3; ++i)
            {
                processes[i] = otm_rs[i];
            }
            processes[3] = otm_w;
            std::random_shuffle(processes.begin(), processes.end());

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
