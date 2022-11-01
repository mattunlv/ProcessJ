/*!
 * implementation based on
 * https://gist.github.com/yohhoy/6da797689e16f6fe880c84f41f788c66
 */

/*!
 *since this is templated, we cannot use a separate .cpp file
 * to define anything AND still have non-specialized template
 * classes in our static library, see
 * https://bytes.com/topic/c/answers/161232-howto-make-template-class-part-static-library
 */

#ifndef UNLV_PROCESS_J_DELAY_QUEUE
#define UNLV_PROCESS_J_DELAY_QUEUE

namespace ProcessJUtilities {

    template<typename Type>
    class delay_queue;

}

template <typename T>
class ProcessJUtilities::delay_queue {

    public:
        delay_queue()
        {

        }

        delay_queue(std::size_t size)
        {
            vec.reserve(size);
        }

        delay_queue(const delay_queue&) = delete;

        ~delay_queue()
        {

        }

        delay_queue& operator=(const delay_queue&) = delete;

        /* returns 1 on failure due to closed queue, 0 otherwise */
        int enqueue(T val, std::chrono::system_clock::time_point tp)
        {
            std::lock_guard<std::mutex> lk(mutex);

            if(closed)
            {
                std::cerr << "error: delay_queue is closed\n";
                return 1;
            }

            /* create a tuple of the item being enqueued with its time */
            vec.emplace_back(std::move(val), tp);

            /* sort the queue based on time -- earliest one first */
            std::sort(begin(vec),
                      end(vec),
                      [](auto&& a, auto&& b) { return a.second > b.second; });

            /* notify whoever is waiting that there is something in the queue */
            cv.notify_one();

            return 0;
        }

        T dequeue()
        {
            /* need a unique_lock<> because we're giving up ownership of the lock
             * if the queue is empty, or if we're waiting for a timer to expire
             */
            std::unique_lock<std::mutex> lk(mutex);
            std::chrono::system_clock::time_point now = std::chrono::system_clock::now();
            now = std::chrono::time_point_cast<std::chrono::milliseconds>(now);

            /* while the queue is empty and closed, or is not empty and
             * there is a timepoint that hasn't expired yet
             */
            while(!(vec.empty() && closed) &&
                  !(!vec.empty() && vec.back().second <= now))
            {
                /* if the queue is empty, wait for it to be populated,
                 * otherwise wait until the timepoint of the next item
                 * has expired
                 */
                if(vec.empty())
                {
                    cv.wait(lk);
                }
                else
                {
                    cv.wait_until(lk, vec.back().second);
                }

                /* update current time */
                now = std::chrono::system_clock::now();
            }

            /* if the queue is empty and closed, return nothing */
            if(vec.empty() && closed)
            {
                return {};
            }

            /* grab an item off the queue */
            T res = std::move(vec.back().first);
            vec.pop_back();

            /* if the queue is empty and closed, wake everyone waiting up */
            if(vec.empty() && closed)
            {
                cv.notify_all();
            }

            /* return the item */
            return res;
        }

        size_t size()
        {
            std::lock_guard<std::mutex> lk(this->mutex);
            size_t res = this->vec.size();
            return res;
        }

        void close()
        {
            std::lock_guard<std::mutex> lk(this->mutex);
            this->closed = true;
            this->cv.notify_all();
        }

    private:
        std::vector<std::pair<T, std::chrono::system_clock::time_point>> vec;
        std::mutex mutex;
        std::condition_variable cv;
        bool closed = false;

};


#endif
