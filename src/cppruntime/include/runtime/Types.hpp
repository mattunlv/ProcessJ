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
     * \var typedef int32_t Integer32;
     * \brief Defines a 32-bit integer.
     */

    typedef int32_t Integer32           ;

    /*!
     * \var typedef const char* StringLiteral;
     * \brief Defines a StringLiteral type.
     */

    typedef const char* StringLiteral   ;

    /*!
     * \var typedef std::string String;
     * \brief Defines a String type.
     */

    typedef std::string String          ;

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

    typedef int32_t Position            ;

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

    /*!
     * \ver typedef size_t Size;
     * \brief Type definition for a size in bytes.
     */

    typedef size_t Size                 ;

    /*!
     * \var template<typename Type> using Vector = std::vector<Type>;
     * \brief Type definition for a std::vector.
     */

    template<typename Type>
    using Vector = std::vector<Type>    ;

    /*!
     * \var typedef char Character;
     * \brief Type Definition for a Character
     */

    typedef char Character;

    /*!
     * \var typedef char* SimpleString
     * \brief Type definition for a simple string
     */

    typedef char* SimpleString          ;

    /*!
     * \var typedef int32_t Orientation
     * \brief Type Definition for an Orientation
     */

    typedef int32_t Orientation         ;

    /*!
     * \var typedef std::ostream OutputStream;
     * \brief Type definition for an OutputStream
     */

    typedef std::ostream OutputStream   ;

}


#endif
