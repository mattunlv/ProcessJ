/*!
 * ProcessJRuntime::ChannelType declaration
 *
 * \author Alexander C. Thomason
 * \author Carlos L. Cuenca
 * \date 03/13/2022
 * \version 1.0.0
 */

#ifndef UNLV_PROCESS_J_CHANNEL_TYPE_HPP
#define UNLV_PROCESS_J_CHANNEL_TYPE_HPP

namespace ProcessJRuntime {

    enum pj_channel_types {

        NONE        ,
        ONE2ONE     ,
        ONE2MANY    ,
        MANY2ONE    ,
        MANY2MANY

    };

    class pj_channel_type;

}

class ProcessJRuntime::pj_channel_type {
    public:
        pj_channel_type()
        : type(pj_channel_types::NONE)
        {

        }

        pj_channel_type(pj_channel_types t)
        : type(t)
        {
            switch(t)
            {
                case pj_channel_types::ONE2ONE:
                type_str = "one-to-one channel for use by one writer and one reader";
                break;
                case pj_channel_types::ONE2MANY:
                type_str = "one-to-many channel for use by one writer and many readers";
                break;
                case pj_channel_types::MANY2ONE:
                type_str = "many-to-one channel for use by many writers and one reader";
                break;
                case pj_channel_types::MANY2MANY:
                type_str = "many-to-many channel for use by many writers and many readers";
                break;
                default:
                type_str = "bad channel type";
                break;
            }
        }

        virtual ~pj_channel_type()
        {

        }

        pj_channel_types get_type()
        {
            return type;
        }

        std::string get_type_string()
        {
            return type_str;
        }

        friend std::ostream& operator<<(std::ostream& o, pj_channel_type& t)
        {
            o << t.type_str;
            return o;
        }

    private:
        pj_channel_types type;
        std::string type_str = "";
};


#endif
