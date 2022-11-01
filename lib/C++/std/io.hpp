#ifndef PJ_IO_HPP
#define PJ_IO_HPP
class io
{
public:
    static void println()
    {
        std::lock_guard<std::mutex> print_lck(pj_runtime::pj_logger::log_mtx);
        std::cout << std::endl;
    }

    template <typename T>
    static void println(T arg)
    {
        std::lock_guard<std::mutex> print_lck(pj_runtime::pj_logger::log_mtx);
        std::cout << arg << std::endl;
    }

    template <typename T, typename... Args>
    static void println(T arg, Args... args)
    {
        std::unique_lock<std::mutex> print_lck(pj_runtime::pj_logger::log_mtx);
        std::cout << arg;
        print_lck.unlock();
        println(args...);
    }

    static void print()
    {
        std::lock_guard<std::mutex> print_lck(pj_runtime::pj_logger::log_mtx);
        std::cout << "";
    }

    template <typename T>
    static void print(T arg)
    {
        std::lock_guard<std::mutex> print_lck(pj_runtime::pj_logger::log_mtx);
        std::cout << arg;
    }

    template <typename T, typename... Args>
    static void print(T arg, Args... args)
    {
        std::unique_lock<std::mutex> print_lck(pj_runtime::pj_logger::log_mtx);
        std::cout << arg;
        print_lck.unlock();
        print(args...);
    }
};
#endif
