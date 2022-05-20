/*!
 * ProcessJTest::ArrayTest Declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_ARRAY_TEST_HPP
#define UNLV_PROCESS_J_ARRAY_TEST_HPP

namespace ProcessJTest { class ArrayTest; }

class ProcessJTest::ArrayTest: public ProcessJTest::Test {

    /// -----------------
    /// Protected Members

protected:

    /*!
     * Returns a new instance of ProcessJTest::TextComponent
     *
     * \return ProcessJTest::WindowComponent pointer.
     */

    ProcessJTest::WindowComponent* createWindowComponent() const;

    /*!
     * Contains the ProcessJTest::ArrayTest logic.
     */

    void run();

    /// --------------
    /// Public Members

public:

    /// --------------------
    /// Overloaded Operators

    /*!
     * Overloaded implicit/explicit conversion operator. Simply returns
     * the ProcessJTest::TextComponent reference associated
     * with the ProcessJTest::ArrayTest. If no ProcessJSystem::TextComponent
     * exists (ProcessJTest::Test::createWindowComponent is not overridden),
     * then this throws a ProcessJTest::Test::NoWindowComponentException().
     *
     * \return Mutable reference to the ProcessJ::Test::TextComponent.
     */

    operator ProcessJSystem::TextComponent&();

};

#endif
