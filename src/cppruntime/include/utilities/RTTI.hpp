/*!
 * RTTI Implementation.
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/12/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_RTTI_HPP
#define UNLV_PROCESS_J_RTTI_HPP

namespace ProcessJUtilities {

    template<typename Type, typename UType>
    static bool instance_of(UType);

}

template <typename T, typename U>
static bool instance_of(U u) {

    if(std::is_class<U>::value)
        return std::is_base_of<T, U>::value;

    return std::is_same<T, U>::value;
}

#endif
