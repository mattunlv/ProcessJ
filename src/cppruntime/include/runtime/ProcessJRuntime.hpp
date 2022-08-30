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
#include<variant>
#include<sstream>
#include<array>
#include<memory>
#include<cstring>
#include<sys/types.h>
#include<sched.h>
#include<pthread.h>
#include<stdio.h>
#include<stdarg.h>

#include<Types.hpp>
#include<Constants.hpp>
#include<Variables.hpp>

#include<Array.hpp>
#include<String.hpp>
#include<Logger.hpp>
#include<Utilities.hpp>
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
#include<Barrier.hpp>
#include<Alternation.hpp>
#include<Record.hpp>
#include<Protocol.hpp>
#include<Par.hpp>

#endif
