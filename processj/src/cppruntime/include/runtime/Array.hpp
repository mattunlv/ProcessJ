/*!
 * ProcessJRuntime::Array declaration
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
    class pj_array;

    template<typename Type>
    class pj_md_array;

}

template<typename T>
class pj_array {

    public:
        int32_t length;

        pj_array()
        {
            m_array = nullptr;
        }

        pj_array(int32_t length)
        : length(length)
        {
           m_array = new T[length];
        }

        pj_array(T* data, T* data_end)
        :length(data_end - data)
        {
            m_array = new T[length];
            T* iter = data;
            std::size_t i = 0;
            while(iter != data_end)
            {
               m_array[i++] = (*iter++);
            }
        }

        pj_array(std::initializer_list<T> values)
        :length(values.size())
        {
            m_array = new T[length];

            std::copy(values.begin(), values.end(), m_array);
        }

        ~pj_array()
        {
            if(m_array)
            {
                delete[] m_array;
                m_array = nullptr;
            }
        }

        T& operator[](int32_t idx)
        {
            if(idx > length)
            {
                std::ostringstream message;
                message << "Invalid Argument: index "
                        << idx << " is out of bounds (size is "
                        << length << ")."
                        << std::endl;
                throw std::invalid_argument(message.str());
            }

            return m_array[idx];
        }

        const T& operator[](int32_t idx) const
        {
            if(idx > length)
            {
                std::ostringstream message;
                message << "Invalid Argument: index "
                        << idx << " is out of bounds (size is "
                        << length << ")."
                        << std::endl;
                throw std::invalid_argument(message.str());
            }

            return m_array[idx];
        }


    private:
        T* m_array;
    };

    template<class T>
    class pj_md_array
    {
    public:
        int32_t length;

        pj_md_array()
        {
            m_array = nullptr;
        }

        pj_md_array(std::initializer_list<T> values)
        :length(values.size())
        {
            m_array = new T[length];

            std::copy(values.begin(), values.end(), m_array);
        }

        pj_md_array(int32_t length)
        : length(length)
        {
            m_array = new T[length];
        }

        ~pj_md_array()
        {
            if(m_array)
            {
                for(int32_t i = 0; i < length; ++i)
                {
                    delete m_array[i];
                }
                delete[] m_array;
                m_array = nullptr;
            }
        }

        T& operator[](int32_t idx)
        {
            if(idx > length)
            {
                std::ostringstream message;
                message << "Invalid Argument: index "
                        << idx << " is out of bounds (size is "
                        << length << ")."
                        << std::endl;
                throw std::invalid_argument(message.str());
            }

            return m_array[idx];
        }

        const T& operator[](int32_t idx) const
        {
            if(idx > length)
            {
                std::ostringstream message;
                message << "Invalid Argument: index "
                        << idx << " is out of bounds (size is "
                        << length << ")."
                        << std::endl;
                throw std::invalid_argument(message.str());
            }

            return m_array[idx];
        }


    private:
        T* m_array;
    };


#endif
