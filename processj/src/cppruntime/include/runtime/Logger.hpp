/*!
 * ProcessJRuntime::Logger Declaration. Declares a logger object.
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_LOGGER_HPP
#define UNLV_PROCESS_J_LOGGER_HPP

namespace ProcessJRuntime { class pj_logger; }

class ProcessJRuntime::pj_logger {

public:

    static void log() {

        std::lock_guard<ProcessJRuntime::Mutex> log_lck(ProcessJRuntime::LogMutex);
        std::cout << std::endl;

    }

    template <typename T>
    static void log(T value) {

        std::lock_guard<ProcessJRuntime::Mutex> log_lck(ProcessJRuntime::LogMutex);
        std::cout << value << std::endl;

    }

    template <typename T, typename... Ts>
    static void log(T value, Ts... values) {

        std::unique_lock<ProcessJRuntime::Mutex> log_lck(ProcessJRuntime::LogMutex);
        std::cout << value;
        log_lck.unlock();
        log(values...);
    }

};

#endif
