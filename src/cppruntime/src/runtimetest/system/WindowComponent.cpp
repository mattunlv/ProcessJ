/*!
 * Primary Constructor, Initializes the WindowComponent
 * to its' default state
 *
 * \param height The height of the component
 * \param Width  The width  of the component
 * \param listener The ProcessJSystem::WindowComponentListener to receive
 * callbacks on ProcessJRuntim::WindowComponent state mutations
 */

#include<ProcessJSystem.hpp>

/// ---------------------
/// Static Initialization

#ifndef PROCESS_J_SYSTEM_WINDOW_COMPONENT_CONSTANTS
#define PROCESS_J_SYSTEM_WINDOW_COMPONENT_CONSTANTS

const ProcessJSystem::Integer32     ProcessJSystem::WindowComponent::WrapContent         = -2 ;
const ProcessJSystem::Integer32     ProcessJSystem::WindowComponent::FillParent          = -1 ;
const ProcessJSystem::Integer32     ProcessJSystem::WindowComponent::Exactly             =  0 ;
const ProcessJSystem::Orientation   ProcessJSystem::WindowComponent::Start               = -2 ;
const ProcessJSystem::Orientation   ProcessJSystem::WindowComponent::End                 = -1 ;
const ProcessJSystem::Orientation   ProcessJSystem::WindowComponent::Center              =  0 ;

#endif

ProcessJSystem::WindowComponent::WindowComponent(ProcessJSystem::WindowComponent::Listener* windowComponentListener):
buffer(), height(0), width(0), positionX(0), positionY(0),
backgroundFill(' '),
horizontalOrientation(ProcessJSystem::WindowComponent::Center),
verticalOrientation(ProcessJSystem::WindowComponent::Center),
horizontalViewSpecification(ProcessJSystem::WindowComponent::Exactly),
verticalViewSpecification(ProcessJSystem::WindowComponent::Exactly),
isDirty(false),
windowComponentListener(windowComponentListener) { /* Empty */ }

/*!
 * Removes any ProcessJTest::WindowComponent::Listeners
 * associated with the ProcessJTest::WindowComponent.
 * Calls back ProcessJTest::WindowComponent::Listener::OnChildReleased()
 * if the ProcessJTest::WindowComponent has a listener.
 */

ProcessJSystem::WindowComponent::~WindowComponent() {

    // Let the window component listener know of the release
    if(windowComponentListener)
        windowComponentListener->OnChildReleased(this);

}

/*!
 * Retrieves the ProcessJSystem::WindowComponent's view buffer
 *
 * \return Immuatable reference to a ProcessJSystem::Array
 */

const ProcessJSystem::Array<ProcessJSystem::Array<ProcessJSystem::Character>>& ProcessJSystem::WindowComponent::getBuffer() const {

    return buffer;

}

/*!
 * Mutates the height of the ProcessJSystem::WindowComponent
 *
 * \param newHeight The newHeight of the window component
 */

void ProcessJSystem::WindowComponent::setHeight(ProcessJSystem::Integer32 height) {

    this->height  = height  ;
    this->isDirty = true    ;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}

/*!
 * Mutates the width of the ProcessJSystem::WindowComponent
 *
 * \param newHeight The newHeight of the window component
 */

void ProcessJSystem::WindowComponent::setWidth(ProcessJSystem::Integer32 width) {

    this->width   = width   ;
    this->isDirty = true    ;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}

/*!
 * Mutates the x coordinate of the ProcessJSystem::WindowComponent
 *
 * \param newHeight The newHeight of the window component
 */

void ProcessJSystem::WindowComponent::setXPosition(ProcessJSystem::Integer32 positionX) {

    this->positionX = positionX ;
    this->isDirty   = true      ;

    if(windowComponentListener)
        windowComponentListener->OnComponentDirty(this);

}

/*!
 * Mutates the y coordinate of the ProcessJSystem::WindowComponent
 *
 * \param newHeight The newHeight of the window component
 */

void ProcessJSystem::WindowComponent::setYPosition(ProcessJSystem::Integer32 positionY) {

    this->positionY = positionY ;
    this->isDirty   = true      ;

    if(windowComponentListener)
        windowComponentListener->OnComponentDirty(this);

}

/*!
 * Mutates the backgroundFill of the ProcessJSystem::WindowComponent
 *
 * \param backgroundFill The desired backgroundFill for the ProcessJSystem::WindowComponent
 */

