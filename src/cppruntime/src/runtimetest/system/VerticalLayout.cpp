/*!
 * Implementation of a VerticalLayout
 *
 * \author Carlos L. Cuenca
 * \date 03/14/2022
 * \version 1.0.0
 */

#include<ProcessJSystem.hpp>

/*!
 * Primary Constructor, Initializes the WindowComponent
 * to its' default state
 *
 * \param listener The ProcessJSystem::WindowComponentListener to receive
 * callbacks on ProcessJRuntim::WindowComponent state mutations
 */

ProcessJSystem::VerticalLayout::VerticalLayout(ProcessJSystem::WindowComponent::Listener* windowComponentListener):
ProcessJSystem::WindowComponentGroup(windowComponentListener) { /* Empty */ }

/*!
 * Invoked when the ProcessJSystem::WindowComponent should
 * measure itself. Passes the available width and height
 *
 * \param width The available width
 * \param height The available height
 */

void ProcessJSystem::VerticalLayout::onMeasure(ProcessJSystem::Integer32 width, ProcessJSystem::Integer32 height) {

    this->width     = width;
    this->height    = height;

    buffer.resize(height);

    for(ProcessJSystem::Size row = 0; row < buffer.size(); row++)
        buffer[row].resize(width);

    for(ProcessJSystem::Size row = 0; row < buffer.size(); row++)
        for(ProcessJSystem::Size column = 0; column < buffer[row].size(); column++)
            buffer[row][column] = backgroundFill;

    for(ProcessJSystem::Size child = 0; child < children.size(); child++) {

        // Retrieve the component
        ProcessJSystem::WindowComponent* component = children[child];

        // Invoke On Measure
        component->onMeasure(width, (height / children.size()));

    }

    ProcessJSystem::Size layoutRow      = 0 ;
    ProcessJSystem::Size layoutColumn   = 0 ;

    for(ProcessJSystem::Size index = 0; buffer.size() && (index < children.size()); index++) {

        if(layoutRow >= height) break;
        // Get the child
        ProcessJSystem::WindowComponent* component = children[index];

        // Iterate through the child
        for(ProcessJSystem::Size row = 0; (component) && (row < component->getBuffer().size()); row++) {

            if(layoutRow >= height) break;

            for(ProcessJSystem::Size column = 0; column < component->getBuffer()[row].size(); column++) {

                if(layoutColumn > width) break;

                buffer[layoutRow][layoutColumn++] = component->getBuffer()[row][column];

            }

            layoutRow++;
            layoutColumn = 0;

        }

    }

}
