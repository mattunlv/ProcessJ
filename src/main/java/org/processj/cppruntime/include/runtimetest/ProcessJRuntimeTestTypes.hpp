/*!
 *
 * ProcessJTest::Types declaration. Defines types that are used throughout
 * the ProcessJTest to promote refactorablility
 *
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_RUNTIME_TEST_TYPES_HPP
#define UNLV_PROCESS_J_RUNTIME_TEST_TYPES_HPP

namespace ProcessJTest {

    /*!
     * \var typedef std::chrono::high_resolution_clock::time_point TimePoint;
     * \brief TimePoint type declaration
     */

    typedef std::chrono::high_resolution_clock::time_point TimePoint;

    /*!
     * \var typedef bool Flag;
     * \brief Defines a flag that corresponds to true or false.
     */

    typedef bool Flag                                           ;

    /*!
     * \var typedef uint32_t UInteger32;
     * \brief Defines an unsigned 32-bit integer.
     */

    typedef uint32_t UInteger32                                 ;

    /*!
     * \var typedef ProcessJSystem::WindowComponent WindowComponent;
     * \brief Defines a WindowComponent
     */

    typedef ProcessJSystem::WindowComponent WindowComponent     ;

    /*!
     * \var typedef ProcessJSystem::TextComponent TextComponent;
     * \brief Defines a TextComponent
     */

    typedef ProcessJSystem::TextComponent TextComponent         ;

}


#endif
