/*!
 * \brief Class that represents a terminal window
 *
 * A basic class that represents a terminal window.
 *
 * \author Carlos L. Cuenca
 * \version 0.1.0
 * \date 03/13/2022
 */

#ifndef UNLV_PROCESS_J_SYSTEM_TEXT_COMPONENT_HPP
#define UNLV_PROCESS_J_SYSTEM_TEXT_COMPONENT_HPP

namespace ProcessJSystem{ class TextComponent; }

class ProcessJSystem::TextComponent : public ProcessJSystem::WindowComponent {

    /// ------------------
    /// Protected Members

protected:

    ProcessJSystem::SimpleString   text                        ; /*< The actual text               */
    ProcessJSystem::UInteger32     textLength                  ; /*< The text length               */
    ProcessJSystem::UInteger32     leftBorderWidth             ; /*< The Left Border Width         */
    ProcessJSystem::UInteger32     rightBorderWidth            ; /*< The Right Border Width        */
    ProcessJSystem::UInteger32     topBorderWidth              ; /*< The Top Border Width          */
    ProcessJSystem::UInteger32     bottomBorderWidth           ; /*< The Bottom Border Width       */
    ProcessJSystem::Character      leftBorderFill              ; /*< Left Border fill character    */
    ProcessJSystem::Character      rightBorderFill             ; /*< Right Border fill character   */
    ProcessJSystem::Character      topBorderFill               ; /*< Top Border fill character     */
    ProcessJSystem::Character      bottomBorderFill            ; /*< Bottom Border fill character  */
    ProcessJSystem::Orientation    horizontalTextOrientation   ; /*< Horizontal Text Orientation   */
    ProcessJSystem::Orientation    verticalTextOrientation     ; /*< Vertical Text Orientation     */

    /*!
     * Invoked when the ProcessJSystem::TextComponent should
     * measure itself. Passes the available width and height
     *
     * \param width The available width
     * \param height The available height
     */

    void onMeasure(ProcessJSystem::Integer32, ProcessJSystem::Integer32);

    /// --------------
    /// Public Members

public:

    /*!
     * Primary Constructor, Initializes the TextComponent
     * to its' default state
     *
     * \param height The height of the component
     * \param Width  The width  of the component
     * \param listener The ProcessJSystem::TextComponentListener to receive
     * callbacks on ProcessJRuntim::TextComponent state mutations
     */

    TextComponent(ProcessJSystem::WindowComponent::Listener*);

    /*!
     * Sets the left border fill
     *
     * \param leftBorderFill The desired left border fill
     */

    void setLeftBorderFill(ProcessJSystem::Character);


    /*!
     * Sets the right border fill
     *
     * \param rightBorderFill The desired right border fill
     */

    void setRightBorderFill(ProcessJSystem::Character);


    /*!
     * Sets the top borer fill
     *
     * \param topBorderFill The desired top border fill
     */

    void setTopBorderFill(ProcessJSystem::Character);

    /*!
     * Sets the bottom borer fill
     *
     * \param bottomBorderFill The desired bottom border fill
     */

    void setBottomBorderFill(ProcessJSystem::Character);

    /*!
     * Sets the border fill
     *
     * \param topBorderFill The desired border fill
     */

    void setBorderFill(ProcessJSystem::Character);

    /*!
     * Sets the left border width
     *
     * \param leftBorderFill The desired left border width
     */

    void setLeftBorderWidth(ProcessJSystem::UInteger32);


    /*!
     * Sets the right border width
     *
     * \param rightBorderWidth The desired right border width
     */

    void setRightBorderWidth(ProcessJSystem::UInteger32);


    /*!
     * Sets the top borer width
     *
     * \param topBorderWidth The desired top border width
     */

    void setTopBorderWidth(ProcessJSystem::UInteger32);

    /*!
     * Sets the bottom borer width
     *
     * \param bottomBorderWidth The desired bottom border width
     */

    void setBottomBorderWidth(ProcessJSystem::UInteger32);

    /*!
     * Sets the border width
     *
     * \param topBorderWidth The desired border width
     */

    void setBorderWidth(ProcessJSystem::UInteger32);

    /*!
     * Sets the text.
     *
     * \param text The desired Text
     */

    void setText(ProcessJSystem::StringLiteral);

    /*!
     * Sets the horizontal text orientation
     *
     * \param horizontal text orientation The desired horizontal text orientation
     */

    void setHorizontalTextOrientation(ProcessJSystem::Orientation);

    /*!
     * Sets the vertical text orientation
     *
     * \param vertical text orientation The desired vertical text orientation
     */

    void setVerticalTextOrientation(ProcessJSystem::Orientation);

};

#endif
