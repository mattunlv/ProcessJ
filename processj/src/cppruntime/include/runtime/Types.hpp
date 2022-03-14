/*!
 *
 * ProcessJRuntime::Types declaration. Defines types that are used throughout
 * the ProcessJRuntime to promote refactorablility
 *
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_TYPES_HPP
#define UNLV_PROCESS_J_TYPES_HPP

namespace ProcessJRuntime {

    /*!
     * \var typedef bool Flag;
     * \brief Defines a flag that corresponds to true or false.
     */

    typedef bool Flag                   ;

    /*!
     * \var typedef uint32_t UInteger32;
     * \brief Defines an unsigned 32-bit integer.
     */

    typedef uint32_t UInteger32         ;

    /*!
     * \var typedef const char* StringLiteral;
     * \brief Defines a StringLiteral type.
     */

    typedef const char* StringLiteral   ;

    /*!
     * \var typedef std::exception Exception.
     * \brief Defines an Exception type used for error handling
     */

    typedef std::exception Exception    ;

    /*!
     * \var typedef const char* SystemCommand
     * \brief Type definition for commands issued to the terminal
     */

    typedef const char* SystemCommand   ;

    /*!
     * \var typedef const char* TerminalColor
     * \brief Type definition for a terminal color
     */

    typedef const char* TerminalColor   ;

    /*!
     * \var typedef uint32_t Position
     * \brief Type definition for a position
     */

    typedef uint32_t Position           ;

    /*!
     * \var typedef uint32_t Count;
     * \brief Type definition for a count
     */

    typedef uint32_t Count              ;

    /*!
     * \var typedef std::mutex Mutex
     * \brief Type definition for a Mutex
     */

    typedef std::mutex Mutex            ;


}


#endif