void ProcessJSystem::WindowComponent::setBackgroundFill(ProcessJSystem::Character backgroundFill) {

    this->backgroundFill = backgroundFill   ;
    this->isDirty        = true             ;

    if(windowComponentListener)
        windowComponentListener->OnComponentDirty(this);

}

/*!
 * Sets the horizontal orientation of the ProcessJSystem::WindowComponent
 *
 * \param horizontalOrientation The desired horizontalOrientation
 */

void ProcessJSystem::WindowComponent::setHorizontalOrientation(ProcessJSystem::Orientation horizontalOrientation) {

    this->horizontalOrientation = horizontalOrientation;

    if(windowComponentListener)
        windowComponentListener->OnComponentDirty(this);

}

/*!
 * Mutates the vertical orientation of the ProcessJSystem::WindowComponent
 *
 * \param verticalOrientation The desired verticalOrientation
 */

void ProcessJSystem::WindowComponent::setVerticalOrientation(ProcessJSystem::Orientation verticalOrientation) {

    this->verticalOrientation = verticalOrientation;

    if(windowComponentListener)
        windowComponentListener->OnComponentDirty(this);

}

/*!
 * Mutates the horizontal view specification
 *
 * \param horizontalViewSpecification The desired horizontal view
 * specification
 */

void ProcessJSystem::WindowComponent::setHorizontalViewSpecification(ProcessJSystem::Integer32 horizontalViewSpecification) {

    this->horizontalViewSpecification = horizontalViewSpecification;
    this->isDirty = true;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}


/*!
 * Mutates the vertical view specification
 *
 * \param verticalViewSpecification The desired vertical view
 * specification
 */

void ProcessJSystem::WindowComponent::setVerticalViewSpecification(ProcessJSystem::Integer32 verticalViewSpecification) {

    this->verticalViewSpecification = verticalViewSpecification;
    this->isDirty = true;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}


/*!
 * Mutates the ProcessJSystem::WindowComponentListener
 *
 * \param windowComponentListener The ProcessJSystem::WindowComponentListener
 * to assign
 */

void ProcessJSystem::WindowComponent::setWindowComponentListener(ProcessJSystem::WindowComponent::Listener* windowComponentListener){

    this->isDirty = true;

    this->windowComponentListener = windowComponentListener;

}

/*!
 * retrieves the height of the ProcessJSystem::WindowComponent
 *
 * \param newHeight The newHeight of the window component
 */

const ProcessJSystem::Integer32& ProcessJSystem::WindowComponent::getHeight() const {

    return height;

}

/*!
 * retrieves the width of the ProcessJSystem::WindowComponent
 *
 * \param newHeight The newHeight of the window component
 */

const ProcessJSystem::Integer32& ProcessJSystem::WindowComponent::getWidth() const {

    return width;

}

/*!
 * retrieves the x coordinate of the ProcessJSystem::WindowComponent
 *
 * \param newHeight The newHeight of the window component
 */

const ProcessJSystem::Integer32& ProcessJSystem::WindowComponent::getXPosition() const {

    return positionX;

}

/*!
 * retrieves the y coordinate of the ProcessJSystem::WindowComponent
 *
 * \param newHeight The newHeight of the window component
 */

const ProcessJSystem::Integer32& ProcessJSystem::WindowComponent::getYPosition() const {

    return positionY;

}

/*!
 * Retrieves the horizontal orientation
 *
 * \return Immutable reference to the horizontal orientation
 */

const ProcessJSystem::Orientation& ProcessJSystem::WindowComponent::getHorizontalOrientation() const {

    return horizontalOrientation;

}

/*!
 * Retrieves the vertical orientation
 *
 * \return Immutable reference to the vertical orientation
 */

const ProcessJSystem::Orientation& ProcessJSystem::WindowComponent::getVerticalOrientation() const {

    return horizontalOrientation;

}


/*!
 * Retrieves the horizontal view specification
 *
 * \return Immutable reference to the horizontal view specification
 */

const ProcessJSystem::Integer32& ProcessJSystem::WindowComponent::getHorizontalViewSpecification() const {

    return horizontalViewSpecification;

}

/*!
 * Retrieves the vertical view specification
 *
 * \return Immutable reference to the vertical view specification
 */

const ProcessJSystem::Integer32& ProcessJSystem::WindowComponent::getVerticalViewSpecification() const {

    return verticalViewSpecification;

}
