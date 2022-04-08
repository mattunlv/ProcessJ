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

    /// -------
    /// Methods

    /*!
     * Mutates the ProcessJTest::Test's ProcessJSystem::WindowComponent.
     *
     * \param windowComponent The desired ProcessJSystem::WindowComponent
     */

    void setWindowComponent(ProcessJSystem::WindowComponent*);

    /// -----------------
    /// Protected Members

protected:

    /*!
     * Should return the ProcessJTest::WindowComponent that
     * displays this test's state information
     *
     * \return ProcessJTest::WindowComponent pointer.
     */

    virtual ProcessJSystem::WindowComponent* createWindowComponent() const;

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
     * Returns a ProcessJTest::Flag denoting if the test
     * passed. This should be implemented by any child classes
     * since test passing may vary
     */

    virtual ProcessJTest::Flag didPass();


    /*!
     * Executes the ProcessJTest::Test while marking the
     * start and end times of the ProcessJTest::Test
     */

    void execute();

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

    /// --------------------
    /// Overloaded Operators

    /*!
     * Overloaded callable operator. Begins running the test
     *
     * \return ProcessJTest::Flag that denotes if the test passed
     * or failed
     */

    ProcessJTest::Flag operator()();

    /*!
     * Overloaded implicit/explicit conversion operator. Simply returns
     * the ProcessJTest::WindowComponent reference associated
     * with the ProcessJTest::Test. If no ProcessJSystem::WindowComponent
     * exists (ProcessJTest::Test::createWindowComponent is not overridden),
     * then this throws a ProcessJTest::Test::NoWindowComponentException().
     *
     * \return Mutable reference to the ProcessJ::Test::WindowComponent.
     */

    operator ProcessJSystem::WindowComponent&();

    /// ----------
    /// Exceptions

    /*!
     * Thrown if a ProcessJTest::Test does not create a
     * ProcessJ::System::WindowComponent.
     */

    class NoWindowComponentException : public ProcessJSystem::Exception {

        /*!
         * Returns the exception message that describes the error.
         */

        ProcessJSystem::StringLiteral what() throw() {

            return ProcessJTest::ExceptionMessageNoWindowComponent;

        }

    };

};

#endif
