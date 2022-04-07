/*!
 * \brief Text Component implementation
 *
 * \author Carlos L. Cuenca
 * \date 03/15/2022
 * \version 1.1.0
 */

#include<ProcessJSystem.hpp>

/*!
 * Primary Constructor, Initializes the TextComponent
 * to its' default state
 *
 * \param height The height of the component
 * \param Width  The width  of the component
 * \param listener The ProcessJSystem::TextComponentListener to receive
 * callbacks on ProcessJRuntim::TextComponent state mutations
 */

ProcessJSystem::TextComponent::TextComponent(ProcessJSystem::WindowComponent::Listener* windowComponentListener):
ProcessJSystem::WindowComponent(windowComponentListener),
text(0), textLength(0), leftBorderWidth(0), rightBorderWidth(0),
topBorderWidth(0), bottomBorderWidth(0), leftBorderFill('/'), rightBorderFill('/'),
topBorderFill('-'), bottomBorderFill('='), horizontalTextOrientation(ProcessJSystem::WindowComponent::Center),
verticalTextOrientation(ProcessJSystem::WindowComponent::Center) { /* Empty */ }

/*!
 * Invoked when the ProcessJSystem::TextComponent should
 * measure itself. Passes the available width and height
 *
 * \param width The available width
 * \param height The available height
 */

void ProcessJSystem::TextComponent::onMeasure(ProcessJSystem::Integer32 width, ProcessJSystem::Integer32 height) {

    this->width     = width     ;
    this->height    = height    ;

    // Resize the buffer
    buffer.resize(height);

    // Resize the buffer
    for(ProcessJSystem::Size row = 0; row < buffer.size(); row++)
        buffer[row].resize(width);

    if((width > 0) && (height > 0)) {

        // Set it to Empty
        for(ProcessJSystem::Size row = 0; row < buffer.size(); row++)
            for(ProcessJSystem::Size column = 0; column < buffer[row].size(); column++)
                buffer[row][column] = backgroundFill;

        for(ProcessJSystem::Size row = 0; row < buffer.size(); row++)
            for(ProcessJSystem::Size column = 0; column < leftBorderWidth; column++)
                buffer[row][column] = leftBorderFill;

        for(ProcessJSystem::Size row = 0; row < buffer.size(); row++)
            for(ProcessJSystem::Size column = (width - rightBorderWidth); column < width; column++)
                buffer[row][column] = rightBorderFill;

        for(ProcessJSystem::Size row = 0; row < topBorderWidth; row++)
            for(ProcessJSystem::Size column = 0; column < buffer[row].size(); column++)
                buffer[row][column] = topBorderFill;

        for(ProcessJSystem::Size row = (height - bottomBorderWidth); row < buffer.size(); row++)
            for(ProcessJSystem::Size column = 0; column < buffer[row].size(); column++)
                buffer[row][column] = bottomBorderFill;

        ProcessJSystem::UInteger32 textXPosition = 0;
        ProcessJSystem::UInteger32 textYPosition = 0;
        ProcessJSystem::UInteger32 index         = 0;

        if(verticalTextOrientation == ProcessJSystem::WindowComponent::End)
            textYPosition = (buffer.size() - 1);

        else if(verticalTextOrientation == ProcessJSystem::WindowComponent::Center)
            textYPosition = (buffer.size() - 1) / 2;

        if(horizontalTextOrientation == ProcessJSystem::WindowComponent::End)
            textXPosition = width - textLength;

        else if(horizontalTextOrientation == ProcessJSystem::WindowComponent::Center)
            textXPosition = ((width / 2) - (textLength / 2));

        if(textXPosition < leftBorderWidth) textXPosition = leftBorderWidth;

        if(textYPosition < topBorderWidth) textYPosition = topBorderWidth;

        for(;(index < textLength) && (textXPosition < width); textXPosition++)
            buffer[textYPosition][textXPosition] = text[index++];

    }

}

/*!
 * Sets the left border fill
 *
 * \param leftBorderFill The desired left border fill
 */

