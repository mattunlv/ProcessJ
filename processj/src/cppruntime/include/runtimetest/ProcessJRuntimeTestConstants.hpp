/*!
 *
 * ProcessJTest::Constants declaration. Defines constants that are used throughout
 * the ProcessJTest to promote refactorablility
 *
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_RUNTIME_TEST_CONSTANTS_HPP
#define UNLV_PROCESS_J_RUNTIME_TEST_CONSTANTS_HPP

namespace ProcessJTest {

    /// ------------------
    /// Exception Messages

    static ProcessJSystem::StringLiteral ExceptionMessageNoWindowComponent      = "Error: Class does not create a ProcessJTest::WindowComponent.";
    static ProcessJSystem::StringLiteral ExceptionMessageNoWindowComponentGroup = "Error: Class does not create a ProcessJTest::WindowComponentGroup.";

}


#endif

