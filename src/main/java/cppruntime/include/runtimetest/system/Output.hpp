/*!
 * \brief Basic output class.
 *
 * Takes in a ProcessJSystem::OutputStream instance an provides
 * helper methods to manipulate the stream
 *
 * \author Carlos L. Cuenca
 * \version 0.1.0
 * \date 10/18/2021
 */

#ifndef UNLV_PROCESS_J_SYSTEM_OUTPUT_HPP
#define UNLV_PROCESS_J_SYSTEM_OUTPUT_HPP

namespace ProcessJSystem {

    template<typename OutputType, typename BufferSize>
    class Output;

}

template<typename OutputType, typename BufferSize>
class ProcessJSystem::Output {

    /// ---------------
    /// Private Members

private:

    ProcessJSystem::OutputStream* output; /*< The ProcessJSystem::OutputStream instance that is handled */

    /// --------------
    /// Public Members

public:

    /*!
     * Primary Constructor, initializes the ProcessJSystem::Output
     * with standard in.
     */

    Output();

    /*!
     * Secondary Constructor, initializes the ProcessJSystem::Output
     * instance with the given output stream
     * \param outputStream the ProcessJSystem::OutputStream to handle
     */

    Output(ProcessJSystem::OutputStream&);

    /*!
     * Copy Constructor, initializes the ProcessJSystem::Output
     * as a copy of the given ProcessJSystem::Output instance
     * \param copy The ProcessJSystem::Output instance to copy
     */

    Output(const ProcessJSystem::Output<OutputType, BufferSize>&);

    /*!
     * Destructor, tears down any data
     */

    ~Output();

    /*!
     * Inserts a single character to the output
     * \param single the single character to insert
     */

    const Output<OutputType, BufferSize>& insert(const OutputType&) const;

    /*!
     * Inserts a null terminated string to the output
     * \param string the c-style string to insert
     */

    const Output<OutputType, BufferSize>& insert(const OutputType*) const;

    /*!
     * Inserts a string to the output
     * \param string the  string to insert
     */

    const Output<OutputType, BufferSize>& insert(const ProcessJSystem::String&) const;

    /*!
     * Moves the terminal cursor position to the specified
     * x and y coordinates
     * \param x The desired x coordinate
     * \param y the desired y coordinate
     */

    void move(ProcessJSystem::Position, ProcessJSystem::Position) const;

    /*!
     * Resizes the terminal with the given dimensions
     * \param width The desired width
     * \param height the desired height
     */

    void resize(ProcessJSystem::Integer32, ProcessJSystem::Integer32) const;

    /// --------------------
    /// Overloaded Operators

    /*!
     * Overloaded stream insertion operator. Inserts a single
     * character to the stream
     * \param output The output to insert to
     * \param data the data to insert
     */

    friend Output<OutputType, BufferSize>& operator<< (Output<OutputType, BufferSize>& output, OutputType& data) {

        output.insert(data);

        return output;

    }

    /*!
     * Overloaded stream insertion operator. Inserts a single
     * character to the stream
     * \param output The output to insert to
     * \param data the data to insert
     */

    friend Output<OutputType, BufferSize>& operator<< (Output<OutputType, BufferSize>& output, const OutputType& data) {

        output.insert(data);

        return output;

    }

    /*!
     * Overloaded stream insertion operator. Inserts a single
     * character to the stream
     * \param output The output to insert to
     * \param data the data to insert
     */

    friend const Output<OutputType, BufferSize>& operator<< (const Output<OutputType, BufferSize>& output, OutputType& data) {

        output.insert(data);

        return output;

    }

    /*!
     * Overloaded stream insertion operator. Inserts a single
     * character to the stream
     * \param output The output to insert to
     * \param data the data to insert
     */

    friend const Output<OutputType, BufferSize>& operator<< (const Output<OutputType, BufferSize>& output, const OutputType& data) {

        output.insert(data);

        return output;

    }

    /*!
     * Overloaded stream insertion operator. Inserts a single
     * character to the stream
     * \param output The output to insert to
     * \param data the data to insert
     */

    friend Output<OutputType, BufferSize>& operator<< (Output<OutputType, BufferSize>& output, OutputType* data) {

        output.insert(data);

        return output;

    }

    /*!
     * Overloaded stream insertion operator. Inserts a single
     * character to the stream
     * \param output The output to insert to
     * \param data the data to insert
     */

    friend Output<OutputType, BufferSize>& operator<< (Output<OutputType, BufferSize>& output, const OutputType* data) {

        output.insert(data);

        return output;

    }

    /*!
     * Overloaded stream insertion operator. Inserts a single
     * character to the stream
     * \param output The output to insert to
     * \param data the data to insert
     */

    friend const Output<OutputType, BufferSize>& operator<< (const Output<OutputType, BufferSize>& output, OutputType* data) {

        output.insert(data);

        return output;

    }

