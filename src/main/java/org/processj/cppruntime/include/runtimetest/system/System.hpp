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

#ifndef UNLV_PROCESS_J_SYSTEM_SYSTEM_HPP
#define UNLV_PROCESS_J_SYSTEM_SYSTEM_HPP

namespace ProcessJSystem{ class System; }

class ProcessJSystem::System {

    /// ---------------
    /// Private Members

private:

    static ProcessJSystem::Flag            IsTerminalRawMode       ; /*< Flag that denotes if the terminal is in raw mode      */
    static ProcessJSystem::Flag            IsTerminalCookedMode    ; /*< Flag that denotes if the terminal is in cooked mode   */
    static ProcessJSystem::SystemCommand   TerminalRawMode         ; /*< The SystemCommand value for terminal raw mode         */
    static ProcessJSystem::SystemCommand   TerminalCookedMode      ; /*< The SystemCommand value for terminal cooked mode      */
    static ProcessJSystem::SystemCommand   TerminalClear           ; /*< The SystemCommand value for terminal clear            */
    static ProcessJSystem::Count           InstanceCount           ; /*< Instance count for system... just in case             */
    static ProcessJSystem::System*         PrimaryInstance         ; /*< Singleton Instance                                    */

    /*!
     * Primary Constructor, Initializes the System
     * to its' default state
     */

    System();

public:

    static ProcessJSystem::TerminalColor ColorReset                ; /*< Resets the terminal color */
    static ProcessJSystem::TerminalColor ColorBlack                ; /*< Black                     */
    static ProcessJSystem::TerminalColor ColorRed                  ; /*< Red                       */
    static ProcessJSystem::TerminalColor ColorGreen                ; /*< Green                     */
    static ProcessJSystem::TerminalColor ColorYellow               ; /*< Yellow                    */
    static ProcessJSystem::TerminalColor ColorBlue                 ; /*< Blue                      */
    static ProcessJSystem::TerminalColor ColorMagenta              ; /*< Magenta                   */
    static ProcessJSystem::TerminalColor ColorCyan                 ; /*< Cyan                      */
    static ProcessJSystem::TerminalColor ColorWhite                ; /*< White                     */
    static ProcessJSystem::TerminalColor ColorBlackBold            ; /*< Bold Black                */
    static ProcessJSystem::TerminalColor ColorRedBold              ; /*< Bold Red                  */
    static ProcessJSystem::TerminalColor ColorGreenBold            ; /*< Bold Green                */
    static ProcessJSystem::TerminalColor ColorYellowBold           ; /*< Bold Yellow               */
    static ProcessJSystem::TerminalColor ColorBlueBold             ; /*< Bold Blue                 */
    static ProcessJSystem::TerminalColor ColorMagentaBold          ; /*< Bold Magenta              */
    static ProcessJSystem::TerminalColor ColorCyanBold             ; /*< Bold Cyan                 */
    static ProcessJSystem::TerminalColor ColorWhiteBold            ; /*< Bold White                */

    /*!
     * Returns the singleton instance of ProcessJSystem::System.
     *
     * \return ProcessJSystem::System instance
     */

    static ProcessJSystem::System* GetInstance();

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
