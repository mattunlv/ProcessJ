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

    static ProcessJSystem::System* SystemHandle         ; /*< Pointer to the System                             */
    static ProcessJSystem::Flag    SystemInitialized    ; /*< Flag denoting if the System has been initialized  */
    static ProcessJTest::UInteger32 Instances           ; /*< The number of active test instances               */

    /// ----------------
    /// Member Variables

    ProcessJTest::TimePoint          start             ; /*< ProcessJTest::TimePoint instance that represents the start time    */
    ProcessJTest::TimePoint          end               ; /*< ProcessJTest::TimePoint instance that represents the end time      */
    ProcessJTest::WindowComponent*   windowComponent   ; /*< ProcessJSystem::WindowComponent that displays test results         */

    /// ------------
    /// Constructors

    /*!
     * Primary constructor. Initializes the ProcessJTest::Test
     * to its' default state.
     */

    Test();

    /*!
     * Destructor. Releases any memory managed by the
     * ProcessJTest::Test
     */

    ~Test();

};

#endif
