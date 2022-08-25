/*!
 * Base Test class. Defines the basic testflow for any
 * unit test.
 *
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.1.0
 */

#ifndef UNLV_PROCESS_J_TEST_HPP
#define UNLV_PROCESS_J_TEST_HPP

namespace ProcessJTest { class Test; }

class ProcessJTest::Test {

    /// ---------------
    /// Private Members

private:

    /// --------------
    /// Static Members

    static ProcessJSystem::System* SystemHandle        ; /*< Pointer to the System                             */
    static ProcessJSystem::Flag    SystemInitialized   ; /*< Flag denoting if the System has been initialized  */

};

#endif
