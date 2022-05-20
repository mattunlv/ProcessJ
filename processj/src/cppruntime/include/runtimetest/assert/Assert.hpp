/*!
 * Collection of static methods that aid with unit testing the runtime.
 * Test-related exceptions are also defined here.
 *
 * \author Carlos L. Cuenca
 * \date 04/07/2022
 * \version 1.1.0
 */

#ifndef UNLV_PROCESS_J_TEST_ASSERT_HPP
#define UNLV_PROCESS_J_TEST_ASSERT_HPP

namespace ProcessJTest { class Assert; }

class ProcessJTest::Assert {

    /// --------------
    /// Public Members

public:

    /// --------------
    /// Static Methods

    /*!
     * Asserts that the given Type pointer is not null.
     * If the given pointer is null, then a ProcessJTest::Assert::TypePointerIsNullException
     * is thrown.
     * \param pointer The pointer to the type to check
     */

    template<typename Type>
    static void IsNotNull(Type* pointer) {

        // If the pointer is null, throw an exception
        if(pointer == nullptr) throw ProcessJTest::Assert::TypePointerIsNullException();

    }

    /*!
     * Asserts that the given Type pointer is null.
     * If the given pointer is not null, then a ProcessJTest::Assert::TypePointerIsNotNullException
     * is thrown.
     * \param pointer The pointer to the type to check
     */

    template<typename Type>
    static void IsNull(Type* pointer) {

        // If the pointer is null, throw an exception
        if(pointer) throw ProcessJTest::Assert::TypePointerIsNotNullException();

    }

    /// ----------
    /// Exceptions

    /*!
     * Exception that gets thrown when a pointer is null.
     *
     * \author Carlos L. Cuenca
     * \date 04/07/2022
     * \version 0.1.0
     */

    class TypePointerIsNullException : public ProcessJSystem::Exception {

        ProcessJSystem::SimpleString what() throw() {

            return "Assertion Error: Type pointer is null.";

        }

    };

    /*!
     * Exception that gets thrown when a pointer is not null.
     *
     * \author Carlos L. Cuenca
     * \date 04/07/2022
     * \version 0.1.0
     */

    class TypePointerIsNotNullException : public ProcessJSystem::Exception {

        ProcessJSystem::SimpleString what() throw() {

            return "Assertion Error: Type pointer is not null.";

        }

    };


};

#endif
