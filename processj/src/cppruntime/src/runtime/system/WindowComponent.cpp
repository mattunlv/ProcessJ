/*!
 * Primary Constructor, Initializes the WindowComponent
 * to its' default state
 *
 * \param height The height of the component
 * \param Width  The width  of the component
 * \param listener The ProcessJRuntime::WindowComponentListener to receive
 * callbacks on ProcessJRuntim::WindowComponent state mutations
 */

#include<ProcessJRuntime.hpp>

ProcessJRuntime::WindowComponent::WindowComponent
(ProcessJRuntime::Size height, ProcessJRuntime::Size width, ProcessJRuntime::WindowComponentListener* windowComponentListener):
height(height), width(width), windowComponentListener(windowComponentListener) { /* Empty */ }

    /// -------
    /// Methods

/*!
 * Mutates the height of the ProcessJRuntime::WindowComponent
 *
 * \param newHeight The newHeight of the window component
 */

void ProcessJRuntime::WindowComponent::setHeight(ProcessJRuntime::Size height) {

    this->height  = height  ;
    this->isDirty = true    ;

    if(windowComponentListener)
        windowComponentListener->OnComponentDirty(this);

}

/*!
 * Mutates the width of the ProcessJRuntime::WindowComponent
 *
 * \param newHeight The newHeight of the window component
 */

void ProcessJRuntime::WindowComponent::setWidth(ProcessJRuntime::Size width) {

    this->width   = width   ;
    this->isDirty = true    ;

    if(windowComponentListener)
        windowComponentListener->OnComponentDirty(this);

}

/*!
 * Mutates the x coordinate of the ProcessJRuntime::WindowComponent
 *
 * \param newHeight The newHeight of the window component
 */

void ProcessJRuntime::WindowComponent::setXPosition(ProcessJRuntime::Integer32 positionX) {

    this->positionX = positionX ;
    this->isDirty   = true      ;

    if(windowComponentListener)
        windowComponentListener->OnComponentDirty(this);

}

/*!
 * Mutates the y coordinate of the ProcessJRuntime::WindowComponent
 *
 * \param newHeight The newHeight of the window component
 */

void ProcessJRuntime::WindowComponent::setYPosition(ProcessJRuntime::Integer32 positionY) {

    this->positionY = positionY ;
    this->isDirty   = true      ;

    if(windowComponentListener)
        windowComponentListener->OnComponentDirty(this);

}

/*!
 * Mutates the ProcessJRuntime::WindowComponentListener
 *
 * \param windowComponentListener The ProcessJRuntime::WindowComponentListener
 * to assign
 */

void ProcessJRuntime::WindowComponent::setWindowComponentListener(ProcessJRuntime::WindowComponentListener* windowComponentListener){

    this->isDirty = true;

    this->windowComponentListener = windowComponentListener;

}
