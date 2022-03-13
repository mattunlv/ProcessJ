/*!
 * ProcessJRuntime implementation file. Allows compilation of
 * an object file.
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.0.0
 */

#include <ProcessJRuntime.hpp>

std::mutex ProcessJRuntime::pj_logger::log_mtx;
