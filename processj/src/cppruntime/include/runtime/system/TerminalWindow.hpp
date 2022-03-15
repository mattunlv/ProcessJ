/*!
 * \brief Class that represents a terminal window
 *
 * A basic class that represents a terminal window.
 *
 * \author Carlos L. Cuenca
 * \version 0.1.0
 * \date 03/13/2022
 */

#ifndef UNLV_PROCESS_J_TERIMINAL_WINDOW_HPP
#define UNLV_PROCESS_J_TERIMINAL_WINDOW_HPP

namespace ProcessJRuntime{ class TerminalWindow; }

class ProcessJRuntime::TerminalWindow {

    /// ---------------
    /// Private Members

private:

    ProcessJRuntime::Size height    ; /*< The Terminal Window height    */
    ProcessJRuntime::Size width     ; /*< The Terminal Window width     */

public:

    /*!
     * Primary Constructor, Initializes the TerminalWindow
     * to its' default state
     */

    TerminalWindow();


};

#endif
