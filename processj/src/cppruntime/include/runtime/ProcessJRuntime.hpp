/*!
 * ProcessJRuntime library header file. Stitches the C++
 * runtime together into a single header.
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_RUNTIME_HPP
#define UNLV_PROCESS_J_RUNTIME_HPP

#pragma once

#include<iostream>
#include<ostream>
#include<string>
#include<mutex>
#include<chrono>
#include<atomic>
#include<thread>
#include<queue>
#include<sys/types.h>
#include<sched.h>
#include<pthread.h>

#include<Utilities.hpp>
#include<Logger.hpp>
#include<Process.hpp>
#include<Timer.hpp>
#include<TimerQueue.hpp>
#include<RunQueue.hpp>
#include<InactivePool.hpp>
#include<Scheduler.hpp>
#include<ChannelType.hpp>
#include<Channel.hpp>
#include<OneToOneChannel.hpp>
#include<OneToManyChannel.hpp>
#include<ManyToOneChannel.hpp>
#include<ManyToManyChannel.hpp>
//#include <pj_barrier.hpp>
//#include <pj_alt.hpp>
//#include <pj_record.hpp>
//#include <pj_protocol.hpp>
//#include <pj_par.hpp>
//#include <pj_array.hpp>
//#include <pj_string.hpp>

#endif
