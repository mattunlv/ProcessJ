/*!
 * ProcessJRuntim::Scheduler declaration.
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_SCHEDULER_HPP
#define UNLV_PROCESS_J_SCHEDULER_HPP

namespace ProcessJRuntime { class pj_scheduler; }

    class ProcessJRuntime::pj_scheduler
    {

    public:
        pj_inactive_pool ip;

        pj_scheduler()
        : cpu(0), cpus(std::thread::hardware_concurrency())
        {

        }

        pj_scheduler(uint32_t cpu)
        : cpu(cpu), cpus(std::thread::hardware_concurrency())
        {

        }

        ~pj_scheduler()
        {
            if(this->sched_thread.joinable())
            {
                this->sched_thread.join();
            }

            /* timer queue should only be killed once */
            tq.kill();

            std::cerr << "[Scheduler " << cpu << "] Total Context Switches: "
                      << context_switches
                      << "\n[Scheduler " << cpu << "] Max RunQueue Size: "
                      << max_rq_size
                      << std::endl;
        }

        void insert(ProcessJRuntime::pj_process* p)
        {
            std::lock_guard<std::mutex> lk(mutex);
            rq.insert(p);
        }

        void insert(pj_timer* t)
        {
            std::lock_guard<std::mutex> lk(mutex);
            tq.insert(t);
        }

        void start()
        {
            /* only need to start the timer queue once */
            tq.start();
            this->sched_thread = std::thread(&pj_scheduler::run, this);

            /* this allows us to use heap-allocated variables
             * in the main driver for the code (i.e. run() in alt_test).
             * without this, we would have to nondeterministically choose
             * if the alt_writer process or the alt_process process
             * "owns" the channel and thus has to free it -- it's owned
             * by the runtime, and should be deleted there.
             * ---
             * later on, shared_ptrs will be used probably exclusively,
             * so no raw pointers will be allowed (and thus, no random
             * pointers that don't at least have a deletor for them
             * when they really go out of scope)
             */
            if(this->sched_thread.joinable())
            {
                this->sched_thread.join();
            }
        }

        void run(void)
        {
            this->isolate_thread();

            while(rq.size() > 0)
            {
                if(static_cast<size_t>(rq.size()) > max_rq_size)
                {
                    max_rq_size = rq.size();
                }

                ProcessJRuntime::pj_process* p = rq.next();

                if(p->is_ready())
                {
                    p->run();
                    context_switches++;
                    if(!p->is_terminated())
                    {
                        rq.insert(p);
                    }
                    else
                    {
                        p->finalize();
                        delete p;
                    }
                }
                else
                {
                    rq.insert(p);
                }
            }
        }

    protected:
        int size()
        {
            std::lock_guard<std::mutex> lk(mutex);
            return rq.size();
        }

        void inc_context_switches()
        {
            std::lock_guard<std::mutex> lk(mutex);
            context_switches++;
        }

        void inc_max_rq_size(size_t size)
        {
            std::lock_guard<std::mutex> lk(mutex);
            if(size > max_rq_size)
            {
                max_rq_size = size;
            }
        }

    private:
        ProcessJRuntime::pj_timer_queue tq;
        ProcessJRuntime::pj_run_queue   rq;

        std::mutex mutex;
        std::mutex iomutex;

        uint64_t start_time       = 0;
        int32_t  context_switches = 0;
        size_t   max_rq_size      = 0;

        std::thread sched_thread;
        uint32_t             cpu;
        uint32_t            cpus;

        void isolate_thread(void)
        {
            std::unique_lock<std::mutex> lock(this->iomutex, std::defer_lock);
            std::thread::id th_id = this->sched_thread.get_id();

            lock.lock();
            ProcessJRuntime::pj_logger::log("isolating thread ", th_id, " to cpu "
                      , cpu);
            lock.unlock();

            cpu_set_t cur_set;
            cpu_set_t new_set;
            pthread_t    p_th;
            uint32_t        i;

            CPU_ZERO(&new_set);

            if(!this->cpus)
            {
                lock.lock();
                std::cerr << "error: hardware_concurrency not set/determinable\n";
                lock.unlock();
                abort();
            }

            uint8_t arr_cur_set[this->cpus];
            uint8_t arr_new_set[this->cpus];

            for(i = 0; i < this->cpus; ++i)
            {
                arr_cur_set[i] = 0;
                arr_new_set[i] = 0;
            }

            p_th = this->sched_thread.native_handle();
            lock.lock();
            ProcessJRuntime::pj_logger::log("the native_handle is ", p_th);
            lock.unlock();

            if(!p_th)
            {
                lock.lock();
                std::cerr << "error: native_handle() returned null\n";
                lock.unlock();
                abort();
            }

            lock.lock();
            ProcessJRuntime::pj_logger::log("getting thread cpu_set...");
            lock.unlock();

            CPU_ZERO(&cur_set);

            if(pthread_getaffinity_np(p_th,
                                      sizeof(cpu_set_t),
                                      &cur_set))
            {
                lock.lock();
                perror("pthread_getaffinity_np");
                lock.unlock();
                abort();
            }

            lock.lock();
            for(i = 0; i < this->cpus; ++i)
            {
                if(CPU_ISSET(i, &cur_set))
                {
                    ProcessJRuntime::pj_logger::log("cpu ", i, " is in thread ", th_id, "'s current cpu set");
                }
            }

            ProcessJRuntime::pj_logger::log("now setting thread ", th_id, "'s cpu_set to ", cpu);
            lock.unlock();

            CPU_SET(cpu, &new_set);
            arr_new_set[cpu] = 1;

            lock.lock();
            ProcessJRuntime::pj_logger::log("new cpu_set is:");
            for(i = 0; i < cpus; ++i)
            {
                std::cout << static_cast<uint32_t>(arr_new_set[i]) << " ";
            }
            ProcessJRuntime::pj_logger::log("which implies:");
            for(i = 0; i < cpus; ++i)
            {
                if(CPU_ISSET(i, &new_set))
                {
                    ProcessJRuntime::pj_logger::log(i);
                }
            }
            std::cout << std::endl;
            lock.unlock();

            if(pthread_setaffinity_np(p_th,
                                      sizeof(cpu_set_t),
                                      &new_set))
            {
                lock.lock();
                perror("pthread_setaffinity_np");
                lock.unlock();
                abort();
            }

            lock.lock();
            ProcessJRuntime::pj_logger::log("verifying thread ", th_id, "'s cpu_set...");
            lock.unlock();

            CPU_ZERO(&cur_set);
            if(pthread_getaffinity_np(p_th,
                                      sizeof(cpu_set_t),
                                      &cur_set))
            {
                lock.lock();
                perror("pthread_getaffinity_np");
                lock.unlock();
                abort();
            }

            lock.lock();
            for(i = 0; i < cpus; ++i)
            {
                if(CPU_ISSET(i, &cur_set))
                {
                    ProcessJRuntime::pj_logger::log("cpu ", i, " is in new current cpu set");
                    arr_cur_set[i] = 1;
                }
            }

            for(i = 0; i < cpus; ++i)
            {
                if(arr_cur_set[i] != arr_new_set[i])
                {
                    std::cerr << "error: cpu " << i << " is in thread " << th_id
                              << "'s cpu_set\n";
                    lock.unlock();
                    abort();
                }
            }
            lock.unlock();

            lock.lock();
            ProcessJRuntime::pj_logger::log("thread ", th_id, "'s cpu_set successfully modified\n");
            lock.unlock();
        }
    };


#endif
