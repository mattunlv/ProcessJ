/*!
 * \brief System Implementation File
 *
 * \author Carlos L. Cuenca
 * \version 1.1.0
 * \date 03/13/2022
 */

#include <ProcessJRuntime.hpp>

/// ----------------------------
/// Static Member Initialization

ProcessJRuntime::Count           ProcessJRuntime::System::InstanceCount           = 0                   ;
ProcessJRuntime::SystemCommand   ProcessJRuntime::System::TerminalRawMode         = "stty raw"          ;
ProcessJRuntime::SystemCommand   ProcessJRuntime::System::TerminalCookedMode      = "stty cooked"       ;
ProcessJRuntime::SystemCommand   ProcessJRuntime::System::TerminalClear           = "clear"             ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorReset              = "\033[0m"           ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorBlack              = "\033[30m"          ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorRed                = "\033[31m"          ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorGreen              = "\033[32m"          ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorYellow             = "\033[33m"          ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorBlue               = "\033[34m"          ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorMagenta            = "\033[35m"          ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorCyan               = "\033[36m"          ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorWhite              = "\033[37m"          ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorBlackBold          = "\033[1m\033[30m"   ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorRedBold            = "\033[1m\033[31m"   ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorGreenBold          = "\033[1m\033[32m"   ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorYellowBold         = "\033[1m\033[33m"   ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorBlueBold           = "\033[1m\033[34m"   ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorMagentaBold        = "\033[1m\033[35m"   ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorCyanBold           = "\033[1m\033[36m"   ;
ProcessJRuntime::TerminalColor   ProcessJRuntime::System::ColorWhiteBold          = "\033[1m\033[37m"   ;
ProcessJRuntime::Flag            ProcessJRuntime::System::IsTerminalRawMode       = false               ;
ProcessJRuntime::Flag            ProcessJRuntime::System::IsTerminalCookedMode    = true                ;
ProcessJRuntime::System*         ProcessJRuntime::System::PrimaryInstance         = 0                   ;

ProcessJRuntime::System::System() {

    ProcessJRuntime::System::InstanceCount++;

}

/*!
 * Returns the singleton instance of ProcessJRuntime::System.
 *
 * \return ProcessJRuntime::System instance
 */

ProcessJRuntime::System* ProcessJRuntime::System::GetInstance() {

    if(!ProcessJRuntime::System::PrimaryInstance)
        ProcessJRuntime::System::PrimaryInstance = new ProcessJRuntime::System();

    return ProcessJRuntime::System::PrimaryInstance;

}

void ProcessJRuntime::System::SetRawMode() {

    if(!ProcessJRuntime::System::IsTerminalRawMode) {


        FILE* pipe = popen(ProcessJRuntime::System::TerminalRawMode, "r");

        pclose(pipe);

        ProcessJRuntime::System::IsTerminalRawMode    = true  ;
        ProcessJRuntime::System::IsTerminalCookedMode = false ;

    }

}

void ProcessJRuntime::System::SetCookedMode() {

    if(!ProcessJRuntime::System::IsTerminalCookedMode) {

        FILE* pipe = popen(ProcessJRuntime::System::TerminalCookedMode, "r");

        pclose(pipe);

        ProcessJRuntime::System::IsTerminalCookedMode = true  ;
        ProcessJRuntime::System::IsTerminalRawMode    = false ;

    }

}

void ProcessJRuntime::System::Clear() {

    std::system(ProcessJRuntime::System::TerminalClear);

}
