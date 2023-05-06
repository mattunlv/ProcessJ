/*!
 *
 * ProcessJTest::Variable declaration. Defines variables that are used throughout
 * the ProcessJTest to promote refactorablility. Future releases should
 * implement command-line arguments that are able to modify the values.
 *
 * \author Carlos L. Cuenca
 * \date 04/07/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_RUNTIME_TEST_VARIABLES_HPP
#define UNLV_PROCESS_J_RUNTIME_TEST_VARIABLES_HPP

namespace ProcessJTest {

    /// -------------------------
    /// Individual Test Variables

    static ProcessJRuntime::UInteger32 RuntimeArrayMinimumSize          = 0             ;
    static ProcessJRuntime::UInteger32 RuntimeArrayMaximumSize          = 400000000     ;
    static ProcessJRuntime::UInteger32 RuntimeArrayCompletionInterval   = 10000         ;

}


#endif

