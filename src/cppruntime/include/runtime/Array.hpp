/*!
 * ProcessJRuntime::Array declaration & implementation.
 * Implements a parameterized array.
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_ARRAY_HPP
#define UNLV_PROCESS_J_ARRAY_HPP

namespace ProcessJRuntime {

    template<typename Type>
    class Array;

}

template<typename Type>
class ProcessJRuntime::Array {

    /// ---------------
    /// Private Members

private:

    Type*                       array   ; /*< The underlying block of memory for the ProcessJRuntime::Array */
    ProcessJRuntime::UInteger32 length  ; /*< The length of the array                                       */

    /// --------------
    /// Public Members

public:

    /// ------------
    /// Constructors

    /*!
     * Default Constructor. Initializes the ProcessJRuntime::Array
     * to its' default state.
     */

    Array();

    /*!
     * Initializes the ProcessJRuntime::Array with a default
     * size of the given length.
     *
     * \param length The desired length of the array.
     */

    Array(ProcessJRuntime::UInteger32);

    /*!
     * Initialize the ProcessJRuntime::Array as a copy
     * of the given memory boundaries.
     *
     * \param start The beginning of the block to copy.
     * \param end The end of the block to copy.
     */

    Array(Type*, Type*);

    /*!
     * Initializes the ProcessJRuntime::Array as a copy of the given
     * values in the std::initializer_list
     *
     * \param values The values to copy
     */

    Array(std::initializer_list<Type>);

    /*!
     * Destructor. Releases the memory held by the ProcessJRuntime::Array.
     */

    ~Array();

    /// -------
    /// Methods

    /*!
     * Returns the size of the ProcessJRuntime::Array.
     *
     * \return ProcessJRuntime::UInteger32
     */

    const ProcessJRuntime::UInteger32& size() const;

    /*!
     * Resizes the ProcessJRuntime::Array
     *
     * \param size The desired ProcessJRuntime::Array size
     */

    void resize(ProcessJRuntime::UInteger32);

    /// --------------------
    /// Overloaded Operators

    /*!
     * Attempts to access the element at the specified index.
     * If the index is greater than or equal to the length (out of bounds)
     * this will throw a ProcessJRuntime::Array::IndexOutOfBoundsException.
     *
     * \param index The index to access
     * \return Reference to a Type
     */

    Type& operator[](ProcessJRuntime::UInteger32);

    /*!
     * Attempts to access the element at the specified index.
     * If the index is greater than or equal to the length (out of bounds)
     * this will throw a ProcessJRuntime::Array::IndexOutOfBoundsException.
     *
     * \param index The index to access
     * \return Reference to an immutable Type
     */

    const Type& operator[](ProcessJRuntime::UInteger32) const;

    /*!
     * Overloaded assignment operator. Sets the current instance to a copy
     * of the right hand side and returns a mutable reference to Array.
     *
     * \param rightHandSide The ProcessJRuntim::Array to copy
     * \return mutableReference to ProcessJRuntime::Array
     */

    Array& operator=(Array& rightHandSide) {

        if((rightHandSide.size() > 0) && (length != rightHandSide.size())) {

            Type* newBlock = new Type[rightHandSide.size()];

            for(ProcessJRuntime::Size index = 0; (index < rightHandSide.size()) && (index < length); index++)
                newBlock[index] = rightHandSide[index];

            if(array) delete[] array;

            array  = newBlock             ;
            length = rightHandSide.size() ;

        } else if((length > 0) && (length == rightHandSide.size())) {

            for(ProcessJRuntime::Size index = 0; index < rightHandSide.size(); index++)
                array[index] = rightHandSide[index];

        } else {

            if(array) delete[] array;

            array   = 0;
            length  = 0;

        }

    }

    /*!
     * Overloaded Stream insertion operator. Inserts the data into
     * the given output stream
     *
     * \param outputStream The OutputStream to modify
     * \param ProcessJRuntime::Array The array to print
     * \return Mutable reference to OutputStream.
     */

    template<typename OutputStream>
    friend OutputStream& operator<<(OutputStream& outputStream, ProcessJRuntime::Array<Type>& array) {

        for(ProcessJRuntime::Size index = 0; index < array.size(); index++)
            outputStream << array[index];

    }

    /// ----------
    /// Exceptions

    /*!
     * ProcessJRuntime::Exception that gets thrown when attempting to
     * access an out of bounds index in the array
     *
     * \author Carlos L. Cuenca
     * \date 03/13/2022
     * \version 1.1.0
     */

    class IndexOutOfBoundsException : public ProcessJRuntime::Exception {

        /*!
         * Displays the message for the ProcessJRuntime::Array::IndexOutOfBoundsException
         *
         * \return StringLiteral
         */

        ProcessJRuntime::StringLiteral what() const noexcept {

            return ProcessJRuntime::ExceptionMessageIndexOutOfBounds;

        }

    };

};

