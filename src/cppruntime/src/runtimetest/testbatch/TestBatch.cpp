/*!
 * ProcessJTest::TestBatch implementation file.
 * \author Carlos L. Cuenca
 * \version 0.1.0
 * \date 04/07/2022
 */

#include<ProcessJRuntimeTest.hpp>

/// ------------------------------
/// Static Member Initilialization

#ifndef PROCESS_J_RUNTIME_TEST_TEST_BATCH_STATIC_MEMBERS
#define PROCESS_J_RUNTIME_TEST_TEST_BATCH_STATIC_MEMBERS

ProcessJTest::UInteger32 ProcessJTest::TestBatch::Instances = 0 ;

#endif


/*!
 * Primary constructor. Initializes the ProcessJTest::TestBatch
 * to its' default state.
 */

ProcessJTest::TestBatch::TestBatch(): start(), end(), windowComponentGroup(0) {

    // Increase the number of active instances
    ProcessJTest::TestBatch::Instances++;

}

/*!
 * Destructor. Releases any memory managed by the
 * ProcessJTest::TestBatch
 */

ProcessJTest::TestBatch::~TestBatch() {

    // Iterate through each test and release them
    for(ProcessJSystem::Size index = 0; index < tests.size(); index++)
        if(tests[index]) {

            // Release the memory
            delete tests[index];

            // Clear it out
            tests[index] = 0;

        }

    // Clear them out
    tests.clear();

    // Release if we have it
    if(windowComponentGroup) delete windowComponentGroup;

    // Decrease the number of active instances
    ProcessJTest::TestBatch::Instances--;

}

/*!
 * Mutates the ProcessJTest::TestBatch's ProcessJSystem::WindowComponentGroup.
 *
 * \param windowComponentGroup The desired ProcessJSystem::WindowComponentGroup
 */

void ProcessJTest::TestBatch::setWindowComponentGroup(ProcessJSystem::WindowComponentGroup* windowComponentGroup) {

    // Release the child if we have one
    if(this->windowComponentGroup) delete this->windowComponentGroup;

    // Assign the new ProcessJSystem::WindowComponentGroup
    this->windowComponentGroup = windowComponentGroup;

}

/*!
 * Should return the ProcessJTest::WindowComponent that
 * displays this test's state information. If this method
 * is not overridden, it will throw a ProcessJ::Test::NoWindowComponentException.
 *
 * \return ProcessJTest::WindowComponent pointer.
 */

ProcessJSystem::WindowComponentGroup* ProcessJTest::TestBatch::createWindowComponentGroup() const {

    // Throw this by default
    throw ProcessJTest::TestBatch::NoWindowComponentGroupException();

}

/*!
 * Marks the starting point of the ProcessJTest::TestBatch
 */

void ProcessJTest::TestBatch::onStart() {

    // Get the current time
    this->start = std::chrono::high_resolution_clock::now();

}

/*!
 * Marks the end point of the ProcessJTest::TestBatch
 */

void ProcessJTest::TestBatch::onEnd() {

    // Get the current time
    this->end = std::chrono::high_resolution_clock::now();

}

/*!
 * Adds a ProcessJTest::Test instance to the ProcessJTest::TestBatch
 * for execution. Gets executed during the next invocation of the ()
 * operator.
 *
 * \param test The test to add to the ProcessJ::TestBatch
 */

void ProcessJTest::TestBatch::add(ProcessJTest::Test* test) {

    // If the test is null, leave
    if(!test) return;

    // If the ProcessJSystem::WindowComponentGroup does not exist, create one
    if(!windowComponentGroup) setWindowComponentGroup(createWindowComponentGroup());

    // Add the test to the list
    tests.push_back(test);

    // Add it to the window componentGroup
    windowComponentGroup->addChild(*test);

}

/*!
 * The test itself. All test logic should go here
 */

void ProcessJTest::TestBatch::run() {

    // We execute the tests linearly TODO: Change to concurrent
    for(ProcessJSystem::Size index = 0; index < tests.size(); index++)
        if(tests[index]) (*tests[index])();

}

/*!
 * Executes the ProcessJTest::TestBatch while marking the
 * start and end times of the ProcessJTest::TestBatch. If the
 * ProcessJ::Test does not contain a ProcessJSystem::WindowComponentGroup,
 * ProcessJ::Test::createWindowComponent() will be invoked here
 * and the Process::Test::WindowComponent will be created.
 */

void ProcessJTest::TestBatch::execute() {

    // Set the window component
    if(!windowComponentGroup) setWindowComponentGroup(createWindowComponentGroup());

    // First we invoke on start
    onStart();

    // Execute the logic
    run();

    // Mark the end
    onEnd();

}

/*!
 * Overloaded callable operator. Begins running the test
 *
 * \return Mutable reference to ProcessJTest::TestBatch
 */

ProcessJTest::TestBatch& ProcessJTest::TestBatch::operator()() {

    // Execute the test
    execute();

    // Return a reference to itself
    return *this;

}

/*!
 * Overloaded implicit/explicit conversion operator. Simply returns
 * the ProcessJTest::WindowComponent reference associated
 * with the ProcessJTest::TestBatch. If no ProcessJSystem::WindowComponentGroup
 * exists (ProcessJTest::TestBatch::createWindowComponent is not overridden),
 * then this throws a ProcessJTest::TestBatch::NoWindowComponentException().
 *
 * \return Mutable reference to the ProcessJ::Test::WindowComponent.
 */

ProcessJTest::TestBatch::operator ProcessJSystem::WindowComponentGroup&() {

    // Attempt to create the ProcessJSystem::WindowComponentGroup
    if(!windowComponentGroup) setWindowComponentGroup(createWindowComponentGroup());

    // We are certain it is not null at this point
    return (*windowComponentGroup);

}
