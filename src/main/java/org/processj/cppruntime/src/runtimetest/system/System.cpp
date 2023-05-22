/*!
 * \brief System Implementation File
 *
 * \author Carlos L. Cuenca
 * \version 1.1.0
 * \date 03/13/2022
 */

#include <ProcessJSystem.hpp>

/// ----------------------------
/// Static Member Initialization

ProcessJSystem::Count           ProcessJSystem::System::InstanceCount           = 0                   ;
ProcessJSystem::SystemCommand   ProcessJSystem::System::TerminalRawMode         = "stty raw"          ;
ProcessJSystem::SystemCommand   ProcessJSystem::System::TerminalCookedMode      = "stty cooked"       ;
ProcessJSystem::SystemCommand   ProcessJSystem::System::TerminalClear           = "clear"             ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorReset              = "\033[0m"           ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorBlack              = "\033[30m"          ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorRed                = "\033[31m"          ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorGreen              = "\033[32m"          ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorYellow             = "\033[33m"          ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorBlue               = "\033[34m"          ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorMagenta            = "\033[35m"          ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorCyan               = "\033[36m"          ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorWhite              = "\033[37m"          ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorBlackBold          = "\033[1m\033[30m"   ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorRedBold            = "\033[1m\033[31m"   ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorGreenBold          = "\033[1m\033[32m"   ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorYellowBold         = "\033[1m\033[33m"   ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorBlueBold           = "\033[1m\033[34m"   ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorMagentaBold        = "\033[1m\033[35m"   ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorCyanBold           = "\033[1m\033[36m"   ;
ProcessJSystem::TerminalColor   ProcessJSystem::System::ColorWhiteBold          = "\033[1m\033[37m"   ;
ProcessJSystem::Flag            ProcessJSystem::System::IsTerminalRawMode       = false               ;
ProcessJSystem::Flag            ProcessJSystem::System::IsTerminalCookedMode    = true                ;
ProcessJSystem::System*         ProcessJSystem::System::PrimaryInstance         = 0                   ;

ProcessJSystem::System::System() {

    ProcessJSystem::System::InstanceCount++;

}

/*!
 * Returns the singleton instance of ProcessJSystem::System.
 *
 * \return ProcessJSystem::System instance
 */

ProcessJSystem::System* ProcessJSystem::System::GetInstance() {

    if(!ProcessJSystem::System::PrimaryInstance)
        ProcessJSystem::System::PrimaryInstance = new ProcessJSystem::System();

    return ProcessJSystem::System::PrimaryInstance;

}

void ProcessJSystem::System::SetRawMode() {

    if(!ProcessJSystem::System::IsTerminalRawMode) {


        FILE* pipe = popen(ProcessJSystem::System::TerminalRawMode, "r");

        pclose(pipe);

        ProcessJSystem::System::IsTerminalRawMode    = true  ;
        ProcessJSystem::System::IsTerminalCookedMode = false ;

    }

}

void ProcessJSystem::System::SetCookedMode() {

    if(!ProcessJSystem::System::IsTerminalCookedMode) {

        FILE* pipe = popen(ProcessJSystem::System::TerminalCookedMode, "r");

        pclose(pipe);

        ProcessJSystem::System::IsTerminalCookedMode = true  ;
        ProcessJSystem::System::IsTerminalRawMode    = false ;

    }

}

void ProcessJSystem::System::Clear() {

    std::system(ProcessJSystem::System::TerminalClear);

}
