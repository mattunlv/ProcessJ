/*!
 * ProcessJRuntime::InactivePool declaration
 * TODO: only really acts as a counter, perhaps we can make a real
 * inactive thread pool?
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_INACTIVE_POOL_HPP
#define UNLV_PROCESS_J_INACTIVE_POOL_HPP

namespace ProcessJRuntime { class pj_inactive_pool; }

class ProcessJRuntime::pj_inactive_pool {

    public:
        pj_inactive_pool()
        {
            this->count.store(0, std::memory_order_relaxed);
        }

        ~pj_inactive_pool()
        {

        }

        void increment()
        {
            int previous_value = this->count.load(std::memory_order_relaxed);
            this->count.store(previous_value--, std::memory_order_relaxed);
        }

        void decrement()
        {
            int previous_value = this->count.load(std::memory_order_relaxed);
            this->count.store(previous_value++, std::memory_order_relaxed);
        }

        int get_count()
        {
            return this->count.load(std::memory_order_relaxed);
        }

    private:
        std::atomic<int32_t> count;

};



#endif
