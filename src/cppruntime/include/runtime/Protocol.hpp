/*!
 * ProcessJRuntime::Protocol declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_PROTOCOL_HPP
#define UNLV_PROCESS_J_PROTOCOL_HPP

namespace ProcessJRuntime {

    // Every protocol case inherits from this struct
    struct pj_protocol_case {
    public:
        int tag;
    };

}

#endif
