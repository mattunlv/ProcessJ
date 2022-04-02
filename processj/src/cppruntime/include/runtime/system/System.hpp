/*!
 * \brief Class that encapsulates an operating system.
 *
 * This is an abstract class that defines the interface for
 * an operating system in order to initialize terminal manipulation correctly
 * and issue the correct codes to the terminal instance.
 *
 * \author Carlos L. Cuenca
 * \version 0.1.0
 * \date 03/13/2022
 */

#ifndef UNLV_PROCESS_J_SYSTEM_HPP
#define UNLV_PROCESS_J_SYSTEM_HPP

namespace ProcessJRuntime{ class System; }

class ProcessJRuntime::System {

    /// ---------------
    /// Private Members

private:

    static ProcessJRuntime::Flag            IsTerminalRawMode       ; /*< Flag that denotes if the terminal is in raw mode      */
    static ProcessJRuntime::Flag            IsTerminalCookedMode    ; /*< Flag that denotes if the terminal is in cooked mode   */
    static ProcessJRuntime::SystemCommand   TerminalRawMode         ; /*< The SystemCommand value for terminal raw mode         */
    static ProcessJRuntime::SystemCommand   TerminalCookedMode      ; /*< The SystemCommand value for terminal cooked mode      */
    static ProcessJRuntime::SystemCommand   TerminalClear           ; /*< The SystemCommand value for terminal clear            */
    static ProcessJRuntime::Count           InstanceCount           ; /*< Instance count for system... just in case             */
    static ProcessJRuntime::System*         PrimaryInstance         ; /*< Singleton Instance                                    */

    /*!
     * Primary Constructor, Initializes the System
     * to its' default state
     */

    System();

public:

    static ProcessJRuntime::TerminalColor ColorReset                ; /*< Resets the terminal color */
    static ProcessJRuntime::TerminalColor ColorBlack                ; /*< Black                     */
    static ProcessJRuntime::TerminalColor ColorRed                  ; /*< Red                       */
    static ProcessJRuntime::TerminalColor ColorGreen                ; /*< Green                     */
    static ProcessJRuntime::TerminalColor ColorYellow               ; /*< Yellow                    */
    static ProcessJRuntime::TerminalColor ColorBlue                 ; /*< Blue                      */
    static ProcessJRuntime::TerminalColor ColorMagenta              ; /*< Magenta                   */
    static ProcessJRuntime::TerminalColor ColorCyan                 ; /*< Cyan                      */
    static ProcessJRuntime::TerminalColor ColorWhite                ; /*< White                     */
    static ProcessJRuntime::TerminalColor ColorBlackBold            ; /*< Bold Black                */
    static ProcessJRuntime::TerminalColor ColorRedBold              ; /*< Bold Red                  */
    static ProcessJRuntime::TerminalColor ColorGreenBold            ; /*< Bold Green                */
    static ProcessJRuntime::TerminalColor ColorYellowBold           ; /*< Bold Yellow               */
    static ProcessJRuntime::TerminalColor ColorBlueBold             ; /*< Bold Blue                 */
    static ProcessJRuntime::TerminalColor ColorMagentaBold          ; /*< Bold Magenta              */
    static ProcessJRuntime::TerminalColor ColorCyanBold             ; /*< Bold Cyan                 */
    static ProcessJRuntime::TerminalColor ColorWhiteBold            ; /*< Bold White                */

    /*!
     * Returns the singleton instance of ProcessJRuntime::System.
     *
     * \return ProcessJRuntime::System instance
     */

    static ProcessJRuntime::System* GetInstance();

    /*!
     * Sets the terminal to raw mode from the value of
     * System::TerminalRawMode
     */

    static void SetRawMode();

    /*!
     * Sets the terminal to cooked mode from the value
     * of System::TerminalCookedMode
     */

    static void SetCookedMode();

    /*!
     * Clears the terminal
     */

    static void Clear();

};

#endif
