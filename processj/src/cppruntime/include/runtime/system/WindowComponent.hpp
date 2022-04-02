/*!
 * \brief Class that represents a terminal window
 *
 * A basic class that represents a terminal window.
 *
 * \author Carlos L. Cuenca
 * \version 0.1.0
 * \date 03/13/2022
 */

#ifndef UNLV_PROCESS_J_WINDOW_COMPONENT_HPP
#define UNLV_PROCESS_J_WINDOW_COMPONENT_HPP

namespace ProcessJRuntime{ class WindowComponent; }

class ProcessJRuntime::WindowComponent {

    /// ------------------
    /// Protected Members

protected:

    ProcessJRuntime::Size                       height                     ; /*< The Terminal Window height                                 */
    ProcessJRuntime::Size                       width                      ; /*< The Terminal Window width                                  */
    ProcessJRuntime::Integer32                  positionX                  ; /*< The x position of the window component                     */
    ProcessJRuntime::Integer32                  positionY                  ; /*< The y position of the window component                     */
    ProcessJRuntime::Flag                       isDirty                    ; /*< Flag denotes that the Window Component needs to be redrawn */
    ProcessJRuntime::WindowComponentListener*   windowComponentListener    ; /*< Send window state callbacks                                */

    /// --------------
    /// Public Members

public:

    /*!
     * Primary Constructor, Initializes the WindowComponent
     * to its' default state
     *
     * \param height The height of the component
     * \param Width  The width  of the component
     * \param listener The ProcessJRuntime::WindowComponentListener to receive
     * callbacks on ProcessJRuntim::WindowComponent state mutations
     */

    WindowComponent(ProcessJRuntime::Size, ProcessJRuntime::Size, ProcessJRuntime::WindowComponentListener*);

    /// -------
    /// Methods

    /*!
     * Mutates the height of the ProcessJRuntime::WindowComponent
     *
     * \param newHeight The newHeight of the window component
     */

    void setHeight(ProcessJRuntime::Size);

    /*!
     * Mutates the width of the ProcessJRuntime::WindowComponent
     *
     * \param newHeight The newHeight of the window component
     */

    void setWidth(ProcessJRuntime::Size);

    /*!
     * Mutates the x coordinate of the ProcessJRuntime::WindowComponent
     *
     * \param newHeight The newHeight of the window component
     */

    void setXPosition(ProcessJRuntime::Integer32);

    /*!
     * Mutates the y coordinate of the ProcessJRuntime::WindowComponent
     *
     * \param newHeight The newHeight of the window component
     */

    void setYPosition(ProcessJRuntime::Integer32);

    /*!
     * Mutates the ProcessJRuntime::WindowComponentListener
     *
     * \param windowComponentListener The ProcessJRuntime::WindowComponentListener
     * to assign
     */

    void setWindowComponentListener(ProcessJRuntime::WindowComponentListener*);

};

#endif