/*!
 * Default Constructor. Initializes the ProcessJRuntime::Array
 * to its' default state.
 */

template<typename Type>
ProcessJRuntime::Array<Type>::Array():
    array(nullptr), length(0) { /* Empty */ }

/*!
 * Initializes the ProcessJRuntime::Array with a default
 * size of the given length.
 *
 * \param length The desired length of the array.
 */

template<typename Type>
ProcessJRuntime::Array<Type>::Array(ProcessJRuntime::UInteger32 length):
    array(new Type[length]), length(length) { /* Empty */ }

/*!
 * Initialize the ProcessJRuntime::Array as a copy
 * of the given memory boundaries.
 *
 * \param start The beginning of the block to copy.
 * \param end The end of the block to copy.
 */

template<typename Type>
ProcessJRuntime::Array<Type>::Array(Type* start, Type* end):
    array(0), length(0) {

    if((end - start) > 0) {

        array = new Type[end - start];
        length = (end - start);

    }

    // We'll traverse the array like this
    Type* original = start    ;
    Type* copy     = array    ;

    // Code golf!
    while(copy && start && (copy ^ original)) (*copy++ = *original++);

}

/*!
 * Initializes the ProcessJRuntime::Array as a copy of the given
 * values in the std::initializer_list
 *
 * \param values The values to copy
 */

template<typename Type>
ProcessJRuntime::Array<Type>::Array(std::initializer_list<Type> values):
    array(new Type[values.size()]), length(values.size()) {

    std::copy(values.begin(), values.end(), array);

}

/*!
 * Destructor. Releases the memory held by the ProcessJRuntime::Array.
 */

template<typename Type>
ProcessJRuntime::Array<Type>::~Array() {

    if(array) delete[] array;

    array = nullptr;

}

/*!
 * Returns the size of the ProcessJRuntime::Array.
 *
 * \return ProcessJRuntime::UInteger32
 */

template<typename Type>
const ProcessJRuntime::UInteger32& ProcessJRuntime::Array<Type>::size() const {

    return length;

}

/*!
 * Resizes the ProcessJRuntime::Array
 *
 * \param size The desired ProcessJRuntime::Array size
 */

template<typename Type>
void ProcessJRuntime::Array<Type>::resize(ProcessJRuntime::UInteger32 size) {

    if((size > 0) && (length != size)) {

        Type* newBlock = new Type[size];

        // Copy the contents over
        for(ProcessJRuntime::Size index = 0; (index < size) && (index < length); index++)
            newBlock[index] = array[index];

        // If there's an existing array, delete it
        if(array) delete[] array;

        // Set the value of the new array and size
        array  = newBlock   ;
        length = size       ;

    } else if(!size) {

        if(array) delete[] array;

        array   = 0;
        length  = 0;

    }

}

/*!
 * Attempts to access the element at the specified index.
 * If the index is greater than or equal to the length (out of bounds)
 * this will throw a ProcessJRuntime::Array::IndexOutOfBoundsException.
 *
 * \param index The index to access
 * \return Reference to a Type
 */

template<typename Type>
Type& ProcessJRuntime::Array<Type>::operator[](ProcessJRuntime::UInteger32 index) {

    // Unsigned number, if we're out of bounds, throw this
    if(index >= length)
        throw ProcessJRuntime::Array<Type>::IndexOutOfBoundsException();

    // Otherwise, return it
    return array[index];

}

/*!
 * Attempts to access the element at the specified index.
 * If the index is greater than or equal to the length (out of bounds)
 * this will throw a ProcessJRuntime::Array::IndexOutOfBoundsException.
 *
 * \param index The index to access
 * \return Reference to an immutable Type
 */

template<typename Type>
const Type& ProcessJRuntime::Array<Type>::operator[](ProcessJRuntime::UInteger32 index) const {

    // Unsigned number, if we're out of bounds, throw this
    if(index >= length)
        throw ProcessJRuntime::Array<Type>::IndexOutOfBoundsException();

    // Otherwise, return it
    return array[index];

}

#endif
