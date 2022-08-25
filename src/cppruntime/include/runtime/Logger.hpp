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

#include<cstddef>
#include<mutex>
#include<iostream>

namespace ProcessJRuntime { class pj_logger; }

class ProcessJRuntime::pj_logger {

private:

    static std::mutex LogMutex;

public:

    static void log() {

        std::lock_guard<std::mutex> log_lck(ProcessJRuntime::pj_logger::LogMutex);
        std::cout << std::endl;

    }

    template <typename T>
    static void log(T value) {

        std::lock_guard<std::mutex> log_lck(ProcessJRuntime::pj_logger::LogMutex);
        std::cout << value << std::endl;

    }

    template <typename T, typename... Ts>
    static void log(T value, Ts... values) {

        std::unique_lock<std::mutex> log_lck(ProcessJRuntime::pj_logger::LogMutex);
        std::cout << value;
        log_lck.unlock();
        log(values...);
    }

};

#endif
