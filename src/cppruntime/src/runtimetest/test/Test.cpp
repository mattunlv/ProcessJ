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

/*!
 * Mutates the ProcessJTest::Test's ProcessJSystem::WindowComponent.
 *
 * \param windowComponent The desired ProcessJSystem::WindowComponent
 */

void ProcessJTest::Test::setWindowComponent(ProcessJSystem::WindowComponent* windowComponent) {

    // Release the child if we have one
    if(this->windowComponent) delete this->windowComponent;

    // Assign the new ProcessJSystem::WindowComponent
    this->windowComponent = windowComponent;

}

/*!
 * Should return the ProcessJTest::WindowComponent that
 * displays this test's state information. If this method
 * is not overridden, it will throw a ProcessJ::Test::NoWindowComponentException.
 *
 * \return ProcessJTest::WindowComponent pointer.
 */

ProcessJTest::WindowComponent* ProcessJTest::Test::createWindowComponent() const {

    // Throw this by default
    throw ProcessJTest::Test::NoWindowComponentException();

}

/*!
 * Marks the starting point of the ProcessJTest::Test
 */

void ProcessJTest::Test::onStart() {

    // Get the current time
    this->start = std::chrono::high_resolution_clock::now();

}

/*!
 * Marks the end point of the ProcessJTest::Test
 */

void ProcessJTest::Test::onEnd() {

    // Get the current time
    this->end = std::chrono::high_resolution_clock::now();

}

/*!
 * The test itself. All test logic should go here
 */

void ProcessJTest::Test::run() { /* Empty */ }

/*!
 * Should write results to the associated
 * ProcessJTest::WindowComponent corresponding with
 * the tests
 */

void ProcessJTest::Test::onDisplayResults() { /* Empty */ }

/*!
 * Executes the ProcessJTest::Test while marking the
 * start and end times of the ProcessJTest::Test. If the
 * ProcessJ::Test does not contain a ProcessJSystem::WindowComponent,
 * ProcessJ::Test::createWindowComponent() will be invoked here
 * and the Process::Test::WindowComponent will be created.
 */

void ProcessJTest::Test::execute() {

    // Set the window component
    if(!windowComponent) setWindowComponent(createWindowComponent());

    // First we invoke on start
    onStart();

    // Execute the logic
    run();

    // Mark the end
    onEnd();

    // Display the results to the give ProcessJTest::WindowComponent
    onDisplayResults();

}

/*!
 * Returns a ProcessJTest::Flag denoting if the test
 * passed. This should be implemented by any child classes
 * since test passing may vary
 *
 * \return Flag denoting if the ProcessJTest::Test passed of failed
 */

ProcessJTest::Flag ProcessJTest::Test::didPass() { return false; }

/*!
 * Overloaded callable operator. Begins running the test
 *
 * \return Mutable reference to ProcessJTest::Test
 */

ProcessJTest::Flag ProcessJTest::Test::operator()() {

    // Execute the test
    execute();

    // Return if the test passed or failed
    return didPass();

}

/*!
 * Overloaded implicit/explicit conversion operator. Simply returns
 * the ProcessJTest::WindowComponent reference associated
 * with the ProcessJTest::Test. If no ProcessJSystem::WindowComponent
 * exists (ProcessJTest::Test::createWindowComponent is not overridden),
 * then this throws a ProcessJTest::Test::NoWindowComponentException().
 *
 * \return Mutable reference to the ProcessJ::Test::WindowComponent.
 */

ProcessJTest::Test::operator ProcessJSystem::WindowComponent&() {

    // Attempt to create the ProcessJSystem::WindowComponent
    if(!windowComponent) setWindowComponent(createWindowComponent());

    // We are certain it is not null at this point
    return (*windowComponent);

}
