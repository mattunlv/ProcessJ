/*!
 * Implementation of a WindowComponentGroup
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

ProcessJSystem::WindowComponentGroup::WindowComponentGroup(ProcessJSystem::WindowComponent::Listener* windowComponentListener):
ProcessJSystem::WindowComponent(windowComponentListener) { /* Empty */ }

/*!
 * Invoked when the window component is dirty and needs to be drawn.
 *
 * \param component The Component to be drawn as a void pointer
 */

void ProcessJSystem::WindowComponentGroup::OnComponentDirty(ProcessJSystem::WindowComponent* component) {

    // Simply Delegate up
    if(windowComponentListener)
        windowComponentListener->OnComponentDirty(component);

}

/*!
 * Invoked when a child view is requesting to be re-measured
 *
 * \parm component The Component that is requesting to be re-measured
 */

void ProcessJSystem::WindowComponentGroup::RequestLayout(ProcessJSystem::WindowComponent* component) {

    if(windowComponentListener)
        windowComponentListener->RequestLayout(component);

}

/*!
 * Invoked when a child is releasing itself.
 *
 * \param component The Component that had its' destructor called.
 */

void ProcessJSystem::WindowComponentGroup::OnChildReleased(ProcessJSystem::WindowComponent* component) {

    auto start = children.begin();
    auto end   = children.end();

    // Iterate until the end or we find a match
    while((start != end) && (*start != component)) start++;

    // Erase the current if we haven't reached the end
    if(start != end) children.erase(start);

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);
}

/*!
 * Adds a child ProcessJSystem::WindowComponent to the
 * ProcessJSystem::WindowComponentGroup.
 *
 * \param child The child to add to the WindowComponent tree
 */

void ProcessJSystem::WindowComponentGroup::addChild(ProcessJSystem::WindowComponent& child) {

    ProcessJSystem::Flag inList = false;

    for(ProcessJSystem::Size index = 0; (index < children.size()) && !inList; index++)
        inList = (children[index] == &child);

    if(!inList) {

        this->isDirty = true;
        child.setWindowComponentListener(this);
        children.push_back(&child);

        if(windowComponentListener)
            windowComponentListener->RequestLayout(this);

    }

}