void ProcessJSystem::TextComponent::setLeftBorderFill(ProcessJSystem::Character leftBorderFill) {

    this->leftBorderFill = leftBorderFill;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}


/*!
 * Sets the right border fill
 *
 * \param rightBorderFill The desired right border fill
 */

void ProcessJSystem::TextComponent::setRightBorderFill(ProcessJSystem::Character rightBorderFill) {

    this->rightBorderFill = rightBorderFill;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}


/*!
 * Sets the top borer fill
 *
 * \param topBorderFill The desired top border fill
 */

void ProcessJSystem::TextComponent::setTopBorderFill(ProcessJSystem::Character topBorderFill) {

    this->topBorderFill = topBorderFill;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}

/*!
 * Sets the bottom borer fill
 *
 * \param bottomBorderFill The desired bottom border fill
 */

void ProcessJSystem::TextComponent::setBottomBorderFill(ProcessJSystem::Character bottomBorderFill) {

    this->bottomBorderFill = bottomBorderFill;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}

/*!
 * Sets the border fill
 *
 * \param topBorderFill The desired border fill
 */

void ProcessJSystem::TextComponent::setBorderFill(ProcessJSystem::Character borderFill) {

    this->leftBorderFill    = borderFill    ;
    this->rightBorderFill   = borderFill    ;
    this->topBorderFill     = borderFill    ;
    this->bottomBorderFill  = borderFill    ;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}

/*!
 * Sets the left border width
 *
 * \param leftBorderFill The desired left border width
 */

void ProcessJSystem::TextComponent::setLeftBorderWidth(ProcessJSystem::UInteger32 leftBorderWidth) {

    this->leftBorderWidth = leftBorderWidth;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}


/*!
 * Sets the right border width
 *
 * \param rightBorderWidth The desired right border width
 */

void ProcessJSystem::TextComponent::setRightBorderWidth(ProcessJSystem::UInteger32 rightBorderWidth) {

    this->rightBorderWidth = rightBorderWidth;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}


/*!
 * Sets the top borer width
 *
 * \param topBorderWidth The desired top border width
 */

void ProcessJSystem::TextComponent::setTopBorderWidth(ProcessJSystem::UInteger32 topBorderWidth) {

    this->topBorderWidth = topBorderWidth;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}

/*!
 * Sets the bottom borer width
 *
 * \param bottomBorderWidth The desired bottom border width
 */

void ProcessJSystem::TextComponent::setBottomBorderWidth(ProcessJSystem::UInteger32 bottomBorderWidth) {

    this->bottomBorderWidth = bottomBorderWidth;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}

/*!
 * Sets the border width
 *
 * \param topBorderWidth The desired border width
 */

void ProcessJSystem::TextComponent::setBorderWidth(ProcessJSystem::UInteger32 borderWidth) {

    this->leftBorderWidth   = borderWidth   ;
    this->rightBorderWidth  = borderWidth   ;
    this->topBorderWidth    = borderWidth   ;
    this->bottomBorderWidth = borderWidth   ;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}

/*!
 * Sets the text.
 *
 * \param text The desired Text
 */

void ProcessJSystem::TextComponent::setText(ProcessJSystem::SimpleString string) {

    ProcessJSystem::UInteger32     length  = 0          ;
    ProcessJSystem::SimpleString   current = string     ;

    // Count the characters
    while(*current++) length++;

    this->textLength = length   ;
    this->text       = string   ;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}

 /*!
 * Sets the horizontal text orientation
 *
 * \param horizontal text orientation The desired horizontal text orientation
 */

void ProcessJSystem::TextComponent::setHorizontalTextOrientation(ProcessJSystem::Orientation horizontalTextOrientation) {

    this->horizontalTextOrientation = horizontalTextOrientation;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}

 /*!
 * Sets the vertical text orientation
 *
 * \param vertical text orientation The desired vertical text orientation
 */

void ProcessJSystem::TextComponent::setVerticalTextOrientation(ProcessJSystem::Orientation verticalTextOrientation) {

    this->verticalTextOrientation = verticalTextOrientation;

    if(windowComponentListener)
        windowComponentListener->RequestLayout(this);

}
