/*!
 * ProcessJTest::Test implementation. Implements the methods
 * and constructors as defined by ProcessJTest::Test
 *
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.1.0
 */

#include<ProcessJRuntimeTest.hpp>

/// ------------------------------
/// Static Member Initilialization

ProcessJRuntime::System* ProcessJTest::Test::SystemHandle       = 0 ;
ProcessJRuntime::Flag    ProcessJTest::Test::SystemInitialized  = 0 ;
