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

ProcessJSystem::VerticalLayout::VerticalLayout(ProcessJSystem::WindowComponentListener* windowComponentListener):
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
            buffer[row][column] = ' ';

    for(ProcessJSystem::Size child = 0; child < children.size(); child++) {

        ProcessJSystem::WindowComponent* component = children[child];

        ProcessJSystem::Integer32 childWidth  = 0   ;
        ProcessJSystem::Integer32 childHeight = 0   ;

        if(component->getHorizontalViewSpecification() == ProcessJSystem::WindowComponent::FillParent)
            childWidth = width;

        else if(component->getHorizontalViewSpecification() == ProcessJSystem::WindowComponent::Exactly)
            childWidth = component->getWidth();

        else childWidth = width;

        if(component->getVerticalViewSpecification() == ProcessJSystem::WindowComponent::FillParent)
            childHeight = height;

        else if(component->getVerticalViewSpecification() == ProcessJSystem::WindowComponent::Exactly)
            childHeight = component->getHeight();

        else childHeight = height;

        // Invoke On Measure
        component->onMeasure(childWidth, childHeight);

    }

    ProcessJSystem::Size threshold = 0;

    for(ProcessJSystem::Size index = 0; (index < children.size()) && (threshold < buffer.size()); index++) {

        ProcessJSystem::WindowComponent* component = children[index];

        for(ProcessJSystem::Size row = 0; (row < component->getBuffer().size()) &&
            (threshold < buffer.size()); row++, threshold++)
            for(ProcessJSystem::Size column = 0; (column < buffer[0].size()) &&
                (column < component->getBuffer()[row].size()); column++)
                    buffer[row][column] = component->getBuffer()[row][column];

    }


}

/*!
 * Invoked when the ProcessJSystem::VerticalLayout should draw itself
 *
 * \param outputStream The output stream to modify
 * \return modified outputStream
 */

template<typename OutputStream>
OutputStream& ProcessJSystem::VerticalLayout::draw(OutputStream& outputStream) {

    for(ProcessJSystem::Size row = 0; row < buffer.size(); row++) {
        for(ProcessJSystem::Size column = 0; column < buffer[row].size(); column++)
            outputStream << buffer[row][column];

        outputStream << '\n';

    }

    return outputStream;

}
