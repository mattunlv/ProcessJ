/*!
 *
 * ProcessJSystem::Constants declaration. Defines constants that are used throughout
 * the ProcessJSystem to promote refactorablility
 *
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_SYSTEM_CONSTANTS_HPP
#define UNLV_PROCESS_J_SYSTEM_CONSTANTS_HPP

namespace ProcessJSystem {

    /// -------
    /// Strings

    static SimpleString AssertionErrorSubscriptOperatorDoesNotThrowExceptionMessage = "Error: Subscript operator does not throw exception with invalid indices.";
    static SimpleString AssertionErrorAreNotEqualMessage                            = "Error: Instances are not equal."                                         ;
    static SimpleString AssertionErrorAreEqualMessage                               = "Error: Instances are equal."                                             ;

    /// ----------
    /// Characters

    static Character NewLine = '\n';

}


#endif

