/*!
 * \brief Represents a group of tests that test a problem
 * domain. This handles creating each of the test instances,
 * adding them to a list, creating the view for each test, and
 * returning the overall view that represents this batch.
 *
 * \author Carlos L. Cuenca
 * \date 04/07/2022
 * \version 0.1.0
 */

#ifndef UNLV_PROCESS_J_TEST_BATCH_HPP
#define UNLV_PROCESS_J_TEST_BATCH_HPP

namespace ProcessJTest { class TestBatch; }

class ProcessJTest::TestBatch {

    /// ---------------
    /// Private Members

private:

    /// --------------
    /// Static Members

    static ProcessJTest::UInteger32 Instances        ; /*< The number of active test batch instances                          */

    /// ----------------
    /// Member Variables

    ProcessJSystem::Vector<ProcessJTest::Test*> tests                   ; /*< The set of ProcessJTest::Tests that will execute within this batch */
    ProcessJTest::TimePoint                     start                   ; /*< ProcessJTest::TimePoint instance that represents the start time    */
    ProcessJTest::TimePoint                     end                     ; /*< ProcessJTest::TimePoint instance that represents the end time      */
    ProcessJSystem::WindowComponentGroup*       windowComponentGroup    ; /*< ProcessJSystem::WindowComponent that displays test results         */

    /// -------
    /// Methods

    /*!
     * Mutates the ProcessJTest::TestBatch's ProcessJSystem::WindowComponentGroup.
     *
     * \param windowComponent The desired ProcessJSystem::WindowComponentGroup
     */

    void setWindowComponentGroup(ProcessJSystem::WindowComponentGroup*);

    /// -----------------
    /// Protected Members

protected:

    /*!
     * Should return the ProcessJTest::WindowComponentGroup that
     * displays this test's state information
     *
     * \return ProcessJTest::WindowComponentGroup pointer.
     */

    virtual ProcessJSystem::WindowComponentGroup* createWindowComponentGroup() const;

    /*!
     * Marks the starting point of the ProcessJTest::TestBatch.
     * Any overrides should invoke ProcessJTest::TestBatchBatch::onStart()
     * in order to get the correct start time.
     */

    virtual void onStart();

    /*!
     * Marks the end point of the ProcessJTest::TestBatch.
     * Any overrides should invokke ProcessJTest::TestBatch::onEnd()
     * in order to get the correct end time.
     */

    virtual void onEnd();

    /*!
     * Adds a ProcessJTest::Test instance to the ProcessJTest::TestBatch
     * for execution. Gets executed during the next invocation of the ()
     * operator.
     *
     * \param test The test to add to the ProcessJ::TestBatch
     */

    void add(ProcessJTest::Test*);

    /*!
     * The test itself. All test logic should go here
     */

    void run();

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
     * Primary constructor. Initializes the ProcessJTest::TestBatch
     * to its' default state.
     */

    TestBatch();

    /*!
     * Destructor. Releases any memory managed by the
     * ProcessJTest::TestBatch
     */

    ~TestBatch();

    /// --------------------
    /// Overloaded Operators

    /*!
     * Overloaded callable operator. Begins running the test
     *
     * \return ProcessJTest::Flag that denotes if the test passed
     * or failed
     */

    ProcessJTest::TestBatch& operator()();

    /*!
     * Overloaded implicit/explicit conversion operator. Simply returns
     * the ProcessJSystem::WindowComponentGroup reference associated
     * with the ProcessJTest::TestBatch. If no ProcessJSystem::WindowComponentGroup
     * exists (ProcessJTest::TestBatch::createWindowComponent is not overridden),
     * then this throws a ProcessJTest::TestBatch::NoWindowComponentGroupException().
     *
     * \return Mutable reference to the ProcessJ::Test::WindowComponent.
     */

    operator ProcessJSystem::WindowComponentGroup&();

    /// ----------
    /// Exceptions

    /*!
     * Thrown if a ProcessJTest::TestBatch does not create a
     * ProcessJ::System::WindowComponent.
     */

    class NoWindowComponentGroupException: public ProcessJSystem::Exception {

        /*!
         * Returns the exception message that describes the error.
         */

        ProcessJSystem::StringLiteral what() throw() {

            return ProcessJTest::ExceptionMessageNoWindowComponentGroup;

        }

    };

};

#endif
