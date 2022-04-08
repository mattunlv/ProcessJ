/*!
 * \brief Class that represents a terminal window
 *
 * A basic class that represents a terminal window.
 *
 * \author Carlos L. Cuenca
 * \version 0.1.0
 * \date 03/13/2022
 */

#ifndef UNLV_PROCESS_J_SYSTEM_WINDOW_COMPONENT_HPP
#define UNLV_PROCESS_J_SYSTEM_WINDOW_COMPONENT_HPP

namespace ProcessJSystem{ class WindowComponent; }

class ProcessJSystem::WindowComponent {

    /// --------------
    /// Public Classes

public:

    /// -------
    /// Classes

    /*!
     * Listener class that receives callbacks based on
     * ProcessJSystem::WindowComponent state changes.
     *
     * \author Carlos L. Cuenca
     * \date 04/07/2022
     * \version 0.1.0
     */

    class Listener {

        /// ---------------
        /// Public Members

    public:

        /*!
         * Invoked when the window component is dirty and needs to be drawn.
         *
         * \param component The Component to be drawn as a void pointer
         */

       virtual void OnComponentDirty(ProcessJSystem::WindowComponent*) = 0;

       /*!
        * Invoked when a child view is requesting to be re-measured
        *
        * \parm component The Component that is requesting to be re-measured
        */

       virtual void RequestLayout(ProcessJSystem::WindowComponent*) = 0;

       /*!
        * Invoked when a child is releasing itself.
        *
        * \param component The Component that had its' destructor called.
        */

       virtual void OnChildReleased(ProcessJSystem::WindowComponent*) = 0;


    };

    /// ------------------
    /// Protected Members

protected:

    ProcessJSystem::Array<ProcessJSystem::Array<ProcessJSystem::Character>> buffer     ; /*< The View's Buffer                                              */
    ProcessJSystem::Integer32                  height                                  ; /*< The Terminal Window height                                     */
    ProcessJSystem::Integer32                  width                                   ; /*< The Terminal Window width                                      */
    ProcessJSystem::Integer32                  positionX                               ; /*< The x position of the window component                         */
    ProcessJSystem::Integer32                  positionY                               ; /*< The y position of the window component                         */
    ProcessJSystem::Character                  backgroundFill                          ; /*< The background fill for the ProcessJSystem::WindowComponent    */
    ProcessJSystem::Orientation                verticalOrientation                     ; /*< Vertical Orientation                                           */
    ProcessJSystem::Orientation                horizontalOrientation                   ; /*< Horizontal Orientation                                         */
    ProcessJSystem::Integer32                  horizontalViewSpecification             ; /*< The horizontal view specification                              */
    ProcessJSystem::Integer32                  verticalViewSpecification               ; /*< The Vertical view specification                                */
    ProcessJSystem::Flag                       isDirty                                 ; /*< Flag denotes that the Window Component needs to be redrawn     */
    ProcessJSystem::WindowComponent::Listener* windowComponentListener                 ; /*< Send window state callbacks                                    */

    /// --------------
    /// Public Members

public:

    static const ProcessJSystem::Integer32     WrapContent         ;
    static const ProcessJSystem::Integer32     FillParent          ;
    static const ProcessJSystem::Integer32     Exactly             ;
    static const ProcessJSystem::Orientation   Start               ;
    static const ProcessJSystem::Orientation   End                 ;
    static const ProcessJSystem::Orientation   Center              ;

    /*!
     * Primary Constructor, Initializes the WindowComponent
     * to its' default state
     *
     * \param height The height of the component
     * \param Width  The width  of the component
     * \param listener The ProcessJSystem::WindowComponentListener to receive
     * callbacks on ProcessJRuntim::WindowComponent state mutations
     */

    WindowComponent(ProcessJSystem::WindowComponent::Listener*);

    /*!
     * Removes any ProcessJTest::WindowComponent::Listeners
     * associated with the ProcessJTest::WindowComponent.
     * Calls back ProcessJTest::WindowComponent::Listener::OnChildReleased()
     * if the ProcessJTest::WindowComponent has a listener.
     */

    virtual ~WindowComponent();

    /// -------
    /// Methods

    /*!
     * Invoked when the ProcessJSystem::WindowComponent should
     * measure itself. Passes the available width and height
     *
     * \param width The available width
     * \param height The available height
     */

    virtual void onMeasure(ProcessJSystem::Integer32, ProcessJSystem::Integer32) = 0;

    /*!
     * Retrieves the ProcessJSystem::WindowComponent's view buffer
     *
     * \return Immuatable reference to a ProcessJSystem::Array
     */

    const ProcessJSystem::Array<ProcessJSystem::Array<ProcessJSystem::Character>>& getBuffer() const;

    /*!
     * Mutates the height of the ProcessJSystem::WindowComponent
     *
     * \param newHeight The newHeight of the window component
     */

