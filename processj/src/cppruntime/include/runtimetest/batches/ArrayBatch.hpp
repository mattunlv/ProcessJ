/*!
 * ProcessJTest::ArrayBatch Declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_ARRAY_TEST_HPP
#define UNLV_PROCESS_J_ARRAY_TEST_HPP

namespace ProcessJTest { class ArrayBatch; }

class ProcessJTest::ArrayBatch: public ProcessJTest::TestBatch {

    /// -----------------
    /// Protected Members

protected:

    /*!
     * Returns a new instance of ProcessJTest::VerticalLayout
     *
     * \return ProcessJTest::WindowComponent pointer.
     */

    ProcessJSystem::WindowComponentGroup* createWindowComponentGroup() const {

        // Create and return the ProcessJSystem::WindowComponentGroup
        return new ProcessJSystem::VerticalLayout(0);

    }

    /// -----
    /// Tests

    /*!
     * \brief Base test class for the ProcessJTest::ArrayBatch.
     *
     * \author Carlos L. Cuenca
     * \date 04/07/2022
     * \version 0.1.0
     */

    class ArrayTest: public ProcessJTest::Test {

        /// -----------------
        /// Protected Members

    protected:

        /*!
         * Creates the ProcessJSystem::WindowComponent that is common to all
         * ProcessJTest::ArrayBatch::ArrayTest instances.
         * \return ProcessJTest::WindowComponent pointer
         */

        ProcessJTest::WindowComponent* createWindowComponent() const {

            // Create the component
            ProcessJSystem::TextComponent* textComponent = new ProcessJTest::TextComponent(0);

            // Style the component
            textComponent->setLeftBorderFill('/');
            textComponent->setTopBorderFill('-');
            textComponent->setLeftBorderWidth(2);
            textComponent->setRightBorderWidth(2);
            textComponent->setBottomBorderWidth(1);
            textComponent->setTopBorderWidth(1);

            // Return it
            return textComponent;

        }

        /// --------------
        /// Public Members

    public:

        /*!
         * Overloaded implicit/explicit conversion operator. Simply returns
         * the ProcessJTest::TextComponent reference associated
         * with the ProcessJTest::ArrayTest. If no ProcessJSystem::TextComponent
         * exists (ProcessJTest::Test::createWindowComponent is not overridden),
         * then this throws a ProcessJTest::Test::NoWindowComponentException().
         *
         * \return Mutable reference to the ProcessJ::Test::TextComponent.
         */

        operator ProcessJSystem::TextComponent&() {

            // Static cast the super class's overloaded operator.
            return static_cast<ProcessJSystem::TextComponent&>(ProcessJTest::Test::operator ProcessJSystem::WindowComponent&());

        }

    };

    /*!
     * \brief Tests the ProcessJRuntime::Array<Type> as a stack-dynamic
     * local.
     *
     * \author Carlos L. Cuenca
     * \date 04/07/2022
     * \version
     */

    template<typename Type>
    class ArrayStackConstructorTest: public ProcessJTest::ArrayBatch::ArrayTest {

        /// -----------------------------------
        /// ProcessJTest::ArrayBatch::ArrayTest

    protected:

        void run() {

            // Get the view
            ProcessJSystem::TextComponent& view = *this;

            // Set the message
            view.setText("Starting ArrayStackTest");

            // Iterate through the range
            for(ProcessJSystem::UInteger32 size = ProcessJTest::RuntimeArrayMinimumSize;
                size < ProcessJTest::RuntimeArrayMaximumSize; size++) {

                // Create the ProcessJRuntime::Array
                ProcessJRuntime::Array<Type> array(size);

                // Assert so we don't continue if there's a discrepancy
                Assert::AreEqual(size, array.size());

                // Report Completion (if Applicable)
                if(!(size % ProcessJTest::RuntimeArrayCompletionInterval)) {

                    // Remove this and the std invocation. We don't want to muck with
                    // namespaces
                    ProcessJSystem::String string = "ArrayStackConstructorTest Completed: " +
                                                    std::to_string(size) + "/" +
                                                    std::to_string(ProcessJTest::RuntimeArrayMaximumSize);

                    // Set the text
                    view.setText(string.c_str());

                }

            }

        }

    };

    /*!
     * \brief Tests the ProcessJRuntime::Array<Type> for
     * resizing operations
     *
     * \author Carlos L. Cuenca
     * \date 04/07/2022
     * \version
     */

    template<typename Type>
    class ArrayResizeTest: public ProcessJTest::ArrayBatch::ArrayTest {

        /// -----------------------------------
        /// ProcessJTest::ArrayBatch::ArrayTest

    protected:

        void run() {

            // Get the view
            ProcessJSystem::TextComponent& view = *this;

            // Set the message
            view.setText("Starting ArrayResizeTest");

            // Create an array
            ProcessJRuntime::Array<Type> array;

            array.resize(10);

            array.resize(150);

        }

    };

    /*!
     * \brief Tests the ProcessJRuntime::Array<Type> for
     * out of bounds exception throwing
     *
     * \author Carlos L. Cuenca
     * \date 04/07/2022
     * \version
     */

    template<typename Type>
    class ArrayOutOfBoundsTest : public ProcessJTest::ArrayBatch::ArrayTest {

        /// -----------------------------------
        /// ProcessJTest::ArrayBatch::ArrayTest

    protected:

        void run() {

            // Get the view
            ProcessJSystem::TextComponent& view = *this;

            // Create the array
            ProcessJRuntime::Array<Type> array;

            // Loop through the range
            for(ProcessJSystem::Integer32 start = ProcessJTest::RuntimeArrayMinimumSize;
                start < ProcessJTest::RuntimeArrayMaximumSize; start++) {

                // Restart
                array.resize(start);

                // Make sure we don't continue if there's a discrepancy
                Assert::AreEqual(start, array.size());

                Assert::AreNotEqual(start, array.size());

                // Attempt to go out of bounds
                Assert::InvalidSubscriptAccessThrowsException(array, start);

                // Report Completion (if Applicable)
                if(!(start % ProcessJTest::RuntimeArrayCompletionInterval)) {

                    // Remove this and the std invocation. We don't want to muck with
                    // namespaces
                    ProcessJSystem::String string = "ArrayOutOfBoundsTest Completed: " +
                                                    std::to_string(start) + "/" +
                                                    std::to_string(ProcessJTest::RuntimeArrayMaximumSize);

                    // Set the text
                    view.setText(string.c_str());

                }

            }

        }

    };

    /// --------------
    /// Public Members

public:

    /// ------------
    /// Constructors

    /*!
     * Primary Constructor. Adds the tests to the batch.
     */

    ArrayBatch(): ProcessJTest::TestBatch() {

        add(new ProcessJTest::ArrayBatch::ArrayStackConstructorTest<ProcessJSystem::UInteger32>);
        add(new ProcessJTest::ArrayBatch::ArrayResizeTest<ProcessJSystem::UInteger32>);
        add(new ProcessJTest::ArrayBatch::ArrayOutOfBoundsTest<ProcessJSystem::UInteger32>);

    }


};

#endif
