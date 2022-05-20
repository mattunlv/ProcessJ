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

    static ProcessJSystem::System*  SystemHandle         ; /*< Pointer to the System                             */
    static ProcessJSystem::Flag     SystemInitialized    ; /*< Flag denoting if the System has been initialized  */
    static ProcessJTest::UInteger32 Instances            ; /*< The number of active test instances               */

    /// ----------------
    /// Member Variables

    ProcessJTest::TimePoint          start             ; /*< ProcessJTest::TimePoint instance that represents the start time    */
    ProcessJTest::TimePoint          end               ; /*< ProcessJTest::TimePoint instance that represents the end time      */
    ProcessJTest::WindowComponent*   windowComponent   ; /*< ProcessJSystem::WindowComponent that displays test results         */

    /// -----------------
    /// Protected Members

protected:

    /*!
     * Should return the ProcessJTest::WindowComponent that
     * displays this test's state information
     *
     * \return ProcessJTest::WindowComponent pointer.
     */

    virtual ProcessJTest::WindowComponent* getCreatedWindowComponent() const;

    /*!
     * Marks the starting point of the ProcessJTest::Test
     */

    virtual void onStart();

    /*!
     * Marks the end point of the ProcessJTest::Test
     */

    virtual void onEnd();

    /*!
     * The test itself. All test logic should go here
     */

    virtual void run();

    /*!
     * Should write results to the associated
     * ProcessJTest::WindowComponent corresponding with
     * the tests
     */

    virtual void onDisplayResults();

    /*!
     * Executes the ProcessJTest::Test while marking the
     * start and end times of the ProcessJTest::Test
     */

    virtual void execute();

    /*!
     * Returns a ProcessJTest::Flag denoting if the test
     * passed. This should be implemented by any child classes
     * since test passing may vary
     */

    virtual ProcessJTest::Flag didPass();

    /// --------------
    /// Public Members

public:

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

    /// -------
    /// Methods

    /*!
     * Returns the window component associated with the
     * ProcessJTest::Test.
     *
     * \return ProcessJTest::WindowComponent instance
     */

    ProcessJTest::WindowComponent* const getWindowComponent() const;

    /// --------------------
    /// Overloaded Operators

    /*!
     * Overloaded callable operator. Begins running the test
     *
     * \return ProcessJTest::Flag that denotes if the test passed
     * or failed
     */

    ProcessJTest::Flag operator()();

};

#endif