    void setHeight(ProcessJSystem::Integer32);

    /*!
     * Mutates the width of the ProcessJSystem::WindowComponent
     *
     * \param newHeight The newHeight of the window component
     */

    void setWidth(ProcessJSystem::Integer32);

    /*!
     * Mutates the x coordinate of the ProcessJSystem::WindowComponent
     *
     * \param newHeight The newHeight of the window component
     */

    void setXPosition(ProcessJSystem::Integer32);

    /*!
     * Mutates the y coordinate of the ProcessJSystem::WindowComponent
     *
     * \param newHeight The newHeight of the window component
     */

    void setYPosition(ProcessJSystem::Integer32);

    /*!
     * Mutates the backgroundFill of the ProcessJSystem::WindowComponent
     *
     * \param backgroundFill The desired backgroundFill for the ProcessJSystem::WindowComponent
     */

    void setBackgroundFill(ProcessJSystem::Character);

    /*!
     * Sets the horizontal orientation of the ProcessJSystem::WindowComponent
     *
     * \param horizontalOrientation The desired horizontalOrientation
     */

    void setHorizontalOrientation(ProcessJSystem::Orientation);

    /*!
     * Mutates the vertical orientation of the ProcessJSystem::WindowComponent
     *
     * \param verticalOrientation The desired verticalOrientation
     */

    void setVerticalOrientation(ProcessJSystem::Orientation);

    /*!
     * Mutates the horizontal view specification
     *
     * \param horizontalViewSpecification The desired horizontal view
     * specification
     */

    void setHorizontalViewSpecification(ProcessJSystem::Integer32);


    /*!
     * Mutates the vertical view specification
     *
     * \param verticalViewSpecification The desired vertical view
     * specification
     */

    void setVerticalViewSpecification(ProcessJSystem::Integer32);

    /*!
     * Mutates the ProcessJSystem::WindowComponentListener
     *
     * \param windowComponentListener The ProcessJSystem::WindowComponentListener
     * to assign
     */

    void setWindowComponentListener(ProcessJSystem::WindowComponent::Listener*);

    /*!
     * retrieves the height of the ProcessJSystem::WindowComponent
     *
     * \param newHeight The newHeight of the window component
     */

    const ProcessJSystem::Integer32& getHeight() const;

    /*!
     * retrieves the width of the ProcessJSystem::WindowComponent
     *
     * \param newHeight The newHeight of the window component
     */

    const ProcessJSystem::Integer32& getWidth() const;

    /*!
     * retrieves the x coordinate of the ProcessJSystem::WindowComponent
     *
     * \param newHeight The newHeight of the window component
     */

    const ProcessJSystem::Integer32& getXPosition() const;

    /*!
     * retrieves the y coordinate of the ProcessJSystem::WindowComponent
     *
     * \param newHeight The newHeight of the window component
     */

    const ProcessJSystem::Integer32& getYPosition() const;

    /*!
     * Retrieves the horizontal orientation
     *
     * \return Immutable reference to the horizontal orientation
     */

    const ProcessJSystem::Orientation& getHorizontalOrientation() const;

    /*!
     * Retrieves the vertical orientation
     *
     * \return Immutable reference to the vertical orientation
     */

    const ProcessJSystem::Orientation& getVerticalOrientation() const;

    /*!
     * Retrieves the horizontal view specification
     *
     * \return Immutable reference to the horizontal view specification
     */

    const ProcessJSystem::Integer32& getHorizontalViewSpecification() const;

    /*!
     * Retrieves the vertical view specification
     *
     * \return Immutable reference to the vertical view specification
     */

    const ProcessJSystem::Integer32& getVerticalViewSpecification() const;

    /// --------------------
    /// Overloaded Operators

    /*!
     * Invoked when the ProcessJSystem::WindowComponent should draw itself.
     *
     * \param outputStream The OutputStream to modify.
     * \return OutputStream
     */

    template<typename OutputStream>
    OutputStream& draw(OutputStream& outputStream) {

        // Clear the terminal
        ProcessJSystem::System::Clear();

        for(ProcessJSystem::Size row = 0; row < buffer.size(); row++) {

            // Print the row with a Linefeed
            outputStream << buffer[row];

            // Print a new line
            outputStream << ProcessJSystem::NewLine;

            // Move back
            outputStream.move((row + 2), 1);

        }

        return outputStream;

    }

    /*!
     * Draws the ProcessJSystem::Window component into the desired OutputStream
     *
     * \param outputStream The OutputStream to modify
     * \param windowComponent The ProcessJSystem::WindowComponent to output
     */

    template<typename OutputStream>
    friend OutputStream& operator<<(OutputStream& outputStream, ProcessJSystem::WindowComponent& windowComponent) {

        // Simply return the outputstream passed to draw
        return windowComponent.draw(outputStream);

    }

};

#endif
