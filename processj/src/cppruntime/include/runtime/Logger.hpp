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

    static std::mutex log_mtx;
    static void log() {

        std::lock_guard<std::mutex> log_lck(log_mtx);
        std::cout << std::endl;

    }

    template <typename T>
    static void log(T value) {

        std::lock_guard<std::mutex> log_lck(log_mtx);
        std::cout << value << std::endl;

    }

    template <typename T, typename... Ts>
    static void log(T value, Ts... values) {

        std::unique_lock<std::mutex> log_lck(log_mtx);
        std::cout << value;
        log_lck.unlock();
        log(values...);
    }

};

/// ---------------------
/// Static Initialization

std::mutex ProcessJRuntime::pj_logger::log_mtx;

#endif