    /*!
     * Overloaded stream insertion operator. Inserts a single
     * character to the stream
     * \param output The output to insert to
     * \param data the data to insert
     */

    friend const Output<OutputType, BufferSize>& operator<< (const Output<OutputType, BufferSize>& output, const OutputType* data) {

        output.insert(data);

        return output;

    }

    /*!
     * Overloaded stream insertion operator. Inserts buffersize
     * characters from the output stream and assigns the value
     * to the given array of output type
     * \param output The output to insert from
     * \param data the data to write into
     */

    friend Output<OutputType, BufferSize>& operator<<(Output<OutputType, BufferSize>& output, ProcessJSystem::String& data) {

        output.insert(data);

        return output;

    }

    /*!
     * Overloaded stream insertion operator. Inserts buffersize
     * characters from the output stream and assigns the value
     * to the given array of output type
     * \param output The output to insert from
     * \param data the data to write into
     */

    friend Output<OutputType, BufferSize>& operator<<(Output<OutputType, BufferSize>& output, const ProcessJSystem::String& data) {

        output.insert(data);

        return output;

    }

    /*!
     * Overloaded stream insertion operator. Inserts buffersize
     * characters from the output stream and assigns the value
     * to the given array of output type
     * \param output The output to insert from
     * \param data the data to write into
     */

    friend const Output<OutputType, BufferSize>& operator<<(const Output<OutputType, BufferSize>& output, ProcessJSystem::String& data) {

        output.insert(data);

        return output;

    }

    /*!
     * Overloaded stream insertion operator. Inserts buffersize
     * characters from the output stream and assigns the value
     * to the given array of output type
     * \param output The output to insert from
     * \param data the data to write into
     */

    friend const Output<OutputType, BufferSize>& operator<<(const Output<OutputType, BufferSize>& output, const ProcessJSystem::String& data) {

        output.insert(data);

        return output;

    }

};

template<typename OutputType, typename BufferSize>
ProcessJSystem::Output<OutputType, BufferSize>::Output():
    output(&std::cout) { /* Empty */ }

template<typename OutputType, typename BufferSize>
ProcessJSystem::Output<OutputType, BufferSize>::Output(ProcessJSystem::OutputStream& outputStream):
    output(&outputStream) { /* Empty */ }

template<typename OutputType, typename BufferSize>
ProcessJSystem::Output<OutputType, BufferSize>::Output(const ProcessJSystem::Output<OutputType, BufferSize>& copy):
    output(copy.output) { /* Empty */ }

template<typename OutputType, typename BufferSize>
ProcessJSystem::Output<OutputType, BufferSize>::~Output<OutputType, BufferSize>() {

    this->output = 0;

}

template<typename OutputType, typename BufferSize>
const ProcessJSystem::Output<OutputType, BufferSize>& ProcessJSystem::Output<OutputType, BufferSize>::insert(const OutputType& single) const {

    if(this->output) {

        (*this->output) << single;
        //this->output->write(&single, 1);

    }

    return *this;

}

template<typename OutputType, typename BufferSize>
const ProcessJSystem::Output<OutputType, BufferSize>& ProcessJSystem::Output<OutputType, BufferSize>::insert(const OutputType* string) const {

    if(this->output) {

        //char buffer[256];
        //va_list args;
        //va_start (args, string);
        //vsprintf (buffer, string, args);
        //perror (buffer);
        //va_end (args);

        //BufferSize index = 0;
        (*this->output << string);
        //while(string[index] != '\0') {
          //  this->output->write((string + index++), 1);
        //}

    }

    return *this;

}

template<typename OutputType, typename BufferSize>
const ProcessJSystem::Output<OutputType, BufferSize>& ProcessJSystem::Output<OutputType, BufferSize>::insert(const ProcessJSystem::String& string) const {

    (*this->output) << string;

    //if(this->output) {
        //this->output->write(string.c_str(), string.size());

    return *this;

}

template<typename OutputType, typename BufferSize>
void ProcessJSystem::Output<OutputType, BufferSize>::move(ProcessJSystem::Position x, ProcessJSystem::Position y) const {

    ProcessJSystem::String command = ("\033[" + std::to_string(x) + ";" + std::to_string(y) + "H");

    if(this->output) {

        (*this->output) << command;

        //this->output->write(command.c_str(), command.size());

    }

}

template<typename OutputType, typename BufferSize>
void ProcessJSystem::Output<OutputType, BufferSize>::resize(ProcessJSystem::Integer32 width, ProcessJSystem::Integer32 height) const {

    ProcessJSystem::String command = ("\033[8;" + std::to_string(height) + ";" + std::to_string(width) + "t");

    if(this->output) {

        (*this->output) << command;

        //this->output->write(command.c_str(), command.size());

    }

}

#endif
