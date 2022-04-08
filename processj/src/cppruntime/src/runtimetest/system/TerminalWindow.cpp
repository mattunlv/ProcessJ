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

void ProcessJSystem::TerminalWindow::OnComponentDirty(ProcessJSystem::WindowComponent* component) {

    // Resize first; The extra one is to account for an additional line (prompt)
    output.resize(width, height + 1);

    // If the root view is not null, redraw
    if(rootView) output << *rootView;

}

/*!
 * Invoked when a child view is requesting to be re-measured
 *
 * \parm component The Component that is requesting to be re-measured
 */

void ProcessJSystem::TerminalWindow::RequestLayout(ProcessJSystem::WindowComponent* component) {

    // Resize first; The extra one is to account for an additional line (prompt)
    output.resize(width, height + 1);

    // Re-measure the view tree
    if(rootView)
        rootView->onMeasure(width, height);

    // Redraw
    output << *(rootView);

}

/*!
 * Invoked when a child is releasing itself.
 *
 * \param component The Component that had its' destructor called.
 */

void ProcessJSystem::TerminalWindow::OnChildReleased(ProcessJSystem::WindowComponent* component) {

    // The only possible instance that can invoke this is the rootView. So clear
    // it out, this must mean the ProcessJSystem::TerminalWindow is being destroyed.
    if(rootView == component) rootView = 0;

}

/*!
 * Mutates the terminal's root view
 *
 * \param rootView The root view
 */

void ProcessJSystem::TerminalWindow::setRootView(ProcessJSystem::WindowComponentGroup& rootView) {

    // Set the root view
    this->rootView = &rootView;

    // Null check
    if(this->rootView) {

        // Set the listener
        this->rootView->setWindowComponentListener(this);

        // Measure it
        this->rootView->onMeasure(width, height);

        // Draw it
        output << *(this->rootView);

    }

}
