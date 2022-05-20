/*!
 * \brief ProcessJSystem::TerminalWindow implementation
 *
 * \author Carlos L. Cuenca
 * \date 03/15/2022
 * \version 1.1.0
 */

#include<ProcessJSystem.hpp>

/*!
 * Primary Constructor, Initializes the TerminalWindow
 * to its' default state
 */

ProcessJSystem::TerminalWindow::TerminalWindow(ProcessJSystem::Integer32 width, ProcessJSystem::Integer32 height):
width(width), height(height), rootView(0), output() { /* Empty */ }

/*!
 * Invoked when the window component is dirty and needs to be drawn.
 *
 * \param component The Component to be drawn as a void pointer
 */

void ProcessJSystem::TerminalWindow::OnComponentDirty(void* component) {

    // Resize first
    output.resize(width, height);

    // Attempt to convert
    ProcessJSystem::WindowComponent* windowComponent = reinterpret_cast<ProcessJSystem::WindowComponent*>(component);

    // Redraw
    if(windowComponent)
        windowComponent->draw(output);

}

/*!
 * Invoked when a child view is requesting to be re-measured
 *
 * \parm component The Component that is requesting to be re-measured
 */

void ProcessJSystem::TerminalWindow::RequestLayout(void* component) {

    // Resize
    output.resize(width, height);

    // Dispatch measure
    if(rootView)
        rootView->onMeasure(width, height);

}

/*!
 * Mutates the terminal's root view
 *
 * \param rootView The root view
 */

void ProcessJSystem::TerminalWindow::setRootView(ProcessJSystem::WindowComponentGroup* rootView) {

    // Set the root view
    rootView = rootView;

    // Clear the terminal
    ProcessJSystem::System::Clear();

    // Move to the beginning
    output.move(1, 1);

    // Measure it
    rootView->onMeasure(width, height);

    // Draw
    rootView->draw(output);

    output << '\n';

}
