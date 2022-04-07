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

#ifndef PROCESS_J_RUNTIME_TEST_TEST_STATIC_MEMBERS
#define PROCESS_J_RUNTIME_TEST_TEST_STATIC_MEMBERS

ProcessJSystem::System*     ProcessJTest::Test::SystemHandle       = 0 ;
ProcessJSystem::Flag        ProcessJTest::Test::SystemInitialized  = 0 ;
ProcessJTest::UInteger32    ProcessJTest::Test::Instances          = 0 ;

#endif


/*!
 * Primary constructor. Initializes the ProcessJTest::Test
 * to its' default state.
 */

ProcessJTest::Test::Test(): start(), end(), windowComponent(0) {

    // Increase the number of active instances
    ProcessJTest::Test::Instances++;

}

/*!
 * Destructor. Releases any memory managed by the
 * ProcessJTest::Test
 */

ProcessJTest::Test::~Test() {

    // Release if we have it
    if(windowComponent) delete windowComponent;

    // Decrease the number of active instances
    ProcessJTest::Test::Instances--;

}
