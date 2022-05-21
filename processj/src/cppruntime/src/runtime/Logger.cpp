/*!
 * \brief ProcessJRuntime::Logger implementation
 *
 * \author Carlos L. Cuenca
 * \author Alexander C. Thomason
 * \date 05/20/2022
 * \version 1.3.0
 */

#include<Logger.hpp>

std::mutex ProcessJRuntime::pj_logger::LogMutex = std::mutex();
