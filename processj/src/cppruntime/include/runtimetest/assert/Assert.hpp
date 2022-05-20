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

    /*!
     * Asserts that the given type will throw an exception when attempting
     * to access the specified element. If an exception is not thrown (and subsequently caught)
     * a SubscriptOperatorDoesNotThrowException will be thrown
     * \param instance An immutable reference to type
     */

    template<typename Type>
    static void InvalidSubscriptAccessThrowsException(const Type& instance, ProcessJSystem::Integer32 index) {

        // Note to future readers: If this looks redundant to you, or it looks like an exception is always
        // going to be thrown, take a look at subscript operator overloading for user-defined types
        // and read up on exceptions. If the given type throws an exception, this method will exit gracefully.
        try {

            // Attempt to access here
            instance[index];

            // If we're here, an exception was not thrown
            throw ProcessJTest::Assert::SubscriptOperatorDoesNotThrowException();

        } catch(ProcessJSystem::Exception& exception) { /* We do nothing here */ }

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

    /*!
     * Thrown when a Type's subscript operator does not throw an exception
     * when attempting to access with an invalid index.
     *
     * \author Carlos L. Cuenca
     * \date 04/07/2022
     * \version 0.1.0
     */

    class SubscriptOperatorDoesNotThrowException: public ProcessJSystem::Exception {

        ProcessJSystem::SimpleString what() throw() {

            return ProcessJSystem::AssertionErrorSubscriptOperatorDoesNotThrowExceptionMessage;

        }

    };

};

#endif
