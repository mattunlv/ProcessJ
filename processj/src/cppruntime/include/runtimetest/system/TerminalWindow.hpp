/*!
 * \brief Class that represents a terminal window
 *
 * A basic class that represents a terminal window.
 *
 * \author Carlos L. Cuenca
 * \version 0.1.0
 * \date 03/13/2022
 */

#ifndef UNLV_PROCESS_J_SYSTEM_TERIMINAL_WINDOW_HPP
#define UNLV_PROCESS_J_SYSTEM_TERIMINAL_WINDOW_HPP

namespace ProcessJSystem{ class TerminalWindow; }

class ProcessJSystem::TerminalWindow : public ProcessJSystem::WindowComponentListener {

    /// ---------------
    /// Private Members

private:

    ProcessJSystem::Size                                                           height      ; /*< The Terminal Window height    */
    ProcessJSystem::Size                                                           width       ; /*< The Terminal Window width     */
    ProcessJSystem::WindowComponentGroup*                                          rootView    ; /*< The root view                 */
    ProcessJSystem::Output<ProcessJSystem::Character, ProcessJSystem::Integer32>   output      ; /*< The output object             */

public:

    /*!
     * Primary Constructor, Initializes the TerminalWindow
     * to its' default state.
     *
     * \param width The desired terminal window width
     * \param height The desired terminal window height
     */

    TerminalWindow(ProcessJSystem::Integer32, ProcessJSystem::Integer32);

    /// ----------------------------------------
    /// ProcessJSystem::WindowComponentListener

    /*!
     * Invoked when the window component is dirty and needs to be drawn.
     *
     * \param component The Component to be drawn as a void pointer
     */

   void OnComponentDirty(void*);

   /*!
    * Invoked when a child view is requesting to be re-measured
    *
    * \parm component The Component that is requesting to be re-measured
    */

   void RequestLayout(void*);

    /*!
     * Mutates the terminal's root view
     *
     * \param rootView The root view
     */

    void setRootView(ProcessJSystem::WindowComponentGroup*);

};

#endif
